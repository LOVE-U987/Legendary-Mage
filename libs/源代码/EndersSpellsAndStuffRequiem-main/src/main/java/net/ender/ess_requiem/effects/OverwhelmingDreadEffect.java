package net.ender.ess_requiem.effects;

import net.ender.ess_requiem.EndersSpellsAndStuffRequiem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class OverwhelmingDreadEffect extends MobEffect {
    public OverwhelmingDreadEffect(MobEffectCategory pCategory, int pColor) {
        super(MobEffectCategory.HARMFUL, 3020845);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, ResourceLocation.fromNamespaceAndPath(EndersSpellsAndStuffRequiem.MOD_ID, "overwhelming_dread"), -.2,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }
    }


