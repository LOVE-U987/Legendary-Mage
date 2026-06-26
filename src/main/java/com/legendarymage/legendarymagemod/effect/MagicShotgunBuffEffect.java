package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.Config;
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
 * 效果属性（随Buff等级提升，数值均从配置读取）：
 * - 法术强度减少（负面效果，每级从配置读取）
 * - 法术吟唱缩减（正面效果，每级从配置读取）
 * - 近战伤害增加（正面效果，每级从配置读取）
 * - 最大法力值减少（负面效果，固定-30%，所有级别相同）
 *
 * 特殊机制：
 * - Buff持续期间，所有法术在吟唱结束后不会主动释放
 * - 当玩家使用近战武器攻击时，立刻释放注入的法术
 * - 注入的法术只能有一个，新的会覆盖旧的
 * - 法术释放后会被消耗
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

    // ==================== 基础数值（每级，从配置读取） ====================

    /**
     * 法术强度减少（每级，从配置读取）
     */
    public static double getSpellPowerReductionPerLevel() {
        return Config.MAGIC_SHOTGUN_BUFF_SPELL_POWER_REDUCTION_PER_LEVEL.get();
    }

    /**
     * 法术吟唱缩减（每级，从配置读取）
     */
    public static double getCastTimeReductionPerLevel() {
        return Config.MAGIC_SHOTGUN_BUFF_CAST_TIME_REDUCTION_PER_LEVEL.get();
    }

    /**
     * 近战伤害加成（每级，从配置读取）
     */
    public static double getMeleeDamagePerLevel() {
        return Config.MAGIC_SHOTGUN_BUFF_MELEE_DAMAGE_PER_LEVEL.get();
    }

    /**
     * 最大Buff等级（从配置读取）
     */
    public static int getMaxBuffLevel() {
        return Config.MAGIC_SHOTGUN_BUFF_MAX_LEVEL.get();
    }

    /**
     * 最大法力值减少的实际值（固定-30%，所有级别）
     */
    public static final double MAX_MANA_REDUCTION = -0.30;

    /**
     * 最大法力值减少的显示值（用于EMIffect等模组显示）
     *
     * 注意：Minecraft会将属性修饰符的值乘以(amplifier + 1)，即Buff等级
     * 显示值 = 实际目标值 / 最大等级，确保满级时总计为-30%
     */
    public static double getMaxManaReductionDisplay() {
        return MAX_MANA_REDUCTION / Math.max(1, getMaxBuffLevel());
    }

    /**
     * 构造函数
     * 注意：配置驱动的属性修饰符在 FMLCommonSetupEvent 阶段集中初始化（ModEffects.initConfigModifiers），
     * 此时 Config 尚未就绪，不可在此处读取配置值。
     */
    public MagicShotgunBuffEffect() {
        super(MobEffectCategory.BENEFICIAL, EFFECT_COLOR);
        // 属性修饰符在 ModEffects.initConfigModifiers() 中延迟初始化
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
        return getSpellPowerReductionPerLevel() * buffLevel;
    }

    /**
     * 计算法术吟唱缩减值
     *
     * @param buffLevel Buff等级（1开始）
     * @return 法术吟唱缩减值（正数表示缩减）
     */
    public static double calculateCastTimeReduction(int buffLevel) {
        return getCastTimeReductionPerLevel() * buffLevel;
    }

    /**
     * 计算近战伤害加成值
     *
     * @param buffLevel Buff等级（1开始）
     * @return 近战伤害加成值
     */
    public static double calculateMeleeDamageBonus(int buffLevel) {
        return getMeleeDamagePerLevel() * buffLevel;
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
