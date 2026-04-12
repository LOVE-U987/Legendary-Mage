package com.legendarymage.legendarymagemod.spell;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.school.ElementSchoolRegistry;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
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

import java.util.List;
import java.util.Optional;

/**
 * 元素棱镜法术
 * 元素流派的传奇法术，创建一片持续20秒的元素棱镜区域
 * 
 * 效果：
 * - 瞬发创建棱镜区域
 * - 持续20秒
 * - 基础范围6格，每级+2格
 * - 每2秒扫描一次，使范围内敌人共享元素标记
 * - 元素反应触发两次
 * - 可被法术强度加成
 * 
 * 数据：
 * - 基础蓝耗125，每级+75
 * - 等级1-5
 * - 传说品质
 * - 可书写
 * 
 * @author Love_U
 * @version 1.0.6
 */
public class ElementalPrismSpell extends AbstractSpell {

    /**
     * 法术ID
     */
    public static final String SPELL_ID = "elemental_prism";

    /**
     * 基础蓝耗
     */
    private static final int BASE_MANA_COST = 125;

    /**
     * 每级蓝耗增量
     */
    private static final int MANA_COST_PER_LEVEL = 75;

    /**
     * 基础范围（格）
     */
    private static final double BASE_RANGE = 6.0;

    /**
     * 每级范围增量（格）
     */
    private static final double RANGE_PER_LEVEL = 2.0;

    /**
     * 持续时间（tick）- 20秒 = 400 tick
     */
    private static final int DURATION_TICKS = 400;

    /**
     * 最小等级
     */
    private static final int MIN_LEVEL = 1;

    /**
     * 最大等级
     */
    private static final int MAX_LEVEL = 5;

    /**
     * 最小稀有度（传说）
     */
    private static final SpellRarity MIN_RARITY = SpellRarity.LEGENDARY;

    /**
     * 法术图标资源位置
     */
    private static final ResourceLocation SPELL_ICON = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, "textures/gui/spell_icons/elemental_prism.png");

    /**
     * 法术资源位置
     */
    private static final ResourceLocation SPELL_RESOURCE = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, SPELL_ID);

    /**
     * 施法动画
     */
    private static final AnimationHolder CAST_ANIMATION = new AnimationHolder(
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "instant_cast"), true);

    /**
     * 施法音效
     */
    private static final Optional<SoundEvent> CAST_SOUND = Optional.of(SoundEvents.BEACON_POWER_SELECT);

    /**
     * 目标选择颜色（紫色）
     */
    private static final Vector3f TARGETING_COLOR = new Vector3f(0.6f, 0.2f, 0.8f);

    /**
     * 构造函数
     */
    public ElementalPrismSpell() {
        this.baseManaCost = BASE_MANA_COST;
        this.manaCostPerLevel = MANA_COST_PER_LEVEL;
        this.castTime = 0; // 瞬发
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 0;
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
                .setMinRarity(MIN_RARITY)
                .setMaxLevel(MAX_LEVEL)
                .setCooldownSeconds(30) // 30秒冷却
                .setAllowCrafting(true);
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
     * @return 元素流派
     */
    @Override
    public SchoolType getSchoolType() {
        return ElementSchoolRegistry.ELEMENT.get();
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
     * 获取施法音效
     * 
     * @return 施法音效
     */
    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return CAST_SOUND;
    }

    /**
     * 获取施法动画
     * 
     * @return 施法动画
     */
    @Override
    public AnimationHolder getCastStartAnimation() {
        return CAST_ANIMATION;
    }

    /**
     * 计算范围
     * 
     * @param spellLevel 法术等级
     * @param spellPower 法术强度
     * @return 最终范围
     */
    private double calculateRange(int spellLevel, float spellPower) {
        double baseRange = BASE_RANGE + (spellLevel - 1) * RANGE_PER_LEVEL;
        // 范围受法术强度加成
        return baseRange * spellPower;
    }

    /**
     * 施法逻辑
     * 瞬发创建元素棱镜区域，持续20秒
     *
     * @param level       世界
     * @param spellLevel  法术等级
     * @param entity      施法实体
     * @param castSource  施法来源
     * @param magicData   魔法数据
     */
    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData magicData) {
        if (!(level instanceof ServerLevel serverLevel)) {
            super.onCast(level, spellLevel, entity, castSource, magicData);
            return;
        }

        // 获取法术强度
        float spellPower = SpellPowerHelper.getBaseSpellPowerAttribute(entity);

        // 计算范围
        double range = calculateRange(spellLevel, spellPower);

        // 获取施法位置
        Vec3 pos = entity.position();

        // 调试输出
        if (com.legendarymage.legendarymagemod.Config.ELEMENTAL_PRISM_DEBUG_OUTPUT.get()) {
            LegendaryMage.LOGGER.info("[元素棱镜] 等级: {} | 范围: {} | 持续时间: {}秒 | 法术强度: {}",
                    spellLevel, String.format("%.1f", range), DURATION_TICKS / 20, String.format("%.2f", spellPower));
        }

        // 创建元素棱镜区域
        ElementalPrismManager.createPrism(serverLevel, pos, range, DURATION_TICKS, entity);

        // 播放施法效果
        playCastEffects(serverLevel, pos, range);

        super.onCast(level, spellLevel, entity, castSource, magicData);
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
                SoundEvents.BEACON_ACTIVATE,
                net.minecraft.sounds.SoundSource.PLAYERS,
                1.0f,
                1.0f
        );

        // 播放彩虹粒子效果
        for (int i = 0; i < 100; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = Math.random() * range;
            double x = pos.x + Math.cos(angle) * radius;
            double z = pos.z + Math.sin(angle) * radius;
            double y = pos.y + Math.random() * 2;

            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.WITCH,
                    x, y, z,
                    1,
                    0, 0.1, 0,
                    0.02
            );
        }

        // 播放圆形冲击波效果
        for (int i = 0; i < 36; i++) {
            double angle = (Math.PI * 2 / 36) * i;
            double x = pos.x + Math.cos(angle) * range;
            double z = pos.z + Math.sin(angle) * range;
            
            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.END_ROD,
                    x, pos.y + 1, z,
                    1,
                    0, 0.1, 0,
                    0.01
            );
        }

        // 中心爆发效果
        level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.FLASH,
                pos.x, pos.y + 1, pos.z,
                1,
                0, 0, 0,
                0
        );
    }

    /**
     * 获取独特信息（显示在法术书中）
     *
     * @param level  法术等级
     * @param entity 实体
     * @return 独特信息列表
     */
    @Override
    public List<MutableComponent> getUniqueInfo(int level, LivingEntity entity) {
        float spellPower = SpellPowerHelper.getBaseSpellPowerAttribute(entity);
        double range = calculateRange(level, spellPower);
        int manaCost = getManaCost(level);

        return List.of(
                Component.translatable("spell.legendarymage.elemental_prism.range", String.format("%.1f", range)),
                Component.translatable("spell.legendarymage.elemental_prism.mana_cost", manaCost),
                Component.translatable("spell.legendarymage.elemental_prism.duration", DURATION_TICKS / 20),
                Component.translatable("spell.legendarymage.elemental_prism.double_reaction")
        );
    }
}
