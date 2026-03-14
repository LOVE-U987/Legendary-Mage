package net.ender.ess_requiem.compat.dte.compat_spells.eldritch;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.events.SpellSummonEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.*;
import net.acetheeldritchking.aces_spell_utils.spells.ASSpellAnimations;
import net.ender.ess_requiem.EndersSpellsAndStuffRequiem;
import net.ender.ess_requiem.compat.dte.dte_registry.DTE_EffectRegistry;
import net.ender.ess_requiem.entity.mobs.nightmare.NightmareEntity;
import net.ender.ess_requiem.registries.GGEffectRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;




public class NightmareSummonSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(EndersSpellsAndStuffRequiem.MOD_ID, "nightmare");


    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.ELDRITCH_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(180)
            .build();

    public NightmareSummonSpell() {
        this.manaCostPerLevel = 40;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 5;
        this.castTime = 0;
        this.baseManaCost = 250;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
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
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        return Utils.preCastTargetHelper(level, entity, playerMagicData, this, 30, .35f);
    }


    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData targetData) {
            var targetEntity = targetData.getTarget((ServerLevel) world);
            {
                if (targetEntity.hasEffect(DTE_EffectRegistry.BLISSFUL_SLEEP)) {
                    targetEntity.removeEffect(DTE_EffectRegistry.BLISSFUL_SLEEP);
                    int summonTime = 20 * 60 * 10;
                    SummonedEntitiesCastData summonedEntitiesCastData = new SummonedEntitiesCastData();

                    NightmareEntity nightmare = new NightmareEntity(world, entity);

                    nightmare.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(targetEntity.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
                    nightmare.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(targetEntity.getAttributeBaseValue(Attributes.KNOCKBACK_RESISTANCE));
                    nightmare.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(targetEntity.getAttributeBaseValue(Attributes.ARMOR_TOUGHNESS));
                    nightmare.getAttribute(Attributes.MAX_HEALTH).setBaseValue(targetEntity.getAttributeBaseValue(Attributes.MAX_HEALTH));
                    nightmare.getAttribute(Attributes.ARMOR).setBaseValue(targetEntity.getAttributeBaseValue(Attributes.ARMOR));
                    nightmare.getAttribute(Attributes.SCALE).setBaseValue(targetEntity.getAttributeBaseValue(Attributes.SCALE));
                    nightmare.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(targetEntity.getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
                    nightmare.getAttribute(AttributeRegistry.SPELL_POWER).setBaseValue(targetEntity.getAttributeBaseValue(AttributeRegistry.SPELL_POWER));
                    nightmare.getAttribute(AttributeRegistry.ELDRITCH_SPELL_POWER).setBaseValue(targetEntity.getAttributeBaseValue(AttributeRegistry.ELDRITCH_SPELL_POWER));
                    nightmare.setHealth((float) nightmare.getAttributeValue(Attributes.MAX_HEALTH));

                    nightmare.moveTo(entity.position());
                    nightmare.finalizeSpawn((ServerLevel) world, world.getCurrentDifficultyAt(nightmare.getOnPos()), MobSpawnType.MOB_SUMMONED, null);
                    var creature = NeoForge.EVENT_BUS.post(new SpellSummonEvent<>(entity, nightmare, this.spellId, spellLevel)).getCreature();
                    world.addFreshEntity(creature);
                    SummonManager.initSummon(entity, creature, summonTime, summonedEntitiesCastData);


                }
                else if (entity instanceof ServerPlayer serverPlayer) {
                    serverPlayer.displayClientMessage(Component.literal(ChatFormatting.ITALIC + "This entity is too awake, for their dreams to be realized.")
                            .withStyle(s -> s.withColor(ChatFormatting.DARK_RED)), true);
                }

                super.onCast(world, spellLevel, entity, castSource, playerMagicData);
            }

        }

    }




    @Override
    public AnimationHolder getCastStartAnimation() {
        return ASSpellAnimations.ANIMATION_CLAP;
    }
}
