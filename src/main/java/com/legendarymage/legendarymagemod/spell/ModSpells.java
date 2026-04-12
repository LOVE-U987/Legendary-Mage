package com.legendarymage.legendarymagemod.spell;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.spell.GiantSnowballSpell;
import com.legendarymage.legendarymagemod.spell.TriDirectionalArrowSpell;

/**
 * 法术注册类
 * 负责注册模组中的所有法术
 * 
 * @author Love_U
 * @version 0.0.1
 */
public class ModSpells {

    /**
     * 法术注册器
     */
    public static final DeferredRegister<AbstractSpell> SPELLS = DeferredRegister.create(
            SpellRegistry.SPELL_REGISTRY_KEY,
            LegendaryMage.MODID
    );

    /**
     * 复苏符文法术
     */
    public static final DeferredHolder<AbstractSpell, ResurrectionRuneSpell> RESURRECTION_RUNE = SPELLS.register(
            ResurrectionRuneSpell.SPELL_ID,
            ResurrectionRuneSpell::new
    );

    /**
     * 聚爆法术
     */
    public static final DeferredHolder<AbstractSpell, ImplosionSpell> IMPLOSION = SPELLS.register(
            ImplosionSpell.SPELL_ID,
            ImplosionSpell::new
    );

    /**
     * 纵火狂法术
     */
    public static final DeferredHolder<AbstractSpell, PyromaniacSpell> PYROMANIAC = SPELLS.register(
            PyromaniacSpell.SPELL_ID,
            PyromaniacSpell::new
    );

    /**
     * 活体冰雕术法术
     */
    public static final DeferredHolder<AbstractSpell, LivingIceSculptureSpell> LIVING_ICE_SCULPTURE = SPELLS.register(
            LivingIceSculptureSpell.SPELL_ID,
            LivingIceSculptureSpell::new
    );

    /**
     * 暴风雪法术
     */
    public static final DeferredHolder<AbstractSpell, BlizzardSpell> BLIZZARD = SPELLS.register(
            BlizzardSpell.SPELL_ID,
            BlizzardSpell::new
    );

    /**
     * 元素爆发法术
     * 元素流派的核心法术
     */
    public static final DeferredHolder<AbstractSpell, ElementalBurstSpell> ELEMENTAL_BURST = SPELLS.register(
            ElementalBurstSpell.SPELL_ID,
            ElementalBurstSpell::new
    );

    /**
     * 魔法散弹法术
     * 咒刃流派的传奇法术，将法力注入武器以近战形式释放
     */
    public static final DeferredHolder<AbstractSpell, MagicShotgunSpell> MAGIC_SHOTGUN = SPELLS.register(
            MagicShotgunSpell.SPELL_ID,
            MagicShotgunSpell::new
    );

    /**
     * 冰爆锥法术
     * 铁魔法-冰系
     * 一种被迫注入了过多法力的冰锥，变得极为不稳定，在击中敌人时会触发冰爆
     */
    public static final DeferredHolder<AbstractSpell, IceExplosionConeSpell> ICE_EXPLOSION_CONE = SPELLS.register(
            IceExplosionConeSpell.SPELL_ID,
            IceExplosionConeSpell::new
    );

    /**
     * 聚能冰锥法术
     * 铁魔法-冰系
     * 舍去了冰爆锥的范围，法力更加集中且稳定。击中敌人必定冰冻，可穿透敌人，
     * 在穿透3个单位或撞向墙体后产生大冰爆，高速运动
     */
    public static final DeferredHolder<AbstractSpell, FocusedIceConeSpell> FOCUSED_ICE_CONE = SPELLS.register(
            FocusedIceConeSpell.SPELL_ID,
            FocusedIceConeSpell::new
    );

    /**
     * 巨雪球法术
     * 铁魔法-冰系
     * 先在头顶生成一个小型的雪块，这个雪块会因吟唱时间的增加而变大，
     * 当吟唱结束或中断会被释放，击中造成巨大的冰爆，且留下一片10秒的暴风雪力场
     */
    public static final DeferredHolder<AbstractSpell, GiantSnowballSpell> GIANT_SNOWBALL = SPELLS.register(
            GiantSnowballSpell.SPELL_ID,
            GiantSnowballSpell::new
    );

    /**
     * 元素弹幕法术
     * 元素流派
     * 持续发射冰、火、雷三种元素球，每种球都有独特的范围效果
     * 冰球造成范围冰冻，火球造成范围烈焰，雷球造成闪电链
     */
    public static final DeferredHolder<AbstractSpell, ElementalBarrageSpell> ELEMENTAL_BARRAGE = SPELLS.register(
            ElementalBarrageSpell.SPELL_ID,
            ElementalBarrageSpell::new
    );

    /**
     * 拖尾特效测试法术
     * 专门用于验证TrailEffect API的测试法术
     * 发射一个带有超明显拖尾效果的测试投射物
     *
     * 【调试用途】
     * 此法术仅用于开发和测试阶段，
     * 正式版本中应该移除或隐藏。
     */
    public static final DeferredHolder<AbstractSpell, TrailTestSpell> TRAIL_TEST = SPELLS.register(
            TrailTestSpell.SPELL_ID,
            TrailTestSpell::new
    );

    /**
     * 三向之矢法术
     * 元素流派
     * 长吟唱后依次释放冰、火、雷三种元素箭
     * 击中敌人立即造成4格范围伤害，落地产生持续5秒的元素区域伤害
     */
    public static final DeferredHolder<AbstractSpell, TriDirectionalArrowSpell> TRI_DIRECTIONAL_ARROW = SPELLS.register(
            TriDirectionalArrowSpell.SPELL_ID,
            TriDirectionalArrowSpell::new
    );

    /**
     * 元素棱镜法术
     * 元素流派
     * 可折射范围内敌人的元素标记，且可使相关的元素反应触发两次
     * 吟唱时间两秒，基础范围6格，每级+2格范围
     * 基础蓝耗125，每级+75，等级1-5，传说品质
     */
    public static final DeferredHolder<AbstractSpell, ElementalPrismSpell> ELEMENTAL_PRISM = SPELLS.register(
            ElementalPrismSpell.SPELL_ID,
            ElementalPrismSpell::new
    );

    /**
     * 注册法术到事件总线
     * 
     * @param eventBus 事件总线
     */
    public static void register(IEventBus eventBus) {
        SPELLS.register(eventBus);
    }
}
