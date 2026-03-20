package com.legendarymage.legendarymagemod.entity;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.entity.spell.FocusedIceConeProjectile;
import com.legendarymage.legendarymagemod.entity.spell.GiantSnowballEntity;
import com.legendarymage.legendarymagemod.entity.spell.IceExplosionConeProjectile;
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
     * 冰爆锥投射物实体
     */
    public static final DeferredHolder<EntityType<?>, EntityType<IceExplosionConeProjectile>> ICE_EXPLOSION_CONE = ENTITIES.register(
            "ice_explosion_cone",
            () -> EntityType.Builder.<IceExplosionConeProjectile>of(IceExplosionConeProjectile::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)  // 投射物尺寸
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("ice_explosion_cone")
    );

    /**
     * 聚能冰锥投射物实体
     */
    public static final DeferredHolder<EntityType<?>, EntityType<FocusedIceConeProjectile>> FOCUSED_ICE_CONE = ENTITIES.register(
            "focused_ice_cone",
            () -> EntityType.Builder.<FocusedIceConeProjectile>of(FocusedIceConeProjectile::new, MobCategory.MISC)
                    .sized(0.4f, 0.4f)  // 投射物尺寸（更集中）
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("focused_ice_cone")
    );

    /**
     * 巨雪球实体
     */
    public static final DeferredHolder<EntityType<?>, EntityType<GiantSnowballEntity>> GIANT_SNOWBALL = ENTITIES.register(
            "giant_snowball",
            () -> EntityType.Builder.<GiantSnowballEntity>of(GiantSnowballEntity::new, MobCategory.MISC)
                    .sized(1.0f, 1.0f)  // 基础尺寸，会根据缩放值调整
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("giant_snowball")
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
