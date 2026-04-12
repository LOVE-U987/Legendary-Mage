package com.legendarymage.legendarymagemod.client;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.trail.BezierTrailManager;
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
 * 贝塞尔拖尾客户端事件处理器
 * 全新架构，完全不同于Perception
 *
 * 【核心特性】
 * - 贝塞尔曲线平滑渲染
 * - HSV颜色空间
 * - 时间轴动画系统
 * - 距离/角度阈值控制
 *
 * @author Love_U
 * @version 2.0.0
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = LegendaryMage.MODID, value = Dist.CLIENT)
public class BezierTrailClientHandler {

    private static boolean enabled = true;
    private static int lastRenderedCount = 0;
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
        BezierTrailManager.getInstance().updateAll();

        // 每100tick输出调试信息
        tickCounter++;
        if (tickCounter % 100 == 0) {
            int count = BezierTrailManager.getInstance().getActiveTrailCount();
            if (count > 0) {
                LegendaryMage.LOGGER.info("[贝塞尔拖尾] 活跃拖尾数: {}", count);
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
        BezierTrailManager.getInstance().renderAll(poseStack, bufferSource, 1.0f);

        // 提交渲染
        bufferSource.endBatch();

        // 更新计数
        int count = BezierTrailManager.getInstance().getActiveTrailCount();
        if (count != lastRenderedCount) {
            lastRenderedCount = count;
            if (count > 0) {
                LegendaryMage.LOGGER.debug("[贝塞尔拖尾] 渲染 {} 个拖尾", count);
            }
        }
    }

    public static void setEnabled(boolean enabled) {
        BezierTrailClientHandler.enabled = enabled;
        if (!enabled) {
            BezierTrailManager.getInstance().clearAll();
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }
}
