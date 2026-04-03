package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.LegendaryMage;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * 终末回响Buff效果
 * 末影与任意元素反应给予施法者的Buff
 * 效果：+末影加成/2的法术抗性，+末影加成/3的法术强度
 * 
 * EMI EFF兼容性说明：
 * 为了在EMIffect等模组中正确显示属性加成，在构造函数中添加了基础属性修饰符。
 * 这些修饰符提供基础显示值，实际效果由 ElementReactionEffects.handleEnderAny() 动态调整。
 * 
 * @author Love_U
 * @version 1.0.0
 */
public class EnderEchoBuffEffect extends MobEffect {

    /**
     * 效果ID
     */
    public static final String EFFECT_ID = "ender_echo_buff";

    /**
     * 效果颜色（暗紫色）
     */
    private static final int EFFECT_COLOR = 0x9932CC;

    /**
     * 法术强度加成比例（末影强度的1/3）
     */
    public static final double SPELL_POWER_RATIO = 0.333;

    /**
     * 法术抗性加成比例（末影强度的1/2）
     */
    public static final double SPELL_RESIST_RATIO = 0.5;

    /**
     * 基础法术强度加成显示值（用于EMIffect显示）
     * 假设基础末影强度为1.5（50%加成），则法术强度加成 = 0.5 * 0.333 = 0.166（16.6%）
     */
    public static final double BASE_SPELL_POWER_BONUS_DISPLAY = 0.166;

    /**
     * 基础法术抗性加成显示值（用于EMIffect显示）
     * 假设基础末影强度为1.5（50%加成），则法术抗性加成 = 0.5 * 0.5 = 0.25（25%）
     */
    public static final double BASE_SPELL_RESIST_BONUS_DISPLAY = 0.25;

    /**
     * 构造函数
     * 添加基础属性修饰符用于EMIffect等模组显示
     * 实际效果由 ElementReactionEffects.handleEnderAny() 动态调整
     */
    public EnderEchoBuffEffect() {
        super(MobEffectCategory.BENEFICIAL, EFFECT_COLOR);
        
        // 添加法术强度修饰符（用于EMIffect显示）
        this.addAttributeModifier(
                AttributeRegistry.SPELL_POWER,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "ender_echo_spell_power"),
                BASE_SPELL_POWER_BONUS_DISPLAY,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
        
        // 添加法术抗性修饰符（用于EMIffect显示）
        this.addAttributeModifier(
                AttributeRegistry.SPELL_RESIST,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "ender_echo_spell_resist"),
                BASE_SPELL_RESIST_BONUS_DISPLAY,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // 效果通过属性修饰符应用，不需要每tick更新
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false; // 不需要每tick更新
    }

    /**
     * 计算法术抗性加成
     * 
     * @param enderPower 末影法术强度
     * @return 法术抗性加成值
     */
    public static double calculateMagicResistBonus(double enderPower) {
        return (enderPower - 1.0) * SPELL_RESIST_RATIO; // 减去基础值1.0
    }

    /**
     * 计算法术强度加成
     * 
     * @param enderPower 末影法术强度
     * @return 法术强度加成值
     */
    public static double calculateSpellPowerBonus(double enderPower) {
        return (enderPower - 1.0) * SPELL_POWER_RATIO; // 减去基础值1.0
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
