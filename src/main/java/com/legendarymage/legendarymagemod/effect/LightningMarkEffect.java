package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.Config;
import com.legendarymage.legendarymagemod.ModLogger;
import com.legendarymage.legendarymagemod.element.ElementMarkData;
import com.legendarymage.legendarymagemod.element.ElementType;
import io.redspace.ironsspellbooks.effect.IMobEffectEndCallback;
import io.redspace.ironsspellbooks.particle.ZapParticleOption;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/**
 * 雷系标记效果（雷电异常）【重写 v2.0】
 *
 * 【效果】
 * - 周期性自动触发闪电链：CD 2 秒，对四周最多 4 个敌人造成闪电链伤害
 * - 伤害由公式计算：buff等级 × 施法者雷系流派强度
 * - 攻击名单排除施法者、施法者队友、双方召唤物
 * - 若命中的敌人也携带雷电异常，则额外传导至 3 个敌人并刷新 CD
 * - 标记（3级及以上）自然结束时给予"感电"Buff（供雷毒元素反应使用）
 *
 * @author Love_U
 * @version 2.0.0
 */
public class LightningMarkEffect extends ElementMarkEffect implements IMobEffectEndCallback {

    /**
     * 效果ID
     */
    public static final String EFFECT_ID = "lightning_mark";

    /**
     * 效果颜色（深紫色）
     */
    private static final int EFFECT_COLOR = 0x9400D3;

    /**
     * 闪电链触发间隔（tick）
     * 2秒 = 40 tick
     */
    private static final int CHAIN_COOLDOWN_TICKS = 40;

    /**
     * 闪电链范围（格）
     */
    private static final double CHAIN_RANGE = 8.0;

    /**
     * 闪电链目标数量（4个敌人）
     */
    private static final int CHAIN_TARGETS = 4;

    /**
     * 传导目标数量（3个敌人）
     */
    private static final int CONDUCT_TARGETS = 3;

    /**
     * 传导范围（格）
     */
    private static final double CONDUCT_RANGE = 6.0;

    /**
     * 感电Buff基础持续时间（tick）
     * 10秒 = 200 tick
     */
    private static final int ELECTROCUTED_BUFF_BASE_DURATION = 200;

    /**
     * 感电Buff每级额外持续时间（tick）
     */
    private static final int ELECTROCUTED_BUFF_DURATION_PER_LEVEL = 100;

    /**
     * 构造函数
     */
    public LightningMarkEffect() {
        super(ElementType.LIGHTNING, EFFECT_COLOR);
    }

    @Override
    public String getEffectId() {
        return EFFECT_ID;
    }

    /**
     * 每 tick 检查冷却，CD 2 秒自动触发一次闪电链
     *
     * @param entity    实体
     * @param amplifier 效果等级（0开始）
     */
    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // 只在服务器端执行
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return true;
        }

        // 检查实体是否已死亡或正在死亡
        if (!entity.isAlive() || entity.isDeadOrDying()) {
            return true;
        }

        // CD 2秒：周期性自动触发闪电链
        if (ElementMarkData.isCooldownReady(entity, ElementType.LIGHTNING, CHAIN_COOLDOWN_TICKS)) {
            boolean conducted = triggerChainLightning(serverLevel, entity, amplifier);

            // 记录触发时刻（开始CD；发生传导时同样刷新CD）
            ElementMarkData.markCooldown(entity, ElementType.LIGHTNING);

            if (Config.ELEMENT_REACTION_DEBUG_OUTPUT.get()) {
                ModLogger.element("[雷电异常] {} 触发闪电链 (标记{}级){}, 伤害: {}",
                        entity.getName().getString(), amplifier + 1,
                        conducted ? "，并发生传导" : "",
                        ElementMarkData.calculateDamage(entity, ElementType.LIGHTNING, null));
            }
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // 每 tick 检查冷却，由 applyEffectTick 控制触发间隔
        return true;
    }

    /**
     * 触发闪电链
     * 对四周最多4个敌人造成闪电链伤害（排除施法者、队友、双方召唤物）
     * 命中的敌人若携带雷电异常，则额外传导至 3 个敌人
     *
     * @param serverLevel 服务器世界
     * @param entity      携带雷电异常的实体
     * @param amplifier   Buff 等级（0开始）
     * @return 是否发生了传导
     */
    private boolean triggerChainLightning(ServerLevel serverLevel, LivingEntity entity, int amplifier) {
        // 施法者（用于排除攻击名单，无记录时以携带者自身为参照）
        LivingEntity caster = ElementMarkData.getCaster(serverLevel, entity, ElementType.LIGHTNING);
        LivingEntity excludeRef = caster != null ? caster : entity;

        // 公式伤害：buff等级 × 雷系流派强度
        float damage = ElementMarkData.calculateDamage(entity, ElementType.LIGHTNING, null);
        if (damage <= 0) {
            return false;
        }

        Vec3 pos = entity.position();

        // 查找范围内的敌人（排除施法者、队友、双方召唤物）
        List<LivingEntity> candidates = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(
                        pos.x - CHAIN_RANGE, pos.y - CHAIN_RANGE, pos.z - CHAIN_RANGE,
                        pos.x + CHAIN_RANGE, pos.y + CHAIN_RANGE, pos.z + CHAIN_RANGE
                ),
                e -> e.isAlive() && !e.isDeadOrDying()
                        && !ElementMarkData.isExcludedFromAttack(e, excludeRef)
        );

        // 按距离排序，优先攻击最近的 4 个目标
        candidates.sort(Comparator.comparingDouble(e -> e.distanceToSqr(entity.position())));
        List<LivingEntity> targets = candidates.subList(0, Math.min(CHAIN_TARGETS, candidates.size()));

        boolean conducted = false;
        Vec3 lastPos = pos;
        for (LivingEntity target : targets) {
            // 造成公式伤害
            target.hurt(serverLevel.damageSources().magic(), damage);

            // 播放连锁闪电特效
            playChainLightningEffect(serverLevel, lastPos, target.position());
            playLightningEffect(serverLevel, target);
            lastPos = target.position();

            // 若目标也携带雷电异常 → 传导至 3 个敌人并刷新CD
            if (ElementMarkData.getMarkLevel(target, ElementType.LIGHTNING) > 0) {
                conductChain(serverLevel, target, excludeRef, damage);
                conducted = true;
            }
        }

        // 播放中心位置的闪电爆发效果
        playLightningEffect(serverLevel, entity);

        return conducted;
    }

    /**
     * 传导闪电链
     * 从携带雷电异常的敌人处传导至周围最多 3 个敌人
     *
     * @param serverLevel 服务器世界
     * @param origin      携带雷电异常的敌人
     * @param excludeRef  排除参照（施法者）
     * @param damage      伤害值
     */
    private void conductChain(ServerLevel serverLevel, LivingEntity origin, LivingEntity excludeRef, float damage) {
        Vec3 pos = origin.position();

        // 查找 origin 周围的敌人（排除施法者阵营）
        List<LivingEntity> candidates = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(
                        pos.x - CONDUCT_RANGE, pos.y - CONDUCT_RANGE, pos.z - CONDUCT_RANGE,
                        pos.x + CONDUCT_RANGE, pos.y + CONDUCT_RANGE, pos.z + CONDUCT_RANGE
                ),
                e -> e.isAlive() && !e.isDeadOrDying() && e != origin
                        && !ElementMarkData.isExcludedFromAttack(e, excludeRef)
        );

        // 按距离排序，优先传导至最近的 3 个目标
        candidates.sort(Comparator.comparingDouble(e -> e.distanceToSqr(origin.position())));
        List<LivingEntity> targets = candidates.subList(0, Math.min(CONDUCT_TARGETS, candidates.size()));

        Vec3 lastPos = pos;
        for (LivingEntity target : targets) {
            target.hurt(serverLevel.damageSources().magic(), damage);
            playChainLightningEffect(serverLevel, lastPos, target.position());
            playLightningEffect(serverLevel, target);
            lastPos = target.position();
        }
    }

    /**
     * 当效果被移除时调用
     * 3级及以上标记自然结束时，给予"感电"Buff（供雷毒元素反应消耗）
     *
     * @param entity    实体
     * @param amplifier 效果等级（0开始）
     */
    @Override
    public void onEffectRemoved(LivingEntity entity, int amplifier) {
        // 检查实体是否已死亡或正在死亡
        if (!entity.isAlive() || entity.isDeadOrDying()) {
            return;
        }

        // 3级及以上（amplifier >= 2）标记移除时给予感电Buff
        if (amplifier >= 2) {
            // 延迟到下一 tick 执行，避免 ConcurrentModificationException
            final int finalAmplifier = amplifier;
            EffectRemovalHandler.addDelayedTask(entity, finalAmplifier, (e, amp) -> {
                if (e.isAlive() && !e.isDeadOrDying()) {
                    applyElectrocutedBuff(e, amp);
                }
            });
        }
    }

    /**
     * 给予感电Buff
     * Buff等级可叠加，每次触发时等级+1
     *
     * @param entity    目标实体
     * @param markLevel 标记等级（0开始）
     */
    private void applyElectrocutedBuff(LivingEntity entity, int markLevel) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        // 获取感电效果
        MobEffect electrocutedEffect = ModEffects.ELECTROCUTED_BUFF.get();
        Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(electrocutedEffect);

        // 检查目标是否已有感电Buff
        MobEffectInstance existingEffect = entity.getEffect(effectHolder);
        int newBuffLevel;
        int baseDuration;

        if (existingEffect != null) {
            // 已有Buff，等级+1（无限叠加）
            newBuffLevel = existingEffect.getAmplifier() + 2;
            baseDuration = existingEffect.getDuration();
        } else {
            // 没有Buff，初始等级为1
            newBuffLevel = 1;
            baseDuration = ELECTROCUTED_BUFF_BASE_DURATION;
        }

        // 计算持续时间（基础持续时间 + 每级额外时间）
        int duration = baseDuration + (newBuffLevel - 1) * ELECTROCUTED_BUFF_DURATION_PER_LEVEL;

        // 施加感电Buff
        entity.addEffect(new MobEffectInstance(
                effectHolder,
                duration,
                newBuffLevel - 1,
                false,
                true,
                true
        ));
    }

    /**
     * 播放闪电效果
     *
     * @param serverLevel 服务器世界
     * @param target      目标实体
     */
    private void playLightningEffect(ServerLevel serverLevel, LivingEntity target) {
        Vec3 targetPos = target.position().add(0, target.getBbHeight() / 2, 0);

        // 铁魔法的 Zap 粒子（从目标位置到自身的闪电）
        serverLevel.sendParticles(
                new ZapParticleOption(targetPos),
                target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                1, 0, 0, 0, 0
        );

        // 播放电力粒子爆发
        serverLevel.sendParticles(
                ParticleTypes.END_ROD,
                target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                10,
                0.3, 0.3, 0.3,
                0.1
        );

        // 播放音效
        serverLevel.playSound(
                null,
                target.getX(), target.getY(), target.getZ(),
                SoundRegistry.CHAIN_LIGHTNING_CHAIN.get(),
                SoundSource.HOSTILE,
                1.0f,
                1.0f
        );
    }

    /**
     * 播放连锁闪电特效（连接两个位置的闪电链）
     *
     * @param serverLevel 服务器世界
     * @param startPos    起始位置
     * @param endPos      结束位置
     */
    private void playChainLightningEffect(ServerLevel serverLevel, Vec3 startPos, Vec3 endPos) {
        serverLevel.sendParticles(
                new ZapParticleOption(endPos.add(0, 0.5, 0)),
                startPos.x, startPos.y + 0.5, startPos.z,
                1, 0, 0, 0, 0
        );
    }
}
