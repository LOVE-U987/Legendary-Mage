package com.legendarymage.legendarymagemod.entity.spell;

import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.particle.ShockwaveParticleOptions;
import io.redspace.ironsspellbooks.particle.SparkParticleOptions;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;

import com.legendarymage.legendarymagemod.entity.ModEntities;
import com.legendarymage.legendarymagemod.spell.GiantSnowballSpell;

import org.jetbrains.annotations.Nullable;

/**
 * 巨雪球实体
 * 在施法者头顶生成并随吟唱时间变大，发射后击中目标造成冰爆并留下暴风雪力场
 * 
 * @author Love_U
 * @version 0.0.1
 */
public class GiantSnowballEntity extends AbstractMagicProjectile {

    /**
     * 同步数据：雪球大小缩放值
     */
    private static final EntityDataAccessor<Float> DATA_SCALE = SynchedEntityData.defineId(
            GiantSnowballEntity.class, EntityDataSerializers.FLOAT);

    /**
     * 同步数据：是否已发射
     */
    private static final EntityDataAccessor<Boolean> DATA_LAUNCHED = SynchedEntityData.defineId(
            GiantSnowballEntity.class, EntityDataSerializers.BOOLEAN);

    /**
     * 伤害值
     */
    private float damage = 30.0f;

    /**
     * 爆炸范围
     */
    private int explosionRadius = 8;

    /**
     * 暴风雪持续时间（tick）
     */
    private int blizzardDuration = 200;

    /**
     * 施法者引用
     */
    @Nullable
    private LivingEntity caster;

    /**
     * 构造函数
     * 
     * @param entityType 实体类型
     * @param level      世界
     */
    public GiantSnowballEntity(EntityType<? extends GiantSnowballEntity> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    /**
     * 构造函数
     * 
     * @param level    世界
     * @param caster   施法者
     * @param position 初始位置
     * @param scale    初始大小
     */
    public GiantSnowballEntity(Level level, LivingEntity caster, Vec3 position, float scale) {
        this(ModEntities.GIANT_SNOWBALL.get(), level);
        this.caster = caster;
        this.setOwner(caster);
        this.setPos(position);
        this.setScale(scale);
        this.setLaunched(false);
    }

    /**
     * 定义同步数据
     */
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SCALE, 0.1f);
        builder.define(DATA_LAUNCHED, false);
    }

    /**
     * 读取附加保存数据
     */
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setScale(compound.getFloat("Scale"));
        this.setLaunched(compound.getBoolean("Launched"));
        this.damage = compound.getFloat("Damage");
        this.explosionRadius = compound.getInt("ExplosionRadius");
        this.blizzardDuration = compound.getInt("BlizzardDuration");
    }

    /**
     * 添加附加保存数据
     */
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("Scale", this.getScale());
        compound.putBoolean("Launched", this.isLaunched());
        compound.putFloat("Damage", this.damage);
        compound.putInt("ExplosionRadius", this.explosionRadius);
        compound.putInt("BlizzardDuration", this.blizzardDuration);
    }

    /**
     * 每 tick 更新
     * 方案A：最小化重写，依赖父类AbstractMagicProjectile的运动逻辑
     */
    @Override
    public void tick() {
        Vec3 posBefore = this.position();
        Vec3 motionBefore = this.getDeltaMovement();
        
        super.tick();
        
        Vec3 posAfter = this.position();
        Vec3 motionAfter = this.getDeltaMovement();
        
        // 如果发射了但位置没有变化，输出警告
        if (this.isLaunched() && posBefore.equals(posAfter) && this.tickCount % 5 == 0) {
            com.legendarymage.legendarymagemod.LegendaryMage.LOGGER.warn("[巨雪球实体] 发射后位置未变化! tick={}, pos={}, motion={}, isNoGravity={}", 
                this.tickCount, posAfter, motionAfter, this.isNoGravity());
        }
        
        // 如果发射后存在时间过长，自动销毁
        if (this.isLaunched() && this.tickCount > 200) {
            this.discard();
        }
    }

    /**
     * 击中方块时的处理
     * 
     * @param blockHitResult 方块击中结果
     */
    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        if (this.level() instanceof ServerLevel serverLevel) {
            triggerExplosion(serverLevel, blockHitResult.getLocation());
        }
        this.discard();
    }

    /**
     * 击中实体时的处理
     * 
     * @param entityHitResult 实体击中结果
     */
    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        
        if (this.level() instanceof ServerLevel serverLevel) {
            // 对击中的实体造成伤害
            if (entityHitResult.getEntity() instanceof LivingEntity target) {
                LivingEntity actualCaster = this.caster != null ? this.caster : 
                        (this.getOwner() instanceof LivingEntity living ? living : null);
                target.hurt(
                    this.damageSources().magic(),
                    this.damage
                );
            }
            
            // 触发冰爆
            triggerExplosion(serverLevel, entityHitResult.getEntity().position());
        }
        
        this.discard();
    }

    /**
     * 触发冰爆和暴风雪力场
     * 
     * @param level  服务器世界
     * @param center 爆炸中心
     */
    private void triggerExplosion(ServerLevel level, Vec3 center) {
        LivingEntity actualCaster = this.caster != null ? this.caster : 
                (this.getOwner() instanceof LivingEntity living ? living : null);
        
        // 播放巨雪球特有的爆炸音效
        playExplosionSounds(level, center);
        
        // 播放巨雪球特有的爆炸粒子
        playExplosionParticles(level, center);
        
        // 触发冰爆
        GiantSnowballSpell.triggerExplosion(level, center, this.damage, this.explosionRadius, actualCaster);
        
        // 创建暴风雪力场
        GiantSnowballSpell.createBlizzardField(level, center, this.explosionRadius, actualCaster);
    }

    /**
     * 播放巨雪球爆炸音效 - 巨大而震撼的冰爆
     * 
     * @param level  服务器世界
     * @param center 爆炸中心位置
     */
    private void playExplosionSounds(ServerLevel level, Vec3 center) {
        float scale = this.getScale();
        
        // 1. 主爆炸音效 - 原版爆炸（根据大小调整音量）
        level.playSound(
            null,
            center.x, center.y, center.z,
            SoundEvents.GENERIC_EXPLODE,
            net.minecraft.sounds.SoundSource.PLAYERS,
            1.0f + scale * 0.5f,
            0.5f + level.random.nextFloat() * 0.2f
        );

        // 2. 玻璃破碎 - 清脆冰裂
        level.playSound(
            null,
            center.x, center.y, center.z,
            SoundEvents.GLASS_BREAK,
            net.minecraft.sounds.SoundSource.PLAYERS,
            1.2f,
            0.6f + level.random.nextFloat() * 0.3f
        );

        // 3. 紫水晶破碎 - 高频魔法感
        level.playSound(
            null,
            center.x, center.y, center.z,
            SoundEvents.AMETHYST_BLOCK_BREAK,
            net.minecraft.sounds.SoundSource.PLAYERS,
            0.9f,
            0.8f + level.random.nextFloat() * 0.3f
        );

        // 4. 铁魔法冰冲击
        level.playSound(
            null,
            center.x, center.y, center.z,
            io.redspace.ironsspellbooks.registries.SoundRegistry.ICE_IMPACT.get(),
            net.minecraft.sounds.SoundSource.PLAYERS,
            1.0f,
            0.7f + level.random.nextFloat() * 0.4f
        );
    }

    /**
     * 播放巨雪球爆炸粒子效果 - 壮观的冰霜大爆炸
     * 
     * @param level  服务器世界
     * @param center 爆炸中心位置
     */
    private void playExplosionParticles(ServerLevel level, Vec3 center) {
        float scale = this.getScale();
        float radius = this.explosionRadius * (0.8f + scale * 0.2f);
        
        // 1. 大型爆炸云
        level.sendParticles(
            ParticleTypes.EXPLOSION,
            center.x, center.y + 0.5, center.z,
            1, 0, 0, 0, 0
        );

        // 2. 爆炸烟雾发射器
        level.sendParticles(
            ParticleTypes.EXPLOSION_EMITTER,
            center.x, center.y + 0.5, center.z,
            1, 0, 0, 0, 0
        );

        // 3. 巨大的冰霜冲击波
        level.sendParticles(
            new BlastwaveParticleOptions(new Vector3f(0.4f, 0.7f, 1.0f), radius * 0.8f),
            center.x, center.y + 0.5, center.z,
            1, 0, 0, 0, 0
        );

        // 4. 向外扩散的震荡波
        level.sendParticles(
            new ShockwaveParticleOptions(new Vector3f(0.6f, 0.8f, 1.0f), radius * 0.5f, false),
            center.x, center.y + 0.2, center.z,
            1, 0, 0, 0, 0
        );

        // 5. 大量雪花粒子 - 冰霜爆发
        level.sendParticles(
            ParticleRegistry.SNOWFLAKE_PARTICLE.get(),
            center.x, center.y + 0.5, center.z,
            (int) (radius * 15 * scale), 
            radius * 0.4, 0.5, radius * 0.4, 
            0.15
        );

        // 6. 原版雪花
        level.sendParticles(
            ParticleTypes.SNOWFLAKE,
            center.x, center.y + 0.5, center.z,
            (int) (radius * 12 * scale),
            radius * 0.35, 0.4, radius * 0.35,
            0.1
        );

        // 7. 冰晶火花 - 向四周飞溅
        level.sendParticles(
            new SparkParticleOptions(new Vector3f(0.6f, 0.8f, 1.0f)),
            center.x, center.y + 0.5, center.z,
            (int) (30 * scale),
            radius * 0.3, 0.3, radius * 0.3,
            0.5
        );

        // 8. 亮蓝色火花 - 核心能量
        level.sendParticles(
            new SparkParticleOptions(new Vector3f(0.3f, 0.9f, 1.0f)),
            center.x, center.y + 0.5, center.z,
            (int) (20 * scale),
            radius * 0.2, 0.2, radius * 0.2,
            0.4
        );

        // 9. 白色烟雾 - 冰霜雾气
        level.sendParticles(
            ParticleTypes.POOF,
            center.x, center.y + 0.3, center.z,
            (int) (15 * scale),
            radius * 0.25, 0.25, radius * 0.25,
            0.1
        );

        // 10. 环形冰霜扩散（多层）
        for (int ring = 1; ring <= 3; ring++) {
            double ringRadius = radius * ring / 3.0;
            for (int i = 0; i < 12; i++) {
                double angle = (i / 12.0) * Math.PI * 2;
                double x = center.x + Math.cos(angle) * ringRadius;
                double z = center.z + Math.sin(angle) * ringRadius;

                level.sendParticles(
                    ParticleTypes.SNOWFLAKE,
                    x, center.y + 0.1, z,
                    2, 0.05, 0.05, 0.05, 0.02
                );
            }
        }

        // 11. 向上的冰霜喷射（大雪球特有）
        if (scale > 0.5) {
            for (int i = 0; i < 8; i++) {
                double angle = (i / 8.0) * Math.PI * 2;
                double x = center.x + Math.cos(angle) * radius * 0.3;
                double z = center.z + Math.sin(angle) * radius * 0.3;
                
                level.sendParticles(
                    ParticleRegistry.SNOWFLAKE_PARTICLE.get(),
                    x, center.y + 0.5, z,
                    3, 0.1, 0.3, 0.1, 0.1
                );
            }
        }
    }

    /**
     * 轨迹粒子效果（客户端每tick调用）- 巨雪球的壮观冰霜轨迹
     */
    @Override
    public void trailParticles() {
        Level level = this.level();
        float scale = this.getScale();
        
        // 1. 雪花轨迹 - 根据雪球大小调整
        int particleCount = (int) (scale * 6) + 2;
        for (int i = 0; i < particleCount; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * scale * 0.6;
            double offsetY = (level.random.nextDouble() - 0.5) * scale * 0.6;
            double offsetZ = (level.random.nextDouble() - 0.5) * scale * 0.6;
            
            // 混合使用铁魔法雪花和原版雪花
            level.addParticle(
                level.random.nextDouble() < 0.5 
                    ? ParticleRegistry.SNOWFLAKE_PARTICLE.get()
                    : ParticleTypes.SNOWFLAKE,
                this.getX() + offsetX, 
                this.getY() + offsetY, 
                this.getZ() + offsetZ,
                0, -0.02 - (scale * 0.02), 0
            );
        }

        // 2. 冰霜火花 - 更频繁，根据大小调整
        if (level.random.nextDouble() < 0.4 * scale) {
            level.addParticle(
                new SparkParticleOptions(new Vector3f(0.6f, 0.8f, 1.0f)),
                this.getX() + (level.random.nextDouble() - 0.5) * scale * 0.3,
                this.getY() + (level.random.nextDouble() - 0.5) * scale * 0.3,
                this.getZ() + (level.random.nextDouble() - 0.5) * scale * 0.3,
                0, 0, 0
            );
        }

        // 3. 亮蓝色火花 - 核心能量
        if (level.random.nextDouble() < 0.25 * scale) {
            level.addParticle(
                new SparkParticleOptions(new Vector3f(0.4f, 0.9f, 1.0f)),
                this.getX(), this.getY(), this.getZ(),
                0, 0, 0
            );
        }

        // 4. 冰霜雾气 - 大雪球特有
        if (scale > 0.5 && level.random.nextDouble() < 0.2 * scale) {
            level.addParticle(
                ParticleTypes.POOF,
                this.getX() + (level.random.nextDouble() - 0.5) * scale * 0.4,
                this.getY() + (level.random.nextDouble() - 0.5) * scale * 0.4,
                this.getZ() + (level.random.nextDouble() - 0.5) * scale * 0.4,
                0, 0.01, 0
            );
        }

        // 5. 大型尾迹 - 根据运动方向产生拖尾
        if (scale > 0.3 && level.random.nextDouble() < 0.15 * scale) {
            Vec3 motion = this.getDeltaMovement();
            level.addParticle(
                ParticleRegistry.SNOWFLAKE_PARTICLE.get(),
                this.getX() - motion.x * 0.3,
                this.getY() - motion.y * 0.3,
                this.getZ() - motion.z * 0.3,
                0, -0.01, 0
            );
        }

        // 6. 紫水晶光芒 - 魔法感（大雪球）
        if (scale > 0.6 && level.random.nextDouble() < 0.1) {
            level.addParticle(
                ParticleTypes.WITCH,
                this.getX() + (level.random.nextDouble() - 0.5) * scale * 0.2,
                this.getY() + (level.random.nextDouble() - 0.5) * scale * 0.2,
                this.getZ() + (level.random.nextDouble() - 0.5) * scale * 0.2,
                0, 0, 0
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
        return 1.5f;
    }

    /**
     * 获取击中音效
     * 
     * @return 击中音效
     */
    @Override
    public java.util.Optional<net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent>> getImpactSound() {
        return java.util.Optional.of(
            net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(
                net.minecraft.sounds.SoundEvents.GLASS_BREAK
            )
        );
    }

    /**
     * 设置雪球大小
     * 
     * @param scale 缩放值（0.1 - 1.0）
     */
    public void setScale(float scale) {
        this.entityData.set(DATA_SCALE, Math.max(0.1f, Math.min(1.0f, scale)));
        // 更新碰撞箱大小
        this.refreshDimensions();
    }

    /**
     * 获取雪球大小
     * 
     * @return 缩放值
     */
    public float getScale() {
        return this.entityData.get(DATA_SCALE);
    }

    /**
     * 设置是否已发射
     * 
     * @param launched 是否已发射
     */
    public void setLaunched(boolean launched) {
        this.entityData.set(DATA_LAUNCHED, launched);
        if (launched) {
            this.setNoGravity(false);
        }
    }

    /**
     * 是否已发射
     * 
     * @return 是否已发射
     */
    public boolean isLaunched() {
        return this.entityData.get(DATA_LAUNCHED);
    }

    /**
     * 设置伤害
     * 
     * @param damage 伤害值
     */
    public void setDamage(float damage) {
        this.damage = damage;
    }

    /**
     * 获取伤害
     * 
     * @return 伤害值
     */
    public float getDamage() {
        return this.damage;
    }

    /**
     * 设置爆炸范围
     * 
     * @param radius 爆炸范围
     */
    public void setExplosionRadius(int radius) {
        this.explosionRadius = radius;
    }

    /**
     * 获取爆炸范围（覆盖父类方法）
     * 
     * @return 爆炸范围
     */
    @Override
    public float getExplosionRadius() {
        return (float) this.explosionRadius;
    }
    
    /**
     * 获取爆炸范围（整数版本）
     * 
     * @return 爆炸范围
     */
    public int getExplosionRadiusInt() {
        return this.explosionRadius;
    }

    /**
     * 设置暴风雪持续时间
     * 
     * @param duration 持续时间（tick）
     */
    public void setBlizzardDuration(int duration) {
        this.blizzardDuration = duration;
    }

    /**
     * 获取暴风雪持续时间
     * 
     * @return 持续时间（tick）
     */
    public int getBlizzardDuration() {
        return this.blizzardDuration;
    }

    /**
     * 设置施法者
     * 
     * @param caster 施法者
     */
    public void setCaster(LivingEntity caster) {
        this.caster = caster;
        this.setOwner(caster);
    }

    /**
     * 获取施法者
     * 
     * @return 施法者
     */
    @Nullable
    public LivingEntity getCaster() {
        return this.caster;
    }

    /**
     * 获取渲染宽度（用于客户端渲染缩放）
     * 
     * @return 渲染宽度
     */
    public float getRenderWidth() {
        return super.getBbWidth() * this.getScale();
    }

    /**
     * 获取渲染高度（用于客户端渲染缩放）
     * 
     * @return 渲染高度
     */
    public float getRenderHeight() {
        return super.getBbHeight() * this.getScale();
    }
}
