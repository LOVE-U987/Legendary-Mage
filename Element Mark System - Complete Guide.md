# Element Mark System - Complete Guide

> 📚 **Detailed Version** - Comprehensive guide from beginner to master

---

## 📖 Table of Contents

1. [System Overview](#system-overview)
2. [JSON Configuration Guide](#json-configuration-guide)
3. [External School Compatibility](#external-school-compatibility)
4. [Tooltip Display System](#tooltip-display-system)
5. [Name Override](#name-override)
6. [Complete Examples](#complete-examples)
7. [Troubleshooting](#troubleshooting)

---

## System Overview

### What Does This System Do?

This Element Mark System allows you to **customize the element mark types that spell schools deal via JSON files**, and **fully supports compatibility with other mods' schools**.

### Core Features

1. **Custom Element Marks** - Define element marks via JSON files
2. **External School Compatibility** - Supports Iron's Spells, Iron's Spells Requiem, and other mods
3. **Tooltip Display** - Automatically displays element mark info when viewing items in-game
4. **Name Override** - Supports modifying school display names
5. **Element Reaction Trigger** - Accumulate element marks to trigger reactions

### How It Works

```
1. Game Launch
   ↓
2. Load JSON Configuration Files
   ↓
3. Register School → Element Mark Mappings
   ↓
4. Player Casts Spell
   ↓
5. Extract School Info from Item
   ↓
6. Query Element Mark Mapping
   ↓
7. Apply Element Marks
   ↓
8. Check and Trigger Element Reactions
```

---

## JSON Configuration Guide

### File Location

```
src/main/resources/data/legendarymage/custom_schools/your_config_name.json
```

### Complete Structure

```json
{
  "name": "School Name",
  "color": color_value,
  "description": "School Description (Optional)",
  "target_school_id": "mod_id:school_id (Optional)",
  "compatible_elements": ["element1", "element2"],
  "attribute_modifiers": {
    "spell_power_bonus": 0.1,
    "magic_resist_bonus": 0.15,
    "mana_cost_reduction": 0.05,
    "cast_time_reduction": 0.05
  },
  "spell_stats": {
    "damage_multiplier": 1.1,
    "range_multiplier": 1.0,
    "duration_multiplier": 1.2,
    "cooldown_reduction": 0.05
  },
  "element_mark_mapping": {
    "default": "element_type",
    "critical": "element_type (Optional)",
    "dot": "element_type (Optional)"
  }
}
```

### Field Details

#### Basic Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | String | ✅ | School display name |
| `color` | Integer | ✅ | School color (decimal) |
| `description` | String | ❌ | School description |
| `target_school_id` | String | ❌ | Target school ID (for mod compatibility) |

#### Element Mark Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `compatible_elements` | Array | ❌ | List of compatible element types |
| `element_mark_mapping` | Object | ✅ | Element mark mapping configuration |

**element_mark_mapping Sub-fields**:
- `default` - Default case (Required)
- `critical` - On critical hit (Optional, requires code support)
- `dot` - On damage over time (Optional, requires code support)

#### Attribute Modifiers (Optional)

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `spell_power_bonus` | Float | Spell power bonus | `0.1` = +10% |
| `magic_resist_bonus` | Float | Magic resist bonus | `0.15` = +15% |
| `mana_cost_reduction` | Float | Mana cost reduction | `0.05` = -5% |
| `cast_time_reduction` | Float | Cast time reduction | `0.1` = -10% |

#### Spell Stats (Optional)

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `damage_multiplier` | Float | Damage multiplier | `1.2` = +20% |
| `range_multiplier` | Float | Range multiplier | `1.15` = +15% |
| `duration_multiplier` | Float | Duration multiplier | `1.3` = +30% |
| `cooldown_reduction` | Float | Cooldown reduction | `0.1` = -10% |

---

## External School Compatibility

### Supported Mods

#### 1. Iron's Spells 'n Spellbooks ⭐⭐⭐

**Supported Schools**:
- `irons_spellbooks:fire` - Fire School
- `irons_spellbooks:ice` - Frost School
- `irons_spellbooks:lightning` - Lightning School
- `irons_spellbooks:holy` - Holy School
- `irons_spellbooks:ender` - Ender School
- `irons_spellbooks:blood` - Blood School
- `irons_spellbooks:evocation` - Evocation School
- `irons_spellbooks:nature` - Nature School
- `irons_spellbooks:anima` - Anima School
- `irons_spellbooks:eldritch` - Eldritch School

#### 2. Iron's Spells Requiem ⚔️

**Supported Schools**:
- `ess_requiem:blade` - Blade School

#### 3. Any Other Mod 🔮

Any mod with a school system is compatible!

**Format**:
```json
{
  "target_school_id": "mod_id:school_id"
}
```

### Configuration Steps

#### Step 1: Identify Target School ID

**Methods**:
1. Check mod documentation or code
2. Use in-game commands (if mod supports)
3. Check mod's resource files

**Common Format**:
```
mod_namespace:school_path
```

**Examples**:
- `irons_spellbooks:fire` - Iron's Spells Fire School
- `ess_requiem:blade` - Requiem Blade School

#### Step 2: Create JSON Configuration

**Example**: Configure element marks for Blade School

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

#### Step 3: Test Configuration

**Check logs after launching game**:

**Success Logs**:
```
[INFO] Configuring element mark mapping for external mod school: ess_requiem:blade -> legendarymage:blade_school
[DEBUG] Registering mapping: ess_requiem:blade [default] -> ender
[INFO] School element mark mapping registration complete: 1 mappings total
```

**Failure Logs**:
```
[ERROR] Invalid target_school_id: invalid_mod:invalid_school (in example.json)
[WARN] Unknown element type: infire (in example.json mapping)
```

---

## Tooltip Display System

### Feature Description

The system automatically displays element mark information in item tooltips.

### Supported Item Types

- ✅ Spellbooks (SpellBook)
- ✅ Scrolls (Scroll)
- ✅ Staffs (StaffItem)
- ✅ Magic Weapons (MagicSword)

### Display Effect

```
Ender Spellbook
Rarity: Rare

Element Marks: 🟣 Ender | 🩸 Blood
```

### How It Works

```java
@EventBusSubscriber(modid = LegendaryMage.MODID, value = Dist.CLIENT)
public class SchoolElementMarkTooltipHandler {
    
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        // 1. Check item type
        // 2. Extract school IDs
        // 3. Query element mark mapping
        // 4. Add to tooltip
    }
}
```

### Element Mark Display Format

| Element Type | Display Text | Color |
|-------------|-------------|-------|
| Fire | Fire | Red (§c) |
| Frost | Frost | Cyan (§b) |
| Lightning | Lightning | Yellow (§e) |
| Poison | Poison | Green (§a) |
| Holy | Holy | White (§f) |
| Blood | Blood | Dark Red (§4) |
| Eldritch | Eldritch | Purple (§5) |
| Ender | Ender | Purple (§5) |

---

## Name Override

### Method 1: Language File Override (Recommended)

**File**: `src/main/resources/assets/legendarymage/lang/en_us.json`

```json
{
  "school.irons_spellbooks.ender": "§5Multi-Ender School§r",
  "school.irons_spellbooks.fire": "§cPoison Flame School§r",
  "school.ess_requiem.blade": "§8Blade School§r"
}
```

**Effects**:
- ✅ All UI displays new name
- ✅ Simple and quick
- ❌ Does not change element marks

### Method 2: JSON Config + Language File

**Step 1**: Create JSON config
```json
{
  "name": "Multi-Ender School",
  "target_school_id": "irons_spellbooks:ender",
  "element_mark_mapping": {
    "default": "ender"
  }
}
```

**Step 2**: Add language file override
```json
{
  "school.irons_spellbooks.ender": "§5Multi-Ender School§r"
}
```

**Effects**:
- ✅ Changes element marks
- ✅ Changes display name
- ✅ Changes color

---

## Complete Examples

### Example 1: Multi-Ender Configuration

**File**: `ender_multi.json`

```json
{
  "name": "Multi-Ender School",
  "color": 13369344,
  "target_school_id": "irons_spellbooks:ender",
  "compatible_elements": ["ender", "blood", "eldritch"],
  "attribute_modifiers": {
    "spell_power_bonus": 0.15,
    "magic_resist_bonus": 0.12,
    "mana_cost_reduction": 0.0,
    "cast_time_reduction": 0.15
  },
  "spell_stats": {
    "damage_multiplier": 1.2,
    "range_multiplier": 1.3,
    "duration_multiplier": 1.0,
    "cooldown_reduction": 0.1
  },
  "element_mark_mapping": {
    "default": "ender",
    "critical": "blood",
    "dot": "eldritch"
  }
}
```

**Effects**:
- Ender school spells → Deal Ender + Blood + Eldritch marks
- +15% Spell Power
- +12% Magic Resist
- +15% Cast Speed
- +20% Damage
- +30% Range

### Example 2: Poison Flame School

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

### Example 3: Blade School

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

---

## Troubleshooting

### Problem 1: Configuration Not Working

**Checklist**:
1. ✅ JSON file location correct
2. ✅ `target_school_id` format correct
3. ✅ Element type names correct
4. ✅ Target school exists (mod installed)
5. ✅ JSON syntax correct

**Debug Methods**:
- Check logs for error messages
- Use `/reload` command to reload datapacks
- Check if other configs work (rule out system issues)

### Problem 2: Name Not Displaying

**Cause**: Translation key doesn't exist

**Solutions**:
1. **Method 1**: Use `target_school_id` (Recommended)
   ```json
   {
     "name": "Blade School",
     "target_school_id": "ess_requiem:blade"
   }
   ```

2. **Method 2**: Add translation key to language file
   ```json
   {
     "school.legendarymage.my_school": "My School"
   }
   ```

### Problem 3: Game Crash

**Possibilities**:
- JSON syntax errors (missing commas, quotes, etc.)
- Element type name typos
- `target_school_id` format errors

**Solutions**:
1. Use JSON validator to check syntax
2. Check crash logs for error details
3. Remove config files one by one to locate problem file

### Problem 4: Tooltip Not Showing

**Check Items**:
1. ✅ Item contains spells (SpellContainer)
2. ✅ School has element mark mapping
3. ✅ Client has mod loaded
4. ✅ Check logs for errors

**Debug Methods**:
- Check logs for error messages
- Verify item has spells
- Confirm mapping configuration is correct

---

## Advanced Usage

### 1. Multi-School Combination Configuration

Configure **multiple element marks** for one school:

```json
{
  "name": "Chaos School",
  "target_school_id": "irons_spellbooks:ender",
  "compatible_elements": ["ender", "blood", "eldritch", "poison"],
  "element_mark_mapping": {
    "default": "ender",
    "critical": "blood",
    "dot": "eldritch"
  }
}
```

**Effects**:
- Different conditions deal different element marks
- Can trigger multiple element reactions

### 2. Attribute Enhancement Configuration

Add **attribute bonuses** to schools:

```json
{
  "name": "Enhanced Fire School",
  "target_school_id": "irons_spellbooks:fire",
  "attribute_modifiers": {
    "spell_power_bonus": 0.2,        // +20% Spell Power
    "magic_resist_bonus": 0.15,      // +15% Magic Resist
    "mana_cost_reduction": 0.1,      // -10% Mana Cost
    "cast_time_reduction": 0.15      // -15% Cast Time
  },
  "spell_stats": {
    "damage_multiplier": 1.3,        // +30% Damage
    "range_multiplier": 1.2,         // +20% Range
    "duration_multiplier": 1.5,      // +50% Duration
    "cooldown_reduction": 0.2        // -20% Cooldown
  }
}
```

### 3. Cross-Mod Reaction Combinations

Create **new element reactions** by configuring different mods' schools:

```json
// Config 1: Make Blade deal Ender marks
{
  "name": "Blade School",
  "target_school_id": "ess_requiem:blade",
  "element_mark_mapping": {
    "default": "ender"
  }
}

// Config 2: Make Fire School deal Poison marks
{
  "name": "Poison Flame School",
  "target_school_id": "irons_spellbooks:fire",
  "element_mark_mapping": {
    "default": "poison"
  }
}
```

**Effects**:
- Player uses both Blade and Fire spells
- Marks on enemy: Ender + Poison
- Triggers **Ender-Poison Reaction**

---

## 🎉 Summary

### What Can You Do?

- ✅ Customize element marks for any school
- ✅ Add element marks to other mods' schools
- ✅ Modify school display names
- ✅ Create new element reaction combinations
- ✅ View element mark info in-game
- ✅ Add attribute bonuses and spell stats

### Core Concepts

1. **JSON Configuration Files** - Define schools and element mark mappings
2. **Registry** - Manage all school configurations
3. **Event Handling** - Look up config and apply marks on spell hit
4. **Tooltip System** - Auto-display element mark info

### Workflow

```
Spell Hit → Query Config → Apply Marks → Check Reactions
```

### Infinite Creativity

With `target_school_id`, you can:
- 🎨 Make Fire School deal Poison marks (Poison Fire!)
- ⚡ Make Frost School deal Lightning marks (Ice Lightning!)
- 🩸 Make Holy School deal Blood marks (Dark Magic!)
- 🔮 Create completely new element reaction combinations

**Unleash your creativity and build your unique magic system!** 🌟

---

**Document Version**: 1.0
**Last Updated**: 2026-03-29
**Author**: Love_U
