package com.legendarymage.legendarymagemod.entity.spell;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * 拖尾测试投射物渲染器
 * 为TrailTestProjectile提供简单的3D渲染
 *
 * 【设计说明】
 * - 使用简单的立方体渲染，便于观察
 * - 彩虹色发光效果，非常醒目
 * - 自发光，不依赖环境光照
 *
 * @author Love_U
 * @version 1.0.6
 */
public class TrailTestProjectileRenderer extends EntityRenderer<TrailTestProjectile> {

    /**
     * 备用纹理（使用钻石块纹理）
     */
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace(
            "textures/block/diamond_block.png");

    /**
     * 构造函数
     *
     * @param context 渲染上下文
     */
    public TrailTestProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    /**
     * 获取纹理位置
     *
     * @param entity 实体
     * @return 纹理资源位置
     */
    @Override
    public ResourceLocation getTextureLocation(TrailTestProjectile entity) {
        return TEXTURE;
    }

    /**
     * 渲染实体
     * 绘制一个简单的发光立方体
     *
     * @param entity        实体
     * @param entityYaw     Y轴旋转
     * @param partialTicks  部分tick
     * @param poseStack     姿态栈
     * @param buffer        缓冲区
     * @param packedLight   光照值
     */
    @Override
    public void render(TrailTestProjectile entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        poseStack.pushPose();

        // 获取实体的运动方向用于旋转
        Vec3 motion = entity.getDeltaMovement();

        // 计算旋转角度，使模型朝向运动方向
        float xRot = -((float) (Mth.atan2(motion.horizontalDistance(), motion.y) * (double) (180F / (float) Math.PI)) - 90.0F);
        float yRot = -((float) (Mth.atan2(motion.z, motion.x) * (double) (180F / (float) Math.PI)) + 90.0F);

        // 应用旋转
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));

        // 缩放 - 使大小与碰撞箱匹配 (0.5f)
        float scale = 0.5f;
        poseStack.scale(scale, scale, scale);

        // 渲染一个简单的发光立方体
        renderGlowingCube(poseStack, buffer, entity.tickCount + partialTicks);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    /**
     * 渲染发光立方体
     *
     * @param poseStack    姿态栈
     * @param buffer       缓冲区
     * @param ageInTicks   存在时间
     */
    private void renderGlowingCube(PoseStack poseStack, MultiBufferSource buffer, float ageInTicks) {
        // 使用发光渲染类型
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(null)));

        // 计算彩虹色
        float hue = (ageInTicks % 360) / 360.0f;
        int color = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        // 立方体大小
        float s = 0.3f;

        Matrix4f matrix = poseStack.last().pose();

        // 前面 (z = s)
        addQuad(vertexConsumer, matrix, r, g, b,
                -s, -s, s,  s, -s, s,  s, s, s,  -s, s, s,
                0, 0, 1);

        // 后面 (z = -s)
        addQuad(vertexConsumer, matrix, r, g, b,
                s, -s, -s,  -s, -s, -s,  -s, s, -s,  s, s, -s,
                0, 0, -1);

        // 右面 (x = s)
        addQuad(vertexConsumer, matrix, r, g, b,
                s, -s, s,  s, -s, -s,  s, s, -s,  s, s, s,
                1, 0, 0);

        // 左面 (x = -s)
        addQuad(vertexConsumer, matrix, r, g, b,
                -s, -s, -s,  -s, -s, s,  -s, s, s,  -s, s, -s,
                -1, 0, 0);

        // 上面 (y = s)
        addQuad(vertexConsumer, matrix, r, g, b,
                -s, s, s,  s, s, s,  s, s, -s,  -s, s, -s,
                0, 1, 0);

        // 下面 (y = -s)
        addQuad(vertexConsumer, matrix, r, g, b,
                -s, -s, -s,  s, -s, -s,  s, -s, s,  -s, -s, s,
                0, -1, 0);
    }

    /**
     * 添加四边形顶点
     */
    private void addQuad(VertexConsumer vertexConsumer, Matrix4f matrix,
                         float r, float g, float b,
                         float x1, float y1, float z1,
                         float x2, float y2, float z2,
                         float x3, float y3, float z3,
                         float x4, float y4, float z4,
                         float nx, float ny, float nz) {
        // 顶点1
        vertexConsumer.addVertex(matrix, x1, y1, z1)
                .setColor(r, g, b, 1.0f)
                .setUv(0, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(nx, ny, nz);

        // 顶点2
        vertexConsumer.addVertex(matrix, x2, y2, z2)
                .setColor(r, g, b, 1.0f)
                .setUv(1, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(nx, ny, nz);

        // 顶点3
        vertexConsumer.addVertex(matrix, x3, y3, z3)
                .setColor(r, g, b, 1.0f)
                .setUv(1, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(nx, ny, nz);

        // 顶点4
        vertexConsumer.addVertex(matrix, x4, y4, z4)
                .setColor(r, g, b, 1.0f)
                .setUv(0, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(nx, ny, nz);
    }

    /**
     * 获取方块光照位置
     * 投射物自身发光
     *
     * @param entity 实体
     * @param pos    方块位置
     * @return 光照值
     */
    @Override
    protected int getBlockLightLevel(TrailTestProjectile entity, BlockPos pos) {
        return 15;
    }
}
