package com.legendarymage.legendarymagemod.element;

import com.legendarymage.legendarymagemod.Config;
import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.ModLogger;
import com.legendarymage.legendarymagemod.command.ElementMappingHotReload;
import com.legendarymage.legendarymagemod.effect.EnderMarkEffect;
import com.legendarymage.legendarymagemod.effect.HolyMarkEffect;
import com.legendarymage.legendarymagemod.effect.IceMarkEffect;
import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.List;
import java.util.Random;

/**
 * 元素反应事件处理器
 * 负责监听各种事件并驱动元素异常系统
 *
 * 【重写 v3.1】
 * - 元素异常的给予判定由"对应流派"决定（不再根据伤害类型关键字匹配）
 * - 冰冻/神圣打击/回响打击等"攻击触发"效果仅在铁魔法法术命中时判定
 * - 黑暗异常 3 级以上每级 +5% 易伤（受伤倍率提升）
 * - 火焰异常死亡时按 buff 等级半径爆炸
 *
 * @author Love_U
 * @version 3.1.0
 */
@EventBusSubscriber(modid = LegendaryMage.MODID)
public class ElementReactionEvents {

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
        if (Config.ELEMENT_REACTION_DEBUG_OUTPUT.get()) {
            ModLogger.element("[元素反应事件] {}", message);
        }
    }

    /**
     * 监听法术伤害事件（铁魔法模组）
     * 当实体受到铁魔法法术伤害时：
     * 1. 按法术流派施加/更新对应的元素异常（75%概率）
     * 2. 判定冰冻/神圣打击/回响打击（仅法术触发）
     *
     * @param event 法术伤害事件
     */
    @SubscribeEvent
    public static void onSpellDamage(SpellDamageEvent event) {
        // 只在服务器端处理
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity target = event.getEntity();
        SpellDamageSource spellDamageSource = event.getSpellDamageSource();
        float damage = event.getAmount();

        // 从法术伤害源获取攻击者
        LivingEntity attacker = null;
        if (spellDamageSource != null && spellDamageSource.getEntity() instanceof LivingEntity livingAttacker) {
            attacker = livingAttacker;
        }

        // 获取法术流派
        io.redspace.ironsspellbooks.api.spells.SchoolType schoolType = null;
        if (spellDamageSource != null && spellDamageSource.spell() != null) {
            schoolType = spellDamageSource.spell().getSchoolType();
        }

        debugLog(String.format("收到铁魔法法术伤害事件: %s -> %s, 流派: %s, 伤害: %.1f",
                attacker != null ? attacker.getName().getString() : "未知",
                target.getName().getString(),
                schoolType != null ? schoolType.getId().getPath() : "未知",
                damage));

        // 1. 按流派施加元素异常
        if (schoolType != null) {
            if (isElementSchool(schoolType)) {
                // 元素流派法术：随机赋予冰、火、雷元素标记
                handleElementSchoolDamage(serverLevel, target, attacker, damage);
            } else {
                // 检查是否有自定义的元素标记映射（包括热加载的运行时映射）
                ResourceLocation schoolId = schoolType.getId();
                if (ElementMappingHotReload.hasMapping(schoolId)) {
                    // 使用自定义映射的元素标记（支持热加载）
                    handleCustomSchoolDamage(serverLevel, target, attacker, schoolId, damage);
                } else {
                    // 其他流派：按对应流派判定元素异常
                    ElementReactionManager.onSpellDamage(serverLevel, target, attacker, schoolType, damage);
                }
            }
        }

        // 2. 法术触发的特殊标记效果（冰冻/神圣打击/回响打击）
        triggerSpecialMarkEffects(target, attacker);
    }

    /**
     * 检查是否是元素流派的法术
     *
     * @param schoolType 法术流派
     * @return 是否是元素流派
     */
    private static boolean isElementSchool(io.redspace.ironsspellbooks.api.spells.SchoolType schoolType) {
        return schoolType.getId().toString().equals("legendarymage:element") ||
               schoolType.getId().getPath().equals("element");
    }

    /**
     * 处理元素流派的法术伤害
     * 随机赋予冰、火、雷元素标记（该流派的设计即为交替性元素伤害）
     *
     * @param serverLevel 服务器世界
     * @param target      目标实体
     * @param attacker    攻击者
     * @param damage      伤害值
     */
    private static void handleElementSchoolDamage(ServerLevel serverLevel, LivingEntity target,
                                                   LivingEntity attacker, float damage) {
        // 随机选择一个元素类型（火、冰、雷）
        ElementType[] elementTypes = {ElementType.FIRE, ElementType.ICE, ElementType.LIGHTNING};
        ElementType randomElement = elementTypes[RANDOM.nextInt(elementTypes.length)];

        debugLog(String.format("元素流派法术伤害: %s -> %s, 随机元素: %s, 伤害: %.1f",
                attacker != null ? attacker.getName().getString() : "未知",
                target.getName().getString(),
                randomElement.getId(),
                damage));

        ElementReactionManager.onElementDamage(serverLevel, target, attacker, randomElement, damage);
    }

    /**
     * 处理自定义流派的法术伤害
     * 根据数据包配置的映射施加对应的元素标记（使用热加载系统获取最新配置）
     *
     * @param serverLevel 服务器世界
     * @param target      目标实体
     * @param attacker    攻击者
     * @param schoolId    流派ID
     * @param damage      伤害值
     */
    private static void handleCustomSchoolDamage(ServerLevel serverLevel, LivingEntity target,
                                                  LivingEntity attacker, ResourceLocation schoolId, float damage) {
        // 使用热加载系统获取元素标记（自动合并数据包配置和运行时配置）
        List<ElementType> elementMarks = ElementMappingHotReload.getElementMarksForSchool(schoolId);

        if (elementMarks.isEmpty()) {
            return;
        }

        // 随机选择一个元素标记（如果配置了多个）
        ElementType elementType = elementMarks.get(RANDOM.nextInt(elementMarks.size()));

        debugLog(String.format("自定义流派法术伤害: %s -> %s, 流派: %s, 元素: %s, 伤害: %.1f",
                attacker != null ? attacker.getName().getString() : "未知",
                target.getName().getString(),
                schoolId,
                elementType.getId(),
                damage));

        ElementReactionManager.onElementDamage(serverLevel, target, attacker, elementType, damage);
    }

    /**
     * 监听实体受到伤害事件（前置阶段）
     * 1. 黑暗异常易伤：3级以上每一级 +5% 易伤（受伤倍率提升）
     * 2. 法术伤害的后备处理（SpellDamageEvent 未触发时的双路保障，
     *    元素施加由 ElementReactionManager 内部同 tick 去重）
     *
     * @param event 伤害事件
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        // 只在服务器端处理
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity target = event.getEntity();
        DamageSource damageSource = event.getSource();

        // 1. 黑暗异常易伤：3级以上每一级 +5% 易伤
        int bloodLevel = ElementMarkData.getMarkLevel(target, ElementType.BLOOD);
        if (bloodLevel >= com.legendarymage.legendarymagemod.effect.BloodMarkEffect.VULNERABILITY_THRESHOLD_LEVEL) {
            float multiplier = com.legendarymage.legendarymagemod.effect.BloodMarkEffect.calculateVulnerabilityMultiplier(bloodLevel);
            event.setNewDamage(event.getNewDamage() * multiplier);

            debugLog(String.format("%s 受到黑暗异常易伤加成: x%.2f (标记%d级)",
                    target.getName().getString(), multiplier, bloodLevel));
        }

        // 2. 法术伤害后备处理（双路保障）
        if (damageSource instanceof SpellDamageSource spellDamageSource) {
            LivingEntity attacker = null;
            if (spellDamageSource.getEntity() instanceof LivingEntity livingAttacker) {
                attacker = livingAttacker;
            }

            io.redspace.ironsspellbooks.api.spells.SchoolType schoolType = null;
            if (spellDamageSource.spell() != null) {
                schoolType = spellDamageSource.spell().getSchoolType();
            }

            if (schoolType != null) {
                ElementType elementType = ElementType.fromSchoolType(schoolType);
                if (elementType != null) {
                    debugLog(String.format("法术伤害(后备): %s -> %s, 流派: %s, 元素: %s, 伤害: %.1f",
                            attacker != null ? attacker.getName().getString() : "环境",
                            target.getName().getString(),
                            schoolType.getId().getPath(),
                            elementType.getId(),
                            event.getNewDamage()));
                    ElementReactionManager.onElementDamage(serverLevel, target, attacker, elementType, event.getNewDamage());
                }
            }
        }
    }

    /**
     * 监听实体死亡事件
     * 火焰异常：死亡后形成与 buff 等级大小相同范围的爆炸
     *
     * @param event 死亡事件
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // 只在服务器端处理
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity entity = event.getEntity();

        // 检查火焰异常等级（1-5），爆炸范围 = buff等级
        int fireLevel = ElementMarkData.getMarkLevel(entity, ElementType.FIRE);
        if (fireLevel <= 0) {
            return;
        }

        float radius = fireLevel;
        double x = entity.getX();
        double y = entity.getY() + entity.getBbHeight() * 0.5;
        double z = entity.getZ();

        // 播放爆炸粒子效果
        serverLevel.sendParticles(
                ParticleTypes.LAVA,
                x, y, z,
                (int) (radius * 5), 1.0, 1.0, 1.0,
                0.1
        );
        serverLevel.sendParticles(
                ParticleTypes.EXPLOSION,
                x, y, z,
                1, 0.5, 0.5, 0.5,
                0
        );

        // 创建爆炸（不破坏地形）
        serverLevel.explode(
                null,
                x, y, z,
                radius,
                Level.ExplosionInteraction.NONE
        );

        debugLog(String.format("%s 因火焰异常死亡，触发等级 %d 的爆炸",
                entity.getName().getString(), fireLevel));
    }

    /**
     * 监听世界tick事件
     * 更新元素反应管理器
     *
     * @param event 世界tick事件
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        // 只在服务器端处理
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        ElementReactionManager.tick(serverLevel);
    }

    /**
     * 触发特殊标记效果（仅铁魔法法术触发）
     * 统一处理冰冻、神圣打击和回响打击
     *
     * @param target   受到法术伤害的目标实体
     * @param attacker 造成伤害的攻击者（可为null）
     */
    private static void triggerSpecialMarkEffects(LivingEntity target, LivingEntity attacker) {
        // 冰冻异常：3级后的攻击 5% 概率冰冻3秒，CD 5秒
        IceMarkEffect.tryTriggerFreeze(target);

        // 光明异常：3级以上的攻击可触发神圣打击，CD 2秒（伤害由公式计算）
        HolyMarkEffect.tryTriggerHolyStrike(target, attacker);

        // 末影异常：3级以上的攻击可触发回响打击，CD 1秒（伤害由公式计算）
        EnderMarkEffect.tryTriggerEchoStrike(attacker, target);
    }
}
