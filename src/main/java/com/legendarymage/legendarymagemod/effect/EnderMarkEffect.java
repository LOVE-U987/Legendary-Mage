package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.element.ElementType;

/**
 * 末影标记效果（末影异常）
 * 
 * @author Love_U
 * @version 0.0.1
 */
public class EnderMarkEffect extends ElementMarkEffect {

    /**
     * 效果ID
     */
    public static final String EFFECT_ID = "ender_mark";

    /**
     * 效果颜色（暗紫色）
     */
    private static final int EFFECT_COLOR = 0x9932CC;

    /**
     * 构造函数
     */
    public EnderMarkEffect() {
        super(ElementType.ENDER, EFFECT_COLOR);
    }

    @Override
    public String getEffectId() {
        return EFFECT_ID;
    }
}
