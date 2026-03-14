package com.legendarymage.legendarymagemod.element;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.effect.ModEffects;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * 元素反应效果类
 * 负责处理各种元素反应的效果实现
 *
 * @author Love_U
 * @version 0.0.3
 */
public class ElementReactionEffects {

    /**
     * 混沌Buff法术强度修饰符UUID
     */
    private static final UUID CHAOS_SPELL_POWER_UUID = UUID.fromString("42345678-1234-1234-1234-123456789abc");

    /**
     * 终末回响法术抗性修饰符UUID
     */
    private static final UUID ENDER_ECHO_RESIST_UUID = UUID.fromString("52345678-1234-1234-1234-123456789abc");

    /**
     * 终末回响法术强度修饰符UUID
     */
    private static final UUID ENDER_ECHO_SPELL_POWER_UUID = UUID.fromString("62345678-1234-1234-1234-123456789abc");

    /**
     * 输出调试日志
     * 通过配置开关控制是否输出
     *
     * @param message 日志消息
     */
    private static void debugLog(String message) {
        if (com.legendarymage.legendarymagemod.Config.ELEMENT_REACTION_DEBUG_OUTPUT.get()) {
            LegendaryMage.LOGGER.info("[元素反应效果] {}", message);
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
                // 末影与任意：给与施法者Buff"终末回响"
                handleEnderAny(serverLevel, target, attacker, existingElement, newElement, existingLevel);
                // 反应后移除被反应的元素标记（末影和另一个元素）
                removeElementMark(target, existingElement);
                removeElementMark(target, newElement);
                break;

            case UNKNOWN:
            default:
                // 未知反应，播放默认效果
                handleUnknownReaction(serverLevel, target, attacker, existingElement, newElement, existingLevel);
                break;
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

        return ReactionType.UNKNOWN;
    }

    // ==================== 具体反应效果实现 ====================

    /**
     * 冰火反应
     * 单次伤害加成
     * 加成后的伤害 = 原伤害 * (冰系加成 + 火系加成) / 2
     */
    private static void handleIceFire(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                       ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行冰火反应效果");

        if (attacker == null) return;

        // 获取冰系和火系加成（铁魔法属性默认值为1.0，需要减去基础值）
        double icePower = getAttributeValue(attacker, AttributeRegistry.ICE_SPELL_POWER);
        double firePower = getAttributeValue(attacker, AttributeRegistry.FIRE_SPELL_POWER);

        // 计算伤害倍数（减去基础值1.0后计算加成）
        double damageMultiplier = ((icePower - 1.0) + (firePower - 1.0)) / 2.0;
        float bonusDamage = 5.0f * (float) Math.max(0, damageMultiplier) * markLevel;

        // 造成额外伤害
        if (bonusDamage > 0) {
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
     * Buff等级 = 1 * (火系加成 + 毒系加成) / 2
     */
    private static void handlePoisonFire(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                          ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行木火反应效果");

        if (attacker == null) return;

        // 获取火系和毒系加成（铁魔法属性默认值为1.0，需要减去基础值）
        double firePower = getAttributeValue(attacker, AttributeRegistry.FIRE_SPELL_POWER);
        double poisonPower = getAttributeValue(attacker, AttributeRegistry.NATURE_SPELL_POWER);

        // 计算Buff等级（基于加成值，不是原始值）
        double powerBonus = ((firePower - 1.0) + (poisonPower - 1.0)) / 2.0;
        int buffLevel = (int) (powerBonus * 2.0); // 每0.5加成=1级
        buffLevel = Math.max(1, Math.min(buffLevel, 3)); // 限制在1-3级

        // 施加烈焰Buff (PyroFlameEffect)
        MobEffect pyroFlameEffect = ModEffects.PYRO_FLAME.get();
        Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(pyroFlameEffect);
        target.addEffect(new MobEffectInstance(
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
     * 伤害计算和"冰火"同理
     */
    private static void handleLightningFire(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                             ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行雷火反应效果");

        if (attacker == null) return;

        // 获取雷系和火系加成（铁魔法属性默认值为1.0，需要减去基础值）
        double lightningPower = getAttributeValue(attacker, AttributeRegistry.LIGHTNING_SPELL_POWER);
        double firePower = getAttributeValue(attacker, AttributeRegistry.FIRE_SPELL_POWER);

        // 计算伤害倍数（减去基础值1.0后计算加成）
        double damageMultiplier = ((lightningPower - 1.0) + (firePower - 1.0)) / 2.0;
        float lightningDamage = 8.0f * (float) Math.max(0, damageMultiplier) * markLevel;

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

        // 造成雷属性伤害
        if (lightningDamage > 0) {
            target.hurt(serverLevel.damageSources().lightningBolt(), lightningDamage);
        }

        debugLog(String.format("雷火反应召唤闪电造成 %.1f 伤害 (雷系: %.1f, 火系: %.1f)",
                lightningDamage, lightningPower, firePower));

        // 播放特效
        playLightningFireParticles(serverLevel, target);
    }

    /**
     * 神圣-猩红反应
     * 单次伤害加成，伤害计算和"冰火"同理
     */
    private static void handleHolyBlood(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                         ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行神圣-猩红反应效果");

        if (attacker == null) return;

        // 获取神圣和猩红加成（铁魔法属性默认值为1.0，需要减去基础值）
        double holyPower = getAttributeValue(attacker, AttributeRegistry.HOLY_SPELL_POWER);
        double bloodPower = getAttributeValue(attacker, AttributeRegistry.BLOOD_SPELL_POWER);

        // 计算伤害倍数（减去基础值1.0后计算加成）
        double damageMultiplier = ((holyPower - 1.0) + (bloodPower - 1.0)) / 2.0;
        float bonusDamage = 6.0f * (float) Math.max(0, damageMultiplier) * markLevel;

        // 造成额外伤害（神圣伤害）
        if (bonusDamage > 0) {
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
     * 等级计算：基于邪术和猩红加成的平均值
     */
    private static void handleEldritchBlood(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                             ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行邪术-猩红反应效果");

        if (attacker == null) return;

        // 获取邪术和猩红加成（铁魔法属性默认值为1.0，需要减去基础值）
        double eldritchPower = getAttributeValue(attacker, AttributeRegistry.ELDRITCH_SPELL_POWER);
        double bloodPower = getAttributeValue(attacker, AttributeRegistry.BLOOD_SPELL_POWER);

        // 计算Buff等级（基于加成值）
        double powerBonus = ((eldritchPower - 1.0) + (bloodPower - 1.0)) / 2.0;
        int buffLevel = (int) (powerBonus * 3.0); // 每0.33加成=1级
        buffLevel = Math.max(1, Math.min(buffLevel, 5)); // 限制在1-5级

        // 施加混沌Buff
        // 注意：ChaosBuffEffect现在在构造函数中已经添加了属性修饰符
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
     * 末影与任意反应
     * 给与施法者Buff"终末回响"
     * "终末回响"Buff：加成施法者法术抗性与法术强度
     * +末影加成/2的法术抗性
     * +末影加成/3的法术强度
     */
    private static void handleEnderAny(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                        ElementType existingElement, ElementType newElement, int markLevel) {
        debugLog("执行末影-任意反应效果");

        if (attacker == null) return;

        // 获取末影加成（铁魔法属性默认值为1.0）
        double enderPower = getAttributeValue(attacker, AttributeRegistry.ENDER_SPELL_POWER);

        // 计算加成值（减去基础值1.0后计算）
        double enderBonus = enderPower - 1.0;
        double spellPowerBonus = enderBonus * com.legendarymage.legendarymagemod.effect.EnderEchoBuffEffect.SPELL_POWER_RATIO;
        double spellResistBonus = enderBonus * com.legendarymage.legendarymagemod.effect.EnderEchoBuffEffect.SPELL_RESIST_RATIO;

        // 移除旧的效果（如果存在）
        removeOldEnderEchoEffect(attacker);

        // 施加终末回响Buff
        // 注意：EnderEchoBuffEffect在构造函数中添加了基础属性修饰符用于显示
        // 但实际效果由下面的addEnderEchoAttributeModifiers动态设置
        MobEffect enderEchoBuffEffect = ModEffects.ENDER_ECHO_BUFF.get();
        Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(enderEchoBuffEffect);
        int duration = 300; // 15秒
        
        // 创建效果实例
        MobEffectInstance effectInstance = new MobEffectInstance(
                effectHolder,
                duration,
                0,
                false, true, true
        );
        
        attacker.addEffect(effectInstance);
        
        // 手动添加动态属性修饰符（因为这个Buff的数值是基于施法者属性的）
        // 这些修饰符提供实际的游戏效果
        addEnderEchoAttributeModifiers(attacker, spellPowerBonus, spellResistBonus);

        debugLog(String.format("末影-任意反应给予终末回响Buff: 法术强度+%.1f%%, 法术抗性+%.1f%% (末影: %.1f)",
                spellPowerBonus * 100, spellResistBonus * 100, enderPower));

        // 播放特效
        playEnderAnyParticles(serverLevel, attacker);
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
        removeAttributeModifier(entity, AttributeRegistry.SPELL_POWER, ENDER_ECHO_SPELL_POWER_UUID);
        removeAttributeModifier(entity, AttributeRegistry.SPELL_RESIST, ENDER_ECHO_RESIST_UUID);
    }

    /**
     * 添加终末回响的动态属性修饰符
     */
    private static void addEnderEchoAttributeModifiers(LivingEntity entity, double spellPowerBonus, double spellResistBonus) {
        // 添加法术强度修饰符
        addAttributeModifier(entity, AttributeRegistry.SPELL_POWER, ENDER_ECHO_SPELL_POWER_UUID,
                "ender_echo_spell_power", spellPowerBonus);
        
        // 添加法术抗性修饰符
        addAttributeModifier(entity, AttributeRegistry.SPELL_RESIST, ENDER_ECHO_RESIST_UUID,
                "ender_echo_spell_resist", spellResistBonus);
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
     * 添加属性修饰符
     * 使用 ResourceLocation 作为标识符（1.21 API）
     *
     * @param entity 实体
     * @param attribute 属性
     * @param uuid UUID（转换为 ResourceLocation）
     * @param name 名称
     * @param amount 数值
     */
    private static void addAttributeModifier(LivingEntity entity, Holder<Attribute> attribute,
                                              UUID uuid, String name, double amount) {
        if (!entity.getAttributes().hasAttribute(attribute)) return;

        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            // 1.21 中 AttributeModifier 使用 ResourceLocation 作为标识符
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, name);
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
     * @param uuid UUID（转换为 ResourceLocation）
     */
    private static void removeAttributeModifier(LivingEntity entity, Holder<Attribute> attribute, UUID uuid) {
        if (!entity.getAttributes().hasAttribute(attribute)) return;

        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            // 1.21 中通过 ResourceLocation 移除修饰符
            // 我们需要遍历所有修饰符并找到匹配的
            for (AttributeModifier modifier : instance.getModifiers()) {
                // 检查修饰符的 ID 是否匹配（通过名称）
                if (modifier.id().getPath().contains("ender_echo")) {
                    instance.removeModifier(modifier.id());
                }
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
        POISON_FIRE,        // 木火：挂上烈焰Buff
        LIGHTNING_FIRE,     // 雷火：在实体位置落雷
        HOLY_BLOOD,         // 神圣-猩红：单次伤害加成
        ELDRITCH_BLOOD,     // 邪术-猩红：给与施法者Buff"混沌"
        ENDER_ANY,          // 末影与任意：给与施法者Buff"终末回响"
        UNKNOWN             // 未知
    }
}
