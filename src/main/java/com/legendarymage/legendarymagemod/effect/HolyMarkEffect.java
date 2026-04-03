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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 神圣系标记效果（光明异常）
 * 3级标记时长结束时，受到一次神圣打击，伤害固定为20
 * 有5秒CD，不会更新元素标记
 * 使用原版伤害源，不触发铁魔法法术系统
 * 
 * @author Love_U
 * @version 1.0.4
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
     * 神圣打击伤害（固定值）
     */
    private static final float HOLY_STRIKE_DAMAGE = 20.0f;

    /**
     * CD时间（tick）
     * 5秒 = 100 tick
     */
    private static final int COOLDOWN_TICKS = 100;

    /**
     * 触发阈值（tick）
     * 当效果持续时间小于等于此值时触发
     */
    private static final int TRIGGER_THRESHOLD = 1;

    /**
     * 玩家CD记录
     */
    private static final Map<UUID, Long> playerCooldowns = new HashMap<>();

    /**
     * 记录已经触发过的实体（防止重复触发）
     */
    private static final Map<UUID, Boolean> triggeredEntities = new HashMap<>();

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

    /**
     * 每tick应用效果
     * 检查效果是否即将结束，如果是3级则触发神圣打击
     * 
     * @param entity 实体
     * @param amplifier 效果等级（0=1级，1=2级，2=3级）
     * @return 是否继续应用效果
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

        // 检查是否为3级标记（amplifier = 2）
        if (amplifier < MAX_LEVEL) {
            return true;
        }

        // 获取效果实例
        Holder<net.minecraft.world.effect.MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(this);
        MobEffectInstance effectInstance = entity.getEffect(effectHolder);

        if (effectInstance == null) {
            return true;
        }

        // 获取实体UUID
        UUID entityId = entity.getUUID();

        // 检查效果持续时间，当效果即将结束且尚未触发时触发神圣打击
        int duration = effectInstance.getDuration();
        if (duration <= TRIGGER_THRESHOLD) {
            // 检查是否已经触发过（防止重复触发）
            if (!triggeredEntities.containsKey(entityId) || !triggeredEntities.get(entityId)) {
                triggeredEntities.put(entityId, true);
                applyHolyStrike(entity);
            }
        } else {
            // 重置触发状态（如果效果被刷新了）
            triggeredEntities.remove(entityId);
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // 每tick都检查
        return true;
    }

    /**
     * 应用神圣打击
     * 使用原版伤害源，不触发铁魔法法术系统
     * 
     * @param entity 目标实体
     */
    private void applyHolyStrike(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        // 检查CD
        UUID entityId = entity.getUUID();
        long currentTime = entity.level().getGameTime();
        
        if (playerCooldowns.containsKey(entityId)) {
            long lastHitTime = playerCooldowns.get(entityId);
            if (currentTime - lastHitTime < COOLDOWN_TICKS) {
                // CD中，不触发
                return;
            }
        }

        // 记录触发时间
        playerCooldowns.put(entityId, currentTime);

        // 使用原版魔法伤害源（不会触发铁魔法法术系统，不会刷新元素标记）
        // 伤害固定为20，不受任何属性影响
        entity.hurt(serverLevel.damageSources().magic(), HOLY_STRIKE_DAMAGE);

        // 播放粒子效果
        playHolyStrikeEffects(serverLevel, entity);
    }

    /**
     * 尝试触发神圣打击（外部调用）
     * 当目标受到任何伤害（包括法术伤害）时调用
     *
     * @param target 目标实体
     * @return 是否触发了神圣打击
     */
    public static boolean tryTriggerHolyStrike(LivingEntity target) {
        // 检查目标是否已死亡或正在死亡
        if (!target.isAlive() || target.isDeadOrDying()) {
            return false;
        }

        // 检查目标是否有3级光明标记
        net.minecraft.world.effect.MobEffect holyMarkEffect = ModEffects.HOLY_MARK.get();
        net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effectHolder =
            net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(holyMarkEffect);

        if (!target.hasEffect(effectHolder)) {
            return false;
        }

        net.minecraft.world.effect.MobEffectInstance effectInstance = target.getEffect(effectHolder);
        if (effectInstance == null) {
            return false;
        }

        // 检查是否为3级标记（amplifier = 2）
        int amplifier = effectInstance.getAmplifier();
        if (amplifier < MAX_LEVEL) {
            LegendaryMage.LOGGER.info("[神圣打击调试] 目标 {} 有光明标记但等级不足: amplifier={}",
                target.getName().getString(), amplifier);
            return false;
        }

        // 检查CD
        UUID entityId = target.getUUID();
        long currentTime = target.level().getGameTime();

        if (playerCooldowns.containsKey(entityId)) {
            long lastHitTime = playerCooldowns.get(entityId);
            if (currentTime - lastHitTime < COOLDOWN_TICKS) {
                // CD中，不触发
                LegendaryMage.LOGGER.info("[神圣打击调试] 目标 {} 在CD中", target.getName().getString());
                return false;
            }
        }

        LegendaryMage.LOGGER.info("[神圣打击调试] 触发神圣打击！目标: {}", target.getName().getString());

        // 记录触发时间
        playerCooldowns.put(entityId, currentTime);

        // 触发神圣打击
        triggerHolyStrike(target);
        return true;
    }

    /**
     * 触发神圣打击（静态方法）
     *
     * @param target 目标实体
     */
    private static void triggerHolyStrike(LivingEntity target) {
        // 再次检查目标是否已死亡或正在死亡
        if (!target.isAlive() || target.isDeadOrDying()) {
            return;
        }

        if (!(target.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        // 使用原版魔法伤害源（不会触发铁魔法法术系统，不会刷新元素标记）
        // 伤害固定为20，不受任何属性影响
        target.hurt(serverLevel.damageSources().magic(), HOLY_STRIKE_DAMAGE);

        // 播放粒子效果
        playHolyStrikeEffectsStatic(serverLevel, target);
    }

    /**
     * 播放神圣打击效果（静态版本）
     *
     * @param serverLevel 服务器世界
     * @param entity 目标实体
     */
    private static void playHolyStrikeEffectsStatic(ServerLevel serverLevel, LivingEntity entity) {
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
                    net.minecraft.core.particles.ParticleTypes.END_ROD,
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
                    net.minecraft.core.particles.ParticleTypes.WITCH,
                    x + offsetX, y + 0.5, z + offsetZ,
                    1,
                    0, 0.05, 0,
                    0.02
            );
        }

        // 6. 灵魂粒子爆发（6 个）
        serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.SOUL,
                x, y, z,
                6,
                0.4, 0.4, 0.4,
                0.05
        );

        // 7. 爆炸粒子
        serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.EXPLOSION,
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

    /**
     * 播放神圣打击效果
     * 
     * @param serverLevel 服务器世界
     * @param entity 目标实体
     */
    private void playHolyStrikeEffects(ServerLevel serverLevel, LivingEntity entity) {
        // 播放阳光粒子效果（代表神圣）
        serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.END_ROD,
                entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(),
                20,
                0.3, 0.5, 0.3,
                0.1
        );

        // 播放闪光效果
        serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.FLASH,
                entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(),
                1,
                0, 0, 0,
                0
        );
    }
}
