package net.ender.ess_requiem.entity.mobs.greg;

import net.ender.ess_requiem.EndersSpellsAndStuffRequiem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class GregModel extends GeoModel<GregEntity> {

    @Override
    public ResourceLocation getModelResource(GregEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(EndersSpellsAndStuffRequiem.MOD_ID, "geo/greg.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource (GregEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(EndersSpellsAndStuffRequiem.MOD_ID, "textures/entity/greg.png");
    }

    @Override
    public void setCustomAnimations(GregEntity animatable, long instanceId, AnimationState<GregEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
    }

    @Override
    public ResourceLocation getAnimationResource(GregEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(EndersSpellsAndStuffRequiem.MOD_ID, "animations/entity/greg_animation.json");
    }

}
