# Floki Strength SMP - Architecture & Data Flow

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    STRENGTH SMP PLUGIN                      │
│                  (Main Entry: StrengthSMP.java)             │
└──────────────────────┬──────────────────────────────────────┘
                       │
          ┌────────────┼────────────┐
          │            │            │
    ┌─────▼─────┐  ┌────▼────┐  ┌──▼────────┐
    │  Config   │  │   Data   │  │ Discord   │
    │  Manager  │  │ Manager  │  │ Manager   │
    │(Settings) │  │(Players) │  │(Bot)      │
    └─────┬─────┘  └────┬────┘  └──┬────────┘
          │            │           │
          └────────────┼───────────┘
                       │
          ┌────────────┼────────────────────┐
          │                                 │
    ┌─────▼────────┐           ┌──────────▼──────┐
    │    LISTENERS │           │    COMMANDS     │
    │              │           │                │
    │ • Death      │           │ • /strength    │
    │ • Damage     │           │ • /withdraw    │
    │ • Combat     │           │ • /bounty      │
    │ • Items      │           │ • /soulbind    │
    │ • Potions    │           │ • /ability     │
    │ • Weapons    │           │ • /strengthsmp │
    │              │           │                │
    └──────────────┘           └────────────────┘
          │                           │
          └───────────┬───────────────┘
                      │
             ┌────────▼────────┐
             │  PLAYER STATE   │
             │                │
             │ • Strength     │
             │ • Weapon       │
             │ • Kills/Deaths │
             │ • Bounties     │
             │ • Combat Tag   │
             │ • Soulbound    │
             └────────────────┘
```

## Data Flow: Kill Event

```
Player A (Attacker) kills Player B (Victim)
│
├─1. DeathListener.onPlayerDeath() fires
│
├─2. Check if PvP (attacker exists)
│   ├─ Yes: Continue
│   └─ No: Natural death, return
│
├─3. Anti-farm check
│   ├─ Too many kills? Refund bounty, block reward
│   └─ OK: Continue
│
├─4. Strength transfer
│   ├─ Get victim strength
│   ├─ Check Safety Net (-3 rule)
│   │  ├─ Victim at -3 & Attacker at +3+?
│   │  │  ├─ Yes: Keep inventory, Speed I, no transfer
│   │  │  └─ No: Normal transfer
│   ├─ Check Underdog rule
│   │  ├─ Victim negative & Attacker +3+?
│   │  │  ├─ Yes: Attacker +2 (instead of +1)
│   │  │  └─ No: Attacker +1
│   └─ Remove 1 from victim
│
├─5. Item drops
│   ├─ Drop Nautilus Shell
│   └─ Drop Death Certificate
│
├─6. Bounty handling
│   ├─ Victim has bounty?
│   │  ├─ Yes: Reward attacker, remove bounty
│   │  └─ No: Skip
│
├─7. Update monarch
│   └─ Is attacker now top player? Update visuals
│
├─8. Discord logging
│   └─ Send kill chronicle to audit channel
│
└─9. Update displays
    ├─ Update player name colors
    └─ Refresh dashboard
```

## Data Flow: Command Execution

```
Player runs: /strength

│
├─1. StrengthCommand.onCommand() fires
│
├─2. Verify player (not console)
│
├─3. Get player data
│   ├─ UUID
│   ├─ Strength (from DataManager cache)
│   └─ Weapon (from DataManager cache)
│
├─4. Format message with color codes
│   └─ Strength color depends on value
│
└─5. Send to player + update display
    ├─ Update name prefix
    └─ Update tab list name
```

## Data Persistence Flow

```
On Join:
Player joins
  │
  ├─ DataManager.initializePlayer(UUID)
  ├─ Load from playerdata.yml
  ├─ Populate in-memory caches
  └─ Return cached data (fast)

On Strength Change:
Call setStrength(UUID, amount)
  │
  ├─ Clamp to min/max
  ├─ Update cache immediately
  ├─ On next save: write to file
  └─ (No delay - instant in-game)

On Server Shutdown:
  │
  ├─ Call DataManager.saveAll()
  ├─ Write all caches to playerdata.yml
  └─ File saved safely
```

## Weapon System Flow

```
Random Weapon Assignment (on join):
  │
  ├─ Get random WeaponType from enum
  ├─ Store in DataManager cache
  └─ Return to player

Weapon Detection (on attack):
  │
  ├─ Get attacker's stored WeaponType
  ├─ Route to appropriate listener
  │  ├─ Sword → SwordListener
  │  ├─ Axe → AxeListener
  │  ├─ Trident → TridentListener
  │  ├─ Shield → ShieldListener
  │  ├─ Bow → BowListener
  │  └─ Crossbow → CrossbowListener
  │
  └─ Listener applies weapon mechanics
```

## Combat Logging Detection

```
Player in combat + logs out
  │
  ├─ PlayerQuitEvent fires
  │
  ├─1. Check combat tag
  │    ├─ Tag active & <15s old?
  │    │  ├─ Yes: Continue
  │    │  └─ No: Return (safe logout)
  │
  ├─2. Check ping
  │    ├─ Ping > 500ms?
  │    │  ├─ Yes: Likely connection issue, no penalty
  │    │  └─ No: Intentional logout
  │
  ├─3. Apply penalty
  │    ├─ Victim: -1 STR
  │    └─ Last attacker: +1 STR
  │
  ├─4. Handle bounty
  │    ├─ Victim has bounty?
  │    │  ├─ Yes: Reward last attacker
  │    │  └─ No: Skip
  │
  └─5. Log to Discord
      └─ Audit channel records logout
```

## Configuration Hierarchy

```
config.yml (defaults)
    │
    ├─ Read by Config class on startup
    ├─ Provides getters for all settings
    └─ Type-safe access throughout plugin
        │
        ├─ Used by: DataManager (strength bounds)
        ├─ Used by: DamageListener (grace period)
        ├─ Used by: DeathListener (penalties)
        ├─ Used by: WeaponListeners (cooldowns)
        ├─ Used by: DiscordManager (tokens, channels)
        └─ Used by: PotionBanListener (ban setting)
```

## Class Dependency Map

```
StrengthSMP (Main)
│
├─ Config
│  └─ Reads config.yml
│
├─ DataManager
│  └─ Saves to playerdata.yml
│
├─ DiscordManager
│  └─ Uses JDA library
│
├─ All Listeners
│  ├─ Use: DataManager (read/write)
│  ├─ Use: Config (settings)
│  └─ Use: DiscordManager (logging)
│
└─ All Commands
   ├─ Use: DataManager (data access)
   ├─ Use: Config (settings)
   └─ Use: DiscordManager (logging)
```

## Event Processing Chain

```
EntityDamageByEntityEvent
  │
  ├─ DamageListener checks permissions
  ├─ WeaponListener (Sword/Axe/etc) applies weapon effects
  ├─ CombatListener tags in combat
  └─ Displays updated strength to both players

PlayerDeathEvent
  │
  ├─ Check for PvP (killer exists)
  ├─ DeathListener processes kill
  ├─ Anti-farm check
  ├─ Strength transfer
  ├─ Item drops
  ├─ Bounty rewards
  ├─ PlayerListener updates display
  ├─ DataManager saves to cache
  └─ DiscordManager logs to audit

PlayerJoinEvent
  │
  ├─ DataManager initializes player
  ├─ PlayerListener updates display
  └─ Welcome message if first join

PlayerQuitEvent
  │
  ├─ Combat logout check
  ├─ DataManager saves all
  └─ DiscordManager updates dashboard
```

---

This architecture provides:
- ✓ Separation of concerns (listeners, commands, data)
- ✓ Centralized configuration
- ✓ Efficient in-memory caching
- ✓ Persistence to disk
- ✓ Discord integration hooks
- ✓ Extensible weapon system
- ✓ Clean event flow
