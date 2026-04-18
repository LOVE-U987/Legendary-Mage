package com.legendarymage.legendarymagemod.client;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.trail.SimpleTrailManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * 简单拖尾客户端事件处理器
 * 类似Perception的事件处理方式
 *
 * @author Love_U
 * @version 1.0.0
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = LegendaryMage.MODID, value = Dist.CLIENT)
public class SimpleTrailClientHandler {

    private static boolean enabled = true;
    private static int tickCounter = 0;

    /**
     * 客户端Tick更新
     */
    @SubscribeEvent
    public static void onClientTick(LevelTickEvent.Post event) {
        if (!enabled) return;
        if (!event.getLevel().isClientSide()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // 更新所有拖尾
        SimpleTrailManager.getInstance().updateAll();

        // 每100tick输出调试信息
        tickCounter++;
        if (tickCounter % 100 == 0) {
            int count = SimpleTrailManager.getInstance().getActiveTrailCount();
            if (count > 0) {
                com.legendarymage.legendarymagemod.ModLogger.spellDebug("[简单拖尾] 活跃拖尾数: {}", count);
            }
        }
    }

    /**
     * 渲染拖尾
     */
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (!enabled) return;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        // 渲染所有拖尾
        SimpleTrailManager.getInstance().renderAll(poseStack, bufferSource, 1.0f);

        // 【关键】提交渲染，确保所有拖尾都被正确渲染
        // 注意：每个拖尾在renderTrail中已经完成自己的渲染提交
        bufferSource.endBatch();
    }

    public static void setEnabled(boolean enabled) {
        SimpleTrailClientHandler.enabled = enabled;
        if (!enabled) {
            SimpleTrailManager.getInstance().clearAll();
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }
}
