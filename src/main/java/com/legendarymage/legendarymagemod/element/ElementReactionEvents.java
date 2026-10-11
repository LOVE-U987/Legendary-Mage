package com.legendarymage.legendarymagemod.element;

import com.legendarymage.legendarymagemod.Config;
import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.ModLogger;
import com.legendarymage.legendarymagemod.command.ElementMappingHotReload;
import com.legendarymage.legendarymagemod.effect.ElementDamageGuard;
import com.legendarymage.legendarymagemod.effect.ModEffects;
import com.legendarymage.legendarymagemod.effect.EnderMarkEffect;
import com.legendarymage.legendarymagemod.effect.HolyMarkEffect;
import com.legendarymage.legendarymagemod.effect.IceMarkEffect;
import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.particle.ZapParticleOption;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.List;
import java.util.Random;

/**
 * 元素反应事件处理器
 * 负责监听各种事件并驱动元素异常系统
 *
 * 【重写 v3.1】
 * - 元素异常的给予判定由"对应流派"决定（不再根据伤害类型关键字匹配）
 * - 冰冻/神圣打击/回响打击等"攻击触发"效果仅在铁魔法法术命中时判定
 * - 黑暗异常 3 级以上每级 +5% 易伤（受伤倍率提升）
 * - 火焰异常死亡时按 buff 等级半径爆炸
 *
 * @author Love_U
 * @version 3.1.0
 */
@EventBusSubscriber(modid = LegendaryMage.MODID)
public class ElementReactionEvents {

    /**
     * 随机数生成器
     */
    private static final Random RANDOM = new Random();

    /**
     * 输出调试日志
     * 通过配置开关控制是否输出
     *
     * @param message 日志消息
     */
    private static void debugLog(String message) {
        if (Config.ELEMENT_REACTION_DEBUG_OUTPUT.get()) {
            ModLogger.element("[元素反应事件] {}", message);
        }
    }

    /**
     * 监听法术伤害事件（铁魔法模组）
     * 当实体受到铁魔法法术伤害时：
     * 1. 按法术流派施加/更新对应的元素异常（75%概率）
     * 2. 判定冰冻/神圣打击/回响打击（仅法术触发）
     *
     * @param event 法术伤害事件
     */
    @SubscribeEvent
    public static void onSpellDamage(SpellDamageEvent event) {
        // 只在服务器端处理
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity target = event.getEntity();
        SpellDamageSource spellDamageSource = event.getSpellDamageSource();
        float damage = event.getAmount();

        // 从法术伤害源获取攻击者
        LivingEntity attacker = null;
        if (spellDamageSource != null && spellDamageSource.getEntity() instanceof LivingEntity livingAttacker) {
            attacker = livingAttacker;
        }

        // 0. 天罚充能：施法者的下一次法术伤害 ×2（触发后消耗）
        float chargedMultiplier = consumeChargedCharge(attacker);
        if (chargedMultiplier > 1.0f) {
            event.setAmount(event.getAmount() * chargedMultiplier);
            damage = event.getAmount();
            debugLog(String.format("天罚充能触发: %s 的法术伤害 ×%.1f",
                    attacker.getName().getString(), chargedMultiplier));
        }

        // 获取法术流派
        io.redspace.ironsspellbooks.api.spells.SchoolType schoolType = null;
        if (spellDamageSource != null && spellDamageSource.spell() != null) {
            schoolType = spellDamageSource.spell().getSchoolType();
        }

        debugLog(String.format("收到铁魔法法术伤害事件: %s -> %s, 流派: %s, 伤害: %.1f",
                attacker != null ? attacker.getName().getString() : "未知",
                target.getName().getString(),
                schoolType != null ? schoolType.getId().getPath() : "未知",
                damage));

        // 1. 按流派施加元素异常
        if (schoolType != null) {
            if (isElementSchool(schoolType)) {
                // 元素流派法术：随机赋予冰、火、雷元素标记
                handleElementSchoolDamage(serverLevel, target, attacker, damage);
            } else {
                // 检查是否有自定义的元素标记映射（包括热加载的运行时映射）
                ResourceLocation schoolId = schoolType.getId();
                if (ElementMappingHotReload.hasMapping(schoolId)) {
                    // 使用自定义映射的元素标记（支持热加载）
                    handleCustomSchoolDamage(serverLevel, target, attacker, schoolId, damage);
                } else {
                    // 其他流派：按对应流派判定元素异常
                    ElementReactionManager.onSpellDamage(serverLevel, target, attacker, schoolType, damage);
                }
            }
        }

        // 2. 法术触发的特殊标记效果（冰冻/神圣打击/回响打击）
        triggerSpecialMarkEffects(target, attacker);
    }

    /**
     * 消耗施法者的充能 Buff（若有）
     * 施法者的下一次法术伤害翻倍，触发后移除充能
     *
     * @param attacker 施法者（可为null）
     * @return 伤害倍率（无充能时返回 1.0）
     */
    private static float consumeChargedCharge(LivingEntity attacker) {
        if (attacker == null) {
            return 1.0f;
        }

        MobEffect chargedEffect = ModEffects.CHARGED_BUFF.get();
        Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(chargedEffect);
        MobEffectInstance instance = attacker.getEffect(effectHolder);
        if (instance == null) {
            return 1.0f;
        }

        // 消耗充能：移除 Buff
        attacker.removeEffect(effectHolder);
        return Config.CHARGED_DAMAGE_MULTIPLIER.get().floatValue();
    }

    /**
     * 检查是否是元素流派的法术
     *
     * @param schoolType 法术流派
     * @return 是否是元素流派
     */
    private static boolean isElementSchool(io.redspace.ironsspellbooks.api.spells.SchoolType schoolType) {
        return schoolType.getId().toString().equals("legendarymage:element") ||
               schoolType.getId().getPath().equals("element");
    }

    /**
     * 处理元素流派的法术伤害
     * 随机赋予冰、火、雷元素标记（该流派的设计即为交替性元素伤害）
     *
     * @param serverLevel 服务器世界
     * @param target      目标实体
     * @param attacker    攻击者
     * @param damage      伤害值
     */
    private static void handleElementSchoolDamage(ServerLevel serverLevel, LivingEntity target,
                                                   LivingEntity attacker, float damage) {
        // 随机选择一个元素类型（火、冰、雷）
        ElementType[] elementTypes = {ElementType.FIRE, ElementType.ICE, ElementType.LIGHTNING};
        ElementType randomElement = elementTypes[RANDOM.nextInt(elementTypes.length)];

        debugLog(String.format("元素流派法术伤害: %s -> %s, 随机元素: %s, 伤害: %.1f",
                attacker != null ? attacker.getName().getString() : "未知",
                target.getName().getString(),
                randomElement.getId(),
                damage));

        ElementReactionManager.onElementDamage(serverLevel, target, attacker, randomElement, damage);
    }

    /**
     * 处理自定义流派的法术伤害
     * 根据数据包配置的映射施加对应的元素标记（使用热加载系统获取最新配置）
     *
     * @param serverLevel 服务器世界
     * @param target      目标实体
     * @param attacker    攻击者
     * @param schoolId    流派ID
     * @param damage      伤害值
     */
    private static void handleCustomSchoolDamage(ServerLevel serverLevel, LivingEntity target,
                                                  LivingEntity attacker, ResourceLocation schoolId, float damage) {
        // 使用热加载系统获取元素标记（自动合并数据包配置和运行时配置）
        List<ElementType> elementMarks = ElementMappingHotReload.getElementMarksForSchool(schoolId);

        if (elementMarks.isEmpty()) {
            return;
        }

        // 随机选择一个元素标记（如果配置了多个）
        ElementType elementType = elementMarks.get(RANDOM.nextInt(elementMarks.size()));

        debugLog(String.format("自定义流派法术伤害: %s -> %s, 流派: %s, 元素: %s, 伤害: %.1f",
                attacker != null ? attacker.getName().getString() : "未知",
                target.getName().getString(),
                schoolId,
                elementType.getId(),
                damage));

        ElementReactionManager.onElementDamage(serverLevel, target, attacker, elementType, damage);
    }

    /**
     * 监听实体受到伤害事件（前置阶段）
     * 1. 黑暗异常易伤：3级以上每一级 +5% 易伤（受伤倍率提升）
     * 2. 法术伤害的后备处理（SpellDamageEvent 未触发时的双路保障，
     *    元素施加由 ElementReactionManager 内部同 tick 去重）
     *
     * @param event 伤害事件
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        // 只在服务器端处理
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity target = event.getEntity();
        DamageSource damageSource = event.getSource();

        // 1. 黑暗异常易伤：3级以上每一级 +5% 易伤
        int bloodLevel = ElementMarkData.getMarkLevel(target, ElementType.BLOOD);
        if (bloodLevel >= com.legendarymage.legendarymagemod.effect.BloodMarkEffect.VULNERABILITY_THRESHOLD_LEVEL) {
            float multiplier = com.legendarymage.legendarymagemod.effect.BloodMarkEffect.calculateVulnerabilityMultiplier(bloodLevel);
            event.setNewDamage(event.getNewDamage() * multiplier);

            debugLog(String.format("%s 受到黑暗异常易伤加成: x%.2f (标记%d级)",
                    target.getName().getString(), multiplier, bloodLevel));
        }

        // 2. 法术伤害后备处理（双路保障）
        if (damageSource instanceof SpellDamageSource spellDamageSource) {
            LivingEntity attacker = null;
            if (spellDamageSource.getEntity() instanceof LivingEntity livingAttacker) {
                attacker = livingAttacker;
            }

            io.redspace.ironsspellbooks.api.spells.SchoolType schoolType = null;
            if (spellDamageSource.spell() != null) {
                schoolType = spellDamageSource.spell().getSchoolType();
            }

            if (schoolType != null) {
                ElementType elementType = ElementType.fromSchoolType(schoolType);
                if (elementType != null) {
                    debugLog(String.format("法术伤害(后备): %s -> %s, 流派: %s, 元素: %s, 伤害: %.1f",
                            attacker != null ? attacker.getName().getString() : "环境",
                            target.getName().getString(),
                            schoolType.getId().getPath(),
                            elementType.getId(),
                            event.getNewDamage()));
                    ElementReactionManager.onElementDamage(serverLevel, target, attacker, elementType, event.getNewDamage());
                }
            }
        }

        // 3. 血雷反击：近战攻击携带血雷 Buff 的目标时，攻击者自身受雷击反伤
        handleBloodThornsReaction(serverLevel, target, damageSource);
    }

    /**
     * 血雷反击处理
     * 近战攻击携带血雷 Buff 的目标时，攻击者自身受雷击反伤
     * 近战判定：directEntity == 攻击者（投射物的 directEntity 是箭矢等，不触发）
     * 反伤带 1 秒冷却，防止互持血雷的两个实体无限递归反伤
     *
     * @param serverLevel  服务器世界
     * @param target       被攻击的目标（血雷携带者）
     * @param damageSource 伤害来源
     */
    private static void handleBloodThornsReaction(ServerLevel serverLevel, LivingEntity target, DamageSource damageSource) {
        // getEntity() 返回 Entity，用 instanceof 转换为 LivingEntity
        LivingEntity attacker = damageSource.getEntity() instanceof LivingEntity livingAttacker ? livingAttacker : null;
        // 排除无攻击者、自伤
        if (attacker == null || attacker == target) {
            return;
        }

        // 近战判定：直接实体 == 攻击者本体（投射物的 directEntity 是箭矢等投射物实体）
        if (damageSource.getDirectEntity() != attacker) {
            return;
        }

        // 检查目标是否携带血雷 Buff
        MobEffect bloodThorns = ModEffects.BLOOD_THORNS.get();
        Holder<MobEffect> thornsHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(bloodThorns);
        MobEffectInstance thornsInstance = target.getEffect(thornsHolder);
        if (thornsInstance == null) {
            return;
        }

        // 反伤冷却（1 秒），防止互持血雷的两个实体无限递归反伤
        CompoundTag data = target.getPersistentData();
        long now = serverLevel.getGameTime();
        long last = data.getLong("legendarymage:blood_thorns_cooldown");
        if (now - last < 20) {
            return;
        }
        data.putLong("legendarymage:blood_thorns_cooldown", now);

        // 反伤强度：基础 × (携带者雷强 + 携带者血强) × 血雷等级（乘法计算，等级可叠加）
        int bloodThornsLevel = thornsInstance.getAmplifier() + 1;
        double lightningPower = ElementMarkData.getSchoolPower(target, ElementType.LIGHTNING);
        double bloodPower = ElementMarkData.getSchoolPower(target, ElementType.BLOOD);
        float baseDamage = Config.BLOOD_THORNS_BASE_DAMAGE.get().floatValue();
        float thornsDamage = baseDamage * (float) (lightningPower + bloodPower) * bloodThornsLevel;

        // 反伤（【伤害剔除】创造/旁观模式玩家不可伤害，剔除反伤）
        if (thornsDamage > 0 && !ElementDamageGuard.shouldSkipDamage(attacker)) {
            attacker.hurt(serverLevel.damageSources().magic(), thornsDamage);

            // 播放反击闪电特效（目标 → 攻击者方向）
            serverLevel.sendParticles(
                    new ZapParticleOption(attacker.position().add(0, attacker.getBbHeight() * 0.5, 0)),
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    1, 0, 0, 0, 0
            );
            serverLevel.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                    SoundRegistry.CHAIN_LIGHTNING_CHAIN.get(), SoundSource.HOSTILE, 0.8f, 1.0f);

            debugLog(String.format("血雷反击: %s 受到雷击反伤: %.1f",
                    attacker.getName().getString(), thornsDamage));
        }
    }

    /**
     * 监听实体死亡事件
     * 火焰异常：死亡后形成与 buff 等级大小相同范围的爆炸
     *
     * @param event 死亡事件
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // 只在服务器端处理
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity entity = event.getEntity();

        // 检查火焰异常等级（1-5），爆炸范围 = buff等级
        int fireLevel = ElementMarkData.getMarkLevel(entity, ElementType.FIRE);
        if (fireLevel <= 0) {
            return;
        }

        float radius = fireLevel;
        double x = entity.getX();
        double y = entity.getY() + entity.getBbHeight() * 0.5;
        double z = entity.getZ();

        // 播放爆炸粒子效果
        serverLevel.sendParticles(
                ParticleTypes.LAVA,
                x, y, z,
                (int) (radius * 5), 1.0, 1.0, 1.0,
                0.1
        );
        serverLevel.sendParticles(
                ParticleTypes.EXPLOSION,
                x, y, z,
                1, 0.5, 0.5, 0.5,
                0
        );

        // 创建爆炸（不破坏地形）
        serverLevel.explode(
                null,
                x, y, z,
                radius,
                Level.ExplosionInteraction.NONE
        );

        debugLog(String.format("%s 因火焰异常死亡，触发等级 %d 的爆炸",
                entity.getName().getString(), fireLevel));
    }

    /**
     * 冰封血脉：治疗抑制监听
     * 目标拥有冰封血脉 Debuff 时，受到的治疗按 Config 比例减半
     */
    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel)) return;

        LivingEntity target = event.getEntity();
        if (!target.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.ICE_BLOOD_DEBUFF.get()))) return;

        double reduction = Config.ICE_BLOOD_HEAL_REDUCTION_RATIO.get();
        // 先记录原治疗量再施加衰减（避免用结果反推原值时的除零风险）
        float originalAmount = event.getAmount();
        event.setAmount(originalAmount * (float) reduction);
        debugLog(String.format("冰封血脉抑制治疗效果: %.1f → %.1f",
                originalAmount, event.getAmount()));
    }

    /**
     * 狱火烙印：吸血监听
     * 目标拥有狱火烙印时，施法者对目标的伤害按比例吸血回复
     * 记录在目标 NBT 的施法者 UUID（legendarymage:hellfire_caster）
     */
    @SubscribeEvent
    public static void onLivingDamagePost(LivingDamageEvent.Post event) {
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) return;

        LivingEntity target = event.getEntity();
        Holder<MobEffect> brandHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.HELLFIRE_BRAND.get());
        if (!target.hasEffect(brandHolder)) return;

        // 从 NBT 获取施法者 UUID
        CompoundTag data = target.getPersistentData();
        if (!data.hasUUID("legendarymage:hellfire_caster")) return;
        java.util.UUID casterUUID = data.getUUID("legendarymage:hellfire_caster");

        // 吸血来源校验：仅施法者阵营（本人/召唤物/队友）造成的伤害才吸血
        // 防止第三方（其他玩家/怪物/环境伤害）打烙印目标时施法者白赚回血
        LivingEntity damageEntity = event.getSource().getEntity() instanceof LivingEntity living ? living : null;
        if (damageEntity == null) {
            return;
        }

        if (serverLevel.getEntity(casterUUID) instanceof LivingEntity caster) {
            // 非施法者阵营的伤害不吸血
            if (!ElementMarkData.isExcludedFromAttack(damageEntity, caster)) {
                return;
            }

            // 对烙印目标造成伤害时按比例回血给施法者
            double ratio = Config.HELLFIRE_LIFESTEAL_RATIO.get();
            float healAmount = event.getNewDamage() * (float) ratio;
            if (healAmount > 0) {
                caster.heal(healAmount);

                debugLog(String.format("狱火烙印吸血: %s 回复 %.1f (伤害: %.1f × %.2f)",
                        caster.getName().getString(), healAmount, event.getNewDamage(), ratio));
            }
        }
    }

    /**
     * 监听世界卸载事件
     * 清理该维度的元素区域数据，防止区域内存泄漏
     *
     * @param event 世界卸载事件
     */
    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            ElementZoneManager.clearZones(serverLevel.dimension());
        }
    }

    /**
     * 监听世界tick事件
     * 更新元素反应管理器
     *
     * @param event 世界tick事件
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        // 只在服务器端处理
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        ElementReactionManager.tick(serverLevel);

        // 更新元素区域（每秒对圈内实体施加效果，排除施法者）
        ElementZoneManager.tick(serverLevel);
    }

    /**
     * 触发特殊标记效果（仅铁魔法法术触发）
     * 统一处理冰冻、神圣打击和回响打击
     *
     * @param target   受到法术伤害的目标实体
     * @param attacker 造成伤害的攻击者（可为null）
     */
    private static void triggerSpecialMarkEffects(LivingEntity target, LivingEntity attacker) {
        // 冰冻异常：3级后的攻击 5% 概率冰冻3秒，CD 5秒
        IceMarkEffect.tryTriggerFreeze(target);

        // 光明异常：3级以上的攻击可触发神圣打击，CD 2秒（伤害由公式计算）
        HolyMarkEffect.tryTriggerHolyStrike(target, attacker);

        // 末影异常：3级以上的攻击可触发回响打击，CD 1秒（伤害由公式计算）
        EnderMarkEffect.tryTriggerEchoStrike(attacker, target);
    }
}
