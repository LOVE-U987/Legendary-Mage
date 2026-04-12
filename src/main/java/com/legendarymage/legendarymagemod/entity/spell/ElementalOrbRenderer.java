package com.legendarymage.legendarymagemod.entity.spell;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.client.model.ElementalOrbModel;
import com.legendarymage.legendarymagemod.spell.ElementalBarrageSpell;

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

/**
 * 元素球投射物渲染器
 * 使用Blockbench创建的3D立体模型渲染冰、火、雷三种元素球
 * 
 * @author Love_U
 * @version 1.0.6
 */
public class ElementalOrbRenderer extends EntityRenderer<ElementalOrbProjectile> {

    /**
     * 冰球纹理
     */
    private static final ResourceLocation ICE_ORB_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, "textures/entity/elemental_orb_ice.png");

    /**
     * 火球纹理
     */
    private static final ResourceLocation FIRE_ORB_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, "textures/entity/elemental_orb_fire.png");

    /**
     * 雷球纹理
     */
    private static final ResourceLocation LIGHTNING_ORB_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, "textures/entity/elemental_orb_lightning.png");

    /**
     * 3D模型
     */
    private final ElementalOrbModel<ElementalOrbProjectile> model;

    /**
     * 构造函数
     * 
     * @param context 渲染上下文
     */
    public ElementalOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        // 从EntityModelSet获取烘焙好的模型
        this.model = new ElementalOrbModel<>(context.bakeLayer(ElementalOrbModel.LAYER_LOCATION));
    }

    /**
     * 获取纹理位置
     * 
     * @param entity 实体
     * @return 纹理资源位置
     */
    @Override
    public ResourceLocation getTextureLocation(ElementalOrbProjectile entity) {
        return switch (entity.getOrbType()) {
            case ICE -> ICE_ORB_TEXTURE;
            case FIRE -> FIRE_ORB_TEXTURE;
            case LIGHTNING -> LIGHTNING_ORB_TEXTURE;
        };
    }

    /**
     * 渲染实体
     * 
     * @param entity        实体
     * @param entityYaw     Y轴旋转
     * @param partialTicks  部分tick
     * @param poseStack     姿态栈
     * @param buffer        缓冲区
     * @param packedLight   光照值
     */
    @Override
    public void render(ElementalOrbProjectile entity, float entityYaw, float partialTicks, 
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
        
        // 缩放 - 调整为与碰撞箱匹配 (0.3f * 2 = 0.6f)
        float scale = 0.6f;
        poseStack.scale(scale, scale, scale);
        
        // 调整Y轴位置使模型中心对齐实体中心
        // 模型原点在Y=19附近，需要向下偏移使其居中
        poseStack.translate(0.0F, -1.5F, 0.0F);
        
        // 获取对应元素类型的纹理
        ResourceLocation texture = getTextureLocation(entity);
        
        // 创建渲染类型和顶点消费者
        // 使用entityTranslucent支持Alpha透明
        RenderType renderType = RenderType.entityTranslucent(texture);
        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
        
        // 设置动画 - 使用实体的tickCount来驱动旋转
        float ageInTicks = entity.tickCount + partialTicks;
        model.setupAnim(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);
        
        // 渲染模型 (-1 表示使用纹理原始颜色)
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
        
        poseStack.popPose();
        
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    /**
     * 获取方块光照位置
     * 
     * @param entity 实体
     * @param pos    方块位置
     * @return 光照值
     */
    @Override
    protected int getBlockLightLevel(ElementalOrbProjectile entity, BlockPos pos) {
        // 元素球自身发光
        return 15;
    }
}
