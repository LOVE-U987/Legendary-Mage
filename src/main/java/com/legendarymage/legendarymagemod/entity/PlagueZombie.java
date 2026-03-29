package com.legendarymage.legendarymagemod.entity;

import com.legendarymage.legendarymagemod.effect.ModEffects;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

/**
 * 瘟疫僵尸实体
 * 暗毒元素反应死亡后转化的僵尸
 * 特性：
 * - 血量为原生物的 50%
 * - 伤害为原生物的 50%
 * - 存在 1 分钟后死亡并毒爆
 * - 毒爆范围 3 格，伤害 = 2 × 瘟疫 Buff 等级
 * - 毒爆传播瘟疫给周围生物
 * - 使用铁魔法的 SummonedZombie 实现召唤机制
 *
 * @author Love_U
 * @version 1.0.0
 */
public class PlagueZombie {

    /**
     * 铁魔法的召唤僵尸
     */
    private io.redspace.ironsspellbooks.entity.mobs.SummonedZombie summonedZombie;

    /**
     * 原生物的血量
     */
    private float originalMaxHealth;

    /**
     * 原生物的伤害
     */
    private float originalAttackDamage;

    /**
     * 瘟疫 Buff 等级
     */
    private int plagueLevel;

    /**
     * 存在时间（tick）
     */
    private int existTime;

    /**
     * 随机数生成器
     */
    private static final Random RANDOM = new Random();

    /**
     * 构造函数（私有，只能通过 createFromEntity 创建）
     */
    private PlagueZombie() {
        this.existTime = 0;
        this.plagueLevel = 1;
    }

    /**
     * 从原生物创建瘟疫僵尸
     *
     * @param originalEntity 原生物
     * @param summoner       召唤者（原施法者）
     * @param plagueLevel    瘟疫 Buff 等级
     * @return 瘟疫僵尸实例
     */
    public static PlagueZombie createFromEntity(LivingEntity originalEntity, LivingEntity summoner, int plagueLevel) {
        Level level = originalEntity.level();
        PlagueZombie plagueZombie = new PlagueZombie();
        
        try {
            // 使用铁魔法的 SummonedZombie - 参考复苏符文
            io.redspace.ironsspellbooks.entity.mobs.SummonedZombie summonedZombie =
                new io.redspace.ironsspellbooks.entity.mobs.SummonedZombie(level, summoner, true);
            
            // 设置位置
            summonedZombie.moveTo(
                originalEntity.getX(), 
                originalEntity.getY(), 
                originalEntity.getZ(),
                originalEntity.getYRot(), 
                originalEntity.getXRot()
            );
            
            // 保存原生物属性
            plagueZombie.originalMaxHealth = originalEntity.getMaxHealth();
            plagueZombie.originalAttackDamage = (float) originalEntity.getAttributeValue(
                net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
            
            // 设置瘟疫等级
            plagueZombie.plagueLevel = plagueLevel;
            
            // 设置属性为原生物的 50%
            summonedZombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
                .setBaseValue(plagueZombie.originalMaxHealth * 0.5);
            summonedZombie.setHealth((float)(plagueZombie.originalMaxHealth * 0.5));
            
            summonedZombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)
                .setBaseValue(plagueZombie.originalAttackDamage * 0.5);
            
            // 添加到世界
            boolean added = level.addFreshEntity(summonedZombie);
            
            if (added) {
                // 触发起身动画
                summonedZombie.triggerRiseAnimation();
                
                // 设置目标（如果召唤者不为 null）
                if (summoner != null) {
                    LivingEntity target = summoner.getLastHurtByMob();
                    if (target != null) {
                        summonedZombie.setTarget(target);
                    }
                }
                
                plagueZombie.summonedZombie = summonedZombie;
                plagueZombie.existTime = 0;
                
                return plagueZombie;
            } else {
                // 添加失败，返回 null
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 实体 tick
     */
    public void tick() {
        if (summonedZombie == null || summonedZombie.isRemoved()) {
            return;
        }
        
        Level level = summonedZombie.level();
        
        if (level.isClientSide) {
            return;
        }

        // 增加存在时间
        existTime++;

        // 检查是否超过 1 分钟（60 秒 = 1200 tick）
        if (existTime >= 1200) {
            // 死亡并毒爆
            explode();
        }
    }

    /**
     * 毒爆
     * 范围 3 格，伤害 = 2 × 瘟疫 Buff 等级
     * 传播瘟疫给周围生物
     */
    private void explode() {
        if (summonedZombie == null || summonedZombie.level().isClientSide) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) summonedZombie.level();
        Vec3 pos = summonedZombie.position();
        double range = 3.0;
        float damage = 2.0f * plagueLevel;

        // 播放毒爆粒子效果
        playExplosionParticles(serverLevel, pos, range);

        // 获取范围内的所有生物
        net.minecraft.world.phys.AABB area = new net.minecraft.world.phys.AABB(
                pos.x - range, pos.y - range, pos.z - range,
                pos.x + range, pos.y + range, pos.z + range
        );

        java.util.List<LivingEntity> entities = serverLevel.getEntitiesOfClass(LivingEntity.class, area);
        
        // 获取召唤者
        LivingEntity summoner = null;
        net.minecraft.world.entity.Entity summonerEntity = summonedZombie.getSummoner();
        if (summonerEntity instanceof LivingEntity) {
            summoner = (LivingEntity) summonerEntity;
        }

        for (LivingEntity entity : entities) {
            // 排除自己和召唤者
            if (entity == summonedZombie || entity == summoner) {
                continue;
            }

            // 检查是否为召唤者的队友
            if (isAlly(summoner, entity)) {
                continue;
            }

            // 造成伤害
            entity.hurt(serverLevel.damageSources().magic(), damage);

            // 传播瘟疫 Buff（等级 = 原等级 / 2）
            int newPlagueLevel = Math.max(1, plagueLevel / 2);
            Holder<net.minecraft.world.effect.MobEffect> plagueEffect = 
                    BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PLAGUE_BUFF.get());
            entity.addEffect(new MobEffectInstance(
                    plagueEffect,
                    200, // 持续时间 10 秒
                    newPlagueLevel - 1,
                    false,
                    true
            ));
        }

        // 僵尸死亡
        summonedZombie.discard();
    }

    /**
     * 播放毒爆粒子效果
     */
    private void playExplosionParticles(ServerLevel serverLevel, Vec3 pos, double range) {
        // 绿色爆炸粒子
        for (int i = 0; i < 30; i++) {
            double offsetX = (RANDOM.nextDouble() - 0.5) * range * 2;
            double offsetY = RANDOM.nextDouble() * range;
            double offsetZ = (RANDOM.nextDouble() - 0.5) * range * 2;

            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.SQUID_INK,
                    pos.x + offsetX,
                    pos.y + offsetY,
                    pos.z + offsetZ,
                    1,
                    0, 0, 0,
                    0
            );
        }

        // 女巫粒子
        for (int i = 0; i < 20; i++) {
            double offsetX = (RANDOM.nextDouble() - 0.5) * range * 2;
            double offsetY = RANDOM.nextDouble() * range;
            double offsetZ = (RANDOM.nextDouble() - 0.5) * range * 2;

            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.WITCH,
                    pos.x + offsetX,
                    pos.y + offsetY,
                    pos.z + offsetZ,
                    1,
                    0.2, 0.2, 0.2,
                    0.05
            );
        }
    }

    /**
     * 检查两个实体是否为队友
     */
    private boolean isAlly(LivingEntity entity1, LivingEntity entity2) {
        if (entity1 == null || entity2 == null) {
            return false;
        }

        // 检查团队 - 修复类型比较问题
        if (entity1.getTeam() != null && entity2.getTeam() != null 
                && entity1.getTeam().getName().equals(entity2.getTeam().getName())) {
            return true;
        }

        return false;
    }

    /**
     * 获取瘟疫等级
     *
     * @return 瘟疫等级
     */
    public int getPlagueLevel() {
        return plagueLevel;
    }

    /**
     * 获取存在时间
     *
     * @return 存在时间（tick）
     */
    public int getExistTime() {
        return existTime;
    }

    /**
     * 获取剩余存在时间
     *
     * @return 剩余 tick 数
     */
    public int getRemainingTime() {
        return Math.max(0, 1200 - existTime);
    }

    /**
     * 获取底层的 SummonedZombie 实体
     *
     * @return SummonedZombie 实例
     */
    public io.redspace.ironsspellbooks.entity.mobs.SummonedZombie getSummonedZombie() {
        return summonedZombie;
    }

    /**
     * 检查僵尸是否仍然存活
     *
     * @return 是否存活
     */
    public boolean isAlive() {
        return summonedZombie != null && !summonedZombie.isRemoved();
    }
}
