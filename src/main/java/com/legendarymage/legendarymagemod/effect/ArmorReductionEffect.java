package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.Config;
import com.legendarymage.legendarymagemod.LegendaryMage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 溶甲效果
 * 降低目标的护甲值，每级减少比例从配置读取
 *
 * @author Love_U
 * @version 1.0.4
 */
public class ArmorReductionEffect extends MobEffect {

    /**
     * 效果ID
     */
    public static final String EFFECT_ID = "armor_reduction";

    /**
     * 效果颜色（腐蚀绿色）
     */
    private static final int EFFECT_COLOR = 0x556B2F;

    /**
     * 构造函数
     * 注意：配置驱动的属性修饰符在 FMLCommonSetupEvent 阶段集中初始化（ModEffects.initConfigModifiers），
     * 此时 Config 尚未就绪，不可在此处读取配置值。
     */
    public ArmorReductionEffect() {
        super(MobEffectCategory.HARMFUL, EFFECT_COLOR);
        // 属性修饰符在 ModEffects.initConfigModifiers() 中延迟初始化
    }

    /**
     * 判断是否应该应用效果更新
     * 
     * @param duration  剩余持续时间
     * @param amplifier 效果等级
     * @return 是否应该更新
     */
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // 这是一个被动效果，不需要每tick更新
        return false;
    }
}
