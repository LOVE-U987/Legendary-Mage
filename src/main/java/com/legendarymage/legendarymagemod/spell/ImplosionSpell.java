package com.legendarymage.legendarymagemod.spell;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.particle.ShockwaveParticleOptions;
import io.redspace.ironsspellbooks.particle.SparkParticleOptions;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.spell.SpellPowerHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 聚爆法术
 * 铁魔法-火系
 * 在指向位置聚集范围内的敌人，然后发动一次火焰爆炸造成伤害
 * 
 * @author Love_U
 * @version 0.0.1
 */
public class ImplosionSpell extends AbstractSpell {

    /**
     * 法术ID
     */
    public static final String SPELL_ID = "implosion";

    /**
     * 基础蓝耗
     */
    private static final int BASE_MANA_COST = 200;

    /**
     * 每级蓝耗增量
     */
    private static final int MANA_COST_PER_LEVEL = 10;

    /**
     * 基础施法时间（tick）
     * 1秒 = 20 tick
     */
    private static final int BASE_CAST_TIME = 20;

    /**
     * 基础冷却时间（tick）
     * 45秒 = 900 tick
     */
    private static final int BASE_COOLDOWN = 900;

    /**
     * 基础聚集范围（格）
     */
    private static final int BASE_PULL_RANGE = 5;

    /**
     * 每级聚集范围增量（格）
     */
    private static final int PULL_RANGE_PER_LEVEL = 1;

    /**
     * 基础伤害
     */
    private static final int BASE_DAMAGE = 20;

    /**
     * 每级伤害增量
     */
    private static final int DAMAGE_PER_LEVEL = 5;

    /**
     * 基础法术强度
     */
    private static final int BASE_SPELL_POWER = 1;

    /**
     * 每级法术强度增量
     */
    private static final int SPELL_POWER_PER_LEVEL = 1;

    /**
     * 最小等级
     */
    private static final int MIN_LEVEL = 1;

    /**
     * 最大等级
     */
    private static final int MAX_LEVEL = 6;

    /**
     * 最小稀有度（传奇）
     */
    private static final int MIN_RARITY = SpellRarity.LEGENDARY.getValue();

    /**
     * 最大稀有度（传奇）
     */
    private static final int MAX_RARITY = SpellRarity.LEGENDARY.getValue();

    /**
     * 聚集力度（用于拉取敌人）
     */
    private static final int PULL_STRENGTH = 1;

    /**
     * 火焰颜色（火系魔法 - 橙红色）
     */
    private static final Vector3f FIRE_COLOR = new Vector3f(1.0f, 0.4f, 0.0f);

    /**
     * 爆炸颜色（深红色）
     */
    private static final Vector3f EXPLOSION_COLOR = new Vector3f(0.9f, 0.2f, 0.0f);

    /**
     * 预警粒子颜色（亮橙色）
     */
    private static final Vector3f WARNING_COLOR = new Vector3f(1.0f, 0.6f, 0.0f);

    /**
     * 法术资源位置
     */
    private static final ResourceLocation SPELL_RESOURCE = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, SPELL_ID);

    /**
     * 法术图标路径
     */
    private static final ResourceLocation SPELL_ICON = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, "textures/gui/spell_icons/implosion.png");

    /**
     * 施法开始动画 - 使用铁魔法火系法术的施法动画
     */
    private static final AnimationHolder CAST_START_ANIMATION = new AnimationHolder(
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "long_cast"), 
            true);

    /**
     * 施法结束动画 - 使用铁魔法火系法术的施法动画
     */
    private static final AnimationHolder CAST_FINISH_ANIMATION = new AnimationHolder(
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "long_cast_finish"),
            true);

    /**
     * 施法开始音效
     */
    private static final Optional<SoundEvent> CAST_START_SOUND = Optional.of(SoundEvents.FIRECHARGE_USE);

    /**
     * 施法结束音效
     */
    private static final Optional<SoundEvent> CAST_FINISH_SOUND = Optional.of(SoundEvents.GENERIC_EXPLODE.value());

    /**
     * 存储施法者目标位置的临时映射
     * 键：施法者UUID，值：目标位置
     */
    private static final Map<UUID, Vec3> casterTargetPositions = new HashMap<>();

    /**
     * 存储施法者范围的临时映射
     */
    private static final Map<UUID, Integer> casterRanges = new HashMap<>();

    /**
     * 构造函数
     */
    public ImplosionSpell() {
        this.baseManaCost = BASE_MANA_COST;
        this.manaCostPerLevel = MANA_COST_PER_LEVEL;
        this.castTime = BASE_CAST_TIME;
        this.baseSpellPower = BASE_SPELL_POWER;
        this.spellPowerPerLevel = SPELL_POWER_PER_LEVEL;
    }

    /**
     * 获取法术资源位置
     * 
     * @return 法术资源位置
     */
    @Override
    public ResourceLocation getSpellResource() {
        return SPELL_RESOURCE;
    }

    /**
     * 获取默认配置
     * 
     * @return 默认配置
     */
    @Override
    public DefaultConfig getDefaultConfig() {
        return new DefaultConfig()
                .setMinRarity(SpellRarity.LEGENDARY)
                .setMaxLevel(MAX_LEVEL)
                .setCooldownSeconds(45)
                .setAllowCrafting(true);
    }

    /**
     * 获取施法类型
     * 
     * @return 施法类型（长施法）
     */
    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    /**
     * 获取法术流派
     * 
     * @return 火系魔法
     */
    @Override
    public SchoolType getSchoolType() {
        return SchoolRegistry.FIRE.get();
    }

    /**
     * 获取目标选择颜色
     * 
     * @return 目标选择颜色
     */
    @Override
    public Vector3f getTargetingColor() {
        return FIRE_COLOR;
    }

    /**
     * 获取最小稀有度
     * 
     * @return 最小稀有度
     */
    @Override
    public int getMinRarity() {
        return MIN_RARITY;
    }

    /**
     * 获取最大等级
     * 
     * @return 最大等级
     */
    @Override
    public int getMaxLevel() {
        return MAX_LEVEL;
    }

    /**
     * 获取最小等级
     * 
     * @return 最小等级
     */
    @Override
    public int getMinLevel() {
        return MIN_LEVEL;
    }

    /**
     * 获取最大稀有度
     * 
     * @return 最大稀有度
     */
    @Override
    public int getMaxRarity() {
        return MAX_RARITY;
    }

    /**
     * 获取蓝耗
     * 
     * @param level 法术等级
     * @return 蓝耗
     */
    @Override
    public int getManaCost(int level) {
        return BASE_MANA_COST + (level - 1) * MANA_COST_PER_LEVEL;
    }

    /**
     * 获取冷却时间
     * 
     * @return 冷却时间（tick）
     */
    @Override
    public int getSpellCooldown() {
        return BASE_COOLDOWN;
    }

    /**
     * 获取施法时间
     * 
     * @param level 法术等级
     * @return 施法时间（tick）
     */
    @Override
    public int getCastTime(int level) {
        return BASE_CAST_TIME;
    }

    /**
     * 获取施法开始音效
     * 
     * @return 施法开始音效
     */
    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return CAST_START_SOUND;
    }

    /**
     * 获取施法结束音效
     * 
     * @return 施法结束音效
     */
    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return CAST_FINISH_SOUND;
    }

    /**
     * 获取施法开始动画
     * 
     * @return 施法开始动画
     */
    @Override
    public AnimationHolder getCastStartAnimation() {
        return CAST_START_ANIMATION;
    }

    /**
     * 获取施法结束动画
     * 
     * @return 施法结束动画
     */
    @Override
    public AnimationHolder getCastFinishAnimation() {
        return CAST_FINISH_ANIMATION;
    }

    /**
     * 获取法术图标资源位置
     *
     * @return 法术图标资源位置
     */
    public ResourceLocation getSpellIcon() {
        return SPELL_ICON;
    }

    /**
     * 获取指向位置
     *
     * @param level    世界
     * @param entity   施法实体
     * @param maxRange 最大距离
     * @return 指向位置
     */
    private Vec3 getTargetPos(Level level, LivingEntity entity, double maxRange) {
        // 获取玩家视线方向
        Vec3 eyePos = entity.getEyePosition(1.0f);
        Vec3 lookVec = entity.getViewVector(1.0f);
        Vec3 endPos = eyePos.add(lookVec.x * maxRange, lookVec.y * maxRange, lookVec.z * maxRange);
        
        // 射线检测
        HitResult hitResult = level.clip(new net.minecraft.world.level.ClipContext(
                eyePos,
                endPos,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                entity
        ));
        
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            // 在击中方块的位置上方一点
            return new Vec3(blockHit.getBlockPos().getX() + 0.5, blockHit.getBlockPos().getY() + 1.0, blockHit.getBlockPos().getZ() + 0.5);
        } else if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) hitResult;
            return entityHit.getEntity().position();
        }
        
        // 如果没有命中任何目标，使用最远点
        return hitResult.getLocation();
    }

    /**
     * 检查施法前条件 - 在此处存储目标位置并播放预警效果
     *
     * @param level      世界
     * @param spellLevel 法术等级
     * @param entity     施法实体
     * @param magicData  魔法数据
     * @return 是否满足施法条件
     */
    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData magicData) {
        if (level instanceof ServerLevel serverLevel) {
            // 获取法术强度属性值（修正后的获取方式）
            float spellPower = SpellPowerHelper.getBaseSpellPowerAttribute(entity);
            int pullRange = getPullRange(spellLevel, spellPower);
            
            // 获取指向位置
            Vec3 targetPos = getTargetPos(level, entity, 50.0);
            
            // 存储目标位置和范围供后续使用
            UUID casterId = entity.getUUID();
            casterTargetPositions.put(casterId, targetPos);
            casterRanges.put(casterId, pullRange);
            
            // 播放预警粒子效果
            playWarningEffects(serverLevel, targetPos, pullRange);
        }
        
        return true;
    }

    /**
     * 服务器端每 tick 调用 - 在呤唱期间持续吸引敌人
     *
     * @param level       世界
     * @param spellLevel  法术等级
     * @param entity      施法实体
     * @param magicData   魔法数据
     */
    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, MagicData magicData) {
        if (level instanceof ServerLevel serverLevel) {
            UUID casterId = entity.getUUID();
            Vec3 centerPos = casterTargetPositions.get(casterId);
            Integer storedRange = casterRanges.get(casterId);
            
            if (centerPos != null && storedRange != null) {
                // 在呤唱期间持续吸引敌人（较弱的吸引力）
                performContinuousPull(serverLevel, centerPos, storedRange, entity);
            }
        }
    }

    /**
     * 施法逻辑 - 在施法结束时直接执行聚集和爆炸
     *
     * @param level       世界
     * @param spellLevel  法术等级
     * @param entity      施法实体
     * @param castSource  施法来源
     * @param magicData   魔法数据
     */
    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData magicData) {
        if (level instanceof ServerLevel serverLevel) {
            UUID casterId = entity.getUUID();
            
            // 从存储中获取目标位置和范围
            Vec3 centerPos = casterTargetPositions.get(casterId);
            Integer storedRange = casterRanges.get(casterId);
            
            // 如果没有存储的位置（可能是直接调用的），重新计算
            if (centerPos == null) {
                centerPos = getTargetPos(level, entity, 50.0);
            }

            // 获取法术强度属性值（修正后的获取方式）
            float spellPower = SpellPowerHelper.getBaseSpellPowerAttribute(entity);

            // 计算聚集范围和伤害
            int pullRange = (storedRange != null) ? storedRange : getPullRange(spellLevel, spellPower);
            float damage = getDamage(spellLevel, spellPower);
            
            // 播放聚集效果
            playPullEffects(serverLevel, centerPos, pullRange);
            
            // 执行聚集逻辑 - 将敌人拉向中心
            performPull(serverLevel, centerPos, pullRange, entity);
            
            // 直接执行爆炸（施法呤唱的1秒已经提供了足够的延迟）
            performExplosion(serverLevel, centerPos, pullRange, damage, entity);
            
            // 清理存储的数据
            casterTargetPositions.remove(casterId);
            casterRanges.remove(casterId);
        }

        super.onCast(level, spellLevel, entity, castSource, magicData);
    }

    /**
     * 播放预警效果 - 在施法期间显示
     *
     * @param level  服务器世界
     * @param pos    中心位置
     * @param range  聚集范围
     */
    private void playWarningEffects(ServerLevel level, Vec3 pos, int range) {
        // 播放预警音效
        level.playSound(
                null,
                pos.x, pos.y, pos.z,
                SoundEvents.FIRE_AMBIENT,
                net.minecraft.sounds.SoundSource.PLAYERS,
                0.5f,
                1.0f + level.random.nextFloat() * 0.5f
        );
        
        // 环形预警粒子 - 向外扩散的圆圈
        for (int ring = 1; ring <= 3; ring++) {
            double ringRadius = range * ring / 3.0;
            for (int i = 0; i < 16; i++) {
                double angle = (i / 16.0) * Math.PI * 2;
                double x = pos.x + Math.cos(angle) * ringRadius;
                double z = pos.z + Math.sin(angle) * ringRadius;
                
                level.sendParticles(
                        new SparkParticleOptions(WARNING_COLOR),
                        x, pos.y + 0.1, z,
                        1, 0, 0.1, 0, 0.01
                );
            }
        }
        
        // 中心旋转粒子
        level.sendParticles(
                ParticleRegistry.FIRE_PARTICLE.get(),
                pos.x, pos.y + 0.5, pos.z,
                10, 0.2, 0.2, 0.2, 0.05
        );
        
        // 向上的火花
        level.sendParticles(
                new SparkParticleOptions(WARNING_COLOR),
                pos.x, pos.y + 0.5, pos.z,
                15, range * 0.3, 0.5, range * 0.3, 0.3
        );
    }

    /**
     * 执行聚集 - 将范围内的敌人拉向中心
     *
     * @param level      服务器世界
     * @param centerPos  中心位置
     * @param range      聚集范围
     * @param caster     施法者
     */
    private void performPull(ServerLevel level, Vec3 centerPos, int range, LivingEntity caster) {
        AABB pullArea = new AABB(
                centerPos.x - range, centerPos.y - range, centerPos.z - range,
                centerPos.x + range, centerPos.y + range, centerPos.z + range
        );
        
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, pullArea, 
                entity -> entity != caster && entity.isAlive());
        
        for (LivingEntity target : targets) {
            Vec3 targetPos = target.position();
            Vec3 direction = centerPos.subtract(targetPos).normalize();
            double distance = targetPos.distanceTo(centerPos);
            
            // 根据距离调整拉取力度，越近拉得越慢
            double strength = PULL_STRENGTH * Math.min(1.0, distance / range);
            
            // 设置速度（向上一点防止卡在地里）
            target.setDeltaMovement(
                    direction.x * strength,
                    Math.max(0.2, direction.y * strength + 0.3),
                    direction.z * strength
            );
            target.hurtMarked = true;
        }
    }

    /**
     * 持续吸引 - 在呤唱期间每 tick 调用，吸引力较弱
     *
     * @param level      服务器世界
     * @param centerPos  中心位置
     * @param range      聚集范围
     * @param caster     施法者
     */
    private void performContinuousPull(ServerLevel level, Vec3 centerPos, int range, LivingEntity caster) {
        AABB pullArea = new AABB(
                centerPos.x - range, centerPos.y - range, centerPos.z - range,
                centerPos.x + range, centerPos.y + range, centerPos.z + range
        );
        
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, pullArea, 
                entity -> entity != caster && entity.isAlive());
        
        // 呤唱期间的吸引力度较弱（约为最终聚集的30%）
        double continuousStrength = PULL_STRENGTH * 0.3;
        
        for (LivingEntity target : targets) {
            Vec3 targetPos = target.position();
            Vec3 direction = centerPos.subtract(targetPos).normalize();
            double distance = targetPos.distanceTo(centerPos);
            
            // 根据距离调整拉取力度
            double strength = continuousStrength * Math.min(1.0, distance / range);
            
            // 添加当前速度，实现持续拉动效果
            Vec3 currentVelocity = target.getDeltaMovement();
            target.setDeltaMovement(
                    currentVelocity.x + direction.x * strength * 0.5,
                    currentVelocity.y + Math.max(0.05, direction.y * strength * 0.3),
                    currentVelocity.z + direction.z * strength * 0.5
            );
            target.hurtMarked = true;
        }
    }

    /**
     * 执行爆炸
     *
     * @param level      服务器世界
     * @param centerPos  中心位置
     * @param range      爆炸范围
     * @param damage     伤害值
     * @param caster     施法者
     */
    private void performExplosion(ServerLevel level, Vec3 centerPos, int range, float damage, LivingEntity caster) {
        // 播放爆炸效果
        playExplosionEffects(level, centerPos, range);
        
        // 对范围内敌人造成伤害
        AABB explosionArea = new AABB(
                centerPos.x - range, centerPos.y - range, centerPos.z - range,
                centerPos.x + range, centerPos.y + range, centerPos.z + range
        );
        
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, explosionArea, 
                entity -> entity != caster && entity.isAlive());
        
        for (LivingEntity target : targets) {
            // 造成火焰伤害
            target.hurt(level.damageSources().source(DamageTypes.IN_FIRE, caster), damage);
            // 点燃目标（仅对可点燃的实体）
            if (target instanceof net.minecraft.world.entity.player.Player player) {
                player.setRemainingFireTicks(100); // 5秒 = 100 tick
            }
        }
    }

    /**
     * 播放聚集效果
     *
     * @param level  服务器世界
     * @param pos    中心位置
     * @param range  聚集范围
     */
    private void playPullEffects(ServerLevel level, Vec3 pos, int range) {
        // 播放聚集音效
        level.playSound(
                null,
                pos.x, pos.y, pos.z,
                SoundEvents.FIRECHARGE_USE,
                net.minecraft.sounds.SoundSource.PLAYERS,
                1.0f,
                0.8f + level.random.nextFloat() * 0.4f
        );
        
        // 环形冲击波（向内收缩效果）
        level.sendParticles(
                new ShockwaveParticleOptions(FIRE_COLOR, range * 0.5f, true),
                pos.x, pos.y + 0.5, pos.z,
                1, 0, 0, 0, 0
        );
        
        // 火焰粒子
        for (int i = 0; i < range * 2; i++) {
            double angle = (i / (double) (range * 2)) * Math.PI * 2;
            double x = pos.x + Math.cos(angle) * range * 0.8;
            double z = pos.z + Math.sin(angle) * range * 0.8;
            
            level.sendParticles(
                    ParticleRegistry.FIRE_PARTICLE.get(),
                    x, pos.y + 0.5, z,
                    3, 0.2, 0.2, 0.2, 0.1
            );
        }
        
        // 火花粒子
        level.sendParticles(
                new SparkParticleOptions(FIRE_COLOR),
                pos.x, pos.y + 1, pos.z,
                20, range * 0.5, 0.5, range * 0.5, 0.5
        );
    }

    /**
     * 播放爆炸效果
     *
     * @param level  服务器世界
     * @param pos    中心位置
     * @param range  爆炸范围
     */
    private void playExplosionEffects(ServerLevel level, Vec3 pos, int range) {
        // 播放爆炸音效
        level.playSound(
                null,
                pos.x, pos.y, pos.z,
                SoundEvents.GENERIC_EXPLODE,
                net.minecraft.sounds.SoundSource.PLAYERS,
                1.5f,
                0.8f + level.random.nextFloat() * 0.4f
        );
        
        // 爆炸冲击波
        level.sendParticles(
                new BlastwaveParticleOptions(EXPLOSION_COLOR, range * 0.8f),
                pos.x, pos.y + 0.5, pos.z,
                1, 0, 0, 0, 0
        );
        
        // 火焰爆发
        level.sendParticles(
                ParticleRegistry.EMBER_PARTICLE.get(),
                pos.x, pos.y + 0.5, pos.z,
                range * 10, range * 0.3, 0.3, range * 0.3, 0.3
        );
        
        // 大型火焰粒子
        for (int i = 0; i < range; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range * 0.5;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            level.sendParticles(
                    ParticleRegistry.FIRE_PARTICLE.get(),
                    x, pos.y + 0.3, z,
                    5, 0.3, 0.5, 0.3, 0.1
            );
        }
    }

    /**
     * 获取聚集范围（带法术强度加成）
     *
     * @param spellLevel 法术等级
     * @param spellPower 法术强度
     * @return 聚集范围（格）
     */
    public int getPullRange(int spellLevel, float spellPower) {
        int baseRange = BASE_PULL_RANGE + (spellLevel - 1) * PULL_RANGE_PER_LEVEL;
        return (int) (baseRange * spellPower / 1.0f);
    }

    /**
     * 获取聚集范围（不带法术强度，用于显示）
     *
     * @param spellLevel 法术等级
     * @return 基础聚集范围（格）
     */
    public int getPullRange(int spellLevel) {
        return BASE_PULL_RANGE + (spellLevel - 1) * PULL_RANGE_PER_LEVEL;
    }

    /**
     * 获取伤害（带法术强度加成）
     *
     * @param spellLevel 法术等级
     * @param spellPower 法术强度
     * @return 伤害值
     */
    public float getDamage(int spellLevel, float spellPower) {
        int baseDamage = BASE_DAMAGE + (spellLevel - 1) * DAMAGE_PER_LEVEL;
        return baseDamage * spellPower / 1.0f;
    }

    /**
     * 获取伤害（不带法术强度，用于显示）
     *
     * @param spellLevel 法术等级
     * @return 基础伤害值
     */
    public float getDamage(int spellLevel) {
        return BASE_DAMAGE + (spellLevel - 1) * DAMAGE_PER_LEVEL;
    }

    /**
     * 获取独特信息（显示在法术书中）
     * 显示法术强度缩放后的实际数值
     *
     * @param level  法术等级
     * @param entity 实体
     * @return 独特信息列表
     */
    @Override
    public List<MutableComponent> getUniqueInfo(int level, LivingEntity entity) {
        // 获取法术强度属性值（修正后的获取方式）
        float spellPower = SpellPowerHelper.getBaseSpellPowerAttribute(entity);

        // 计算实际数值（带法术强度缩放）
        int actualRange = getPullRange(level, spellPower);
        float actualDamage = getDamage(level, spellPower);

        // 获取基础数值
        int baseRange = getPullRange(level);
        float baseDamage = getDamage(level);

        return List.of(
                net.minecraft.network.chat.Component.translatable("spell.legendarymage.implosion.range", actualRange, baseRange),
                net.minecraft.network.chat.Component.translatable("spell.legendarymage.implosion.damage", String.format("%.1f", actualDamage), String.format("%.1f", baseDamage)),
                net.minecraft.network.chat.Component.translatable("spell.legendarymage.implosion.spell_power", String.format("%.1f", spellPower))
        );
    }
}
