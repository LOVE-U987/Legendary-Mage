package com.legendarymage.legendarymagemod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 血雷 Buff 效果
 * 雷系与血系元素反应给予施法者的增益 Buff
 * 效果：持续期间，近战攻击携带者的敌人自身受到雷击反伤（类似原版荆棘）
 * 反伤逻辑由 ElementReactionEvents 的伤害监听处理（近战判定：directEntity == 攻击者）
 *
 * @author Love_U
 * @version 1.0.1
 */
public class BloodThornsEffect extends MobEffect {

    /**
     * 效果 ID
     */
    public static final String EFFECT_ID = "blood_thorns";

    /**
     * 效果颜色（暗红紫色）
     */
    private static final int EFFECT_COLOR = 0x6E1423;

    /**
     * 构造函数
     */
    public BloodThornsEffect() {
        super(MobEffectCategory.BENEFICIAL, EFFECT_COLOR);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // 反伤由 ElementReactionEvents 的伤害监听处理，不需要每 tick 更新
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // 不需要每 tick 更新
        return false;
    }

    /**
     * 获取效果 ID
     *
     * @return 效果 ID
     */
    public String getEffectId() {
        return EFFECT_ID;
    }
}
