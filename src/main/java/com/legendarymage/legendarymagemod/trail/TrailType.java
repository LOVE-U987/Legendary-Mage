package com.legendarymage.legendarymagemod.trail;

import com.legendarymage.legendarymagemod.LegendaryMage;
import org.joml.Vector3f;

/**
 * 拖尾类型枚举
 * 定义不同的拖尾形状和行为模式
 *
 * 【扩展指南】
 * 要添加新的拖尾类型：
 * 1. 在此枚举中添加新常量
 * 2. 在TrailRenderer中添加对应的渲染逻辑
 * 3. （可选）创建专门的Trail子类处理特殊行为
 *
 * 【性能考虑】
 * 不同类型的计算复杂度不同：
 * - LINEAR: O(n) - 最快
 * - CURVE: O(n) - 中等（需要额外插值）
 * - SPIRAL: O(n*m) - 较慢（每个点需要旋转计算）
 *
 * @author Love_U
 * @version 1.0.6
 */
public enum TrailType {

    /**
     * 直线拖尾
     * 点与点之间直接连接，形成折线/线段
     * 适用场景：剑气、激光束、快速移动的投射物
     */
    LINEAR("linear", "直线拖尾"),

    /**
     * 曲线拖尾（贝塞尔平滑）
     * 使用Catmull-Rom样条或贝塞尔曲线平滑轨迹
     * 适用场景：魔法光束、优雅的法术轨迹、流体效果
     */
    CURVE("curve", "曲线拖尾"),

    /**
     * 螺旋拖尾
     * 轨迹围绕中心轴螺旋扭曲，产生动态视觉效果
     * 适用场景：龙卷风、漩涡法术、能量风暴
     */
    SPIRAL("spiral", "螺旋拖尾"),

    /**
     * 波浪拖尾
     * 轨迹沿正弦波波动，产生飘动效果
     * 适用场景：火焰、烟雾、能量流
     */
    WAVE("wave", "波浪拖尾"),

    /**
     * 粒子化拖尾
     * 模拟粒子效果但使用几何体渲染，性能更优
     * 适用场景：星尘、火花、魔法碎片
     */
    PARTICLE_LIKE("particle_like", "粒子化拖尾");

    /**
     * 类型ID（用于序列化和配置）
     */
    private final String id;

    /**
     * 显示名称（用于调试和UI）
     */
    private final String displayName;

    /**
     * 构造函数
     *
     * @param id 类型唯一标识符
     * @param displayName 人类可读的显示名称
     */
    TrailType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    /**
     * 获取类型ID
     *
     * @return 唯一标识符字符串
     */
    public String getId() {
        return id;
    }

    /**
     * 获取显示名称
     *
     * @return 人类可读的名称
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 根据ID查找类型
     *
     * @param id 类型ID
     * @return 对应的TrailType，如果未找到返回LINEAR作为默认值
     */
    public static TrailType fromId(String id) {
        for (TrailType type : values()) {
            if (type.id.equalsIgnoreCase(id)) {
                return type;
            }
        }
        return LINEAR; // 默认返回直线类型
    }
}
