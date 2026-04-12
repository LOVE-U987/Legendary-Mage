package com.legendarymage.legendarymagemod.trail;

import com.legendarymage.legendarymagemod.LegendaryMage;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * 拖尾效果核心类
 * 管理单个拖尾效果的完整生命周期：创建→更新→渲染→销毁
 *
 * 【设计理念】
 * 此类采用"轨迹历史记录"模式：
 * - 维护一个固定大小的点队列（Deque）
 * - 每帧添加新点，自动移除过期点
 * - 渲染时遍历所有点绘制几何体
 *
 * 【使用示例】
 * <pre>
 * // 创建拖尾效果
 * TrailEffect trail = new TrailEffect(TrailType.LINEAR);
 * trail.setMaxLifetime(2.0);  // 2秒生命周期
 * trail.setMaxPoints(50);      // 最多50个点
 * trail.setColor(new Vector3f(1.0f, 0.5f, 0.0f));  // 橙色
 * trail.setWidth(0.2f);        // 宽度
 *
 * // 在游戏循环中更新
 * while (isActive) {
 *     trail.addPoint(currentPosition);
 *     trail.update(deltaTime);  // 自动清理过期点
 * }
 * </pre>
 *
 * 【性能特性】
 * - 时间复杂度: O(n) 更新, O(n) 渲染（n=点数）
 * - 空间复杂度: O(maxPoints)
 * - 使用ArrayDeque保证高效的头部/尾部操作
 *
 * @author Love_U
 * @version 1.0.6
 */
public class TrailEffect {

    /**
     * 唯一标识符（用于调试和管理）
     */
    private final String id;

    /**
     * 拖尾类型
     */
    private TrailType type;

    /**
     * 轨迹点历史记录（按时间排序，最新的在尾部）
     */
    private final Deque<TrailPoint> points;

    /**
     * 最大生命周期（秒），超过此时间的点会被移除
     */
    private double maxLifetime;

    /**
     * 最大点数限制（防止内存溢出）
     */
    private int maxPoints;

    /**
     * 基础颜色（RGB, 0.0-1.0）
     */
    private Vector3f baseColor;

    /**
     * 基础宽度（世界单位）
     */
    private float baseWidth;

    /**
     * 是否激活状态
     */
    private boolean active;

    /**
     * 创建时间戳
     */
    private final long creationTime;

    /**
     * 总生命周期计时器（秒）
     */
    private double totalLifetime;

    /**
     * 是否启用淡出效果
     */
    private boolean fadeOutEnabled;

    /**
     * 淡出开始时间（占总生命周期的比例，0.0-1.0）
     */
    private float fadeOutStartRatio;

    /**
     * 是否启用颜色渐变（从起点到终点）
     */
    private boolean colorGradientEnabled;

    /**
     * 终点颜色（如果启用渐变）
     */
    private Vector3f endColor;

    /**
     * 宽度衰减因子（每秒宽度减少的比例，0.0=不衰减）
     */
    private float widthDecayRate;

    /**
     * 构造函数 - 最简版本（使用默认参数）
     *
     * @param type 拖尾类型
     */
    public TrailEffect(TrailType type) {
        this("trail_" + System.currentTimeMillis(), type);
    }

    /**
     * 构造函数 - 带ID版本
     *
     * @param id 唯一标识符
     * @param type 拖尾类型
     */
    public TrailEffect(String id, TrailType type) {
        this.id = id;
        this.type = type;
        this.points = new ArrayDeque<>();
        this.maxLifetime = 1.0;      // 默认1秒
        this.maxPoints = 30;         // 默认30个点
        this.baseColor = new Vector3f(1.0f, 1.0f, 1.0f);  // 默认白色
        this.baseWidth = 0.1f;       // 默认宽度
        this.active = true;
        this.creationTime = System.nanoTime();
        this.totalLifetime = 0.0;
        this.fadeOutEnabled = true;   // 默认启用淡出
        this.fadeOutStartRatio = 0.7f; // 最后30%开始淡出
        this.colorGradientEnabled = false;
        this.endColor = new Vector3f(1.0f, 1.0f, 1.0f);
        this.widthDecayRate = 0.0f;   // 默认不衰减
    }

    // ==================== 核心方法 ====================

    /**
     * 添加新的轨迹点
     * 会自动应用当前的颜色、宽度和透明度设置
     *
     * @param position 世界坐标位置
     */
    public void addPoint(Vec3 position) {
        if (!active || points.size() >= maxPoints) return;

        // 计算透明度（基于淡出设置）
        float alpha = calculateAlpha();

        // 计算当前宽度（基于衰减）
        float currentWidth = calculateCurrentWidth();

        // 计算当前颜色（基于渐变设置）
        Vector3f currentColor = calculateCurrentColor();

        TrailPoint point = new TrailPoint(position, currentColor, alpha, currentWidth);
        points.addLast(point);
    }

    /**
     * 添加带自定义属性的轨迹点
     *
     * @param position 世界坐标位置
     * @param color 自定义颜色
     * @param alpha 自定义透明度
     * @param width 自定义宽度
     */
    public void addPoint(Vec3 position, Vector3f color, float alpha, float width) {
        if (!active || points.size() >= maxPoints) return;

        TrailPoint point = new TrailPoint(position, color, alpha, width);
        points.addLast(point);
    }

    /**
     * 更新拖尾状态
     * 必须每帧调用以清理过期点和更新状态
     *
     * @param deltaTime 距上一帧的时间（秒）
     */
    public void update(double deltaTime) {
        if (!active) return;

        // 更新总生命周期
        totalLifetime += deltaTime;

        // 清理过期的点
        removeExpiredPoints();

        // 检查是否应该停用
        if (points.isEmpty() && totalLifetime > maxLifetime) {
            active = false;
        }
    }

    /**
     * 移除所有过期的轨迹点
     * 基于maxLifetime进行判断
     */
    private void removeExpiredPoints() {
        long currentTimeNanos = System.nanoTime();
        long maxLifetimeNanos = (long) (maxLifetime * 1_000_000_000L);

        Iterator<TrailPoint> iterator = points.iterator();
        while (iterator.hasNext()) {
            TrailPoint point = iterator.next();
            if ((currentTimeNanos - point.timestamp) > maxLifetimeNanos) {
                iterator.remove();
            }
        }
    }

    /**
     * 立即停止此拖尾效果
     * 不会立即清除点，而是让它们自然淡出
     */
    public void stop() {
        active = false;
    }

    /**
     * 立即清除所有轨迹点并停用
     */
    public void clear() {
        points.clear();
        active = false;
    }

    // ==================== 属性计算方法 ====================

    /**
     * 计算当前透明度（考虑淡出效果）
     *
     * @return 透明度值 (0.0-1.0)
     */
    private float calculateAlpha() {
        if (!fadeOutEnabled || maxLifetime <= 0) {
            return 1.0f;
        }

        double fadeStartTime = maxLifetime * fadeOutStartRatio;
        if (totalLifetime < fadeStartTime) {
            return 1.0f;
        }

        // 线性淡出
        double fadeProgress = (totalLifetime - fadeStartTime) / (maxLifetime - fadeStartTime);
        return Math.max(0.0f, 1.0f - (float) fadeProgress);
    }

    /**
     * 计算当前宽度（考虑衰减效果）
     *
     * @return 当前宽度值
     */
    private float calculateCurrentWidth() {
        if (widthDecayRate <= 0.0f) {
            return baseWidth;
        }

        // 指数衰减: width = baseWidth * e^(-decayRate * time)
        float decayedWidth = (float) (baseWidth * Math.exp(-widthDecayRate * totalLifetime));
        return Math.max(decayedWidth, 0.001f); // 最小宽度
    }

    /**
     * 计算当前颜色（考虑渐变效果）
     *
     * @return 当前颜色向量
     */
    private Vector3f calculateCurrentColor() {
        if (!colorGradientEnabled || maxLifetime <= 0) {
            return baseColor;
        }

        // 线性插值从baseColor到endColor
        float t = Math.min(1.0f, (float) (totalLifetime / maxLifetime));
        return new Vector3f(
                baseColor.x + (endColor.x - baseColor.x) * t,
                baseColor.y + (endColor.y - baseColor.y) * t,
                baseColor.z + (endColor.z - baseColor.z) * t
        );
    }

    // ==================== Getter和Setter方法 ====================

    /**
     * 获取唯一标识符
     */
    public String getId() {
        return id;
    }

    /**
     * 获取拖尾类型
     */
    public TrailType getType() {
        return type;
    }

    /**
     * 设置拖尾类型
     *
     * @param type 新的拖尾类型
     */
    public void setType(TrailType type) {
        this.type = type;
    }

    /**
     * 获取轨迹点列表（只读视图）
     *
     * @return 轨迹点的不可修改视图
     */
    public Iterable<TrailPoint> getPoints() {
        return () -> points.iterator(); // 返回迭代器而非集合本身，保护内部数据
    }

    /**
     * 获取当前点数
     */
    public int getPointCount() {
        return points.size();
    }

    /**
     * 获取最大生命周期（秒）
     */
    public double getMaxLifetime() {
        return maxLifetime;
    }

    /**
     * 设置最大生命周期（秒）
     *
     * @param maxLifetime 生命周期长度（必须>0）
     */
    public void setMaxLifetime(double maxLifetime) {
        this.maxLifetime = Math.max(maxLifetime, 0.1); // 最小0.1秒
    }

    /**
     * 获取最大点数
     */
    public int getMaxPoints() {
        return maxPoints;
    }

    /**
     * 设置最大点数限制
     *
     * @param maxPoints 最大点数（必须在10-500之间）
     */
    public void setMaxPoints(int maxPoints) {
        this.maxPoints = Math.clamp(maxPoints, 10, 500);
    }

    /**
     * 获取基础颜色
     */
    public Vector3f getColor() {
        return baseColor;
    }

    /**
     * 设置基础颜色
     *
     * @param color RGB颜色向量 (0.0-1.0)
     */
    public void setColor(Vector3f color) {
        this.baseColor = color;
    }

    /**
     * 获取基础宽度
     */
    public float getWidth() {
        return baseWidth;
    }

    /**
     * 设置基础宽度
     *
     * @param width 宽度值（世界单位，必须>0）
     */
    public void setWidth(float width) {
        this.baseWidth = Math.max(width, 0.001f);
    }

    /**
     * 检查是否仍然激活
     */
    public boolean isActive() {
        return active;
    }

    /**
     * 获取总生存时间（秒）
     */
    public double getTotalLifetime() {
        return totalLifetime;
    }

    /**
     * 检查是否启用淡出效果
     */
    public boolean isFadeOutEnabled() {
        return fadeOutEnabled;
    }

    /**
     * 设置是否启用淡出效果
     *
     * @param enabled true=启用淡出
     */
    public void setFadeOutEnabled(boolean enabled) {
        this.fadeOutEnabled = enabled;
    }

    /**
     * 获取淡出开始比例
     */
    public float getFadeOutStartRatio() {
        return fadeOutStartRatio;
    }

    /**
     * 设置淡出开始时间点（占总生命周期的比例）
     *
     * @param ratio 比例值 (0.0-1.0)，例如0.7表示最后30%开始淡出
     */
    public void setFadeOutStartRatio(float ratio) {
        this.fadeOutStartRatio = Math.clamp(ratio, 0.0f, 1.0f);
    }

    /**
     * 检查是否启用颜色渐变
     */
    public boolean isColorGradientEnabled() {
        return colorGradientEnabled;
    }

    /**
     * 设置是否启用颜色渐变
     *
     * @param enabled true=启用渐变
     */
    public void setColorGradientEnabled(boolean enabled) {
        this.colorGradientEnabled = enabled;
    }

    /**
     * 设置终点颜色（用于渐变）
     *
     * @param endColor 终点时的颜色
     */
    public void setEndColor(Vector3f endColor) {
        this.endColor = endColor;
        this.colorGradientEnabled = true;
    }

    /**
     * 获取宽度衰减率
     */
    public float getWidthDecayRate() {
        return widthDecayRate;
    }

    /**
     * 设置宽度衰减率
     *
     * @param decayRate 每秒衰减比例（0.0=不衰减，1.0=快速衰减）
     */
    public void setWidthDecayRate(float decayRate) {
        this.widthDecayRate = Math.max(decayRate, 0.0f);
    }

    // ==================== 工具方法 ====================

    /**
     * 获取第一个（最旧的）点
     *
     * @return 最旧的轨迹点，如果没有则返回null
     */
    public TrailPoint getFirstPoint() {
        return points.peekFirst();
    }

    /**
     * 获取最后一个（最新的）点
     *
     * @return 最新的轨迹点，如果没有则返回null
     */
    public TrailPoint getLastPoint() {
        return points.peekLast();
    }

    /**
     * 获取总轨迹长度（所有线段的总和）
     *
     * @return 轨迹总长度（世界单位）
     */
    public double getTotalLength() {
        double length = 0.0;
        TrailPoint prev = null;

        for (TrailPoint point : points) {
            if (prev != null) {
                length += prev.distanceTo(point);
            }
            prev = point;
        }

        return length;
    }

    /**
     * 转换为字符串表示（用于调试）
     *
     * @return 格式化的字符串
     */
    @Override
    public String toString() {
        return String.format("TrailEffect[id=%s, type=%s, points=%d, lifetime=%.2fs, active=%b]",
                id, type.name(), points.size(), totalLifetime, active);
    }
}
