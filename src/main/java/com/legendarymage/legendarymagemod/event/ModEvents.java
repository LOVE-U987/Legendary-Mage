package com.legendarymage.legendarymagemod.event;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.ModLogger;
import com.legendarymage.legendarymagemod.effect.HolyMarkEffect;
import com.legendarymage.legendarymagemod.effect.ModEffects;
import com.legendarymage.legendarymagemod.effect.PyroFlameEffect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * 模组事件处理器
 * 处理元素反应相关事件
 * 
 * @author Love_U
 */
@EventBusSubscriber(modid = LegendaryMage.MODID)
public class ModEvents {

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
     * 处理生物死亡事件
     * 1. 检查是否带有烈焰效果，如果是则触发爆炸
     * 
     * @param event 生物死亡事件
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        try {
            LivingEntity entity = event.getEntity();
            
            // 只在服务器端处理
            if (!(entity.level() instanceof ServerLevel serverLevel)) {
                return;
            }
            
            // 清理 HolyMarkEffect 中的静态 Map 数据
            HolyMarkEffect.cleanupEntity(entity);
            
            // ========== 处理烈焰效果爆炸 ==========
            handlePyroFlameExplosion(entity);
        } catch (Exception e) {
            ModLogger.error("[事件处理] onLivingDeath 发生异常", e);
        }
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
     * 处理世界tick事件
     * 
     * @param event 世界tick事件
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        try {
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
        } catch (Exception e) {
            ModLogger.error("[事件处理] onLevelTick 发生异常", e);
        }
    }
}

