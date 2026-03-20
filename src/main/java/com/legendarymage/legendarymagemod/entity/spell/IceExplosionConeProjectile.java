package com.legendarymage.legendarymagemod.entity.spell;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.particle.ShockwaveParticleOptions;
import io.redspace.ironsspellbooks.particle.SparkParticleOptions;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;

import com.legendarymage.legendarymagemod.entity.ModEntities;

import java.util.List;
import java.util.Optional;

/**
 * 冰爆锥投射物实体
 * 一种被迫注入了过多法力的冰锥，变得极为不稳定，在击中敌人时会触发冰爆
 * 
 * @author Love_U
 * @version 0.0.1
 */
public class IceExplosionConeProjectile extends AbstractMagicProjectile {

    /**
     * 冰爆范围（格）
     * 不受法术等级和法术强度加成
     */
    private static final float EXPLOSION_RADIUS = 3.0f;

    /**
     * 冰系颜色
     */
    private static final Vector3f ICE_COLOR = new Vector3f(0.6f, 0.8f, 1.0f);

    /**
     * 爆炸颜色
     */
    private static final Vector3f EXPLOSION_COLOR = new Vector3f(0.4f, 0.7f, 1.0f);

    /**
     * 构造函数
     * 
     * @param entityType 实体类型
     * @param level      世界
     */
    public IceExplosionConeProjectile(EntityType<? extends IceExplosionConeProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    /**
     * 构造函数
     * 
     * @param level   世界
     * @param shooter 发射者
     */
    public IceExplosionConeProjectile(Level level, LivingEntity shooter) {
        this(ModEntities.ICE_EXPLOSION_CONE.get(), level);
        setOwner(shooter);
    }

    /**
     * 击中方块时的处理
     * 
     * @param blockHitResult 方块击中结果
     */
    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        triggerExplosion(blockHitResult.getLocation());
        discard();
    }

    /**
     * 击中实体时的处理
     * 
     * @param entityHitResult 实体击中结果
     */
    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        
        // 对击中的实体造成伤害
        entityHitResult.getEntity().hurt(
            this.damageSources().source(DamageTypes.MAGIC, getOwner()),
            getDamage()
        );
        
        // 触发冰爆
        triggerExplosion(entityHitResult.getEntity().position());
        
        pierceOrDiscard();
    }

    /**
     * 触发冰爆效果
     * 
     * @param center 爆炸中心位置
     */
    private void triggerExplosion(Vec3 center) {
        Level level = this.level();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // 播放多层爆炸音效 - 原版爆炸音效 + 玻璃破碎 + 冰块碎裂
        playExplosionSounds(serverLevel, center);

        // 播放多层粒子效果
        playExplosionParticles(serverLevel, center);

        // 对范围内敌人造成伤害
        net.minecraft.world.phys.AABB explosionArea = new net.minecraft.world.phys.AABB(
            center.x - EXPLOSION_RADIUS, center.y - EXPLOSION_RADIUS, center.z - EXPLOSION_RADIUS,
            center.x + EXPLOSION_RADIUS, center.y + EXPLOSION_RADIUS, center.z + EXPLOSION_RADIUS
        );

        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(
            LivingEntity.class, 
            explosionArea,
            entity -> entity != getOwner() && entity.isAlive()
        );

        // 爆炸伤害为直接伤害的50%
        float explosionDamage = getDamage() * 0.5f;

        for (LivingEntity target : targets) {
            double distance = target.position().distanceTo(center);
            if (distance <= EXPLOSION_RADIUS) {
                // 根据距离衰减伤害
                float damageMultiplier = 1.0f - (float) (distance / EXPLOSION_RADIUS) * 0.5f;
                float actualDamage = explosionDamage * damageMultiplier;

                target.hurt(
                    this.damageSources().source(DamageTypes.MAGIC, getOwner()),
                    actualDamage
                );

                // 给目标添加缓慢效果
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                    60, // 3秒
                    1,
                    false,
                    true
                ));
            }
        }
    }

    /**
     * 播放爆炸音效 - 多层音效叠加营造震撼效果
     * 
     * @param level  服务器世界
     * @param center 爆炸中心位置
     */
    private void playExplosionSounds(ServerLevel level, Vec3 center) {
        // 主爆炸音效 - 原版爆炸音效（低沉震撼）
        level.playSound(
            null,
            center.x, center.y, center.z,
            SoundEvents.GENERIC_EXPLODE,
            net.minecraft.sounds.SoundSource.PLAYERS,
            1.2f,
            0.6f + level.random.nextFloat() * 0.2f
        );

        // 玻璃破碎音效（清脆）
        level.playSound(
            null,
            center.x, center.y, center.z,
            SoundEvents.GLASS_BREAK,
            net.minecraft.sounds.SoundSource.PLAYERS,
            1.0f,
            0.7f + level.random.nextFloat() * 0.3f
        );

        // 冰块碎裂音效
        level.playSound(
            null,
            center.x, center.y, center.z,
            SoundEvents.POINTED_DRIPSTONE_LAND,
            net.minecraft.sounds.SoundSource.PLAYERS,
            0.8f,
            0.5f + level.random.nextFloat() * 0.4f
        );

        // 冰霜爆裂音效
        level.playSound(
            null,
            center.x, center.y, center.z,
            SoundRegistry.ICE_IMPACT.get(),
            net.minecraft.sounds.SoundSource.PLAYERS,
            1.0f,
            0.8f + level.random.nextFloat() * 0.4f
        );
    }

    /**
     * 播放爆炸粒子效果 - 精简而精致的冰霜爆炸效果
     * 
     * @param level  服务器世界
     * @param center 爆炸中心位置
     */
    private void playExplosionParticles(ServerLevel level, Vec3 center) {
        // 1. 小型爆炸云 - 保留原版感但更小
        level.sendParticles(
            ParticleTypes.EXPLOSION,
            center.x, center.y + 0.3, center.z,
            1, 0, 0, 0, 0
        );

        // 2. 冰霜冲击波 - 缩小范围
        level.sendParticles(
            new BlastwaveParticleOptions(EXPLOSION_COLOR, EXPLOSION_RADIUS * 0.6f),
            center.x, center.y + 0.3, center.z,
            1, 0, 0, 0, 0
        );

        // 3. 雪花粒子 - 减少数量
        level.sendParticles(
            ParticleRegistry.SNOWFLAKE_PARTICLE.get(),
            center.x, center.y + 0.3, center.z,
            (int) (EXPLOSION_RADIUS * 8), 
            EXPLOSION_RADIUS * 0.25, 0.3, EXPLOSION_RADIUS * 0.25, 
            0.1
        );

        // 4. 原版雪花 - 增加冰感
        level.sendParticles(
            ParticleTypes.SNOWFLAKE,
            center.x, center.y + 0.3, center.z,
            (int) (EXPLOSION_RADIUS * 6),
            EXPLOSION_RADIUS * 0.2, 0.2, EXPLOSION_RADIUS * 0.2,
            0.05
        );

        // 5. 冰晶火花 - 减少数量
        level.sendParticles(
            new SparkParticleOptions(ICE_COLOR),
            center.x, center.y + 0.3, center.z,
            15,
            EXPLOSION_RADIUS * 0.2, 0.2, EXPLOSION_RADIUS * 0.2,
            0.3
        );

        // 6. 白色烟雾 - 冰霜雾气，减少数量
        level.sendParticles(
            ParticleTypes.POOF,
            center.x, center.y + 0.2, center.z,
            8,
            EXPLOSION_RADIUS * 0.15, 0.15, EXPLOSION_RADIUS * 0.15,
            0.05
        );

        // 7. 地面冰霜效果 - 简化为单层
        for (int i = 0; i < 8; i++) {
            double angle = (i / 8.0) * Math.PI * 2;
            double x = center.x + Math.cos(angle) * EXPLOSION_RADIUS * 0.6;
            double z = center.z + Math.sin(angle) * EXPLOSION_RADIUS * 0.6;

            level.sendParticles(
                ParticleTypes.SNOWFLAKE,
                x, center.y + 0.1, z,
                2, 0.03, 0.03, 0.03, 0.01
            );
        }
    }

    /**
     * 轨迹粒子效果（客户端每tick调用）
     */
    @Override
    public void trailParticles() {
        Level level = this.level();
        
        // 1. 冰晶轨迹 - 更多雪花粒子
        for (int i = 0; i < 3; i++) {
            double speed = 0.04;
            double dx = Utils.random.nextDouble() * 2 * speed - speed;
            double dy = Utils.random.nextDouble() * 2 * speed - speed;
            double dz = Utils.random.nextDouble() * 2 * speed - speed;
            
            level.addParticle(
                ParticleRegistry.SNOWFLAKE_PARTICLE.get(),
                this.getX() + dx, this.getY() + dy, this.getZ() + dz,
                dx * 0.5, dy * 0.5, dz * 0.5
            );
        }

        // 2. 冰霜火花 - 更频繁
        if (Utils.random.nextDouble() < 0.4) {
            level.addParticle(
                new SparkParticleOptions(ICE_COLOR),
                this.getX(), this.getY(), this.getZ(),
                0, 0, 0
            );
        }

        // 3. 蓝色火花 - 更亮的轨迹
        if (Utils.random.nextDouble() < 0.2) {
            level.addParticle(
                new SparkParticleOptions(new Vector3f(0.5f, 0.9f, 1.0f)),
                this.getX(), this.getY(), this.getZ(),
                0, 0, 0
            );
        }

        // 4. 原版白色烟雾轨迹 - 增加原版感
        if (Utils.random.nextDouble() < 0.15) {
            level.addParticle(
                ParticleTypes.POOF,
                this.getX(), this.getY(), this.getZ(),
                0, 0.02, 0
            );
        }

        // 5. 小雪球轨迹 - 模拟冰晶
        if (Utils.random.nextDouble() < 0.1) {
            level.addParticle(
                ParticleTypes.SNOWFLAKE,
                this.getX(), this.getY(), this.getZ(),
                0, -0.02, 0
            );
        }
    }

    /**
     * 击中粒子效果（服务器端调用）
     * 
     * @param x X坐标
     * @param y Y坐标
     * @param z Z坐标
     */
    @Override
    public void impactParticles(double x, double y, double z) {
        // 击中时的粒子效果在 triggerExplosion 中处理
    }

    /**
     * 获取投射物速度
     * 
     * @return 速度
     */
    @Override
    public float getSpeed() {
        return 1.8f;
    }

    /**
     * 获取击中音效
     * 
     * @return 击中音效
     */
    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return Optional.of(SoundRegistry.ICE_IMPACT);
    }
}
