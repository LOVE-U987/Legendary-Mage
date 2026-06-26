package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.Config;
import com.legendarymage.legendarymagemod.LegendaryMage;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * 暗夜无光效果
 * 血系元素反应给予目标的Debuff
 * 效果：减少血系法术抗性5%
 * 
 * @author Love_U
 * @version 1.0.5
 */
public class DarknessBuffEffect extends MobEffect {

    /**
     * 效果ID
     */
    public static final String EFFECT_ID = "darkness_buff";

    /**
     * 效果颜色（深红色）
     */
    private static final int EFFECT_COLOR = 0x8B0000;

    /**
     * 构造函数
     * 注意：配置驱动的属性修饰符在 FMLCommonSetupEvent 阶段集中初始化（ModEffects.initConfigModifiers），
     * 此时 Config 尚未就绪，不可在此处读取配置值。
     */
    public DarknessBuffEffect() {
        super(MobEffectCategory.HARMFUL, EFFECT_COLOR);
        // 属性修饰符在 ModEffects.initConfigModifiers() 中延迟初始化
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // 效果通过属性修饰符应用，不需要每tick更新
        return true;
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

    /**
     * 获取血系法术抗性减少值
     *
     * @return 血系法术抗性减少值
     */
    public static double getBloodMagicResistReduction() {
        return Config.DARKNESS_BLOOD_RESIST_REDUCTION.get();
    }

    /**
     * 获取效果ID
     * 
     * @return 效果ID
     */
    public String getEffectId() {
        return EFFECT_ID;
    }
}
