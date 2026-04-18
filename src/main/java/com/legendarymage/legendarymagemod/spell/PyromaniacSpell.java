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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;

import com.legendarymage.legendarymagemod.Config;
import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.effect.ModEffects;
import com.legendarymage.legendarymagemod.spell.SpellPowerHelper;

import java.util.List;
import java.util.Optional;

/**
 * 纵火狂法术
 * 向四周的敌人散发可叠加等级的"烈焰"Buff
 * 烈焰Buff类似凋零效果，但当带有烈焰的生物死亡时会触发小型爆炸
 * 
 * @author Love_U
 * @version 1.0.7
 */
public class PyromaniacSpell extends AbstractSpell {

    /**
     * 法术ID
     */
    public static final String SPELL_ID = "pyromaniac";

    /**
     * 基础蓝耗
     */
    private static final int BASE_MANA_COST = 150;

    /**
     * 每级蓝耗增量
     */
    private static final int MANA_COST_PER_LEVEL = 20;

    /**
     * 基础施法时间（tick）
     * 5秒 = 100 tick
     */
    private static final int BASE_CAST_TIME = 100;

    /**
     * 冷却时间（tick）
     * 100秒 = 2000 tick
     */
    private static final int COOLDOWN_TICKS = 2000;

    /**
     * 基础范围（格）
     */
    private static final int BASE_RANGE = 5;

    /**
     * 每级范围增量（格）
     */
    private static final int RANGE_PER_LEVEL = 1;

    /**
     * 基础Buff持续时间（秒）
     */
    private static final int BASE_EFFECT_DURATION = 10;

    /**
     * 每级Buff持续时间增量（秒）
     */
    private static final int EFFECT_DURATION_PER_LEVEL = 1;

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
     * 法术图标资源位置
     */
    private static final ResourceLocation SPELL_ICON = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, "textures/gui/spell_icons/pyromaniac.png");

    /**
     * 法术资源位置
     */
    private static final ResourceLocation SPELL_RESOURCE = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, SPELL_ID);

    /**
     * 施法开始动画 - 使用铁魔法火系法术的通用施法动画
     */
    private static final AnimationHolder CAST_START_ANIMATION = new AnimationHolder(
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "long_cast"),
            true);

    /**
     * 施法结束动画 - 使用铁魔法火系法术的施法完成动画
     */
    private static final AnimationHolder CAST_FINISH_ANIMATION = new AnimationHolder(
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "long_cast_finish"),
            true);

    /**
     * 施法开始音效
     */
    private static final Optional<SoundEvent> CAST_START_SOUND = Optional.of(SoundEvents.BLAZE_AMBIENT);

    /**
     * 施法结束音效
     */
    private static final Optional<SoundEvent> CAST_FINISH_SOUND = Optional.of(SoundEvents.FIRECHARGE_USE);

    /**
     * 目标选择颜色（火焰橙红色）
     */
    private static final Vector3f TARGETING_COLOR = new Vector3f(1.0f, 0.3f, 0.0f);

    /**
     * 构造函数
     */
    public PyromaniacSpell() {
        this.baseManaCost = BASE_MANA_COST;
        this.manaCostPerLevel = MANA_COST_PER_LEVEL;
        this.castTime = BASE_CAST_TIME;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 2;
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
                .setCooldownSeconds(6)
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
        return TARGETING_COLOR;
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
     * 施法逻辑
     * 向周围敌人施加烈焰Buff
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
            Vec3 pos = entity.position();

            // 获取法术强度属性值（修正后的获取方式）
            float spellPower = SpellPowerHelper.getBaseSpellPowerAttribute(entity);

            // 计算范围和Buff等级（带法术强度缩放）
            int range = getRange(spellLevel, spellPower);
            int effectLevel = getEffectLevel(spellLevel);  // Buff等级 = 法术等级 - 1（0-based）
            int effectDuration = getEffectDuration(spellLevel, spellPower);

            // 获取范围内的所有敌人
            AABB searchArea = new AABB(
                    pos.x - range, pos.y - range, pos.z - range,
                    pos.x + range, pos.y + range, pos.z + range
            );

            List<LivingEntity> targets = level.getEntitiesOfClass(
                    LivingEntity.class,
                    searchArea,
                    target -> canTargetEntity(entity, target)
            );

            int affectedCount = 0;

            // 对每个目标施加烈焰效果
            for (LivingEntity target : targets) {
                // 检查目标是否已经有烈焰效果
                MobEffectInstance existingEffect = target.getEffect(ModEffects.PYRO_FLAME);
                
                int newAmplifier;
                if (existingEffect != null) {
                    // 基于现有等级实时计算新等级
                    // 公式：新等级 = 现有等级 + (现有等级 / 2) + 法术基础等级
                    // 这样等级越高，叠加越快，但始终保持增长
                    int currentAmplifier = existingEffect.getAmplifier();
                    newAmplifier = currentAmplifier + (currentAmplifier / 2) + effectLevel;
                } else {
                    // 新目标，使用基础等级
                    newAmplifier = effectLevel;
                }
                
                // 创建效果实例（叠加后的等级）
                MobEffectInstance effectInstance = new MobEffectInstance(
                        ModEffects.PYRO_FLAME,
                        effectDuration,
                        newAmplifier,
                        false,  // 不显示粒子
                        true,   // 显示图标
                        true    // 显示效果名称
                );

                // 添加效果
                target.addEffect(effectInstance);
                affectedCount++;

                // 播放单个目标受击效果
                PyromaniacParticles.playTargetHitEffect(serverLevel, target.position(), target.getBbHeight(), target.getBbWidth());
            }

            // 播放施法效果
            playCastEffects(serverLevel, pos, range);
        }

        super.onCast(level, spellLevel, entity, castSource, magicData);
    }

    /**
     * 获取范围（带法术强度缩放）
     *
     * @param spellLevel 法术等级
     * @param spellPower 法术强度
     * @return 范围（格）
     */
    public int getRange(int spellLevel, float spellPower) {
        int baseRange = BASE_RANGE + (spellLevel - 1) * RANGE_PER_LEVEL;
        return (int) (baseRange * spellPower / 1.0f);
    }

    /**
     * 获取范围（不带法术强度，用于显示）
     *
     * @param spellLevel 法术等级
     * @return 基础范围（格）
     */
    public int getRange(int spellLevel) {
        return BASE_RANGE + (spellLevel - 1) * RANGE_PER_LEVEL;
    }

    /**
     * 获取Buff等级
     * 
     * @param spellLevel 法术等级
     * @return Buff等级（0-based）
     */
    public int getEffectLevel(int spellLevel) {
        // 法术等级1-5 对应 Buff等级0-4
        return spellLevel - 1;
    }

    /**
     * 获取Buff持续时间（带法术强度缩放）
     *
     * @param spellLevel 法术等级
     * @param spellPower 法术强度
     * @return 持续时间（tick）
     */
    public int getEffectDuration(int spellLevel, float spellPower) {
        int baseDuration = (BASE_EFFECT_DURATION + (spellLevel - 1) * EFFECT_DURATION_PER_LEVEL) * 20;  // 转换为tick
        return (int) (baseDuration * spellPower / 1.0f);
    }

    /**
     * 获取Buff持续时间（不带法术强度，用于显示）
     *
     * @param spellLevel 法术等级
     * @return 基础持续时间（秒）
     */
    public int getEffectDuration(int spellLevel) {
        return BASE_EFFECT_DURATION + (spellLevel - 1) * EFFECT_DURATION_PER_LEVEL;
    }

    /**
     * 播放施法效果
     * 
     * @param level  服务器世界
     * @param pos    位置
     * @param range  范围
     */
    private void playCastEffects(ServerLevel level, Vec3 pos, int range) {
        // 播放音效
        level.playSound(
                null,
                pos.x,
                pos.y,
                pos.z,
                SoundEvents.BLAZE_SHOOT,
                net.minecraft.sounds.SoundSource.PLAYERS,
                1.0f,
                0.8f + level.random.nextFloat() * 0.4f
        );

        // 使用新的粒子系统
        PyromaniacParticles.playCastBurst(level, pos, range);
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
        int actualRange = getRange(level, spellPower);
        int actualDuration = getEffectDuration(level, spellPower) / 20;  // 转换为秒

        // 获取基础数值
        int baseRange = getRange(level);
        int baseDuration = getEffectDuration(level);
        int effectLevel = getEffectLevel(level) + 1;  // 显示为1-based

        return List.of(
                Component.translatable("spell.legendarymage.pyromaniac.range", actualRange, baseRange),
                Component.translatable("spell.legendarymage.pyromaniac.duration", actualDuration, baseDuration),
                Component.translatable("spell.legendarymage.pyromaniac.effect_level", effectLevel),
                Component.translatable("spell.legendarymage.pyromaniac.spell_power", String.format("%.1f", spellPower))
        );
    }

    /**
     * 判断施法者是否可以目标指定实体
     * 检查是否为敌对关系，不会伤害友军和召唤物
     * 同时检查实体是否在烈焰Buff黑名单中
     * 
     * @param caster 施法者
     * @param target 目标
     * @return 是否可以目标
     */
    private boolean canTargetEntity(LivingEntity caster, LivingEntity target) {
        // 不能目标自己
        if (caster == target) {
            return false;
        }
        
        // 检查实体是否在烈焰Buff黑名单中
        if (isEntityInPyroFlameBlacklist(target)) {
            return false;
        }
        
        // 从配置读取是否影响友军和召唤物
        boolean affectAllies = Config.PYROMANIAC_AFFECT_ALLIES.get();
        boolean affectSummons = Config.PYROMANIAC_AFFECT_SUMMONS.get();
        
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
     * 检查实体是否在烈焰Buff黑名单中
     * 
     * @param target 目标实体
     * @return 是否在黑名单中
     */
    private boolean isEntityInPyroFlameBlacklist(LivingEntity target) {
        // 获取黑名单列表
        var blacklist = Config.PYRO_FLAME_ENTITY_BLACKLIST.get();

        // 如果黑名单为空，返回false
        if (blacklist == null || blacklist.isEmpty()) {
            return false;
        }

        // 获取实体的资源ID（如 "minecraft:creeper"）
        String entityTypeId = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString();

        // 检查实体类型是否在黑名单中
        for (String blacklistedEntity : blacklist) {
            if (blacklistedEntity != null && !blacklistedEntity.isEmpty()) {
                // 支持两种格式："minecraft:creeper" 或 "creeper"
                if (blacklistedEntity.equals(entityTypeId) ||
                    blacklistedEntity.equals(entityTypeId.replace("minecraft:", ""))) {
                    return true;
                }
            }
        }

        return false;
    }
}
