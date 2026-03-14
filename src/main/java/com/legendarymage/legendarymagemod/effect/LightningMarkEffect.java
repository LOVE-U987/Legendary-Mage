package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.element.ElementType;

/**
 * 雷系标记效果（雷电异常）
 * 
 * @author Love_U
 * @version 0.0.1
 */
public class LightningMarkEffect extends ElementMarkEffect {

    /**
     * 效果ID
     */
    public static final String EFFECT_ID = "lightning_mark";

    /**
     * 效果颜色（深紫色）
     */
    private static final int EFFECT_COLOR = 0x9400D3;

    /**
     * 构造函数
     */
    public LightningMarkEffect() {
        super(ElementType.LIGHTNING, EFFECT_COLOR);
    }

    @Override
    public String getEffectId() {
        return EFFECT_ID;
    }
}
