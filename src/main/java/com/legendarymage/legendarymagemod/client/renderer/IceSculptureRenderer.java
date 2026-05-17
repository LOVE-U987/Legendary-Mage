package com.legendarymage.legendarymagemod.client.renderer;

import com.legendarymage.legendarymagemod.client.IceSculptureTextureManager;
import com.legendarymage.legendarymagemod.spell.LivingIceSculptureEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 活体冰雕生物渲染器
 * 使用自定义玩家模型渲染冰雕生物，支持彩蛋纹理切换和坐下动画
 * 
 * @author Love_U
 * @version 3.1.0
 */
public class IceSculptureRenderer extends LivingEntityRenderer<LivingIceSculptureEntity, IceSculptureModel> {

    /**
     * 坐下时的Y轴偏移量（向下平移，使模型接触地面）
     */
    private static final float SITTING_Y_OFFSET = -0.65f;

    /**
     * 构造函数
     * 使用细体玩家模型（slim model）
     * 
     * @param context 渲染上下文
     */
    public IceSculptureRenderer(EntityRendererProvider.Context context) {
        super(context, new IceSculptureModel(context.bakeLayer(ModelLayers.PLAYER_SLIM), true), 0.5f);
    }

    /**
     * 渲染实体
     * 当冰雕处于坐下状态时，向下平移模型以接触地面
     * 
     * @param entity       冰雕实体
     * @param entityYaw    实体水平旋转
     * @param partialTick  部分tick
     * @param poseStack    姿态栈
     * @param buffer       缓冲源
     * @param packedLight  光照值
     */
    @Override
    public void render(LivingIceSculptureEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // 如果冰雕处于坐下状态，向下平移模型
        if (entity.isOrderedToSit()) {
            poseStack.pushPose();
            poseStack.translate(0.0f, SITTING_Y_OFFSET, 0.0f);
            super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
            poseStack.popPose();
        } else {
            super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
        }
    }

    /**
     * 获取纹理
     * 根据实体存储的纹理索引和全局模式选择纹理
     * 
     * @param entity 冰雕生物实体
     * @return 纹理资源位置
     */
    @Override
    public ResourceLocation getTextureLocation(LivingIceSculptureEntity entity) {
        // 获取全局纹理模式
        IceSculptureTextureManager.TextureMode globalMode = IceSculptureTextureManager.getCurrentMode();
        
        // 如果是默认模式，使用默认纹理
        if (globalMode == IceSculptureTextureManager.TextureMode.DEFAULT) {
            return IceSculptureTextureManager.DEFAULT_TEXTURE;
        }
        
        // 如果是彩蛋模式，使用实体存储的纹理索引
        int textureIndex = entity.getEasterEggTextureIndex();
        return switch (textureIndex) {
            case 1 -> IceSculptureTextureManager.EASTER_EGG_TEXTURE_1;
            case 2 -> IceSculptureTextureManager.EASTER_EGG_TEXTURE_2;
            default -> IceSculptureTextureManager.DEFAULT_TEXTURE;
        };
    }
}