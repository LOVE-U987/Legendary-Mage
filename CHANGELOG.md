# Changelog

All notable changes to Legendary Mage will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.4] - 2026-03-29

### ✨ New Features

#### Element Mark System - Complete Overhaul
- **External School Compatibility System**
  - Added `target_school_id` field to support compatibility with other mods' schools
  - Now supports Iron's Spells, Iron's Spells Requiem, and other mods with school systems
  - Example: Can apply element marks to "Blade" school from Iron's Spells Requiem

- **Tooltip Display System**
  - New `SchoolElementMarkTooltipHandler` automatically displays element mark info on items
  - Shows "元素标记：XXXX" below original text when viewing spell books, scrolls, and staves
  - Client-side event handler using NeoForge's ItemTooltipEvent
  - Extracts school information from:
    - Spell Books (SpellBook)
    - Scrolls (Scroll)
    - Staffs (StaffItem)
    - Magic Swords (MagicSword)

- **School Name Override**
  - Support for customizing school display names in UI
  - External mod schools use literal text rendering for proper display
  - Custom schools use translation keys for localization support

#### Documentation System
- **Bilingual Documentation**
  - Complete Chinese documentation (2 versions)
    - 元素标记系统 - 快速入门.md (Quick Start)
    - 元素标记系统详解.md (Complete Guide)
  - Complete English documentation (2 versions)
    - Element Mark System - Quick Start.md
    - Element Mark System - Complete Guide.md
  - Organized docs/ folder structure for easy maintenance

### 🔧 Changes

#### Custom School System
- **CustomSchoolData.java**
  - Added `targetSchoolId` field (Optional<String>)
  - Updated Codec to support new field
  - Enables JSON configs to target external mod schools

- **SchoolElementMappingRegistry.java**
  - Modified `registerMappings()` to use `target_school_id` when present
  - Logs mapping registration for external mod schools
  - Maintains backward compatibility with custom schools

- **CustomSchoolRegistry.java**
  - Modified `createSchoolType()` to handle external vs custom schools differently
  - External schools: Use `Component.literal()` with JSON name
  - Custom schools: Use `Component.translatable()` with translation key
  - Fixed font rendering issue for external mod school names

#### Code Quality
- Removed deprecated API usage (hasTag/getTag in Minecraft 1.21)
- Fixed all compilation errors (13 total fixes during development)
- Updated imports to use correct NeoForge 1.21.1 packages
- Simplified item school extraction logic using ISpellContainer API

### 📝 Documentation Updates

#### Quick Start Guide
- 3-step quick start instructions
- 3 practical examples (Ender, Blade, Fire schools)
- Tooltip display explanation
- Color code reference table
- Important notes section

#### Complete Guide
- Full system architecture explanation
- JSON configuration field reference
- External school compatibility guide
- Supported mods list
- Tooltip system implementation details
- Name override methods (2 approaches)
- Complete example configurations
- Troubleshooting section (4 common issues)
- Advanced usage and creative combinations

### 🐛 Bug Fixes

- Fixed JSON configuration not applying to external mod schools
- Fixed external mod school names not rendering in UI
- Fixed compilation errors with MagicWeapon class dependency
- Fixed ItemTooltipEvent import path
- Fixed MOD_ID vs MODID constant naming
- Fixed deprecated hasTag()/getTag() method usage
- Fixed SchoolRegistry.get() method not existing
- Fixed SchoolType.getIdentifier() changed to getId()

### 📦 Technical Details

#### Files Added
- `src/main/java/com/legendarymage/legendarymagemod/client/SchoolElementMarkTooltipHandler.java`
- `docs/Element Mark System - Quick Start.md`
- `docs/Element Mark System - Complete Guide.md`
- `docs/元素标记系统 - 快速入门.md`
- `docs/元素标记系统详解.md`
- `src/main/resources/data/legendarymage/custom_schools/blade_school.json`
- `src/main/resources/data/legendarymage/custom_schools/example_fire_override.json`
- `src/main/resources/data/legendarymage/custom_schools/example_ice_override.json`
- `src/main/resources/data/legendarymage/custom_schools/example_ender_override.json`

#### Files Modified
- `src/main/java/com/legendarymage/legendarymagemod/data/CustomSchoolData.java`
- `src/main/java/com/legendarymage/legendarymagemod/data/SchoolElementMappingRegistry.java`
- `src/main/java/com/legendarymage/legendarymagemod/data/CustomSchoolRegistry.java`
- `src/main/java/com/legendarymage/legendarymagemod/spell/MagicShotgunSpell.java` (debug logging)

#### Files Deleted
- 6 old MD documentation files (consolidated into 4 new files)

### 🎯 Example Configurations

#### Blade School (External Mod)
```json
{
  "name": "咒刃学派",
  "color": 7667579,
  "description": "将魔法与近战武器结合的战斗流派",
  "target_school_id": "ess_requiem:blade",
  "compatible_elements": ["ender", "blood", "poison"],
  "element_mark_mapping": {
    "default": "ender",
    "critical": "blood",
    "dot": "poison"
  }
}
```

#### Fire School Override
```json
{
  "name": "自定义火焰学派",
  "color": 16711680,
  "target_school_id": "irons_spellbooks:fire",
  "element_mark_mapping": {
    "default": "poison"
  }
}
```

### 🌟 Supported Mods

The element mark system now supports schools from:
- Iron's Spells 'n Spellbooks (irons_spellbooks:*)
- Iron's Spells Requiem (ess_requiem:*)
- Ace's Spell Utils (aces_spell_utils:*)
- Any mod using Iron's Spells API school system

### 📚 Development Notes

- **Minecraft Version**: 1.21.1
- **NeoForge Version**: 21.1.219
- **Iron's Spells API**: 3.15.0+
- **Java Version**: 17+

#### Key APIs Used
- `ISpellContainer` - Spell container system
- `SpellSlot.getSpell()` - Get spell from slot
- `SpellType.getSchoolType()` - Get school from spell
- `SchoolType.getId()` - Get school ResourceLocation
- `ItemTooltipEvent` - Client-side tooltip events

---

## [1.0.3] - Previous Version

### Features
- Initial Element Mark System implementation
- Basic school-element mapping
- JSON configuration support
- Element reaction system

### Known Issues (Fixed in 1.0.4)
- Could not support external mod schools
- No tooltip display for element marks
- Documentation was fragmented (6 separate files)
- Various compilation errors

---

## Summary of Changes from 1.0.3 to 1.0.4

The 1.0.4 update represents a **major enhancement** to the Element Mark System, focusing on:

1. **External Mod Compatibility** - The system can now work with any mod that uses Iron's Spells API, including Iron's Spells Requiem's Blade school.

2. **User Experience** - Added in-game tooltip display so players can see element mark information directly when viewing items.

3. **Documentation** - Completely reorganized and rewrote all documentation, providing both quick start and comprehensive guides in both Chinese and English.

4. **Code Quality** - Fixed all compilation errors and updated to use current NeoForge 1.21.1 APIs.

This update transforms the Element Mark System from a basic customization tool into a **comprehensive, production-ready system** that supports cross-mod compatibility and provides excellent user documentation.
