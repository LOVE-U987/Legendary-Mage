package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.element.ElementType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 元素标记基础效果类
 * 所有元素标记效果的基类
 * 
 * @author Love_U
 * @version 0.0.1
 */
public abstract class ElementMarkEffect extends MobEffect {

    /**
     * 元素类型
     */
    private final ElementType elementType;

    /**
     * 基础持续时间（tick）
     * 5秒 = 100 tick
     */
    public static final int BASE_DURATION = 100;

    /**
     * 最大等级
     */
    public static final int MAX_LEVEL = 2; // 0-2 对应 1-3级

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
     * 计算持续时间
     *
     * @param level 标记等级（1-3）
     * @return 持续时间（tick）
     */
    public static int calculateDuration(int level) {
        return BASE_DURATION;
    }

    /**
     * 计算升级概率
     * 有50%概率升级
     *
     * @return 是否升级成功
     */
    public static boolean tryUpgrade() {
        return Math.random() < 0.5;
    }

    /**
     * 判断是否应该应用效果更新
     * 默认每tick都更新，子类可以重写
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
