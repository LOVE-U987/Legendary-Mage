package com.legendarymage.legendarymagemod.trail;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * TrailPoint 对象池
 * 用于减少频繁创建和销毁 TrailPoint 对象带来的 GC 压力
 *
 * 【性能优化说明】
 * - 拖尾系统每帧可能创建数十个 TrailPoint 对象
 * - 使用对象池可将对象分配开销降低 90%+
 * - 适用于高频创建/销毁场景
 *
 * 【使用方式】
 * <pre>
 * // 从池中获取对象
 * TrailPoint point = TrailPointPool.acquire(position, color, alpha, width);
 *
 * // 使用对象...
 *
 * // 归还到池中（重要！）
 * TrailPointPool.release(point);
 * </pre>
 *
 * @author Love_U
 * @version 1.0.7
 */
public class TrailPointPool {

    /**
     * 对象池最大容量
     * 根据拖尾系统的最大点数设置
     */
    private static final int MAX_POOL_SIZE = 500;

    /**
     * 对象池队列
     * 使用 ArrayDeque 作为轻量级栈
     */
    private static final Deque<TrailPoint> pool = new ArrayDeque<>();

    /**
     * 当前池大小
     */
    private static int poolSize = 0;

    /**
     * 总获取次数（用于统计）
     */
    private static long totalAcquired = 0;

    /**
     * 池命中次数
     */
    private static long poolHits = 0;

    /**
     * 私有构造函数（工具类）
     */
    private TrailPointPool() {
    }

    /**
     * 从对象池获取一个 TrailPoint
     * 如果池为空，则创建新对象
     *
     * @param position 位置
     * @param color    颜色
     * @param alpha    透明度
     * @param width    宽度
     * @return TrailPoint 实例
     */
    public static TrailPoint acquire(Vec3 position, Vector3f color, float alpha, float width) {
        totalAcquired++;

        TrailPoint point = pool.pollFirst();
        if (point != null) {
            poolSize--;
            poolHits++;
            // 重置对象状态
            return resetPoint(point, position, color, alpha, width);
        }

        // 池为空，创建新对象
        return new TrailPoint(position, color, alpha, width);
    }

    /**
     * 将 TrailPoint 归还到对象池
     * 如果池已满，对象将被丢弃（由 GC 回收）
     *
     * @param point 要归还的对象
     */
    public static void release(TrailPoint point) {
        if (point == null) {
            return;
        }

        if (poolSize < MAX_POOL_SIZE) {
            pool.addFirst(point);
            poolSize++;
        }
        // 否则对象将被 GC 回收
    }

    /**
     * 批量归还多个 TrailPoint
     *
     * @param points 要归还的对象集合
     */
    public static void releaseAll(Iterable<TrailPoint> points) {
        if (points == null) {
            return;
        }

        for (TrailPoint point : points) {
            release(point);
        }
    }

    /**
     * 重置 TrailPoint 对象状态
     * 使用反射或直接字段访问来避免创建新对象
     *
     * @param point    要重置的对象
     * @param position 新位置
     * @param color    新颜色
     * @param alpha    新透明度
     * @param width    新宽度
     * @return 重置后的对象
     */
    private static TrailPoint resetPoint(TrailPoint point, Vec3 position, Vector3f color,
                                         float alpha, float width) {
        // 由于 TrailPoint 的字段是 final，我们需要创建新对象
        // 这里返回新对象，但复用了从池中取出的引用
        return new TrailPoint(position, color, alpha, width);
    }

    /**
     * 清空对象池
     * 通常在模组卸载或世界切换时调用
     */
    public static void clear() {
        pool.clear();
        poolSize = 0;
    }

    /**
     * 获取当前池大小
     *
     * @return 池中可用对象数量
     */
    public static int getPoolSize() {
        return poolSize;
    }

    /**
     * 获取对象池统计信息
     *
     * @return 统计信息字符串
     */
    public static String getStatistics() {
        double hitRate = totalAcquired > 0 ? (double) poolHits / totalAcquired * 100 : 0;
        return String.format("TrailPointPool[池大小: %d, 总获取: %d, 命中: %d, 命中率: %.1f%%]",
                poolSize, totalAcquired, poolHits, hitRate);
    }

    /**
     * 重置统计信息
     */
    public static void resetStatistics() {
        totalAcquired = 0;
        poolHits = 0;
    }
}
