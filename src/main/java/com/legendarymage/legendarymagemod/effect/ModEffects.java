package com.legendarymage.legendarymagemod.effect;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.legendarymage.legendarymagemod.LegendaryMage;

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
     * 注册效果到事件总线
     * 
     * @param eventBus 事件总线
     */
    public static void register(IEventBus eventBus) {
        EFFECTS.register(eventBus);
    }
}
