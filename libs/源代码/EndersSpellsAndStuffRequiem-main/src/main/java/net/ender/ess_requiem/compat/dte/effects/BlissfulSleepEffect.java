package net.ender.ess_requiem.compat.dte.effects;



import io.redspace.ironsspellbooks.effect.ISyncedMobEffect;
import net.ender.ess_requiem.EndersSpellsAndStuffRequiem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class BlissfulSleepEffect extends MobEffect implements ISyncedMobEffect {
    public BlissfulSleepEffect(MobEffectCategory category, int color) {
        super(MobEffectCategory.HARMFUL, 6160515);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, ResourceLocation.fromNamespaceAndPath(EndersSpellsAndStuffRequiem.MOD_ID, "blissful_sleep"),
                -1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);



    }




}

