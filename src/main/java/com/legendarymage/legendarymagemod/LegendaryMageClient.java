package com.legendarymage.legendarymagemod;

import com.legendarymage.legendarymagemod.client.renderer.IceSculptureRenderer;
import com.legendarymage.legendarymagemod.entity.ModEntities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
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
        
        LegendaryMage.LOGGER.info("冰雕生物渲染器已注册");
    }
}
