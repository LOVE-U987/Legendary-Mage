package com.legendarymage.legendarymagemod.spell;

import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.particle.ShockwaveParticleOptions;
import io.redspace.ironsspellbooks.particle.SparkParticleOptions;
import io.redspace.ironsspellbooks.particle.FogParticleOptions;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;

/**
 * 复苏符文粒子效果管理器
 * 使用铁魔法的真实粒子API创建血系法术风格效果
 * 
 * @author Love_U
 * @version 0.0.1
 */
public class ResurrectionRuneParticles {

    /**
     * 血系魔法颜色 - 深红色
     */
    private static final Vector3f BLOOD_COLOR = new Vector3f(0.6f, 0.0f, 0.0f);
    private static final Vector3f DARK_BLOOD_COLOR = new Vector3f(0.3f, 0.0f, 0.0f);
    private static final Vector3f BRIGHT_BLOOD_COLOR = new Vector3f(0.8f, 0.1f, 0.1f);
    
    /**
     * 灵魂颜色 - 蓝白色
     */
    private static final Vector3f SOUL_COLOR = new Vector3f(0.5f, 0.8f, 1.0f);

    /**
     * 播放施法时的爆发粒子效果
     * 类似铁魔法的法术施放效果
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    public static void playCastBurst(ServerLevel level, Vec3 pos, int range) {
        // 1. 血系冲击波 - 使用铁魔法的BlastwaveParticle
        playBloodBlastwave(level, pos, range);
        
        // 2. 冲击波环 - 使用ShockwaveParticle
        playShockwaveRing(level, pos, range);
        
        // 3. 血雾扩散 - 使用FogParticle
        playBloodFog(level, pos, range);
        
        // 4. 火花飞溅 - 使用SparkParticle
        playBloodSparks(level, pos, range);
        
        // 5. 血滴粒子
        playBloodDrops(level, pos, range);
        
        // 6. 灵魂粒子
        playSoulParticles(level, pos, range);
    }

    /**
     * 播放血系冲击波效果 - 使用BlastwaveParticle
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    private static void playBloodBlastwave(ServerLevel level, Vec3 pos, int range) {
        // 创建多层冲击波
        for (int ring = 0; ring < 3; ring++) {
            double radius = range * (0.3 + ring * 0.35);
            
            // 使用铁魔法的BlastwaveParticle
            level.sendParticles(
                    new BlastwaveParticleOptions(BLOOD_COLOR, (float) radius * 0.5f),
                    pos.x,
                    pos.y + 0.1,
                    pos.z,
                    1,
                    0,
                    0,
                    0,
                    0
            );
        }
    }

    /**
     * 播放冲击波环效果 - 使用ShockwaveParticle
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    private static void playShockwaveRing(ServerLevel level, Vec3 pos, int range) {
        // 使用ShockwaveParticle创建环形冲击波
        level.sendParticles(
                new ShockwaveParticleOptions(BLOOD_COLOR, range * 0.8f, true),
                pos.x,
                pos.y + 0.1,
                pos.z,
                3,
                0.5,
                0,
                0.5,
                0.1
        );
    }

    /**
     * 播放血雾效果 - 使用FogParticle，创建铺盖地面的半透明血雾
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    private static void playBloodFog(ServerLevel level, Vec3 pos, int range) {
        // 增加密度，创建铺盖地面的效果
        int fogCount = range * 3; // 增加数量
        
        for (int i = 0; i < fogCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            // 更均匀分布
            double distance = level.random.nextDouble() * range * 0.9;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            // 使用更小的scale创建更透明的效果
            // 降低高度到贴近地面
            float scale = 0.4f + level.random.nextFloat() * 0.3f; // 更小的尺寸 = 更透明
            
            level.sendParticles(
                    new FogParticleOptions(BLOOD_COLOR, scale),
                    x,
                    pos.y + 0.05 + level.random.nextDouble() * 0.3, // 贴近地面
                    z,
                    1,
                    0.15,
                    0.02,
                    0.15,
                    0.005
            );
        }
        
        // 添加第二层更淡的血雾，增加层次感
        int secondLayerCount = range * 2;
        for (int i = 0; i < secondLayerCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range * 0.95;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            // 更小的粒子，更透明
            level.sendParticles(
                    new FogParticleOptions(DARK_BLOOD_COLOR, 0.3f + level.random.nextFloat() * 0.2f),
                    x,
                    pos.y + 0.02 + level.random.nextDouble() * 0.2,
                    z,
                    1,
                    0.2,
                    0.01,
                    0.2,
                    0.003
            );
        }
    }

    /**
     * 播放血火花效果 - 使用SparkParticle
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    private static void playBloodSparks(ServerLevel level, Vec3 pos, int range) {
        int sparkCount = range * 4;
        
        for (int i = 0; i < sparkCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            // 使用SparkParticle
            level.sendParticles(
                    new SparkParticleOptions(BRIGHT_BLOOD_COLOR),
                    x,
                    pos.y + 0.5 + level.random.nextDouble() * 2,
                    z,
                    1,
                    (level.random.nextDouble() - 0.5) * 0.3,
                    level.random.nextDouble() * 0.3,
                    (level.random.nextDouble() - 0.5) * 0.3,
                    0.1
            );
        }
    }

    /**
     * 播放血滴粒子 - 使用铁魔法的BLOOD_PARTICLE
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    private static void playBloodDrops(ServerLevel level, Vec3 pos, int range) {
        int bloodCount = range * 6;
        
        for (int i = 0; i < bloodCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            // 使用铁魔法的血粒子
            level.sendParticles(
                    ParticleRegistry.BLOOD_PARTICLE.get(),
                    x,
                    pos.y + 0.5 + level.random.nextDouble() * 3,
                    z,
                    1,
                    0.1,
                    0.2,
                    0.1,
                    0.05
            );
        }
    }

    /**
     * 播放灵魂粒子 - 使用WISP_PARTICLE
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    private static void playSoulParticles(ServerLevel level, Vec3 pos, int range) {
        int soulCount = range * 3;
        
        for (int i = 0; i < soulCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range * 0.8;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            // 使用铁魔法的幽灵粒子
            level.sendParticles(
                    ParticleRegistry.WISP_PARTICLE.get(),
                    x,
                    pos.y + 0.1,
                    z,
                    1,
                    0,
                    0.3 + level.random.nextDouble() * 0.2,
                    0,
                    0.02
            );
        }
    }

    /**
     * 播放持续符文边界效果
     * 
     * @param level      服务器世界
     * @param rune       符文区域
     * @param tickCount  当前tick数
     */
    public static void playRuneBoundary(ServerLevel level, ResurrectionRuneManager.ResurrectionRune rune, int tickCount) {
        double radius = rune.range;
        
        // 每10tick更新一次边界
        if (tickCount % 10 != 0) return;
        
        // 1. 血系符文环 - 旋转效果
        playRotatingBloodRing(level, rune, tickCount);
        
        // 2. 随机血雾
        if (tickCount % 20 == 0) {
            playRandomBloodFog(level, rune);
        }
        
        // 3. 地面血迹（低频）
        if (tickCount % 30 == 0) {
            playGroundBlood(level, rune);
        }
        
        // 4. 中心光柱（低频）
        if (tickCount % 40 == 0) {
            playCenterPillar(level, rune);
        }
    }

    /**
     * 播放旋转的血系符文环 - 增强版，使用血粒子和火花
     * 
     * @param level     服务器世界
     * @param rune      符文区域
     * @param tickCount 当前tick数
     */
    private static void playRotatingBloodRing(ServerLevel level, ResurrectionRuneManager.ResurrectionRune rune, int tickCount) {
        double radius = rune.range;
        int particles = (int) (radius * 6); // 增加粒子密度
        
        // 旋转偏移 - 加快旋转速度
        double rotation = (tickCount * 0.2) % (2 * Math.PI);
        
        for (int i = 0; i < particles; i++) {
            double angle = (2 * Math.PI * i) / particles + rotation;
            double x = rune.x + Math.cos(angle) * radius;
            double z = rune.z + Math.sin(angle) * radius;
            
            // 1. 主血粒子 - 更明显的位置
            level.sendParticles(
                    ParticleRegistry.BLOOD_PARTICLE.get(),
                    x,
                    rune.y + 0.15, // 稍微抬高
                    z,
                    2, // 增加数量
                    0.05,
                    0.1,
                    0.05,
                    0.02
            );
            
            // 2. 每隔几个粒子添加火花，增强可见性
            if (i % 3 == 0) {
                level.sendParticles(
                        new SparkParticleOptions(BRIGHT_BLOOD_COLOR),
                        x,
                        rune.y + 0.2,
                        z,
                        1,
                        0.1,
                        0.15,
                        0.1,
                        0.05
                );
            }
            
            // 3. 添加地面血迹标记
            if (i % 5 == 0 && tickCount % 20 == 0) {
                level.sendParticles(
                        ParticleRegistry.BLOOD_GROUND_PARTICLE.get(),
                        x,
                        rune.y + 0.05,
                        z,
                        1,
                        0,
                        0,
                        0,
                        0
                );
            }
        }
        
        // 4. 添加内圈光环（更明显的双层环）
        double innerRadius = radius * 0.7;
        int innerParticles = (int) (innerRadius * 4);
        double innerRotation = -rotation * 0.5; // 反向旋转
        
        for (int i = 0; i < innerParticles; i++) {
            double angle = (2 * Math.PI * i) / innerParticles + innerRotation;
            double x = rune.x + Math.cos(angle) * innerRadius;
            double z = rune.z + Math.sin(angle) * innerRadius;
            
            level.sendParticles(
                    ParticleRegistry.WISP_PARTICLE.get(),
                    x,
                    rune.y + 0.1,
                    z,
                    1,
                    0,
                    0.08,
                    0,
                    0.01
            );
        }
    }

    /**
     * 播放随机血雾 - 铺盖地面的半透明效果
     * 使用与转化血雾相同的大小(1.5f)，但更淡的颜色
     *
     * @param level 服务器世界
     * @param rune  符文区域
     */
    private static void playRandomBloodFog(ServerLevel level, ResurrectionRuneManager.ResurrectionRune rune) {
        // 增加密度
        int count = (int) (rune.range * 1.5);

        // 淡血色 - 比BLOOD_COLOR更淡
        Vector3f lightBloodColor = new Vector3f(0.9f, 0.3f, 0.3f);

        for (int i = 0; i < count; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * rune.range * 0.9;
            double x = rune.x + Math.cos(angle) * distance;
            double z = rune.z + Math.sin(angle) * distance;

            // 使用与转化血雾相同的大小(1.5f)，但颜色更淡
            float scale = 1.2f + level.random.nextFloat() * 0.6f;

            level.sendParticles(
                    new FogParticleOptions(lightBloodColor, scale),
                    x,
                    rune.y + 0.03 + level.random.nextDouble() * 0.25, // 贴近地面
                    z,
                    1,
                    0.12,
                    0.02,
                    0.12,
                    0.005
            );
        }
    }

    /**
     * 播放地面血迹 - 使用BLOOD_GROUND_PARTICLE
     * 
     * @param level 服务器世界
     * @param rune  符文区域
     */
    private static void playGroundBlood(ServerLevel level, ResurrectionRuneManager.ResurrectionRune rune) {
        int count = (int) (rune.range * 0.8);
        
        for (int i = 0; i < count; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * rune.range * 0.9;
            double x = rune.x + Math.cos(angle) * distance;
            double z = rune.z + Math.sin(angle) * distance;
            
            // 使用铁魔法的地面血迹粒子
            level.sendParticles(
                    ParticleRegistry.BLOOD_GROUND_PARTICLE.get(),
                    x,
                    rune.y + 0.05,
                    z,
                    1,
                    0,
                    0,
                    0,
                    0
            );
        }
    }

    /**
     * 播放中心光柱效果 - 增强版，使用血粒子、火花和冲击波
     * 
     * @param level 服务器世界
     * @param rune  符文区域
     */
    private static void playCenterPillar(ServerLevel level, ResurrectionRuneManager.ResurrectionRune rune) {
        // 1. 中心冲击波扩散
        level.sendParticles(
                new ShockwaveParticleOptions(BLOOD_COLOR, 1.5f, true),
                rune.x,
                rune.y + 0.1,
                rune.z,
                1,
                0,
                0,
                0,
                0
        );
        
        // 2. 中心向上发射的血色光柱 - 增加高度和密度
        for (int i = 0; i < 12; i++) { // 增加高度
            // 血粒子 - 增加数量和速度
            level.sendParticles(
                    ParticleRegistry.BLOOD_PARTICLE.get(),
                    rune.x,
                    rune.y + 0.3 + i * 0.4,
                    rune.z,
                    3, // 增加数量
                    0.15,
                    0.4, // 增加上升速度
                    0.15,
                    0.08
            );
            
            // 火花 - 更频繁
            if (i % 2 == 0) {
                level.sendParticles(
                        new SparkParticleOptions(BRIGHT_BLOOD_COLOR),
                        rune.x,
                        rune.y + 0.3 + i * 0.4,
                        rune.z,
                        2, // 增加数量
                        0.2,
                        0.6, // 增加上升速度
                        0.2,
                        0.15
                );
            }
            
            // 添加幽灵粒子增加神秘感
            if (i % 3 == 0) {
                level.sendParticles(
                        ParticleRegistry.WISP_PARTICLE.get(),
                        rune.x,
                        rune.y + 0.5 + i * 0.4,
                        rune.z,
                        1,
                        0.1,
                        0.5,
                        0.1,
                        0.1
                );
            }
        }
        
        // 3. 中心爆发效果
        for (int i = 0; i < 8; i++) {
            double angle = (2 * Math.PI * i) / 8;
            double x = rune.x + Math.cos(angle) * 0.5;
            double z = rune.z + Math.sin(angle) * 0.5;
            
            level.sendParticles(
                    ParticleRegistry.BLOOD_PARTICLE.get(),
                    x,
                    rune.y + 0.2,
                    z,
                    2,
                    Math.cos(angle) * 0.2,
                    0.3,
                    Math.sin(angle) * 0.2,
                    0.1
            );
        }
    }

    /**
     * 播放转化时的粒子效果
     * 生物死亡转化为亡灵时的效果
     * 
     * @param level 服务器世界
     * @param pos   位置
     */
    public static void playConversionEffect(ServerLevel level, Vec3 pos) {
        // 1. 冲击波
        level.sendParticles(
                new ShockwaveParticleOptions(SOUL_COLOR, 2.0f, true),
                pos.x,
                pos.y + 0.1,
                pos.z,
                1,
                0,
                0,
                0,
                0
        );
        
        // 2. 灵魂爆发 - 使用WISP_PARTICLE
        for (int i = 0; i < 15; i++) {
            level.sendParticles(
                    ParticleRegistry.WISP_PARTICLE.get(),
                    pos.x,
                    pos.y + 1,
                    pos.z,
                    1,
                    (level.random.nextDouble() - 0.5) * 0.5,
                    level.random.nextDouble() * 0.5,
                    (level.random.nextDouble() - 0.5) * 0.5,
                    0.1
            );
        }
        
        // 3. 血雾爆发
        for (int i = 0; i < 10; i++) {
            level.sendParticles(
                    new FogParticleOptions(BLOOD_COLOR, 1.5f),
                    pos.x,
                    pos.y + 0.5,
                    pos.z,
                    1,
                    (level.random.nextDouble() - 0.5) * 0.8,
                    level.random.nextDouble() * 0.3,
                    (level.random.nextDouble() - 0.5) * 0.8,
                    0.08
            );
        }
        
        // 4. 血滴飞溅
        for (int i = 0; i < 20; i++) {
            level.sendParticles(
                    ParticleRegistry.BLOOD_PARTICLE.get(),
                    pos.x,
                    pos.y + 1,
                    pos.z,
                    1,
                    (level.random.nextDouble() - 0.5) * 0.6,
                    level.random.nextDouble() * 0.5,
                    (level.random.nextDouble() - 0.5) * 0.6,
                    0.1
            );
        }
        
        // 5. 地面血迹
        for (int i = 0; i < 8; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * 2;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            level.sendParticles(
                    ParticleRegistry.BLOOD_GROUND_PARTICLE.get(),
                    x,
                    pos.y + 0.05,
                    z,
                    1,
                    0,
                    0,
                    0,
                    0
            );
        }
        
        // 6. 火花
        for (int i = 0; i < 10; i++) {
            level.sendParticles(
                    new SparkParticleOptions(BRIGHT_BLOOD_COLOR),
                    pos.x,
                    pos.y + 0.5 + level.random.nextDouble() * 2,
                    pos.z,
                    1,
                    (level.random.nextDouble() - 0.5) * 0.4,
                    level.random.nextDouble() * 0.4,
                    (level.random.nextDouble() - 0.5) * 0.4,
                    0.1
            );
        }
    }
}
