package com.legendarymage.legendarymagemod.element;

import com.legendarymage.legendarymagemod.LegendaryMage;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * 元素标记数据工具类
 * 负责管理元素异常 Buff 的施法者记录、触发冷却、流派强度与伤害计算，
 * 以及雷电异常的攻击名单排除（施法者、队友、双方召唤物）。
 *
 * 数据通过实体持久化 NBT 存储，随实体生命周期自动清理：
 * - legendarymage:element_casters   : 元素ID -> 施法者UUID
 * - legendarymage:element_cooldowns : 元素ID -> 上次触发时刻（游戏tick）
 *
 * @author Love_U
 * @version 1.0.0
 */
public final class ElementMarkData {

    /**
     * NBT 键：施法者记录
     */
    private static final String NBT_CASTERS = LegendaryMage.MODID + ":element_casters";

    /**
     * NBT 键：冷却记录
     */
    private static final String NBT_COOLDOWNS = LegendaryMage.MODID + ":element_cooldowns";

    private ElementMarkData() {
        // 禁止实例化
    }

    // ==================== 施法者记录 ====================

    /**
     * 记录某元素标记的施法者（用于多人游戏伤害合并计算）
     *
     * @param target      被标记的目标
     * @param elementType 元素类型
     * @param caster      施法者
     */
    public static void setCaster(LivingEntity target, ElementType elementType, LivingEntity caster) {
        if (target == null || elementType == null || caster == null) {
            return;
        }
        CompoundTag data = target.getPersistentData();
        CompoundTag casters = data.getCompound(NBT_CASTERS);
        casters.putString(elementType.getId(), caster.getStringUUID());
        data.put(NBT_CASTERS, casters);
    }

    /**
     * 获取某元素标记的施法者UUID
     *
     * @param target      被标记的目标
     * @param elementType 元素类型
     * @return 施法者UUID，可能为null
     */
    public static UUID getCasterUuid(LivingEntity target, ElementType elementType) {
        if (target == null || elementType == null) {
            return null;
        }
        CompoundTag casters = target.getPersistentData().getCompound(NBT_CASTERS);
        String uuid = casters.getString(elementType.getId());
        if (uuid.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 获取某元素标记的施法者实体（服务器端）
     *
     * @param serverLevel 服务器世界
     * @param target      被标记的目标
     * @param elementType 元素类型
     * @return 施法者实体，可能为null（已下线/离线/环境伤害）
     */
    public static LivingEntity getCaster(ServerLevel serverLevel, LivingEntity target, ElementType elementType) {
        UUID uuid = getCasterUuid(target, elementType);
        if (uuid == null) {
            return null;
        }
        if (serverLevel.getEntity(uuid) instanceof LivingEntity living) {
            return living;
        }
        return null;
    }

    // ==================== 触发冷却 ====================

    /**
     * 判断元素效果触发冷却是否就绪
     *
     * @param target      目标实体
     * @param elementType 元素类型
     * @param cdTicks     冷却时长（tick）
     * @return 是否就绪（冷却已过或从未触发过）
     */
    public static boolean isCooldownReady(LivingEntity target, ElementType elementType, int cdTicks) {
        CompoundTag cooldowns = target.getPersistentData().getCompound(NBT_COOLDOWNS);
        String key = elementType.getId();
        // 从未触发过，视为就绪
        if (!cooldowns.contains(key)) {
            return true;
        }
        long last = cooldowns.getLong(key);
        return target.level().getGameTime() - last >= cdTicks;
    }

    /**
     * 记录元素效果触发时刻（开始冷却）
     *
     * @param target      目标实体
     * @param elementType 元素类型
     */
    public static void markCooldown(LivingEntity target, ElementType elementType) {
        CompoundTag data = target.getPersistentData();
        CompoundTag cooldowns = data.getCompound(NBT_COOLDOWNS);
        cooldowns.putLong(elementType.getId(), target.level().getGameTime());
        data.put(NBT_COOLDOWNS, cooldowns);
    }

    // ==================== 等级与流派强度 ====================

    /**
     * 获取实体某元素异常的等级（1-5），无标记返回0
     *
     * @param target      目标实体
     * @param elementType 元素类型
     * @return 标记等级（1-5）或 0
     */
    public static int getMarkLevel(LivingEntity target, ElementType elementType) {
        if (target == null || elementType == null) {
            return 0;
        }
        MobEffect effect = elementType.getMarkEffect();
        if (effect == null) {
            return 0;
        }
        Holder<MobEffect> holder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect);
        MobEffectInstance instance = target.getEffect(holder);
        return instance != null ? instance.getAmplifier() + 1 : 0;
    }

    /**
     * 获取元素对应的法术流派强度属性
     *
     * @param elementType 元素类型
     * @return 对应的流派强度属性
     */
    public static Holder<Attribute> getSchoolPowerAttribute(ElementType elementType) {
        switch (elementType) {
            case FIRE:
                return AttributeRegistry.FIRE_SPELL_POWER;
            case ICE:
                return AttributeRegistry.ICE_SPELL_POWER;
            case LIGHTNING:
                return AttributeRegistry.LIGHTNING_SPELL_POWER;
            case POISON:
                return AttributeRegistry.NATURE_SPELL_POWER;
            case HOLY:
                return AttributeRegistry.HOLY_SPELL_POWER;
            case BLOOD:
                return AttributeRegistry.BLOOD_SPELL_POWER;
            case ENDER:
                return AttributeRegistry.ENDER_SPELL_POWER;
            case ELDRITCH:
            default:
                return AttributeRegistry.ELDRITCH_SPELL_POWER;
        }
    }

    /**
     * 获取实体对应法术流派的强度值
     *
     * 与铁魔法的伤害公式保持一致（见 AbstractSpell#getEntityPowerMultiplier）：
     * 有效流派强度 = 通用法术强度(SPELL_POWER) × 流派法术强度(如 ENDER_SPELL_POWER)
     * 这样元素异常伤害才能享受玩家的法术强度加成
     *
     * @param entity      实体
     * @param elementType 元素类型
     * @return 流派强度值（无属性时按基础值 1.0）
     */
    public static double getSchoolPower(LivingEntity entity, ElementType elementType) {
        if (entity == null) {
            return 1.0;
        }
        double power = 1.0;

        // 流派法术强度（如 末影 → ENDER_SPELL_POWER）
        Holder<Attribute> schoolAttribute = getSchoolPowerAttribute(elementType);
        if (entity.getAttributes().hasAttribute(schoolAttribute)) {
            power *= entity.getAttributeValue(schoolAttribute);
        }

        // 通用法术强度（法术强度加成的重要来源）
        if (entity.getAttributes().hasAttribute(AttributeRegistry.SPELL_POWER)) {
            power *= entity.getAttributeValue(AttributeRegistry.SPELL_POWER);
        }

        return power;
    }

    /**
     * 计算元素异常伤害 = buff等级 × 对应法术流派强度
     *
     * 多人游戏规则：不同玩家攻击形成的异常，伤害由双方玩家该流派强度加在一起共同计算
     * （记录的施法者强度 + 当前攻击者强度，仅当攻击者为不同玩家时叠加）
     *
     * @param target      携带异常的目标
     * @param elementType 元素类型
     * @param attacker    当前攻击者（可为null）
     * @return 计算后的伤害值
     */
    public static float calculateDamage(LivingEntity target, ElementType elementType, LivingEntity attacker) {
        int level = getMarkLevel(target, elementType);
        if (level <= 0) {
            return 0.0f;
        }

        // 基础强度：记录在标记上的施法者
        double power = 1.0;
        LivingEntity caster = null;
        if (target.level() instanceof ServerLevel serverLevel) {
            caster = getCaster(serverLevel, target, elementType);
        }
        power = getSchoolPower(caster, elementType);

        if (attacker instanceof Player) {
            if (caster != null && !attacker.getUUID().equals(caster.getUUID())) {
                // 不同玩家攻击：双方流派强度相加
                power += getSchoolPower(attacker, elementType);
            } else if (caster == null) {
                // 无施法者记录时，回退使用当前攻击者强度
                power = getSchoolPower(attacker, elementType);
            }
        }

        return (float) (level * power);
    }

    // ==================== 友军与召唤物排除 ====================

    /**
     * 判断候选实体是否应从元素攻击名单中排除
     *
     * 排除规则（雷电异常专用）：
     * - 施法者自身
     * - 施法者队友
     * - 施法者的召唤物
     * - 队友的召唤物
     *
     * @param candidate 候选目标
     * @param reference 参考实体（施法者，可为null）
     * @return 是否应排除
     */
    public static boolean isExcludedFromAttack(LivingEntity candidate, LivingEntity reference) {
        if (candidate == null) {
            return true;
        }
        if (reference == null) {
            return false;
        }
        // 施法者自身
        if (candidate == reference) {
            return true;
        }
        // 施法者队友（同一队伍）
        if (candidate.getTeam() != null && candidate.getTeam().equals(reference.getTeam())) {
            return true;
        }
        // 召唤物：施法者的召唤物或队友的召唤物
        if (candidate instanceof IMagicSummon summon) {
            Entity owner = summon.getSummoner();
            if (owner == reference) {
                return true;
            }
            if (owner != null && owner.getTeam() != null && reference.getTeam() != null
                    && owner.getTeam().equals(reference.getTeam())) {
                return true;
            }
        }
        return false;
    }
}
