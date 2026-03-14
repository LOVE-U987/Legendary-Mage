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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

import org.joml.Vector3f;

import com.legendarymage.legendarymagemod.Config;
import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.spell.SpellPowerHelper;

/**
 * 复苏符文法术
 * 在地表创建一片区域，在该区域里死亡的所有生物将转化为施法者的亡灵
 * 
 * @author Love_U
 * @version 0.0.1
 */
public class ResurrectionRuneSpell extends AbstractSpell {

    /**
     * 法术ID
     */
    public static final String SPELL_ID = "resurrection_rune";

    /**
 * 基础蓝耗
 */
private static final int BASE_MANA_COST = 100;

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
 * 基础冷却时间（tick）
 * 1分钟 = 1200 tick
 */
private static final int BASE_COOLDOWN = 1200;

/**
 * 基础持续时间（秒）
 */
private static final int BASE_DURATION = 15;

/**
 * 每级持续时间增量（秒）
 */
private static final int DURATION_PER_LEVEL = 5;

/**
 * 基础范围（格）
 */
private static final int BASE_RANGE = 7;

/**
 * 每级范围增量（格）
 */
private static final int RANGE_PER_LEVEL = 1;

/**
 * 基础法术强度
 */
private static final int BASE_SPELL_POWER = 1;

/**
 * 每级法术强度增量
 */
private static final int SPELL_POWER_PER_LEVEL = 2;

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
            LegendaryMage.MODID, "textures/gui/spell_icons/resurrection_rune.png");

    /**
     * 法术资源位置
     */
    private static final ResourceLocation SPELL_RESOURCE = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, SPELL_ID);

    /**
     * 施法开始动画 - 使用铁魔法内置的长施法动画
     */
    private static final AnimationHolder CAST_START_ANIMATION = new AnimationHolder(
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "long_cast"),
            true);

    /**
     * 施法结束动画 - 使用铁魔法内置的长施法结束动画
     */
    private static final AnimationHolder CAST_FINISH_ANIMATION = new AnimationHolder(
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "long_cast_finish"),
            true);

    /**
     * 施法开始音效
     */
    private static final Optional<SoundEvent> CAST_START_SOUND = Optional.of(SoundEvents.EVOKER_PREPARE_SUMMON);

    /**
     * 施法结束音效
     */
    private static final Optional<SoundEvent> CAST_FINISH_SOUND = Optional.of(SoundEvents.EVOKER_CAST_SPELL);

    /**
     * 目标选择颜色（血系魔法 - 深红色）
     */
    private static final Vector3f TARGETING_COLOR = new Vector3f(0.6f, 0.0f, 0.0f);

    /**
     * 构造函数
     */
    public ResurrectionRuneSpell() {
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
                .setCooldownSeconds(60)
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
     * @return 血系魔法
     */
    @Override
    public SchoolType getSchoolType() {
        return SchoolRegistry.BLOOD.get();
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
     * 施法逻辑
     *
     * @param level       世界
     * @param spellLevel  法术等级
     * @param entity      施法实体
     * @param castSource  施法来源
     * @param magicData   魔法数据
     */
    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData magicData) {
        if (level instanceof ServerLevel serverLevel && entity instanceof ServerPlayer player) {
            Vec3 pos = player.position();

            // 获取法术强度属性值（修正后的获取方式）
            float spellPower = SpellPowerHelper.getBaseSpellPowerAttribute(entity);

            // 计算持续时间和范围（带法术强度缩放）
            int duration = getDuration(spellLevel, spellPower);
            int range = getRange(spellLevel, spellPower);

            // 创建复苏符文区域 - 使用load方法确保从世界数据存储获取
            ResurrectionRuneManager manager = ResurrectionRuneManager.load(serverLevel);

            manager.createRune(
                serverLevel,
                pos.x,
                pos.y,
                pos.z,
                range,
                duration,
                player.getUUID(),
                spellPower
            );

            // 播放施法效果
            playCastEffects(serverLevel, pos, range);
        }

        super.onCast(level, spellLevel, entity, castSource, magicData);
    }

    /**
     * 获取持续时间（带法术强度缩放）
     *
     * @param spellLevel 法术等级
     * @param spellPower 法术强度
     * @return 持续时间（秒）
     */
    public int getDuration(int spellLevel, float spellPower) {
        int baseDuration = BASE_DURATION + (spellLevel - 1) * DURATION_PER_LEVEL;
        double multiplier = Config.RESURRECTION_RUNE_SPELL_POWER_MULTIPLIER.get();
        return (int) (baseDuration * spellPower / 1.0f * multiplier);
    }

    /**
     * 获取持续时间（不带法术强度，用于显示）
     *
     * @param spellLevel 法术等级
     * @return 基础持续时间（秒）
     */
    public int getDuration(int spellLevel) {
        return BASE_DURATION + (spellLevel - 1) * DURATION_PER_LEVEL;
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
        double multiplier = Config.RESURRECTION_RUNE_SPELL_POWER_MULTIPLIER.get();
        return (int) (baseRange * spellPower / 1.0f * multiplier);
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
     * 播放施法效果 - 使用铁魔法风格的粒子系统
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
                SoundEvents.EVOKER_CAST_SPELL,
                net.minecraft.sounds.SoundSource.PLAYERS,
                1.0f,
                0.8f + level.random.nextFloat() * 0.4f
        );

        // 使用铁魔法风格的粒子效果
        ResurrectionRuneParticles.playCastBurst(level, pos, range);
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
        int actualDuration = getDuration(level, spellPower);
        int actualRange = getRange(level, spellPower);

        // 获取基础数值
        int baseDuration = getDuration(level);
        int baseRange = getRange(level);

        return List.of(
                Component.translatable("spell.legendarymage.resurrection_rune.duration", actualDuration, baseDuration),
                Component.translatable("spell.legendarymage.resurrection_rune.range", actualRange, baseRange),
                Component.translatable("spell.legendarymage.resurrection_rune.spell_power", String.format("%.1f", spellPower))
        );
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
        // 检查是否在地表
        if (!level.dimensionType().hasSkyLight()) {
            if (entity instanceof Player player) {
                player.sendSystemMessage(Component.translatable(
                        "spell.legendarymage.resurrection_rune.requires_surface"
                ));
            }
            return false;
        }
        
        return true;
    }
}
