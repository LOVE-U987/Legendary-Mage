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
 * 混沌Buff效果
 * 邪术-猩红元素反应给予施法者的Buff
 *
 * 【效果说明】
 * 法术强度加成，基于Buff等级动态计算：
 * - 1级 (amplifier=0): +5% 法术强度
 * - 2级 (amplifier=1): +10% 法术强度
 * - 3级 (amplifier=2): +15% 法术强度
 * - 以此类推...
 *
 * 注意：Minecraft属性修饰符系统会自动将amount乘以(amplifier+1)
 * 因此构造函数中设置的值是"每级基础加成"，而非总加成。
 *
 * @author Love_U
 * @version 1.0.1 (修正文档与实现的一致性)
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
     * 构造函数
     * 注意：配置驱动的属性修饰符在 FMLCommonSetupEvent 阶段集中初始化（ModEffects.initConfigModifiers），
     * 此时 Config 尚未就绪，不可在此处读取配置值。
     */
    public ChaosBuffEffect() {
        super(MobEffectCategory.BENEFICIAL, EFFECT_COLOR);
        // 属性修饰符在 ModEffects.initConfigModifiers() 中延迟初始化
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
     * 基于Minecraft属性系统的实际公式：每级加成 * (amplifier + 1)
     *
     * @param amplifier Buff等级（0开始，即1级为0）
     * @return 法术强度加成值（例如0.05表示5%）
     */
    public static double calculateSpellPowerBonus(int amplifier) {
        return Config.CHAOS_SPELL_POWER_BONUS_PER_LEVEL.get() * (amplifier + 1);
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
