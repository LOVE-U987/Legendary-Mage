package com.legendarymage.legendarymagemod.spell;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.legendarymage.legendarymagemod.LegendaryMage;

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
     * 注册法术到事件总线
     * 
     * @param eventBus 事件总线
     */
    public static void register(IEventBus eventBus) {
        SPELLS.register(eventBus);
    }
}
