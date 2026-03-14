package com.legendarymage.legendarymagemod.entity;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.spell.LivingIceSculptureEntity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 实体注册类
 * 负责注册模组中的所有实体
 * 
 * @author Love_U
 * @version 0.0.1
 */
public class ModEntities {

    /**
     * 实体注册器
     */
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(
            BuiltInRegistries.ENTITY_TYPE,
            LegendaryMage.MODID
    );

    /**
     * 活体冰雕生物实体
     */
    public static final DeferredHolder<EntityType<?>, EntityType<LivingIceSculptureEntity>> ICE_SCULPTURE = ENTITIES.register(
            "ice_sculpture",
            () -> EntityType.Builder.<LivingIceSculptureEntity>of(LivingIceSculptureEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.8f)  // 玩家尺寸
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .build("ice_sculpture")
    );

    /**
     * 注册实体到事件总线
     * 
     * @param eventBus 事件总线
     */
    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
