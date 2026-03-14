package com.legendarymage.legendarymagemod.school;

import com.legendarymage.legendarymagemod.LegendaryMage;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Supplier;

/**
 * 元素流派注册类
 * 负责注册元素流派及其相关属性
 * 
 * 特性：该流派法术造成交替性元素伤害时，按对应流派的法术强度加成单独计算
 * 
 * @author Love_U
 * @version 0.0.1
 */
public class ElementSchoolRegistry {

    /**
     * 元素流派注册表键
     */
    public static final ResourceKey<Registry<SchoolType>> ELEMENT_SCHOOL_REGISTRY_KEY = ResourceKey.createRegistryKey(
            ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "element_schools"));

    /**
     * 元素流派注册器
     */
    private static final DeferredRegister<SchoolType> ELEMENT_SCHOOLS = DeferredRegister.create(
            SchoolRegistry.SCHOOL_REGISTRY_KEY, LegendaryMage.MODID);

    /**
     * 元素流派注册表
     */
    public static final Registry<SchoolType> REGISTRY = new RegistryBuilder<SchoolType>(ELEMENT_SCHOOL_REGISTRY_KEY)
            .create();

    /**
     * 元素流派资源位置
     */
    public static final ResourceLocation ELEMENT_RESOURCE = ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "element");

    /**
     * 元素流派标签
     */
    public static final TagKey<Item> ELEMENT_FOCUS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "focuses/element"));

    /**
     * 元素流派音效注册
     */
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(
            BuiltInRegistries.SOUND_EVENT.key(), LegendaryMage.MODID);

    /**
     * 元素流派施法音效
     */
    public static final DeferredHolder<SoundEvent, SoundEvent> ELEMENT_CAST = SOUND_EVENTS.register(
            "element_cast",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "element_cast")));

    /**
     * 元素流派实例
     * 使用紫色渐变作为元素流派的代表色
     */
    public static final Supplier<SchoolType> ELEMENT = ELEMENT_SCHOOLS.register("element",
            () -> new SchoolType(
                    ELEMENT_RESOURCE,
                    ELEMENT_FOCUS,
                    Component.translatable("school.legendarymage.element")
                            .withStyle(Style.EMPTY.withColor(0x9b59b6)), // 紫色
                    ElementAttributeRegistry.ELEMENT_SPELL_POWER,
                    ElementAttributeRegistry.ELEMENT_MAGIC_RESIST,
                    ELEMENT_CAST,
                    ElementDamageTypes.ELEMENT_MAGIC
            ));

    /**
     * 注册元素流派到事件总线
     * 
     * @param eventBus 事件总线
     */
    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
        ELEMENT_SCHOOLS.register(eventBus);
        LegendaryMage.LOGGER.info("元素流派已注册");
    }

    /**
     * 注册元素流派注册表
     * 
     * @param event 新注册表事件
     */
    public static void registerRegistry(NewRegistryEvent event) {
        event.register(REGISTRY);
    }
}
