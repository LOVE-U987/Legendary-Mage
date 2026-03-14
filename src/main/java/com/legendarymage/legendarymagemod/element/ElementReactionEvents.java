package com.legendarymage.legendarymagemod.element;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.school.ElementSchoolRegistry;
import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.Random;

/**
 * 元素反应事件处理器
 * 负责监听各种事件并触发元素反应系统
 * 
 * @author Love_U
 * @version 3.0.0
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
        if (com.legendarymage.legendarymagemod.Config.ELEMENT_REACTION_DEBUG_OUTPUT.get()) {
            LegendaryMage.LOGGER.info("[元素反应事件] {}", message);
        }
    }

    /**
     * 监听法术伤害事件（铁魔法模组）
     * 当实体受到铁魔法法术伤害时，施加对应的元素标记
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

        // 从法术伤害源获取攻击者和法术流派
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

        // 处理法术伤害，施加元素标记
        if (schoolType != null) {
            // 检查是否是元素流派的法术
            if (isElementSchool(schoolType)) {
                // 元素流派的法术：随机赋予冰、火、雷元素标记
                handleElementSchoolDamage(serverLevel, target, attacker, damage);
            } else {
                // 其他流派的法术：按正常逻辑处理
                ElementReactionManager.onSpellDamage(serverLevel, target, attacker, schoolType, damage);
            }
        }
    }

    /**
     * 检查是否是元素流派的法术
     *
     * @param schoolType 法术流派
     * @return 是否是元素流派
     */
    private static boolean isElementSchool(io.redspace.ironsspellbooks.api.spells.SchoolType schoolType) {
        // 检查是否是元素流派
        // 元素流派的ID是 "legendarymage:element"
        return schoolType.getId().toString().equals("legendarymage:element") ||
               schoolType.getId().getPath().equals("element");
    }

    /**
     * 处理元素流派的法术伤害
     * 随机赋予冰、火、雷元素标记
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

        // 使用 ElementReactionManager 处理元素伤害
        ElementReactionManager.onElementDamage(serverLevel, target, attacker, randomElement, damage);
    }

    /**
     * 监听实体受到伤害事件（通用，兼容其他模组）
     * 用于检测法术伤害并施加对应的元素标记
     * 
     * @param event 伤害事件
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        // 只在服务器端处理
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity target = event.getEntity();
        DamageSource damageSource = event.getSource();
        float damage = event.getNewDamage();

        // 首先检查是否是铁魔法的法术伤害（已经在 onSpellDamage 中处理过）
        if (damageSource instanceof SpellDamageSource) {
            // 铁魔法的法术伤害已经在 onSpellDamage 中处理
            return;
        }

        // 尝试从伤害源获取元素类型（兼容其他模组的法术）
        ElementType elementType = getElementTypeFromDamageSource(damageSource);
        
        if (elementType != null) {
            LivingEntity attacker = null;
            if (damageSource.getEntity() instanceof LivingEntity livingAttacker) {
                attacker = livingAttacker;
            }

            debugLog(String.format("检测到元素伤害(通用): %s -> %s, 元素: %s, 伤害: %.1f",
                    attacker != null ? attacker.getName().getString() : "环境",
                    target.getName().getString(),
                    elementType.getId(),
                    damage));

            // 处理元素伤害，施加或升级标记
            ElementReactionManager.onElementDamage(serverLevel, target, attacker, elementType, damage);
        }
    }

    /**
     * 监听世界tick事件
     * 更新所有元素标记的计时器
     * 
     * @param event 世界tick事件
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        // 只在服务器端处理
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        // 更新元素反应管理器
        ElementReactionManager.tick(serverLevel);
    }

    /**
     * 从伤害源获取元素类型
     * 根据伤害类型判断对应的元素
     * 支持多种常见的伤害类型命名，兼容其他模组
     *
     * @param damageSource 伤害源
     * @return 对应的元素类型，如果不是元素伤害则返回null
     */
    private static ElementType getElementTypeFromDamageSource(DamageSource damageSource) {
        if (damageSource == null) {
            return null;
        }

        String damageType = damageSource.type().msgId().toLowerCase();

        // 检查各种元素伤害类型 - 火系
        // 注意：排除 "onFire"，这是原版火焰伤害（如烈焰Buff造成的伤害），不应该刷新火焰标记
        if (damageType.contains("fire") || damageType.contains("flame") || damageType.contains("lava")
                || damageType.contains("burn") || damageType.contains("heat") || damageType.contains("inferno")) {
            // 排除原版火焰伤害 "onFire"，避免烈焰Buff的伤害刷新火焰标记
            if (!damageType.equals("onfire")) {
                return ElementType.FIRE;
            }
        }
        
        // 冰系
        if (damageType.contains("ice") || damageType.contains("frost") || damageType.contains("freeze") 
                || damageType.contains("cold") || damageType.contains("snow") || damageType.contains("chill")) {
            return ElementType.ICE;
        }
        
        // 雷系
        if (damageType.contains("lightning") || damageType.contains("thunder") || damageType.contains("electric") 
                || damageType.contains("shock") || damageType.contains("volt") || damageType.contains("storm")) {
            return ElementType.LIGHTNING;
        }
        
        // 毒系/自然系
        if (damageType.contains("poison") || damageType.contains("toxic") || damageType.contains("nature") 
                || damageType.contains("venom") || damageType.contains("acid") || damageType.contains("corrosive")) {
            return ElementType.POISON;
        }
        
        // 神圣系
        if (damageType.contains("holy") || damageType.contains("divine") || damageType.contains("light") 
                || damageType.contains("radiant") || damageType.contains("blessed") || damageType.contains("sacred")) {
            return ElementType.HOLY;
        }
        
        // 血系/黑暗系
        if (damageType.contains("blood") || damageType.contains("dark") || damageType.contains("wither") 
                || damageType.contains("shadow") || damageType.contains("necrotic") || damageType.contains("unholy")) {
            return ElementType.BLOOD;
        }
        
        // 末影系/虚空系
        if (damageType.contains("ender") || damageType.contains("void") || damageType.contains("teleport") 
                || damageType.contains("rift") || damageType.contains("dimensional")) {
            return ElementType.ENDER;
        }
        
        // 邪术系/诅咒系
        if (damageType.contains("eldritch") || damageType.contains("curse") || damageType.contains("magic") 
                || damageType.contains("arcane") || damageType.contains("spell") || damageType.contains("mystic")) {
            return ElementType.ELDRITCH;
        }

        return null;
    }
}
