package com.legendarymage.legendarymagemod.spell;

import com.legendarymage.legendarymagemod.LegendaryMage;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * 复苏符文区域管理器
 * 管理所有复苏符文区域的创建、更新和移除
 * 处理区域内生物死亡后的亡灵转化逻辑
 * 
 * @author Love_U
 * @version 0.0.1
 */
public class ResurrectionRuneManager extends SavedData {

    /**
     * 数据存储ID
     */
    private static final String DATA_ID = "legendarymage_resurrection_runes";

    /**
     * 调试模式开关
     */
    private static final boolean DEBUG_MODE = false;

    /**
     * 性能优化配置
     */
    private static final int PARTICLE_INTERVAL = 10; // 每10tick播放一次粒子（0.5秒）
    private static final int DEBUG_LOG_INTERVAL = 100; // 每100tick输出一次调试信息（5秒）
    private static final double PARTICLE_DENSITY = 0.5; // 粒子密度系数（0-1）

    /**
     * 单例实例
     */
    private static ResurrectionRuneManager instance;

    /**
     * 符文区域列表
     */
    private final List<ResurrectionRune> runes = new ArrayList<>();

    /**
     * 已转化的亡灵实体UUID到施法者UUID的映射
     */
    private final Map<UUID, UUID> undeadOwners = new HashMap<>();

    /**
     * 私有构造函数
     */
    private ResurrectionRuneManager() {
    }

    /**
     * 输出调试日志
     * 
     * @param message 日志消息
     */
    private void debugLog(String message) {
        if (DEBUG_MODE) {
            com.legendarymage.legendarymagemod.ModLogger.spell("[复苏符文调试] {}", message);
        }
    }

    /**
     * 获取管理器实例
     * 
     * @return 单例实例
     */
    public static ResurrectionRuneManager getInstance() {
        if (instance == null) {
            instance = new ResurrectionRuneManager();
        }
        return instance;
    }

    /**
     * 从世界加载数据
     * 
     * @param level 服务器世界
     * @return 管理器实例
     */
    public static ResurrectionRuneManager load(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new Factory<>(
                        ResurrectionRuneManager::new,
                        (tag, provider) -> ResurrectionRuneManager.load(tag),
                        null
                ),
                DATA_ID
        );
    }

    /**
     * 从NBT加载数据
     * 
     * @param tag NBT标签
     * @return 管理器实例
     */
    public static ResurrectionRuneManager load(CompoundTag tag) {
        ResurrectionRuneManager manager = new ResurrectionRuneManager();
        
        // 加载符文区域
        ListTag runesTag = tag.getList("Runes", 10);
        for (int i = 0; i < runesTag.size(); i++) {
            CompoundTag runeTag = runesTag.getCompound(i);
            ResurrectionRune rune = ResurrectionRune.fromNBT(runeTag);
            manager.runes.add(rune);
        }
        
        // 加载亡灵所有者映射
        CompoundTag ownersTag = tag.getCompound("UndeadOwners");
        for (String key : ownersTag.getAllKeys()) {
            UUID undeadId = UUID.fromString(key);
            UUID ownerId = ownersTag.getUUID(key);
            manager.undeadOwners.put(undeadId, ownerId);
        }
        
        return manager;
    }

    /**
     * 保存数据到NBT
     * 
     * @param tag NBT标签
     * @param provider Holder查找提供者
     * @return NBT标签
     */
    @Override
    @Nonnull
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        // 保存符文区域
        ListTag runesTag = new ListTag();
        for (ResurrectionRune rune : runes) {
            runesTag.add(rune.toNBT());
        }
        tag.put("Runes", runesTag);
        
        // 保存亡灵所有者映射
        CompoundTag ownersTag = new CompoundTag();
        for (Map.Entry<UUID, UUID> entry : undeadOwners.entrySet()) {
            ownersTag.putUUID(entry.getKey().toString(), entry.getValue());
        }
        tag.put("UndeadOwners", ownersTag);
        
        return tag;
    }

    /**
     * 创建新的复苏符文区域
     *
     * @param level      服务器世界
     * @param x          X坐标
     * @param y          Y坐标
     * @param z          Z坐标
     * @param range      范围（格）
     * @param duration   持续时间（秒）
     * @param ownerUUID  施法者UUID
     * @param spellPower 法术强度
     * @return 创建的符文区域
     */
    public ResurrectionRune createRune(ServerLevel level, double x, double y, double z,
                                       int range, int duration, UUID ownerUUID, float spellPower) {
        debugLog(String.format("创建符文区域 - 位置: [%.2f, %.2f, %.2f], 范围: %d, 持续时间: %d秒, 施法者: %s, 法术强度: %.2f",
                x, y, z, range, duration, ownerUUID.toString(), spellPower));

        ResurrectionRune rune = new ResurrectionRune(x, y, z, range, duration, ownerUUID, spellPower);
        runes.add(rune);
        setDirty();

        debugLog("符文区域创建成功！当前活跃符文数量: " + runes.size());
        return rune;
    }



    /**
     * 更新所有符文区域（每tick调用）- 性能优化版
     * 只在有活跃符文时执行更新
     *
     * @param level 服务器世界
     */
    public void tick(ServerLevel level) {
        // 如果没有活跃符文，直接返回（性能优化）
        if (runes.isEmpty()) {
            return;
        }

        // 更新符文区域
        Iterator<ResurrectionRune> iterator = runes.iterator();
        while (iterator.hasNext()) {
            ResurrectionRune rune = iterator.next();
            rune.tick();

            // 播放粒子效果（按配置间隔）
            if (rune.ticksExisted % PARTICLE_INTERVAL == 0) {
                playRuneParticles(level, rune);
            }

            // 应用Buff效果（根据配置间隔）
            if (com.legendarymage.legendarymagemod.Config.RESURRECTION_RUNE_BUFF_ENABLED.get() &&
                rune.ticksExisted % com.legendarymage.legendarymagemod.Config.RESURRECTION_RUNE_BUFF_INTERVAL.get() == 0) {
                applyBuffsToEntitiesInRune(level, rune);
            }

            // 移除过期的符文
            if (rune.isExpired()) {
                if (DEBUG_MODE) {
                    debugLog(String.format("符文区域过期 - 位置: [%.2f, %.2f, %.2f], 施法者: %s",
                            rune.x, rune.y, rune.z, rune.ownerUUID.toString()));
                }
                iterator.remove();
                setDirty();
            }
        }

        // 清理不存在的亡灵（每100tick执行一次）
        if (level.getServer().getTickCount() % 100 == 0) {
            cleanupUndeadOwners(level);
        }
    }

    /**
     * 为符文范围内的实体应用Buff效果
     *
     * @param level 服务器世界
     * @param rune  符文区域
     */
    private void applyBuffsToEntitiesInRune(ServerLevel level, ResurrectionRune rune) {
        // 获取范围内的所有玩家
        List<ServerPlayer> players = level.getEntitiesOfClass(
                ServerPlayer.class,
                new AABB(
                        rune.x - rune.range, rune.y - 5, rune.z - rune.range,
                        rune.x + rune.range, rune.y + 5, rune.z + rune.range
                ),
                entity -> rune.contains(entity.position())
        );

        // 为玩家应用Buff
        for (ServerPlayer player : players) {
            applyPlayerBuffs(player, rune.spellPower);
        }

        // 获取范围内的所有召唤物（属于施法者的亡灵）
        List<LivingEntity> summons = level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(
                        rune.x - rune.range, rune.y - 5, rune.z - rune.range,
                        rune.x + rune.range, rune.y + 5, rune.z + rune.range
                ),
                entity -> {
                    // 检查是否是该符文施法者的召唤物
                    if (entity instanceof io.redspace.ironsspellbooks.entity.mobs.SummonedZombie) {
                        return rune.contains(entity.position()) &&
                               undeadOwners.containsKey(entity.getUUID()) &&
                               undeadOwners.get(entity.getUUID()).equals(rune.ownerUUID);
                    }
                    return false;
                }
        );

        // 为召唤物应用Buff
        for (LivingEntity summon : summons) {
            applySummonBuffs(summon, rune.spellPower);
        }

        if (DEBUG_MODE && (players.size() > 0 || summons.size() > 0)) {
            debugLog(String.format("符文范围内 - 玩家: %d, 召唤物: %d, 法术强度: %.2f",
                    players.size(), summons.size(), rune.spellPower));
        }
    }

    /**
     * 为玩家应用Buff效果
     *
     * @param player     玩家
     * @param spellPower 法术强度
     */
    private void applyPlayerBuffs(ServerPlayer player, float spellPower) {
        // 计算Buff等级（基于法术强度和配置倍率）
        double buffMultiplier = com.legendarymage.legendarymagemod.Config.RESURRECTION_RUNE_BUFF_MULTIPLIER.get();
        int buffLevel = Math.min(4, Math.max(0, (int) (spellPower / 1.0f * buffMultiplier) - 1));
        int duration = 100; // 5秒（100 tick），确保持续刷新

        // 1. 伤害吸收 - 鲜血护盾
        player.addEffect(new MobEffectInstance(
                MobEffects.ABSORPTION,
                duration,
                buffLevel,
                false,  // 无粒子效果（减少视觉干扰）
                false,  // 不显示图标
                true    // 显示在HUD
        ));

        // 2. 生命恢复 - 鲜血再生
        player.addEffect(new MobEffectInstance(
                MobEffects.REGENERATION,
                duration,
                Math.max(0, buffLevel - 1),
                false,
                false,
                true
        ));

        // 3. 抗性提升 - 鲜血护甲（高级法术强度时）
        if (buffLevel >= 2) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE,
                    duration,
                    Math.max(0, buffLevel - 2),
                    false,
                    false,
                    true
            ));
        }

        // 4. 力量 - 鲜血之力（高级法术强度时）
        if (buffLevel >= 3) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_BOOST,
                    duration,
                    Math.max(0, buffLevel - 3),
                    false,
                    false,
                    true
            ));
        }
    }

    /**
     * 为召唤物应用Buff效果
     *
     * @param summon     召唤物
     * @param spellPower 法术强度
     */
    private void applySummonBuffs(LivingEntity summon, float spellPower) {
        // 计算Buff等级（基于法术强度和配置倍率，召唤物获得更强效果）
        double buffMultiplier = com.legendarymage.legendarymagemod.Config.RESURRECTION_RUNE_BUFF_MULTIPLIER.get();
        int buffLevel = Math.min(4, Math.max(0, (int) (spellPower / 8.0f * buffMultiplier)));
        int duration = 100; // 5秒

        // 1. 力量 - 亡灵强化
        summon.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_BOOST,
                duration,
                buffLevel,
                false,
                false,
                true
        ));

        // 2. 抗性提升 - 亡灵坚韧
        summon.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE,
                duration,
                Math.max(0, buffLevel - 1),
                false,
                false,
                true
        ));

        // 3. 速度 - 亡灵迅捷
        summon.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SPEED,
                duration,
                Math.max(0, buffLevel - 2),
                false,
                false,
                true
        ));

        // 4. 生命恢复 - 亡灵复苏（高级法术强度时）
        if (buffLevel >= 2) {
            summon.addEffect(new MobEffectInstance(
                    MobEffects.REGENERATION,
                    duration,
                    Math.max(0, buffLevel - 2),
                    false,
                    false,
                    true
            ));
        }

        // 5. 火焰抗性 - 亡灵免疫（高级法术强度时）
        if (buffLevel >= 3) {
            summon.addEffect(new MobEffectInstance(
                    MobEffects.FIRE_RESISTANCE,
                    duration,
                    0,
                    false,
                    false,
                    true
            ));
        }
    }

    /**
     * 播放符文粒子效果 - 使用铁魔法风格的粒子系统
     * 
     * @param level 服务器世界
     * @param rune  符文区域
     */
    private void playRuneParticles(ServerLevel level, ResurrectionRune rune) {
        // 使用新的粒子系统播放符文边界效果
        ResurrectionRuneParticles.playRuneBoundary(level, rune, rune.ticksExisted);
    }

    /**
     * 检查实体是否在复苏符文区域内
     * 
     * @param entity 实体
     * @return 包含该实体的符文区域，如果不在任何区域内则返回null
     */
    public ResurrectionRune getRuneForEntity(Entity entity) {
        Vec3 pos = entity.position();
        debugLog(String.format("检查实体是否在符文区域内 - 实体: %s, 位置: [%.2f, %.2f, %.2f]", 
                entity.getType().getDescriptionId(), pos.x, pos.y, pos.z));
        debugLog("当前活跃符文数量: " + runes.size());
        
        for (ResurrectionRune rune : runes) {
            double distance = Math.sqrt(
                Math.pow(pos.x - rune.x, 2) + 
                Math.pow(pos.y - rune.y, 2) + 
                Math.pow(pos.z - rune.z, 2)
            );
            debugLog(String.format("检查符文 - 中心: [%.2f, %.2f, %.2f], 范围: %d, 距离: %.2f", 
                    rune.x, rune.y, rune.z, rune.range, distance));
            
            if (rune.contains(pos)) {
                debugLog("实体在符文区域内！");
                return rune;
            }
        }
        
        debugLog("实体不在任何符文区域内");
        return null;
    }

    /**
     * 处理生物死亡事件
     *
     * @param level  服务器世界
     * @param entity 死亡的生物
     * @return 转化后的亡灵实体，如果未转化则返回null
     */
    public LivingEntity handleEntityDeath(ServerLevel level, LivingEntity entity) {
        debugLog("========================================");
        debugLog("处理生物死亡事件 - 实体: " + entity.getType().getDescriptionId());
        debugLog("实体UUID: " + entity.getUUID().toString());
        debugLog("实体位置: [" + String.format("%.2f", entity.getX()) + ", " +
                String.format("%.2f", entity.getY()) + ", " +
                String.format("%.2f", entity.getZ()) + "]");

        // 检查是否在符文区域内
        ResurrectionRune rune = getRuneForEntity(entity);
        if (rune == null) {
            debugLog("转化失败: 实体不在符文区域内");
            return null;
        }

        debugLog("找到符文区域 - 施法者UUID: " + rune.ownerUUID.toString() + ", 法术强度: " + String.format("%.2f", rune.spellPower));

        // 检查是否已经是亡灵
        if (isUndead(entity)) {
            debugLog("转化失败: 实体已经是亡灵类型");
            return null;
        }
        debugLog("实体不是亡灵，可以继续转化");

        // 获取施法者
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(rune.ownerUUID);
        if (owner == null) {
            debugLog("转化失败: 施法者不在线或不存在 - UUID: " + rune.ownerUUID.toString());
            return null;
        }
        debugLog("施法者在线: " + owner.getName().getString());

        // 检查实体是否为Mob类型
        if (!(entity instanceof Mob)) {
            debugLog("转化失败: 实体不是Mob类型 - 实际类型: " + entity.getClass().getName());
            return null;
        }
        debugLog("实体是Mob类型，可以转化");

        // 转化亡灵
        debugLog("开始转化亡灵...");
        LivingEntity undead = convertToUndead(level, entity, owner, rune.spellPower);

        if (undead != null) {
            debugLog("转化成功！亡灵UUID: " + undead.getUUID().toString());
            // 记录亡灵所有者
            undeadOwners.put(undead.getUUID(), rune.ownerUUID);
            setDirty();
            debugLog("已记录亡灵所有者映射");

            // 播放转化效果 - 使用铁魔法风格的粒子
            ResurrectionRuneParticles.playConversionEffect(level, entity.position());
            debugLog("播放转化效果完成");
        } else {
            debugLog("转化失败: convertToUndead返回null");
        }

        debugLog("========================================");
        return undead;
    }

    /**
     * 将生物转化为亡灵 - 使用铁魔法的SummonedZombie API
     *
     * @param level      服务器世界
     * @param original   原始生物
     * @param owner      施法者
     * @param spellPower 法术强度
     * @return 转化后的亡灵
     */
    private LivingEntity convertToUndead(ServerLevel level, LivingEntity original, ServerPlayer owner, float spellPower) {
        debugLog("开始创建亡灵实体（使用铁魔法API）...");
        debugLog("法术强度: " + String.format("%.2f", spellPower));
        LivingEntity undead = null;

        try {
            // 使用铁魔法的SummonedZombie - 它会自动处理与召唤者的关系
            debugLog("创建SummonedZombie...");
            io.redspace.ironsspellbooks.entity.mobs.SummonedZombie summonedZombie =
                new io.redspace.ironsspellbooks.entity.mobs.SummonedZombie(level, owner, true);

            debugLog("SummonedZombie创建成功");

            // 设置位置
            summonedZombie.moveTo(original.getX(), original.getY(), original.getZ(),
                    original.getYRot(), original.getXRot());
            debugLog(String.format("设置僵尸位置: [%.2f, %.2f, %.2f]",
                    original.getX(), original.getY(), original.getZ()));

            // 继承属性（带法术强度增强）
            debugLog("开始继承属性...");
            inheritAttributes(summonedZombie, original, spellPower);

            // 设置召唤者（再次确认）
            summonedZombie.setSummoner(owner);
            debugLog("设置召唤者: " + owner.getName().getString());

            // 添加到世界
            debugLog("尝试将SummonedZombie添加到世界...");
            boolean added = level.addFreshEntity(summonedZombie);

            if (added) {
                debugLog("SummonedZombie成功添加到世界！实体ID: " + summonedZombie.getId());

                // 触发起身动画
                summonedZombie.triggerRiseAnimation();
                debugLog("触发起身动画");

                undead = summonedZombie;
            } else {
                debugLog("错误: SummonedZombie添加到世界失败！");
            }
        } catch (Exception e) {
            debugLog("错误: 创建SummonedZombie时发生异常: " + e.getMessage());
            e.printStackTrace();
        }

        return undead;
    }

    /**
     * 继承属性（带法术强度增强）
     *
     * @param undead     亡灵实体
     * @param original   原始生物
     * @param spellPower 法术强度
     */
    private void inheritAttributes(LivingEntity undead, LivingEntity original, float spellPower) {
        debugLog("继承属性详情（法术强度: " + String.format("%.2f", spellPower) + "):");

        // 计算法术强度倍率（以10.0为基准），并应用配置文件的倍率
        double configMultiplier = com.legendarymage.legendarymagemod.Config.RESURRECTION_RUNE_SPELL_POWER_MULTIPLIER.get();
        float powerMultiplier = (float) (spellPower / 1.0f * configMultiplier);
        debugLog("  - 配置倍率: " + String.format("%.2f", configMultiplier));
        debugLog("  - 最终强度倍率: " + String.format("%.2f", powerMultiplier));

        // 继承最大生命值（带法术强度加成）
        AttributeInstance originalHealth = original.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance undeadHealth = undead.getAttribute(Attributes.MAX_HEALTH);
        if (originalHealth != null && undeadHealth != null) {
            double originalValue = originalHealth.getBaseValue();
            double enhancedValue = originalValue * powerMultiplier;
            undeadHealth.setBaseValue(enhancedValue);
            undead.setHealth(undead.getMaxHealth());
            debugLog("  - 生命值: " + String.format("%.2f", originalValue) + " -> " + String.format("%.2f", enhancedValue));
        } else {
            debugLog("  - 生命值: 无法继承（属性为null）");
        }

        // 继承护甲值（带法术强度加成）
        AttributeInstance originalArmor = original.getAttribute(Attributes.ARMOR);
        AttributeInstance undeadArmor = undead.getAttribute(Attributes.ARMOR);
        if (originalArmor != null && undeadArmor != null) {
            double originalValue = originalArmor.getBaseValue();
            double enhancedValue = originalValue * powerMultiplier;
            undeadArmor.setBaseValue(enhancedValue);
            debugLog("  - 护甲值: " + String.format("%.2f", originalValue) + " -> " + String.format("%.2f", enhancedValue));
        } else {
            debugLog("  - 护甲值: 无法继承（属性为null）");
        }

        // 继承护甲韧性（带法术强度加成）
        AttributeInstance originalToughness = original.getAttribute(Attributes.ARMOR_TOUGHNESS);
        AttributeInstance undeadToughness = undead.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (originalToughness != null && undeadToughness != null) {
            double originalValue = originalToughness.getBaseValue();
            double enhancedValue = originalValue * powerMultiplier;
            undeadToughness.setBaseValue(enhancedValue);
            debugLog("  - 护甲韧性: " + String.format("%.2f", originalValue) + " -> " + String.format("%.2f", enhancedValue));
        } else {
            debugLog("  - 护甲韧性: 无法继承（属性为null）");
        }

        // 继承攻击力（带法术强度加成）
        AttributeInstance originalDamage = original.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance undeadDamage = undead.getAttribute(Attributes.ATTACK_DAMAGE);
        if (originalDamage != null && undeadDamage != null) {
            double originalValue = originalDamage.getBaseValue();
            double enhancedValue = originalValue * powerMultiplier;
            undeadDamage.setBaseValue(enhancedValue);
            debugLog("  - 攻击力: " + String.format("%.2f", originalValue) + " -> " + String.format("%.2f", enhancedValue));
        } else {
            debugLog("  - 攻击力: 无法继承（属性为null）");
        }

        // 继承移动速度（带法术强度加成）
        AttributeInstance originalSpeed = original.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance undeadSpeed = undead.getAttribute(Attributes.MOVEMENT_SPEED);
        if (originalSpeed != null && undeadSpeed != null) {
            double originalValue = originalSpeed.getBaseValue();
            double enhancedValue = originalValue * (1.0f + (powerMultiplier - 1.0f) * 0.5f); // 速度加成减半
            undeadSpeed.setBaseValue(enhancedValue);
            debugLog("  - 移动速度: " + String.format("%.4f", originalValue) + " -> " + String.format("%.4f", enhancedValue));
        }
    }



    /**
     * 检查实体是否为亡灵
     * 
     * @param entity 实体
     * @return 是否为亡灵
     */
    private boolean isUndead(LivingEntity entity) {
        boolean isUndead = entity instanceof net.minecraft.world.entity.monster.Zombie 
            || entity instanceof net.minecraft.world.entity.monster.Skeleton
            || entity instanceof net.minecraft.world.entity.monster.Phantom
            || entity instanceof net.minecraft.world.entity.monster.Drowned
            || entity instanceof net.minecraft.world.entity.monster.Husk
            || entity instanceof net.minecraft.world.entity.monster.Stray
            || entity instanceof net.minecraft.world.entity.monster.WitherSkeleton
            || entity instanceof net.minecraft.world.entity.boss.wither.WitherBoss;
        
        if (isUndead) {
            debugLog("实体是亡灵类型: " + entity.getClass().getSimpleName());
        }
        
        return isUndead;
    }

    /**
     * 获取亡灵的所有者
     * 
     * @param undeadUUID 亡灵UUID
     * @return 所有者UUID，如果没有则返回null
     */
    public UUID getUndeadOwner(UUID undeadUUID) {
        return undeadOwners.get(undeadUUID);
    }

    /**
     * 清理不存在的亡灵
     * 
     * @param level 服务器世界
     */
    private void cleanupUndeadOwners(ServerLevel level) {
        int removedCount = 0;
        Iterator<Map.Entry<UUID, UUID>> iterator = undeadOwners.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<UUID, UUID> entry = iterator.next();
            Entity entity = level.getEntity(entry.getKey());
            if (entity == null || !entity.isAlive()) {
                iterator.remove();
                removedCount++;
                setDirty();
            }
        }
        
        if (removedCount > 0) {
            debugLog("清理不存在的亡灵记录: " + removedCount + " 条");
        }
    }

    /**
     * 获取当前活跃符文数量（用于调试）
     * 
     * @return 符文数量
     */
    public int getActiveRuneCount() {
        return runes.size();
    }

    /**
     * 复苏符文区域类
     */
    public static class ResurrectionRune {
        /**
         * X坐标
         */
        public final double x;

        /**
         * Y坐标
         */
        public final double y;

        /**
         * Z坐标
         */
        public final double z;

        /**
         * 范围（格）
         */
        public final int range;

        /**
         * 持续时间（tick）
         */
        public final int duration;

        /**
         * 施法者UUID
         */
        public final UUID ownerUUID;

        /**
         * 法术强度
         */
        public final float spellPower;

        /**
         * 已存在的tick数
         */
        public int ticksExisted = 0;

        /**
         * 构造函数
         *
         * @param x          X坐标
         * @param y          Y坐标
         * @param z          Z坐标
         * @param range      范围
         * @param duration   持续时间（秒）
         * @param ownerUUID  施法者UUID
         * @param spellPower 法术强度
         */
        public ResurrectionRune(double x, double y, double z, int range, int duration, UUID ownerUUID, float spellPower) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.range = range;
            this.duration = duration * 20; // 转换为tick
            this.ownerUUID = ownerUUID;
            this.spellPower = spellPower;
        }

        /**
         * 更新
         */
        public void tick() {
            ticksExisted++;
        }

        /**
         * 检查是否过期
         * 
         * @return 是否过期
         */
        public boolean isExpired() {
            return ticksExisted >= duration;
        }

        /**
         * 检查位置是否在区域内
         * 
         * @param pos 位置
         * @return 是否在区域内
         */
        public boolean contains(Vec3 pos) {
            double dx = pos.x - x;
            double dy = pos.y - y;
            double dz = pos.z - z;
            return (dx * dx + dy * dy + dz * dz) <= (range * range);
        }

        /**
         * 转换为NBT
         *
         * @return NBT标签
         */
        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("X", x);
            tag.putDouble("Y", y);
            tag.putDouble("Z", z);
            tag.putInt("Range", range);
            tag.putInt("Duration", duration);
            tag.putUUID("Owner", ownerUUID);
            tag.putFloat("SpellPower", spellPower);
            tag.putInt("TicksExisted", ticksExisted);
            return tag;
        }

        /**
         * 从NBT加载
         *
         * @param tag NBT标签
         * @return 符文区域
         */
        public static ResurrectionRune fromNBT(CompoundTag tag) {
            double x = tag.getDouble("X");
            double y = tag.getDouble("Y");
            double z = tag.getDouble("Z");
            int range = tag.getInt("Range");
            int duration = tag.getInt("Duration");
            UUID ownerUUID = tag.getUUID("Owner");
            float spellPower = tag.getFloat("SpellPower");
            if (spellPower == 0) spellPower = 10.0f; // 默认值，兼容旧存档

            ResurrectionRune rune = new ResurrectionRune(x, y, z, range, duration / 20, ownerUUID, spellPower);
            rune.ticksExisted = tag.getInt("TicksExisted");
            return rune;
        }
    }
}
