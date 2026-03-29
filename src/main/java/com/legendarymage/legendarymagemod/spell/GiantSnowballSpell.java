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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.entity.ModEntities;
import com.legendarymage.legendarymagemod.entity.spell.GiantSnowballEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 巨雪球法术
 * 铁魔法-冰系
 * 先在头顶生成一个小型的雪块，这个雪块会因吟唱时间的增加而变大，
 * 当吟唱结束或中断会被释放，击中造成巨大的冰爆，且留下一片10秒的暴风雪力场
 * 
 * @author Love_U
 * @version 1.0.1
 */
public class GiantSnowballSpell extends AbstractSpell {

    /**
     * 法术ID
     */
    public static final String SPELL_ID = "giant_snowball";

    /**
     * 基础蓝耗
     */
    private static final int BASE_MANA_COST = 300;

    /**
     * 每级蓝耗增量
     */
    private static final int MANA_COST_PER_LEVEL = 50;

    /**
     * 基础施法时间（tick）
     * 最大吟唱时间10秒 = 200 tick
     */
    private static final int MAX_CAST_TIME = 200;

    /**
     * 冷却时间（tick）
     * 1分钟 = 1200 tick
     */
    private static final int COOLDOWN_TICKS = 1200;

    /**
     * 施法间隔（tick）
     * 1秒 = 20 tick
     */
    private static final int CAST_INTERVAL_TICKS = 20;

    /**
     * 基础伤害
     */
    private static final int BASE_DAMAGE = 30;

    /**
     * 每级伤害增量
     */
    private static final int DAMAGE_PER_LEVEL = 15;

    /**
     * 基础爆炸范围（格）
     */
    private static final int BASE_EXPLOSION_RADIUS = 8;

    /**
     * 每级爆炸范围增量（格）
     */
    private static final int EXPLOSION_RADIUS_PER_LEVEL = 1;

    /**
     * 暴风雪力场持续时间（tick）
     * 固定10秒 = 200 tick
     */
    private static final int BLIZZARD_DURATION_TICKS = 200;

    /**
     * 最小等级
     */
    private static final int MIN_LEVEL = 1;

    /**
     * 最大等级
     */
    private static final int MAX_LEVEL = 5;

    /**
     * 最小稀有度（传奇）
     */
    private static final int MIN_RARITY = SpellRarity.LEGENDARY.getValue();

    /**
     * 最大稀有度（传奇）
     */
    private static final int MAX_RARITY = SpellRarity.LEGENDARY.getValue();

    /**
     * 冰系颜色
     */
    private static final Vector3f ICE_COLOR = new Vector3f(0.6f, 0.8f, 1.0f);

    /**
     * 法术资源位置
     */
    private static final ResourceLocation SPELL_RESOURCE = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, SPELL_ID);

    /**
     * 法术图标路径
     */
    private static final ResourceLocation SPELL_ICON = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, "textures/gui/spell_icons/giant_snowball.png");

    /**
     * 施法开始动画 - 使用铁魔法长施法动画
     */
    private static final AnimationHolder CAST_START_ANIMATION = new AnimationHolder(
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "long_cast"), 
            true);

    /**
     * 施法结束动画
     */
    private static final AnimationHolder CAST_FINISH_ANIMATION = new AnimationHolder(
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "long_cast_finish"),
            true);

    /**
     * 施法开始音效
     */
    private static final Optional<SoundEvent> CAST_START_SOUND = Optional.of(SoundEvents.SNOW_PLACE);

    /**
     * 施法结束音效
     */
    private static final Optional<SoundEvent> CAST_FINISH_SOUND = Optional.of(SoundEvents.GLASS_BREAK);

    /**
     * 存储施法者当前雪球的映射
     * 键：施法者UUID，值：当前雪球的缩放值
     */
    private static final Map<UUID, Float> casterSnowballScales = new HashMap<>();

    /**
     * 存储施法者当前雪球的实体映射
     */
    private static final Map<UUID, GiantSnowballEntity> casterSnowballEntities = new HashMap<>();

    /**
     * 存储施法者的法术等级
     */
    private static final Map<UUID, Integer> casterSpellLevels = new HashMap<>();

    /**
     * 构造函数
     */
    public GiantSnowballSpell() {
        this.baseManaCost = BASE_MANA_COST;
        this.manaCostPerLevel = MANA_COST_PER_LEVEL;
        this.castTime = MAX_CAST_TIME;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
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
                .setCooldownSeconds(60)
                .setAllowCrafting(false);  // 不可书写
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
     * @return 冰系魔法
     */
    @Override
    public SchoolType getSchoolType() {
        return SchoolRegistry.ICE.get();
    }

    /**
     * 获取目标选择颜色
     * 
     * @return 目标选择颜色
     */
    @Override
    public Vector3f getTargetingColor() {
        return ICE_COLOR;
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
        return COOLDOWN_TICKS;
    }

    /**
     * 获取施法时间
     * 
     * @param level 法术等级
     * @return 施法时间（tick）
     */
    @Override
    public int getCastTime(int level) {
        return MAX_CAST_TIME;
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
     * 检查施法前条件
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
            UUID casterId = entity.getUUID();
            
            // 存储法术等级
            casterSpellLevels.put(casterId, spellLevel);
            
            // 初始化雪球缩放值
            casterSnowballScales.put(casterId, 0.1f);
            
            // 在施法者头顶创建雪球实体
            Vec3 headPos = entity.position().add(0, entity.getBbHeight() + 1.5, 0);
            GiantSnowballEntity snowball = new GiantSnowballEntity(serverLevel, entity, headPos, 0.1f);
            serverLevel.addFreshEntity(snowball);
            
            casterSnowballEntities.put(casterId, snowball);
        }
        
        return true;
    }

    /**
     * 服务器端每 tick 调用 - 在吟唱期间雪球逐渐变大
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
            GiantSnowballEntity snowball = casterSnowballEntities.get(casterId);
            
            if (snowball != null && snowball.isAlive()) {
                // 计算当前吟唱进度（0.0 - 1.0）
                int currentCastTime = magicData.getCastDurationRemaining();
                float progress = 1.0f - ((float) currentCastTime / MAX_CAST_TIME);
                
                // 雪球大小从0.1到1.0
                float newScale = 0.1f + (progress * 0.9f);
                casterSnowballScales.put(casterId, newScale);
                
                // 更新雪球大小和位置
                snowball.setScale(newScale);
                Vec3 headPos = entity.position().add(0, entity.getBbHeight() + 1.5 + (newScale * 0.5), 0);
                snowball.setPos(headPos);
            }
        }
    }

    /**
     * 施法逻辑 - 在施法结束时释放雪球
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
            
            LegendaryMage.LOGGER.info("[巨雪球] onCast开始 - 施法者: {}, 等级: {}", casterId, spellLevel);
            
            // 获取当前雪球
            GiantSnowballEntity snowball = casterSnowballEntities.get(casterId);
            float scale = casterSnowballScales.getOrDefault(casterId, 0.1f);
            
            LegendaryMage.LOGGER.info("[巨雪球] 当前雪球: {}, 大小: {}", snowball != null ? snowball.getId() : "null", scale);
            
            // 获取法术强度
            float spellPower = SpellPowerHelper.getBaseSpellPowerAttribute(entity);
            
            // 计算伤害和范围
            float damage = getDamage(spellLevel, spellPower, scale);
            int explosionRadius = getExplosionRadius(spellLevel, spellPower);
            
            LegendaryMage.LOGGER.info("[巨雪球] 计算结果 - 伤害: {}, 范围: {}, 法术强度: {}", damage, explosionRadius, spellPower);
            
            if (snowball != null && snowball.isAlive()) {
                LegendaryMage.LOGGER.info("[巨雪球] 雪球存在且存活，准备发射");
                
                // 计算发射方向（玩家视线方向）
                Vec3 lookVec = entity.getViewVector(1.0f).normalize();
                LegendaryMage.LOGGER.info("[巨雪球] 视线方向: {}", lookVec);
                
                // 关键修复：将雪球设置到玩家眼睛前方，避免与玩家碰撞
                // 从玩家眼睛位置开始，沿视线方向向前移动3格
                Vec3 eyePos = entity.getEyePosition(1.0f);
                Vec3 launchPos = eyePos.add(lookVec.scale(3.0));
                snowball.setPos(launchPos);
                LegendaryMage.LOGGER.info("[巨雪球] 眼睛位置: {}, 调整后的发射位置: {}", eyePos, launchPos);
                
                // 设置雪球为发射状态
                snowball.setLaunched(true);
                LegendaryMage.LOGGER.info("[巨雪球] setLaunched(true)已调用");
                
                // 清除施法者引用，防止tick中继续跟随
                snowball.setCaster(null);
                LegendaryMage.LOGGER.info("[巨雪球] setCaster(null)已调用");
                
                // 发射雪球
                // Projectile.shoot() 需要 (x, y, z, velocity, inaccuracy)
                snowball.shoot(lookVec.x, lookVec.y, lookVec.z, 1.5f, 0);
                LegendaryMage.LOGGER.info("[巨雪球] shoot()已调用，发射后DeltaMovement: {}", snowball.getDeltaMovement());
                
                // 设置伤害和范围
                snowball.setDamage(damage);
                snowball.setExplosionRadius(explosionRadius);
                snowball.setBlizzardDuration(BLIZZARD_DURATION_TICKS);
                
                LegendaryMage.LOGGER.info("[巨雪球] 雪球已发射完成！位置: {}, 速度: {}", snowball.position(), snowball.getDeltaMovement().length());
            } else {
                LegendaryMage.LOGGER.warn("[巨雪球] 雪球不存在或已死亡，直接触发爆炸");
                // 如果雪球不存在，直接在当前位置触发爆炸
                Vec3 pos = entity.position().add(0, entity.getBbHeight() + 2, 0);
                triggerExplosion(serverLevel, pos, damage, explosionRadius, entity);
                createBlizzardField(serverLevel, pos, explosionRadius, entity);
            }
            
            // 清理存储的数据
            casterSnowballScales.remove(casterId);
            casterSnowballEntities.remove(casterId);
            casterSpellLevels.remove(casterId);
            
            LegendaryMage.LOGGER.info("[巨雪球] onCast结束 - 数据已清理");
        }

        super.onCast(level, spellLevel, entity, castSource, magicData);
    }

    /**
     * 施法被取消时的处理
     *
     * @param level      世界
     * @param spellLevel 法术等级
     * @param entity     施法实体
     */
    public void onCastCanceled(Level level, int spellLevel, LivingEntity entity) {
        if (level instanceof ServerLevel serverLevel) {
            UUID casterId = entity.getUUID();
            
            // 获取当前雪球
            GiantSnowballEntity snowball = casterSnowballEntities.get(casterId);
            float scale = casterSnowballScales.getOrDefault(casterId, 0.1f);
            
            // 获取法术强度
            float spellPower = SpellPowerHelper.getBaseSpellPowerAttribute(entity);
            
            // 计算伤害和范围（根据当前大小）
            float damage = getDamage(spellLevel, spellPower, scale);
            int explosionRadius = getExplosionRadius(spellLevel, spellPower);
            
            if (snowball != null && snowball.isAlive()) {
                // 计算发射方向（玩家视线方向）
                Vec3 lookVec = entity.getViewVector(1.0f).normalize();
                
                // 关键修复：将雪球设置到玩家眼睛前方，避免与玩家碰撞
                Vec3 eyePos = entity.getEyePosition(1.0f);
                Vec3 launchPos = eyePos.add(lookVec.scale(3.0));
                snowball.setPos(launchPos);
                
                // 设置雪球为发射状态
                snowball.setLaunched(true);
                
                // 清除施法者引用，防止tick中继续跟随
                snowball.setCaster(null);
                
                // 发射雪球
                // Projectile.shoot() 需要 (x, y, z, velocity, inaccuracy)
                snowball.shoot(lookVec.x, lookVec.y, lookVec.z, 1.5f, 0);
                
                // 设置伤害和范围
                snowball.setDamage(damage);
                snowball.setExplosionRadius(explosionRadius);
                snowball.setBlizzardDuration(BLIZZARD_DURATION_TICKS);
            }
            
            // 清理存储的数据
            casterSnowballScales.remove(casterId);
            casterSnowballEntities.remove(casterId);
            casterSpellLevels.remove(casterId);
        }
    }

    /**
     * 触发冰爆
     *
     * @param level           服务器世界
     * @param center          爆炸中心
     * @param damage          伤害值
     * @param explosionRadius 爆炸范围
     * @param caster          施法者
     */
    public static void triggerExplosion(ServerLevel level, Vec3 center, float damage, int explosionRadius, LivingEntity caster) {
        // 播放爆炸音效
        level.playSound(
                null,
                center.x, center.y, center.z,
                SoundEvents.GLASS_BREAK,
                net.minecraft.sounds.SoundSource.PLAYERS,
                1.5f,
                0.8f + level.random.nextFloat() * 0.4f
        );

        // 播放爆炸粒子效果
        level.sendParticles(
                io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions.class.cast(
                    new io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions(
                        new Vector3f(0.4f, 0.7f, 1.0f), explosionRadius * 0.5f)
                ),
                center.x, center.y + 0.5, center.z,
                1, 0, 0, 0, 0
        );

        // 播放雪花粒子效果
        level.sendParticles(
                io.redspace.ironsspellbooks.registries.ParticleRegistry.SNOWFLAKE_PARTICLE.get(),
                center.x, center.y + 0.5, center.z,
                explosionRadius * 10,
                explosionRadius * 0.3, 0.3, explosionRadius * 0.3,
                0.1
        );

        // 对范围内敌人造成伤害
        net.minecraft.world.phys.AABB explosionArea = new net.minecraft.world.phys.AABB(
                center.x - explosionRadius, center.y - explosionRadius, center.z - explosionRadius,
                center.x + explosionRadius, center.y + explosionRadius, center.z + explosionRadius
        );

        java.util.List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                explosionArea,
                target -> target != caster && target.isAlive()
        );

        for (LivingEntity target : targets) {
            double distance = target.position().distanceTo(center);
            if (distance <= explosionRadius) {
                // 根据距离衰减伤害
                float damageMultiplier = 1.0f - (float) (distance / explosionRadius) * 0.5f;
                float actualDamage = damage * damageMultiplier;

                target.hurt(
                        level.damageSources().magic(),
                        actualDamage
                );

                // 给目标添加缓慢和冰冻效果
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                        100, // 5秒
                        2,
                        false,
                        true
                ));
            }
        }
    }

    /**
     * 创建暴风雪力场
     *
     * @param level           服务器世界
     * @param center          中心位置
     * @param explosionRadius 爆炸范围（力场大小）
     * @param caster          施法者
     */
    public static void createBlizzardField(ServerLevel level, Vec3 center, int explosionRadius, LivingEntity caster) {
        // 使用BlizzardManager创建暴风雪力场
        float blizzardDamage = 5.0f; // 暴风雪每秒伤害
        BlizzardManager.createBlizzard(level, center, explosionRadius, BLIZZARD_DURATION_TICKS, blizzardDamage, caster);
    }

    /**
     * 获取伤害（带法术强度加成和吟唱进度）
     *
     * @param spellLevel 法术等级
     * @param spellPower 法术强度
     * @param scale      雪球大小（0.1 - 1.0）
     * @return 伤害值
     */
    public float getDamage(int spellLevel, float spellPower, float scale) {
        int baseDamage = BASE_DAMAGE + (spellLevel - 1) * DAMAGE_PER_LEVEL;
        float scaledDamage = baseDamage * scale; // 根据雪球大小缩放
        return scaledDamage * spellPower / 1.0f;
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
     * 获取爆炸范围（带法术强度加成）
     *
     * @param spellLevel 法术等级
     * @param spellPower 法术强度
     * @return 爆炸范围（格）
     */
    public int getExplosionRadius(int spellLevel, float spellPower) {
        int baseRadius = BASE_EXPLOSION_RADIUS + (spellLevel - 1) * EXPLOSION_RADIUS_PER_LEVEL;
        return (int) (baseRadius * spellPower / 1.0f);
    }

    /**
     * 获取爆炸范围（不带法术强度，用于显示）
     *
     * @param spellLevel 法术等级
     * @return 基础爆炸范围（格）
     */
    public int getExplosionRadius(int spellLevel) {
        return BASE_EXPLOSION_RADIUS + (spellLevel - 1) * EXPLOSION_RADIUS_PER_LEVEL;
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
        // 获取法术强度属性值
        float spellPower = SpellPowerHelper.getBaseSpellPowerAttribute(entity);

        // 计算实际数值（带法术强度缩放）
        int actualRadius = getExplosionRadius(level, spellPower);
        float actualDamage = getDamage(level, spellPower, 1.0f); // 最大伤害

        // 获取基础数值
        int baseRadius = getExplosionRadius(level);
        float baseDamage = getDamage(level);

        return List.of(
                Component.translatable("spell.legendarymage.giant_snowball.damage", String.format("%.1f", actualDamage), String.format("%.1f", baseDamage)),
                Component.translatable("spell.legendarymage.giant_snowball.radius", actualRadius, baseRadius),
                Component.translatable("spell.legendarymage.giant_snowball.duration", BLIZZARD_DURATION_TICKS / 20),
                Component.translatable("spell.legendarymage.giant_snowball.spell_power", String.format("%.1f", spellPower))
        );
    }
}
