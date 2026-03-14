package com.legendarymage.legendarymagemod.spell;

import org.joml.Vector3f;

import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.particle.FogParticleOptions;
import io.redspace.ironsspellbooks.particle.ShockwaveParticleOptions;
import io.redspace.ironsspellbooks.particle.SparkParticleOptions;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/**
 * 纵火狂法术粒子效果管理器
 * 使用铁魔法的粒子API创建火系法术风格效果
 * 
 * @author Love_U
 * @version 0.0.1
 */
public class PyromaniacParticles {

    /**
     * 火焰核心颜色 - 亮橙色
     */
    private static final Vector3f FIRE_CORE_COLOR = new Vector3f(1.0f, 0.4f, 0.0f);
    
    /**
     * 火焰外层颜色 - 红色
     */
    private static final Vector3f FIRE_OUTER_COLOR = new Vector3f(0.9f, 0.1f, 0.0f);
    
    /**
     * 火焰中心颜色 - 黄色
     */
    private static final Vector3f FIRE_CENTER_COLOR = new Vector3f(1.0f, 0.8f, 0.0f);
    
    /**
     * 烟雾颜色 - 深灰色
     */
    private static final Vector3f SMOKE_COLOR = new Vector3f(0.2f, 0.2f, 0.2f);

    /**
     * 播放施法时的爆发粒子效果
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    public static void playCastBurst(ServerLevel level, Vec3 pos, int range) {
        // 1. 火焰冲击波
        playFireBlastwave(level, pos, range);
        
        // 2. 火焰冲击波环
        playFireShockwaveRing(level, pos, range);
        
        // 3. 火焰雾气扩散
        playFireFog(level, pos, range);
        
        // 4. 火花飞溅
        playFireSparks(level, pos, range);
        
        // 5. 火焰粒子爆发
        playFireBurst(level, pos, range);
        
        // 6. 岩浆粒子
        playLavaParticles(level, pos, range);
    }

    /**
     * 播放火焰冲击波效果 - 使用BlastwaveParticle
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    private static void playFireBlastwave(ServerLevel level, Vec3 pos, int range) {
        // 创建多层火焰冲击波
        for (int ring = 0; ring < 3; ring++) {
            double radius = range * (0.3 + ring * 0.35);
            
            // 使用铁魔法的BlastwaveParticle，火焰颜色
            level.sendParticles(
                    new BlastwaveParticleOptions(FIRE_CORE_COLOR, (float) radius * 0.5f),
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
     * 播放火焰冲击波环效果 - 使用ShockwaveParticle
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    private static void playFireShockwaveRing(ServerLevel level, Vec3 pos, int range) {
        // 使用ShockwaveParticle创建火焰环形冲击波
        level.sendParticles(
                new ShockwaveParticleOptions(FIRE_OUTER_COLOR, range * 0.8f, true),
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
     * 播放火焰雾气效果 - 使用FogParticle
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    private static void playFireFog(ServerLevel level, Vec3 pos, int range) {
        int fogCount = range * 2;
        
        for (int i = 0; i < fogCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range * 0.9;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            float scale = 0.8f + level.random.nextFloat() * 0.4f;
            
            level.sendParticles(
                    new FogParticleOptions(FIRE_CORE_COLOR, scale),
                    x,
                    pos.y + 0.1 + level.random.nextDouble() * 0.5,
                    z,
                    1,
                    0.1,
                    0.05,
                    0.1,
                    0.01
            );
        }
    }

    /**
     * 播放火花效果 - 使用SparkParticle
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    private static void playFireSparks(ServerLevel level, Vec3 pos, int range) {
        int sparkCount = range * 5;
        
        for (int i = 0; i < sparkCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            // 使用SparkParticle创建火花
            level.sendParticles(
                    new SparkParticleOptions(FIRE_CENTER_COLOR),
                    x,
                    pos.y + 0.5 + level.random.nextDouble() * 2,
                    z,
                    1,
                    (level.random.nextDouble() - 0.5) * 0.3,
                    level.random.nextDouble() * 0.4,
                    (level.random.nextDouble() - 0.5) * 0.3,
                    0.1
            );
        }
    }

    /**
     * 播放火焰粒子爆发
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    private static void playFireBurst(ServerLevel level, Vec3 pos, int range) {
        int fireCount = range * 8;
        
        for (int i = 0; i < fireCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            // 使用铁魔法的火焰粒子
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
        }
    }

    /**
     * 播放岩浆粒子
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    private static void playLavaParticles(ServerLevel level, Vec3 pos, int range) {
        int lavaCount = range * 3;
        
        for (int i = 0; i < lavaCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range * 0.8;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            // 使用原版岩浆粒子
            level.sendParticles(
                    ParticleTypes.LAVA,
                    x,
                    pos.y + 0.1,
                    z,
                    1,
                    0.1,
                    0.1,
                    0.1,
                    0.02
            );
        }
    }

    /**
     * 播放单个目标受击效果
     * 
     * @param level  服务器世界
     * @param pos    目标位置
     * @param height 目标高度
     * @param width  目标宽度
     */
    public static void playTargetHitEffect(ServerLevel level, Vec3 pos, double height, double width) {
        // 火焰粒子环绕
        level.sendParticles(
                ParticleRegistry.FIRE_PARTICLE.get(),
                pos.x,
                pos.y + height * 0.5,
                pos.z,
                10,
                width * 0.3,
                height * 0.3,
                width * 0.3,
                0.05
        );

        // 火花飞溅
        level.sendParticles(
                new SparkParticleOptions(FIRE_CENTER_COLOR),
                pos.x,
                pos.y + height * 0.5,
                pos.z,
                8,
                width * 0.2,
                height * 0.2,
                width * 0.2,
                0.08
        );
        
        // 烟雾
        level.sendParticles(
                new FogParticleOptions(SMOKE_COLOR, 0.5f),
                pos.x,
                pos.y + height * 0.5,
                pos.z,
                5,
                width * 0.2,
                height * 0.2,
                width * 0.2,
                0.02
        );
    }

    /**
     * 播放烈焰Buff持续效果
     * 
     * @param level     服务器世界
     * @param pos       实体位置
     * @param height    实体高度
     * @param width     实体宽度
     * @param tickCount 当前tick数
     */
    public static void playBuffAmbientEffect(ServerLevel level, Vec3 pos, double height, double width, int tickCount) {
        // 每5tick播放一次效果
        if (tickCount % 5 != 0) return;
        
        // 火焰粒子
        level.sendParticles(
                ParticleRegistry.FIRE_PARTICLE.get(),
                pos.x,
                pos.y + height * 0.5,
                pos.z,
                2,
                width * 0.2,
                height * 0.2,
                width * 0.2,
                0.02
        );
        
        // 偶尔播放火花
        if (tickCount % 10 == 0) {
            level.sendParticles(
                    new SparkParticleOptions(FIRE_CENTER_COLOR),
                    pos.x,
                    pos.y + height * 0.6,
                    pos.z,
                    1,
                    width * 0.1,
                    height * 0.1,
                    width * 0.1,
                    0.03
            );
        }
    }

    /**
     * 播放爆炸效果（死亡时触发）
     * 
     * @param level           服务器世界
     * @param pos             爆炸位置
     * @param explosionPower  爆炸威力
     */
    public static void playExplosionEffect(ServerLevel level, Vec3 pos, float explosionPower, double entityHeight) {
        // 1. 火焰冲击波
        level.sendParticles(
                new ShockwaveParticleOptions(FIRE_OUTER_COLOR, explosionPower * 0.8f, true),
                pos.x,
                pos.y + entityHeight * 0.5,
                pos.z,
                1,
                0,
                0,
                0,
                0
        );
        
        // 2. 火焰爆发
        int fireCount = (int) (10 * explosionPower);
        level.sendParticles(
                ParticleRegistry.FIRE_PARTICLE.get(),
                pos.x,
                pos.y + entityHeight * 0.5,
                pos.z,
                fireCount,
                explosionPower * 0.3,
                explosionPower * 0.3,
                explosionPower * 0.3,
                0.1 * explosionPower
        );
        
        // 3. 火花爆发
        int sparkCount = (int) (8 * explosionPower);
        for (int i = 0; i < sparkCount; i++) {
            level.sendParticles(
                    new SparkParticleOptions(FIRE_CENTER_COLOR),
                    pos.x,
                    pos.y + entityHeight * 0.5,
                    pos.z,
                    1,
                    (level.random.nextDouble() - 0.5) * explosionPower * 0.4,
                    level.random.nextDouble() * explosionPower * 0.3,
                    (level.random.nextDouble() - 0.5) * explosionPower * 0.4,
                    0.1
            );
        }
        
        // 4. 烟雾
        int smokeCount = (int) (6 * explosionPower);
        level.sendParticles(
                new FogParticleOptions(SMOKE_COLOR, 0.8f * explosionPower),
                pos.x,
                pos.y + entityHeight * 0.5,
                pos.z,
                smokeCount,
                explosionPower * 0.2,
                explosionPower * 0.2,
                explosionPower * 0.2,
                0.03
        );
        
        // 5. 岩浆粒子
        level.sendParticles(
                ParticleTypes.LAVA,
                pos.x,
                pos.y + entityHeight * 0.3,
                pos.z,
                (int) (4 * explosionPower),
                explosionPower * 0.2,
                explosionPower * 0.1,
                explosionPower * 0.2,
                0.05
        );
    }
}
