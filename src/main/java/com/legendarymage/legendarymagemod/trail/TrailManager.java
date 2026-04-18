package com.legendarymage.legendarymagemod.trail;

import com.legendarymage.legendarymagemod.LegendaryMage;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 拖尾效果管理器
 * 集中管理所有活跃的拖尾效果的完整生命周期
 *
 * 【设计模式】
 * 采用 **单例 + 工厂** 模式：
 * - 单例：全局唯一的拖尾管理器
 * - 工厂：提供便捷的拖尾创建方法
 *
 * 【线程安全】
 * 使用ConcurrentHashMap保证线程安全性，
 * 支持在多线程环境中安全地添加/移除拖尾效果。
 *
 * 【使用示例】
 * <pre>
 * // 获取管理器实例
 * TrailManager manager = TrailManager.getInstance();
 *
 * // 创建一个简单的火焰拖尾
 * TrailEffect fireTrail = manager.createTrail(
 *     "fire_spell_trail",
 *     TrailType.LINEAR,
 *     new Vector3f(1.0f, 0.3f, 0.0f),  // 橙红色
 *     2.0f,  // 宽度
 *     3.0    // 生命周期（秒）
 * );
 *
 * // 在游戏循环中更新和渲染
 * manager.updateAll(0.016);  // ~60fps
 * manager.renderAll(poseStack, bufferSource, partialTick);
 *
 * // 法术结束时清理
 * manager.removeTrail("fire_spell_trail");
 * </pre>
 *
 * 【性能特性】
 * - 时间复杂度: O(n) 更新, O(n) 渲染（n=活跃拖尾数）
 * - 自动清理已停用的拖尾，防止内存泄漏
 * - 支持最大数量限制，避免性能问题
 *
 * @author Love_U
 * @version 1.0.7
 */
public class TrailManager {

    /**
     * 单例实例（延迟初始化）
     */
    private static volatile TrailManager instance;

    /**
     * 所有活跃的拖尾效果映射 (ID → TrailEffect)
     * 使用ConcurrentHashMap保证线程安全
     */
    private final Map<String, TrailEffect> activeTrails;

    /**
     * 最大允许同时存在的拖尾数量
     * 超过此数量时，最旧的拖尾会被自动移除
     */
    private int maxTrails;

    /**
     * 全局默认最大生命周期（秒）
     * 新创建的拖尾如果未指定生命周期，使用此值
     */
    private double defaultMaxLifetime;

    /**
     * 是否启用调试输出
     */
    private boolean debugEnabled;

    /**
     * 私有构造函数（强制使用getInstance()）
     */
    private TrailManager() {
        this.activeTrails = new ConcurrentHashMap<>();
        this.maxTrails = 100;           // 默认最多100个拖尾
        this.defaultMaxLifetime = 3.0;   // 默认3秒生命周期
        this.debugEnabled = false;
    }

    /**
     * 获取TrailManager单例实例
     * 使用双重检查锁定（Double-Checked Locking）确保线程安全
     *
     * @return 唯一的TrailManager实例
     */
    public static TrailManager getInstance() {
        if (instance == null) {
            synchronized (TrailManager.class) {
                if (instance == null) {
                    instance = new TrailManager();
                }
            }
        }
        return instance;
    }

    // ==================== 工厂方法（便捷创建） ====================

    /**
     * 创建一个新的拖尾效果（简化版本）
     *
     * @param id 唯一标识符
     * @param type 拖尾类型
     * @param color RGB颜色
     * @param width 宽度（世界单位）
     * @param lifetimeSec 生命周期（秒）
     * @return 新创建的TrailEffect实例
     */
    public TrailEffect createTrail(String id, TrailType type, Vector3f color,
                                    float width, double lifetimeSec) {
        TrailEffect trail = new TrailEffect(id, type);
        trail.setColor(color);
        trail.setWidth(width);
        trail.setMaxLifetime(lifetimeSec);

        addTrail(trail);

        return trail;
    }

    /**
     * 创建一个带颜色渐变的拖尾效果
     *
     * @param id 唯一标识符
     * @param type 拖尾类型
     * @param startColor 起点颜色
     * @param endColor 终点颜色
     * @param width 宽度
     * @param lifetimeSec 生命周期（秒）
     * @return 新创建的TrailEffect实例
     */
    public TrailEffect createGradientTrail(String id, TrailType type,
                                            Vector3f startColor, Vector3f endColor,
                                            float width, double lifetimeSec) {
        TrailEffect trail = createTrail(id, type, startColor, width, lifetimeSec);
        trail.setEndColor(endColor);
        trail.setColorGradientEnabled(true);

        return trail;
    }

    /**
     * 创建一个发光拖尾效果（Additive混合）
     * 适用场景：激光、能量束、魔法光效
     *
     * @param id 唯一标识符
     * @param type 拖尾类型
     * @param color 发光颜色
     * @param width 宽度
     * @param lifetimeSec 生命周期（秒）
     * @return 新创建的TrailEffect实例（已配置为GLOW渲染模式）
     */
    public TrailEffect createGlowingTrail(String id, TrailType type,
                                            Vector3f color, float width, double lifetimeSec) {
        // 注意：渲染模式在渲染时指定，这里只配置外观属性
        TrailEffect trail = createTrail(id, type, color, width, lifetimeSec);
        trail.setFadeOutEnabled(false); // 发光拖尾通常不淡出

        return trail;
    }

    /**
     * 为法术快速创建拖尾的便捷方法
     * 自动生成ID并使用合理的默认参数
     *
     * @param spellName 法术名称（用于生成ID）
     * @param type 拖尾类型
     * @param color 颜色
     * @return 新创建的TrailEffect实例
     */
    public TrailEffect createSpellTrail(String spellName, TrailType type, Vector3f color) {
        String generatedId = spellName + "_" + System.currentTimeMillis();
        return createTrail(generatedId, type, color, 0.15f, defaultMaxLifetime);
    }

    // ==================== 生命周期管理 ====================

    /**
     * 添加拖尾到管理器
     * 如果已存在相同ID的拖尾，会先移除旧的
     *
     * @param trail 要添加的拖尾效果
     */
    public void addTrail(TrailEffect trail) {
        if (trail == null) {
            if (debugEnabled) {
                com.legendarymage.legendarymagemod.ModLogger.warn("尝试添加null拖尾效果");
            }
            return;
        }

        // 检查是否超过最大数量限制
        if (activeTrails.size() >= maxTrails) {
            removeOldestTrail();
        }

        // 如果存在同ID的旧拖尾，先移除
        if (activeTrails.containsKey(trail.getId())) {
            TrailEffect oldTrail = activeTrails.get(trail.getId());
            oldTrail.clear(); // 清理旧拖尾的数据

            if (debugEnabled) {
                LegendaryMage.LOGGER.debug("替换已有拖尾: {}", trail.getId());
            }
        }

        activeTrails.put(trail.getId(), trail);

        if (debugEnabled) {
            LegendaryMage.LOGGER.debug("添加拖尾效果: {} [总数: {}]", trail.getId(), activeTrails.size());
        }
    }

    /**
     * 根据ID移除拖尾效果
     * 会调用trail.clear()清理数据
     *
     * @param id 要移除的拖尾ID
     * @return 如果找到并移除返回true，否则false
     */
    public boolean removeTrail(String id) {
        TrailEffect removed = activeTrails.remove(id);

        if (removed != null) {
            removed.clear();

            if (debugEnabled) {
                LegendaryMage.LOGGER.debug("移除拖尾效果: {} [剩余: {}]", id, activeTrails.size());
            }
            return true;
        }

        return false;
    }

    /**
     * 移除最旧的拖尾效果（基于创建时间）
     * 当超过maxTrails限制时自动调用
     */
    private void removeOldestTrail() {
        String oldestId = null;
        long oldestTime = Long.MAX_VALUE;

        for (Map.Entry<String, TrailEffect> entry : activeTrails.entrySet()) {
            // 注意：我们无法直接获取creationTime（它是private的）
            // 这里使用一个简单策略：移除第一个找到的非活动拖尾
            // 或者如果都是活动的，移除第一个
            if (!entry.getValue().isActive()) {
                oldestId = entry.getKey();
                break;
            } else if (oldestId == null) {
                oldestId = entry.getKey();
            }
        }

        if (oldestId != null) {
            removeTrail(oldestId);
            if (debugEnabled) {
                com.legendarymage.legendarymagemod.ModLogger.warn("达到最大拖尾数量限制({})，自动移除最旧拖尾: {}", maxTrails, oldestId);
            }
        }
    }

    /**
     * 清理所有已停用的拖尾效果
     * 应该定期调用以释放内存
     *
     * @return 清理的数量
     */
    public int cleanupInactiveTrails() {
        int cleanedCount = 0;

        Iterator<Map.Entry<String, TrailEffect>> iterator =
                activeTrails.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, TrailEffect> entry = iterator.next();
            if (!entry.getValue().isActive() && entry.getValue().getPointCount() == 0) {
                iterator.remove();
                cleanedCount++;
            }
        }

        if (cleanedCount > 0 && debugEnabled) {
           com.legendarymage.legendarymagemod.ModLogger.spellDebug("清理了{}个不活跃的拖尾效果", cleanedCount);
        }

        return cleanedCount;
    }

    /**
     * 清空所有拖尾效果
     * 通常在世界卸载或模组重载时调用
     */
    public void clearAll() {
        for (TrailEffect trail : activeTrails.values()) {
            trail.clear();
        }
        activeTrails.clear();

        if (debugEnabled) {
            com.legendarymage.legendarymagemod.ModLogger.spell("清空所有拖尾效果");
        }
    }

    // ==================== 更新和渲染 ====================

    /**
     * 清理计时器
     * 用于控制清理频率，避免每帧都进行清理检查
     */
    private int cleanupTimer = 0;

    /**
     * 清理间隔（帧数）
     * 每60帧（约1秒@60fps）清理一次
     */
    private static final int CLEANUP_INTERVAL = 60;

    /**
     * 更新所有活跃的拖尾效果
     * 必须每帧调用一次
     *
     * @param deltaTime 距上一帧的时间（秒），通常为~0.016（60fps）
     */
    public void updateAll(double deltaTime) {
        // 批量更新所有拖尾
        for (TrailEffect trail : activeTrails.values()) {
            if (trail.isActive()) {
                trail.update(deltaTime);
            }
        }

        // 定期清理不活跃的拖尾（使用计数器而非时间取模，更精确）
        cleanupTimer++;
        if (cleanupTimer >= CLEANUP_INTERVAL) {
            cleanupTimer = 0;
            int cleaned = cleanupInactiveTrails();
            if (cleaned > 0 && debugEnabled) {
                com.legendarymage.legendarymagemod.ModLogger.spellDebug("[TrailManager] 清理了 {} 个不活跃拖尾", cleaned);
            }
        }
    }

    /**
     * 渲染所有活跃的拖尾效果
     * 应该在RenderLevelStageEvent.After事件中调用
     *
     * @param poseStack 位姿堆栈
     * @param bufferSource 多缓冲源
     * @param partialTick 部分tick时间
     */
    public void renderAll(com.mojang.blaze3d.vertex.PoseStack poseStack,
                           net.minecraft.client.renderer.MultiBufferSource bufferSource,
                           float partialTick) {
        // 使用新的几何体拖尾渲染器
        GeometryTrailRenderer.getInstance().renderAll(
                activeTrails.values(), poseStack, bufferSource, partialTick,
                net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera().getPosition()
        );
    }

    /**
     * 渲染所有活跃的拖尾效果（带自定义渲染模式）
     * 注意：几何体渲染器不支持渲染模式参数，此方法现在与普通renderAll行为相同
     *
     * @param poseStack 位姿堆栈
     * @param bufferSource 多缓冲源
     * @param partialTick 部分tick时间
     * @param mode 渲染模式（已弃用，保留参数以兼容旧代码）
     */
    public void renderAll(com.mojang.blaze3d.vertex.PoseStack poseStack,
                           net.minecraft.client.renderer.MultiBufferSource bufferSource,
                           float partialTick, TrailRenderer.RenderMode mode) {
        // 忽略mode参数，使用几何体渲染器
        renderAll(poseStack, bufferSource, partialTick);
    }

    // ==================== 查询方法 ====================

    /**
     * 根据ID获取拖尾效果
     *
     * @param id 拖尾ID
     * @return 对应的TrailEffect，如果不存在返回null
     */
    public TrailEffect getTrail(String id) {
        return activeTrails.get(id);
    }

    /**
     * 检查是否存在指定ID的拖尾
     *
     * @param id 拖尾ID
     * @return 如果存在且活跃返回true
     */
    public boolean hasTrail(String id) {
        TrailEffect trail = activeTrails.get(id);
        return trail != null && trail.isActive();
    }

    /**
     * 获取当前活跃拖尾的总数
     *
     * @return 活跃拖尾数量
     */
    public int getActiveTrailCount() {
        int count = 0;
        for (TrailEffect trail : activeTrails.values()) {
            if (trail.isActive()) count++;
        }
        return count;
    }

    /**
     * 获取所有拖尾的总数（包括不活跃的）
     *
     * @return 总数
     */
    public int getTotalTrailCount() {
        return activeTrails.size();
    }

    /**
     * 获取内部拖尾映射（用于调试和高级操作）
     * 注意：返回的是原始Map的副本，修改不会影响实际数据
     *
     * @return 包含所有拖尾的不可修改Map视图
     */
    public java.util.Map<String, TrailEffect> getTrails() {
        return java.util.Collections.unmodifiableMap(activeTrails);
    }

    // ==================== 配置方法 ====================

    /**
     * 设置最大允许的拖尾数量
     *
     * @param maxTrails 最大数量（必须在10-500之间）
     */
    public void setMaxTrails(int maxTrails) {
        this.maxTrails = Math.clamp(maxTrails, 10, 500);
    }

    /**
     * 获取最大拖尾数量
     */
    public int getMaxTrails() {
        return maxTrails;
    }

    /**
     * 设置默认最大生命周期
     *
     * @param lifetimeSec 默认生命周期（秒）
     */
    public void setDefaultMaxLifetime(double lifetimeSec) {
        this.defaultMaxLifetime = Math.max(lifetimeSec, 0.5);
    }

    /**
     * 获取默认最大生命周期
     */
    public double getDefaultMaxLifetime() {
        return defaultMaxLifetime;
    }

    /**
     * 启用或禁用调试日志
     *
     * @param enabled true=启用调试输出
     */
    public void setDebugEnabled(boolean enabled) {
        this.debugEnabled = enabled;
    }

    /**
     * 检查调试是否启用
     */
    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    // ==================== 统计信息 ====================

    /**
     * 获取管理器的统计信息（用于调试和性能监控）
     *
     * @return 格式化的统计字符串
     */
    public String getStatistics() {
        return String.format("TrailManager[总拖尾: %d, 活跃: %d, 最大: %d, 默认生命: %.1fs]",
                getTotalTrailCount(),
                getActiveTrailCount(),
                maxTrails,
                defaultMaxLifetime);
    }
}
