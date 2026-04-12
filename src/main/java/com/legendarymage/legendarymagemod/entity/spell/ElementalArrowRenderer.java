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

/**
 * 元素箭投射物渲染器
 * 渲染冰、火、雷三种元素箭
 * 
 * @author Love_U
 * @version 1.0.6
 */
public class ElementalArrowRenderer extends EntityRenderer<ElementalArrowProjectile> {

    /**
     * 冰箭纹理
     */
    private static final ResourceLocation ICE_ARROW_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, "textures/entity/elemental_arrow_ice.png");

    /**
     * 火箭纹理
     */
    private static final ResourceLocation FIRE_ARROW_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, "textures/entity/elemental_arrow_fire.png");

    /**
     * 雷箭纹理
     */
    private static final ResourceLocation LIGHTNING_ARROW_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, "textures/entity/elemental_arrow_lightning.png");

    /**
     * 构造函数
     * 
     * @param context 渲染上下文
     */
    public ElementalArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    /**
     * 获取纹理位置
     * 
     * @param entity 实体
     * @return 纹理资源位置
     */
    @Override
    public ResourceLocation getTextureLocation(ElementalArrowProjectile entity) {
        return switch (entity.getArrowType()) {
            case ICE -> ICE_ARROW_TEXTURE;
            case FIRE -> FIRE_ARROW_TEXTURE;
            case LIGHTNING -> LIGHTNING_ARROW_TEXTURE;
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
    public void render(ElementalArrowProjectile entity, float entityYaw, float partialTicks, 
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        
        poseStack.pushPose();
        
        // 获取实体的运动方向用于旋转
        Vec3 motion = entity.getDeltaMovement();
        
        // 计算旋转角度，使箭矢朝向运动方向
        float xRot = -((float) (Mth.atan2(motion.horizontalDistance(), motion.y) * (double) (180F / (float) Math.PI)) - 90.0F);
        float yRot = -((float) (Mth.atan2(motion.z, motion.x) * (double) (180F / (float) Math.PI)) + 90.0F);
        
        // 应用旋转
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
        
        // 缩放 - 调整为与碰撞箱匹配
        float scale = 0.5f;
        poseStack.scale(scale, scale, scale);
        
        // 获取对应元素类型的纹理
        ResourceLocation texture = getTextureLocation(entity);
        
        // 创建渲染类型和顶点消费者
        RenderType renderType = RenderType.entityCutoutNoCull(texture);
        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
        
        // 渲染简单的箭头形状（使用两个交叉的平面）
        renderArrow(poseStack, vertexConsumer, packedLight);
        
        poseStack.popPose();
        
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    /**
     * 渲染箭头形状
     * 
     * @param poseStack       姿态栈
     * @param vertexConsumer  顶点消费者
     * @param packedLight     光照值
     */
    private void renderArrow(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight) {
        // 渲染一个十字交叉的平面来模拟箭矢
        // 第一个平面（X方向）
        renderPlane(poseStack, vertexConsumer, packedLight, 0);
        
        // 第二个平面（旋转90度）
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        renderPlane(poseStack, vertexConsumer, packedLight, 0);
        poseStack.popPose();
    }

    /**
     * 渲染单个平面
     * 
     * @param poseStack       姿态栈
     * @param vertexConsumer  顶点消费者
     * @param packedLight     光照值
     * @param overlay         覆盖层
     */
    private void renderPlane(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int overlay) {
        float minU = 0.0F;
        float maxU = 1.0F;
        float minV = 0.0F;
        float maxV = 1.0F;
        
        // 平面大小
        float size = 0.5F;
        
        // 获取当前的变换矩阵
        var matrix = poseStack.last().pose();
        var normalMatrix = poseStack.last();
        
        // 渲染四边形（两个三角形）
        // 顶点1：左下
        vertexConsumer.addVertex(matrix, -size, -size, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(minU, maxV)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(normalMatrix, 0.0F, 1.0F, 0.0F);
        
        // 顶点2：右下
        vertexConsumer.addVertex(matrix, size, -size, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(maxU, maxV)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(normalMatrix, 0.0F, 1.0F, 0.0F);
        
        // 顶点3：右上
        vertexConsumer.addVertex(matrix, size, size, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(maxU, minV)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(normalMatrix, 0.0F, 1.0F, 0.0F);
        
        // 顶点4：左上
        vertexConsumer.addVertex(matrix, -size, size, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(minU, minV)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(normalMatrix, 0.0F, 1.0F, 0.0F);
    }

    /**
     * 获取方块光照位置
     * 
     * @param entity 实体
     * @param pos    方块位置
     * @return 光照值
     */
    @Override
    protected int getBlockLightLevel(ElementalArrowProjectile entity, BlockPos pos) {
        // 元素箭自身发光
        return 15;
    }
}
