# Legendary-Mage 法术图标绘制指南

> 基于模组源码（v1.0.6+）深度分析生成，所有数据均来自实际代码。

---

## 一、全局设计规范

### 1.1 流派色彩体系（从源码中的 `getTargetingColor()` 和 `SchoolType` 提取）

| 流派 | 颜色代码 | RGB | 视觉特征 |
|------|---------|-----|---------|
| 🔥 火系 (FIRE) | `#FF4D00` | (1.0, 0.3, 0.0) | 炽热橙红 |
| ❄️ 冰系 (ICE) | `#99CCFF` | (0.6, 0.8, 1.0) | 冰蓝冷色 |
| 🩸 血系 (BLOOD) | `#990000` | (0.6, 0.0, 0.0) | 深暗血红 |
| 💜 元素 (ELEMENT) | `#9B59B6` | 紫色 (0x9b59b6) | 神秘紫 |
| ⚡ 雷系 (LIGHTNING) | `#CCE5FF` | (0.8, 0.9, 1.0) | 亮白蓝 |
| 🗡️ 咒刃 (BLADE) | — | 外部流派 | ess_requiem 定义 |

### 1.2 尺寸与格式
- **尺寸**: 128×128 像素（与 Iron's Spellbooks 标准一致）
- **格式**: PNG，透明背景
- **风格**: Minecraft 像素艺术风格，高对比度，简洁可辨识
- **布局**: 居中构图，主体占画面 60-70%

### 1.3 图标加载机制（从源码确认）
```java
// 每个法术类都有类似代码：
private static final ResourceLocation SPELL_ICON = ResourceLocation.fromNamespaceAndPath(
    LegendaryMage.MODID, "textures/gui/spell_icons/<spell_id>.png");

public ResourceLocation getSpellIcon() { return SPELL_ICON; }
```
**路径规则**: `textures/gui/spell_icons/` + `法术ID.png`

---

## 二、已有图标（已有 9 个，可作风格参考）

| 法术ID | 文件名 | 流派 | 稀有度 |
|--------|-------|------|--------|
| resurrection_rune | resurrection_rune.png | BLOOD | 传奇 |
| implosion | implosion.png | FIRE | 传奇 |
| pyromaniac | pyromaniac.png | FIRE | 传奇 |
| living_ice_sculpture | living_ice_sculpture.png | ICE | 传奇 |
| blizzard | blizzard.png | ICE | 传奇 |
| focused_ice_cone | focused_ice_cone.png | ICE | 稀有-传奇 |
| ice_explosion_cone | ice_explosion_cone.png | ICE | 稀有-传奇 |
| elemental_barrage | elemental_barrage.png | ELEMENT | 稀有 |
| magic_shotgun | magic_shotgun.png | BLADE | 传奇 |

---

## 三、缺失图标详细设计（5个）

### 3.1 元素爆发 (elemental_burst) ⚠️ 缺失

```yaml
类别: 元素流派 / 长吟唱范围AOE
稀有度: 史诗 → 传奇  等级: 1-5
目标色: 紫色 (0.6, 0.2, 0.8)
施法音效: AMETHYST_BLOCK_CHIME (紫水晶钟声) → AMETHYST_BLOCK_HIT (紫水晶撞击)
```

**法术机制（源码分析）**:
- 以施法者为中心的范围爆炸，**轮流**造成火/冰/雷三种元素伤害
- 每种元素伤害单独享受对应流派的法术强度加成
- 自动施加对应的元素标记

**视觉表现建议**:
- **核心元素**: 一束三色能量从中心同时向外爆发——红色(火)、蓝色(冰)、黄色(雷)
- 三种颜色应该以螺旋或扇形交错的方式从中心发散
- 中心可以有一个紫色核心（0x9b59b6，元素流派标志色）向外辐射
- 外圈有冲击波效果，类似 blastwave 粒子

**AI 绘图提示词**:
```
pixel art spell icon, Minecraft style, 128x128, transparent background, top-down view.
A central purple glowing orb (hex #9B59B6) radiating three elemental energy waves outward:
- Fire wave: orange-red (#FF4D00) spiraling from top-right
- Ice wave: ice blue (#99CCFF) spiraling from bottom-left  
- Lightning wave: pale yellow (#CCE5FF) spiraling from top-left
The three colors intertwine like a triple-helix burst from the center.
Small elemental spark particles at the edges. Blastwave shockwave ring around the perimeter.
Clean game UI style, high contrast, bold shapes, no text.
```

**色彩参考**: 中心紫 #9B59B6 / 火橙 #FF4D00 / 冰蓝 #99CCFF / 雷白蓝 #CCE5FF

---

### 3.2 元素棱镜 (elemental_prism) ⚠️ 缺失

```yaml
类别: 元素流派 / 瞬发区域型
稀有度: 传奇  等级: 1-5
目标色: 紫色 (0.6, 0.2, 0.8)
施法音效: BEACON_POWER_SELECT (信标能量选择) → BEACON_ACTIVATE (信标激活)
```

**法术机制（源码分析）**:
- 瞬发创建一片持续20秒的元素棱镜区域
- 基础范围6格，每级+2格
- 每2秒扫描区域内敌人，共享元素标记
- **元素反应触发两次**（核心特性）
- 粒子效果: WITCH粒子（彩虹色雾）+ END_ROD（圆形光圈）+ FLASH（中心闪光）

**视觉表现建议**:
- **核心元素**: 一个六边形或三角形的棱镜晶体，折射出彩虹色光芒
- 棱镜底部有一圈紫色光环（END_ROD粒子效果）
- 从棱镜各面折射出三原色（红/蓝/黄）光束
- 顶部有信标光柱的暗示
- "双重"的概念可以通过两个重叠的半透明菱形来表达

**AI 绘图提示词**:
```
pixel art spell icon, Minecraft style, 128x128, transparent background, top-down view.
A hexagonal prism crystal floating at center, glowing with purple (#9B59B6) light.
The prism refracts beams of fire red (#FF4D00), ice blue (#99CCFF), and lightning yellow (#CCE5FF) outward in six directions.
A bright beacon-like pillar of light rises from the top of the prism.
Double-layered diamond outlines overlapping around the crystal, representing the "double reaction" effect.
Witch-particle-like rainbow sparkles scattered around the prism.
Clean game UI style, high contrast, geometric shapes, no text.
```

**色彩参考**: 晶体紫 #9B59B6 / 折射光 红#FF4D00 蓝#99CCFF 黄#CCE5FF

---

### 3.3 巨雪球 (giant_snowball) ⚠️ 缺失

```yaml
类别: 冰系 / 长吟唱投掷型
稀有度: 传奇  等级: 1-5
目标色: 冰蓝色 (0.6, 0.8, 1.0)
施法音效: SNOW_PLACE (放置雪) → GLASS_BREAK (玻璃破碎=冰爆)
```

**法术机制（源码分析）**:
- 先在头顶生成小型雪块，吟唱越长雪球越大（0.1f → 1.0f 缩放）
- 吟唱结束或中断时释放
- 击中造成巨大冰爆（基础8格范围，每级+1）
- 留下一片10秒的暴风雪力场（BizzardManager）
- 爆炸粒子: BlastwaveParticleOptions（冰蓝冲击波）+ SNOWFLAKE_PARTICLE（雪花）
- 施加缓慢III效果

**视觉表现建议**:
- **核心元素**: 一个正在被掷出的巨大雪球，带有冰晶纹理
- 雪球周围环绕着冰块碎片和雪花
- 背景暗示冰爆冲击波
- 雪球底部有空气扭曲的速度线
- 整体呈白色/冰蓝色调

**AI 绘图提示词**:
```
pixel art spell icon, Minecraft style, 128x128, transparent background, side-angle view.
A massive snowball (white with ice-blue #99CCFF highlights) being hurled forward.
Ice crystal fragments shattering off the snowball's surface.
Snowflake particles trailing behind.
Blue blastwave shockwave ring expanding outward from the snowball.
Frosty mist and cold air distortion at the edges.
Bottom section hints at a blizzard zone with swirling snow.
Clean game UI style, high contrast, dynamic motion, no text.
```

**色彩参考**: 主白 / 冰蓝 #99CCFF / 暗冰蓝 #6699CC

---

### 3.4 三向之矢 (tri_directional_arrow) ⚠️ 缺失

```yaml
类别: 元素流派 / 长吟唱投射型
稀有度: 史诗  等级: 1-8
音效: AMETHYST_BLOCK_CHIME (紫水晶钟声)
```

**法术机制（源码分析）**:
- 长吟唱后**依次**释放冰→火→雷→冰→…循环的元素箭
- 基础3支箭（冰、火、雷各1），每级+1支，最多15支
- 冰箭：冰冻+缓慢
- 火箭：燃烧+4格范围火焰伤害
- 雷箭：感电+4格范围雷电伤害
- 箭矢落地产生持续5秒的元素区域
- 沿视线方向直线发射（lookVec.scale）

**视觉表现建议**:
- **核心元素**: 三支不同颜色的箭呈扇形排列，左冰蓝、中火红、右雷黄
- 箭矢正在从一把元素弓中飞出
- 每支箭带有对应元素的尾迹
- 底部有5秒持续区域的暗示
- 强调"三向"(tri-directional)的对称布局

**AI 绘图提示词**:
```
pixel art spell icon, Minecraft style, 128x128, transparent background.
Three elemental arrows fanning out in a symmetrical spread pattern:
- Left arrow: ice blue (#99CCFF) with frost trail and snowflakes
- Center arrow: fire red (#FF4D00) with flame trail and embers  
- Right arrow: lightning yellow (#CCE5FF) with electric sparks
Each arrow leaves a colored elemental trail behind it.
The arrows emerge from a central purple (#9B59B6) magical bow or energy point.
Small ground impact zones of ice/fire/lightning at the bottom edges.
Clean game UI style, high contrast, symmetrical composition, no text.
```

**色彩参考**: 冰箭 #99CCFF / 火箭 #FF4D00 / 雷箭 #CCE5FF / 核心紫 #9B59B6

---

### 3.5 拖尾测试 (trail_test) ⚠️ 缺失（测试法术）

```yaml
类别: 元素流派 / 测试用
稀有度: 普通  等级: 1
描述: 用于测试TrailEffect API的调试法术
```

**图标建议**:
- 简单的紫色"T"字母或实验烧瓶
- 纯功能图标，保持简洁
- 使用元素流派紫色调

**AI 绘图提示词**:
```
pixel art icon, Minecraft style, 128x128, transparent background.
A simple magical test tube or beaker emitting purple (#9B59B6) sparkles.
Minimalist design, single color accent, clean lines.
Labeled with subtle geometric marks.
Clean game UI style, no text.
```

---

## 四、缺失效果图标设计（3个）

### 4.1 终末回响 (ender_echo_buff) ⚠️ 缺失

```yaml
效果ID: ender_echo_buff
效果类: EnderEchoBuffEffect
描述: 末影与任意元素反应给予施法者的Buff
特效: 末影粒子 + 紫色回声波纹
```

**视觉表现**:
- 中心是末影珍珠/末影人的眼睛
- 向外扩散的紫色波纹（回声效果）
- 末影粒子（类似传送时的粒子）散布四周

**AI 绘图提示词**:
```
pixel art effect icon, Minecraft style, 128x128, transparent background.
An ender pearl with a glowing purple (#9B59B6) eye at center.
Concentric ripple rings echoing outward in purple and dark purple.
Ender particles (small purple squares) scattered around the edges.
The "echo" effect shown as fading wave circles.
Clean game UI style, high contrast, no text.
```

### 4.2 避雷针 (lightning_rod_buff) ⚠️ 缺失

```yaml
效果ID: lightning_rod_buff
效果类: LightningRodBuffEffect
描述: 冰雷元素反应给予施法者的Buff，减少雷系和冰系抗性
```

**视觉表现**:
- 中心是避雷针/铁栏杆形状
- 上方有闪电劈下
- 冰晶附着在避雷针上（冰雷结合的视觉效果）

**AI 绘图提示词**:
```
pixel art effect icon, Minecraft style, 128x128, transparent background.
A metallic lightning rod in the center with ice crystals (#99CCFF) growing on its sides.
A yellow lightning bolt (#CCE5FF) striking down from above onto the rod.
Electric sparks and frost particles mixing around the impact point.
The dual ice-lightning theme represented by half blue half yellow glow.
Clean game UI style, high contrast, no text.
```

### 4.3 瘟疫 (plague_buff) ⚠️ 缺失

```yaml
效果ID: plague_buff
效果类: PlagueBuffEffect
描述: 暗毒元素反应给予目标的Debuff，降低生命值并可能转化为僵尸
```

**视觉表现**:
- 中心是病毒/骷髅头形状
- 绿色毒气向外扩散
- 暗紫色背景暗示暗毒结合

**AI 绘图提示词**:
```
pixel art effect icon, Minecraft style, 128x128, transparent background.
A skull silhouette in the center with green (#4CAF50) toxic gas swirling around it.
Dark purple (#2E003E) shadow emanating from behind the skull.
Green poison particles rising upward, suggesting pestilence and decay.
The skull has zombie-like features, hinting at transformation.
Clean game UI style, high contrast, eerie atmosphere, no text.
```

---

## 五、效果图标完整对照表

### 已有效果图标 (14/17)

| 效果ID | 文件名 | 类型 | 色彩 |
|--------|-------|------|------|
| pyro_flame | pyro_flame.png | 负面/火焰异常 | 红橙 |
| blood_mark | blood_mark.png | 标记/血系 | 深红 |
| holy_mark | holy_mark.png | 标记/神圣 | 金白 |
| eldritch_mark | eldritch_mark.png | 标记/邪术 | 紫绿 |
| poison_mark | poison_mark.png | 标记/毒素 | 绿色 |
| fire_mark | fire_mark.png | 标记/火系 | 红橙 |
| ice_mark | ice_mark.png | 标记/冰系 | 蓝色 |
| lightning_mark | lightning_mark.png | 标记/雷系 | 黄色 |
| ender_mark | ender_mark.png | 标记/末影 | 紫色 |
| chaos_buff | chaos_buff.png | Buff/混沌 | 紫绿蓝 |
| armor_reduction | armor_reduction.png | Debuff/溶甲 | 灰红 |
| darkness_buff | darkness_buff.png | Debuff/暗夜无光 | 黑紫 |
| electrocuted_buff | electrocuted_buff.png | Debuff/触电 | 黄蓝 |
| magic_shotgun_buff | magic_shotgun_buff.png | Buff/咒刃 | 紫蓝金 |

### 缺失效果图标 (3/17)

| 效果ID | 文件名 | 类型 | 需要绘制 |
|--------|-------|------|:---:|
| ender_echo_buff | ender_echo_buff.png | Buff/终末回响 | ⚠️ |
| lightning_rod_buff | lightning_rod_buff.png | Buff/避雷针 | ⚠️ |
| plague_buff | plague_buff.png | Debuff/瘟疫 | ⚠️ |

---

## 六、文件名对照（复制粘贴用）

### 缺失法术图标 → 目标路径
```
elemental_burst.png   → textures/gui/spell_icons/elemental_burst.png
elemental_prism.png   → textures/gui/spell_icons/elemental_prism.png
giant_snowball.png    → textures/gui/spell_icons/giant_snowball.png
tri_directional_arrow.png → textures/gui/spell_icons/tri_directional_arrow.png
trail_test.png        → textures/gui/spell_icons/trail_test.png
```

### 缺失效果图标 → 目标路径
```
ender_echo_buff.png   → textures/mob_effect/ender_echo_buff.png
lightning_rod_buff.png → textures/mob_effect/lightning_rod_buff.png
plague_buff.png       → textures/mob_effect/plague_buff.png
```

---

## 七、生成优先级建议

| 优先级 | 图标 | 理由 |
|:---:|------|------|
| 🔴 P0 | elemental_burst.png | 核心传奇法术，游戏内显示为紫黑缺失纹理 |
| 🔴 P0 | elemental_prism.png | 核心传奇法术，游戏内显示为紫黑缺失纹理 |
| 🔴 P0 | giant_snowball.png | 核心传奇法术，游戏内显示为紫黑缺失纹理 |
| 🟡 P1 | tri_directional_arrow.png | 史诗法术，使用频率高 |
| 🟡 P1 | ender_echo_buff.png | 战斗中频繁出现的效果 |
| 🟡 P1 | lightning_rod_buff.png | 战斗中频繁出现的效果 |
| 🟢 P2 | plague_buff.png | 条件触发效果 |
| 🟢 P2 | trail_test.png | 仅调试用 |

---

*文档基于 Legendary-Mage v1.0.6+ 源码生成。所有色彩代码、法术参数、音效信息均提取自实际 Java 源码。*