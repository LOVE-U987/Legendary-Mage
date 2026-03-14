package net.ender.ess_requiem.spells.spellblade;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.events.SpellSummonEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.capabilities.magic.SummonedEntitiesCastData;
import net.ender.ess_requiem.EndersSpellsAndStuffRequiem;
import net.ender.ess_requiem.entity.mobs.hopping_skull.HoppingSkullEntity;
import net.ender.ess_requiem.entity.mobs.skull_mass.SkullMassEntity;
import net.ender.ess_requiem.entity.mobs.summoned_weapon.SoulmasterSwordEntity;
import net.ender.ess_requiem.registries.GGSchoolRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;
import java.util.List;


public class SoulmasterSummonSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(EndersSpellsAndStuffRequiem.MOD_ID, "soulmaster_summon");


    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of((Component.translatable("ui.irons_spellbooks.damage", getDamage(spellLevel, caster))
        ));
    }

    public double getHealthBonus(int spellLevel, LivingEntity caster) {

        return (getSpellPower(spellLevel, caster) - 1) * .5;
    }


    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(GGSchoolRegistry.BLADE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(190)
            .build();

    public SoulmasterSummonSpell() {
        this.manaCostPerLevel = 6;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 5;
        this.castTime = 0;
        this.baseManaCost = 80;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
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
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return 2;
    }

    @Override
    public void onRecastFinished(ServerPlayer serverPlayer, RecastInstance recastInstance, RecastResult recastResult, ICastDataSerializable castDataSerializable) {
        if (SummonManager.recastFinishedHelper(serverPlayer, recastInstance, recastResult, castDataSerializable)) {
            super.onRecastFinished(serverPlayer, recastInstance, recastResult, castDataSerializable);
        }
    }

    @Override
    public ICastDataSerializable getEmptyCastData() {
        return new SummonedEntitiesCastData();
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        var recasts = playerMagicData.getPlayerRecasts();
        if (!recasts.hasRecastForSpell(this)) {
            AttributeModifier healthModifier = new AttributeModifier(IronsSpellbooks.id("spell_power_health_bonus"), getHealthBonus(spellLevel, entity), AttributeModifier.Operation.ADD_VALUE);
            AttributeModifier damageModifier = new AttributeModifier(IronsSpellbooks.id("spell_power_damage_bonus"), getDamage(spellLevel, entity), AttributeModifier.Operation.ADD_VALUE);
            int summonTime = 20 * 60 * 10;

            SummonedEntitiesCastData summonedEntitiesCastData = new SummonedEntitiesCastData();
            int count = 1;
            for (int i = 0; i < count; i++) {
                SoulmasterSwordEntity sword = new SoulmasterSwordEntity(world, entity);
                sword.moveTo(entity.getEyePosition().add(new Vec3(Utils.getRandomScaled(2), 1, Utils.getRandomScaled(2))));
                sword.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(damageModifier);
                sword.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(healthModifier);
                sword.setHealth(sword.getMaxHealth());
                sword.finalizeSpawn((ServerLevel) world, world.getCurrentDifficultyAt(sword.getOnPos()), MobSpawnType.MOB_SUMMONED, null);
                var creature = NeoForge.EVENT_BUS.post(new SpellSummonEvent<>(entity, sword, this.spellId, spellLevel)).getCreature();
                world.addFreshEntity(creature);
                SummonManager.initSummon(entity, creature, summonTime, summonedEntitiesCastData);
            }
            RecastInstance recastInstance = new RecastInstance(this.getSpellId(), spellLevel, getRecastCount(spellLevel, entity), summonTime, castSource, summonedEntitiesCastData);
            recasts.addRecast(recastInstance, playerMagicData);
        }
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);



    }




    private float getDamage(int spellLevel, LivingEntity entity) {
        return getWeaponDamage(entity);
    }

    private float getWeaponDamage(LivingEntity entity) {
        if (entity == null) {
            return 0;
        }
        float weaponDamage = Utils.getWeaponDamage(entity);
        var weaponItem = entity.getWeaponItem();
        if (!weaponItem.isEmpty() && weaponItem.has(DataComponents.ENCHANTMENTS)) {
            weaponDamage += Utils.processEnchantment(entity.level(), Enchantments.SHARPNESS, EnchantmentEffectComponents.DAMAGE, weaponItem.get(DataComponents.ENCHANTMENTS));
        }
        return weaponDamage;
    }



}






