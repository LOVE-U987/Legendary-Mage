package com.legendarymage.legendarymagemod;

import java.util.List;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 传奇法师模组配置类
 * 包含元素标记显示配置、BUFF效果和相关调试配置
 */
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

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

    // 沸血引爆反应(火+血,斩杀)
    public static final ModConfigSpec.DoubleValue BOILING_BLOOD_BASE_DAMAGE;
    public static final ModConfigSpec.DoubleValue BOILING_BLOOD_LOST_HEALTH_RATIO;

    // 光暗湮灭反应(神圣+邪术,净化爆发)
    public static final ModConfigSpec.DoubleValue ANNIHILATION_BASE_DAMAGE;
    public static final ModConfigSpec.DoubleValue ANNIHILATION_PER_EFFECT_DAMAGE;

    // 邪雷挪移反应(雷+邪术,电磁引力拉拽)
    public static final ModConfigSpec.DoubleValue WARP_PULL_DISTANCE;
    public static final ModConfigSpec.DoubleValue WARP_BOSS_HEALTH_THRESHOLD;

    // 天罚充能 Buff(雷+神圣,施法者一次性质变)
    public static final ModConfigSpec.DoubleValue CHARGED_DAMAGE_MULTIPLIER;
    public static final ModConfigSpec.IntValue CHARGED_DURATION_SECONDS;

    // 圣火净化反应(火+神圣,净化+亡灵特攻)
    public static final ModConfigSpec.DoubleValue HOLY_FIRE_BASE_DAMAGE;
    public static final ModConfigSpec.DoubleValue HOLY_FIRE_UNDEAD_MULTIPLIER;

    // 血雷反击反应(雷+血,近战反伤)
    public static final ModConfigSpec.DoubleValue BLOOD_THORNS_BASE_DAMAGE;
    public static final ModConfigSpec.IntValue BLOOD_THORNS_DURATION_SECONDS;

    // 霜毒凝雾反应(冰+毒,区域叠层)
    public static final ModConfigSpec.DoubleValue FROST_FOG_RADIUS;
    public static final ModConfigSpec.IntValue FROST_FOG_DURATION_SECONDS;
    public static final ModConfigSpec.IntValue FROST_FOG_POISON_LEVEL;

    // 邪瘟毒云反应(邪术+毒,区域传染)
    public static final ModConfigSpec.DoubleValue PLAGUE_CLOUD_RADIUS;
    public static final ModConfigSpec.IntValue PLAGUE_CLOUD_DURATION_SECONDS;

    // 冰封血脉反应(冰+血,治疗抑制)
    public static final ModConfigSpec.DoubleValue ICE_BLOOD_HEAL_REDUCTION_RATIO;
    public static final ModConfigSpec.IntValue ICE_BLOOD_DURATION_SECONDS;

    // 虚空寒霜反应(冰+邪术,恐惧击退)
    public static final ModConfigSpec.DoubleValue VOID_FROST_KNOCKBACK_DISTANCE;
    public static final ModConfigSpec.IntValue VOID_FROST_DURATION_SECONDS;

    // 圣冰结晶反应(冰+神圣,区域减速)
    public static final ModConfigSpec.DoubleValue HOLY_ICE_SLOW_RADIUS;
    public static final ModConfigSpec.IntValue HOLY_ICE_DURATION_SECONDS;
    public static final ModConfigSpec.IntValue HOLY_ICE_SLOW_AMPLIFIER;

    // 狱火烙印反应(火+邪术,吸血)
    public static final ModConfigSpec.DoubleValue HELLFIRE_LIFESTEAL_RATIO;
    public static final ModConfigSpec.IntValue HELLFIRE_DURATION_SECONDS;

    // 圣露回源反应(神圣+毒,回复法术魔力)
    public static final ModConfigSpec.DoubleValue HOLY_DEW_MANA_AMOUNT;

    // ==================== 调试配置 ====================
    public static final ModConfigSpec.BooleanValue GLOBAL_DEBUG_MODE;
    public static final ModConfigSpec.BooleanValue TRAIL_SYSTEM_DEBUG_OUTPUT;
    public static final ModConfigSpec.BooleanValue ECHO_STRIKE_DEBUG_OUTPUT;
    public static final ModConfigSpec.BooleanValue ELEMENT_REACTION_DEBUG_OUTPUT;

    static {
        // ==================== 元素标记头顶显示配置 ====================
        BUILDER.push("elementMarkIcon");

        ELEMENT_MARK_ICON_ENABLED = BUILDER
                .comment("Enable element mark icons displayed above entities' heads.",
                        "When enabled, icons representing active element marks will be rendered above entities.",
                        "Default: true")
                .define("enabled", true);

        ELEMENT_MARK_ICON_HEIGHT = BUILDER
                .comment("Distance from the top of the entity's collision box to the CENTER of the mark icons.",
                        "The icon is about 0.36 blocks tall, so with the default 0.5 the icon's bottom sits roughly",
                        "5 pixels (5/16 block) above the collision box.",
                        "Higher values move the icons further above the entity.",
                        "Default: 0.5 blocks",
                        "Range: 0.0 - 3.0")
                .defineInRange("height_offset", 0.5, 0.0, 3.0);

        ELEMENT_MARK_ICON_SCALE = BUILDER
                .comment("Scale of element mark icons.",
                        "Higher values make the icons larger.",
                        "Default: 1.0 (normal size)",
                        "Range: 0.5 - 3.0")
                .defineInRange("scale", 1.0, 0.5, 3.0);

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

        // 沸血引爆反应配置
        BUILDER.push("boilingBlood");

        BOILING_BLOOD_BASE_DAMAGE = BUILDER
                .comment("Base burst damage of the boiling blood reaction (fire + blood).",
                        "Total damage = (base + missing health * lostHealthRatio) * (firePower + bloodPower) * 2.5",
                        "Default: 1.0",
                        "Range: 0.0 - 20.0")
                .defineInRange("baseDamage", 1.0, 0.0, 20.0);

        BOILING_BLOOD_LOST_HEALTH_RATIO = BUILDER
                .comment("Ratio of the target's missing health converted into burst damage.",
                        "Higher values make low-health targets take massively more damage (execution).",
                        "Default: 0.3",
                        "Range: 0.0 - 2.0")
                .defineInRange("lostHealthRatio", 0.3, 0.0, 2.0);

        BUILDER.pop();

        // 光暗湮灭反应配置
        BUILDER.push("annihilation");

        ANNIHILATION_BASE_DAMAGE = BUILDER
                .comment("Base annihilation damage of the annihilation reaction (holy + eldritch).",
                        "Total damage = (base + removedEffects * perEffectDamage) * (holyPower + eldritchPower) * 2.5",
                        "Default: 2.0",
                        "Range: 0.0 - 20.0")
                .defineInRange("baseDamage", 2.0, 0.0, 20.0);

        ANNIHILATION_PER_EFFECT_DAMAGE = BUILDER
                .comment("Bonus annihilation damage per removed effect instance.",
                        "Default: 1.5",
                        "Range: 0.0 - 20.0")
                .defineInRange("perEffectDamage", 1.5, 0.0, 20.0);

        BUILDER.pop();

        // 邪雷挪移反应配置
        BUILDER.push("enderWarp");

        WARP_PULL_DISTANCE = BUILDER
                .comment("Distance the target is pulled towards the caster by the ender warp reaction (lightning + eldritch).",
                        "The pull is a velocity displacement, not an enderman-style teleport.",
                        "Default: 5.0 blocks",
                        "Range: 1.0 - 16.0")
                .defineInRange("pullDistance", 5.0, 1.0, 16.0);

        WARP_BOSS_HEALTH_THRESHOLD = BUILDER
                .comment("Entities whose max health is greater than or equal to this value are immune to the ender warp pull.",
                        "Protects boss fights from being disrupted.",
                        "Default: 100.0",
                        "Range: 1.0 - 1000.0")
                .defineInRange("bossHealthThreshold", 100.0, 1.0, 1000.0);

        BUILDER.pop();

        // 天罚充能 Buff 配置
        BUILDER.push("chargedBuff");

        CHARGED_DAMAGE_MULTIPLIER = BUILDER
                .comment("Damage multiplier applied to the caster's next spell while the charged buff is active.",
                        "The buff is consumed after one spell hit.",
                        "Default: 2.0",
                        "Range: 1.0 - 10.0")
                .defineInRange("damageMultiplier", 2.0, 1.0, 10.0);

        CHARGED_DURATION_SECONDS = BUILDER
                .comment("Duration of the charged buff in seconds.",
                        "Default: 15 seconds",
                        "Range: 1 - 60")
                .defineInRange("durationSeconds", 15, 1, 60);

        BUILDER.pop();

        // 圣火净化反应配置
        BUILDER.push("holyFire");

        HOLY_FIRE_BASE_DAMAGE = BUILDER
                .comment("Base damage of the holy fire purification reaction (fire + holy).",
                        "Total damage = base * (firePower + holyPower) * 2.5, undead targets take this multiplied by undeadMultiplier.",
                        "Also purifies (removes) all beneficial effects from the target.",
                        "Default: 3.0",
                        "Range: 0.0 - 20.0")
                .defineInRange("baseDamage", 3.0, 0.0, 20.0);

        HOLY_FIRE_UNDEAD_MULTIPLIER = BUILDER
                .comment("Damage multiplier against undead/undying creatures for the holy fire reaction.",
                        "Default: 2.0",
                        "Range: 1.0 - 10.0")
                .defineInRange("undeadMultiplier", 2.0, 1.0, 10.0);

        BUILDER.pop();

        // 血雷反击反应配置
        BUILDER.push("bloodThorns");

        BLOOD_THORNS_BASE_DAMAGE = BUILDER
                .comment("Base thorns damage dealt back to melee attackers hitting a target with the blood thorns buff (lightning + blood).",
                        "Thorns damage = base * (lightningPower + bloodPower).",
                        "Default: 3.0",
                        "Range: 0.0 - 20.0")
                .defineInRange("baseDamage", 3.0, 0.0, 20.0);

        BLOOD_THORNS_DURATION_SECONDS = BUILDER
                .comment("Duration of the blood thorns buff in seconds.",
                        "Default: 8 seconds",
                        "Range: 1 - 60")
                .defineInRange("durationSeconds", 8, 1, 60);

        BUILDER.pop();

        // 霜毒凝雾反应配置
        BUILDER.push("frostFog");

        FROST_FOG_RADIUS = BUILDER
                .comment("Radius of the frost poison fog cloud spawned by the frost fog reaction (ice + poison).",
                        "Default: 3.0 blocks",
                        "Range: 1.0 - 8.0")
                .defineInRange("radius", 3.0, 1.0, 8.0);

        FROST_FOG_DURATION_SECONDS = BUILDER
                .comment("Duration of the frost poison fog cloud in seconds.",
                        "Default: 5 seconds",
                        "Range: 1 - 30")
                .defineInRange("durationSeconds", 5, 1, 30);

        FROST_FOG_POISON_LEVEL = BUILDER
                .comment("Poison effect level applied per second to entities inside the frost fog.",
                        "Default: 1",
                        "Range: 1 - 5")
                .defineInRange("poisonLevel", 1, 1, 5);

        BUILDER.pop();

        // 邪瘟毒云反应配置
        BUILDER.push("plagueCloud");

        PLAGUE_CLOUD_RADIUS = BUILDER
                .comment("Radius of the eldritch plague cloud spawned by the plague cloud reaction (eldritch + poison).",
                        "Default: 3.0 blocks",
                        "Range: 1.0 - 8.0")
                .defineInRange("radius", 3.0, 1.0, 8.0);

        PLAGUE_CLOUD_DURATION_SECONDS = BUILDER
                .comment("Duration of the eldritch plague cloud in seconds.",
                        "Default: 5 seconds",
                        "Range: 1 - 30")
                .defineInRange("durationSeconds", 5, 1, 30);

        BUILDER.pop();

        // 冰封血脉反应配置
        BUILDER.push("iceBlood");

        ICE_BLOOD_HEAL_REDUCTION_RATIO = BUILDER
                .comment("Heal reduction ratio for the ice blood reaction (ice + blood).",
                        "Applied as a multiplier to incoming heals on the target.",
                        "Default: 0.5 (50% heal reduction)",
                        "Range: 0.0 - 1.0")
                .defineInRange("healReductionRatio", 0.5, 0.0, 1.0);

        ICE_BLOOD_DURATION_SECONDS = BUILDER
                .comment("Duration of the ice blood debuff in seconds.",
                        "Default: 8 seconds",
                        "Range: 1 - 30")
                .defineInRange("durationSeconds", 8, 1, 30);

        BUILDER.pop();

        // 虚空寒霜反应配置
        BUILDER.push("voidFrost");

        VOID_FROST_KNOCKBACK_DISTANCE = BUILDER
                .comment("Knockback distance for the void frost reaction (ice + eldritch).",
                        "Target is pushed away from the caster by this distance.",
                        "Default: 3.0 blocks",
                        "Range: 1.0 - 10.0")
                .defineInRange("knockbackDistance", 3.0, 1.0, 10.0);

        VOID_FROST_DURATION_SECONDS = BUILDER
                .comment("Base duration of the fear effect in seconds.",
                        "Duration scales with mark level (level * duration).",
                        "Default: 3 seconds",
                        "Range: 1 - 10")
                .defineInRange("durationSeconds", 3, 1, 10);

        BUILDER.pop();

        // 圣冰结晶反应配置
        BUILDER.push("holyIce");

        HOLY_ICE_SLOW_RADIUS = BUILDER
                .comment("Radius of the holy ice slow cloud in blocks.",
                        "Default: 3.0",
                        "Range: 1.0 - 8.0")
                .defineInRange("slowRadius", 3.0, 1.0, 8.0);

        HOLY_ICE_DURATION_SECONDS = BUILDER
                .comment("Duration of the holy ice slow cloud in seconds.",
                        "Default: 5",
                        "Range: 1 - 30")
                .defineInRange("durationSeconds", 5, 1, 30);

        HOLY_ICE_SLOW_AMPLIFIER = BUILDER
                .comment("Slow effect amplifier applied to entities inside the cloud.",
                        "Default: 1",
                        "Range: 0 - 5")
                .defineInRange("slowAmplifier", 1, 0, 5);

        BUILDER.pop();

        // 狱火烙印反应配置
        BUILDER.push("hellfireBrand");

        HELLFIRE_LIFESTEAL_RATIO = BUILDER
                .comment("Lifesteal ratio for the hellfire brand reaction (fire + eldritch).",
                        "Caster heals for damage_dealt * this_ratio when hitting a branded target.",
                        "Default: 0.1 (10%)",
                        "Range: 0.0 - 1.0")
                .defineInRange("lifestealRatio", 0.1, 0.0, 1.0);

        HELLFIRE_DURATION_SECONDS = BUILDER
                .comment("Duration of the hellfire brand debuff in seconds.",
                        "Default: 8 seconds",
                        "Range: 1 - 30")
                .defineInRange("durationSeconds", 8, 1, 30);

        BUILDER.pop();

        // 圣露回源反应配置
        BUILDER.push("holyDew");

        HOLY_DEW_MANA_AMOUNT = BUILDER
                .comment("Mana amount restored by the holy dew reaction (holy + poison).",
                        "Caster recovers this amount of mana. Triggered when holy hits a target with a poison mark.",
                        "Default: 10.0",
                        "Range: 0.0 - 100.0")
                .defineInRange("manaAmount", 10.0, 0.0, 100.0);

        BUILDER.pop();

        // 关闭 BUFF 配置分类
        BUILDER.pop();

        // ==================== 调试配置 ====================
        BUILDER.push("debug");

        GLOBAL_DEBUG_MODE = BUILDER
                .comment("Enable global debug mode for all mod systems.",
                        "When enabled, detailed debug logs will be output to console.",
                        "This affects all debug output from element reactions, entities, and trails.",
                        "Default: false (debug mode disabled)")
                .define("globalDebugMode", false);

        TRAIL_SYSTEM_DEBUG_OUTPUT = BUILDER
                .comment("Enable debug output for Trail System to console.",
                        "Includes tick updates, render counts, and trail statistics.",
                        "Default: false (debug output disabled)")
                .define("trailSystemDebugOutput", false);

        ECHO_STRIKE_DEBUG_OUTPUT = BUILDER
                .comment("Enable debug output for Echo Strike system to console.",
                        "Includes trigger attempts, success/failure reasons, and damage info.",
                        "Default: false (debug output disabled)")
                .define("echoStrikeDebugOutput", false);

        ELEMENT_REACTION_DEBUG_OUTPUT = BUILDER
                .comment("Enable debug output for Element Reaction system to console.",
                        "This includes mark application, upgrades, and reaction triggers.",
                        "Default: false (debug output disabled)")
                .define("elementReactionDebugOutput", false);

        BUILDER.pop();
    }

    static final ModConfigSpec SPEC = BUILDER.build();
}
