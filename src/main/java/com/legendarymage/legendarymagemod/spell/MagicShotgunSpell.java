package com.legendarymage.legendarymagemod.spell;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.particle.ShockwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.effect.MagicShotgunBuffEffect;
import com.legendarymage.legendarymagemod.effect.ModEffects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 魔法散弹法术
 * 咒刃流派的传奇法术
 * 
 * 作用：将法力注入武器，以近战的形式释放，代价是会丢失元素反应与部分法术强度
 * 
 * 数据：
 * - 瞬间释放的两段法术
 * - 第一段给与施法者"魔法散弹"Buff
 * - 第二段去除Buff
 * - 时长固定为1分钟
 * - 法术等级最低1级，最高5级
 * - 传奇品质，不可书写
 * - CD 2分钟
 * 
 * 每加一法术等级+1级Buff，时长不影响
 * 法术强度无法影响Buff等级与时长
 * 
 * 魔法散弹Buff属性（随等级提升）：
 * - 法术强度减少：-10%（每级-5%）
 * - 法术吟唱缩减：+15%（每级+5%）
 * - 近战伤害加成：+5点（每级+2点）
 * 
 * Buff机制：
 * - 该Buff持续期间，所有法术在吟唱结束后不会主动释放
 * - 当玩家使用近战武器攻击时，立刻释放注入的法术
 * - 当在为武器注入法术时，若武器里已有被注入的法术，将直接覆盖
 * - 该Buff会修改法术的伤害计算机制：伤害 = 玩家的近战伤害 * 注入法术的法力值 / 200
 * 
 * @author Love_U
 * @version 1.0.0
 */
public class MagicShotgunSpell extends AbstractSpell {

    /**
     * 法术ID
     */
    public static final String SPELL_ID = "magic_shotgun";

    /**
     * 基础蓝耗（固定100）
     */
    private static final int BASE_MANA_COST = 100;

    /**
     * 每级蓝耗增量（固定消耗，无增量）
     */
    private static final int MANA_COST_PER_LEVEL = 0;

    /**
     * 施法时间（tick）
     * 瞬间释放
     */
    private static final int CAST_TIME = 0;

    /**
     * 冷却时间（tick）
     * 1秒 = 20 tick
     */
    private static final int COOLDOWN_TICKS = 20;

    /**
     * 最小等级
     */
    private static final int MIN_LEVEL = 1;

    /**
     * 最大等级
     */
    private static final int MAX_LEVEL = 5;

    /**
     * 最小稀有度（传奇）
     */
    private static final int MIN_RARITY = SpellRarity.LEGENDARY.getValue();

    /**
     * 最大稀有度（传奇）
     */
    private static final int MAX_RARITY = SpellRarity.LEGENDARY.getValue();

    /**
     * 法术资源位置
     */
    private static final ResourceLocation SPELL_RESOURCE = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, SPELL_ID);

    /**
     * 法术图标路径
     */
    private static final ResourceLocation SPELL_ICON = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, "textures/gui/spell_icons/magic_shotgun.png");

    /**
     * 咒刃流派资源位置
     */
    private static final ResourceLocation BLADE_SCHOOL_RESOURCE = ResourceLocation.fromNamespaceAndPath(
            "ess_requiem", "blade");

    /**
     * 目标选择颜色（咒刃深紫色）
     */
    private static final Vector3f TARGETING_COLOR = new Vector3f(0.29f, 0.0f, 0.5f);

    /**
     * 粒子颜色（紫色）
     */
    private static final Vector3f PARTICLE_COLOR = new Vector3f(0.5f, 0.0f, 0.8f);

    /**
     * 施法开始音效
     */
    private static final Optional<SoundEvent> CAST_START_SOUND = Optional.of(SoundEvents.ENCHANTMENT_TABLE_USE);

    /**
     * 施法结束音效
     */
    private static final Optional<SoundEvent> CAST_FINISH_SOUND = Optional.of(SoundEvents.AMETHYST_BLOCK_CHIME);

    /**
     * 存储施法者是否是第一次施法的映射
     * 键：施法者UUID，值：是否是第一次施法
     */
    private static final Map<UUID, Boolean> casterFirstCastMap = new HashMap<>();

    /**
     * 构造函数
     */
    public MagicShotgunSpell() {
        this.baseManaCost = BASE_MANA_COST;
        this.manaCostPerLevel = MANA_COST_PER_LEVEL;
        this.castTime = CAST_TIME;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 0; // 法术强度不影响Buff
    }

    /**
     * 获取法术资源位置
     * 
     * @return 法术资源位置
     */
    @Override
    public ResourceLocation getSpellResource() {
        return SPELL_RESOURCE;
    }

    /**
     * 获取默认配置
     * 
     * @return 默认配置
     */
    @Override
    public DefaultConfig getDefaultConfig() {
        return new DefaultConfig()
                .setMinRarity(SpellRarity.LEGENDARY)
                .setMaxLevel(MAX_LEVEL)
                .setCooldownSeconds(1)  // 1秒
                .setAllowCrafting(false); // 不可书写
    }

    /**
     * 获取施法类型
     * 
     * @return 施法类型（瞬发）
     */
    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    /**
     * 获取法术流派
     * 
     * @return 咒刃流派
     */
    @Override
    public SchoolType getSchoolType() {
        // 尝试从 SchoolRegistry 获取咒刃流派
        // 由于咒刃是其他模组的流派，我们使用 holder 方式获取
        try {
            // 使用 BuiltInRegistries 获取已注册的流派
            for (Holder<SchoolType> schoolHolder : SchoolRegistry.REGISTRY.asLookup().listElements().toList()) {
                SchoolType school = schoolHolder.value();
                if (school.getId().equals(BLADE_SCHOOL_RESOURCE)) {
                    return school;
                }
            }
            // 如果找不到，记录日志
            LegendaryMage.LOGGER.warn("未找到咒刃流派：{}，将使用末影流派作为替代", BLADE_SCHOOL_RESOURCE);
        } catch (Exception e) {
            // 如果发生异常，记录日志
            LegendaryMage.LOGGER.error("获取咒刃流派时发生错误：{}", e.getMessage());
        }
        
        // 如果找不到咒刃流派，使用末影流派作为替代（同为暗色系）
        return SchoolRegistry.ENDER.get();
    }

    /**
     * 获取目标选择颜色
     * 
     * @return 目标选择颜色
     */
    @Override
    public Vector3f getTargetingColor() {
        return TARGETING_COLOR;
    }

    /**
     * 获取最小稀有度
     * 
     * @return 最小稀有度
     */
    @Override
    public int getMinRarity() {
        return MIN_RARITY;
    }

    /**
     * 获取最大等级
     * 
     * @return 最大等级
     */
    @Override
    public int getMaxLevel() {
        return MAX_LEVEL;
    }

    /**
     * 获取最小等级
     * 
     * @return 最小等级
     */
    @Override
    public int getMinLevel() {
        return MIN_LEVEL;
    }

    /**
     * 获取最大稀有度
     * 
     * @return 最大稀有度
     */
    @Override
    public int getMaxRarity() {
        return MAX_RARITY;
    }

    /**
     * 获取蓝耗
     * 
     * @param level 法术等级
     * @return 蓝耗
     */
    @Override
    public int getManaCost(int level) {
        return BASE_MANA_COST + (level - 1) * MANA_COST_PER_LEVEL;
    }

    /**
     * 获取冷却时间
     * 
     * @return 冷却时间（tick）
     */
    @Override
    public int getSpellCooldown() {
        return COOLDOWN_TICKS;
    }

    /**
     * 获取施法时间
     * 
     * @param level 法术等级
     * @return 施法时间（tick）
     */
    @Override
    public int getCastTime(int level) {
        return CAST_TIME;
    }

    /**
     * 获取施法开始音效
     * 
     * @return 施法开始音效
     */
    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return CAST_START_SOUND;
    }

    /**
     * 获取施法结束音效
     * 
     * @return 施法结束音效
     */
    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return CAST_FINISH_SOUND;
    }

    /**
     * 获取法术图标资源位置
     *
     * @return 法术图标资源位置
     */
    public ResourceLocation getSpellIcon() {
        return SPELL_ICON;
    }

    /**
     * 检查施法前条件
     * 判断是第一次还是第二次施法
     *
     * @param level      世界
     * @param spellLevel 法术等级
     * @param entity     施法实体
     * @param magicData  魔法数据
     * @return 是否满足施法条件
     */
    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData magicData) {
        if (entity instanceof Player player) {
            UUID casterId = entity.getUUID();
            
            // 检查玩家是否已经有Buff
            boolean hasBuff = MagicShotgunManager.hasBuff(player);
            
            // 如果是第一次施法（没有Buff），记录状态
            // 如果是第二次施法（有Buff），也记录状态
            casterFirstCastMap.put(casterId, !hasBuff);
        }
        
        return true;
    }

    /**
     * 施法逻辑
     * 两段式施法：第一段给予Buff，第二段移除Buff
     *
     * @param level       世界
     * @param spellLevel  法术等级
     * @param entity      施法实体
     * @param castSource  施法来源
     * @param magicData   魔法数据
     */
    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData magicData) {
        if (level instanceof ServerLevel serverLevel && entity instanceof Player player) {
            UUID casterId = entity.getUUID();
            
            // 获取是否是第一次施法
            Boolean isFirstCast = casterFirstCastMap.get(casterId);
            if (isFirstCast == null) {
                // 默认根据Buff状态判断
                isFirstCast = !MagicShotgunManager.hasBuff(player);
            }
            
            // Buff等级 = 法术等级
            int buffLevel = spellLevel;
            
            // 应用或移除Buff
            MagicShotgunManager.applyBuff(player, buffLevel, isFirstCast);
            
            // 播放效果
            if (isFirstCast) {
                playApplyEffect(serverLevel, player);
            } else {
                playRemoveEffect(serverLevel, player);
            }
            
            // 清理映射
            casterFirstCastMap.remove(casterId);
        }

        super.onCast(level, spellLevel, entity, castSource, magicData);
    }

    /**
     * 播放施加Buff的效果
     * 
     * @param level  服务器世界
     * @param player 玩家
     */
    private void playApplyEffect(ServerLevel level, Player player) {
        Vec3 pos = player.position();
        
        // 播放音效
        level.playSound(
                null,
                pos.x, pos.y, pos.z,
                SoundEvents.ENCHANTMENT_TABLE_USE,
                net.minecraft.sounds.SoundSource.PLAYERS,
                1.0f,
                0.8f + level.random.nextFloat() * 0.4f
        );
        
        // 紫色冲击波
        level.sendParticles(
                new ShockwaveParticleOptions(PARTICLE_COLOR, 1.5f, true),
                pos.x, pos.y + 0.5, pos.z,
                1, 0, 0, 0, 0
        );
        
        // 魔法粒子
        level.sendParticles(
                ParticleRegistry.WISP_PARTICLE.get(),
                pos.x, pos.y + 1, pos.z,
                20, 0.5, 0.5, 0.5, 0.1
        );
    }

    /**
     * 播放移除Buff的效果
     * 
     * @param level  服务器世界
     * @param player 玩家
     */
    private void playRemoveEffect(ServerLevel level, Player player) {
        Vec3 pos = player.position();
        
        // 播放音效
        level.playSound(
                null,
                pos.x, pos.y, pos.z,
                SoundEvents.AMETHYST_BLOCK_CHIME,
                net.minecraft.sounds.SoundSource.PLAYERS,
                1.0f,
                0.8f + level.random.nextFloat() * 0.4f
        );
        
        // 消散粒子
        level.sendParticles(
                ParticleRegistry.WISP_PARTICLE.get(),
                pos.x, pos.y + 1, pos.z,
                15, 0.3, 0.3, 0.3, 0.05
        );
    }

    /**
     * 获取独特信息（显示在法术书中）
     * 显示Buff的实际数值
     *
     * @param level  法术等级
     * @param entity 实体
     * @return 独特信息列表
     */
    @Override
    public List<MutableComponent> getUniqueInfo(int level, LivingEntity entity) {
        // Buff等级 = 法术等级
        int buffLevel = level;
        
        // 计算Buff属性
        double spellPowerReduction = MagicShotgunBuffEffect.calculateSpellPowerReduction(buffLevel) * 100;
        double castTimeReduction = MagicShotgunBuffEffect.calculateCastTimeReduction(buffLevel) * 100;
        double meleeDamageBonus = MagicShotgunBuffEffect.calculateMeleeDamageBonus(buffLevel);
        
        return List.of(
                Component.translatable("spell.legendarymage.magic_shotgun.buff_level", buffLevel),
                Component.translatable("spell.legendarymage.magic_shotgun.spell_power_reduction", String.format("%.0f", spellPowerReduction)),
                Component.translatable("spell.legendarymage.magic_shotgun.cast_time_reduction", String.format("%.0f", castTimeReduction)),
                Component.translatable("spell.legendarymage.magic_shotgun.melee_damage_bonus", String.format("%.0f", meleeDamageBonus)),
                Component.translatable("spell.legendarymage.magic_shotgun.max_mana_reduction")
        );
    }
}
