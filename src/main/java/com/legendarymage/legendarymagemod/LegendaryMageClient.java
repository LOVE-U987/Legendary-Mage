package com.legendarymage.legendarymagemod;

import com.legendarymage.legendarymagemod.ModLogger;
import com.legendarymage.legendarymagemod.client.gui.ModernConfigScreen;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * 客户端初始化类
 * 当前仅注册配置界面，所有法术相关渲染器已移除。
 * 
 * @author Love_U
 * @version 1.0.9
 */
@Mod(value = LegendaryMage.MODID, dist = Dist.CLIENT)
public class LegendaryMageClient {

    /**
     * 构造函数
     * 
     * @param container 模组容器
     */
    public LegendaryMageClient(ModContainer container) {
        // 注册现代风格配置界面
        container.registerExtensionPoint(IConfigScreenFactory.class, 
            (modContainer, screen) -> new ModernConfigScreen(screen));

        ModLogger.system("传奇法师模组客户端初始化完成（法术模块已移除）");
    }
}
