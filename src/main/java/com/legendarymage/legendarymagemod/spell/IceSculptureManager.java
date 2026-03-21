package com.legendarymage.legendarymagemod.spell;

import com.legendarymage.legendarymagemod.Config;
import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.client.IceSculptureTextureManager;

import io.redspace.ironsspellbooks.entity.mobs.frozen_humanoid.FrozenHumanoid;
import io.redspace.ironsspellbooks.registries.EntityRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * 冰雕管理器
 * 管理冰雕的生成、定时和转化逻辑
 * 
 * 机制说明：
 * 1. 法术释放后创建冰雕场，生成铁魔法的 FrozenHumanoid 冰雕实体
 * 2. 每个冰雕有独立的20秒计时器
 * 3. 20秒后冰雕转化为活体冰雕（可移动攻击）
 * 4. 如果在转化前击败冰雕，则阻止活体冰雕生成
 * 5. 法术持续期间不断生成新的冰雕
 * 
 * @author Love_U
 * @version 0.0.2
 */
public class IceSculptureManager {

    /**
     * 存储所有活跃的冰雕场
     * Key: 世界维度ID, Value: 该维度的冰雕场列表
     */
    private static final Map<String, List<IceSculptureField>> activeFields = new HashMap<>();

    /**
     * 存储所有活跃的冰雕（用于追踪是否被击败）
     * Key: 冰雕实体UUID, Value: 冰雕数据
     */
    private static final Map<UUID, SculptureData> activeSculptures = new HashMap<>();

    /**
     * 获取冰雕持续时间（tick）
     */
    private static int getSculptureLifetimeTicks() {
        return Config.LIVING_ICE_SCULPTURE_LIFETIME_TICKS.get();
    }

    /**
     * 获取冰雕生成间隔（tick）
     */
    private static int getSpawnIntervalTicks() {
        return Config.LIVING_ICE_SCULPTURE_SPAWN_INTERVAL.get();
    }

    /**
     * 获取每个冰雕场同时存在的最大冰雕数量
     */
    private static int getMaxSculpturesPerField() {
        return Config.LIVING_ICE_SCULPTURE_MAX_SCULPTURES.get();
    }

    /**
     * 创建冰雕场
     *
     * @param level       服务器世界
     * @param center      中心位置
     * @param range       范围
     * @param duration    法术持续时间（tick）
     * @param spellPower  法术强度
     * @param caster      施法者
     */
    public static void createIceSculptureField(ServerLevel level, Vec3 center, double range, 
                                                int duration, float spellPower, LivingEntity caster) {
        String dimensionId = level.dimension().location().toString();
        
        // 【日志分析】记录冰雕场创建
        LegendaryMage.LOGGER.info("[冰雕场] 创建冰雕场! 维度={}, 中心=({}, {}, {}), 范围={}, 持续时间={}tick, 法术强度={}, 施法者={}",
                dimensionId,
                String.format("%.2f", center.x),
                String.format("%.2f", center.y),
                String.format("%.2f", center.z),
                String.format("%.1f", range),
                duration,
                String.format("%.1f", spellPower),
                caster != null ? caster.getName().getString() : "null");
        
        // 创建冰雕场数据
        IceSculptureField field = new IceSculptureField(
                level, 
                center, 
                range, 
                duration, 
                spellPower, 
                caster
        );
        
        // 添加到活跃列表
        activeFields.computeIfAbsent(dimensionId, k -> new ArrayList<>()).add(field);
        
        // 播放施法效果
        IceSculptureParticles.playCastEffect(level, center, range);
        
        // 播放施法音效
        level.playSound(null, BlockPos.containing(center), 
                SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 1.0f, 0.8f);
    }

    /**
     * 世界Tick事件处理
     * 每tick更新所有冰雕场
     *
     * @param level 服务器世界
     */
    public static void onWorldTick(ServerLevel level) {
        String dimensionId = level.dimension().location().toString();
        List<IceSculptureField> fields = activeFields.get(dimensionId);
        
        if (fields == null || fields.isEmpty()) {
            return;
        }
        
        // 更新所有冰雕的计时器
        updateSculptures(level);
        
        // 遍历所有冰雕场，更新倒计时
        Iterator<IceSculptureField> iterator = fields.iterator();
        while (iterator.hasNext()) {
            IceSculptureField field = iterator.next();
            
            // 更新法术持续时间
            field.remainingTicks--;
            
            // 播放地面持续粒子效果（每5tick一次）
            if (field.remainingTicks % 5 == 0) {
                IceSculptureParticles.playGroundEffect(level, field.center, field.range);
            }
            
            // 检查是否需要生成新冰雕
            field.ticksSinceLastSpawn++;
            if (field.ticksSinceLastSpawn >= getSpawnIntervalTicks() && field.sculptures.size() < getMaxSculpturesPerField()) {
                spawnNewSculpture(level, field);
                field.ticksSinceLastSpawn = 0;
            }
            
            // 检查法术是否到期
            if (field.remainingTicks <= 0) {
                iterator.remove();
            }
        }
        
        // 如果该维度没有活跃冰雕场，清理map
        if (fields.isEmpty()) {
            activeFields.remove(dimensionId);
        }
    }

    /**
     * 生成新冰雕
     *
     * @param level 服务器世界
     * @param field 冰雕场数据
     */
    private static void spawnNewSculpture(ServerLevel level, IceSculptureField field) {
        // 在范围内随机生成位置
        Random random = new Random();
        double angle = random.nextDouble() * 2 * Math.PI;
        double distance = random.nextDouble() * field.range;
        
        double x = field.center.x + Math.cos(angle) * distance;
        double z = field.center.z + Math.sin(angle) * distance;
        
        // 找到地面高度
        BlockPos pos = findGroundPosition(level, (int) x, (int) field.center.y, (int) z);
        
        if (pos != null) {
            // 创建铁魔法的 FrozenHumanoid 冰雕实体
            FrozenHumanoid sculpture = new FrozenHumanoid(level, field.caster);
            // 确保生成在地面方块上方，避免卡在方块内受到挤压伤害
            sculpture.setPos(pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5);
            
            // 设置碎裂伤害（基于法术强度）
            float shatterDamage = calculateShatterDamage(field.spellPower);
            sculpture.setShatterDamage(shatterDamage);
            
            // 注意：不设置 FrozenHumanoid 的 deathTimer，我们自己管理计时器
            // 这样可以确保在计时器到期时进行转化而不是直接死亡
            
            // 添加到世界
            if (level.addFreshEntity(sculpture)) {
                // 设置无敌时间，防止生成瞬间受到伤害
                // 使用 setInvulnerable 方法设置无敌状态，20 tick = 1秒
                sculpture.setInvulnerable(true);
                // 使用 level.schedule 在20 tick后取消无敌状态
                level.getServer().schedule(20, () -> {
                    if (sculpture.isAlive()) {
                        sculpture.setInvulnerable(false);
                    }
                });
                // 计算冰雕生命值（基于法术强度，至少有MIN_SCULPTURE_HEALTH点）
                double sculptureHealth = calculateSculptureHealth(field.spellPower);
                
                // 设置冰雕生命值
                var maxHealthAttr = sculpture.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
                if (maxHealthAttr != null) {
                    maxHealthAttr.setBaseValue(sculptureHealth);
                    sculpture.setHealth((float) sculptureHealth);
                } else {
                    LegendaryMage.LOGGER.error("[冰雕生成] 无法获取MAX_HEALTH属性! UUID={}", sculpture.getUUID());
                }
                
                // 给冰雕添加伤害吸收效果，防止被一击秒杀
                // FrozenHumanoid 的 hurt 方法会直接 discard，所以我们用伤害吸收来缓冲
                int absorptionLevel = Math.min(4, Math.max(1, (int) (field.spellPower / 10)));
                sculpture.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.ABSORPTION,
                        getSculptureLifetimeTicks() + 100,  // 持续时间比生命周期稍长
                        absorptionLevel,
                        false,  // 无粒子效果
                        false,  // 不显示图标
                        false   // 不显示在HUD
                ));
                
                // 给冰雕添加抗性提升效果，减少受到的伤害
                int resistanceLevel = Math.min(2, Math.max(0, (int) (field.spellPower / 20)));
                if (resistanceLevel > 0) {
                    sculpture.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,
                            getSculptureLifetimeTicks() + 100,
                            resistanceLevel - 1,
                            false, false, false
                    ));
                }
                
                // 创建冰雕数据
                SculptureData data = new SculptureData(
                        sculpture.getUUID(),
                        field.caster,
                        field.spellPower,
                        getSculptureLifetimeTicks(),
                        sculptureHealth,
                        calculateEntityAttack(field.spellPower)
                );
                
                field.sculptures.add(data);
                activeSculptures.put(sculpture.getUUID(), data);
                
                // 播放生成效果
                IceSculptureParticles.playSculptureSpawnEffect(level, List.of(pos));
                
                // 播放生成音效
                level.playSound(null, pos, 
                        SoundEvents.GLASS_PLACE, SoundSource.HOSTILE, 1.0f, 1.0f);
                
                // 播放冰雕出现音效
                level.playSound(null, pos,
                        SoundEvents.PLAYER_HURT_FREEZE, SoundSource.HOSTILE, 0.8f, 0.5f);
                
                // 【日志分析】记录冰雕生成信息
                float actualHealth = sculpture.getHealth();
                float actualMaxHealth = sculpture.getMaxHealth();
                String healthStatus;
                if (actualHealth <= 0) {
                    healthStatus = "【警告:生命值为0】";
                } else if (actualMaxHealth <= 0) {
                    healthStatus = "【警告:最大生命值异常】";
                } else {
                    healthStatus = "正常";
                }
                
                LegendaryMage.LOGGER.info("[冰雕生成] 冰雕已生成! UUID={}, 位置=({}, {}, {}), 生命值={}/{}, 状态={}, 无敌时间={}, 法术强度={}, 施法者={}",
                        sculpture.getUUID(),
                        String.format("%.2f", sculpture.getX()),
                        String.format("%.2f", sculpture.getY()),
                        String.format("%.2f", sculpture.getZ()),
                        String.format("%.1f", actualHealth),
                        String.format("%.1f", actualMaxHealth),
                        healthStatus,
                        sculpture.invulnerableTime,
                        String.format("%.1f", field.spellPower),
                        field.caster != null ? field.caster.getName().getString() : "null");
            } else {
                // 【日志分析】记录添加实体失败
                LegendaryMage.LOGGER.warn("[冰雕生成] 添加实体到世界失败! 位置=({}, {}, {})",
                        pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5);
            }
        } else {
            // 【日志分析】记录找不到合适位置
            LegendaryMage.LOGGER.debug("[冰雕生成] 找不到合适的地面位置! 尝试位置=({}, {})", (int) x, (int) z);
        }
    }

    /**
     * 更新所有冰雕的计时器
     *
     * @param level 服务器世界
     */
    private static void updateSculptures(ServerLevel level) {
        String dimensionId = level.dimension().location().toString();
        List<IceSculptureField> fields = activeFields.get(dimensionId);
        
        Iterator<Map.Entry<UUID, SculptureData>> iterator = activeSculptures.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<UUID, SculptureData> entry = iterator.next();
            SculptureData data = entry.getValue();
            
            // 检查冰雕是否还存在
            FrozenHumanoid sculpture = (FrozenHumanoid) level.getEntity(data.sculptureUUID);
            if (sculpture == null || !sculpture.isAlive()) {
                // 【日志分析】记录冰雕死亡或消失
                if (sculpture == null) {
                    LegendaryMage.LOGGER.warn("[冰雕更新] 冰雕实体已不存在! UUID={}, 剩余tick={}, 可能原因: 实体被移除/discarded/区块卸载", 
                            data.sculptureUUID, data.remainingTicks);
                } else {
                    // 获取死亡时的详细信息
                    float deathHealth = sculpture.getHealth();
                    float deathMaxHealth = sculpture.getMaxHealth();
                    String deathReason;
                    
                    if (deathHealth <= 0) {
                        deathReason = "生命值归零";
                    } else if (deathMaxHealth <= 0) {
                        deathReason = "最大生命值异常(<=0)";
                    } else if (data.remainingTicks > 10) {
                        deathReason = "提前死亡(非正常转化)";
                    } else {
                        deathReason = "受到伤害/被击败";
                    }
                    
                    LegendaryMage.LOGGER.warn("[冰雕更新] 冰雕已死亡! UUID={}, 死亡原因: {}, 死亡时生命值={}/{}, 剩余tick={}, 位置=({}, {}, {})", 
                            data.sculptureUUID, 
                            deathReason,
                            String.format("%.1f", deathHealth),
                            String.format("%.1f", deathMaxHealth),
                            data.remainingTicks,
                            String.format("%.2f", sculpture.getX()),
                            String.format("%.2f", sculpture.getY()),
                            String.format("%.2f", sculpture.getZ()));
                }
                
                // 冰雕被击败或消失，从活跃列表移除
                iterator.remove();
                // 同时从所属 field 的 sculptures 列表中移除
                if (fields != null) {
                    for (IceSculptureField field : fields) {
                        field.sculptures.remove(data);
                    }
                }
                continue;
            }
            
            // 更新当前生命值（用于转化时继承）
            float currentHealth = sculpture.getHealth();
            float maxHealth = sculpture.getMaxHealth();
            
            // 【日志分析】检测生命值异常变化
            if (currentHealth < data.remainingHealth - 0.1f) {
                LegendaryMage.LOGGER.warn("[冰雕更新] 冰雕受到伤害! UUID={}, 生命值: {} -> {}, 减少={}, 无敌时间={}, 位置=({}, {}, {})",
                        data.sculptureUUID,
                        String.format("%.1f", data.remainingHealth),
                        String.format("%.1f", currentHealth),
                        String.format("%.1f", data.remainingHealth - currentHealth),
                        sculpture.invulnerableTime,
                        String.format("%.2f", sculpture.getX()),
                        String.format("%.2f", sculpture.getY()),
                        String.format("%.2f", sculpture.getZ()));
            }
            
            data.remainingHealth = currentHealth;
            
            // 更新倒计时
            data.remainingTicks--;
            
            // 播放环境粒子效果
            if (data.remainingTicks % 20 == 0) {
                IceSculptureParticles.playSculptureAmbientEffect(level, sculpture.blockPosition());
            }
            
            // 【日志分析】记录即将转化的冰雕
            if (data.remainingTicks == 5) {
                LegendaryMage.LOGGER.info("[冰雕更新] 冰雕即将转化! UUID={}, 当前生命值={}/{}, 位置=({}, {}, {})",
                        data.sculptureUUID,
                        String.format("%.1f", currentHealth),
                        String.format("%.1f", maxHealth),
                        String.format("%.2f", sculpture.getX()),
                        String.format("%.2f", sculpture.getY()),
                        String.format("%.2f", sculpture.getZ()));
            }
            
            // 检查是否到期（即将自动受伤死亡）
            if (data.remainingTicks <= 0) {
                // 【日志分析】记录正常转化
                LegendaryMage.LOGGER.info("[冰雕更新] 冰雕计时器到期，开始转化! UUID={}, 最终生命值={}",
                        data.sculptureUUID, String.format("%.1f", currentHealth));
                
                // 冰雕即将死亡，转化为活体冰雕
                convertToLivingSculpture(level, sculpture, data);
                iterator.remove();
                // 同时从所属 field 的 sculptures 列表中移除
                if (fields != null) {
                    for (IceSculptureField field : fields) {
                        field.sculptures.remove(data);
                    }
                }
            }
        }
    }

    /**
     * 将冰雕转化为活体冰雕
     *
     * @param level     服务器世界
     * @param sculpture 冰雕实体
     * @param data      冰雕数据
     */
    private static void convertToLivingSculpture(ServerLevel level, FrozenHumanoid sculpture, SculptureData data) {
        BlockPos pos = sculpture.blockPosition();
        
        // 播放转化效果
        IceSculptureParticles.playConversionEffect(level, sculpture.position());
        
        // 播放转化音效
        level.playSound(null, pos,
                SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 1.0f, 0.8f);
        level.playSound(null, pos,
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 1.0f, 1.5f);
        
        // 移除冰雕实体
        sculpture.discard();
        
        // 创建活体冰雕实体
        LivingIceSculptureEntity livingSculpture = new LivingIceSculptureEntity(level, data.owner);
        livingSculpture.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        
        // 设置生命值（继承冰雕剩余生命值比例）
        float healthRatio = data.remainingHealth / data.maxHealth;
        double livingHealth = calculateEntityHealth(data.spellPower);
        livingSculpture.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(livingHealth);
        livingSculpture.setHealth((float) (livingHealth * healthRatio));
        
        // 设置攻击力
        double attackDamage = calculateEntityAttack(data.spellPower);
        livingSculpture.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).setBaseValue(attackDamage);
        
        // 设置彩蛋纹理索引（默认为0，彩蛋模式下随机1或2）
        // 注意：纹理索引在实体中默认为0，这里根据当前全局模式决定是否随机
        if (IceSculptureTextureManager.getCurrentMode() == IceSculptureTextureManager.TextureMode.EASTER_EGG) {
            int randomTextureIndex = new Random().nextBoolean() ? 1 : 2;
            livingSculpture.setEasterEggTextureIndex(randomTextureIndex);
        } else {
            // 默认模式，确保纹理索引为0
            livingSculpture.setEasterEggTextureIndex(0);
        }
        
        // 添加到世界
        if (level.addFreshEntity(livingSculpture)) {
            // 播放生成效果
            IceSculptureParticles.playEntitySpawnEffect(level, livingSculpture.position());
        }
    }

    /**
     * 找到地面位置
     *
     * @param level 服务器世界
     * @param x     X坐标
     * @param y     Y坐标（起始高度）
     * @param z     Z坐标
     * @return 地面位置，如果没有找到则返回null
     */
    private static BlockPos findGroundPosition(ServerLevel level, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        
        // 向下查找固体方块
        for (int i = 0; i < 10; i++) {
            BlockPos below = pos.below();
            if (!level.isEmptyBlock(below)) {
                // 找到地面，返回上方空气位置
                if (level.isEmptyBlock(pos)) {
                    return pos;
                }
            }
            pos = below;
        }
        
        // 向上查找空气
        pos = new BlockPos(x, y, z);
        for (int i = 0; i < 10; i++) {
            if (level.isEmptyBlock(pos)) {
                BlockPos below = pos.below();
                if (!level.isEmptyBlock(below)) {
                    return pos;
                }
            }
            pos = pos.above();
        }
        
        return null;
    }

    /**
     * 计算碎裂伤害
     *
     * @param spellPower 法术强度
     * @return 碎裂伤害
     */
    private static float calculateShatterDamage(float spellPower) {
        // 使用配置：基础伤害 + 每点法术强度增加的伤害
        return (float) (Config.LIVING_ICE_SCULPTURE_SHATTER_DAMAGE_BASE.get() 
                + (spellPower - 10) * Config.LIVING_ICE_SCULPTURE_SHATTER_DAMAGE_PER_SPELL_POWER.get());
    }

    /**
     * 冰雕最低生命值
     */
    private static final double MIN_SCULPTURE_HEALTH = 10.0;

    /**
     * 活体冰雕最低生命值
     */
    private static final double MIN_ENTITY_HEALTH = 20.0;

    /**
     * 计算冰雕生命值（FrozenHumanoid）
     *
     * @param spellPower 法术强度
     * @return 生命值
     */
    private static double calculateSculptureHealth(float spellPower) {
        // 使用配置：基础生命值 + 每点法术强度增加的生命值
        double health = Config.LIVING_ICE_SCULPTURE_HEALTH_BASE.get() 
                + (spellPower - 10) * Config.LIVING_ICE_SCULPTURE_HEALTH_PER_SPELL_POWER.get();
        // 确保至少为最低生命值
        return Math.max(health, MIN_SCULPTURE_HEALTH);
    }

    /**
     * 计算活体冰雕生命值
     *
     * @param spellPower 法术强度
     * @return 生命值
     */
    private static double calculateEntityHealth(float spellPower) {
        // 活体冰雕生命值是冰雕的2倍，但至少有最低生命值
        double health = calculateSculptureHealth(spellPower) * 2.0;
        return Math.max(health, MIN_ENTITY_HEALTH);
    }

    /**
     * 计算活体冰雕攻击力
     *
     * @param spellPower 法术强度
     * @return 攻击力
     */
    private static double calculateEntityAttack(float spellPower) {
        // 使用配置：基础攻击力 + 每点法术强度增加的攻击力
        return Config.LIVING_ICE_SCULPTURE_DAMAGE_BASE.get() 
                + (spellPower - 10) * Config.LIVING_ICE_SCULPTURE_DAMAGE_PER_SPELL_POWER.get();
    }

    /**
     * 清理所有冰雕场（用于维度卸载或服务器关闭）
     *
     * @param level 服务器世界
     */
    public static void clearFields(ServerLevel level) {
        String dimensionId = level.dimension().location().toString();
        activeFields.remove(dimensionId);
        
        // 清理该维度的冰雕数据
        activeSculptures.entrySet().removeIf(entry -> {
            SculptureData data = entry.getValue();
            return data != null && level.getEntity(data.sculptureUUID) != null;
        });
    }

    /**
     * 检查实体是否是冰雕
     *
     * @param entity 实体
     * @return 是否是冰雕
     */
    public static boolean isSculpture(net.minecraft.world.entity.Entity entity) {
        return entity instanceof FrozenHumanoid && activeSculptures.containsKey(entity.getUUID());
    }

    /**
     * 获取冰雕数据
     *
     * @param sculptureUUID 冰雕UUID
     * @return 冰雕数据
     */
    public static SculptureData getSculptureData(UUID sculptureUUID) {
        return activeSculptures.get(sculptureUUID);
    }

    /**
     * 冰雕场数据类
     */
    private static class IceSculptureField {
        /**
         * 服务器世界
         */
        final ServerLevel level;
        
        /**
         * 中心位置
         */
        final Vec3 center;
        
        /**
         * 范围
         */
        final double range;
        
        /**
         * 法术剩余tick数
         */
        int remainingTicks;
        
        /**
         * 法术强度
         */
        final float spellPower;
        
        /**
         * 施法者
         */
        final LivingEntity caster;
        
        /**
         * 当前活跃的冰雕列表
         */
        final List<SculptureData> sculptures;
        
        /**
         * 距离上次生成冰雕的tick数
         */
        int ticksSinceLastSpawn;

        /**
         * 构造函数
         */
        IceSculptureField(ServerLevel level, Vec3 center, double range, int duration, 
                         float spellPower, LivingEntity caster) {
            this.level = level;
            this.center = center;
            this.range = range;
            this.remainingTicks = duration;
            this.spellPower = spellPower;
            this.caster = caster;
            this.sculptures = new ArrayList<>();
            this.ticksSinceLastSpawn = 0;
        }
    }

    /**
     * 冰雕数据类（公开，供其他类使用）
     */
    public static class SculptureData {
        /**
         * 冰雕实体UUID
         */
        public final UUID sculptureUUID;
        
        /**
         * 召唤者
         */
        public final LivingEntity owner;
        
        /**
         * 法术强度
         */
        public final float spellPower;
        
        /**
         * 剩余tick数
         */
        public int remainingTicks;
        
        /**
         * 最大生命值
         */
        public final float maxHealth;
        
        /**
         * 当前生命值
         */
        public float remainingHealth;

        /**
         * 构造函数
         */
        SculptureData(UUID sculptureUUID, LivingEntity owner, float spellPower, 
                     int lifetimeTicks, double maxHealth, double attackDamage) {
            this.sculptureUUID = sculptureUUID;
            this.owner = owner;
            this.spellPower = spellPower;
            this.remainingTicks = lifetimeTicks;
            this.maxHealth = (float) maxHealth;
            this.remainingHealth = this.maxHealth;
        }
    }
}
