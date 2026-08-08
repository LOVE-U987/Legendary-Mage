# Legendary Mage Changelog

---

## v1.1.2 (2026-08-08)

### 修复: 移除 GeckoLib 强制前置, 仅保留铁魔法作为唯一 Mod 前置

* `META-INF/neoforge.mods.toml`(模板): 删除 `geckolib`(required, `[4.8.3,4.9)`)、`curios`(required)、`playeranimator`(required) 依赖块, 前置仅保留 `neoforge` / `minecraft` / `irons_spellbooks`(required) 与 `patchouli`(optional)
* `META-INF/neoforge.mods.toml`(模板): 更新 description 中 Requires 列表, 仅声明 Iron's Spells 'n Spellbooks
* `build.gradle`: GeckoLib 依赖由 `implementation` 降级为 `compileOnly` + `localRuntime`(编译期兼容 + 本地 dev 运行, 不发布为依赖); Curios、PlayerAnimator 降级为 `localRuntime`(铁魔法传递依赖, 仅本地运行需要)
* `gradle.properties`: 清理无引用死变量 `playeranimator_version` / `irons_spells_version` / `irons_lib_version`, 保留仍被引用的 `geckolib_version` / `curios_version`
* 注意: GeckoLib / Curios / Player Animator 为 Iron's Spells 的传递依赖, 由铁魔法 jar 内 `neoforge.mods.toml` 的 required 声明自动带出, 玩家无需单独安装

