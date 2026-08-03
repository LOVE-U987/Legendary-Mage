package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.element.ElementType;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * 邪术标记效果（邪术异常）【重写 v2.0】
 *
 * 【效果】
 * - 每一级减 5% 法术抗性（属性修饰符按等级自动缩放）
 * - 每一级减 5% 法术强度（属性修饰符按等级自动缩放）
 *
 * @author Love_U
 * @version 2.0.0
 */
public class EldritchMarkEffect extends ElementMarkEffect {

    /**
     * 效果ID
     */
    public static final String EFFECT_ID = "eldritch_mark";

    /**
     * 效果颜色（深紫色）
     */
    private static final int EFFECT_COLOR = 0x4B0082;

    /**
     * 每级法术抗性减少（5%）
     */
    public static final double SPELL_RESIST_REDUCTION_PER_LEVEL = -0.05;

    /**
     * 每级法术强度减少（5%）
     */
    public static final double SPELL_POWER_REDUCTION_PER_LEVEL = -0.05;

    /**
     * 构造函数
     * 注册每级 -5% 法术抗性与 -5% 法术强度的属性修饰符
     */
    public EldritchMarkEffect() {
        super(ElementType.ELDRITCH, EFFECT_COLOR);

        // 每级 -5% 法术抗性
        this.addAttributeModifier(
                AttributeRegistry.SPELL_RESIST,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "eldritch_mark_spell_resist"),
                SPELL_RESIST_REDUCTION_PER_LEVEL,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );

        // 每级 -5% 法术强度
        this.addAttributeModifier(
                AttributeRegistry.SPELL_POWER,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "eldritch_mark_spell_power"),
                SPELL_POWER_REDUCTION_PER_LEVEL,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    @Override
    public String getEffectId() {
        return EFFECT_ID;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // 被动效果，无需每 tick 处理
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // 纯被动效果，属性修饰符已按等级缩放
        return false;
    }

    /**
     * 计算法术抗性减少值
     *
     * @param markLevel 标记等级（1开始）
     * @return 法术抗性减少值（负数表示减少）
     */
    public static double calculateSpellResistReduction(int markLevel) {
        return SPELL_RESIST_REDUCTION_PER_LEVEL * markLevel;
    }

    /**
     * 计算法术强度减少值
     *
     * @param markLevel 标记等级（1开始）
     * @return 法术强度减少值（负数表示减少）
     */
    public static double calculateSpellPowerReduction(int markLevel) {
        return SPELL_POWER_REDUCTION_PER_LEVEL * markLevel;
    }
}
