package com.legendarymage.legendarymagemod.school;

import com.legendarymage.legendarymagemod.LegendaryMage;
import io.redspace.ironsspellbooks.api.attribute.MagicPercentAttribute;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 元素流派属性注册类
 * 注册元素流派特有的法术强度和抗性属性
 * 
 * @author Love_U
 * @version 0.0.1
 */
@EventBusSubscriber(modid = LegendaryMage.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ElementAttributeRegistry {

    /**
     * 属性注册器
     */
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(
            Registries.ATTRIBUTE, LegendaryMage.MODID);

    /**
     * 元素法术强度属性
     * 影响元素流派法术的基础伤害
     */
    public static final DeferredHolder<Attribute, Attribute> ELEMENT_SPELL_POWER = ATTRIBUTES.register(
            "element_spell_power",
            () -> new MagicPercentAttribute("attribute.legendarymage.element_spell_power", 1.0D, -100.0D, 100.0D)
                    .setSyncable(true));

    /**
     * 元素魔法抗性属性
     * 减少受到的元素流派法术伤害
     */
    public static final DeferredHolder<Attribute, Attribute> ELEMENT_MAGIC_RESIST = ATTRIBUTES.register(
            "element_magic_resist",
            () -> new MagicPercentAttribute("attribute.legendarymage.element_magic_resist", 1.0D, -100.0D, 100.0D)
                    .setSyncable(true));

    /**
     * 注册属性到事件总线
     * 
     * @param eventBus 事件总线
     */
    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
        LegendaryMage.LOGGER.info("元素流派属性已注册");
    }

    /**
     * 为所有实体类型添加元素流派属性
     * 
     * @param event 实体属性修改事件
     */
    @SubscribeEvent
    public static void modifyEntityAttributes(EntityAttributeModificationEvent event) {
        event.getTypes().forEach(entityType -> {
            ATTRIBUTES.getEntries().forEach(attribute -> {
                event.add(entityType, attribute);
            });
        });
        LegendaryMage.LOGGER.info("元素流派属性已添加到实体");
    }
}
