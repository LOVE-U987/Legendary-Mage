package com.legendarymage.legendarymagemod.trail.math;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * 贝塞尔曲线工具类
 * 提供平滑的曲线插值，让拖尾更自然流畅
 *
 * 【设计特点】
 * - 支持二次和三次贝塞尔曲线
 * - Catmull-Rom样条：通过所有控制点的平滑曲线
 * - 与Perception的直接连线完全不同
 *
 * 【数学原理】
 * 贝塞尔曲线使用控制点来定义曲线形状，
 * 通过插值公式生成平滑路径
 *
 * @author Love_U
 * @version 1.0.6
 */
public class BezierCurve {

    /**
     * 二次贝塞尔曲线插值
     *
     * @param p0 起点
     * @param p1 控制点
     * @param p2 终点
     * @param t  插值参数 (0-1)
     * @return 曲线上的点
     */
    public static Vec3 quadratic(Vec3 p0, Vec3 p1, Vec3 p2, float t) {
        float u = 1 - t;
        float tt = t * t;
        float uu = u * u;

        double x = uu * p0.x + 2 * u * t * p1.x + tt * p2.x;
        double y = uu * p0.y + 2 * u * t * p1.y + tt * p2.y;
        double z = uu * p0.z + 2 * u * t * p1.z + tt * p2.z;

        return new Vec3(x, y, z);
    }

    /**
     * 三次贝塞尔曲线插值
     *
     * @param p0 起点
     * @param p1 控制点1
     * @param p2 控制点2
     * @param p3 终点
     * @param t  插值参数 (0-1)
     * @return 曲线上的点
     */
    public static Vec3 cubic(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float t) {
        float u = 1 - t;
        float tt = t * t;
        float uu = u * u;
        float uuu = uu * u;
        float ttt = tt * t;

        double x = uuu * p0.x + 3 * uu * t * p1.x + 3 * u * tt * p2.x + ttt * p3.x;
        double y = uuu * p0.y + 3 * uu * t * p1.y + 3 * u * tt * p2.y + ttt * p3.y;
        double z = uuu * p0.z + 3 * uu * t * p1.z + 3 * u * tt * p2.z + ttt * p3.z;

        return new Vec3(x, y, z);
    }

    /**
     * Catmull-Rom样条插值
     * 优点：曲线通过所有控制点，非常平滑
     *
     * @param p0 前一个点
     * @param p1 当前点（曲线通过）
     * @param p2 下一个点（曲线通过）
     * @param p3 后一个点
     * @param t  插值参数 (0-1)
     * @param alpha 张力参数 (0-1, 默认0.5)
     * @return 曲线上的点
     */
    public static Vec3 catmullRom(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float t, float alpha) {
        float t0 = 0;
        float t1 = t0 + (float) Math.pow(p1.distanceTo(p0), alpha);
        float t2 = t1 + (float) Math.pow(p2.distanceTo(p1), alpha);
        float t3 = t2 + (float) Math.pow(p3.distanceTo(p2), alpha);

        // 防止除零
        if (t1 == t0 || t2 == t1 || t3 == t2) {
            return p1.lerp(p2, t);
        }

        float tt = t1 + (t2 - t1) * t;

        Vec3 a1 = p0.scale((t1 - tt) / (t1 - t0)).add(p1.scale((tt - t0) / (t1 - t0)));
        Vec3 a2 = p1.scale((t2 - tt) / (t2 - t1)).add(p2.scale((tt - t1) / (t2 - t1)));
        Vec3 a3 = p2.scale((t3 - tt) / (t3 - t2)).add(p3.scale((tt - t2) / (t3 - t2)));

        Vec3 b1 = a1.scale((t2 - tt) / (t2 - t0)).add(a2.scale((tt - t0) / (t2 - t0)));
        Vec3 b2 = a2.scale((t3 - tt) / (t3 - t1)).add(a3.scale((tt - t1) / (t3 - t1)));

        return b1.scale((t2 - tt) / (t2 - t1)).add(b2.scale((tt - t1) / (t2 - t1)));
    }

    /**
     * 简化版Catmull-Rom（使用默认张力）
     */
    public static Vec3 catmullRom(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float t) {
        return catmullRom(p0, p1, p2, p3, t, 0.5f);
    }

    /**
     * 生成平滑曲线点集
     * 使用Catmull-Rom样条生成一系列插值点
     *
     * @param controlPoints 控制点列表
     * @param segmentsPerCurve 每段曲线的细分数量
     * @return 平滑曲线上的点集
     */
    public static List<Vec3> generateSmoothCurve(List<Vec3> controlPoints, int segmentsPerCurve) {
        List<Vec3> result = new ArrayList<>();

        if (controlPoints.size() < 2) {
            return controlPoints;
        }

        // 对于每对相邻点，使用Catmull-Rom样条
        for (int i = 0; i < controlPoints.size() - 1; i++) {
            Vec3 p0 = i > 0 ? controlPoints.get(i - 1) : controlPoints.get(i);
            Vec3 p1 = controlPoints.get(i);
            Vec3 p2 = controlPoints.get(i + 1);
            Vec3 p3 = i < controlPoints.size() - 2 ? controlPoints.get(i + 2) : p2;

            for (int j = 0; j <= segmentsPerCurve; j++) {
                float t = (float) j / segmentsPerCurve;
                Vec3 point = catmullRom(p0, p1, p2, p3, t);
                result.add(point);
            }
        }

        return result;
    }

    /**
     * 计算曲线长度
     *
     * @param points 曲线上的点
     * @return 近似长度
     */
    public static double calculateLength(List<Vec3> points) {
        double length = 0;
        for (int i = 1; i < points.size(); i++) {
            length += points.get(i).distanceTo(points.get(i - 1));
        }
        return length;
    }

    /**
     * 在曲线上等距采样
     *
     * @param points 原始曲线点
     * @param targetDistance 目标采样间距
     * @return 等距采样后的点
     */
    public static List<Vec3> resampleEquidistant(List<Vec3> points, double targetDistance) {
        if (points.size() < 2) return points;

        List<Vec3> result = new ArrayList<>();
        result.add(points.get(0));

        double accumulatedDistance = 0;
        Vec3 lastPoint = points.get(0);

        for (int i = 1; i < points.size(); i++) {
            Vec3 currentPoint = points.get(i);
            double segmentDistance = currentPoint.distanceTo(lastPoint);
            accumulatedDistance += segmentDistance;

            while (accumulatedDistance >= targetDistance) {
                double ratio = (accumulatedDistance - targetDistance) / segmentDistance;
                Vec3 samplePoint = currentPoint.lerp(lastPoint, ratio);
                result.add(samplePoint);
                accumulatedDistance -= targetDistance;
                lastPoint = samplePoint;
            }

            lastPoint = currentPoint;
        }

        // 确保最后一个点被包含
        if (!result.get(result.size() - 1).equals(points.get(points.size() - 1))) {
            result.add(points.get(points.size() - 1));
        }

        return result;
    }

    /**
     * 计算曲线在某点的切线方向
     *
     * @param prev 前一个点
     * @param next 后一个点
     * @return 归一化的切线向量
     */
    public static Vec3 calculateTangent(Vec3 prev, Vec3 next) {
        return next.subtract(prev).normalize();
    }

    /**
     * 计算曲线在某点的法线方向（垂直于切线）
     *
     * @param tangent 切线
     * @param up      上方向参考
     * @return 归一化的法线向量
     */
    public static Vec3 calculateNormal(Vec3 tangent, Vec3 up) {
        return tangent.cross(up).normalize();
    }
}
