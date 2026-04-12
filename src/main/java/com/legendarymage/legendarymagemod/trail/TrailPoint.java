package com.legendarymage.legendarymagemod.trail;

import com.legendarymage.legendarymagemod.LegendaryMage;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * 轨迹点数据类
 * 存储拖尾特效中单个点的完整信息
 *
 * 【使用场景】
 * 用于构建TrailEffect的轨迹历史记录。
 * 每个点包含位置、外观属性和时间戳，
 * 支持插值计算以实现平滑的拖尾效果。
 *
 * 【性能优化】
 * 此对象会被频繁创建和销毁，因此：
 * - 使用基本数据类型而非包装类
 * - 避免复杂的计算逻辑
 * - 保持字段为public final以便直接访问
 *
 * @author Love_U
 * @version 1.0.6
 */
public class TrailPoint {

    /**
     * 世界坐标位置
     */
    public final Vec3 position;

    /**
     * 颜色 (RGB, 0.0-1.0)
     */
    public final Vector3f color;

    /**
     * 透明度 (0.0-1.0, 1.0=完全不透明)
     */
    public final float alpha;

    /**
     * 该点的宽度（世界单位）
     */
    public final float width;

    /**
     * 创建时间戳（System.nanoTime()）
     * 用于计算年龄和淡出
     */
    public final long timestamp;

    /**
     * 构造函数 - 完整参数版本
     *
     * @param position 世界坐标位置
     * @param color RGB颜色向量 (0.0-1.0)
     * @param alpha 透明度 (0.0-1.0)
     * @param width 点的宽度（世界单位）
     */
    public TrailPoint(Vec3 position, Vector3f color, float alpha, float width) {
        this.position = position;
        this.color = color;
        this.alpha = Math.clamp(alpha, 0.0f, 1.0f);
        this.width = Math.max(width, 0.001f); // 最小宽度防止除零
        this.timestamp = System.nanoTime();
    }

    /**
     * 构造函数 - 简化版本（使用默认透明度和宽度）
     *
     * @param position 世界坐标位置
     * @param color RGB颜色向量 (0.0-1.0)
     */
    public TrailPoint(Vec3 position, Vector3f color) {
        this(position, color, 1.0f, 0.1f);
    }

    /**
     * 构造函数 - 最简版本（使用白色）
     *
     * @param position 世界坐标位置
     */
    public TrailPoint(Vec3 position) {
        this(position, new Vector3f(1.0f, 1.0f, 1.0f), 1.0f, 0.1f);
    }

    /**
     * 计算此点的年龄（纳秒）
     *
     * @return 自创建以来经过的时间（纳秒）
     */
    public long getAgeNanos() {
        return System.nanoTime() - this.timestamp;
    }

    /**
     * 计算此点的年龄（秒）
     *
     * @return 自创建以来经过的时间（秒）
     */
    public double getAgeSeconds() {
        return getAgeNanos() / 1_000_000_000.0;
    }

    /**
     * 计算此点到另一点的距离
     *
     * @param other 另一个轨迹点
     * @return 两点之间的欧几里得距离
     */
    public double distanceTo(TrailPoint other) {
        return this.position.distanceTo(other.position);
    }

    /**
     * 在两点之间进行线性插值
     *
     * @param other 终点
     * @param t 插值因子 (0.0=起点, 1.0=终点)
     * @return 插值后的新轨迹点
     */
    public TrailPoint lerp(TrailPoint other, float t) {
        Vec3 interpolatedPos = this.position.lerp(other.position, t);
        Vector3f interpolatedColor = new Vector3f(
                this.color.x + (other.color.x - this.color.x) * t,
                this.color.y + (other.color.y - this.color.y) * t,
                this.color.z + (other.color.z - this.color.z) * t
        );
        float interpolatedAlpha = this.alpha + (other.alpha - this.alpha) * t;
        float interpolatedWidth = this.width + (other.width - this.width) * t;

        return new TrailPoint(interpolatedPos, interpolatedColor, interpolatedAlpha, interpolatedWidth);
    }

    /**
     * 创建此点的副本（带新的时间戳）
     * 用于重用点数据但更新时间戳的场景
     *
     * @return 新的轨迹点副本
     */
    public TrailPoint copyWithNewTimestamp() {
        return new TrailPoint(this.position, this.color, this.alpha, this.width);
    }
}
