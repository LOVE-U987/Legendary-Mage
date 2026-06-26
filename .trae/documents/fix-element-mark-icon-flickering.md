# 修复元素标记头顶显示闪烁问题

## 问题描述
元素标记头顶显示的图标存在闪烁/渲染异常，且 1/2/3 级图标的透明度差异在视觉上无法体现。

## 根本原因分析

### 问题 1（闪烁主因）：`EQUAL_DEPTH_TEST` 导致 Z-Fighting
`ElementMarkIconRenderer.java` 第 204 行使用 `RenderType.entityCutoutNoCull(texture)`，该渲染类型使用 **`EQUAL_DEPTH_TEST`（等于深度测试）**。当在 `RenderLivingEvent.Post` 中渲染比尔博板（billboard quad）时，其 Z 深度与已渲染的生物模型深度值存在微小的重叠/冲突，导致了 **z-fighting**，表现为图标剧烈闪烁。

### 问题 2（透明度无效）：不支持 Alpha 混合
`entityCutoutNoCull` 使用 `NO_TRANSPARENCY`（无透明度）模式。代码中设置的 alpha 值（1级=180, 2级=220, 3级=255）全部被忽略——所有片元只要 alpha >= 0.1 就被视为完全不透明。因此 1/2/3 级图标视觉上看起来完全一样。

### 问题 3（代码冗余）：`wrapAsHolder()` 间接调用
`checkAndAddMark()` 方法中先通过 `ModEffects.BLOOD_MARK.get()` 获取原始 `MobEffect` 实例，然后使用 `BuiltInRegistries.MOB_EFFECT.wrapAsHolder()` 重新包装成 `Holder`。而 `ModEffects.BLOOD_MARK` 本身已经是 `DeferredHolder<MobEffect, ?>`（即 `Holder<MobEffect>`），可以直接使用。

### 问题 4（死代码）：未使用的变量和 import
- 第 7 行 `import com.mojang.blaze3d.systems.RenderSystem;` 未使用
- 第 226 行 `int color = (alpha << 24) | 0xFFFFFF;` 声明了但未使用

## 修改方案

### 修改文件
**`c:\Users\97128\Documents\GitHub\Legendary-Mage\src\main\java\com\legendarymage\legendarymagemod\client\renderer\ElementMarkIconRenderer.java`**

### 改动 1：修复渲染类型（解决闪烁 + 透明度问题）
**将 `RenderType.entityCutoutNoCull(texture)` 改为 `RenderType.entityTranslucentNoCull(texture)`**

原因：
- `entityTranslucentNoCull` 使用 `LEQUAL_DEPTH_TEST`（小于等于深度测试），避免 z-fighting 导致的闪烁
- 支持真正的 Alpha 混合，使 1/2/3 级的透明度差异能正确显示

### 改动 2：简化效果查找，直接使用 `DeferredHolder`
- 将 `checkAndAddMark()` 方法的 `MobEffect effect` 参数改为 `Holder<MobEffect> effectHolder`
- 修改 `getEntityElementMarks()` 中的调用，直接传入 `ModEffects.BLOOD_MARK` 等
- 移除 `BuiltInRegistries.MOB_EFFECT.wrapAsHolder()` 调用

### 改动 3：清理死代码
- 移除未使用的 `import com.mojang.blaze3d.systems.RenderSystem;`
- 移除未使用的 `int color = (alpha << 24) | 0xFFFFFF;` 变量声明

## 影响范围
- **仅影响客户端渲染**，不影响服务器逻辑和元素反应机制
- 修改后图标会：
  - ✅ 不再闪烁
  - ✅ 1级半透明、2级较亮、3级全亮（透明度差异可见）
  - ✅ 性能影响可忽略（头顶图标数量通常 ≤ 8 个）

## 验证步骤
1. 运行 `gradlew build` 确认编译通过
2. 启动游戏，用元素法术攻击实体
3. 观察实体头顶的图标是否不再闪烁
4. 观察不同等级的图标透明度是否有明显差异
