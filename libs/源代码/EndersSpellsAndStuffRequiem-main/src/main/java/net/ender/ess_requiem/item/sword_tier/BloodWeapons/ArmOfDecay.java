package net.ender.ess_requiem.item.sword_tier.BloodWeapons;

import io.redspace.ironsspellbooks.api.item.weapons.ExtendedSwordItem;
import io.redspace.ironsspellbooks.api.item.weapons.MagicSwordItem;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.item.UniqueItem;
import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import net.acetheeldritchking.aces_spell_utils.utils.ASRarities;
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

public class ArmOfDecay extends MagicSwordItem implements UniqueItem {
    public static final int COOLDOWN = 600;


    public ArmOfDecay() {
        super(GGSwordTier.ARM_OF_DECAY, ItemPropertiesHelper.equipment().rarity(ASRarities.COSMIC_RARITY_PROXY.getValue()).attributes(ExtendedSwordItem.createAttributes(GGSwordTier.ARM_OF_DECAY)),
                SpellDataRegistryHolder.of(
                        new SpellDataRegistryHolder(GGSpellRegistry.FINALITY_OF_DECAY, 1),
                        new SpellDataRegistryHolder(GGSpellRegistry.CORPSE_EXPLOSION, 1),
                        new SpellDataRegistryHolder(GGSpellRegistry.DECAYING_WILL, 1)));

    }

    //THANK YOU ACE MY GOAT FOR TEACHING ME THIS

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("item.ess_requiem.arm_of_decay.lore").
                withStyle(ChatFormatting.RED).
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
            tooltipComponents.add(Component.literal(" ").append(Component.translatable(this.getDescriptionId() + ".desc")).withStyle(ChatFormatting.YELLOW));
            assert attacker != null;
        } else
        {
            tooltipComponents.add(Component.translatable("item.ess_requiem.more_details").withStyle(ChatFormatting.GRAY));
        }
    }


}
