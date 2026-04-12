package com.legendarymage.legendarymagemod.spell;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.entity.ModEntities;
import com.legendarymage.legendarymagemod.entity.spell.ElementalArrowProjectile;
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

import java.util.List;
import java.util.Optional;

/**
 * 三向之矢法术
 * 元素流派的长吟唱法术，完全依次释放冰、火、雷三种元素箭
 * 
 * 效果：
 * - 冰箭：造成冰冻伤害，附加缓慢效果，击中产生4格范围冰冻伤害
 * - 火箭：造成火焰伤害，附加燃烧效果，击中产生4格范围火焰伤害
 * - 雷箭：造成雷电伤害，附加感电效果，击中产生4格范围雷电伤害
 * - 箭矢落在地面可产生持续5秒的元素区域伤害
 * 
 * 数据：
 * - CD：30秒
 * - 基础伤害：15点
 * - 基础箭数：3支（冰、火、雷各1支）
 * - 蓝耗：150点
 * - 吟唱时间：2秒
 * - 持续伤害：基础伤害/5，持续5秒
 * - 等级：1-8级
 * - 稀有度：卓越（EPIC）起可书写
 * 
 * 等级加成：
 * - 每级+10点伤害
 * - 每级+1支箭（最多15支）
 * - 每级+50点蓝耗
 * - 多余法术强度转化为伤害
 * 
 * @author Love_U
 * @version 1.0.6
 */
public class TriDirectionalArrowSpell extends AbstractSpell {

    /**
     * 法术ID
     */
    public static final String SPELL_ID = "tri_directional_arrow";

    /**
     * 基础蓝耗
     */
    private static final int BASE_MANA_COST = 150;

    /**
     * 每级蓝耗增量
     */
    private static final int MANA_COST_PER_LEVEL = 50;

    /**
     * 基础伤害
     */
    private static final float BASE_DAMAGE = 15.0f;

    /**
     * 每级伤害增量
     */
    private static final float DAMAGE_PER_LEVEL = 10.0f;

    /**
     * 基础箭矢数量
     */
    private static final int BASE_ARROW_COUNT = 3;

    /**
     * 每级箭矢数量增量
     */
    private static final int ARROW_COUNT_PER_LEVEL = 1;

    /**
     * 最大箭矢数量
     */
    private static final int MAX_ARROW_COUNT = 15;

    /**
     * 冷却时间（秒）
     */
    private static final int COOLDOWN_SECONDS = 30;

    /**
     * 吟唱时间（tick）
     */
    private static final int CAST_TIME_TICKS = 40; // 2秒

    /**
     * 最小等级
     */
    private static final int MIN_LEVEL = 1;

    /**
     * 最大等级
     */
    private static final int MAX_LEVEL = 8;

    /**
     * 最小稀有度（卓越）
     */
    private static final SpellRarity MIN_RARITY = SpellRarity.EPIC;

    /**
     * 法术图标资源位置
     */
    private static final ResourceLocation SPELL_ICON = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, "textures/gui/spell_icons/tri_directional_arrow.png");

    /**
     * 法术资源位置
     */
    private static final ResourceLocation SPELL_RESOURCE = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, SPELL_ID);

    /**
     * 施法动画
     */
    private static final AnimationHolder CAST_ANIMATION = new AnimationHolder(
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "long_cast"), true);

    /**
     * 施法音效
     */
    private static final Optional<SoundEvent> CAST_SOUND = Optional.of(SoundEvents.AMETHYST_BLOCK_CHIME);



    /**
     * 构造函数
     */
    public TriDirectionalArrowSpell() {
        this.baseManaCost = BASE_MANA_COST;
        this.manaCostPerLevel = MANA_COST_PER_LEVEL;
        this.castTime = CAST_TIME_TICKS;
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
                .setCooldownSeconds(COOLDOWN_SECONDS)
                .setAllowCrafting(true);
    }

    /**
     * 获取施法类型
     * 
     * @return 施法类型（长吟唱）
     */
    @Override
    public CastType getCastType() {
        return CastType.LONG;
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
     * 计算基础伤害
     * 
     * @param spellLevel 法术等级
     * @return 基础伤害
     */
    private float calculateBaseDamage(int spellLevel) {
        return BASE_DAMAGE + (spellLevel - 1) * DAMAGE_PER_LEVEL;
    }

    /**
     * 计算最终伤害（包含法术强度加成和溢出转化）
     * 
     * @param spellLevel 法术等级
     * @param spellPower 法术强度
     * @return 最终伤害
     */
    private float calculateFinalDamage(int spellLevel, float spellPower) {
        float baseDamage = calculateBaseDamage(spellLevel);
        
        // 法术强度基础值为1.0，超过1.0的部分为加成
        float powerBonus = Math.max(0, spellPower - 1.0f);
        
        // 基础伤害 * 法术强度 + 溢出法术强度转化为伤害
        return baseDamage * spellPower + powerBonus * 10.0f;
    }

    /**
     * 计算箭矢数量
     * 
     * @param spellLevel 法术等级
     * @return 箭矢数量
     */
    private int calculateArrowCount(int spellLevel) {
        int count = BASE_ARROW_COUNT + (spellLevel - 1) * ARROW_COUNT_PER_LEVEL;
        return Math.min(count, MAX_ARROW_COUNT);
    }

    /**
     * 获取指定索引的元素类型
     * 依次循环：冰→火→雷→冰→...
     * 
     * @param index 索引
     * @return 元素类型
     */
    private ElementalArrowProjectile.ArrowType getArrowTypeByIndex(int index) {
        int typeIndex = index % 3;
        return switch (typeIndex) {
            case 0 -> ElementalArrowProjectile.ArrowType.ICE;
            case 1 -> ElementalArrowProjectile.ArrowType.FIRE;
            case 2 -> ElementalArrowProjectile.ArrowType.LIGHTNING;
            default -> ElementalArrowProjectile.ArrowType.ICE;
        };
    }

    /**
     * 施法逻辑
     * 长吟唱后依次释放冰、火、雷元素箭
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

        // 计算最终伤害
        float finalDamage = calculateFinalDamage(spellLevel, spellPower);

        // 计算箭矢数量
        int arrowCount = calculateArrowCount(spellLevel);

        // 调试输出
        if (com.legendarymage.legendarymagemod.Config.TRI_DIRECTIONAL_ARROW_DEBUG_OUTPUT.get()) {
            LegendaryMage.LOGGER.info("[三向之矢] 等级: {} | 基础伤害: {} | 最终伤害: {} | 箭数: {} | 法术强度: {}",
                    spellLevel, calculateBaseDamage(spellLevel), finalDamage, arrowCount, spellPower);
        }

        // 计算发射间隔：总时间3秒 / 箭矢数量
        int fireInterval = (int) (60.0f / arrowCount); // 60 tick = 3秒
        if (fireInterval < 2) fireInterval = 2; // 最小间隔2tick，防止过于密集

        // 获取当前tick作为基准
        final int baseTick = serverLevel.getServer().getTickCount();

        // 直接依次发射所有箭矢，使用动态计算的时间间隔
        for (int i = 0; i < arrowCount; i++) {
            final int index = i;
            final ElementalArrowProjectile.ArrowType arrowType = getArrowTypeByIndex(i);
            final int delay = index * fireInterval;

            // 延迟发射，动态时间间隔
            serverLevel.getServer().tell(new net.minecraft.server.TickTask(
                    baseTick + delay,
                    () -> {
                        if (entity.isAlive()) {
                            spawnArrow(serverLevel, entity, arrowType, finalDamage, spellPower, spellLevel);
                        }
                    }
            ));
        }

        super.onCast(level, spellLevel, entity, castSource, magicData);
    }

    /**
     * 生成元素箭投射物
     * 
     * @param level       服务器世界
     * @param caster      施法者
     * @param arrowType   元素类型
     * @param damage      伤害
     * @param spellPower  法术强度
     * @param spellLevel  法术等级
     */
    private void spawnArrow(ServerLevel level, LivingEntity caster, ElementalArrowProjectile.ArrowType arrowType, 
                           float damage, float spellPower, int spellLevel) {
        // 计算发射位置（从施法者眼睛位置发出）
        Vec3 eyePos = caster.getEyePosition(1.0f);
        Vec3 lookVec = caster.getLookAngle();

        // 创建投射物
        ElementalArrowProjectile arrow = new ElementalArrowProjectile(level, caster);
        arrow.setArrowType(arrowType);
        arrow.setBaseDamage(damage);
        arrow.setSpellPower(spellPower);
        arrow.setSpellLevel(spellLevel);

        // 设置位置
        arrow.setPos(eyePos.x, eyePos.y, eyePos.z);

        // 沿视线方向直线发射，无散射
        Vec3 velocity = lookVec.scale(arrow.getSpeed());
        arrow.setDeltaMovement(velocity);

        // 添加到世界
        level.addFreshEntity(arrow);

        // 播放音效
        level.playSound(null, eyePos.x, eyePos.y, eyePos.z,
                SoundEvents.ARROW_SHOOT,
                caster.getSoundSource(),
                0.8f,
                0.9f + (float) Math.random() * 0.2f);
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
        float baseDamage = calculateBaseDamage(level);
        float finalDamage = calculateFinalDamage(level, spellPower);
        int arrowCount = calculateArrowCount(level);
        float groundDamage = finalDamage / 5.0f;

        return List.of(
                Component.translatable("spell.legendarymage.tri_directional_arrow.base_damage", String.format("%.1f", baseDamage)),
                Component.translatable("spell.legendarymage.tri_directional_arrow.final_damage", String.format("%.1f", finalDamage)),
                Component.translatable("spell.legendarymage.tri_directional_arrow.arrow_count", arrowCount),
                Component.translatable("spell.legendarymage.tri_directional_arrow.ground_damage", String.format("%.1f", groundDamage)),
                Component.translatable("spell.legendarymage.tri_directional_arrow.area_radius", 4),
                Component.translatable("spell.legendarymage.tri_directional_arrow.spell_power", String.format("%.2f", spellPower))
        );
    }
}
