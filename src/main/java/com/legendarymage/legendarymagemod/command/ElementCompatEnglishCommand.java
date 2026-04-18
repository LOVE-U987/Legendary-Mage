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
 * Element Compatibility Command - English Version
 * Allows players to configure spell school element mappings via commands
 *
 * 【Features】
 * - View school info when holding a scroll
 * - Set and modify element mark mappings (supports multiple elements)
 * - Full English interface
 *
 * 【Usage】
 * /legendarymage elementcompat [element list] - Set element marks for current spell school
 * Example: /legendarymage elementcompat fire ice lightning
 *
 * @author Love_U
 * @version 1.0.0
 */
public class ElementCompatEnglishCommand {

    /**
     * Command name
     */
    public static final String COMMAND_NAME = "legendarymage";

    /**
     * Subcommand name
     */
    public static final String SUBCOMMAND_NAME = "elementcompat";

    /**
     * Element type suggestions provider
     */
    private static final SuggestionProvider<CommandSourceStack> ELEMENT_SUGGESTIONS = (context, builder) -> {
        List<String> elements = Arrays.stream(ElementType.values())
                .map(ElementType::getId)
                .toList();
        return SharedSuggestionProvider.suggest(elements, builder);
    };

    /**
     * Register command
     *
     * @param dispatcher Command dispatcher
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal(COMMAND_NAME)
                .requires(source -> source.hasPermission(2)) // Requires OP level 2
                .then(Commands.literal(SUBCOMMAND_NAME)
                    .executes(ElementCompatEnglishCommand::executeInfo)
                    .then(Commands.argument("elements", StringArgumentType.greedyString())
                        .suggests(ELEMENT_SUGGESTIONS)
                        .executes(ElementCompatEnglishCommand::executeSetElements)))
                .then(Commands.literal(SUBCOMMAND_NAME)
                    .then(Commands.literal("info")
                        .executes(ElementCompatEnglishCommand::executeInfo)))
                .then(Commands.literal(SUBCOMMAND_NAME)
                    .then(Commands.literal("clear")
                        .executes(ElementCompatEnglishCommand::executeClear)))
                .then(Commands.literal(SUBCOMMAND_NAME)
                    .then(Commands.literal("auto")
                        .executes(ElementCompatEnglishCommand::executeAuto)))
                // 导出命令 /legendarymage elementcompat export
                .then(Commands.literal(SUBCOMMAND_NAME)
                    .then(Commands.literal("export")
                        .executes(ElementCompatEnglishCommand::executeExport)))
        );
    }

    /**
     * Execute set elements command
     */
    private static int executeSetElements(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String elementsString = StringArgumentType.getString(context, "elements");

        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("This command can only be executed by players"));
            return 0;
        }

        ItemStack mainHand = player.getMainHandItem();
        SchoolType schoolType = getSchoolTypeFromItem(mainHand);

        if (schoolType == null) {
            source.sendFailure(Component.literal("§cPlease hold a spell scroll or spellbook!"));
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

        if (validElements.isEmpty()) {
            source.sendFailure(Component.literal("§cNo valid element types!"));
            source.sendFailure(Component.literal("§7Available: fire, ice, lightning, poison, blood, holy, eldritch, ender"));
            return 0;
        }

        StringBuilder elementNames = new StringBuilder();
        for (int i = 0; i < validElements.size(); i++) {
            if (i > 0) elementNames.append("§7, §6");
            elementNames.append(validElements.get(i).getId());
        }

        final String finalElementNames = elementNames.toString();
        source.sendSuccess(() -> Component.literal(
                "§aSuccessfully set elements for §e" + schoolId + "§a:"), true);
        source.sendSuccess(() -> Component.literal("§6" + finalElementNames), false);
        source.sendSuccess(() -> Component.literal("§7[Hot Reload] Changes are effective immediately!"), false);

        if (!invalidElements.isEmpty()) {
            source.sendSuccess(() -> Component.literal(
                    "§7Warning: Ignored invalid elements: §c" + String.join(", ", invalidElements)), false);
        }

        return 1;
    }

    /**
     * Execute info command
     */
    private static int executeInfo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("This command can only be executed by players"));
            return 0;
        }

        ItemStack mainHand = player.getMainHandItem();
        SchoolType schoolType = getSchoolTypeFromItem(mainHand);

        if (schoolType == null) {
            source.sendFailure(Component.literal("§cPlease hold a spell scroll or spellbook!"));
            source.sendSuccess(() -> Component.literal("§7Usage: /legendarymage elementcompat [element list]"), false);
            source.sendSuccess(() -> Component.literal("§7Example: /legendarymage elementcompat fire ice"), false);
            return 0;
        }

        ResourceLocation schoolId = schoolType.getId();
        source.sendSuccess(() -> Component.literal("§6========== School Info =========="), false);
        source.sendSuccess(() -> Component.literal("§eSchool ID: §f" + schoolId), false);
        source.sendSuccess(() -> Component.literal("§eDisplay Name: §f" + schoolType.getDisplayName().getString()), false);

        // 使用热加载系统获取映射（包含数据包和运行时配置）
        List<ElementType> mappings = ElementMappingHotReload.getElementMarksForSchool(schoolId);

        if (mappings.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7Element Mapping: §cNot configured"), false);
            source.sendSuccess(() -> Component.literal("§7Use §f/legendarymage elementcompat <element list> §7to configure"), false);
        } else {
            source.sendSuccess(() -> Component.literal("§7Configured Elements:"), false);
            for (ElementType element : mappings) {
                source.sendSuccess(() -> Component.literal("  §a- " + element.getId()), false);
            }
        }

        source.sendSuccess(() -> Component.literal("§6================================="), false);

        return 1;
    }

    /**
     * Execute clear command
     */
    private static int executeClear(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("This command can only be executed by players"));
            return 0;
        }

        ItemStack mainHand = player.getMainHandItem();
        SchoolType schoolType = getSchoolTypeFromItem(mainHand);

        if (schoolType == null) {
            source.sendFailure(Component.literal("§cPlease hold a spell scroll or spellbook!"));
            return 0;
        }

        ResourceLocation schoolId = schoolType.getId();
        // 使用热加载系统清除映射
        ElementMappingHotReload.clearMappingsAndReload(schoolId);

        source.sendSuccess(() -> Component.literal(
                "§aCleared all element mappings for §e" + schoolId), true);
        source.sendSuccess(() -> Component.literal("§7[Hot Reload] Changes are effective immediately!"), false);

        return 1;
    }

    /**
     * Execute auto command
     */
    private static int executeAuto(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("This command can only be executed by players"));
            return 0;
        }

        ItemStack mainHand = player.getMainHandItem();
        SchoolType schoolType = getSchoolTypeFromItem(mainHand);

        if (schoolType == null) {
            source.sendFailure(Component.literal("§cPlease hold a spell scroll or spellbook!"));
            return 0;
        }

        ResourceLocation schoolId = schoolType.getId();
        String schoolName = schoolType.getDisplayName().getString().toLowerCase();

        ElementType detectedElement = detectElementFromName(schoolName);

        if (detectedElement != null) {
            // 使用热加载系统添加映射
            ElementMappingHotReload.addMappingAndReload(schoolId, "default", detectedElement);
            source.sendSuccess(() -> Component.literal(
                    "§aAuto-detected element: §6" + detectedElement.getId() +
                    " §a(based on school name: §e" + schoolName + "§a)"), true);
            source.sendSuccess(() -> Component.literal("§7[Hot Reload] Changes are effective immediately!"), false);
            source.sendSuccess(() -> Component.literal(
                    "§7Tip: Use §f/legendarymage elementcompat <element list> §7to add more"), false);
        } else {
            source.sendFailure(Component.literal("§cCould not auto-detect element type, please set manually"));
            source.sendSuccess(() -> Component.literal("§7Example: /legendarymage elementcompat fire ice lightning"), false);
        }

        return detectedElement != null ? 1 : 0;
    }

    /**
     * Execute export command
     * Export current configuration to datapack
     */
    private static int executeExport(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("This command can only be executed by players"));
            return 0;
        }

        // Check if holding a scroll/book
        ItemStack mainHand = player.getMainHandItem();
        SchoolType schoolType = getSchoolTypeFromItem(mainHand);

        if (schoolType != null) {
            // Export current held school
            ResourceLocation schoolId = schoolType.getId();
            String schoolName = schoolType.getDisplayName().getString();

            boolean success = DataPackExporter.exportSchoolConfig(player, schoolId, schoolName);
            if (success) {
                source.sendSuccess(() -> Component.literal(
                        "§7Tip: Place the exported datapack in your world's 'datapacks' folder and run /reload"), false);
            }
            return success ? 1 : 0;
        } else {
            // Export all configurations
            if (!DataPackExporter.hasExportableConfigs()) {
                source.sendFailure(Component.literal("§cNo exportable configurations found!"));
                source.sendSuccess(() -> Component.literal("§7Hold a scroll and configure elements first"), false);
                return 0;
            }

            int count = DataPackExporter.exportAllConfigs(player);
            if (count > 0) {
                source.sendSuccess(() -> Component.literal(
                        "§7Tip: Place the exported datapack in your world's 'datapacks' folder and run /reload"), false);
            }
            return count > 0 ? 1 : 0;
        }
    }

    // ==================== Helper Methods ====================

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

        String trimmed = elementId.trim().toLowerCase();

        try {
            return ElementType.valueOf(trimmed.toUpperCase());
        } catch (IllegalArgumentException ignored) {
        }

        for (ElementType type : ElementType.values()) {
            if (type.getId().equalsIgnoreCase(trimmed)) {
                return type;
            }
        }

        return null;
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
