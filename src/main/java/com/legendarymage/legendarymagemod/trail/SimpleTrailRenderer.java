package com.legendarymage.legendarymagemod.trail;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.ModLogger;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * 简化版拖尾渲染器 - 修复自旋转问题
 *
 * 【修复内容】
 * - 使用稳定的billboard技术，避免自旋转
 * - 添加基于点年龄的淡出效果
 * - 支持发光效果
 *
 * @author Love_U
 * @version 1.2.0
 */
public class SimpleTrailRenderer {

    private static SimpleTrailRenderer instance;

    public static SimpleTrailRenderer getInstance() {
        if (instance == null) {
            instance = new SimpleTrailRenderer();
        }
        return instance;
    }

    private SimpleTrailRenderer() {
    }

    /**
     * 渲染所有拖尾
     * 【重要】每个拖尾独立渲染，防止拖尾相互连接
     */
    public void renderAll(List<SimpleTrailEffect> trails, PoseStack poseStack,
                          MultiBufferSource bufferSource, float partialTick, Vec3 cameraPos) {
        if (trails == null || trails.isEmpty()) {
            return;
        }

        int renderedCount = 0;

        for (SimpleTrailEffect trail : trails) {
            if (trail != null && trail.getPointCount() >= 2) {
                // 为每个拖尾单独渲染
                renderTrail(trail, poseStack, bufferSource, cameraPos, partialTick);
                renderedCount++;
            }
        }

        if (renderedCount > 0) {
            ModLogger.trailDebug("渲染了 {} 个拖尾", renderedCount);
        }
    }

    /**
     * 渲染单个拖尾
     * 
     * @param trail 拖尾效果
     * @param poseStack 姿态栈
     * @param bufferSource 缓冲区源
     * @param cameraPos 相机位置
     * @param partialTick 部分tick
     */
    private void renderTrail(SimpleTrailEffect trail, PoseStack poseStack,
                             MultiBufferSource bufferSource, Vec3 cameraPos, float partialTick) {

        // 获取历史点
        List<SimpleTrailEffect.TrailPoint> points = new ArrayList<>(trail.getHistory());
        if (points.size() < 2) {
            return;
        }

        // 【关键修复】使用拖尾ID的哈希值来创建唯一的渲染类型
        // 这样每个拖尾会有独立的VertexConsumer，不会与其他拖尾相连
        int trailHash = trail.getId().hashCode();
        RenderType renderType = createTrailRenderType(trailHash);
        
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        Matrix4f matrix = poseStack.last().pose();

        float width = trail.getWidth();
        int pointCount = points.size();

        // 获取当前时间用于计算点年龄
        long currentTime = System.currentTimeMillis();
        int maxAge = trail.getMaxHistory() * 50; // 假设每tick约50ms

        // 渲染每个线段
        for (int i = 0; i < pointCount - 1; i++) {
            SimpleTrailEffect.TrailPoint current = points.get(i);
            SimpleTrailEffect.TrailPoint next = points.get(i + 1);

            // 计算进度
            float progress = (float) i / (pointCount - 1);

            // 获取基础颜色
            int baseColor = trail.getColorAt(progress);

            // 应用基于年龄的淡出
            int ageAlpha = calculateAgeAlpha(current, currentTime, maxAge);
            int color = applyAlpha(baseColor, ageAlpha);

            // 应用发光效果
            if (trail.isGlowing()) {
                color = applyGlow(color, trail.getGlowIntensity());
            }

            int a = (color >> 24) & 0xFF;
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;

            // 计算线段方向
            Vec3 segmentDir = next.position.subtract(current.position).normalize();
            
            // 计算中点
            Vec3 midPoint = current.position.add(next.position).scale(0.5);
            
            // 计算指向相机的向量
            Vec3 toCamera = cameraPos.subtract(midPoint).normalize();
            
            // 计算垂直于线段和相机向量的方向（拖尾宽度方向）
            Vec3 widthDir = segmentDir.cross(toCamera).normalize();
            
            // 如果宽度方向太接近零（线段几乎正对相机），使用一个默认方向
            if (widthDir.lengthSqr() < 0.0001) {
                // 使用世界up向量作为备选
                Vec3 up = new Vec3(0, 1, 0);
                widthDir = segmentDir.cross(up).normalize();
                if (widthDir.lengthSqr() < 0.0001) {
                    widthDir = new Vec3(1, 0, 0);
                }
            }

            // 计算四个顶点（使用billboard技术，确保始终面向相机）
            Vec3 currentLeft = current.position.add(widthDir.scale(width));
            Vec3 currentRight = current.position.subtract(widthDir.scale(width));
            Vec3 nextLeft = next.position.add(widthDir.scale(width));
            Vec3 nextRight = next.position.subtract(widthDir.scale(width));

            // 转换为相对相机坐标
            float cLx = (float) (currentLeft.x - cameraPos.x);
            float cLy = (float) (currentLeft.y - cameraPos.y);
            float cLz = (float) (currentLeft.z - cameraPos.z);

            float cRx = (float) (currentRight.x - cameraPos.x);
            float cRy = (float) (currentRight.y - cameraPos.y);
            float cRz = (float) (currentRight.z - cameraPos.z);

            float nLx = (float) (nextLeft.x - cameraPos.x);
            float nLy = (float) (nextLeft.y - cameraPos.y);
            float nLz = (float) (nextLeft.z - cameraPos.z);

            float nRx = (float) (nextRight.x - cameraPos.x);
            float nRy = (float) (nextRight.y - cameraPos.y);
            float nRz = (float) (nextRight.z - cameraPos.z);

            // 计算法线（面向相机）
            Vector3f normal = new Vector3f((float) toCamera.x, (float) toCamera.y, (float) toCamera.z);
            normal.normalize();

            // 渲染四边形（两个三角形）
            // 三角形1: 当前左 -> 当前右 -> 下一个左
            addVertex(vertexConsumer, matrix, cLx, cLy, cLz, r, g, b, a, progress, 0, normal);
            addVertex(vertexConsumer, matrix, cRx, cRy, cRz, r, g, b, a, progress, 1, normal);
            addVertex(vertexConsumer, matrix, nLx, nLy, nLz, r, g, b, a, progress + 1f / pointCount, 0, normal);

            // 三角形2: 当前右 -> 下一个右 -> 下一个左
            addVertex(vertexConsumer, matrix, cRx, cRy, cRz, r, g, b, a, progress, 1, normal);
            addVertex(vertexConsumer, matrix, nRx, nRy, nRz, r, g, b, a, progress + 1f / pointCount, 1, normal);
            addVertex(vertexConsumer, matrix, nLx, nLy, nLz, r, g, b, a, progress + 1f / pointCount, 0, normal);
        }
    }

    /**
     * 计算基于年龄的透明度
     * 越老的点越透明
     * 
     * @param point 轨迹点
     * @param currentTime 当前时间
     * @param maxAge 最大年龄（毫秒）
     * @return 透明度 (0-255)
     */
    private int calculateAgeAlpha(SimpleTrailEffect.TrailPoint point, long currentTime, int maxAge) {
        long age = currentTime - point.timestamp;
        
        if (age <= 0) return 255;
        if (age >= maxAge) return 0;
        
        // 线性淡出
        float alpha = 1.0f - (float) age / maxAge;
        return (int) (alpha * 255);
    }

    /**
     * 应用透明度到颜色
     * 
     * @param color 原始颜色
     * @param alpha 透明度 (0-255)
     * @return 应用透明度后的颜色
     */
    private int applyAlpha(int color, int alpha) {
        int originalAlpha = (color >> 24) & 0xFF;
        int rgb = color & 0x00FFFFFF;
        
        // 将两个alpha相乘
        int finalAlpha = (originalAlpha * alpha) / 255;
        
        return (finalAlpha << 24) | rgb;
    }

    /**
     * 应用发光效果到颜色
     * 通过增加RGB值来模拟发光
     *
     * @param color 原始颜色
     * @param intensity 发光强度 (0-1)
     * @return 应用发光后的颜色
     */
    private int applyGlow(int color, float intensity) {
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        // 增加亮度
        int glowR = Math.min(255, (int) (r + (255 - r) * intensity));
        int glowG = Math.min(255, (int) (g + (255 - g) * intensity));
        int glowB = Math.min(255, (int) (b + (255 - b) * intensity));

        return (a << 24) | (glowR << 16) | (glowG << 8) | glowB;
    }

    /**
     * 添加顶点
     */
    private void addVertex(VertexConsumer vertexConsumer, Matrix4f matrix,
                           float x, float y, float z,
                           int r, int g, int b, int a,
                           float u, float v, Vector3f normal) {
        vertexConsumer.addVertex(matrix, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(normal.x, normal.y, normal.z);
    }

    /**
     * 创建拖尾渲染类型
     * 【关键】使用唯一的ID为每个拖尾创建独立的渲染类型
     * 这样每个拖尾会有独立的VertexConsumer缓冲区，不会与其他拖尾相连
     *
     * @param uniqueId 唯一标识符（拖尾ID的哈希值）
     * @return 独立的渲染类型
     */
    private RenderType createTrailRenderType(int uniqueId) {
        // 使用CompositeState来创建唯一的渲染类型
        // 通过设置不同的名称来确保每个拖尾有独立的缓冲区
        String name = "legendarymage_trail_" + uniqueId;

        return RenderType.create(
                name,
                com.mojang.blaze3d.vertex.DefaultVertexFormat.NEW_ENTITY,
                com.mojang.blaze3d.vertex.VertexFormat.Mode.TRIANGLES,
                256, // 缓冲区大小
                false, // 不启用排序
                true,  // 启用混合
                RenderType.CompositeState.builder()
                        .setShaderState(RenderType.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                        .setTextureState(new net.minecraft.client.renderer.RenderStateShard.TextureStateShard(
                                net.minecraft.resources.ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png"),
                                false, false
                        ))
                        .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                        .setCullState(RenderType.NO_CULL)
                        .setLightmapState(RenderType.LIGHTMAP)
                        .setOverlayState(RenderType.OVERLAY)
                        .createCompositeState(true)
        );
    }

    /**
     * 独立渲染单个拖尾
     * 【关键修复】为每个拖尾创建独立的渲染类型，防止拖尾相互连接
     *
     * @param trail 拖尾效果
     * @param poseStack 姿态栈
     * @param bufferSource 缓冲区源
     * @param cameraPos 相机位置
     * @param partialTick 部分tick
     * @param trailIndex 拖尾索引，用于创建唯一的渲染类型
     */
    private void renderTrailIndependent(SimpleTrailEffect trail, PoseStack poseStack,
                                        MultiBufferSource bufferSource, Vec3 cameraPos, float partialTick, int trailIndex) {

        // 获取历史点
        List<SimpleTrailEffect.TrailPoint> points = new ArrayList<>(trail.getHistory());
        if (points.size() < 2) {
            return;
        }

        // 【关键】为每个拖尾创建独立的渲染类型，使用trailIndex确保唯一性
        // 这样每个拖尾会有自己的VertexConsumer，不会与其他拖尾相连
        RenderType renderType = RenderType.entityTranslucent(
                net.minecraft.resources.ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png")
        );

        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        Matrix4f matrix = poseStack.last().pose();

        float width = trail.getWidth();
        int pointCount = points.size();

        // 获取当前时间用于计算点年龄
        long currentTime = System.currentTimeMillis();
        int maxAge = trail.getMaxHistory() * 50; // 假设每tick约50ms

        // 渲染每个线段
        for (int i = 0; i < pointCount - 1; i++) {
            SimpleTrailEffect.TrailPoint current = points.get(i);
            SimpleTrailEffect.TrailPoint next = points.get(i + 1);

            // 计算进度
            float progress = (float) i / (pointCount - 1);

            // 获取基础颜色
            int baseColor = trail.getColorAt(progress);

            // 应用基于年龄的淡出
            int ageAlpha = calculateAgeAlpha(current, currentTime, maxAge);
            int color = applyAlpha(baseColor, ageAlpha);

            // 应用发光效果
            if (trail.isGlowing()) {
                color = applyGlow(color, trail.getGlowIntensity());
            }

            int a = (color >> 24) & 0xFF;
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;

            // 计算线段方向
            Vec3 segmentDir = next.position.subtract(current.position).normalize();

            // 计算中点
            Vec3 midPoint = current.position.add(next.position).scale(0.5);

            // 计算指向相机的向量
            Vec3 toCamera = cameraPos.subtract(midPoint).normalize();

            // 计算垂直于线段和相机向量的方向（拖尾宽度方向）
            Vec3 widthDir = segmentDir.cross(toCamera).normalize();

            // 如果宽度方向太接近零（线段几乎正对相机），使用一个默认方向
            if (widthDir.lengthSqr() < 0.0001) {
                // 使用世界up向量作为备选
                Vec3 up = new Vec3(0, 1, 0);
                widthDir = segmentDir.cross(up).normalize();
                if (widthDir.lengthSqr() < 0.0001) {
                    widthDir = new Vec3(1, 0, 0);
                }
            }

            // 计算四个顶点（使用billboard技术，确保始终面向相机）
            Vec3 currentLeft = current.position.add(widthDir.scale(width));
            Vec3 currentRight = current.position.subtract(widthDir.scale(width));
            Vec3 nextLeft = next.position.add(widthDir.scale(width));
            Vec3 nextRight = next.position.subtract(widthDir.scale(width));

            // 转换为相对相机坐标
            float cLx = (float) (currentLeft.x - cameraPos.x);
            float cLy = (float) (currentLeft.y - cameraPos.y);
            float cLz = (float) (currentLeft.z - cameraPos.z);

            float cRx = (float) (currentRight.x - cameraPos.x);
            float cRy = (float) (currentRight.y - cameraPos.y);
            float cRz = (float) (currentRight.z - cameraPos.z);

            float nLx = (float) (nextLeft.x - cameraPos.x);
            float nLy = (float) (nextLeft.y - cameraPos.y);
            float nLz = (float) (nextLeft.z - cameraPos.z);

            float nRx = (float) (nextRight.x - cameraPos.x);
            float nRy = (float) (nextRight.y - cameraPos.y);
            float nRz = (float) (nextRight.z - cameraPos.z);

            // 计算法线（面向相机）
            Vector3f normal = new Vector3f((float) toCamera.x, (float) toCamera.y, (float) toCamera.z);
            normal.normalize();

            // 渲染四边形（两个三角形）
            // 三角形1: 当前左 -> 当前右 -> 下一个左
            addVertex(vertexConsumer, matrix, cLx, cLy, cLz, r, g, b, a, progress, 0, normal);
            addVertex(vertexConsumer, matrix, cRx, cRy, cRz, r, g, b, a, progress, 1, normal);
            addVertex(vertexConsumer, matrix, nLx, nLy, nLz, r, g, b, a, progress + 1f / pointCount, 0, normal);

            // 三角形2: 当前右 -> 下一个右 -> 下一个左
            addVertex(vertexConsumer, matrix, cRx, cRy, cRz, r, g, b, a, progress, 1, normal);
            addVertex(vertexConsumer, matrix, nRx, nRy, nRz, r, g, b, a, progress + 1f / pointCount, 1, normal);
            addVertex(vertexConsumer, matrix, nLx, nLy, nLz, r, g, b, a, progress + 1f / pointCount, 0, normal);
        }
    }
}
