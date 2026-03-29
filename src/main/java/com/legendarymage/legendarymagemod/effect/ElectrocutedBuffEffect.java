package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.LegendaryMage;
import io.redspace.ironsspellbooks.particle.ZapParticleOption;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 触电效果
 * 雷系元素反应给予目标的 Debuff
 * 效果：每 2 秒在自身位置释放一次连锁闪电，伤害固定为 5*等级
 * 使用原版伤害源，不触发铁魔法法术系统
 * 带有白名单机制，不会伤害施法者及其队友
 *
 * @author Love_U
 * @version 0.0.5
 */
public class ElectrocutedBuffEffect extends MobEffect {

    /**
     * 效果 ID
     */
    public static final String EFFECT_ID = "electrocuted_buff";

    /**
     * 基础伤害
     */
    private static final float BASE_DAMAGE = 5.0f;

    /**
     * 触发间隔（tick）
     * 2 秒 = 40 tick
     */
    private static final int TRIGGER_INTERVAL = 40;

    /**
     * 连锁闪电范围
     */
    private static final double CHAIN_RANGE = 8.0;

    /**
     * 连锁闪电最大目标数
     */
    private static final int MAX_CHAIN_TARGETS = 3;

    /**
     * 构造函数
     */
    public ElectrocutedBuffEffect() {
        super(MobEffectCategory.HARMFUL, 0x55FFFF);
    }

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

        // 获取效果实例以检查持续时间
        Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(this);
        MobEffectInstance effectInstance = entity.getEffect(effectHolder);

        if (effectInstance == null) {
            return true;
        }

        // 每 2 秒触发一次连锁闪电
        int duration = effectInstance.getDuration();
        if (duration % TRIGGER_INTERVAL == 0) {
            triggerChainLightning(serverLevel, entity, amplifier);
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // 每 tick 都检查，但在 applyEffectTick 中控制触发频率
        return true;
    }

    /**
     * 触发连锁闪电
     * 使用原版伤害源，不触发铁魔法法术系统
     * 不会伤害玩家和白名单中的实体
     *
     * @param serverLevel 服务器世界
     * @param entity      携带触电 Buff 的实体（施法者）
     * @param amplifier   Buff 等级（0 开始）
     */
    private void triggerChainLightning(ServerLevel serverLevel, LivingEntity entity, int amplifier) {
        // 计算伤害（固定值，不受法术强度影响）
        int buffLevel = amplifier + 1;
        float damage = BASE_DAMAGE * buffLevel;

        Vec3 pos = entity.position();

        // 查找范围内的敌对实体
        AABB searchArea = new AABB(
                pos.x - CHAIN_RANGE, pos.y - CHAIN_RANGE, pos.z - CHAIN_RANGE,
                pos.x + CHAIN_RANGE, pos.y + CHAIN_RANGE, pos.z + CHAIN_RANGE
        );

        List<LivingEntity> nearbyEntities = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                searchArea,
                e -> {
                    // 排除已死亡或正在死亡的实体
                    if (!e.isAlive() || e.isDeadOrDying()) return false;
                    // 排除所有玩家
                    if (e instanceof net.minecraft.world.entity.player.Player) return false;
                    return true;
                }
        );

        // 对最近的几个目标造成伤害
        int targetsHit = 0;
        Vec3 lastPos = pos;
        for (LivingEntity target : nearbyEntities) {
            if (targetsHit >= MAX_CHAIN_TARGETS) {
                break;
            }

            // 使用 hurt 方法造成伤害
            target.hurt(serverLevel.damageSources().magic(), damage);

            // 播放连锁闪电特效
            playChainLightningEffect(serverLevel, lastPos, target.position());

            // 播放目标位置的闪电效果
            playLightningEffect(serverLevel, target);

            lastPos = target.position();
            targetsHit++;
        }

        // 播放中心位置的闪电爆发效果
        playLightningEffect(serverLevel, entity);
    }

    /**
     * 播放闪电效果
     *
     * @param serverLevel 服务器世界
     * @param target      目标实体
     */
    private void playLightningEffect(ServerLevel serverLevel, LivingEntity target) {
        // 铁魔法连锁闪电法术的粒子效果
        Vec3 targetPos = target.position().add(0, target.getBbHeight() / 2, 0);

        // 播放铁魔法的 Zap 粒子（从目标位置到自身的闪电）
        serverLevel.sendParticles(
                new ZapParticleOption(targetPos),
                target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                1, 0, 0, 0, 0
        );

        // 播放电力粒子爆发
        serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.END_ROD,
                target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                10,
                0.3, 0.3, 0.3,
                0.1
        );

        // 播放音效
        serverLevel.playSound(
                null,
                target.getX(), target.getY(), target.getZ(),
                SoundRegistry.CHAIN_LIGHTNING_CHAIN.get(),
                SoundSource.HOSTILE,
                1.0f,
                1.0f
        );
    }

    /**
     * 播放连锁闪电特效（连接两个位置的闪电链）
     * 使用铁魔法的 ZapParticleOption，这是铁魔法连锁闪电法术的原版粒子
     *
     * @param serverLevel 服务器世界
     * @param startPos    起始位置
     * @param endPos      结束位置
     */
    private void playChainLightningEffect(ServerLevel serverLevel, Vec3 startPos, Vec3 endPos) {
        // 使用铁魔法的 Zap 粒子，这是连锁闪电法术的专用粒子
        // 它会在 startPos 和 endPos 之间生成一条闪电链
        serverLevel.sendParticles(
                new ZapParticleOption(endPos.add(0, 0.5, 0)),
                startPos.x, startPos.y + 0.5, startPos.z,
                1, 0, 0, 0, 0
        );
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
