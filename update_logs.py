#!/usr/bin/env python3
"""
批量更新日志调用脚本
将 LegendaryMage.LOGGER 替换为 ModLogger

使用方法:
    python update_logs.py

作者: Love_U
版本: 1.0.0
"""

import os
import re
from pathlib import Path

# 项目根目录
PROJECT_ROOT = Path("c:/Users/97128/Documents/GitHub/Legendary-Mage")

# Java源文件目录
SOURCE_DIR = PROJECT_ROOT / "src/main/java/com/legendarymage/legendarymagemod"

# 需要处理的文件列表（相对路径）
FILES_TO_PROCESS = [
    # 法术相关
    "spell/GiantSnowballSpell.java",
    "spell/ElementalPrismSpell.java",
    "spell/TrailTestSpell.java",
    "spell/ElementalBurstSpell.java",
    "spell/MagicShotgunSpell.java",
    
    # 实体相关
    "entity/spell/GiantSnowballEntity.java",
    "entity/spell/TrailTestProjectile.java",
    
    # 拖尾系统
    "trail/TrailManager.java",
    "trail/TrailPresets.java",
    "trail/BezierTrailManager.java",
    "trail/BezierTrailRenderer.java",
    "trail/GeometryTrailRenderer.java",
    
    # 事件
    "event/ModEvents.java",
    
    # 客户端
    "client/SimpleTrailClientHandler.java",
    "client/BezierTrailClientHandler.java",
    "client/TrailClientHandler.java",
    
    # 元素反应
    "element/ElementReactionEffects.java",
    
    # 客户端主类
    "LegendaryMageClient.java",
    
    # 效果
    "effect/EnderMarkEffect.java",
    "effect/HolyMarkEffect.java",
    "effect/EffectRemovalHandler.java",
    
    # 物品
    "item/ModItems.java",
    
    # 客户端UI
    "client/SchoolElementMarkTooltipHandler.java",
    
    # 数据
    "data/CustomSchoolRegistry.java",
    "data/SchoolElementMappingRegistry.java",
    "data/CustomSchoolDataLoader.java",
    "data/CustomSchoolAttributes.java",
    
    # 实体管理
    "entity/PlagueZombieManager.java",
    
    # 法术管理
    "spell/IceSculptureManager.java",
    "spell/ResurrectionRuneManager.java",
    "spell/BlizzardManager.java",
    
    # 学派
    "school/ElementSchoolRegistry.java",
    "school/ElementAttributeRegistry.java",
    
    # 命令
    "command/ElementReactionCommands.java",
]

# 替换规则
REPLACEMENTS = [
    # 替换 Config.XXX_DEBUG_OUTPUT.get() 包裹的 LOGGER 调用
    (
        r'if\s*\(\s*com\.legendarymage\.legendarymagemod\.Config\.[A-Z_]+_DEBUG_OUTPUT\.get\(\)\s*\)\s*\{\s*\n\s*LegendaryMage\.LOGGER\.(debug|info)\s*\(\s*\n?\s*"([^"]+)"(?:\s*,\s*([^)]+))?\s*\)\s*;\s*\n\s*\}',
        r'com.legendarymage.legendarymagemod.ModLogger.\1("\2"\3);'
    ),
    
    # 替换直接的 LOGGER 调用（不在if语句中）
    (
        r'LegendaryMage\.LOGGER\.(debug|info|warn|error)\s*\(\s*\n?\s*"([^"]+)"(?:\s*,\s*([^)]+))?\s*\)\s*;',
        r'com.legendarymage.legendarymagemod.ModLogger.\1("\2"\3);'
    ),
]


def process_file(file_path: Path) -> tuple[int, int]:
    """
    处理单个文件
    
    返回: (替换次数, 删除的if语句次数)
    """
    if not file_path.exists():
        print(f"⚠️ 文件不存在: {file_path}")
        return 0, 0
    
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    original_content = content
    replacements = 0
    if_removed = 0
    
    # 处理 Config.XXX_DEBUG_OUTPUT.get() 包裹的 LOGGER 调用
    # 匹配多行if语句
    pattern1 = re.compile(
        r'if\s*\(\s*com\.legendarymage\.legendarymagemod\.Config\.[A-Z_]+_DEBUG_OUTPUT\.get\(\)\s*\)\s*\{\s*\n'
        r'(\s*)LegendaryMage\.LOGGER\.(debug|info)\s*\(\s*\n?'
        r'(\s*)"([^"]+)"'
        r'((?:\s*,\s*[^)]+)?)'
        r'\s*\)\s*;\s*\n'
        r'\s*\}',
        re.MULTILINE
    )
    
    def replace_if_logger(match):
        nonlocal replacements, if_removed
        indent = match.group(1)
        level = match.group(2)
        inner_indent = match.group(3)
        message = match.group(4)
        args = match.group(5)
        
        # 根据日志级别选择方法
        if level == 'debug':
            method = 'spellDebug'  # 默认使用spellDebug，可能需要根据上下文调整
        else:
            method = 'spell'
        
        result = f'{indent}com.legendarymage.legendarymagemod.ModLogger.{method}({inner_indent}"{message}"{args});'
        replacements += 1
        if_removed += 1
        return result
    
    content = pattern1.sub(replace_if_logger, content)
    
    # 处理直接的 LOGGER 调用（不在if语句中）
    pattern2 = re.compile(
        r'LegendaryMage\.LOGGER\.(debug|info|warn|error)\s*\(\s*\n?\s*"([^"]+)"((?:\s*,\s*[^)]+)?)\s*\)\s*;'
    )
    
    def replace_direct_logger(match):
        nonlocal replacements
        level = match.group(1)
        message = match.group(2)
        args = match.group(3)
        
        # 根据日志级别选择方法
        if level == 'debug':
            method = 'spellDebug'
        elif level == 'info':
            method = 'spell'
        elif level == 'warn':
            method = 'warn'
        else:  # error
            method = 'error'
        
        result = f'com.legendarymage.legendarymagemod.ModLogger.{method}("{message}"{args});'
        replacements += 1
        return result
    
    content = pattern2.sub(replace_direct_logger, content)
    
    # 如果内容有变化，写回文件
    if content != original_content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"✅ 已更新: {file_path.name} (替换{replacements}处, 删除if语句{if_removed}处)")
    else:
        print(f"⏭️  无变化: {file_path.name}")
    
    return replacements, if_removed


def main():
    """主函数"""
    print("=" * 60)
    print("Legendary-Mage 日志更新脚本")
    print("=" * 60)
    print()
    
    total_replacements = 0
    total_if_removed = 0
    processed_files = 0
    
    for relative_path in FILES_TO_PROCESS:
        file_path = SOURCE_DIR / relative_path
        replacements, if_removed = process_file(file_path)
        total_replacements += replacements
        total_if_removed += if_removed
        if replacements > 0 or if_removed > 0:
            processed_files += 1
    
    print()
    print("=" * 60)
    print(f"处理完成!")
    print(f"  - 处理文件数: {processed_files}")
    print(f"  - 总替换次数: {total_replacements}")
    print(f"  - 删除if语句: {total_if_removed}")
    print("=" * 60)
    print()
    print("注意: 请检查替换后的代码，确保分类正确")
    print("建议运行: ./gradlew compileJava 验证编译")


if __name__ == "__main__":
    main()
