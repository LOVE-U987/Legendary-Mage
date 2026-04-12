package com.legendarymage.legendarymagemod.trail.color;

import org.joml.Vector3f;

/**
 * HSV颜色空间表示
 * 提供比RGB更自然的颜色渐变和彩虹效果
 *
 * 【设计特点】
 * - H: 色相 (0-360度)
 * - S: 饱和度 (0-1)
 * - V: 明度 (0-1)
 * - 支持彩虹循环、平滑渐变
 *
 * 【与Perception的区别】
 * - Perception使用ARGB字符串
 * - 我们使用HSV色彩空间，渐变更自然
 *
 * @author Love_U
 * @version 1.0.6
 */
public class HSVColor {

    /**
     * 色相 (0-360)
     */
    private float hue;

    /**
     * 饱和度 (0-1)
     */
    private float saturation;

    /**
     * 明度 (0-1)
     */
    private float value;

    /**
     * 透明度 (0-1)
     */
    private float alpha;

    /**
     * 构造函数
     *
     * @param hue        色相 (0-360)
     * @param saturation 饱和度 (0-1)
     * @param value      明度 (0-1)
     * @param alpha      透明度 (0-1)
     */
    public HSVColor(float hue, float saturation, float value, float alpha) {
        this.hue = normalizeHue(hue);
        this.saturation = clamp(saturation, 0, 1);
        this.value = clamp(value, 0, 1);
        this.alpha = clamp(alpha, 0, 1);
    }

    /**
     * 从RGB创建HSV颜色
     *
     * @param r 红色 (0-1)
     * @param g 绿色 (0-1)
     * @param b 蓝色 (0-1)
     * @param a 透明度 (0-1)
     * @return HSVColor
     */
    public static HSVColor fromRGB(float r, float g, float b, float a) {
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float hue = 0;
        if (delta != 0) {
            if (max == r) {
                hue = ((g - b) / delta) % 6;
            } else if (max == g) {
                hue = (b - r) / delta + 2;
            } else {
                hue = (r - g) / delta + 4;
            }
            hue *= 60;
            if (hue < 0) hue += 360;
        }

        float saturation = max == 0 ? 0 : delta / max;
        float value = max;

        return new HSVColor(hue, saturation, value, a);
    }

    /**
     * 从RGB整数创建
     *
     * @param rgb RGB整数 (0xRRGGBB)
     * @param alpha 透明度 (0-1)
     * @return HSVColor
     */
    public static HSVColor fromRGB(int rgb, float alpha) {
        float r = ((rgb >> 16) & 0xFF) / 255.0f;
        float g = ((rgb >> 8) & 0xFF) / 255.0f;
        float b = (rgb & 0xFF) / 255.0f;
        return fromRGB(r, g, b, alpha);
    }

    /**
     * 创建彩虹色
     *
     * @param progress 进度 (0-1)
     * @param saturation 饱和度
     * @param value 明度
     * @param alpha 透明度
     * @return 彩虹色
     */
    public static HSVColor rainbow(float progress, float saturation, float value, float alpha) {
        return new HSVColor(progress * 360, saturation, value, alpha);
    }

    /**
     * 转换为RGB
     *
     * @return RGB向量 (r, g, b)
     */
    public Vector3f toRGB() {
        float c = value * saturation;
        float x = c * (1 - Math.abs((hue / 60) % 2 - 1));
        float m = value - c;

        float r, g, b;

        if (hue < 60) {
            r = c; g = x; b = 0;
        } else if (hue < 120) {
            r = x; g = c; b = 0;
        } else if (hue < 180) {
            r = 0; g = c; b = x;
        } else if (hue < 240) {
            r = 0; g = x; b = c;
        } else if (hue < 300) {
            r = x; g = 0; b = c;
        } else {
            r = c; g = 0; b = x;
        }

        return new Vector3f(r + m, g + m, b + m);
    }

    /**
     * 转换为RGB整数
     *
     * @return RGB整数 (0xRRGGBB)
     */
    public int toRGBInt() {
        Vector3f rgb = toRGB();
        int r = (int) (rgb.x * 255);
        int g = (int) (rgb.y * 255);
        int b = (int) (rgb.z * 255);
        return (r << 16) | (g << 8) | b;
    }

    /**
     * 颜色插值（在HSV空间）
     *
     * @param target 目标颜色
     * @param t 插值因子 (0-1)
     * @return 插值后的颜色
     */
    public HSVColor lerp(HSVColor target, float t) {
        // 色相使用最短路径插值
        float hueDiff = target.hue - this.hue;
        if (hueDiff > 180) hueDiff -= 360;
        if (hueDiff < -180) hueDiff += 360;
        float newHue = this.hue + hueDiff * t;

        float newSaturation = this.saturation + (target.saturation - this.saturation) * t;
        float newValue = this.value + (target.value - this.value) * t;
        float newAlpha = this.alpha + (target.alpha - this.alpha) * t;

        return new HSVColor(newHue, newSaturation, newValue, newAlpha);
    }

    /**
     * 调整色相
     *
     * @param delta 变化量
     * @return 新的HSVColor
     */
    public HSVColor shiftHue(float delta) {
        return new HSVColor(hue + delta, saturation, value, alpha);
    }

    /**
     * 调整饱和度
     *
     * @param factor 因子
     * @return 新的HSVColor
     */
    public HSVColor adjustSaturation(float factor) {
        return new HSVColor(hue, saturation * factor, value, alpha);
    }

    /**
     * 调整明度
     *
     * @param factor 因子
     * @return 新的HSVColor
     */
    public HSVColor adjustValue(float factor) {
        return new HSVColor(hue, saturation, value * factor, alpha);
    }

    // ==================== Getters ====================

    public float getHue() { return hue; }
    public float getSaturation() { return saturation; }
    public float getValue() { return value; }
    public float getAlpha() { return alpha; }

    // ==================== 预设颜色 ====================

    public static final HSVColor RED = new HSVColor(0, 1, 1, 1);
    public static final HSVColor YELLOW = new HSVColor(60, 1, 1, 1);
    public static final HSVColor GREEN = new HSVColor(120, 1, 1, 1);
    public static final HSVColor CYAN = new HSVColor(180, 1, 1, 1);
    public static final HSVColor BLUE = new HSVColor(240, 1, 1, 1);
    public static final HSVColor MAGENTA = new HSVColor(300, 1, 1, 1);
    public static final HSVColor WHITE = new HSVColor(0, 0, 1, 1);
    public static final HSVColor BLACK = new HSVColor(0, 0, 0, 1);

    // ==================== 私有方法 ====================

    private float normalizeHue(float hue) {
        while (hue < 0) hue += 360;
        while (hue >= 360) hue -= 360;
        return hue;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public String toString() {
        return String.format("HSV(%.1f, %.2f, %.2f, %.2f)", hue, saturation, value, alpha);
    }
}
