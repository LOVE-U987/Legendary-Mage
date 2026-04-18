### 🚩This warehouse adopts the *branch management* strategy, with each branch corresponding to its module version:

<!-- 
Legendary Mage / 传奇法师
Minecraft NeoForge 1.21.1 魔法扩展模组
-->

<div align="center">

<img src="https://img.shields.io/badge/Minecraft-1.21.1-5D8736?style=for-the-badge&logo=minecraft&logoColor=white">
<img src="https://img.shields.io/badge/NeoForge-21.1+-E04E14?style=for-the-badge">
<img src="https://img.shields.io/badge/Requires-Iron's%20Spells%20'n%20Spellbooks-8A2BE2?style=for-the-badge">

<br><br>

<pre style="font-family: 'Courier New', monospace; line-height: 1.2; color: #9370DB; text-shadow: 0 0 10px #9370DB;">
██╗     ███████╗ ██████╗ ███████╗███╗   ██╗██████╗  █████╗ ██████╗ ██╗   ██╗    ███╗   ███╗ █████╗  ██████╗ ███████╗
██║     ██╔════╝██╔════╝ ██╔════╝████╗  ██║██╔══██╗██╔══██╗██╔══██╗╚██╗ ██╔╝    ████╗ ████║██╔══██╗██╔════╝ ██╔════╝
██║     █████╗  ██║  ███╗█████╗  ██╔██╗ ██║██████╔╝███████║██████╔╝ ╚████╔╝     ██╔████╔██║███████║██║  ███╗█████╗  
██║     ██╔══╝  ██║   ██║██╔══╝  ██║╚██╗██║██╔══██╗██╔══██║██╔══██╗  ╚██╔╝      ██║╚██╔╝██║██╔══██║██║   ██║██╔══╝  
███████╗███████╗╚██████╔╝███████╗██║ ╚████║██║  ██║██║  ██║██████╔╝   ██║       ██║ ╚═╝ ██║██║  ██║╚██████╔╝███████╗
╚══════╝╚══════╝ ╚═════╝ ╚══════╝╚═╝  ╚═══╝╚═╝  ╚═╝╚═╝  ╚═╝╚═════╝    ╚═╝       ╚═╝     ╚═╝╚═╝  ╚═╝ ╚═════╝ ╚══════╝
</pre>


<h1>🧙 Legendary Mage</h1>

<p><i>Unleash Your Arcane Potential</i></p>

<img src="https://img.shields.io/badge/🔥_Elemental_Reactions-FF6B6B?style=flat-square">
<img src="https://img.shields.io/badge/⚡_Unique_Spells-9B59B6?style=flat-square">
<img src="https://img.shields.io/badge/📦_Datapack_Support-2ECC71?style=flat-square">

</div>

---

## 📖 Overview

**Legendary Mage** is a **NeoForge 1.21.1** magic expansion mod built upon **[Iron's Spells 'n Spellbooks](https://www.curseforge.com/minecraft/mc-mods/irons-spells-n-spellbooks)**, introducing a brand-new **Elemental Reaction System** and uniquely designed spells.

> 🎭 *Translation Note: Currently using translation tools, so there may be inaccuracies. Feedback and corrections via GitHub Issues are greatly appreciated!*

---

## ⚡ Core Mechanics

<div align="center">

| Mechanic | Description |
|:---:|:---|
| **Elemental Mark System** | Apply elemental marks to targets; accumulate to thresholds to trigger special effects |
| **Level 2 Threshold** | Can participate in elemental reactions |
| **Level 3 Threshold** | Triggers elemental debuff effects |
| **Datapack Extension** | Supports custom elemental reactions and compatibility with third-party magic mods |

</div>

---

## 🔥 Elemental Debuffs

> Triggered when elemental marks reach **Level 3** (unless specially noted)

### 1. Fire 🔥

| Version | Effect |
|:---:|:---|
| **≤ V1.0.3** | **-5%** max HP per level + magic damage |
| **≥ V1.0.4** | **-2%** max HP per level + magic damage |

### 2. Eldritch 👁️

| Feature | Effect |
|:---:|:---|
| **Special** | **No level prerequisite**, takes effect immediately |
| Effect | **-10%** max HP per level |

### 3. Ice ❄️

| Condition | Effect |
|:---:|:---|
| Ice Mark Level 3 | **Freeze** target once (crowd control) |

### 4. Lightning ⚡ *V1.0.4 Added*

| Feature | Value |
|:---:|:---|
| Effect | **Electrocution**: Periodically releases **chain lightning** to nearby enemies |
| Damage Bonus | **+5** damage per level |
| Cooldown | **3 seconds** |
| Stacking | Levels stackable |

### 5. Poison ☠️

| Version | Effect |
|:---:|:---|
| **≤ V1.0.3** | **Poison**: DoT damage, **stackable** |
| **≥ V1.0.4** | **Armor Melting**: **-2% armor** per level, **stackable** |

### 6. Ender 🖤

| Condition | Effect |
|:---:|:---|
| Ender Mark Level 3 | **50% chance** to trigger **Echo Strike** |

### 7. Holy ✨ *V1.0.4 Added*

| Condition | Effect | Value |
|:---:|:---|:---:|
| Holy Mark Level 3 | **Divine Strike** | **20** damage, CD **5s** |

### 8. Dark 🌑 *V1.0.4 Added, V1.0.5 Reworked*

| Version | Effect |
|:---:|:---|
| **V1.0.5** | **Lightless Night**: **-5% spell resistance** per level, **stackable** |

---

## ⚗️ Elemental Reactions *V1.0.4 Added*

<div align="center">

| Combination | Name | Effect |
|:---:|:---:|:---|
| 🔥 **Fire** + ❄️ **Ice** | **Melt** | Single damage boost |
| 🔥 **Fire** + ⚡ **Lightning** | **Thundercall** | Summon **lightning strike** at target |
| 🔥 **Fire** + ☠️ **Poison** | **Detonation** | Increase **Fire Buff by 1 level** |
| ⚡ **Lightning** + ☠️ **Poison** | **EMP** | Consume **Electrocution** to release **EMP wave** |
| ❄️ **Ice** + ⚡ **Lightning** | **Superconduct** | Gain **Lightning Rod Buff** (-5% Ice/Lightning resistance) |
| 🌑 **Dark** + ☠️ **Poison** | **Plague** | Continuous corrosion *V1.0.4 added* |
| 🖤 **Ender** + Any Element | **Final Echo** | Spell power + resistance boost |
| 👁️ **Eldritch** + 🌑 **Dark** | **Chaos** | Significant spell power boost |

</div>

---

## 📦 Datapack Support

> **V1.0.5+** Full datapack extension support

- ✅ Add **custom elemental marks** to any spell school
- ✅ Create **new elemental reactions**
- ✅ **Seamless compatibility** with third-party magic mods

📚 **Documentation**: [Legendary-Mage Wiki](https://github.com/LOVE-U987/Legendary-Mage/wiki)

---

<div align="center">

## 📥 Download & Feedback

[![CurseForge](https://img.shields.io/badge/CurseForge-F16436?style=for-the-badge&logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/legendary-mage)
[![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?style=for-the-badge&logo=modrinth&logoColor=white)](https://modrinth.com/mod/legendary-mage)
[![GitHub](https://img.shields.io/badge/GitHub-Issues-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/LOVE-U987/Legendary-Mage/issues)

> 🎨 **About Textures**: Currently using placeholder textures. If you're interested in contributing artwork, I'd be truly grateful to hear from you!

*❄️⚡ Forge your legend, master the elements ⚔️🔥*

</div>
