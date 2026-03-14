package com.legendarymage.legendarymagemod.spell;

import org.joml.Vector3f;

import io.redspace.ironsspellbooks.particle.FogParticleOptions;
import io.redspace.ironsspellbooks.particle.SparkParticleOptions;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/**
 * 暴风雪粒子效果管理器
 * 使用铁魔法的粒子API创建暴风雪风格效果
 * 
 * @author Love_U
 * @version 0.0.1
 */
public class BlizzardParticles {

    /**
     * 雪花颜色 - 白色
     */
    private static final Vector3f SNOW_COLOR = new Vector3f(0.9f, 0.95f, 1.0f);
    
    /**
     * 冰雾颜色 - 淡蓝色
     */
    private static final Vector3f ICE_FOG_COLOR = new Vector3f(0.7f, 0.85f, 1.0f);
    
    /**
     * 冰晶颜色 - 亮蓝色
     */
    private static final Vector3f ICE_CRYSTAL_COLOR = new Vector3f(0.5f, 0.8f, 1.0f);
    
    /**
     * 寒冷气息颜色 - 深蓝色
     */
    private static final Vector3f COLD_MIST_COLOR = new Vector3f(0.4f, 0.7f, 0.95f);

    /**
     * 播放施法时的粒子效果
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    public static void playCastEffect(ServerLevel level, Vec3 pos, double range) {
        // 1. 冰雾爆发
        playIceFogBurst(level, pos, range);
        
        // 2. 雪花爆发
        playSnowflakeBurst(level, pos, range);
        
        // 3. 冰晶飞溅
        playIceCrystalBurst(level, pos, range);
        
        // 4. 寒冷气息扩散
        playColdMistBurst(level, pos, range);
        
        // 5. 冰块粒子
        playIceParticles(level, pos, range);
    }

    /**
     * 播放冰雾爆发效果 - 使用FogParticle
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    private static void playIceFogBurst(ServerLevel level, Vec3 pos, double range) {
        int fogCount = (int) (range * 3);
        
        for (int i = 0; i < fogCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range * 0.8;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            float scale = 0.8f + level.random.nextFloat() * 0.6f;
            
            level.sendParticles(
                    new FogParticleOptions(ICE_FOG_COLOR, scale),
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
     * 播放雪花爆发效果
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    private static void playSnowflakeBurst(ServerLevel level, Vec3 pos, double range) {
        int snowCount = (int) (range * 8);
        
        for (int i = 0; i < snowCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            // 使用铁魔法的雪花粒子
            level.sendParticles(
                    ParticleRegistry.SNOWFLAKE_PARTICLE.get(),
                    x,
                    pos.y + 0.5 + level.random.nextDouble() * 3,
                    z,
                    1,
                    0.2,
                    0.3,
                    0.2,
                    0.02
            );
        }
    }

    /**
     * 播放冰晶飞溅效果 - 使用SparkParticle
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    private static void playIceCrystalBurst(ServerLevel level, Vec3 pos, double range) {
        int crystalCount = (int) (range * 5);
        
        for (int i = 0; i < crystalCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range * 0.9;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            // 使用SparkParticle创建冰晶效果
            level.sendParticles(
                    new SparkParticleOptions(ICE_CRYSTAL_COLOR),
                    x,
                    pos.y + 0.3 + level.random.nextDouble() * 2,
                    z,
                    1,
                    (level.random.nextDouble() - 0.5) * 0.2,
                    level.random.nextDouble() * 0.3,
                    (level.random.nextDouble() - 0.5) * 0.2,
                    0.05
            );
        }
    }

    /**
     * 播放寒冷气息扩散效果
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    private static void playColdMistBurst(ServerLevel level, Vec3 pos, double range) {
        int mistCount = (int) (range * 4);
        
        for (int i = 0; i < mistCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range * 0.7;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            float scale = 0.5f + level.random.nextFloat() * 0.4f;
            
            level.sendParticles(
                    new FogParticleOptions(COLD_MIST_COLOR, scale),
                    x,
                    pos.y + 0.2 + level.random.nextDouble() * 1.5,
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
     * 播放冰块粒子效果
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    private static void playIceParticles(ServerLevel level, Vec3 pos, double range) {
        int iceCount = (int) (range * 3);
        
        for (int i = 0; i < iceCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range * 0.8;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            // 使用原版冰块效果
            level.sendParticles(
                    ParticleTypes.ITEM_SNOWBALL,
                    x,
                    pos.y + 0.5 + level.random.nextDouble() * 2,
                    z,
                    1,
                    0.1,
                    0.2,
                    0.1,
                    0.03
            );
        }
    }

    /**
     * 播放环境持续效果
     * 在暴风雪持续期间每tick播放
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    public static void playAmbientEffect(ServerLevel level, Vec3 pos, double range) {
        // 1. 持续下雪效果
        playFallingSnow(level, pos, range);
        
        // 2. 冰雾环绕
        playAmbientIceFog(level, pos, range);
        
        // 3. 偶尔闪烁的冰晶
        if (level.random.nextInt(3) == 0) {
            playAmbientIceCrystals(level, pos, range);
        }
    }

    /**
     * 播放下雪效果
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    private static void playFallingSnow(ServerLevel level, Vec3 pos, double range) {
        int snowCount = (int) (range * 2);
        
        for (int i = 0; i < snowCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            // 在区域上空生成雪花
            level.sendParticles(
                    ParticleRegistry.SNOWFLAKE_PARTICLE.get(),
                    x,
                    pos.y + 3 + level.random.nextDouble() * 2,
                    z,
                    1,
                    0.1,
                    -0.3,  // 向下飘落
                    0.1,
                    0.01
            );
        }
    }

    /**
     * 播放环境冰雾效果
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    private static void playAmbientIceFog(ServerLevel level, Vec3 pos, double range) {
        int fogCount = (int) (range * 0.5);
        
        for (int i = 0; i < fogCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range * 0.9;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            float scale = 0.6f + level.random.nextFloat() * 0.4f;
            
            level.sendParticles(
                    new FogParticleOptions(ICE_FOG_COLOR, scale),
                    x,
                    pos.y + 0.1 + level.random.nextDouble() * 0.3,
                    z,
                    1,
                    0.05,
                    0.02,
                    0.05,
                    0.005
            );
        }
    }

    /**
     * 播放环境冰晶效果
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    private static void playAmbientIceCrystals(ServerLevel level, Vec3 pos, double range) {
        int crystalCount = (int) (range * 0.3);
        
        for (int i = 0; i < crystalCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            level.sendParticles(
                    new SparkParticleOptions(ICE_CRYSTAL_COLOR),
                    x,
                    pos.y + 0.5 + level.random.nextDouble() * 1.5,
                    z,
                    1,
                    0.05,
                    0.1,
                    0.05,
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
    public static void playHitEffect(ServerLevel level, Vec3 pos, double height, double width) {
        // 雪花环绕
        level.sendParticles(
                ParticleRegistry.SNOWFLAKE_PARTICLE.get(),
                pos.x,
                pos.y + height * 0.5,
                pos.z,
                8,
                width * 0.3,
                height * 0.3,
                width * 0.3,
                0.03
        );

        // 冰晶飞溅
        level.sendParticles(
                new SparkParticleOptions(ICE_CRYSTAL_COLOR),
                pos.x,
                pos.y + height * 0.5,
                pos.z,
                5,
                width * 0.2,
                height * 0.2,
                width * 0.2,
                0.05
        );
        
        // 寒冷气息
        level.sendParticles(
                new FogParticleOptions(COLD_MIST_COLOR, 0.4f),
                pos.x,
                pos.y + height * 0.5,
                pos.z,
                3,
                width * 0.15,
                height * 0.15,
                width * 0.15,
                0.02
        );
        
        // 冰块碎片
        level.sendParticles(
                ParticleTypes.ITEM_SNOWBALL,
                pos.x,
                pos.y + height * 0.5,
                pos.z,
                4,
                width * 0.2,
                height * 0.2,
                width * 0.2,
                0.04
        );
    }

    /**
     * 播放暴风雪结束效果
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    public static void playEndEffect(ServerLevel level, Vec3 pos, double range) {
        // 冰雾消散
        int fogCount = (int) (range * 2);
        
        for (int i = 0; i < fogCount; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * range;
            double x = pos.x + Math.cos(angle) * distance;
            double z = pos.z + Math.sin(angle) * distance;
            
            level.sendParticles(
                    new FogParticleOptions(ICE_FOG_COLOR, 0.5f),
                    x,
                    pos.y + 0.1 + level.random.nextDouble() * 0.5,
                    z,
                    1,
                    0.1,
                    0.05,
                    0.1,
                    0.02
            );
        }
        
        // 最后的雪花
        level.sendParticles(
                ParticleRegistry.SNOWFLAKE_PARTICLE.get(),
                pos.x,
                pos.y + 1,
                pos.z,
                (int) (range * 4),
                range * 0.5,
                1,
                range * 0.5,
                0.05
        );
    }
}
