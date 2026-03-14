package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.element.ElementType;
import io.redspace.ironsspellbooks.effect.IMobEffectEndCallback;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * 毒系标记效果（毒素异常）
 * 3级标记时长结束时，给予中毒Buff，可叠加等级
 * 
 * @author Love_U
 * @version 0.0.2
 */
public class PoisonMarkEffect extends ElementMarkEffect implements IMobEffectEndCallback {

    /**
     * 效果ID
     */
    public static final String EFFECT_ID = "poison_mark";

    /**
     * 效果颜色（酸橙绿）
     */
    private static final int EFFECT_COLOR = 0x32CD32;

    /**
     * 中毒Buff基础持续时间（tick）
     * 10秒 = 200 tick
     */
    private static final int POISON_BASE_DURATION = 200;

    /**
     * 中毒Buff每级额外持续时间（tick）
     */
    private static final int POISON_DURATION_PER_LEVEL = 100;

    /**
     * 构造函数
     */
    public PoisonMarkEffect() {
        super(ElementType.POISON, EFFECT_COLOR);
    }

    @Override
    public String getEffectId() {
        return EFFECT_ID;
    }

    /**
     * 当效果被移除时调用
     * 如果标记为3级（amplifier=2），则给予中毒Buff
     * 
     * @param entity 实体
     * @param amplifier 效果等级（0=1级，1=2级，2=3级）
     */
    public void onEffectRemoved(LivingEntity entity, int amplifier) {
        // 检查实体是否已死亡或正在死亡
        if (!entity.isAlive() || entity.isDeadOrDying()) {
            return;
        }
        
        // 检查是否为3级标记（amplifier = 2）
        if (amplifier >= MAX_LEVEL) {
            applyPoisonBuff(entity, amplifier);
        }
    }

    /**
     * 给予中毒Buff
     * Buff等级可无限叠加，每次触发时等级+1
     * 
     * @param entity 目标实体
     * @param markLevel 标记等级（0开始）
     */
    private void applyPoisonBuff(LivingEntity entity, int markLevel) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        // 获取原版中毒效果（在1.21中已经是Holder<MobEffect>）
        Holder<MobEffect> effectHolder = MobEffects.POISON;

        // 检查目标是否已有中毒Buff
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
            baseDuration = POISON_BASE_DURATION;
        }

        // 计算持续时间（基础持续时间 + 每级额外时间）
        int duration = baseDuration + (newBuffLevel - 1) * POISON_DURATION_PER_LEVEL;

        // 施加中毒Buff
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
