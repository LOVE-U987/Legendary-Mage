package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.Config;
import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.ModLogger;
import com.legendarymage.legendarymagemod.element.ElementMarkData;
import com.legendarymage.legendarymagemod.element.ElementType;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.particle.ShockwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.joml.Vector3f;

/**
 * 末影标记效果（末影异常）【重写 v2.0】
 *
 * 【效果】
 * - 每一级 -2% 法术强度（属性修饰符按等级自动缩放）
 * - 每一级 +4% 法术冷却（降低冷却缩减，属性修饰符按等级自动缩放）
 * - 3级以上的攻击可触发回响打击，CD 1秒，伤害 = buff等级 × 施法者末影流派强度
 *
 * @author Love_U
 * @version 2.0.0
 */
public class EnderMarkEffect extends ElementMarkEffect {

    /**
     * 效果ID
     */
    public static final String EFFECT_ID = "ender_mark";

    /**
     * 效果颜色（暗紫色）
     */
    private static final int EFFECT_COLOR = 0x9932CC;

    /**
     * 每级法术强度减少（2%）
     */
    private static final double SPELL_POWER_REDUCTION_PER_LEVEL = -0.02;

    /**
     * 每级冷却缩减降低（4%，即法术冷却 +4%）
     */
    private static final double COOLDOWN_REDUCTION_PER_LEVEL = -0.04;

    /**
     * 回响打击触发所需等级（3级以上）
     */
    private static final int ECHO_TRIGGER_LEVEL = 3;

    /**
     * 回响打击CD（tick）
     * 1秒 = 20 tick
     */
    private static final int ECHO_COOLDOWN_TICKS = 20;

    /**
     * 构造函数
     * 注册每级 -2% 法术强度与 -4% 冷却缩减的属性修饰符
     */
    public EnderMarkEffect() {
        super(ElementType.ENDER, EFFECT_COLOR);

        // 每级 -2% 法术强度
        this.addAttributeModifier(
                AttributeRegistry.SPELL_POWER,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "ender_mark_spell_power"),
                SPELL_POWER_REDUCTION_PER_LEVEL,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );

        // 每级 -4% 冷却缩减（法术冷却 +4%）
        this.addAttributeModifier(
                AttributeRegistry.COOLDOWN_REDUCTION,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "ender_mark_cooldown_reduction"),
                COOLDOWN_REDUCTION_PER_LEVEL,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    @Override
    public String getEffectId() {
        return EFFECT_ID;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // 被动效果，无需每 tick 处理
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // 纯被动效果，属性修饰符已按等级缩放
        return false;
    }

    /**
     * 尝试触发回响打击（由法术伤害事件调用）
     *
     * 触发条件：
     * - 目标携带 3 级以上末影异常
     * - CD 1 秒
     * 伤害 = buff等级 × 末影流派强度（多人游戏由双方玩家流派强度共同计算）
     *
     * @param attacker 攻击者
     * @param target   目标（携带末影异常）
     * @return 是否触发了回响打击
     */
    public static boolean tryTriggerEchoStrike(LivingEntity attacker, LivingEntity target) {
        // 检查目标是否已死亡或正在死亡
        if (target == null || !target.isAlive() || target.isDeadOrDying()) {
            return false;
        }

        // 检查末影异常等级（3级以上）
        int level = ElementMarkData.getMarkLevel(target, ElementType.ENDER);
        if (level < ECHO_TRIGGER_LEVEL) {
            if (Config.ECHO_STRIKE_DEBUG_OUTPUT.get()) {
                ModLogger.spell("[回响打击调试] 目标 {} 的末影标记等级不足: {}级",
                        target.getName().getString(), level);
            }
            return false;
        }

        // 检查CD（1秒）
        if (!ElementMarkData.isCooldownReady(target, ElementType.ENDER, ECHO_COOLDOWN_TICKS)) {
            return false;
        }

        if (Config.ECHO_STRIKE_DEBUG_OUTPUT.get()) {
            ModLogger.spell("[回响打击调试] 触发回响打击！攻击者：{}, 目标：{}",
                    attacker != null ? attacker.getName().getString() : "未知",
                    target.getName().getString());
        }

        // 记录触发时刻（开始CD）
        ElementMarkData.markCooldown(target, ElementType.ENDER);

        // 触发回响打击
        triggerEchoStrike(attacker, target);
        return true;
    }

    /**
     * 触发回响打击
     * 使用原版魔法伤害源，不会触发铁魔法法术系统，不会刷新元素标记
     * 伤害由公式计算：buff等级 × 末影流派强度
     *
     * @param attacker 攻击者
     * @param target   目标
     */
    private static void triggerEchoStrike(LivingEntity attacker, LivingEntity target) {
        if (!(target.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        // 公式伤害：buff等级 × 末影流派强度（多人合并）
        float echoDamage = ElementMarkData.calculateDamage(target, ElementType.ENDER, attacker);
        if (echoDamage <= 0) {
            return;
        }

        target.hurt(serverLevel.damageSources().magic(), echoDamage);

        // 播放末影粒子效果
        playEchoStrikeEffects(serverLevel, attacker, target);
    }

    /**
     * 播放回响打击效果
     *
     * @param serverLevel 服务器世界
     * @param attacker    攻击者
     * @param target      目标
     */
    private static void playEchoStrikeEffects(ServerLevel serverLevel, LivingEntity attacker, LivingEntity target) {
        double tx = target.getX();
        double ty = target.getY() + target.getBbHeight() * 0.5;
        double tz = target.getZ();

        double ax = attacker != null ? attacker.getX() : tx;
        double ay = attacker != null ? attacker.getY() + attacker.getBbHeight() * 0.5 : ty;
        double az = attacker != null ? attacker.getZ() : tz;

        // 末影系颜色：紫色
        Vector3f enderPurple = new Vector3f(0.6f, 0.2f, 0.8f);

        // 1. 目标位置冲击波（铁魔法末影系）
        serverLevel.sendParticles(
                new ShockwaveParticleOptions(enderPurple, 2.0f, true),
                tx, ty, tz,
                1, 0, 0, 0, 0
        );

        // 2. 攻击者位置冲击波（回响源）
        serverLevel.sendParticles(
                new BlastwaveParticleOptions(enderPurple, 1.5f),
                ax, ay, az,
                1, 0, 0, 0, 0
        );

        // 3. 末影粒子连接攻击者和目标（轨迹效果）
        int particleCount = 8;
        for (int i = 0; i < particleCount; i++) {
            double t = i / (double) particleCount;
            double px = ax + (tx - ax) * t;
            double py = ay + (ty - ay) * t;
            double pz = az + (tz - az) * t;

            serverLevel.sendParticles(
                    ParticleRegistry.UNSTABLE_ENDER_PARTICLE.get(),
                    px, py, pz,
                    1,
                    0, 0.05, 0,
                    0.02
            );
        }

        // 4. 目标位置末影不稳定粒子爆发（25 个）
        serverLevel.sendParticles(
                ParticleRegistry.UNSTABLE_ENDER_PARTICLE.get(),
                tx, ty, tz,
                25,
                0.5, 0.5, 0.5,
                0.18
        );

        // 5. 龙息粒子（紫色烟雾，10 个）
        serverLevel.sendParticles(
                ParticleTypes.DRAGON_BREATH,
                tx, ty, tz,
                10,
                0.3, 0.4, 0.3,
                0.05
        );

        // 6. 末影之眼粒子（8 个，围绕目标）
        for (int i = 0; i < 8; i++) {
            double angle = (i / 8.0) * Math.PI * 2;
            double radius = 1.2;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    tx + offsetX, ty, tz + offsetZ,
                    1,
                    0, 0.05, 0,
                    0.02
            );
        }

        // 7. 爆炸粒子
        serverLevel.sendParticles(
                ParticleTypes.EXPLOSION,
                tx, ty, tz,
                2,
                0.2, 0.2, 0.2,
                0.05
        );

        // 音效 - 末影回响打击（铁魔法原版音效）
        serverLevel.playSound(
                null,
                tx, ty, tz,
                SoundRegistry.ECHOING_STRIKE.get(),
                SoundSource.PLAYERS,
                1.0f,
                1.0f + serverLevel.random.nextFloat() * 0.3f
        );
    }
}
