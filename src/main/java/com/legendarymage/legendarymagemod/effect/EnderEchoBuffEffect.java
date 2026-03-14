package com.legendarymage.legendarymagemod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 终末回响Buff效果
 * 末影与任意元素反应给予施法者的Buff
 * 效果：+末影加成/2的法术抗性，+末影加成/3的法术强度
 * 
 * 注意：这个Buff的属性加成是动态的，基于施法者的末影法术强度
 * 属性修饰符完全由 ElementReactionEffects.handleEnderAny() 动态添加
 * 不在构造函数中添加任何属性修饰符，以避免与手动添加的修饰符冲突
 * 
 * @author Love_U
 * @version 0.0.6
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
     * 构造函数
     * 不添加任何属性修饰符，完全由ElementReactionEffects动态管理
     */
    public EnderEchoBuffEffect() {
        super(MobEffectCategory.BENEFICIAL, EFFECT_COLOR);
        // 不在构造函数中添加属性修饰符
        // 因为属性值是动态的，基于施法者的末影法术强度
        // 且完全由 ElementReactionEffects.handleEnderAny() 手动添加
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
