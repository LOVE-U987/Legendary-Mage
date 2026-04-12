package com.legendarymage.legendarymagemod.entity.spell;

import java.util.Optional;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.entity.ModEntities;
import com.legendarymage.legendarymagemod.trail.SimpleTrailEffect;
import com.legendarymage.legendarymagemod.trail.SimpleTrailManager;

import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 拖尾特效测试投射物 - 简单版 (类似Perception)
 * 
 * 【特点】
 * - 直接连线，无插值
 * - ARGB颜色格式
 * - 实体死亡后整体淡出
 * - 向运动反方向偏移
 * 
 * @author Love_U
 * @version 1.0.6
 */
public class TrailTestProjectile extends AbstractMagicProjectile {

    /**
     * 简单拖尾实例
     */
    private SimpleTrailEffect simpleTrail = null;

    /**
     * 拖尾是否已初始化
     */
    private boolean trailInitialized = false;

    /**
     * 帧计数器
     */
    private int frameCount = 0;

    /**
     * 构造函数
     */
    public TrailTestProjectile(EntityType<? extends TrailTestProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    /**
     * 构造函数
     */
    public TrailTestProjectile(Level level, LivingEntity shooter) {
        super(ModEntities.TRAIL_TEST.get(), level);
        setOwner(shooter);

        LegendaryMage.LOGGER.info("[简单拖尾测试] 构造函数被调用！线程: {}, 是否客户端: {}",
                Thread.currentThread().getName(),
                level.isClientSide());
    }

    /**
     * 初始化简单拖尾
     * 类似Perception的配置方式
     */
    private void initializeSimpleTrail() {
        if (trailInitialized) return;

        String trailId = "simple_trail_" + this.getId();

        LegendaryMage.LOGGER.info("========================================");
        LegendaryMage.LOGGER.info("[简单拖尾测试] 正在初始化拖尾...");
        LegendaryMage.LOGGER.info("[简单拖尾测试] 拖尾ID: {}", trailId);

        // 创建火元素拖尾（带发光效果）
        simpleTrail = SimpleTrailManager.getInstance().createFireElementTrail(trailId, this);

        if (simpleTrail != null) {
            LegendaryMage.LOGGER.info("[简单拖尾测试] 拖尾创建成功！");
            LegendaryMage.LOGGER.info("[简单拖尾测试] 配置:");
            LegendaryMage.LOGGER.info("   - 宽度: 0.1");
            LegendaryMage.LOGGER.info("   - 最大历史: 12");
            LegendaryMage.LOGGER.info("   - 更新间隔: 1 tick");
            LegendaryMage.LOGGER.info("   - 起始颜色: 橙红色 (0xFFFF4500)");
            LegendaryMage.LOGGER.info("   - 结束颜色: 红色 (0x80FF0000)");
            LegendaryMage.LOGGER.info("   - 发光: 是");
            LegendaryMage.LOGGER.info("   - 发光强度: 0.8");
            LegendaryMage.LOGGER.info("   - 元素类型: FIRE");
        } else {
            LegendaryMage.LOGGER.error("[简单拖尾测试] 拖尾创建失败！");
        }

        trailInitialized = true;
        LegendaryMage.LOGGER.info("========================================");
    }

    /**
     * 定义同步数据
     */
    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    /**
     * 轨迹粒子效果
     */
    @Override
    public void trailParticles() {
        Level level = level();

        // 添加粒子效果（类似Perception的额外效果）
        level.addParticle(ParticleTypes.END_ROD, getX(), getY(), getZ(), 0, 0.05, 0);

        if (this.tickCount % 3 == 0) {
            double offsetX = (Math.random() - 0.5) * 0.3;
            double offsetY = (Math.random() - 0.5) * 0.3;
            double offsetZ = (Math.random() - 0.5) * 0.3;
            level.addParticle(ParticleTypes.FIREWORK, getX() + offsetX, getY() + offsetY, getZ() + offsetZ, 0, 0, 0);
        }
    }

    /**
     * 每tick更新
     */
    @Override
    public void tick() {
        super.tick();

        // 延迟初始化
        if (!trailInitialized && level().isClientSide()) {
            LegendaryMage.LOGGER.info("[简单拖尾测试] 客户端首次Tick！初始化拖尾...");
            initializeSimpleTrail();
        }

        // 15秒后销毁
        if (tickCount > 300) {
            LegendaryMage.LOGGER.info("[简单拖尾测试] 达到最大生存时间，准备销毁");
            cleanupTrail();
            discard();
        }
    }

    /**
     * 击中方块
     */
    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        LegendaryMage.LOGGER.info("[简单拖尾测试] 击中方块! 位置: {}", blockHitResult.getLocation());

        Level level = level();
        for (int i = 0; i < 20; i++) {
            double offsetX = (Math.random() - 0.5) * 1.0;
            double offsetY = (Math.random() - 0.5) * 1.0;
            double offsetZ = (Math.random() - 0.5) * 1.0;
            level.addParticle(ParticleTypes.EXPLOSION_EMITTER,
                    blockHitResult.getLocation().x + offsetX,
                    blockHitResult.getLocation().y + offsetY,
                    blockHitResult.getLocation().z + offsetZ, 0, 0, 0);
        }

        cleanupTrail();
        discard();
    }

    /**
     * 击中实体
     */
    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        LegendaryMage.LOGGER.info("[简单拖尾测试] 击中实体! {}", entityHitResult.getEntity().getName().getString());
        cleanupTrail();
        discard();
    }

    /**
     * 清理拖尾
     */
    private void cleanupTrail() {
        if (simpleTrail != null) {
            LegendaryMage.LOGGER.info("[简单拖尾测试] 清理拖尾...");
            simpleTrail.stop();
            LegendaryMage.LOGGER.info("[简单拖尾测试] 最终点数: {}", simpleTrail.getPointCount());
            simpleTrail = null;
        }
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return Optional.of(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.GLASS_BREAK));
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        Level level = level();
        for (int i = 0; i < 20; i++) {
            level.addParticle(ParticleTypes.END_ROD, x, y, z,
                    (Math.random() - 0.5) * 0.3,
                    (Math.random() - 0.5) * 0.3,
                    (Math.random() - 0.5) * 0.3);
        }
    }

    @Override
    public float getSpeed() {
        return 1.0f;
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target);
    }
}
