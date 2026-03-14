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
import com.legendarymage.legendarymagemod.spell.SpellPowerHelper;

import java.util.List;
import java.util.Optional;

/**
 * 活体冰雕术
 * 释放后创建一片范围，该范围内将自然生成冰雕，冰雕不会自然死亡，且大幅度提升冰雕生命值
 * 到达一定时间后，冰雕破碎发射碎片的同时在每一个冰雕破碎位置生成一个冰雕生物
 * 冰雕生物属于召唤物类型，可为玩家战斗
 * 
 * @author Love_U
 * @version 0.0.1
 */
public class LivingIceSculptureSpell extends AbstractSpell {

    /**
     * 法术ID
     */
    public static final String SPELL_ID = "living_ice_sculpture";

    /**
     * 基础蓝耗
     */
    private static final int BASE_MANA_COST = 250;

    /**
     * 每级蓝耗增量
     */
    private static final int MANA_COST_PER_LEVEL = 50;

    /**
     * 基础施法时间（tick）
     * 3秒 = 60 tick
     */
    private static final int BASE_CAST_TIME = 60;

    /**
     * 冷却时间（tick）
     * 2分钟 = 2400 tick
     */
    private static final int COOLDOWN_TICKS = 2400;

    /**
     * 基础范围（格）
     */
    private static final double BASE_RANGE = 5.0;

    /**
     * 每级范围增量（格）
     */
    private static final double RANGE_PER_LEVEL = 0.5;

    /**
     * 基础有效时间（秒）
     */
    private static final int BASE_DURATION = 60;

    /**
     * 每级有效时间增量（秒）
     */
    private static final int DURATION_PER_LEVEL = 1;

    /**
     * 最小等级
     */
    private static final int MIN_LEVEL = 1;

    /**
     * 最大等级
     */
    private static final int MAX_LEVEL = 8;

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
            LegendaryMage.MODID, "textures/gui/spell_icons/living_ice_sculpture.png");

    /**
     * 法术资源位置
     */
    private static final ResourceLocation SPELL_RESOURCE = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, SPELL_ID);

    /**
     * 施法开始动画 - 使用铁魔法冰系法术的通用施法动画
     */
    private static final AnimationHolder CAST_START_ANIMATION = new AnimationHolder(
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "long_cast"),
            true);

    /**
     * 施法结束动画 - 使用铁魔法冰系法术的施法完成动画
     */
    private static final AnimationHolder CAST_FINISH_ANIMATION = new AnimationHolder(
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "long_cast_finish"),
            true);

    /**
     * 施法开始音效
     */
    private static final Optional<SoundEvent> CAST_START_SOUND = Optional.of(SoundEvents.AMBIENT_UNDERWATER_LOOP);

    /**
     * 施法结束音效
     */
    private static final Optional<SoundEvent> CAST_FINISH_SOUND = Optional.of(SoundEvents.GLASS_BREAK);

    /**
     * 目标选择颜色（冰蓝色）
     */
    private static final Vector3f TARGETING_COLOR = new Vector3f(0.0f, 0.5f, 1.0f);

    /**
     * 构造函数
     */
    public LivingIceSculptureSpell() {
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
                .setCooldownSeconds(120)
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
     * 创建一片范围，该范围内生成冰雕，一段时间后冰雕破碎并生成冰雕生物
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

            // 计算范围和持续时间（带法术强度缩放）
            double range = getRange(spellLevel, spellPower);
            int duration = getDuration(spellLevel, spellPower);

            // 生成冰雕并设置定时破碎生成冰雕生物
            IceSculptureManager.createIceSculptureField(serverLevel, pos, range, duration, spellPower, entity);

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
        double baseRange = BASE_RANGE + (spellLevel - 1) * RANGE_PER_LEVEL;
        return baseRange * spellPower / 1.0f;
    }

    /**
     * 获取范围（不带法术强度，用于显示）
     *
     * @param spellLevel 法术等级
     * @return 基础范围（格）
     */
    public double getRange(int spellLevel) {
        return BASE_RANGE + (spellLevel - 1) * RANGE_PER_LEVEL;
    }

    /**
     * 获取持续时间（带法术强度缩放）
     *
     * @param spellLevel 法术等级
     * @param spellPower 法术强度
     * @return 持续时间（tick）
     */
    public int getDuration(int spellLevel, float spellPower) {
        int baseDuration = (BASE_DURATION + (spellLevel - 1) * DURATION_PER_LEVEL) * 20;  // 转换为tick
        return (int) (baseDuration * spellPower / 1.0f);
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
     * 播放施法效果
     * 
     * @param level  服务器世界
     * @param pos    位置
     * @param range  范围
     */
    private void playCastEffects(ServerLevel level, Vec3 pos, double range) {
        // 播放音效
        level.playSound(
                null,
                pos.x,
                pos.y,
                pos.z,
                SoundEvents.GLASS_PLACE,
                net.minecraft.sounds.SoundSource.PLAYERS,
                1.0f,
                0.8f + level.random.nextFloat() * 0.4f
        );

        // 使用粒子系统
        IceSculptureParticles.playCastEffect(level, pos, range);
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
        int actualDuration = getDuration(level, spellPower) / 20;  // 转换为秒

        // 获取基础数值
        double baseRange = getRange(level);
        int baseDuration = getDuration(level);

        return List.of(
                Component.translatable("spell.legendarymage.living_ice_sculpture.range", String.format("%.1f", actualRange), String.format("%.1f", baseRange)),
                Component.translatable("spell.legendarymage.living_ice_sculpture.duration", actualDuration, baseDuration),
                Component.translatable("spell.legendarymage.living_ice_sculpture.spell_power", String.format("%.1f", spellPower))
        );
    }
}
