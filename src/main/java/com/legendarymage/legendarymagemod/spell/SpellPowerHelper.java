package com.legendarymage.legendarymagemod.spell;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.world.entity.LivingEntity;

/**
 * 法术强度计算辅助类
 * 提供修正后的法术强度计算方法
 * 
 * 问题背景：
 * Iron's Spellbooks 的 AbstractSpell.getSpellPower() 方法返回的值包含了
 * (baseSpellPower + spellPowerPerLevel * (spellLevel - 1)) * entitySpellPowerModifier
 * 这导致在计算法术强度加成时会出现错误（多乘了基础法术强度）
 * 
 * 本辅助类提供正确的方法获取基础法术强度属性值
 * 
 * @author Love_U
 * @version 1.0.0
 */
public class SpellPowerHelper {

    /**
     * 基础法术强度属性默认值
     * Iron's Spellbooks 的 SPELL_POWER 属性默认值为 1.0（100%）
     */
    public static final float BASE_SPELL_POWER_DEFAULT = 1.0f;

    /**
     * 获取实体的基础法术强度属性值（不包含法术等级加成）
     * 这是修正后的法术强度获取方法，用于正确计算法术强度加成
     *
     * 返回值 = 实体当前的法术强度属性值（默认1.0 = 100%，每+0.1 = +10%加成）
     *
     * @param entity 施法实体
     * @return 基础法术强度属性值（默认1.0 = 100%）
     */
    public static float getBaseSpellPowerAttribute(LivingEntity entity) {
        if (entity == null) {
            return BASE_SPELL_POWER_DEFAULT;
        }
        float value = (float) entity.getAttributeValue(AttributeRegistry.SPELL_POWER);
        // 确保返回值至少为基础值的一半，防止负加成导致异常
        return Math.max(value, BASE_SPELL_POWER_DEFAULT * 0.1f); // 最低为0.1（10%）
    }

    /**
     * 获取法术强度加成倍数
     * 返回值 = (法术强度属性值 / 1.0) = 法术强度属性值
     *
     * 例如：
     * - 法术强度属性值1.0（无加成 = 100%）-> 返回1.0
     * - 法术强度属性值1.15（+15%加成 = 115%）-> 返回1.15
     * - 法术强度属性值2.0（+100%加成 = 200%）-> 返回2.0
     *
     * @param entity 施法实体
     * @return 法术强度加成倍数
     */
    public static float getSpellPowerMultiplier(LivingEntity entity) {
        return getBaseSpellPowerAttribute(entity) / BASE_SPELL_POWER_DEFAULT;
    }

    /**
     * 计算法术强度百分比加成
     * 返回值 = ((法术强度属性值 - 1.0) / 1.0) * 100%
     *
     * 例如：
     * - 法术强度属性值1.0（无加成 = 100%）-> 返回0.0（0%）
     * - 法术强度属性值1.15（+15%加成 = 115%）-> 返回0.15（15%）
     * - 法术强度属性值2.0（+100%加成 = 200%）-> 返回1.0（100%）
     *
     * @param entity 施法实体
     * @return 法术强度百分比加成（0.0 = 0%，1.0 = 100%）
     */
    public static float getSpellPowerBonusPercent(LivingEntity entity) {
        return (getBaseSpellPowerAttribute(entity) - BASE_SPELL_POWER_DEFAULT) / BASE_SPELL_POWER_DEFAULT;
    }

    /**
     * 应用法术强度加成到基础值
     * 计算公式：结果 = 基础值 * 法术强度属性值
     *
     * @param baseValue 基础值
     * @param entity 施法实体
     * @return 应用法术强度加成后的值
     */
    public static float applySpellPowerBonus(float baseValue, LivingEntity entity) {
        return baseValue * getSpellPowerMultiplier(entity);
    }

    /**
     * 应用法术强度加成到基础值（带上限）
     * 计算公式：结果 = min(基础值 * 法术强度属性值, maxValue)
     *
     * @param baseValue 基础值
     * @param entity 施法实体
     * @param maxValue 最大值
     * @return 应用法术强度加成后的值（不超过最大值）
     */
    public static float applySpellPowerBonusWithCap(float baseValue, LivingEntity entity, float maxValue) {
        return Math.min(baseValue * getSpellPowerMultiplier(entity), maxValue);
    }
}
