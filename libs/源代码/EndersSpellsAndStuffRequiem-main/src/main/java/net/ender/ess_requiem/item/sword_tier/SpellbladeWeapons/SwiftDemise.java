package net.ender.ess_requiem.item.sword_tier.SpellbladeWeapons;

import io.redspace.ironsspellbooks.api.item.weapons.ExtendedSwordItem;
import io.redspace.ironsspellbooks.api.item.weapons.MagicSwordItem;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import net.acetheeldritchking.aces_spell_utils.utils.ASRarities;
import net.ender.ess_requiem.item.GGSwordTier;
import net.ender.ess_requiem.registries.GGSpellRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Unbreakable;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class SwiftDemise extends MagicSwordItem {
    public static final int COOLDOWN = 250;

    public SwiftDemise() {
        super(GGSwordTier.SWIFT_DEMISE, ItemPropertiesHelper.equipment().component(DataComponents.UNBREAKABLE, new Unbreakable(true)).fireResistant().rarity(ASRarities.COSMIC_RARITY_PROXY.getValue()).attributes(ExtendedSwordItem.createAttributes(GGSwordTier.SWIFT_DEMISE)),
                SpellDataRegistryHolder.of
                        (new SpellDataRegistryHolder(GGSpellRegistry.DISMANTLE, 1),
                                new SpellDataRegistryHolder(GGSpellRegistry.QUICK_SLICE, 6)

                        ));


    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("item.ess_requiem.swift_demise.lore").
                withStyle(ChatFormatting.GOLD).
                withStyle(ChatFormatting.ITALIC));

        if (Screen.hasShiftDown())
        {
            LivingEntity attacker = MinecraftInstanceHelper.getPlayer();

            tooltipComponents.add(
                    Component.translatable(
                            "tooltip.irons_spellbooks.passive_ability",
                            Component.literal(Utils.timeFromTicks(COOLDOWN, 1)).withStyle(ChatFormatting.LIGHT_PURPLE)
                    ).withStyle(ChatFormatting.DARK_PURPLE)

            );
            tooltipComponents.add(Component.translatable(this.getDescriptionId() + ".desc").
                    withStyle(ChatFormatting.YELLOW).
                    withStyle(ChatFormatting.ITALIC)
            );
            tooltipComponents.add(Component.translatable(this.getDescriptionId() + ".desc2").
                    withStyle(ChatFormatting.YELLOW).
                    withStyle(ChatFormatting.ITALIC)
            );


            tooltipComponents.add(Component.literal(" ").append(Component.translatable(this.getDescriptionId() + ".desc3")).withStyle(ChatFormatting.RED));
            assert attacker != null;
        } else
        {
            tooltipComponents.add(Component.translatable("item.ess_requiem.more_details").withStyle(ChatFormatting.GRAY));
        }
    }

}
