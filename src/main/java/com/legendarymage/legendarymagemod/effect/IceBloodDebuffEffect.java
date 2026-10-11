package com.legendarymage.legendarymagemod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 冰封血脉 Debuff 效果
 * 冰系与血系元素反应给予目标的 Debuff
 * 效果：持续期间目标受到的治疗减半（由 ElementReactionEvents 的 LivingHealEvent 监听处理）
 *
 * @author Love_U
 * @version 1.0.0
 */
public class IceBloodDebuffEffect extends MobEffect {

    /**
     * 效果 ID
     */
    public static final String EFFECT_ID = "ice_blood_debuff";

    /**
     * 效果颜色（冰蓝色）
     */
    private static final int EFFECT_COLOR = 0x4A90D9;

    /**
     * 构造函数
     */
    public IceBloodDebuffEffect() {
        super(MobEffectCategory.HARMFUL, EFFECT_COLOR);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // 治疗抑制由 ElementReactionEvents 的 LivingHealEvent 监听处理，不需要每 tick 更新
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
