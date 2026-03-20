package com.legendarymage.legendarymagemod.data;

import com.legendarymage.legendarymagemod.LegendaryMage;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义法术流派属性系统
 * 为自定义流派动态创建法术强度和魔法抗性属性
 *
 * @author Love_U
 * @version 1.0.0
 */
public class CustomSchoolAttributes {

    /**
     * 属性注册器
     */
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(
            BuiltInRegistries.ATTRIBUTE.key(), LegendaryMage.MODID);

    /**
     * 法术强度属性映射
     * Key: 流派ID, Value: 属性Holder
     */
    private static final Map<ResourceLocation, Holder<Attribute>> SPELL_POWER_ATTRIBUTES = new HashMap<>();

    /**
     * 魔法抗性属性映射
     * Key: 流派ID, Value: 属性Holder
     */
    private static final Map<ResourceLocation, Holder<Attribute>> MAGIC_RESIST_ATTRIBUTES = new HashMap<>();

    /**
     * 注册属性到事件总线
     *
     * @param eventBus 事件总线
     */
    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }

    /**
     * 获取或创建法术强度属性
     *
     * @param schoolId 流派ID
     * @return 属性Holder
     */
    public static Holder<Attribute> getOrCreateSpellPowerAttribute(ResourceLocation schoolId) {
        // 检查是否已存在
        if (SPELL_POWER_ATTRIBUTES.containsKey(schoolId)) {
            return SPELL_POWER_ATTRIBUTES.get(schoolId);
        }

        // 创建属性ID
        String attributeId = schoolId.getPath() + "_spell_power";
        ResourceLocation fullId = ResourceLocation.fromNamespaceAndPath(schoolId.getNamespace(), attributeId);

        // 创建属性
        Attribute attribute = new RangedAttribute(
                "attribute." + schoolId.getNamespace() + "." + attributeId,
                1.0,  // 默认值
                0.0,  // 最小值
                100.0 // 最大值
        ).setSyncable(true);

        // 注册属性
        DeferredHolder<Attribute, Attribute> holder = ATTRIBUTES.register(attributeId, () -> attribute);

        // 存储映射
        SPELL_POWER_ATTRIBUTES.put(schoolId, holder);

        LegendaryMage.LOGGER.debug("创建法术强度属性: {} for {}", attributeId, schoolId);

        return holder;
    }

    /**
     * 获取或创建魔法抗性属性
     *
     * @param schoolId 流派ID
     * @return 属性Holder
     */
    public static Holder<Attribute> getOrCreateMagicResistAttribute(ResourceLocation schoolId) {
        // 检查是否已存在
        if (MAGIC_RESIST_ATTRIBUTES.containsKey(schoolId)) {
            return MAGIC_RESIST_ATTRIBUTES.get(schoolId);
        }

        // 创建属性ID
        String attributeId = schoolId.getPath() + "_magic_resist";
        ResourceLocation fullId = ResourceLocation.fromNamespaceAndPath(schoolId.getNamespace(), attributeId);

        // 创建属性
        Attribute attribute = new RangedAttribute(
                "attribute." + schoolId.getNamespace() + "." + attributeId,
                0.0,  // 默认值
                0.0,  // 最小值
                100.0 // 最大值
        ).setSyncable(true);

        // 注册属性
        DeferredHolder<Attribute, Attribute> holder = ATTRIBUTES.register(attributeId, () -> attribute);

        // 存储映射
        MAGIC_RESIST_ATTRIBUTES.put(schoolId, holder);

        LegendaryMage.LOGGER.debug("创建魔法抗性属性: {} for {}", attributeId, schoolId);

        return holder;
    }

    /**
     * 获取法术强度属性
     *
     * @param schoolId 流派ID
     * @return 属性Holder，如果不存在则返回null
     */
    public static Holder<Attribute> getSpellPowerAttribute(ResourceLocation schoolId) {
        return SPELL_POWER_ATTRIBUTES.get(schoolId);
    }

    /**
     * 获取魔法抗性属性
     *
     * @param schoolId 流派ID
     * @return 属性Holder，如果不存在则返回null
     */
    public static Holder<Attribute> getMagicResistAttribute(ResourceLocation schoolId) {
        return MAGIC_RESIST_ATTRIBUTES.get(schoolId);
    }

    /**
     * 检查是否存在法术强度属性
     *
     * @param schoolId 流派ID
     * @return 是否存在
     */
    public static boolean hasSpellPowerAttribute(ResourceLocation schoolId) {
        return SPELL_POWER_ATTRIBUTES.containsKey(schoolId);
    }

    /**
     * 检查是否存在魔法抗性属性
     *
     * @param schoolId 流派ID
     * @return 是否存在
     */
    public static boolean hasMagicResistAttribute(ResourceLocation schoolId) {
        return MAGIC_RESIST_ATTRIBUTES.containsKey(schoolId);
    }

    /**
     * 清除所有属性映射
     */
    public static void clear() {
        SPELL_POWER_ATTRIBUTES.clear();
        MAGIC_RESIST_ATTRIBUTES.clear();
    }
}
