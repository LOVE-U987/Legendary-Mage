package com.legendarymage.legendarymagemod.trail;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.trail.color.HSVColor;
import com.legendarymage.legendarymagemod.trail.math.BezierCurve;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.List;

/**
 * 贝塞尔拖尾渲染器
 * 使用平滑曲线渲染拖尾，支持HSV颜色和动画
 *
 * 【渲染流程】
 * 1. 获取平滑曲线点（已插值）
 * 2. 计算每点的切线和法线
 * 3. 使用Triangle Strip渲染带状几何体
 * 4. 应用HSV颜色和动画值
 *
 * 【与Perception的区别】
 * - Perception：直接连线，固定淡出
 * - 我们：贝塞尔曲线，时间轴动画，HSV渐变
 *
 * @author Love_U
 * @version 2.0.0
 */
public class BezierTrailRenderer {

    private static BezierTrailRenderer instance;

    public static BezierTrailRenderer getInstance() {
        if (instance == null) {
            instance = new BezierTrailRenderer();
        }
        return instance;
    }

    private BezierTrailRenderer() {
    }

    /**
     * 渲染所有拖尾
     */
    public void renderAll(Collection<BezierTrailEffect> trails, PoseStack poseStack,
                          MultiBufferSource bufferSource, float partialTick, Vec3 cameraPos) {
        if (trails == null || trails.isEmpty()) {
            return;
        }

        int renderedCount = 0;

        for (BezierTrailEffect trail : trails) {
            if (trail != null && trail.getSmoothPointCount() >= 2) {
                renderTrail(trail, poseStack, bufferSource, cameraPos);
                renderedCount++;
            }
        }

        if (renderedCount > 0) {
            LegendaryMage.LOGGER.debug("[贝塞尔拖尾渲染] 渲染了 {} 个拖尾", renderedCount);
        }
    }

    /**
     * 渲染单个拖尾
     */
    private void renderTrail(BezierTrailEffect trail, PoseStack poseStack,
                             MultiBufferSource bufferSource, Vec3 cameraPos) {

        List<Vec3> points = trail.getSmoothCurve();
        if (points.size() < 2) {
            return;
        }

        // 获取当前动画值
        float currentWidth = trail.getCurrentWidth();
        HSVColor currentColor = trail.getCurrentColor();

        // 如果完全透明，跳过渲染
        if (currentColor.getAlpha() <= 0.01f) {
            return;
        }

        // 使用半透明渲染
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(
                net.minecraft.resources.ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png")
        ));

        Matrix4f matrix = poseStack.last().pose();

        int pointCount = points.size();

        // 计算每点的切线和法线
        for (int i = 0; i < pointCount; i++) {
            Vec3 point = points.get(i);

            // 计算切线
            Vec3 tangent;
            if (i == 0) {
                tangent = points.get(1).subtract(point).normalize();
            } else if (i == pointCount - 1) {
                tangent = point.subtract(points.get(i - 1)).normalize();
            } else {
                Vec3 toNext = points.get(i + 1).subtract(point).normalize();
                Vec3 fromPrev = point.subtract(points.get(i - 1)).normalize();
                tangent = toNext.add(fromPrev).normalize();
            }

            // 计算法线（垂直于切线和相机方向）
            Vec3 toCamera = cameraPos.subtract(point).normalize();
            Vec3 normal = tangent.cross(toCamera).normalize();

            // 如果法线太接近零向量，使用默认上方向
            if (normal.lengthSqr() < 0.001) {
                normal = new Vec3(0, 1, 0);
            }

            // 计算进度（0-1）
            float progress = (float) i / (pointCount - 1);

            // 计算当前点的宽度和颜色
            // 起点到终点逐渐变细
            float widthAtPoint = currentWidth * (1.0f - progress * 0.5f);

            // 颜色渐变（可选：基于进度调整色相）
            HSVColor colorAtPoint = currentColor;
            if (trail.isActive()) {
                // 活跃时可以添加彩虹效果
                colorAtPoint = currentColor.shiftHue(progress * 30); // 轻微色相偏移
            }

            // 转换为RGB
            Vector3f rgb = colorAtPoint.toRGB();
            int r = (int) (rgb.x * 255);
            int g = (int) (rgb.y * 255);
            int b = (int) (rgb.z * 255);
            int a = (int) (colorAtPoint.getAlpha() * 255);

            // 计算左右顶点位置
            Vec3 leftPos = point.add(normal.scale(widthAtPoint));
            Vec3 rightPos = point.subtract(normal.scale(widthAtPoint));

            // 转换为相对相机坐标
            float lx = (float) (leftPos.x - cameraPos.x);
            float ly = (float) (leftPos.y - cameraPos.y);
            float lz = (float) (leftPos.z - cameraPos.z);

            float rx = (float) (rightPos.x - cameraPos.x);
            float ry = (float) (rightPos.y - cameraPos.y);
            float rz = (float) (rightPos.z - cameraPos.z);

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
}
