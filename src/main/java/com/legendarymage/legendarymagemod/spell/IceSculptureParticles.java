package com.legendarymage.legendarymagemod.spell;

import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.particle.FogParticleOptions;
import io.redspace.ironsspellbooks.particle.ShockwaveParticleOptions;
import io.redspace.ironsspellbooks.particle.SparkParticleOptions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import org.joml.Vector3f;

/**
 * 冰雕粒子效果类
 * 管理冰雕相关的所有粒子效果
 * 使用铁魔法(Iron's Spells n Spellbooks)的粒子系统
 * 
 * @author Love_U
 * @version 0.0.2
 */
public class IceSculptureParticles {

    /**
     * 冰蓝色 - 用于冰系法术
     */
    private static final Vector3f ICE_COLOR = new Vector3f(0.6f, 0.9f, 1.0f);
    
    /**
     * 深蓝色 - 用于冰霜效果
     */
    private static final Vector3f FROST_COLOR = new Vector3f(0.3f, 0.6f, 0.9f);
    
    /**
     * 白色 - 用于雪花效果
     */
    private static final Vector3f SNOW_COLOR = new Vector3f(1.0f, 1.0f, 1.0f);

    /**
     * 播放施法效果
     * 在法术释放时播放，使用铁魔法的冲击波和雾气粒子
     * 
     * @param level 服务器世界
     * @param pos   中心位置
     * @param range 范围
     */
    public static void playCastEffect(ServerLevel level, Vec3 pos, double range) {
        // 使用铁魔法的冲击波粒子 - 冰蓝色（需要3个参数：颜色、半径、是否反向）
        level.sendParticles(
                new ShockwaveParticleOptions(ICE_COLOR, (float) range, false),
                pos.x, pos.y + 0.5, pos.z,
                1, 0, 0, 0, 0
        );

        // 使用铁魔法的爆炸波粒子 - 冰霜扩散效果
        level.sendParticles(
                new BlastwaveParticleOptions(FROST_COLOR, (float) range * 0.8f),
                pos.x, pos.y + 0.2, pos.z,
                3, range * 0.3, 0.1, range * 0.3, 0.02
        );

        // 使用铁魔法的雾气粒子 - 冰霜雾气
        level.sendParticles(
                new FogParticleOptions(FROST_COLOR, 2.0f),
                pos.x, pos.y + 1, pos.z,
                30, range * 0.5, 0.5, range * 0.5, 0.05
        );

        // 播放雪花粒子
        level.sendParticles(
                ParticleTypes.SNOWFLAKE,
                pos.x, pos.y + 2, pos.z,
                50, range * 0.5, 1.0, range * 0.5, 0.1
        );

        // 播放大型烟雾效果
        level.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                pos.x, pos.y + 1, pos.z,
                20, range * 0.5, 1.0, range * 0.5, 0.02
        );

        // 使用铁魔法的火花粒子 - 冰晶碎片（只需要颜色参数）
        level.sendParticles(
                new SparkParticleOptions(ICE_COLOR),
                pos.x, pos.y + 1, pos.z,
                20, range * 0.3, 0.5, range * 0.3, 0.1
        );
    }

    /**
     * 播放地面持续粒子效果
     * 在法术生效期间持续播放
     * 
     * @param level  服务器世界
     * @param center 中心位置
     * @param range  范围
     */
    public static void playGroundEffect(ServerLevel level, Vec3 center, double range) {
        // 在范围内随机位置生成冰霜地面效果
        int particleCount = (int) (range * 2);
        
        for (int i = 0; i < particleCount; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double distance = Math.random() * range;
            double x = center.x + Math.cos(angle) * distance;
            double z = center.z + Math.sin(angle) * distance;
            double y = center.y + 0.1;
            
            // 使用铁魔法的雾气粒子 - 地面冰霜
            level.sendParticles(
                    new FogParticleOptions(FROST_COLOR, 1.0f),
                    x, y, z,
                    1, 0.2, 0.05, 0.2, 0.01
            );
        }
        
        // 中心位置持续产生冰霜效果
        level.sendParticles(
                ParticleTypes.SNOWFLAKE,
                center.x, center.y + 0.5, center.z,
                5, range * 0.3, 0.2, range * 0.3, 0.02
        );
    }

    /**
     * 播放冰雕生成效果
     * 
     * @param level     服务器世界
     * @param positions 冰雕位置列表
     */
    public static void playSculptureSpawnEffect(ServerLevel level, List<BlockPos> positions) {
        for (BlockPos pos : positions) {
            Vec3 vecPos = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

            // 使用铁魔法的冲击波粒子（需要3个参数）
            level.sendParticles(
                    new ShockwaveParticleOptions(ICE_COLOR, 1.5f, false),
                    vecPos.x, vecPos.y, vecPos.z,
                    1, 0, 0, 0, 0
            );

            // 播放冰晶效果
            level.sendParticles(
                    ParticleTypes.END_ROD,
                    vecPos.x, vecPos.y, vecPos.z,
                    10, 0.3, 0.5, 0.3, 0.05
            );

            // 播放雪花效果
            level.sendParticles(
                    ParticleTypes.SNOWFLAKE,
                    vecPos.x, vecPos.y + 0.5, vecPos.z,
                    15, 0.2, 0.3, 0.2, 0.02
            );

            // 使用铁魔法的火花粒子（只需要颜色参数）
            level.sendParticles(
                    new SparkParticleOptions(SNOW_COLOR),
                    vecPos.x, vecPos.y, vecPos.z,
                    8, 0.3, 0.3, 0.3, 0.05
            );
        }
    }

    /**
     * 播放冰雕环境粒子效果
     * 冰雕存在期间持续播放的环境效果
     * 
     * @param level 服务器世界
     * @param pos   冰雕位置
     */
    public static void playSculptureAmbientEffect(ServerLevel level, BlockPos pos) {
        Vec3 vecPos = new Vec3(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
        
        // 使用铁魔法的雾气粒子 - 冰雕周围的寒气
        level.sendParticles(
                new FogParticleOptions(FROST_COLOR, 0.8f),
                vecPos.x, vecPos.y, vecPos.z,
                2, 0.3, 0.3, 0.3, 0.02
        );
        
        // 偶尔产生雪花
        if (Math.random() < 0.3) {
            level.sendParticles(
                    ParticleTypes.SNOWFLAKE,
                    vecPos.x, vecPos.y + 0.5, vecPos.z,
                    1, 0.2, 0.2, 0.2, 0.01
            );
        }
    }

    /**
     * 播放冰雕破碎效果
     * 
     * @param level 服务器世界
     * @param pos   位置
     */
    public static void playShatterEffect(ServerLevel level, BlockPos pos) {
        Vec3 vecPos = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        // 使用铁魔法的冲击波粒子 - 破碎冲击（需要3个参数）
        level.sendParticles(
                new ShockwaveParticleOptions(ICE_COLOR, 2.0f, false),
                vecPos.x, vecPos.y, vecPos.z,
                1, 0, 0, 0, 0
        );

        // 使用铁魔法的爆炸波粒子
        level.sendParticles(
                new BlastwaveParticleOptions(FROST_COLOR, 1.5f),
                vecPos.x, vecPos.y, vecPos.z,
                2, 0.5, 0.5, 0.5, 0.1
        );

        // 播放冰块破碎粒子
        level.sendParticles(
                ParticleTypes.ITEM_SNOWBALL,
                vecPos.x, vecPos.y, vecPos.z,
                30, 0.3, 0.5, 0.3, 0.2
        );

        // 播放雪花飞溅
        level.sendParticles(
                ParticleTypes.SNOWFLAKE,
                vecPos.x, vecPos.y, vecPos.z,
                50, 0.5, 0.5, 0.5, 0.3
        );

        // 使用铁魔法的火花粒子 - 冰晶碎片飞溅（只需要颜色参数）
        level.sendParticles(
                new SparkParticleOptions(SNOW_COLOR),
                vecPos.x, vecPos.y, vecPos.z,
                25, 0.5, 0.5, 0.5, 0.3
        );

        // 播放白色粒子效果
        level.sendParticles(
                ParticleTypes.EFFECT,
                vecPos.x, vecPos.y, vecPos.z,
                20, 0.5, 0.5, 0.5, 0.1
        );
    }

    /**
     * 播放冰雕生物生成效果
     * 
     * @param level 服务器世界
     * @param pos   位置
     */
    public static void playEntitySpawnEffect(ServerLevel level, Vec3 pos) {
        // 使用铁魔法的冲击波粒子（需要3个参数）
        level.sendParticles(
                new ShockwaveParticleOptions(ICE_COLOR, 1.5f, false),
                pos.x, pos.y + 1, pos.z,
                1, 0, 0, 0, 0
        );

        // 播放雪花漩涡
        level.sendParticles(
                ParticleTypes.SNOWFLAKE,
                pos.x, pos.y + 1, pos.z,
                100, 0.5, 1.0, 0.5, 0.1
        );

        // 播放冰晶效果
        level.sendParticles(
                ParticleTypes.END_ROD,
                pos.x, pos.y + 1, pos.z,
                30, 0.3, 0.8, 0.3, 0.05
        );

        // 使用铁魔法的雾气粒子
        level.sendParticles(
                new FogParticleOptions(FROST_COLOR, 1.5f),
                pos.x, pos.y + 0.5, pos.z,
                20, 0.5, 0.5, 0.5, 0.05
        );

        // 使用铁魔法的火花粒子（只需要颜色参数）
        level.sendParticles(
                new SparkParticleOptions(ICE_COLOR),
                pos.x, pos.y + 1, pos.z,
                15, 0.5, 0.5, 0.5, 0.1
        );
    }

    /**
     * 播放冰雕生物破碎效果（自然消失）
     * 
     * @param level 服务器世界
     * @param pos   位置
     */
    public static void playEntityShatterEffect(ServerLevel level, Vec3 pos) {
        // 使用铁魔法的冲击波粒子（需要3个参数）
        level.sendParticles(
                new ShockwaveParticleOptions(FROST_COLOR, 1.5f, false),
                pos.x, pos.y + 1, pos.z,
                1, 0, 0, 0, 0
        );

        // 播放冰块破碎粒子
        level.sendParticles(
                ParticleTypes.ITEM_SNOWBALL,
                pos.x, pos.y + 1, pos.z,
                50, 0.4, 0.8, 0.4, 0.3
        );

        // 播放雪花飞溅
        level.sendParticles(
                ParticleTypes.SNOWFLAKE,
                pos.x, pos.y + 1, pos.z,
                80, 0.6, 0.8, 0.6, 0.2
        );

        // 使用铁魔法的火花粒子（只需要颜色参数）
        level.sendParticles(
                new SparkParticleOptions(SNOW_COLOR),
                pos.x, pos.y + 1, pos.z,
                40, 0.4, 0.6, 0.4, 0.3
        );

        // 播放白色粒子效果
        level.sendParticles(
                ParticleTypes.EFFECT,
                pos.x, pos.y + 0.5, pos.z,
                30, 1.0, 0.5, 1.0, 0.05
        );
    }

    /**
     * 播放冰雕生物死亡效果
     * 
     * @param level 服务器世界
     * @param pos   位置
     */
    public static void playEntityDeathEffect(ServerLevel level, Vec3 pos) {
        // 使用铁魔法的爆炸波粒子
        level.sendParticles(
                new BlastwaveParticleOptions(ICE_COLOR, 2.0f),
                pos.x, pos.y + 1, pos.z,
                3, 0.5, 0.5, 0.5, 0.1
        );

        // 播放冰块破碎粒子
        level.sendParticles(
                ParticleTypes.ITEM_SNOWBALL,
                pos.x, pos.y + 1, pos.z,
                60, 0.5, 1.0, 0.5, 0.4
        );

        // 播放雪花飞溅
        level.sendParticles(
                ParticleTypes.SNOWFLAKE,
                pos.x, pos.y + 1, pos.z,
                100, 0.8, 1.0, 0.8, 0.3
        );

        // 使用铁魔法的火花粒子（只需要颜色参数）
        level.sendParticles(
                new SparkParticleOptions(SNOW_COLOR),
                pos.x, pos.y + 1, pos.z,
                50, 0.5, 0.8, 0.5, 0.4
        );

        // 播放爆炸效果
        level.sendParticles(
                ParticleTypes.EXPLOSION,
                pos.x, pos.y + 1, pos.z,
                5, 0.3, 0.3, 0.3, 0.1
        );

        // 播放白色粒子效果
        level.sendParticles(
                ParticleTypes.EFFECT,
                pos.x, pos.y + 0.5, pos.z,
                40, 1.5, 0.5, 1.5, 0.05
        );
    }

    /**
     * 播放冻结期环境效果
     * 冰雕生物在冻结期间持续播放的粒子效果
     * 
     * @param level 服务器世界
     * @param pos   位置
     */
    public static void playFrozenAmbientEffect(ServerLevel level, Vec3 pos) {
        // 使用铁魔法的雾气粒子 - 冰霜寒气
        level.sendParticles(
                new FogParticleOptions(FROST_COLOR, 0.6f),
                pos.x, pos.y + 1, pos.z,
                3, 0.4, 0.4, 0.4, 0.02
        );
        
        // 偶尔产生冰晶效果
        if (Math.random() < 0.3) {
            level.sendParticles(
                    ParticleTypes.END_ROD,
                    pos.x, pos.y + 1.5, pos.z,
                    2, 0.3, 0.3, 0.3, 0.02
            );
        }
    }

    /**
     * 播放解冻效果
     * 冰雕生物从冻结状态解冻时的效果
     * 
     * @param level 服务器世界
     * @param pos   位置
     */
    public static void playEntityUnfreezeEffect(ServerLevel level, Vec3 pos) {
        // 使用铁魔法的冲击波粒子 - 解冻冲击
        level.sendParticles(
                new ShockwaveParticleOptions(ICE_COLOR, 2.0f, false),
                pos.x, pos.y + 0.5, pos.z,
                1, 0, 0, 0, 0
        );

        // 使用铁魔法的爆炸波粒子
        level.sendParticles(
                new BlastwaveParticleOptions(FROST_COLOR, 1.5f),
                pos.x, pos.y + 0.5, pos.z,
                2, 0.5, 0.5, 0.5, 0.1
        );

        // 播放冰块碎裂粒子
        level.sendParticles(
                ParticleTypes.ITEM_SNOWBALL,
                pos.x, pos.y + 1, pos.z,
                40, 0.5, 0.8, 0.5, 0.3
        );

        // 播放雪花飞溅
        level.sendParticles(
                ParticleTypes.SNOWFLAKE,
                pos.x, pos.y + 1.5, pos.z,
                60, 0.6, 0.8, 0.6, 0.2
        );

        // 使用铁魔法的火花粒子 - 冰晶碎片
        level.sendParticles(
                new SparkParticleOptions(SNOW_COLOR),
                pos.x, pos.y + 1, pos.z,
                30, 0.5, 0.5, 0.5, 0.2
        );

        // 播放白色粒子效果
        level.sendParticles(
                ParticleTypes.EFFECT,
                pos.x, pos.y + 0.5, pos.z,
                25, 1.0, 0.5, 1.0, 0.05
        );
    }

    /**
     * 播放解冻期环境效果
     * 冰雕生物在解冻期间持续播放的粒子效果
     * 
     * @param level 服务器世界
     * @param pos   位置
     */
    public static void playUnfrozenAmbientEffect(ServerLevel level, Vec3 pos) {
        // 使用铁魔法的雾气粒子 - 微弱的寒气
        level.sendParticles(
                new FogParticleOptions(FROST_COLOR, 0.4f),
                pos.x, pos.y + 1, pos.z,
                2, 0.3, 0.3, 0.3, 0.01
        );
        
        // 偶尔产生雪花
        if (Math.random() < 0.2) {
            level.sendParticles(
                    ParticleTypes.SNOWFLAKE,
                    pos.x, pos.y + 1.5, pos.z,
                    1, 0.2, 0.2, 0.2, 0.01
            );
        }
    }

    /**
     * 播放冰雕转化效果
     * 冰雕转化为活体冰雕时的效果
     * 
     * @param level 服务器世界
     * @param pos   位置
     */
    public static void playConversionEffect(ServerLevel level, Vec3 pos) {
        // 使用铁魔法的冲击波粒子 - 转化冲击
        level.sendParticles(
                new ShockwaveParticleOptions(ICE_COLOR, 2.5f, false),
                pos.x, pos.y + 0.5, pos.z,
                1, 0, 0, 0, 0
        );

        // 使用铁魔法的爆炸波粒子
        level.sendParticles(
                new BlastwaveParticleOptions(FROST_COLOR, 2.0f),
                pos.x, pos.y + 0.5, pos.z,
                3, 0.6, 0.6, 0.6, 0.15
        );

        // 播放冰块碎裂粒子
        level.sendParticles(
                ParticleTypes.ITEM_SNOWBALL,
                pos.x, pos.y + 1, pos.z,
                50, 0.6, 1.0, 0.6, 0.4
        );

        // 播放雪花飞溅
        level.sendParticles(
                ParticleTypes.SNOWFLAKE,
                pos.x, pos.y + 1.5, pos.z,
                80, 0.8, 1.0, 0.8, 0.3
        );

        // 使用铁魔法的火花粒子 - 冰晶碎片
        level.sendParticles(
                new SparkParticleOptions(SNOW_COLOR),
                pos.x, pos.y + 1, pos.z,
                40, 0.6, 0.6, 0.6, 0.3
        );

        // 播放白色粒子效果
        level.sendParticles(
                ParticleTypes.EFFECT,
                pos.x, pos.y + 0.5, pos.z,
                35, 1.2, 0.6, 1.2, 0.08
        );

        // 播放末影珍珠效果（转化特效）
        level.sendParticles(
                ParticleTypes.PORTAL,
                pos.x, pos.y + 1, pos.z,
                60, 0.5, 0.8, 0.5, 0.5
        );
    }
}
