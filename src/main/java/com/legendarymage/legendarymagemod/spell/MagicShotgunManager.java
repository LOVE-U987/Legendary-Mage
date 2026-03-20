package com.legendarymage.legendarymagemod.spell;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.effect.MagicShotgunBuffEffect;
import com.legendarymage.legendarymagemod.effect.ModEffects;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 魔法散弹管理器
 * 负责管理玩家注入的法术，处理法术存储、释放和伤害计算
 * 
 * 核心功能：
 * 1. 存储玩家当前注入的法术
 * 2. 在玩家近战攻击时释放注入的法术
 * 3. 计算基于近战伤害和法力值的特殊伤害公式
 * 
 * @author Love_U
 * @version 1.0.0
 */
public class MagicShotgunManager {

    /**
     * 存储每个玩家注入的法术数据
     * 键：玩家UUID
     * 值：注入的法术数据
     */
    private static final Map<UUID, InjectedSpellData> injectedSpells = new HashMap<>();

    /**
     * 存储每个玩家上次近战攻击造成的伤害
     * 用于计算法术伤害
     */
    private static final Map<UUID, Float> lastMeleeDamages = new HashMap<>();

    /**
     * Buff持续时间（tick）
     * 1分钟 = 60秒 = 1200 tick
     */
    public static final int BUFF_DURATION_TICKS = 1200;

    /**
     * 注入法术数据类
     * 存储注入的法术信息
     */
    public static class InjectedSpellData {
        /**
         * 法术ID
         */
        private final ResourceLocation spellId;

        /**
         * 法术等级
         */
        private final int spellLevel;

        /**
         * 法术法力消耗
         */
        private final int manaCost;

        /**
         * 施法来源
         */
        private final CastSource castSource;

        /**
         * 构造函数
         * 
         * @param spellId    法术ID
         * @param spellLevel 法术等级
         * @param manaCost   法力消耗
         * @param castSource 施法来源
         */
        public InjectedSpellData(ResourceLocation spellId, int spellLevel, int manaCost, CastSource castSource) {
            this.spellId = spellId;
            this.spellLevel = spellLevel;
            this.manaCost = manaCost;
            this.castSource = castSource;
        }

        /**
         * 获取法术ID
         * 
         * @return 法术ID
         */
        public ResourceLocation getSpellId() {
            return spellId;
        }

        /**
         * 获取法术等级
         * 
         * @return 法术等级
         */
        public int getSpellLevel() {
            return spellLevel;
        }

        /**
         * 获取法力消耗
         * 
         * @return 法力消耗
         */
        public int getManaCost() {
            return manaCost;
        }

        /**
         * 获取施法来源
         * 
         * @return 施法来源
         */
        public CastSource getCastSource() {
            return castSource;
        }
    }

    /**
     * 注入法术
     * 将法术存储到玩家的注入槽位中，会覆盖之前注入的法术
     * 
     * @param player     玩家
     * @param spellId    法术ID
     * @param spellLevel 法术等级
     * @param manaCost   法力消耗
     * @param castSource 施法来源
     */
    public static void injectSpell(Player player, ResourceLocation spellId, int spellLevel, int manaCost, CastSource castSource) {
        if (player == null) return;

        UUID playerId = player.getUUID();
        InjectedSpellData data = new InjectedSpellData(spellId, spellLevel, manaCost, castSource);
        injectedSpells.put(playerId, data);
    }

    /**
     * 注入法术（从SpellData）
     * 
     * @param player     玩家
     * @param spellData  法术数据
     * @param castSource 施法来源
     */
    public static void injectSpell(Player player, SpellData spellData, CastSource castSource) {
        if (player == null || spellData == null) return;

        AbstractSpell spell = spellData.getSpell();
        if (spell == null) return;

        int spellLevel = spellData.getLevel();
        int manaCost = spell.getManaCost(spellLevel);

        injectSpell(player, spell.getSpellResource(), spellLevel, manaCost, castSource);
    }

    /**
     * 释放注入的法术
     * 在玩家近战攻击时调用，释放注入的法术并计算伤害
     * 
     * @param player      玩家
     * @param meleeDamage 近战攻击造成的伤害
     * @param target      被攻击的目标
     * @return 是否成功释放法术
     */
    public static boolean releaseInjectedSpell(Player player, float meleeDamage, LivingEntity target) {
        if (player == null || player.level().isClientSide()) {
            return false;
        }

        UUID playerId = player.getUUID();
        InjectedSpellData data = injectedSpells.get(playerId);

        if (data == null) {
            return false; // 没有注入的法术
        }

        // 存储近战伤害用于伤害计算
        lastMeleeDamages.put(playerId, meleeDamage);

        // 获取魔法数据
        MagicData magicData = MagicData.getPlayerMagicData(player);

        // 释放法术
        // 注意：这里我们需要修改法术的伤害计算，所以使用自定义的释放逻辑
        castModifiedSpell(player, data, target, meleeDamage);

        // 消耗注入的法术
        injectedSpells.remove(playerId);
        lastMeleeDamages.remove(playerId);

        return true;
    }

    /**
     * 释放修改后的法术
     * 使用特殊的伤害计算公式：伤害 = 近战伤害 * 法力值 / 200
     * 
     * @param player      玩家
     * @param data        注入的法术数据
     * @param target      被攻击的目标
     * @param meleeDamage 近战伤害
     */
    private static void castModifiedSpell(Player player, InjectedSpellData data, 
                                          LivingEntity target, float meleeDamage) {
        Level level = player.level();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // 计算修改后的伤害
        // 公式：伤害 = 近战伤害 * 法力值 / 200
        float modifiedDamage = calculateModifiedDamage(meleeDamage, data.getManaCost());

        // 设置当前正在释放魔法散弹法术的玩家和伤害
        setCurrentMagicShotgunCaster(player, modifiedDamage);

        try {
            // 这里我们触发法术效果
            // 由于无法直接修改其他法术的伤害，我们使用一种简化的方式：
            // 对目标造成基于计算公式的额外伤害
            
            // 创建伤害源
            net.minecraft.world.damagesource.DamageSource damageSource = 
                level.damageSources().magic();
            
            // 对目标造成伤害
            target.hurt(damageSource, modifiedDamage);
            
            // 播放特效
            playReleaseEffect(serverLevel, player, target);
            
        } finally {
            // 清除标记
            clearCurrentMagicShotgunCaster(player);
        }
    }

    /**
     * 播放法术释放效果
     * 
     * @param level  服务器世界
     * @param player 玩家
     * @param target 目标
     */
    private static void playReleaseEffect(ServerLevel level, Player player, LivingEntity target) {
        // 播放音效
        level.playSound(
                null,
                target.getX(), target.getY(), target.getZ(),
                net.minecraft.sounds.SoundEvents.EVOKER_CAST_SPELL,
                net.minecraft.sounds.SoundSource.PLAYERS,
                1.0f,
                0.8f + level.random.nextFloat() * 0.4f
        );

        // 紫色粒子效果
        level.sendParticles(
                io.redspace.ironsspellbooks.registries.ParticleRegistry.WISP_PARTICLE.get(),
                target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                10, 0.2, 0.2, 0.2, 0.1
        );
    }

    /**
     * 计算修改后的伤害
     * 公式：伤害 = 近战伤害 * 法力值 / 200
     * 
     * @param meleeDamage 近战伤害
     * @param manaCost    法力消耗
     * @return 修改后的伤害
     */
    public static float calculateModifiedDamage(float meleeDamage, int manaCost) {
        return meleeDamage * manaCost / 200.0f;
    }

    // ==================== 当前施法者追踪 ====================

    /**
     * 存储当前正在释放魔法散弹法术的玩家
     */
    private static final Map<UUID, Float> currentMagicShotgunCasters = new HashMap<>();

    /**
     * 设置当前正在释放魔法散弹法术的玩家
     * 
     * @param player 玩家
     * @param damage 修改后的伤害
     */
    private static void setCurrentMagicShotgunCaster(Player player, float damage) {
        if (player != null) {
            currentMagicShotgunCasters.put(player.getUUID(), damage);
        }
    }

    /**
     * 清除当前正在释放魔法散弹法术的玩家
     * 
     * @param player 玩家
     */
    private static void clearCurrentMagicShotgunCaster(Player player) {
        if (player != null) {
            currentMagicShotgunCasters.remove(player.getUUID());
        }
    }

    /**
     * 检查玩家是否正在释放魔法散弹法术
     * 
     * @param player 玩家
     * @return 是否正在释放魔法散弹法术
     */
    public static boolean isMagicShotgunCaster(Player player) {
        return player != null && currentMagicShotgunCasters.containsKey(player.getUUID());
    }

    /**
     * 获取玩家当前的魔法散弹修改伤害
     * 
     * @param player 玩家
     * @return 修改后的伤害，如果不是魔法散弹施法者则返回-1
     */
    public static float getMagicShotgunDamage(Player player) {
        if (player == null) {
            return -1;
        }
        return currentMagicShotgunCasters.getOrDefault(player.getUUID(), -1f);
    }

    // ==================== Buff管理 ====================

    /**
     * 给玩家施加魔法散弹Buff
     * 
     * @param player      玩家
     * @param buffLevel   Buff等级（1-5）
     * @param isFirstCast 是否是第一次施法（第一段）
     */
    public static void applyBuff(Player player, int buffLevel, boolean isFirstCast) {
        if (player == null || player.level().isClientSide()) {
            return;
        }

        if (isFirstCast) {
            // 第一段施法：添加Buff
            Holder<net.minecraft.world.effect.MobEffect> buffHolder = ModEffects.MAGIC_SHOTGUN_BUFF;
            MobEffectInstance effectInstance = new MobEffectInstance(
                    buffHolder,
                    BUFF_DURATION_TICKS,
                    buffLevel - 1,  // amplifier是0-based
                    false,          // 不显示粒子
                    true,           // 显示图标
                    true            // 显示效果名称
            );
            player.addEffect(effectInstance);
            
            // 注意：最大法力值修饰符现在由Buff效果自己处理
            // MagicShotgunBuffEffect重写了getAttributeModifierValue方法来返回固定值
            // 但实际效果仍然需要手动调整，因为Minecraft会自动将修饰符值乘以(等级+1)
            adjustMaxManaModifier(player, buffLevel);
        } else {
            // 第二段施法：移除Buff
            removeBuffAndAttributes(player);
        }
    }
    
    /**
     * 调整最大法力值修饰符，确保实际效果为固定-30%（不随等级变化）
     * 
     * @param player 玩家
     * @param buffLevel Buff等级（1-5）
     */
    private static void adjustMaxManaModifier(Player player, int buffLevel) {
        var maxManaAttr = player.getAttribute(io.redspace.ironsspellbooks.api.registry.AttributeRegistry.MAX_MANA);
        if (maxManaAttr != null) {
            // 先移除由Buff效果自动添加的修饰符
            maxManaAttr.removeModifier(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                    LegendaryMage.MODID, "magic_shotgun_max_mana"));
            
            // 计算固定-30%需要的实际值
            // Minecraft会将修饰符值乘以(amplifier + 1)，即buffLevel
            // 所以我们需要计算：实际值 = 目标值 / buffLevel
            // 使用DISPLAY值作为基础，这样实际效果会正确计算
            double targetReduction = com.legendarymage.legendarymagemod.effect.MagicShotgunBuffEffect.MAX_MANA_REDUCTION_DISPLAY * com.legendarymage.legendarymagemod.effect.MagicShotgunBuffEffect.MAX_BUFF_LEVEL;
            double actualValue = targetReduction / buffLevel;
            
            // 添加调整后的修饰符
            AttributeModifier modifier = new AttributeModifier(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                            LegendaryMage.MODID, "magic_shotgun_max_mana"),
                    actualValue,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );
            maxManaAttr.addTransientModifier(modifier);
        }
    }
    
    /**
     * 移除Buff并清除所有相关属性
     * 手动清除属性修饰符，确保伤害计算正确恢复
     * 
     * @param player 玩家
     */
    private static void removeBuffAndAttributes(Player player) {
        if (player == null) return;
        
        // 移除Buff效果
        Holder<net.minecraft.world.effect.MobEffect> buffHolder = ModEffects.MAGIC_SHOTGUN_BUFF;
        player.removeEffect(buffHolder);
        
        // 手动清除属性修饰符（确保伤害计算正确）
        removeAllAttributes(player);

        // 同时清除注入的法术
        injectedSpells.remove(player.getUUID());
    }
    
    /**
     * 移除所有魔法散弹相关的属性修饰符
     * 
     * @param player 玩家
     */
    private static void removeAllAttributes(Player player) {
        // 法术强度属性
        var spellPowerAttr = player.getAttribute(io.redspace.ironsspellbooks.api.registry.AttributeRegistry.SPELL_POWER);
        if (spellPowerAttr != null) {
            spellPowerAttr.removeModifier(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                    LegendaryMage.MODID, "magic_shotgun_spell_power"));
        }
        
        // 法术吟唱缩减属性
        var castTimeAttr = player.getAttribute(io.redspace.ironsspellbooks.api.registry.AttributeRegistry.CAST_TIME_REDUCTION);
        if (castTimeAttr != null) {
            castTimeAttr.removeModifier(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                    LegendaryMage.MODID, "magic_shotgun_cast_time"));
        }
        
        // 近战伤害属性
        var attackDamageAttr = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        if (attackDamageAttr != null) {
            attackDamageAttr.removeModifier(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                    LegendaryMage.MODID, "magic_shotgun_melee_damage"));
        }
        
        // 最大法力值属性
        var maxManaAttr = player.getAttribute(io.redspace.ironsspellbooks.api.registry.AttributeRegistry.MAX_MANA);
        if (maxManaAttr != null) {
            maxManaAttr.removeModifier(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                    LegendaryMage.MODID, "magic_shotgun_max_mana"));
        }
    }

    /**
     * 检查玩家是否有魔法散弹Buff
     * 
     * @param player 玩家
     * @return 是否有Buff
     */
    public static boolean hasBuff(Player player) {
        if (player == null) {
            return false;
        }
        Holder<net.minecraft.world.effect.MobEffect> buffHolder = ModEffects.MAGIC_SHOTGUN_BUFF;
        return player.hasEffect(buffHolder);
    }

    /**
     * 获取玩家的魔法散弹Buff等级
     * 
     * @param player 玩家
     * @return Buff等级（1-5），如果没有Buff则返回0
     */
    public static int getBuffLevel(Player player) {
        if (player == null) {
            return 0;
        }

        Holder<net.minecraft.world.effect.MobEffect> buffHolder = ModEffects.MAGIC_SHOTGUN_BUFF;
        MobEffectInstance effect = player.getEffect(buffHolder);
        if (effect == null) {
            return 0;
        }

        // amplifier是0-based，所以+1
        return effect.getAmplifier() + 1;
    }

    /**
     * 检查玩家是否有注入的法术
     * 
     * @param player 玩家
     * @return 是否有注入的法术
     */
    public static boolean hasInjectedSpell(Player player) {
        if (player == null) {
            return false;
        }
        return injectedSpells.containsKey(player.getUUID());
    }

    /**
     * 获取玩家注入的法术数据
     * 
     * @param player 玩家
     * @return 注入的法术数据，如果没有则返回null
     */
    public static InjectedSpellData getInjectedSpell(Player player) {
        if (player == null) {
            return null;
        }
        return injectedSpells.get(player.getUUID());
    }

    /**
     * 清除玩家注入的法术
     * 
     * @param player 玩家
     */
    public static void clearInjectedSpell(Player player) {
        if (player != null) {
            injectedSpells.remove(player.getUUID());
        }
    }

    /**
     * 清除所有数据（用于世界卸载或调试）
     */
    public static void clearAll() {
        injectedSpells.clear();
        lastMeleeDamages.clear();
        currentMagicShotgunCasters.clear();
    }
}
