package com.legendarymage.legendarymagemod.element;

import com.legendarymage.legendarymagemod.effect.*;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

/**
 * 元素类型枚举
 * 定义所有可用的元素类型及其与铁魔法流派的对应关系
 * 
 * @author Love_U
 * @version 0.0.1
 */
public enum ElementType {

    /**
     * 血系 - 对应黑暗异常
     */
    BLOOD("blood", SchoolRegistry.BLOOD, 0x8B0000, ModEffects.BLOOD_MARK),

    /**
     * 神圣系 - 对应光明异常
     */
    HOLY("holy", SchoolRegistry.HOLY, 0xFFD700, ModEffects.HOLY_MARK),

    /**
     * 邪术 - 对应邪术异常
     */
    ELDRITCH("eldritch", SchoolRegistry.ELDRITCH, 0x4B0082, ModEffects.ELDRITCH_MARK),

    /**
     * 毒系 - 对应毒素异常
     */
    POISON("poison", SchoolRegistry.NATURE, 0x32CD32, ModEffects.POISON_MARK),

    /**
     * 火系 - 对应火焰异常
     */
    FIRE("fire", SchoolRegistry.FIRE, 0xFF4500, ModEffects.FIRE_MARK),

    /**
     * 冰系 - 对应冰冻异常
     */
    ICE("ice", SchoolRegistry.ICE, 0x00CED1, ModEffects.ICE_MARK),

    /**
     * 雷系 - 对应雷电异常
     */
    LIGHTNING("lightning", SchoolRegistry.LIGHTNING, 0x9400D3, ModEffects.LIGHTNING_MARK),

    /**
     * 末影 - 对应末影异常
     */
    ENDER("ender", SchoolRegistry.ENDER, 0x9932CC, ModEffects.ENDER_MARK);

    /**
     * 元素ID
     */
    private final String id;

    /**
     * 对应的铁魔法流派
     */
    private final Supplier<SchoolType> schoolType;

    /**
     * 元素颜色（用于粒子效果等）
     */
    private final int color;

    /**
     * 对应的标记效果
     */
    private final DeferredHolder<MobEffect, ? extends ElementMarkEffect> markEffect;

    /**
     * 构造函数
     *
     * @param id        元素ID
     * @param schoolType 对应的铁魔法流派
     * @param color     元素颜色
     * @param markEffect 对应的标记效果
     */
    ElementType(String id, Supplier<SchoolType> schoolType, int color, 
                DeferredHolder<MobEffect, ? extends ElementMarkEffect> markEffect) {
        this.id = id;
        this.schoolType = schoolType;
        this.color = color;
        this.markEffect = markEffect;
    }

    /**
     * 获取元素ID
     *
     * @return 元素ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获取对应的铁魔法流派
     *
     * @return 铁魔法流派
     */
    public SchoolType getSchoolType() {
        return schoolType.get();
    }

    /**
     * 获取元素颜色
     *
     * @return 元素颜色
     */
    public int getColor() {
        return color;
    }

    /**
     * 获取对应的标记效果
     *
     * @return 标记效果
     */
    public MobEffect getMarkEffect() {
        return markEffect.get();
    }

    /**
     * 根据铁魔法流派获取对应的元素类型
     *
     * @param schoolType 铁魔法流派
     * @return 对应的元素类型，如果没有找到则返回null
     */
    public static ElementType fromSchoolType(SchoolType schoolType) {
        for (ElementType type : values()) {
            if (type.getSchoolType().equals(schoolType)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据ID获取元素类型
     *
     * @param id 元素ID
     * @return 对应的元素类型，如果没有找到则返回null
     */
    public static ElementType fromId(String id) {
        for (ElementType type : values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }
}
