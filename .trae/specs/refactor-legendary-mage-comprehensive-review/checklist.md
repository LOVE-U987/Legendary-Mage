# Checklist

## 依赖与兼容性
- [x] `build.gradle` 中 Iron's Spells、GeckoLib、Curios、Player Animator、Patchouli 已改为 Maven/CurseMaven 坐标
- [x] 必要传递依赖（如 Caelus、Apothic Attributes 等）已声明
- [x] `neoforge.mods.toml` 中各依赖版本范围已收紧到实际验证区间
- [x] `./gradlew clean compileJava` 成功，无新增编译错误

## 仓库清理
- [x] 根目录 `io/` 下反编译 `.class` 文件已删除
- [x] `libs/源代码/` 中完整模组源码已删除（或仅保留 README 说明）
- [x] `model/` 中临时 Blockbench 工作文件已清理
- [x] `.gitignore` 已更新，防止二进制源码、反编译文件、临时模型再次提交

## 包结构与 API
- [x] `LivingIceSculptureEntity` 已移动到 `entity/` 包，所有引用已修正
- [x] `IceSculptureModel` 已移动到 `client/model/` 包，所有引用已修正
- [x] 重复拖尾系统已评估并合并/删除，仅剩一套可扩展实现
- [x] 新建 `api/` 包并提取稳定的扩展点接口（自定义流派、元素反应等）

## 功能完整性
- [ ] `ModSpells.java` 中每个法术均对应完整实现的 Spell 类
- [ ] `Config.java` 中每个配置项在代码中至少有一处引用，悬空配置已移除或补完
- [ ] "浇水"相关功能状态已确认：要么完整实现，要么从注册/配置/lang/手册中彻底移除
- [ ] `TrailTestSpell`、`TrailTestProjectile` 等调试代码已通过配置开关禁用或移除
- [ ] `src/main/resources/` 中 lang、声音、模型、贴图、patchouli 与代码注册项一一对应

## 日志治理
- [ ] `IceSculptureManager`、`HolyMarkEffect`、`EnderMarkEffect` 等高频 INFO 已改为 DEBUG 并受开关控制
- [ ] 代码中直接调用 `LegendaryMage.LOGGER.*` 的位置已统一改为 `ModLogger`
- [ ] 测试代码中的日志轰炸已删除或屏蔽
- [ ] 默认配置下启动/施法/战斗日志无刷屏

## 性能优化
- [ ] `TrailPointPool` 对象池真正复用对象或已被移除
- [ ] `BlizzardManager`、`ElementalPrismManager` 等扫描逻辑已缓存目标并使用平方距离
- [ ] `ResurrectionRuneManager.getRuneForEntity()` 已移除不必要的 `Math.sqrt`
- [ ] 粒子生成已提供密度配置，默认密度不过高
- [ ] 所有 static 集合均有清理机制，`IceSculptureManager.clearFields()` 逻辑已修复为"实体不存在时移除"

## 错误处理与稳定性
- [ ] 所有 `e.printStackTrace()` 已替换为 `ModLogger.error(..., e)`
- [ ] `BlizzardManager.dealDamage()` 等 catch 块记录完整异常堆栈
- [ ] `ElementReactionManager.getReactableMarks()` 返回不可变副本或新列表
- [ ] `MagicShotgunSpell.getSchoolType()` 异常捕获范围合理，意外异常不再被静默吞掉

## 代码风格
- [ ] 未使用导入已清理
- [ ] 通配符导入已展开为显式导入
- [ ] 服务端判断统一使用 `level instanceof ServerLevel`
- [ ] 新增/移动的类、方法、参数已补充中文注释
- [ ] `LegendaryMage.java` 注册顺序已调整为 `NewRegistry → Attributes → Sounds → Items → Entities → Effects → Schools → Spells → Config → ReloadListener`

## 弃用 API 与运行时兼容
- [ ] `AbstractSpell.getMinRarity()`、`SummonedZombie` 构造等弃用 API 已迁移或封装
- [ ] 编译无新增 deprecation 警告，或已有明确兼容注释
- [ ] 目标集成环境客户端/服务端启动无类找不到、版本不兼容崩溃

## 最终验证
- [ ] `./gradlew build` 成功
- [ ] `./gradlew runClient` 运行 5 分钟以上无异常刷屏
- [ ] 每个已注册法术均完成游戏内施放测试
- [ ] 本 checklist 所有条目均已勾选
