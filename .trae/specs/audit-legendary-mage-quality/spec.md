# Legendary-Mage 项目深度质量审查 Spec

## Why
对 Legendary-Mage 模组进行一次全面的日志、性能、错误处理、客户端/服务端分离与内存泄漏风险审查，输出结构化 Markdown 报告，帮助后续有针对性地优化代码质量与运行稳定性。

## What Changes
- 审查指定源码文件与包：ModLogger、trail/*、spell 管理器（Blizzard/IceSculpture/ResurrectionRune/ElementalPrism）、entity/spell/*、ModEvents、effect/*、element/*。
- 识别并记录日志滥用、无条件输出、性能瓶颈、错误处理缺陷、客户端/服务端分离问题、内存泄漏风险。
- 为每个问题标注高/中/低风险等级。
- 不修改任何源文件，仅输出审查报告。

## Impact
- Affected specs：代码质量审查、性能优化建议、稳定性改进建议。
- Affected code：本次审查覆盖的所有 Java 源文件，但不会直接修改它们。

## ADDED Requirements
### Requirement: 日志滥用识别
The system SHALL 定位所有 LOGGER.info 在每次施法、每 tick 或高频事件中的调用位置，并评估其输出频次对日志系统与服务器性能的影响。

#### Scenario: 高频 INFO 日志
- **WHEN** 施法实体创建、每 tick 更新或命中事件触发时
- **THEN** 若存在 LOGGER.info 无条件输出，则记录文件路径、行号、调用上下文与风险等级

### Requirement: 调试日志开关控制
The system SHALL 检查所有调试日志是否受 Config.GLOBAL_DEBUG_MODE 或 ModLogger 分类开关控制，并识别无条件输出的调试日志。

#### Scenario: 受控与未受控调试日志
- **WHEN** 代码中存在 LOGGER.debug / ModLogger.spell 等调试输出
- **THEN** 区分其是否经过配置开关过滤，列出未受控条目

### Requirement: 性能瓶颈识别
The system SHALL 扫描频繁对象创建、嵌套循环、每 tick AABB 实体扫描、重复数学计算、过度粒子生成等潜在性能问题。

#### Scenario: 大规模战斗场景
- **WHEN** 多个法术区域或投射物同时存在
- **THEN** 若存在 O(n²)、高频对象分配或大量粒子/实体扫描，则记录风险等级

### Requirement: 错误处理缺陷识别
The system SHALL 检查异常捕获后未记录或吞掉异常、空指针风险、资源泄漏、并发修改集合等缺陷。

#### Scenario: 异常路径与并发访问
- **WHEN** 代码中存在 try-catch、集合迭代、静态 Map 或多线程访问
- **THEN** 若存在异常被静默吞掉、NPE 风险或 ConcurrentModificationException 风险，则记录

### Requirement: 客户端/服务端分离检查
The system SHALL 检查网络逻辑是否被错误地放在客户端或纯服务端，确保粒子、音效、伤害与状态更新分属正确侧。

#### Scenario: 跨端逻辑
- **WHEN** 代码调用 level.isClientSide() 或 ServerLevel/ClientLevel 相关 API
- **THEN** 若发现逻辑放错边（如客户端造成伤害、服务端生成客户端粒子），则记录

### Requirement: 内存泄漏风险识别
The system SHALL 检查静态集合只增不减、监听器/事件未注销、未清理的投射物/效果引用等内存泄漏风险。

#### Scenario: 静态集合生命周期
- **WHEN** 类使用 static Map/Set/List 存储玩家、实体或效果状态
- **THEN** 若缺少过期清理或引用未释放，则记录风险等级

## MODIFIED Requirements
- 无。本次任务不修改现有需求，仅做审查。

## REMOVED Requirements
- 无。
