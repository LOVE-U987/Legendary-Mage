---
name: "legendarymage-trail-system"
description: "提供Legendary-Mage模组中拖尾特效系统的使用指南。Invoke when user asks how to use the trail effect system, add trails to spells/projectiles, or customize trail effects."
---

# Legendary-Mage 拖尾特效系统使用指南

## 概述

本系统为Legendary-Mage模组提供了一个**几何渲染**的拖尾特效系统，不使用粒子效果，而是使用三角形带（Triangle Strip）渲染技术，实现流畅、立体、发光的拖尾效果。

## 核心特性

- ✅ **几何渲染** - 使用OpenGL三角形渲染，非粒子效果
- ✅ **发光效果** - 支持发光（Glowing）和发光强度调节
- ✅ **年龄渐变** - 拖尾点随时间淡出
- ✅ **元素集成** - 与元素系统（火、冰、雷等）深度集成
- ✅ **独立渲染** - 每个拖尾独立渲染，不会相互连接
- ✅ **Billboard技术** - 拖尾始终面向相机，无自旋转问题

## 系统架构

```
SimpleTrailEffect (拖尾效果)
    ↓
SimpleTrailManager (管理器) ← 工厂方法创建预设拖尾
    ↓
SimpleTrailRenderer (渲染器) ← 使用独立RenderType渲染
    ↓
SimpleTrailClientHandler (客户端处理器) ← 事件驱动更新和渲染
```

## 快速开始

### 1. 为投射物添加拖尾

```java
public class MyProjectile extends AbstractMagicProjectile {
    
    // 拖尾实例
    private SimpleTrailEffect trail = null;
    private boolean trailInitialized = false;
    
    /**
     * 初始化拖尾
     */
    private void initializeTrail() {
        if (trailInitialized) return;
        
        // 使用UUID创建唯一ID，避免多个投射物共享拖尾
        String trailId = "my_projectile_" + this.getUUID().toString();
        
        SimpleTrailManager manager = SimpleTrailManager.getInstance();
        
        // 创建火元素拖尾
        trail = manager.createFireElementTrail(trailId, this);
        
        trailInitialized = true;
    }
    
    /**
     * 每tick更新拖尾
     */
    @Override
    public void trailParticles() {
        if (level.isClientSide()) {
            if (!trailInitialized) {
                initializeTrail();
            }
            // SimpleTrailEffect使用自动更新机制
            // 更新在SimpleTrailClientHandler中处理
        }
        
        // 原有粒子效果...
    }
    
    /**
     * 清理拖尾
     */
    @Override
    public void remove(RemovalReason reason) {
        if (trail != null && trail.isActive()) {
            trail.stop(); // 让拖尾自然淡出
        }
        super.remove(reason);
    }
}
```

### 2. 使用预设拖尾

```java
// 火元素拖尾 - 橙红渐变，高强度发光
trail = manager.createFireElementTrail(trailId, entity);

// 冰元素拖尾 - 深青到青色，中等发光
trail = manager.createIceElementTrail(trailId, entity);

// 雷元素拖尾 - 紫到白色，最强发光
trail = manager.createLightningElementTrail(trailId, entity);

// 毒元素拖尾 - 绿色渐变
trail = manager.createPoisonElementTrail(trailId, entity);

// 神圣元素拖尾 - 金色到白色
trail = manager.createHolyElementTrail(trailId, entity);

// 血元素拖尾 - 深红色渐变
trail = manager.createBloodElementTrail(trailId, entity);
```

### 3. 自定义拖尾

```java
SimpleTrailEffect trail = new SimpleTrailEffect(trailId, entity);

// 基础配置
trail.setWidth(0.1f);                    // 拖尾宽度
trail.setMaxHistory(12);                 // 最大历史点数
trail.setMinSpeed(0.001f);               // 最小速度阈值
trail.setUpdateInterval(1);              // 更新间隔（tick）

// 颜色配置（ARGB格式）
trail.setStartColor(0xFFFF4500);         // 起始颜色（橙红）
trail.setEndColor(0x80FF0000);           // 结束颜色（半透明红）

// 位置偏移
trail.setOffset(new Vec3(0, 0.1, 0));    // 位置偏移
trail.setBackwardShift(0.1f);            // 向后偏移
trail.setMotionShift(0.25f);             // 运动反方向偏移

// 发光效果
trail.setGlowing(true);                  // 启用发光
trail.setGlowIntensity(0.8f);            // 发光强度（0-1）

// 元素集成
trail.setElementType(ElementType.FIRE);  // 设置元素类型
trail.setSpawnElementParticles(true);    // 产生元素粒子

// 淡出配置
trail.setFadeSpeed(0.05f);               // 淡出速度
```

## 高级用法

### 创建自定义预设

在 `SimpleTrailEffect` 类中添加静态工厂方法：

```java
/**
 * 创建自定义拖尾预设
 */
public static SimpleTrailEffect createCustomTrail(String id, Entity entity) {
    SimpleTrailEffect trail = new SimpleTrailEffect(id, entity);
    trail.setWidth(0.15f);
    trail.setMaxHistory(15);
    trail.setStartColor(0xFF00FF00);  // 绿色
    trail.setEndColor(0x8000FF00);    // 半透明绿
    trail.setGlowing(true);
    trail.setGlowIntensity(0.6f);
    return trail;
}
```

然后在 `SimpleTrailManager` 中添加对应方法：

```java
public SimpleTrailEffect createCustomTrail(String id, Entity entity) {
    SimpleTrailEffect trail = SimpleTrailEffect.createCustomTrail(id, entity);
    trails.put(id, trail);
    return trail;
}
```

### 手动控制拖尾生命周期

```java
// 停止拖尾（触发淡出）
trail.stop();

// 检查拖尾状态
if (trail.isActive()) { ... }           // 是否活跃
if (trail.isFading()) { ... }           // 是否正在淡出
if (trail.shouldRemove()) { ... }       // 是否可以移除

// 获取拖尾信息
int pointCount = trail.getPointCount(); // 当前点数
String id = trail.getId();              // 拖尾ID
```

### 管理器操作

```java
SimpleTrailManager manager = SimpleTrailManager.getInstance();

// 停止特定拖尾
manager.stopTrail(trailId);

// 移除特定拖尾
manager.removeTrail(trailId);

// 获取拖尾
SimpleTrailEffect trail = manager.getTrail(trailId);

// 获取所有拖尾
List<SimpleTrailEffect> allTrails = manager.getAllTrails();

// 获取活跃拖尾数量
int count = manager.getActiveTrailCount();

// 清除所有拖尾
manager.clearAll();
```

## 颜色格式说明

拖尾系统使用 **ARGB** 格式（与Minecraft一致）：

```java
int color = 0xAARRGGBB;

// 示例：
0xFFFF4500  // 不透明橙红色
0x80FF0000  // 半透明红色（50%透明）
0x00FFFFFF  // 完全透明白色
```

## 注意事项

### ⚠️ 关键要点

1. **使用UUID创建唯一ID**
   ```java
   // ✅ 正确 - 使用UUID确保唯一性
   String trailId = "trail_" + this.getUUID().toString();
   
   // ❌ 错误 - getId()在客户端可能重复
   String trailId = "trail_" + this.getId();
   ```

2. **延迟初始化**
   - 拖尾应在 `trailParticles()` 中延迟初始化
   - 确保只在客户端（`level.isClientSide()`）创建拖尾

3. **清理资源**
   - 投射物移除时调用 `trail.stop()` 让拖尾自然淡出
   - 不要直接设为null，让淡出效果完成

4. **性能考虑**
   - `maxHistory` 控制拖尾长度，建议 8-15
   - `updateInterval` 控制更新频率，建议 1-2
   - 避免创建过多拖尾（>50个同时活跃）

### 🔧 调试

启用调试输出查看拖尾状态：

```java
// 在配置中启用
Config.ELEMENTAL_BARRAGE_DEBUG_OUTPUT.set(true);

// 查看日志输出：
// [简单拖尾] 创建拖尾: trail_uuid_xxx
// [简单拖尾] 活跃拖尾数: 5
// [简单拖尾渲染] 渲染了 5 个拖尾
```

## 故障排除

| 问题 | 可能原因 | 解决方案 |
|------|---------|---------|
| 拖尾不显示 | 未在客户端初始化 | 确保 `level.isClientSide()` 检查 |
| 拖尾相互连接 | ID重复 | 使用 `getUUID()` 而非 `getId()` |
| 拖尾自旋转 | 宽度方向计算问题 | 已修复，使用Billboard技术 |
| 拖尾不淡出 | 未调用stop() | 在remove()中调用 `trail.stop()` |
| 性能问题 | 拖尾过多/过长 | 减少 `maxHistory` 或增加 `updateInterval` |

## 示例：完整法术集成

参考 `ElementalOrbProjectile` 类的实现：

```java
public class ElementalOrbProjectile extends AbstractMagicProjectile {
    
    private SimpleTrailEffect orbTrail = null;
    private boolean trailInitialized = false;
    
    private void initializeOrbTrail() {
        if (trailInitialized) return;
        
        String trailId = "elemental_orb_" + this.getUUID().toString();
        SimpleTrailManager manager = SimpleTrailManager.getInstance();
        
        switch (getOrbType()) {
            case ICE -> orbTrail = manager.createIceElementTrail(trailId, this);
            case FIRE -> orbTrail = manager.createFireElementTrail(trailId, this);
            case LIGHTNING -> orbTrail = manager.createLightningElementTrail(trailId, this);
        }
        
        trailInitialized = true;
    }
    
    @Override
    public void trailParticles() {
        if (level.isClientSide()) {
            if (!trailInitialized) initializeOrbTrail();
        }
        // 原有粒子效果...
    }
    
    private void cleanupTrail() {
        if (orbTrail != null) {
            if (orbTrail.isActive()) {
                orbTrail.stop();
            }
            orbTrail = null;
        }
    }
    
    @Override
    public void remove(RemovalReason reason) {
        cleanupTrail();
        super.remove(reason);
    }
}
```

## 相关文件

- `SimpleTrailEffect.java` - 拖尾效果核心类
- `SimpleTrailManager.java` - 拖尾管理器
- `SimpleTrailRenderer.java` - 拖尾渲染器
- `SimpleTrailClientHandler.java` - 客户端事件处理
- `ElementalOrbProjectile.java` - 使用示例

## 版本信息

- **版本**: 1.2.0
- **作者**: Love_U
- **最后更新**: 2026-04-12
- **适用版本**: NeoForge 1.21.1
