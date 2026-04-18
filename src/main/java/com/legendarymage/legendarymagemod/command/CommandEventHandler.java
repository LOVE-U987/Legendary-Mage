package com.legendarymage.legendarymagemod.command;

import com.legendarymage.legendarymagemod.LegendaryMage;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * 命令注册事件处理器
 * 负责在游戏中注册模组命令
 *
 * @author Love_U
 * @version 1.1.0
 */
@EventBusSubscriber(modid = LegendaryMage.MODID)
public class CommandEventHandler {

    /**
     * 注册命令事件处理器
     *
     * @param event 注册命令事件
     */
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        // 注册全英文版命令
        ElementCompatEnglishCommand.register(event.getDispatcher());
        LegendaryMage.LOGGER.info("命令注册完成: /legendarymage elementcompat");

        // 注册全中文版命令
        ElementCompatChineseCommand.register(event.getDispatcher());
        LegendaryMage.LOGGER.info("命令注册完成: /传奇法师 元素兼容性");
    }
}
