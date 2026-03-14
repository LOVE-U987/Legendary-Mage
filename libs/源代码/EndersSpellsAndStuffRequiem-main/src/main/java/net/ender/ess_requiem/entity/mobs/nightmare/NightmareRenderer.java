package net.ender.ess_requiem.entity.mobs.nightmare;


import com.mojang.blaze3d.vertex.PoseStack;
import net.ender.ess_requiem.EndersSpellsAndStuffRequiem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class NightmareRenderer extends GeoEntityRenderer<NightmareEntity> {


    public NightmareRenderer(EntityRendererProvider.Context renderManager, GeoModel<NightmareEntity> model) {
        super(renderManager, model);
        this.shadowRadius = .7f;
    }

    @Override
    public ResourceLocation getTextureLocation(NightmareEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(EndersSpellsAndStuffRequiem.MOD_ID, "textures/entity/nightmare.png");
    }

    @Override
    public void render(NightmareEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
