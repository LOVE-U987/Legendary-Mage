package com.legendarymage.legendarymagemod.command;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

/**
 * 传奇法师模组命令注册
 * 
 * @author Love_U
 */
@EventBusSubscriber(modid = LegendaryMage.MODID, value = Dist.CLIENT)
public class LegendaryMageCommands {

    /**
     * 注册客户端命令
     * 
     * @param event 注册客户端命令事件
     */
    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // 注册 /陌路Molu 彩蛋命令
        dispatcher.register(
            Commands.literal("陌路Molu")
                .executes(LegendaryMageCommands::executeEasterEggCommand)
        );
    }

    /**
     * 执行彩蛋命令
     * 
     * @param context 命令上下文
     * @return 命令执行结果
     */
    private static int executeEasterEggCommand(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal("§b[传奇法师] §r你好呀！感谢你发现了我的彩蛋～ §e❤️§r"), false);
        return 1;
    }
}
