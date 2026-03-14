### 🚩This warehouse adopts the *branch management* strategy, with each branch corresponding to its module version:

# 🧙*Legendary Mage - Unleash Your Arcane Potential!*

> 🎭I'm currently using translation tools, so there might be some inaccuracies. I appreciate your understanding. If you're willing to assist me, please feel free to submit an issue on GitHub.

## 📖 Overview

- 🧙**Legendary Mage** is a magic expansion mod for **Minecraft NeoForge 1.21.1** built on top of ​**Iron's Spellbooks**​. It features a brand-new **Elemental Reaction System** alongside a collection of uniquely designed spells.

> 🎨 ​**About Art Assets**​: The mod currently uses placeholder textures. As a student developer with limited time and resources, I welcome any artists interested in contributing to reach out!

---

## ✨ Key Features

### 🔮 Spell System (7 Unique Spells)

表格

| Spell Name                     | School           | Level | Rarity         | Mechanic                                                 |
| :------------------------------- | :----------------- | :------ | :--------------- | :--------------------------------------------------------- |
| **Resurrection Rune**    | Blood            | 1-5   | Legendary      | Creates a zone that converts slain creatures into undead |
| **Implosion**            | Fire             | 1-6   | Legendary      | Pulls enemies together then detonates in flames          |
| **Pyromaniac**           | Fire             | 1-5   | Legendary      | Stacking Flame debuff; detonates on death                |
| **Living Ice Sculpture** | Ice              | 1-8   | Legendary      | Spawns ice sculptures that transform into combat allies  |
| **Blizzard**             | Ice              | 1-3   | Legendary      | Instant AoE with sustained damage and slowness           |
| **Elemental Burst**      | Elemental        | 1-5   | Epic-Legendary | Cycles through Fire/Ice/Lightning damage (WIP)           |
| **Magic Shotgun**        | Spellblade/Ender | 1-5   | Legendary      | Two-stage casting; converts spells into melee damage     |

---

### 🌟 Elemental Reaction System

**8 Elemental Types** and ​**6 Elemental Reactions**​:

#### 🚩Elemental Types

表格

| Element                | Color       | Description                  |
| :----------------------- | :------------ | :----------------------------- |
| 🔥 **Fire**      | Orange-Red  | Burn and burst damage        |
| ❄️ **Ice**     | Cyan        | Control and sustained damage |
| ⚡ **Lightning** | Violet      | High burst and chaining      |
| ☠️ **Poison**  | Lime Green  | Sustained toxin damage       |
| 🩸 **Blood**     | Crimson     | Life manipulation and undead |
| ✨ **Holy**      | Gold        | Healing and purification     |
| 🔮 **Eldritch**  | Indigo      | Forbidden dark powers        |
| 🌑 **Ender**     | Dark Purple | Space and phasing            |

#### 🚩Reaction Table

表格

| Reaction                   | Trigger          | Effect                                                            |
| :--------------------------- | :----------------- | :------------------------------------------------------------------ |
| **Melt**             | Ice + Fire       | Bonus damage based on both schools' power                         |
| **Burning**          | Poison + Fire    | Apply Flame debuff; sustained burning                             |
| **Thunderfire**      | Lightning + Fire | Summon lightning strike for lightning damage                      |
| **Divine-Crimson**   | Holy + Blood     | Bonus holy damage                                                 |
| **Eldritch-Crimson** | Eldritch + Blood | Caster gains **Chaos Buff** (spell power boost)             |
| **Ender Echo**       | Ender + Any      | Caster gains **Ender Echo Buff** (spell power + resistance) |

#### 🚩Elemental Mark Mechanic

* Taking damage applies corresponding **Elemental Mark** (8 types)
* Marks have ​**3 tiers**​, upgraded by repeated damage of same type
* Marks at **Tier ≥2** can participate in reactions
* Successful reaction clears the corresponding marks

---

### 💫 Buff/Effect System

#### 🎇Elemental Marks (8)

`Fire Mark`, `Ice Mark`, `Lightning Mark`, `Poison Mark`, `Blood Mark`, `Holy Mark`, `Eldritch Mark`, `Ender Mark`

#### 🛠️Special Buffs (4)

表格

| Buff                    | Source                      | Effect                         |
| :------------------------ | :---------------------------- | :------------------------------- |
| **Chaos**         | Eldritch-Crimson Reaction   | Spell power increase           |
| **Ender Echo**    | Ender Reaction              | Spell power + magic resistance |
| **Magic Shotgun** | Magic Shotgun spell         | Convert spells to melee damage |
| **Flame**         | Burning Reaction/Pyromaniac | Fire DOT; explodes on death    |
