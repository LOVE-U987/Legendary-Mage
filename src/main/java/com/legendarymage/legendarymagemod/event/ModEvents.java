package com.legendarymage.legendarymagemod.event;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.ModLogger;
import com.legendarymage.legendarymagemod.effect.ModEffects;
import com.legendarymage.legendarymagemod.effect.PyroFlameEffect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * 模组事件处理器
 * 处理元素反应相关事件
 * 
 * 【重写 v2.0】
 * - 移除 HolyMarkEffect 静态 Map 清理逻辑（现改用实体 NBT 存储冷却，随实体自动清理）
 * - 保留烈焰效果（木火反应产物）的死亡爆炸处理
 * 
 * @author Love_U
 */
@EventBusSubscriber(modid = LegendaryMage.MODID)
public class ModEvents {

    /**
     * 处理生物死亡事件
     * 检查是否带有烈焰效果（木火反应产物），如果是则触发爆炸
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
}
