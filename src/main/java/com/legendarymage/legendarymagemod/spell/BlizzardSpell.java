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
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
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
import com.legendarymage.legendarymagemod.Config;
import com.legendarymage.legendarymagemod.sound.ModSounds;

import java.util.List;
import java.util.Optional;

/**
 * 暴风雪法术
 * 选定一个目标，在其位置创造一片暴风雪区域，该区域内的敌人每隔0.5秒会受到一次冰冻伤害
 * 法术等级增加范围和伤害，法术强度也影响范围和伤害
 * 
 * @author Love_U
 * @version 0.0.1
 */
public class BlizzardSpell extends AbstractSpell {

    /**
     * 法术ID
     */
    public static final String SPELL_ID = "blizzard";

    /**
     * 基础蓝耗
     */
    private static final int BASE_MANA_COST = 300;

    /**
     * 每级蓝耗增量
     */
    private static final int MANA_COST_PER_LEVEL = 30;

    /**
     * 基础施法时间（tick）
     * 瞬发 = 0 tick
     */
    private static final int BASE_CAST_TIME = 0;

    /**
     * 冷却时间（tick）
     * 1分钟 = 1200 tick
     */
    private static final int COOLDOWN_TICKS = 1200;

    /**
     * 每15%法术强度增加的范围（格）
     */
    private static final double RANGE_PER_SPELL_POWER_PERCENT = 1.0 / 15.0;

    /**
     * 每15%法术强度增加的伤害
     */
    private static final float DAMAGE_PER_SPELL_POWER_PERCENT = 2.0f / 15.0f;

    /**
     * 最小等级
     */
    private static final int MIN_LEVEL = 1;

    /**
     * 最大等级
     */
    private static final int MAX_LEVEL = 3;

    /**
     * 最小稀有度（传奇）
     */
    private static final int MIN_RARITY = SpellRarity.LEGENDARY.getValue();

    /**
     * 最大稀有度（传奇）
     */
    private static final int MAX_RARITY = SpellRarity.LEGENDARY.getValue();

    /**
     * 伤害间隔（tick）
     * 0.5秒 = 10 tick
     */
    private static final int DAMAGE_INTERVAL_TICKS = 10;

    /**
     * 法术图标资源位置
     */
    private static final ResourceLocation SPELL_ICON = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, "textures/gui/spell_icons/blizzard.png");

    /**
     * 法术资源位置
     */
    private static final ResourceLocation SPELL_RESOURCE = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, SPELL_ID);

    /**
     * 施法开始动画 - 使用铁魔法冰系法术的通用施法动画
     */
    private static final AnimationHolder CAST_START_ANIMATION = new AnimationHolder(
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "instant_cast"),
            true);

    /**
     * 施法结束动画 - 使用铁魔法冰系法术的施法完成动画
     */
    private static final AnimationHolder CAST_FINISH_ANIMATION = new AnimationHolder(
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "instant_cast_finish"),
            true);

    /**
     * 施法音效 - 使用自定义暴风雪音效
     */
    private static final Optional<SoundEvent> CAST_SOUND = Optional.of(ModSounds.BLIZZARD_CAST.get());

    /**
     * 目标选择颜色（冰蓝色）
     */
    private static final Vector3f TARGETING_COLOR = new Vector3f(0.6f, 0.8f, 1.0f);

    /**
     * 构造函数
     */
    public BlizzardSpell() {
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
                .setCooldownSeconds(60)
                .setAllowCrafting(true);  // 可书写
    }

    /**
     * 获取施法类型
     * 
     * @return 施法类型（瞬发）
     */
    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
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
     * 获取施法音效
     * 
     * @return 施法音效
     */
    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return CAST_SOUND;
    }

    /**
     * 获取施法结束音效
     * 
     * @return 施法结束音效
     */
    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return CAST_SOUND;
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
     * 检查施法前条件
     * 需要选定一个目标才能施放
     *
     * @param level      世界
     * @param spellLevel 法术等级
     * @param entity     施法实体
     * @param magicData  魔法数据
     * @return 是否满足施法条件
     */
    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData magicData) {
        // 使用铁魔法的工具方法检查目标，范围32格，瞄准辅助0.35f
        return Utils.preCastTargetHelper(level, entity, magicData, this, 32, 0.35f);
    }

    /**
     * 施法逻辑
     * 在目标位置创建一片暴风雪区域，区域内的敌人每隔0.5秒受到冰冻伤害
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
            Vec3 pos = null;

            // 从施法数据中获取目标位置
            if (magicData.getAdditionalCastData() instanceof TargetEntityCastData targetData) {
                pos = targetData.getTargetPosition(serverLevel);
            }

            // 如果没有目标位置（例如通过命令施法），使用施法者位置
            if (pos == null) {
                pos = entity.position();
            }

            // 获取法术强度属性值（修正后的获取方式）
            float spellPower = SpellPowerHelper.getBaseSpellPowerAttribute(entity);

            // 计算范围、持续时间和伤害（带法术强度缩放）
            double range = getRange(spellLevel, spellPower);
            int durationTicks = getDurationTicks(spellLevel, spellPower);
            float damage = getDamage(spellLevel, spellPower);

            // 创建暴风雪区域
            BlizzardManager.createBlizzard(serverLevel, pos, range, durationTicks, damage, entity);

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
    public double getRange(int spellLevel, float spellPower) {
        double baseRange = Config.BLIZZARD_BASE_RANGE.get() + (spellLevel - 1) * Config.BLIZZARD_RANGE_PER_LEVEL.get();
        double spellPowerMultiplier = Config.BLIZZARD_SPELL_POWER_MULTIPLIER.get();
        double spellPowerBonus = (spellPower - 10.0f) * RANGE_PER_SPELL_POWER_PERCENT * spellPowerMultiplier;
        return Math.max(1.0, baseRange + spellPowerBonus);
    }

    /**
     * 获取范围（不带法术强度，用于显示）
     *
     * @param spellLevel 法术等级
     * @return 基础范围（格）
     */
    public double getRange(int spellLevel) {
        return Config.BLIZZARD_BASE_RANGE.get() + (spellLevel - 1) * Config.BLIZZARD_RANGE_PER_LEVEL.get();
    }

    /**
     * 获取持续时间（带法术强度缩放）
     *
     * @param spellLevel 法术等级
     * @param spellPower 法术强度
     * @return 持续时间（tick）
     */
    public int getDurationTicks(int spellLevel, float spellPower) {
        int baseDurationSeconds = Config.BLIZZARD_BASE_DURATION_SECONDS.get() + (spellLevel - 1) * Config.BLIZZARD_DURATION_PER_LEVEL.get();
        int baseDuration = baseDurationSeconds * 20;  // 转换为tick
        double spellPowerMultiplier = Config.BLIZZARD_SPELL_POWER_MULTIPLIER.get();
        return (int) (baseDuration * spellPower / 1.0f * spellPowerMultiplier);
    }

    /**
     * 获取持续时间（不带法术强度，用于显示）
     *
     * @param spellLevel 法术等级
     * @return 基础持续时间（秒）
     */
    public int getDuration(int spellLevel) {
        return Config.BLIZZARD_BASE_DURATION_SECONDS.get() + (spellLevel - 1) * Config.BLIZZARD_DURATION_PER_LEVEL.get();
    }

    /**
     * 获取伤害（带法术强度缩放）
     *
     * @param spellLevel 法术等级
     * @param spellPower 法术强度
     * @return 伤害值
     */
    public float getDamage(int spellLevel, float spellPower) {
        float baseDamage = Config.BLIZZARD_BASE_DAMAGE.get().floatValue() + (spellLevel - 1) * Config.BLIZZARD_DAMAGE_PER_LEVEL.get().floatValue();
        double spellPowerMultiplier = Config.BLIZZARD_SPELL_POWER_MULTIPLIER.get();
        float spellPowerBonus = (spellPower - 10.0f) * DAMAGE_PER_SPELL_POWER_PERCENT * (float) spellPowerMultiplier;
        return Math.max(1.0f, baseDamage + spellPowerBonus);
    }

    /**
     * 获取伤害（不带法术强度，用于显示）
     *
     * @param spellLevel 法术等级
     * @return 基础伤害
     */
    public float getDamage(int spellLevel) {
        return Config.BLIZZARD_BASE_DAMAGE.get().floatValue() + (spellLevel - 1) * Config.BLIZZARD_DAMAGE_PER_LEVEL.get().floatValue();
    }

    /**
     * 获取伤害间隔
     *
     * @return 伤害间隔（tick）
     */
    public int getDamageInterval() {
        return Config.BLIZZARD_DAMAGE_INTERVAL_TICKS.get();
    }

    /**
     * 播放施法效果
     * 
     * @param level  服务器世界
     * @param pos    位置
     * @param range  范围
     */
    private void playCastEffects(ServerLevel level, Vec3 pos, double range) {
        // 播放自定义暴风雪音效
        level.playSound(
                null,
                pos.x,
                pos.y,
                pos.z,
                ModSounds.BLIZZARD_CAST.get(),
                net.minecraft.sounds.SoundSource.PLAYERS,
                1.0f,
                1.0f
        );

        // 播放暴风雪粒子效果
        BlizzardParticles.playCastEffect(level, pos, range);
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
        double actualRange = getRange(level, spellPower);
        int actualDuration = getDurationTicks(level, spellPower) / 20;  // 转换为秒
        float actualDamage = getDamage(level, spellPower);

        // 获取基础数值
        double baseRange = getRange(level);
        int baseDuration = getDuration(level);
        float baseDamage = getDamage(level);

        return List.of(
                Component.translatable("spell.legendarymage.blizzard.range", String.format("%.1f", actualRange), String.format("%.1f", baseRange)),
                Component.translatable("spell.legendarymage.blizzard.duration", actualDuration, baseDuration),
                Component.translatable("spell.legendarymage.blizzard.damage", String.format("%.1f", actualDamage), String.format("%.1f", baseDamage)),
                Component.translatable("spell.legendarymage.blizzard.interval", DAMAGE_INTERVAL_TICKS / 20.0f),
                Component.translatable("spell.legendarymage.blizzard.spell_power", String.format("%.1f", spellPower))
        );
    }
}
