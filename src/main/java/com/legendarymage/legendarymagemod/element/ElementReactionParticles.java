package com.legendarymage.legendarymagemod.element;

import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.particle.FogParticleOptions;
import io.redspace.ironsspellbooks.particle.ShockwaveParticleOptions;
import io.redspace.ironsspellbooks.particle.SparkParticleOptions;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * 元素反应粒子效果管理器
 * 使用铁魔法的粒子API创建炫酷的元素反应效果
 * 
 * @author Love_U
 * @version 0.0.1
 */
public class ElementReactionParticles {

    // ==================== 颜色定义 ====================
    
    /** 火焰核心颜色 - 亮橙色 */
    private static final Vector3f FIRE_CORE = new Vector3f(1.0f, 0.4f, 0.0f);
    /** 火焰外层颜色 - 红色 */
    private static final Vector3f FIRE_OUTER = new Vector3f(0.9f, 0.1f, 0.0f);
    /** 火焰中心颜色 - 黄色 */
    private static final Vector3f FIRE_CENTER = new Vector3f(1.0f, 0.8f, 0.0f);
    
    /** 冰霜核心颜色 - 青色 */
    private static final Vector3f ICE_CORE = new Vector3f(0.5f, 0.9f, 1.0f);
    /** 冰霜外层颜色 - 蓝色 */
    private static final Vector3f ICE_OUTER = new Vector3f(0.3f, 0.7f, 0.95f);
    /** 冰晶颜色 - 亮蓝色 */
    private static final Vector3f ICE_CRYSTAL = new Vector3f(0.7f, 0.95f, 1.0f);
    
    /** 雷电核心颜色 - 亮紫色 */
    private static final Vector3f LIGHTNING_CORE = new Vector3f(0.9f, 0.9f, 1.0f);
    /** 雷电外层颜色 - 紫色 */
    private static final Vector3f LIGHTNING_OUTER = new Vector3f(0.7f, 0.5f, 1.0f);
    
    /** 毒素核心颜色 - 酸橙绿 */
    private static final Vector3f POISON_CORE = new Vector3f(0.5f, 1.0f, 0.2f);
    /** 毒素外层颜色 - 深绿色 */
    private static final Vector3f POISON_OUTER = new Vector3f(0.2f, 0.7f, 0.1f);
    
    /** 神圣核心颜色 - 金色 */
    private static final Vector3f HOLY_CORE = new Vector3f(1.0f, 0.9f, 0.3f);
    /** 神圣外层颜色 - 亮黄色 */
    private static final Vector3f HOLY_OUTER = new Vector3f(1.0f, 0.8f, 0.1f);
    /** 神圣中心颜色 - 亮金色 */
    private static final Vector3f HOLY_CENTER = new Vector3f(1.0f, 0.95f, 0.5f);
    
    /** 猩红核心颜色 - 血红色 */
    private static final Vector3f BLOOD_CORE = new Vector3f(0.8f, 0.0f, 0.1f);
    /** 猩红外层颜色 - 深红色 */
    private static final Vector3f BLOOD_OUTER = new Vector3f(0.6f, 0.0f, 0.05f);
    
    /** 邪术核心颜色 - 紫色 */
    private static final Vector3f ELDRITCH_CORE = new Vector3f(0.6f, 0.2f, 0.8f);
    /** 邪术外层颜色 - 深紫色 */
    private static final Vector3f ELDRITCH_OUTER = new Vector3f(0.4f, 0.1f, 0.6f);
    
    /** 末影核心颜色 - 深紫色 */
    private static final Vector3f ENDER_CORE = new Vector3f(0.6f, 0.3f, 0.8f);
    /** 末影外层颜色 - 黑色 */
    private static final Vector3f ENDER_OUTER = new Vector3f(0.2f, 0.1f, 0.3f);

    // ==================== 冰火反应效果 ====================
    
    /**
     * 播放冰火反应效果
     * 蒸汽爆发 + 冰火混合粒子
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    public static void playIceFireReaction(ServerLevel level, Vec3 pos, double range) {
        // 1. 蒸汽爆发 - 使用白色雾气
        playSteamBurst(level, pos, range);
        
        // 2. 冰火冲击波
        playIceFireShockwave(level, pos, range);
        
        // 3. 火焰与冰霜粒子混合
        playFireIceMix(level, pos, range);
        
        // 4. 冰晶与火花飞溅
        playIceFireSparks(level, pos, range);
        
        // 5. 蒸汽环绕
        playSteamAmbient(level, pos, range);
    }
    
    /**
     * 播放蒸汽爆发效果
     */
    private static void playSteamBurst(ServerLevel level, Vec3 pos, double range) {
        Vector3f steamColor = new Vector3f(0.9f, 0.9f, 0.95f);
        int steamCount = (int) (range * 5);
        
        for (int i = 0; i < steamCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            float scale = 0.8f + level.random.nextFloat() * 0.6f;
            
            level.sendParticles(
                    new FogParticleOptions(steamColor, scale),
                    x,
                    pos.y + 0.1 + level.random.nextDouble() * 1.5,
                    z,
                    1,
                    0.1,
                    0.2,
                    0.1,
                    0.02
            );
        }
    }
    
    /**
     * 播放冰火冲击波效果
     */
    private static void playIceFireShockwave(ServerLevel level, Vec3 pos, double range) {
        // 火焰冲击波
        level.sendParticles(
                new ShockwaveParticleOptions(FIRE_OUTER, (float) range * 0.6f, true),
                pos.x,
                pos.y + 0.1,
                pos.z,
                2,
                0.3,
                0,
                0.3,
                0.1
        );
        
        // 冰霜冲击波（延迟一点）
        level.sendParticles(
                new ShockwaveParticleOptions(ICE_OUTER, (float) range * 0.5f, false),
                pos.x,
                pos.y + 0.2,
                pos.z,
                2,
                0.2,
                0,
                0.2,
                0.05
        );
    }
    
    /**
     * 播放火焰与冰霜混合效果
     */
    private static void playFireIceMix(ServerLevel level, Vec3 pos, double range) {
        int particleCount = (int) (range * 6);
        
        for (int i = 0; i < particleCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            // 交替生成火焰和冰霜粒子
            if (i % 2 == 0) {
                // 火焰粒子
                level.sendParticles(
                        ParticleRegistry.FIRE_PARTICLE.get(),
                        x,
                        pos.y + 0.3 + level.random.nextDouble() * 2,
                        z,
                        1,
                        0.1,
                        0.2,
                        0.1,
                        0.05
                );
            } else {
                // 冰霜粒子
                level.sendParticles(
                        ParticleRegistry.SNOWFLAKE_PARTICLE.get(),
                        x,
                        pos.y + 0.3 + level.random.nextDouble() * 2,
                        z,
                        1,
                        0.1,
                        0.2,
                        0.1,
                        0.03
                );
            }
        }
    }
    
    /**
     * 播放冰火火花飞溅效果
     */
    private static void playIceFireSparks(ServerLevel level, Vec3 pos, double range) {
        int sparkCount = (int) (range * 4);
        
        for (int i = 0; i < sparkCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range * 0.8;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            // 交替生成火花和冰晶
            Vector3f color = (i % 2 == 0) ? FIRE_CENTER : ICE_CRYSTAL;
            
            level.sendParticles(
                    new SparkParticleOptions(color),
                    x,
                    pos.y + 0.5 + level.random.nextDouble() * 2,
                    z,
                    1,
                    (level.random.nextDouble() - 0.5) * 0.3,
                    level.random.nextDouble() * 0.4,
                    (level.random.nextDouble() - 0.5) * 0.3,
                    0.08
            );
        }
    }
    
    /**
     * 播放蒸汽环绕效果
     */
    private static void playSteamAmbient(ServerLevel level, Vec3 pos, double range) {
        Vector3f steamColor = new Vector3f(0.85f, 0.85f, 0.9f);
        int ambientCount = (int) (range * 2);
        
        for (int i = 0; i < ambientCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range * 0.9;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            level.sendParticles(
                    new FogParticleOptions(steamColor, 0.5f),
                    x,
                    pos.y + 0.1 + level.random.nextDouble() * 0.5,
                    z,
                    1,
                    0.05,
                    0.1,
                    0.05,
                    0.01
            );
        }
    }

    // ==================== 木火反应效果 ====================
    
    /**
     * 播放木火（毒火）反应效果
     * 绿色火焰爆发 + 毒雾扩散
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    public static void playPoisonFireReaction(ServerLevel level, Vec3 pos, double range) {
        // 1. 毒火冲击波
        playPoisonFireShockwave(level, pos, range);
        
        // 2. 毒雾爆发
        playPoisonFogBurst(level, pos, range);
        
        // 3. 绿色火焰粒子
        playGreenFireParticles(level, pos, range);
        
        // 4. 毒液飞溅
        playPoisonSplash(level, pos, range);
        
        // 5. 毒气环绕
        playPoisonAmbient(level, pos, range);
    }
    
    /**
     * 播放毒火冲击波效果
     */
    private static void playPoisonFireShockwave(ServerLevel level, Vec3 pos, double range) {
        // 绿色火焰冲击波
        level.sendParticles(
                new ShockwaveParticleOptions(POISON_CORE, (float) range * 0.7f, true),
                pos.x,
                pos.y + 0.1,
                pos.z,
                3,
                0.4,
                0,
                0.4,
                0.1
        );
    }
    
    /**
     * 播放毒雾爆发效果
     */
    private static void playPoisonFogBurst(ServerLevel level, Vec3 pos, double range) {
        int fogCount = (int) (range * 6);
        
        for (int i = 0; i < fogCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            float scale = 0.9f + level.random.nextFloat() * 0.7f;
            
            level.sendParticles(
                    new FogParticleOptions(POISON_OUTER, scale),
                    x,
                    pos.y + 0.1 + level.random.nextDouble() * 1.5,
                    z,
                    1,
                    0.15,
                    0.1,
                    0.15,
                    0.02
            );
        }
    }
    
    /**
     * 播放绿色火焰粒子效果
     */
    private static void playGreenFireParticles(ServerLevel level, Vec3 pos, double range) {
        int fireCount = (int) (range * 8);
        
        for (int i = 0; i < fireCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            // 使用女巫粒子模拟毒火
            level.sendParticles(
                    ParticleTypes.WITCH,
                    x,
                    pos.y + 0.3 + level.random.nextDouble() * 2,
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
     * 播放毒液飞溅效果
     */
    private static void playPoisonSplash(ServerLevel level, Vec3 pos, double range) {
        int splashCount = (int) (range * 5);
        
        for (int i = 0; i < splashCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range * 0.8;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            level.sendParticles(
                    new SparkParticleOptions(POISON_CORE),
                    x,
                    pos.y + 0.5 + level.random.nextDouble() * 1.5,
                    z,
                    1,
                    (level.random.nextDouble() - 0.5) * 0.3,
                    level.random.nextDouble() * 0.3,
                    (level.random.nextDouble() - 0.5) * 0.3,
                    0.06
            );
        }
    }
    
    /**
     * 播放毒气环绕效果
     */
    private static void playPoisonAmbient(ServerLevel level, Vec3 pos, double range) {
        int ambientCount = (int) (range * 3);
        
        for (int i = 0; i < ambientCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range * 0.9;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            level.sendParticles(
                    new FogParticleOptions(POISON_OUTER, 0.6f),
                    x,
                    pos.y + 0.1 + level.random.nextDouble() * 0.4,
                    z,
                    1,
                    0.08,
                    0.05,
                    0.08,
                    0.01
            );
        }
    }

    // ==================== 雷火反应效果 ====================
    
    /**
     * 播放雷火反应效果
     * 雷电爆发 + 火焰闪电混合
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    public static void playLightningFireReaction(ServerLevel level, Vec3 pos, double range) {
        // 1. 雷电冲击波
        playLightningShockwave(level, pos, range);
        
        // 2. 电火花爆发
        playElectricSparkBurst(level, pos, range);
        
        // 3. 火焰与雷电混合
        playFireLightningMix(level, pos, range);
        
        // 4. 雷电轨迹
        playLightningTrails(level, pos, range);
        
        // 5. 电光环绕
        playElectricAmbient(level, pos, range);
    }
    
    /**
     * 播放雷电冲击波效果
     */
    private static void playLightningShockwave(ServerLevel level, Vec3 pos, double range) {
        // 雷电冲击波
        level.sendParticles(
                new ShockwaveParticleOptions(LIGHTNING_OUTER, (float) range * 0.8f, true),
                pos.x,
                pos.y + 0.1,
                pos.z,
                4,
                0.5,
                0,
                0.5,
                0.15
        );
        
        // 内部白色核心
        level.sendParticles(
                new BlastwaveParticleOptions(LIGHTNING_CORE, (float) range * 0.4f),
                pos.x,
                pos.y + 0.2,
                pos.z,
                2,
                0,
                0,
                0,
                0
        );
    }
    
    /**
     * 播放电火花爆发效果
     */
    private static void playElectricSparkBurst(ServerLevel level, Vec3 pos, double range) {
        int sparkCount = (int) (range * 10);
        
        for (int i = 0; i < sparkCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            level.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    x,
                    pos.y + 0.3 + level.random.nextDouble() * 2.5,
                    z,
                    1,
                    (level.random.nextDouble() - 0.5) * 0.4,
                    level.random.nextDouble() * 0.5,
                    (level.random.nextDouble() - 0.5) * 0.4,
                    0.1
            );
        }
    }
    
    /**
     * 播放火焰与雷电混合效果
     */
    private static void playFireLightningMix(ServerLevel level, Vec3 pos, double range) {
        int mixCount = (int) (range * 6);
        
        for (int i = 0; i < mixCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            if (i % 3 == 0) {
                // 火焰
                level.sendParticles(
                        ParticleRegistry.FIRE_PARTICLE.get(),
                        x,
                        pos.y + 0.3 + level.random.nextDouble() * 2,
                        z,
                        1,
                        0.1,
                        0.2,
                        0.1,
                        0.05
                );
            } else {
                // 电火花
                level.sendParticles(
                        new SparkParticleOptions(LIGHTNING_CORE),
                        x,
                        pos.y + 0.5 + level.random.nextDouble() * 2,
                        z,
                        1,
                        (level.random.nextDouble() - 0.5) * 0.3,
                        level.random.nextDouble() * 0.4,
                        (level.random.nextDouble() - 0.5) * 0.3,
                        0.08
                );
            }
        }
    }
    
    /**
     * 播放雷电轨迹效果
     * 使用火花粒子模拟闪电轨迹
     */
    private static void playLightningTrails(ServerLevel level, Vec3 pos, double range) {
        int trailCount = (int) (range * 3);
        
        for (int i = 0; i < trailCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range * 0.7;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            // 使用火花粒子模拟向上的闪电轨迹
            int steps = 5 + level.random.nextInt(5);
            for (int j = 0; j < steps; j++) {
                double y = pos.y + 0.5 + (j * 0.4);
                double offsetX = (level.random.nextDouble() - 0.5) * 0.2;
                double offsetZ = (level.random.nextDouble() - 0.5) * 0.2;
                
                level.sendParticles(
                        new SparkParticleOptions(LIGHTNING_CORE),
                        x + offsetX,
                        y,
                        z + offsetZ,
                        1,
                        0.05,
                        0.3,
                        0.05,
                        0.05
                );
            }
        }
    }
    
    /**
     * 播放电光环绕效果
     */
    private static void playElectricAmbient(ServerLevel level, Vec3 pos, double range) {
        int ambientCount = (int) (range * 4);
        
        for (int i = 0; i < ambientCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range * 0.9;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            level.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    x,
                    pos.y + 0.2 + level.random.nextDouble() * 0.5,
                    z,
                    1,
                    0.1,
                    0.1,
                    0.1,
                    0.05
            );
        }
    }

    // ==================== 神圣-猩红反应效果 ====================
    
    /**
     * 播放神圣-猩红反应效果
     * 光暗碰撞 + 能量爆发
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    public static void playHolyBloodReaction(ServerLevel level, Vec3 pos, double range) {
        // 1. 光暗冲击波
        playHolyBloodShockwave(level, pos, range);
        
        // 2. 能量爆发
        playEnergyBurst(level, pos, range);
        
        // 3. 光暗粒子混合
        playHolyBloodMix(level, pos, range);
        
        // 4. 能量火花
        playEnergySparks(level, pos, range);
        
        // 5. 光暗环绕
        playHolyBloodAmbient(level, pos, range);
    }
    
    /**
     * 播放光暗冲击波效果
     */
    private static void playHolyBloodShockwave(ServerLevel level, Vec3 pos, double range) {
        // 神圣冲击波
        level.sendParticles(
                new ShockwaveParticleOptions(HOLY_OUTER, (float) range * 0.7f, true),
                pos.x,
                pos.y + 0.1,
                pos.z,
                3,
                0.4,
                0,
                0.4,
                0.1
        );
        
        // 猩红冲击波（反向）
        level.sendParticles(
                new ShockwaveParticleOptions(BLOOD_OUTER, (float) range * 0.6f, false),
                pos.x,
                pos.y + 0.2,
                pos.z,
                3,
                0.3,
                0,
                0.3,
                0.08
        );
    }
    
    /**
     * 播放能量爆发效果
     */
    private static void playEnergyBurst(ServerLevel level, Vec3 pos, double range) {
        int burstCount = (int) (range * 8);
        
        for (int i = 0; i < burstCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            // 交替使用金色和血红色
            Vector3f color = (i % 2 == 0) ? HOLY_CORE : BLOOD_CORE;
            
            level.sendParticles(
                    new SparkParticleOptions(color),
                    x,
                    pos.y + 0.5 + level.random.nextDouble() * 2,
                    z,
                    1,
                    (level.random.nextDouble() - 0.5) * 0.3,
                    level.random.nextDouble() * 0.4,
                    (level.random.nextDouble() - 0.5) * 0.3,
                    0.08
            );
        }
    }
    
    /**
     * 播放光暗粒子混合效果
     */
    private static void playHolyBloodMix(ServerLevel level, Vec3 pos, double range) {
        int mixCount = (int) (range * 6);
        
        for (int i = 0; i < mixCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            if (i % 2 == 0) {
                // 神圣粒子 - 使用End Rod
                level.sendParticles(
                        ParticleTypes.END_ROD,
                        x,
                        pos.y + 0.3 + level.random.nextDouble() * 2,
                        z,
                        1,
                        0.1,
                        0.2,
                        0.1,
                        0.03
                );
            } else {
                // 猩红粒子 - 使用Damage Indicator
                level.sendParticles(
                        ParticleTypes.DAMAGE_INDICATOR,
                        x,
                        pos.y + 0.3 + level.random.nextDouble() * 2,
                        z,
                        1,
                        0.1,
                        0.2,
                        0.1,
                        0.02
                );
            }
        }
    }
    
    /**
     * 播放能量火花效果
     */
    private static void playEnergySparks(ServerLevel level, Vec3 pos, double range) {
        int sparkCount = (int) (range * 5);
        
        for (int i = 0; i < sparkCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range * 0.8;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            Vector3f color = (i % 2 == 0) ? HOLY_CENTER : BLOOD_CORE;
            
            level.sendParticles(
                    new SparkParticleOptions(color),
                    x,
                    pos.y + 0.5 + level.random.nextDouble() * 1.5,
                    z,
                    1,
                    (level.random.nextDouble() - 0.5) * 0.25,
                    level.random.nextDouble() * 0.3,
                    (level.random.nextDouble() - 0.5) * 0.25,
                    0.06
            );
        }
    }
    
    /**
     * 播放光暗环绕效果
     */
    private static void playHolyBloodAmbient(ServerLevel level, Vec3 pos, double range) {
        int ambientCount = (int) (range * 3);
        
        for (int i = 0; i < ambientCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range * 0.9;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            Vector3f color = (i % 2 == 0) ? HOLY_OUTER : BLOOD_OUTER;
            
            level.sendParticles(
                    new FogParticleOptions(color, 0.5f),
                    x,
                    pos.y + 0.1 + level.random.nextDouble() * 0.4,
                    z,
                    1,
                    0.06,
                    0.04,
                    0.06,
                    0.01
            );
        }
    }

    // ==================== 邪术-猩红反应效果 ====================
    
    /**
     * 播放邪术-猩红反应效果
     * 混沌能量爆发 + 紫色血雾
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    public static void playEldritchBloodReaction(ServerLevel level, Vec3 pos, double range) {
        // 1. 混沌冲击波
        playChaosShockwave(level, pos, range);
        
        // 2. 混沌能量爆发
        playChaosBurst(level, pos, range);
        
        // 3. 紫色血雾
        playPurpleBloodFog(level, pos, range);
        
        // 4. 混沌火花
        playChaosSparks(level, pos, range);
        
        // 5. 混沌环绕
        playChaosAmbient(level, pos, range);
    }
    
    /**
     * 播放混沌冲击波效果
     */
    private static void playChaosShockwave(ServerLevel level, Vec3 pos, double range) {
        // 邪术冲击波
        level.sendParticles(
                new ShockwaveParticleOptions(ELDRITCH_OUTER, (float) range * 0.8f, true),
                pos.x,
                pos.y + 0.1,
                pos.z,
                4,
                0.5,
                0,
                0.5,
                0.12
        );
        
        // 内部猩红核心
        level.sendParticles(
                new BlastwaveParticleOptions(BLOOD_CORE, (float) range * 0.4f),
                pos.x,
                pos.y + 0.2,
                pos.z,
                2,
                0,
                0,
                0,
                0
        );
    }
    
    /**
     * 播放混沌能量爆发效果
     */
    private static void playChaosBurst(ServerLevel level, Vec3 pos, double range) {
        int burstCount = (int) (range * 10);
        
        for (int i = 0; i < burstCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            // 使用传送门粒子模拟混沌能量
            level.sendParticles(
                    ParticleTypes.PORTAL,
                    x,
                    pos.y + 0.3 + level.random.nextDouble() * 2.5,
                    z,
                    1,
                    (level.random.nextDouble() - 0.5) * 0.3,
                    level.random.nextDouble() * 0.4,
                    (level.random.nextDouble() - 0.5) * 0.3,
                    0.08
            );
        }
    }
    
    /**
     * 播放紫色血雾效果
     */
    private static void playPurpleBloodFog(ServerLevel level, Vec3 pos, double range) {
        int fogCount = (int) (range * 7);
        
        for (int i = 0; i < fogCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            float scale = 1.0f + level.random.nextFloat() * 0.8f;
            
            level.sendParticles(
                    new FogParticleOptions(ELDRITCH_CORE, scale),
                    x,
                    pos.y + 0.1 + level.random.nextDouble() * 1.8,
                    z,
                    1,
                    0.2,
                    0.15,
                    0.2,
                    0.03
            );
        }
    }
    
    /**
     * 播放混沌火花效果
     */
    private static void playChaosSparks(ServerLevel level, Vec3 pos, double range) {
        int sparkCount = (int) (range * 6);
        
        for (int i = 0; i < sparkCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range * 0.8;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            Vector3f color = (i % 2 == 0) ? ELDRITCH_CORE : BLOOD_CORE;
            
            level.sendParticles(
                    new SparkParticleOptions(color),
                    x,
                    pos.y + 0.5 + level.random.nextDouble() * 2,
                    z,
                    1,
                    (level.random.nextDouble() - 0.5) * 0.35,
                    level.random.nextDouble() * 0.4,
                    (level.random.nextDouble() - 0.5) * 0.35,
                    0.09
            );
        }
    }
    
    /**
     * 播放混沌环绕效果
     */
    private static void playChaosAmbient(ServerLevel level, Vec3 pos, double range) {
        int ambientCount = (int) (range * 4);
        
        for (int i = 0; i < ambientCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range * 0.9;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            level.sendParticles(
                    ParticleTypes.REVERSE_PORTAL,
                    x,
                    pos.y + 0.1 + level.random.nextDouble() * 0.5,
                    z,
                    1,
                    0.1,
                    0.08,
                    0.1,
                    0.02
            );
        }
    }

    // ==================== 末影-任意反应效果 ====================
    
    /**
     * 播放末影-任意反应效果
     * 虚空能量爆发 + 末影粒子
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    public static void playEnderAnyReaction(ServerLevel level, Vec3 pos, double range) {
        // 1. 虚空冲击波
        playVoidShockwave(level, pos, range);
        
        // 2. 末影粒子爆发
        playEnderBurst(level, pos, range);
        
        // 3. 虚空雾气
        playVoidFog(level, pos, range);
        
        // 4. 末影火花
        playEnderSparks(level, pos, range);
        
        // 5. 末影环绕
        playEnderAmbient(level, pos, range);
    }
    
    /**
     * 播放虚空冲击波效果
     */
    private static void playVoidShockwave(ServerLevel level, Vec3 pos, double range) {
        // 末影冲击波
        level.sendParticles(
                new ShockwaveParticleOptions(ENDER_OUTER, (float) range * 0.7f, true),
                pos.x,
                pos.y + 0.1,
                pos.z,
                3,
                0.4,
                0,
                0.4,
                0.1
        );
        
        // 内部紫色核心
        level.sendParticles(
                new BlastwaveParticleOptions(ENDER_CORE, (float) range * 0.35f),
                pos.x,
                pos.y + 0.2,
                pos.z,
                2,
                0,
                0,
                0,
                0
        );
    }
    
    /**
     * 播放末影粒子爆发效果
     */
    private static void playEnderBurst(ServerLevel level, Vec3 pos, double range) {
        int burstCount = (int) (range * 8);
        
        for (int i = 0; i < burstCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            level.sendParticles(
                    ParticleTypes.PORTAL,
                    x,
                    pos.y + 0.3 + level.random.nextDouble() * 2,
                    z,
                    1,
                    (level.random.nextDouble() - 0.5) * 0.25,
                    level.random.nextDouble() * 0.35,
                    (level.random.nextDouble() - 0.5) * 0.25,
                    0.06
            );
        }
    }
    
    /**
     * 播放虚空雾气效果
     */
    private static void playVoidFog(ServerLevel level, Vec3 pos, double range) {
        int fogCount = (int) (range * 5);
        
        for (int i = 0; i < fogCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            float scale = 0.8f + level.random.nextFloat() * 0.6f;
            
            level.sendParticles(
                    new FogParticleOptions(ENDER_OUTER, scale),
                    x,
                    pos.y + 0.1 + level.random.nextDouble() * 1.2,
                    z,
                    1,
                    0.12,
                    0.08,
                    0.12,
                    0.02
            );
        }
    }
    
    /**
     * 播放末影火花效果
     */
    private static void playEnderSparks(ServerLevel level, Vec3 pos, double range) {
        int sparkCount = (int) (range * 5);
        
        for (int i = 0; i < sparkCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range * 0.8;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            level.sendParticles(
                    new SparkParticleOptions(ENDER_CORE),
                    x,
                    pos.y + 0.5 + level.random.nextDouble() * 1.8,
                    z,
                    1,
                    (level.random.nextDouble() - 0.5) * 0.3,
                    level.random.nextDouble() * 0.35,
                    (level.random.nextDouble() - 0.5) * 0.3,
                    0.07
            );
        }
    }
    
    /**
     * 播放末影环绕效果
     */
    private static void playEnderAmbient(ServerLevel level, Vec3 pos, double range) {
        int ambientCount = (int) (range * 4);
        
        for (int i = 0; i < ambientCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range * 0.9;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            level.sendParticles(
                    ParticleTypes.DRAGON_BREATH,
                    x,
                    pos.y + 0.1 + level.random.nextDouble() * 0.4,
                    z,
                    1,
                    0.08,
                    0.06,
                    0.08,
                    0.01
            );
        }
    }

    // ==================== 通用效果 ====================
    
    /**
     * 播放元素反应通用爆发效果
     * 适用于所有反应类型的基础效果
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param color 粒子颜色
     * @param range 范围
     */
    public static void playGenericReactionBurst(ServerLevel level, Vec3 pos, Vector3f color, double range) {
        // 冲击波
        level.sendParticles(
                new ShockwaveParticleOptions(color, (float) range * 0.6f, true),
                pos.x,
                pos.y + 0.1,
                pos.z,
                2,
                0.3,
                0,
                0.3,
                0.08
        );
        
        // 粒子爆发
        int burstCount = (int) (range * 6);
        for (int i = 0; i < burstCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            level.sendParticles(
                    new SparkParticleOptions(color),
                    x,
                    pos.y + 0.5 + level.random.nextDouble() * 1.5,
                    z,
                    1,
                    (level.random.nextDouble() - 0.5) * 0.25,
                    level.random.nextDouble() * 0.3,
                    (level.random.nextDouble() - 0.5) * 0.25,
                    0.06
            );
        }
    }
    
    /**
     * 播放元素标记升级效果
     * 当元素标记升级时播放
     * 
     * @param level       服务器世界
     * @param target      目标实体
     * @param elementType 元素类型
     * @param newLevel    新等级
     */
    public static void playMarkUpgradeEffect(ServerLevel level, LivingEntity target, ElementType elementType, int newLevel) {
        Vec3 pos = target.position();
        Vector3f color = getElementColor(elementType);
        
        // 升级光环
        level.sendParticles(
                new BlastwaveParticleOptions(color, 0.5f * newLevel),
                pos.x,
                pos.y + target.getBbHeight() * 0.5,
                pos.z,
                1,
                0,
                0,
                0,
                0
        );
        
        // 升级粒子
        int particleCount = newLevel * 8;
        for (int i = 0; i < particleCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * 0.5 * newLevel;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            level.sendParticles(
                    new SparkParticleOptions(color),
                    x,
                    pos.y + target.getBbHeight() * 0.5 + level.random.nextDouble() * 0.5,
                    z,
                    1,
                    0.05,
                    0.1,
                    0.05,
                    0.03
            );
        }
    }
    
    /**
     * 获取元素对应的颜色
     * 
     * @param elementType 元素类型
     * @return 颜色向量
     */
    private static Vector3f getElementColor(ElementType elementType) {
        return switch (elementType) {
            case FIRE -> FIRE_CORE;
            case ICE -> ICE_CORE;
            case LIGHTNING -> LIGHTNING_CORE;
            case POISON -> POISON_CORE;
            case HOLY -> HOLY_CORE;
            case BLOOD -> BLOOD_CORE;
            case ELDRITCH -> ELDRITCH_CORE;
            case ENDER -> ENDER_CORE;
        };
    }
}
