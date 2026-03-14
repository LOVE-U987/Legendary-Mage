package net.ender.ess_requiem.item.sword_tier.SpellbladeWeapons;

import io.redspace.ironsspellbooks.api.item.weapons.ExtendedSwordItem;
import io.redspace.ironsspellbooks.api.item.weapons.MagicSwordItem;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import net.ender.ess_requiem.item.GGSwordTier;
import net.ender.ess_requiem.registries.GGSpellRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class Expertise extends MagicSwordItem {

    public Expertise() {
        super(GGSwordTier.EXPERTISE, ItemPropertiesHelper.equipment().rarity(Rarity.EPIC).attributes(ExtendedSwordItem.createAttributes(GGSwordTier.EXPERTISE)),
                SpellDataRegistryHolder.of(new SpellDataRegistryHolder(GGSpellRegistry.CLEAVE, 1),
                        new SpellDataRegistryHolder(GGSpellRegistry.DISMANTLE, 1))
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("item.ess_requiem.expertise.lore").
                withStyle(ChatFormatting.RED).
                withStyle(ChatFormatting.ITALIC));

    }

}
