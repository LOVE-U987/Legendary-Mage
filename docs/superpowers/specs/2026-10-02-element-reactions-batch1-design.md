# 元素反应批次一设计定稿

> 日期:2026-10-02
> 项目:Legendary-Mage(NeoForge 1.21.1)
> 状态:已获 MOLU 批准,进入实现

## 概述

Legendary-Mage 元素系统扩展:补全 13 种未定义非末影元素组合中的第一批 4 个,机制跳出"伤害/加成"窠臼,覆盖**斩杀、净化爆发、位移控制、一次性质变**四大新机制类型。

## 批次划分(13 种组合分 3 批)

| 批次 | 反应(元素组合) |
|---|---|
| 第一批(本次实现) | 沸血引爆(火+血)、光暗湮灭(神圣+邪术)、邪雷挪移(雷+邪术)、天罚充能(雷+神圣) |
| 第二批(区域/反伤/吸血) | 圣火净化(火+神圣)、血雷反击(雷+血)、霜毒凝雾(冰+毒)、邪瘟毒云(邪术+毒) |
| 第三批(收尾) | 冰封血脉(冰+血)、虚空寒霜(冰+邪术)、圣冰结晶(冰+神圣)、狱火烙印(火+邪术)、圣露回源(神圣+毒) |

## 第一批 4 个反应设计

### 1. 沸血引爆(火+血)—— 斩杀
- **触发**:目标同时携带火标记 + 血标记(走现有 `processElementHit` 反应检测,1 级即可)
- **机制**:斩杀伤害 = [基础伤害 + 已损失生命 × 转化比] × (火强 + 血强) × 2.5
- **Config**:`BOILING_BLOOD_BASE_DAMAGE`(默认 1.0)、`BOILING_BLOOD_LOST_HEALTH_RATIO`(默认 0.3)
- **特效**:红橙爆发(铁魔法 `FIRE_PARTICLE` + 原版 `EXPLOSION`/`DUST` 红色)
- **无新 MobEffect**(纯一次性);反应后移除火 + 血标记

### 2. 光暗湮灭(神圣+邪术)—— 净化 + 爆发
- **触发**:目标同时携带神圣 + 邪术标记
- **机制**:清除目标身上**全部效果**(含元素标记与增益),湮灭伤害 = [基础 + 移除数量 × 单个伤害] × (神圣强 + 邪术强) × 2.5
- **Config**:`ANNIHILATION_BASE_DAMAGE`(默认 2.0)、`ANNIHILATION_PER_EFFECT_DAMAGE`(默认 1.5)
- **特效**:黑白双色冲击波(`Shockwave`/`Blastwave` 参数化颜色 Vector3f)
- **无新 MobEffect**

### 3. 邪雷挪移(雷+邪术)—— 位移控制(电磁引力拉拽)
- **触发**:目标同时携带雷 + 邪术标记
- **机制**:目标被电磁引力**拉向施法者**(固定距离),速度向量位移(`setDeltaMovement` + `hurtMarked` 同步),**非末影人式随机传送**(随机传送打乱战斗节奏,MOLU 确认废弃)
- **拉拽距离**:`WARP_PULL_DISTANCE`(默认 5 格)
- **BOSS 保护**:血量阈值判定(`maxHealth >= WARP_BOSS_HEALTH_THRESHOLD`,默认 100)的 BOSS/大型敌人**免疫拉拽**
- **无施法者时**:跳过拉拽,标记照常消耗(与现有反应模式一致)
- **无伤害**,纯位移;特效:紫色引力波 + `Zap` 轨迹(目标 → 施法者方向)
- **无新 MobEffect**
- **创造/旁观玩家**:跳过拉拽(控制类效果,与冰冻剔除一致)

### 4. 天罚充能(雷+神圣)—— 施法者一次性质变
- **触发**:目标同时携带雷 + 神圣标记,**受益者是施法者**
- **机制**:施法者获得"充能"Buff(15 秒),期间**下一次法术伤害 ×2**,触发后消耗
- **实现**:新 MobEffect `ChargedBuffEffect` + 伤害监听(`SpellDamageEvent` 优先改伤害,不可用则 `LivingDamageEvent.Pre` 兜底;伤害源必须是充能者本人的法术)
- **Config**:`CHARGED_DAMAGE_MULTIPLIER`(默认 2.0)、`CHARGED_DURATION_SECONDS`(默认 15)
- **特效**:金紫充能环绕(`END_ROD` + `DUST` 金紫 + `Zap`)
- **新 MobEffect**:`ChargedBuffEffect`(**图标 `charged_buff.png` 已就绪**:32×32 RGBA 透明,与现有惯例一致)
- 充能为增益,对创造玩家无副作用,不加守卫

## 公共改动

- `ReactionType` 枚举 +4:`BOILING_BLOOD`、`ANNIHILATION`、`WARP`、`SMITE_CHARGE`
- `getReactionType` +4 个分支
- `ElementReactionEffects` +4 个 handle 方法 + 4 个粒子方法
- 新类 `ChargedBuffEffect` + `ModEffects` 注册
- Config 新增数值项(7 个)
- **全部伤害走 `ElementDamageGuard.shouldSkipDamage` 剔除**(上一轮修复的守卫)
- 反应后移除参与反应的两个元素标记(系统一致性)
- changelog 同步 + 知识库沉淀
