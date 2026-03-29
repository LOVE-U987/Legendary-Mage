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
import io.redspace.ironsspellbooks.damage.SpellDamageSource;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.entity.spell.IceExplosionConeProjectile;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * 冰爆锥法术
 * 铁魔法-冰系
 * 一种被迫注入了过多法力的冰锥，变得极为不稳定，在击中敌人时会触发冰爆
 * 
 * 数据：基础50蓝耗，5秒CD，伤害5点，最低一级，最高10级，从稀有到传说，可书写
 * 每一法术等级+10点蓝耗，+5点伤害，冰爆范围不受加成，发射无视重力
 * 可被法术强度加成，冰爆范围不受加成
 * 
 * @author Love_U
 * @version 1.0.1
 */
public class IceExplosionConeSpell extends AbstractSpell {

    /**
     * 法术ID
     */
    public static final String SPELL_ID = "ice_explosion_cone";

    /**
     * 基础蓝耗
     */
    private static final int BASE_MANA_COST = 50;

    /**
     * 每级蓝耗增量
     */
    private static final int MANA_COST_PER_LEVEL = 10;

    /**
     * 基础冷却时间（tick）
     * 5秒 = 100 tick
     */
    private static final int BASE_COOLDOWN = 100;

    /**
     * 基础伤害
     */
    private static final int BASE_DAMAGE = 5;

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
    private static final int SPELL_POWER_PER_LEVEL = 0;

    /**
     * 最小等级
     */
    private static final int MIN_LEVEL = 1;

    /**
     * 最大等级
     */
    private static final int MAX_LEVEL = 10;

    /**
     * 最小稀有度（稀有）
     */
    private static final int MIN_RARITY = SpellRarity.RARE.getValue();

    /**
     * 最大稀有度（传说）
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
            LegendaryMage.MODID, "textures/gui/spell_icons/ice_explosion_cone.png");

    /**
     * 施法动画 - 使用铁魔法冰系法术的施法动画
     */
    private static final AnimationHolder CAST_ANIMATION = new AnimationHolder(
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "instant_cast"), 
            true);

    /**
     * 施法音效
     */
    private static final Optional<SoundEvent> CAST_SOUND = Optional.of(SoundEvents.SNOWBALL_THROW);

    /**
     * 构造函数
     */
    public IceExplosionConeSpell() {
        this.baseManaCost = BASE_MANA_COST;
        this.manaCostPerLevel = MANA_COST_PER_LEVEL;
        this.castTime = 0; // 瞬发
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
                .setMinRarity(SpellRarity.RARE)
                .setMaxLevel(MAX_LEVEL)
                .setCooldownSeconds(5)
                .setAllowCrafting(true)
                .setSchoolResource(SchoolRegistry.ICE_RESOURCE);
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
        return ICE_COLOR;
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
     * 获取冷却时间
     * 
     * @return 冷却时间（tick）
     */
    @Override
    public int getSpellCooldown() {
        return BASE_COOLDOWN;
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
     * 获取法术图标资源位置
     *
     * @return 法术图标资源位置
     */
    public ResourceLocation getSpellIcon() {
        return SPELL_ICON;
    }

    /**
     * 施法逻辑 - 发射冰爆锥投射物
     *
     * @param level       世界
     * @param spellLevel  法术等级
     * @param entity      施法实体
     * @param castSource  施法来源
     * @param magicData   魔法数据
     */
    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData magicData) {
        // 创建冰爆锥投射物
        IceExplosionConeProjectile projectile = new IceExplosionConeProjectile(level, entity);
        
        // 设置投射物位置（从施法者眼睛高度发射）
        Vec3 spawnPos = entity.position().add(0, entity.getEyeHeight() - projectile.getBoundingBox().getYsize() * 0.5f, 0);
        projectile.setPos(spawnPos);
        
        // 设置投射物方向（沿视线方向）
        projectile.shoot(entity.getLookAngle());
        
        // 获取法术强度属性值
        float spellPower = SpellPowerHelper.getBaseSpellPowerAttribute(entity);
        
        // 计算并设置伤害
        float damage = getDamage(spellLevel, spellPower);
        projectile.setDamage(damage);
        
        // 添加到世界
        level.addFreshEntity(projectile);

        super.onCast(level, spellLevel, entity, castSource, magicData);
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
        return baseDamage * spellPower;
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
     * 获取冰爆范围（固定值，不受加成）
     *
     * @return 冰爆范围（格）
     */
    public float getExplosionRadius() {
        return 3.0f;
    }

    /**
     * 获取伤害来源
     * 
     * @param projectile 投射物
     * @param attacker   攻击者
     * @return 伤害来源
     */
    @Override
    public SpellDamageSource getDamageSource(@Nullable Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker).setFreezeTicks(40);
    }

    /**
     * 获取冰爆锥法术的伤害来源（静态方法，方便投射物使用）
     * 
     * @param projectile 投射物
     * @param attacker   攻击者
     * @return 伤害来源
     */
    public static SpellDamageSource getIceExplosionConeDamageSource(Entity projectile, Entity attacker) {
        IceExplosionConeSpell spell = new IceExplosionConeSpell();
        return spell.getDamageSource(projectile, attacker);
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
        float actualDamage = getDamage(level, spellPower);

        // 获取基础数值
        float baseDamage = getDamage(level);

        return List.of(
                net.minecraft.network.chat.Component.translatable("spell.legendarymage.ice_explosion_cone.damage", String.format("%.1f", actualDamage), String.format("%.1f", baseDamage)),
                net.minecraft.network.chat.Component.translatable("spell.legendarymage.ice_explosion_cone.explosion_radius", String.format("%.1f", getExplosionRadius())),
                net.minecraft.network.chat.Component.translatable("spell.legendarymage.ice_explosion_cone.spell_power", String.format("%.1f", spellPower))
        );
    }
}
