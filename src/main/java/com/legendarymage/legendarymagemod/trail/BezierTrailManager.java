package com.legendarymage.legendarymagemod.trail;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.trail.color.HSVColor;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 贝塞尔拖尾管理器
 * 管理所有贝塞尔拖尾效果的创建、更新和销毁
 *
 * 【与Perception的区别】
 * - Perception：基于实体类型配置，工厂模式
 * - 我们：程序化创建，组件化配置
 *
 * @author Love_U
 * @version 1.0.6
 */
public class BezierTrailManager {

    private static final double TICK_DELTA = 0.05; // 20 TPS

    private static BezierTrailManager instance;

    public static BezierTrailManager getInstance() {
        if (instance == null) {
            instance = new BezierTrailManager();
        }
        return instance;
    }

    private final Map<String, BezierTrailEffect> activeTrails;

    private BezierTrailManager() {
        this.activeTrails = new ConcurrentHashMap<>();
    }

    // ==================== 创建拖尾 ====================

    /**
     * 创建新的拖尾效果
     *
     * @param id 拖尾ID
     * @param color 基础颜色
     * @param width 基础宽度
     * @param maxLifetime 最大生存时间（秒）
     * @return 创建的拖尾效果
     */
    public BezierTrailEffect createTrail(String id, HSVColor color, float width, double maxLifetime) {
        BezierTrailEffect trail = new BezierTrailEffect(id, color, width, maxLifetime);
        activeTrails.put(id, trail);
        LegendaryMage.LOGGER.debug("[贝塞尔拖尾] 创建拖尾: {}", id);
        return trail;
    }

    /**
     * 创建新的拖尾效果（自动生成ID）
     */
    public BezierTrailEffect createTrail(HSVColor color, float width, double maxLifetime) {
        String id = UUID.randomUUID().toString();
        return createTrail(id, color, width, maxLifetime);
    }

    /**
     * 创建彩虹拖尾
     */
    public BezierTrailEffect createRainbowTrail(String id, float width, double maxLifetime) {
        BezierTrailEffect trail = createTrail(id, HSVColor.RED, width, maxLifetime);
        trail.setColorAnimation(com.legendarymage.legendarymagemod.trail.animation.TimelineAnimation.rainbowCycle(2.0f, 1.0f, 1.0f));
        return trail;
    }

    // ==================== 更新 ====================

    /**
     * 更新所有拖尾
     * 应该在客户端tick事件中调用
     */
    public void updateAll() {
        updateAll(TICK_DELTA);
    }

    /**
     * 更新所有拖尾
     *
     * @param deltaTime 时间增量（秒）
     */
    public void updateAll(double deltaTime) {
        activeTrails.values().removeIf(trail -> {
            trail.update(deltaTime);

            // 移除已完成且已完全淡出的拖尾
            if (!trail.isActive() && trail.getCurrentColor().getAlpha() <= 0.01f) {
                LegendaryMage.LOGGER.debug("[贝塞尔拖尾] 移除拖尾: {}", trail.getId());
                return true;
            }

            return false;
        });
    }

    // ==================== 渲染 ====================

    /**
     * 渲染所有拖尾
     */
    public void renderAll(com.mojang.blaze3d.vertex.PoseStack poseStack,
                          net.minecraft.client.renderer.MultiBufferSource bufferSource,
                          float partialTick) {
        Vec3 cameraPos = net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        BezierTrailRenderer.getInstance().renderAll(activeTrails.values(), poseStack, bufferSource, partialTick, cameraPos);
    }

    // ==================== 查询 ====================

    /**
     * 根据ID获取拖尾
     */
    public BezierTrailEffect getTrail(String id) {
        return activeTrails.get(id);
    }

    /**
     * 获取所有活跃的拖尾
     */
    public Collection<BezierTrailEffect> getAllTrails() {
        return activeTrails.values();
    }

    /**
     * 获取活跃拖尾数量
     */
    public int getActiveTrailCount() {
        return activeTrails.size();
    }

    /**
     * 停止拖尾（不再添加新点）
     */
    public void stopTrail(String id) {
        BezierTrailEffect trail = activeTrails.get(id);
        if (trail != null) {
            trail.stop();
        }
    }

    /**
     * 移除拖尾
     */
    public void removeTrail(String id) {
        activeTrails.remove(id);
    }

    /**
     * 清除所有拖尾
     */
    public void clearAll() {
        activeTrails.clear();
        LegendaryMage.LOGGER.info("[贝塞尔拖尾] 清除所有拖尾");
    }
}
