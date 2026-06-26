package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.Config;
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
     * 基础末影强度加成（假设基础末影强度为 1.5，即 50% 加成）
     * 用于计算 EMIffect 等模组中的显示值
     */
    private static final double BASE_ENDER_POWER_BONUS = 0.5;

    /**
     * 获取基础法术强度加成显示值（用于EMIffect显示）
     * 计算方式：基础末影加成 * 配置中的法术强度比例
     */
    public static double getBaseSpellPowerBonusDisplay() {
        return BASE_ENDER_POWER_BONUS * getSpellPowerRatio();
    }

    /**
     * 获取基础法术抗性加成显示值（用于EMIffect显示）
     * 计算方式：基础末影加成 * 配置中的法术抗性比例
     */
    public static double getBaseSpellResistBonusDisplay() {
        return BASE_ENDER_POWER_BONUS * getSpellResistRatio();
    }

    /**
     * 获取法术强度加成比例（从配置读取）
     *
     * @return 法术强度加成比例
     */
    public static double getSpellPowerRatio() {
        return Config.ENDER_ECHO_SPELL_POWER_RATIO.get();
    }

    /**
     * 获取法术抗性加成比例（从配置读取）
     *
     * @return 法术抗性加成比例
     */
    public static double getSpellResistRatio() {
        return Config.ENDER_ECHO_SPELL_RESIST_RATIO.get();
    }

    /**
     * 构造函数
     * 注意：配置驱动的属性修饰符在 FMLCommonSetupEvent 阶段集中初始化（ModEffects.initConfigModifiers），
     * 此时 Config 尚未就绪，不可在此处读取配置值。
     */
    public EnderEchoBuffEffect() {
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
     * 计算法术抗性加成
     *
     * @param enderPower 末影法术强度
     * @return 法术抗性加成值
     */
    public static double calculateMagicResistBonus(double enderPower) {
        return (enderPower - 1.0) * getSpellResistRatio(); // 减去基础值1.0
    }

    /**
     * 计算法术强度加成
     *
     * @param enderPower 末影法术强度
     * @return 法术强度加成值
     */
    public static double calculateSpellPowerBonus(double enderPower) {
        return (enderPower - 1.0) * getSpellPowerRatio(); // 减去基础值1.0
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
