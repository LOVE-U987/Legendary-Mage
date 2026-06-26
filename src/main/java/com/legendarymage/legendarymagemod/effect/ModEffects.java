package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.Config;
import com.legendarymage.legendarymagemod.LegendaryMage;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 效果注册类
 * 负责注册模组中的所有状态效果
 * 
 * @author Love_U
 * @version 1.0.0
 */
public class ModEffects {

    /**
     * 效果注册器
     */
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(
            Registries.MOB_EFFECT,
            LegendaryMage.MODID
    );

    /**
     * 烈焰效果
     * 类似凋零效果，但死亡时会触发小型爆炸
     */
    public static final DeferredHolder<MobEffect, PyroFlameEffect> PYRO_FLAME = EFFECTS.register(
            PyroFlameEffect.EFFECT_ID,
            PyroFlameEffect::new
    );

    // ==================== 元素标记效果 ====================

    /**
     * 血系标记效果（黑暗异常）
     */
    public static final DeferredHolder<MobEffect, BloodMarkEffect> BLOOD_MARK = EFFECTS.register(
            BloodMarkEffect.EFFECT_ID,
            BloodMarkEffect::new
    );

    /**
     * 神圣系标记效果（光明异常）
     */
    public static final DeferredHolder<MobEffect, HolyMarkEffect> HOLY_MARK = EFFECTS.register(
            HolyMarkEffect.EFFECT_ID,
            HolyMarkEffect::new
    );

    /**
     * 邪术标记效果（邪术异常）
     */
    public static final DeferredHolder<MobEffect, EldritchMarkEffect> ELDRITCH_MARK = EFFECTS.register(
            EldritchMarkEffect.EFFECT_ID,
            EldritchMarkEffect::new
    );

    /**
     * 毒系标记效果（毒素异常）
     */
    public static final DeferredHolder<MobEffect, PoisonMarkEffect> POISON_MARK = EFFECTS.register(
            PoisonMarkEffect.EFFECT_ID,
            PoisonMarkEffect::new
    );

    /**
     * 火系标记效果（火焰异常）
     */
    public static final DeferredHolder<MobEffect, FireMarkEffect> FIRE_MARK = EFFECTS.register(
            FireMarkEffect.EFFECT_ID,
            FireMarkEffect::new
    );

    /**
     * 冰系标记效果（冰冻异常）
     */
    public static final DeferredHolder<MobEffect, IceMarkEffect> ICE_MARK = EFFECTS.register(
            IceMarkEffect.EFFECT_ID,
            IceMarkEffect::new
    );

    /**
     * 雷系标记效果（雷电异常）
     */
    public static final DeferredHolder<MobEffect, LightningMarkEffect> LIGHTNING_MARK = EFFECTS.register(
            LightningMarkEffect.EFFECT_ID,
            LightningMarkEffect::new
    );

    /**
     * 末影标记效果（末影异常）
     */
    public static final DeferredHolder<MobEffect, EnderMarkEffect> ENDER_MARK = EFFECTS.register(
            EnderMarkEffect.EFFECT_ID,
            EnderMarkEffect::new
    );

    // ==================== 元素反应Buff效果 ====================

    /**
     * 混沌Buff效果
     * 邪术-猩红元素反应给予施法者的Buff
     */
    public static final DeferredHolder<MobEffect, ChaosBuffEffect> CHAOS_BUFF = EFFECTS.register(
            ChaosBuffEffect.EFFECT_ID,
            ChaosBuffEffect::new
    );

    /**
     * 溶甲效果
     * 毒素元素反应给予目标的Debuff，降低护甲值
     */
    public static final DeferredHolder<MobEffect, ArmorReductionEffect> ARMOR_REDUCTION = EFFECTS.register(
            ArmorReductionEffect.EFFECT_ID,
            ArmorReductionEffect::new
    );

    /**
     * 暗夜无光效果
     * 血系元素反应给予目标的Debuff
     */
    public static final DeferredHolder<MobEffect, DarknessBuffEffect> DARKNESS_BUFF = EFFECTS.register(
            DarknessBuffEffect.EFFECT_ID,
            DarknessBuffEffect::new
    );

    /**
     * 触电效果
     * 雷系元素反应给予目标的Debuff
     */
    public static final DeferredHolder<MobEffect, ElectrocutedBuffEffect> ELECTROCUTED_BUFF = EFFECTS.register(
            ElectrocutedBuffEffect.EFFECT_ID,
            ElectrocutedBuffEffect::new
    );

    /**
     * 终末回响Buff效果
     * 末影与任意元素反应给予施法者的Buff
     */
    public static final DeferredHolder<MobEffect, EnderEchoBuffEffect> ENDER_ECHO_BUFF = EFFECTS.register(
            EnderEchoBuffEffect.EFFECT_ID,
            EnderEchoBuffEffect::new
    );

    // ==================== 魔法散弹效果 ====================

    /**
     * 魔法散弹 Buff 效果
     * 咒刃流派的特殊 Buff，将法力注入武器以近战形式释放
     */
    public static final DeferredHolder<MobEffect, MagicShotgunBuffEffect> MAGIC_SHOTGUN_BUFF = EFFECTS.register(
            MagicShotgunBuffEffect.EFFECT_ID,
            MagicShotgunBuffEffect::new
    );

    /**
     * 避雷针 Buff 效果
     * 冰雷元素反应给予施法者的 Buff，减少雷系和冰系抗性
     */
    public static final DeferredHolder<MobEffect, LightningRodBuffEffect> LIGHTNING_ROD_BUFF = EFFECTS.register(
            LightningRodBuffEffect.EFFECT_ID,
            LightningRodBuffEffect::new
    );

    /**
     * 瘟疫 Buff 效果
     * 暗毒元素反应给予目标的 Debuff，降低生命值并可能转化为僵尸
     */
    public static final DeferredHolder<MobEffect, PlagueBuffEffect> PLAGUE_BUFF = EFFECTS.register(
            PlagueBuffEffect.EFFECT_ID,
            PlagueBuffEffect::new
    );

    /**
     * 延迟初始化配置驱动的属性修饰符
     * 在 FMLCommonSetupEvent 阶段调用，此时 Config 已加载就绪。
     * 构造函数阶段不可访问 Config，因此将 addAttributeModifier 统一集中在此处初始化。
     */
    public static void initConfigModifiers() {
        // === 混沌 Buff ===
        CHAOS_BUFF.get().addAttributeModifier(
                AttributeRegistry.SPELL_POWER,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "chaos_buff_spell_power"),
                Config.CHAOS_SPELL_POWER_BONUS_PER_LEVEL.get(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );

        // === 暗夜无光 Buff ===
        DARKNESS_BUFF.get().addAttributeModifier(
                AttributeRegistry.BLOOD_MAGIC_RESIST,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "darkness_buff_blood_resist"),
                Config.DARKNESS_BLOOD_RESIST_REDUCTION.get(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );

        // === 溶甲效果 ===
        ARMOR_REDUCTION.get().addAttributeModifier(
                Attributes.ARMOR,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "armor_reduction"),
                -Config.ARMOR_REDUCTION_PER_LEVEL.get(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );

        // === 避雷针 Buff ===
        LIGHTNING_ROD_BUFF.get().addAttributeModifier(
                AttributeRegistry.ICE_MAGIC_RESIST,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "lightning_rod_ice_resist"),
                Config.LIGHTNING_ROD_ICE_RESIST_REDUCTION.get(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
        LIGHTNING_ROD_BUFF.get().addAttributeModifier(
                AttributeRegistry.LIGHTNING_MAGIC_RESIST,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "lightning_rod_lightning_resist"),
                Config.LIGHTNING_ROD_LIGHTNING_RESIST_REDUCTION.get(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );

        // === 瘟疫 Buff ===
        PLAGUE_BUFF.get().addAttributeModifier(
                Attributes.MAX_HEALTH,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "plague_max_health"),
                -Config.PLAGUE_MAX_HEALTH_REDUCTION_PER_LEVEL.get(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );

        // === 烈焰效果 ===
        PYRO_FLAME.get().addAttributeModifier(
                Attributes.MAX_HEALTH,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "pyro_flame_health"),
                -Config.PYRO_FLAME_MAX_HEALTH_REDUCTION_PER_LEVEL.get(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );

        // === 魔法散弹 Buff ===
        MAGIC_SHOTGUN_BUFF.get().addAttributeModifier(
                AttributeRegistry.SPELL_POWER,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "magic_shotgun_spell_power"),
                -MagicShotgunBuffEffect.getSpellPowerReductionPerLevel(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
        MAGIC_SHOTGUN_BUFF.get().addAttributeModifier(
                AttributeRegistry.CAST_TIME_REDUCTION,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "magic_shotgun_cast_time"),
                -MagicShotgunBuffEffect.getCastTimeReductionPerLevel(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
        MAGIC_SHOTGUN_BUFF.get().addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "magic_shotgun_melee_damage"),
                MagicShotgunBuffEffect.getMeleeDamagePerLevel(),
                AttributeModifier.Operation.ADD_VALUE
        );
        MAGIC_SHOTGUN_BUFF.get().addAttributeModifier(
                AttributeRegistry.MAX_MANA,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "magic_shotgun_max_mana"),
                MagicShotgunBuffEffect.getMaxManaReductionDisplay(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );

        // === 终末回响 Buff ===
        ENDER_ECHO_BUFF.get().addAttributeModifier(
                AttributeRegistry.SPELL_POWER,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "ender_echo_spell_power"),
                EnderEchoBuffEffect.getBaseSpellPowerBonusDisplay(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
        ENDER_ECHO_BUFF.get().addAttributeModifier(
                AttributeRegistry.SPELL_RESIST,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "ender_echo_spell_resist"),
                EnderEchoBuffEffect.getBaseSpellResistBonusDisplay(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    /**
     * 注册效果到事件总线
     * 
     * @param eventBus 事件总线
     */
    public static void register(IEventBus eventBus) {
        EFFECTS.register(eventBus);
    }
}
