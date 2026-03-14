package net.ender.ess_requiem.item.sword_tier.EldritchWeapons;

import io.redspace.ironsspellbooks.api.item.weapons.ExtendedSwordItem;
import io.redspace.ironsspellbooks.api.item.weapons.MagicSwordItem;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import net.ender.ess_requiem.item.GGSwordTier;
import net.ender.ess_requiem.registries.GGSpellRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class BrokenPromise extends MagicSwordItem {


    public BrokenPromise() {
        super(GGSwordTier.BROKEN_PROMISE, ItemPropertiesHelper.equipment().rarity(Rarity.EPIC).attributes(ExtendedSwordItem.createAttributes(GGSwordTier.BROKEN_PROMISE)),
                SpellDataRegistryHolder.of(new SpellDataRegistryHolder(GGSpellRegistry.EBONY_CATAPHRACT, 1),
                        new SpellDataRegistryHolder(GGSpellRegistry.NIGHT_VEIL, 1))
        );
    }



}
