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
 * 效果：减少冰系法术抗性5%，减少雷系法术抗性10%
 * 
 * @author Love_U
 * @version 1.0.5
 */
public class LightningRodBuffEffect extends MobEffect {

    /**
     * 冰系抗性减少百分比（固定-5%）
     */
    private static final double ICE_RESIST_REDUCTION = -0.05; // -5%

    /**
     * 雷系抗性减少百分比（固定-10%）
     */
    private static final double LIGHTNING_RESIST_REDUCTION = -0.10; // -10%

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
     * 添加属性修饰符用于EMIffect等模组显示
     */
    public LightningRodBuffEffect() {
        super(MobEffectCategory.HARMFUL, EFFECT_COLOR);
        
        // 添加冰系抗性修饰符（-5%）
        this.addAttributeModifier(
                AttributeRegistry.ICE_MAGIC_RESIST,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "lightning_rod_ice_resist"),
                ICE_RESIST_REDUCTION,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
        
        // 添加雷系抗性修饰符（-10%）
        this.addAttributeModifier(
                AttributeRegistry.LIGHTNING_MAGIC_RESIST,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "lightning_rod_lightning_resist"),
                LIGHTNING_RESIST_REDUCTION,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    /**
     * 获取冰系抗性减少百分比
     * 
     * @return 冰系抗性减少百分比（固定-5%）
     */
    public static double getIceResistReduction() {
        return ICE_RESIST_REDUCTION;
    }

    /**
     * 获取雷系抗性减少百分比
     * 
     * @return 雷系抗性减少百分比（固定-10%）
     */
    public static double getLightningResistReduction() {
        return LIGHTNING_RESIST_REDUCTION;
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
