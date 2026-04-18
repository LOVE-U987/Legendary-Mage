package com.legendarymage.legendarymagemod.command;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.data.SchoolElementMappingRegistry;
import com.legendarymage.legendarymagemod.element.ElementType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellSlot;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.UniqueSpellBook;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * 元素兼容性命令 - 全中文版
 * 允许玩家在游戏中通过命令配置法术流派的元素标记映射
 *
 * 【功能】
 * - 手持卷轴时查看流派信息
 * - 设置和修改元素标记映射（支持多个元素）
 * - 完全中文界面
 *
 * 【使用方法】
 * /传奇法师 元素兼容性 [元素列表] - 设置当前手持卷轴流派的元素标记
 * 例如: /传奇法师 元素兼容性 火 冰 雷
 *
 * @author Love_U
 * @version 1.0.0
 */
public class ElementCompatChineseCommand {

    /**
     * 中文命令名称
     */
    public static final String COMMAND_NAME = "传奇法师";

    /**
     * 子命令名称
     */
    public static final String SUBCOMMAND_NAME = "元素兼容性";

    /**
     * 中文元素名称映射
     */
    private static final Map<String, ElementType> CHINESE_ELEMENT_MAP = new HashMap<>();

    static {
        // 火系
        CHINESE_ELEMENT_MAP.put("火", ElementType.FIRE);
        CHINESE_ELEMENT_MAP.put("火焰", ElementType.FIRE);
        CHINESE_ELEMENT_MAP.put("炎", ElementType.FIRE);

        // 冰系
        CHINESE_ELEMENT_MAP.put("冰", ElementType.ICE);
        CHINESE_ELEMENT_MAP.put("霜", ElementType.ICE);
        CHINESE_ELEMENT_MAP.put("雪", ElementType.ICE);

        // 雷系
        CHINESE_ELEMENT_MAP.put("雷", ElementType.LIGHTNING);
        CHINESE_ELEMENT_MAP.put("电", ElementType.LIGHTNING);
        CHINESE_ELEMENT_MAP.put("闪电", ElementType.LIGHTNING);

        // 毒系
        CHINESE_ELEMENT_MAP.put("毒", ElementType.POISON);
        CHINESE_ELEMENT_MAP.put("毒素", ElementType.POISON);
        CHINESE_ELEMENT_MAP.put("自然", ElementType.POISON);

        // 血系
        CHINESE_ELEMENT_MAP.put("血", ElementType.BLOOD);
        CHINESE_ELEMENT_MAP.put("血系", ElementType.BLOOD);
        CHINESE_ELEMENT_MAP.put("暗", ElementType.BLOOD);
        CHINESE_ELEMENT_MAP.put("暗影", ElementType.BLOOD);

        // 神圣
        CHINESE_ELEMENT_MAP.put("圣", ElementType.HOLY);
        CHINESE_ELEMENT_MAP.put("神圣", ElementType.HOLY);
        CHINESE_ELEMENT_MAP.put("光", ElementType.HOLY);
        CHINESE_ELEMENT_MAP.put("光明", ElementType.HOLY);

        // 邪术
        CHINESE_ELEMENT_MAP.put("邪", ElementType.ELDRITCH);
        CHINESE_ELEMENT_MAP.put("邪术", ElementType.ELDRITCH);
        CHINESE_ELEMENT_MAP.put("咒", ElementType.ELDRITCH);
        CHINESE_ELEMENT_MAP.put("秘", ElementType.ELDRITCH);

        // 末影
        CHINESE_ELEMENT_MAP.put("末影", ElementType.ENDER);
        CHINESE_ELEMENT_MAP.put("末", ElementType.ENDER);
        CHINESE_ELEMENT_MAP.put("虚空", ElementType.ENDER);
    }

    /**
     * 元素类型建议提供者
     */
    private static final SuggestionProvider<CommandSourceStack> ELEMENT_SUGGESTIONS = (context, builder) -> {
        List<String> elements = new ArrayList<>(Arrays.asList(
                "火", "冰", "雷", "毒", "血", "圣", "邪", "末影"
        ));
        return SharedSuggestionProvider.suggest(elements, builder);
    };

    /**
     * 注册命令
     *
     * @param dispatcher 命令分发器
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal(COMMAND_NAME)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal(SUBCOMMAND_NAME)
                    .executes(ElementCompatChineseCommand::executeInfo)
                    .then(Commands.argument("elements", StringArgumentType.greedyString())
                        .suggests(ELEMENT_SUGGESTIONS)
                        .executes(ElementCompatChineseCommand::executeSetElements)))
                .then(Commands.literal(SUBCOMMAND_NAME)
                    .then(Commands.literal("info")
                        .executes(ElementCompatChineseCommand::executeInfo)))
                .then(Commands.literal(SUBCOMMAND_NAME)
                    .then(Commands.literal("clear")
                        .executes(ElementCompatChineseCommand::executeClear)))
                .then(Commands.literal(SUBCOMMAND_NAME)
                    .then(Commands.literal("auto")
                        .executes(ElementCompatChineseCommand::executeAuto)))
                // 导出命令 /传奇法师 元素兼容性 导出
                .then(Commands.literal(SUBCOMMAND_NAME)
                    .then(Commands.literal("导出")
                        .executes(ElementCompatChineseCommand::executeExport)))
        );
    }

    /**
     * 执行设置多个元素命令
     */
    private static int executeSetElements(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String elementsString = StringArgumentType.getString(context, "elements");

        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("此命令只能由玩家执行"));
            return 0;
        }

        ItemStack mainHand = player.getMainHandItem();
        SchoolType schoolType = getSchoolTypeFromItem(mainHand);

        if (schoolType == null) {
            source.sendFailure(Component.literal("§c请手持法术卷轴或法术书！"));
            return 0;
        }

        ResourceLocation schoolId = schoolType.getId();
        String[] elementIds = elementsString.split("\\s+");
        List<ElementType> validElements = new ArrayList<>();
        List<String> invalidElements = new ArrayList<>();

        for (String elementId : elementIds) {
            elementId = elementId.trim();
            if (elementId.isEmpty()) continue;

            ElementType elementType = parseElementType(elementId);
            if (elementType != null) {
                validElements.add(elementType);
                // 使用热加载系统添加映射
                ElementMappingHotReload.addMappingAndReload(schoolId, "default_" + validElements.size(), elementType);
            } else {
                invalidElements.add(elementId);
            }
        }

        // 显示结果
        if (validElements.isEmpty()) {
            source.sendFailure(Component.literal("§c没有有效的元素类型！"));
            source.sendFailure(Component.literal("§7可用元素: 火, 冰, 雷, 毒, 血, 圣, 邪, 末影"));
            return 0;
        }

        // 发送成功消息
        StringBuilder elementNames = new StringBuilder();
        for (int i = 0; i < validElements.size(); i++) {
            if (i > 0) elementNames.append("§7, §6");
            elementNames.append(getElementChineseName(validElements.get(i)));
        }

        final String finalElementNames = elementNames.toString();
        source.sendSuccess(() -> Component.literal(
                "§a成功设置 §e" + schoolId + " §a的元素兼容性:"), true);
        source.sendSuccess(() -> Component.literal("§6" + finalElementNames), false);
        source.sendSuccess(() -> Component.literal("§7[热重载] 配置已立即生效！"), false);

        // 如果有无效元素，显示警告
        if (!invalidElements.isEmpty()) {
            source.sendSuccess(() -> Component.literal(
                    "§7警告: 忽略无效元素: §c" + String.join(", ", invalidElements)), false);
        }

        return 1;
    }

    /**
     * 执行 info 命令
     */
    private static int executeInfo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("此命令只能由玩家执行"));
            return 0;
        }

        ItemStack mainHand = player.getMainHandItem();
        SchoolType schoolType = getSchoolTypeFromItem(mainHand);

        if (schoolType == null) {
            source.sendFailure(Component.literal("§c请手持法术卷轴或法术书！"));
            source.sendSuccess(() -> Component.literal("§7用法: /传奇法师 元素兼容性 [元素列表]"), false);
            source.sendSuccess(() -> Component.literal("§7示例: /传奇法师 元素兼容性 火 冰 雷"), false);
            return 0;
        }

        ResourceLocation schoolId = schoolType.getId();
        source.sendSuccess(() -> Component.literal("§6========== 流派信息 =========="), false);
        source.sendSuccess(() -> Component.literal("§e流派ID: §f" + schoolId), false);
        source.sendSuccess(() -> Component.literal("§e显示名称: §f" + schoolType.getDisplayName().getString()), false);
// 使用热加载系统获取映射（包含数据包和运行时配置）
        List<ElementType> mappings = ElementMappingHotReload.getElementMarksForSchool(schoolId);

        if (mappings.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7元素映射: §c未配置"), false);
            source.sendSuccess(() -> Component.literal("§7使用 §f/传奇法师 元素兼容性 <元素列表> §7来配置"), false);
        } else {
            source.sendSuccess(() -> Component.literal("§7已配置元素:"), false);
            for (ElementType element : mappings) {
                String chineseName = getElementChineseName(element);
                source.sendSuccess(() -> Component.literal("  §a- " + chineseName + " §7(" + element.getId() + ")"), false);
            }
        }

        source.sendSuccess(() -> Component.literal("§6=============================="), false);

        return 1;
    }

    /**
     * 执行 clear 命令
     */
    private static int executeClear(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("此命令只能由玩家执行"));
            return 0;
        }

        ItemStack mainHand = player.getMainHandItem();
        SchoolType schoolType = getSchoolTypeFromItem(mainHand);

        if (schoolType == null) {
            source.sendFailure(Component.literal("§c请手持法术卷轴或法术书！"));
            return 0;
        }

        ResourceLocation schoolId = schoolType.getId();
        // 使用热加载系统清除映射
        ElementMappingHotReload.clearMappingsAndReload(schoolId);

        source.sendSuccess(() -> Component.literal(
                "§a已清除 §e" + schoolId + " §a的所有元素映射"), true);
        source.sendSuccess(() -> Component.literal("§7[热重载] 配置已立即生效！"), false);

        return 1;
    }

    /**
     * 执行 auto 命令
     */
    private static int executeAuto(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("此命令只能由玩家执行"));
            return 0;
        }

        ItemStack mainHand = player.getMainHandItem();
        SchoolType schoolType = getSchoolTypeFromItem(mainHand);

        if (schoolType == null) {
            source.sendFailure(Component.literal("§c请手持法术卷轴或法术书！"));
            return 0;
        }

        ResourceLocation schoolId = schoolType.getId();
        String schoolName = schoolType.getDisplayName().getString().toLowerCase();

        ElementType detectedElement = detectElementFromName(schoolName);

        if (detectedElement != null) {
            // 使用热加载系统添加映射
            ElementMappingHotReload.addMappingAndReload(schoolId, "default", detectedElement);
            String chineseName = getElementChineseName(detectedElement);
            source.sendSuccess(() -> Component.literal(
                    "§a自动检测到元素: §6" + chineseName + " §7(" + detectedElement.getId() + ")" +
                    " §a(基于流派名称: §e" + schoolName + "§a)"), true);
            source.sendSuccess(() -> Component.literal("§7[热重载] 配置已立即生效！"), false);
            source.sendSuccess(() -> Component.literal(
                    "§7提示: 使用 §f/传奇法师 元素兼容性 <元素列表> §7添加更多元素"), false);
        } else {
            source.sendFailure(Component.literal("§c无法自动检测元素类型，请手动设置"));
            source.sendSuccess(() -> Component.literal("§7示例: /传奇法师 元素兼容性 火 冰 雷"), false);
        }

        return detectedElement != null ? 1 : 0;
    }

    /**
     * 执行导出命令
     * 将当前配置导出为数据包
     */
    private static int executeExport(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("此命令只能由玩家执行"));
            return 0;
        }

        // 检查是否手持卷轴
        ItemStack mainHand = player.getMainHandItem();
        SchoolType schoolType = getSchoolTypeFromItem(mainHand);

        if (schoolType != null) {
            // 导出当前手持的流派
            ResourceLocation schoolId = schoolType.getId();
            String schoolName = schoolType.getDisplayName().getString();

            boolean success = DataPackExporter.exportSchoolConfig(player, schoolId, schoolName);
            if (success) {
                source.sendSuccess(() -> Component.literal(
                        "§7提示: 将导出的数据包放入世界的'datapacks'文件夹后执行 /reload"), false);
            }
            return success ? 1 : 0;
        } else {
            // 导出所有配置
            if (!DataPackExporter.hasExportableConfigs()) {
                source.sendFailure(Component.literal("§c没有可导出的配置！"));
                source.sendSuccess(() -> Component.literal("§7请先手持卷轴并配置元素"), false);
                return 0;
            }

            int count = DataPackExporter.exportAllConfigs(player);
            if (count > 0) {
                source.sendSuccess(() -> Component.literal(
                        "§7提示: 将导出的数据包放入世界的'datapacks'文件夹后执行 /reload"), false);
            }
            return count > 0 ? 1 : 0;
        }
    }

    // ==================== 辅助方法 ====================

    private static SchoolType getSchoolTypeFromItem(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return null;
        }

        if (itemStack.is(ItemRegistry.SCROLL.get())) {
            if (ISpellContainer.isSpellContainer(itemStack)) {
                ISpellContainer container = ISpellContainer.get(itemStack);
                List<SpellSlot> spells = container.getActiveSpells();
                if (!spells.isEmpty()) {
                    AbstractSpell spell = spells.get(0).getSpell();
                    if (spell != null) {
                        return spell.getSchoolType();
                    }
                }
            }
        }

        if (itemStack.getItem() instanceof SpellBook || itemStack.getItem() instanceof UniqueSpellBook) {
            if (ISpellContainer.isSpellContainer(itemStack)) {
                ISpellContainer container = ISpellContainer.get(itemStack);
                List<SpellSlot> spells = container.getActiveSpells();
                if (!spells.isEmpty()) {
                    AbstractSpell spell = spells.get(0).getSpell();
                    if (spell != null) {
                        return spell.getSchoolType();
                    }
                }
            }
        }

        return null;
    }

    private static ElementType parseElementType(String elementId) {
        if (elementId == null || elementId.trim().isEmpty()) {
            return null;
        }

        String trimmed = elementId.trim();

        // 首先检查中文映射
        ElementType chineseResult = CHINESE_ELEMENT_MAP.get(trimmed);
        if (chineseResult != null) {
            return chineseResult;
        }

        return null;
    }

    private static String getElementChineseName(ElementType elementType) {
        return switch (elementType) {
            case FIRE -> "火";
            case ICE -> "冰";
            case LIGHTNING -> "雷";
            case POISON -> "毒";
            case BLOOD -> "血";
            case HOLY -> "圣";
            case ELDRITCH -> "邪";
            case ENDER -> "末影";
            default -> elementType.getId();
        };
    }

    private static ElementType detectElementFromName(String name) {
        String lowerName = name.toLowerCase();

        if (lowerName.contains("fire") || lowerName.contains("flame") ||
            lowerName.contains("pyro") || lowerName.contains("heat") ||
            lowerName.contains("burn") || lowerName.contains("岩浆") ||
            lowerName.contains("火") || lowerName.contains("焰")) {
            return ElementType.FIRE;
        }

        if (lowerName.contains("ice") || lowerName.contains("frost") ||
            lowerName.contains("cold") || lowerName.contains("freeze") ||
            lowerName.contains("snow") || lowerName.contains("冰") ||
            lowerName.contains("霜") || lowerName.contains("雪")) {
            return ElementType.ICE;
        }

        if (lowerName.contains("lightning") || lowerName.contains("thunder") ||
            lowerName.contains("electric") || lowerName.contains("storm") ||
            lowerName.contains("volt") || lowerName.contains("雷") ||
            lowerName.contains("电") || lowerName.contains("风暴")) {
            return ElementType.LIGHTNING;
        }

        if (lowerName.contains("poison") || lowerName.contains("toxic") ||
            lowerName.contains("venom") || lowerName.contains("nature") ||
            lowerName.contains("毒") || lowerName.contains("自然")) {
            return ElementType.POISON;
        }

        if (lowerName.contains("blood") || lowerName.contains("dark") ||
            lowerName.contains("shadow") || lowerName.contains("血") ||
            lowerName.contains("暗") || lowerName.contains("影")) {
            return ElementType.BLOOD;
        }

        if (lowerName.contains("holy") || lowerName.contains("divine") ||
            lowerName.contains("light") || lowerName.contains("sacred") ||
            lowerName.contains("blessed") || lowerName.contains("圣") ||
            lowerName.contains("光") || lowerName.contains("神")) {
            return ElementType.HOLY;
        }

        if (lowerName.contains("eldritch") || lowerName.contains("curse") ||
            lowerName.contains("arcane") || lowerName.contains("mystic") ||
            lowerName.contains("邪") || lowerName.contains("咒") ||
            lowerName.contains("秘")) {
            return ElementType.ELDRITCH;
        }

        if (lowerName.contains("ender") || lowerName.contains("void") ||
            lowerName.contains("teleport") || lowerName.contains("影") ||
            lowerName.contains("末") || lowerName.contains("虚空")) {
            return ElementType.ENDER;
        }

        return null;
    }
}
