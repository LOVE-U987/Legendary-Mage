package com.legendarymage.legendarymagemod.element;

import com.legendarymage.legendarymagemod.Config;
import com.legendarymage.legendarymagemod.ModLogger;
import com.legendarymage.legendarymagemod.effect.ElementMarkEffect;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * 元素反应管理器
 * 负责处理元素异常 Buff 的施加、升级和元素反应的触发
 *
 * 【重写 v2.0】
 * - 攻击有 75% 概率给予元素异常 buff 或更新时长（旧版为 50% 升级）
 * - 异常 buff 上限 5 级，重复命中同元素异常时等级+1（满级仅刷新时长）
 * - 时长固定 5 秒
 * - 1 级即可参与元素反应（旧版需 2 级）
 * - 施法者由"对应流派"判定（ElementType.fromSchoolType），不依赖伤害类型
 * - 记录施法者 UUID（用于多人游戏伤害合并计算）
 * - 双路事件（SpellDamageEvent + LivingDamageEvent.Post）同 tick 去重，避免重复叠加
 *
 * @author Love_U
 * @version 2.0.0
 */
public class ElementReactionManager {

    /**
     * 随机数生成器
     */
    private static final Random RANDOM = new Random();

    /**
     * 元素类型到效果 Holder 的缓存映射
     * 避免每次查询都访问注册表，提升性能
     */
    private static final Map<ElementType, Holder<MobEffect>> EFFECT_HOLDER_CACHE = new EnumMap<>(ElementType.class);

    /**
     * 可反应元素列表缓存
     * 重用此列表避免每次创建新的 ArrayList
     */
    private static final List<ElementType> REACTABLE_MARKS_CACHE = new ArrayList<>(ElementType.values().length);

    /**
     * 上次法术伤害处理 tick 记录（去重用）
     * 目标UUID -> 上次处理的游戏tick
     */
    private static final Map<UUID, Long> LAST_PROCESSED_TICK = new HashMap<>();

    /**
     * 初始化效果 Holder 缓存
     */
    static {
        for (ElementType elementType : ElementType.values()) {
            MobEffect effect = elementType.getMarkEffect();
            if (effect != null) {
                EFFECT_HOLDER_CACHE.put(elementType, BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect));
            }
        }
    }

    /**
     * 输出调试日志
     * 通过配置开关控制是否输出
     *
     * @param message 日志消息
     */
    private static void debugLog(String message) {
        if (Config.ELEMENT_REACTION_DEBUG_OUTPUT.get()) {
            ModLogger.element("[元素反应] {}", message);
        }
    }

    /**
     * 处理法术伤害事件
     * 当实体受到法术伤害时调用，根据"对应流派"判定元素类型并施加/更新异常 Buff
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
            return;
        }

        // 由对应流派判定元素类型
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

        processElementHit(serverLevel, target, attacker, elementType);
    }

    /**
     * 处理元素伤害事件（已确定元素类型）
     * 当实体受到特定元素攻击时调用，用于尝试施加或更新对应元素异常
     *
     * @param serverLevel 服务器世界
     * @param target      被攻击的目标
     * @param attacker    攻击者
     * @param elementType 元素类型
     * @param damage      伤害值
     */
    public static void onElementDamage(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                        ElementType elementType, float damage) {
        // 检查目标是否已死亡或正在死亡
        if (!target.isAlive() || target.isDeadOrDying()) {
            return;
        }

        debugLog(String.format("元素伤害: %s -> %s, 元素: %s, 伤害: %.1f",
                attacker != null ? attacker.getName().getString() : "环境",
                target.getName().getString(),
                elementType.getId(),
                damage));

        processElementHit(serverLevel, target, attacker, elementType);
    }

    /**
     * 核心处理逻辑
     * 75% 概率给予元素异常 buff 或更新时长
     *
     * @param serverLevel 服务器世界
     * @param target      被攻击的目标
     * @param attacker    攻击者
     * @param elementType 元素类型
     */
    private static void processElementHit(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                           ElementType elementType) {
        // 同 tick 去重：SpellDamageEvent 与 LivingDamageEvent.Post 会在同一法术命中时双路触发
        if (isDuplicateHit(serverLevel, target)) {
            return;
        }

        // 75% 概率判定
        if (!ElementMarkEffect.shouldApplyMark()) {
            debugLog(String.format("75%%判定失败，%s 未施加/更新 %s 标记",
                    target.getName().getString(), elementType.getId()));
            return;
        }

        Holder<MobEffect> markHolder = getEffectHolder(elementType);
        MobEffectInstance existing = target.getEffect(markHolder);

        if (existing != null) {
            // 已有同类型标记：等级+1（上限5级），时长刷新为5秒
            int newAmplifier = Math.min(existing.getAmplifier() + 1, ElementMarkEffect.MAX_LEVEL);
            target.addEffect(new MobEffectInstance(
                    markHolder,
                    ElementMarkEffect.BASE_DURATION,
                    newAmplifier,
                    false,
                    true,
                    true
            ));

            if (newAmplifier > existing.getAmplifier()) {
                debugLog(String.format("标记升级成功! %s 的 %s 标记升至 %d 级",
                        target.getName().getString(),
                        elementType.getId(),
                        newAmplifier + 1));
                ElementReactionEffects.playUpgradeParticles(serverLevel, target, elementType, newAmplifier + 1);
            } else {
                debugLog(String.format("%s 的 %s 标记已达最高级 %d 级，仅刷新时长",
                        target.getName().getString(), elementType.getId(), newAmplifier + 1));
            }
        } else {
            // 无同类型标记：检查是否触发元素反应（1级即可参与反应）
            ElementType reactable = pickReactableMark(target);
            if (reactable != null) {
                int reactableLevel = ElementMarkData.getMarkLevel(target, reactable);

                debugLog(String.format("触发元素反应! %s(%d级) + %s",
                        reactable.getId(),
                        reactableLevel,
                        elementType.getId()));

                // 触发元素反应（内部会移除参与反应的两个元素标记）
                ElementReactionEffects.handleReaction(serverLevel, target, attacker,
                        reactable, elementType, reactableLevel);
                ElementReactionEffects.playReactionParticles(serverLevel, target, reactable, elementType);
            }

            // 施加新的标记（1级，amplifier = 0）
            target.addEffect(new MobEffectInstance(
                    markHolder,
                    ElementMarkEffect.BASE_DURATION,
                    0,
                    false,
                    true,
                    true
            ));

            debugLog(String.format("施加 %s 标记 (1级) 到 %s",
                    elementType.getId(),
                    target.getName().getString()));
        }

        // 记录施法者（多人游戏伤害合并计算需要）
        if (attacker != null) {
            ElementMarkData.setCaster(target, elementType, attacker);
        }
    }

    /**
     * 同 tick 去重
     * SpellDamageEvent 与 LivingDamageEvent.Post 会对同一法术命中双路触发，
     * 这里以（目标UUID, 游戏tick）为键只处理一次，避免异常等级被重复叠加。
     *
     * @param serverLevel 服务器世界
     * @param target      目标实体
     * @return 是否为本 tick 内重复处理
     */
    private static boolean isDuplicateHit(ServerLevel serverLevel, LivingEntity target) {
        long now = serverLevel.getGameTime();
        Long last = LAST_PROCESSED_TICK.get(target.getUUID());
        if (last != null && last == now) {
            return true;
        }
        LAST_PROCESSED_TICK.put(target.getUUID(), now);

        // 定期清理过期记录，避免内存泄漏
        if (now % 1000 == 0) {
            LAST_PROCESSED_TICK.entrySet().removeIf(entry -> entry.getValue() < now - 100);
        }
        return false;
    }

    /**
     * 随机选择一个目标身上可反应的元素标记（1级即可参与反应）
     *
     * @param target 目标实体
     * @return 可反应的元素类型，没有则返回null
     */
    private static ElementType pickReactableMark(LivingEntity target) {
        REACTABLE_MARKS_CACHE.clear();

        for (ElementType elementType : ElementType.values()) {
            Holder<MobEffect> effectHolder = getEffectHolder(elementType);
            MobEffectInstance effect = target.getEffect(effectHolder);
            if (effect != null) { // 1级即可反应
                REACTABLE_MARKS_CACHE.add(elementType);
            }
        }

        if (REACTABLE_MARKS_CACHE.isEmpty()) {
            return null;
        }
        return REACTABLE_MARKS_CACHE.get(RANDOM.nextInt(REACTABLE_MARKS_CACHE.size()));
    }

    /**
     * 获取效果的 Holder
     *
     * @param elementType 元素类型
     * @return 效果的 Holder
     */
    private static Holder<MobEffect> getEffectHolder(ElementType elementType) {
        Holder<MobEffect> cached = EFFECT_HOLDER_CACHE.get(elementType);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中（理论上不应该发生），动态创建并缓存
        MobEffect effect = elementType.getMarkEffect();
        if (effect != null) {
            cached = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect);
            EFFECT_HOLDER_CACHE.put(elementType, cached);
        }
        return cached;
    }

    /**
     * 手动施加元素标记（用于命令或其他系统调用）
     * 直接设定指定等级（1-5），并记录施法者
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
        // 检查目标是否已死亡或正在死亡
        if (!target.isAlive() || target.isDeadOrDying()) {
            debugLog(String.format("目标已死亡或正在死亡，跳过标记施加: %s",
                    target.getName().getString()));
            return false;
        }

        // 施加标记（直接覆盖为指定等级）
        Holder<MobEffect> markEffect = getEffectHolder(elementType);
        int amplifier = Math.max(0, Math.min(markLevel - 1, ElementMarkEffect.MAX_LEVEL));
        target.addEffect(new MobEffectInstance(
                markEffect,
                ElementMarkEffect.BASE_DURATION,
                amplifier,
                false,
                true,
                true
        ));

        // 记录施法者
        if (applier != null) {
            ElementMarkData.setCaster(target, elementType, applier);
        }

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
     * 每tick更新（Buff 系统会自动管理持续时间，这里不需要额外操作）
     *
     * @param serverLevel 服务器世界
     */
    public static void tick(ServerLevel serverLevel) {
        // Buff 系统会自动处理持续时间
    }
}
