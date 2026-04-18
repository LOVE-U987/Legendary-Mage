package com.legendarymage.legendarymagemod.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.element.ElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 数据包导出器
 * 将运行时配置的元素映射导出为数据包文件
 *
 * 【功能】
 * - 导出当前配置到数据包目录
 * - 生成符合模组规范的JSON文件
 * - 支持导出单个流派或全部配置
 *
 * 【导出格式】
 * 导出到: config/legendarymage/datapacks/<namespace>/custom_schools/<school_id>.json
 *
 * @author Love_U
 * @version 1.0.0
 */
public class DataPackExporter {

    /**
     * Gson实例（美化输出）
     */
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    /**
     * 导出基础路径
     */
    private static final String EXPORT_BASE_PATH = "config/legendarymage/datapacks";

    /**
     * 导出单个流派的配置
     *
     * @param player      玩家（用于发送消息）
     * @param schoolId    流派ID
     * @param schoolName  流派显示名称
     * @return 是否导出成功
     */
    public static boolean exportSchoolConfig(ServerPlayer player, ResourceLocation schoolId, String schoolName) {
        // 获取该流派的元素映射
        List<ElementType> elements = ElementMappingHotReload.getElementMarksForSchool(schoolId);

        if (elements.isEmpty()) {
            player.sendSystemMessage(Component.literal("§c该流派没有配置元素映射！"));
            return false;
        }

        try {
            // 构建JSON数据
            Map<String, Object> jsonData = buildSchoolJson(schoolId, schoolName, elements);

            // 确定导出路径
            String namespace = schoolId.getNamespace();
            String path = schoolId.getPath();

            // 创建目录
            Path exportDir = Paths.get(EXPORT_BASE_PATH, namespace, "custom_schools");
            File dir = exportDir.toFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 写入文件
            File outputFile = new File(dir, path + ".json");
            try (FileWriter writer = new FileWriter(outputFile)) {
                GSON.toJson(jsonData, writer);
            }

            player.sendSystemMessage(Component.literal(
                    "§a成功导出配置到: §f" + outputFile.getAbsolutePath()));

            LegendaryMage.LOGGER.info("[数据包导出] 已导出 {} 的配置到 {}", schoolId, outputFile.getAbsolutePath());

            return true;

        } catch (IOException e) {
            player.sendSystemMessage(Component.literal(
                    "§c导出失败: §f" + e.getMessage()));
            LegendaryMage.LOGGER.error("[数据包导出] 导出 {} 时发生错误", schoolId, e);
            return false;
        }
    }

    /**
     * 导出所有配置
     *
     * @param player 玩家（用于发送消息）
     * @return 导出的文件数量
     */
    public static int exportAllConfigs(ServerPlayer player) {
        Map<ResourceLocation, List<ElementType>> allMappings = ElementMappingHotReload.getAllHotReloadMappings();

        if (allMappings.isEmpty()) {
            player.sendSystemMessage(Component.literal("§c没有可导出的运行时配置！"));
            return 0;
        }

        int successCount = 0;
        int failCount = 0;

        for (Map.Entry<ResourceLocation, List<ElementType>> entry : allMappings.entrySet()) {
            ResourceLocation schoolId = entry.getKey();
            List<ElementType> elements = entry.getValue();

            try {
                Map<String, Object> jsonData = buildSchoolJson(schoolId, schoolId.toString(), elements);

                String namespace = schoolId.getNamespace();
                String path = schoolId.getPath();

                Path exportDir = Paths.get(EXPORT_BASE_PATH, namespace, "custom_schools");
                File dir = exportDir.toFile();
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File outputFile = new File(dir, path + ".json");
                try (FileWriter writer = new FileWriter(outputFile)) {
                    GSON.toJson(jsonData, writer);
                }

                successCount++;
                LegendaryMage.LOGGER.debug("[数据包导出] 已导出 {} 的配置", schoolId);

            } catch (IOException e) {
                failCount++;
                LegendaryMage.LOGGER.error("[数据包导出] 导出 {} 时发生错误", schoolId, e);
            }
        }

        player.sendSystemMessage(Component.literal(
                "§a导出完成: §f成功 " + successCount + " §c失败 " + failCount));
        player.sendSystemMessage(Component.literal(
                "§7导出位置: §f" + new File(EXPORT_BASE_PATH).getAbsolutePath()));

        LegendaryMage.LOGGER.info("[数据包导出] 批量导出完成: 成功 {}, 失败 {}", successCount, failCount);

        return successCount;
    }

    /**
     * 构建流派JSON数据
     *
     * @param schoolId   流派ID
     * @param schoolName 流派名称
     * @param elements   元素列表
     * @return JSON数据Map
     */
    private static Map<String, Object> buildSchoolJson(ResourceLocation schoolId, String schoolName, List<ElementType> elements) {
        Map<String, Object> json = new LinkedHashMap<>();

        // 基本信息
        json.put("name", schoolName);
        json.put("color", generateColorFromElements(elements));
        json.put("description", "Auto-generated element compatibility config for " + schoolId);

        // 目标流派ID（指向原始流派）
        json.put("target_school_id", schoolId.toString());

        // 兼容元素
        List<String> compatibleElements = elements.stream()
                .map(ElementType::getId)
                .toList();
        json.put("compatible_elements", compatibleElements);

        // 元素标记映射
        Map<String, String> elementMarkMapping = new LinkedHashMap<>();
        for (int i = 0; i < elements.size(); i++) {
            String condition = i == 0 ? "default" : "condition_" + i;
            elementMarkMapping.put(condition, elements.get(i).getId());
        }
        json.put("element_mark_mapping", elementMarkMapping);

        // 默认属性修饰符
        Map<String, Object> attributeModifiers = new LinkedHashMap<>();
        attributeModifiers.put("spell_power_bonus", 0.0);
        attributeModifiers.put("magic_resist_bonus", 0.0);
        attributeModifiers.put("mana_cost_reduction", 0.0);
        attributeModifiers.put("cast_time_reduction", 0.0);
        json.put("attribute_modifiers", attributeModifiers);

        return json;
    }

    /**
     * 根据元素生成颜色
     *
     * @param elements 元素列表
     * @return 颜色值（十六进制）
     */
    private static int generateColorFromElements(List<ElementType> elements) {
        if (elements.isEmpty()) {
            return 0xFFFFFF; // 默认白色
        }

        // 如果只有一个元素，使用该元素的颜色
        if (elements.size() == 1) {
            return getElementColor(elements.get(0));
        }

        // 多个元素时，混合颜色
        int r = 0, g = 0, b = 0;
        for (ElementType element : elements) {
            int color = getElementColor(element);
            r += (color >> 16) & 0xFF;
            g += (color >> 8) & 0xFF;
            b += color & 0xFF;
        }

        r /= elements.size();
        g /= elements.size();
        b /= elements.size();

        return (r << 16) | (g << 8) | b;
    }

    /**
     * 获取元素对应的颜色
     *
     * @param element 元素类型
     * @return 颜色值
     */
    private static int getElementColor(ElementType element) {
        return switch (element) {
            case FIRE -> 0xFF4500;      // 火红
            case ICE -> 0x00CED1;       // 冰蓝
            case LIGHTNING -> 0xFFD700; // 雷电金
            case POISON -> 0x32CD32;    // 毒绿
            case BLOOD -> 0x8B0000;     // 血红
            case HOLY -> 0xFFFACD;      // 圣光黄
            case ELDRITCH -> 0x9400D3;  // 邪术紫
            case ENDER -> 0x2F4F4F;     // 末影灰
            default -> 0xFFFFFF;
        };
    }

    /**
     * 获取导出目录路径
     *
     * @return 导出目录的绝对路径
     */
    public static String getExportPath() {
        return new File(EXPORT_BASE_PATH).getAbsolutePath();
    }

    /**
     * 检查是否有可导出的配置
     *
     * @return 是否有配置
     */
    public static boolean hasExportableConfigs() {
        return !ElementMappingHotReload.getAllHotReloadMappings().isEmpty();
    }
}
