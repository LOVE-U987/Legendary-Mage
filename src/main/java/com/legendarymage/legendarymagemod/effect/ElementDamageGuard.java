package com.legendarymage.legendarymagemod.effect;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * 元素伤害剔除守卫
 * 元素系统对创造模式与旁观模式玩家的伤害剔除：
 * - 创造/旁观玩家不承受元素 buff 的直接伤害（灼烧、毒素、闪电链、神圣/回响打击、元素反应、毒爆等）
 * - 挂载会降低最大生命值的元素标记时，剔除属性修饰符 clamp 造成的扣血
 *
 * 【根因背景】
 * 原版 Player.hurt 对创造玩家有 abilities.invulnerable 免疫（magic/onFire/lightningBolt 均不在
 * BYPASSES_INVULNERABILITY 标签内），但 LivingEntity.onAttributeUpdated 在 MAX_HEALTH 修饰符挂上时
 * 会把当前血量直接 clamp 到新上限——该路径绕过 hurt 免疫，导致创造玩家挂火系标记时掉血。
 *
 * @author Love_U
 * @version 1.0.0
 */
public final class ElementDamageGuard {

    /**
     * 工具类，禁止实例化
     */
    private ElementDamageGuard() {
    }

    /**
     * 判断实体是否应被剔除元素伤害
     * 创造模式与旁观模式玩家返回 true
     *
     * @param entity 待检查实体
     * @return true 表示应跳过元素伤害
     */
    public static boolean shouldSkipDamage(LivingEntity entity) {
        return entity instanceof Player player && (player.isCreative() || player.isSpectator());
    }

    /**
     * 对实体施加效果，并对创造/旁观模式玩家剔除 MAX_HEALTH 修饰符 clamp 造成的扣血
     * 挂载降低最大生命值的标记（如火系标记、瘟疫、烈焰）时，原版会把当前血量 clamp 到新上限
     * （该路径绕过 hurt 免疫），这里通过"记录挂前血量 → 挂载 → 恢复"在同一 tick 内补回被扣的血
     *
     * @param target   目标实体
     * @param instance 效果实例
     * @return 是否成功施加效果
     */
    public static boolean addEffectSafe(LivingEntity target, MobEffectInstance instance) {
        // 非创造/旁观实体：走原版施加逻辑
        if (!shouldSkipDamage(target)) {
            return target.addEffect(instance);
        }

        // 创造/旁观玩家：记录挂前血量
        float prevHealth = target.getHealth();
        boolean added = target.addEffect(instance);

        // 剔除 MAX_HEALTH 修饰符 clamp 造成的扣血（仅在被 clamp 下降时补回，避免误加血）
        if (added && target.getHealth() < prevHealth) {
            target.setHealth(prevHealth);
        }
        return added;
    }
}
