package com.legendarymage.legendarymagemod.entity.spell;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import com.legendarymage.legendarymagemod.entity.ModEntities;
import com.legendarymage.legendarymagemod.spell.GiantSnowballSpell;

import org.jetbrains.annotations.Nullable;

/**
 * 巨雪球实体 - 简化版本
 * 使用原生Projectile逻辑，避免AbstractMagicProjectile的复杂击中检测
 * 
 * @author Love_U
 * @version 1.0.7
 */
public class GiantSnowballEntity extends Projectile {

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
     * 施法者引用（客户端用）
     */
    @Nullable
    private LivingEntity cachedCaster;

    /**
     * 构造函数
     * 
     * @param entityType 实体类型
     * @param level      世界
     */
    public GiantSnowballEntity(EntityType<? extends GiantSnowballEntity> entityType, Level level) {
        super(entityType, level);
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
        this.setOwner(caster);
        this.cachedCaster = caster;
        this.setPos(position);
        this.setScale(scale);
        this.setLaunched(false);
        this.setNoGravity(true); // 未发射时无重力
    }

    /**
     * 定义同步数据
     */
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
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
     */
    @Override
    public void tick() {
        // 调用父类tick()处理基本逻辑
        super.tick();

        // 如果未发射，保持在原地（不运动）
        if (!this.isLaunched()) {
            this.setDeltaMovement(Vec3.ZERO);
        }

        // 如果发射了，处理运动和击中检测
        if (this.isLaunched()) {
            // 输出调试日志（前 10 tick）
            if (this.tickCount <= 10) {
                com.legendarymage.legendarymagemod.LegendaryMage.LOGGER.info(
                    "[巨雪球] tick={}, pos={}, motion={}, isNoGravity={}",
                    this.tickCount, this.position(), this.getDeltaMovement(), this.isNoGravity()
                );
            }

            // 获取当前位置（移动前）
            Vec3 oldPos = this.position();

            // 先应用重力
            if (!this.isNoGravity()) {
                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(motion.x, motion.y - 0.03, motion.z); // 标准重力
            }

            // 获取当前运动向量
            Vec3 motion = this.getDeltaMovement();

            // 计算新位置
            Vec3 newPos = oldPos.add(motion);

            // 先进行击中检测（检测从旧位置到新位置的射线）
            // 使用clip方法检测路径上的碰撞
            HitResult hitResult = this.level().clip(new net.minecraft.world.level.ClipContext(
                oldPos,
                newPos,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                this
            ));

            // 检测实体碰撞
            HitResult entityHitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);

            // 优先处理实体碰撞
            if (entityHitResult.getType() != HitResult.Type.MISS) {
                com.legendarymage.legendarymagemod.LegendaryMage.LOGGER.info(
                    "[巨雪球] 击中实体! 位置: {}",
                    entityHitResult.getLocation()
                );
                this.onHit(entityHitResult);
                return; // 击中后不再继续移动
            }

            // 然后处理方块碰撞
            if (hitResult.getType() != HitResult.Type.MISS) {
                com.legendarymage.legendarymagemod.LegendaryMage.LOGGER.info(
                    "[巨雪球] 击中方块! 位置: {}",
                    hitResult.getLocation()
                );
                this.onHit(hitResult);
                return; // 击中后不再继续移动
            }

            // 没有击中，更新位置
            this.setPos(newPos);

            // 空气阻力
            this.setDeltaMovement(motion.scale(0.99));
        }

        // 超时销毁
        if (this.isLaunched() && this.tickCount > 200) {
            com.legendarymage.legendarymagemod.ModLogger.spell("[巨雪球] 超时销毁");
            this.discard();
        }
    }

    /**
     * 击中处理
     */
    protected void onHit(HitResult hitResult) {
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            this.onHitBlock((BlockHitResult) hitResult);
        } else if (hitResult.getType() == HitResult.Type.ENTITY) {
            this.onHitEntity((EntityHitResult) hitResult);
        }
    }

    /**
     * 检查是否可以击中实体
     * 排除施法者和盟友
     */
    @Override
    protected boolean canHitEntity(net.minecraft.world.entity.Entity pTarget) {
        // 排除施法者
        var owner = this.getOwner();
        if (owner != null && pTarget == owner) {
            return false;
        }
        
        // 排除盟友
        if (owner != null && owner.isAlliedTo(pTarget)) {
            return false;
        }
        
        // 排除非生物实体
        if (!(pTarget instanceof LivingEntity)) {
            return false;
        }
        
        return super.canHitEntity(pTarget);
    }

    /**
     * 击中方块时的处理
     */
    protected void onHitBlock(BlockHitResult blockHitResult) {
        if (this.level() instanceof ServerLevel serverLevel) {
            triggerExplosion(serverLevel, blockHitResult.getLocation());
        }
        this.discard();
    }

    /**
     * 击中实体时的处理
     */
    protected void onHitEntity(EntityHitResult entityHitResult) {
        if (this.level() instanceof ServerLevel serverLevel) {
            if (entityHitResult.getEntity() instanceof LivingEntity target) {
                target.hurt(
                    this.damageSources().magic(),
                    this.damage
                );
            }
            triggerExplosion(serverLevel, entityHitResult.getEntity().position());
        }
        this.discard();
    }

    /**
     * 触发冰爆
     */
    private void triggerExplosion(ServerLevel level, Vec3 center) {
        LivingEntity actualCaster = this.getOwner() instanceof LivingEntity living ? living : null;
        GiantSnowballSpell.triggerExplosion(level, center, this.damage, this.explosionRadius, actualCaster);
        GiantSnowballSpell.createBlizzardField(level, center, this.explosionRadius, actualCaster);
    }

    /**
     * 设置雪球大小
     */
    public void setScale(float scale) {
        this.entityData.set(DATA_SCALE, Math.max(0.1f, Math.min(1.0f, scale)));
    }

    /**
     * 获取雪球大小
     */
    public float getScale() {
        return this.entityData.get(DATA_SCALE);
    }

    /**
     * 设置是否已发射
     */
    public void setLaunched(boolean launched) {
        this.entityData.set(DATA_LAUNCHED, launched);
        if (launched) {
            this.setNoGravity(false); // 发射后受重力
        }
    }

    /**
     * 是否已发射
     */
    public boolean isLaunched() {
        return this.entityData.get(DATA_LAUNCHED);
    }

    /**
     * 设置伤害
     */
    public void setDamage(float damage) {
        this.damage = damage;
    }

    /**
     * 获取伤害
     */
    public float getDamage() {
        return this.damage;
    }

    /**
     * 设置爆炸范围
     */
    public void setExplosionRadius(int radius) {
        this.explosionRadius = radius;
    }

    /**
     * 获取爆炸范围
     */
    public int getExplosionRadius() {
        return this.explosionRadius;
    }

    /**
     * 设置暴风雪持续时间
     */
    public void setBlizzardDuration(int duration) {
        this.blizzardDuration = duration;
    }

    /**
     * 获取暴风雪持续时间
     */
    public int getBlizzardDuration() {
        return this.blizzardDuration;
    }

    /**
     * 设置施法者
     */
    public void setCaster(LivingEntity caster) {
        this.cachedCaster = caster;
        this.setOwner(caster);
    }

    /**
     * 获取施法者
     */
    @Nullable
    public LivingEntity getCaster() {
        return this.cachedCaster;
    }
}
