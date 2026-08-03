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
 * 火系标记效果（火焰异常）【重写 v2.0】
 *
 * 【效果】
 * - 每一级减 2% 最大生命值（属性修饰符按等级自动缩放）
 * - 每 1 秒（CD 1秒）受到一次伤害，伤害 = buff等级 × 施法者火系流派强度
 * - 死亡后形成与 buff 等级大小相同范围的爆炸（由 ElementReactionEvents 处理）
 *
 * @author Love_U
 * @version 2.0.0
 */
public class FireMarkEffect extends ElementMarkEffect {

    /**
     * 效果ID
     */
    public static final String EFFECT_ID = "fire_mark";

    /**
     * 效果颜色（橙红色）
     */
    private static final int EFFECT_COLOR = 0xFF4500;

    /**
     * 每级最大生命值减少（2%）
     */
    private static final double MAX_HEALTH_REDUCTION_PER_LEVEL = -0.02;

    /**
     * 火焰伤害触发间隔（tick）
     * 1秒 = 20 tick
     */
    private static final int DAMAGE_INTERVAL = 20;

    /**
     * 构造函数
     * 注册每级 -2% 最大生命值的属性修饰符
     */
    public FireMarkEffect() {
        super(ElementType.FIRE, EFFECT_COLOR);
        // 每级 -2% 最大生命值（MobEffectInstance 会按 amplifier+1 自动缩放修饰符）
        this.addAttributeModifier(
                Attributes.MAX_HEALTH,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "fire_mark_max_health"),
                MAX_HEALTH_REDUCTION_PER_LEVEL,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    @Override
    public String getEffectId() {
        return EFFECT_ID;
    }

    /**
     * 每 tick 检查冷却，每 1 秒造成一次公式伤害
     * 伤害 = buff等级 × 施法者火系流派强度
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

        // CD 1秒：每 20 tick 触发一次灼烧
        if (ElementMarkData.isCooldownReady(entity, ElementType.FIRE, DAMAGE_INTERVAL)) {
            // 伤害由公式计算：buff等级 × 火系流派强度
            float damage = ElementMarkData.calculateDamage(entity, ElementType.FIRE, null);
            if (damage > 0) {
                entity.hurt(serverLevel.damageSources().magic(), damage);

                // 播放火焰粒子效果
                serverLevel.sendParticles(
                        ParticleTypes.FLAME,
                        entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(),
                        3,
                        entity.getBbWidth() * 0.3, entity.getBbHeight() * 0.2, entity.getBbWidth() * 0.3,
                        0.02
                );

                if (Config.ELEMENT_REACTION_DEBUG_OUTPUT.get()) {
                    ModLogger.element("[火焰异常] {} 受到灼烧伤害: {} (标记{}级)",
                            entity.getName().getString(), damage, amplifier + 1);
                }
            }
            ElementMarkData.markCooldown(entity, ElementType.FIRE);
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // 每 tick 检查冷却，由 applyEffectTick 控制触发间隔
        return true;
    }
}
