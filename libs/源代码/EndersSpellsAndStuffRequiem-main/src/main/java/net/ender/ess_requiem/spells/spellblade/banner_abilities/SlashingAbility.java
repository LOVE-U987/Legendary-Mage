package net.ender.ess_requiem.spells.spellblade.banner_abilities;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.network.particles.ShockwaveParticlesPacket;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import net.ender.ess_requiem.EndersSpellsAndStuffRequiem;
import net.ender.ess_requiem.entity.spells.spellblade_cut.SpellbladeCutProjectile;
import net.ender.ess_requiem.registries.GGSchoolRegistry;
import net.ender.ess_requiem.registries.GGSoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;


public class SlashingAbility extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(EndersSpellsAndStuffRequiem.MOD_ID, "slashing_ability");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {

        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(getRadius(spellLevel, caster), 2))
        );

    }


    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(GGSchoolRegistry.BLADE_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(0)
            .build();

    public SlashingAbility() {
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 15;
        this.spellPowerPerLevel = 0;
        this.castTime = 20;
        this.baseManaCost = 0;
    }


    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public int getEffectiveCastTime(int spellLevel, @Nullable LivingEntity entity) {
        //due to animation timing, we do not want cast time attribute to affect this spell
        return getCastTime(spellLevel);
    }


    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(GGSoundRegistry.CURSED_REVIVE.get());
    }


    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        float radius = getRadius(spellLevel, entity);


        MagicManager.spawnParticles(level, new BlastwaveParticleOptions(GGSchoolRegistry.SPELLBLADE.get().getTargetingColor(), radius), entity.getX(), entity.getY() + .165f, entity.getZ(), 1, 0, 0, 0, 0, true);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new ShockwaveParticlesPacket(new Vec3(entity.getX(), entity.getY() + .165f, entity.getZ()), radius, ParticleTypes.END_ROD));
        level.getEntities(entity, entity.getBoundingBox().inflate(radius, 4, radius), (target) -> !DamageSources.isFriendlyFireBetween(target, entity) && Utils.hasLineOfSight(level, entity, target, false)).forEach(target -> {
            if (target instanceof LivingEntity livingEntity && livingEntity.distanceToSqr(entity) < radius * radius) {
                DamageSources.applyDamage(target, getDamage(spellLevel, entity), getDamageSource(entity));

                int count = 10;
                float damage = getDamage(spellLevel, entity);
                Vec3 center = target.position().add(0, target.getEyeHeight() / 2, 0);
                float degreesPerNeedle = 360f / count;
                for (int i = 0; i < count; i++) {
                    Vec3 offset = new Vec3(0, Math.random(), .55).normalize().scale(target.getBbWidth() + 2.75f).yRot(degreesPerNeedle * i * Mth.DEG_TO_RAD);
                    Vec3 spawn = center.add(offset);
                    Vec3 motion = center.subtract(spawn).normalize();

                    SpellbladeCutProjectile cut = new SpellbladeCutProjectile(level, entity);
                    cut.moveTo(spawn);
                    cut.shoot(motion.scale(.35f));
                    cut.setDamage(damage);
                    cut.setScale(1.2f);
                    level.addFreshEntity(cut);
                }
            }


        });

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }


    private float getDamage(int spellLevel, LivingEntity entity)
    {
        float damage = getDamageForAttribute(this, entity, spellLevel, AttributeRegistry.SUMMON_DAMAGE, 1);

        return damage;
    }

    public static float getDamageForAttribute(SlashingAbility spell, LivingEntity entity, int spellLevel, DeferredHolder<Attribute, Attribute> attr1, float modifier)
    {
        double attrValue1;
        if(entity != null) {
            attrValue1 = entity.getAttributeValue(AttributeRegistry.SUMMON_DAMAGE);
        }else{
            attrValue1 = 1;
        }

        float damage = (float) (modifier * (spell.getSpellPower(spellLevel, entity) + attrValue1));

        return damage;
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.PREPARE_CROSS_ARMS;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.CAST_T_POSE;
    }

    public float getRadius(int spellPower, LivingEntity caster) {
        return 18;
    }

    @Override
    public boolean canBeCraftedBy(Player player) {
        return false;
    }

    @Override
    public boolean allowCrafting() {
        return false;
    }

    @Override
    public boolean allowLooting() {
        return false;
    }

}











