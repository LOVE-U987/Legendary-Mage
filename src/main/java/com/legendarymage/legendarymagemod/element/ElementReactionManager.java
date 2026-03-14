package com.legendarymage.legendarymagemod.element;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.effect.ElementMarkEffect;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 元素反应管理器
 * 负责处理元素标记 Buff 的施加、升级和元素反应的触发
 * 
 * @author Love_U
 * @version 3.0.0
 */
public class ElementReactionManager {

    /**
     * 随机数生成器
     */
    private static final Random RANDOM = new Random();

    /**
     * 输出调试日志
     * 通过配置开关控制是否输出
     *
     * @param message 日志消息
     */
    private static void debugLog(String message) {
        if (com.legendarymage.legendarymagemod.Config.ELEMENT_REACTION_DEBUG_OUTPUT.get()) {
            LegendaryMage.LOGGER.info("[元素反应] {}", message);
        }
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
        // 检查目标是否已死亡或正在死亡
        if (!target.isAlive() || target.isDeadOrDying()) {
            debugLog(String.format("目标已死亡或正在死亡，跳过法术伤害处理: %s",
                    target.getName().getString()));
            return;
        }
        
        // 获取对应的元素类型
        ElementType elementType = ElementType.fromSchoolType(schoolType);
        
        if (elementType == null) {
            // 该流派没有对应的元素类型
            return;
        }
        
        debugLog(String.format("法术伤害: %s -> %s, 流派: %s, 对应元素: %s, 伤害: %.1f",
                attacker != null ? attacker.getName().getString() : "环境",
                target.getName().getString(),
                schoolType.getId().getPath(),
                elementType.getId(),
                damage));
        
        // 处理元素标记
        processElementMark(serverLevel, target, attacker, elementType);
    }

    /**
     * 处理元素伤害事件
     * 当实体受到特定元素伤害时调用，用于尝试升级对应元素标记
     *
     * @param serverLevel 服务器世界
     * @param target      被攻击的目标
     * @param attacker    攻击者
     * @param elementType 元素类型
     * @param damage      伤害值
     */
    public static void onElementDamage(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                        ElementType elementType, float damage) {
        // 检查目标是否已死亡或正在死亡，避免在死亡事件中触发标记升级
        if (!target.isAlive() || target.isDeadOrDying()) {
            debugLog(String.format("目标已死亡或正在死亡，跳过元素标记处理: %s",
                    target.getName().getString()));
            return;
        }
        
        debugLog(String.format("元素伤害: %s -> %s, 元素: %s, 伤害: %.1f",
                attacker != null ? attacker.getName().getString() : "环境",
                target.getName().getString(),
                elementType.getId(),
                damage));
        
        // 获取目标当前的该元素标记 Buff
        Holder<MobEffect> markEffect = getEffectHolder(elementType);
        MobEffectInstance existingEffect = target.getEffect(markEffect);
        
        if (existingEffect != null) {
            int currentAmplifier = existingEffect.getAmplifier();
            
            // 有50%概率升级标记
            if (ElementMarkEffect.tryUpgrade() && currentAmplifier < ElementMarkEffect.MAX_LEVEL) {
                // 升级 Buff
                target.removeEffect(markEffect);
                target.addEffect(new MobEffectInstance(
                        markEffect,
                        ElementMarkEffect.BASE_DURATION,
                        currentAmplifier + 1,
                        false,
                        true,
                        true
                ));
                
                debugLog(String.format("标记升级成功! %s 的 %s 标记升至 %d 级",
                        target.getName().getString(),
                        elementType.getId(),
                        currentAmplifier + 2));
                
                // 触发升级效果
                ElementReactionEffects.playUpgradeParticles(serverLevel, target, elementType, currentAmplifier + 2);
            } else {
                // 重置持续时间
                target.removeEffect(markEffect);
                target.addEffect(new MobEffectInstance(
                        markEffect,
                        ElementMarkEffect.BASE_DURATION,
                        currentAmplifier,
                        false,
                        true,
                        true
                ));
                
                debugLog(String.format("标记持续时间重置 (升级失败或已达最高级)"));
            }
        } else {
            // 如果没有该元素标记，则施加新的标记
            processElementMark(serverLevel, target, attacker, elementType);
        }
    }

    /**
     * 处理元素标记的施加逻辑
     * 允许多种元素标记同时存在
     *
     * @param serverLevel    服务器世界
     * @param target         目标实体
     * @param attacker       攻击者
     * @param newElementType 新施加的元素类型
     */
    private static void processElementMark(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                           ElementType newElementType) {
        // 检查目标是否已死亡或正在死亡
        if (!target.isAlive() || target.isDeadOrDying()) {
            return;
        }
        
        // 检查是否已有同类型标记
        Holder<MobEffect> newMarkEffect = getEffectHolder(newElementType);
        MobEffectInstance existingEffectOfSameType = target.getEffect(newMarkEffect);
        
        if (existingEffectOfSameType != null) {
            // 已有同类型标记，尝试升级
            int currentAmplifier = existingEffectOfSameType.getAmplifier();
            
            if (ElementMarkEffect.tryUpgrade() && currentAmplifier < ElementMarkEffect.MAX_LEVEL) {
                target.removeEffect(newMarkEffect);
                target.addEffect(new MobEffectInstance(
                        newMarkEffect,
                        ElementMarkEffect.BASE_DURATION,
                        currentAmplifier + 1,
                        false,
                        true,
                        true
                ));
                
                debugLog(String.format("同类型标记升级成功! %s 的 %s 标记升至 %d 级",
                        target.getName().getString(),
                        newElementType.getId(),
                        currentAmplifier + 2));
                
                ElementReactionEffects.playUpgradeParticles(serverLevel, target, newElementType, currentAmplifier + 2);
            } else {
                // 重置持续时间
                target.removeEffect(newMarkEffect);
                target.addEffect(new MobEffectInstance(
                        newMarkEffect,
                        ElementMarkEffect.BASE_DURATION,
                        currentAmplifier,
                        false,
                        true,
                        true
                ));
            }
            return;
        }
        
        // 获取目标已有的可以反应的元素标记
        List<ElementType> reactableMarks = getReactableMarks(target);
        
        // 检查是否有可以反应的标记
        if (!reactableMarks.isEmpty()) {
            // 随机选择一个可以反应的标记进行反应
            ElementType reactableElement = reactableMarks.get(RANDOM.nextInt(reactableMarks.size()));
            Holder<MobEffect> reactableEffectHolder = getEffectHolder(reactableElement);
            MobEffectInstance reactableEffect = target.getEffect(reactableEffectHolder);
            
            if (reactableEffect != null) {
                int reactableLevel = reactableEffect.getAmplifier() + 1; // 转换为1-3级
                
                debugLog(String.format("触发元素反应! %s(%d级) + %s",
                        reactableElement.getId(),
                        reactableLevel,
                        newElementType.getId()));
                
                // 触发元素反应
                ElementReactionEffects.handleReaction(serverLevel, target, attacker, 
                        reactableElement, newElementType, reactableLevel);
                
                // 播放反应粒子效果
                ElementReactionEffects.playReactionParticles(serverLevel, target, reactableElement, newElementType);
                
                // 注意：不再清除被反应的标记，允许多种标记同时存在
                // 如果需要清除，可以在这里添加逻辑
            }
        }
        
        // 施加新的标记（1级，amplifier = 0）
        // 这不会覆盖其他类型的标记
        target.addEffect(new MobEffectInstance(
                newMarkEffect,
                ElementMarkEffect.BASE_DURATION,
                0, // 等级1
                false,
                true,
                true
        ));
        
        debugLog(String.format("施加 %s 标记 (1级) 到 %s",
                newElementType.getId(),
                target.getName().getString()));
    }

    /**
     * 获取实体可以反应的元素标记
     * 等级 >= 2 (amplifier >= 1) 的标记可以反应
     *
     * @param target 目标实体
     * @return 可以反应的元素类型列表
     */
    private static List<ElementType> getReactableMarks(LivingEntity target) {
        List<ElementType> result = new ArrayList<>();
        
        for (ElementType elementType : ElementType.values()) {
            Holder<MobEffect> effectHolder = getEffectHolder(elementType);
            MobEffectInstance effect = target.getEffect(effectHolder);
            if (effect != null && effect.getAmplifier() >= 1) { // 等级 >= 2
                result.add(elementType);
            }
        }
        
        return result;
    }

    /**
     * 获取效果的 Holder
     *
     * @param elementType 元素类型
     * @return 效果的 Holder
     */
    private static Holder<MobEffect> getEffectHolder(ElementType elementType) {
        MobEffect effect = elementType.getMarkEffect();
        // 在 1.21 中，我们需要从注册表获取 Holder
        // 由于效果已经注册，我们可以直接返回一个直接的 Holder
        return net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect);
    }

    /**
     * 手动施加元素标记（用于命令或其他系统调用）
     *
     * @param serverLevel 服务器世界
     * @param target      目标实体
     * @param applier     施加者
     * @param elementType 元素类型
     * @param markLevel   标记等级（1-3）
     * @return 是否成功施加
     */
    public static boolean applyMark(ServerLevel serverLevel, LivingEntity target, LivingEntity applier,
                                    ElementType elementType, int markLevel) {
        // 检查目标是否已死亡或正在死亡
        if (!target.isAlive() || target.isDeadOrDying()) {
            debugLog(String.format("目标已死亡或正在死亡，跳过标记施加: %s",
                    target.getName().getString()));
            return false;
        }
        
        // 检查是否已有同类型标记
        Holder<MobEffect> markEffect = getEffectHolder(elementType);
        if (target.hasEffect(markEffect)) {
            debugLog(String.format("%s 已有 %s 标记，跳过施加",
                    target.getName().getString(),
                    elementType.getId()));
            return false;
        }
        
        // 施加标记
        int amplifier = Math.max(0, Math.min(markLevel - 1, ElementMarkEffect.MAX_LEVEL));
        target.addEffect(new MobEffectInstance(
                markEffect,
                ElementMarkEffect.BASE_DURATION,
                amplifier,
                false,
                true,
                true
        ));
        
        debugLog(String.format("手动施加 %s 标记 (%d级) 到 %s",
                elementType.getId(),
                amplifier + 1,
                target.getName().getString()));
        
        return true;
    }

    /**
     * 清除实体的所有元素标记
     *
     * @param serverLevel 服务器世界
     * @param target      目标实体
     */
    public static void clearAllMarks(ServerLevel serverLevel, LivingEntity target) {
        // 检查目标是否已死亡或正在死亡
        if (!target.isAlive() || target.isDeadOrDying()) {
            return;
        }
        
        for (ElementType elementType : ElementType.values()) {
            target.removeEffect(getEffectHolder(elementType));
        }
        
        debugLog(String.format("清除 %s 的所有元素标记", target.getName().getString()));
    }

    /**
     * 清除实体的指定类型元素标记
     *
     * @param serverLevel 服务器世界
     * @param target      目标实体
     * @param elementType 元素类型
     */
    public static void clearMark(ServerLevel serverLevel, LivingEntity target, ElementType elementType) {
        // 检查目标是否已死亡或正在死亡
        if (!target.isAlive() || target.isDeadOrDying()) {
            return;
        }
        
        target.removeEffect(getEffectHolder(elementType));
        
        debugLog(String.format("清除 %s 的 %s 标记", 
                target.getName().getString(), 
                elementType.getId()));
    }

    /**
     * 获取实体的元素标记信息
     *
     * @param serverLevel 服务器世界
     * @param target      目标实体
     * @return 标记信息字符串
     */
    public static String getMarkInfo(ServerLevel serverLevel, LivingEntity target) {
        StringBuilder sb = new StringBuilder();
        boolean hasMark = false;
        
        for (ElementType elementType : ElementType.values()) {
            Holder<MobEffect> effectHolder = getEffectHolder(elementType);
            MobEffectInstance effect = target.getEffect(effectHolder);
            if (effect != null) {
                if (hasMark) {
                    sb.append(", ");
                }
                int level = effect.getAmplifier() + 1;
                float seconds = effect.getDuration() / 20.0f;
                sb.append(String.format("%s(%d级, %.1fs)",
                        elementType.getId(),
                        level,
                        seconds));
                hasMark = true;
            }
        }
        
        return hasMark ? sb.toString() : "无元素标记";
    }

    /**
     * 获取实体所有元素标记的列表
     *
     * @param target 目标实体
     * @return 元素类型列表
     */
    public static List<ElementType> getAllMarks(LivingEntity target) {
        List<ElementType> result = new ArrayList<>();
        
        for (ElementType elementType : ElementType.values()) {
            if (target.hasEffect(getEffectHolder(elementType))) {
                result.add(elementType);
            }
        }
        
        return result;
    }

    /**
     * 每tick更新（现在不需要额外处理，因为 Buff 系统自己管理）
     *
     * @param serverLevel 服务器世界
     */
    public static void tick(ServerLevel serverLevel) {
        // Buff 系统会自动处理持续时间，这里不需要额外操作
        // 可以在这里添加一些全局的元素反应效果处理
    }
}
