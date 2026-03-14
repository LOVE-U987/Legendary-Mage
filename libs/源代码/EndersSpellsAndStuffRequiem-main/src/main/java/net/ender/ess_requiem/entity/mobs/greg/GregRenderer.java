package net.ender.ess_requiem.entity.mobs.greg;

import com.mojang.blaze3d.vertex.PoseStack;
import net.ender.ess_requiem.EndersSpellsAndStuffRequiem;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GregRenderer extends GeoEntityRenderer<GregEntity> {

    public GregRenderer(EntityRendererProvider.Context renderManager, GeoModel<GregEntity> model) {
        super(renderManager, model);
        this.shadowRadius = 0.2f;
    }

    @Override
    public ResourceLocation getTextureLocation(GregEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(EndersSpellsAndStuffRequiem.MOD_ID, "textures/entity/greg.png");
    }

    @Override
    public void render(GregEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
