package net.ender.ess_requiem.spells.spellblade.banner_abilities;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.events.SpellSummonEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.capabilities.magic.SummonedEntitiesCastData;
import net.ender.ess_requiem.EndersSpellsAndStuffRequiem;
import net.ender.ess_requiem.entity.mobs.gilded_weapon.GildedWeaponEntity;
import net.ender.ess_requiem.registries.GGSchoolRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;


public class GildedSwordSummon extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(EndersSpellsAndStuffRequiem.MOD_ID, "gild_summon");


    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of((Component.translatable("ui.irons_spellbooks.damage", getDamage(spellLevel, caster))
        ));
    }




    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(GGSchoolRegistry.BLADE_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(0)
            .build();

    public GildedSwordSummon() {
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 0;
        this.castTime = 18;
        this.baseManaCost = 0;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }


    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }




    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {

        int summonTime = 20 * 60 * 10;
        SummonedEntitiesCastData summonedEntitiesCastData = new SummonedEntitiesCastData();
        GildedWeaponEntity weapon = new GildedWeaponEntity(world, entity);
        weapon.moveTo(entity.getEyePosition().add(new Vec3(Utils.getRandomScaled(2), 1, Utils.getRandomScaled(2))));
        weapon.finalizeSpawn((ServerLevel) world, world.getCurrentDifficultyAt(weapon.getOnPos()), MobSpawnType.MOB_SUMMONED, null);
        var creature = NeoForge.EVENT_BUS.post(new SpellSummonEvent<>(entity, weapon, this.spellId, spellLevel)).getCreature();
        world.addFreshEntity(creature);
        SummonManager.initSummon(entity, creature, summonTime, summonedEntitiesCastData);


        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    private float getDamage(int spellLevel, LivingEntity entity) {
        return 15;
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
