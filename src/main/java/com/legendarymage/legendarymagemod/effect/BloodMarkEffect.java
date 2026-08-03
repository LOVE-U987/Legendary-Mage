package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.element.ElementType;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * 血系标记效果（黑暗异常）【重写 v2.0】
 *
 * 【效果】
 * - 每一级减 10% 血系法术抗性（属性修饰符按等级自动缩放）
 * - 3级以上每一级加 5% 易伤（受伤倍率提升，由 ElementReactionEvents 处理）
 *
 * @author Love_U
 * @version 2.0.0
 */
public class BloodMarkEffect extends ElementMarkEffect {

    /**
     * 效果ID
     */
    public static final String EFFECT_ID = "blood_mark";

    /**
     * 效果颜色（深红色）
     */
    private static final int EFFECT_COLOR = 0x8B0000;

    /**
     * 每级血系法术抗性减少（10%）
     */
    private static final double BLOOD_RESIST_REDUCTION_PER_LEVEL = -0.10;

    /**
     * 易伤触发等级（3级以上，即 >= 4级）
     */
    public static final int VULNERABILITY_THRESHOLD_LEVEL = 4;

    /**
     * 3级以上每级易伤加成（5%）
     */
    public static final double VULNERABILITY_PER_LEVEL = 0.05;

    /**
     * 构造函数
     * 注册每级 -10% 血系法术抗性的属性修饰符
     */
    public BloodMarkEffect() {
        super(ElementType.BLOOD, EFFECT_COLOR);
        // 每级 -10% 血系法术抗性（MobEffectInstance 会按 amplifier+1 自动缩放修饰符）
        this.addAttributeModifier(
                AttributeRegistry.BLOOD_MAGIC_RESIST,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "blood_mark_blood_resist"),
                BLOOD_RESIST_REDUCTION_PER_LEVEL,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    @Override
    public String getEffectId() {
        return EFFECT_ID;
    }

    /**
     * 计算黑暗异常的易伤倍率
     * 3级以上每一级 +5% 易伤
     *
     * @param markLevel 标记等级（1-5）
     * @return 受伤倍率（>= 1.0）
     */
    public static float calculateVulnerabilityMultiplier(int markLevel) {
        if (markLevel < VULNERABILITY_THRESHOLD_LEVEL) {
            return 1.0f;
        }
        return 1.0f + (float) VULNERABILITY_PER_LEVEL * (markLevel - 3);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // 被动效果，无需每 tick 处理（易伤由事件处理器计算）
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // 纯被动效果，属性修饰符已按等级缩放
        return false;
    }
}
