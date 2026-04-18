package com.legendarymage.legendarymagemod.data;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.element.ElementType;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * 法术流派与元素标记映射注册表
 * 管理从数据包加载的流派到元素标记的映射关系
 *
 * @author Love_U
 * @version 1.0.0
 */
public class SchoolElementMappingRegistry {

    /**
     * 流派到元素标记的映射
     * Key: 流派ID (ResourceLocation), Value: 该流派造成的元素标记类型列表
     */
    private static final Map<ResourceLocation, List<ElementType>> SCHOOL_ELEMENT_MAPPINGS = new HashMap<>();

    /**
     * 元素标记到流派的反向映射
     * Key: 元素类型, Value: 可以造成该标记的流派ID列表
     */
    private static final Map<ElementType, List<ResourceLocation>> ELEMENT_SCHOOL_MAPPINGS = new HashMap<>();

    /**
     * 注册所有从数据包加载的映射
     */
    public static void registerMappings() {
        com.legendarymage.legendarymagemod.ModLogger.spell("正在注册法术流派-元素标记映射...");

        // 清除之前的映射
        SCHOOL_ELEMENT_MAPPINGS.clear();
        ELEMENT_SCHOOL_MAPPINGS.clear();

        // 获取加载的数据
        Map<ResourceLocation, CustomSchoolData> loadedData = CustomSchoolDataLoader.getLoadedSchools();

        for (Map.Entry<ResourceLocation, CustomSchoolData> entry : loadedData.entrySet()) {
            ResourceLocation jsonId = entry.getKey();  // JSON 文件的 ID（如 legendarymage:blade_school）
            CustomSchoolData data = entry.getValue();

            // 检查是否有元素标记映射配置
            if (data.elementMarkMapping().isPresent()) {
                Map<String, String> mapping = data.elementMarkMapping().get();
                
                // 如果有 target_school_id，使用它；否则使用 JSON 文件 ID
                ResourceLocation targetSchoolId = jsonId;
                if (data.targetSchoolId().isPresent()) {
                    String targetId = data.targetSchoolId().get();
                    try {
                        targetSchoolId = ResourceLocation.parse(targetId);
                        com.legendarymage.legendarymagemod.ModLogger.spell("为外部模组流派配置元素标记映射：{} -> {}", targetId, jsonId);
                    } catch (Exception e) {
                        com.legendarymage.legendarymagemod.ModLogger.error("无效的 target_school_id: {} (在 {} 中)", targetId, jsonId);
                        continue;
                    }
                }
                
                registerMapping(targetSchoolId, mapping);
            }
        }

        LegendaryMage.LOGGER.info("法术流派-元素标记映射注册完成: 共 {} 个映射", SCHOOL_ELEMENT_MAPPINGS.size());
    }

    /**
     * 注册单个流派的元素标记映射
     *
     * @param schoolId 流派ID
     * @param mapping  映射配置 (Key: 伤害类型/条件, Value: 元素类型)
     */
    private static void registerMapping(ResourceLocation schoolId, Map<String, String> mapping) {
        List<ElementType> elementTypes = new ArrayList<>();

        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            String condition = entry.getKey();  // 如 "default", "critical", "dot" 等
            String elementTypeId = entry.getValue();  // 如 "poison", "fire", "ice" 等

            // 解析元素类型
            ElementType elementType = parseElementType(elementTypeId);
            if (elementType != null) {
                elementTypes.add(elementType);

                // 添加到反向映射
                ELEMENT_SCHOOL_MAPPINGS.computeIfAbsent(elementType, k -> new ArrayList<>()).add(schoolId);

                com.legendarymage.legendarymagemod.ModLogger.spellDebug("注册映射: {} [{}] -> {}", schoolId, condition, elementTypeId);
            } else {
                com.legendarymage.legendarymagemod.ModLogger.warn("未知的元素类型: {} (在 {} 的映射中)", elementTypeId, schoolId);
            }
        }

        if (!elementTypes.isEmpty()) {
            SCHOOL_ELEMENT_MAPPINGS.put(schoolId, elementTypes);
        }
    }

    /**
     * 解析元素类型字符串
     *
     * @param elementTypeId 元素类型ID
     * @return ElementType 枚举值，如果未知则返回null
     */
    private static ElementType parseElementType(String elementTypeId) {
        try {
            return ElementType.valueOf(elementTypeId.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 尝试通过ID匹配
            for (ElementType type : ElementType.values()) {
                if (type.getId().equalsIgnoreCase(elementTypeId)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * 获取指定流派造成的元素标记类型
     *
     * @param schoolId 流派ID
     * @return 元素标记类型列表，如果没有配置则返回空列表
     */
    public static List<ElementType> getElementMarksForSchool(ResourceLocation schoolId) {
        return SCHOOL_ELEMENT_MAPPINGS.getOrDefault(schoolId, Collections.emptyList());
    }

    /**
     * 获取可以造成指定元素标记的所有流派
     *
     * @param elementType 元素类型
     * @return 流派ID列表
     */
    public static List<ResourceLocation> getSchoolsForElementMark(ElementType elementType) {
        return ELEMENT_SCHOOL_MAPPINGS.getOrDefault(elementType, Collections.emptyList());
    }

    /**
     * 检查指定流派是否配置了元素标记映射
     *
     * @param schoolId 流派ID
     * @return 是否配置了映射
     */
    public static boolean hasMapping(ResourceLocation schoolId) {
        return SCHOOL_ELEMENT_MAPPINGS.containsKey(schoolId);
    }

    /**
     * 检查指定流派是否可以造成指定元素标记
     *
     * @param schoolId    流派ID
     * @param elementType 元素类型
     * @return 是否可以造成该标记
     */
    public static boolean canApplyElementMark(ResourceLocation schoolId, ElementType elementType) {
        List<ElementType> marks = SCHOOL_ELEMENT_MAPPINGS.get(schoolId);
        return marks != null && marks.contains(elementType);
    }

    /**
     * 获取所有已注册的映射
     *
     * @return 映射表
     */
    public static Map<ResourceLocation, List<ElementType>> getAllMappings() {
        return new HashMap<>(SCHOOL_ELEMENT_MAPPINGS);
    }

    /**
     * 清除所有映射
     */
    public static void clear() {
        SCHOOL_ELEMENT_MAPPINGS.clear();
        ELEMENT_SCHOOL_MAPPINGS.clear();
    }
}
