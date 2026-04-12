package com.legendarymage.legendarymagemod.trail;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.element.ElementType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 简化版拖尾效果 - 类似Perception的实现
 * 
 * 【设计特点】
 * - 直接连线，不插值
 * - ARGB颜色格式
 * - 实体死亡后整体淡出
 * - 向运动反方向偏移
 * 
 * 【与Perception的对应关系】
 * - size -> width
 * - maxPoints -> maxHistory
 * - minSpeed -> minSpeed
 * - updateFrequency -> updateInterval
 * - fadeInColor/fadeOutColor -> startColor/endColor
 * - positionOffset -> offset
 * - backwardShift -> backwardShift
 * - motionShift -> motionShift
 * 
 * @author Love_U
 * @version 1.0.6
 */
public class SimpleTrailEffect {

    // ==================== 配置参数 ====================
    
    /**
     * 拖尾宽度
     */
    private float width = 0.15f;
    
    /**
     * 最大历史点数
     */
    private int maxHistory = 10;
    
    /**
     * 最小速度阈值
     */
    private float minSpeed = 0.001f;
    
    /**
     * 更新间隔（tick）
     */
    private int updateInterval = 1;
    
    /**
     * 起始颜色 (ARGB)
     */
    private int startColor = 0xFFFFFFFF;
    
    /**
     * 结束颜色 (ARGB)
     */
    private int endColor = 0x00FFFFFF;
    
    /**
     * 位置偏移
     */
    private Vec3 offset = Vec3.ZERO;
    
    /**
     * 向后偏移（远离玩家）
     */
    private float backwardShift = 0f;
    
    /**
     * 运动反方向偏移
     */
    private float motionShift = 0.25f;
    
    // ==================== 运行时数据 ====================
    
    /**
     * 历史位置队列
     */
    private final Deque<TrailPoint> history;
    
    /**
     * 绑定的实体
     */
    private Entity entity;
    
    /**
     * 拖尾ID
     */
    private final String id;
    
    /**
     * 是否活跃
     */
    private boolean active = true;
    
    /**
     * 更新计数器
     */
    private int updateCounter = 0;
    
    /**
     * 淡出进度（0-1）
     */
    private float fadeProgress = 0f;
    
    /**
     * 淡出速度
     */
    private float fadeSpeed = 0.05f;

    /**
     * 是否发光
     */
    private boolean glowing = false;

    /**
     * 发光强度 (0-1)
     */
    private float glowIntensity = 0.5f;

    /**
     * 元素类型（用于元素反应集成）
     */
    private ElementType elementType = null;

    /**
     * 是否产生元素粒子
     */
    private boolean spawnElementParticles = false;

    /**
     * 粒子生成间隔（tick）
     */
    private int particleSpawnInterval = 5;

    /**
     * 轨迹点
     */
    public static class TrailPoint {
        public final Vec3 position;
        public final int tick;
        public final long timestamp;
        
        public TrailPoint(Vec3 position, int tick) {
            this.position = position;
            this.tick = tick;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    // ==================== 构造函数 ====================
    
    public SimpleTrailEffect(String id, Entity entity) {
        this.id = id;
        this.entity = entity;
        this.history = new ArrayDeque<>();
    }
    
    // ==================== 更新逻辑 ====================
    
    /**
     * 更新拖尾
     * 
     * @param partialTick 部分tick
     */
    public void update(float partialTick) {
        if (entity == null) return;
        
        // 检查实体是否存活
        if (!entity.isAlive()) {
            active = false;
        }
        
        if (active) {
            // 更新计数
            updateCounter++;
            if (updateCounter < updateInterval) {
                return;
            }
            updateCounter = 0;
            
            // 检查速度
            if (entity.getKnownMovement().length() < minSpeed) {
                return;
            }
            
            // 计算拖尾位置
            Vec3 position = calculateTrailPosition(partialTick);
            
            // 添加到历史
            history.addLast(new TrailPoint(position, entity.tickCount));
            
            // 限制历史长度
            while (history.size() > maxHistory) {
                history.pollFirst();
            }
        } else {
            // 淡出阶段
            fadeProgress += fadeSpeed;
            if (fadeProgress >= 1.0f) {
                fadeProgress = 1.0f;
                history.clear();
            }
        }
    }
    
    /**
     * 计算拖尾位置
     * 参考Perception的getTrailPosition实现
     */
    private Vec3 calculateTrailPosition(float partialTick) {
        // 获取实体位置
        Vec3 entityPos = entity.tickCount > 1 
            ? entity.getPosition(partialTick) 
            : entity.position();
        
        // 向运动反方向偏移
        Vec3 motion = entity.getDeltaMovement();
        if (motion.lengthSqr() > 0.0001) {
            entityPos = entityPos.add(motion.normalize().scale(-motionShift));
        }
        
        // 应用位置偏移
        entityPos = entityPos.add(offset);
        
        // 向远离玩家的方向偏移
        // 简化处理：向运动反方向再偏移一点
        if (backwardShift > 0 && motion.lengthSqr() > 0.0001) {
            entityPos = entityPos.add(motion.normalize().scale(-backwardShift));
        }
        
        return entityPos;
    }
    
    /**
     * 停止拖尾
     */
    public void stop() {
        active = false;
    }
    
    // ==================== 渲染查询 ====================
    
    /**
     * 获取历史点
     */
    public Deque<TrailPoint> getHistory() {
        return history;
    }
    
    /**
     * 获取当前有效颜色（考虑淡出）
     */
    public int getCurrentStartColor() {
        return applyFade(startColor);
    }
    
    public int getCurrentEndColor() {
        return applyFade(endColor);
    }
    
    /**
     * 应用淡出到颜色
     */
    private int applyFade(int color) {
        if (fadeProgress <= 0) return color;
        
        int alpha = (color >> 24) & 0xFF;
        int rgb = color & 0x00FFFFFF;
        
        // 淡出alpha
        alpha = (int) (alpha * (1.0f - fadeProgress));
        
        return (alpha << 24) | rgb;
    }
    
    /**
     * 获取两点之间的颜色插值
     */
    public int getColorAt(float progress) {
        int start = getCurrentStartColor();
        int end = getCurrentEndColor();
        
        int startA = (start >> 24) & 0xFF;
        int startR = (start >> 16) & 0xFF;
        int startG = (start >> 8) & 0xFF;
        int startB = start & 0xFF;
        
        int endA = (end >> 24) & 0xFF;
        int endR = (end >> 16) & 0xFF;
        int endG = (end >> 8) & 0xFF;
        int endB = end & 0xFF;
        
        int a = (int) (startA + (endA - startA) * progress);
        int r = (int) (startR + (endR - startR) * progress);
        int g = (int) (startG + (endG - startG) * progress);
        int b = (int) (startB + (endB - startB) * progress);
        
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    
    // ==================== Getters & Setters ====================
    
    public String getId() { return id; }
    public boolean isActive() { return active; }
    public int getPointCount() { return history.size(); }
    public float getWidth() { return width; }
    public boolean isFading() { return !active && fadeProgress < 1.0f; }
    public boolean shouldRemove() { return !active && fadeProgress >= 1.0f; }
    
    public void setWidth(float width) { this.width = width; }
    public int getMaxHistory() { return maxHistory; }
    public void setMaxHistory(int max) { this.maxHistory = max; }
    public void setMinSpeed(float speed) { this.minSpeed = speed; }
    public void setUpdateInterval(int interval) { this.updateInterval = interval; }
    public void setStartColor(int color) { this.startColor = color; }
    public void setEndColor(int color) { this.endColor = color; }
    public void setOffset(Vec3 offset) { this.offset = offset; }
    public void setBackwardShift(float shift) { this.backwardShift = shift; }
    public void setMotionShift(float shift) { this.motionShift = shift; }
    public void setFadeSpeed(float speed) { this.fadeSpeed = speed; }

    public boolean isGlowing() { return glowing; }
    public void setGlowing(boolean glowing) { this.glowing = glowing; }

    public float getGlowIntensity() { return glowIntensity; }
    public void setGlowIntensity(float intensity) { this.glowIntensity = intensity; }

    public ElementType getElementType() { return elementType; }
    public void setElementType(ElementType type) { this.elementType = type; }

    public boolean isSpawnElementParticles() { return spawnElementParticles; }
    public void setSpawnElementParticles(boolean spawn) { this.spawnElementParticles = spawn; }

    public int getParticleSpawnInterval() { return particleSpawnInterval; }
    public void setParticleSpawnInterval(int interval) { this.particleSpawnInterval = interval; }

    // ==================== 预设配置 ====================
    
    /**
     * 创建箭头拖尾配置
     */
    public static SimpleTrailEffect createArrowTrail(String id, Entity entity) {
        SimpleTrailEffect trail = new SimpleTrailEffect(id, entity);
        trail.setWidth(0.075f);
        trail.setMaxHistory(5);
        trail.setMinSpeed(0.001f);
        trail.setUpdateInterval(1);
        trail.setStartColor(0x00000000);  // 透明
        trail.setEndColor(0x80000000);    // 半透明黑
        return trail;
    }
    
    /**
     * 创建光谱箭拖尾配置
     */
    public static SimpleTrailEffect createSpectralArrowTrail(String id, Entity entity) {
        SimpleTrailEffect trail = new SimpleTrailEffect(id, entity);
        trail.setWidth(0.075f);
        trail.setMaxHistory(10);
        trail.setMinSpeed(0.001f);
        trail.setUpdateInterval(1);
        trail.setStartColor(0xFFFFD505);  // 金黄色
        trail.setEndColor(0x80000000);    // 半透明黑
        return trail;
    }
    
    /**
     * 创建三叉戟拖尾配置
     */
    public static SimpleTrailEffect createTridentTrail(String id, Entity entity) {
        SimpleTrailEffect trail = new SimpleTrailEffect(id, entity);
        trail.setWidth(0.05f);
        trail.setMaxHistory(10);
        trail.setMinSpeed(0.001f);
        trail.setUpdateInterval(1);
        trail.setStartColor(0xFF00FFEA);  // 青色
        trail.setEndColor(0x80000000);    // 半透明黑
        return trail;
    }
    
    /**
     * 创建经验球拖尾配置
     */
    public static SimpleTrailEffect createExperienceOrbTrail(String id, Entity entity) {
        SimpleTrailEffect trail = new SimpleTrailEffect(id, entity);
        trail.setWidth(0.075f);
        trail.setMaxHistory(5);
        trail.setMinSpeed(0.001f);
        trail.setUpdateInterval(1);
        trail.setStartColor(0xFF66FF00);  // 绿色
        trail.setEndColor(0x80D9FF00);    // 黄绿色
        trail.setOffset(new Vec3(0, 0.175, 0));
        trail.setBackwardShift(0.1f);
        trail.setMotionShift(0.1f);
        return trail;
    }
    
    /**
     * 创建烟花火箭拖尾配置
     */
    public static SimpleTrailEffect createFireworkTrail(String id, Entity entity) {
        SimpleTrailEffect trail = new SimpleTrailEffect(id, entity);
        trail.setWidth(0.075f);
        trail.setMaxHistory(15);
        trail.setMinSpeed(0.001f);
        trail.setUpdateInterval(1);
        trail.setStartColor(0x80FFFFFF);  // 半透明白
        trail.setEndColor(0xFFFF0000);    // 红色
        trail.setOffset(new Vec3(0, 0.25, 0));
        trail.setMotionShift(1.0f);
        return trail;
    }

    // ==================== 元素拖尾预设 ====================

    /**
     * 创建火元素拖尾
     */
    public static SimpleTrailEffect createFireElementTrail(String id, Entity entity) {
        SimpleTrailEffect trail = new SimpleTrailEffect(id, entity);
        trail.setWidth(0.1f);
        trail.setMaxHistory(12);
        trail.setMinSpeed(0.001f);
        trail.setUpdateInterval(1);
        trail.setStartColor(0xFFFF4500);  // 橙红色
        trail.setEndColor(0x80FF0000);    // 红色
        trail.setGlowing(true);
        trail.setGlowIntensity(0.8f);
        trail.setElementType(ElementType.FIRE);
        trail.setSpawnElementParticles(true);
        return trail;
    }

    /**
     * 创建冰元素拖尾
     */
    public static SimpleTrailEffect createIceElementTrail(String id, Entity entity) {
        SimpleTrailEffect trail = new SimpleTrailEffect(id, entity);
        trail.setWidth(0.08f);
        trail.setMaxHistory(10);
        trail.setMinSpeed(0.001f);
        trail.setUpdateInterval(1);
        trail.setStartColor(0xFF00CED1);  // 深青色
        trail.setEndColor(0x8000FFFF);    // 青色
        trail.setGlowing(true);
        trail.setGlowIntensity(0.6f);
        trail.setElementType(ElementType.ICE);
        trail.setSpawnElementParticles(true);
        return trail;
    }

    /**
     * 创建雷元素拖尾
     */
    public static SimpleTrailEffect createLightningElementTrail(String id, Entity entity) {
        SimpleTrailEffect trail = new SimpleTrailEffect(id, entity);
        trail.setWidth(0.06f);
        trail.setMaxHistory(8);
        trail.setMinSpeed(0.001f);
        trail.setUpdateInterval(1);
        trail.setStartColor(0xFF9400D3);  // 紫色
        trail.setEndColor(0x80FFFFFF);    // 白色
        trail.setGlowing(true);
        trail.setGlowIntensity(1.0f);
        trail.setElementType(ElementType.LIGHTNING);
        trail.setSpawnElementParticles(true);
        return trail;
    }

    /**
     * 创建毒元素拖尾
     */
    public static SimpleTrailEffect createPoisonElementTrail(String id, Entity entity) {
        SimpleTrailEffect trail = new SimpleTrailEffect(id, entity);
        trail.setWidth(0.09f);
        trail.setMaxHistory(14);
        trail.setMinSpeed(0.001f);
        trail.setUpdateInterval(2);
        trail.setStartColor(0xFF32CD32);  // 绿色
        trail.setEndColor(0x80006400);    // 深绿色
        trail.setElementType(ElementType.POISON);
        trail.setSpawnElementParticles(true);
        return trail;
    }

    /**
     * 创建神圣元素拖尾
     */
    public static SimpleTrailEffect createHolyElementTrail(String id, Entity entity) {
        SimpleTrailEffect trail = new SimpleTrailEffect(id, entity);
        trail.setWidth(0.1f);
        trail.setMaxHistory(10);
        trail.setMinSpeed(0.001f);
        trail.setUpdateInterval(1);
        trail.setStartColor(0xFFFFD700);  // 金色
        trail.setEndColor(0x80FFFFFF);    // 白色
        trail.setGlowing(true);
        trail.setGlowIntensity(0.9f);
        trail.setElementType(ElementType.HOLY);
        trail.setSpawnElementParticles(true);
        return trail;
    }

    /**
     * 创建血元素拖尾
     */
    public static SimpleTrailEffect createBloodElementTrail(String id, Entity entity) {
        SimpleTrailEffect trail = new SimpleTrailEffect(id, entity);
        trail.setWidth(0.12f);
        trail.setMaxHistory(12);
        trail.setMinSpeed(0.001f);
        trail.setUpdateInterval(1);
        trail.setStartColor(0xFF8B0000);  // 深红色
        trail.setEndColor(0x804B0000);    // 暗红色
        trail.setElementType(ElementType.BLOOD);
        trail.setSpawnElementParticles(true);
        return trail;
    }
}
