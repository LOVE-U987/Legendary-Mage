package com.legendarymage.legendarymagemod.trail;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * 拖尾特效使用示例和工具类
 * 展示如何在实际场景中使用TrailEffect API
 *
 * 【包含内容】
 * 1. 预设的常用拖尾配置（火焰、冰霜、闪电等）
 * 2. 法术集成的示例方法
 * 3. 实体移动轨迹追踪
 *
 * 【快速开始】
 * <pre>
 * // 创建一个火焰剑气拖尾
 * TrailEffect trail = TrailPresets.createFireSwordTrail("my_fire_sword");
 *
 * // 在施法时添加点
 * Vec3 currentPos = entity.position().add(0, 1.5, 0);
 * trail.addPoint(currentPos);
 *
 * // 在渲染事件中
 * TrailManager.getInstance().renderAll(poseStack, bufferSource, partialTick);
 * </pre>
 *
 * @author Love_U
 * @version 1.0.6
 */
@net.neoforged.api.distmarker.OnlyIn(net.neoforged.api.distmarker.Dist.CLIENT)
public class TrailPresets {

    /**
     * ==================== 预设颜色常量 ====================
     */

    /**
     * 火焰红橙色
     */
    public static final Vector3f COLOR_FIRE = new Vector3f(1.0f, 0.4f, 0.0f);

    /**
     * 冰霜蓝白色
     */
    public static final Vector3f COLOR_ICE = new Vector3f(0.6f, 0.8f, 1.0f);

    /**
     * 亮蓝色
     */
    public static final Vector3f COLOR_LIGHTNING = new Vector3f(0.7f, 0.8f, 1.0f);

    /**
     * 神圣金白色
     */
    public static final Vector3f COLOR_HOLY = new Vector3f(1.0f, 0.95f, 0.6f);

    /**
     * 暗紫色（末影/诡秘）
     */
    public static final Vector3f COLOR_ENDER = new Vector3f(0.5f, 0.0f, 0.7f);

    /**
     * 血红色
     */
    public static final Vector3f COLOR_BLOOD = new Vector3f(0.6f, 0.0f, 0.0f);

    /**
     * 自然绿色
     */
    public static final Vector3f COLOR_NATURE = new Vector3f(0.3f, 0.8f, 0.2f);

    /**
     * 纯白色
     */
    public static final Vector3f COLOR_WHITE = new Vector3f(1.0f, 1.0f, 1.0f);

    // ==================== 预设工厂方法 ====================

    /**
     * 创建火焰拖尾（适用于火系法术）
     * 特性：橙红色渐变到黄色，中等宽度，快速衰减
     *
     * @param id 唯一标识符
     * @return 配置好的TrailEffect实例
     */
    public static TrailEffect createFireTrail(String id) {
        TrailManager manager = TrailManager.getInstance();

        return manager.createGradientTrail(
                id,
                TrailType.LINEAR,
                COLOR_FIRE,                          // 起点：橙红色
                new Vector3f(1.0f, 1.0f, 0.3f),      // 终点：金黄色
                0.2f,                                // 宽度
                1.5                                  // 生命周期（秒）
        );
    }

    /**
     * 创建冰霜拖尾（适用于冰系法术）
     * 特性：蓝白色，细长，带透明度渐变
     *
     * @param id 唯一标识符
     * @return 配置好的TrailEffect实例
     */
    public static TrailEffect createIceTrail(String id) {
        TrailManager manager = TrailManager.getInstance();

        TrailEffect trail = manager.createTrail(
                id,
                TrailType.CURVE,
                COLOR_ICE,
                0.12f,                               // 较细的宽度
                2.0                                  // 较长的生命周期
        );

        // 启用淡出效果（最后40%开始淡出）
        trail.setFadeOutEnabled(true);
        trail.setFadeOutStartRatio(0.6f);

        return trail;
    }

    /**
     * 创建闪电拖尾（适用于雷系法术）
     * 特性：亮蓝色，发光效果，锯齿状
     *
     * @param id 唯一标识符
     * @return 配置好的TrailEffect实例
     */
    public static TrailEffect createLightningTrail(String id) {
        TrailManager manager = TrailManager.getInstance();

        TrailEffect trail = manager.createGlowingTrail(
                id,
                TrailType.WAVE,
                COLOR_LIGHTNING,
                0.08f,                               // 很细的宽度
                0.8                                  // 短暂的生命周期（闪烁效果）
        );

        return trail;
    }

    /**
     * 创建神圣光束拖尾（适用于神圣系法术）
     * 特性：金白色，直线，发光，不衰减
     *
     * @param id 唯一标识符
     * @return 配置好的TrailEffect实例
     */
    public static TrailEffect createHolyBeamTrail(String id) {
        TrailManager manager = TrailManager.getInstance();

        TrailEffect trail = manager.createGlowingTrail(
                id,
                TrailType.LINEAR,
                COLOR_HOLY,
                0.25f,                               // 宽度适中
                3.0                                  // 长生命周期
        );

        // 不启用淡出（神圣光束应该保持稳定）
        trail.setFadeOutEnabled(false);

        return trail;
    }

    /**
     * 创建末影漩涡拖尾（适用于末影系法术）
     * 特性：暗紫色，螺旋形状，神秘感
     *
     * @param id 唯一标识符
     * @return 配置好的TrailEffect实例
     */
    public static TrailEffect createEnderVortexTrail(String id) {
        TrailManager manager = TrailManager.getInstance();

        TrailEffect trail = manager.createGradientTrail(
                id,
                TrailType.SPIRAL,
                COLOR_ENDER,
                new Vector3f(0.2f, 0.0f, 0.9f),       // 终点：深紫色
                0.18f,
                2.5
        );

        return trail;
    }

    /**
     * 创建血刃拖尾（适用于血系法术）
     * 特性：深红色，粒子化效果，滴落感
     *
     * @param id 唯一标识符
     * @return 配置好的TrailEffect实例
     */
    public static TrailEffect createBloodBladeTrail(String id) {
        TrailManager manager = TrailManager.getInstance();

        TrailEffect trail = manager.createTrail(
                id,
                TrailType.PARTICLE_LIKE,
                COLOR_BLOOD,
                0.15f,
                1.2
        );

        // 宽度快速衰减模拟滴落效果
        trail.setWidthDecayRate(1.5f);

        return trail;
    }

    /**
     * 创建通用剑气拖尾（适用于近战法术）
     * 特性：可自定义颜色，中等参数
     *
     * @param id 唯一标识符
     * @param color 剑气颜色
     * @return 配置好的TrailEffect实例
     */
    public static TrailEffect createSwordQiTrail(String id, Vector3f color) {
        TrailManager manager = TrailManager.getInstance();

        return manager.createTrail(
                id,
                TrailType.LINEAR,
                color,
                0.22f,                              // 中等宽度
                0.6                                 // 短生命周期（快速挥动）
        );
    }

    // ==================== 实用工具方法 ====================

    /**
     * 追踪实体的移动轨迹
     * 自动每帧记录实体位置，形成跟随拖尾
     *
     * 【使用方式】
     * 在客户端tick事件中调用：
     * <pre>
     * TrailPresets.trackEntityMovement(player, "player_trail");
     * </pre>
     *
     * @param entity 要追踪的实体
     * @param trailId 拖尾ID（建议使用entity.getStringUUID()保证唯一）
     * @return 对应的TrailEffect（如果已存在则返回已有的）
     */
    public static TrailEffect trackEntityMovement(LivingEntity entity, String trailId) {
        if (entity == null || !entity.isAlive()) return null;

        TrailManager manager = TrailManager.getInstance();
        TrailEffect trail = manager.getTrail(trailId);

        // 如果不存在，创建一个新的
        if (trail == null || !trail.isActive()) {
            trail = manager.createSpellTrail(trailId, TrailType.CURVE, COLOR_WHITE);
            trail.setMaxPoints(20);              // 较少的点数以节省性能
            trail.setWidth(0.05f);               // 细线条
            trail.setFadeOutEnabled(true);
            trail.setMaxLifetime(2.0);           // 2秒历史记录
        }

        // 记录当前位置（在脚部位置稍微抬高一点）
        Vec3 position = entity.position().add(0, entity.getBbHeight() * 0.5, 0);
        trail.addPoint(position);

        return trail;
    }

    /**
     * 为投射物创建拖尾效果
     * 适用于箭矢、法球、能量弹等
     *
     * @param projectileId 投射物唯一ID
     * @param position 当前位置
     * @param color 拖尾颜色
     */
    public static void addProjectileTrailPoint(String projectileId, Vec3 position, Vector3f color) {
        TrailManager manager = TrailManager.getInstance();
        TrailEffect trail = manager.getTrail(projectileId);

        if (trail == null || !trail.isActive()) {
            // 首次调用时自动创建
            trail = manager.createTrail(projectileId, TrailType.LINEAR, color, 0.1f, 2.0);
            trail.setMaxPoints(30);
        }

        trail.addPoint(position);
    }

    /**
     * 创建一个简单的"测试"拖尾用于调试
     * 白色直线，长生命周期，方便观察效果
     *
     * @return 测试用的TrailEffect
     */
    public static TrailEffect createDebugTrail() {
        String debugId = "debug_trail_" + System.currentTimeMillis();
        TrailManager manager = TrailManager.getInstance();

        TrailEffect trail = manager.createTrail(
                debugId,
                TrailType.LINEAR,
                COLOR_WHITE,
                0.3f,                               // 明显的宽度
                10.0                                // 超长生命周期便于观察
        );

        trail.setFadeOutEnabled(false);             // 不淡出
        trail.setMaxPoints(100);                     // 允许很多点

        com.legendarymage.legendarymagemod.ModLogger.spell("创建调试拖尾: {}", debugId);

        return trail;
    }
}
