package com.legendarymage.legendarymagemod.spell;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.element.ElementReactionManager;
import com.legendarymage.legendarymagemod.element.ElementType;
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
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.particle.SparkParticleOptions;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 元素爆发法术
 * 元素流派的核心法术，按顺序轮流造成火、冰、雷三种元素伤害
 * 每种元素伤害单独计算对应流派的法术强度加成
 * 
 * 特性：
 * - 轮流造成火、冰、雷三种元素伤害
 * - 火元素伤害享受火系法术强度加成
 * - 冰元素伤害享受冰系法术强度加成
 * - 雷元素伤害享受雷系法术强度加成
 * - 根据伤害类型自动挂上对应的元素标记
 * 
 * @author Love_U
 * @version 3.0.0
 */
public class ElementalBurstSpell extends AbstractSpell {

    /**
     * 法术ID
     */
    public static final String SPELL_ID = "elemental_burst";

    /**
     * 基础蓝耗
     */
    private static final int BASE_MANA_COST = 200;

    /**
     * 每级蓝耗增量
     */
    private static final int MANA_COST_PER_LEVEL = 30;

    /**
     * 基础施法时间（tick）
     */
    private static final int BASE_CAST_TIME = 60;

    /**
     * 冷却时间（tick）
     * 15秒 = 300 tick
     */
    private static final int COOLDOWN_TICKS = 300;

    /**
     * 基础范围（格）
     */
    private static final int BASE_RANGE = 8;

    /**
     * 每级范围增量（格）
     */
    private static final int RANGE_PER_LEVEL = 1;

    /**
     * 基础伤害
     */
    private static final float BASE_DAMAGE = 15.0f;

    /**
     * 每级伤害增量
     */
    private static final float DAMAGE_PER_LEVEL = 5.0f;

    /**
     * 最小等级
     */
    private static final int MIN_LEVEL = 1;

    /**
     * 最大等级
     */
    private static final int MAX_LEVEL = 5;

    /**
     * 最小稀有度（史诗）
     */
    private static final int MIN_RARITY = SpellRarity.EPIC.getValue();

    /**
     * 最大稀有度（传奇）
     */
    private static final int MAX_RARITY = SpellRarity.LEGENDARY.getValue();

    /**
     * 法术图标资源位置
     */
    private static final ResourceLocation SPELL_ICON = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, "textures/gui/spell_icons/elemental_burst.png");

    /**
     * 法术资源位置
     */
    private static final ResourceLocation SPELL_RESOURCE = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, SPELL_ID);

    /**
     * 施法开始动画
     */
    private static final AnimationHolder CAST_START_ANIMATION = new AnimationHolder(
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "long_cast"), true);

    /**
     * 施法结束动画
     */
    private static final AnimationHolder CAST_FINISH_ANIMATION = new AnimationHolder(
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "long_cast_finish"), true);

    /**
     * 施法开始音效
     */
    private static final Optional<SoundEvent> CAST_START_SOUND = Optional.of(SoundEvents.AMETHYST_BLOCK_CHIME);

    /**
     * 施法结束音效
     */
    private static final Optional<SoundEvent> CAST_FINISH_SOUND = Optional.of(SoundEvents.AMETHYST_BLOCK_HIT);

    /**
     * 目标选择颜色（紫色）
     */
    private static final Vector3f TARGETING_COLOR = new Vector3f(0.6f, 0.2f, 0.8f);

    /**
     * 元素类型枚举
     */
    public enum ElementType {
        FIRE("fire", new Vector3f(1.0f, 0.3f, 0.0f), AttributeRegistry.FIRE_SPELL_POWER),
        ICE("ice", new Vector3f(0.5f, 0.8f, 1.0f), AttributeRegistry.ICE_SPELL_POWER),
        LIGHTNING("lightning", new Vector3f(0.8f, 0.9f, 1.0f), AttributeRegistry.LIGHTNING_SPELL_POWER);

        private final String name;
        private final Vector3f color;
        private final Holder<Attribute> powerAttribute;

        ElementType(String name, Vector3f color, Holder<Attribute> powerAttribute) {
            this.name = name;
            this.color = color;
            this.powerAttribute = powerAttribute;
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

        /**
         * 获取对应的元素标记类型
         */
        public com.legendarymage.legendarymagemod.element.ElementType getMarkType() {
            return switch (this) {
                case FIRE -> com.legendarymage.legendarymagemod.element.ElementType.FIRE;
                case ICE -> com.legendarymage.legendarymagemod.element.ElementType.ICE;
                case LIGHTNING -> com.legendarymage.legendarymagemod.element.ElementType.LIGHTNING;
            };
        }

        /**
         * 获取下一个元素类型（轮流顺序）
         */
        public ElementType getNext() {
            return switch (this) {
                case FIRE -> ICE;
                case ICE -> LIGHTNING;
                case LIGHTNING -> FIRE;
            };
        }
    }

    /**
     * 当前元素类型的计数器
     * 使用AtomicInteger确保线程安全
     */
    private static final AtomicInteger elementCounter = new AtomicInteger(0);

    /**
     * 构造函数
     */
    public ElementalBurstSpell() {
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
                .setMinRarity(SpellRarity.EPIC)
                .setMaxLevel(MAX_LEVEL)
                .setCooldownSeconds(15)
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
     * 获取当前元素类型（轮流）
     * 
     * @return 当前元素类型
     */
    private ElementType getCurrentElement() {
        int index = elementCounter.getAndIncrement() % 3;
        return ElementType.values()[index];
    }

    /**
     * 施法逻辑
     * 轮流造成火、冰、雷三种元素伤害，每种伤害单独计算对应流派的法术强度加成
     * 根据伤害类型自动挂上对应的元素标记
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

            // 获取当前元素类型（轮流）
            ElementType currentElement = getCurrentElement();

            // 获取基础法术强度属性值（修正后的获取方式）
            float baseSpellPower = SpellPowerHelper.getBaseSpellPowerAttribute(entity);

            // 获取当前元素对应的流派法术强度加成
            float specificElementPower = getSpecificElementPower(entity, currentElement);

            // 计算最终伤害 = 基础伤害 × 基础法术强度加成 × 对应元素流派强度加成
            float baseDamage = BASE_DAMAGE + (spellLevel - 1) * DAMAGE_PER_LEVEL;
            float finalDamage = baseDamage * (baseSpellPower / 10.0f) * (specificElementPower / 10.0f);

            // 计算范围
            int range = getRange(spellLevel, baseSpellPower);

            // 调试输出
            if (com.legendarymage.legendarymagemod.Config.ELEMENTAL_BURST_DEBUG_OUTPUT.get()) {
                String elementName = switch (currentElement) {
                    case FIRE -> "§c火焰§r";
                    case ICE -> "§b冰霜§r";
                    case LIGHTNING -> "§9雷霆§r";
                };
                LegendaryMage.LOGGER.info("[元素爆发] 元素类型: {} | 基础伤害: {} | 基础法术强度: {} | {}系强度: {} | 最终伤害: {} | 范围: {}格",
                        elementName, baseDamage, baseSpellPower,
                        switch (currentElement) {
                            case FIRE -> "火";
                            case ICE -> "冰";
                            case LIGHTNING -> "雷";
                        },
                        specificElementPower, finalDamage, range);
            }

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

            // 对每个目标造成伤害并施加元素标记
            for (LivingEntity target : targets) {
                // 使用魔法伤害源（避免使用未注册的伤害类型）
                DamageSource damageSource = level.damageSources().magic();

                // 造成元素伤害
                target.hurt(damageSource, finalDamage);

                // 直接施加对应的元素标记
                com.legendarymage.legendarymagemod.element.ElementType markType = currentElement.getMarkType();
                ElementReactionManager.onElementDamage(serverLevel, target, entity, markType, finalDamage);

                affectedCount++;
            }

            // 播放施法效果
            playCastEffects(serverLevel, pos, range, currentElement);

            LegendaryMage.LOGGER.debug("元素爆发法术施放：元素类型={}, 伤害={}, 目标数={}", 
                    currentElement.getName(), finalDamage, affectedCount);
        }

        super.onCast(level, spellLevel, entity, castSource, magicData);
    }

    /**
     * 获取特定元素流派的法术强度
     * 
     * @param entity 施法实体
     * @param elementType 元素类型
     * @return 对应元素流派的法术强度
     */
    private float getSpecificElementPower(LivingEntity entity, ElementType elementType) {
        Holder<Attribute> powerAttribute = elementType.getPowerAttribute();
        if (entity.getAttributes().hasAttribute(powerAttribute)) {
            return (float) entity.getAttributeValue(powerAttribute);
        }
        return 10.0f; // 默认值
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
     * 播放施法效果
     * 
     * @param level  服务器世界
     * @param pos    位置
     * @param range  范围
     * @param elementType 当前元素类型
     */
    private void playCastEffects(ServerLevel level, Vec3 pos, int range, ElementType elementType) {
        // 播放音效
        level.playSound(
                null,
                pos.x,
                pos.y,
                pos.z,
                SoundEvents.AMETHYST_BLOCK_HIT,
                net.minecraft.sounds.SoundSource.PLAYERS,
                1.0f,
                0.8f + level.random.nextFloat() * 0.4f
        );

        // 播放粒子效果
        Vector3f color = elementType.getColor();
        
        // 冲击波粒子
        level.sendParticles(
                new BlastwaveParticleOptions(color, range * 0.5f),
                pos.x, pos.y + 0.5, pos.z,
                1, 0, 0, 0, 0
        );

        // 元素粒子
        for (int i = 0; i < 20; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * range;
            double offsetY = level.random.nextDouble() * 2;
            double offsetZ = (level.random.nextDouble() - 0.5) * range;
            
            level.sendParticles(
                    new SparkParticleOptions(color),
                    pos.x + offsetX,
                    pos.y + offsetY,
                    pos.z + offsetZ,
                    1,
                    0.1, 0.1, 0.1,
                    0.1
            );
        }
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
        // 获取基础法术强度属性值（修正后的获取方式）
        float spellPower = SpellPowerHelper.getBaseSpellPowerAttribute(entity);

        // 计算实际数值（带法术强度缩放）
        int actualRange = getRange(level, spellPower);
        float baseDamage = BASE_DAMAGE + (level - 1) * DAMAGE_PER_LEVEL;
        float actualDamage = baseDamage * (spellPower / 1.0f);

        // 获取基础数值
        int baseRange = getRange(level);

        // 获取下一个将要施放的元素类型
        ElementType nextElement = ElementType.values()[elementCounter.get() % 3];

        return List.of(
                Component.translatable("spell.legendarymage.elemental_burst.range", actualRange, baseRange),
                Component.translatable("spell.legendarymage.elemental_burst.damage", String.format("%.1f", actualDamage)),
                Component.translatable("spell.legendarymage.elemental_burst.next_element",
                        Component.translatable("element.legendarymage." + nextElement.getName())),
                Component.translatable("spell.legendarymage.elemental_burst.spell_power", String.format("%.1f", spellPower))
        );
    }

    /**
     * 判断施法者是否可以目标指定实体
     * 检查是否为敌对关系，不会伤害友军和召唤物
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
        
        // 玩家施法时的特殊处理
        if (caster instanceof Player playerCaster) {
            // 不能目标其他玩家（除非在PVP中）
            if (target instanceof Player targetPlayer) {
                // 如果是同一个玩家，不目标
                if (playerCaster == targetPlayer) {
                    return false;
                }
                // 检查是否可以伤害该玩家
                return playerCaster.canHarmPlayer(targetPlayer);
            }
            
            // 检查目标是否是召唤物
            if (target instanceof OwnableEntity ownable) {
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
        if (target instanceof OwnableEntity ownable) {
            // 如果是施法者拥有的召唤物，不目标
            if (ownable.getOwnerUUID() != null && caster.getUUID() != null && 
                ownable.getOwnerUUID().equals(caster.getUUID())) {
                return false;
            }
        }
        
        // 检查是否为同一队伍
        return caster.isAlliedTo(target);
    }
}
