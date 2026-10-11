package com.legendarymage.legendarymagemod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 天罚充能 Buff 效果
 * 雷系与神圣系元素反应给予施法者的增益 Buff
 * 效果：下一次法术伤害 ×2，触发后消耗（一次性质变）
 * 伤害翻倍与消耗逻辑由 ElementReactionEvents 的法术伤害监听处理
 *
 * @author Love_U
 * @version 1.0.0
 */
public class ChargedBuffEffect extends MobEffect {

    /**
     * 效果 ID
     */
    public static final String EFFECT_ID = "charged_buff";

    /**
     * 效果颜色（金紫色）
     */
    private static final int EFFECT_COLOR = 0xD4AF37;

    /**
     * 构造函数
     */
    public ChargedBuffEffect() {
        super(MobEffectCategory.BENEFICIAL, EFFECT_COLOR);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // 效果由 ElementReactionEvents 的法术伤害监听处理，不需要每 tick 更新
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
