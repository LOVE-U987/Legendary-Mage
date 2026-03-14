package com.legendarymage.legendarymagemod;

import net.neoforged.neoforge.common.ModConfigSpec;

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
    public static final ModConfigSpec.DoubleValue PYROMANIAC_EXPLOSION_BASE_POWER;
    public static final ModConfigSpec.DoubleValue PYROMANIAC_EXPLOSION_POWER_PER_LEVEL;
    public static final ModConfigSpec.BooleanValue PYROMANIAC_EXPLOSION_DESTROY_BLOCKS;
    public static final ModConfigSpec.BooleanValue PYROMANIAC_EXPLOSION_CAUSE_FIRE;
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

    // ==================== 元素爆发法术配置 ====================
    public static final ModConfigSpec.BooleanValue ELEMENTAL_BURST_DEBUG_OUTPUT;

    // ==================== 元素反应配置 ====================
    public static final ModConfigSpec.BooleanValue ELEMENT_REACTION_DEBUG_OUTPUT;

    static {
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
        
        PYROMANIAC_EXPLOSION_BASE_POWER = BUILDER
                .comment("Base explosion power for Pyro Flame effect when entity dies.",
                        "Default: 1.0",
                        "Range: 0.5 - 3.0")
                .defineInRange("explosionBasePower", 1.0, 0.5, 3.0);

        PYROMANIAC_EXPLOSION_POWER_PER_LEVEL = BUILDER
                .comment("Additional explosion power per buff level.",
                        "Default: 0.3",
                        "Range: 0.1 - 1.0")
                .defineInRange("explosionPowerPerLevel", 0.3, 0.1, 1.0);

        PYROMANIAC_EXPLOSION_DESTROY_BLOCKS = BUILDER
                .comment("Whether Pyro Flame explosion can destroy blocks.",
                        "Default: false (safe explosion)")
                .define("explosionDestroyBlocks", false);

        PYROMANIAC_EXPLOSION_CAUSE_FIRE = BUILDER
                .comment("Whether Pyro Flame explosion can cause fire.",
                        "Default: false")
                .define("explosionCauseFire", false);

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

        // 元素爆发配置
        BUILDER.push("elementalBurst");
        
        ELEMENTAL_BURST_DEBUG_OUTPUT = BUILDER
                .comment("Enable debug output for Elemental Burst spell to console.",
                        "Default: true (will output element type and damage info)")
                .define("debugOutput", true);
        
        BUILDER.pop();

        // 元素反应配置
        BUILDER.push("elementReaction");
        
        ELEMENT_REACTION_DEBUG_OUTPUT = BUILDER
                .comment("Enable debug output for Element Reaction system to console.",
                        "This includes mark application, upgrades, and reaction triggers.",
                        "Default: true (will output debug info)")
                .define("debugOutput", true);
        
        BUILDER.pop();
    }

    static final ModConfigSpec SPEC = BUILDER.build();
}
