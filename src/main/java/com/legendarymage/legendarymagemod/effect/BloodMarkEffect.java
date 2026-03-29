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
 * 血系标记效果（黑暗异常）
 * 3级标记时长结束时，获得"暗夜无光"buff
 * 减少法术抗性，每级-5%，可叠加
 * 
 * @author Love_U
 * @version 1.0.0
 */
public class BloodMarkEffect extends ElementMarkEffect implements IMobEffectEndCallback {

    /**
     * 效果ID
     */
    public static final String EFFECT_ID = "blood_mark";

    /**
     * 效果颜色（深红色）
     */
    private static final int EFFECT_COLOR = 0x8B0000;

    /**
     * 暗夜无光Buff基础持续时间（tick）
     * 10秒 = 200 tick
     */
    private static final int DARKNESS_BUFF_BASE_DURATION = 200;

    /**
     * 暗夜无光Buff每级额外持续时间（tick）
     */
    private static final int DARKNESS_BUFF_DURATION_PER_LEVEL = 100;

    /**
     * 构造函数
     */
    public BloodMarkEffect() {
        super(ElementType.BLOOD, EFFECT_COLOR);
    }

    @Override
    public String getEffectId() {
        return EFFECT_ID;
    }

    /**
     * 当效果被移除时调用
     * 如果标记为3级（amplifier=2），则给予暗夜无光Buff
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
            applyDarknessBuff(entity, amplifier);
        }
    }

    /**
     * 给予暗夜无光Buff
     * Buff等级可无限叠加，每次触发时等级+1
     * 
     * @param entity 目标实体
     * @param markLevel 标记等级（0开始）
     */
    private void applyDarknessBuff(LivingEntity entity, int markLevel) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        // 获取暗夜无光效果
        MobEffect darknessEffect = ModEffects.DARKNESS_BUFF.get();
        Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(darknessEffect);

        // 检查目标是否已有暗夜无光Buff
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
            baseDuration = DARKNESS_BUFF_BASE_DURATION;
        }

        // 计算持续时间（基础持续时间 + 每级额外时间）
        int duration = baseDuration + (newBuffLevel - 1) * DARKNESS_BUFF_DURATION_PER_LEVEL;

        // 施加暗夜无光Buff
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
