package net.ender.ess_requiem.compat.dte.dte_registry;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.ironsspellbooks.item.weapons.ExtendedWeaponTier;
import io.redspace.ironsspellbooks.item.weapons.IronsWeaponTier;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.acetheeldritchking.aces_spell_utils.registries.ASAttributeRegistry;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class DTEWeaponTiers implements Tier, IronsWeaponTier {

    public static ExtendedWeaponTier DREAM_RIPPER_SCYTHE = new ExtendedWeaponTier(2500, 14f, -2.4f, 35,
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            () -> Ingredient.of(ItemRegistry.BLOODY_VELLUM.get()),
            new AttributeContainer(AttributeRegistry.BLOOD_SPELL_POWER, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
            new AttributeContainer(AttributeRegistry.ELDRITCH_SPELL_POWER, 0.18, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
            new AttributeContainer(ASAttributeRegistry.MANA_REND, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
            new AttributeContainer(ASAttributeRegistry.SPELL_RES_PENETRATION, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));



    int uses;
    float damage;
    float speed;
    int enchantmentValue;
    TagKey<Block> incorrectBlocksForDrops;
    Supplier<Ingredient> repairIngredient;
    AttributeContainer[] attributes;

    public void ExtendedWeaponTier(int uses, float damage, float speed, int enchantmentValue, TagKey<Block> incorrectBlocksForDrops, Supplier<Ingredient> repairIngredient, AttributeContainer... attributes) {
        this.uses = uses;
        this.damage = damage;
        this.speed = speed;
        this.enchantmentValue = enchantmentValue;
        this.incorrectBlocksForDrops = incorrectBlocksForDrops;
        this.repairIngredient = repairIngredient;
        this.attributes = attributes;
    }

    @Override
    public int getUses() {
        return uses;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return damage;
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return incorrectBlocksForDrops;
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient.get();
    }

    @Override
    public AttributeContainer[] getAdditionalAttributes() {
        return this.attributes;
    }
}
