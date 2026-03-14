package net.ender.ess_requiem.compat.dte.dte_sword_tier;

import io.redspace.ironsspellbooks.api.item.weapons.ExtendedSwordItem;
import io.redspace.ironsspellbooks.api.item.weapons.MagicSwordItem;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.item.UniqueItem;
import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import net.acetheeldritchking.aces_spell_utils.utils.ASRarities;

import net.ender.ess_requiem.compat.dte.dte_registry.DTESpellRegistry;
import net.ender.ess_requiem.compat.dte.dte_registry.DTEWeaponTiers;
import net.ender.ess_requiem.registries.GGSpellRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Unbreakable;

import java.util.List;

public class DreamRipperItem extends MagicSwordItem implements UniqueItem {
    public DreamRipperItem() {
        super(


                DTEWeaponTiers.DREAM_RIPPER_SCYTHE,
                ItemPropertiesHelper.equipment(1).fireResistant().rarity(ASRarities.ACCURSED_RARITY_PROXY.getValue()).attributes(ExtendedSwordItem.createAttributes(DTEWeaponTiers.DREAM_RIPPER_SCYTHE)).component(DataComponents.UNBREAKABLE, new Unbreakable(false)),
                SpellDataRegistryHolder.of(

                        new SpellDataRegistryHolder(DTESpellRegistry.FOREVER_DREAMING, 1),
                        new SpellDataRegistryHolder(DTESpellRegistry.NIGHTMARE_SUMMON, 1)));

    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("item.ess_requiem.dream_ripper.lore").
                withStyle(ChatFormatting.DARK_RED).
                withStyle(ChatFormatting.ITALIC));

    }

}
