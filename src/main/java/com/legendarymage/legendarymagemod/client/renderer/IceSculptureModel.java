package com.legendarymage.legendarymagemod.client.renderer;

import com.legendarymage.legendarymagemod.spell.LivingIceSculptureEntity;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;

/**
 * 活体冰雕自定义玩家模型
 * 继承原版玩家模型，添加坐下姿势支持
 * 
 * @author Love_U
 * @version 1.0.0
 */
public class IceSculptureModel extends PlayerModel<LivingIceSculptureEntity> {

    /**
     * 构造函数
     * 
     * @param root     模型根部件
     * @param slim     是否为细体模型
     */
    public IceSculptureModel(ModelPart root, boolean slim) {
        super(root, slim);
    }

    /**
     * 设置模型动画
     * 当冰雕处于坐下状态时，应用骑乘/坐下姿势
     * 
     * @param entity            冰雕实体
     * @param limbSwing         肢体摆动
     * @param limbSwingAmount   肢体摆动幅度
     * @param ageInTicks        存在时间（tick）
     * @param netHeadYaw        头部水平旋转
     * @param headPitch         头部俯仰角
     */
    @Override
    public void setupAnim(LivingIceSculptureEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // 如果冰雕被命令坐下，设置 riding 标志以触发原版坐下动画
        if (entity.isOrderedToSit()) {
            this.riding = true;
        }

        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // 重置 riding 标志，避免影响其他渲染
        this.riding = false;
    }
}