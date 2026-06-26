# Legendary-Mage 全面重构审查 Spec

## Why
Legendary-Mage 模组在长期迭代中积累了依赖管理混乱、日志滥用、重复实现、内存泄漏隐患与功能声明和实现不一致等问题。为确保其作为高质量 addon 被其他系统稳定集成，需要对代码库进行一次系统性重构审查，明确重写范围、实施步骤与质量验证标准。

## What Changes
- 重构依赖管理策略：将核心第三方依赖从本地 jar 迁移至 Maven/CurseMaven 坐标，收紧 `mods.toml` 版本范围。
- 清理仓库污染源：移除根目录反编译 `.class` 文件、`libs/源代码/` 完整模组源码、`model/` 临时工作文件。
- 整理包结构与类职责：将实体类移入 `entity/`、模型类移入 `client/model/`，合并/删除重复拖尾系统。
- 补全或移除声明功能：移除/隐藏 `TrailTestSpell` 等调试代码，明确"浇水"等功能是否存在缺失。
- 修复错误处理与稳定性缺陷：替换 `e.printStackTrace()`、修复 `IceSculptureManager.clearFields()` 反向清理逻辑、消除共享可变缓存。
- 优化性能：修复 `TrailPointPool` 对象池失效、减少每 tick AABB 扫描、降低粒子密度、统一使用平方距离比较。
- 优化日志系统：将高频 INFO 日志降级为 DEBUG 并统一受 `ModLogger` 开关控制，删除测试代码中的日志轰炸。
- 统一代码风格：清理未使用导入、减少通配符导入、补充类/方法/参数注释、统一服务端判断风格。

## Impact
- Affected specs: `audit-legendary-mage-functional-completeness`、`audit-legendary-mage-quality`（作为本次重构的子项参考）。
- Affected code:
  - `build.gradle`、`gradle.properties`、`settings.gradle`
  - `src/main/templates/META-INF/neoforge.mods.toml`
  - 根目录 `io/`、`model/`、`libs/源代码/`
  - `src/main/java/com/legendarymage/legendarymagemod/` 下 `spell/`、`effect/`、`entity/`、`trail/`、`client/`、`command/`、`element/`、`data/`、`school/` 等包
  - `src/main/resources/` 下声音、lang、patchouli、模型/贴图资源

## ADDED Requirements

### Requirement: 依赖与兼容性治理
The system SHALL 提供清晰、可维护且兼容的第三方依赖管理策略，确保作为 addon 被其他项目依赖时不会引入传递依赖黑洞。

#### Scenario: 核心依赖迁移
- **WHEN** 构建脚本声明 Iron's Spells、GeckoLib、Curios、Player Animator、Patchouli 等依赖
- **THEN** 优先使用 Maven/CurseMaven 坐标，仅对 truly 缺失的库使用 `localRuntime`/`files()`，并声明必要的传递依赖

#### Scenario: 版本范围收紧
- **WHEN** `neoforge.mods.toml` 声明依赖版本范围
- **THEN** 范围应收紧到实际验证过的版本区间（如 Iron's Spells `[1.21.1-3.15.0,1.21.1-4.0.0)`），避免未来大版本运行时类缺失

#### Scenario: 弃用 API 迁移
- **WHEN** 代码使用 Iron's Spells 已弃用 API（`AbstractSpell.getMinRarity()`、`SummonedZombie` 构造等）
- **THEN** 应迁移到当前推荐 API，或添加明确的版本兼容注释与回退逻辑

### Requirement: 仓库与结构清理
The system SHALL 移除不应进入版本控制的文件，并建立清晰的包边界与职责分离。

#### Scenario: 污染源移除
- **WHEN** 仓库中存在反编译 `.class`、`libs/源代码/` 完整模组源码、`model/` 临时文件
- **THEN** 应将其从 Git 历史中移除（必要时使用 BFG/Git filter-repo），并更新 `.gitignore`

#### Scenario: 类位置归位
- **WHEN** 实体类位于 `spell/`、模型类位于 `renderer/` 等职责错位的包
- **THEN** 应移动到正确包，并通过 NeoForge 注册表绑定保持注册路径不变

#### Scenario: API / impl 分离
- **WHEN** 模组需要向其他 addon 暴露扩展点（自定义流派、元素反应、拖尾）
- **THEN** 应提供稳定的 `api/` 包，内部实现放在 `impl/` 或现有包中并标记 `@Internal`

### Requirement: 功能完整性保障
The system SHALL 确保已声明功能均有对应实现，未实现功能不得作为正式功能注册或暴露。

#### Scenario: 法术实现核对
- **WHEN** `ModSpells.java` 注册了一个法术
- **THEN** 其 Spell 类应包含完整的 `onCast` / 伤害计算 / 等级缩放 / 粒子音效 / 配置引用，否则标记为缺失并补全或移除注册

#### Scenario: 配置项引用核对
- **WHEN** `Config.java` 声明了一个配置项
- **THEN** 代码中应存在至少一处引用；悬空配置应移除或补完对应功能

#### Scenario: 未实现模块处理
- **WHEN** 源码或资源中存在"浇水"、占位法术、测试法术等已命名但未实现功能
- **THEN** 应明确补全实现，或从注册/配置/lang/手册中彻底移除，避免误导用户

#### Scenario: 调试代码隔离
- **WHEN** 代码中存在 `TrailTestSpell`、`TrailTestProjectile` 等仅用于开发调试的功能
- **THEN** 应通过配置开关完全禁用，或在发布版本中移除

### Requirement: 日志输出治理
The system SHALL 大幅减少不必要的日志输出，并将调试日志统一纳入配置开关控制。

#### Scenario: 高频日志降级
- **WHEN** 每次施法、每 tick、每次受伤/命中事件触发日志输出
- **THEN** 非错误信息应使用 `ModLogger.debug*` 并受 `GLOBAL_DEBUG_MODE` 或独立分类开关控制，避免 INFO 刷屏

#### Scenario: 统一日志入口
- **WHEN** 代码中直接调用 `LegendaryMage.LOGGER.info/debug/warn/error`
- **THEN** 应统一改为 `ModLogger` 分类方法，便于后续集中管理和关闭调试输出

### Requirement: 性能与资源优化
The system SHALL 消除已识别的性能瓶颈、对象浪费与内存泄漏隐患。

#### Scenario: 对象池修复
- **WHEN** `TrailPointPool` 用于回收拖尾点对象
- **THEN** 应真正复用对象（字段非 final 或移除对象池），避免每次 `acquire()` 仍创建新对象

#### Scenario: 扫描与计算优化
- **WHEN** `BlizzardManager`、`ElementalPrismManager` 等周期性扫描范围内实体
- **THEN** 应缓存目标列表、使用平方距离比较、减少重复 `new AABB`/`new ArrayList` 分配

#### Scenario: 粒子密度控制
- **WHEN** 法术命中/触发时生成粒子效果
- **THEN** 应提供配置密度系数，避免在多人战斗中发送过多网络包导致客户端卡顿

#### Scenario: 静态集合生命周期
- **WHEN** 使用 static Map/Set/List 存储玩家、实体或效果状态
- **THEN** 应存在基于实体死亡、维度卸载、超时的清理机制，避免只增不减

### Requirement: 错误处理与稳定性
The system SHALL 正确处理异常、避免静默吞错，并消除并发与空指针隐患。

#### Scenario: 异常完整记录
- **WHEN** 代码捕获异常（尤其是 `ResurrectionRuneManager.convertToUndead`、`BlizzardManager.dealDamage`）
- **THEN** 应使用 `ModLogger.error(..., throwable)` 记录完整堆栈，禁止仅 `e.printStackTrace()` 或只记录 `e.getMessage()`

#### Scenario: 共享可变缓存安全
- **WHEN** `ElementReactionManager` 返回静态共享的 `REACTABLE_MARKS_CACHE`
- **THEN** 应返回不可变副本或新列表，避免调用方修改导致全局状态异常

#### Scenario: 清理逻辑正确性
- **WHEN** `IceSculptureManager.clearFields()` 清理已失效的冰雕记录
- **THEN** 条件应为"实体不存在"时移除，而非"实体仍存在"时移除

### Requirement: 代码风格与可维护性
The system SHALL 统一代码风格，提升可读性与长期维护性。

#### Scenario: 导入清理
- **WHEN** 源文件中存在未使用导入或通配符导入
- **THEN** 应移除未使用导入，将通配符导入展开为显式导入

#### Scenario: 注释规范
- **WHEN** 新增或修改类、方法、参数时
- **THEN** 应补充类级、方法级、参数级中文注释，说明职责与约束

#### Scenario: 服务端判断统一
- **WHEN** 代码需要区分服务端与客户端逻辑
- **THEN** 统一使用 `level instanceof ServerLevel` 进行判断，保持风格一致

## MODIFIED Requirements
### Requirement: 初始化注册顺序
**原要求**: `LegendaryMage.java` 按 `Sounds → Items → Attributes → Schools → Spells → Effects → Entities` 顺序注册。
**修改后**: 注册顺序应调整为 `NewRegistry → Attributes → Sounds → Items → Entities → Effects → Schools → Spells → Config → ReloadListener`，避免 Spell 构造时访问未注册的 Effect/Entity。

## REMOVED Requirements
### Requirement: 本地 jar + 完整源码依赖
**Reason**: 本地 jar 丢失传递依赖、源码版本混乱、仓库体积膨胀，且阻止其他项目通过 Maven 安全依赖本模组。
**Migration**: 将依赖迁移到 Maven/CurseMaven 坐标；仅对未发布到 Maven 的库保留 `localRuntime`。
