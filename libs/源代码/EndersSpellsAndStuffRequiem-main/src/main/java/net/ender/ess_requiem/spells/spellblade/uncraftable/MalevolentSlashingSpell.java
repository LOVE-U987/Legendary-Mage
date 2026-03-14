package net.ender.ess_requiem.spells.spellblade.uncraftable;


import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.events.SpellSummonEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.capabilities.magic.SummonedEntitiesCastData;

import io.redspace.ironsspellbooks.registries.SoundRegistry;

import net.ender.ess_requiem.EndersSpellsAndStuffRequiem;
import net.ender.ess_requiem.entity.mobs.battle_standard.BattleStandardEntity;

import net.ender.ess_requiem.registries.GGEffectRegistry;
import net.ender.ess_requiem.registries.GGSchoolRegistry;

import net.ender.ess_requiem.registries.GGSoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;

import net.minecraft.world.effect.MobEffectInstance;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import net.minecraft.world.level.Level;

import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;

import java.util.List;
import java.util.Optional;


public class MalevolentSlashingSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(EndersSpellsAndStuffRequiem.MOD_ID, "place_standard");
    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.percent_health", (int) (100 + getHealthBonus(spellLevel, caster) * 100))
        );
    }

    public double getHealthBonus(int spellLevel, LivingEntity caster) {

        return (getSpellPower(spellLevel, caster) - 1) * .5;
    }


    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(GGSchoolRegistry.BLADE_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(300)
            .build();

    public MalevolentSlashingSpell() {
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = 25;
        this.baseManaCost = 550;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(GGSoundRegistry.BANNER_SUMMON.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.DARK_SPELL_02.get());
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public boolean canBeInterrupted(@Nullable Player player) {
        return false;
    }



    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        AttributeModifier healthModifier = new AttributeModifier(IronsSpellbooks.id("spell_power_health_bonus"), getHealthBonus(spellLevel, entity), AttributeModifier.Operation.ADD_VALUE);

        int summonTime = 20 * 60 * 10;
        entity.addEffect(new MobEffectInstance(GGEffectRegistry.BANNER_PROTECTION, 2400));

        SummonedEntitiesCastData summonedEntitiesCastData = new SummonedEntitiesCastData();


        BattleStandardEntity weapon = new BattleStandardEntity(world, entity);
        weapon.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(healthModifier);
        weapon.setHealth(weapon.getMaxHealth());
        weapon.addEffect(new MobEffectInstance(GGEffectRegistry.BANNER_PROTECTION, 10000));
        weapon.moveTo(entity.getEyePosition().add(new Vec3(Utils.getRandomScaled(2), 1, Utils.getRandomScaled(2))));
        weapon.finalizeSpawn((ServerLevel) world, world.getCurrentDifficultyAt(weapon.getOnPos()), MobSpawnType.MOB_SUMMONED, null);
        var creature = NeoForge.EVENT_BUS.post(new SpellSummonEvent<>(entity, weapon, this.spellId, spellLevel)).getCreature();
        world.addFreshEntity(creature);
        SummonManager.initSummon(entity, creature, summonTime, summonedEntitiesCastData);


        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }


    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CAST_KNEELING_PRAYER;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.FINISH_ANIMATION;
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
