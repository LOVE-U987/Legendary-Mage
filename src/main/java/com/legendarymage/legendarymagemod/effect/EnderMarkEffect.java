package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.element.ElementType;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.particle.ShockwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

/**
 * 末影标记效果（末影异常）
 * 3 级后有 50% 的几率攻击追加一次回响打击
 * 回响打击使用原版伤害源，无法更新元素标记
 *
 * @author Love_U
 * @version 1.0.4
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
     * 回响打击触发概率
     */
    private static final double ECHO_STRIKE_CHANCE = 0.5;

    /**
     * 回响打击伤害比例（原始伤害的50%）
     */
    private static final float ECHO_DAMAGE_RATIO = 0.5f;

    /**
     * 构造函数
     */
    public EnderMarkEffect() {
        super(ElementType.ENDER, EFFECT_COLOR);
    }

    @Override
    public String getEffectId() {
        return EFFECT_ID;
    }

    /**
     * 当效果被添加时调用
     * 
     * @param entity 实体
     * @param amplifier 效果等级（0=1级，1=2级，2=3级）
     */
    @Override
    public void onEffectAdded(LivingEntity entity, int amplifier) {
        // 效果添加时的处理
    }

    /**
     * 当效果被移除时调用
     *
     * @param entity 实体
     * @param amplifier 效果等级（0=1 级，1=2 级，2=3 级）
     */
    public void onEffectRemoved(LivingEntity entity, int amplifier) {
        // 清理工作（如果需要）
    }

    /**
     * 尝试触发回响打击
     * 由外部攻击事件调用
     *
     * @param attacker 攻击者
     * @param target 目标
     * @param originalDamage 原始伤害
     * @return 是否触发了回响打击
     */
    public static boolean tryTriggerEchoStrike(LivingEntity attacker, LivingEntity target, float originalDamage) {
        // 检查攻击者和目标是否已死亡或正在死亡
        if (!attacker.isAlive() || attacker.isDeadOrDying() ||
            !target.isAlive() || target.isDeadOrDying()) {
            LegendaryMage.LOGGER.info("[回响打击调试] 攻击者或目标已死亡");
            return false;
        }

        // 检查目标是否有 3 级末影标记
        MobEffect enderMarkEffect = ModEffects.ENDER_MARK.get();
        Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(enderMarkEffect);

        if (!target.hasEffect(effectHolder)) {
            LegendaryMage.LOGGER.info("[回响打击调试] 目标 {} 没有末影标记", target.getName().getString());
            return false;
        }

        MobEffectInstance effectInstance = target.getEffect(effectHolder);
        if (effectInstance == null) {
            LegendaryMage.LOGGER.info("[回响打击调试] 目标 {} 的末影标记实例为 null", target.getName().getString());
            return false;
        }

        // 检查是否为 3 级标记（amplifier = 2）
        int amplifier = effectInstance.getAmplifier();
        if (amplifier < MAX_LEVEL) {
            LegendaryMage.LOGGER.info("[回响打击调试] 目标 {} 有末影标记但等级不足：amplifier={}",
                target.getName().getString(), amplifier);
            return false;
        }

        // 50%几率触发
        double randomValue = Math.random();
        if (randomValue >= ECHO_STRIKE_CHANCE) {
            LegendaryMage.LOGGER.info("[回响打击调试] 回响打击未触发（几率失败）：random={}, threshold={}",
                randomValue, ECHO_STRIKE_CHANCE);
            return false;
        }

        LegendaryMage.LOGGER.info("[回响打击调试] 触发回响打击！攻击者：{}, 目标：{}, 伤害：{}",
            attacker.getName().getString(), target.getName().getString(), originalDamage);

        // 触发回响打击
        triggerEchoStrike(attacker, target, originalDamage);
        return true;
    }

    /**
     * 触发回响打击
     * 使用原版伤害源，不触发铁魔法法术系统
     *
     * @param attacker 攻击者
     * @param target 目标
     * @param originalDamage 原始伤害
     */
    private static void triggerEchoStrike(LivingEntity attacker, LivingEntity target, float originalDamage) {
        // 再次检查攻击者和目标是否已死亡或正在死亡
        if (!attacker.isAlive() || attacker.isDeadOrDying() ||
            !target.isAlive() || target.isDeadOrDying()) {
            return;
        }

        if (!(attacker.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        // 计算回响伤害（原始伤害的50%，固定值，不受法术强度影响）
        float echoDamage = originalDamage * ECHO_DAMAGE_RATIO;

        // 使用原版魔法伤害源（不会触发铁魔法法术系统，不会刷新元素标记）
        target.hurt(serverLevel.damageSources().magic(), echoDamage);

        // 播放末影粒子效果
        playEchoStrikeEffects(serverLevel, attacker, target);
    }

    /**
     * 播放回响打击效果
     *
     * @param serverLevel 服务器世界
     * @param attacker 攻击者
     * @param target 目标
     */
    private static void playEchoStrikeEffects(ServerLevel serverLevel, LivingEntity attacker, LivingEntity target) {
        double tx = target.getX();
        double ty = target.getY() + target.getBbHeight() * 0.5;
        double tz = target.getZ();

        double ax = attacker.getX();
        double ay = attacker.getY() + attacker.getBbHeight() * 0.5;
        double az = attacker.getZ();

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
                net.minecraft.core.particles.ParticleTypes.DRAGON_BREATH,
                tx, ty, tz,
                10,
                0.3, 0.4, 0.3,
                0.05
        );

        // 6. 末影之眼粒子（8 个，围绕目标）- 使用 END_ROD 替代
        for (int i = 0; i < 8; i++) {
            double angle = (i / 8.0) * Math.PI * 2;
            double radius = 1.2;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.END_ROD,
                    tx + offsetX, ty, tz + offsetZ,
                    1,
                    0, 0.05, 0,
                    0.02
            );
        }

        // 7. 爆炸粒子
        serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.EXPLOSION,
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
