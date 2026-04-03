package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.LegendaryMage;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

/**
 * 瘟疫 Buff 效果
 * 暗毒元素反应产生的 Buff
 * 效果：
 * - 每级减少 2% 最大生命值
 * - 死亡时 25% 概率变为我方僵尸
 * - 死亡时 75% 概率毒爆
 * 
 * @author Love_U
 * @version 1.0.4
 */
public class PlagueBuffEffect extends MobEffect {

    /**
     * 每级减少的最大生命值百分比
     */
    private static final double MAX_HEALTH_REDUCTION_PER_LEVEL = 0.02; // 2%

    /**
     * 僵尸转化概率
     */
    private static final double ZOMBIE_CONVERSION_CHANCE = 0.25; // 25%

    /**
     * 毒爆概率
     */
    private static final double EXPLOSION_CHANCE = 0.75; // 75%

    /**
     * Buff 持续时间（秒）
     */
    public static final int DURATION_SECONDS = 10;

    /**
     * 最大叠加层数
     */
    public static final int MAX_STACKS = 10;

    /**
     * 效果颜色 - 暗绿色
     */
    private static final int EFFECT_COLOR = 0x2D4A1C;

    /**
     * 效果 ID
     */
    public static final String EFFECT_ID = "plague_buff";

    /**
     * 构造函数
     */
    public PlagueBuffEffect() {
        super(MobEffectCategory.HARMFUL, EFFECT_COLOR);
        
        // 添加最大生命值减少修饰符（每级 -2%）
        // 注意：Minecraft 会自动乘以 (amplifier + 1)
        this.addAttributeModifier(
            Attributes.MAX_HEALTH,
            ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "plague_max_health"),
            -MAX_HEALTH_REDUCTION_PER_LEVEL,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    /**
     * 计算最大生命值减少百分比
     * 
     * @param amplifier Buff 等级（从 0 开始）
     * @return 生命值减少百分比
     */
    public static double calculateMaxHealthReduction(int amplifier) {
        return (amplifier + 1) * MAX_HEALTH_REDUCTION_PER_LEVEL;
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

    /**
     * 获取僵尸转化概率
     * 
     * @return 僵尸转化概率
     */
    public static double getZombieConversionChance() {
        return ZOMBIE_CONVERSION_CHANCE;
    }

    /**
     * 获取毒爆概率
     * 
     * @return 毒爆概率
     */
    public static double getExplosionChance() {
        return EXPLOSION_CHANCE;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // 瘟疫 Buff 通过属性修饰符持续生效
        // 死亡时的效果在 LivingDeathEvent 中处理
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
        return LegendaryMage.MODID + ":plague_buff";
    }
}
