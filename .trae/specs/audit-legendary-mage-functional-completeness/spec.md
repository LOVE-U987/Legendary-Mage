# Legendary-Mage 功能完整性审查 Spec

## Why
对 Legendary-Mage 模组已注册的法术、效果、实体、物品、配置项及资源文件进行一次系统性的功能完整性审查，识别实现缺失、配置悬空、TODO/FIXME、资源缺漏与调试残留，输出结构化 Markdown 报告，为后续发布前清理与补完提供依据。

## What Changes
- 审查 `src/main/java/com/legendarymage/legendarymagemod/spell/` 下全部法术类及 `ModSpells.java` 的注册对应关系。
- 审查 `src/main/java/com/legendarymage/legendarymagemod/effect/` 下全部效果类及 `ModEffects.java` 的注册对应关系。
- 审查 `src/main/java/com/legendarymage/legendarymagemod/entity/` 下全部实体类及 `ModEntities.java` 的注册对应关系。
- 审查 `src/main/java/com/legendarymage/legendarymagemod/item/` 下全部物品类及注册对应关系。
- 审查 `Config.java` 中配置项与实际代码的引用对应关系，识别未使用配置。
- 检索项目中已声明但未实现的功能模块（尤其是“浇水”相关功能、`TrailTestSpell` 等测试/调试功能）。
- 检索 `TODO` / `FIXME` / 空方法 / 占位实现的具体文件位置。
- 检查 `src/main/resources/` 下的声音、模型、贴图、lang、数据包等资源缺失情况。
- 评估测试代码/调试代码是否应保留在正式发布中。
- 不修改任何源文件，仅输出审查报告。

## Impact
- Affected specs：功能完整性审查、发布前清理清单。
- Affected code：本次审查覆盖的所有 Java 源文件与资源文件，但不会直接修改它们。

## ADDED Requirements
### Requirement: 法术实现状态审查
The system SHALL 列出 `ModSpells.java` 中每个已注册法术，并判定其对应 Spell 类的实现状态为“完整 / 部分 / 缺失”。

#### Scenario: 已注册法术存在对应类
- **WHEN** 法术在 `ModSpells.java` 中注册
- **THEN** 审查其 Spell 类是否包含完整的施法逻辑、伤害/效果应用、等级缩放与必要的事件处理

#### Scenario: 测试/调试法术
- **WHEN** 法术名称或注释表明其为测试用途（如 `TrailTestSpell`）
- **THEN** 标注为调试残留，并建议是否在正式发布中移除

### Requirement: 效果实现状态审查
The system SHALL 列出 `ModEffects.java` 中每个已注册效果，并判定其对应 Effect 类的实现状态。

#### Scenario: 效果存在完整逻辑
- **WHEN** Effect 类实现了属性修饰符、触发逻辑或死亡事件回调
- **THEN** 标记为完整

#### Scenario: 效果为空壳或仅有占位实现
- **WHEN** Effect 类仅包含构造函数与默认空方法
- **THEN** 标记为缺失/部分实现，并记录位置

### Requirement: 实体实现状态审查
The system SHALL 列出 `ModEntities.java` 中每个已注册实体，并判定其对应 Entity 类的实现状态。

#### Scenario: 实体存在渲染器与行为逻辑
- **WHEN** Entity 类拥有完整的 tick 更新、碰撞/命中处理、客户端渲染注册
- **THEN** 标记为完整

#### Scenario: 实体缺少渲染器或行为不完整
- **WHEN** Entity 未注册 renderer 或核心逻辑为空
- **THEN** 标记为部分/缺失实现

### Requirement: 物品实现状态审查
The system SHALL 列出 `ModItems.java`（或同类注册文件）中已注册物品，并判定其对应 Item 类的实现状态。

### Requirement: 配置项与实际实现对应关系
The system SHALL 检查 `Config.java` 中每个配置项是否在代码中被实际引用，识别悬空配置。

#### Scenario: 配置项被使用
- **WHEN** 配置项在法术/效果/事件中被读取
- **THEN** 记录其用途

#### Scenario: 配置项未被使用
- **WHEN** 配置项在代码中无引用
- **THEN** 标记为悬空配置，并建议移除或补完实现

### Requirement: 已声明但未实现模块识别
The system SHALL 检索源码与资源中已命名但未实现的功能，特别是“浇水”相关功能、`TrailTestSpell` 等测试/调试功能。

### Requirement: TODO/FIXME/空方法定位
The system SHALL 检索项目中所有 `TODO`、`FIXME` 注释与空方法体，记录文件路径、行号与上下文。

### Requirement: 资源文件缺失检查
The system SHALL 检查 `src/main/resources/assets/legendarymage/` 与数据包目录下的声音、模型、贴图、lang、数据包等资源，识别缺失文件。

### Requirement: 调试代码发布评估
The system SHALL 评估测试法术、调试日志、临时渲染代码是否应保留在正式发布中，并给出处理建议。

## MODIFIED Requirements
- 无。本次任务不修改现有需求，仅做审查。

## REMOVED Requirements
- 无。
