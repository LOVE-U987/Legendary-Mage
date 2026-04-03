package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.LegendaryMage;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.core.Holder;

/**
 * 避雷针 Buff 效果
 * 冰雷元素反应产生的 Buff
 * 效果：减少雷系和冰系元素抗性，每级减少 5%
 * 
 * @author Love_U
 * @version 1.0.4
 */
public class LightningRodBuffEffect extends MobEffect {

    /**
     * 每级减少的元素抗性百分比
     */
    private static final double RESISTANCE_REDUCTION_PER_LEVEL = 0.05; // 5%

    /**
     * Buff 持续时间（秒）
     */
    public static final int DURATION_SECONDS = 10;

    /**
     * 最大叠加层数
     */
    public static final int MAX_STACKS = 5;

    /**
     * 效果颜色 - 蓝色
     */
    private static final int EFFECT_COLOR = 0x55AAFF;

    /**
     * 效果 ID
     */
    public static final String EFFECT_ID = "lightning_rod_buff";

    /**
     * 构造函数
     */
    public LightningRodBuffEffect() {
        super(MobEffectCategory.BENEFICIAL, EFFECT_COLOR);
        
        // 避雷针 Buff 通过元素反应系统动态应用效果
        // 不在构造函数中添加固定修饰符
    }

    /**
     * 计算元素抗性减少百分比
     * 
     * @param amplifier Buff 等级（从 0 开始）
     * @return 抗性减少百分比
     */
    public static double calculateResistanceReduction(int amplifier) {
        return (amplifier + 1) * RESISTANCE_REDUCTION_PER_LEVEL;
    }

    /**
     * 获取 Buff 等级（从 1 开始）
     * 
     * @param amplifier Buff 等级（从 0 开始）
     * @return Buff 等级（从 1 开始）
     */
    public static int getBuffLevel(int amplifier) {
        return amplifier + 1;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // 避雷针 Buff 通过属性修饰符持续生效
        // 不需要每 tick 更新
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }

    /**
     * 获取效果 ID
     * 
     * @return 效果 ID
     */
    public String getEffectId() {
        return LegendaryMage.MODID + ":lightning_rod_buff";
    }
}
