package net.ender.ess_requiem.compat.dte.dte_registry;

import net.ender.ess_requiem.EndersSpellsAndStuffRequiem;
import net.ender.ess_requiem.compat.dte.effects.BlissfulSleepEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DTE_EffectRegistry {
    public static final DeferredRegister<MobEffect> MOB_EFFECT_DEFERRED_REGISTER = DeferredRegister.create(Registries.MOB_EFFECT, EndersSpellsAndStuffRequiem.MOD_ID);

    public static void register(IEventBus eventBus) {
        MOB_EFFECT_DEFERRED_REGISTER.register(eventBus);
    }

    public static final DeferredHolder<MobEffect, MobEffect> BLISSFUL_SLEEP = MOB_EFFECT_DEFERRED_REGISTER.register("blissful_sleep",
            () -> new BlissfulSleepEffect(MobEffectCategory.HARMFUL, 6160515));

}
