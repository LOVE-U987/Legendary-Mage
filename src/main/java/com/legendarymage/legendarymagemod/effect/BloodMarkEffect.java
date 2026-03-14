package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.element.ElementType;

/**
 * 血系标记效果（黑暗异常）
 * 
 * @author Love_U
 * @version 0.0.1
 */
public class BloodMarkEffect extends ElementMarkEffect {

    /**
     * 效果ID
     */
    public static final String EFFECT_ID = "blood_mark";

    /**
     * 效果颜色（深红色）
     */
    private static final int EFFECT_COLOR = 0x8B0000;

    /**
     * 构造函数
     */
    public BloodMarkEffect() {
        super(ElementType.BLOOD, EFFECT_COLOR);
    }

    @Override
    public String getEffectId() {
        return EFFECT_ID;
    }
}
