package com.legendarymage.legendarymagemod.spell;

import com.legendarymage.legendarymagemod.Config;
import com.legendarymage.legendarymagemod.entity.ModEntities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

/**
 * 活体冰雕实体
 * 由冰雕转化而来的召唤物，使用玩家模型，具有冰雕纹理
 * 会像宠物一样跟随主人并攻击主人攻击的目标
 * 
 * @author Love_U
 * @version 0.0.2
 */
public class LivingIceSculptureEntity extends PathfinderMob implements OwnableEntity {

    /**
     * 实体数据同步键 - 所有者UUID
     */
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID = SynchedEntityData.defineId(
            LivingIceSculptureEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    /**
     * 实体数据同步键 - 是否正在破碎
     */
    private static final EntityDataAccessor<Boolean> DATA_SHATTERING = SynchedEntityData.defineId(
            LivingIceSculptureEntity.class, EntityDataSerializers.BOOLEAN);

    /**
     * 实体数据同步键 - 彩蛋纹理索引（0=默认, 1=彩蛋1, 2=彩蛋2）
     */
    private static final EntityDataAccessor<Integer> DATA_EASTER_EGG_TEXTURE_INDEX = SynchedEntityData.defineId(
            LivingIceSculptureEntity.class, EntityDataSerializers.INT);

    /**
     * 实体数据同步键 - 是否处于坐下状态
     * true=坐下（原地待命），false=跟随主人
     */
    private static final EntityDataAccessor<Boolean> DATA_ORDERED_TO_SIT = SynchedEntityData.defineId(
            LivingIceSculptureEntity.class, EntityDataSerializers.BOOLEAN);

    /**
     * 基础生命值
     */
    private static final double BASE_MAX_HEALTH = 100.0;

    /**
     * 基础移动速度
     */
    private static final double BASE_MOVEMENT_SPEED = 0.3;

    /**
     * 基础攻击力
     */
    private static final double BASE_ATTACK_DAMAGE = 10.0;

    /**
     * 获取生命周期（tick）
     */
    private static int getLifetimeTicks() {
        return Config.LIVING_ICE_SCULPTURE_ENTITY_LIFETIME_TICKS.get();
    }

    /**
     * 召唤者引用（客户端缓存）
     */
    @Nullable
    private LivingEntity owner;

    /**
     * 生命周期剩余tick数
     */
    private int lifetimeTicksRemaining;

    /**
     * 构造函数
     * 
     * @param entityType 实体类型
     * @param level      世界
     */
    public LivingIceSculptureEntity(EntityType<? extends LivingIceSculptureEntity> entityType, Level level) {
        super(entityType, level);
        this.setNoAi(false);
    }

    /**
     * 构造函数（带召唤者）
     * 
     * @param level  世界
     * @param owner  召唤者
     */
    public LivingIceSculptureEntity(Level level, @Nullable LivingEntity owner) {
        this(ModEntities.ICE_SCULPTURE.get(), level);
        if (owner != null) {
            this.setOwnerUUID(owner.getUUID());
            this.owner = owner;
        }
        // 初始化生命周期
        this.lifetimeTicksRemaining = getLifetimeTicks();
    }

    /**
     * 定义实体数据同步
     */
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_OWNER_UUID, Optional.empty());
        builder.define(DATA_SHATTERING, false);
        builder.define(DATA_EASTER_EGG_TEXTURE_INDEX, 0);  // 默认使用默认纹理
        builder.define(DATA_ORDERED_TO_SIT, false);  // 默认跟随主人
    }

    /**
     * 注册实体属性
     * 
     * @return 属性供应器
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, BASE_MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, BASE_MOVEMENT_SPEED)
                .add(Attributes.ATTACK_DAMAGE, BASE_ATTACK_DAMAGE)
                .add(Attributes.ATTACK_SPEED, 1.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    /**
     * 注册AI目标 - 宠物式攻击模式
     */
    @Override
    protected void registerGoals() {
        super.registerGoals();

        // 优先级1：如果主人攻击了某个目标，攻击该目标
        this.targetSelector.addGoal(1, new OwnerHurtTargetGoal(this));

        // 优先级2：如果主人被攻击，反击攻击者
        this.targetSelector.addGoal(2, new OwnerHurtByTargetGoal(this));

        // 优先级3：攻击最近的敌对生物（当主人没有指定目标时）
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, true));

        // 优先级4：被攻击时反击
        this.targetSelector.addGoal(4, new HurtByTargetGoal(this));

        // 行为目标
        // 优先级1：坐下时原地待命（最高优先级，覆盖其他移动行为）
        this.goalSelector.addGoal(0, new SitGoal(this));

        // 优先级2：攻击目标
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, true));

        // 优先级3：跟随主人（坐下时不会激活）
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.0, 10.0, 2.0));

        // 优先级4：随机移动（坐下时不会移动）
        this.goalSelector.addGoal(3, new ConditionalRandomStrollGoal(this, 0.8));

        // 优先级5：看向目标
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    /**
     * 主人攻击目标时，冰雕也攻击该目标
     */
    private static class OwnerHurtTargetGoal extends TargetGoal {
        private final LivingIceSculptureEntity entity;
        private LivingEntity ownerLastHurt;
        private int timestamp;

        public OwnerHurtTargetGoal(LivingIceSculptureEntity entity) {
            super(entity, false);
            this.entity = entity;
        }

        @Override
        public boolean canUse() {
            LivingEntity owner = this.entity.getOwner();
            if (owner == null) {
                return false;
            }
            this.ownerLastHurt = owner.getLastHurtMob();
            int i = owner.getLastHurtMobTimestamp();
            return i != this.timestamp && this.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT);
        }

        @Override
        public void start() {
            this.mob.setTarget(this.ownerLastHurt);
            LivingEntity owner = this.entity.getOwner();
            if (owner != null) {
                this.timestamp = owner.getLastHurtMobTimestamp();
            }
            super.start();
        }
    }

    /**
     * 主人被攻击时，冰雕反击攻击者
     */
    private static class OwnerHurtByTargetGoal extends TargetGoal {
        private final LivingIceSculptureEntity entity;
        private LivingEntity ownerLastHurtBy;
        private int timestamp;

        public OwnerHurtByTargetGoal(LivingIceSculptureEntity entity) {
            super(entity, false);
            this.entity = entity;
        }

        @Override
        public boolean canUse() {
            LivingEntity owner = this.entity.getOwner();
            if (owner == null) {
                return false;
            }
            this.ownerLastHurtBy = owner.getLastHurtByMob();
            int i = owner.getLastHurtByMobTimestamp();
            return i != this.timestamp && this.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT);
        }

        @Override
        public void start() {
            this.mob.setTarget(this.ownerLastHurtBy);
            LivingEntity owner = this.entity.getOwner();
            if (owner != null) {
                this.timestamp = owner.getLastHurtByMobTimestamp();
            }
            super.start();
        }
    }

    /**
     * 设置所有者UUID
     * 
     * @param uuid 所有者UUID
     */
    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_OWNER_UUID, Optional.ofNullable(uuid));
    }

    /**
     * 获取所有者UUID
     * 
     * @return 所有者UUID
     */
    @Override
    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNER_UUID).orElse(null);
    }

    /**
     * 获取所有者
     * 
     * @return 所有者实体
     */
    @Override
    @Nullable
    public LivingEntity getOwner() {
        if (this.owner == null && this.getOwnerUUID() != null) {
            if (this.level() instanceof ServerLevel serverLevel) {
                Entity entity = serverLevel.getEntity(this.getOwnerUUID());
                if (entity instanceof LivingEntity livingEntity) {
                    this.owner = livingEntity;
                }
            }
        }
        return this.owner;
    }

    /**
     * 设置召唤者
     * 
     * @param summoner 召唤者
     */
    public void setSummoner(@Nullable LivingEntity summoner) {
        if (summoner != null) {
            this.setOwnerUUID(summoner.getUUID());
            this.owner = summoner;
        }
    }

    /**
     * 检查是否为所有者
     * 
     * @param entity 实体
     * @return 是否为所有者
     */
    public boolean isOwnedBy(LivingEntity entity) {
        return entity.getUUID().equals(this.getOwnerUUID());
    }

    /**
     * 设置彩蛋纹理索引
     * 
     * @param index 纹理索引（0=默认, 1=彩蛋1, 2=彩蛋2）
     */
    public void setEasterEggTextureIndex(int index) {
        this.entityData.set(DATA_EASTER_EGG_TEXTURE_INDEX, index);
    }

    /**
     * 获取彩蛋纹理索引
     * 
     * @return 纹理索引（0=默认, 1=彩蛋1, 2=彩蛋2）
     */
    public int getEasterEggTextureIndex() {
        return this.entityData.get(DATA_EASTER_EGG_TEXTURE_INDEX);
    }

    /**
     * 是否可以被所有者伤害
     * 
     * @param source 伤害来源
     * @param amount 伤害值
     * @return 是否可以被伤害
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 检查伤害来源是否为所有者
        if (source.getEntity() instanceof LivingEntity livingEntity) {
            if (this.isOwnedBy(livingEntity)) {
                // 所有者不能伤害召唤物
                return false;
            }
        }
        return super.hurt(source, amount);
    }

    /**
     * 实体Tick更新
     */
    @Override
    public void tick() {
        super.tick();

        // 服务器端处理
        if (!this.level().isClientSide) {
            // 生命周期倒计时
            if (this.lifetimeTicksRemaining > 0) {
                this.lifetimeTicksRemaining--;
                
                // 播放环境粒子效果（每2秒一次）
                if (this.lifetimeTicksRemaining % 40 == 0) {
                    IceSculptureParticles.playUnfrozenAmbientEffect((ServerLevel) this.level(), this.position());
                }
            } else {
                // 生命周期结束，开始破碎
                this.startShattering();
                return;
            }

            // 检查所有者是否在线/存活
            if (this.tickCount % 20 == 0) {
                LivingEntity owner = this.getOwner();
                if (owner == null || !owner.isAlive()) {
                    // 所有者不在或死亡，冰雕生物开始破碎
                    this.startShattering();
                }
            }
        }
    }

    /**
     * 开始破碎（自然消失）
     */
    private void startShattering() {
        this.entityData.set(DATA_SHATTERING, true);

        // 播放破碎效果
        if (this.level() instanceof ServerLevel serverLevel) {
            IceSculptureParticles.playEntityShatterEffect(serverLevel, this.position());
        }

        // 播放破碎音效
        this.playSound(SoundEvents.GLASS_BREAK, 1.0f, 0.8f);

        // 移除实体
        this.discard();
    }

    /**
     * 死亡处理
     */
    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        // 播放死亡效果
        if (this.level() instanceof ServerLevel serverLevel) {
            IceSculptureParticles.playEntityDeathEffect(serverLevel, this.position());
        }

        // 播放死亡音效
        this.playSound(SoundEvents.GLASS_BREAK, 1.0f, 0.6f);
    }

    /**
     * 获取死亡音效
     * 
     * @return 死亡音效
     */
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GLASS_BREAK;
    }

    /**
     * 获取受伤音效
     * 
     * @return 受伤音效
     */
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.GLASS_HIT;
    }

    /**
     * 获取环境音效
     * 
     * @return 环境音效
     */
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PLAYER_HURT_FREEZE;
    }

    /**
     * 保存实体数据到NBT
     * 
     * @param compound NBT标签
     */
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.getOwnerUUID() != null) {
            compound.putUUID("Owner", this.getOwnerUUID());
        }
        compound.putInt("LifetimeTicks", this.lifetimeTicksRemaining);
        compound.putInt("EasterEggTextureIndex", this.getEasterEggTextureIndex());
        compound.putBoolean("OrderedToSit", this.isOrderedToSit());
    }

    /**
     * 从NBT读取实体数据
     * 
     * @param compound NBT标签
     */
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID("Owner")) {
            this.setOwnerUUID(compound.getUUID("Owner"));
        }
        if (compound.contains("LifetimeTicks")) {
            this.lifetimeTicksRemaining = compound.getInt("LifetimeTicks");
        }
        if (compound.contains("EasterEggTextureIndex")) {
            this.setEasterEggTextureIndex(compound.getInt("EasterEggTextureIndex"));
        }
        if (compound.contains("OrderedToSit")) {
            this.setOrderedToSit(compound.getBoolean("OrderedToSit"));
        }
    }

    /**
     * 是否掉落战利品
     * 
     * @return 是否掉落战利品
     */
    @Override
    protected boolean shouldDropLoot() {
        return false;  // 召唤物不掉落战利品
    }

    /**
     * 是否掉落经验
     * 
     * @return 是否掉落经验
     */
    @Override
    public boolean shouldDropExperience() {
        return false;  // 召唤物不掉落经验
    }

    /**
     * 是否可以被 leash
     * 
     * @return 是否可以被 leash
     */
    @Override
    public boolean canBeLeashed() {
        return false;  // 召唤物不能被 leash
    }

    /**
     * 获取坐下状态
     * 
     * @return 是否处于坐下状态
     */
    public boolean isOrderedToSit() {
        return this.entityData.get(DATA_ORDERED_TO_SIT);
    }

    /**
     * 设置坐下状态
     * 
     * @param sitting true=坐下（原地待命），false=跟随主人
     */
    public void setOrderedToSit(boolean sitting) {
        this.entityData.set(DATA_ORDERED_TO_SIT, sitting);
    }

    /**
     * 玩家与冰雕交互
     * 主人右键点击时切换跟随/坐下状态
     * 
     * @param player 交互的玩家
     * @param hand   交互的手
     * @return 交互结果
     */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        // 只有主人可以切换状态
        if (!this.isOwnedBy(player)) {
            return InteractionResult.PASS;
        }

        // 只响应主手的空手交互
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        // 切换坐下/跟随状态
        boolean wasSitting = this.isOrderedToSit();
        this.setOrderedToSit(!wasSitting);

        // 播放交互音效
        this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.6f, wasSitting ? 1.2f : 0.8f);

        // 发送状态提示消息
        if (!this.level().isClientSide) {
            if (wasSitting) {
                player.displayClientMessage(
                    Component.translatable("entity.legendarymage.ice_sculpture.follow"), true);
            } else {
                player.displayClientMessage(
                    Component.translatable("entity.legendarymage.ice_sculpture.sit"), true);
            }
        }

        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    /**
     * 是否免疫火焰伤害
     * 
     * @return 是否免疫火焰
     */
    @Override
    public boolean fireImmune() {
        return false;  // 冰雕生物不免疫火焰（冰怕火）
    }

    /**
     * 跟随主人目标类（宠物AI）
     */
    private static class FollowOwnerGoal extends Goal {
        private final LivingIceSculptureEntity entity;
        private final double speedModifier;
        private final double stopDistance;
        private final double startDistance;
        private LivingEntity owner;
        private int timeToRecalcPath;

        public FollowOwnerGoal(LivingIceSculptureEntity entity, double speedModifier, double startDistance, double stopDistance) {
            this.entity = entity;
            this.speedModifier = speedModifier;
            this.startDistance = startDistance;
            this.stopDistance = stopDistance;
        }

        @Override
        public boolean canUse() {
            if (this.entity.isOrderedToSit()) {
                return false;
            }
            LivingEntity owner = this.entity.getOwner();
            if (owner == null) {
                return false;
            }
            if (owner.distanceToSqr(this.entity) < this.startDistance * this.startDistance) {
                return false;
            }
            this.owner = owner;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.entity.getNavigation().isDone()) {
                return false;
            }
            if (this.owner == null || !this.owner.isAlive()) {
                return false;
            }
            return this.entity.distanceToSqr(this.owner) > this.stopDistance * this.stopDistance;
        }

        @Override
        public void start() {
            this.timeToRecalcPath = 0;
        }

        @Override
        public void stop() {
            this.owner = null;
            this.entity.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.owner != null && --this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = 10;
                this.entity.getNavigation().moveTo(this.owner, this.speedModifier);
            }
        }
    }

    /**
     * 坐下待命目标
     * 当冰雕被命令坐下时，停止所有导航和移动，原地待命
     */
    private static class SitGoal extends Goal {
        private final LivingIceSculptureEntity entity;

        public SitGoal(LivingIceSculptureEntity entity) {
            this.entity = entity;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return this.entity.isOrderedToSit() && !this.entity.isInWater();
        }

        @Override
        public boolean canContinueToUse() {
            return this.entity.isOrderedToSit();
        }

        @Override
        public void start() {
            this.entity.getNavigation().stop();
            this.entity.setTarget(null);
        }
    }

    /**
     * 带坐下检查的随机移动目标
     * 坐下时不会进行随机移动
     */
    private static class ConditionalRandomStrollGoal extends RandomStrollGoal {
        private final LivingIceSculptureEntity entity;

        public ConditionalRandomStrollGoal(LivingIceSculptureEntity entity, double speedModifier) {
            super(entity, speedModifier);
            this.entity = entity;
        }

        @Override
        public boolean canUse() {
            if (this.entity.isOrderedToSit()) {
                return false;
            }
            return super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            if (this.entity.isOrderedToSit()) {
                return false;
            }
            return super.canContinueToUse();
        }
    }
}
