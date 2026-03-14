package net.ender.ess_requiem.compat.dte.compat_spells.eldritch;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.acetheeldritchking.aces_spell_utils.spells.ASSpellAnimations;
import net.ender.ess_requiem.EndersSpellsAndStuffRequiem;
import net.ender.ess_requiem.compat.dte.dte_registry.DTE_EffectRegistry;
import net.ender.ess_requiem.entity.spells.eternal_battlefield.EternalBattlefield;
import net.ender.ess_requiem.registries.GGSoundRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class ForeverDreamingSpell extends AbstractSpell {

    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(EndersSpellsAndStuffRequiem.MOD_ID, "forever_dreaming");


    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getDuration(spellLevel, caster) * 3, 1)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.ELDRITCH_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(90)
            .build();

    public ForeverDreamingSpell() {
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 15;
        this.spellPowerPerLevel = 0;
        this.castTime = 10;
        this.baseManaCost = 95;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(GGSoundRegistry.MIND_GENERIC_CAST.get());
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
    public int getEffectiveCastTime(int spellLevel, @Nullable LivingEntity entity) {

        return getCastTime(spellLevel);
    }


    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        return Utils.preCastTargetHelper(level, entity, playerMagicData, this, 64, .55f);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData targetData) {
            var targetEntity = targetData.getTarget((ServerLevel) level);

            final float MAX_HEALTH = targetEntity.getMaxHealth();
            float baseHealth = targetEntity.getHealth();
            double percent = (baseHealth / MAX_HEALTH) * 100;

             if (percent <= 40)
             {
                targetEntity.addEffect(new MobEffectInstance(DTE_EffectRegistry.BLISSFUL_SLEEP, getDuration(spellLevel, entity)));
            }


            else {
                if (entity instanceof ServerPlayer serverPlayer) {
                    serverPlayer.displayClientMessage(Component.literal(ChatFormatting.ITALIC + "This entity is not injured enough to fall into slumber.")
                            .withStyle(s -> s.withColor(ChatFormatting.DARK_RED)), true);
                }
            }

        }

    }




    public int getDuration(int spellLevel, LivingEntity caster) {
        return (int) (getSpellPower(spellLevel, caster) * 3);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return ASSpellAnimations.ANIMATION_MALEVOLENT_HAND_SIGN;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return ASSpellAnimations.ANIMATION_MALEVOLENT_HAND_SIGN_RELEASE;
    }
}
