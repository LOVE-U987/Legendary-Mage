package com.legendarymage.legendarymagemod.trail;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 几何体拖尾渲染器 - v3.0
 * 修复断开问题，实现正确的淡出时机
 *
 * 【核心改进】
 * 1. 使用Triangle Strip正确渲染连续的带状拖尾
 * 2. 淡出时机：拖尾完成后（停止添加点）整体淡出
 * 3. 飞行过程中保持完整显示，不提前淡出
 *
 * @author Love_U
 * @version 3.0.0
 */
public class GeometryTrailRenderer {

    private static GeometryTrailRenderer instance;

    public static GeometryTrailRenderer getInstance() {
        if (instance == null) {
            instance = new GeometryTrailRenderer();
        }
        return instance;
    }

    private GeometryTrailRenderer() {
    }

    /**
     * 渲染所有拖尾效果
     */
    public void renderAll(Collection<TrailEffect> trails, PoseStack poseStack,
                          MultiBufferSource bufferSource, float partialTick, Vec3 cameraPos) {
        if (trails == null || trails.isEmpty()) {
            return;
        }

        int renderedCount = 0;

        for (TrailEffect trail : trails) {
            // 只要有至少2个点就渲染（不管是否active，都要渲染淡出效果）
            if (trail != null && trail.getPointCount() >= 2) {
                renderTrail(trail, poseStack, bufferSource, cameraPos, partialTick);
                renderedCount++;
            }
        }

        if (renderedCount > 0) {
            LegendaryMage.LOGGER.debug("[几何拖尾渲染] 渲染了 {} 个拖尾效果", renderedCount);
        }
    }

    /**
     * 渲染单个拖尾
     */
    private void renderTrail(TrailEffect trail, PoseStack poseStack,
                             MultiBufferSource bufferSource, Vec3 cameraPos, float partialTick) {

        // 获取所有点
        List<TrailPoint> points = new ArrayList<>();
        for (TrailPoint point : trail.getPoints()) {
            points.add(point);
        }

        if (points.size() < 2) {
            return;
        }

        // 计算整体透明度（基于拖尾是否完成）
        float globalAlpha = calculateGlobalAlpha(trail, partialTick);
        if (globalAlpha <= 0.01f) {
            return; // 完全透明，跳过渲染
        }

        // 使用半透明渲染类型
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(
                net.minecraft.resources.ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png")
        ));

        Matrix4f matrix = poseStack.last().pose();

        // 获取颜色
        Vector3f startColor = trail.getColor();
        Vector3f endColor = trail.isColorGradientEnabled() ?
                new Vector3f(1.0f, 0.2f, 0.8f) : startColor;

        float baseWidth = trail.getWidth();

        // 计算每个点的法线方向（垂直于视线和轨迹方向）
        int pointCount = points.size();
        List<Vec3> normals = new ArrayList<>();

        for (int i = 0; i < pointCount; i++) {
            Vec3 tangent = calculateTangent(points, i);
            Vec3 toCamera = cameraPos.subtract(points.get(i).position).normalize();
            Vec3 normal = tangent.cross(toCamera).normalize();
            normals.add(normal);
        }

        // 渲染Triangle Strip
        // 每个点生成两个顶点（左右两侧）
        for (int i = 0; i < pointCount; i++) {
            TrailPoint point = points.get(i);
            Vec3 pos = point.position;
            Vec3 normal = normals.get(i);

            // 计算进度（用于颜色渐变）
            float progress = (float) i / (pointCount - 1);

            // 计算颜色（渐变）
            Vector3f color = interpolateColor(startColor, endColor, progress);

            // 计算宽度（起点粗，终点细）
            float width = baseWidth * (1.0f - progress * 0.3f);

            // 左右顶点位置
            Vec3 leftPos = pos.add(normal.scale(width));
            Vec3 rightPos = pos.subtract(normal.scale(width));

            // 转换为相对相机坐标
            float lx = (float) (leftPos.x - cameraPos.x);
            float ly = (float) (leftPos.y - cameraPos.y);
            float lz = (float) (leftPos.z - cameraPos.z);

            float rx = (float) (rightPos.x - cameraPos.x);
            float ry = (float) (rightPos.y - cameraPos.y);
            float rz = (float) (rightPos.z - cameraPos.z);

            // 颜色值
            int r = (int) (color.x * 255);
            int g = (int) (color.y * 255);
            int b = (int) (color.z * 255);
            int a = (int) (globalAlpha * 255);

            // Triangle Strip顺序：右、左
            // 右侧顶点
            vertexConsumer.addVertex(matrix, rx, ry, rz)
                    .setColor(r, g, b, a)
                    .setUv(progress, 0)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(15728880)
                    .setNormal((float) normal.x, (float) normal.y, (float) normal.z);

            // 左侧顶点
            vertexConsumer.addVertex(matrix, lx, ly, lz)
                    .setColor(r, g, b, a)
                    .setUv(progress, 1)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(15728880)
                    .setNormal((float) -normal.x, (float) -normal.y, (float) -normal.z);
        }
    }

    /**
     * 计算切线方向
     */
    private Vec3 calculateTangent(List<TrailPoint> points, int index) {
        if (index == 0) {
            // 第一个点：指向第二个点
            return points.get(1).position.subtract(points.get(0).position).normalize();
        } else if (index == points.size() - 1) {
            // 最后一个点：从前一个点指向这里
            return points.get(index).position.subtract(points.get(index - 1).position).normalize();
        } else {
            // 中间点：前后方向的平均
            Vec3 toNext = points.get(index + 1).position.subtract(points.get(index).position).normalize();
            Vec3 fromPrev = points.get(index).position.subtract(points.get(index - 1).position).normalize();
            return toNext.add(fromPrev).normalize();
        }
    }

    /**
     * 计算整体透明度
     * 关键逻辑：拖尾完成后才开始淡出
     */
    private float calculateGlobalAlpha(TrailEffect trail, float partialTick) {
        // 如果拖尾仍然活跃（还在添加新点），保持完全不透明
        if (trail.isActive()) {
            return 1.0f;
        }

        // 拖尾已完成（停止添加点），开始整体淡出
        // 基于拖尾的年龄计算透明度
        double age = trail.getTotalLifetime();
        double fadeDuration = trail.getMaxLifetime() * 0.5; // 淡出持续时间为最大生命周期的一半

        if (age <= 0 || fadeDuration <= 0) {
            return 1.0f;
        }

        // 计算淡出进度
        double fadeProgress = age / fadeDuration;

        // 返回透明度（1.0 -> 0.0）
        return (float) Math.max(0.0f, 1.0f - fadeProgress);
    }

    /**
     * 颜色插值
     */
    private Vector3f interpolateColor(Vector3f start, Vector3f end, float t) {
        return new Vector3f(
                start.x + (end.x - start.x) * t,
                start.y + (end.y - start.y) * t,
                start.z + (end.z - start.z) * t
        );
    }
}
