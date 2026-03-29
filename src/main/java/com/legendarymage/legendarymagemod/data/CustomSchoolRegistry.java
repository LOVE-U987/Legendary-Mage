package com.legendarymage.legendarymagemod.data;

import com.legendarymage.legendarymagemod.LegendaryMage;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义法术流派注册系统
 * 负责将数据包加载的自定义流派注册到游戏中
 *
 * @author Love_U
 * @version 1.0.0
 */
public class CustomSchoolRegistry {

    /**
     * 已注册的自定义流派映射
     */
    private static final Map<ResourceLocation, SchoolType> REGISTERED_SCHOOLS = new HashMap<>();

    /**
     * 流派属性映射
     */
    private static final Map<ResourceLocation, CustomSchoolData> SCHOOL_DATA_MAP = new HashMap<>();

    /**
     * 注册所有从数据包加载的自定义流派
     */
    public static void registerLoadedSchools() {
        LegendaryMage.LOGGER.info("正在注册自定义法术流派...");

        // 清除之前的注册
        REGISTERED_SCHOOLS.clear();
        SCHOOL_DATA_MAP.clear();

        // 获取加载的数据
        Map<ResourceLocation, CustomSchoolData> loadedData = CustomSchoolDataLoader.getLoadedSchools();

        for (Map.Entry<ResourceLocation, CustomSchoolData> entry : loadedData.entrySet()) {
            ResourceLocation id = entry.getKey();
            CustomSchoolData data = entry.getValue();

            try {
                // 创建并注册流派
                SchoolType schoolType = createSchoolType(id, data);
                REGISTERED_SCHOOLS.put(id, schoolType);
                SCHOOL_DATA_MAP.put(id, data);

                LegendaryMage.LOGGER.info("已注册自定义法术流派: {} - {}", id, data.name());
            } catch (Exception e) {
                LegendaryMage.LOGGER.error("注册自定义法术流派失败: {}", id, e);
            }
        }

        LegendaryMage.LOGGER.info("自定义法术流派注册完成: 共 {} 个", REGISTERED_SCHOOLS.size());

        // 注册流派-元素标记映射
        SchoolElementMappingRegistry.registerMappings();
    }

    /**
     * 创建 SchoolType 实例
     *
     * @param id   流派 ID
     * @param data 流派数据
     * @return SchoolType 实例
     */
    private static SchoolType createSchoolType(ResourceLocation id, CustomSchoolData data) {
        // 创建流派资源位置
        ResourceLocation schoolResource = id;

        // 创建焦点标签
        TagKey<Item> focusTag = ItemTags.create(ResourceLocation.fromNamespaceAndPath(
                id.getNamespace(), "focuses/" + id.getPath()));

        // 创建显示名称组件
        // 如果有 target_school_id，说明是外部模组流派，直接使用 JSON 中的 name
        // 否则使用翻译键（兼容自定义流派）
        Component displayName;
        if (data.targetSchoolId().isPresent()) {
            // 外部模组流派：直接使用 JSON 中的名称
            displayName = Component.literal(data.name())
                    .withStyle(Style.EMPTY.withColor(data.color()));
            LegendaryMage.LOGGER.debug("为外部模组流派创建显示名称：{} -> {}", id, data.name());
        } else {
            // 自定义流派：使用翻译键
            String translationKey = "school." + id.getNamespace() + "." + id.getPath();
            displayName = Component.translatable(translationKey)
                    .withStyle(Style.EMPTY.withColor(data.color()));
            LegendaryMage.LOGGER.debug("为自定义流派创建显示名称：{} -> {}", id, translationKey);
        }

        // 获取或创建法术强度属性
        Holder<net.minecraft.world.entity.ai.attributes.Attribute> spellPowerAttribute =
                CustomSchoolAttributes.getOrCreateSpellPowerAttribute(id);

        // 获取或创建魔法抗性属性
        Holder<net.minecraft.world.entity.ai.attributes.Attribute> magicResistAttribute =
                CustomSchoolAttributes.getOrCreateMagicResistAttribute(id);

        // 创建 SchoolType
        // 在 1.21 中，SoundEvent 需要包装为 Holder
        Holder<SoundEvent> castSoundHolder = net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.AMETHYST_BLOCK_CHIME);

        // 创建 SchoolType
        return new SchoolType(
                schoolResource,
                focusTag,
                displayName,
                spellPowerAttribute,
                magicResistAttribute,
                castSoundHolder,
                null  // 伤害类型，可以后续扩展
        );
    }

    /**
     * 获取已注册的自定义流派
     *
     * @param id 流派ID
     * @return SchoolType 实例，如果不存在则返回null
     */
    public static SchoolType getSchool(ResourceLocation id) {
        return REGISTERED_SCHOOLS.get(id);
    }

    /**
     * 获取流派数据
     *
     * @param id 流派ID
     * @return 流派数据，如果不存在则返回null
     */
    public static CustomSchoolData getSchoolData(ResourceLocation id) {
        return SCHOOL_DATA_MAP.get(id);
    }

    /**
     * 获取所有已注册的自定义流派
     *
     * @return 流派映射
     */
    public static Map<ResourceLocation, SchoolType> getAllSchools() {
        return new HashMap<>(REGISTERED_SCHOOLS);
    }

    /**
     * 检查是否已注册指定ID的流派
     *
     * @param id 流派ID
     * @return 是否已注册
     */
    public static boolean isRegistered(ResourceLocation id) {
        return REGISTERED_SCHOOLS.containsKey(id);
    }

    /**
     * 获取流派的属性修饰符
     *
     * @param id 流派ID
     * @return 属性修饰符，如果不存在则返回null
     */
    public static CustomSchoolData.AttributeModifiers getAttributeModifiers(ResourceLocation id) {
        CustomSchoolData data = SCHOOL_DATA_MAP.get(id);
        if (data != null && data.attributeModifiers().isPresent()) {
            return data.attributeModifiers().get();
        }
        return null;
    }

    /**
     * 获取流派的法术统计
     *
     * @param id 流派ID
     * @return 法术统计，如果不存在则返回null
     */
    public static CustomSchoolData.SpellStats getSpellStats(ResourceLocation id) {
        CustomSchoolData data = SCHOOL_DATA_MAP.get(id);
        if (data != null && data.spellStats().isPresent()) {
            return data.spellStats().get();
        }
        return null;
    }

    /**
     * 清除所有注册的流派
     */
    public static void clear() {
        REGISTERED_SCHOOLS.clear();
        SCHOOL_DATA_MAP.clear();
    }
}
