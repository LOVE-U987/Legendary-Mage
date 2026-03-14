package net.ender.ess_requiem.compat.dte.dte_registry;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.ender.ess_requiem.EndersSpellsAndStuffRequiem;
import net.ender.ess_requiem.compat.dte.compat_spells.eldritch.ForeverDreamingSpell;
import net.ender.ess_requiem.compat.dte.compat_spells.eldritch.NightmareSummonSpell;
import net.ender.ess_requiem.compat.dte.compat_spells.eldritch.uncraftable.NightmareScreamSpell;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class DTESpellRegistry {
    public static final DeferredRegister<AbstractSpell> SPELLS =
            DeferredRegister.create(io.redspace.ironsspellbooks.api.registry.SpellRegistry.SPELL_REGISTRY_KEY, EndersSpellsAndStuffRequiem.MOD_ID);

    public static DeferredHolder<AbstractSpell, AbstractSpell> registerSpell(AbstractSpell spell) {
        return SPELLS.register(spell.getSpellName(), () -> spell);
    }

    //ELDRITCH
    public static final Supplier<AbstractSpell> NIGHTMARE_SUMMON = registerSpell (new NightmareSummonSpell());
    public static final Supplier<AbstractSpell> FOREVER_DREAMING = registerSpell(new ForeverDreamingSpell());


    //UNCRAFTABLE ELDRITCH
    public static final Supplier<AbstractSpell> NIGHTMARE_SCREAM = registerSpell(new NightmareScreamSpell());



    public static void register(IEventBus eventBus)
    {
        SPELLS.register(eventBus);
    }
}
