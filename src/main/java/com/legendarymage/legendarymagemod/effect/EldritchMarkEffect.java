package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.element.ElementType;

/**
 * 邪术标记效果（邪术异常）
 * 
 * @author Love_U
 * @version 0.0.1
 */
public class EldritchMarkEffect extends ElementMarkEffect {

    /**
     * 效果ID
     */
    public static final String EFFECT_ID = "eldritch_mark";

    /**
     * 效果颜色（靛蓝色）
     */
    private static final int EFFECT_COLOR = 0x4B0082;

    /**
     * 构造函数
     */
    public EldritchMarkEffect() {
        super(ElementType.ELDRITCH, EFFECT_COLOR);
    }

    @Override
    public String getEffectId() {
        return EFFECT_ID;
    }
}
