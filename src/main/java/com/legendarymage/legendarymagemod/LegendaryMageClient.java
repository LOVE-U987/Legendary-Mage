package com.legendarymage.legendarymagemod;

import com.legendarymage.legendarymagemod.client.model.ElementalOrbModel;
import com.legendarymage.legendarymagemod.client.renderer.IceSculptureRenderer;
import com.legendarymage.legendarymagemod.entity.ModEntities;
import com.legendarymage.legendarymagemod.entity.spell.ElementalArrowRenderer;
import com.legendarymage.legendarymagemod.entity.spell.ElementalOrbRenderer;
import com.legendarymage.legendarymagemod.entity.spell.FocusedIceConeRenderer;
import com.legendarymage.legendarymagemod.entity.spell.GiantSnowballRenderer;
import com.legendarymage.legendarymagemod.entity.spell.IceExplosionConeRenderer;
import com.legendarymage.legendarymagemod.entity.spell.TrailTestProjectileRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * 客户端初始化类
 * 负责注册客户端特有的内容，如渲染器
 * 
 * @author Love_U
 * @version 0.0.1
 */
@Mod(value = LegendaryMage.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = LegendaryMage.MODID, value = Dist.CLIENT)
public class LegendaryMageClient {

    /**
     * 构造函数
     * 
     * @param container 模组容器
     */
    public LegendaryMageClient(ModContainer container) {
        // 注册配置界面
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    /**
     * 客户端设置事件
     * 注册实体渲染器
     * 
     * @param event 客户端设置事件
     */
    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        LegendaryMage.LOGGER.info("HELLO FROM CLIENT SETUP");
        LegendaryMage.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        
        // 注册冰雕生物渲染器
        EntityRenderers.register(ModEntities.ICE_SCULPTURE.get(), IceSculptureRenderer::new);
        
        // 注册冰爆锥渲染器
        EntityRenderers.register(ModEntities.ICE_EXPLOSION_CONE.get(), IceExplosionConeRenderer::new);
        
        // 注册聚能冰锥渲染器
        EntityRenderers.register(ModEntities.FOCUSED_ICE_CONE.get(), FocusedIceConeRenderer::new);
        
        // 注册巨雪球渲染器
        EntityRenderers.register(ModEntities.GIANT_SNOWBALL.get(), GiantSnowballRenderer::new);
        
        // 注册元素球渲染器
        EntityRenderers.register(ModEntities.ELEMENTAL_ORB.get(), ElementalOrbRenderer::new);
        
        // 注册元素箭渲染器
        EntityRenderers.register(ModEntities.ELEMENTAL_ARROW.get(), ElementalArrowRenderer::new);

        // 注册拖尾测试投射物渲染器
        EntityRenderers.register(ModEntities.TRAIL_TEST.get(), TrailTestProjectileRenderer::new);

        LegendaryMage.LOGGER.info("冰雕生物渲染器已注册");
        LegendaryMage.LOGGER.info("冰爆锥渲染器已注册");
        LegendaryMage.LOGGER.info("聚能冰锥渲染器已注册");
        LegendaryMage.LOGGER.info("巨雪球渲染器已注册");
        LegendaryMage.LOGGER.info("元素球渲染器已注册");
        LegendaryMage.LOGGER.info("元素箭渲染器已注册");
        LegendaryMage.LOGGER.info("拖尾测试投射物渲染器已注册");
    }

    /**
     * 注册模型层定义
     * 
     * @param event 注册层定义事件
     */
    @SubscribeEvent
    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // 注册冰爆锥模型层
        event.registerLayerDefinition(
            IceExplosionConeRenderer.MODEL_LAYER_LOCATION,
            IceExplosionConeRenderer::createBodyLayer
        );
        
        // 注册聚能冰锥模型层
        event.registerLayerDefinition(
            FocusedIceConeRenderer.MODEL_LAYER_LOCATION,
            FocusedIceConeRenderer::createBodyLayer
        );
        
        // 注册巨雪球模型层
        event.registerLayerDefinition(
            GiantSnowballRenderer.MODEL_LAYER_LOCATION,
            GiantSnowballRenderer::createBodyLayer
        );
        
        // 注册元素球模型层
        event.registerLayerDefinition(
            ElementalOrbModel.LAYER_LOCATION,
            ElementalOrbModel::createBodyLayer
        );
        
        LegendaryMage.LOGGER.info("冰爆锥模型层已注册");
        LegendaryMage.LOGGER.info("聚能冰锥模型层已注册");
        LegendaryMage.LOGGER.info("巨雪球模型层已注册");
        LegendaryMage.LOGGER.info("元素球模型层已注册");
    }
}
