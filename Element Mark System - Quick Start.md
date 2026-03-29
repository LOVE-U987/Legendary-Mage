# Element Mark System - Quick Start Guide

> 📚 **Quick Version** - Get started with the Element Mark System in minutes

***

## 🎯 System Overview

This system allows you to **customize the element mark types that spell schools deal**, and supports **compatibility with other mods' schools**.

### Core Features

1. **Custom Element Marks** - Configure element marks via JSON files
2. **External School Compatibility** - Supports Iron's Spells and other mods' schools
3. **Tooltip Display** - Shows element mark info when viewing items in-game
4. **Name Override** - Supports modifying school display names

***

## 📝 Quick Start

### Step 1: Create JSON Configuration File

**File Location**:
```
src/main/resources/data/legendarymage/custom_schools/your_config_name.json
```

**Basic Structure**:
```json
{
  "name": "Your School Name",
  "color": color_value,
  "target_school_id": "mod_id:school_id",
  "element_mark_mapping": {
    "default": "element_type"
  }
}
```

### Step 2: Configure Element Marks

**Supported Element Types**:
- `fire` - Fire 🔥
- `ice` - Frost ❄️
- `lightning` - Lightning ⚡
- `poison` - Poison ☠️
- `holy` - Holy ✨
- `blood` - Blood 🩸
- `eldritch` - Eldritch 🔮
- `ender` - Ender 🟣

### Step 3: Modify School Name (Optional)

**File**: `src/main/resources/assets/legendarymage/lang/en_us.json`

```json
{
  "school.mod_id.school_id": "§5Your School Name§r"
}
```

***

## 🎨 Practical Examples

### Example 1: Modify Ender School

**File**: `ender_multi.json`

```json
{
  "name": "Multi-Ender School",
  "color": 13369344,
  "target_school_id": "irons_spellbooks:ender",
  "element_mark_mapping": {
    "default": "ender",
    "critical": "blood"
  }
}
```

**Effects**:
- Ender school spells → Deal Ender + Blood marks
- Can trigger more element reactions

### Example 2: Blade School Configuration

**File**: `blade_school.json`

```json
{
  "name": "Blade School",
  "color": 7667579,
  "target_school_id": "ess_requiem:blade",
  "element_mark_mapping": {
    "default": "ender",
    "critical": "blood"
  }
}
```

**Effects**:
- Blade spells → Deal Ender + Blood marks
- Supports external mod schools

### Example 3: Change Fire School to Poison

**File**: `fire_to_poison.json`

```json
{
  "name": "Poison Flame School",
  "color": 16711680,
  "target_school_id": "irons_spellbooks:fire",
  "element_mark_mapping": {
    "default": "poison"
  }
}
```

**Effects**:
- Fire spells → Deal Poison marks
- Can trigger Poison-Fire, Lightning-Poison reactions

***

## 🎯 Tooltip Display

The system automatically displays element mark information in item tooltips:

```
Ender Spellbook
Rarity: Rare

Element Marks: 🟣 Ender | 🩸 Blood
```

**Supported Items**:
- ✅ Spellbooks (SpellBook)
- ✅ Scrolls (Scroll)
- ✅ Staffs (StaffItem)
- ✅ Magic Weapons (MagicSword)

***

## ⚠️ Important Notes

### 1. target_school_id Format

**Correct**:
```json
"target_school_id": "irons_spellbooks:fire"
"target_school_id": "ess_requiem:blade"
```

**Incorrect**:
```json
"target_school_id": "fire"  // ❌ Missing namespace
"target_school_id": "irons_spellbooks/fire"  // ❌ Should be colon
```

### 2. Element Types Must Be Correct

**Supported Types**:
```json
"fire", "ice", "lightning", "poison", 
"holy", "blood", "eldritch", "ender"
```

**Incorrect Examples**:
```json
"default": "thunder"  // ❌ Should be "lightning"
"default": "dark"     // ❌ Should be "blood" or "eldritch"
```

### 3. Color Codes

**Used in Language Files**:
```json
{
  "school.irons_spellbooks.ender": "§5Multi-Ender School§r"
}
```

**Color Codes**:
- `§0` - Black
- `§1` - Dark Blue
- `§2` - Dark Green
- `§3` - Dark Cyan
- `§4` - Dark Red
- `§5` - Purple
- `§6` - Gold
- `§7` - Gray
- `§8` - Dark Gray
- `§9` - Blue
- `§a` - Green
- `§b` - Cyan
- `§c` - Red
- `§d` - Light Purple
- `§e` - Yellow
- `§f` - White
- `§r` - Reset (Required!)

***

## 📚 More Information

For detailed documentation, see: [`Element Mark System - Complete Guide.md`](file:///c:/Users/97128/Documents/GitHub/Legendary-Mage/docs/Element%20Mark%20System%20-%20Complete%20Guide.md)

**Includes**:
- ✅ Detailed system mechanics
- ✅ Java code explanations
- ✅ Complete workflow diagrams
- ✅ Advanced usage and troubleshooting
- ✅ Full list of supported mods

***

## 🎉 Summary

### What Can You Do?

- ✅ Customize element marks for any school
- ✅ Add element marks to other mods' schools
- ✅ Modify school display names
- ✅ Create new element reaction combinations
- ✅ View element mark info in-game

### Quick Reference

**JSON Config** → Change element marks
**Language File** → Change display names
**Tooltip System** → Auto-display element marks

**The only limit is your imagination!** 🌟

***

**Document Version**: 1.0
**Last Updated**: 2026-03-29
**Author**: Love_U
