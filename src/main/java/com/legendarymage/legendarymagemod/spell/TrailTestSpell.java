package com.legendarymage.legendarymagemod.spell;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.entity.ModEntities;
import com.legendarymage.legendarymagemod.entity.spell.TrailTestProjectile;
import com.legendarymage.legendarymagemod.school.ElementSchoolRegistry;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

/**
 * 拖尾特效测试法术
 * 专门用于验证和调试TrailEffect API的测试法术
 *
 * 【使用方式】
 * 1. 通过 /give 命令或创造模式获取此法术卷轴
 * 2. 对准任意方向施放
 * 3. 观察发射的投射物是否带有彩色拖尾效果
 *
 * 【预期效果】
 * 发射一个投射物，投射物身后会留下一条长长的、
 * 彩虹渐变的、发光的几何体拖尾轨迹。
 *
 * 【调试信息】
 * 如果启用了Config中的调试输出，
 * 控制台会显示详细的拖尾创建和更新日志。
 *
 * @author Love_U
 * @version 1.0.6 (测试专用)
 */
public class TrailTestSpell extends AbstractSpell {

    /**
     * 法术ID
     */
    public static final String SPELL_ID = "trail_test";

    /**
     * 法术图标资源位置
     */
    private static final ResourceLocation SPELL_ICON = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, "textures/gui/spell_icons/trail_test.png");

    /**
     * 法术资源位置
     */
    private static final ResourceLocation SPELL_RESOURCE = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, SPELL_ID);

    /**
     * 施法动画
     */
    private static final AnimationHolder CAST_ANIMATION = new AnimationHolder(
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "instant_cast"), true);

    /**
     * 施法音效
     */
    private static final Optional<SoundEvent> CAST_SOUND = Optional.of(SoundEvents.FIREWORK_ROCKET_BLAST);

    /**
     * 构造函数
     */
    public TrailTestSpell() {
        this.baseManaCost = 0;      // 0蓝耗（测试用）
        this.manaCostPerLevel = 0;
        this.castTime = 0;          // 瞬发
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 0;
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
                .setMinRarity(SpellRarity.COMMON)   // 普通（容易获得）
                .setMaxLevel(1)                       // 只有1级（简化）
                .setCooldownSeconds(0)               // 无冷却
                .setAllowCrafting(true);             // 允许合成
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
     * @return 元素流派
     */
    @Override
    public SchoolType getSchoolType() {
        return ElementSchoolRegistry.ELEMENT.get();
    }

    /**
     * 获取最大等级
     *
     * @return 最大等级
     */
    @Override
    public int getMaxLevel() {
        return 1;
    }

    /**
     * 获取最小等级
     *
     * @return 最小等级
     */
    @Override
    public int getMinLevel() {
        return 1;
    }

    /**
     * 获取蓝耗
     *
     * @param level 法术等级
     * @return 蓝耗（始终为0）
     */
    @Override
    public int getManaCost(int level) {
        return 0; // 测试法术不消耗蓝量
    }

    /**
     * 获取施法音效
     *
     * @return 施法音效
     */
    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return CAST_SOUND;
    }

    /**
     * 获取施法动画
     *
     * @return 施法动画
     */
    @Override
    public AnimationHolder getCastStartAnimation() {
        return CAST_ANIMATION;
    }

    /**
     * 施法逻辑
     * 发射一个测试用的拖尾投射物
     *
     * @param level       世界
     * @param spellLevel  法术等级
     * @param entity      施法实体
     * @param castSource  施法来源
     * @param magicData   魔法数据
     */
    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData magicData) {
        if (!(level instanceof ServerLevel serverLevel)) {
            super.onCast(level, spellLevel, entity, castSource, magicData);
            return;
        }

        com.legendarymage.legendarymagemod.ModLogger.spell("========================================");
        com.legendarymage.legendarymagemod.ModLogger.spell("[拖尾测试] 开始施放拖尾测试法术！");
        LegendaryMage.LOGGER.info("[拖尾测试] 施法者: {}", entity.getName().getString());
        LegendaryMage.LOGGER.info("[拖尾测试] 世界类型: {}", level.isClientSide() ? "客户端" : "服务器");
        com.legendarymage.legendarymagemod.ModLogger.spell("========================================");

        // 计算发射位置（从眼睛位置）
        Vec3 eyePos = entity.getEyePosition(1.0f);
        Vec3 lookVec = entity.getLookAngle();

        // 创建测试投射物
        TrailTestProjectile projectile = new TrailTestProjectile(serverLevel, entity);

        // 设置位置
        projectile.setPos(eyePos.x, eyePos.y, eyePos.z);

        // 设置速度（朝向视线方向，速度适中以便观察）
        double speed = 1.2f; // 较慢的速度便于观察拖尾
        Vec3 velocity = lookVec.normalize().scale(speed);

        projectile.setDeltaMovement(velocity);

        // 添加到世界
        serverLevel.addFreshEntity(projectile);

       LegendaryMage.LOGGER.info("[拖尾测试] 投射物已生成！ID: {}", projectile.getId());
        LegendaryMage.LOGGER.info("[拖尾测试] 初始位置: ({}, {}, {})",
                String.format("%.2f", eyePos.x),
                String.format("%.2f", eyePos.y),
                String.format("%.2f", eyePos.z));
        LegendaryMage.LOGGER.info("[拖尾测试] 飞行方向: ({}, {}, {})",
                String.format("%.2f", velocity.x),
                String.format("%.2f", velocity.y),
                String.format("%.2f", velocity.z));
        com.legendarymage.legendarymagemod.ModLogger.spell("========================================");

        super.onCast(level, spellLevel, entity, castSource, magicData);
    }

    /**
     * 获取独特信息（显示在法术书中）
     *
     * @param level  法术等级
     * @param entity 实体
     * @return 独特信息列表
     */
    @Override
    public List<MutableComponent> getUniqueInfo(int level, LivingEntity entity) {
        return List.of(
                Component.translatable("spell.legendarymage.trail_test.purpose"),
                Component.translatable("spell.legendarymage.trail_test.effect"),
                Component.translatable("spell.legendarymage.trail_test.debug")
        );
    }
}
