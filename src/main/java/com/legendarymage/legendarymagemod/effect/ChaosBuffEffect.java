package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.LegendaryMage;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * 混沌Buff效果
 * 邪术-猩红元素反应给予施法者的Buff
 * 效果：法术强度+10%，每一等级+5%
 * 
 * @author Love_U
 * @version 0.0.2
 */
public class ChaosBuffEffect extends MobEffect {

    /**
     * 效果ID
     */
    public static final String EFFECT_ID = "chaos_buff";

    /**
     * 效果颜色（深紫色）
     */
    private static final int EFFECT_COLOR = 0x4B0082;

    /**
     * 基础法术强度加成（10% = 0.1）
     */
    public static final double BASE_SPELL_POWER_BONUS = 0.1;

    /**
     * 每级法术强度加成（5% = 0.05）
     */
    public static final double SPELL_POWER_BONUS_PER_LEVEL = 0.05;

    /**
     * 构造函数
     * 在注册时添加属性修饰符，利用Minecraft原生属性系统
     */
    public ChaosBuffEffect() {
        super(MobEffectCategory.BENEFICIAL, EFFECT_COLOR);
        
        // 添加法术强度修饰符（基础10%，每级+5%）
        // 注意：addAttributeModifier的amount是每级的增量
        this.addAttributeModifier(
                AttributeRegistry.SPELL_POWER,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "chaos_buff_spell_power"),
                SPELL_POWER_BONUS_PER_LEVEL, // 每级+5%
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // 效果通过属性修饰符应用，不需要每tick更新
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false; // 不需要每tick更新
    }

    /**
     * 计算法术强度加成
     * 基础10% + 每级5%
     * 
     * @param level Buff等级（0开始）
     * @return 法术强度加成值
     */
    public static double calculateSpellPowerBonus(int level) {
        return BASE_SPELL_POWER_BONUS + (level * SPELL_POWER_BONUS_PER_LEVEL);
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
