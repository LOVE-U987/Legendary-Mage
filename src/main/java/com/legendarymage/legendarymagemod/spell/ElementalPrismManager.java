package com.legendarymage.legendarymagemod.spell;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.element.ElementReactionManager;
import com.legendarymage.legendarymagemod.element.ElementType;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * 元素棱镜管理器
 * 管理元素棱镜区域的创建、持续效果和元素标记共享
 * 
 * 效果：
 * - 在指定范围内创建元素棱镜区域，持续20秒
 * - 每2秒扫描一次范围内的敌人，收集并共享元素标记
 * - 元素反应触发两次
 * 
 * @author Love_U
 * @version 1.0.6
 */
public class ElementalPrismManager {

    /**
     * 扫描间隔（tick）- 每2秒扫描一次
     */
    private static final int SCAN_INTERVAL_TICKS = 40;

    /**
     * 粒子效果间隔（tick）
     */
    private static final int PARTICLE_INTERVAL_TICKS = 10;

    /**
     * 存储所有活跃的元素棱镜区域
     * 键：世界维度ID，值：该维度的棱镜列表
     */
    private static final Map<String, List<ElementalPrismZone>> activePrisms = new HashMap<>();

    /**
     * 创建元素棱镜区域
     *
     * @param level          服务器世界
     * @param center         中心位置
     * @param range          范围（格）
     * @param durationTicks  持续时间（tick）- 20秒 = 400 tick
     * @param caster         施法者
     */
    public static void createPrism(ServerLevel level, Vec3 center, double range, 
                                    int durationTicks, LivingEntity caster) {
        String dimensionId = level.dimension().location().toString();
        
        // 创建新的棱镜区域
        ElementalPrismZone prism = new ElementalPrismZone(center, range, durationTicks, caster);
        
        // 添加到活跃列表
        activePrisms.computeIfAbsent(dimensionId, k -> new ArrayList<>()).add(prism);
        
        LegendaryMage.LOGGER.info("[元素棱镜] 已在 {} 创建棱镜区域，范围: {}格，持续时间: {}tick", 
                dimensionId, String.format("%.1f", range), durationTicks);
    }

    /**
     * 更新所有元素棱镜区域
     * 每tick调用一次
     *
     * @param level 服务器世界
     */
    public static void tick(ServerLevel level) {
        String dimensionId = level.dimension().location().toString();
        List<ElementalPrismZone> prisms = activePrisms.get(dimensionId);
        
        if (prisms == null || prisms.isEmpty()) {
            return;
        }
        
        // 遍历所有棱镜区域
        Iterator<ElementalPrismZone> iterator = prisms.iterator();
        while (iterator.hasNext()) {
            ElementalPrismZone prism = iterator.next();
            
            // 更新棱镜
            prism.tick(level);
            
            // 检查是否结束
            if (prism.isFinished()) {
                iterator.remove();
                LegendaryMage.LOGGER.debug("[元素棱镜] 棱镜区域已结束");
            }
        }
        
        // 如果该维度没有棱镜了，清理map
        if (prisms.isEmpty()) {
            activePrisms.remove(dimensionId);
        }
    }

    /**
     * 清除所有元素棱镜区域
     *
     * @param level 服务器世界
     */
    public static void clearAll(ServerLevel level) {
        String dimensionId = level.dimension().location().toString();
        activePrisms.remove(dimensionId);
        LegendaryMage.LOGGER.info("[元素棱镜] 已清除 {} 的所有棱镜区域", dimensionId);
    }

    /**
     * 元素棱镜区域类
     * 表示一个元素棱镜区域的属性和状态
     */
    public static class ElementalPrismZone {
        
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
         * 施法者
         */
        private final LivingEntity caster;
        
        /**
         * 已过去的tick数
         */
        private int elapsedTicks;
        
        /**
         * 距离上次扫描的tick数
         */
        private int ticksSinceLastScan;
        
        /**
         * 距离上次粒子效果的tick数
         */
        private int ticksSinceLastParticles;

        /**
         * 构造函数
         *
         * @param center          中心位置
         * @param range           范围（格）
         * @param durationTicks   持续时间（tick）
         * @param caster          施法者
         */
        public ElementalPrismZone(Vec3 center, double range, int durationTicks, LivingEntity caster) {
            this.center = center;
            this.range = range;
            this.totalDurationTicks = durationTicks;
            this.caster = caster;
            this.elapsedTicks = 0;
            this.ticksSinceLastScan = 0;
            this.ticksSinceLastParticles = 0;
        }

        /**
         * 更新棱镜区域
         *
         * @param level 服务器世界
         */
        public void tick(ServerLevel level) {
            elapsedTicks++;
            ticksSinceLastScan++;
            ticksSinceLastParticles++;
            
            // 播放粒子效果
            if (ticksSinceLastParticles >= PARTICLE_INTERVAL_TICKS) {
                playAmbientParticles(level);
                ticksSinceLastParticles = 0;
            }
            
            // 扫描并共享元素标记
            if (ticksSinceLastScan >= SCAN_INTERVAL_TICKS) {
                scanAndShareMarks(level);
                ticksSinceLastScan = 0;
            }
        }

        /**
         * 播放环境粒子效果
         *
         * @param level 服务器世界
         */
        private void playAmbientParticles(ServerLevel level) {
            // 播放旋转的棱镜粒子效果
            double angle = (elapsedTicks % 360) * Math.PI / 180.0;
            
            // 在范围边缘播放粒子
            for (int i = 0; i < 8; i++) {
                double particleAngle = angle + (i * Math.PI / 4);
                double x = center.x + Math.cos(particleAngle) * range;
                double z = center.z + Math.sin(particleAngle) * range;
                
                level.sendParticles(
                        ParticleTypes.WITCH,
                        x, center.y + 1, z,
                        1,
                        0, 0.1, 0,
                        0.02
                );
            }
            
            // 在中心播放粒子
            level.sendParticles(
                    ParticleTypes.END_ROD,
                    center.x, center.y + 1, center.z,
                    3,
                    0.2, 0.2, 0.2,
                    0.01
            );
        }

        /**
         * 扫描范围内的敌人并共享元素标记
         *
         * @param level 服务器世界
         */
        private void scanAndShareMarks(ServerLevel level) {
            // 获取范围内的所有敌人
            List<LivingEntity> targets = getTargetsInRange(level);
            
            if (targets.isEmpty()) {
                return;
            }
            
            // 收集所有目标身上的元素标记
            Map<ElementType, Integer> allMarks = collectAllMarks(targets);
            
            if (allMarks.isEmpty()) {
                return;
            }
            
            if (com.legendarymage.legendarymagemod.Config.ELEMENTAL_PRISM_DEBUG_OUTPUT.get()) {
                LegendaryMage.LOGGER.info("[元素棱镜] 扫描到 {} 个目标，{} 种元素标记", 
                        targets.size(), allMarks.size());
            }
            
            // 共享元素标记给所有目标
            shareMarksToAllTargets(level, targets, allMarks);
            
            // 触发两次元素反应
            triggerDoubleReactions(level, targets);
        }

        /**
         * 获取范围内的所有敌人
         * 
         * @param level 服务器世界
         * @return 目标列表
         */
        private List<LivingEntity> getTargetsInRange(ServerLevel level) {
            AABB area = new AABB(
                    center.x - range, center.y - range, center.z - range,
                    center.x + range, center.y + range, center.z + range
            );

            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);
            List<LivingEntity> targets = new ArrayList<>();

            for (LivingEntity entity : entities) {
                // 排除施法者自身
                if (entity == caster) {
                    continue;
                }
                
                // 检查是否为敌人
                if (!isAlly(entity)) {
                    targets.add(entity);
                }
            }

            return targets;
        }

        /**
         * 检查目标是否为队友
         * 
         * @param target 目标实体
         * @return 是否为队友
         */
        private boolean isAlly(LivingEntity target) {
            // 使用铁魔法的团队检查
            if (caster instanceof Player playerCaster &&
                target instanceof Player targetPlayer) {
                return playerCaster.getTeam() != null && 
                       playerCaster.getTeam().equals(targetPlayer.getTeam());
            }

            // 检查是否为同一阵营的生物
            return caster.getTeam() != null && caster.getTeam().equals(target.getTeam());
        }

        /**
         * 收集所有目标身上的元素标记
         * 
         * @param targets 目标列表
         * @return 元素标记映射（元素类型 -> 最高等级）
         */
        private Map<ElementType, Integer> collectAllMarks(List<LivingEntity> targets) {
            Map<ElementType, Integer> allMarks = new HashMap<>();

            for (LivingEntity target : targets) {
                for (ElementType elementType : ElementType.values()) {
                    Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(elementType.getMarkEffect());
                    MobEffectInstance effect = target.getEffect(effectHolder);
                    
                    if (effect != null) {
                        int level = effect.getAmplifier() + 1; // 转换为1-3级
                        // 保留最高等级
                        allMarks.merge(elementType, level, Math::max);
                    }
                }
            }

            return allMarks;
        }

        /**
         * 共享元素标记给所有目标
         * 
         * @param level     服务器世界
         * @param targets   目标列表
         * @param allMarks  所有元素标记
         */
        private void shareMarksToAllTargets(ServerLevel level, List<LivingEntity> targets, 
                                            Map<ElementType, Integer> allMarks) {
            for (LivingEntity target : targets) {
                // 检查目标是否已死亡
                if (!target.isAlive() || target.isDeadOrDying()) {
                    continue;
                }

                for (Map.Entry<ElementType, Integer> entry : allMarks.entrySet()) {
                    ElementType elementType = entry.getKey();
                    int markLevel = entry.getValue();

                    // 检查目标是否已有该类型的标记
                    Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(elementType.getMarkEffect());
                    MobEffectInstance existingEffect = target.getEffect(effectHolder);

                    if (existingEffect == null) {
                        // 目标没有该标记，施加标记
                        int amplifier = Math.max(0, Math.min(markLevel - 1, 2)); // 限制在0-2
                        target.addEffect(new MobEffectInstance(
                                effectHolder,
                                100, // 5秒持续时间
                                amplifier,
                                false,
                                true,
                                true
                        ));

                        // 播放标记施加粒子效果
                        playMarkApplyParticles(level, target, elementType);

                        if (com.legendarymage.legendarymagemod.Config.ELEMENTAL_PRISM_DEBUG_OUTPUT.get()) {
                            LegendaryMage.LOGGER.info("[元素棱镜] 共享 {} 标记({}级)给 {}",
                                    elementType.getId(), markLevel, target.getName().getString());
                        }
                    }
                }
            }
        }

        /**
         * 触发两次元素反应
         * 
         * @param level   服务器世界
         * @param targets 目标列表
         */
        private void triggerDoubleReactions(ServerLevel level, List<LivingEntity> targets) {
            // 第一次反应
            for (LivingEntity target : targets) {
                if (!target.isAlive() || target.isDeadOrDying()) {
                    continue;
                }

                // 获取目标身上的所有元素标记
                List<ElementType> targetMarks = ElementReactionManager.getAllMarks(target);
                
                if (targetMarks.size() >= 2) {
                    // 随机选择两个不同的标记进行反应
                    Collections.shuffle(targetMarks);
                    ElementType element1 = targetMarks.get(0);
                    ElementType element2 = targetMarks.get(1);

                    // 获取标记等级
                    Holder<MobEffect> effectHolder1 = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(element1.getMarkEffect());
                    MobEffectInstance effect1 = target.getEffect(effectHolder1);
                    int level1 = effect1 != null ? effect1.getAmplifier() + 1 : 1;

                    // 触发反应
                    com.legendarymage.legendarymagemod.element.ElementReactionEffects.handleReaction(
                            level, target, caster, element1, element2, level1);

                    if (com.legendarymage.legendarymagemod.Config.ELEMENTAL_PRISM_DEBUG_OUTPUT.get()) {
                        LegendaryMage.LOGGER.info("[元素棱镜] 第一次反应: {} + {} 在 {}",
                                element1.getId(), element2.getId(), target.getName().getString());
                    }
                }
            }

            // 延迟执行第二次反应（0.5秒后）
            level.getServer().tell(new net.minecraft.server.TickTask(
                    level.getServer().getTickCount() + 10,
                    () -> {
                        for (LivingEntity target : targets) {
                            if (!target.isAlive() || target.isDeadOrDying()) {
                                continue;
                            }

                            // 重新获取目标身上的所有元素标记
                            List<ElementType> targetMarks = ElementReactionManager.getAllMarks(target);
                            
                            if (targetMarks.size() >= 2) {
                                // 再次触发反应
                                Collections.shuffle(targetMarks);
                                ElementType element1 = targetMarks.get(0);
                                ElementType element2 = targetMarks.get(1);

                                Holder<MobEffect> effectHolder1 = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(element1.getMarkEffect());
                                MobEffectInstance effect1 = target.getEffect(effectHolder1);
                                int level1 = effect1 != null ? effect1.getAmplifier() + 1 : 1;

                                com.legendarymage.legendarymagemod.element.ElementReactionEffects.handleReaction(
                                        level, target, caster, element1, element2, level1);

                                if (com.legendarymage.legendarymagemod.Config.ELEMENTAL_PRISM_DEBUG_OUTPUT.get()) {
                                    LegendaryMage.LOGGER.info("[元素棱镜] 第二次反应: {} + {} 在 {}",
                                            element1.getId(), element2.getId(), target.getName().getString());
                                }
                            }
                        }
                    }
            ));
        }

        /**
         * 播放标记施加粒子效果
         * 
         * @param level       服务器世界
         * @param target      目标
         * @param elementType 元素类型
         */
        private void playMarkApplyParticles(ServerLevel level, LivingEntity target, ElementType elementType) {
            Vec3 pos = target.position();

            // 在目标周围播放粒子
            for (int i = 0; i < 5; i++) {
                double offsetX = (Math.random() - 0.5) * target.getBbWidth();
                double offsetY = Math.random() * target.getBbHeight();
                double offsetZ = (Math.random() - 0.5) * target.getBbWidth();

                level.sendParticles(
                        ParticleTypes.WITCH,
                        pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                        1,
                        0, 0.05, 0,
                        0.01
                );
            }
        }

        /**
         * 检查位置是否在棱镜区域内
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
         * 检查棱镜是否已结束
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
