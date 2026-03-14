package com.legendarymage.legendarymagemod.effect;

import com.legendarymage.legendarymagemod.Config;
import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.spell.PyromaniacParticles;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 烈焰效果
 * 类似凋零效果，持续造成伤害
 * 当带有此效果的生物死亡时，会触发小型爆炸
 * 
 * @author Love_U
 * @version 0.0.1
 */
public class PyroFlameEffect extends MobEffect {

    /**
     * 效果ID
     */
    public static final String EFFECT_ID = "pyro_flame";

    /**
     * 基础每秒伤害
     */
    private static final float BASE_DAMAGE_PER_SECOND = 1.0f;

    /**
     * 伤害间隔（tick）
     * 每1秒造成一次伤害 = 20 tick
     */
    private static final int DAMAGE_INTERVAL = 20;

    /**
     * 效果颜色（火焰橙红色）
     */
    private static final int EFFECT_COLOR = 0xFF4500;

    /**
     * 构造函数
     */
    public PyroFlameEffect() {
        super(MobEffectCategory.HARMFUL, EFFECT_COLOR);
        
        // 添加属性修改器：降低最大生命值
        this.addAttributeModifier(
                Attributes.MAX_HEALTH,
                ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "pyro_flame_health"),
                -0.05,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    /**
     * 应用效果更新逻辑
     * 每tick执行，但只在特定间隔造成伤害
     * 
     * @param entity      实体
     * @param amplifier   效果等级（0为基础等级）
     */
    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // 只在特定间隔造成伤害
        if (entity.tickCount % DAMAGE_INTERVAL == 0) {
            // 计算伤害：基础伤害 + 等级加成
            float damage = BASE_DAMAGE_PER_SECOND * (amplifier + 1);
            
            // 造成火焰伤害
            entity.hurt(entity.level().damageSources().onFire(), damage);
            
            // 播放火焰粒子效果
            if (entity.level() instanceof ServerLevel serverLevel) {
                PyromaniacParticles.playBuffAmbientEffect(
                        serverLevel, 
                        entity.position(), 
                        entity.getBbHeight(), 
                        entity.getBbWidth(), 
                        entity.tickCount
                );
            }
        }
        return true;
    }

    /**
     * 判断是否应该应用效果更新
     * 
     * @param duration  剩余持续时间
     * @param amplifier 效果等级
     * @return 是否应该更新
     */
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // 每tick都检查，让applyEffectTick自己控制伤害间隔
        return true;
    }

    /**
     * 触发爆炸效果
     * 当带有烈焰效果的生物死亡时调用
     * 
     * @param level     世界
     * @param entity    死亡的实体
     * @param amplifier 效果等级
     */
    public static void triggerExplosion(Level level, LivingEntity entity, int amplifier) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        
        Vec3 pos = entity.position();
        
        // 从配置读取爆炸参数
        float basePower = Config.PYROMANIAC_EXPLOSION_BASE_POWER.get().floatValue();
        float powerPerLevel = Config.PYROMANIAC_EXPLOSION_POWER_PER_LEVEL.get().floatValue();
        boolean destroyBlocks = Config.PYROMANIAC_EXPLOSION_DESTROY_BLOCKS.get();
        boolean causeFire = Config.PYROMANIAC_EXPLOSION_CAUSE_FIRE.get();
        
        // 计算爆炸威力
        float explosionPower = basePower + (amplifier * powerPerLevel);
        
        // 播放爆炸粒子效果
        PyromaniacParticles.playExplosionEffect(serverLevel, pos, explosionPower, entity.getBbHeight());
        
        // 根据配置决定爆炸类型
        Level.ExplosionInteraction explosionInteraction;
        if (destroyBlocks) {
            explosionInteraction = causeFire ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.MOB;
        } else {
            // 不破坏地形的爆炸
            explosionInteraction = Level.ExplosionInteraction.NONE;
        }
        
        // 创建爆炸
        level.explode(
                null,  // 爆炸源（null表示环境爆炸）
                pos.x,
                pos.y + entity.getBbHeight() * 0.5,
                pos.z,
                explosionPower,
                explosionInteraction
        );
    }
}
