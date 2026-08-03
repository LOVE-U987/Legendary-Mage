package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.Config;
import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.ModLogger;
import com.legendarymage.legendarymagemod.element.ElementMarkData;
import com.legendarymage.legendarymagemod.element.ElementType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 毒系标记效果（毒素异常）【重写 v2.0】
 *
 * 【效果】
 * - 每一秒受到一次伤害，伤害 = buff等级 × 施法者毒系流派强度
 * - 每一级减 5% 护甲（属性修饰符按等级自动缩放）
 *
 * @author Love_U
 * @version 2.0.0
 */
public class PoisonMarkEffect extends ElementMarkEffect {

    /**
     * 效果ID
     */
    public static final String EFFECT_ID = "poison_mark";

    /**
     * 效果颜色（酸橙绿）
     */
    private static final int EFFECT_COLOR = 0x32CD32;

    /**
     * 每级护甲减少（5%）
     */
    private static final double ARMOR_REDUCTION_PER_LEVEL = -0.05;

    /**
     * 毒素伤害触发间隔（tick）
     * 1秒 = 20 tick
     */
    private static final int DAMAGE_INTERVAL = 20;

    /**
     * 构造函数
     * 注册每级 -5% 护甲的属性修饰符
     */
    public PoisonMarkEffect() {
        super(ElementType.POISON, EFFECT_COLOR);
        // 每级 -5% 护甲（MobEffectInstance 会按 amplifier+1 自动缩放修饰符）
        this.addAttributeModifier(
                Attributes.ARMOR,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "poison_mark_armor_reduction"),
                ARMOR_REDUCTION_PER_LEVEL,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    @Override
    public String getEffectId() {
        return EFFECT_ID;
    }

    /**
     * 每 tick 检查冷却，每一秒造成一次公式伤害
     * 伤害 = buff等级 × 施法者毒系流派强度
     *
     * @param entity    实体
     * @param amplifier 效果等级（0开始）
     */
    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // 只在服务器端执行
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return true;
        }

        // 检查实体是否已死亡或正在死亡
        if (!entity.isAlive() || entity.isDeadOrDying()) {
            return true;
        }

        // 每秒触发一次毒素伤害
        if (ElementMarkData.isCooldownReady(entity, ElementType.POISON, DAMAGE_INTERVAL)) {
            // 伤害由公式计算：buff等级 × 毒系流派强度
            float damage = ElementMarkData.calculateDamage(entity, ElementType.POISON, null);
            if (damage > 0) {
                entity.hurt(serverLevel.damageSources().magic(), damage);

                // 播放毒素粒子效果
                serverLevel.sendParticles(
                        ParticleTypes.ITEM_SLIME,
                        entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(),
                        4,
                        entity.getBbWidth() * 0.3, entity.getBbHeight() * 0.2, entity.getBbWidth() * 0.3,
                        0.02
                );

                if (Config.ELEMENT_REACTION_DEBUG_OUTPUT.get()) {
                    ModLogger.element("[毒素异常] {} 受到毒素伤害: {} (标记{}级)",
                            entity.getName().getString(), damage, amplifier + 1);
                }
            }
            ElementMarkData.markCooldown(entity, ElementType.POISON);
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // 每 tick 检查冷却，由 applyEffectTick 控制触发间隔
        return true;
    }
}
