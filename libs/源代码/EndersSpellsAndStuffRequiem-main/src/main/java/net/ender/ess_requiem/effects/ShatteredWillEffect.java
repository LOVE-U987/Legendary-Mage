package net.ender.ess_requiem.effects;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.ender.ess_requiem.EndersSpellsAndStuffRequiem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ShatteredWillEffect extends MobEffect {
    public ShatteredWillEffect(MobEffectCategory pCategory, int pColor) {
        super(MobEffectCategory.HARMFUL, 7552367);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, ResourceLocation.fromNamespaceAndPath(EndersSpellsAndStuffRequiem.MOD_ID, "shattered_will"), -.2,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }
    }

