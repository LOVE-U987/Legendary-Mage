package com.legendarymage.legendarymagemod.client.renderer;

import com.legendarymage.legendarymagemod.client.IceSculptureTextureManager;
import com.legendarymage.legendarymagemod.spell.LivingIceSculptureEntity;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 活体冰雕生物渲染器
 * 使用细体玩家模型渲染冰雕生物，支持彩蛋纹理切换
 * 
 * @author Love_U
 * @version 3.0.0
 */
public class IceSculptureRenderer extends LivingEntityRenderer<LivingIceSculptureEntity, PlayerModel<LivingIceSculptureEntity>> {

    /**
     * 构造函数
     * 使用细体玩家模型（slim model）
     * 
     * @param context 渲染上下文
     */
    public IceSculptureRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true), 0.5f);
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
