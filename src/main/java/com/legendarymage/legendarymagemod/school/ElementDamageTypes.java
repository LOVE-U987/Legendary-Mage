package com.legendarymage.legendarymagemod.school;

import com.legendarymage.legendarymagemod.LegendaryMage;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;

/**
 * 元素流派伤害类型
 * 定义元素流派法术造成的伤害类型
 * 
 * @author Love_U
 * @version 0.0.2
 */
public class ElementDamageTypes {

    /**
     * 注册伤害类型
     * 
     * @param name 伤害类型名称
     * @return 伤害类型的资源键
     */
    public static ResourceKey<DamageType> register(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, 
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, name));
    }

    /**
     * 元素魔法伤害类型（通用）
     * 用于元素流派法术造成的伤害
     */
    public static final ResourceKey<DamageType> ELEMENT_MAGIC = register("element_magic");

    /**
     * 火焰元素伤害
     * 用于火系法术造成的伤害
     */
    public static final ResourceKey<DamageType> ELEMENT_FIRE = register("element_fire");

    /**
     * 冰霜元素伤害
     * 用于冰系法术造成的伤害
     */
    public static final ResourceKey<DamageType> ELEMENT_ICE = register("element_ice");

    /**
     * 雷电元素伤害
     * 用于雷系法术造成的伤害
     */
    public static final ResourceKey<DamageType> ELEMENT_LIGHTNING = register("element_lightning");

    /**
     * 初始化伤害类型
     * 在数据生成时调用
     * 
     * @param context 引导上下文
     */
    public static void bootstrap(BootstrapContext<DamageType> context) {
        // 通用元素魔法伤害
        context.register(ELEMENT_MAGIC, 
                new DamageType(ELEMENT_MAGIC.location().getPath(), 
                        DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0f));

        // 火焰元素伤害
        context.register(ELEMENT_FIRE, 
                new DamageType(ELEMENT_FIRE.location().getPath(), 
                        DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.1f));

        // 冰霜元素伤害
        context.register(ELEMENT_ICE, 
                new DamageType(ELEMENT_ICE.location().getPath(), 
                        DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.1f));

        // 雷电元素伤害
        context.register(ELEMENT_LIGHTNING, 
                new DamageType(ELEMENT_LIGHTNING.location().getPath(), 
                        DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.1f));
    }
}
