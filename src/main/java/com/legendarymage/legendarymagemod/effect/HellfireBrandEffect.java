package com.legendarymage.legendarymagemod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 狱火烙印 Debuff 效果
 * 火系与邪术系元素反应给予目标的 Debuff
 * 效果：持续期间对目标的伤害按比例吸血给施法者（由 ElementReactionEvents 的 LivingDamageEvent.Post 监听处理）
 *
 * @author Love_U
 * @version 1.0.0
 */
public class HellfireBrandEffect extends MobEffect {

    /**
     * 效果 ID
     */
    public static final String EFFECT_ID = "hellfire_brand";

    /**
     * 效果颜色（暗红色）
     */
    private static final int EFFECT_COLOR = 0x8B0000;

    /**
     * 构造函数
     */
    public HellfireBrandEffect() {
        super(MobEffectCategory.HARMFUL, EFFECT_COLOR);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // 吸血由 ElementReactionEvents 的 LivingDamageEvent.Post 监听处理，不需要每 tick 更新
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
