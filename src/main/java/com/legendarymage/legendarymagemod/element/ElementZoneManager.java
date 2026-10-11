package com.legendarymage.legendarymagemod.element;

import com.legendarymage.legendarymagemod.Config;
import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.ModLogger;
import com.legendarymage.legendarymagemod.effect.ElementDamageGuard;
import com.legendarymage.legendarymagemod.effect.ElementMarkEffect;
import com.legendarymage.legendarymagemod.effect.ModEffects;
import io.redspace.ironsspellbooks.particle.FogParticleOptions;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 元素区域管理器
 * 自制区域效果系统：替代原版 AreaEffectCloud（原版药水云对 owner 也生效，无法排除施法者）
 *
 * 【功能】
 * - 区域数据内存管理：位置/半径/剩余时长/施法者/效果类型
 * - LevelTickEvent 驱动：每秒对圈内实体施加效果
 * - 排除规则：施法者自身、施法者队友、施法者召唤物、创造/旁观模式玩家
 * - 定期清理过期区域，避免内存泄漏
 *
 * @author Love_U
 * @version 1.0.0
 */
public final class ElementZoneManager {

    /**
     * 区域类型
     */
    public enum ZoneType {
        /**
         * 霜毒凝雾（冰+毒）：圈内每秒叠加原版中毒
         */
        FROST_FOG,

        /**
         * 邪瘟毒云（邪术+毒）：圈内持续被施加邪术标记（连锁反应机会）
         */
        PLAGUE_CLOUD,

        /**
         * 圣冰结晶（冰+神圣）：圈内持续减速
         */
        HOLY_ICE
    }

    /**
     * 区域数据（可变类，剩余时长每 tick 递减）
     */
    private static final class ZoneData {
        final Vec3 center;
        final double radius;
        int remainingTicks;
        /** 下次施加效果的刻（long，避免 getGameTime 长期运行后的整型溢出） */
        long nextApplyTick;
        final UUID casterUUID;
        final ZoneType type;

        ZoneData(Vec3 center, double radius, int durationTicks, long currentTick, UUID casterUUID, ZoneType type) {
            this.center = center;
            this.radius = radius;
            this.remainingTicks = durationTicks;
            this.nextApplyTick = currentTick;
            this.casterUUID = casterUUID;
            this.type = type;
        }
    }

    /**
     * 活跃区域记录：维度 -> 区域列表
     */
    private static final Map<ResourceKey<net.minecraft.world.level.Level>, List<ZoneData>> ACTIVE_ZONES = new HashMap<>();

    /**
     * 霜毒雾区颜色（青绿）
     */
    private static final Vector3f FROST_COLOR = new Vector3f(0.4f, 0.9f, 0.7f);

    /**
     * 邪瘟毒云颜色（深绿）
     */
    private static final Vector3f PLAGUE_COLOR = new Vector3f(0.3f, 0.6f, 0.4f);

    /**
     * 圣冰结晶颜色（冰蓝）
     */
    private static final Vector3f HOLY_ICE_COLOR = new Vector3f(0.5f, 0.8f, 1.0f);

    /**
     * 区域内效果的刷新时长（tick）
     * 40 tick = 2 秒，覆盖每秒一次的施加间隔，避免效果闪烁
     */
    private static final int REFRESH_DURATION_TICKS = 40;

    private ElementZoneManager() {
        // 禁止实例化
    }

    /**
     * 生成元素区域
     *
     * @param serverLevel 服务器世界
     * @param center      区域中心（目标位置）
     * @param caster      施法者（排除对象）
     * @param type        区域类型
     * @param radius      半径（格）
     * @param durationSeconds 持续时间（秒）
     */
    public static void spawnZone(ServerLevel serverLevel, Vec3 center, LivingEntity caster,
                                  ZoneType type, double radius, int durationSeconds) {
        if (caster == null) {
            return;
        }

        ZoneData zone = new ZoneData(
                center,
                radius,
                durationSeconds * 20,
                serverLevel.getGameTime(),
                caster.getUUID(),
                type
        );

        ACTIVE_ZONES.computeIfAbsent(serverLevel.dimension(), k -> new ArrayList<>()).add(zone);

        debugLog(String.format("生成区域: %s, 中心 (%.1f, %.1f, %.1f), 半径 %.1f, 持续 %d秒, 施法者 %s",
                type, center.x, center.y, center.z, radius, durationSeconds, caster.getName().getString()));
    }

    /**
     * 每 tick 更新区域（由 LevelTickEvent.Post 驱动）
     * 每秒对圈内实体施加效果，排除施法者/队友/召唤物/创造旁观玩家
     *
     * @param serverLevel 服务器世界
     */
    public static void tick(ServerLevel serverLevel) {
        List<ZoneData> zones = ACTIVE_ZONES.get(serverLevel.dimension());
        if (zones == null || zones.isEmpty()) {
            return;
        }

        long now = serverLevel.getGameTime();
        List<ZoneData> remaining = new ArrayList<>(zones.size());

        for (ZoneData zone : zones) {
            zone.remainingTicks--;
            if (zone.remainingTicks <= 0) {
                debugLog(String.format("区域过期: %s @ (%.1f, %.1f, %.1f)",
                        zone.type, zone.center.x, zone.center.y, zone.center.z));
                continue;
            }

            // 每秒（20 tick）施加一次效果
            if (now >= zone.nextApplyTick) {
                zone.nextApplyTick = now + 20;
                applyZoneEffects(serverLevel, zone);
            }

            remaining.add(zone);
        }

        if (remaining.isEmpty()) {
            ACTIVE_ZONES.remove(serverLevel.dimension());
        } else {
            ACTIVE_ZONES.put(serverLevel.dimension(), remaining);
        }
    }

    /**
     * 对圈内实体施加区域效果
     * 排除：施法者自身、队友、召唤物、创造/旁观模式玩家
     *
     * @param serverLevel 服务器世界
     * @param zone        区域数据
     */
    private static void applyZoneEffects(ServerLevel serverLevel, ZoneData zone) {
        LivingEntity caster = serverLevel.getEntity(zone.casterUUID) instanceof LivingEntity living ? living : null;

        AABB box = new AABB(
                zone.center.x - zone.radius, zone.center.y - 0.5, zone.center.z - zone.radius,
                zone.center.x + zone.radius, zone.center.y + 3.0, zone.center.z + zone.radius
        );

        List<LivingEntity> entities = serverLevel.getEntitiesOfClass(LivingEntity.class, box);

        // 每秒播一批雾粒子（持续感）
        playZoneAmbient(serverLevel, zone);

        int affected = 0;
        for (LivingEntity entity : entities) {
            if (entity == null || !entity.isAlive()) {
                continue;
            }

            // 排除施法者自身
            if (entity.getUUID().equals(zone.casterUUID)) {
                continue;
            }

            // 排除施法者队友与召唤物
            if (ElementMarkData.isExcludedFromAttack(entity, caster)) {
                continue;
            }

            // 排除创造/旁观模式玩家
            if (ElementDamageGuard.shouldSkipDamage(entity)) {
                continue;
            }

            applyEffectToEntity(serverLevel, entity, zone);
            affected++;
        }

        if (affected > 0) {
            debugLog(String.format("区域 %s 影响 %d 个实体", zone.type, affected));
        }
    }

    /**
     * 对单个实体施加对应区域类型的效果
     *
     * @param serverLevel 服务器世界
     * @param entity      圈内实体
     * @param zone        区域数据
     */
    private static void applyEffectToEntity(ServerLevel serverLevel, LivingEntity entity, ZoneData zone) {
        switch (zone.type) {
            case FROST_FOG -> {
                // 圈内每秒叠加原版中毒（40 tick = 2 秒刷新，覆盖每秒的施加间隔，避免效果闪烁）
                int poisonLevel = Config.FROST_FOG_POISON_LEVEL.get();
                entity.addEffect(new MobEffectInstance(MobEffects.POISON, REFRESH_DURATION_TICKS, poisonLevel - 1));
            }
            case PLAGUE_CLOUD -> {
                // 圈内持续被施加邪术标记（1 级即可参与反应，制造连锁反应机会）
                MobEffect eldritchMark = ModEffects.ELDRITCH_MARK.get();
                Holder<MobEffect> markHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(eldritchMark);
                entity.addEffect(new MobEffectInstance(
                        markHolder,
                        ElementMarkEffect.BASE_DURATION,
                        0
                ));
            }
            case HOLY_ICE -> {
                // 圈内持续减速（40 tick = 2 秒刷新，覆盖每秒的施加间隔，避免效果闪烁）
                int slowAmp = Config.HOLY_ICE_SLOW_AMPLIFIER.get();
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, REFRESH_DURATION_TICKS, slowAmp));
            }
        }
    }

    /**
     * 播放区域持续雾粒子（每次施加效果时调用，制造持续感）
     *
     * @param serverLevel 服务器世界
     * @param zone        区域数据
     */
    private static void playZoneAmbient(ServerLevel serverLevel, ZoneData zone) {
        Vector3f color = switch (zone.type) {
            case FROST_FOG -> FROST_COLOR;
            case PLAGUE_CLOUD -> PLAGUE_COLOR;
            case HOLY_ICE -> HOLY_ICE_COLOR;
        };

        for (int i = 0; i < 6; i++) {
            double angle = serverLevel.random.nextDouble() * Math.PI * 2;
            double distance = serverLevel.random.nextDouble() * zone.radius;
            serverLevel.sendParticles(
                    new FogParticleOptions(color, 1.2f),
                    zone.center.x + Math.cos(angle) * distance,
                    zone.center.y + 0.2 + serverLevel.random.nextDouble() * 1.2,
                    zone.center.z + Math.sin(angle) * distance,
                    1, 0.1, 0.15, 0.1, 0.01
            );
        }
    }

    /**
     * 清除指定维度的所有区域（世界卸载时调用，防内存泄漏）
     *
     * @param dimension 维度
     */
    public static void clearZones(ResourceKey<net.minecraft.world.level.Level> dimension) {
        ACTIVE_ZONES.remove(dimension);
    }

    /**
     * 输出调试日志
     *
     * @param message 日志消息
     */
    private static void debugLog(String message) {
        if (Config.ELEMENT_REACTION_DEBUG_OUTPUT.get()) {
            ModLogger.element("[元素区域] {}", message);
        }
    }
}
