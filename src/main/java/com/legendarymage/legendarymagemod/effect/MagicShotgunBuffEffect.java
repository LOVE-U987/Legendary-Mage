package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.LegendaryMage;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 魔法散弹Buff效果
 * 咒刃流派的特殊Buff，将法力注入武器以近战形式释放
 * 
 * 效果属性（随Buff等级提升）：
 * - 法术强度减少（负面效果，每级-10%）
 * - 法术吟唱缩减（正面效果，每级+10%）
 * - 近战伤害增加（正面效果，每级+5点）
 * - 最大法力值减少（负面效果，固定-30%，所有级别相同）
 * 
 * 特殊机制：
 * - Buff持续期间，所有法术在吟唱结束后不会主动释放
 * - 当玩家使用近战武器攻击时，立刻释放注入的法术
 * - 注入的法术只能有一个，新的会覆盖旧的
 * - 法术释放后会被消耗
 * 
 * Buff数值：
 * - 法术强度：每级-10%
 * - 吟唱缩减：每级+10%
 * - 近战伤害：每级+5点
 * - 最大法力值：固定-30%（所有级别）
 * 
 * @author Love_U
 * @version 1.0.0
 */
public class MagicShotgunBuffEffect extends MobEffect {

    /**
     * 效果ID
     */
    public static final String EFFECT_ID = "magic_shotgun_buff";

    /**
     * 效果颜色（深紫色，咒刃风格）
     */
    private static final int EFFECT_COLOR = 0x4A0080;

    // ==================== 基础数值（每级） ====================

    /**
     * 法术强度减少（每级-10% = -0.1）
     */
    public static final double SPELL_POWER_REDUCTION_PER_LEVEL = -0.10;

    /**
     * 法术吟唱缩减（每级+10% = +0.1）
     */
    public static final double CAST_TIME_REDUCTION_PER_LEVEL = -0.10;

    /**
     * 近战伤害加成（每级+5点）
     */
    public static final double MELEE_DAMAGE_PER_LEVEL = 5.0;

    /**
     * 最大法力值减少的实际值（固定-30% = -0.3，所有级别）
     */
    public static final double MAX_MANA_REDUCTION = -0.30;

    /**
     * 最大法力值减少的显示值（用于EMIffect等模组显示）
     * 
     * 注意：Minecraft会将属性修饰符的值乘以(amplifier + 1)，即Buff等级
     * 假设最大等级为5，为了在EMIffect中显示-30%，我们需要设置：
     * DISPLAY_VALUE = ACTUAL_VALUE / MAX_LEVEL = -0.30 / 5 = -0.06
     * 这样5级时显示：-0.06 * 5 = -0.30（即-30%）
     * 
     * 对于1级Buff，显示值为-6%，但实际效果仍为-30%
     */
    public static final double MAX_MANA_REDUCTION_DISPLAY = -0.06;

    /**
     * 最大Buff等级（用于计算显示值）
     */
    public static final int MAX_BUFF_LEVEL = 5;

    /**
     * 构造函数
     * 在注册时添加属性修饰符，利用Minecraft原生属性系统
     */
    public MagicShotgunBuffEffect() {
        super(MobEffectCategory.BENEFICIAL, EFFECT_COLOR);
        
        // 添加法术强度修饰符（每级-10%）
        this.addAttributeModifier(
                AttributeRegistry.SPELL_POWER,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "magic_shotgun_spell_power"),
                SPELL_POWER_REDUCTION_PER_LEVEL,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
        
        // 添加法术吟唱缩减修饰符（每级+10%，使用负值因为属性是"缩减"）
        this.addAttributeModifier(
                AttributeRegistry.CAST_TIME_REDUCTION,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "magic_shotgun_cast_time"),
                CAST_TIME_REDUCTION_PER_LEVEL,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
        
        // 添加近战伤害修饰符（每级+5点）
        this.addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "magic_shotgun_melee_damage"),
                MELEE_DAMAGE_PER_LEVEL,
                AttributeModifier.Operation.ADD_VALUE
        );
        
        // 添加最大法力值修饰符（用于在Buff提示中显示）
        // 使用DISPLAY值，这样在EMIffect等模组中会显示为-30%（5级时）
        this.addAttributeModifier(
                AttributeRegistry.MAX_MANA,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "magic_shotgun_max_mana"),
                MAX_MANA_REDUCTION_DISPLAY,
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
     * 计算法术强度减少值
     * 
     * @param buffLevel Buff等级（1开始）
     * @return 法术强度减少值（负数表示减少）
     */
    public static double calculateSpellPowerReduction(int buffLevel) {
        return SPELL_POWER_REDUCTION_PER_LEVEL * buffLevel;
    }

    /**
     * 计算法术吟唱缩减值
     * 
     * @param buffLevel Buff等级（1开始）
     * @return 法术吟唱缩减值（正数表示缩减）
     */
    public static double calculateCastTimeReduction(int buffLevel) {
        return -CAST_TIME_REDUCTION_PER_LEVEL * buffLevel; // 负负得正
    }

    /**
     * 计算近战伤害加成值
     * 
     * @param buffLevel Buff等级（1开始）
     * @return 近战伤害加成值
     */
    public static double calculateMeleeDamageBonus(int buffLevel) {
        return MELEE_DAMAGE_PER_LEVEL * buffLevel;
    }

    /**
     * 获取最大法力值减少值
     * 所有级别固定-30%
     * 
     * @return 最大法力值减少值（负数表示减少）
     */
    public static double getMaxManaReduction() {
        return MAX_MANA_REDUCTION;
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
