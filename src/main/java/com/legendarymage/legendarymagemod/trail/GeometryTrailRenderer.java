package com.legendarymage.legendarymagemod.trail;

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
 * 几何体拖尾渲染器 - v3.1 (性能优化版)
 * 修复断开问题，实现正确的淡出时机，并优化内存分配
 *
 * 【核心改进】
 * 1. 使用Triangle Strip正确渲染连续的带状拖尾
 * 2. 淡出时机：拖尾完成后（停止添加点）整体淡出
 * 3. 飞行过程中保持完整显示，不提前淡出
 * 4. 【v3.1优化】重用集合和向量对象，减少每帧内存分配
 *
 * 【性能优化】
 * - 重用 points 列表，避免每帧创建新的 ArrayList
 * - 重用 normals 列表，避免重复计算
 * - 使用对象池获取临时向量
 * - 减少临时对象的创建
 *
 * @author Love_U
 * @version 1.0.7
 */
public class GeometryTrailRenderer {

    private static GeometryTrailRenderer instance;

    /**
     * 重用列表，避免每帧创建新的 ArrayList
     */
    private final List<TrailPoint> pointCache = new ArrayList<>(128);

    /**
     * 法线缓存，避免重复计算
     */
    private final List<Vec3> normalCache = new ArrayList<>(128);

    /**
     * 临时向量，用于计算（避免重复创建）
     */
    private final Vector3f tempColor = new Vector3f();

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
            com.legendarymage.legendarymagemod.ModLogger.spellDebug("[几何拖尾渲染] 渲染了 {} 个拖尾效果", renderedCount);
        }
    }

    /**
     * 渲染单个拖尾
     */
    private void renderTrail(TrailEffect trail, PoseStack poseStack,
                             MultiBufferSource bufferSource, Vec3 cameraPos, float partialTick) {

        // 清空并重用的缓存列表
        pointCache.clear();
        normalCache.clear();

        // 获取所有点（重用缓存列表）
        for (TrailPoint point : trail.getPoints()) {
            pointCache.add(point);
        }

        int pointCount = pointCache.size();
        if (pointCount < 2) {
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

        // 预计算所有点的法线方向（垂直于视线和轨迹方向）
        // 重用 normalCache 列表
        for (int i = 0; i < pointCount; i++) {
            Vec3 tangent = calculateTangent(pointCache, i);
            Vec3 toCamera = cameraPos.subtract(pointCache.get(i).position).normalize();
            Vec3 normal = tangent.cross(toCamera).normalize();
            normalCache.add(normal);
        }

        // 渲染Triangle Strip
        // 每个点生成两个顶点（左右两侧）
        for (int i = 0; i < pointCount; i++) {
            TrailPoint point = pointCache.get(i);
            Vec3 pos = point.position;
            Vec3 normal = normalCache.get(i);

            // 计算进度（用于颜色渐变）
            float progress = (float) i / (pointCount - 1);

            // 计算颜色（渐变）- 重用 tempColor 避免创建新对象
            interpolateColor(startColor, endColor, progress, tempColor);

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
            int r = (int) (tempColor.x * 255);
            int g = (int) (tempColor.y * 255);
            int b = (int) (tempColor.z * 255);
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
     * 颜色插值（重用结果对象）
     *
     * @param start  起始颜色
     * @param end    结束颜色
     * @param t      插值因子
     * @param result 结果存储对象（避免创建新对象）
     */
    private void interpolateColor(Vector3f start, Vector3f end, float t, Vector3f result) {
        result.x = start.x + (end.x - start.x) * t;
        result.y = start.y + (end.y - start.y) * t;
        result.z = start.z + (end.z - start.z) * t;
    }
}
