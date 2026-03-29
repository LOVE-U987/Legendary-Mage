package com.legendarymage.legendarymagemod.event;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.joml.Vector3f;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.effect.EnderMarkEffect;
import com.legendarymage.legendarymagemod.effect.HolyMarkEffect;
import com.legendarymage.legendarymagemod.spell.MagicShotgunManager;

import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.particle.ShockwaveParticleOptions;
import io.redspace.ironsspellbooks.particle.SparkParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

/**
 * 魔法散弹事件处理器
 * 处理与魔法散弹相关的所有事件：
 * 1. 法术吟唱时注入法术（当玩家有魔法散弹Buff时）
 * 2. 近战攻击时释放注入的法术
 * 3. 使用咒刃流派风格的粒子效果
 * 4. 增强的法术注入特效和释放动画
 *
 * @author Love_U
 * @version 0.0.5
 */
@EventBusSubscriber(modid = LegendaryMage.MODID)
public class MagicShotgunEvents {

    /**
     * 存储玩家即将造成的近战伤害
     * 用于在AttackEntityEvent中记录伤害，在LivingDamageEvent中使用
     */
    private static final Map<UUID, Float> pendingMeleeDamages = new HashMap<>();

    // ==================== 咒刃流派颜色定义 ====================

    /**
     * 咒刃主题色 - 幽魂青（Sculk Soul颜色）
     */
    private static final Vector3f BLADE_SCULK_CYAN = new Vector3f(0.0f, 0.9f, 0.9f);

    /**
     * 咒刃主题色 - 暗紫黑
     */
    private static final Vector3f BLADE_DARK_PURPLE = new Vector3f(0.15f, 0.0f, 0.3f);

    /**
     * 咒刃主题色 - 亮紫青
     */
    private static final Vector3f BLADE_BRIGHT_CYAN = new Vector3f(0.3f, 0.9f, 1.0f);

    /**
     * 法术预施放事件
     * 当玩家有魔法散弹Buff时，阻止法术正常释放，改为注入法术
     *
     * @param event 法术预施放事件
     */
    @SubscribeEvent
    public static void onSpellPreCast(SpellPreCastEvent event) {
        Player player = event.getEntity();
        if (player == null || player.level().isClientSide()) {
            return;
        }

        // 检查是否是魔法散弹法术本身
        String spellId = event.getSpellId();
        if (spellId.equals("legendarymage:magic_shotgun")) {
            // 魔法散弹法术正常施放，不需要注入
            return;
        }

        // 检查玩家是否有魔法散弹Buff
        if (!MagicShotgunManager.hasBuff(player)) {
            return; // 没有Buff，正常施放法术
        }

        // 获取法术信息
        int spellLevel = event.getSpellLevel();
        CastSource castSource = event.getCastSource();

        // 从spellId获取法术实例来计算法力消耗
        ResourceLocation spellResource = ResourceLocation.parse(spellId);
        AbstractSpell spell = SpellRegistry.REGISTRY.get(spellResource);
        int manaCost = 100; // 默认蓝耗
        if (spell != null) {
            manaCost = spell.getManaCost(spellLevel);
        }

        // 获取玩家魔法数据
        MagicData magicData = MagicData.getPlayerMagicData(player);
        
        // 检查玩家是否有足够的法力值
        if (magicData.getMana() < manaCost) {
            // 法力不足，取消施法
            event.setCanceled(true);
            return;
        }

        // 取消正常施法，改为注入法术
        event.setCanceled(true);

        // 消耗法力值
        magicData.addMana(-manaCost);
        // 注意：addMana会自动同步到客户端，不需要手动调用syncToClient

        // 注入法术
        MagicShotgunManager.injectSpell(player, spellResource, spellLevel, manaCost, castSource);

        // 播放增强的注入特效
        playEnhancedInjectEffect((ServerLevel) player.level(), player);
    }

    /**
     * 播放增强的法术注入效果
     * 包含多层粒子效果、冲击波、能量漩涡
     *
     * @param level  服务器世界
     * @param player 玩家
     */
    private static void playEnhancedInjectEffect(ServerLevel level, Player player) {
        Vec3 pos = player.position();
        double x = pos.x;
        double y = pos.y;
        double z = pos.z;

        // 1. 核心能量爆发 - 大型冲击波（1个）
        level.sendParticles(
                new ShockwaveParticleOptions(BLADE_BRIGHT_CYAN, 2.5f, true),
                x, y + 0.5, z,
                1, 0, 0, 0, 0
        );

        // 2. 能量内环 - 快速旋转的火花（8个）
        for (int i = 0; i < 8; i++) {
            double angle = (i / 8.0) * Math.PI * 2 + level.getGameTime() * 0.5;
            double radius = 0.8;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

            level.sendParticles(
                    new SparkParticleOptions(BLADE_SCULK_CYAN),
                    x + offsetX, y + 0.8, z + offsetZ,
                    1, 0, 0.1, 0, 0.02
            );
        }

        // 3. 能量外环 - 稍大的旋转粒子（12个）
        for (int i = 0; i < 12; i++) {
            double angle = (i / 12.0) * Math.PI * 2 - level.getGameTime() * 0.3;
            double radius = 1.5;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;

            level.sendParticles(
                    ParticleTypes.ENCHANT,
                    x + offsetX, y + 0.6 + Math.sin(level.getGameTime() * 0.2) * 0.2, z + offsetZ,
                    1, 0, 0.02, 0, 0.01
            );
        }

        // 4. 幽魂灵魂粒子 - 咒刃标志性效果（6个）
        level.sendParticles(
                ParticleTypes.SCULK_SOUL,
                x, y + 1.0, z,
                6, 0.4, 0.4, 0.4, 0.03
        );

        // 5. 向上飘散的灵魂粒子（5个）
        for (int i = 0; i < 5; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 0.6;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.6;
            level.sendParticles(
                    ParticleTypes.SOUL,
                    x + offsetX, y + 0.5, z + offsetZ,
                    1, 0, 0.15, 0, 0.02
            );
        }

        // 6. 能量爆炸波 - 向外扩散（1个）
        level.sendParticles(
                new BlastwaveParticleOptions(BLADE_DARK_PURPLE, 2.0f),
                x, y + 0.2, z,
                1, 0, 0, 0, 0
        );

        // 7. 魔法符文 - 从地面升起（6个）
        for (int i = 0; i < 6; i++) {
            double angle = (i / 6.0) * Math.PI * 2;
            double radius = 1.2;
            double startX = x + Math.cos(angle) * radius;
            double startZ = z + Math.sin(angle) * radius;

            level.sendParticles(
                    ParticleTypes.WITCH,
                    startX, y + 0.1, startZ,
                    1, 0, 0.08, 0, 0.015
            );
        }

        // 8. 中心能量核心（3个）
        level.sendParticles(
                ParticleTypes.END_ROD,
                x, y + 1.2, z,
                3, 0.1, 0.1, 0.1, 0.02
        );

        // 播放音效组合
        // 主音效 - 能量注入
        level.playSound(
                null,
                x, y, z,
                SoundEvents.ENCHANTMENT_TABLE_USE,
                net.minecraft.sounds.SoundSource.PLAYERS,
                0.8f,
                0.6f + level.random.nextFloat() * 0.2f
        );

        // 次音效 - 幽魂共鸣
        level.playSound(
                null,
                x, y, z,
                SoundEvents.SCULK_CLICKING,
                net.minecraft.sounds.SoundSource.PLAYERS,
                0.6f,
                0.7f + level.random.nextFloat() * 0.3f
        );

        // 第三音效 - 灵魂吸收
        level.playSound(
                null,
                x, y, z,
                SoundEvents.SOUL_ESCAPE,
                net.minecraft.sounds.SoundSource.PLAYERS,
                0.5f,
                0.5f + level.random.nextFloat() * 0.2f
        );
    }

    /**
     * 实体攻击事件
     * 当玩家有魔法散弹Buff并且有注入的法术时，记录近战伤害并播放动画
     *
     * @param event 攻击实体事件
     */
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player == null || player.level().isClientSide()) {
            return;
        }

        // 检查玩家是否有魔法散弹Buff
        if (!MagicShotgunManager.hasBuff(player)) {
            return;
        }

        // 检查是否有注入的法术
        if (!MagicShotgunManager.hasInjectedSpell(player)) {
            return;
        }

        // 记录这次攻击，等待LivingDamageEvent来获取实际伤害
        pendingMeleeDamages.put(player.getUUID(), 0f);

        // 播放注入释放动画（仅在客户端播放）
        if (player.level().isClientSide()) {
            playInjectReleaseAnimation(player);
        }
    }

    /**
     * 播放法术注入释放动画
     * 使用Iron's Spells的动画系统
     *
     * @param player 玩家
     */
    private static void playInjectReleaseAnimation(Player player) {
        // 使用横扫攻击动画作为注入法术的释放动作
        // 这个动画会在玩家攻击时播放，表示注入的法术被释放
        // 参数：持续时间(10 ticks)，攻击范围(1.5倍)，使用的物品(主手物品)
        player.startAutoSpinAttack(10, 1.5f, player.getMainHandItem());
    }

    /**
     * 生物受伤事件
     * 处理魔法散弹的伤害计算和法术释放
     * 同时处理末影异常的回响打击
     *
     * @param event 生物受伤事件
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        DamageSource source = event.getSource();
        LivingEntity target = event.getEntity();

        // 检查伤害来源是否是玩家
        if (!(source.getEntity() instanceof Player player)) {
            return;
        }

        // 检查是否是客户端
        if (player.level().isClientSide()) {
            return;
        }

        // 获取实际造成的伤害
        float actualDamage = event.getNewDamage();

        // 处理3级光明标记的神圣打击（近战触发）
        // 当目标受到近战伤害时，如果目标有3级光明标记，触发神圣打击
        HolyMarkEffect.tryTriggerHolyStrike(target);

        // 处理末影异常的回响打击（3级末影标记有50%几率触发）
        // 注意：这需要在魔法散弹处理之前，因为两者可以共存
        EnderMarkEffect.tryTriggerEchoStrike(player, target, actualDamage);

        // 检查玩家是否有待处理的魔法散弹攻击
        if (!pendingMeleeDamages.containsKey(player.getUUID())) {
            return;
        }

        // 检查玩家是否有魔法散弹Buff
        if (!MagicShotgunManager.hasBuff(player)) {
            pendingMeleeDamages.remove(player.getUUID());
            return;
        }

        // 检查是否有注入的法术
        if (!MagicShotgunManager.hasInjectedSpell(player)) {
            pendingMeleeDamages.remove(player.getUUID());
            return;
        }

        // 释放注入的法术
        boolean released = MagicShotgunManager.releaseInjectedSpell(player, actualDamage, target);

        if (released) {
            // 播放释放特效
            playEnhancedReleaseEffect((ServerLevel) player.level(), player, target);
        }

        // 清除待处理标记
        pendingMeleeDamages.remove(player.getUUID());
    }

    /**
     * 播放增强的法术释放效果
     * 包含能量爆发、冲击波、粒子风暴
     *
     * @param level  服务器世界
     * @param player 玩家
     * @param target 被攻击目标
     */
    private static void playEnhancedReleaseEffect(ServerLevel level, Player player, LivingEntity target) {
        Vec3 playerPos = player.position();
        Vec3 targetPos = target.position();
        double px = playerPos.x;
        double py = playerPos.y;
        double pz = playerPos.z;
        double tx = targetPos.x;
        double ty = targetPos.y;
        double tz = targetPos.z;

        // 1. 玩家位置的能量爆发 - 法术从武器释放（1个大型冲击波）
        level.sendParticles(
                new ShockwaveParticleOptions(BLADE_BRIGHT_CYAN, 2.0f, true),
                px, py + 1, pz,
                1, 0, 0, 0, 0
        );

        // 2. 目标位置的大型冲击波 - 法术命中（1个）
        level.sendParticles(
                new ShockwaveParticleOptions(BLADE_SCULK_CYAN, 3.0f, true),
                tx, ty + target.getBbHeight() / 2, tz,
                1, 0, 0, 0, 0
        );

        // 3. 能量轨迹 - 从玩家到目标的能量流（5个）
        int steps = 6;
        for (int i = 1; i < steps; i++) {
            double ratio = i / (double) steps;
            double lx = px + (tx - px) * ratio;
            double ly = py + 1 + (ty + target.getBbHeight() / 2 - py - 1) * ratio;
            double lz = pz + (tz - pz) * ratio;

            level.sendParticles(
                    ParticleTypes.END_ROD,
                    lx, ly, lz,
                    1, 0, 0, 0, 0
            );
        }

        // 4. 目标周围的幽魂爆发（8个）
        level.sendParticles(
                ParticleTypes.SCULK_SOUL,
                tx, ty + target.getBbHeight() / 2, tz,
                8, 0.4, 0.4, 0.4, 0.05
        );

        // 5. 灵魂粒子爆发 - 表示被注入的法术释放（6个）
        level.sendParticles(
                ParticleTypes.SOUL,
                tx, ty + 0.5, tz,
                6, 0.3, 0.25, 0.3, 0.04
        );

        // 6. 目标周围的能量火花（8个）
        for (int i = 0; i < 8; i++) {
            double angle = (i / 8.0) * Math.PI * 2;
            double offsetX = Math.cos(angle) * 0.7;
            double offsetZ = Math.sin(angle) * 0.7;

            level.sendParticles(
                    new SparkParticleOptions(BLADE_BRIGHT_CYAN),
                    tx + offsetX, ty + target.getBbHeight() / 2, tz + offsetZ,
                    1, 0, 0.1, 0, 0.03
            );
        }

        // 7. 爆炸效果（2个）
        level.sendParticles(
                ParticleTypes.EXPLOSION,
                tx, ty + 0.8, tz,
                2, 0.15, 0.15, 0.15, 0.02
        );

        // 8. 能量外溢 - 向外的粒子流（10个）
        for (int i = 0; i < 10; i++) {
            double angle = (i / 10.0) * Math.PI * 2;
            double velocityX = Math.cos(angle) * 0.1;
            double velocityZ = Math.sin(angle) * 0.1;

            level.sendParticles(
                    ParticleTypes.WITCH,
                    tx, ty + 1, tz,
                    1, velocityX, 0.05, velocityZ, 0.02
            );
        }

        // 播放音效组合
        // 主释放音效 - 幽魂尖啸
        level.playSound(
                null,
                tx, ty, tz,
                SoundEvents.SCULK_SHRIEKER_SHRIEK,
                net.minecraft.sounds.SoundSource.PLAYERS,
                0.5f,
                1.0f + level.random.nextFloat() * 0.3f
        );

        // 次音效 - 灵魂释放
        level.playSound(
                null,
                tx, ty, tz,
                SoundEvents.SOUL_ESCAPE,
                net.minecraft.sounds.SoundSource.PLAYERS,
                0.7f,
                0.7f + level.random.nextFloat() * 0.4f
        );

        // 第三音效 - 爆炸
        level.playSound(
                null,
                tx, ty, tz,
                SoundEvents.GENERIC_EXPLODE,
                net.minecraft.sounds.SoundSource.PLAYERS,
                0.5f,
                1.3f + level.random.nextFloat() * 0.2f
        );
    }
}
