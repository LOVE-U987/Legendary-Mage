package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.element.ElementType;
import io.redspace.ironsspellbooks.effect.IMobEffectEndCallback;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/**
 * 冰系标记效果（冰冻异常）
 * 3级标记时长结束时，给予铁魔法的冰冻效果（Chilled）
 * 
 * @author Love_U
 * @version 0.0.2
 */
public class IceMarkEffect extends ElementMarkEffect implements IMobEffectEndCallback {

    /**
     * 效果ID
     */
    public static final String EFFECT_ID = "ice_mark";

    /**
     * 效果颜色（深青色）
     */
    private static final int EFFECT_COLOR = 0x00CED1;

    /**
     * 冰冻效果基础持续时间（tick）
     * 8秒 = 160 tick
     */
    private static final int CHILLED_BASE_DURATION = 160;

    /**
     * 冰冻效果每级额外持续时间（tick）
     */
    private static final int CHILLED_DURATION_PER_LEVEL = 40;

    /**
     * 构造函数
     */
    public IceMarkEffect() {
        super(ElementType.ICE, EFFECT_COLOR);
    }

    @Override
    public String getEffectId() {
        return EFFECT_ID;
    }

    /**
     * 当效果被移除时调用
     * 如果标记为3级（amplifier=2），则给予铁魔法的冰冻效果
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
            applyChilledEffect(entity, amplifier);
        }
    }

    /**
     * 给予铁魔法的冰冻效果（Chilled）
     * 效果等级 = 标记等级
     * 
     * @param entity 目标实体
     * @param markLevel 标记等级（0开始）
     */
    private void applyChilledEffect(LivingEntity entity, int markLevel) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        // 计算效果等级（1开始）
        int effectLevel = markLevel + 1;

        // 计算持续时间
        int duration = CHILLED_BASE_DURATION + (effectLevel - 1) * CHILLED_DURATION_PER_LEVEL;

        // 获取铁魔法的Chilled效果
        Holder<MobEffect> effectHolder = MobEffectRegistry.CHILLED;

        // 施加冰冻效果
        entity.addEffect(new MobEffectInstance(
                effectHolder,
                duration,
                effectLevel - 1,  // 等级（0开始）
                false,
                true,
                true
        ));
    }
}
