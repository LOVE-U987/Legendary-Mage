package com.legendarymage.legendarymagemod.entity.spell;

import io.redspace.ironsspellbooks.api.util.Utils;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;

import com.legendarymage.legendarymagemod.entity.ModEntities;
import com.legendarymage.legendarymagemod.spell.FocusedIceConeSpell;
import com.legendarymage.legendarymagemod.trail.SimpleTrailEffect;
import com.legendarymage.legendarymagemod.trail.SimpleTrailManager;

import java.util.List;
import java.util.Optional;

/**
 * 聚能冰锥投射物实体
 * 舍去了冰爆锥的范围，法力更加集中且稳定。击中敌人必定冰冻，可穿透敌人，
 * 在穿透3个单位或撞向墙体后产生大冰爆，高速运动
 * 
 * @author Love_U
 * @version 0.0.1
 */
public class FocusedIceConeProjectile extends AbstractMagicProjectile {

    /**
     * 冰爆范围（格）
     * 不受法术等级和法术强度加成
     */
    private static final float EXPLOSION_RADIUS = 5.0f;

    /**
     * 最大穿透次数
     */
    private static final int MAX_PIERCE_COUNT = 3;

    /**
     * 冰系颜色
     */
    private static final Vector3f ICE_COLOR = new Vector3f(0.4f, 0.7f, 1.0f);

    /**
     * 爆炸颜色
     */
    private static final Vector3f EXPLOSION_COLOR = new Vector3f(0.2f, 0.6f, 1.0f);

    /**
     * 当前已穿透次数
     */
    private int pierceCount = 0;

    /**
     * 拖尾特效实例（每个投射物拥有独立的拖尾）
     * 仅在客户端创建和使用
     */
    private SimpleTrailEffect coneTrail = null;

    /**
     * 拖尾是否已初始化标志
     */
    private boolean trailInitialized = false;

    /**
     * 构造函数
     *
     * @param entityType 实体类型
     * @param level      世界
     */
    public FocusedIceConeProjectile(EntityType<? extends FocusedIceConeProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    /**
     * 构造函数
     * 
     * @param level   世界
     * @param shooter 发射者
     */
    public FocusedIceConeProjectile(Level level, LivingEntity shooter) {
        this(ModEntities.FOCUSED_ICE_CONE.get(), level);
        setOwner(shooter);
    }

    /**
     * 初始化聚能冰锥的拖尾特效
     * 创建冰元素拖尾
     */
    private void initializeConeTrail() {
        if (trailInitialized) return;

        // 生成唯一ID - 使用UUID确保每个投射物都有独立的拖尾
        String trailId = "focused_ice_cone_" + this.getUUID().toString();

        // 创建冰元素拖尾
        SimpleTrailManager manager = SimpleTrailManager.getInstance();
        coneTrail = manager.createIceElementTrail(trailId, this);

        trailInitialized = true;
    }

    /**
     * 清理拖尾特效资源
     * 在投射物销毁时调用，防止内存泄漏
     */
    private void cleanupTrail() {
        if (coneTrail != null) {
            // 停止拖尾效果（让剩余的点自然淡出）
            if (coneTrail.isActive()) {
                coneTrail.stop();
            }
            coneTrail = null;
        }
    }

    /**
     * 击中方块时的处理
     * 撞向墙体后产生大冰爆
     *
     * @param blockHitResult 方块击中结果
     */
    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        triggerExplosion(blockHitResult.getLocation());
        cleanupTrail();
        discard();
    }

    /**
     * 击中实体时的处理
     * 必定冰冻，可穿透 3 个敌人
     * 
     * @param entityHitResult 实体击中结果
     */
    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        
        // 只对活着的生物造成伤害
        if (entityHitResult.getEntity() instanceof LivingEntity target && target.isAlive()) {
            // 使用法术伤害来源，以便元素反应系统识别
            if (getOwner() instanceof LivingEntity owner) {
                target.hurt(
                    FocusedIceConeSpell.getFocusedIceConeDamageSource(this, owner),
                    getDamage()
                );
            }
            
            // 必定冰冻 - 添加冰冻效果
            target.setTicksFrozen(target.getTicksFrozen() + 100);
            
            // 添加缓慢效果
            target.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                100, // 5 秒
                2,   // 3 级缓慢
                false,
                true
            ));
            
            // 增加穿透计数
            pierceCount++;
            
            // 检查是否达到最大穿透次数
            if (pierceCount >= MAX_PIERCE_COUNT) {
                // 触发大冰爆
                triggerExplosion(target.position());
                cleanupTrail();
                discard();
            }
            // 否则继续穿透（不销毁投射物）
        }
    }

    /**
     * 触发大冰爆效果 - 聚能冰锥的终极爆炸，更集中更有冲击力
     * 
     * @param center 爆炸中心位置
     */
    private void triggerExplosion(Vec3 center) {
        Level level = this.level();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // 播放多层冰爆音效
        playExplosionSounds(serverLevel, center);

        // 播放冰爆粒子效果
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

        // 爆炸伤害为直接伤害的75%
        float explosionDamage = getDamage() * 0.75f;

        for (LivingEntity target : targets) {
            double distance = target.position().distanceTo(center);
            if (distance <= EXPLOSION_RADIUS) {
                // 根据距离衰减伤害
                float damageMultiplier = 1.0f - (float) (distance / EXPLOSION_RADIUS) * 0.5f;
                float actualDamage = explosionDamage * damageMultiplier;

                // 使用法术伤害来源，以便元素反应系统识别
                if (getOwner() instanceof LivingEntity owner) {
                    target.hurt(
                        FocusedIceConeSpell.getFocusedIceConeDamageSource(this, owner),
                        actualDamage
                    );
                }

                // 给目标添加冰冻效果
                target.setTicksFrozen(target.getTicksFrozen() + 60);
                
                // 给目标添加缓慢效果
                target.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    60, // 3 秒
                    1,
                    false,
                    true
                ));
            }
        }
    }

    /**
     * 播放聚能冰锥爆炸音效 - 法力高度集中的冰爆，更纯粹的法力释放
     * 
     * @param level  服务器世界
     * @param center 爆炸中心位置
     */
    private void playExplosionSounds(ServerLevel level, Vec3 center) {
        // 铁魔法冰冲击主音效 - 法力驱动的核心（更突出）
        level.playSound(
            null,
            center.x, center.y, center.z,
            SoundRegistry.ICE_IMPACT.get(),
            net.minecraft.sounds.SoundSource.PLAYERS,
            1.3f,
            0.6f + level.random.nextFloat() * 0.4f
        );

        // 玻璃破碎音效（冰晶高度压缩后碎裂）
        level.playSound(
            null,
            center.x, center.y, center.z,
            SoundEvents.GLASS_BREAK,
            net.minecraft.sounds.SoundSource.PLAYERS,
            1.1f,
            0.5f + level.random.nextFloat() * 0.3f
        );
        
        // 冰冻伤害音效（法力冻结效果）
        level.playSound(
            null,
            center.x, center.y, center.z,
            SoundEvents.PLAYER_HURT_FREEZE,
            net.minecraft.sounds.SoundSource.PLAYERS,
            1.0f,
            0.6f + level.random.nextFloat() * 0.3f
        );

        // 魔法能量释放音效（聚能后的法力爆发）
        level.playSound(
            null,
            center.x, center.y, center.z,
            SoundEvents.ENDER_EYE_DEATH,
            net.minecraft.sounds.SoundSource.PLAYERS,
            0.7f,
            1.3f + level.random.nextFloat() * 0.2f
        );
    }

    /**
     * 播放聚能冰锥爆炸粒子效果 - 集中而强烈的冰霜爆发
     * 
     * @param level  服务器世界
     * @param center 爆炸中心位置
     */
    private void playExplosionParticles(ServerLevel level, Vec3 center) {
        // 1. 中心爆炸云
        level.sendParticles(
            ParticleTypes.EXPLOSION,
            center.x, center.y + 0.5, center.z,
            1, 0, 0, 0, 0
        );

        // 2. 强烈的冰霜冲击波（聚能冰锥特色）
        level.sendParticles(
            new BlastwaveParticleOptions(EXPLOSION_COLOR, EXPLOSION_RADIUS * 0.7f),
            center.x, center.y + 0.5, center.z,
            1, 0, 0, 0, 0
        );

        // 3. 向内收缩的震荡波（聚能感）
        level.sendParticles(
            new ShockwaveParticleOptions(ICE_COLOR, EXPLOSION_RADIUS * 0.4f, true),
            center.x, center.y + 0.2, center.z,
            1, 0, 0, 0, 0
        );

        // 4. 大量雪花粒子 - 冰霜爆发
        level.sendParticles(
            ParticleRegistry.SNOWFLAKE_PARTICLE.get(),
            center.x, center.y + 0.5, center.z,
            (int) (EXPLOSION_RADIUS * 12), 
            EXPLOSION_RADIUS * 0.35, 0.4, EXPLOSION_RADIUS * 0.35, 
            0.12
        );

        // 5. 原版雪花 - 增强冰感
        level.sendParticles(
            ParticleTypes.SNOWFLAKE,
            center.x, center.y + 0.5, center.z,
            (int) (EXPLOSION_RADIUS * 10),
            EXPLOSION_RADIUS * 0.3, 0.3, EXPLOSION_RADIUS * 0.3,
            0.08
        );

        // 6. 冰晶火花 - 向四周飞溅
        level.sendParticles(
            new SparkParticleOptions(ICE_COLOR),
            center.x, center.y + 0.5, center.z,
            25,
            EXPLOSION_RADIUS * 0.25, 0.25, EXPLOSION_RADIUS * 0.25,
            0.4
        );

        // 7. 亮蓝色火花 - 聚能光芒
        level.sendParticles(
            new SparkParticleOptions(new Vector3f(0.3f, 0.8f, 1.0f)),
            center.x, center.y + 0.5, center.z,
            15,
            EXPLOSION_RADIUS * 0.15, 0.15, EXPLOSION_RADIUS * 0.15,
            0.3
        );

        // 8. 冰霜雾气
        level.sendParticles(
            ParticleTypes.POOF,
            center.x, center.y + 0.3, center.z,
            12,
            EXPLOSION_RADIUS * 0.2, 0.2, EXPLOSION_RADIUS * 0.2,
            0.08
        );

        // 9. 环形冰霜火花（聚能特色）
        for (int i = 0; i < 12; i++) {
            double angle = (i / 12.0) * Math.PI * 2;
            double x = center.x + Math.cos(angle) * (EXPLOSION_RADIUS * 0.4);
            double z = center.z + Math.sin(angle) * (EXPLOSION_RADIUS * 0.4);
            level.sendParticles(
                new SparkParticleOptions(ICE_COLOR),
                x, center.y + 0.3, z,
                2, 0, 0.1, 0, 0.05
            );
        }

        // 10. 地面冰霜扩散
        for (int i = 0; i < 10; i++) {
            double angle = (i / 10.0) * Math.PI * 2;
            double x = center.x + Math.cos(angle) * EXPLOSION_RADIUS * 0.7;
            double z = center.z + Math.sin(angle) * EXPLOSION_RADIUS * 0.7;
            level.sendParticles(
                ParticleTypes.SNOWFLAKE,
                x, center.y + 0.1, z,
                3, 0.05, 0.05, 0.05, 0.02
            );
        }
    }

    /**
     * 轨迹粒子效果（客户端每tick调用）- 聚能冰锥的高速冰霜轨迹
     * 集成简单拖尾特效API
     */
    @Override
    public void trailParticles() {
        Level level = this.level();

        // ========== 集成简单拖尾特效 ==========
        // 仅在客户端且拖尾已初始化时更新拖尾
        if (level.isClientSide()) {
            // 延迟初始化拖尾
            if (!trailInitialized) {
                initializeConeTrail();
            }

            // SimpleTrailEffect 使用自动更新机制，不需要手动添加点
            // 更新在 SimpleTrailClientHandler 中通过 onRenderLevel 事件处理
        }

        // ========== 原有粒子效果（保留不变） ==========
        // 1. 冰晶轨迹 - 参考铁魔法Icicle，更加集中
        for (int i = 0; i < 2; i++) {
            double speed = 0.04;
            double dx = Utils.random.nextDouble() * 2 * speed - speed;
            double dy = Utils.random.nextDouble() * 2 * speed - speed;
            double dz = Utils.random.nextDouble() * 2 * speed - speed;

            // 混合使用铁魔法雪花和原版雪花
            level.addParticle(
                Utils.random.nextDouble() < 0.4 ? ParticleRegistry.SNOWFLAKE_PARTICLE.get() : ParticleTypes.SNOWFLAKE,
                this.getX() + dx, this.getY() + dy, this.getZ() + dz,
                dx * 0.3, dy * 0.3, dz * 0.3
            );
        }

        // 2. 冰霜火花 - 聚能光芒
        if (Utils.random.nextDouble() < 0.35) {
            level.addParticle(
                new SparkParticleOptions(ICE_COLOR),
                this.getX(), this.getY(), this.getZ(),
                0, 0, 0
            );
        }

        // 3. 亮蓝色火花 - 聚能核心
        if (Utils.random.nextDouble() < 0.25) {
            level.addParticle(
                new SparkParticleOptions(new Vector3f(0.3f, 0.8f, 1.0f)),
                this.getX(), this.getY(), this.getZ(),
                0, 0, 0
            );
        }

        // 4. 冰霜雾气轨迹
        if (Utils.random.nextDouble() < 0.2) {
            level.addParticle(
                ParticleTypes.POOF,
                this.getX() + (Utils.random.nextDouble() - 0.5) * 0.15,
                this.getY() + (Utils.random.nextDouble() - 0.5) * 0.15,
                this.getZ() + (Utils.random.nextDouble() - 0.5) * 0.15,
                0, 0.01, 0
            );
        }

        // 5. 高速尾迹 - 紫水晶色粒子（聚能感）
        if (Utils.random.nextDouble() < 0.15) {
            level.addParticle(
                ParticleTypes.WITCH,
                this.getX() - this.getDeltaMovement().x * 0.5,
                this.getY() - this.getDeltaMovement().y * 0.5,
                this.getZ() - this.getDeltaMovement().z * 0.5,
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
     * 高速运动
     * 
     * @return 速度
     */
    @Override
    public float getSpeed() {
        return 2.5f;
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
