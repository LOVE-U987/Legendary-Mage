# Tasks

- [x] Task 1: 依赖与兼容性治理
  - [x] SubTask 1.1: 在 `build.gradle` 中将 Iron's Spells、GeckoLib、Curios、Player Animator、Patchouli 改为 Maven/CurseMaven 坐标，并声明必要传递依赖
  - [x] SubTask 1.2: 收紧 `neoforge.mods.toml` 中 `irons_spellbooks`、`geckolib`、`curios`、`playeranimator` 的版本范围到实际验证区间
  - [x] SubTask 1.3: 处理无法迁移到 Maven 的库，改用 `localRuntime` 而非 `implementation files()`
  - [x] SubTask 1.4: 运行 `./gradlew clean compileJava` 验证依赖可解析且无新增编译错误

- [x] Task 2: 仓库污染源清理
  - [x] SubTask 2.1: 删除根目录 `io/` 下的反编译 `.class` 文件
  - [x] SubTask 2.2: 删除 `libs/源代码/` 中的完整模组源码（保留必要的本地 jar 或迁移后移除）
  - [x] SubTask 2.3: 清理 `model/` 中的临时 Blockbench 工作文件，仅保留真正需要的资源
  - [x] SubTask 2.4: 更新 `.gitignore`，防止二进制源码、反编译文件、临时模型再次进入版本控制

- [x] Task 3: 包结构与 API 分离
  - [x] SubTask 3.1: 将 `spell/LivingIceSculptureEntity.java` 移动到 `entity/` 包并修正引用
  - [x] SubTask 3.2: 将 `client/renderer/IceSculptureModel.java` 移动到 `client/model/` 并修正引用
  - [x] SubTask 3.3: 评估并合并/删除重复拖尾系统（`TrailEffect`、`SimpleTrailEffect`、`BezierTrailEffect`），保留一套可扩展实现
  - [x] SubTask 3.4: 新建 `api/` 包，将 `CustomSchoolRegistry`、`ElementReactionManager` 等扩展点提取为稳定接口

- [ ] Task 4: 功能完整性保障
  - [ ] SubTask 4.1: 核对 `ModSpells.java` 中每个法术与对应 Spell 类的实现完整性，标记缺失项
  - [ ] SubTask 4.2: 核对 `Config.java` 中每个配置项的源码引用，移除悬空配置或补完实现
  - [ ] SubTask 4.3: 搜索并确认"浇水"相关功能状态：若存在声明则补全实现，否则从所有资源/文档中移除
  - [ ] SubTask 4.4: 将 `TrailTestSpell`、`TrailTestProjectile` 等调试代码通过配置开关禁用，或在发布版本中移除
  - [ ] SubTask 4.5: 检查 `src/main/resources/` 中 lang、声音、模型、贴图、patchouli 数据与代码注册项一一对应

- [ ] Task 5: 日志输出治理
  - [ ] SubTask 5.1: 将 `IceSculptureManager`、`HolyMarkEffect`、`EnderMarkEffect` 等高频 `LOGGER.info` 调用改为 `ModLogger.spellDebug`
  - [ ] SubTask 5.2: 将代码中直接调用 `LegendaryMage.LOGGER.*` 的位置统一改为 `ModLogger` 分类方法
  - [ ] SubTask 5.3: 删除或屏蔽测试代码中的日志轰炸（如 `TrailTestProjectile`、`TrailTestSpell`）
  - [ ] SubTask 5.4: 验证非错误日志在默认配置下不再刷屏

- [ ] Task 6: 性能与资源优化
  - [ ] SubTask 6.1: 修复 `TrailPointPool` 对象池失效问题（字段改为非 final 或移除对象池）
  - [ ] SubTask 6.2: 优化 `BlizzardManager`、`ElementalPrismManager` 的实体扫描：缓存目标、使用平方距离、减少重复分配
  - [ ] SubTask 6.3: 优化 `ResurrectionRuneManager.getRuneForEntity()` 的距离计算，移除不必要的 `Math.sqrt`
  - [ ] SubTask 6.4: 为粒子生成添加密度配置系数，降低默认密度
  - [ ] SubTask 6.5: 审查所有 static 集合的清理机制，修复 `IceSculptureManager.clearFields()` 反向清理逻辑

- [ ] Task 7: 错误处理与稳定性
  - [ ] SubTask 7.1: 将 `ResurrectionRuneManager.convertToUndead()`、`client/TrailClientHandler.java`、`entity/PlagueZombie.java` 中的 `e.printStackTrace()` 改为 `ModLogger.error(..., e)`
  - [ ] SubTask 7.2: 将 `BlizzardManager.dealDamage()` 等宽泛 catch 块改为记录完整异常堆栈
  - [ ] SubTask 7.3: 修复 `ElementReactionManager.getReactableMarks()` 返回共享可变列表的问题，返回不可变副本或新列表
  - [ ] SubTask 7.4: 修复 `MagicShotgunSpell.getSchoolType()` 中过度宽泛的异常捕获，区分预期回退与意外异常

- [ ] Task 8: 代码风格与可维护性
  - [ ] SubTask 8.1: 使用 IDE/工具清理约 80 处未使用导入
  - [ ] SubTask 8.2: 将 17 处通配符导入展开为显式导入
  - [ ] SubTask 8.3: 统一服务端判断风格为 `level instanceof ServerLevel`
  - [ ] SubTask 8.4: 为新增/移动的类、方法、参数补充中文注释
  - [ ] SubTask 8.5: 调整 `LegendaryMage.java` 注册顺序为 `NewRegistry → Attributes → Sounds → Items → Entities → Effects → Schools → Spells → Config → ReloadListener`

- [ ] Task 9: 弃用 API 与运行时兼容性
  - [ ] SubTask 9.1: 迁移或封装 `AbstractSpell.getMinRarity()`、`SummonedZombie` 构造等 Iron's Spells 弃用 API
  - [ ] SubTask 9.2: 验证 `./gradlew clean compileJava` 无新增 deprecation 警告或已记录可接受的兼容注释
  - [ ] SubTask 9.3: 在目标集成环境中运行客户端/服务端，确认无类找不到、版本不兼容崩溃

- [ ] Task 10: 最终验证与回归测试
  - [ ] SubTask 10.1: 运行完整 Gradle 构建 `./gradlew build`，确认无编译错误
  - [ ] SubTask 10.2: 运行 `./gradlew runClient` 至少 5 分钟，检查日志无异常刷屏
  - [ ] SubTask 10.3: 对每个已注册法术进行游戏内施放测试，确认功能正常
  - [ ] SubTask 10.4: 使用检查清单逐项确认所有重构点已完成

# Task Dependencies
- Task 2 依赖 Task 1（依赖迁移后才知道哪些本地源码可删）
- Task 3 依赖 Task 2（清理后再移动类，避免冲突）
- Task 4 依赖 Task 3（包结构调整后再核对功能完整性）
- Task 5 依赖 Task 4（先确定哪些调试代码保留/移除）
- Task 6 依赖 Task 3（拖尾系统合并后再优化对象池）
- Task 7 依赖 Task 4（功能稳定后再修复错误处理）
- Task 8 依赖 Task 3、Task 5、Task 6、Task 7（最后统一风格）
- Task 9 依赖 Task 1、Task 8
- Task 10 依赖 Task 1-9
