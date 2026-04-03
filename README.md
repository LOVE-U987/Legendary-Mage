# 🚩Legendary Mage v1.0.4 Update Log

## 📋 Update Overview

| Category | Number of Changes | Status |
|:---:|:---:|:---:|
| 🌟 Element Anomaly | 6 items | 5 enhancements, 1 adjustment |
| ⚡ Elemental Reaction | 3 Items | Brand-new Mechanism |
| 🔥 Numerical balance | 2 items | Optimization and adjustment |
| 🛠️ Data Package Support | 1 | Compatible with other modules |


---

## 🔥 Element Abnormal State Redesign

### <span style="color:#e74c3c">🔥 Flame Anomaly</span>

| Level | Effect | Change |
|:---:|:---|:---:|
| **Level 3** | **2%** of maximum health per level | ⬇️ Weakened (originally 5%) |

---

### <span style="color:#f1c40f">✨ Abnormal Brightness</span>

| Level | Effect | Notes |
|:---:|:---|:---|
| **Level 3** | **20%** of Holy Blight damage | Elemental Mark does not update |
| | | ⏱️ CD: **5 seconds** |

> ⚠️ **Note**: Holy Strike is an independent damage source and does not trigger Elemental Chain Reaction

---

### <span style="color:#2c3e50">🌑 Dark Anomaly</span>

| Level | Effect | Characteristic |
|:---:|:---|:---|
| **Level 3** | Obtain the "Dark and Dull" buff | 🔷 Stackable |
| | Reduces **Blood Magic Resistance** | Calculated independently for each level |

---

### <span style="color:#95a5a6">🌫️ Uninfluenced Anomaly</span>

| Level | Effect | Trigger Probability |
|:---:|:---|:---:|
| **Level 3** | **Echo Strike** added to attack | **50%** |

---

### <span style="color:#8e44ad">🔮 Unusual Black Magic</span>

| Mechanism | Value |
|:---|:---:|
| Spell Resistance Reduction | **-10%** / Level |
| Superposition method | Linear superposition |

---

### <span style="color:#3498db">⚡ Electric Shock Anomaly</span> ⭐ Key Update

| Level | Effect | Parameter |
|:---:|:---|:---|
| **Level 3** | Obtain the "Electrify" buff | 🔷 Stackable |
| Trigger mechanism | Release chain lightning every **2 seconds** | Fixed interval |
| Damage Value | **5** / Hit | Ignores Defense |
| Special rules | Do not update element tags | Independent calculation |

```

```

⚡ Chain lightning mechanism ⚡
- Trigger interval: 2 seconds
-  Release position: Current position of the self
-  Damage type: Lightning damage
- Element interaction: ❌ Do not update tags
- Stacking Effect: ✅ The higher the level, the damage increases while the frequency remains unchanged

---

## ⚔️ Elemental Reaction System

### Quick Reference Table for Reactions

| Reaction | Element combination | Effect | Key value |
|:---:|:---:|:---|:---|
| **Thunder Poison Reaction** | ⚡Thunder + 🦠Poison | Range: Electromagnetic Wave | Damage = Electricity Sensitivity Level × 5
Range = 3 squares |
| **Ice and Thunder Reaction** | ❄️Ice + ⚡Thunder | Lightning Rod buff | Thunder Resistance -5% per level
Ice resistance -5%/level
🔷 Stackable |
| **Dark Poison Reaction** | 🌑Dark + 🦠Poison | Plague System | See below for details ⬇️ |

---

### 💀 Detailed Explanation of Plague Mechanism (Dark Poison Reaction)

#### Basic effect

```

```

┌─────────────────────────────────────┐
│  "Plague" buff attribute                  │
├─────────────────────────────────────┤
│  • Maximum Health: -2% / Level                │
│  • Stacking method: 🔷 Unlimited stacking allowed          │
│  • Death Transformation: 75% turn into our zombies ⬆️    │
│    (originally 25%)                          │
│  • Zombie Lifespan: Forced Poison Explosion Death after 1 Minute    │
└─────────────────────────────────────┘


#### Poison explosion propagation mechanism

| Attribute | Calculation formula | Result |
|:---|:---|:---:|
| **Propagation range** | Fixed value | **3 cells** |
| **buff level** | original level ÷ 2 | round down |
| **Damage Value** | 2 × Current buff Level | Area Damage |
| **Communication target** | Surrounding enemies | All targets |

#### Zombie unit attributes
🧟 Our zombie generation rules
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Base health = Original health of dead creature × 0.5
Base damage = original damage of dead creature × 0.5
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Special mechanism:
├─ Existence duration: 60 seconds
├─ End of time limit: Forcefully trigger poison explosion
├─ Poison explosion effect: Spread plague to enemies within 3 squares
└─ Chain reaction: Newly infected individuals can also be transformed into zombies

---

## 🛠️ Data Package Support

### 🎉 For details, please refer to [WIKI](https://github.com/LOVE-U987/Legendary-Mage/wiki)

---

## 📊 Summary of Numerical Changes

### Weakening items ⬇️

| Item | Old value | New value | Range |
|:---|:---:|:---:|:---:|
| Life Loss from Raging Flames | 5%/Level | 2%/Level | -60% |

### Enhancements ⬆️

| Item | Old mechanism | New mechanism | Improvement |
|:---|:---|:---|:---:|
| Dark Poison Death Conversion | 25% Zombie | 75% Zombie | +200% |
| Electrification System | No Level 3 Effect | Chain Lightning Mechanism | Brand New |
| Bright Strike | None | 20% Holy Damage | New |

### New mechanism 🆕

- ✅ Dark and Dull buff system
- ✅ Echo combat mechanism
- ✅ Electromagnetic wave range damage
- ✅ Reduction in resistance of lightning rod
- ✅ Plague transmission system
- ✅ Element Mark System


<div align="center">

*Balance adjustment | Mechanism optimization | Experience upgrade*

📅 Update Date: March 29, 2026
🔖 Version: v1.0.4

</div>

