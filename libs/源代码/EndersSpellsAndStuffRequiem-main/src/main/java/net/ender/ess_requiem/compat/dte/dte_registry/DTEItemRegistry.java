package net.ender.ess_requiem.compat.dte.dte_registry;

import net.ender.ess_requiem.EndersSpellsAndStuffRequiem;
import net.ender.ess_requiem.compat.dte.dte_sword_tier.DreamRipperItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collection;

public class DTEItemRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EndersSpellsAndStuffRequiem.MOD_ID);

    // Dream Reaver Ymir
    public static final DeferredHolder<Item, Item> DREAM_RIPPER_SCYTHE = ITEMS.register("dream_ripper", DreamRipperItem::new);


    public static Collection<DeferredHolder<Item, ? extends Item>> getDTE_ESSR_Items()
    {
        return ITEMS.getEntries();
    }

    public static void register(IEventBus eventBus)
    {
        ITEMS.register(eventBus);
    }
}
