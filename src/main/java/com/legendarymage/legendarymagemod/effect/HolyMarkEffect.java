package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.ModLogger;
import com.legendarymage.legendarymagemod.element.ElementMarkData;
import com.legendarymage.legendarymagemod.element.ElementType;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.particle.ShockwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

/**
 * 神圣系标记效果（光明异常）【重写 v2.0】
 *
 * 【效果】
 * - 3级以上的攻击可触发神圣打击，CD 2秒
 * - 神圣打击伤害由公式计算：buff等级 × 施法者神圣系流派强度
 * - 使用原版伤害源，不触发铁魔法法术系统，不会刷新元素标记
 *
 * @author Love_U
 * @version 2.0.0
 */
public class HolyMarkEffect extends ElementMarkEffect {

    /**
     * 效果ID
     */
    public static final String EFFECT_ID = "holy_mark";

    /**
     * 效果颜色（金色）
     */
    private static final int EFFECT_COLOR = 0xFFD700;

    /**
     * 神圣打击触发所需等级（3级以上）
     */
    private static final int HOLY_TRIGGER_LEVEL = 3;

    /**
     * 神圣打击CD（tick）
     * 2秒 = 40 tick
     */
    private static final int HOLY_COOLDOWN_TICKS = 40;

    /**
     * 构造函数
     */
    public HolyMarkEffect() {
        super(ElementType.HOLY, EFFECT_COLOR);
    }

    @Override
    public String getEffectId() {
        return EFFECT_ID;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // 被动效果，无需每 tick 处理（神圣打击由攻击事件触发）
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // 纯被动效果
        return false;
    }

    /**
     * 尝试触发神圣打击（由法术伤害事件调用）
     *
     * 触发条件：
     * - 目标携带 3 级以上光明异常
     * - CD 2 秒
     * 伤害 = buff等级 × 神圣系流派强度（多人游戏由双方玩家流派强度共同计算）
     *
     * @param target   目标（携带光明异常）
     * @param attacker 攻击者（可为null）
     * @return 是否触发了神圣打击
     */
    public static boolean tryTriggerHolyStrike(LivingEntity target, LivingEntity attacker) {
        // 检查目标是否已死亡或正在死亡
        if (target == null || !target.isAlive() || target.isDeadOrDying()) {
            return false;
        }

        // 检查光明异常等级（3级以上）
        int level = ElementMarkData.getMarkLevel(target, ElementType.HOLY);
        if (level < HOLY_TRIGGER_LEVEL) {
            return false;
        }

        // 检查CD（2秒）
        if (!ElementMarkData.isCooldownReady(target, ElementType.HOLY, HOLY_COOLDOWN_TICKS)) {
            return false;
        }

        ModLogger.spellDebug("[神圣打击调试] 触发神圣打击！目标: {}", target.getName().getString());

        // 记录触发时刻（开始CD）
        ElementMarkData.markCooldown(target, ElementType.HOLY);

        // 触发神圣打击
        triggerHolyStrike(target, attacker);
        return true;
    }

    /**
     * 触发神圣打击
     * 使用原版魔法伤害源，不会触发铁魔法法术系统，不会刷新元素标记
     * 伤害由公式计算：buff等级 × 神圣系流派强度
     *
     * @param target   目标
     * @param attacker 攻击者
     */
    private static void triggerHolyStrike(LivingEntity target, LivingEntity attacker) {
        if (!(target.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        // 公式伤害：buff等级 × 神圣系流派强度（多人合并）
        float damage = ElementMarkData.calculateDamage(target, ElementType.HOLY, attacker);
        if (damage <= 0) {
            return;
        }

        target.hurt(serverLevel.damageSources().magic(), damage);

        // 播放神圣打击粒子效果
        playHolyStrikeEffects(serverLevel, target);
    }

    /**
     * 播放神圣打击效果
     *
     * @param serverLevel 服务器世界
     * @param entity      目标实体
     */
    private static void playHolyStrikeEffects(ServerLevel serverLevel, LivingEntity entity) {
        double x = entity.getX();
        double y = entity.getY() + entity.getBbHeight() * 0.5;
        double z = entity.getZ();

        // 神圣系颜色：金色
        Vector3f holyGold = new Vector3f(1.0f, 0.9f, 0.2f);
        Vector3f holyWhite = new Vector3f(1.0f, 1.0f, 0.9f);

        // 1. 核心冲击波 - 从目标位置爆发
        serverLevel.sendParticles(
                new ShockwaveParticleOptions(holyGold, 2.5f, true),
                x, y, z,
                1, 0, 0, 0, 0
        );

        // 2. 第二层冲击波 - 稍大
        serverLevel.sendParticles(
                new BlastwaveParticleOptions(holyWhite, 3.0f),
                x, y, z,
                1, 0, 0, 0, 0
        );

        // 3. 神圣之火粒子（16 个，向四周飞溅）
        for (int i = 0; i < 16; i++) {
            double angle = (i / 16.0) * Math.PI * 2;
            double radius = 0.5;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

            serverLevel.sendParticles(
                    ParticleRegistry.FIRE_PARTICLE.get(),
                    x + offsetX, y, z + offsetZ,
                    1,
                    offsetX * 0.3, 0.2, offsetZ * 0.3,
                    0.02
            );
        }

        // 4. 向上飞升的光柱粒子（12 个）
        for (int i = 0; i < 12; i++) {
            double offsetX = (serverLevel.random.nextDouble() - 0.5) * 0.8;
            double offsetZ = (serverLevel.random.nextDouble() - 0.5) * 0.8;

            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    x + offsetX, y, z + offsetZ,
                    1,
                    0, 0.15, 0,
                    0.03
            );
        }

        // 5. 附魔符文粒子（8 个）
        for (int i = 0; i < 8; i++) {
            double angle = (i / 8.0) * Math.PI * 2;
            double radius = 1.0;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

            serverLevel.sendParticles(
                    ParticleTypes.WITCH,
                    x + offsetX, y + 0.5, z + offsetZ,
                    1,
                    0, 0.05, 0,
                    0.02
            );
        }

        // 6. 灵魂粒子爆发（6 个）
        serverLevel.sendParticles(
                ParticleTypes.SOUL,
                x, y, z,
                6,
                0.4, 0.4, 0.4,
                0.05
        );

        // 7. 爆炸粒子
        serverLevel.sendParticles(
                ParticleTypes.EXPLOSION,
                x, y, z,
                2,
                0.2, 0.2, 0.2,
                0.02
        );

        // 音效 - 神圣能量爆发
        serverLevel.playSound(
                null,
                x, y, z,
                SoundRegistry.HOLY_CAST.get(),
                SoundSource.PLAYERS,
                1.0f,
                1.0f + serverLevel.random.nextFloat() * 0.2f
        );
    }
}
