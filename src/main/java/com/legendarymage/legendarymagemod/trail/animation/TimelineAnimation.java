package com.legendarymage.legendarymagemod.trail.animation;

import com.legendarymage.legendarymagemod.trail.color.HSVColor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 时间轴动画系统
 * 提供关键帧动画支持，用于控制拖尾的宽度、透明度、颜色随时间变化
 *
 * 【设计特点】
 * - 关键帧系统：在特定时间点定义属性值
 * - 自动插值：在关键帧之间平滑过渡
 * - 多属性支持：宽度、透明度、颜色、色相偏移
 * - 与Perception的整体淡出完全不同
 *
 * 【使用场景】
 * - 拖尾生成时的渐入效果
 * - 拖尾完成后的渐出效果
 * - 彩虹色相循环
 * - 脉冲宽度变化
 *
 * @author Love_U
 * @version 1.0.6
 */
public class TimelineAnimation {

    /**
     * 关键帧列表
     */
    private final List<KeyFrame> keyFrames;

    /**
     * 动画总时长（秒）
     */
    private float duration;

    /**
     * 是否循环播放
     */
    private boolean looping;

    /**
     * 缓动函数
     */
    private Function<Float, Float> easingFunction;

    /**
     * 构造函数
     *
     * @param duration 动画时长（秒）
     * @param looping  是否循环
     */
    public TimelineAnimation(float duration, boolean looping) {
        this.keyFrames = new ArrayList<>();
        this.duration = duration;
        this.looping = looping;
        this.easingFunction = EasingFunctions.LINEAR;
    }

    /**
     * 添加关键帧
     *
     * @param time 时间点 (0-1，相对于duration)
     * @param frame 关键帧数据
     * @return this，支持链式调用
     */
    public TimelineAnimation addKeyFrame(float time, KeyFrame frame) {
        frame.time = clamp(time, 0, 1);
        keyFrames.add(frame);
        // 按时间排序
        keyFrames.sort((a, b) -> Float.compare(a.time, b.time));
        return this;
    }

    /**
     * 在指定时间点采样动画值
     *
     * @param currentTime 当前时间（秒）
     * @return 采样结果
     */
    public AnimationValue sample(float currentTime) {
        if (keyFrames.isEmpty()) {
            return AnimationValue.DEFAULT;
        }

        // 处理循环
        float normalizedTime = currentTime / duration;
        if (looping) {
            normalizedTime = normalizedTime % 1.0f;
        } else {
            normalizedTime = clamp(normalizedTime, 0, 1);
        }

        // 找到当前时间所在的关键帧区间
        KeyFrame prevFrame = null;
        KeyFrame nextFrame = null;

        for (int i = 0; i < keyFrames.size(); i++) {
            if (keyFrames.get(i).time >= normalizedTime) {
                nextFrame = keyFrames.get(i);
                prevFrame = i > 0 ? keyFrames.get(i - 1) : keyFrames.get(0);
                break;
            }
        }

        if (nextFrame == null) {
            // 超过最后一个关键帧
            return keyFrames.get(keyFrames.size() - 1).toValue();
        }

        if (prevFrame == nextFrame || prevFrame.time == nextFrame.time) {
            return prevFrame.toValue();
        }

        // 计算插值因子
        float t = (normalizedTime - prevFrame.time) / (nextFrame.time - prevFrame.time);
        t = easingFunction.apply(t);

        // 插值
        return interpolate(prevFrame, nextFrame, t);
    }

    /**
     * 在两个关键帧之间插值
     */
    private AnimationValue interpolate(KeyFrame prev, KeyFrame next, float t) {
        AnimationValue result = new AnimationValue();

        // 宽度插值
        result.width = lerp(prev.width, next.width, t);

        // 透明度插值
        result.alpha = lerp(prev.alpha, next.alpha, t);

        // 颜色插值（在HSV空间）
        if (prev.color != null && next.color != null) {
            result.color = prev.color.lerp(next.color, t);
        } else if (next.color != null) {
            result.color = next.color;
        } else if (prev.color != null) {
            result.color = prev.color;
        }

        // 色相偏移插值
        result.hueShift = lerp(prev.hueShift, next.hueShift, t);

        return result;
    }

    /**
     * 线性插值
     */
    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    /**
     * 设置缓动函数
     */
    public TimelineAnimation setEasing(Function<Float, Float> easing) {
        this.easingFunction = easing;
        return this;
    }

    /**
     * 获取动画时长
     */
    public float getDuration() {
        return duration;
    }

    /**
     * 设置动画时长
     */
    public void setDuration(float duration) {
        this.duration = duration;
    }

    /**
     * 是否已完成（非循环模式下）
     */
    public boolean isFinished(float currentTime) {
        if (looping) return false;
        return currentTime >= duration;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    // ==================== 预设动画 ====================

    /**
     * 创建渐入动画
     * 从透明到不透明
     */
    public static TimelineAnimation fadeIn(float duration) {
        return new TimelineAnimation(duration, false)
                .addKeyFrame(0.0f, new KeyFrame().alpha(0.0f).width(0.0f))
                .addKeyFrame(1.0f, new KeyFrame().alpha(1.0f).width(1.0f))
                .setEasing(EasingFunctions.EASE_OUT_QUAD);
    }

    /**
     * 创建渐出动画
     * 从不透明到透明
     */
    public static TimelineAnimation fadeOut(float duration) {
        return new TimelineAnimation(duration, false)
                .addKeyFrame(0.0f, new KeyFrame().alpha(1.0f).width(1.0f))
                .addKeyFrame(1.0f, new KeyFrame().alpha(0.0f).width(0.0f))
                .setEasing(EasingFunctions.EASE_IN_QUAD);
    }

    /**
     * 创建彩虹循环动画
     */
    public static TimelineAnimation rainbowCycle(float duration, float saturation, float value) {
        TimelineAnimation anim = new TimelineAnimation(duration, true);

        for (int i = 0; i <= 6; i++) {
            float hue = i * 60; // 红、黄、绿、青、蓝、紫、红
            float time = i / 6.0f;
            anim.addKeyFrame(time, new KeyFrame()
                    .color(HSVColor.rainbow(time, saturation, value, 1.0f)));
        }

        return anim;
    }

    /**
     * 创建脉冲动画
     */
    public static TimelineAnimation pulse(float duration, float minWidth, float maxWidth) {
        TimelineAnimation anim = new TimelineAnimation(duration, true);
        anim.addKeyFrame(0.0f, new KeyFrame().width(minWidth))
                .addKeyFrame(0.5f, new KeyFrame().width(maxWidth))
                .addKeyFrame(1.0f, new KeyFrame().width(minWidth))
                .setEasing(EasingFunctions.EASE_IN_OUT_SINE);
        return anim;
    }

    // ==================== 内部类 ====================

    /**
     * 关键帧
     */
    public static class KeyFrame {
        float time;           // 时间点 (0-1)
        float width = 1.0f;   // 宽度倍数
        float alpha = 1.0f;   // 透明度
        HSVColor color;       // 颜色
        float hueShift = 0;   // 色相偏移

        public KeyFrame width(float width) {
            this.width = width;
            return this;
        }

        public KeyFrame alpha(float alpha) {
            this.alpha = alpha;
            return this;
        }

        public KeyFrame color(HSVColor color) {
            this.color = color;
            return this;
        }

        public KeyFrame hueShift(float shift) {
            this.hueShift = shift;
            return this;
        }

        AnimationValue toValue() {
            AnimationValue v = new AnimationValue();
            v.width = width;
            v.alpha = alpha;
            v.color = color;
            v.hueShift = hueShift;
            return v;
        }
    }

    /**
     * 动画值
     */
    public static class AnimationValue {
        public static final AnimationValue DEFAULT = new AnimationValue();

        public float width = 1.0f;
        public float alpha = 1.0f;
        public HSVColor color;
        public float hueShift = 0;
    }

    /**
     * 缓动函数
     */
    public static class EasingFunctions {
        public static final Function<Float, Float> LINEAR = t -> t;

        public static final Function<Float, Float> EASE_IN_QUAD = t -> t * t;

        public static final Function<Float, Float> EASE_OUT_QUAD = t -> 1 - (1 - t) * (1 - t);

        public static final Function<Float, Float> EASE_IN_OUT_QUAD = t ->
                t < 0.5f ? 2 * t * t : 1 - (float) Math.pow(-2 * t + 2, 2) / 2;

        public static final Function<Float, Float> EASE_IN_SINE = t ->
                1 - (float) Math.cos(t * Math.PI / 2);

        public static final Function<Float, Float> EASE_OUT_SINE = t ->
                (float) Math.sin(t * Math.PI / 2);

        public static final Function<Float, Float> EASE_IN_OUT_SINE = t ->
                -((float) Math.cos(Math.PI * t) - 1) / 2;

        public static final Function<Float, Float> EASE_OUT_BOUNCE = t -> {
            if (t < 1 / 2.75f) {
                return 7.5625f * t * t;
            } else if (t < 2 / 2.75f) {
                return 7.5625f * (t -= 1.5f / 2.75f) * t + 0.75f;
            } else if (t < 2.5 / 2.75f) {
                return 7.5625f * (t -= 2.25f / 2.75f) * t + 0.9375f;
            } else {
                return 7.5625f * (t -= 2.625f / 2.75f) * t + 0.984375f;
            }
        };
    }
}
