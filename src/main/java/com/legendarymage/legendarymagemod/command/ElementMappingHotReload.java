package com.legendarymage.legendarymagemod.command;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.data.SchoolElementMappingRegistry;
import com.legendarymage.legendarymagemod.element.ElementType;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * 元素映射热加载系统
 * 提供运行时动态更新元素映射的功能
 *
 * 【功能】
 * - 将运行时命令配置的映射同步到 SchoolElementMappingRegistry
 * - 支持热重载，无需重启游戏
 * - 提供查询接口供 ElementReactionEvents 使用
 *
 * 【工作原理】
 * 1. 玩家通过命令配置元素映射（存储在 RuntimeElementMapping）
 * 2. 调用 hotReload() 方法将运行时映射同步到注册表
 * 3. ElementReactionEvents 通过本类查询映射（合并数据包+运行时）
 *
 * @author Love_U
 * @version 1.0.0
 */
public class ElementMappingHotReload {

    /**
     * 热加载的运行时映射缓存
     * 用于快速查询
     */
    private static final Map<ResourceLocation, List<ElementType>> HOT_RELOAD_CACHE = new HashMap<>();

    /**
     * 是否启用热加载
     */
    private static boolean hotReloadEnabled = true;

    /**
     * 执行热加载
     * 将 RuntimeElementMapping 中的映射同步到本系统
     *
     * @return 热加载的映射数量
     */
    public static int hotReload() {
        if (!hotReloadEnabled) {
            LegendaryMage.LOGGER.warn("[热加载] 热加载功能已禁用");
            return 0;
        }

        // 清除缓存
        HOT_RELOAD_CACHE.clear();

        // 从运行时映射加载
        Map<ResourceLocation, Map<String, ElementType>> runtimeMappings = RuntimeElementMapping.getAllMappings();
        int count = 0;

        for (Map.Entry<ResourceLocation, Map<String, ElementType>> entry : runtimeMappings.entrySet()) {
            ResourceLocation schoolId = entry.getKey();
            Map<String, ElementType> mappings = entry.getValue();

            // 转换为列表
            List<ElementType> elementList = new ArrayList<>(mappings.values());
            if (!elementList.isEmpty()) {
                HOT_RELOAD_CACHE.put(schoolId, elementList);
                count += elementList.size();

                LegendaryMage.LOGGER.debug("[热加载] 加载映射: {} -> {}", schoolId,
                        elementList.stream().map(ElementType::getId).toList());
            }
        }

        LegendaryMage.LOGGER.info("[热加载] 元素映射热加载完成: {} 个流派, {} 个映射",
                HOT_RELOAD_CACHE.size(), count);

        return count;
    }

    /**
     * 获取指定流派的元素标记（合并数据包和运行时）
     *
     * @param schoolId 流派ID
     * @return 元素标记类型列表
     */
    public static List<ElementType> getElementMarksForSchool(ResourceLocation schoolId) {
        List<ElementType> result = new ArrayList<>();

        // 1. 首先添加数据包配置的映射
        List<ElementType> dataPackMarks = SchoolElementMappingRegistry.getElementMarksForSchool(schoolId);
        if (dataPackMarks != null && !dataPackMarks.isEmpty()) {
            result.addAll(dataPackMarks);
        }

        // 2. 添加热加载的运行时映射
        List<ElementType> hotReloadMarks = HOT_RELOAD_CACHE.get(schoolId);
        if (hotReloadMarks != null && !hotReloadMarks.isEmpty()) {
            // 去重添加
            for (ElementType element : hotReloadMarks) {
                if (!result.contains(element)) {
                    result.add(element);
                }
            }
        }

        return result;
    }

    /**
     * 检查指定流派是否有元素标记映射（包括运行时）
     *
     * @param schoolId 流派ID
     * @return 是否有映射
     */
    public static boolean hasMapping(ResourceLocation schoolId) {
        // 检查数据包映射
        if (SchoolElementMappingRegistry.hasMapping(schoolId)) {
            return true;
        }

        // 检查热加载的运行时映射
        List<ElementType> hotReloadMarks = HOT_RELOAD_CACHE.get(schoolId);
        return hotReloadMarks != null && !hotReloadMarks.isEmpty();
    }

    /**
     * 添加单个映射并立即热加载
     *
     * @param schoolId    流派ID
     * @param condition   条件
     * @param elementType 元素类型
     */
    public static void addMappingAndReload(ResourceLocation schoolId, String condition, ElementType elementType) {
        // 添加到运行时映射
        RuntimeElementMapping.addMapping(schoolId, condition, elementType);

        // 更新热加载缓存
        HOT_RELOAD_CACHE.computeIfAbsent(schoolId, k -> new ArrayList<>());
        List<ElementType> elements = HOT_RELOAD_CACHE.get(schoolId);

        // 避免重复
        if (!elements.contains(elementType)) {
            elements.add(elementType);
        }

        LegendaryMage.LOGGER.debug("[热加载] 添加映射并热重载: {} [{}] -> {}",
                schoolId, condition, elementType.getId());
    }

    /**
     * 清除指定流派的映射并热重载
     *
     * @param schoolId 流派ID
     */
    public static void clearMappingsAndReload(ResourceLocation schoolId) {
        // 清除运行时映射
        RuntimeElementMapping.clearMappings(schoolId);

        // 从热加载缓存中移除
        HOT_RELOAD_CACHE.remove(schoolId);

        LegendaryMage.LOGGER.debug("[热加载] 清除映射并热重载: {}", schoolId);
    }

    /**
     * 获取所有热加载的映射
     *
     * @return 映射表
     */
    public static Map<ResourceLocation, List<ElementType>> getAllHotReloadMappings() {
        return new HashMap<>(HOT_RELOAD_CACHE);
    }

    /**
     * 设置热加载启用状态
     *
     * @param enabled 是否启用
     */
    public static void setHotReloadEnabled(boolean enabled) {
        hotReloadEnabled = enabled;
        LegendaryMage.LOGGER.info("[热加载] 热加载功能已{}", enabled ? "启用" : "禁用");
    }

    /**
     * 检查热加载是否启用
     *
     * @return 是否启用
     */
    public static boolean isHotReloadEnabled() {
        return hotReloadEnabled;
    }

    /**
     * 清除所有热加载的映射
     */
    public static void clearAll() {
        HOT_RELOAD_CACHE.clear();
        RuntimeElementMapping.clearAll();
        LegendaryMage.LOGGER.info("[热加载] 已清除所有热加载映射");
    }
}
