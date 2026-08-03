package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.element.ElementType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 元素标记基础效果类
 * 所有元素标记效果的基类
 *
 * 【重写 v2.0】
 * - 上限等级提升为 5 级（amplifier 0-4）
 * - 时长固定 5 秒（100 tick）
 * - 移除旧的 50% 概率升级机制（升级逻辑统一移至 ElementReactionManager，改为 75% 概率施加/更新）
 *
 * @author Love_U
 * @version 2.0.0
 */
public abstract class ElementMarkEffect extends MobEffect {

    /**
     * 元素类型
     */
    private final ElementType elementType;

    /**
     * 基础持续时间（tick）
     * 5秒 = 100 tick，时长固定不变
     */
    public static final int BASE_DURATION = 100;

    /**
     * 基础持续时间（秒）
     */
    public static final int BASE_DURATION_SECONDS = 5;

    /**
     * 最大等级（amplifier 0-4 对应 1-5级）
     */
    public static final int MAX_LEVEL = 4;

    /**
     * 攻击施加/更新元素异常的概率（75%）
     */
    public static final double APPLY_CHANCE = 0.75;

    /**
     * 构造函数
     *
     * @param elementType 元素类型
     * @param color       效果颜色
     */
    protected ElementMarkEffect(ElementType elementType, int color) {
        super(MobEffectCategory.HARMFUL, color);
        this.elementType = elementType;
    }

    /**
     * 获取元素类型
     *
     * @return 元素类型
     */
    public ElementType getElementType() {
        return elementType;
    }

    /**
     * 获取效果ID
     *
     * @return 效果ID
     */
    public abstract String getEffectId();

    /**
     * 由 amplifier 计算等级（1-5级）
     *
     * @param amplifier 效果等级（0开始）
     * @return 标记等级（1-5）
     */
    public static int getLevelFromAmplifier(int amplifier) {
        return Math.min(amplifier + 1, MAX_LEVEL + 1);
    }

    /**
     * 计算持续时间
     * 时长固定 5 秒
     *
     * @param level 标记等级（1-5）
     * @return 持续时间（tick）
     */
    public static int calculateDuration(int level) {
        return BASE_DURATION;
    }

    /**
     * 概率判定：是否给予元素异常 buff 或更新时长（75%）
     *
     * @return 是否判定成功
     */
    public static boolean shouldApplyMark() {
        return Math.random() < APPLY_CHANCE;
    }

    /**
     * 判断是否应该应用效果更新
     * 子类可重写以控制触发频率
     *
     * @param duration  剩余持续时间
     * @param amplifier 效果等级
     * @return 是否应该更新
     */
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
