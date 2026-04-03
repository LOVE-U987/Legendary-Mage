package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.LegendaryMage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * 效果移除处理器
 * 用于处理效果移除时的延迟任务，避免 ConcurrentModificationException
 * 
 * 问题背景：
 * 当牛奶清除效果时，Minecraft 会遍历 activeEffects HashMap 并移除效果。
 * 如果在遍历过程中访问或修改这个 HashMap（即使是通过 getEffect() 读取），
 * 就会触发 ConcurrentModificationException。
 * 
 * 解决方案：
 * 将效果移除后的处理任务延迟到下一 tick 执行，确保 HashMap 的迭代已经完成。
 * 
 * @author Love_U
 * @version 1.0.5
 */
@EventBusSubscriber(modid = LegendaryMage.MODID)
public class EffectRemovalHandler {

    /**
     * 延迟任务列表
     * 存储需要在下一 tick 执行的任务
     */
    private static final List<DelayedEffectTask> delayedTasks = new ArrayList<>();

    /**
     * 添加延迟任务
     * 任务将在下一 tick 执行
     * 
     * @param entity 目标实体
     * @param amplifier 效果等级
     * @param handler 处理函数 (entity, amplifier) -> void
     */
    public static void addDelayedTask(LivingEntity entity, int amplifier, BiConsumer<LivingEntity, Integer> handler) {
        if (entity == null || !entity.isAlive() || entity.isDeadOrDying()) {
            return;
        }
        
        synchronized (delayedTasks) {
            delayedTasks.add(new DelayedEffectTask(entity, amplifier, handler));
        }
    }

    /**
     * 世界 tick 事件处理
     * 每 tick 执行延迟任务
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        // 复制任务列表以避免在迭代时修改
        List<DelayedEffectTask> tasksToExecute;
        synchronized (delayedTasks) {
            if (delayedTasks.isEmpty()) {
                return;
            }
            tasksToExecute = new ArrayList<>(delayedTasks);
            delayedTasks.clear();
        }

        // 执行任务
        Iterator<DelayedEffectTask> iterator = tasksToExecute.iterator();
        while (iterator.hasNext()) {
            DelayedEffectTask task = iterator.next();
            
            // 检查实体是否仍然有效
            LivingEntity entity = task.entity;
            if (entity == null || !entity.isAlive() || entity.isDeadOrDying()) {
                continue;
            }
            
            try {
                // 执行任务
                task.handler.accept(entity, task.amplifier);
            } catch (Exception e) {
                LegendaryMage.LOGGER.error("[EffectRemovalHandler] 执行延迟任务时发生错误: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * 延迟任务内部类
     */
    private static class DelayedEffectTask {
        final LivingEntity entity;
        final int amplifier;
        final BiConsumer<LivingEntity, Integer> handler;

        DelayedEffectTask(LivingEntity entity, int amplifier, BiConsumer<LivingEntity, Integer> handler) {
            this.entity = entity;
            this.amplifier = amplifier;
            this.handler = handler;
        }
    }
}
