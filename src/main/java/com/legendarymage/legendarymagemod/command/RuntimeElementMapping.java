package com.legendarymage.legendarymagemod.command;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.element.ElementType;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 运行时元素映射存储
 * 存储通过命令配置的元素映射，支持持久化
 *
 * 【功能】
 * - 临时存储命令配置的元素映射
 * - 与 SchoolElementMappingRegistry 集成
 * - 支持保存到数据包文件
 *
 * @author Love_U
 * @version 1.0.0
 */
public class RuntimeElementMapping {

    /**
     * 运行时映射存储
     * Key: 流派ID, Value: (条件 -> 元素类型) 映射
     */
    private static final Map<ResourceLocation, Map<String, ElementType>> RUNTIME_MAPPINGS = new ConcurrentHashMap<>();

    /**
     * 添加元素映射
     *
     * @param schoolId    流派ID
     * @param condition   条件（如 "default", "critical"）
     * @param elementType 元素类型
     */
    public static void addMapping(ResourceLocation schoolId, String condition, ElementType elementType) {
        RUNTIME_MAPPINGS.computeIfAbsent(schoolId, k -> new HashMap<>())
                .put(condition, elementType);

        LegendaryMage.LOGGER.debug("[RuntimeMapping] 添加映射: {} [{}] -> {}",
                schoolId, condition, elementType.getId());
    }

    /**
     * 移除元素映射
     *
     * @param schoolId  流派ID
     * @param condition 条件
     * @return 是否成功移除
     */
    public static boolean removeMapping(ResourceLocation schoolId, String condition) {
        Map<String, ElementType> mappings = RUNTIME_MAPPINGS.get(schoolId);
        if (mappings != null) {
            boolean removed = mappings.remove(condition) != null;
            if (removed) {
                LegendaryMage.LOGGER.debug("[RuntimeMapping] 移除映射: {} [{}]", schoolId, condition);
            }
            return removed;
        }
        return false;
    }

    /**
     * 获取指定流派的所有映射
     *
     * @param schoolId 流派ID
     * @return 条件到元素类型的映射
     */
    public static Map<String, ElementType> getMappings(ResourceLocation schoolId) {
        return RUNTIME_MAPPINGS.getOrDefault(schoolId, new HashMap<>());
    }

    /**
     * 清除指定流派的所有映射
     *
     * @param schoolId 流派ID
     */
    public static void clearMappings(ResourceLocation schoolId) {
        RUNTIME_MAPPINGS.remove(schoolId);
        LegendaryMage.LOGGER.debug("[RuntimeMapping] 清除所有映射: {}", schoolId);
    }

    /**
     * 获取所有运行时映射
     *
     * @return 所有映射的副本
     */
    public static Map<ResourceLocation, Map<String, ElementType>> getAllMappings() {
        Map<ResourceLocation, Map<String, ElementType>> copy = new HashMap<>();
        RUNTIME_MAPPINGS.forEach((schoolId, mappings) ->
                copy.put(schoolId, new HashMap<>(mappings)));
        return copy;
    }

    /**
     * 检查是否存在指定流派的映射
     *
     * @param schoolId 流派ID
     * @return 是否存在
     */
    public static boolean hasMappings(ResourceLocation schoolId) {
        Map<String, ElementType> mappings = RUNTIME_MAPPINGS.get(schoolId);
        return mappings != null && !mappings.isEmpty();
    }

    /**
     * 获取指定条件的元素类型
     *
     * @param schoolId  流派ID
     * @param condition 条件
     * @return 元素类型，如果不存在则返回null
     */
    public static ElementType getElementForCondition(ResourceLocation schoolId, String condition) {
        Map<String, ElementType> mappings = RUNTIME_MAPPINGS.get(schoolId);
        if (mappings != null) {
            return mappings.get(condition);
        }
        return null;
    }

    /**
     * 清除所有运行时映射
     */
    public static void clearAll() {
        RUNTIME_MAPPINGS.clear();
        LegendaryMage.LOGGER.debug("[RuntimeMapping] 清除所有运行时映射");
    }

    /**
     * 获取运行时映射的元素类型列表
     *
     * @param schoolId 流派ID
     * @return 元素类型列表
     */
    public static List<ElementType> getElementTypesForSchool(ResourceLocation schoolId) {
        Map<String, ElementType> mappings = RUNTIME_MAPPINGS.get(schoolId);
        if (mappings != null) {
            return new ArrayList<>(mappings.values());
        }
        return Collections.emptyList();
    }
}
