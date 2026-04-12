package com.legendarymage.legendarymagemod.client;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.trail.TrailManager;
import com.legendarymage.legendarymagemod.trail.TrailRenderer;
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
 * 拖尾特效客户端事件处理器
 * 负责在客户端更新和渲染所有活跃的拖尾效果
 *
 * 【职责】
 * 1. 每帧更新所有拖尾的状态（清理过期点、计算淡出等）
 * 2. 在渲染阶段绘制所有拖尾几何体（Triangle Strip）
 *
 * 【关键修复】
 * v1.0.1: 添加了 RenderLevelStageEvent 处理器，
 *       确保拖尾几何体真正被渲染到屏幕上！
 *
 * @author Love_U
 * @version 1.0.1 (修复版)
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = LegendaryMage.MODID, value = Dist.CLIENT)
public class TrailClientHandler {

    /**
     * 是否启用拖尾特效的全局开关
     */
    private static boolean trailEffectsEnabled = true;

    /**
     * 上次渲染的拖尾数量（用于调试日志）
     */
    private static int lastRenderedCount = 0;

    // ==================== 更新逻辑 ====================

    /**
     * 监听世界Tick事件（Post阶段）
     * 用于每帧更新所有拖尾效果的状态
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!trailEffectsEnabled) return;
        if (!event.getLevel().isClientSide()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // 获取拖尾管理器实例
        TrailManager manager = TrailManager.getInstance();

        // 计算deltaTime（秒）
        float deltaTime = 0.05f;

        // 更新所有活跃的拖尾效果
        manager.updateAll(deltaTime);

        // 【调试日志】每100tick输出一次状态（约5秒）
        if (mc.player.tickCount % 100 == 0) {
            int activeCount = manager.getActiveTrailCount();
            int totalCount = manager.getTotalTrailCount();

           LegendaryMage.LOGGER.info("========================================");
           LegendaryMage.LOGGER.info("[拖尾-ClientHandler] 📊 Tick更新报告");
           LegendaryMage.LOGGER.info("[拖尾-ClientHandler] 活跃拖尾数: {}", activeCount);
           LegendaryMage.LOGGER.info("[拖尾-ClientHandler] 总拖尾数: {}", totalCount);
           LegendaryMage.LOGGER.info("[拖尾-ClientHandler] 上次渲染数: {}", lastRenderedCount);

            if (activeCount > 0) {
                // 列出前3个活跃拖尾的信息
                int count = 0;
                for (var entry : new java.util.HashMap<>(manager.getTrails()).entrySet()) {
                    if (count >= 3) break;
                   LegendaryMage.LOGGER.info("  [{}] {}", count + 1, entry.getValue());
                    count++;
                }
            }
           LegendaryMage.LOGGER.info("========================================");
        }
    }

    // ==================== 渲染逻辑（核心修复）====================

    /**
     * 监听世界渲染阶段事件
     * 在正确的时机绘制所有拖尾几何体
     *
     * 【重要说明】
     * 必须在 AFTER_TRANSLUCENT_BLOCKS 阶段渲染，
     * 以确保：
     * 1. 拖尾在不透明方块之后（正确遮挡关系）
     * 2. 拖尾在半透明方块之前或同时（混合顺序合理）
     * 3. 拖尾在UI之前（不会被UI遮挡）
     *
     * @param event 渲染层级阶段事件
     */
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        // 检查是否启用
        if (!trailEffectsEnabled) return;

        // 只在半透明方块之后渲染
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        // 获取必要的渲染组件
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource =
                Minecraft.getInstance().renderBuffers().bufferSource();

        // 部分tick时间（用于平滑插值）
        // 使用固定值1.0f，因为拖尾每tick更新一次，不需要插值
        float partialTick = 1.0f;

        // 获取拖尾管理器
        TrailManager manager = TrailManager.getInstance();

        // 检查是否有需要渲染的拖尾
        int activeCount = manager.getActiveTrailCount();

        if (activeCount > 0) {
            // ====== 这里是核心！真正调用渲染器 ======
            try {
                // 【调试日志】确认渲染事件被触发
               LegendaryMage.LOGGER.info("[拖尾-渲染] 🎨 渲染事件触发！准备渲染 {} 个拖尾...", activeCount);

                // 使用 TrailManager 渲染所有活跃的拖尾
                // TrailManager内部会遍历所有拖尾并调用TrailRenderer逐个渲染
                manager.renderAll(poseStack, bufferSource, partialTick);

                // 提交缓冲区数据到GPU（非常重要！）
                bufferSource.endBatch();

                // 更新调试计数器
                lastRenderedCount = activeCount;

               LegendaryMage.LOGGER.info("[拖尾-渲染] ✅ 渲染完成！已提交到GPU");

            } catch (Exception e) {
                // 捕获并记录任何渲染错误，避免崩溃
               LegendaryMage.LOGGER.error("[拖尾渲染] ❌ 渲染过程中发生异常: {}", e.getMessage());
                e.printStackTrace();
            }
        } else {
            // 【调试日志】每5秒提示一次无拖尾
            if (Minecraft.getInstance().player.tickCount % 100 == 0) {
               LegendaryMage.LOGGER.warn("[拖尾-渲染] ⚠️ 当前没有活跃的拖尾需要渲染");
            }
        }
    }

    // ==================== 调试辅助方法 ====================

    /**
     * 获取上次渲染的拖尾数量（用于调试）
     *
     * @return 上次渲染调用时处理的拖尾数量
     */
    public static int getLastRenderedCount() {
        return lastRenderedCount;
    }

    /**
     * 设置拖尾特效全局开关
     *
     * @param enabled true=启用，false=禁用
     */
    public static void setTrailEffectsEnabled(boolean enabled) {
        trailEffectsEnabled = enabled;

       LegendaryMage.LOGGER.info("拖尾特效全局开关: {}", enabled ? "启用" : "禁用");

        if (!enabled) {
            TrailManager.getInstance().clearAll();
        }
    }

    /**
     * 获取拖尾特效是否启用
     *
     * @return 是否启用
     */
    public static boolean isTrailEffectsEnabled() {
        return trailEffectsEnabled;
    }
}
