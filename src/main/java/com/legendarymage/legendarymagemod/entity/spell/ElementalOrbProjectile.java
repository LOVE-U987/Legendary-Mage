package com.legendarymage.legendarymagemod.entity.spell;

import com.legendarymage.legendarymagemod.effect.ModEffects;
import com.legendarymage.legendarymagemod.element.ElementReactionManager;
import com.legendarymage.legendarymagemod.element.ElementType;
import com.legendarymage.legendarymagemod.entity.ModEntities;
import com.legendarymage.legendarymagemod.spell.ElementalBarrageSpell;
import com.legendarymage.legendarymagemod.trail.SimpleTrailEffect;
import com.legendarymage.legendarymagemod.trail.SimpleTrailManager;

import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;

import net.minecraft.world.effect.MobEffect;
import io.redspace.ironsspellbooks.particle.ShockwaveParticleOptions;
import io.redspace.ironsspellbooks.particle.SparkParticleOptions;
import io.redspace.ironsspellbooks.particle.ZapParticleOption;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;

import net.minecraft.core.Holder;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 元素球投射物实体 - v2.0 (简单拖尾版)
 * 元素弹幕法术使用的投射物，支持冰、火、雷三种元素类型
 *
 * 【更新内容】
 * - v2.0: 使用新的SimpleTrailEffect拖尾系统
 *
 * 效果：
 * - 冰球：造成范围冰冻效果（缓慢+冰冻）
 * - 火球：造成范围烈焰BUFF效果（持续火焰伤害）
 * - 雷球：造成闪电链效果（连锁跳跃）
 *
 * @author Love_U
 * @version 1.0.6
 */
public class ElementalOrbProjectile extends AbstractMagicProjectile {

    /**
     * 元素类型数据同步器
     * 使用整数存储：0=ICE, 1=FIRE, 2=LIGHTNING
     */
    private static final EntityDataAccessor<Integer> DATA_ORB_TYPE = SynchedEntityData.defineId(
            ElementalOrbProjectile.class, EntityDataSerializers.INT);

    /**
     * 伤害值
     */
    private float damage = 5.0f;

    /**
     * 效果范围（格）
     */
    private int range = 4;

    /**
     * 法术强度
     */
    private float spellPower = 1.0f;

    /**
     * 闪电链最大跳跃次数
     */
    private static final int MAX_CHAIN_JUMPS = 4;

    /**
     * 闪电链每次跳跃范围
     */
    private static final float CHAIN_RANGE = 4.0f;

    /**
     * 已经击中过的实体（用于闪电链）
     */
    private final List<Entity> hitEntities = new ArrayList<>();

    /**
     * 简单拖尾特效实例（每个投射物拥有独立的拖尾）
     * 仅在客户端创建和使用
     */
    private SimpleTrailEffect orbTrail = null;

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
    public ElementalOrbProjectile(EntityType<? extends ElementalOrbProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true); // 无视重力
    }

    /**
     * 构造函数
     *
     * @param level   世界
     * @param shooter 发射者
     */
    public ElementalOrbProjectile(Level level, LivingEntity shooter) {
        this(ModEntities.ELEMENTAL_ORB.get(), level);
        setOwner(shooter);
    }

    /**
     * 初始化元素球的拖尾特效
     * 根据不同的元素类型创建不同样式的拖尾
     */
    private void initializeOrbTrail() {
        if (trailInitialized) return;

        // 生成唯一ID - 使用UUID确保每个投射物都有独立的拖尾
        // 避免使用getId()，因为在客户端多个实体可能拥有相同的ID
        String trailId = "elemental_orb_" + this.getUUID().toString();

        // 根据元素类型创建对应拖尾
        SimpleTrailManager manager = SimpleTrailManager.getInstance();

        switch (getOrbType()) {
            case ICE -> {
                // 冰球：冰元素拖尾
                orbTrail = manager.createIceElementTrail(trailId, this);
            }

            case FIRE -> {
                // 火球：火元素拖尾
                orbTrail = manager.createFireElementTrail(trailId, this);
            }

            case LIGHTNING -> {
                // 雷球：雷元素拖尾
                orbTrail = manager.createLightningElementTrail(trailId, this);
            }
        }

        trailInitialized = true;

        com.legendarymage.legendarymagemod.ModLogger.entityDebug(
                "[元素弹幕-投射物] 初始化拖尾: {} | 类型: {}",
                trailId, getOrbType().getName()
        );
    }

    /**
     * 定义同步数据
     *
     * @param builder 数据构建器
     */
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ORB_TYPE, 0); // 默认为ICE
    }

    /**
     * 设置元素类型
     *
     * @param orbType 元素类型
     */
    public void setOrbType(ElementalBarrageSpell.OrbType orbType) {
        int typeId = switch (orbType) {
            case ICE -> 0;
            case FIRE -> 1;
            case LIGHTNING -> 2;
        };
        this.entityData.set(DATA_ORB_TYPE, typeId);
    }

    /**
     * 获取元素类型
     *
     * @return 元素类型
     */
    public ElementalBarrageSpell.OrbType getOrbType() {
        int typeId = this.entityData.get(DATA_ORB_TYPE);
        return switch (typeId) {
            case 1 -> ElementalBarrageSpell.OrbType.FIRE;
            case 2 -> ElementalBarrageSpell.OrbType.LIGHTNING;
            default -> ElementalBarrageSpell.OrbType.ICE;
        };
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
     * 设置范围
     *
     * @param range 范围（格）
     */
    public void setRange(int range) {
        this.range = range;
    }

    /**
     * 设置法术强度
     *
     * @param spellPower 法术强度
     */
    public void setSpellPower(float spellPower) {
        this.spellPower = spellPower;
    }

    /**
     * 击中方块时的处理
     *
     * @param blockHitResult 方块击中结果
     */
    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);

        if (level() instanceof ServerLevel serverLevel) {
            // 在击中位置触发范围效果
            triggerAreaEffect(serverLevel, blockHitResult.getLocation());
        }

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

        if (!(level() instanceof ServerLevel serverLevel)) {
            discard();
            return;
        }

        Entity target = entityHitResult.getEntity();
        if (!(target instanceof LivingEntity livingTarget)) {
            discard();
            return;
        }

        // 记录已击中
        hitEntities.add(target);

        // 获取所有者
        LivingEntity owner = getOwner() instanceof LivingEntity ? (LivingEntity) getOwner() : null;

        // 根据元素类型处理
        switch (getOrbType()) {
            case ICE -> handleIceHit(serverLevel, livingTarget, owner);
            case FIRE -> handleFireHit(serverLevel, livingTarget, owner);
            case LIGHTNING -> handleLightningHit(serverLevel, livingTarget, owner);
        }

        // 触发范围效果
        triggerAreaEffect(serverLevel, target.position());

        discard();
    }

    /**
     * 处理冰球击中
     *
     * @param serverLevel 服务器世界
     * @param target      目标
     * @param owner       所有者
     */
    private void handleIceHit(ServerLevel serverLevel, LivingEntity target, LivingEntity owner) {
        // 造成直接伤害
        DamageSource damageSource = createDamageSource(serverLevel, owner);
        target.hurt(damageSource, damage);

        // 施加冰冻效果（缓慢）
        target.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                60, // 3秒
                2,  // 3级缓慢
                false,
                true,
                true
        ));

        // 施加元素标记
        if (owner != null) {
            ElementReactionManager.onElementDamage(serverLevel, target, owner, ElementType.ICE, damage);
        }

        // 播放音效
        serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundRegistry.ICE_IMPACT.get(), getSoundSource(), 1.0f, 1.0f);
    }

    /**
     * 处理火球击中
     *
     * @param serverLevel 服务器世界
     * @param target      目标
     * @param owner       所有者
     */
    private void handleFireHit(ServerLevel serverLevel, LivingEntity target, LivingEntity owner) {
        // 造成直接伤害
        DamageSource damageSource = createDamageSource(serverLevel, owner);
        target.hurt(damageSource, damage);

        // 施加烈焰BUFF（使用纵火狂的烈焰效果）
        var pyroFlameHolder = ModEffects.PYRO_FLAME;
        if (pyroFlameHolder.get() != null) {
            target.addEffect(new MobEffectInstance(
                    pyroFlameHolder,
                    100, // 5秒
                    0,   // 1级
                    false,
                    true,
                    true
            ));
        }

        // 点燃目标
        target.setRemainingFireTicks(60); // 3秒燃烧

        // 施加元素标记
        if (owner != null) {
            ElementReactionManager.onElementDamage(serverLevel, target, owner, ElementType.FIRE, damage);
        }

        // 播放音效
        serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.FIRECHARGE_USE, getSoundSource(), 1.0f, 1.0f);
    }

    /**
     * 处理雷球击中
     *
     * @param serverLevel 服务器世界
     * @param target      目标
     * @param owner       所有者
     */
    private void handleLightningHit(ServerLevel serverLevel, LivingEntity target, LivingEntity owner) {
        // 造成直接伤害
        DamageSource damageSource = createDamageSource(serverLevel, owner);
        target.hurt(damageSource, damage);

        // 施加元素标记
        if (owner != null) {
            ElementReactionManager.onElementDamage(serverLevel, target, owner, ElementType.LIGHTNING, damage);
        }

        // 播放音效
        serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.LIGHTNING_BOLT_IMPACT, getSoundSource(), 1.0f, 1.0f);

        // 触发闪电链效果
        triggerChainLightning(serverLevel, target, owner, 0);
    }

    /**
     * 触发闪电链效果
     *
     * @param serverLevel 服务器世界
     * @param source      源头实体
     * @param owner       所有者
     * @param jumpCount   当前跳跃次数
     */
    private void triggerChainLightning(ServerLevel serverLevel, LivingEntity source, LivingEntity owner, int jumpCount) {
        if (jumpCount >= MAX_CHAIN_JUMPS) {
            return;
        }

        // 查找范围内的其他目标
        Vec3 pos = source.position();
        List<LivingEntity> nearbyTargets = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                source.getBoundingBox().inflate(CHAIN_RANGE),
                entity -> entity != source
                        && entity != owner
                        && entity.isAlive()
                        && !hitEntities.contains(entity)
                        && !DamageSources.isFriendlyFireBetween(entity, owner)
        );

        if (nearbyTargets.isEmpty()) {
            return;
        }

        // 按距离排序
        nearbyTargets.sort(Comparator.comparingDouble(o -> o.distanceToSqr(source)));

        // 选择最近的目标
        LivingEntity nextTarget = nearbyTargets.get(0);
        hitEntities.add(nextTarget);

        // 计算跳跃伤害（每次递减20%）
        float chainDamage = damage * (1.0f - jumpCount * 0.2f);

        // 造成伤害
        DamageSource damageSource = createDamageSource(serverLevel, owner);
        nextTarget.hurt(damageSource, chainDamage);

        // 施加元素标记
        if (owner != null) {
            ElementReactionManager.onElementDamage(serverLevel, nextTarget, owner, ElementType.LIGHTNING, chainDamage);
        }

        // 播放闪电链粒子效果
        Vec3 start = source.position().add(0, source.getBbHeight() / 2, 0);
        Vec3 end = nextTarget.position().add(0, nextTarget.getBbHeight() / 2, 0);
        serverLevel.sendParticles(
                new ZapParticleOption(end),
                start.x, start.y, start.z,
                1, 0, 0, 0, 0
        );

        // 播放音效
        serverLevel.playSound(null, nextTarget.getX(), nextTarget.getY(), nextTarget.getZ(),
                SoundRegistry.CHAIN_LIGHTNING_CHAIN.get(), getSoundSource(), 0.8f, 1.0f);

        // 继续连锁
        triggerChainLightning(serverLevel, nextTarget, owner, jumpCount + 1);
    }

    /**
     * 触发范围效果
     *
     * @param serverLevel 服务器世界
     * @param center      中心位置
     */
    private void triggerAreaEffect(ServerLevel serverLevel, Vec3 center) {
        switch (getOrbType()) {
            case ICE -> triggerIceAreaEffect(serverLevel, center);
            case FIRE -> triggerFireAreaEffect(serverLevel, center);
            case LIGHTNING -> triggerLightningAreaEffect(serverLevel, center);
        }
    }

    /**
     * 触发冰球范围效果
     *
     * @param serverLevel 服务器世界
     * @param center      中心位置
     */
    private void triggerIceAreaEffect(ServerLevel serverLevel, Vec3 center) {
        Vector3f color = getOrbType().getColor();

        // 冰霜冲击波
        serverLevel.sendParticles(
                new ShockwaveParticleOptions(color, range * 0.5f, true),
                center.x, center.y + 0.5, center.z,
                1, 0, 0, 0, 0
        );

        // 雪花粒子
        serverLevel.sendParticles(
                ParticleRegistry.SNOWFLAKE_PARTICLE.get(),
                center.x, center.y + 0.5, center.z,
                range * 3,
                range * 0.3, 0.3, range * 0.3,
                0.1
        );

        // 对范围内敌人施加缓慢效果
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                new net.minecraft.world.phys.AABB(
                        center.x - range, center.y - range, center.z - range,
                        center.x + range, center.y + range, center.z + range
                ),
                entity -> entity.isAlive()
                        && entity != getOwner()
                        && !DamageSources.isFriendlyFireBetween(entity, getOwner())
        );

        LivingEntity owner = getOwner() instanceof LivingEntity ? (LivingEntity) getOwner() : null;

        for (LivingEntity target : targets) {
            // 检查距离
            if (target.position().distanceTo(center) <= range) {
                // 施加缓慢效果
                target.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        40, // 2秒
                        0,
                        false,
                        true,
                        true
                ));

                // 施加冰元素标记
                if (owner != null) {
                    ElementReactionManager.onElementDamage(serverLevel, target, owner, ElementType.ICE, 0);
                }
            }
        }
    }

    /**
     * 触发火球范围效果
     *
     * @param serverLevel 服务器世界
     * @param center      中心位置
     */
    private void triggerFireAreaEffect(ServerLevel serverLevel, Vec3 center) {
        Vector3f color = getOrbType().getColor();

        // 火焰冲击波
        serverLevel.sendParticles(
                new ShockwaveParticleOptions(color, range * 0.5f, true),
                center.x, center.y + 0.5, center.z,
                1, 0, 0, 0, 0
        );

        // 火焰粒子
        serverLevel.sendParticles(
                ParticleRegistry.FIRE_PARTICLE.get(),
                center.x, center.y + 0.5, center.z,
                range * 4,
                range * 0.3, 0.3, range * 0.3,
                0.1
        );

        // 对范围内敌人施加烈焰效果
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                new net.minecraft.world.phys.AABB(
                        center.x - range, center.y - range, center.z - range,
                        center.x + range, center.y + range, center.z + range
                ),
                entity -> entity.isAlive()
                        && entity != getOwner()
                        && !DamageSources.isFriendlyFireBetween(entity, getOwner())
        );

        LivingEntity owner = getOwner() instanceof LivingEntity ? (LivingEntity) getOwner() : null;
        var pyroFlameHolder = ModEffects.PYRO_FLAME;

        for (LivingEntity target : targets) {
            // 检查距离
            if (target.position().distanceTo(center) <= range) {
                // 施加烈焰BUFF
                if (pyroFlameHolder.get() != null) {
                    target.addEffect(new MobEffectInstance(
                            pyroFlameHolder,
                            60, // 3秒
                            0,
                            false,
                            true,
                            true
                    ));
                }

                // 点燃
                target.setRemainingFireTicks(40); // 2秒

                // 施加火元素标记
                if (owner != null) {
                    ElementReactionManager.onElementDamage(serverLevel, target, owner, ElementType.FIRE, 0);
                }
            }
        }
    }

    /**
     * 触发雷球范围效果
     *
     * @param serverLevel 服务器世界
     * @param center      中心位置
     */
    private void triggerLightningAreaEffect(ServerLevel serverLevel, Vec3 center) {
        Vector3f color = getOrbType().getColor();

        // 雷电冲击波
        serverLevel.sendParticles(
                new ShockwaveParticleOptions(color, range * 0.5f, true),
                center.x, center.y + 0.5, center.z,
                1, 0, 0, 0, 0
        );

        // 电火花粒子
        serverLevel.sendParticles(
                ParticleRegistry.ELECTRICITY_PARTICLE.get(),
                center.x, center.y + 0.5, center.z,
                range * 5,
                range * 0.3, 0.3, range * 0.3,
                0.2
        );

        // 对范围内敌人造成伤害和标记
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                new net.minecraft.world.phys.AABB(
                        center.x - range, center.y - range, center.z - range,
                        center.x + range, center.y + range, center.z + range
                ),
                entity -> entity.isAlive()
                        && entity != getOwner()
                        && !DamageSources.isFriendlyFireBetween(entity, getOwner())
        );

        LivingEntity owner = getOwner() instanceof LivingEntity ? (LivingEntity) getOwner() : null;
        DamageSource damageSource = createDamageSource(serverLevel, owner);

        for (LivingEntity target : targets) {
            double distance = target.position().distanceTo(center);
            if (distance <= range) {
                // 范围伤害为直接伤害的30%
                float areaDamage = damage * 0.3f * (1.0f - (float) distance / range * 0.5f);
                target.hurt(damageSource, areaDamage);

                // 施加雷元素标记
                if (owner != null) {
                    ElementReactionManager.onElementDamage(serverLevel, target, owner, ElementType.LIGHTNING, areaDamage);
                }
            }
        }
    }

    /**
     * 创建伤害源
     *
     * @param serverLevel 服务器世界
     * @param owner       所有者
     * @return 伤害源
     */
    private DamageSource createDamageSource(ServerLevel serverLevel, LivingEntity owner) {
        // 使用魔法伤害源
        return serverLevel.damageSources().magic();
    }

    /**
     * 轨迹粒子效果（客户端每tick调用）
     * 集成简单拖尾特效API
     * 粒子在实体周围呈球形生成，控制数量避免占用过多特效资源
     */
    @Override
    public void trailParticles() {
        Level level = level();
        Vector3f color = getOrbType().getColor();

        // ========== 集成简单拖尾特效 ==========
        // 仅在客户端且拖尾已初始化时更新拖尾
        if (level.isClientSide()) {
            // 延迟初始化拖尾
            if (!trailInitialized) {
                initializeOrbTrail();
            }

            // SimpleTrailEffect 使用自动更新机制，不需要手动添加点
            // 更新在 SimpleTrailClientHandler 中通过 onRenderLevel 事件处理
        }

        // ========== 球形粒子效果（优化版） ==========
        // 每tick只生成1-2个粒子，控制总量
        if (tickCount % 2 == 0) { // 每2tick生成一次，减少粒子数量
            spawnSphericalParticles(level, color);
        }
    }

    /**
     * 在实体周围生成球形分布的粒子
     * 使用球坐标系计算粒子位置
     * 
     * @param level 世界
     * @param color 粒子颜色
     */
    private void spawnSphericalParticles(Level level, Vector3f color) {
        // 球形半径
        double radius = 0.4;
        
        // 随机生成球坐标
        double theta = Math.random() * Math.PI * 2; // 水平角度 0-2π
        double phi = Math.acos(2 * Math.random() - 1); // 垂直角度 0-π
        
        // 球坐标转直角坐标
        double x = radius * Math.sin(phi) * Math.cos(theta);
        double y = radius * Math.sin(phi) * Math.sin(theta);
        double z = radius * Math.cos(phi);
        
        // 粒子生成位置（实体中心 + 球形偏移）
        double particleX = getX() + x;
        double particleY = getY() + getBbHeight() / 2 + y; // 在实体中心高度
        double particleZ = getZ() + z;

        // 根据元素类型生成不同粒子
        switch (getOrbType()) {
            case ICE -> {
                // 冰霜粒子 - 雪花
                level.addParticle(
                        ParticleRegistry.SNOWFLAKE_PARTICLE.get(),
                        particleX, particleY, particleZ,
                        0, 0, 0
                );
            }
            case FIRE -> {
                // 火焰粒子 - 火球
                level.addParticle(
                        ParticleRegistry.FIRE_PARTICLE.get(),
                        particleX, particleY, particleZ,
                        0, 0.02, 0
                );
            }
            case LIGHTNING -> {
                // 雷电粒子 - 电火花
                level.addParticle(
                        ParticleRegistry.ELECTRICITY_PARTICLE.get(),
                        particleX, particleY, particleZ,
                        (Math.random() - 0.5) * 0.05,
                        (Math.random() - 0.5) * 0.05,
                        (Math.random() - 0.5) * 0.05
                );
            }
        }

        // 偶尔添加火花效果（10%概率）
        if (Math.random() < 0.1) {
            level.addParticle(
                    new SparkParticleOptions(color),
                    getX(), getY() + getBbHeight() / 2, getZ(),
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
        // 击中粒子效果在 triggerAreaEffect 中处理
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
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return switch (getOrbType()) {
            case ICE -> Optional.of(SoundRegistry.ICE_IMPACT);
            case FIRE -> Optional.of(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.FIRECHARGE_USE));
            case LIGHTNING -> Optional.of(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.LIGHTNING_BOLT_IMPACT));
        };
    }

    /**
     * 是否可以击中实体
     *
     * @param target 目标实体
     * @return 是否可以击中
     */
    @Override
    protected boolean canHitEntity(Entity target) {
        return target instanceof LivingEntity
                && !hitEntities.contains(target)
                && super.canHitEntity(target);
    }

    /**
     * 每tick更新
     */
    @Override
    public void tick() {
        super.tick();

        // 超过一定时间自动消失
        if (tickCount > 100) { // 5秒
            // 清理拖尾资源
            cleanupTrail();
            discard();
        }
    }

    /**
     * 清理拖尾特效资源
     * 在投射物销毁时调用，防止内存泄漏
     */
    private void cleanupTrail() {
        if (orbTrail != null) {
            // 停止拖尾效果（让剩余的点自然淡出）
            if (orbTrail.isActive()) {
                orbTrail.stop();
            }

            com.legendarymage.legendarymagemod.ModLogger.entityDebug(
                    "[元素弹幕-投射物] 清理拖尾: {}", orbTrail.getId()
            );

            orbTrail = null;
        }
    }
}
