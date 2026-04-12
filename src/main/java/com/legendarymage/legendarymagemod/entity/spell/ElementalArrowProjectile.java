package com.legendarymage.legendarymagemod.entity.spell;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.effect.ModEffects;
import com.legendarymage.legendarymagemod.element.ElementReactionManager;
import com.legendarymage.legendarymagemod.element.ElementType;
import com.legendarymage.legendarymagemod.entity.ModEntities;
import com.legendarymage.legendarymagemod.trail.SimpleTrailEffect;
import com.legendarymage.legendarymagemod.trail.SimpleTrailManager;

import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.particle.ShockwaveParticleOptions;
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
import java.util.List;
import java.util.Optional;

/**
 * 元素箭投射物实体
 * 三向之矢法术使用的投射物，支持冰、火、雷三种元素类型
 * 
 * 效果：
 * - 冰箭：造成冰冻伤害，附加缓慢效果
 * - 火箭：造成火焰伤害，附加燃烧效果
 * - 雷箭：造成雷电伤害，附加感电效果
 * 
 * 特殊机制：
 * - 击中敌人时立即造成4格范围元素伤害
 * - 落在地面时产生持续5秒的元素区域伤害
 * 
 * @author Love_U
 * @version 1.0.6
 */
public class ElementalArrowProjectile extends AbstractMagicProjectile {

    /**
     * 元素类型数据同步器
     * 使用整数存储：0=ICE, 1=FIRE, 2=LIGHTNING
     */
    private static final EntityDataAccessor<Integer> DATA_ARROW_TYPE = SynchedEntityData.defineId(
            ElementalArrowProjectile.class, EntityDataSerializers.INT);

    /**
     * 基础伤害
     */
    private float baseDamage = 15.0f;

    /**
     * 法术强度
     */
    private float spellPower = 1.0f;

    /**
     * 法术等级
     */
    private int spellLevel = 1;

    /**
     * 范围伤害半径（格）
     */
    private static final float AREA_RADIUS = 4.0f;

    /**
     * 地面持续伤害时长（tick）
     */
    private static final int GROUND_EFFECT_DURATION = 100; // 5秒

    /**
     * 地面持续伤害间隔（tick）
     */
    private static final int GROUND_EFFECT_INTERVAL = 20; // 1秒

    /**
     * 已经击中过的实体列表
     */
    private final List<Entity> hitEntities = new ArrayList<>();

    /**
     * 拖尾特效实例（每个投射物拥有独立的拖尾）
     * 仅在客户端创建和使用
     */
    private SimpleTrailEffect arrowTrail = null;

    /**
     * 拖尾是否已初始化标志
     */
    private boolean trailInitialized = false;

    /**
     * 元素箭类型枚举
     */
    public enum ArrowType {
        ICE(0, "ice", new Vector3f(0.5f, 0.8f, 1.0f), ElementType.ICE),
        FIRE(1, "fire", new Vector3f(1.0f, 0.3f, 0.0f), ElementType.FIRE),
        LIGHTNING(2, "lightning", new Vector3f(0.8f, 0.9f, 1.0f), ElementType.LIGHTNING);

        private final int id;
        private final String name;
        private final Vector3f color;
        private final ElementType elementType;

        ArrowType(int id, String name, Vector3f color, ElementType elementType) {
            this.id = id;
            this.name = name;
            this.color = color;
            this.elementType = elementType;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Vector3f getColor() {
            return color;
        }

        public ElementType getElementType() {
            return elementType;
        }

        /**
         * 根据ID获取元素类型
         * 
         * @param id 元素ID
         * @return 元素类型
         */
        public static ArrowType byId(int id) {
            for (ArrowType type : values()) {
                if (type.id == id) {
                    return type;
                }
            }
            return ICE;
        }
    }

    /**
     * 构造函数
     * 
     * @param entityType 实体类型
     * @param level      世界
     */
    public ElementalArrowProjectile(EntityType<? extends ElementalArrowProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true); // 无视重力
    }

    /**
     * 构造函数
     * 
     * @param level   世界
     * @param shooter 发射者
     */
    public ElementalArrowProjectile(Level level, LivingEntity shooter) {
        this(ModEntities.ELEMENTAL_ARROW.get(), level);
        setOwner(shooter);
    }

    /**
     * 定义同步数据
     * 
     * @param builder 数据构建器
     */
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ARROW_TYPE, 0); // 默认为ICE
    }

    /**
     * 设置元素类型
     * 
     * @param arrowType 元素类型
     */
    public void setArrowType(ArrowType arrowType) {
        this.entityData.set(DATA_ARROW_TYPE, arrowType.getId());
    }

    /**
     * 获取元素类型
     * 
     * @return 元素类型
     */
    public ArrowType getArrowType() {
        return ArrowType.byId(this.entityData.get(DATA_ARROW_TYPE));
    }

    /**
     * 设置基础伤害
     * 
     * @param damage 伤害值
     */
    public void setBaseDamage(float damage) {
        this.baseDamage = damage;
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
     * 设置法术等级
     * 
     * @param spellLevel 法术等级
     */
    public void setSpellLevel(int spellLevel) {
        this.spellLevel = spellLevel;
    }

    /**
     * 计算最终伤害
     * 
     * @return 最终伤害值
     */
    private float calculateDamage() {
        return baseDamage * spellPower;
    }

    /**
     * 计算持续区域伤害
     * 
     * @return 持续伤害值（基础伤害/5）
     */
    private float calculateGroundDamage() {
        return (baseDamage / 5.0f) * spellPower;
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
            Vec3 hitPos = blockHitResult.getLocation();

            // 在击中位置创建持续伤害区域
            createGroundEffect(serverLevel, hitPos);

            // 触发范围伤害
            triggerAreaDamage(serverLevel, hitPos, false);
        }

        // 清理拖尾
        cleanupTrail();
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

        // 计算伤害
        float damage = calculateDamage();

        // 根据元素类型处理直接伤害
        switch (getArrowType()) {
            case ICE -> handleIceHit(serverLevel, livingTarget, owner, damage);
            case FIRE -> handleFireHit(serverLevel, livingTarget, owner, damage);
            case LIGHTNING -> handleLightningHit(serverLevel, livingTarget, owner, damage);
        }

        // 击中敌人时立即触发4格范围伤害
        triggerAreaDamage(serverLevel, target.position(), true);

        // 清理拖尾
        cleanupTrail();
        discard();
    }

    /**
     * 处理冰箭击中
     * 
     * @param serverLevel 服务器世界
     * @param target      目标
     * @param owner       所有者
     * @param damage      伤害值
     */
    private void handleIceHit(ServerLevel serverLevel, LivingEntity target, LivingEntity owner, float damage) {
        // 造成直接伤害
        DamageSource damageSource = createDamageSource(serverLevel, owner);
        target.hurt(damageSource, damage);

        // 施加冰冻效果（缓慢）
        target.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                60, // 3秒
                1,  // 2级缓慢
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
     * 处理火箭击中
     * 
     * @param serverLevel 服务器世界
     * @param target      目标
     * @param owner       所有者
     * @param damage      伤害值
     */
    private void handleFireHit(ServerLevel serverLevel, LivingEntity target, LivingEntity owner, float damage) {
        // 造成直接伤害
        DamageSource damageSource = createDamageSource(serverLevel, owner);
        target.hurt(damageSource, damage);

        // 点燃目标
        target.setRemainingFireTicks(100); // 5秒燃烧

        // 施加元素标记
        if (owner != null) {
            ElementReactionManager.onElementDamage(serverLevel, target, owner, ElementType.FIRE, damage);
        }

        // 播放音效
        serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.FIRECHARGE_USE, getSoundSource(), 1.0f, 1.0f);
    }

    /**
     * 处理雷箭击中
     * 
     * @param serverLevel 服务器世界
     * @param target      目标
     * @param owner       所有者
     * @param damage      伤害值
     */
    private void handleLightningHit(ServerLevel serverLevel, LivingEntity target, LivingEntity owner, float damage) {
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
    }

    /**
     * 触发范围伤害（4格）
     * 
     * @param serverLevel 服务器世界
     * @param center      中心位置
     * @param isDirectHit 是否直接击中敌人
     */
    private void triggerAreaDamage(ServerLevel serverLevel, Vec3 center, boolean isDirectHit) {
        ArrowType arrowType = getArrowType();
        Vector3f color = arrowType.getColor();
        LivingEntity owner = getOwner() instanceof LivingEntity ? (LivingEntity) getOwner() : null;
        
        // 播放粒子效果
        serverLevel.sendParticles(
                new ShockwaveParticleOptions(color, AREA_RADIUS * 0.5f, true),
                center.x, center.y + 0.5, center.z,
                1, 0, 0, 0, 0
        );

        // 根据元素类型播放不同的粒子效果
        switch (arrowType) {
            case ICE -> {
                serverLevel.sendParticles(
                        ParticleRegistry.SNOWFLAKE_PARTICLE.get(),
                        center.x, center.y + 0.5, center.z,
                        (int) (AREA_RADIUS * 5),
                        AREA_RADIUS * 0.3, 0.3, AREA_RADIUS * 0.3,
                        0.1
                );
            }
            case FIRE -> {
                serverLevel.sendParticles(
                        ParticleRegistry.FIRE_PARTICLE.get(),
                        center.x, center.y + 0.5, center.z,
                        (int) (AREA_RADIUS * 6),
                        AREA_RADIUS * 0.3, 0.3, AREA_RADIUS * 0.3,
                        0.1
                );
            }
            case LIGHTNING -> {
                serverLevel.sendParticles(
                        ParticleRegistry.ELECTRICITY_PARTICLE.get(),
                        center.x, center.y + 0.5, center.z,
                        (int) (AREA_RADIUS * 8),
                        AREA_RADIUS * 0.3, 0.3, AREA_RADIUS * 0.3,
                        0.2
                );
            }
        }

        // 获取范围内的所有生物
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                new net.minecraft.world.phys.AABB(
                        center.x - AREA_RADIUS, center.y - AREA_RADIUS, center.z - AREA_RADIUS,
                        center.x + AREA_RADIUS, center.y + AREA_RADIUS, center.z + AREA_RADIUS
                ),
                entity -> entity.isAlive() 
                        && entity != getOwner()
                        && !hitEntities.contains(entity)
                        && !DamageSources.isFriendlyFireBetween(entity, getOwner())
        );

        // 计算范围伤害
        float areaDamage = calculateDamage() * 0.5f; // 范围伤害为直接伤害的50%
        DamageSource damageSource = createDamageSource(serverLevel, owner);

        for (LivingEntity target : targets) {
            double distance = target.position().distanceTo(center);
            if (distance <= AREA_RADIUS) {
                // 距离衰减
                float finalDamage = areaDamage * (1.0f - (float) distance / AREA_RADIUS * 0.5f);
                target.hurt(damageSource, finalDamage);

                // 施加元素效果和标记
                switch (arrowType) {
                    case ICE -> {
                        target.addEffect(new MobEffectInstance(
                                MobEffects.MOVEMENT_SLOWDOWN,
                                40, // 2秒
                                0,
                                false,
                                true,
                                true
                        ));
                        if (owner != null) {
                            ElementReactionManager.onElementDamage(serverLevel, target, owner, ElementType.ICE, finalDamage);
                        }
                    }
                    case FIRE -> {
                        target.setRemainingFireTicks(40); // 2秒燃烧
                        if (owner != null) {
                            ElementReactionManager.onElementDamage(serverLevel, target, owner, ElementType.FIRE, finalDamage);
                        }
                    }
                    case LIGHTNING -> {
                        if (owner != null) {
                            ElementReactionManager.onElementDamage(serverLevel, target, owner, ElementType.LIGHTNING, finalDamage);
                        }
                    }
                }
            }
        }
    }

    /**
     * 创建地面持续伤害效果
     * 
     * @param serverLevel 服务器世界
     * @param center      中心位置
     */
    private void createGroundEffect(ServerLevel serverLevel, Vec3 center) {
        // 创建持续伤害区域
        int duration = GROUND_EFFECT_DURATION;
        float groundDamage = calculateGroundDamage();
        ArrowType arrowType = getArrowType();
        LivingEntity owner = getOwner() instanceof LivingEntity ? (LivingEntity) getOwner() : null;

        // 使用服务器任务调度器来创建持续效果
        for (int i = 0; i < duration / GROUND_EFFECT_INTERVAL; i++) {
            final int tick = i * GROUND_EFFECT_INTERVAL;
            serverLevel.getServer().tell(new net.minecraft.server.TickTask(
                    serverLevel.getServer().getTickCount() + tick,
                    () -> {
                        if (!this.isRemoved()) {
                            applyGroundDamage(serverLevel, center, groundDamage, arrowType, owner);
                        }
                    }
            ));
        }
    }

    /**
     * 应用地面持续伤害
     * 
     * @param serverLevel 服务器世界
     * @param center      中心位置
     * @param damage      伤害值
     * @param arrowType   元素类型
     * @param owner       所有者
     */
    private void applyGroundDamage(ServerLevel serverLevel, Vec3 center, float damage, ArrowType arrowType, LivingEntity owner) {
        // 播放粒子效果
        Vector3f color = arrowType.getColor();
        
        switch (arrowType) {
            case ICE -> {
                serverLevel.sendParticles(
                        ParticleRegistry.SNOWFLAKE_PARTICLE.get(),
                        center.x, center.y + 0.1, center.z,
                        3,
                        AREA_RADIUS * 0.2, 0.1, AREA_RADIUS * 0.2,
                        0.05
                );
            }
            case FIRE -> {
                serverLevel.sendParticles(
                        ParticleRegistry.FIRE_PARTICLE.get(),
                        center.x, center.y + 0.1, center.z,
                        4,
                        AREA_RADIUS * 0.2, 0.1, AREA_RADIUS * 0.2,
                        0.05
                );
            }
            case LIGHTNING -> {
                serverLevel.sendParticles(
                        ParticleRegistry.ELECTRICITY_PARTICLE.get(),
                        center.x, center.y + 0.1, center.z,
                        5,
                        AREA_RADIUS * 0.2, 0.1, AREA_RADIUS * 0.2,
                        0.1
                );
            }
        }

        // 对范围内敌人造成伤害
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                new net.minecraft.world.phys.AABB(
                        center.x - AREA_RADIUS, center.y - 1, center.z - AREA_RADIUS,
                        center.x + AREA_RADIUS, center.y + 2, center.z + AREA_RADIUS
                ),
                entity -> entity.isAlive() 
                        && entity != getOwner()
                        && !DamageSources.isFriendlyFireBetween(entity, getOwner())
        );

        DamageSource damageSource = createDamageSource(serverLevel, owner);

        for (LivingEntity target : targets) {
            double distance = target.position().distanceTo(center);
            if (distance <= AREA_RADIUS) {
                // 距离衰减
                float finalDamage = damage * (1.0f - (float) distance / AREA_RADIUS * 0.5f);
                target.hurt(damageSource, finalDamage);

                // 施加元素标记
                if (owner != null) {
                    ElementReactionManager.onElementDamage(serverLevel, target, owner, arrowType.getElementType(), finalDamage);
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
        return serverLevel.damageSources().magic();
    }

    /**
     * 初始化元素箭的拖尾特效
     * 根据不同的元素类型创建不同样式的拖尾
     */
    private void initializeArrowTrail() {
        if (trailInitialized) return;

        // 生成唯一ID - 使用UUID确保每个投射物都有独立的拖尾
        String trailId = "elemental_arrow_" + this.getUUID().toString();

        // 根据元素类型创建对应拖尾
        SimpleTrailManager manager = SimpleTrailManager.getInstance();
        ArrowType arrowType = getArrowType();

        switch (arrowType) {
            case ICE -> {
                // 冰箭：冰元素拖尾
                arrowTrail = manager.createIceElementTrail(trailId, this);
            }

            case FIRE -> {
                // 火箭：火元素拖尾
                arrowTrail = manager.createFireElementTrail(trailId, this);
            }

            case LIGHTNING -> {
                // 雷箭：雷元素拖尾
                arrowTrail = manager.createLightningElementTrail(trailId, this);
            }
        }

        trailInitialized = true;

        if (com.legendarymage.legendarymagemod.Config.ELEMENTAL_BARRAGE_DEBUG_OUTPUT.get()) {
            com.legendarymage.legendarymagemod.LegendaryMage.LOGGER.debug(
                    "[元素箭-投射物] 初始化拖尾: {} | 类型: {}",
                    trailId, arrowType.getName()
            );
        }
    }

    /**
     * 清理拖尾特效资源
     * 在投射物销毁时调用，防止内存泄漏
     */
    private void cleanupTrail() {
        if (arrowTrail != null) {
            // 停止拖尾效果（让剩余的点自然淡出）
            if (arrowTrail.isActive()) {
                arrowTrail.stop();
            }

            if (com.legendarymage.legendarymagemod.Config.ELEMENTAL_BARRAGE_DEBUG_OUTPUT.get()) {
                com.legendarymage.legendarymagemod.LegendaryMage.LOGGER.debug(
                        "[元素箭-投射物] 清理拖尾: {}", arrowTrail.getId()
                );
            }

            arrowTrail = null;
        }
    }

    /**
     * 轨迹粒子效果（客户端每tick调用）
     * 集成简单拖尾特效API
     */
    @Override
    public void trailParticles() {
        Level level = level();
        ArrowType arrowType = getArrowType();
        Vector3f color = arrowType.getColor();

        // ========== 集成简单拖尾特效 ==========
        // 仅在客户端且拖尾已初始化时更新拖尾
        if (level.isClientSide()) {
            // 延迟初始化拖尾
            if (!trailInitialized) {
                initializeArrowTrail();
            }

            // SimpleTrailEffect 使用自动更新机制，不需要手动添加点
            // 更新在 SimpleTrailClientHandler 中通过 onRenderLevel 事件处理
        }

        // ========== 原有粒子效果（保留不变） ==========
        // 根据元素类型显示不同轨迹
        switch (arrowType) {
            case ICE -> {
                level.addParticle(
                        ParticleRegistry.SNOWFLAKE_PARTICLE.get(),
                        getX(), getY(), getZ(),
                        0, 0, 0
                );
            }
            case FIRE -> {
                level.addParticle(
                        ParticleRegistry.FIRE_PARTICLE.get(),
                        getX(), getY(), getZ(),
                        0, 0.02, 0
                );
            }
            case LIGHTNING -> {
                level.addParticle(
                        ParticleRegistry.ELECTRICITY_PARTICLE.get(),
                        getX(), getY(), getZ(),
                        (Math.random() - 0.5) * 0.1,
                        (Math.random() - 0.5) * 0.1,
                        (Math.random() - 0.5) * 0.1
                );
            }
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
        // 击中粒子效果在 triggerAreaDamage 中处理
    }

    /**
     * 获取投射物速度
     * 
     * @return 速度
     */
    @Override
    public float getSpeed() {
        return 3.5f; // 增加速度，确保箭矢能快速飞行
    }

    /**
     * 获取击中音效
     * 
     * @return 击中音效
     */
    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return switch (getArrowType()) {
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
        if (tickCount > 200) { // 10秒
            discard();
        }
    }
}
