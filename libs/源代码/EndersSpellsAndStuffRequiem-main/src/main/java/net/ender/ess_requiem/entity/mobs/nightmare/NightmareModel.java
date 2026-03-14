package net.ender.ess_requiem.entity.mobs.nightmare;


import net.ender.ess_requiem.EndersSpellsAndStuffRequiem;
import net.ender.ess_requiem.entity.mobs.greg.GregEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class NightmareModel extends GeoModel<NightmareEntity> {


    @Override
    public ResourceLocation getModelResource(NightmareEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(EndersSpellsAndStuffRequiem.MOD_ID, "geo/nightmare.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource (NightmareEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(EndersSpellsAndStuffRequiem.MOD_ID, "textures/entity/nightmare.png");
    }

    @Override
    public void setCustomAnimations(NightmareEntity animatable, long instanceId, AnimationState<NightmareEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
    }

    @Override
    public ResourceLocation getAnimationResource(NightmareEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(EndersSpellsAndStuffRequiem.MOD_ID, "animations/entity/nightmare_animation.json");
    }


}
