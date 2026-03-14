package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.element.ElementType;

/**
 * 神圣系标记效果（光明异常）
 * 
 * @author Love_U
 * @version 0.0.1
 */
public class HolyMarkEffect extends ElementMarkEffect {

    /**
     * 效果ID
     */
    public static final String EFFECT_ID = "holy_mark";

    /**
     * 效果颜色（金色）
     */
    private static final int EFFECT_COLOR = 0xFFD700;

    /**
     * 构造函数
     */
    public HolyMarkEffect() {
        super(ElementType.HOLY, EFFECT_COLOR);
    }

    @Override
    public String getEffectId() {
        return EFFECT_ID;
    }
}
