package com.legendarymage.legendarymagemod.trail;

import com.legendarymage.legendarymagemod.trail.animation.TimelineAnimation;
import com.legendarymage.legendarymagemod.trail.color.HSVColor;
import com.legendarymage.legendarymagemod.trail.math.BezierCurve;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * 贝塞尔曲线拖尾效果 - 全新架构
 * 完全不同于Perception的组件化设计
 *
 * 【核心组件】
 * 1. 点序列管理：基于距离和角度阈值添加点
 * 2. 贝塞尔曲线：Catmull-Rom样条平滑插值
 * 3. HSV颜色：更自然的颜色渐变
 * 4. 时间轴动画：关键帧控制宽度/透明度/颜色变化
 *
 * 【与Perception的区别】
 * - Perception：直接连线，整体淡出，ARGB颜色
 * - 我们：贝塞尔曲线，时间轴动画，HSV颜色，组件化
 *
 * @author Love_U
 * @version 1.0.6
 */
public class BezierTrailEffect {

    // ==================== 核心数据 ====================

    /**
     * 原始控制点（实体实际经过的位置）
     */
    private final List<TrailPoint> controlPoints;

    /**
     * 平滑曲线点（贝塞尔插值后）
     */
    private List<Vec3> smoothCurve;

    /**
     * 拖尾ID
     */
    private final String id;

    /**
     * 基础颜色
     */
    private HSVColor baseColor;

    /**
     * 基础宽度
     */
    private float baseWidth;

    // ==================== 状态控制 ====================

    /**
     * 是否活跃（还在添加新点）
     */
    private boolean active;

    /**
     * 生存时间（秒）
     */
    private double lifetime;

    /**
     * 最大生存时间（秒）
     */
    private final double maxLifetime;

    /**
     * 完成后的生存时间（用于淡出）
     */
    private double fadeOutTime;

    // ==================== 阈值配置 ====================

    /**
     * 最小添加距离（避免点太密集）
     */
    private float minAddDistance = 0.15f;

    /**
     * 最小角度变化（度）
     */
    private float minAngleChange = 5.0f;

    /**
     * 最大点数
     */
    private int maxPoints = 30;

    // ==================== 动画组件 ====================

    /**
     * 渐入动画（拖尾生成时）
     */
    private TimelineAnimation fadeInAnimation;

    /**
     * 渐出动画（拖尾完成后）
     */
    private TimelineAnimation fadeOutAnimation;

    /**
     * 颜色动画（可选）
     */
    private TimelineAnimation colorAnimation;

    // ==================== 平滑配置 ====================

    /**
     * 每段曲线的细分数量
     */
    private int curveSegments = 8;

    /**
     * 是否启用平滑
     */
    private boolean smoothingEnabled = true;

    /**
     * 是否等距重采样
     */
    private boolean equidistantResampling = true;

    /**
     * 重采样间距
     */
    private double resampleDistance = 0.1;

    // ==================== 构造函数 ====================

    /**
     * 构造函数
     *
     * @param id          拖尾ID
     * @param baseColor   基础颜色
     * @param baseWidth   基础宽度
     * @param maxLifetime 最大生存时间（秒）
     */
    public BezierTrailEffect(String id, HSVColor baseColor, float baseWidth, double maxLifetime) {
        this.id = id;
        this.baseColor = baseColor;
        this.baseWidth = baseWidth;
        this.maxLifetime = maxLifetime;
        this.controlPoints = new ArrayList<>();
        this.smoothCurve = new ArrayList<>();
        this.active = true;
        this.lifetime = 0;
        this.fadeOutTime = 0;

        // 默认动画
        this.fadeInAnimation = TimelineAnimation.fadeIn(0.3f);
        this.fadeOutAnimation = TimelineAnimation.fadeOut(1.0f);
    }

    // ==================== 点管理 ====================

    /**
     * 添加新点
     * 基于距离和角度阈值智能添加
     *
     * @param position 新位置
     * @return 是否成功添加
     */
    public boolean addPoint(Vec3 position) {
        if (!active) {
            return false;
        }

        // 第一个点直接添加
        if (controlPoints.isEmpty()) {
            controlPoints.add(new TrailPoint(position, System.currentTimeMillis()));
            updateSmoothCurve();
            return true;
        }

        // 检查距离
        TrailPoint lastPoint = controlPoints.get(controlPoints.size() - 1);
        double distance = position.distanceTo(lastPoint.position);

        if (distance < minAddDistance) {
            return false; // 距离太近，跳过
        }

        // 检查角度变化（如果有至少2个点）
        if (controlPoints.size() >= 2) {
            Vec3 prevDir = lastPoint.position.subtract(controlPoints.get(controlPoints.size() - 2).position).normalize();
            Vec3 newDir = position.subtract(lastPoint.position).normalize();
            double angle = Math.toDegrees(Math.acos(prevDir.dot(newDir)));

            // 如果角度变化小且距离不够大，跳过
            if (angle < minAngleChange && distance < minAddDistance * 2) {
                return false;
            }
        }

        // 添加新点
        controlPoints.add(new TrailPoint(position, System.currentTimeMillis()));

        // 限制最大点数
        if (controlPoints.size() > maxPoints) {
            controlPoints.remove(0);
        }

        updateSmoothCurve();
        return true;
    }

    /**
     * 更新平滑曲线
     */
    private void updateSmoothCurve() {
        if (controlPoints.size() < 2) {
            smoothCurve.clear();
            for (TrailPoint p : controlPoints) {
                smoothCurve.add(p.position);
            }
            return;
        }

        if (!smoothingEnabled) {
            // 不平滑，直接使用控制点
            smoothCurve.clear();
            for (TrailPoint p : controlPoints) {
                smoothCurve.add(p.position);
            }
            return;
        }

        // 使用Catmull-Rom样条生成平滑曲线
        List<Vec3> controlPositions = new ArrayList<>();
        for (TrailPoint p : controlPoints) {
            controlPositions.add(p.position);
        }

        smoothCurve = BezierCurve.generateSmoothCurve(controlPositions, curveSegments);

        // 等距重采样
        if (equidistantResampling) {
            smoothCurve = BezierCurve.resampleEquidistant(smoothCurve, resampleDistance);
        }
    }

    // ==================== 更新 ====================

    /**
     * 更新拖尾状态
     *
     * @param deltaTime 时间增量（秒）
     */
    public void update(double deltaTime) {
        if (active) {
            lifetime += deltaTime;

            // 检查是否超过最大生存时间
            if (lifetime >= maxLifetime) {
                stop();
            }
        } else {
            // 已完成，进入淡出阶段
            fadeOutTime += deltaTime;
        }
    }

    /**
     * 停止拖尾（不再添加新点）
     */
    public void stop() {
        this.active = false;
    }

    // ==================== 渲染查询 ====================

    /**
     * 获取平滑曲线点
     */
    public List<Vec3> getSmoothCurve() {
        return smoothCurve;
    }

    /**
     * 获取当前动画值
     */
    public TimelineAnimation.AnimationValue getCurrentAnimationValue() {
        if (active) {
            // 活跃状态：使用渐入动画
            return fadeInAnimation.sample((float) lifetime);
        } else {
            // 淡出状态：使用渐出动画
            return fadeOutAnimation.sample((float) fadeOutTime);
        }
    }

    /**
     * 获取当前颜色（考虑动画）
     */
    public HSVColor getCurrentColor() {
        TimelineAnimation.AnimationValue anim = getCurrentAnimationValue();
        HSVColor color = anim.color != null ? anim.color : baseColor;

        // 应用色相偏移
        if (anim.hueShift != 0) {
            color = color.shiftHue(anim.hueShift);
        }

        // 应用透明度
        if (anim.alpha < 1.0f) {
            color = new HSVColor(color.getHue(), color.getSaturation(), color.getValue(), anim.alpha);
        }

        return color;
    }

    /**
     * 获取当前宽度（考虑动画）
     */
    public float getCurrentWidth() {
        TimelineAnimation.AnimationValue anim = getCurrentAnimationValue();
        return baseWidth * anim.width;
    }

    // ==================== Getters & Setters ====================

    public String getId() { return id; }
    public boolean isActive() { return active; }
    public int getPointCount() { return controlPoints.size(); }
    public int getSmoothPointCount() { return smoothCurve.size(); }
    public double getLifetime() { return lifetime; }
    public double getMaxLifetime() { return maxLifetime; }
    public double getFadeOutTime() { return fadeOutTime; }

    public void setMinAddDistance(float distance) { this.minAddDistance = distance; }
    public void setMinAngleChange(float angle) { this.minAngleChange = angle; }
    public void setMaxPoints(int max) { this.maxPoints = max; }
    public void setCurveSegments(int segments) { this.curveSegments = segments; }
    public void setSmoothingEnabled(boolean enabled) { this.smoothingEnabled = enabled; }
    public void setEquidistantResampling(boolean enabled) { this.equidistantResampling = enabled; }
    public void setResampleDistance(double distance) { this.resampleDistance = distance; }

    public void setFadeInAnimation(TimelineAnimation anim) { this.fadeInAnimation = anim; }
    public void setFadeOutAnimation(TimelineAnimation anim) { this.fadeOutAnimation = anim; }
    public void setColorAnimation(TimelineAnimation anim) { this.colorAnimation = anim; }

    // ==================== 内部类 ====================

    /**
     * 轨迹点
     */
    public static class TrailPoint {
        public final Vec3 position;
        public final long timestamp;

        public TrailPoint(Vec3 position, long timestamp) {
            this.position = position;
            this.timestamp = timestamp;
        }
    }
}
