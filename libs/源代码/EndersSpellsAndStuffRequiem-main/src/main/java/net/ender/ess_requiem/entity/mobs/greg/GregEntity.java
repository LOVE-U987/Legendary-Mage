package net.ender.ess_requiem.entity.mobs.greg;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.goals.*;
import net.acetheeldritchking.aces_spell_utils.entity.mobs.UniqueAbstractSpellCastingMob;
import net.ender.ess_requiem.entity.mobs.battle_standard.BattleStandardEntity;
import net.ender.ess_requiem.registries.GGAttributeRegistry;
import net.ender.ess_requiem.registries.GGEntityRegistry;
import net.ender.ess_requiem.registries.GGSpellRegistry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class GregEntity extends UniqueAbstractSpellCastingMob {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GregEntity(Level level, LivingEntity owner) {
        this(GGEntityRegistry.GREG.get(), level);

    }
    public GregEntity(EntityType<? extends AbstractSpellCastingMob> entityType, Level world) {
        super(entityType, world);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
        xpReward = 5;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pSpawnType, @org.jetbrains.annotations.Nullable SpawnGroupData pSpawnGroupData) {

        return super.finalizeSpawn(pLevel, pDifficulty, pSpawnType, pSpawnGroupData);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_DAMAGE, 0)
                .add(Attributes.MAX_HEALTH, 5)
                .add(Attributes.FOLLOW_RANGE, 0)
                .add(Attributes.ENTITY_INTERACTION_RANGE, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1);

    }

    @Override
    public void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
    }


    @Override
    public void die(DamageSource pDamageSource) {

        super.die(pDamageSource);
    }


    //Sounds and Stuff
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.STEM_PLACE;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.STEM_BREAK;
    }


    RawAnimation animationToPlay = null;
    private final AnimationController<GregEntity> animationController = new AnimationController<>(this, "controller", 0, this::predicate);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(animationController);


    }
    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> tAnimationState) {
        tAnimationState.getController().setAnimation(RawAnimation.begin().then("idle", Animation.LoopType.LOOP));
        return PlayState.CONTINUE;
    }


    @Override
    public boolean isAnimating() {
        return animationController.getAnimationState() != AnimationController.State.STOPPED || super.isAnimating();
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public double getTick(Object object) {
        return this.tickCount;
    }

}
