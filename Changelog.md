# Legendary Mage Changelog

***

## v1.1.3 (2026-10-11)

### 修复: 代码审查发现的问题（吸血来源校验 + 边界防护 + 架构统一）

- **\[Critical] 吸血来源校验**: `ElementReactionEvents.onLivingDamagePost` 增加施法者阵营校验(`ElementMarkData.isExcludedFromAttack`), 仅施法者本人/召唤物/队友的伤害才吸血, 防止第三方（其他玩家/怪物/环境伤害）打烙印目标时施法者白赚回血
- **虚空寒霜零向量防护**: `handleVoidFrost` 增加 `lengthSqr() < 0.01` 检查（与邪雷挪移一致）, 位置重合时跳过击退而非速度被清零
- **区域管理器整型溢出**: `ElementZoneManager.ZoneData.nextApplyTick` 由 `int` 改为 `long`, 避免 `getGameTime` 长期运行（约 3.4 年）后溢出导致区域失效
- **区域数据清理**: `clearZones` 挂载到 `LevelEvent.Unload`, 世界卸载时清理该维度区域数据（防内存泄漏）
- **除零风险**: `onLivingHeal` 先记录原治疗量再施加衰减, 消除 Config 允许 0.0 时 debugLog 的除零
- **粒子架构统一**: 批次三的 5 个反应特效从 `ElementReactionEffects` 内联迁移到 `ElementReactionParticles` 统一层（IceBlood/VoidFrost/HolyIce/HellfireBrand/HolyDew）, 全部遵守 `budgetParticles` 预算
- **注释规范**: IceBloodDebuffEffect/HellfireBrandEffect 补齐方法级注释; 区域刷新时长提取为 `REFRESH_DURATION_TICKS` 常量并注明用途

***



### 修复: 区域云排除施法者 + 吸血反馈增强

- 新增 `element/ElementZoneManager`: 自制区域效果系统, 替代原版 `AreaEffectCloud`（反编译确认原版药水云对 owner 也生效, 无排除机制, 施法者站进自己的雾区会被中毒/减速）
- 霜毒凝雾 / 邪瘟毒云 / 圣冰结晶三个区域反应改走 `ElementZoneManager.spawnZone`, 排除规则: 施法者自身、施法者队友、召唤物、创造/旁观模式玩家
- 区域驱动: `LevelTickEvent.Post` 每 tick 递减时长, 每秒对圈内实体施加效果, 过期自动清理（防内存泄漏）; 每次施加时播一批持续雾粒子
- 反编译确认: 原版 `ServerLevel.getEntity(UUID)` 包含玩家（`ServerLevel.java:925` 原版自己就用它查玩家）, 吸血监听的施法者查找逻辑正确

***


### 新增: 元素反应批次三收尾 —— 冰封血脉 / 虚空寒霜 / 圣冰结晶 / 狱火烙印 / 圣露回源

- 元素系统扩展第三批(最后 5 种组合), 全 13 种非末影组合补全完毕! 13 种 + ENDER\_ANY = 元素两两全反应系统完整
- **冰封血脉(冰+血)**: 给与目标治疗抑制 Debuff(8 秒), 期间受到的治疗按 Config 比例减半(默认 50%), 监听 `LivingHealEvent.setAmount`
- **虚空寒霜(冰+邪术)**: 恐惧击退目标(背离施法者方向 `setDeltaMovement`), 附带短暂虚弱效果 `WEAKNESS`, 击退距离 Config 驱动
- **圣冰结晶(冰+神圣)**: 生成持续减速区域(原版药水云 + `MOVEMENT_SLOWDOWN`), 半径/持续/减速等级 Config 可调
- **狱火烙印(火+邪术)**: 给与目标烙印 Debuff(NBT 记录施法者 UUID), 期间对烙印目标的伤害按比例(默认 10%)吸血给施法者, 监听 `LivingDamageEvent.Post`
- **圣露回源(神圣+毒)**: 毒被圣光净化 → 施法者回复法术魔力(使用铁魔法 `DataAttachmentRegistry.MAGIC_DATA` + `addMana` API)
- 新增 `effect/IceBloodDebuffEffect` + `effect/HellfireBrandEffect` + ModEffects 注册
- `ReactionType` +5、分支 +5、case +5、Config +11 数值项(5 分组)、lang 条目(中英文)
- 技术踩坑: `IAttachmentHolder.getData(Supplier<AttachmentType<T>>)` 可直接传入 `DeferredHolder`, 无需 `.get()`; `LivingDamageEvent.Post` 在 1.21 监听吸血; `LivingHealEvent` 可 `setAmount(float)` 修改治疗量
- 伤害剔除: 控制类(虚空击退)对创造/旁观玩家跳过, 吸血与回蓝不区分玩家模式

***


### 新增: 元素反应批次二 —— 圣火净化 / 血雷反击 / 霜毒凝雾 / 邪瘟毒云

- 元素系统扩展第二批: 补全 4 种未定义组合, 区域/反伤/净化类机制
- **圣火净化(火+神圣)**: 清除目标身上全部增益 Buff(净化), 对亡灵/不死生物额外重击(×2 特攻), 伤害 = 基础 × (火强+神圣强) × 2.5
- **血雷反击(雷+血)**: 给施法者挂"血雷"增益 Buff(8 秒, 类似原版荆棘), 近战攻击携带者的敌人自身受雷击反伤(近战判定 directEntity == 攻击者, 投射物不触发), 反伤 = 基础 × (携带者雷强+血强) × **血雷等级**, **等级可叠加**(每次反应触发等级+1, 无限叠加, 时长刷新), 带 1 秒冷却防止互持血雷无限递归
- **霜毒凝雾(冰+毒)**: 目标位置生成霜毒雾区(原版药水云, 青绿雾, 半径 3 格, 5 秒), 圈内实体每秒叠加原版中毒
- **邪瘟毒云(邪术+毒)**: 目标位置生成邪瘟毒云(原版药水云, 深绿雾, 半径 3 格, 5 秒), 圈内实体持续被施加邪术标记(制造连锁反应机会)
- 新增 `effect/BloodThornsEffect` 血雷 Buff + 图标 `blood_thorns.png`(32×32 RGBA 透明)
- `ReactionType` 枚举 +4(HOLY\_FIRE / BLOOD\_THORNS / FROST\_FOG / PLAGUE\_CLOUD)与 `getReactionType` 分支 +4
- `ElementReactionEffects` 新增 4 个 handle 方法 + `removeBeneficialEffects` 辅助方法 + 4 个粒子调用方法; `ElementReactionParticles` 新增 4 个反应特效(金橙净化爆发/暗红电光/青绿霜雾/深绿毒云)
- `ElementReactionEvents.onLivingDamage` 新增血雷反伤处理(递归防护 + 创造/旁观玩家剔除反伤)
- `Config` 新增 9 个数值项(holyFire / bloodThorns / frostFog / plagueCloud 分组)
- lang 新增血雷名称与描述(中英文)
- 兼容性说明: `AreaEffectCloud` 的 `reapplicationDelay` 原版默认即 20(每秒重复施加), 1.21.1 无 setter 直接用默认值
- 伤害剔除一致性: 反伤/反应伤害对创造/旁观玩家剔除(`ElementDamageGuard`), 挂载与清除效果不区分玩家模式(MOLU 确认)

***


### 新增: 元素反应批次一 —— 沸血引爆 / 光暗湮灭 / 邪雷挪移 / 天罚充能

- 元素系统扩展: 补全 13 种未定义非末影元素组合中的第一批 4 个, 机制跳出"伤害/加成"窠臼(设计文档: `docs/superpowers/specs/2026-10-02-element-reactions-batch1-design.md`)
- **沸血引爆(火+血)**: 按目标已损失生命比例造成斩杀爆发伤害, 公式 = \[基础 + 已损失生命 × 转化比] × (火强+血强) × 2.5, 残血收割者
- **光暗湮灭(神圣+邪术)**: 清除目标身上全部效果(含元素标记与增益), 按移除数量造成湮灭爆发伤害, 矛盾元素顶点
- **邪雷挪移(雷+邪术)**: 目标被电磁引力拉向施法者(速度向量位移, 非末影人式随机传送, 不打乱战斗节奏), BOSS(血量阈值判定)免疫拉拽, 无伤害纯位移控制
- **天罚充能(雷+神圣)**: 施法者获得"充能"Buff(15 秒), 下一次法术伤害 ×2 触发后消耗, 一次性质变
- 新增 `effect/ChargedBuffEffect` 充能 Buff + 图标 `charged_buff.png`(32×32 RGBA 透明)
- `ReactionType` 枚举 +4(BOILING\_BLOOD / ANNIHILATION / WARP / SMITE\_CHARGE)与 `getReactionType` 分支 +4
- `ElementReactionEffects` 新增 4 个 handle 方法与 4 个粒子调用方法; `ElementReactionParticles` 新增 4 个反应特效(红橙爆发/黑白湮灭冲击波/紫色引力波/金紫充能光环)
- `ElementReactionEvents.onSpellDamage` 新增充能消耗: 施法者的下一次铁魔法法术伤害 ×2 后移除充能
- `Config` 新增 8 个数值项(boilingBlood / annihilation / enderWarp / chargedBuff 分组)
- 全部伤害走 `ElementDamageGuard` 剔除创造/旁观玩家; 反应后移除参与反应的两个元素标记

***

<br />


- 新增 `effect/ElementDamageGuard`: 元素伤害剔除守卫工具类
  - `shouldSkipDamage(LivingEntity)`: 创造模式与旁观模式玩家返回 true
  - `addEffectSafe(LivingEntity, MobEffectInstance)`: 挂载效果并对创造/旁观玩家剔除 MAX\_HEALTH 修饰符 clamp 造成的扣血
- 根因: 原版 `Player.hurt` 对创造玩家有 `abilities.invulnerable` 免疫, 但 `LivingEntity.onAttributeUpdated` 在 `MAX_HEALTH` 修饰符挂上时会把当前血量直接 clamp 到新上限 —— 该路径绕过 hurt 免疫, 创造玩家挂上火系标记(每级 -2% 最大生命值)时被直接扣血
- 持续伤害路径剔除: `FireMarkEffect` / `PoisonMarkEffect` / `PyroFlameEffect` 的 `applyEffectTick` 对创造/旁观玩家直接跳过(不掉血、不记冷却、不播粒子)
- 攻击触发类伤害剔除: `HolyMarkEffect` 神圣打击、`EnderMarkEffect` 回响打击、`LightningMarkEffect` 闪电链与传导(2 处候选筛选)对创造/旁观玩家目标跳过
- 元素反应伤害剔除: `ElementReactionEffects` 冰火/雷火/神圣-猩红反应对创造/旁观玩家目标跳过伤害(特效保留), 雷毒反应范围 AOE 循环排除创造/旁观玩家
- 毒爆伤害剔除: `PlagueZombie` 毒爆范围伤害循环排除创造/旁观玩家(同时不再向其传播瘟疫)
- 冰冻剔除: `IceMarkEffect.tryTriggerFreeze` 对创造/旁观玩家直接跳过(不施加 CHILLED、不抬升冰冻值)
- 挂载点 clamp 补偿: `ElementReactionManager.processElementHit`(2 处)/`applyMark` 与 `ElementReactionEffects.handlePoisonFire`/`handleBloodPoison` 的标记挂载改用 `addEffectSafe`, 创造/旁观玩家挂载后血量补回挂前值
- 数值不变: 全部伤害公式、冷却、概率、反应倍率均未改动, 生存模式玩家行为 100% 不变
- 未改动: `ElectrocutedBuffEffect`(已排除所有玩家)、死亡爆炸(`serverLevel.explode` 走原版爆炸免伤)

***

## v1.1.2 (2026-08-08)

### 修复: 移除 GeckoLib 强制前置, 仅保留铁魔法作为唯一 Mod 前置

- `META-INF/neoforge.mods.toml`(模板): 删除 `geckolib`(required, `[4.8.3,4.9)`)、`curios`(required)、`playeranimator`(required) 依赖块, 前置仅保留 `neoforge` / `minecraft` / `irons_spellbooks`(required) 与 `patchouli`(optional)
- `META-INF/neoforge.mods.toml`(模板): 更新 description 中 Requires 列表, 仅声明 Iron's Spells 'n Spellbooks
- `build.gradle`: GeckoLib 依赖由 `implementation` 降级为 `compileOnly` + `localRuntime`(编译期兼容 + 本地 dev 运行, 不发布为依赖); Curios、PlayerAnimator 降级为 `localRuntime`(铁魔法传递依赖, 仅本地运行需要)
- `gradle.properties`: 清理无引用死变量 `playeranimator_version` / `irons_spells_version` / `irons_lib_version`, 保留仍被引用的 `geckolib_version` / `curios_version`
- 注意: GeckoLib / Curios / Player Animator 为 Iron's Spells 的传递依赖, 由铁魔法 jar 内 `neoforge.mods.toml` 的 required 声明自动带出, 玩家无需单独安装

