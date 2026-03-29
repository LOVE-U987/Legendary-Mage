package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.element.ElementType;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.effect.IMobEffectEndCallback;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 邪术标记效果（邪术异常）
 * 每一级减少法术抗性10%
 * 可无限叠加
 * 
 * @author Love_U
 * @version 1.0.0
 */
public class EldritchMarkEffect extends ElementMarkEffect implements IMobEffectEndCallback {

    /**
     * 效果ID
     */
    public static final String EFFECT_ID = "eldritch_mark";

    /**
     * 效果颜色（深绿色）
     */
    private static final int EFFECT_COLOR = 0x006400;

    /**
     * 每级法术抗性减少（10% = 0.10）
     */
    public static final double SPELL_RESIST_REDUCTION_PER_LEVEL = -0.10;

    /**
     * 构造函数
     * 添加属性修饰符用于EMIffect显示
     */
    public EldritchMarkEffect() {
        super(ElementType.ELDRITCH, EFFECT_COLOR);
        
        // 添加法术抗性修饰符（每级-10%，用于EMIffect显示）
        this.addAttributeModifier(
                AttributeRegistry.SPELL_RESIST,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "eldritch_mark_spell_resist"),
                SPELL_RESIST_REDUCTION_PER_LEVEL,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    @Override
    public String getEffectId() {
        return EFFECT_ID;
    }

    /**
     * 当效果被添加时调用
     * 动态调整属性修饰符
     * 
     * @param entity 实体
     * @param amplifier 效果等级（0=1级，1=2级，2=3级）
     */
    @Override
    public void onEffectAdded(LivingEntity entity, int amplifier) {
        // 属性修饰符通过构造函数自动应用
        // 每级-10%法术抗性
    }

    /**
     * 当效果被移除时调用
     * 
     * @param entity 实体
     * @param amplifier 效果等级（0=1级，1=2级，2=3级）
     */
    @Override
    public void onEffectRemoved(LivingEntity entity, int amplifier) {
        // 属性修饰符会自动移除
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
}
