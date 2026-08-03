package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.Config;
import com.legendarymage.legendarymagemod.ModLogger;
import com.legendarymage.legendarymagemod.element.ElementMarkData;
import com.legendarymage.legendarymagemod.element.ElementType;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/**
 * 冰系标记效果（冰冻异常）【重写 v2.0】
 *
 * 【效果】
 * - 3级后的攻击有 50% 概率冰冻目标 3 秒，CD 5 秒
 *
 * @author Love_U
 * @version 2.0.0
 */
public class IceMarkEffect extends ElementMarkEffect {

    /**
     * 效果ID
     */
    public static final String EFFECT_ID = "ice_mark";

    /**
     * 效果颜色（深青色）
     */
    private static final int EFFECT_COLOR = 0x00CED1;

    /**
     * 冰冻触发所需等级（3级后）
     */
    private static final int FREEZE_TRIGGER_LEVEL = 3;

    /**
     * 冰冻触发概率（50%）
     */
    private static final double FREEZE_CHANCE = 0.5;

    /**
     * 冰冻持续时间（tick）
     * 3秒 = 60 tick
     */
    private static final int FREEZE_DURATION = 60;

    /**
     * 冰冻CD（tick）
     * 5秒 = 100 tick
     */
    private static final int FREEZE_COOLDOWN_TICKS = 100;

    /**
     * 构造函数
     */
    public IceMarkEffect() {
        super(ElementType.ICE, EFFECT_COLOR);
    }

    @Override
    public String getEffectId() {
        return EFFECT_ID;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // 被动效果，无需每 tick 处理（冰冻由攻击事件触发）
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // 纯被动效果
        return false;
    }

    /**
     * 尝试触发冰冻（由法术伤害事件调用）
     *
     * 触发条件：
     * - 目标携带 3 级及以上的冰冻异常
     * - 5% 概率判定成功
     * - CD 5 秒
     *
     * @param target 目标（携带冰冻异常）
     * @return 是否成功冰冻
     */
    public static boolean tryTriggerFreeze(LivingEntity target) {
        // 检查目标是否已死亡或正在死亡
        if (target == null || !target.isAlive() || target.isDeadOrDying()) {
            return false;
        }

        // 检查冰冻异常等级（3级后）
        int level = ElementMarkData.getMarkLevel(target, ElementType.ICE);
        if (level < FREEZE_TRIGGER_LEVEL) {
            return false;
        }

        // 5% 概率判定
        if (Math.random() >= FREEZE_CHANCE) {
            return false;
        }

        // 检查CD（5秒）
        if (!ElementMarkData.isCooldownReady(target, ElementType.ICE, FREEZE_COOLDOWN_TICKS)) {
            return false;
        }

        if (Config.ELEMENT_REACTION_DEBUG_OUTPUT.get()) {
            ModLogger.element("[冰冻异常] {} 被冰冻 3 秒 (标记{}级)", target.getName().getString(), level);
        }

        // 记录触发时刻（开始CD）
        ElementMarkData.markCooldown(target, ElementType.ICE);

        // ===== 冰冻：铁魔法原生冰冻效果 =====
        // 与铁魔法冰系法术（如 Frostwave/ConeOfCold）一致：
        // CHILLED 效果（每级 -20% 移速）+ 抬升冰冻值至完全冻结，
        // 完全冻结时由铁魔法的 ChilledEffect 触发冰棺，实现完整冰冻定身

        // 1) 施加铁魔法冰冻效果 CHILLED（标记等级越高冻结越强）
        int chilledAmplifier = Math.max(2, Math.min(level - 1, 4)); // 标记3-5级 → CHILLED 3-5级
        target.addEffect(new MobEffectInstance(
                MobEffectRegistry.CHILLED,
                FREEZE_DURATION,
                chilledAmplifier,
                false,
                true,
                true
        ));

        // 2) 抬升冰冻值至完全冻结线以上，维持约 3 秒（原版每 tick 衰减 2）
        //    完全冻结 + CHILLED 会触发铁魔法的冰棺，实现真正意义上的冰冻
        target.setTicksFrozen(target.getTicksFrozen() + FREEZE_DURATION * 2);

        // 播放冰冻粒子效果
        if (target.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.SNOWFLAKE,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    20,
                    target.getBbWidth() * 0.4, target.getBbHeight() * 0.3, target.getBbWidth() * 0.4,
                    0.05
            );
        }

        return true;
    }
}
