package net.ender.ess_requiem.effects;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.ender.ess_requiem.registries.GGAttributeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class RevivingSicknessEffect extends MobEffect {
    public RevivingSicknessEffect(MobEffectCategory category, int color) {
        super(MobEffectCategory.HARMFUL, 263441);

        this.addAttributeModifier(AttributeRegistry.SPELL_POWER, ResourceLocation.parse("reviving_sickness"),
                -.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        this.addAttributeModifier(AttributeRegistry.SPELL_POWER, ResourceLocation.parse("reviving_sickness"),
                -.2, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        this.addAttributeModifier(AttributeRegistry.COOLDOWN_REDUCTION, ResourceLocation.parse("reviving_sickness"),
                -.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, ResourceLocation.parse("reviving_sickness"),
                -.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);


    }

}
