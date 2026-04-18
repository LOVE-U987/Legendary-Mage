package com.legendarymage.legendarymagemod.event;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.effect.HolyMarkEffect;
import com.legendarymage.legendarymagemod.effect.ModEffects;
import com.legendarymage.legendarymagemod.effect.PyroFlameEffect;
import com.legendarymage.legendarymagemod.entity.ModEntities;
import com.legendarymage.legendarymagemod.spell.LivingIceSculptureEntity;
import com.legendarymage.legendarymagemod.spell.IceSculptureManager;
import com.legendarymage.legendarymagemod.spell.ResurrectionRuneManager;
import com.legendarymage.legendarymagemod.spell.BlizzardManager;
import com.legendarymage.legendarymagemod.spell.ElementalPrismManager;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * 模组事件处理器
 * 处理与复苏符文相关的事件
 * 
 * @author Love_U
 * @version 0.0.1
 */
@EventBusSubscriber(modid = LegendaryMage.MODID)
public class ModEvents {

    /**
     * 调试模式开关
     */
    private static final boolean DEBUG_MODE = false;

    /**
     * 清理计数器
     */
    private static int cleanupCounter = 0;

    /**
     * 清理间隔（tick）
     * 每 5 分钟（6000 tick）清理一次
     */
    private static final int CLEANUP_INTERVAL = 6000;

    /**
     * 输出调试日志
     * 
     * @param message 日志消息
     */
    private static void debugLog(String message) {
        if (DEBUG_MODE) {
            com.legendarymage.legendarymagemod.ModLogger.spell("[事件调试] {}", message);
        }
    }

    /**
     * 处理生物死亡事件
     * 1. 检查是否在复苏符文区域内，如果是则转化为亡灵
     * 2. 检查是否带有烈焰效果，如果是则触发爆炸
     * 
     * @param event 生物死亡事件
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        
        // 只在服务器端处理
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        
        // 清理 HolyMarkEffect 中的静态 Map 数据
        HolyMarkEffect.cleanupEntity(entity);
        
        // ========== 处理烈焰效果爆炸 ==========
        handlePyroFlameExplosion(entity);
        
        // ========== 处理复苏符文转化 ==========
        handleResurrectionRune(entity, serverLevel);
    }

    /**
     * 处理烈焰效果爆炸
     * 当带有烈焰效果的生物死亡时触发爆炸
     * 
     * @param entity 死亡的实体
     */
    private static void handlePyroFlameExplosion(LivingEntity entity) {
        // 检查是否带有烈焰效果
        MobEffectInstance pyroFlameEffect = entity.getEffect(ModEffects.PYRO_FLAME);
        
        if (pyroFlameEffect != null) {
            int amplifier = pyroFlameEffect.getAmplifier();
            
            // 触发爆炸
            PyroFlameEffect.triggerExplosion(entity.level(), entity, amplifier);
        }
    }

    /**
     * 处理复苏符文转化
     * 检查是否在复苏符文区域内，如果是则转化为亡灵
     * 
     * @param entity     死亡的实体
     * @param serverLevel 服务器世界
     */
    private static void handleResurrectionRune(LivingEntity entity, ServerLevel serverLevel) {
        // 获取管理器
        ResurrectionRuneManager manager = ResurrectionRuneManager.load(serverLevel);
        
        // 性能优化：如果没有活跃符文，直接返回
        if (manager.getActiveRuneCount() == 0) {
            return;
        }
        
        // 有活跃符文时才输出调试信息
        debugLog("========================================");
        debugLog("收到生物死亡事件 - 有活跃符文");
        debugLog("实体类型: " + entity.getType().getDescriptionId());
        debugLog("实体位置: [" + String.format("%.2f", entity.getX()) + ", " + 
                String.format("%.2f", entity.getY()) + ", " + 
                String.format("%.2f", entity.getZ()) + "]");
        debugLog("当前活跃符文数量: " + manager.getActiveRuneCount());
        
        // 处理转化
        LivingEntity undead = manager.handleEntityDeath(serverLevel, entity);
        
        if (undead != null) {
            debugLog("转化成功！亡灵实体已生成 - ID: " + undead.getId());
        } else {
            debugLog("转化未执行或失败");
        }
        
        debugLog("========================================");
    }

    /**
     * 处理世界tick事件
     * 更新所有复苏符文区域和冰雕场
     * 
     * @param event 世界tick事件
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        // 只在服务器端处理
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        
        // 定期清理过期数据
        cleanupCounter++;
        if (cleanupCounter >= CLEANUP_INTERVAL) {
            cleanupCounter = 0;
            HolyMarkEffect.cleanupExpiredCooldowns(serverLevel.getGameTime());
        }
        
        // 更新复苏符文区域
        ResurrectionRuneManager manager = ResurrectionRuneManager.load(serverLevel);
        manager.tick(serverLevel);
        
        // 更新冰雕场
        IceSculptureManager.onWorldTick(serverLevel);
        
        // 更新暴风雪区域
        BlizzardManager.tick(serverLevel);
        
        // 更新元素棱镜区域
        ElementalPrismManager.tick(serverLevel);
    }

    /**
     * 注册实体属性
     * 
     * @param event 实体属性创建事件
     */
    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        // 注册冰雕生物属性
        event.put(ModEntities.ICE_SCULPTURE.get(), LivingIceSculptureEntity.createAttributes().build());
    }
}
