package com.legendarymage.legendarymagemod;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Arrays;
import java.util.List;

/**
 * 传奇法师模组配置类
 * 包含复苏符文法术和纵火狂法术的相关配置
 */
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // ==================== 复苏符文法术配置 ====================
    public static final ModConfigSpec.DoubleValue RESURRECTION_RUNE_SPELL_POWER_MULTIPLIER;
    public static final ModConfigSpec.BooleanValue RESURRECTION_RUNE_BUFF_ENABLED;
    public static final ModConfigSpec.IntValue RESURRECTION_RUNE_BUFF_INTERVAL;
    public static final ModConfigSpec.DoubleValue RESURRECTION_RUNE_BUFF_MULTIPLIER;

    // ==================== 纵火狂法术配置 ====================
    public static final ModConfigSpec.BooleanValue PYROMANIAC_AFFECT_ALLIES;
    public static final ModConfigSpec.BooleanValue PYROMANIAC_AFFECT_SUMMONS;

    // ==================== 聚爆法术配置 ====================
    public static final ModConfigSpec.DoubleValue IMPLOSION_SPELL_POWER_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue IMPLOSION_PULL_STRENGTH;
    public static final ModConfigSpec.IntValue IMPLOSION_EXPLOSION_DELAY_TICKS;
    public static final ModConfigSpec.IntValue IMPLOSION_FIRE_DURATION_SECONDS;

    // ==================== 活体冰雕术配置 ====================
    public static final ModConfigSpec.IntValue LIVING_ICE_SCULPTURE_SPAWN_INTERVAL;
    public static final ModConfigSpec.IntValue LIVING_ICE_SCULPTURE_MAX_SCULPTURES;
    public static final ModConfigSpec.IntValue LIVING_ICE_SCULPTURE_LIFETIME_TICKS;
    public static final ModConfigSpec.DoubleValue LIVING_ICE_SCULPTURE_HEALTH_BASE;
    public static final ModConfigSpec.DoubleValue LIVING_ICE_SCULPTURE_HEALTH_PER_SPELL_POWER;
    public static final ModConfigSpec.DoubleValue LIVING_ICE_SCULPTURE_DAMAGE_BASE;
    public static final ModConfigSpec.DoubleValue LIVING_ICE_SCULPTURE_DAMAGE_PER_SPELL_POWER;
    public static final ModConfigSpec.DoubleValue LIVING_ICE_SCULPTURE_SHATTER_DAMAGE_BASE;
    public static final ModConfigSpec.DoubleValue LIVING_ICE_SCULPTURE_SHATTER_DAMAGE_PER_SPELL_POWER;
    public static final ModConfigSpec.IntValue LIVING_ICE_SCULPTURE_ENTITY_LIFETIME_TICKS;

    // ==================== 暴风雪法术配置 ====================
    public static final ModConfigSpec.DoubleValue BLIZZARD_SPELL_POWER_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue BLIZZARD_BASE_RANGE;
    public static final ModConfigSpec.DoubleValue BLIZZARD_RANGE_PER_LEVEL;
    public static final ModConfigSpec.DoubleValue BLIZZARD_BASE_DAMAGE;
    public static final ModConfigSpec.DoubleValue BLIZZARD_DAMAGE_PER_LEVEL;
    public static final ModConfigSpec.IntValue BLIZZARD_BASE_DURATION_SECONDS;
    public static final ModConfigSpec.IntValue BLIZZARD_DURATION_PER_LEVEL;
    public static final ModConfigSpec.IntValue BLIZZARD_DAMAGE_INTERVAL_TICKS;
    public static final ModConfigSpec.BooleanValue BLIZZARD_AFFECT_ALLIES;
    public static final ModConfigSpec.BooleanValue BLIZZARD_AFFECT_SUMMONS;
    public static final ModConfigSpec.IntValue BLIZZARD_SLOWNESS_DURATION_TICKS;
    public static final ModConfigSpec.IntValue BLIZZARD_SLOWNESS_AMPLIFIER;

    // ==================== 全局调试模式配置 ====================
    public static final ModConfigSpec.BooleanValue GLOBAL_DEBUG_MODE;

    // ==================== 拖尾系统调试配置 ====================
    public static final ModConfigSpec.BooleanValue TRAIL_SYSTEM_DEBUG_OUTPUT;

    // ==================== 回响打击调试配置 ====================
    public static final ModConfigSpec.BooleanValue ECHO_STRIKE_DEBUG_OUTPUT;

    // ==================== 元素标记调试配置 ====================
    public static final ModConfigSpec.BooleanValue ELEMENTAL_BURST_DEBUG_OUTPUT;

    // ==================== 元素反应调试配置 ====================
    public static final ModConfigSpec.BooleanValue ELEMENT_REACTION_DEBUG_OUTPUT;

    // ==================== 图标法术调试配置 ====================
    public static final ModConfigSpec.BooleanValue ELEMENTAL_BARRAGE_DEBUG_OUTPUT;
    public static final ModConfigSpec.BooleanValue TRI_DIRECTIONAL_ARROW_DEBUG_OUTPUT;
    public static final ModConfigSpec.BooleanValue ELEMENTAL_PRISM_DEBUG_OUTPUT;

    // ==================== 魔法散弹法术配置 ====================
    public static final ModConfigSpec.BooleanValue MAGIC_SHOTGUN_ENABLED;
    public static final ModConfigSpec.IntValue MAGIC_SHOTGUN_MANA_COST;
    public static final ModConfigSpec.IntValue MAGIC_SHOTGUN_COOLDOWN;
    public static final ModConfigSpec.DoubleValue MAGIC_SHOTGUN_BASE_DAMAGE;
    public static final ModConfigSpec.DoubleValue MAGIC_SHOTGUN_DAMAGE_PER_LEVEL;
    public static final ModConfigSpec.DoubleValue MAGIC_SHOTGUN_EFFECT_RADIUS;
    public static final ModConfigSpec.IntValue MAGIC_SHOTGUN_MANA_COST_PER_SHOT;
    public static final ModConfigSpec.DoubleValue MAGIC_SHOTGUN_MAX_MANA_REDUCTION;

    // ==================== 冰爆锥法术配置 ====================
    public static final ModConfigSpec.BooleanValue ICE_EXPLOSION_CONE_ENABLED;
    public static final ModConfigSpec.IntValue ICE_EXPLOSION_CONE_MANA_COST;
    public static final ModConfigSpec.IntValue ICE_EXPLOSION_CONE_COOLDOWN;
    public static final ModConfigSpec.DoubleValue ICE_EXPLOSION_CONE_BASE_DAMAGE;
    public static final ModConfigSpec.DoubleValue ICE_EXPLOSION_CONE_DAMAGE_PER_LEVEL;
    public static final ModConfigSpec.DoubleValue ICE_EXPLOSION_CONE_EXPLOSION_RADIUS;
    public static final ModConfigSpec.DoubleValue ICE_EXPLOSION_CONE_PROJECTILE_SPEED;

    // ==================== 聚能冰锥法术配置 ====================
    public static final ModConfigSpec.BooleanValue FOCUSED_ICE_CONE_ENABLED;
    public static final ModConfigSpec.IntValue FOCUSED_ICE_CONE_MANA_COST;
    public static final ModConfigSpec.IntValue FOCUSED_ICE_CONE_COOLDOWN;
    public static final ModConfigSpec.DoubleValue FOCUSED_ICE_CONE_BASE_DAMAGE;
    public static final ModConfigSpec.DoubleValue FOCUSED_ICE_CONE_DAMAGE_PER_LEVEL;
    public static final ModConfigSpec.IntValue FOCUSED_ICE_CONE_MAX_PIERCE;
    public static final ModConfigSpec.DoubleValue FOCUSED_ICE_CONE_PIERCE_DAMAGE_REDUCTION;
    public static final ModConfigSpec.DoubleValue FOCUSED_ICE_CONE_EXPLOSION_RADIUS;
    public static final ModConfigSpec.DoubleValue FOCUSED_ICE_CONE_EXPLOSION_DAMAGE;

    // ==================== 巨雪球法术配置 ====================
    public static final ModConfigSpec.BooleanValue GIANT_SNOWBALL_ENABLED;
    public static final ModConfigSpec.IntValue GIANT_SNOWBALL_MANA_COST;
    public static final ModConfigSpec.IntValue GIANT_SNOWBALL_COOLDOWN;
    public static final ModConfigSpec.DoubleValue GIANT_SNOWBALL_BASE_DAMAGE;
    public static final ModConfigSpec.DoubleValue GIANT_SNOWBALL_DAMAGE_PER_LEVEL;
    public static final ModConfigSpec.IntValue GIANT_SNOWBALL_GROWTH_TIME_TICKS;
    public static final ModConfigSpec.DoubleValue GIANT_SNOWBALL_MAX_SIZE_MULTIPLIER;
    public static final ModConfigSpec.IntValue GIANT_SNOWBALL_BLIZZARD_DURATION;
    public static final ModConfigSpec.DoubleValue GIANT_SNOWBALL_BLIZZARD_RADIUS;

    // ==================== 元素标记头顶显示配置 ====================
    public static final ModConfigSpec.BooleanValue ELEMENT_MARK_ICON_ENABLED;
    public static final ModConfigSpec.DoubleValue ELEMENT_MARK_ICON_HEIGHT;
    public static final ModConfigSpec.DoubleValue ELEMENT_MARK_ICON_SCALE;

    // ==================== BUFF 配置 ====================
    // 避雷针 Buff
    public static final ModConfigSpec.DoubleValue LIGHTNING_ROD_ICE_RESIST_REDUCTION;
    public static final ModConfigSpec.DoubleValue LIGHTNING_ROD_LIGHTNING_RESIST_REDUCTION;
    public static final ModConfigSpec.IntValue LIGHTNING_ROD_DURATION_SECONDS;
    public static final ModConfigSpec.IntValue LIGHTNING_ROD_MAX_STACKS;

    // 暗夜无光 Buff
    public static final ModConfigSpec.DoubleValue DARKNESS_BLOOD_RESIST_REDUCTION;

    // 混沌 Buff
    public static final ModConfigSpec.DoubleValue CHAOS_SPELL_POWER_BONUS_PER_LEVEL;

    // 瘟疫 Buff
    public static final ModConfigSpec.DoubleValue PLAGUE_MAX_HEALTH_REDUCTION_PER_LEVEL;
    public static final ModConfigSpec.DoubleValue PLAGUE_ZOMBIE_CONVERSION_CHANCE;
    public static final ModConfigSpec.DoubleValue PLAGUE_EXPLOSION_CHANCE;
    public static final ModConfigSpec.IntValue PLAGUE_DURATION_SECONDS;
    public static final ModConfigSpec.IntValue PLAGUE_MAX_STACKS;

    // 感电 Buff
    public static final ModConfigSpec.DoubleValue ELECTROCUTED_BASE_DAMAGE;
    public static final ModConfigSpec.IntValue ELECTROCUTED_TRIGGER_INTERVAL;
    public static final ModConfigSpec.DoubleValue ELECTROCUTED_CHAIN_RANGE;
    public static final ModConfigSpec.IntValue ELECTROCUTED_MAX_CHAIN_TARGETS;

    // 终末回响 Buff
    public static final ModConfigSpec.DoubleValue ENDER_ECHO_SPELL_POWER_RATIO;
    public static final ModConfigSpec.DoubleValue ENDER_ECHO_SPELL_RESIST_RATIO;

    // 魔法散弹 Buff
    public static final ModConfigSpec.DoubleValue MAGIC_SHOTGUN_BUFF_SPELL_POWER_REDUCTION_PER_LEVEL;
    public static final ModConfigSpec.DoubleValue MAGIC_SHOTGUN_BUFF_CAST_TIME_REDUCTION_PER_LEVEL;
    public static final ModConfigSpec.DoubleValue MAGIC_SHOTGUN_BUFF_MELEE_DAMAGE_PER_LEVEL;
    public static final ModConfigSpec.IntValue MAGIC_SHOTGUN_BUFF_MAX_LEVEL;

    // 溶甲 Buff
    public static final ModConfigSpec.DoubleValue ARMOR_REDUCTION_PER_LEVEL;

    // 烈焰 Buff
    public static final ModConfigSpec.DoubleValue PYRO_FLAME_DAMAGE_PER_SECOND;
    public static final ModConfigSpec.DoubleValue PYRO_FLAME_MAX_HEALTH_REDUCTION_PER_LEVEL;
    public static final ModConfigSpec.IntValue PYRO_FLAME_DAMAGE_INTERVAL;
    public static final ModConfigSpec.DoubleValue PYRO_FLAME_EXPLOSION_BASE_POWER;
    public static final ModConfigSpec.DoubleValue PYRO_FLAME_EXPLOSION_POWER_PER_LEVEL;
    public static final ModConfigSpec.BooleanValue PYRO_FLAME_EXPLOSION_DESTROY_BLOCKS;
    public static final ModConfigSpec.BooleanValue PYRO_FLAME_EXPLOSION_CAUSE_FIRE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> PYRO_FLAME_ENTITY_BLACKLIST;

    static {
        // ==================== 法术配置 ====================
        BUILDER.push("spells");

        // 复苏符文配置
        BUILDER.push("resurrectionRune");
        
        RESURRECTION_RUNE_SPELL_POWER_MULTIPLIER = BUILDER
                .comment("Spell power multiplier for Resurrection Rune spell. Higher values make spell power more effective.",
                        "Default: 1.0 (100% effectiveness)",
                        "Range: 0.1 - 5.0")
                .defineInRange("spellPowerMultiplier", 1.0, 0.1, 5.0);

        RESURRECTION_RUNE_BUFF_ENABLED = BUILDER
                .comment("Whether to enable buff effects for players and summons within the resurrection rune area.",
                        "Default: true")
                .define("buffEnabled", true);

        RESURRECTION_RUNE_BUFF_INTERVAL = BUILDER
                .comment("Interval in ticks between buff applications.",
                        "Default: 40 ticks (2 seconds)",
                        "Range: 20 - 200")
                .defineInRange("buffInterval", 40, 20, 200);

        RESURRECTION_RUNE_BUFF_MULTIPLIER = BUILDER
                .comment("Buff strength multiplier. Higher values make buffs stronger.",
                        "Default: 1.0 (100% effectiveness)",
                        "Range: 0.1 - 3.0")
                .defineInRange("buffMultiplier", 1.0, 0.1, 3.0);
        
        BUILDER.pop();

        // 纵火狂配置
        BUILDER.push("pyromaniac");

        PYROMANIAC_AFFECT_ALLIES = BUILDER
                .comment("Whether Pyromaniac spell can affect allies and summons.",
                        "Default: false (will not affect allies)")
                .define("affectAllies", false);

        PYROMANIAC_AFFECT_SUMMONS = BUILDER
                .comment("Whether Pyromaniac spell can affect summons.",
                        "Default: false (will not affect summons)")
                .define("affectSummons", false);

        BUILDER.pop();

        // 聚爆配置
        BUILDER.push("implosion");
        
        IMPLOSION_SPELL_POWER_MULTIPLIER = BUILDER
                .comment("Spell power multiplier for Implosion spell. Higher values make spell power more effective.",
                        "Default: 1.0 (100% effectiveness)",
                        "Range: 0.1 - 5.0")
                .defineInRange("spellPowerMultiplier", 1.0, 0.1, 5.0);

        IMPLOSION_PULL_STRENGTH = BUILDER
                .comment("Pull strength for Implosion spell. Higher values pull enemies faster.",
                        "Default: 0.8",
                        "Range: 0.1 - 2.0")
                .defineInRange("pullStrength", 0.8, 0.1, 2.0);

        IMPLOSION_EXPLOSION_DELAY_TICKS = BUILDER
                .comment("Delay in ticks between pull and explosion for Implosion spell.",
                        "Default: 10 ticks (0.5 seconds)",
                        "Range: 0 - 60")
                .defineInRange("explosionDelayTicks", 10, 0, 60);

        IMPLOSION_FIRE_DURATION_SECONDS = BUILDER
                .comment("Duration in seconds that targets are set on fire after explosion.",
                        "Default: 5 seconds",
                        "Range: 0 - 30")
                .defineInRange("fireDurationSeconds", 5, 0, 30);
        
        BUILDER.pop();

        // 活体冰雕术配置
        BUILDER.push("livingIceSculpture");
        
        LIVING_ICE_SCULPTURE_SPAWN_INTERVAL = BUILDER
                .comment("Interval in ticks between spawning new ice sculptures.",
                        "Default: 40 ticks (2 seconds)",
                        "Range: 20 - 200")
                .defineInRange("spawnInterval", 40, 20, 200);

        LIVING_ICE_SCULPTURE_MAX_SCULPTURES = BUILDER
                .comment("Maximum number of ice sculptures that can exist simultaneously per spell cast.",
                        "Default: 8 sculptures",
                        "Range: 1 - 20")
                .defineInRange("maxSculptures", 8, 1, 20);

        LIVING_ICE_SCULPTURE_LIFETIME_TICKS = BUILDER
                .comment("Lifetime of ice sculpture in ticks before converting to living sculpture.",
                        "Default: 400 ticks (20 seconds)",
                        "Range: 100 - 1200")
                .defineInRange("lifetimeTicks", 400, 100, 1200);

        LIVING_ICE_SCULPTURE_HEALTH_BASE = BUILDER
                .comment("Base health for ice sculptures.",
                        "Default: 50.0",
                        "Range: 10.0 - 200.0")
                .defineInRange("healthBase", 50.0, 10.0, 200.0);

        LIVING_ICE_SCULPTURE_HEALTH_PER_SPELL_POWER = BUILDER
                .comment("Additional health per spell power point.",
                        "Default: 5.0",
                        "Range: 0.0 - 20.0")
                .defineInRange("healthPerSpellPower", 5.0, 0.0, 20.0);

        LIVING_ICE_SCULPTURE_DAMAGE_BASE = BUILDER
                .comment("Base attack damage for living ice sculptures.",
                        "Default: 10.0",
                        "Range: 1.0 - 50.0")
                .defineInRange("damageBase", 10.0, 1.0, 50.0);

        LIVING_ICE_SCULPTURE_DAMAGE_PER_SPELL_POWER = BUILDER
                .comment("Additional attack damage per spell power point.",
                        "Default: 1.0",
                        "Range: 0.0 - 5.0")
                .defineInRange("damagePerSpellPower", 1.0, 0.0, 5.0);

        LIVING_ICE_SCULPTURE_SHATTER_DAMAGE_BASE = BUILDER
                .comment("Base shatter damage when ice sculpture is destroyed.",
                        "Default: 10.0",
                        "Range: 0.0 - 50.0")
                .defineInRange("shatterDamageBase", 10.0, 0.0, 50.0);

        LIVING_ICE_SCULPTURE_SHATTER_DAMAGE_PER_SPELL_POWER = BUILDER
                .comment("Additional shatter damage per spell power point.",
                        "Default: 1.0",
                        "Range: 0.0 - 5.0")
                .defineInRange("shatterDamagePerSpellPower", 1.0, 0.0, 5.0);

        LIVING_ICE_SCULPTURE_ENTITY_LIFETIME_TICKS = BUILDER
                .comment("Lifetime of living ice sculpture entity in ticks before naturally shattering.",
                        "Default: 3600 ticks (3 minutes)",
                        "Range: 600 - 7200")
                .defineInRange("entityLifetimeTicks", 3600, 600, 7200);
        
        BUILDER.pop();

        // 暴风雪配置
        BUILDER.push("blizzard");
        
        BLIZZARD_SPELL_POWER_MULTIPLIER = BUILDER
                .comment("Spell power multiplier for Blizzard spell. Higher values make spell power more effective.",
                        "Default: 1.0 (100% effectiveness)",
                        "Range: 0.1 - 5.0")
                .defineInRange("spellPowerMultiplier", 1.0, 0.1, 5.0);

        BLIZZARD_BASE_RANGE = BUILDER
                .comment("Base range of the blizzard area in blocks.",
                        "Default: 8.0 blocks",
                        "Range: 1.0 - 20.0")
                .defineInRange("baseRange", 8.0, 1.0, 20.0);

        BLIZZARD_RANGE_PER_LEVEL = BUILDER
                .comment("Additional range per spell level in blocks.",
                        "Default: 1.0 block per level",
                        "Range: 0.0 - 5.0")
                .defineInRange("rangePerLevel", 1.0, 0.0, 5.0);

        BLIZZARD_BASE_DAMAGE = BUILDER
                .comment("Base damage dealt by the blizzard per hit.",
                        "Default: 5.0 damage",
                        "Range: 1.0 - 50.0")
                .defineInRange("baseDamage", 5.0, 1.0, 50.0);

        BLIZZARD_DAMAGE_PER_LEVEL = BUILDER
                .comment("Additional damage per spell level.",
                        "Default: 5.0 damage per level",
                        "Range: 0.0 - 20.0")
                .defineInRange("damagePerLevel", 5.0, 0.0, 20.0);

        BLIZZARD_BASE_DURATION_SECONDS = BUILDER
                .comment("Base duration of the blizzard in seconds.",
                        "Default: 20 seconds",
                        "Range: 5 - 60")
                .defineInRange("baseDurationSeconds", 20, 5, 60);

        BLIZZARD_DURATION_PER_LEVEL = BUILDER
                .comment("Additional duration per spell level in seconds.",
                        "Default: 2 seconds per level",
                        "Range: 0 - 10")
                .defineInRange("durationPerLevel", 2, 0, 10);

        BLIZZARD_DAMAGE_INTERVAL_TICKS = BUILDER
                .comment("Interval between damage ticks in ticks.",
                        "Default: 10 ticks (0.5 seconds)",
                        "Range: 5 - 100")
                .defineInRange("damageIntervalTicks", 10, 5, 100);

        BLIZZARD_AFFECT_ALLIES = BUILDER
                .comment("Whether Blizzard spell can affect allies and summons.",
                        "Default: false (will not affect allies)")
                .define("affectAllies", false);

        BLIZZARD_AFFECT_SUMMONS = BUILDER
                .comment("Whether Blizzard spell can affect summons.",
                        "Default: false (will not affect summons)")
                .define("affectSummons", false);

        BLIZZARD_SLOWNESS_DURATION_TICKS = BUILDER
                .comment("Duration of slowness effect applied to targets in ticks.",
                        "Default: 40 ticks (2 seconds)",
                        "Range: 20 - 200")
                .defineInRange("slownessDurationTicks", 40, 20, 200);

        BLIZZARD_SLOWNESS_AMPLIFIER = BUILDER
                .comment("Amplifier (level) of slowness effect. 0 = level 1, 1 = level 2, etc.",
                        "Default: 1 (level 2 slowness)",
                        "Range: 0 - 4")
                .defineInRange("slownessAmplifier", 1, 0, 4);
        
        BUILDER.pop();

        // 魔法散弹配置
        BUILDER.push("magicShotgun");
        
        MAGIC_SHOTGUN_ENABLED = BUILDER
                .comment("Enable Magic Shotgun spell.",
                        "Default: true")
                .define("enabled", true);
        
        MAGIC_SHOTGUN_MANA_COST = BUILDER
                .comment("Mana cost for Magic Shotgun spell.",
                        "Default: 40")
                .defineInRange("mana_cost", 40, 10, 200);
        
        MAGIC_SHOTGUN_COOLDOWN = BUILDER
                .comment("Cooldown in seconds for Magic Shotgun spell.",
                        "Default: 15")
                .defineInRange("cooldown", 15, 0, 120);
        
        MAGIC_SHOTGUN_BASE_DAMAGE = BUILDER
                .comment("Base damage for Magic Shotgun spell.",
                        "Default: 15.0")
                .defineInRange("base_damage", 15.0, 1.0, 100.0);
        
        MAGIC_SHOTGUN_DAMAGE_PER_LEVEL = BUILDER
                .comment("Additional damage per spell level.",
                        "Default: 5.0")
                .defineInRange("damage_per_level", 5.0, 0.0, 20.0);
        
        MAGIC_SHOTGUN_EFFECT_RADIUS = BUILDER
                .comment("Effect radius in blocks.",
                        "Default: 3.0")
                .defineInRange("effect_radius", 3.0, 1.0, 10.0);
        
        MAGIC_SHOTGUN_MANA_COST_PER_SHOT = BUILDER
                .comment("Mana cost per shot.",
                        "Default: 10")
                .defineInRange("mana_cost_per_shot", 10, 0, 50);
        
        MAGIC_SHOTGUN_MAX_MANA_REDUCTION = BUILDER
                .comment("Maximum mana reduction percentage.",
                        "Default: -0.30 (-30%)",
                        "Range: -0.9 to 0.0")
                .defineInRange("max_mana_reduction", -0.30, -0.9, 0.0);
        
        BUILDER.pop();

        // 冰爆锥配置
        BUILDER.push("iceExplosionCone");
        
        ICE_EXPLOSION_CONE_ENABLED = BUILDER
                .comment("Enable Ice Explosion Cone spell.",
                        "Default: true")
                .define("enabled", true);
        
        ICE_EXPLOSION_CONE_MANA_COST = BUILDER
                .comment("Mana cost for Ice Explosion Cone spell.",
                        "Default: 50")
                .defineInRange("mana_cost", 50, 10, 200);
        
        ICE_EXPLOSION_CONE_COOLDOWN = BUILDER
                .comment("Cooldown in seconds.",
                        "Default: 20")
                .defineInRange("cooldown", 20, 0, 120);
        
        ICE_EXPLOSION_CONE_BASE_DAMAGE = BUILDER
                .comment("Base damage.",
                        "Default: 12.0")
                .defineInRange("base_damage", 12.0, 1.0, 100.0);
        
        ICE_EXPLOSION_CONE_DAMAGE_PER_LEVEL = BUILDER
                .comment("Damage per level.",
                        "Default: 4.0")
                .defineInRange("damage_per_level", 4.0, 0.0, 20.0);
        
        ICE_EXPLOSION_CONE_EXPLOSION_RADIUS = BUILDER
                .comment("Explosion radius.",
                        "Default: 3.0")
                .defineInRange("explosion_radius", 3.0, 1.0, 10.0);
        
        ICE_EXPLOSION_CONE_PROJECTILE_SPEED = BUILDER
                .comment("Projectile speed.",
                        "Default: 1.2")
                .defineInRange("projectile_speed", 1.2, 0.5, 5.0);
        
        BUILDER.pop();

        // 聚能冰锥配置
        BUILDER.push("focusedIceCone");
        
        FOCUSED_ICE_CONE_ENABLED = BUILDER
                .comment("Enable Focused Ice Cone spell.",
                        "Default: true")
                .define("enabled", true);
        
        FOCUSED_ICE_CONE_MANA_COST = BUILDER
                .comment("Mana cost.",
                        "Default: 70")
                .defineInRange("mana_cost", 70, 10, 200);
        
        FOCUSED_ICE_CONE_COOLDOWN = BUILDER
                .comment("Cooldown in seconds.",
                        "Default: 25")
                .defineInRange("cooldown", 25, 0, 120);
        
        FOCUSED_ICE_CONE_BASE_DAMAGE = BUILDER
                .comment("Base damage.",
                        "Default: 18.0")
                .defineInRange("base_damage", 18.0, 1.0, 100.0);
        
        FOCUSED_ICE_CONE_DAMAGE_PER_LEVEL = BUILDER
                .comment("Damage per level.",
                        "Default: 6.0")
                .defineInRange("damage_per_level", 6.0, 0.0, 20.0);
        
        FOCUSED_ICE_CONE_MAX_PIERCE = BUILDER
                .comment("Maximum pierce count.",
                        "Default: 3")
                .defineInRange("max_pierce", 3, 0, 10);
        
        FOCUSED_ICE_CONE_PIERCE_DAMAGE_REDUCTION = BUILDER
                .comment("Damage reduction per pierce.",
                        "Default: 0.2")
                .defineInRange("pierce_damage_reduction", 0.2, 0.0, 0.9);
        
        FOCUSED_ICE_CONE_EXPLOSION_RADIUS = BUILDER
                .comment("Explosion radius.",
                        "Default: 4.0")
                .defineInRange("explosion_radius", 4.0, 1.0, 10.0);
        
        FOCUSED_ICE_CONE_EXPLOSION_DAMAGE = BUILDER
                .comment("Explosion damage.",
                        "Default: 25.0")
                .defineInRange("explosion_damage", 25.0, 0.0, 100.0);
        
        BUILDER.pop();

        // 巨雪球配置
        BUILDER.push("giantSnowball");
        
        GIANT_SNOWBALL_ENABLED = BUILDER
                .comment("Enable Giant Snowball spell.",
                        "Default: true")
                .define("enabled", true);
        
        GIANT_SNOWBALL_MANA_COST = BUILDER
                .comment("Mana cost.",
                        "Default: 80")
                .defineInRange("mana_cost", 80, 10, 200);
        
        GIANT_SNOWBALL_COOLDOWN = BUILDER
                .comment("Cooldown in seconds.",
                        "Default: 30")
                .defineInRange("cooldown", 30, 0, 120);
        
        GIANT_SNOWBALL_BASE_DAMAGE = BUILDER
                .comment("Base damage.",
                        "Default: 15.0")
                .defineInRange("base_damage", 15.0, 1.0, 100.0);
        
        GIANT_SNOWBALL_DAMAGE_PER_LEVEL = BUILDER
                .comment("Damage per level.",
                        "Default: 5.0")
                .defineInRange("damage_per_level", 5.0, 0.0, 20.0);
        
        GIANT_SNOWBALL_GROWTH_TIME_TICKS = BUILDER
                .comment("Growth time in ticks.",
                        "Default: 40")
                .defineInRange("growth_time_ticks", 40, 10, 200);
        
        GIANT_SNOWBALL_MAX_SIZE_MULTIPLIER = BUILDER
                .comment("Maximum size multiplier.",
                        "Default: 3.0")
                .defineInRange("max_size_multiplier", 3.0, 1.0, 10.0);
        
        GIANT_SNOWBALL_BLIZZARD_DURATION = BUILDER
                .comment("Blizzard duration in seconds.",
                        "Default: 5")
                .defineInRange("blizzard_duration", 5, 1, 30);
        
        GIANT_SNOWBALL_BLIZZARD_RADIUS = BUILDER
                .comment("Blizzard radius.",
                        "Default: 4.0")
                .defineInRange("blizzard_radius", 4.0, 1.0, 10.0);
        
        BUILDER.pop();

        // 元素标记头顶显示配置
        BUILDER.push("elementMarkIcon");
        
        ELEMENT_MARK_ICON_ENABLED = BUILDER
                .comment("Enable element mark icons displayed above entities' heads.",
                        "When enabled, icons representing active element marks will be rendered above entities.",
                        "Default: true")
                .define("enabled", true);
        
        ELEMENT_MARK_ICON_HEIGHT = BUILDER
                .comment("Height offset for element mark icons above entity heads.",
                        "Higher values move the icons further above the entity.",
                        "Default: 0.5 blocks above entity height",
                        "Range: 0.1 - 3.0")
                .defineInRange("height_offset", 0.5, 0.1, 3.0);
        
        ELEMENT_MARK_ICON_SCALE = BUILDER
                .comment("Scale of element mark icons.",
                        "Higher values make the icons larger.",
                        "Default: 1.0 (normal size)",
                        "Range: 0.5 - 3.0")
                .defineInRange("scale", 1.0, 0.5, 3.0);
        
        BUILDER.pop();
        
        // 关闭法术配置分类
        BUILDER.pop();

        // ==================== BUFF 配置 ====================
        BUILDER.push("buffs");

        // 避雷针 Buff 配置
        BUILDER.push("lightningRodBuff");

        LIGHTNING_ROD_ICE_RESIST_REDUCTION = BUILDER
                .comment("Ice magic resist reduction per level (as decimal).",
                        "Default: -0.05 (-5%)",
                        "Range: -0.5 to 0.0")
                .defineInRange("iceResistReduction", -0.05, -0.5, 0.0);

        LIGHTNING_ROD_LIGHTNING_RESIST_REDUCTION = BUILDER
                .comment("Lightning magic resist reduction per level (as decimal).",
                        "Default: -0.10 (-10%)",
                        "Range: -0.5 to 0.0")
                .defineInRange("lightningResistReduction", -0.10, -0.5, 0.0);

        LIGHTNING_ROD_DURATION_SECONDS = BUILDER
                .comment("Duration of Lightning Rod buff in seconds.",
                        "Default: 10 seconds",
                        "Range: 1 - 60")
                .defineInRange("durationSeconds", 10, 1, 60);

        LIGHTNING_ROD_MAX_STACKS = BUILDER
                .comment("Maximum stack count for Lightning Rod buff.",
                        "Default: 5 stacks",
                        "Range: 1 - 10")
                .defineInRange("maxStacks", 5, 1, 10);

        BUILDER.pop();

        // 暗夜无光 Buff 配置
        BUILDER.push("darknessBuff");

        DARKNESS_BLOOD_RESIST_REDUCTION = BUILDER
                .comment("Blood magic resist reduction (as decimal).",
                        "Default: -0.05 (-5%)",
                        "Range: -0.5 to 0.0")
                .defineInRange("bloodResistReduction", -0.05, -0.5, 0.0);

        BUILDER.pop();

        // 混沌 Buff 配置
        BUILDER.push("chaosBuff");

        CHAOS_SPELL_POWER_BONUS_PER_LEVEL = BUILDER
                .comment("Spell power bonus per level (as decimal).",
                        "Default: 0.05 (+5% per level)",
                        "Range: 0.0 - 0.5")
                .defineInRange("spellPowerBonusPerLevel", 0.05, 0.0, 0.5);

        BUILDER.pop();

        // 瘟疫 Buff 配置
        BUILDER.push("plagueBuff");

        PLAGUE_MAX_HEALTH_REDUCTION_PER_LEVEL = BUILDER
                .comment("Max health reduction per level (as decimal).",
                        "Default: 0.02 (-2% per level)",
                        "Range: 0.0 - 0.1")
                .defineInRange("maxHealthReductionPerLevel", 0.02, 0.0, 0.1);

        PLAGUE_ZOMBIE_CONVERSION_CHANCE = BUILDER
                .comment("Chance to convert to zombie on death (as decimal).",
                        "Default: 0.25 (25%)",
                        "Range: 0.0 - 1.0")
                .defineInRange("zombieConversionChance", 0.25, 0.0, 1.0);

        PLAGUE_EXPLOSION_CHANCE = BUILDER
                .comment("Chance for poison explosion on death (as decimal).",
                        "Default: 0.75 (75%)",
                        "Range: 0.0 - 1.0")
                .defineInRange("explosionChance", 0.75, 0.0, 1.0);

        PLAGUE_DURATION_SECONDS = BUILDER
                .comment("Duration of Plague buff in seconds.",
                        "Default: 10 seconds",
                        "Range: 1 - 60")
                .defineInRange("durationSeconds", 10, 1, 60);

        PLAGUE_MAX_STACKS = BUILDER
                .comment("Maximum stack count for Plague buff.",
                        "Default: 10 stacks",
                        "Range: 1 - 20")
                .defineInRange("maxStacks", 10, 1, 20);

        BUILDER.pop();

        // 感电 Buff 配置
        BUILDER.push("electrocutedBuff");

        ELECTROCUTED_BASE_DAMAGE = BUILDER
                .comment("Base damage of chain lightning per level.",
                        "Default: 5.0",
                        "Range: 1.0 - 50.0")
                .defineInRange("baseDamage", 5.0, 1.0, 50.0);

        ELECTROCUTED_TRIGGER_INTERVAL = BUILDER
                .comment("Trigger interval in ticks (20 ticks = 1 second).",
                        "Default: 40 ticks (2 seconds)",
                        "Range: 20 - 200")
                .defineInRange("triggerInterval", 40, 20, 200);

        ELECTROCUTED_CHAIN_RANGE = BUILDER
                .comment("Chain lightning range in blocks.",
                        "Default: 8.0 blocks",
                        "Range: 1.0 - 32.0")
                .defineInRange("chainRange", 8.0, 1.0, 32.0);

        ELECTROCUTED_MAX_CHAIN_TARGETS = BUILDER
                .comment("Maximum number of chain lightning targets.",
                        "Default: 3 targets",
                        "Range: 1 - 10")
                .defineInRange("maxChainTargets", 3, 1, 10);

        BUILDER.pop();

        // 终末回响 Buff 配置
        BUILDER.push("enderEchoBuff");

        ENDER_ECHO_SPELL_POWER_RATIO = BUILDER
                .comment("Spell power bonus ratio based on ender power (as decimal).",
                        "Default: 0.333 (33.3% of ender power bonus)",
                        "Range: 0.0 - 1.0")
                .defineInRange("spellPowerRatio", 0.333, 0.0, 1.0);

        ENDER_ECHO_SPELL_RESIST_RATIO = BUILDER
                .comment("Spell resist bonus ratio based on ender power (as decimal).",
                        "Default: 0.5 (50% of ender power bonus)",
                        "Range: 0.0 - 1.0")
                .defineInRange("spellResistRatio", 0.5, 0.0, 1.0);

        BUILDER.pop();

        // 魔法散弹 Buff 配置
        BUILDER.push("magicShotgunBuff");

        MAGIC_SHOTGUN_BUFF_SPELL_POWER_REDUCTION_PER_LEVEL = BUILDER
                .comment("Spell power reduction per level (as decimal, negative value).",
                        "Default: -0.10 (-10% per level)",
                        "Range: -0.5 to 0.0")
                .defineInRange("spellPowerReductionPerLevel", -0.10, -0.5, 0.0);

        MAGIC_SHOTGUN_BUFF_CAST_TIME_REDUCTION_PER_LEVEL = BUILDER
                .comment("Cast time reduction per level (as decimal).",
                        "Default: 0.10 (+10% per level)",
                        "Range: 0.0 - 0.5")
                .defineInRange("castTimeReductionPerLevel", 0.10, 0.0, 0.5);

        MAGIC_SHOTGUN_BUFF_MELEE_DAMAGE_PER_LEVEL = BUILDER
                .comment("Melee damage bonus per level.",
                        "Default: 5.0 damage per level",
                        "Range: 0.0 - 20.0")
                .defineInRange("meleeDamagePerLevel", 5.0, 0.0, 20.0);

        MAGIC_SHOTGUN_BUFF_MAX_LEVEL = BUILDER
                .comment("Maximum buff level for Magic Shotgun.",
                        "Default: 5 levels",
                        "Range: 1 - 10")
                .defineInRange("maxBuffLevel", 5, 1, 10);

        BUILDER.pop();

        // 溶甲 Buff 配置
        BUILDER.push("armorReductionBuff");

        ARMOR_REDUCTION_PER_LEVEL = BUILDER
                .comment("Armor reduction per level (as decimal).",
                        "Default: 0.02 (-2% per level)",
                        "Range: 0.0 - 0.1")
                .defineInRange("armorReductionPerLevel", 0.02, 0.0, 0.1);

        BUILDER.pop();

        // 烈焰 Buff 配置
        BUILDER.push("pyroFlameBuff");

        PYRO_FLAME_DAMAGE_PER_SECOND = BUILDER
                .comment("Base damage per second from Pyro Flame effect.",
                        "Default: 1.0 damage per second",
                        "Range: 0.5 - 10.0")
                .defineInRange("damagePerSecond", 1.0, 0.5, 10.0);

        PYRO_FLAME_MAX_HEALTH_REDUCTION_PER_LEVEL = BUILDER
                .comment("Max health reduction per level (as decimal).",
                        "Default: 0.02 (-2% per level)",
                        "Range: 0.0 - 0.1")
                .defineInRange("maxHealthReductionPerLevel", 0.02, 0.0, 0.1);

        PYRO_FLAME_DAMAGE_INTERVAL = BUILDER
                .comment("Damage interval in ticks (20 ticks = 1 second).",
                        "Default: 20 ticks (1 second)",
                        "Range: 10 - 100")
                .defineInRange("damageInterval", 20, 10, 100);

        PYRO_FLAME_EXPLOSION_BASE_POWER = BUILDER
                .comment("Base explosion power when entity with Pyro Flame dies.",
                        "Default: 1.0",
                        "Range: 0.5 - 3.0")
                .defineInRange("explosionBasePower", 1.0, 0.5, 3.0);

        PYRO_FLAME_EXPLOSION_POWER_PER_LEVEL = BUILDER
                .comment("Additional explosion power per buff level.",
                        "Default: 0.3",
                        "Range: 0.1 - 1.0")
                .defineInRange("explosionPowerPerLevel", 0.3, 0.1, 1.0);

        PYRO_FLAME_EXPLOSION_DESTROY_BLOCKS = BUILDER
                .comment("Whether Pyro Flame explosion can destroy blocks.",
                        "Default: false (safe explosion)")
                .define("explosionDestroyBlocks", false);

        PYRO_FLAME_EXPLOSION_CAUSE_FIRE = BUILDER
                .comment("Whether Pyro Flame explosion can cause fire.",
                        "Default: false")
                .define("explosionCauseFire", false);

        PYRO_FLAME_ENTITY_BLACKLIST = BUILDER
                .comment("List of entity types that cannot receive Pyro Flame buff.",
                        "Format: [\"minecraft:creeper\", \"minecraft:skeleton\"]",
                        "Default: empty list (no blacklist)")
                .defineListAllowEmpty("entityBlacklist", List.of(),
                        () -> "", obj -> obj instanceof String);

        BUILDER.pop();
        
        // 关闭 BUFF 配置分类
        BUILDER.pop();

        // ==================== 调试配置 ====================
        // 【独立配置】调试配置不隶属于法术或BUFF配置
        BUILDER.push("debug");

        GLOBAL_DEBUG_MODE = BUILDER
                .comment("Enable global debug mode for all mod systems.",
                        "When enabled, detailed debug logs will be output to console.",
                        "This affects all debug output from spells, entities, trails, and element reactions.",
                        "Default: false (debug mode disabled)")
                .define("globalDebugMode", false);

        // 拖尾系统调试配置
        TRAIL_SYSTEM_DEBUG_OUTPUT = BUILDER
                .comment("Enable debug output for Trail System to console.",
                        "Includes tick updates, render counts, and trail statistics.",
                        "Default: false (debug output disabled)")
                .define("trailSystemDebugOutput", false);

        // 回响打击调试配置
        ECHO_STRIKE_DEBUG_OUTPUT = BUILDER
                .comment("Enable debug output for Echo Strike system to console.",
                        "Includes trigger attempts, success/failure reasons, and damage info.",
                        "Default: false (debug output disabled)")
                .define("echoStrikeDebugOutput", false);

        // 元素标记调试配置
        ELEMENTAL_BURST_DEBUG_OUTPUT = BUILDER
                .comment("Enable debug output for Elemental Burst spell to console.",
                        "Default: false (debug output disabled)")
                .define("elementalBurstDebugOutput", false);

        // 元素反应调试配置
        ELEMENT_REACTION_DEBUG_OUTPUT = BUILDER
                .comment("Enable debug output for Element Reaction system to console.",
                        "This includes mark application, upgrades, and reaction triggers.",
                        "Default: false (debug output disabled)")
                .define("elementReactionDebugOutput", false);

        // 图标法术调试配置
        ELEMENTAL_BARRAGE_DEBUG_OUTPUT = BUILDER
                .comment("Enable debug output for Elemental Barrage spell to console.",
                        "Default: false (debug output disabled)")
                .define("elementalBarrageDebugOutput", false);

        TRI_DIRECTIONAL_ARROW_DEBUG_OUTPUT = BUILDER
                .comment("Enable debug output for Tri Directional Arrow spell to console.",
                        "Default: false (debug output disabled)")
                .define("triDirectionalArrowDebugOutput", false);

        ELEMENTAL_PRISM_DEBUG_OUTPUT = BUILDER
                .comment("Enable debug output for Elemental Prism spell to console.",
                        "Default: false (debug output disabled)")
                .define("elementalPrismDebugOutput", false);

        BUILDER.pop();
    }

    static final ModConfigSpec SPEC = BUILDER.build();
}
