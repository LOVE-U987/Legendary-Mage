package net.ender.ess_requiem.effects;

import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.ender.ess_requiem.EndersSpellsAndStuffRequiem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class SkillfulWoundEffect extends MagicMobEffect {
    public SkillfulWoundEffect(MobEffectCategory pCategory, int pColor) {
        super(MobEffectCategory.HARMFUL, 13528209);
        this.addAttributeModifier(Attributes.MAX_HEALTH, ResourceLocation.fromNamespaceAndPath(EndersSpellsAndStuffRequiem.MOD_ID, "skillful_wound"), -2,
                AttributeModifier.Operation.ADD_VALUE);
    }
    }

