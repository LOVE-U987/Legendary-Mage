package com.legendarymage.legendarymagemod.api.element;

import com.legendarymage.legendarymagemod.element.ElementReactionManager;
import com.legendarymage.legendarymagemod.element.ElementType;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

/**
 * 元素反应 API 门面
 * 向其他 addon 提供稳定的元素标记施加、清除与查询能力
 * 所有方法均委托给内部 {@link ElementReactionManager} 实现
 *
 * @author Love_U
 * @version 1.0.0
 */
public final class ElementReactionApi {

    private ElementReactionApi() {
        // 禁止实例化
    }

    /**
     * 处理法术伤害事件
     * 当实体受到法术伤害时调用，用于施加或升级元素标记 Buff
     *
     * @param serverLevel 服务器世界
     * @param target      被攻击的目标
     * @param attacker    攻击者
     * @param schoolType  法术流派
     * @param damage      伤害值
     */
    public static void onSpellDamage(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                     SchoolType schoolType, float damage) {
        ElementReactionManager.onSpellDamage(serverLevel, target, attacker, schoolType, damage);
    }

    /**
     * 处理元素伤害事件
     * 当实体受到特定元素攻击时调用，用于尝试施加或更新对应元素标记（75%概率）
     *
     * @param serverLevel 服务器世界
     * @param target      被攻击的目标
     * @param attacker    攻击者
     * @param elementType 元素类型
     * @param damage      伤害值
     */
    public static void onElementDamage(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                       ElementType elementType, float damage) {
        ElementReactionManager.onElementDamage(serverLevel, target, attacker, elementType, damage);
    }

    /**
     * 手动施加元素标记（用于命令或其他系统调用）
     *
     * @param serverLevel 服务器世界
     * @param target      目标实体
     * @param applier     施加者
     * @param elementType 元素类型
     * @param markLevel   标记等级（1-5）
     * @return 是否成功施加
     */
    public static boolean applyMark(ServerLevel serverLevel, LivingEntity target, LivingEntity applier,
                                    ElementType elementType, int markLevel) {
        return ElementReactionManager.applyMark(serverLevel, target, applier, elementType, markLevel);
    }

    /**
     * 清除实体的所有元素标记
     *
     * @param serverLevel 服务器世界
     * @param target      目标实体
     */
    public static void clearAllMarks(ServerLevel serverLevel, LivingEntity target) {
        ElementReactionManager.clearAllMarks(serverLevel, target);
    }

    /**
     * 清除实体的指定类型元素标记
     *
     * @param serverLevel 服务器世界
     * @param target      目标实体
     * @param elementType 元素类型
     */
    public static void clearMark(ServerLevel serverLevel, LivingEntity target, ElementType elementType) {
        ElementReactionManager.clearMark(serverLevel, target, elementType);
    }

    /**
     * 获取实体的元素标记信息
     *
     * @param serverLevel 服务器世界
     * @param target      目标实体
     * @return 标记信息字符串
     */
    public static String getMarkInfo(ServerLevel serverLevel, LivingEntity target) {
        return ElementReactionManager.getMarkInfo(serverLevel, target);
    }

    /**
     * 获取实体所有元素标记的列表
     *
     * @param target 目标实体
     * @return 元素类型列表
     */
    public static List<ElementType> getAllMarks(LivingEntity target) {
        return ElementReactionManager.getAllMarks(target);
    }
}
