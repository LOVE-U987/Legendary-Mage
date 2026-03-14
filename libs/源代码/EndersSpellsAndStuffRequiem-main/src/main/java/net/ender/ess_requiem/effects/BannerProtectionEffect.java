package net.ender.ess_requiem.effects;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.ender.ess_requiem.registries.GGAttributeRegistry;
import net.ender.ess_requiem.registries.GGSpellRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class BannerProtectionEffect extends MagicMobEffect {

    public BannerProtectionEffect(MobEffectCategory category, int color) {
        super(MobEffectCategory.BENEFICIAL, 14522123);

        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, ResourceLocation.parse("banner_protection"),
                5, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(GGAttributeRegistry.BLADE_SPELL_POWER, ResourceLocation.parse("banner_protection"),
                .1 , AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }


}


