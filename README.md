<p align="center">
  <img src="https://cdn.modrinth.com/data/cached_images/bed2b23cc62873fd95ae03c36db515e82b5b00b4.png" alt="StrengthSMP Banner" width="600"/>
</p>

<h1 align="center">⚔️ StrengthSMP (v1.1.12) ⚔️</h1>

<p align="center">
  <b>The Ultimate Competitive Strength PvP Overhaul for Minecraft Servers</b><br/>
  <i>Ultra-Lightweight • Universally Compatible (1.16 - 26.x) • Fully Customizable</i>
</p>

<p align="center">
  <a href="https://hangar.papermc.io/SagheerAkram/StrengthSMP"><img src="https://img.shields.io/hangar/dt/StrengthSMP?logo=paper&style=flat&color=2ecc71" alt="Hangar Downloads"></a>
  <a href="https://modrinth.com/plugin/strength-smp-"><img src="https://img.shields.io/modrinth/dt/strength-smp-?logo=modrinth&style=flat&color=1bd96a" alt="Modrinth Downloads"></a>
  <a href="https://www.curseforge.com/minecraft/bukkit-plugins/strength-smp"><img src="https://img.shields.io/badge/CurseForge-Available-f16436?logo=curseforge&style=flat" alt="CurseForge"></a>
  <a href="https://www.spigotmc.org/resources/strength-smp.134679/"><img src="https://img.shields.io/badge/Spigot-Available-ffaa00?logo=spigotmc&style=flat" alt="Spigot"></a>
</p>

---

## 🌟 What's New in Version 1.1.12?

* **🎯 Bounties pay out:** whoever kills the target now claims the bounty. Refunded bounties no longer come back after a restart.
* **🗡️ Bound weapons are safe:** no more duplicates with keep-inventory, never lost on logout or restart, and they can't be slipped into chests or onto armor stands.
* **⚔️ Fairer combat:** weapon damage keeps vanilla's attack cooldown, enchantments and crits. Combat-logging follows the same protection and anti-farm rules as a real kill.
* **💾 Safer saving:** no overlapping writes, no half-written files after a crash, and the shutdown save always runs.
* **🛡️ Protection that works:** death protection, `anti-farm.max-kills-same-player` and `combat.grace-period` now behave as configured.
* **🌍 Runs on 1.16:** fixed crashes on 1.16 servers.

Full notes are on the release pages.

---

## 🔗 Official Links
- 🛒 **BuiltByBit (BBB):** [Official Resource Page](https://builtbybit.com/resources/strength-smp-by-floki.105677/)
- 🌐 **SpigotMC:** [Spigot Resource](https://www.spigotmc.org/resources/strength-smp.134679/)
- 🟢 **Modrinth:** [Modrinth Page](https://modrinth.com/plugin/strength-smp-sagheerakram)
- 🔥 **CurseForge:** [CurseForge Resource](https://www.curseforge.com/minecraft/bukkit-plugins/strength-smp)
- 🐙 **GitHub Repo:** [SagheerAkram/strength-smp](https://github.com/SagheerAkram/strength-smp)

---

## 💎 Core Features

### 🔮 Custom 3D Items & Live Particles
![Items](https://cdn.modrinth.com/data/cached_images/bed2b23cc62873fd95ae03c36db515e82b5b00b4.png)

Every custom item features **stunning, live-updating particle effects** in player hands and inventories:
* 🌟 **Strength Orb:** Swirls with golden fire sparks radiating raw combat power.
* 🔮 **Reroll Book:** Surrounded by floating purple magical runes and dark void particles.
* 📜 **Death Certificate:** Dark premium parchment sealed with a glowing red wax skull.

---

### 🩸 Dynamic Strength Economy & Shards
Killing players steals their strength, elevating your status while demoting your victim.
* 💎 **Physical Withdrawals:** Convert active Strength into physical **Strength Shards** (`/withdraw`) to trade in shop GUIs or store safely in vaults.
* 🛡️ **Underdog Advantage:** Weaker players earn **2x Strength** when defeating higher-ranked giants.

![Strength Showcase](https://cdn.modrinth.com/data/cached_images/57e2258a70261028e61c20158a320ad3f73278ef.png)
![Strength Recipe](https://cdn.modrinth.com/data/cached_images/22ed879942fb3732cc367571434f160355fbc37b.png)

---

### 🗡️ Soul-Binding GUI (`/bind`)
Prevent loss of your precious class weapon upon death while unlocking deep statistics tracking!
![Weapon Binding GUI](https://i.postimg.cc/T3xf7bHJ/weapon-binding-gui.png)

---

### 🎲 Weapon Reroll & Awakening Crate System
Cinematic, crate-style shuffle animations when using Reroll Grimoire books to roll a new weapon class.
![Reroll Showcase](https://cdn.modrinth.com/data/cached_images/0b9ac9ab90f17b2abc405dbdf2bc54329aef0e47.png)

---

## 👑 Dynamic Strength Chat Ranks

Prefix titles scale up or down automatically in chat based on players' active **Strength level**:

| Strength Level | Prefix Title | Color / Style | Description |
| :--- | :--- | :--- | :--- |
| **+10 STR** | `[Demigod]` | 🟨 **Fiery Gold** | The ultimate server champion |
| **+9 STR** | `[Overlord]` | 🟪 **Vibrant Pink** | A ruling warlord commanding respect |
| **+8 STR** | `[Grandmaster]`| 🟣 **Dark Purple** | Master of battlefield tactics |
| **+5 STR** | `[Champion]` | 🟥 **Blood Red** | Top-tier PvP warrior |
| **0 STR** | `[Neutral]` | 🟨 **Yellow** | Default starting tier |
| **-10 STR** | `[Hopeless]` | ⬛ **Dark Gray** | Stripped of all power, double effort needed |

---

## ⚔️ Weapon Classes & Abilities

Right-click with your designated weapon class to activate custom active abilities:

### 🗡️ **Sword (Assassin)**
* **Active (Excalibur Beam):** Fires a crescent slicing wave of golden sparks forward 12 blocks, piercing through enemies and launching them upwards.
![Sword Ability](https://i.postimg.cc/C57JRHpf/sword-ezgif-com-optimize.gif)

### 🎯 **Crossbow (Scorpion)**
* **Active (Scorpion Hook):** Fires a high-velocity chain and hook dragging hit targets directly to your feet and stunning them.
![Crossbow Ability](https://i.postimg.cc/XJDgfNz6/crossbow-ezgif-com-optimize.gif)

### 🛡️ **Shield (Tank)**
* **Active (Shield Forcefield):** Releases an expanding electric ring (6-block radius) pushing back enemies and applying Slowness II for 3s. Features passive damage counter-attacks!
![Shield Ability](https://i.postimg.cc/LsBkb8Yj/shield-ezgif-com-optimize.gif)

### 🪓 **Axe (Juggernaut)**
* **Active (Dimensional Cleave):** Creates a spatial rift 3.5 blocks ahead that vacuums nearby entities for 2s, followed by a sonic boom implosion. Passive shield-disabling stuns.

### 🏹 **Bow (Archer)**
* **Active (Gravity Singularity):** Launches a gravity arrow that spawns a vortex on impact, pulling all entities within 8 blocks to the center.

### 🔱 **Trident (Gladiator)**
* **Active (Poseidon Riptide):** Propels yourself forward in air or land with aqua nautilus trails, damaging all entities caught in your flight path.

---

## 👑 Monarch & Bounty Systems
* **Monarch System:** The top killer on the server is crowned the Monarch, broadcasting their status and location with unique perks.
* **Bounty System:** Place item/strength bounties on enemies to prompt server-wide bounty hunts (`/bounty`).
![Bounty System](https://i.postimg.cc/B6h1D8yg/bounty-ezgif-com-optimize.gif)

---

## 🔒 Fair Play & Safeguards
* **Totem Limits:** Configurable cap on Totem of Undying usage per engagement.
* **Anti-Kill Farming:** Prevents repeated strength stealing from the same target within a cooldown period.
* **Potion Ban:** Optional toggle to ban vanilla Strength potions for true skill-based balance.
* **Death & Newbie Protection:** Temporary death cooldown protection to allow fallen players to recover.

![Totem Limit](https://i.postimg.cc/zBcWRbHX/totemgif-ezgif-com-optimize.gif)

---

## 📜 Commands & Permissions

### 👤 Player Commands
| Command | Alias | Description |
| :--- | :--- | :--- |
| `/bind` | - | Open the Soul-Binding menu & statistics grimoire |
| `/strength` | `/str`, `/power` | View active strength level and current rank |
| `/withdraw <amount>` | - | Withdraw strength into physical Strength Shard items |
| `/bounty <player> <amount>`| - | Place a bounty on a target player |
| `/removeprotection` | - | Remove your active death or newbie protection |

### 🛠️ Admin Commands (`strengthsmp.admin`)
| Command | Description |
| :--- | :--- |
| `/strengthsmp reload` | Reload configuration files, message strings, and Discord webhooks |
| `/strengthsmp setstrength <player> <amount>` | Set a player's exact strength value |
| `/strengthsmp setweapon <player> <weapon>` | Set a player's active class weapon |
| `/strengthsmp reroll <player>` | Force reroll a player's class weapon via GUI animation |
| `/strengthsmp info <player>` | View administrative diagnostics for a player |
| `/strengthsmp setupboard` / `setupleaderboard` | Initialize hologram or scoreboard leaderboards |
| `/bountyrefund <player>` | Refund an active bounty placed on a player |
| `/announce [info\|monarch]` | Send custom broadcast announcements to linked Discord channels |
| `/punish <player>` | Punish a player (doubles strength loss on death for 24h) |
| `/notimer <player>` | Remove 4-hour death protection from a player |
| `/nonewbie <player>` | Remove newbie protection from a player |
| `/killreset` | Perform seasonal leaderboard and kill resets |

---

## 🔌 PlaceholderAPI Integration

StrengthSMP provides high-performance expansion placeholders via identifier `%strengthsmp_*%`:

| Placeholder | Output Description | Example Output |
| :--- | :--- | :--- |
| `%strengthsmp_strength%` | Current active Strength | `5` |
| `%strengthsmp_strength_rank%` | Configured rank title | `[Champion]` |
| `%strengthsmp_max_strength%` | Server max strength cap | `10` |
| `%strengthsmp_bounty%` | Current active bounty on player | `250` |
| `%strengthsmp_monarch%` | Name of current Monarch | `SagheerAkram` |
| `%strengthsmp_season_day%` | Current season day counter | `14` |
| `%strengthsmp_contract_progress%` | Player contract progress | `3` |
| `%strengthsmp_contract_goal%` | Player contract target goal | `5` |
| `%strengthsmp_contract_percentage%` | Contract progress percentage | `60%` |
| `%strengthsmp_combat_status%` | In-combat indicator | `In Combat` / `Safe` |
| `%strengthsmp_weapon_class%` | Player class weapon | `Sword (Assassin)` |
| `%strengthsmp_has_bound_weapon%` | Soul-bound status | `Yes` / `No` |
| `%strengthsmp_bound_weapon_class%`| Bound weapon name | `Sword` / `None` |
| `%strengthsmp_kill_history_count%`| Total logged PvP kills | `42` |

---

## 👥 Support & Community
Need assistance or want to suggest new features?
- 💬 **Discord Support Server:** [Join Community Discord](https://discord.gg/rACYmjqxNw)
- 👨‍💻 **Developer:** **Sagheer Akram** *(aka Floki)*
