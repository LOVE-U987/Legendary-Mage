package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.LegendaryMage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 溶甲效果
 * 降低目标的护甲值，每级减少2%
 * 
 * @author Love_U
 * @version 0.0.1
 */
public class ArmorReductionEffect extends MobEffect {

    /**
     * 效果ID
     */
    public static final String EFFECT_ID = "armor_reduction";

    /**
     * 效果颜色（腐蚀绿色）
     */
    private static final int EFFECT_COLOR = 0x556B2F;

    /**
     * 构造函数
     */
    public ArmorReductionEffect() {
        super(MobEffectCategory.HARMFUL, EFFECT_COLOR);
        
        // 添加属性修改器：降低护甲值（每级2%）
        this.addAttributeModifier(
                Attributes.ARMOR,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "armor_reduction"),
                -0.02,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    /**
     * 判断是否应该应用效果更新
     * 
     * @param duration  剩余持续时间
     * @param amplifier 效果等级
     * @return 是否应该更新
     */
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // 这是一个被动效果，不需要每tick更新
        return false;
    }
}
