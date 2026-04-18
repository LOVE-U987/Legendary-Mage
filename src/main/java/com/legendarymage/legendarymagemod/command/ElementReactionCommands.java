package com.legendarymage.legendarymagemod.command;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.element.ElementReactionManager;
import com.legendarymage.legendarymagemod.element.ElementType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 元素反应命令注册
 * 提供元素标记的调试和管理命令
 *
 * @author Love_U
 * @version 0.0.2
 */
@EventBusSubscriber(modid = LegendaryMage.MODID)
public class ElementReactionCommands {

    /**
     * 元素类型建议提供者
     */
    private static final SuggestionProvider<CommandSourceStack> ELEMENT_TYPE_SUGGESTIONS = (context, builder) -> {
        List<String> elementTypes = Arrays.stream(ElementType.values())
                .map(ElementType::getId)
                .collect(Collectors.toList());
        return SharedSuggestionProvider.suggest(elementTypes, builder);
    };

    /**
     * 注册命令
     *
     * @param event 注册命令事件
     */
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // 注册 /elementmark 命令
        dispatcher.register(
                Commands.literal("elementmark")
                        .requires(source -> source.hasPermission(2)) // 需要权限等级2
                        .then(Commands.literal("apply")
                                .then(Commands.argument("target", EntityArgument.entity())
                                        .then(Commands.argument("element", StringArgumentType.word())
                                                .suggests(ELEMENT_TYPE_SUGGESTIONS)
                                                .then(Commands.argument("level", IntegerArgumentType.integer(1, 3))
                                                        .executes(ElementReactionCommands::executeApplyMark)
                                                )
                                                .executes(ElementReactionCommands::executeApplyMarkLevel1)
                                        )
                                )
                        )
                        .then(Commands.literal("remove")
                                .then(Commands.argument("target", EntityArgument.entity())
                                        .then(Commands.argument("element", StringArgumentType.word())
                                                .suggests(ELEMENT_TYPE_SUGGESTIONS)
                                                .executes(ElementReactionCommands::executeRemoveMark)
                                        )
                                        .then(Commands.literal("all")
                                                .executes(ElementReactionCommands::executeRemoveAllMarks)
                                        )
                                )
                        )
                        .then(Commands.literal("info")
                                .then(Commands.argument("target", EntityArgument.entity())
                                        .executes(ElementReactionCommands::executeGetInfo)
                                )
                                .executes(ElementReactionCommands::executeGetSelfInfo)
                        )
                        .then(Commands.literal("list")
                                .executes(ElementReactionCommands::executeListAllMarks)
                        )
        );

        // 注册简写 /em 命令
        dispatcher.register(
                Commands.literal("em")
                        .requires(source -> source.hasPermission(2))
                        .redirect(dispatcher.getRoot().getChild("elementmark"))
        );
    }

    /**
     * 执行施加元素标记命令（指定等级）
     *
     * @param context 命令上下文
     * @return 命令执行结果
     */
    private static int executeApplyMark(CommandContext<CommandSourceStack> context) {
        try {
            Entity target = EntityArgument.getEntity(context, "target");
            String elementId = StringArgumentType.getString(context, "element");
            int level = IntegerArgumentType.getInteger(context, "level");

            if (!(target instanceof LivingEntity livingTarget)) {
                context.getSource().sendFailure(Component.literal("§c目标必须是生物实体"));
                return 0;
            }

            if (!(context.getSource().getLevel() instanceof ServerLevel serverLevel)) {
                context.getSource().sendFailure(Component.literal("§c只能在服务器端执行此命令"));
                return 0;
            }

            ElementType elementType = ElementType.fromId(elementId);
            if (elementType == null) {
                context.getSource().sendFailure(Component.literal("§c未知的元素类型: " + elementId));
                return 0;
            }

            ServerPlayer player = context.getSource().getPlayer();
            boolean success = ElementReactionManager.applyMark(serverLevel, livingTarget, player, elementType, level);

            if (success) {
                String message = String.format("§a成功给 %s 施加 %s 元素标记 (%d级)",
                        target.getName().getString(),
                        elementType.getId(),
                        level);
                context.getSource().sendSuccess(() -> Component.literal(message), true);
                return 1;
            } else {
                context.getSource().sendFailure(Component.literal("§c施加失败，目标可能已有该元素标记"));
                return 0;
            }
        } catch (Exception e) {
            com.legendarymage.legendarymagemod.ModLogger.error("[元素命令] 施加标记失败", e);
            context.getSource().sendFailure(Component.literal("§c执行失败: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 执行施加元素标记命令（默认1级）
     *
     * @param context 命令上下文
     * @return 命令执行结果
     */
    private static int executeApplyMarkLevel1(CommandContext<CommandSourceStack> context) {
        try {
            Entity target = EntityArgument.getEntity(context, "target");
            String elementId = StringArgumentType.getString(context, "element");

            if (!(target instanceof LivingEntity livingTarget)) {
                context.getSource().sendFailure(Component.literal("§c目标必须是生物实体"));
                return 0;
            }

            if (!(context.getSource().getLevel() instanceof ServerLevel serverLevel)) {
                context.getSource().sendFailure(Component.literal("§c只能在服务器端执行此命令"));
                return 0;
            }

            ElementType elementType = ElementType.fromId(elementId);
            if (elementType == null) {
                context.getSource().sendFailure(Component.literal("§c未知的元素类型: " + elementId));
                return 0;
            }

            ServerPlayer player = context.getSource().getPlayer();
            boolean success = ElementReactionManager.applyMark(serverLevel, livingTarget, player, elementType, 1);

            if (success) {
                String message = String.format("§a成功给 %s 施加 %s 元素标记 (1级)",
                        target.getName().getString(),
                        elementType.getId());
                context.getSource().sendSuccess(() -> Component.literal(message), true);
                return 1;
            } else {
                context.getSource().sendFailure(Component.literal("§c施加失败，目标可能已有该元素标记"));
                return 0;
            }
        } catch (Exception e) {
            com.legendarymage.legendarymagemod.ModLogger.error("[元素命令] 施加标记失败", e);
            context.getSource().sendFailure(Component.literal("§c执行失败: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 执行移除指定元素标记命令
     *
     * @param context 命令上下文
     * @return 命令执行结果
     */
    private static int executeRemoveMark(CommandContext<CommandSourceStack> context) {
        try {
            Entity target = EntityArgument.getEntity(context, "target");
            String elementId = StringArgumentType.getString(context, "element");

            if (!(target instanceof LivingEntity livingTarget)) {
                context.getSource().sendFailure(Component.literal("§c目标必须是生物实体"));
                return 0;
            }

            if (!(context.getSource().getLevel() instanceof ServerLevel serverLevel)) {
                context.getSource().sendFailure(Component.literal("§c只能在服务器端执行此命令"));
                return 0;
            }

            ElementType elementType = ElementType.fromId(elementId);
            if (elementType == null) {
                context.getSource().sendFailure(Component.literal("§c未知的元素类型: " + elementId));
                return 0;
            }

            ElementReactionManager.clearMark(serverLevel, livingTarget, elementType);

            String message = String.format("§a已移除 %s 的 %s 元素标记",
                    target.getName().getString(),
                    elementType.getId());
            context.getSource().sendSuccess(() -> Component.literal(message), true);
            return 1;
        } catch (Exception e) {
            com.legendarymage.legendarymagemod.ModLogger.error("[元素命令] 移除标记失败", e);
            context.getSource().sendFailure(Component.literal("§c执行失败: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 执行移除所有元素标记命令
     *
     * @param context 命令上下文
     * @return 命令执行结果
     */
    private static int executeRemoveAllMarks(CommandContext<CommandSourceStack> context) {
        try {
            Entity target = EntityArgument.getEntity(context, "target");

            if (!(target instanceof LivingEntity livingTarget)) {
                context.getSource().sendFailure(Component.literal("§c目标必须是生物实体"));
                return 0;
            }

            if (!(context.getSource().getLevel() instanceof ServerLevel serverLevel)) {
                context.getSource().sendFailure(Component.literal("§c只能在服务器端执行此命令"));
                return 0;
            }

            ElementReactionManager.clearAllMarks(serverLevel, livingTarget);

            String message = String.format("§a已清除 %s 的所有元素标记",
                    target.getName().getString());
            context.getSource().sendSuccess(() -> Component.literal(message), true);
            return 1;
        } catch (Exception e) {
            com.legendarymage.legendarymagemod.ModLogger.error("[元素命令] 清除标记失败", e);
            context.getSource().sendFailure(Component.literal("§c执行失败: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 执行获取目标元素标记信息命令
     *
     * @param context 命令上下文
     * @return 命令执行结果
     */
    private static int executeGetInfo(CommandContext<CommandSourceStack> context) {
        try {
            Entity target = EntityArgument.getEntity(context, "target");

            if (!(target instanceof LivingEntity livingTarget)) {
                context.getSource().sendFailure(Component.literal("§c目标必须是生物实体"));
                return 0;
            }

            if (!(context.getSource().getLevel() instanceof ServerLevel serverLevel)) {
                context.getSource().sendFailure(Component.literal("§c只能在服务器端执行此命令"));
                return 0;
            }

            String info = ElementReactionManager.getMarkInfo(serverLevel, livingTarget);

            String message = String.format("§b[元素标记] §r%s: %s",
                    target.getName().getString(),
                    info);
            context.getSource().sendSuccess(() -> Component.literal(message), false);
            return 1;
        } catch (Exception e) {
            com.legendarymage.legendarymagemod.ModLogger.error("[元素命令] 获取信息失败", e);
            context.getSource().sendFailure(Component.literal("§c执行失败: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 执行获取自身元素标记信息命令
     *
     * @param context 命令上下文
     * @return 命令执行结果
     */
    private static int executeGetSelfInfo(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayer();
            if (player == null) {
                context.getSource().sendFailure(Component.literal("§c此命令只能由玩家执行"));
                return 0;
            }

            if (!(context.getSource().getLevel() instanceof ServerLevel serverLevel)) {
                context.getSource().sendFailure(Component.literal("§c只能在服务器端执行此命令"));
                return 0;
            }

            String info = ElementReactionManager.getMarkInfo(serverLevel, player);

            String message = String.format("§b[元素标记] §r你的状态: %s", info);
            context.getSource().sendSuccess(() -> Component.literal(message), false);
            return 1;
        } catch (Exception e) {
            com.legendarymage.legendarymagemod.ModLogger.error("[元素命令] 获取信息失败", e);
            context.getSource().sendFailure(Component.literal("§c执行失败: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 执行列出所有活跃元素标记命令
     *
     * @param context 命令上下文
     * @return 命令执行结果
     */
    private static int executeListAllMarks(CommandContext<CommandSourceStack> context) {
        try {
            if (!(context.getSource().getLevel() instanceof ServerLevel serverLevel)) {
                context.getSource().sendFailure(Component.literal("§c只能在服务器端执行此命令"));
                return 0;
            }

            // 获取所有带有元素标记的玩家
            List<ServerPlayer> playersWithMarks = serverLevel.players().stream()
                    .filter(player -> !ElementReactionManager.getAllMarks(player).isEmpty())
                    .collect(Collectors.toList());

            StringBuilder sb = new StringBuilder();
            sb.append("§b[元素标记系统] §r\n");
            sb.append("§f带有标记的玩家数: §e").append(playersWithMarks.size()).append("\n\n");

            if (playersWithMarks.isEmpty()) {
                sb.append("§7暂无玩家带有元素标记");
            } else {
                sb.append("§f标记详情:\n");
                for (ServerPlayer player : playersWithMarks) {
                    String info = ElementReactionManager.getMarkInfo(serverLevel, player);
                    sb.append("§e").append(player.getName().getString()).append("§r: ").append(info).append("\n");
                }
            }

            String finalMessage = sb.toString();
            context.getSource().sendSuccess(() -> Component.literal(finalMessage), false);
            return 1;
        } catch (Exception e) {
            com.legendarymage.legendarymagemod.ModLogger.error("[元素命令] 列出标记失败", e);
            context.getSource().sendFailure(Component.literal("§c执行失败: " + e.getMessage()));
            return 0;
        }
    }
}
