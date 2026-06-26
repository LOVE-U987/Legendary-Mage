package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.Config;
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
 * - 每级减少最大生命值（比例从配置读取）
 * - 死亡时概率变为我方僵尸（从配置读取）
 * - 死亡时概率毒爆（从配置读取）
 *
 * @author Love_U
 * @version 1.0.4
 */
public class PlagueBuffEffect extends MobEffect {

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
     * 注意：配置驱动的属性修饰符在 FMLCommonSetupEvent 阶段集中初始化（ModEffects.initConfigModifiers），
     * 此时 Config 尚未就绪，不可在此处读取配置值。
     */
    public PlagueBuffEffect() {
        super(MobEffectCategory.HARMFUL, EFFECT_COLOR);
        // 属性修饰符在 ModEffects.initConfigModifiers() 中延迟初始化
    }

    /**
     * 计算最大生命值减少百分比
     *
     * @param amplifier Buff 等级（从 0 开始）
     * @return 生命值减少百分比
     */
    public static double calculateMaxHealthReduction(int amplifier) {
        return (amplifier + 1) * Config.PLAGUE_MAX_HEALTH_REDUCTION_PER_LEVEL.get();
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
        return Config.PLAGUE_ZOMBIE_CONVERSION_CHANCE.get();
    }

    /**
     * 获取毒爆概率
     *
     * @return 毒爆概率
     */
    public static double getExplosionChance() {
        return Config.PLAGUE_EXPLOSION_CHANCE.get();
    }

    /**
     * 获取瘟疫 Buff 持续时间（秒）
     *
     * @return 持续时间（秒）
     */
    public static int getDurationSeconds() {
        return Config.PLAGUE_DURATION_SECONDS.get();
    }

    /**
     * 获取瘟疫 Buff 最大层数
     *
     * @return 最大层数
     */
    public static int getMaxStacks() {
        return Config.PLAGUE_MAX_STACKS.get();
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
        return EFFECT_ID;
    }
}
