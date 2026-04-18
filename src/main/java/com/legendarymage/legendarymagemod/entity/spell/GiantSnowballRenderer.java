package com.legendarymage.legendarymagemod.entity.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import com.legendarymage.legendarymagemod.LegendaryMage;

/**
 * 巨雪球渲染器
 * 负责渲染巨雪球实体的模型
 * 
 * @author Love_U
 * @version 1.0.7
 */
public class GiantSnowballRenderer extends EntityRenderer<GiantSnowballEntity> {

    /**
     * 纹理路径
     */
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, "textures/entity/giant_snowball.png");

    /**
     * 模型层位置
     */
    public static final ModelLayerLocation MODEL_LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "giant_snowball"), "main");

    /**
     * 模型部件
     */
    private final ModelPart bone;

    /**
     * 构造函数
     * 
     * @param context 渲染上下文
     */
    public GiantSnowballRenderer(Context context) {
        super(context);
        ModelPart modelpart = context.bakeLayer(MODEL_LAYER_LOCATION);
        this.bone = modelpart.getChild("bone");
    }

    /**
     * 创建身体层定义
     * 使用Blockbench导出的模型数据（基于用户提供的巨雪球.java模型）
     * 
     * @return 层定义
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", 
            CubeListBuilder.create()
                // 底部雪块 (texOffs 0, 56) - 14x12x14
                .texOffs(0, 56).addBox(-7.0F, -7.0F, -7.0F, 14.0F, 12.0F, 14.0F, new CubeDeformation(0.0F))
                // 中部雪块 (texOffs 0, 30) - 22x4x22
                .texOffs(0, 30).addBox(-11.0F, -3.0F, -11.0F, 22.0F, 4.0F, 22.0F, new CubeDeformation(0.0F))
                // 顶部雪块 (texOffs 0, 0) - 28x2x28
                .texOffs(0, 0).addBox(-14.0F, -2.0F, -14.0F, 28.0F, 2.0F, 28.0F, new CubeDeformation(0.0F)), 
            PartPose.offset(0.0F, 10.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    /**
     * 渲染实体
     * 
     * @param entity        实体
     * @param yaw           偏航角
     * @param partialTicks  部分tick
     * @param poseStack     姿态栈
     * @param bufferSource  缓冲源
     * @param light         光照
     */
    @Override
    public void render(GiantSnowballEntity entity, float yaw, float partialTicks, 
                       PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();

        // 获取雪球大小
        float scale = entity.getScale();
        
        // 如果已发射，根据运动方向旋转
        if (entity.isLaunched()) {
            Vec3 motion = entity.getDeltaMovement();
            if (motion.lengthSqr() > 0.001) {
                float xRot = -((float) (Mth.atan2(motion.horizontalDistance(), motion.y) * (double) (180F / (float) Math.PI)) - 90.0F);
                float yRot = -((float) (Mth.atan2(motion.z, motion.x) * (double) (180F / (float) Math.PI)) + 90.0F);
                poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
                poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
            }
        } else {
            // 未发射时缓慢旋转
            float rotation = (entity.tickCount + partialTicks) * 2.0f;
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        }

        // 应用缩放 - 基础大小 * 雪球缩放值 * 5 倍放大
        float renderScale = scale * 2.5f; // 0.5 * 5 = 2.5，放大 5 倍
        poseStack.scale(renderScale, renderScale, renderScale);
        
        // 调整位置使模型居中
        poseStack.translate(0, -0.5, 0);

        // 渲染模型
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        this.bone.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();

        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    /**
     * 获取纹理位置
     * 
     * @param entity 实体
     * @return 纹理资源位置
     */
    @Override
    public ResourceLocation getTextureLocation(GiantSnowballEntity entity) {
        return TEXTURE;
    }
}
