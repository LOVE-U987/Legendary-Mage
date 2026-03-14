package com.legendarymage.legendarymagemod.spell;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.Config;
import com.legendarymage.legendarymagemod.sound.ModSounds;

import java.util.*;

/**
 * 暴风雪管理器
 * 管理暴风雪区域的创建、持续伤害和粒子效果
 * 
 * @author Love_U
 * @version 0.0.1
 */
public class BlizzardManager {

    /**
     * 粒子效果间隔（tick）
     */
    private static final int PARTICLE_INTERVAL_TICKS = 5;

    /**
     * 冰冻伤害类型资源位置
     */
    private static final ResourceLocation ICE_MAGIC_DAMAGE = ResourceLocation.fromNamespaceAndPath(
            "irons_spellbooks", "ice_magic");

    /**
     * 存储所有活跃的暴风雪区域
     * 键：世界维度ID，值：该维度的暴风雪列表
     */
    private static final Map<String, List<BlizzardZone>> activeBlizzards = new HashMap<>();

    /**
     * 创建暴风雪区域
     *
     * @param level          服务器世界
     * @param center         中心位置
     * @param range          范围（格）
     * @param durationTicks  持续时间（tick）
     * @param damage         每次伤害值
     * @param caster         施法者
     */
    public static void createBlizzard(ServerLevel level, Vec3 center, double range, 
                                      int durationTicks, float damage, LivingEntity caster) {
        String dimensionId = level.dimension().location().toString();
        
        // 创建新的暴风雪区域
        BlizzardZone blizzard = new BlizzardZone(center, range, durationTicks, damage, caster);
        
        // 添加到活跃列表
        activeBlizzards.computeIfAbsent(dimensionId, k -> new ArrayList<>()).add(blizzard);
        
        LegendaryMage.LOGGER.info("[暴风雪] 已在 {} 创建暴风雪区域，范围: {}格，持续时间: {}tick，伤害: {}", 
                dimensionId, String.format("%.1f", range), durationTicks, damage);
    }

    /**
     * 更新所有暴风雪区域
     * 每tick调用一次
     *
     * @param level 服务器世界
     */
    public static void tick(ServerLevel level) {
        String dimensionId = level.dimension().location().toString();
        List<BlizzardZone> blizzards = activeBlizzards.get(dimensionId);
        
        if (blizzards == null || blizzards.isEmpty()) {
            return;
        }
        
        // 遍历所有暴风雪区域
        Iterator<BlizzardZone> iterator = blizzards.iterator();
        while (iterator.hasNext()) {
            BlizzardZone blizzard = iterator.next();
            
            // 更新暴风雪
            blizzard.tick(level);
            
            // 检查是否结束
            if (blizzard.isFinished()) {
                iterator.remove();
                LegendaryMage.LOGGER.debug("[暴风雪] 暴风雪区域已结束");
            }
        }
        
        // 如果该维度没有暴风雪了，清理map
        if (blizzards.isEmpty()) {
            activeBlizzards.remove(dimensionId);
        }
    }

    /**
     * 获取指定位置的暴风雪区域
     *
     * @param level 服务器世界
     * @param pos   位置
     * @return 包含该位置的暴风雪区域列表
     */
    public static List<BlizzardZone> getBlizzardsAt(ServerLevel level, Vec3 pos) {
        String dimensionId = level.dimension().location().toString();
        List<BlizzardZone> blizzards = activeBlizzards.get(dimensionId);
        
        if (blizzards == null) {
            return Collections.emptyList();
        }
        
        List<BlizzardZone> result = new ArrayList<>();
        for (BlizzardZone blizzard : blizzards) {
            if (blizzard.isInside(pos)) {
                result.add(blizzard);
            }
        }
        return result;
    }

    /**
     * 清除所有暴风雪区域
     *
     * @param level 服务器世界
     */
    public static void clearAll(ServerLevel level) {
        String dimensionId = level.dimension().location().toString();
        activeBlizzards.remove(dimensionId);
        LegendaryMage.LOGGER.info("[暴风雪] 已清除 {} 的所有暴风雪区域", dimensionId);
    }

    /**
     * 暴风雪区域类
     * 表示一个暴风雪区域的属性和状态
     */
    public static class BlizzardZone {
        
        /**
         * 中心位置
         */
        private final Vec3 center;
        
        /**
         * 范围（格）
         */
        private final double range;
        
        /**
         * 总持续时间（tick）
         */
        private final int totalDurationTicks;
        
        /**
         * 每次伤害值
         */
        private final float damage;
        
        /**
         * 施法者
         */
        private final LivingEntity caster;
        
        /**
         * 已过去的tick数
         */
        private int elapsedTicks;
        
        /**
         * 距离上次伤害的tick数
         */
        private int ticksSinceLastDamage;
        
        /**
         * 距离上次粒子效果的tick数
         */
        private int ticksSinceLastParticles;
        
        /**
         * 距离上次音效的tick数
         */
        private int ticksSinceLastSound;
        
        /**
         * 音效播放间隔（tick）
         * 每11秒播放一次环境音效
         */
        private static final int SOUND_INTERVAL_TICKS = 220;

        /**
         * 构造函数
         *
         * @param center          中心位置
         * @param range           范围（格）
         * @param durationTicks   持续时间（tick）
         * @param damage          每次伤害值
         * @param caster          施法者
         */
        public BlizzardZone(Vec3 center, double range, int durationTicks, float damage, LivingEntity caster) {
            this.center = center;
            this.range = range;
            this.totalDurationTicks = durationTicks;
            this.damage = damage;
            this.caster = caster;
            this.elapsedTicks = 0;
            this.ticksSinceLastDamage = 0;
            this.ticksSinceLastParticles = 0;
            this.ticksSinceLastSound = 0;
        }

        /**
         * 更新暴风雪区域
         *
         * @param level 服务器世界
         */
        public void tick(ServerLevel level) {
            elapsedTicks++;
            ticksSinceLastDamage++;
            ticksSinceLastParticles++;
            ticksSinceLastSound++;
            
            // 播放粒子效果
            if (ticksSinceLastParticles >= PARTICLE_INTERVAL_TICKS) {
                playAmbientParticles(level);
                ticksSinceLastParticles = 0;
            }
            
            // 播放环境音效
            if (ticksSinceLastSound >= SOUND_INTERVAL_TICKS) {
                playAmbientSound(level);
                ticksSinceLastSound = 0;
            }
            
            // 造成伤害（使用配置的伤害间隔）
            if (ticksSinceLastDamage >= Config.BLIZZARD_DAMAGE_INTERVAL_TICKS.get()) {
                dealDamage(level);
                ticksSinceLastDamage = 0;
            }
        }
        
        /**
         * 播放环境音效
         *
         * @param level 服务器世界
         */
        private void playAmbientSound(ServerLevel level) {
            // 播放暴风雪环境音效
            level.playSound(
                    null,
                    center.x,
                    center.y,
                    center.z,
                    ModSounds.BLIZZARD_AMBIENT.get(),
                    net.minecraft.sounds.SoundSource.WEATHER,
                    0.8f,
                    1.0f
            );
        }

        /**
         * 播放环境粒子效果
         *
         * @param level 服务器世界
         */
        private void playAmbientParticles(ServerLevel level) {
            BlizzardParticles.playAmbientEffect(level, center, range);
        }

        /**
         * 对区域内的敌人造成伤害
         *
         * @param level 服务器世界
         */
        private void dealDamage(ServerLevel level) {
            // 创建搜索区域
            AABB searchArea = new AABB(
                    center.x - range, center.y - range, center.z - range,
                    center.x + range, center.y + range, center.z + range
            );
            
            // 获取范围内的所有生物 - 使用新的ArrayList来避免并发修改
            List<LivingEntity> targets = new ArrayList<>(level.getEntitiesOfClass(
                    LivingEntity.class,
                    searchArea,
                    target -> canTargetEntity(target) && isInside(target.position()) && target.isAlive()
            ));
            
            // 获取冰冻伤害类型
            ResourceKey<DamageType> iceDamageKey = ResourceKey.create(
                    Registries.DAMAGE_TYPE, ICE_MAGIC_DAMAGE);
            DamageSource iceDamageSource = new DamageSource(
                    level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(iceDamageKey),
                    caster
            );
            
            // 对每个目标造成伤害
            // 使用索引遍历而不是迭代器，以避免ConcurrentModificationException
            for (int i = 0; i < targets.size(); i++) {
                LivingEntity target = targets.get(i);
                
                // 再次检查目标是否仍然存活且有效
                if (target == null || !target.isAlive() || target.isRemoved()) {
                    continue;
                }
                
                try {
                    // 造成冰冻伤害
                    target.hurt(iceDamageSource, damage);
                    
                    // 如果目标仍然存活，添加缓慢效果
                    if (target.isAlive() && !target.isRemoved()) {
                        target.addEffect(new MobEffectInstance(
                                MobEffects.MOVEMENT_SLOWDOWN,
                                Config.BLIZZARD_SLOWNESS_DURATION_TICKS.get(),
                                Config.BLIZZARD_SLOWNESS_AMPLIFIER.get(),
                                false,
                                true,
                                true
                        ));
                        
                        // 播放受击粒子效果
                        BlizzardParticles.playHitEffect(level, target.position(), target.getBbHeight(), target.getBbWidth());
                    }
                } catch (Exception e) {
                    // 捕获任何异常，防止崩溃
                    LegendaryMage.LOGGER.error("[暴风雪] 对目标造成伤害时发生错误: {}", e.getMessage());
                }
            }
        }

        /**
         * 判断是否可以目标指定实体
         *
         * @param target 目标
         * @return 是否可以目标
         */
        private boolean canTargetEntity(LivingEntity target) {
            // 不能目标施法者自己
            if (target == caster) {
                return false;
            }
            
            // 从配置读取是否影响友军和召唤物
            boolean affectAllies = Config.BLIZZARD_AFFECT_ALLIES.get();
            boolean affectSummons = Config.BLIZZARD_AFFECT_SUMMONS.get();
            
            // 玩家施法时的特殊处理
            if (caster instanceof Player playerCaster) {
                // 不能目标其他玩家（除非在PVP中且配置允许）
                if (target instanceof Player targetPlayer) {
                    // 如果是同一个玩家，不目标
                    if (playerCaster == targetPlayer) {
                        return false;
                    }
                    // 根据配置决定是否影响友军
                    if (!affectAllies) {
                        return playerCaster.canHarmPlayer(targetPlayer);
                    }
                    return true;
                }
                
                // 检查目标是否是召唤物
                if (!affectSummons && target instanceof OwnableEntity ownable) {
                    // 如果是施法者拥有的召唤物，不目标
                    if (ownable.getOwnerUUID() != null && ownable.getOwnerUUID().equals(playerCaster.getUUID())) {
                        return false;
                    }
                }
                
                // 可以目标所有其他非玩家实体
                return true;
            }
            
            // 非玩家施法者：检查是否为敌对关系
            // 如果目标是玩家，可以目标
            if (target instanceof Player) {
                return true;
            }
            
            // 检查目标是否是召唤物
            if (!affectSummons && target instanceof OwnableEntity ownable) {
                // 如果是施法者拥有的召唤物，不目标
                if (ownable.getOwnerUUID() != null && caster.getUUID() != null && 
                    ownable.getOwnerUUID().equals(caster.getUUID())) {
                    return false;
                }
            }
            
            // 根据配置决定是否影响友军
            if (!affectAllies) {
                // 检查是否为同一队伍
                return caster.isAlliedTo(target);
            }
            
            return true;
        }

        /**
         * 检查位置是否在暴风雪区域内
         *
         * @param pos 位置
         * @return 是否在区域内
         */
        public boolean isInside(Vec3 pos) {
            double dx = pos.x - center.x;
            double dy = pos.y - center.y;
            double dz = pos.z - center.z;
            return (dx * dx + dy * dy + dz * dz) <= (range * range);
        }

        /**
         * 检查暴风雪是否已结束
         *
         * @return 是否结束
         */
        public boolean isFinished() {
            return elapsedTicks >= totalDurationTicks;
        }

        /**
         * 获取剩余时间（tick）
         *
         * @return 剩余时间
         */
        public int getRemainingTicks() {
            return Math.max(0, totalDurationTicks - elapsedTicks);
        }

        /**
         * 获取中心位置
         *
         * @return 中心位置
         */
        public Vec3 getCenter() {
            return center;
        }

        /**
         * 获取范围
         *
         * @return 范围（格）
         */
        public double getRange() {
            return range;
        }

        /**
         * 获取施法者
         *
         * @return 施法者
         */
        public LivingEntity getCaster() {
            return caster;
        }
    }
}
