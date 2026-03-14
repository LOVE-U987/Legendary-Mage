package com.legendarymage.legendarymagemod.command;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.client.IceSculptureTextureManager;
import com.legendarymage.legendarymagemod.client.IceSculptureTextureManager.TextureMode;
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
 * 包含彩蛋命令等功能
 * 
 * @author Love_U
 * @version 0.0.1
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
     * 切换活体冰雕的纹理
     * 
     * @param context 命令上下文
     * @return 命令执行结果
     */
    private static int executeEasterEggCommand(CommandContext<CommandSourceStack> context) {
        // 切换纹理模式
        IceSculptureTextureManager.cycleTextureMode();
        
        // 获取当前模式
        TextureMode currentMode = IceSculptureTextureManager.getCurrentMode();
        
        // 发送反馈消息
        String message = switch (currentMode) {
            case DEFAULT -> "§b[传奇法师] §r活体冰雕纹理已切换为：§f默认§r";
            case EASTER_EGG -> "§b[传奇法师] §r活体冰雕纹理已切换为：§e彩蛋模式（随机）§r";
        };
        
        context.getSource().sendSuccess(() -> Component.literal(message), false);
        
        return 1;
    }
}
