package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.element.ElementType;
import io.redspace.ironsspellbooks.effect.IMobEffectEndCallback;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/**
 * 火系标记效果（火焰异常）
 * 3级标记时长结束时，给予烈焰Buff，可叠加等级
 * 
 * @author Love_U
 * @version 1.0.0
 */
public class FireMarkEffect extends ElementMarkEffect implements IMobEffectEndCallback {

    /**
     * 效果ID
     */
    public static final String EFFECT_ID = "fire_mark";

    /**
     * 效果颜色（橙红色）
     */
    private static final int EFFECT_COLOR = 0xFF4500;

    /**
     * 烈焰Buff基础持续时间（tick）
     * 5秒 = 100 tick
     */
    private static final int PYRO_FLAME_BASE_DURATION = 100;

    /**
     * 烈焰Buff每级额外持续时间（tick）
     */
    private static final int PYRO_FLAME_DURATION_PER_LEVEL = 50;

    /**
     * 构造函数
     */
    public FireMarkEffect() {
        super(ElementType.FIRE, EFFECT_COLOR);
    }

    @Override
    public String getEffectId() {
        return EFFECT_ID;
    }

    /**
     * 当效果被移除时调用
     * 如果标记为3级（amplifier=2），则给予烈焰Buff
     * 
     * @param entity 实体
     * @param amplifier 效果等级（0=1级，1=2级，2=3级）
     */
    @Override
    public void onEffectRemoved(LivingEntity entity, int amplifier) {
        // 检查实体是否已死亡或正在死亡
        if (!entity.isAlive() || entity.isDeadOrDying()) {
            return;
        }
        
        // 检查是否为3级标记（amplifier = 2）
        if (amplifier >= MAX_LEVEL) {
            // 使用延迟任务避免ConcurrentModificationException
            // 当牛奶移除效果时，不能在onEffectRemoved中直接访问效果列表
            // 必须延迟到下一 tick 执行
            final int finalAmplifier = amplifier;
            EffectRemovalHandler.addDelayedTask(entity, finalAmplifier, (e, amp) -> {
                if (e.isAlive() && !e.isDeadOrDying()) {
                    applyPyroFlameBuff(e, amp);
                }
            });
        }
    }

    /**
     * 给予烈焰Buff
     * Buff等级可无限叠加，每次触发时等级+1
     * 
     * @param entity 目标实体
     * @param markLevel 标记等级（0开始）
     */
    private void applyPyroFlameBuff(LivingEntity entity, int markLevel) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        // 获取烈焰效果
        MobEffect pyroFlameEffect = ModEffects.PYRO_FLAME.get();
        Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(pyroFlameEffect);

        // 检查目标是否已有烈焰Buff
        MobEffectInstance existingEffect = entity.getEffect(effectHolder);
        int newBuffLevel;
        int baseDuration;

        if (existingEffect != null) {
            // 已有Buff，等级+1（无限叠加）
            newBuffLevel = existingEffect.getAmplifier() + 2;  // +2因为amplifier是0开始的
            baseDuration = existingEffect.getDuration();
        } else {
            // 没有Buff，初始等级为1
            newBuffLevel = 1;
            baseDuration = PYRO_FLAME_BASE_DURATION;
        }

        // 计算持续时间（基础持续时间 + 每级额外时间）
        int duration = baseDuration + (newBuffLevel - 1) * PYRO_FLAME_DURATION_PER_LEVEL;

        // 施加烈焰Buff
        entity.addEffect(new MobEffectInstance(
                effectHolder,
                duration,
                newBuffLevel - 1,  // 等级（0开始）
                false,
                true,
                true
        ));
    }
}
