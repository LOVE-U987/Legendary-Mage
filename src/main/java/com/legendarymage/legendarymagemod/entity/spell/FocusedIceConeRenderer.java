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
 * 聚能冰锥渲染器
 * 负责渲染聚能冰锥投射物的模型
 * 
 * @author Love_U
 * @version 0.0.1
 */
public class FocusedIceConeRenderer extends EntityRenderer<FocusedIceConeProjectile> {

    /**
     * 纹理路径
     */
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, "textures/entity/focused_ice_cone.png");

    /**
     * 模型层位置
     */
    public static final ModelLayerLocation MODEL_LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "focused_ice_cone"), "main");

    /**
     * 模型部件
     */
    private final ModelPart bone;

    /**
     * 构造函数
     * 
     * @param context 渲染上下文
     */
    public FocusedIceConeRenderer(Context context) {
        super(context);
        ModelPart modelpart = context.bakeLayer(MODEL_LAYER_LOCATION);
        this.bone = modelpart.getChild("bone");
    }

    /**
     * 创建身体层定义
     * 使用Blockbench导出的模型数据（聚能冰锥模型）
     * 
     * @return 层定义
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", 
            CubeListBuilder.create()
                .texOffs(9, 24).addBox(0.0F, -4.0F, -2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(2, 0).addBox(-1.0F, -5.0F, 2.0F, 4.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)), 
            PartPose.offset(-1.0F, 10.0F, -7.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
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
    public void render(FocusedIceConeProjectile entity, float yaw, float partialTicks, 
                       PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();

        // 根据运动方向旋转模型
        Vec3 motion = entity.getDeltaMovement();
        float xRot = -((float) (Mth.atan2(motion.horizontalDistance(), motion.y) * (double) (180F / (float) Math.PI)) - 90.0F);
        float yRot = -((float) (Mth.atan2(motion.z, motion.x) * (double) (180F / (float) Math.PI)) + 90.0F);
        
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
        poseStack.scale(1, -1, 1);

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
    public ResourceLocation getTextureLocation(FocusedIceConeProjectile entity) {
        return TEXTURE;
    }
}
