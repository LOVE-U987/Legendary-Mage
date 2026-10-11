package com.legendarymage.legendarymagemod.element;

import java.util.List;

import com.legendarymage.legendarymagemod.Config;
import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.ModLogger;
import com.legendarymage.legendarymagemod.effect.ElementDamageGuard;
import com.legendarymage.legendarymagemod.effect.ElementMarkEffect;
import com.legendarymage.legendarymagemod.effect.LightningRodBuffEffect;
import com.legendarymage.legendarymagemod.effect.ModEffects;
import com.legendarymage.legendarymagemod.effect.PlagueBuffEffect;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.particle.FogParticleOptions;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * 元素反应效果类
 * 负责处理各种元素反应的效果实现
 *
 * @author Love_U
 * @version 0.0.3
 */
public class ElementReactionEffects {

    /**
     * 终末回响法术抗性修饰符名称
     */
    private static final String ENDER_ECHO_RESIST_MODIFIER_NAME = "ender_echo_spell_resist";

    /**
     * 终末回响法术强度修饰符名称
     */
    private static final String ENDER_ECHO_SPELL_POWER_MODIFIER_NAME = "ender_echo_spell_power";

    /**
     * 输出调试日志
     * 通过配置开关控制是否输出
     *
     * @param message 日志消息
     */
    private static void debugLog(String message) {
        if (Config.ELEMENT_REACTION_DEBUG_OUTPUT.get()) {
            com.legendarymage.legendarymagemod.ModLogger.spell("[元素反应效果] {}", message);
        }
    }

    /**
     * 处理元素反应
     * 这是主要的元素反应处理入口，根据反应类型调用对应的效果
     *
     * @param serverLevel     服务器世界
     * @param target          目标实体
     * @param attacker        攻击者
     * @param existingElement 已有的元素
     * @param newElement      新施加的元素
     * @param existingLevel   已有元素的等级
     */
    public static void handleReaction(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                      ElementType existingElement, ElementType newElement, int existingLevel) {
        try {
            // 检查目标是否已死亡或正在死亡
            if (!target.isAlive() || target.isDeadOrDying()) {
                debugLog(String.format("目标已死亡或正在死亡，跳过元素反应处理: %s",
                        target.getName().getString()));
                return;
            }
            
            // 获取反应类型
            ReactionType reactionType = getReactionType(existingElement, newElement);

            debugLog(String.format("处理反应: %s + %s = %s (等级: %d)",
                    existingElement.getId(),
                    newElement.getId(),
                    reactionType.name(),
                    existingLevel));

            // 根据反应类型执行对应效果
            switch (reactionType) {
                case ICE_FIRE:
                    // 冰火：单次伤害加成
                    handleIceFire(serverLevel, target, attacker, existingElement, newElement, existingLevel);
                    // 反应后移除被反应的元素标记（冰和火）
                    removeElementMark(target, existingElement);
                    removeElementMark(target, newElement);
                    break;

                case POISON_FIRE:
                    // 木火：挂上烈焰Buff
                    handlePoisonFire(serverLevel, target, attacker, existingElement, newElement, existingLevel);
                    // 反应后移除被反应的元素标记（毒和火）
                    removeElementMark(target, existingElement);
                    removeElementMark(target, newElement);
                    break;

                case LIGHTNING_FIRE:
                    // 雷火：在实体位置落雷
                    handleLightningFire(serverLevel, target, attacker, existingElement, newElement, existingLevel);
                    // 反应后移除被反应的元素标记（雷和火）
                    removeElementMark(target, existingElement);
                    removeElementMark(target, newElement);
                    break;

                case HOLY_BLOOD:
                    // 神圣-猩红：单次伤害加成
                    handleHolyBlood(serverLevel, target, attacker, existingElement, newElement, existingLevel);
                    // 反应后移除被反应的元素标记（神圣和血）
                    removeElementMark(target, existingElement);
                    removeElementMark(target, newElement);
                    break;

                case ELDRITCH_BLOOD:
                    // 邪术-猩红：给与施法者Buff"混沌"
                    handleEldritchBlood(serverLevel, target, attacker, existingElement, newElement, existingLevel);
                    // 反应后移除被反应的元素标记（邪术和血）
                    removeElementMark(target, existingElement);
                    removeElementMark(target, newElement);
                    break;

                case ENDER_ANY:
                    // 末影与任意：给与施法者 Buff"终末回响"
                    handleEnderAny(serverLevel, target, attacker, existingElement, newElement, existingLevel);
                    // 反应后移除被反应的元素标记（末影和另一个元素）
                    removeElementMark(target, existingElement);
                    removeElementMark(target, newElement);
                    break;

                case LIGHTNING_POISON:
                    // 雷毒：消耗感电 Buff 释放电磁波范围伤害
                    handleLightningPoison(serverLevel, target, attacker, existingElement, newElement, existingLevel);
                    // 反应后移除被反应的元素标记（雷和毒）
                    removeElementMark(target, existingElement);
                    removeElementMark(target, newElement);
                    break;

                case ICE_LIGHTNING:
                    // 冰雷：获得避雷针 Buff
                    handleIceLightning(serverLevel, target, attacker, existingElement, newElement, existingLevel);
                    // 反应后移除被反应的元素标记（冰和雷）
                    removeElementMark(target, existingElement);
                    removeElementMark(target, newElement);
                    break;

                case BLOOD_POISON:
                    // 暗毒：获得瘟疫 Buff
                    handleBloodPoison(serverLevel, target, attacker, existingElement, newElement, existingLevel);
                    // 反应后移除被反应的元素标记（血和毒）
                    removeElementMark(target, existingElement);
                    removeElementMark(target, newElement);
                    break;

                case BOILING_BLOOD:
                    // 沸血：按已损失生命比例斩杀爆发
                    handleBoilingBlood(serverLevel, target, attacker, existingElement, newElement, existingLevel);
                    // 反应后移除被反应的元素标记（火和血）
                    removeElementMark(target, existingElement);
                    removeElementMark(target, newElement);
                    break;

                case ANNIHILATION:
                    // 光暗湮灭：清除全部效果并按数量爆发
                    handleAnnihilation(serverLevel, target, attacker, existingElement, newElement, existingLevel);
                    // 反应后移除被反应的元素标记（神圣和邪术）——湮灭内部已清除全部效果
                    removeElementMark(target, existingElement);
                    removeElementMark(target, newElement);
                    break;

                case WARP:
                    // 邪雷挪移：电磁引力拉向施法者
                    handleWarp(serverLevel, target, attacker, existingElement, newElement, existingLevel);
                    // 反应后移除被反应的元素标记（雷和邪术）
                    removeElementMark(target, existingElement);
                    removeElementMark(target, newElement);
                    break;

                case SMITE_CHARGE:
                    // 天罚充能：给与施法者充能 Buff
                    handleSmiteCharge(serverLevel, target, attacker, existingElement, newElement, existingLevel);
                    // 反应后移除被反应的元素标记（雷和神圣）
                    removeElementMark(target, existingElement);
                    removeElementMark(target, newElement);
                    break;

                case HOLY_FIRE:
                    // 圣火：清除增益并对亡灵特攻
                    handleHolyFire(serverLevel, target, attacker, existingElement, newElement, existingLevel);
                    // 反应后移除被反应的元素标记（火和神圣）
                    removeElementMark(target, existingElement);
                    removeElementMark(target, newElement);
                    break;

                case BLOOD_THORNS:
                    // 血雷：给与目标近战反伤的荆棘 Buff
                    handleBloodThorns(serverLevel, target, attacker, existingElement, newElement, existingLevel);
                    // 反应后移除被反应的元素标记（雷和血）
                    removeElementMark(target, existingElement);
                    removeElementMark(target, newElement);
                    break;

                case FROST_FOG:
                    // 霜毒：生成持续叠加中毒的雾区
                    handleFrostFog(serverLevel, target, attacker, existingElement, newElement, existingLevel);
                    // 反应后移除被反应的元素标记（冰和毒）
                    removeElementMark(target, existingElement);
                    removeElementMark(target, newElement);
                    break;

                case PLAGUE_CLOUD:
                    // 邪瘟：生成持续施加邪术标记的毒云
                    handlePlagueCloud(serverLevel, target, attacker, existingElement, newElement, existingLevel);
                    // 反应后移除被反应的元素标记（邪术和毒）
                    removeElementMark(target, existingElement);
                    removeElementMark(target, newElement);
                    break;

                case ICE_BLOOD:
                    // 冰封：给与目标治疗抑制 Debuff
                    handleIceBlood(serverLevel, target, attacker, existingElement, newElement, existingLevel);
                    removeElementMark(target, existingElement);
                    removeElementMark(target, newElement);
                    break;

                case VOID_FROST:
                    // 虚空：恐惧击退目标
                    handleVoidFrost(serverLevel, target, attacker, existingElement, newElement, existingLevel);
                    removeElementMark(target, existingElement);
                    removeElementMark(target, newElement);
                    break;

                case HOLY_ICE:
                    // 圣冰：生成持续减速的区域
                    handleHolyIce(serverLevel, target, attacker, existingElement, newElement, existingLevel);
                    removeElementMark(target, existingElement);
                    removeElementMark(target, newElement);
                    break;

                case HELLFIRE_BRAND:
                    // 狱火：给与目标吸血烙印 Debuff
                    handleHellfireBrand(serverLevel, target, attacker, existingElement, newElement, existingLevel);
                    removeElementMark(target, existingElement);
                    removeElementMark(target, newElement);
                    break;

                case HOLY_DEW:
                    // 圣露：回复施法者 mana
                    handleHolyDew(serverLevel, target, attacker, existingElement, newElement, existingLevel);
                    removeElementMark(target, existingElement);
                    removeElementMark(target, newElement);
                    break;

                case UNKNOWN:
                default:
                    // 未知反应，播放默认效果
                    handleUnknownReaction(serverLevel, target, attacker, existingElement, newElement, existingLevel);
                    break;
            }
        } catch (Exception e) {
            ModLogger.error("[元素反应] handleReaction 发生异常", e);
        }
    }

    /**
     * 移除指定类型的元素标记
     *
     * @param target      目标实体
     * @param elementType 元素类型
     */
    private static void removeElementMark(LivingEntity target, ElementType elementType) {
        // 检查目标是否已死亡或正在死亡
        if (!target.isAlive() || target.isDeadOrDying()) {
            return;
        }
        
        MobEffect markEffect = elementType.getMarkEffect();
        if (markEffect != null) {
            Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(markEffect);
            if (target.hasEffect(effectHolder)) {
                target.removeEffect(effectHolder);
                debugLog(String.format("移除 %s 的元素标记", elementType.getId()));
            }
        }
    }

    /**
     * 获取反应类型
     * 根据两种元素确定反应类型
     *
     * @param element1 第一种元素
     * @param element2 第二种元素
     * @return 反应类型
     */
    private static ReactionType getReactionType(ElementType element1, ElementType element2) {
        // 检查冰火反应
        if ((element1 == ElementType.ICE && element2 == ElementType.FIRE) ||
            (element1 == ElementType.FIRE && element2 == ElementType.ICE)) {
            return ReactionType.ICE_FIRE;
        }

        // 检查木火反应
        if ((element1 == ElementType.POISON && element2 == ElementType.FIRE) ||
            (element1 == ElementType.FIRE && element2 == ElementType.POISON)) {
            return ReactionType.POISON_FIRE;
        }

        // 检查雷火反应
        if ((element1 == ElementType.LIGHTNING && element2 == ElementType.FIRE) ||
            (element1 == ElementType.FIRE && element2 == ElementType.LIGHTNING)) {
            return ReactionType.LIGHTNING_FIRE;
        }

        // 检查神圣-猩红反应
        if ((element1 == ElementType.HOLY && element2 == ElementType.BLOOD) ||
            (element1 == ElementType.BLOOD && element2 == ElementType.HOLY)) {
            return ReactionType.HOLY_BLOOD;
        }

        // 检查邪术-猩红反应
        if ((element1 == ElementType.ELDRITCH && element2 == ElementType.BLOOD) ||
            (element1 == ElementType.BLOOD && element2 == ElementType.ELDRITCH)) {
            return ReactionType.ELDRITCH_BLOOD;
        }

        // 检查末影与任意反应
        if (element1 == ElementType.ENDER || element2 == ElementType.ENDER) {
            return ReactionType.ENDER_ANY;
        }

        // 检查雷毒反应
        if ((element1 == ElementType.LIGHTNING && element2 == ElementType.POISON) ||
            (element1 == ElementType.POISON && element2 == ElementType.LIGHTNING)) {
            return ReactionType.LIGHTNING_POISON;
        }

        // 检查冰雷反应
        if ((element1 == ElementType.ICE && element2 == ElementType.LIGHTNING) ||
            (element1 == ElementType.LIGHTNING && element2 == ElementType.ICE)) {
            return ReactionType.ICE_LIGHTNING;
        }

        // 检查暗毒反应（血系 - 毒系）
        if ((element1 == ElementType.BLOOD && element2 == ElementType.POISON) ||
            (element1 == ElementType.POISON && element2 == ElementType.BLOOD)) {
            return ReactionType.BLOOD_POISON;
        }

        // 检查沸血反应（火系 - 血系）
        if ((element1 == ElementType.FIRE && element2 == ElementType.BLOOD) ||
            (element1 == ElementType.BLOOD && element2 == ElementType.FIRE)) {
            return ReactionType.BOILING_BLOOD;
        }

        // 检查湮灭反应（神圣 - 邪术）
        if ((element1 == ElementType.HOLY && element2 == ElementType.ELDRITCH) ||
            (element1 == ElementType.ELDRITCH && element2 == ElementType.HOLY)) {
            return ReactionType.ANNIHILATION;
        }

        // 检查挪移反应（雷系 - 邪术）
        if ((element1 == ElementType.LIGHTNING && element2 == ElementType.ELDRITCH) ||
            (element1 == ElementType.ELDRITCH && element2 == ElementType.LIGHTNING)) {
            return ReactionType.WARP;
        }

        // 检查充能反应（雷系 - 神圣）
        if ((element1 == ElementType.LIGHTNING && element2 == ElementType.HOLY) ||
            (element1 == ElementType.HOLY && element2 == ElementType.LIGHTNING)) {
            return ReactionType.SMITE_CHARGE;
        }

        // 检查圣火反应（火系 - 神圣）
        if ((element1 == ElementType.FIRE && element2 == ElementType.HOLY) ||
            (element1 == ElementType.HOLY && element2 == ElementType.FIRE)) {
            return ReactionType.HOLY_FIRE;
        }

        // 检查血雷反应（雷系 - 血系）
        if ((element1 == ElementType.LIGHTNING && element2 == ElementType.BLOOD) ||
            (element1 == ElementType.BLOOD && element2 == ElementType.LIGHTNING)) {
            return ReactionType.BLOOD_THORNS;
        }

        // 检查霜毒反应（冰系 - 毒系）
        if ((element1 == ElementType.ICE && element2 == ElementType.POISON) ||
            (element1 == ElementType.POISON && element2 == ElementType.ICE)) {
            return ReactionType.FROST_FOG;
        }

        // 检查邪瘟反应（邪术 - 毒系）
        if ((element1 == ElementType.ELDRITCH && element2 == ElementType.POISON) ||
            (element1 == ElementType.POISON && element2 == ElementType.ELDRITCH)) {
            return ReactionType.PLAGUE_CLOUD;
        }

        // 检查冰封反应（冰系 - 血系）
        if ((element1 == ElementType.ICE && element2 == ElementType.BLOOD) ||
            (element1 == ElementType.BLOOD && element2 == ElementType.ICE)) {
            return ReactionType.ICE_BLOOD;
        }

        // 检查虚空反应（冰系 - 邪术）
        if ((element1 == ElementType.ICE && element2 == ElementType.ELDRITCH) ||
            (element1 == ElementType.ELDRITCH && element2 == ElementType.ICE)) {
            return ReactionType.VOID_FROST;
        }

        // 检查圣冰反应（冰系 - 神圣）
        if ((element1 == ElementType.ICE && element2 == ElementType.HOLY) ||
            (element1 == ElementType.HOLY && element2 == ElementType.ICE)) {
            return ReactionType.HOLY_ICE;
        }

        // 检查狱火反应（火系 - 邪术）
        if ((element1 == ElementType.FIRE && element2 == ElementType.ELDRITCH) ||
            (element1 == ElementType.ELDRITCH && element2 == ElementType.FIRE)) {
            return ReactionType.HELLFIRE_BRAND;
        }

        // 检查圣露反应（神圣 - 毒系）
        if ((element1 == ElementType.HOLY && element2 == ElementType.POISON) ||
            (element1 == ElementType.POISON && element2 == ElementType.HOLY)) {
            return ReactionType.HOLY_DEW;
        }

        return ReactionType.UNKNOWN;
    }

    // ==================== 具体反应效果实现 ====================

    /**
     * 冰火反应
     * 单次伤害加成
     * 伤害 = 对应buff等级(元素标记等级) × (冰系强度 + 火系强度) × 2.5
     */
    private static void handleIceFire(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                       ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行冰火反应效果");

        if (attacker == null) return;

        // 获取冰系和火系流派强度（原始属性值，铁魔法默认值为1.0）
        double icePower = getAttributeValue(attacker, AttributeRegistry.ICE_SPELL_POWER);
        double firePower = getAttributeValue(attacker, AttributeRegistry.FIRE_SPELL_POWER);

        // 伤害 = 对应buff等级 × 双方流派强度总和 × 2.5
        float bonusDamage = markLevel * (float) (icePower + firePower) * 2.5f;

        // 造成额外伤害（【伤害剔除】创造/旁观模式玩家不承受元素反应伤害，特效保留）
        if (bonusDamage > 0 && !ElementDamageGuard.shouldSkipDamage(target)) {
            target.hurt(serverLevel.damageSources().magic(), bonusDamage);
        }

        debugLog(String.format("冰火反应造成额外伤害: %.1f (冰系: %.1f, 火系: %.1f)",
                bonusDamage, icePower, firePower));

        // 播放特效
        playIceFireParticles(serverLevel, target);
    }

    /**
     * 木火反应
     * 挂上烈焰Buff
     * Buff等级 = 对应buff等级(元素标记等级) × (火系强度 + 毒系强度)
     */
    private static void handlePoisonFire(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                          ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行木火反应效果");

        if (attacker == null) return;

        // 获取火系和毒系流派强度（原始属性值，铁魔法默认值为1.0）
        double firePower = getAttributeValue(attacker, AttributeRegistry.FIRE_SPELL_POWER);
        double poisonPower = getAttributeValue(attacker, AttributeRegistry.NATURE_SPELL_POWER);

        // 自我加强公式：Buff等级 = 对应buff等级 × 双方流派强度总和
        int buffLevel = (int) (markLevel * (firePower + poisonPower));
        buffLevel = Math.max(1, Math.min(buffLevel, 3)); // 限制在1-3级

        // 施加烈焰Buff (PyroFlameEffect)
        // 【伤害剔除】addEffectSafe 对创造/旁观玩家剔除 MAX_HEALTH 修饰符 clamp 造成的扣血
        MobEffect pyroFlameEffect = ModEffects.PYRO_FLAME.get();
        Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(pyroFlameEffect);
        ElementDamageGuard.addEffectSafe(target, new MobEffectInstance(
                effectHolder,
                100 * buffLevel, // 持续时间
                buffLevel - 1,   // 等级（0开始）
                false, true, true
        ));

        debugLog(String.format("木火反应施加烈焰Buff: 等级 %d (火系: %.1f, 毒系: %.1f)",
                buffLevel, firePower, poisonPower));

        // 播放特效
        playPoisonFireParticles(serverLevel, target);
    }

    /**
     * 雷火反应
     * 在实体位置落雷
     * 伤害 = 对应buff等级(元素标记等级) × (雷系强度 + 火系强度) × 2.5
     */
    private static void handleLightningFire(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                             ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行雷火反应效果");

        if (attacker == null) return;

        // 获取雷系和火系流派强度（原始属性值，铁魔法默认值为1.0）
        double lightningPower = getAttributeValue(attacker, AttributeRegistry.LIGHTNING_SPELL_POWER);
        double firePower = getAttributeValue(attacker, AttributeRegistry.FIRE_SPELL_POWER);

        // 伤害 = 对应buff等级 × 双方流派强度总和 × 2.5
        float lightningDamage = markLevel * (float) (lightningPower + firePower) * 2.5f;

        // 在目标位置召唤闪电
        Vec3 pos = target.position();

        // 创建闪电效果
        net.minecraft.world.entity.LightningBolt lightning = new net.minecraft.world.entity.LightningBolt(
                EntityType.LIGHTNING_BOLT, serverLevel);
        lightning.moveTo(pos.x, pos.y, pos.z);
        // 设置闪电的造成者（如果是玩家）
        if (attacker instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            lightning.setCause(serverPlayer);
        }
        serverLevel.addFreshEntity(lightning);

        // 造成雷属性伤害（【伤害剔除】创造/旁观模式玩家不承受元素反应伤害，闪电特效保留）
        if (lightningDamage > 0 && !ElementDamageGuard.shouldSkipDamage(target)) {
            target.hurt(serverLevel.damageSources().lightningBolt(), lightningDamage);
        }

        debugLog(String.format("雷火反应召唤闪电造成 %.1f 伤害 (雷系: %.1f, 火系: %.1f)",
                lightningDamage, lightningPower, firePower));

        // 播放特效
        playLightningFireParticles(serverLevel, target);
    }

    /**
     * 神圣-猩红反应
     * 单次伤害加成
     * 伤害 = 对应buff等级(元素标记等级) × (神圣强度 + 猩红强度) × 2.5
     */
    private static void handleHolyBlood(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                         ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行神圣-猩红反应效果");

        if (attacker == null) return;

        // 获取神圣和猩红流派强度（原始属性值，铁魔法默认值为1.0）
        double holyPower = getAttributeValue(attacker, AttributeRegistry.HOLY_SPELL_POWER);
        double bloodPower = getAttributeValue(attacker, AttributeRegistry.BLOOD_SPELL_POWER);

        // 伤害 = 对应buff等级 × 双方流派强度总和 × 2.5
        float bonusDamage = markLevel * (float) (holyPower + bloodPower) * 2.5f;

        // 造成额外伤害（【伤害剔除】创造/旁观模式玩家不承受元素反应伤害，特效保留）
        if (bonusDamage > 0 && !ElementDamageGuard.shouldSkipDamage(target)) {
            target.hurt(serverLevel.damageSources().magic(), bonusDamage);
        }

        debugLog(String.format("神圣-猩红反应造成 %.1f 神圣伤害 (神圣: %.1f, 猩红: %.1f)",
                bonusDamage, holyPower, bloodPower));

        // 播放特效
        playHolyBloodParticles(serverLevel, target);
    }

    /**
     * 邪术-猩红反应
     * 给与施法者Buff"混沌"
     * "混沌"Buff：法术强度+10%，每一等级+5%
     * Buff等级 = 对应buff等级(元素标记等级) × (邪术强度 + 猩红强度)
     */
    private static void handleEldritchBlood(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                             ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行邪术-猩红反应效果");

        if (attacker == null) return;

        // 获取邪术和猩红流派强度（原始属性值，铁魔法默认值为1.0）
        double eldritchPower = getAttributeValue(attacker, AttributeRegistry.ELDRITCH_SPELL_POWER);
        double bloodPower = getAttributeValue(attacker, AttributeRegistry.BLOOD_SPELL_POWER);

        // 自我加强公式：Buff等级 = 对应buff等级 × 双方流派强度总和
        int buffLevel = (int) (markLevel * (eldritchPower + bloodPower));
        buffLevel = Math.max(1, Math.min(buffLevel, 5)); // 限制在1-5级

        // 移除旧的混沌Buff效果（如果存在）
        removeOldChaosBuffEffect(attacker);

        // 施加混沌Buff
        // 注意：ChaosBuffEffect在构造函数中添加了属性修饰符
        // 属性值 = 基础值10% + 每级5%，与buffLevel对应
        MobEffect chaosBuffEffect = ModEffects.CHAOS_BUFF.get();
        Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(chaosBuffEffect);
        attacker.addEffect(new MobEffectInstance(
                effectHolder,
                200 * buffLevel, // 持续时间（10秒 * 等级）
                buffLevel - 1,   // 等级（0开始）
                false, true, true
        ));

        debugLog(String.format("邪术-猩红反应给予混沌Buff: 等级 %d (邪术: %.1f, 猩红: %.1f)",
                buffLevel, eldritchPower, bloodPower));

        // 播放特效
        playEldritchBloodParticles(serverLevel, attacker);
    }

    /**
     * 移除旧的混沌Buff效果
     */
    private static void removeOldChaosBuffEffect(LivingEntity entity) {
        MobEffect chaosBuffEffect = ModEffects.CHAOS_BUFF.get();
        Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(chaosBuffEffect);
        
        if (entity.hasEffect(effectHolder)) {
            entity.removeEffect(effectHolder);
            debugLog("移除旧的混沌Buff效果");
        }
    }

    /**
     * 末影与任意反应
     * 给与施法者Buff"终末回响"
     * "终末回响"Buff：加成施法者法术抗性与法术强度
     * 加强值 = 对应buff等级(元素标记等级) × (末影强度 + 另一元素强度)
     * 其中法术抗性 = 加强值 × 抗性比例，法术强度 = 加强值 × 强度比例
     *
     * 注意：此方法的属性修饰符管理采用"先移除旧值→添加效果→替换为动态值"的三步策略
     * 虽然存在极短时间窗口（1-2 tick）内属性值为构造函数的显示用固定值，
     * 但由于Minecraft主线程单tick执行特性，其他代码几乎不可能在这个窗口期读取到错误值。
     * 这是EMIffect等模组兼容性要求的权衡设计。
     */
    private static void handleEnderAny(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                        ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行末影-任意反应效果");

        if (attacker == null) return;

        // 获取末影与另一元素的流派强度（原始属性值，铁魔法默认值为1.0）
        ElementType otherElement = (existingElement == ElementType.ENDER) ? newElement : existingElement;
        double enderPower = getElementPower(attacker, ElementType.ENDER);
        double otherPower = getElementPower(attacker, otherElement);

        // 自我加强公式：加强值 = 对应buff等级 × 双方流派强度总和
        double reinforceValue = markLevel * (enderPower + otherPower);
        double spellPowerBonus = reinforceValue * com.legendarymage.legendarymagemod.effect.EnderEchoBuffEffect.getSpellPowerRatio();
        double spellResistBonus = reinforceValue * com.legendarymage.legendarymagemod.effect.EnderEchoBuffEffect.getSpellResistRatio();

        // 第一步：完全移除旧的终末回响效果及其所有动态属性修饰符（如果存在）
        removeOldEnderEchoEffect(attacker);

        // 第二步：施加新的终末回响Buff（此时会自动应用构造函数中的基础显示用修饰符）
        MobEffect enderEchoBuffEffect = ModEffects.ENDER_ECHO_BUFF.get();
        Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(enderEchoBuffEffect);
        int duration = 300; // 15秒

        MobEffectInstance effectInstance = new MobEffectInstance(
                effectHolder,
                duration,
                0,
                false, true, true
        );

        attacker.addEffect(effectInstance);

        // 第三步：立即将基础显示用修饰符替换为动态计算的实际游戏效果修饰符
        // 这确保属性值在同一个tick内就被修正为正确值
        replaceEnderEchoModifiersWithDynamicValues(attacker, spellPowerBonus, spellResistBonus);

        debugLog(String.format("末影-任意反应给予终末回响Buff: 法术强度+%.1f%%, 法术抗性+%.1f%% (末影: %.1f)",
                spellPowerBonus * 100, spellResistBonus * 100, enderPower));

        // 播放特效
        playEnderAnyParticles(serverLevel, attacker);
    }

    /**
     * 将终末回响的基础显示用修饰符替换为动态计算的实际游戏效果修饰符
     * 这是一个原子操作，确保在同一tick内完成替换，减少竞态条件风险
     *
     * @param entity 实体
     * @param spellPowerBonus 动态计算的法术强度加成
     * @param spellResistBonus 动态计算的法术抗性加成
     */
    private static void replaceEnderEchoModifiersWithDynamicValues(LivingEntity entity, double spellPowerBonus, double spellResistBonus) {
        // 移除构造函数中自动添加的基础显示用修饰符（固定值，用于EMIffect显示）
        removeEnderEchoBaseModifiers(entity);

        // 添加基于施法者属性动态计算的实际游戏效果修饰符
        addEnderEchoAttributeModifiers(entity, spellPowerBonus, spellResistBonus);

        debugLog(String.format("终末回响属性修饰符已替换为动态值: 法术强度+%.1f%%, 法术抗性+%.1f%%",
                spellPowerBonus * 100, spellResistBonus * 100));
    }

    /**
     * 移除终末回响的基础属性修饰符（构造函数中添加的显示用修饰符）
     */
    private static void removeEnderEchoBaseModifiers(LivingEntity entity) {
        // 移除法术强度基础修饰符
        if (entity.getAttributes().hasAttribute(AttributeRegistry.SPELL_POWER)) {
            var instance = entity.getAttribute(AttributeRegistry.SPELL_POWER);
            if (instance != null) {
                ResourceLocation baseId = ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "ender_echo_spell_power");
                instance.removeModifier(baseId);
            }
        }
        
        // 移除法术抗性基础修饰符
        if (entity.getAttributes().hasAttribute(AttributeRegistry.SPELL_RESIST)) {
            var instance = entity.getAttribute(AttributeRegistry.SPELL_RESIST);
            if (instance != null) {
                ResourceLocation baseId = ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "ender_echo_spell_resist");
                instance.removeModifier(baseId);
            }
        }
    }

    /**
     * 移除旧的终末回响效果及其属性修饰符
     */
    private static void removeOldEnderEchoEffect(LivingEntity entity) {
        MobEffect enderEchoBuffEffect = ModEffects.ENDER_ECHO_BUFF.get();
        Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(enderEchoBuffEffect);
        
        if (entity.hasEffect(effectHolder)) {
            entity.removeEffect(effectHolder);
        }
        
        // 移除属性修饰符
        removeAttributeModifier(entity, AttributeRegistry.SPELL_POWER, ENDER_ECHO_SPELL_POWER_MODIFIER_NAME);
        removeAttributeModifier(entity, AttributeRegistry.SPELL_RESIST, ENDER_ECHO_RESIST_MODIFIER_NAME);
    }

    /**
     * 添加终末回响的动态属性修饰符
     */
    private static void addEnderEchoAttributeModifiers(LivingEntity entity, double spellPowerBonus, double spellResistBonus) {
        // 添加法术强度修饰符
        addAttributeModifier(entity, AttributeRegistry.SPELL_POWER, ENDER_ECHO_SPELL_POWER_MODIFIER_NAME,
                spellPowerBonus);

        // 添加法术抗性修饰符
        addAttributeModifier(entity, AttributeRegistry.SPELL_RESIST, ENDER_ECHO_RESIST_MODIFIER_NAME,
                spellResistBonus);
    }

    /**
     * 未知反应
     * 默认效果
     */
    private static void handleUnknownReaction(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                              ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行未知反应效果 (预留)");
        // 播放默认粒子效果
        playDefaultReactionParticles(serverLevel, target);
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取属性值
     *
     * @param entity 实体
     * @param attribute 属性
     * @return 属性值，如果没有该属性则返回1.0（铁魔法默认值）
     */
    private static double getAttributeValue(LivingEntity entity, Holder<Attribute> attribute) {
        if (entity.getAttributes().hasAttribute(attribute)) {
            return entity.getAttributeValue(attribute);
        }
        return 1.0; // 铁魔法属性默认值是1.0（100%）
    }

    /**
     * 根据元素类型获取对应的流派法术强度属性
     *
     * @param elementType 元素类型
     * @return 流派法术强度属性
     */
    private static Holder<Attribute> getElementPowerAttribute(ElementType elementType) {
        return switch (elementType) {
            case ICE -> AttributeRegistry.ICE_SPELL_POWER;
            case FIRE -> AttributeRegistry.FIRE_SPELL_POWER;
            case LIGHTNING -> AttributeRegistry.LIGHTNING_SPELL_POWER;
            case HOLY -> AttributeRegistry.HOLY_SPELL_POWER;
            case BLOOD -> AttributeRegistry.BLOOD_SPELL_POWER;
            case ELDRITCH -> AttributeRegistry.ELDRITCH_SPELL_POWER;
            case ENDER -> AttributeRegistry.ENDER_SPELL_POWER;
            case POISON -> AttributeRegistry.NATURE_SPELL_POWER;
        };
    }

    /**
     * 获取实体指定元素的流派强度（原始属性值，铁魔法默认值为1.0）
     *
     * @param entity      实体
     * @param elementType 元素类型
     * @return 流派强度属性值，无属性时返回1.0
     */
    private static double getElementPower(LivingEntity entity, ElementType elementType) {
        return getAttributeValue(entity, getElementPowerAttribute(elementType));
    }

    /**
     * 添加属性修饰符
     * 使用 ResourceLocation 作为标识符（1.21 API）
     *
     * @param entity 实体
     * @param attribute 属性
     * @param modifierName 修饰符名称（用于构建 ResourceLocation ID）
     * @param amount 数值
     */
    private static void addAttributeModifier(LivingEntity entity, Holder<Attribute> attribute,
                                              String modifierName, double amount) {
        if (!entity.getAttributes().hasAttribute(attribute)) return;

        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            // 1.21 中 AttributeModifier 使用 ResourceLocation 作为标识符
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, modifierName);
            // 使用 ADD_MULTIPLIED_TOTAL 操作类型（百分比加成）
            AttributeModifier modifier = new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            instance.addTransientModifier(modifier);
        }
    }

    /**
     * 移除属性修饰符
     * 使用 ResourceLocation 作为标识符（1.21 API）
     *
     * @param entity 实体
     * @param attribute 属性
     * @param modifierName 修饰符名称（用于构建 ResourceLocation ID）
     */
    private static void removeAttributeModifier(LivingEntity entity, Holder<Attribute> attribute, String modifierName) {
        if (!entity.getAttributes().hasAttribute(attribute)) return;

        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            // 1.21 中通过 ResourceLocation 移除修饰符
            // 使用传入的修饰符名称构建完整的 ResourceLocation ID
            ResourceLocation modifierId = ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, modifierName);
            if (instance.getModifier(modifierId) != null) {
                instance.removeModifier(modifierId);
                debugLog(String.format("成功移除属性修饰符: %s (属性: %s)", modifierName, attribute.unwrapKey().map(k -> k.location().getPath()).orElse("unknown")));
            }
        }
    }

    // ==================== 粒子效果 ====================

    /**
     * 播放冰火反应粒子效果
     * 使用炫酷的蒸汽爆发、冲击波和火花效果
     */
    private static void playIceFireParticles(ServerLevel serverLevel, LivingEntity target) {
        Vec3 pos = target.position();
        double range = target.getBbWidth() * 2;

        // 使用炫酷的粒子效果系统
        ElementReactionParticles.playIceFireReaction(serverLevel, pos, range);
    }

    /**
     * 播放木火反应粒子效果
     * 使用炫酷的绿色火焰冲击波和毒雾效果
     */
    private static void playPoisonFireParticles(ServerLevel serverLevel, LivingEntity target) {
        Vec3 pos = target.position();
        double range = target.getBbWidth() * 2;

        // 使用炫酷的粒子效果系统
        ElementReactionParticles.playPoisonFireReaction(serverLevel, pos, range);
    }

    /**
     * 播放雷火反应粒子效果
     * 使用炫酷的雷电冲击波、闪电轨迹和火花效果
     */
    private static void playLightningFireParticles(ServerLevel serverLevel, LivingEntity target) {
        Vec3 pos = target.position();
        double range = target.getBbWidth() * 2;

        // 使用炫酷的粒子效果系统
        ElementReactionParticles.playLightningFireReaction(serverLevel, pos, range);
    }

    /**
     * 播放神圣-猩红反应粒子效果
     * 使用炫酷的神圣-猩红冲击波和能量爆发效果
     */
    private static void playHolyBloodParticles(ServerLevel serverLevel, LivingEntity target) {
        Vec3 pos = target.position();
        double range = target.getBbWidth() * 2;

        // 使用炫酷的粒子效果系统
        ElementReactionParticles.playHolyBloodReaction(serverLevel, pos, range);
    }

    /**
     * 播放邪术-猩红反应粒子效果
     * 使用炫酷的混沌冲击波和传送门效果
     */
    private static void playEldritchBloodParticles(ServerLevel serverLevel, LivingEntity target) {
        Vec3 pos = target.position();
        double range = target.getBbWidth() * 2;

        // 使用炫酷的粒子效果系统
        ElementReactionParticles.playEldritchBloodReaction(serverLevel, pos, range);
    }

    /**
     * 播放末影-任意反应粒子效果
     * 使用炫酷的虚空冲击波和末影粒子效果
     */
    private static void playEnderAnyParticles(ServerLevel serverLevel, LivingEntity target) {
        Vec3 pos = target.position();
        double range = target.getBbWidth() * 2;

        // 使用炫酷的粒子效果系统
        ElementReactionParticles.playEnderAnyReaction(serverLevel, pos, range);
    }

    /**
     * 播放反应粒子效果
     *
     * @param serverLevel     服务器世界
     * @param target          目标实体
     * @param existingElement 已有元素
     * @param newElement      新元素
     */
    public static void playReactionParticles(ServerLevel serverLevel, LivingEntity target,
                                              ElementType existingElement, ElementType newElement) {
        Vec3 pos = target.position();

        // 获取两种元素的颜色
        int color1 = existingElement.getColor();
        int color2 = newElement.getColor();

        // 播放混合粒子效果
        for (int i = 0; i < 20; i++) {
            double offsetX = (Math.random() - 0.5) * target.getBbWidth();
            double offsetY = Math.random() * target.getBbHeight();
            double offsetZ = (Math.random() - 0.5) * target.getBbWidth();

            // 交替使用两种颜色
            int color = (i % 2 == 0) ? color1 : color2;

            // 发送彩色粒子（使用女巫药水粒子作为基础）
            serverLevel.sendParticles(
                    ParticleTypes.WITCH,
                    pos.x + offsetX,
                    pos.y + offsetY,
                    pos.z + offsetZ,
                    1,
                    0.1, 0.1, 0.1,
                    0.05
            );
        }

        // 播放爆炸粒子
        serverLevel.sendParticles(
                ParticleTypes.POOF,
                pos.x,
                pos.y + target.getBbHeight() * 0.5,
                pos.z,
                10,
                0.3, 0.3, 0.3,
                0.1
        );
    }

    /**
     * 播放升级粒子效果
     *
     * @param serverLevel 服务器世界
     * @param target      目标实体
     * @param elementType 元素类型
     * @param newLevel    新等级
     */
    public static void playUpgradeParticles(ServerLevel serverLevel, LivingEntity target,
                                            ElementType elementType, int newLevel) {
        Vec3 pos = target.position();

        // 根据等级播放不同数量的粒子
        int particleCount = newLevel * 5;

        for (int i = 0; i < particleCount; i++) {
            double offsetX = (Math.random() - 0.5) * target.getBbWidth();
            double offsetY = Math.random() * target.getBbHeight();
            double offsetZ = (Math.random() - 0.5) * target.getBbWidth();

            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    pos.x + offsetX,
                    pos.y + offsetY,
                    pos.z + offsetZ,
                    1,
                    0, 0.1, 0,
                    0.02
            );
        }
    }

    /**
     * 播放默认反应粒子效果
     *
     * @param serverLevel 服务器世界
     * @param target      目标实体
     */
    private static void playDefaultReactionParticles(ServerLevel serverLevel, LivingEntity target) {
        Vec3 pos = target.position();

        serverLevel.sendParticles(
                ParticleTypes.FLASH,
                pos.x,
                pos.y + target.getBbHeight() * 0.5,
                pos.z,
                1,
                0, 0, 0,
                0
        );
    }

    /**
     * 反应类型枚举
     */
    public enum ReactionType {
        ICE_FIRE,           // 冰火：单次伤害加成
        POISON_FIRE,        // 木火：挂上烈焰 Buff
        LIGHTNING_FIRE,     // 雷火：在实体位置落雷
        HOLY_BLOOD,         // 神圣 - 猩红：单次伤害加成
        ELDRITCH_BLOOD,     // 邪术 - 猩红：给与施法者 Buff"混沌"
        ENDER_ANY,          // 末影与任意：给与施法者 Buff"终末回响"
        LIGHTNING_POISON,   // 雷毒：消耗感电 Buff 释放电磁波范围伤害
        ICE_LIGHTNING,      // 冰雷：获得避雷针 Buff
        BLOOD_POISON,       // 暗毒：获得瘟疫 Buff
        BOILING_BLOOD,      // 沸血：按已损失生命比例斩杀爆发（火+血）
        ANNIHILATION,       // 湮灭：清除全部效果并按数量爆发（神圣+邪术）
        WARP,               // 挪移：电磁引力拉向施法者（雷+邪术）
        SMITE_CHARGE,       // 充能：给与施法者下一次法术伤害翻倍的充能（雷+神圣）
        HOLY_FIRE,          // 圣火：清除增益并对亡灵特攻（火+神圣）
        BLOOD_THORNS,       // 血雷：给与目标近战反伤的荆棘 Buff（雷+血）
        FROST_FOG,          // 霜毒：生成持续叠加中毒的雾区（冰+毒）
        PLAGUE_CLOUD,       // 邪瘟：生成持续施加邪术标记的毒云（邪术+毒）
        ICE_BLOOD,          // 冰封：治疗抑制 Debuff（冰+血）
        VOID_FROST,         // 虚空：击退恐惧（冰+邪术）
        HOLY_ICE,           // 圣冰：区域减速（冰+神圣）
        HELLFIRE_BRAND,     // 狱火：吸血烙印 Debuff（火+邪术）
        HOLY_DEW,           // 圣露：回复施法者 mana（神圣+毒）
        UNKNOWN             // 未知
    }

    // ==================== 新增元素反应效果 ====================

    /**
     * 雷毒反应
     * 消耗目标身上的感电 Buff 释放电磁波范围伤害
     * 游戏玩法逻辑：
     * 1. 施法者先对目标施加感电Buff（通过其他雷系法术或效果）
     * 2. 然后使用毒系法术触发雷毒反应
     * 3. 反应会消耗目标身上的感电Buff，释放电磁波范围伤害
     *
     * 这种设计鼓励玩家组合使用不同元素的法术，形成连招策略。
     *
     * @param serverLevel 服务器世界
     * @param target 目标实体（需要拥有感电Buff才能触发）
     * @param attacker 攻击者（施法者）
     * @param existingElement 已有元素（雷系）
     * @param newElement 新施加元素（毒系）
     * @param markLevel 元素标记等级
     */
    private static void handleLightningPoison(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                               ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行雷毒反应效果");

        if (attacker == null) return;

        // 获取目标身上的感电 Buff 等级（关键：检查的是目标，不是施法者）
        int electrocutedLevel = getElectrocutedBuffLevel(target);
        if (electrocutedLevel <= 0) {
            debugLog("目标没有感电 Buff，跳过雷毒反应（提示：需要先对目标施加感电效果）");
            return;
        }

        // 获取雷系和毒系流派强度（原始属性值，铁魔法默认值为1.0）
        double lightningPower = getAttributeValue(attacker, AttributeRegistry.LIGHTNING_SPELL_POWER);
        double poisonPower = getAttributeValue(attacker, AttributeRegistry.NATURE_SPELL_POWER);

        // 伤害 = 对应buff等级(感电Buff等级) × 双方流派强度总和 × 2.5
        double damage = electrocutedLevel * (lightningPower + poisonPower) * 2.5;

        // 获取 3 格范围内的所有生物
        double range = 3.0;
        AABB area = new AABB(
                target.position().x - range, target.position().y - range, target.position().z - range,
                target.position().x + range, target.position().y + range, target.position().z + range
        );

        List<LivingEntity> entities = serverLevel.getEntitiesOfClass(LivingEntity.class, area);

        int affectedCount = 0;
        for (LivingEntity entity : entities) {
            // 排除施法者及其队友
            if (entity == attacker || isAlly(attacker, entity)) {
                continue;
            }

            // 【伤害剔除】排除创造/旁观模式玩家
            if (ElementDamageGuard.shouldSkipDamage(entity)) {
                continue;
            }

            // 造成伤害（使用魔法伤害源）
            entity.hurt(serverLevel.damageSources().magic(), (float) damage);
            affectedCount++;
        }

        debugLog(String.format("雷毒反应造成电磁波伤害：%.1f (感电等级：%d), 影响 %d 个目标",
                damage, electrocutedLevel, affectedCount));

        // 消耗目标身上的感电 Buff
        removeElectrocutedBuff(target);

        // 播放特效
        playLightningPoisonParticles(serverLevel, target.position(), range);
    }

    /**
     * 冰雷反应
     * 获得避雷针 Buff，减少雷系和冰系元素抗性，每级减少 5%
     * Buff等级 = 对应buff等级(元素标记等级) × (冰系强度 + 雷系强度)
     * 注意：避雷针 Buff 是给予敌人的 Debuff，让敌人减少雷冰抗性
     */
    private static void handleIceLightning(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                            ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行冰雷反应效果");

        if (attacker == null) return;

        // 获取冰系和雷系流派强度（原始属性值，铁魔法默认值为1.0）
        double icePower = getAttributeValue(attacker, AttributeRegistry.ICE_SPELL_POWER);
        double lightningPower = getAttributeValue(attacker, AttributeRegistry.LIGHTNING_SPELL_POWER);

        // 自我加强公式：Buff等级 = 对应buff等级 × 双方流派强度总和，受最大层数限制
        int maxStacks = LightningRodBuffEffect.getMaxStacks();
        int durationSeconds = LightningRodBuffEffect.getDurationSeconds();
        int buffLevel = Math.min((int) (markLevel * (icePower + lightningPower)), maxStacks);

        // 施加避雷针 Buff 给目标（敌人）
        // 这样施法者才能对带有避雷针 Buff 的敌人造成更高的雷/冰伤害
        MobEffect lightningRodEffect = ModEffects.LIGHTNING_ROD_BUFF.get();
        Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(lightningRodEffect);
        target.addEffect(new MobEffectInstance(
                effectHolder,
                durationSeconds * 20, // 持续时间（tick）
                buffLevel - 1,   // 等级（0 开始）
                false, true, true
        ));

        debugLog(String.format("冰雷反应对目标施加避雷针 Buff: 等级 %d, 持续时间 %d秒",
                buffLevel, durationSeconds));

        // 播放特效
        playIceLightningParticles(serverLevel, target);
    }

    /**
     * 暗毒反应
     * 获得瘟疫 Buff，可叠加，每级减 2% 血量上限
     * Buff等级 = 对应buff等级(元素标记等级) × (血系强度 + 毒系强度)
     * 死亡时 25% 变为我方僵尸，75% 毒爆
     */
    private static void handleBloodPoison(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                           ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行暗毒反应效果");

        if (attacker == null) return;

        // 获取血系和毒系流派强度（原始属性值，铁魔法默认值为1.0）
        double bloodPower = getAttributeValue(attacker, AttributeRegistry.BLOOD_SPELL_POWER);
        double poisonPower = getAttributeValue(attacker, AttributeRegistry.NATURE_SPELL_POWER);

        // 自我加强公式：Buff等级 = 对应buff等级 × 双方流派强度总和，受最大层数限制
        int maxStacks = PlagueBuffEffect.getMaxStacks();
        int durationSeconds = PlagueBuffEffect.getDurationSeconds();
        int buffLevel = Math.min((int) (markLevel * (bloodPower + poisonPower)), maxStacks);

        // 施加瘟疫 Buff
        MobEffect plagueEffect = ModEffects.PLAGUE_BUFF.get();
        Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(plagueEffect);
        MobEffectInstance instance = new MobEffectInstance(
                effectHolder,
                durationSeconds * 20, // 持续时间（tick）
                buffLevel - 1,   // 等级（0 开始）
                false, true, true
        );
        // 【伤害剔除】addEffectSafe 对创造/旁观玩家剔除 MAX_HEALTH 修饰符 clamp 造成的扣血
        ElementDamageGuard.addEffectSafe(target, instance);

        // 在目标 NBT 中记录施法者 UUID（用于死亡时追踪）
        if (attacker instanceof net.minecraft.server.level.ServerPlayer) {
            target.getPersistentData().putUUID("PlagueCaster", attacker.getUUID());
        }

        debugLog(String.format("暗毒反应施加瘟疫 Buff: 等级 %d, 持续时间 %d 秒，施法者：%s",
                buffLevel, durationSeconds, attacker.getName().getString()));

        // 播放特效
        playBloodPoisonParticles(serverLevel, target);
    }

    /**
     * 沸血反应
     * 按目标已损失生命比例造成斩杀爆发伤害
     * 伤害 = [基础伤害 + 已损失生命 × 转化比] × (火系强度 + 血系强度) × 2.5
     *
     * @param serverLevel 服务器世界
     * @param target 目标实体
     * @param attacker 攻击者（施法者）
     * @param existingElement 已有元素
     * @param newElement 新施加元素
     * @param markLevel 元素标记等级
     */
    private static void handleBoilingBlood(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                            ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行沸血反应效果");

        if (attacker == null) return;

        // 获取火系和血系流派强度（原始属性值，铁魔法默认值为1.0）
        double firePower = getAttributeValue(attacker, AttributeRegistry.FIRE_SPELL_POWER);
        double bloodPower = getAttributeValue(attacker, AttributeRegistry.BLOOD_SPELL_POWER);

        // 斩杀伤害：基础伤害 + 已损失生命 × 转化比
        float baseDamage = Config.BOILING_BLOOD_BASE_DAMAGE.get().floatValue();
        double lostHealthRatio = Config.BOILING_BLOOD_LOST_HEALTH_RATIO.get();
        float missingHealth = target.getMaxHealth() - target.getHealth();
        float damage = (baseDamage + (float) (missingHealth * lostHealthRatio)) * (float) (firePower + bloodPower) * 2.5f;

        // 造成斩杀伤害（【伤害剔除】创造/旁观模式玩家不承受元素反应伤害，特效保留）
        if (damage > 0 && !ElementDamageGuard.shouldSkipDamage(target)) {
            target.hurt(serverLevel.damageSources().magic(), damage);
        }

        debugLog(String.format("沸血反应造成斩杀伤害: %.1f (基础: %.1f, 已损失生命: %.1f)",
                damage, baseDamage, missingHealth));

        // 播放特效
        playBoilingBloodParticles(serverLevel, target);
    }

    /**
     * 光暗湮灭反应
     * 清除目标身上全部效果（含元素标记与增益），并按移除数量造成湮灭爆发伤害
     * 伤害 = [基础伤害 + 移除数量 × 单个效果伤害] × (神圣强度 + 邪术强度) × 2.5
     *
     * @param serverLevel 服务器世界
     * @param target 目标实体
     * @param attacker 攻击者（施法者）
     * @param existingElement 已有元素
     * @param newElement 新施加元素
     * @param markLevel 元素标记等级
     */
    private static void handleAnnihilation(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                            ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行光暗湮灭反应效果");

        if (attacker == null) return;

        // 获取神圣和邪术流派强度（原始属性值，铁魔法默认值为1.0）
        double holyPower = getAttributeValue(attacker, AttributeRegistry.HOLY_SPELL_POWER);
        double eldritchPower = getAttributeValue(attacker, AttributeRegistry.ELDRITCH_SPELL_POWER);

        // 1. 统计并清除目标身上的全部效果（含元素标记与增益）
        int removedCount = removeAllEffects(target);

        // 2. 湮灭伤害：基础伤害 + 移除数量 × 单个效果伤害
        float baseDamage = Config.ANNIHILATION_BASE_DAMAGE.get().floatValue();
        float perEffectDamage = Config.ANNIHILATION_PER_EFFECT_DAMAGE.get().floatValue();
        float damage = (baseDamage + removedCount * perEffectDamage) * (float) (holyPower + eldritchPower) * 2.5f;

        // 造成湮灭伤害（【伤害剔除】创造/旁观模式玩家不承受元素反应伤害，特效保留）
        if (damage > 0 && !ElementDamageGuard.shouldSkipDamage(target)) {
            target.hurt(serverLevel.damageSources().magic(), damage);
        }

        debugLog(String.format("光暗湮灭清除 %d 个效果并造成湮灭伤害: %.1f",
                removedCount, damage));

        // 播放特效
        playAnnihilationParticles(serverLevel, target);
    }

    /**
     * 移除实体身上的全部效果
     * 含所有元素标记与增益/有害效果
     *
     * @param target 目标实体
     * @return 移除的效果数量
     */
    private static int removeAllEffects(LivingEntity target) {
        // 复制集合遍历移除，避免 ConcurrentModificationException
        java.util.List<MobEffectInstance> effects = new java.util.ArrayList<>(target.getActiveEffects());
        for (MobEffectInstance effect : effects) {
            target.removeEffect(effect.getEffect());
        }
        return effects.size();
    }

    /**
     * 邪雷挪移反应
     * 目标被电磁引力拉向施法者（速度向量位移，非末影人式随机传送）
     * BOSS/大型敌人（血量阈值判定）免疫拉拽，保护 BOSS 战节奏
     * 无伤害，纯位移控制
     *
     * @param serverLevel 服务器世界
     * @param target 目标实体
     * @param attacker 攻击者（施法者）
     * @param existingElement 已有元素
     * @param newElement 新施加元素
     * @param markLevel 元素标记等级
     */
    private static void handleWarp(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                    ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行邪雷挪移反应效果");

        if (attacker == null) return;

        // 播放特效
        playWarpParticles(serverLevel, target);

        // 【伤害剔除】创造/旁观模式玩家不承受元素控制效果（与冰冻剔除一致）
        if (ElementDamageGuard.shouldSkipDamage(target)) {
            return;
        }

        // BOSS 保护：血量阈值以上的实体免疫拉拽（保护 BOSS 战节奏）
        float bossThreshold = Config.WARP_BOSS_HEALTH_THRESHOLD.get().floatValue();
        if (target.getMaxHealth() >= bossThreshold) {
            debugLog(String.format("目标 %s 最大生命值 %.1f 达到 BOSS 阈值 %.1f，免疫挪移",
                    target.getName().getString(), target.getMaxHealth(), bossThreshold));
            return;
        }

        // 拉拽方向：目标 → 施法者
        Vec3 pullDirection = attacker.position().subtract(target.position());
        if (pullDirection.lengthSqr() < 0.01) {
            // 重合位置，无需拉拽
            return;
        }
        pullDirection = pullDirection.normalize();

        // 速度向量位移（非末影人式传送），y 分量限幅避免飞得过高
        double pullDistance = Config.WARP_PULL_DISTANCE.get();
        Vec3 pullVelocity = pullDirection.scale(pullDistance * 0.3);
        target.setDeltaMovement(pullVelocity.x, Math.min(pullVelocity.y, 0.4), pullVelocity.z);
        target.hurtMarked = true; // 同步速度到客户端

        debugLog(String.format("邪雷挪移将 %s 拉向施法者 (距离: %.1f)",
                target.getName().getString(), pullDistance));
    }

    /**
     * 天罚充能反应
     * 给与施法者"充能"Buff：下一次法术伤害 ×2（触发后消耗）
     * 受益者是施法者；伤害翻倍与消耗由 ElementReactionEvents 的法术伤害监听处理
     *
     * @param serverLevel 服务器世界
     * @param target 目标实体
     * @param attacker 攻击者（施法者）
     * @param existingElement 已有元素
     * @param newElement 新施加元素
     * @param markLevel 元素标记等级
     */
    private static void handleSmiteCharge(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                           ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行天罚充能反应效果");

        if (attacker == null) return;

        // 获取充能效果
        MobEffect chargedEffect = ModEffects.CHARGED_BUFF.get();
        Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(chargedEffect);

        // 施加充能 Buff（固定等级，时长从配置读取；同效果存在时原版自动刷新时长）
        int durationSeconds = Config.CHARGED_DURATION_SECONDS.get();
        // 【增益类】对创造玩家无副作用，走 addEffectSafe 统一施加
        ElementDamageGuard.addEffectSafe(attacker, new MobEffectInstance(
                effectHolder,
                durationSeconds * 20,
                0,
                false, true, true
        ));

        debugLog(String.format("天罚充能给予施法者 %s 充能 Buff (%d秒)",
                attacker.getName().getString(), durationSeconds));

        // 播放特效
        playSmiteChargeParticles(serverLevel, attacker);
    }

    /**
     * 圣火反应
     * 清除目标身上的增益 Buff（净化），并对亡灵/不死生物额外重击
     * 伤害 = 基础 × (火系强度 + 神圣强度) × 2.5，亡灵目标再 × 亡灵特攻倍率
     *
     * @param serverLevel 服务器世界
     * @param target 目标实体
     * @param attacker 攻击者（施法者）
     * @param existingElement 已有元素
     * @param newElement 新施加元素
     * @param markLevel 元素标记等级
     */
    private static void handleHolyFire(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                        ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行圣火反应效果");

        if (attacker == null) return;

        // 获取火系和神圣流派强度（原始属性值，铁魔法默认值为1.0）
        double firePower = getAttributeValue(attacker, AttributeRegistry.FIRE_SPELL_POWER);
        double holyPower = getAttributeValue(attacker, AttributeRegistry.HOLY_SPELL_POWER);

        // 1. 净化：清除目标身上的增益 Buff（不含有害效果与元素标记，与湮灭的"清全部"区分）
        int purifiedCount = removeBeneficialEffects(target);
        if (purifiedCount > 0) {
            debugLog(String.format("圣火反应净化目标 %d 个增益效果", purifiedCount));
        }

        // 2. 特攻：对亡灵/不死生物额外重击
        float baseDamage = Config.HOLY_FIRE_BASE_DAMAGE.get().floatValue();
        float damage = baseDamage * (float) (firePower + holyPower) * 2.5f;
        boolean isUndead = target.getType().is(net.minecraft.tags.EntityTypeTags.UNDEAD);
        if (isUndead) {
            damage *= Config.HOLY_FIRE_UNDEAD_MULTIPLIER.get().floatValue();
        }

        // 造成伤害（【伤害剔除】创造/旁观模式玩家不承受元素反应伤害，特效保留）
        if (damage > 0 && !ElementDamageGuard.shouldSkipDamage(target)) {
            target.hurt(serverLevel.damageSources().magic(), damage);
        }

        debugLog(String.format("圣火反应造成伤害: %.1f (亡灵特攻: %s)",
                damage, isUndead ? "是" : "否"));

        // 播放特效
        playHolyFireParticles(serverLevel, target);
    }

    /**
     * 血雷反应
     * 给与施法者"血雷"Buff：持续期间近战攻击携带者的敌人自身受雷击反伤（类似原版荆棘）
     * 反伤伤害依据血雷等级乘法计算，Buff 等级可叠加（每次触发等级+1，无限叠加）
     * 反伤与递归防护逻辑由 ElementReactionEvents 的伤害监听处理
     *
     * @param serverLevel 服务器世界
     * @param target 目标实体
     * @param attacker 攻击者（施法者）
     * @param existingElement 已有元素
     * @param newElement 新施加元素
     * @param markLevel 元素标记等级
     */
    private static void handleBloodThorns(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                           ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行血雷反应效果");

        if (attacker == null) return;

        // 获取血雷效果
        MobEffect bloodThornsEffect = ModEffects.BLOOD_THORNS.get();
        Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(bloodThornsEffect);

        // 检查施法者是否已有血雷 Buff（等级可叠加，无限叠加）
        MobEffectInstance existingEffect = attacker.getEffect(effectHolder);
        int newBuffLevel;
        if (existingEffect != null) {
            // 已有血雷，等级+1
            newBuffLevel = existingEffect.getAmplifier() + 2;
        } else {
            // 没有血雷，初始等级 1
            newBuffLevel = 1;
        }

        // 施加血雷 Buff 给施法者（等级可叠加，时长刷新为固定值）
        int durationSeconds = Config.BLOOD_THORNS_DURATION_SECONDS.get();
        ElementDamageGuard.addEffectSafe(attacker, new MobEffectInstance(
                effectHolder,
                durationSeconds * 20,
                newBuffLevel - 1,
                false, true, true
        ));

        debugLog(String.format("血雷反应给予施法者 %s 血雷 Buff (%d级, %d秒)",
                attacker.getName().getString(), newBuffLevel, durationSeconds));

        // 播放特效
        playBloodThornsParticles(serverLevel, attacker);
    }

    /**
     * 霜毒反应
     * 在目标位置生成霜毒雾区（原版药水云，自定义青绿雾）
     * 圈内实体每秒被叠加原版中毒
     *
     * @param serverLevel 服务器世界
     * @param target 目标实体
     * @param attacker 攻击者（施法者）
     * @param existingElement 已有元素
     * @param newElement 新施加元素
     * @param markLevel 元素标记等级
     */
    private static void handleFrostFog(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                        ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行霜毒反应效果");

        if (attacker == null) return;

        // 生成霜毒雾区（自制区域管理，排除施法者）
        float radius = Config.FROST_FOG_RADIUS.get().floatValue();
        int durationSeconds = Config.FROST_FOG_DURATION_SECONDS.get();
        int poisonLevel = Config.FROST_FOG_POISON_LEVEL.get();

        ElementZoneManager.spawnZone(serverLevel, target.position(), attacker,
                ElementZoneManager.ZoneType.FROST_FOG, radius, durationSeconds);

        debugLog(String.format("霜毒反应生成雾区: 半径 %.1f, 持续 %d秒, 中毒 %d级",
                radius, durationSeconds, poisonLevel));

        // 播放生成特效
        playFrostFogParticles(serverLevel, target);
    }

    /**
     * 邪瘟反应
     * 在目标位置生成邪瘟毒云（原版药水云，深绿雾）
     * 圈内实体持续被施加邪术标记（1 级即可参与反应，制造连锁反应机会）
     *
     * @param serverLevel 服务器世界
     * @param target 目标实体
     * @param attacker 攻击者（施法者）
     * @param existingElement 已有元素
     * @param newElement 新施加元素
     * @param markLevel 元素标记等级
     */
    private static void handlePlagueCloud(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                           ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行邪瘟反应效果");

        if (attacker == null) return;

        // 生成邪瘟毒云（自制区域管理，排除施法者）
        float radius = Config.PLAGUE_CLOUD_RADIUS.get().floatValue();
        int durationSeconds = Config.PLAGUE_CLOUD_DURATION_SECONDS.get();

        ElementZoneManager.spawnZone(serverLevel, target.position(), attacker,
                ElementZoneManager.ZoneType.PLAGUE_CLOUD, radius, durationSeconds);

        debugLog(String.format("邪瘟反应生成毒云: 半径 %.1f, 持续 %d秒, 持续施加邪术标记",
                radius, durationSeconds));

        // 播放生成特效
        playPlagueCloudParticles(serverLevel, target);
    }

    /**
     * 冰封血脉反应
     * 给与目标治疗抑制 Debuff：持续期间目标受到的治疗减半（比例从 Config 读取）
     * 治疗减半逻辑由 ElementReactionEvents 的 LivingHealEvent 监听处理
     */
    private static void handleIceBlood(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                        ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行冰封血脉反应效果");
        if (attacker == null) return;

        MobEffect iceBloodEffect = ModEffects.ICE_BLOOD_DEBUFF.get();
        Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(iceBloodEffect);
        int duration = Config.ICE_BLOOD_DURATION_SECONDS.get() * 20;
        ElementDamageGuard.addEffectSafe(target, new MobEffectInstance(effectHolder, duration, 0, false, true, true));

        debugLog(String.format("冰封血脉给予目标 %s 治疗抑制 (%d秒)",
                target.getName().getString(), Config.ICE_BLOOD_DURATION_SECONDS.get()));
        playIceBloodParticles(serverLevel, target);
    }

    /**
     * 虚空寒霜反应
     * 恐惧击退目标：将目标朝远离施法者的方向弹开（击退距离 Config 驱动）
     */
    private static void handleVoidFrost(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                         ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行虚空寒霜反应效果");
        if (attacker == null) return;

        if (ElementDamageGuard.shouldSkipDamage(target)) return;

        // 击退方向：目标 → 远离施法者
        Vec3 knockDir = target.position().subtract(attacker.position());
        if (knockDir.lengthSqr() < 0.01) {
            // 位置重合时不击退（normalize 零向量会返回 ZERO，避免速度被清零的副作用）
            debugLog(String.format("虚空寒霜: %s 与施法者位置重合，跳过击退",
                    target.getName().getString()));
        } else {
            Vec3 normalized = knockDir.normalize();
            double knockDist = Config.VOID_FROST_KNOCKBACK_DISTANCE.get() * 0.3;
            target.setDeltaMovement(normalized.scale(knockDist));
            target.hurtMarked = true;
        }

        // 短暂恐慌效果（标记等级 × 基础时长）
        net.minecraft.world.effect.MobEffectInstance panic = new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.WEAKNESS,
                (markLevel >= 1 ? markLevel : 1) * Config.VOID_FROST_DURATION_SECONDS.get() * 20,
                0, false, true, true);
        target.addEffect(panic);

        debugLog(String.format("虚空寒霜击退 %s (距离: %.1f)",
                target.getName().getString(), Config.VOID_FROST_KNOCKBACK_DISTANCE.get()));
        playVoidFrostParticles(serverLevel, target);
    }

    /**
     * 圣冰结晶反应
     * 在目标位置生成圣冰结晶区域（自制区域管理，排除施法者），圈内实体持续减速
     */
    private static void handleHolyIce(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                       ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行圣冰结晶反应效果");
        if (attacker == null) return;

        float radius = Config.HOLY_ICE_SLOW_RADIUS.get().floatValue();
        int duration = Config.HOLY_ICE_DURATION_SECONDS.get();

        ElementZoneManager.spawnZone(serverLevel, target.position(), attacker,
                ElementZoneManager.ZoneType.HOLY_ICE, radius, duration);

        debugLog(String.format("圣冰结晶生成减速区域: 半径 %.1f, 持续 %d秒", radius, duration));
        playHolyIceParticles(serverLevel, target);
    }

    /**
     * 狱火烙印反应
     * 给与目标狱火烙印 Debuff（记录施法者 UUID）：期间对目标的伤害按比例吸血给施法者
     * 吸血逻辑由 ElementReactionEvents 的 LivingDamageEvent.Post 监听处理
     */
    private static void handleHellfireBrand(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                             ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行狱火烙印反应效果");
        if (attacker == null) return;

        MobEffect hellfireEffect = ModEffects.HELLFIRE_BRAND.get();
        Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(hellfireEffect);
        int duration = Config.HELLFIRE_DURATION_SECONDS.get() * 20;

        ElementDamageGuard.addEffectSafe(target, new MobEffectInstance(effectHolder, duration, 0, false, true, true));

        // 记录施法者 UUID（吸血时用）
        target.getPersistentData().putUUID("legendarymage:hellfire_caster", attacker.getUUID());

        debugLog(String.format("狱火烙印给予目标 %s 烙印 (%d秒)，施法者 %s",
                target.getName().getString(), Config.HELLFIRE_DURATION_SECONDS.get(), attacker.getName().getString()));
        playHellfireBrandParticles(serverLevel, target);
    }

    /**
     * 圣露回源反应
     * 毒被圣光净化→ 施法者回复法术魔力
     * 使用铁魔法的 MagicManager.getPlayerMagicData + addMana API
     */
    private static void handleHolyDew(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                       ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行圣露回源反应效果");
        if (attacker == null) return;

        if (attacker instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            io.redspace.ironsspellbooks.api.magic.MagicData holyDewData = ((net.neoforged.neoforge.attachment.IAttachmentHolder) serverPlayer).getData(io.redspace.ironsspellbooks.registries.DataAttachmentRegistry.MAGIC_DATA);
            if (holyDewData != null) {
                holyDewData.addMana(Config.HOLY_DEW_MANA_AMOUNT.get().floatValue());
            }

            debugLog(String.format("圣露回源恢复施法者 %s 魔力: %.1f",
                    serverPlayer.getName().getString(), Config.HOLY_DEW_MANA_AMOUNT.get()));
        }

        playHolyDewParticles(serverLevel, target);
    }

    /**
     * 移除实体身上的全部增益效果
     *
     * @param target 目标实体
     * @return 移除的增益数量
     */
    private static int removeBeneficialEffects(LivingEntity target) {
        // 复制集合遍历移除，避免 ConcurrentModificationException
        java.util.List<MobEffectInstance> effects = new java.util.ArrayList<>(target.getActiveEffects());
        int removed = 0;
        for (MobEffectInstance effect : effects) {
            if (effect.getEffect().value().getCategory() == net.minecraft.world.effect.MobEffectCategory.BENEFICIAL) {
                target.removeEffect(effect.getEffect());
                removed++;
            }
        }
        return removed;
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取感电 Buff 等级
     *
     * @param entity 实体
     * @return Buff 等级（从 1 开始），如果没有则返回 0
     */
    private static int getElectrocutedBuffLevel(LivingEntity entity) {
        MobEffect electrocutedEffect = ModEffects.ELECTROCUTED_BUFF.get();
        if (electrocutedEffect == null) {
            return 0;
        }

        Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(electrocutedEffect);
        MobEffectInstance instance = entity.getEffect(effectHolder);
        return instance != null ? instance.getAmplifier() + 1 : 0;
    }

    /**
     * 移除感电 Buff
     *
     * @param entity 实体
     */
    private static void removeElectrocutedBuff(LivingEntity entity) {
        MobEffect electrocutedEffect = ModEffects.ELECTROCUTED_BUFF.get();
        if (electrocutedEffect != null) {
            Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(electrocutedEffect);
            entity.removeEffect(effectHolder);
        }
    }

    /**
     * 检查两个实体是否为队友
     *
     * @param entity1 实体 1
     * @param entity2 实体 2
     * @return 是否为队友
     */
    private static boolean isAlly(LivingEntity entity1, LivingEntity entity2) {
        // 使用铁魔法的团队检查
        if (entity1 instanceof net.minecraft.world.entity.player.Player player1 &&
            entity2 instanceof net.minecraft.world.entity.player.Player player2) {
            return player1.getTeam() != null && player1.getTeam().equals(player2.getTeam());
        }

        // 检查是否为同一阵营的生物
        return entity1.getTeam() != null && entity1.getTeam().equals(entity2.getTeam());
    }

    /**
     * 播放雷毒反应粒子效果
     */
    private static void playLightningPoisonParticles(ServerLevel serverLevel, Vec3 pos, double range) {
        // 使用铁魔法风格的特效
        ElementReactionParticles.playLightningPoisonReaction(serverLevel, pos, range);
    }

    /**
     * 播放冰雷反应粒子效果
     */
    private static void playIceLightningParticles(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 pos = entity.position();
        double range = entity.getBbWidth() * 2;
        // 使用铁魔法风格的特效
        ElementReactionParticles.playIceLightningReaction(serverLevel, pos, range);
    }

    /**
     * 播放暗毒反应粒子效果
     */
    private static void playBloodPoisonParticles(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 pos = entity.position();
        double range = entity.getBbWidth() * 2;
        // 使用铁魔法风格的特效
        ElementReactionParticles.playBloodPoisonReaction(serverLevel, pos, range);
    }

    /**
     * 播放沸血反应粒子效果
     * 红橙双色爆发
     */
    private static void playBoilingBloodParticles(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 pos = entity.position();
        double range = entity.getBbWidth() * 2;
        // 使用铁魔法风格的特效
        ElementReactionParticles.playBoilingBloodReaction(serverLevel, pos, range);
    }

    /**
     * 播放光暗湮灭反应粒子效果
     * 黑白双色湮灭冲击波
     */
    private static void playAnnihilationParticles(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 pos = entity.position();
        double range = entity.getBbWidth() * 2;
        // 使用铁魔法风格的特效
        ElementReactionParticles.playAnnihilationReaction(serverLevel, pos, range);
    }

    /**
     * 播放邪雷挪移反应粒子效果
     * 紫色引力冲击波
     */
    private static void playWarpParticles(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 pos = entity.position();
        double range = entity.getBbWidth() * 2;
        // 使用铁魔法风格的特效
        ElementReactionParticles.playWarpReaction(serverLevel, pos, range);
    }

    /**
     * 播放天罚充能粒子效果
     * 金紫双色充能光环
     */
    private static void playSmiteChargeParticles(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 pos = entity.position();
        double range = entity.getBbWidth() * 2;
        // 使用铁魔法风格的特效
        ElementReactionParticles.playSmiteChargeReaction(serverLevel, pos, range);
    }

    /**
     * 播放圣火反应粒子效果
     * 金橙净化爆发
     */
    private static void playHolyFireParticles(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 pos = entity.position();
        double range = entity.getBbWidth() * 2;
        // 使用铁魔法风格的特效
        ElementReactionParticles.playHolyFireReaction(serverLevel, pos, range);
    }

    /**
     * 播放血雷反应粒子效果
     * 暗红电光环绕
     */
    private static void playBloodThornsParticles(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 pos = entity.position();
        double range = entity.getBbWidth() * 2;
        // 使用铁魔法风格的特效
        ElementReactionParticles.playBloodThornsReaction(serverLevel, pos, range);
    }

    /**
     * 播放霜毒反应粒子效果
     * 青绿雾爆发
     */
    private static void playFrostFogParticles(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 pos = entity.position();
        double range = entity.getBbWidth() * 2;
        // 使用铁魔法风格的特效
        ElementReactionParticles.playFrostFogReaction(serverLevel, pos, range);
    }

    /**
     * 播放邪瘟反应粒子效果
     * 深绿毒云爆发
     */
    private static void playPlagueCloudParticles(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 pos = entity.position();
        double range = entity.getBbWidth() * 2;
        // 使用铁魔法风格的特效
        ElementReactionParticles.playPlagueCloudReaction(serverLevel, pos, range);
    }

    /**
     * 播放冰封血脉反应粒子效果
     * 冰蓝霜雾 + 血色粒子
     */
    private static void playIceBloodParticles(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 pos = entity.position();
        double range = entity.getBbWidth() * 2;
        ElementReactionParticles.playIceBloodReaction(serverLevel, pos, range);
    }

    /**
     * 播放虚空寒霜反应粒子效果
     * 紫色末影冲击波
     */
    private static void playVoidFrostParticles(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 pos = entity.position();
        double range = entity.getBbWidth() * 2;
        ElementReactionParticles.playVoidFrostReaction(serverLevel, pos, range);
    }

    /**
     * 播放圣冰结晶反应粒子效果
     * 冰蓝霜雾 + 金色圣光
     */
    private static void playHolyIceParticles(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 pos = entity.position();
        double range = entity.getBbWidth() * 2;
        ElementReactionParticles.playHolyIceReaction(serverLevel, pos, range);
    }

    /**
     * 播放狱火烙印反应粒子效果
     * 火焰粒子 + 紫色邪术印记
     */
    private static void playHellfireBrandParticles(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 pos = entity.position();
        double range = entity.getBbWidth() * 2;
        ElementReactionParticles.playHellfireBrandReaction(serverLevel, pos, range);
    }

    /**
     * 播放圣露回源反应粒子效果
     * 金色净化光柱
     */
    private static void playHolyDewParticles(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 pos = entity.position();
        double range = entity.getBbWidth() * 2;
        ElementReactionParticles.playHolyDewReaction(serverLevel, pos, range);
    }
}
