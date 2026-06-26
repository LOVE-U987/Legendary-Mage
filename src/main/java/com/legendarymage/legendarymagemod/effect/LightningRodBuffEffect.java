package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.Config;
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
 * 效果：减少冰系与雷系法术抗性，具体数值从配置读取
 *
 * @author Love_U
 * @version 1.0.5
 */
public class LightningRodBuffEffect extends MobEffect {

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
     * 注意：配置驱动的属性修饰符在 FMLCommonSetupEvent 阶段集中初始化（ModEffects.initConfigModifiers），
     * 此时 Config 尚未就绪，不可在此处读取配置值。
     */
    public LightningRodBuffEffect() {
        super(MobEffectCategory.HARMFUL, EFFECT_COLOR);
        // 属性修饰符在 ModEffects.initConfigModifiers() 中延迟初始化
    }

    /**
     * 获取冰系抗性减少百分比
     *
     * @return 冰系抗性减少百分比
     */
    public static double getIceResistReduction() {
        return Config.LIGHTNING_ROD_ICE_RESIST_REDUCTION.get();
    }

    /**
     * 获取雷系抗性减少百分比
     *
     * @return 雷系抗性减少百分比
     */
    public static double getLightningResistReduction() {
        return Config.LIGHTNING_ROD_LIGHTNING_RESIST_REDUCTION.get();
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
     * 获取避雷针 Buff 持续时间（秒）
     *
     * @return 持续时间（秒）
     */
    public static int getDurationSeconds() {
        return Config.LIGHTNING_ROD_DURATION_SECONDS.get();
    }

    /**
     * 获取避雷针 Buff 最大层数
     *
     * @return 最大层数
     */
    public static int getMaxStacks() {
        return Config.LIGHTNING_ROD_MAX_STACKS.get();
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
        return EFFECT_ID;
    }
}
