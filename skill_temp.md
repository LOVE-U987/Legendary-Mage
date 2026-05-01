# NeoForge 1.21.1 配置界面开发指南

## 概述

本 Skill 记录 NeoForge 1.21.1 环境下开发 Minecraft 模组配置界面的完整经验，包括：
- 基础配置界面架构
- 常见问题及解决方案
- 渲染优化技巧
- 滚动列表实现
- 控件裁剪处理
- 动画系统实现
- 语言文件配置

## 基础架构

### 1. 主配置屏幕类

```java
public class ModConfigScreen extends Screen {
    private final Screen parent;
    private final List<AbstractWidget> configWidgets = new ArrayList<>();
    private int scrollOffset = 0;
    
    // 布局常量
    private static final int LIST_TOP = 40;
    private static final int LIST_BOTTOM = 40;
    private static final int PADDING = 20;
    private static final int ROW_HEIGHT = 25;
    private static final int CATEGORY_HEIGHT = 20;
    
    public ModConfigScreen(Screen parent) {
        super(Component.literal("配置标题"));
        this.parent = parent;
    }
}
```

### 2. 初始化控件

```java
@Override
protected void init() {
    this.configWidgets.clear();
    int currentY = LIST_TOP;
    int widgetX = this.width - PADDING - 110;
    
    // 添加配置行
    currentY = addConfigRow(currentY, "配置项名称", "悬停提示",
        createBooleanButton(true, value -> { /* 处理变化 */ }),
        widgetX);
    
    // 添加返回按钮
    this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
        .bounds(this.width / 2 - 100, this.height - 28, 200, 20)
        .build());
}
```

### 3. 创建控件工厂方法

```java
private CycleButton<Boolean> createBooleanButton(boolean initialValue, Consumer<Boolean> onChange) {
    return CycleButton.booleanBuilder(
        Component.literal("是"),
        Component.literal("否")
    ).displayOnlyValue()
    .withInitialValue(initialValue)
    .create(0, 0, 100, 20, Component.empty(),
        (button, value) -> onChange.accept(value));
}

private EditBox createDoubleEditBox(double initialValue, double min, double max, Consumer<Double> onChange) {
    EditBox box = new EditBox(this.minecraft.font, 0, 0, 100, 20, Component.empty());
    box.setValue(String.valueOf(initialValue));
    box.setResponder(value -> {
        try {
            double val = Double.parseDouble(value);
            if (val >= min && val <= max) {
                onChange.accept(val);
            }
        } catch (NumberFormatException ignored) {}
    });
    return box;
}
```

## 关键问题及解决方案

### 问题 1：背景模糊导致文字模糊

**现象**：配置界面打开后，所有文字都模糊不清，像被糊住一样。

**原因**：Minecraft 1.21 的 `Screen.renderBackground()` 会自动调用 `renderBlurredBackground()`，对游戏世界截图并应用高斯模糊着色器。这个后处理效果会影响后续渲染的所有内容。

**解决方案**：
```java
@Override
protected void renderBlurredBackground(float partialTick) {
    // 空实现 - 完全禁用模糊背景
}
```

**注意**：这是 Epic Fight 模组使用的相同技术，可以完全禁用背景模糊效果。

### 问题 2：按钮/控件超出边界不裁剪

**现象**：滚动时，按钮突然出现在列表边界外，而不是像文字那样被遮挡。

**原因**：调用 `super.render()` 会渲染所有按钮，但这个调用在裁剪区域之外，导致按钮不受裁剪影响。

**解决方案**：手动控制按钮渲染顺序

```java
@Override
public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    // 1. 渲染背景
    guiGraphics.fill(0, 0, this.width, this.height, 0xFF2C2C2C);
    
    // 2. 启用裁剪区域
    guiGraphics.enableScissor(PADDING, LIST_TOP, this.width - PADDING, this.height - LIST_BOTTOM);
    
    // 3. 在裁剪区域内渲染文字
    renderConfigRows(guiGraphics);
    
    // 4. 在裁剪区域内手动渲染按钮（关键！）
    for (AbstractWidget widget : configWidgets) {
        if (widget.visible) {
            widget.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }
    
    // 5. 禁用裁剪区域
    guiGraphics.disableScissor();
    
    // 6. 渲染不需要裁剪的控件（如完成按钮）
    for (var renderable : this.renderables) {
        if (renderable instanceof Button button) {
            button.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }
}
```

### 问题 3：控件可见性控制

**现象**：超出边界的控件仍然可以交互（点击）。

**解决方案**：根据位置动态设置 `visible` 属性

```java
private void updateWidget(int index, int y, int x) {
    AbstractWidget widget = configWidgets.get(index);
    widget.setX(x);
    widget.setY(y);
    // 只有当控件在可视区域内时才显示
    boolean visible = y + ROW_HEIGHT > LIST_TOP && y < this.height - LIST_BOTTOM;
    widget.visible = visible;
}
```

### 问题 4：鼠标滚轮事件处理

**现象**：滚轮滚动不流畅或无效。

**解决方案**：
```java
@Override
public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
    int maxScroll = Math.max(0, totalContentHeight - (this.height - LIST_TOP - LIST_BOTTOM));
    scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - scrollY * 10));
    updateWidgetPositions();
    return true;
}
```

### 问题 5：悬停提示（Tooltip）

**实现方式**：
```java
widget.setTooltip(Tooltip.create(Component.literal("提示文字")));
```

**注意**：Minecraft 原生支持淡入淡出动画效果，无需额外实现。

## 动画系统实现

### 1. 界面打开动画

实现面板从下方滑入、透明度渐变的打开动画：

```java
public class ModernConfigScreen extends Screen {
    // 界面打开动画进度 (0.0 - 1.0)
    private float openAnimationProgress = 0.0f;
    
    // 动画开始时间
    private long animationStartTime = 0;
    
    // 是否首次打开
    private boolean firstOpen = true;
    
    public ModernConfigScreen(Screen parent) {
        super(Component.literal("配置标题"));
        this.parent = parent;
        this.animationStartTime = System.currentTimeMillis();
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 更新打开动画
        if (firstOpen) {
            long elapsed = System.currentTimeMillis() - animationStartTime;
            openAnimationProgress = Math.min(1.0f, elapsed / 400.0f);
            openAnimationProgress = easeOutCubic(openAnimationProgress);
            
            if (openAnimationProgress >= 1.0f) {
                firstOpen = false;
            }
            
            // 动画期间持续重绘
            if (firstOpen) {
                this.init();
            }
        }
        
        // 应用面板打开动画
        int animPanelTop = panelTop + (int) ((1.0f - openAnimationProgress) * 30);
        int animAlpha = (int) (openAnimationProgress * 255);
        
        // 渲染面板背景（带透明度）
        renderPanelBackground(graphics, animPanelTop, animAlpha);
        
        // 标题动画
        int titleY = panelTop - 50 + (int) ((1.0f - openAnimationProgress) * 20);
        int titleAlpha = (animAlpha << 24) | 0xFFFFFF;
        graphics.drawCenteredString(this.font, this.title, this.width / 2, titleY, titleAlpha);
    }
    
    // 缓动函数
    private float easeOutCubic(float t) {
        return 1.0f - (float) Math.pow(1.0f - t, 3);
    }
}
```

### 2. 分类切换动画

区分首次打开和切换分类的动画：

```java
// 切换分类时的动画进度
private float categorySwitchProgress = 1.0f;

// 是否正在切换分类
private boolean isSwitchingCategory = false;

// 分类切换动画开始时间
private long categorySwitchStartTime = 0;

private void selectCategory(int index) {
    if (index != selectedCategory) {
        selectedCategory = index;
        scrollOffset = 0;
        needsSave = false;
        isSwitchingCategory = true;
        categorySwitchStartTime = System.currentTimeMillis();
        categorySwitchProgress = 0.0f;
        this.init();
    }
}

// 在 render() 中处理
if (isSwitchingCategory) {
    long elapsed = System.currentTimeMillis() - categorySwitchStartTime;
    categorySwitchProgress = Math.min(1.0f, elapsed / 300.0f);
    categorySwitchProgress = easeOutCubic(categorySwitchProgress);
    
    if (categorySwitchProgress >= 1.0f) {
        isSwitchingCategory = false;
    }
    
    if (isSwitchingCategory) {
        this.init();
    }
}
```

### 3. 内容项进入动画

每个配置项依次进入的动画效果：

```java
// 内容项进入动画进度列表
private final List<Float> entryAnimationProgress = new ArrayList<>();

// 当前项索引（用于动画延迟）
private int entryIndex = 0;

private float getEntryAnimationProgress(int index) {
    // 首次打开或切换分类时播放动画
    if (!firstOpen && !isSwitchingCategory) return 1.0f;
    
    long startTime = firstOpen ? animationStartTime : categorySwitchStartTime;
    long elapsed = System.currentTimeMillis() - startTime;
    float delay = index * 40;  // 每项延迟40ms
    float duration = 250;      // 动画持续时间250ms
    float progress = Math.max(0.0f, Math.min(1.0f, (elapsed - delay) / duration));
    
    return easeOutCubic(progress);
}

// 在添加配置项时使用
private int addBooleanRow(int x, int y, int width, int height, 
                         String label, ModConfigSpec.BooleanValue configValue) {
    float entryProgress = getEntryAnimationProgress(entryIndex);
    int animOffset = (int) ((1.0f - entryProgress) * 20);  // 20像素位移
    int animAlpha = (int) (entryProgress * 255);           // 透明度渐变
    
    // 应用动画偏移
    int drawY = y - scrollOffset - animOffset;
    
    // 渲染标签（带动画）
    if (drawY >= panelTop + 10 && drawY <= panelTop + panelHeight - 10) {
        addLabel(x, drawY + 6, label, animAlpha);
    }
    
    entryIndex++;
    return y + height;
}
```

### 4. 缓动函数

```java
// easeOutCubic: 1 - (1 - t)^3
private float easeOutCubic(float t) {
    return 1.0f - (float) Math.pow(1.0f - t, 3);
}

// easeOutQuart: 1 - (1 - t)^4
private float easeOutQuart(float t) {
    return 1.0f - (float) Math.pow(1.0f - t, 4);
}

// easeInOutCubic
private float easeInOutCubic(float t) {
    return t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
}
```

## 语言文件配置

### 1. 翻译键命名规范

使用统一前缀，按功能分组：

```
modid.modern_config.title                    // 界面标题
modid.modern_config.category.spells          // 分类名称
modid.modern_config.category.spells.tooltip  // 分类提示
modid.modern_config.save                     // 按钮文字
modid.modern_config.save.tooltip             // 按钮提示
modid.modern_config.value.on                 // 通用值：开启
modid.modern_config.value.off                // 通用值：关闭
modid.modern_config.unsaved                  // 未保存提示
```

### 2. 配置项翻译键

```
modid.modern_config.resurrection_rune.buff_enabled              // 标签
modid.modern_config.resurrection_rune.buff_enabled.tooltip      // 提示
modid.modern_config.resurrection_rune.spell_power_multiplier    // 标签
modid.modern_config.resurrection_rune.spell_power_multiplier.tooltip  // 提示
```

### 3. 代码中使用翻译

```java
private static final String TRANSLATION_PREFIX = "legendarymage.modern_config";

// 标题
public ModernConfigScreen(Screen parent) {
    super(Component.translatable(TRANSLATION_PREFIX + ".title"));
    this.parent = parent;
}

// 分类
private void initializeCategories() {
    categories.add(new ConfigCategory("spells", 
        TRANSLATION_PREFIX + ".category.spells",
        TRANSLATION_PREFIX + ".category.spells.tooltip"));
}

// 配置项标签
String label = Component.translatable(TRANSLATION_PREFIX + "." + translationKey).getString();
String tooltip = Component.translatable(TRANSLATION_PREFIX + "." + translationKey + ".tooltip").getString();

// 按钮文字
Component.translatable(TRANSLATION_PREFIX + ".save")
Component.translatable(TRANSLATION_PREFIX + ".cancel")

// 通用值
Component.translatable(TRANSLATION_PREFIX + ".value.on")
Component.translatable(TRANSLATION_PREFIX + ".value.off")
```

### 4. 语言文件示例

**zh_cn.json**：
```json
{
  "legendarymage.modern_config.title": "Legendary Mage Config",
  "legendarymage.modern_config.category.spells": "法术配置",
  "legendarymage.modern_config.category.spells.tooltip": "配置各种法术的参数和效果",
  "legendarymage.modern_config.save": "保存",
  "legendarymage.modern_config.save.tooltip": "保存所有更改并关闭界面",
  "legendarymage.modern_config.value.on": "开启",
  "legendarymage.modern_config.value.off": "关闭",
  "legendarymage.modern_config.resurrection_rune.buff_enabled": "复苏符文 - 启用BUFF",
  "legendarymage.modern_config.resurrection_rune.buff_enabled.tooltip": "是否启用复苏符文的BUFF效果"
}
```

**en_us.json**：
```json
{
  "legendarymage.modern_config.title": "Legendary Mage Config",
  "legendarymage.modern_config.category.spells": "Spell Config",
  "legendarymage.modern_config.category.spells.tooltip": "Configure spell parameters and effects",
  "legendarymage.modern_config.save": "Save",
  "legendarymage.modern_config.save.tooltip": "Save all changes and close",
  "legendarymage.modern_config.value.on": "On",
  "legendarymage.modern_config.value.off": "Off",
  "legendarymage.modern_config.resurrection_rune.buff_enabled": "Resurrection Rune - Enable Buff",
  "legendarymage.modern_config.resurrection_rune.buff_enabled.tooltip": "Enable buff effects for Resurrection Rune"
}
```

### 5. 注意事项

1. **键值对完整性**：中英文文件必须包含完全相同的键
2. **命名规范**：使用小写字母和下划线，避免特殊字符
3. **分组管理**：按功能模块分组，便于维护
4. **避免硬编码**：所有界面文字必须通过翻译键获取
5. **动态内容**：使用 `Component.translatable()` 而不是 `Component.literal()`

## 完整渲染流程

```java
@Override
public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    // 1. 纯色背景（禁用模糊）
    guiGraphics.fill(0, 0, this.width, this.height, 0xFF2C2C2C);
    
    // 2. 列表区域背景
    guiGraphics.fill(PADDING, LIST_TOP, this.width - PADDING, this.height - LIST_BOTTOM, 0xCC000000);
    
    // 3. 标题和提示（在裁剪区域外）
    guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
    
    // 4. 启用裁剪
    guiGraphics.enableScissor(PADDING, LIST_TOP, this.width - PADDING, this.height - LIST_BOTTOM);
    
    // 5. 渲染文字和按钮（在裁剪区域内）
    renderConfigRows(guiGraphics);
    renderConfigWidgets(guiGraphics, mouseX, mouseY, partialTick);
    
    // 6. 禁用裁剪
    guiGraphics.disableScissor();
    
    // 7. 渲染固定控件（如完成按钮）
    renderFixedWidgets(guiGraphics, mouseX, mouseY, partialTick);
}
```

## 已知限制

1. **ContainerObjectSelectionList 问题**：NeoForge 1.21.1 中该类存在渲染兼容性问题，建议使用自定义布局
2. **renderBackground() 副作用**：会自动触发模糊效果，建议使用 `guiGraphics.fill()` 替代
3. **super.render() 行为**：会渲染所有已注册的控件，但不受裁剪区域影响

## 最佳实践

1. **始终禁用模糊背景**：重写 `renderBlurredBackground()` 为空实现
2. **手动控制渲染顺序**：不要依赖 `super.render()`，手动渲染所有控件
3. **使用裁剪区域**：`guiGraphics.enableScissor()` 和 `guiGraphics.disableScissor()`
4. **动态可见性**：根据控件位置动态设置 `visible` 属性
5. **纯色背景**：使用 `guiGraphics.fill()` 而不是 `renderBackground()`
6. **动画分离**：区分首次打开动画和分类切换动画
7. **语言文件**：所有文字使用翻译键，避免硬编码

## 参考实现

完整的配置界面实现可以参考：
- Epic Fight 模组的 `TPSSettingScreen.java`
- 本项目的 `AdaptiveNemesisConfigScreen.java`
