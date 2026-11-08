<p align="center">
  <img src="https://cdn.modrinth.com/data/cached_images/0717aa272d6b2a1153ff80f4a4081d1ed91b28e1.png" alt="StrengthSMP" width="100%"/>
</p>

<h3 align="center">Every kill takes 1 Strength from your victim and gives it to you. Every death costs you one.</h3>

<p align="center">
  <a href="https://modrinth.com/plugin/strength-smp-sagheerakram"><img src="https://img.shields.io/badge/Modrinth-Download-1bd96a?logo=modrinth&style=for-the-badge" alt="Modrinth"></a>
  <a href="https://www.curseforge.com/minecraft/bukkit-plugins/strength-smp"><img src="https://img.shields.io/badge/CurseForge-Download-f16436?logo=curseforge&style=for-the-badge" alt="CurseForge"></a>
  <a href="https://builtbybit.com/resources/strength-smp.105677/"><img src="https://img.shields.io/badge/BuiltByBit-Download-2f81f7?style=for-the-badge" alt="BuiltByBit"></a>
  <a href="https://discord.gg/rACYmjqxNw"><img src="https://img.shields.io/badge/Discord-Support-5865f2?logo=discord&logoColor=white&style=for-the-badge" alt="Discord"></a>
</p>

<p align="center">
  <b>Minecraft 1.16 to 26.x</b> · Spigot, Paper, Purpur · Java 8+ · no required dependencies · PlaceholderAPI optional
</p>

---

## Contents

- [How it works](#how-it-works)
- [Six classes](#six-classes)
- [Bind your weapon](#bind-your-weapon)
- [The economy](#the-economy)
- [Items](#items)
- [Fair play](#fair-play)
- [Commands](#commands)
- [Placeholders](#placeholders)
- [Ranks](#ranks)
- [Building from source](#building-from-source)
- [What's new in 1.1.12](#whats-new-in-1112)

---

## How it works

<p align="center"><img src="https://cdn.modrinth.com/data/cached_images/294ff1cc9595b1289032bae75efa9082e1a3363e.png" alt="Strength runs from -10 to +10" width="100%"/></p>

Strength runs from **-10 to +10**. Each point means more damage dealt and less damage taken, and your rank sits in chat next to your name, so everyone can see who's winning and who's worth hunting.

## Six classes

Everyone rolls a class weapon, and each one plays differently. Don't like your roll? Craft a **Reroll Grimoire** and spin a new class.

<table>
  <tr>
    <td><img src="https://cdn.modrinth.com/data/cached_images/7ee2144995bf3e44da47704ac42fdce7c4534d9e.jpeg" alt="Assassin"/></td>
    <td><img src="https://cdn.modrinth.com/data/cached_images/c8d8098d0132fff3fc427830b71709c8883e9f28.jpeg" alt="Juggernaut"/></td>
  </tr>
  <tr>
    <td><img src="https://cdn.modrinth.com/data/cached_images/0a6f4364bd35b0d77f58fca0d29469b60b36c452.jpeg" alt="Tank"/></td>
    <td><img src="https://cdn.modrinth.com/data/cached_images/afecf194ff4f5495fe132981922819038d956f07.jpeg" alt="Archer"/></td>
  </tr>
  <tr>
    <td><img src="https://cdn.modrinth.com/data/cached_images/6eae4de808f340ef764abe3f2f8544beb9e9985f.jpeg" alt="Scorpion"/></td>
    <td><img src="https://cdn.modrinth.com/data/cached_images/5f75fbc017d1feb0edcbc2f48385ae9aeae953a2.jpeg" alt="Gladiator"/></td>
  </tr>
</table>

| Class | Weapon | Bound ability |
| :--- | :--- | :--- |
| Assassin | Sword | **Excalibur Beam**, a 12-block crescent |
| Juggernaut | Axe | **Dimensional Cleave**, a rift that pulls, then implodes |
| Tank | Shield | **Forcefield**, a 6-block knockback ring |
| Archer | Bow | **Gravity Singularity**, a vortex |
| Scorpion | Crossbow | **Scorpion Hook**, drags a target to your feet |
| Gladiator | Trident | **Poseidon's Wrath**, chain lightning |

<p align="center"><img src="https://cdn.modrinth.com/data/cached_images/09ac8097869ec142185946508a2430bc3eb5de3a.jpeg" alt="Reroll Grimoire recipe" width="70%"/></p>

## Bind your weapon

<p align="center"><img src="https://cdn.modrinth.com/data/cached_images/40dc1090452d70f36a28831c18a562c09e9a621d.jpeg" alt="The /bind menu" width="100%"/></p>

`/bind` costs 1 Strength. A bound weapon can't be dropped by accident, flies back to you after you die, and unlocks its **F / Q** ability. Pick one of three models for it, and the menu keeps count of your kills, damage, time and abilities used.

<p align="center"><img src="https://cdn.modrinth.com/data/cached_images/c8cdfd8a8de308fa3758e8a6facabab20ded901e.jpeg" alt="Three models per weapon" width="100%"/></p>

## The economy

<p align="center"><img src="https://cdn.modrinth.com/data/cached_images/9499eeddc952ad9b2e026269187694df99e3a7f8.jpeg" alt="The /strength menu" width="100%"/></p>

- **Underdog bonus:** beat someone 3+ Strength above you and you take double.
- **`/withdraw`** turns Strength above 0 into shards you can trade, stash or lose.
- **Bounties** put a price on anyone's head, and the killer claims it.
- **The Monarch:** the strongest player is crowned and glows. Anyone can track them, but only by direction, never coordinates.

## Items

<p align="center"><img src="https://cdn.modrinth.com/data/cached_images/7b0523b56f9e56829e187675cab9f5c83ca0e2bd.png" alt="Strength Shard, Reroll Grimoire and Death Certificate" width="100%"/></p>

3D models with animated details, plus custom models for every class weapon. The resource pack installs automatically, and it's optional.

<p align="center"><img src="https://cdn.modrinth.com/data/cached_images/a2f4f7dd7a3d8ee20aa864995b8203332f0a4be8.jpeg" alt="Strength Shard recipe" width="70%"/></p>

## Fair play

Totem limit · kill-farm protection · 15s combat tag · protection after death and for new players · Strength potions blocked. Every rule can be turned off in `config.yml`.

## Commands

### Players

| Command | Description |
| :--- | :--- |
| `/strength` (`/str`, `/power`) | Your rank, class, the Monarch and every bounty, in one menu |
| `/bind` | Bind your class weapon and pick its model |
| `/withdraw <amount>` | Turn Strength into Strength Shards |
| `/bounty <player> <amount>` | Put a bounty on a player, or open the bounty board |
| `/removeprotection` | Drop your own death or newbie protection |

### Admins (`strengthsmp.admin`)

| Command | Description |
| :--- | :--- |
| `/strengthsmp reload` | Reload config, messages and webhooks |
| `/strengthsmp setstrength <player> <amount>` | Set a player's Strength |
| `/strengthsmp setweapon <player> <class>` | Set a player's class |
| `/strengthsmp reroll <player>` | Re-roll a player's class |
| `/strengthsmp info <player>` | Show a player's details |
| `/strengthsmp maintenance <on\|off>` | Pause all Strength changes |
| `/strengthsmp setupboard` | Set up the leaderboard |
| `/bountyrefund <player>` | Refund the bounties on a player |
| `/punish <player>` | Double a player's Strength loss for 24 hours |
| `/notimer <player>` | Remove a player's protection timers |
| `/nonewbie <player>` | Remove a player's newbie protection |
| `/killreset` | Reset kill counts for a new season |
| `/announce [info\|monarch]` | Post an announcement to Discord |

## Placeholders

With PlaceholderAPI installed, use `%strengthsmp_<name>%`:

| Name | Example |
| :--- | :--- |
| `strength` | `5` |
| `strength_rank` | `[Champion]` |
| `max_strength` | `10` |
| `bounty` | `3` |
| `monarch` / `monarch_name` | `Floki` |
| `season_day` | `14` |
| `contract_progress` / `contract_goal` / `contract_percentage` | `3` / `5` / `60%` |
| `combat_status` | `In Combat` |
| `weapon_class` | `Sword (Assassin)` |
| `has_bound_weapon` / `bound_weapon_class` | `Yes` / `Sword` |
| `kill_history_count` | `42` |

## Ranks

21 ranks, one for every Strength level, all renameable in `config.yml`:

| Strength | Rank | | Strength | Rank |
| :---: | :--- | :--- | :---: | :--- |
| -10 | Hopeless | | 1 | Initiate |
| -9 | Wretched | | 2 | Soldier |
| -8 | Frail | | 3 | Knight |
| -7 | Weakling | | 4 | Veteran |
| -6 | Peasant | | 5 | Champion |
| -5 | Vassal | | 6 | Warlord |
| -4 | Citizen | | 7 | Conqueror |
| -3 | Squire | | 8 | Grandmaster |
| -2 | Apprentice | | 9 | Overlord |
| -1 | Novice | | 10 | **Demigod** |
| 0 | Neutral | | | |

Also included: Discord webhooks (kill feed, live leaderboard), Blood Moon and Titan Rift events, and 9 languages (English, Russian, Spanish, German, French, Portuguese, Hindi, Urdu, Hebrew).

## Building from source

You need a JDK (17 or newer) and Maven.

```bash
git clone https://github.com/SagheerAkram/strength-smp.git
cd strength-smp
mvn package
```

The plugin is written to `target/StrengthSMP-1.1.12.jar`. It's compiled for Java 8, so the same jar runs on every supported server version.

## What's new in 1.1.12

- **Bounties pay out:** whoever kills the target claims the bounty. Refunded bounties no longer come back after a restart.
- **Bound weapons are safe:** no duplicates with keep-inventory, never lost on logout or restart, and they can't be slipped into chests or onto armor stands.
- **Fairer combat:** weapon damage keeps vanilla's attack cooldown, enchantments and crits. Combat-logging follows the same protection and anti-farm rules as a real kill.
- **Safer saving:** no overlapping writes, no half-written files after a crash, and the shutdown save always runs.
- **Protection that works:** death protection, `anti-farm.max-kills-same-player` and `combat.grace-period` behave as configured.
- **Runs on 1.16:** fixed crashes on 1.16 servers.

---

<p align="center">
  <b>StrengthSMP Pro</b> is a separate paid edition that adds <b>The Hollow</b>: die at -10 and you wake up in a world the plugin builds by itself, and you have to earn your way out. Coming soon on BuiltByBit.<br/><br/>
  Questions or bugs: <a href="https://discord.gg/rACYmjqxNw">Discord</a> · Made by <b>Sagheer Akram</b> (Floki)
</p>
