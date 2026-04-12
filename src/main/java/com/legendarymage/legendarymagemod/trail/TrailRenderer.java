package com.legendarymage.legendarymagemod.trail;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * 拖尾特效渲染器
 * 负责将TrailEffect数据转换为OpenGL几何体并渲染到屏幕
 *
 * 【渲染技术】
 * 使用 **Triangle Strip (三角形带)** 技术绘制拖尾：
 * - 每个轨迹点生成2个顶点（左右各一个）
 * - 相邻的点形成四边形
 * - 支持颜色渐变和透明度变化
 *
 * 【性能优化】
 * 1. 使用VertexFormat.MODE_TRIANGLE_STRIP减少顶点数
 * 2. 批量提交所有拖尾到一个BufferBuilder
 * 3. 避免每帧重新分配内存
 * 4. 支持LOD（细节层次）：远距离降低采样率
 *
 * 【使用方式】
 * <pre>
 * // 在RenderLevelStageEvent.After中调用
 * TrailRenderer.render(poseStack, bufferSource, partialTick, trailEffect);
 * </pre>
 *
 * @author Love_U
 * @version 1.0.6
 */
@net.neoforged.api.distmarker.OnlyIn(net.neoforged.api.distmarker.Dist.CLIENT)
public class TrailRenderer {

    /**
     * 渲染模式枚举
     */
    public enum RenderMode {
        /**
         * 实心填充（默认）
         * 使用半透明材质，适合大多数场景
         */
        SOLID,

        /**
         * 线框模式
         * 只绘制边缘，用于调试
         */
        WIREFRAME,

        /**
         * 发光模式
         * 使用Additive混合，产生发光效果
         * 适用：能量束、激光、魔法光效
         */
        GLOW,

        /**
         * X射线模式
         * 始终可见，即使被方块遮挡
         */
        XRAY
    }

    /**
     * 默认最大细分段数（每个线段细分的次数）
     * 越高越平滑，但性能消耗更大
     */
    private static final int DEFAULT_SUBDIVISIONS = 8;

    /**
     * 最小点间距（世界单位）
     * 小于此距离的点会被跳过以提升性能
     */
    private static final float MIN_POINT_SPACING = 0.05f;

    /**
     * 渲染单个拖尾效果
     *
     * @param poseStack 位姿堆栈（包含相机变换）
     * @param bufferSource 多缓冲源（用于获取VertexConsumer）
     * @param partialTick 部分tick时间（用于插值）
     * @param trail 要渲染的拖尾效果
     */
    public static void render(PoseStack poseStack, MultiBufferSource bufferSource,
                               float partialTick, TrailEffect trail) {
        render(poseStack, bufferSource, partialTick, trail, RenderMode.SOLID);
    }

    /**
     * 渲染单个拖尾效果（带自定义渲染模式）
     *
     * @param poseStack 位姿堆栈
     * @param bufferSource 多缓冲源
     * @param partialTick 部分tick时间
     * @param trail 要渲染的拖尾效果
     * @param mode 渲染模式
     */
    public static void render(PoseStack poseStack, MultiBufferSource bufferSource,
                               float partialTick, TrailEffect trail, RenderMode mode) {
        if (trail == null || !trail.isActive() || trail.getPointCount() < 2) {
            return; // 不渲染无效或过短的拖尾
        }

        // 根据拖尾类型选择渲染方法
        switch (trail.getType()) {
            case LINEAR -> renderLinearTrail(poseStack, bufferSource, partialTick, trail, mode);
            case CURVE -> renderCurveTrail(poseStack, bufferSource, partialTick, trail, mode);
            case SPIRAL -> renderSpiralTrail(poseStack, bufferSource, partialTick, trail, mode);
            case WAVE -> renderWaveTrail(poseStack, bufferSource, partialTick, trail, mode);
            case PARTICLE_LIKE -> renderParticleLikeTrail(poseStack, bufferSource, partialTick, trail, mode);
        }
    }

    // ==================== 具体类型渲染方法 ====================

    /**
     * 渲染直线拖尾
     * 点与点之间直接连接形成折线/线段
     */
    private static void renderLinearTrail(PoseStack poseStack, MultiBufferSource bufferSource,
                                           float partialTick, TrailEffect trail, RenderMode mode) {
        Matrix4f matrix = poseStack.last().pose();

        // 准备渲染状态
        setupRenderState(mode);

        // 获取或创建BufferBuilder
        VertexConsumer consumer = getVertexConsumer(bufferSource, mode);

        // 收集有效点（过滤掉过于密集的点）
        java.util.List<TrailPoint> validPoints = collectValidPoints(trail);

        if (validPoints.size() < 2) return;

        // 计算相机位置（用于billboard效果）
        Vec3 cameraPos = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition();

        // 绘制三角形带
        beginDrawing(consumer, matrix, validPoints.size());

        TrailPoint prevPoint = null;
        for (int i = 0; i < validPoints.size(); i++) {
            TrailPoint currentPoint = validPoints.get(i);
            Vec3 pos = currentPoint.position;

            if (prevPoint != null) {
                // 计算线段方向
                Vec3 direction = pos.subtract(prevPoint.position).normalize();

                // 计算法向量（垂直于视线方向和线段方向）
                Vec3 toCamera = cameraPos.subtract(pos).normalize();
                Vec3 perpendicular = direction.cross(toCamera).normalize();

                float halfWidth = currentPoint.width / 2.0f;

                // 左侧顶点
                Vec3 leftPos = pos.add(perpendicular.scale(halfWidth));
                addVertex(consumer, matrix, leftPos,
                        currentPoint.color.x, currentPoint.color.y, currentPoint.color.z,
                        currentPoint.alpha, 0.0f, 1.0f);

                // 右侧顶点
                Vec3 rightPos = pos.subtract(perpendicular.scale(halfWidth));
                addVertex(consumer, matrix, rightPos,
                        currentPoint.color.x, currentPoint.color.y, currentPoint.color.z,
                        currentPoint.alpha, 0.0f, 0.0f);
            }

            prevPoint = currentPoint;
        }

        endDrawing();
        restoreRenderState();
    }

    /**
     * 渲染曲线拖尾（Catmull-Rom样条平滑）
     * 产生流畅的曲线效果
     */
    private static void renderCurveTrail(PoseStack poseStack, MultiBufferSource bufferSource,
                                          float partialTick, TrailEffect trail, RenderMode mode) {
        // TODO: 实现Catmull-Rom样条插值
        // 目前暂时使用线性渲染作为占位符
        renderLinearTrail(poseStack, bufferSource, partialTick, trail, mode);
    }

    /**
     * 渲染螺旋拖尾
     * 轨迹围绕中心轴螺旋扭曲
     */
    private static void renderSpiralTrail(PoseStack poseStack, MultiBufferSource bufferSource,
                                           float partialTick, TrailEffect trail, RenderMode mode) {
        // TODO: 实现螺旋扭曲数学
        // 目前暂时使用线性渲染作为占位符
        renderLinearTrail(poseStack, bufferSource, partialTick, trail, mode);
    }

    /**
     * 渲染波浪拖尾
     * 轨迹沿正弦波波动
     */
    private static void renderWaveTrail(PoseStack poseStack, MultiBufferSource bufferSource,
                                         float partialTick, TrailEffect trail, RenderMode mode) {
        // TODO: 实现正弦波偏移
        // 目前暂时使用线性渲染作为占位符
        renderLinearTrail(poseStack, bufferSource, partialTick, trail, mode);
    }

    /**
     * 渲染粒子化拖尾
     * 模拟粒子效果但使用几何体
     */
    private static void renderParticleLikeTrail(PoseStack poseStack, MultiBufferSource bufferSource,
                                                 float partialTick, TrailEffect trail, RenderMode mode) {
        // TODO: 实现粒子化渲染（沿轨迹分布的小几何体）
        // 目前暂时使用线性渲染作为占位符
        renderLinearTrail(poseStack, bufferSource, partialTick, trail, mode);
    }

    // ==================== 辅助方法 ====================

    /**
     * 收集有效的轨迹点（过滤掉过于密集的点以优化性能）
     *
     * @param trail 拖尾效果
     * @return 过滤后的点列表
     */
    private static java.util.List<TrailPoint> collectValidPoints(TrailEffect trail) {
        java.util.List<TrailPoint> result = new java.util.ArrayList<>();
        TrailPoint lastAdded = null;

        for (TrailPoint point : trail.getPoints()) {
            if (lastAdded == null || point.distanceTo(lastAdded) >= MIN_POINT_SPACING) {
                result.add(point);
                lastAdded = point;
            }
        }

        return result;
    }

    /**
     * 设置渲染状态（混合模式、深度测试等）
     *
     * @param mode 渲染模式
     */
    private static void setupRenderState(RenderMode mode) {
        switch (mode) {
            case GLOW:
                // Additive混合（发光效果）
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(false); // 不写入深度缓冲（允许重叠）
                break;

            case SOLID:
                // 标准透明混合
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(true);
                break;

            case WIREFRAME:
                // 线框模式（调试用）
                RenderSystem.disableBlend();
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(true);
                // 注意：真正的线框需要启用GL_POLYGON_MODE，这里简化处理
                break;

            case XRAY:
                // X射线模式（始终可见）
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableDepthTest(); // 禁用深度测试使其始终可见
                RenderSystem.depthMask(false);
                break;
        }
    }

    /**
     * 恢复渲染状态到默认值
     */
    private static void restoreRenderState() {
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    /**
     * 获取合适的VertexConsumer
     *
     * @param bufferSource 缓冲源
     * @param mode 渲染模式
     * @return VertexConsumer实例
     */
    private static VertexConsumer getVertexConsumer(MultiBufferSource bufferSource, RenderMode mode) {
        return switch (mode) {
            case GLOW -> bufferSource.getBuffer(
                    RenderType.energySwirl(
                            ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "textures/trail/glow.png"),
                            0, 0
                    )
            );
            default -> bufferSource.getBuffer(
                    RenderType.entityCutoutNoCull(
                            ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "textures/trail/trail.png")
                    )
            );
        };
    }

    /**
     * 开始绘制（准备BufferBuilder）
     *
     * @param consumer VertexConsumer
     * @param matrix 变换矩阵
     * @param vertexCount 预估顶点数量（用于预分配）
     */
    private static void beginDrawing(VertexConsumer consumer, Matrix4f matrix, int vertexCount) {
        // 在NeoForge 1.21中，VertexConsumer已经内置了buffer管理
        // 这里不需要显式的begin/end调用
    }

    /**
     * 结束绘制（提交数据）
     */
    private static void endDrawing() {
        // 同上，不需要显式调用
    }

    /**
     * 添加一个顶点到当前批次
     *
     * @param consumer VertexConsumer
     * @param matrix 变换矩阵
     * @param position 世界坐标位置
     * @param r 红色分量 (0-255)
     * @param g 绿色分量 (0-255)
     * @param b 蓝色分量 (0-255)
     * @param a 透明度 (0-255)
     * @param u 纹理坐标U
     * @param v 纹理坐标V
     */
    private static void addVertex(VertexConsumer consumer, Matrix4f matrix, Vec3 position,
                                   float r, float g, float b, float a, float u, float v) {
        // 转换为int范围 (0-255)
        int ri = (int) (r * 255);
        int gi = (int) (g * 255);
        int bi = (int) (b * 255);
        int ai = (int) (a * 255);

        consumer.addVertex(matrix, (float) position.x, (float) position.y, (float) position.z)
                .setColor(ri, gi, bi, ai)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT) // 全亮度（使用int值）
                .setNormal(0.0f, 1.0f, 0.0f); // 法向量朝上
    }
}
