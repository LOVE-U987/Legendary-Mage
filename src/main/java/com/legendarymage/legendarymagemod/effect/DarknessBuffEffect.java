package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.LegendaryMage;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * 暗夜无光效果
 * 血系元素反应给予目标的Debuff
 * 效果：减少血系法术抗性5%
 * 
 * @author Love_U
 * @version 1.0.5
 */
public class DarknessBuffEffect extends MobEffect {

    /**
     * 效果ID
     */
    public static final String EFFECT_ID = "darkness_buff";

    /**
     * 效果颜色（深红色）
     */
    private static final int EFFECT_COLOR = 0x8B0000;

    /**
     * 血系法术抗性减少（固定-5% = -0.05）
     */
    public static final double BLOOD_MAGIC_RESIST_REDUCTION = -0.05;

    /**
     * 构造函数
     * 添加属性修饰符用于EMIffect显示
     */
    public DarknessBuffEffect() {
        super(MobEffectCategory.HARMFUL, EFFECT_COLOR);
        
        // 添加血系法术抗性修饰符（固定-5%）
        this.addAttributeModifier(
                AttributeRegistry.BLOOD_MAGIC_RESIST,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "darkness_buff_blood_resist"),
                BLOOD_MAGIC_RESIST_REDUCTION,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // 效果通过属性修饰符应用，不需要每tick更新
        return true;
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

    /**
     * 获取血系法术抗性减少值
     * 
     * @return 血系法术抗性减少值（固定-5%）
     */
    public static double getBloodMagicResistReduction() {
        return BLOOD_MAGIC_RESIST_REDUCTION;
    }

    /**
     * 获取效果ID
     * 
     * @return 效果ID
     */
    public String getEffectId() {
        return EFFECT_ID;
    }
}
