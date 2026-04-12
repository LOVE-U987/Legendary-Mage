package com.legendarymage.legendarymagemod.spell;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.element.ElementReactionManager;
import com.legendarymage.legendarymagemod.element.ElementType;
import com.legendarymage.legendarymagemod.entity.ModEntities;
import com.legendarymage.legendarymagemod.entity.spell.ElementalOrbProjectile;
import com.legendarymage.legendarymagemod.school.ElementSchoolRegistry;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 元素弹幕法术
 * 元素流派的持续发射法术，轮流发射冰、火、雷三种元素球
 * 
 * 效果：
 * - 冰球：造成范围冰冻效果
 * - 火球：造成范围烈焰BUFF效果
 * - 雷球：造成铁魔法的闪电链效果
 * - 所有伤害均可造成对应的元素标记
 * 
 * 基础数据：
 * - 瞬间释放法术
 * - 发射间隔随卷轴等级缩减
 * - 基础伤害5点，每级+10点
 * - 初始发射3个，每级+1个
 * - 范围固定4格，不受加成
 * - 蓝耗100点
 * - 卷轴等级从稀有开始，共10级
 * 
 * @author Love_U
 * @version 1.0.6
 */
public class ElementalBarrageSpell extends AbstractSpell {

    /**
     * 法术ID
     */
    public static final String SPELL_ID = "elemental_barrage";

    /**
     * 基础蓝耗
     */
    private static final int BASE_MANA_COST = 100;

    /**
     * 基础伤害
     */
    private static final float BASE_DAMAGE = 5.0f;

    /**
     * 每级伤害增量
     */
    private static final float DAMAGE_PER_LEVEL = 10.0f;

    /**
     * 初始弹幕数量
     */
    private static final int BASE_ORB_COUNT = 3;

    /**
     * 每级弹幕数量增量
     */
    private static final int ORB_COUNT_PER_LEVEL = 1;

    /**
     * 固定范围（格）- 不受加成
     */
    private static final int FIXED_RANGE = 4;

    /**
     * 基础发射间隔（tick）
     */
    private static final int BASE_FIRE_INTERVAL = 10;

    /**
     * 每级发射间隔缩减（tick）
     */
    private static final int FIRE_INTERVAL_REDUCTION_PER_LEVEL = 1;

    /**
     * 最小发射间隔（tick）
     */
    private static final int MIN_FIRE_INTERVAL = 3;

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
    private static final SpellRarity MIN_RARITY = SpellRarity.UNCOMMON;

    /**
     * 法术图标资源位置
     */
    private static final ResourceLocation SPELL_ICON = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, "textures/gui/spell_icons/elemental_barrage.png");

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
    private static final Optional<SoundEvent> CAST_SOUND = Optional.of(SoundEvents.AMETHYST_BLOCK_CHIME);

    /**
     * 元素类型计数器（用于轮流发射）
     */
    private static final AtomicInteger elementCounter = new AtomicInteger(0);

    /**
     * 元素球类型枚举
     */
    public enum OrbType {
        ICE("ice", new Vector3f(0.5f, 0.8f, 1.0f), AttributeRegistry.ICE_SPELL_POWER, ElementType.ICE),
        FIRE("fire", new Vector3f(1.0f, 0.3f, 0.0f), AttributeRegistry.FIRE_SPELL_POWER, ElementType.FIRE),
        LIGHTNING("lightning", new Vector3f(0.8f, 0.9f, 1.0f), AttributeRegistry.LIGHTNING_SPELL_POWER, ElementType.LIGHTNING);

        private final String name;
        private final Vector3f color;
        private final Holder<Attribute> powerAttribute;
        private final ElementType elementType;

        OrbType(String name, Vector3f color, Holder<Attribute> powerAttribute, ElementType elementType) {
            this.name = name;
            this.color = color;
            this.powerAttribute = powerAttribute;
            this.elementType = elementType;
        }

        public String getName() {
            return name;
        }

        public Vector3f getColor() {
            return color;
        }

        public Holder<Attribute> getPowerAttribute() {
            return powerAttribute;
        }

        public ElementType getElementType() {
            return elementType;
        }

        /**
         * 获取下一个元素类型（轮流顺序：冰→火→雷→冰）
         */
        public OrbType getNext() {
            return switch (this) {
                case ICE -> FIRE;
                case FIRE -> LIGHTNING;
                case LIGHTNING -> ICE;
            };
        }
    }

    /**
     * 构造函数
     */
    public ElementalBarrageSpell() {
        this.baseManaCost = BASE_MANA_COST;
        this.manaCostPerLevel = 0; // 蓝耗固定
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
                .setCooldownSeconds(0) // 无冷却，蓝耗控制频率
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
        return BASE_MANA_COST;
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
     * 获取当前元素类型（轮流）
     * 
     * @return 当前元素类型
     */
    private OrbType getCurrentOrbType() {
        int index = elementCounter.getAndIncrement() % 3;
        return OrbType.values()[index];
    }

    /**
     * 计算伤害
     * 
     * @param spellLevel 法术等级
     * @param spellPower 法术强度
     * @return 最终伤害
     */
    private float calculateDamage(int spellLevel, float spellPower) {
        float baseDamage = BASE_DAMAGE + (spellLevel - 1) * DAMAGE_PER_LEVEL;
        // 伤害受法术强度加成（法术强度基础值为1.0）
        return baseDamage * spellPower;
    }

    /**
     * 计算弹幕数量
     * 
     * @param spellLevel 法术等级
     * @return 弹幕数量
     */
    private int calculateOrbCount(int spellLevel) {
        return BASE_ORB_COUNT + (spellLevel - 1) * ORB_COUNT_PER_LEVEL;
    }

    /**
     * 计算发射间隔
     * 
     * @param spellLevel 法术等级
     * @return 发射间隔（tick）
     */
    private int calculateFireInterval(int spellLevel) {
        int interval = BASE_FIRE_INTERVAL - (spellLevel - 1) * FIRE_INTERVAL_REDUCTION_PER_LEVEL;
        return Math.max(interval, MIN_FIRE_INTERVAL);
    }

    /**
     * 施法逻辑
     * 瞬间释放，发射多个元素球，轮流为冰、火、雷
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

        // 计算伤害
        float damage = calculateDamage(spellLevel, spellPower);

        // 计算弹幕数量
        int orbCount = calculateOrbCount(spellLevel);

        // 计算发射间隔
        int fireInterval = calculateFireInterval(spellLevel);

        // 获取起始元素类型
        OrbType startOrbType = getCurrentOrbType();

        // 调试输出
        if (com.legendarymage.legendarymagemod.Config.ELEMENTAL_BARRAGE_DEBUG_OUTPUT.get()) {
            LegendaryMage.LOGGER.info("[元素弹幕] 等级: {} | 伤害: {} | 弹幕数: {} | 间隔: {}tick | 起始元素: {}",
                    spellLevel, damage, orbCount, fireInterval, startOrbType.getName());
        }

        // 发射弹幕
        for (int i = 0; i < orbCount; i++) {
            final int index = i;
            final OrbType orbType = getOrbTypeByIndex(startOrbType, i);

            // 计算延迟：基础间隔 + 随机延迟（最多0.5秒 = 10 tick）
            final int baseDelay = index * fireInterval;
            final int randomDelay = (int) (Math.random() * 10); // 0-10 tick 随机延迟
            final int totalDelay = baseDelay + randomDelay;

            // 延迟发射
            level.getServer().execute(() -> {
                serverLevel.getServer().tell(new net.minecraft.server.TickTask(
                        serverLevel.getServer().getTickCount() + totalDelay,
                        () -> {
                            if (entity.isAlive()) {
                                spawnOrb(serverLevel, entity, orbType, damage, spellPower);
                            }
                        }
                ));
            });
        }

        super.onCast(level, spellLevel, entity, castSource, magicData);
    }

    /**
     * 根据索引获取元素类型
     * 
     * @param startType 起始类型
     * @param index     索引
     * @return 元素类型
     */
    private OrbType getOrbTypeByIndex(OrbType startType, int index) {
        OrbType result = startType;
        for (int i = 0; i < index; i++) {
            result = result.getNext();
        }
        return result;
    }

    /**
     * 生成元素球投射物
     * 
     * @param level       服务器世界
     * @param caster      施法者
     * @param orbType     元素类型
     * @param damage      伤害
     * @param spellPower  法术强度
     */
    private void spawnOrb(ServerLevel level, LivingEntity caster, OrbType orbType, float damage, float spellPower) {
        // 计算发射位置（从施法者眼睛位置发出）
        Vec3 eyePos = caster.getEyePosition(1.0f);
        Vec3 lookVec = caster.getLookAngle();

        // 创建投射物
        ElementalOrbProjectile orb = new ElementalOrbProjectile(level, caster);
        orb.setOrbType(orbType);
        orb.setDamage(damage);
        orb.setRange(FIXED_RANGE);
        orb.setSpellPower(spellPower);

        // 设置位置和速度
        orb.setPos(eyePos.x, eyePos.y, eyePos.z);

        // 添加散开效果 - 增大散开角度
        double spread = 0.3; // 增加散开程度
        Vec3 velocity = lookVec.add(
                (Math.random() - 0.5) * spread,
                (Math.random() - 0.5) * spread,
                (Math.random() - 0.5) * spread
        ).normalize().scale(orb.getSpeed());

        orb.setDeltaMovement(velocity);

        // 添加到世界
        level.addFreshEntity(orb);

        // 播放音效
        level.playSound(null, eyePos.x, eyePos.y, eyePos.z,
                SoundEvents.AMETHYST_BLOCK_HIT,
                caster.getSoundSource(),
                0.5f,
                0.8f + (float) Math.random() * 0.4f);
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
        float damage = calculateDamage(level, spellPower);
        int orbCount = calculateOrbCount(level);
        int fireInterval = calculateFireInterval(level);

        // 计算每秒发射数量
        float orbsPerSecond = 20.0f / fireInterval;

        return List.of(
                Component.translatable("spell.legendarymage.elemental_barrage.damage", String.format("%.1f", damage)),
                Component.translatable("spell.legendarymage.elemental_barrage.orb_count", orbCount),
                Component.translatable("spell.legendarymage.elemental_barrage.fire_rate", String.format("%.1f", orbsPerSecond)),
                Component.translatable("spell.legendarymage.elemental_barrage.range", FIXED_RANGE)
        );
    }
}
