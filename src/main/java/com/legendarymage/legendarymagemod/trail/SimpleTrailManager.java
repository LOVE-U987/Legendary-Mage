package com.legendarymage.legendarymagemod.trail;

import com.legendarymage.legendarymagemod.ModLogger;
import com.legendarymage.legendarymagemod.element.ElementType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简化版拖尾管理器 - 类似Perception的管理方式
 *
 * 【设计特点】
 * - 绑定到实体
 * - 实体死亡后自动淡出
 * - 工厂方法创建预设拖尾
 *
 * @author Love_U
 * @version 1.0.6
 */
public class SimpleTrailManager {

    private static final float TICK_DELTA = 0.05f;

    private static SimpleTrailManager instance;

    public static SimpleTrailManager getInstance() {
        if (instance == null) {
            instance = new SimpleTrailManager();
        }
        return instance;
    }

    private final Map<String, SimpleTrailEffect> trails;

    private SimpleTrailManager() {
        this.trails = new ConcurrentHashMap<>();
    }

    // ==================== 创建拖尾 ====================

    /**
     * 创建拖尾
     *
     * @param id     拖尾ID
     * @param entity 绑定的实体
     * @return 创建的拖尾
     */
    public SimpleTrailEffect createTrail(String id, Entity entity) {
        SimpleTrailEffect trail = new SimpleTrailEffect(id, entity);
        trails.put(id, trail);
        ModLogger.trailDebug("创建拖尾: {}", id);
        return trail;
    }

    /**
     * 创建箭头拖尾
     */
    public SimpleTrailEffect createArrowTrail(String id, Entity entity) {
        SimpleTrailEffect trail = SimpleTrailEffect.createArrowTrail(id, entity);
        trails.put(id, trail);
        return trail;
    }

    /**
     * 创建光谱箭拖尾
     */
    public SimpleTrailEffect createSpectralArrowTrail(String id, Entity entity) {
        SimpleTrailEffect trail = SimpleTrailEffect.createSpectralArrowTrail(id, entity);
        trails.put(id, trail);
        return trail;
    }

    /**
     * 创建三叉戟拖尾
     */
    public SimpleTrailEffect createTridentTrail(String id, Entity entity) {
        SimpleTrailEffect trail = SimpleTrailEffect.createTridentTrail(id, entity);
        trails.put(id, trail);
        return trail;
    }

    /**
     * 创建经验球拖尾
     */
    public SimpleTrailEffect createExperienceOrbTrail(String id, Entity entity) {
        SimpleTrailEffect trail = SimpleTrailEffect.createExperienceOrbTrail(id, entity);
        trails.put(id, trail);
        return trail;
    }

    /**
     * 创建烟花火箭拖尾
     */
    public SimpleTrailEffect createFireworkTrail(String id, Entity entity) {
        SimpleTrailEffect trail = SimpleTrailEffect.createFireworkTrail(id, entity);
        trails.put(id, trail);
        return trail;
    }

    // ==================== 元素拖尾 ====================

    /**
     * 根据元素类型创建拖尾
     */
    public SimpleTrailEffect createElementTrail(String id, Entity entity, ElementType elementType) {
        SimpleTrailEffect trail;
        switch (elementType) {
            case FIRE:
                trail = SimpleTrailEffect.createFireElementTrail(id, entity);
                break;
            case ICE:
                trail = SimpleTrailEffect.createIceElementTrail(id, entity);
                break;
            case LIGHTNING:
                trail = SimpleTrailEffect.createLightningElementTrail(id, entity);
                break;
            case POISON:
                trail = SimpleTrailEffect.createPoisonElementTrail(id, entity);
                break;
            case HOLY:
                trail = SimpleTrailEffect.createHolyElementTrail(id, entity);
                break;
            case BLOOD:
                trail = SimpleTrailEffect.createBloodElementTrail(id, entity);
                break;
            default:
                trail = SimpleTrailEffect.createArrowTrail(id, entity);
                break;
        }
        trails.put(id, trail);
        return trail;
    }

    /**
     * 创建火元素拖尾
     */
    public SimpleTrailEffect createFireElementTrail(String id, Entity entity) {
        SimpleTrailEffect trail = SimpleTrailEffect.createFireElementTrail(id, entity);
        trails.put(id, trail);
        return trail;
    }

    /**
     * 创建冰元素拖尾
     */
    public SimpleTrailEffect createIceElementTrail(String id, Entity entity) {
        SimpleTrailEffect trail = SimpleTrailEffect.createIceElementTrail(id, entity);
        trails.put(id, trail);
        return trail;
    }

    /**
     * 创建雷元素拖尾
     */
    public SimpleTrailEffect createLightningElementTrail(String id, Entity entity) {
        SimpleTrailEffect trail = SimpleTrailEffect.createLightningElementTrail(id, entity);
        trails.put(id, trail);
        return trail;
    }

    /**
     * 创建毒元素拖尾
     */
    public SimpleTrailEffect createPoisonElementTrail(String id, Entity entity) {
        SimpleTrailEffect trail = SimpleTrailEffect.createPoisonElementTrail(id, entity);
        trails.put(id, trail);
        return trail;
    }

    /**
     * 创建神圣元素拖尾
     */
    public SimpleTrailEffect createHolyElementTrail(String id, Entity entity) {
        SimpleTrailEffect trail = SimpleTrailEffect.createHolyElementTrail(id, entity);
        trails.put(id, trail);
        return trail;
    }

    /**
     * 创建血元素拖尾
     */
    public SimpleTrailEffect createBloodElementTrail(String id, Entity entity) {
        SimpleTrailEffect trail = SimpleTrailEffect.createBloodElementTrail(id, entity);
        trails.put(id, trail);
        return trail;
    }

    // ==================== 更新 ====================

    /**
     * 更新所有拖尾
     */
    public void updateAll() {
        updateAll(TICK_DELTA);
    }

    /**
     * 更新所有拖尾
     *
     * @param deltaTime 时间增量
     */
    public void updateAll(float deltaTime) {
        trails.values().removeIf(trail -> {
            trail.update(deltaTime);

            // 移除已完全淡出的拖尾
            if (trail.shouldRemove()) {
                ModLogger.trailDebug("移除拖尾: {}", trail.getId());
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
        SimpleTrailRenderer.getInstance().renderAll(new ArrayList<>(trails.values()), poseStack, bufferSource, partialTick, cameraPos);
    }

    // ==================== 查询 ====================

    public SimpleTrailEffect getTrail(String id) {
        return trails.get(id);
    }

    public List<SimpleTrailEffect> getAllTrails() {
        return new ArrayList<>(trails.values());
    }

    public int getActiveTrailCount() {
        return trails.size();
    }

    public void stopTrail(String id) {
        SimpleTrailEffect trail = trails.get(id);
        if (trail != null) {
            trail.stop();
        }
    }

    public void removeTrail(String id) {
        trails.remove(id);
    }

    public void clearAll() {
        trails.clear();
        ModLogger.trail("清除所有拖尾");
    }
}
