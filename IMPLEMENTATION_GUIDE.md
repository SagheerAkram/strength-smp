# Implementation Guide for Floki Strength SMP Plugin

## Project Structure Overview

This is a professional Paper/Spigot plugin project for Minecraft 1.21.x+ that implements a comprehensive PvP strength system.

### Directory Structure

```
StrengthSMP PLUGIN/
├── pom.xml                                    # Maven build configuration
├── src/main/
│   ├── java/com/floki/strengthsmp/
│   │   ├── StrengthSMP.java                  # Main plugin class
│   │   ├── config/
│   │   │   └── Config.java                   # Configuration manager
│   │   ├── data/
│   │   │   ├── DataManager.java              # Player data persistence
│   │   │   └── WeaponType.java               # Weapon enumeration
│   │   ├── discord/
│   │   │   └── DiscordManager.java           # Discord bot integration
│   │   ├── listeners/
│   │   │   ├── PlayerListener.java           # Player join/quit
│   │   │   ├── DeathListener.java            # Death mechanics
│   │   │   ├── DamageListener.java           # Damage tracking
│   │   │   ├── CombatListener.java           # Combat tagging
│   │   │   ├── ItemInteractionListener.java  # Item interactions
│   │   │   ├── PotionBanListener.java        # Potion blocking
│   │   │   ├── SwordListener.java            # Sword mechanics
│   │   │   ├── AxeListener.java              # Axe mechanics
│   │   │   ├── TridentListener.java          # Trident mechanics
│   │   │   ├── ShieldListener.java           # Shield mechanics
│   │   │   ├── BowListener.java              # Bow mechanics
│   │   │   └── CrossbowListener.java         # Crossbow mechanics
│   │   ├── commands/
│   │   │   ├── BaseCommand.java              # Command base class
│   │   │   ├── StrengthInfoCommand.java      # /strength command
│   │   │   ├── WithdrawCommand.java          # /withdraw command
│   │   │   ├── BountyCommand.java            # /bounty command
│   │   │   ├── BountyRefundCommand.java      # /bountyrefund command
│   │   │   ├── SoulbindCommand.java          # /soulbind command
│   │   │   ├── AbilityCommand.java           # /ability command
│   │   │   └── AdminCommand.java             # /strengthsmp admin panel
│   │   ├── weapons/                          # (Reserved for weapon implementations)
│   │   └── util/                             # (Reserved for utilities)
│   └── resources/
│       ├── plugin.yml                        # Plugin manifest
│       └── config.yml                        # Default configuration
└── target/                                   # Build output directory

```

## Setup Instructions

### Prerequisites
- Java 21 or later
- Maven 3.8.0+
- Paper API 1.21

### Building the Plugin

1. **Navigate to project directory:**
```bash
cd c:\Users\SM\Desktop\StrengthSMP PLUGIN
```

2. **Build with Maven:**
```bash
mvn clean package
```

3. **Find the built JAR:**
```
target/StrengthSMP-1.0.0.jar
```

4. **Install on server:**
```bash
cp target/StrengthSMP-1.0.0.jar Desktop/test server/plugins/
```

5. **Restart server**

## Configuration (config.yml)

Edit `src/main/resources/config.yml` to customize:

- Strength system parameters (min/max, penalties)
- Weapon cooldowns and durations
- Discord bot token and channel IDs
- Anti-farm settings
- Combat mechanics

## Key Features Implemented

### ✓ Core Data Storage
- YAML-based persistent player data
- In-memory caching for performance
- UUID-based player tracking

### ✓ Configuration System
- Centralized config management
- Type-safe configuration access
- Fully customizable settings

### ✓ Weapon System
- 6 unique weapon classes
- Enum-based weapon management
- Random weapon assignment on join

### ✓ Command Framework
- Base command class for consistency
- Admin permission checking
- Extensible command system

### ✓ Event System
- Listener classes for all major events
- Player join/quit handling
- Death and damage tracking

### ✓ Discord Integration (Basic)
- JDA bot framework
- Connection management
- Ready for dashboard implementation

## Features Requiring Full Implementation

### 1. Death Mechanics (`DeathListener.java`)
- [ ] Process PvP kills
- [ ] Award/deduct strength
- [ ] Drop Nautilus Shell items
- [ ] Create Death Certificate
- [ ] Implement Safety Net rule (-3 rule)
- [ ] Implement Underdog rule
- [ ] Handle bounty rewards
- [ ] Anti-farm detection

### 2. Combat Logging (`CombatListener.java`)
- [ ] Tag players in combat
- [ ] Track logout penalty
- [ ] Check ping threshold
- [ ] Prevent logout abuse

### 3. Weapon Abilities
- [ ] Sword: Dual-wield + auto-crit
- [ ] Axe: Stun mechanic + kinetic damage
- [ ] Trident: Lightning passive + water wave
- [ ] Shield: Speed II + immortality
- [ ] Bow: Sonic beam ultimate
- [ ] Crossbow: Glowing arrows + leash

### 4. Item Interactions (`ItemInteractionListener.java`)
- [ ] Nautilus Shell right-click
- [ ] Reroll Book mechanic
- [ ] Custom item detection

### 5. Soulbind System (`SoulbindCommand.java`)
- [ ] NBT-based item binding
- [ ] UUID checking
- [ ] Cost (3 STR for bind, 1 STR for transfer)
- [ ] Unbreakable enchantment
- [ ] Keep-inventory on death

### 6. Discord Features
- [ ] Live dashboard with embedded messages
- [ ] Auto-updating leaderboard
- [ ] Kill chronicle logging
- [ ] Bounty audit system
- [ ] Teammate list detection

### 7. Potion Banning
- [ ] Detect strength potions
- [ ] Remove from inventory
- [ ] Block dispenser use
- [ ] Periodic cleanup task

## Quick Implementation Checklist

To complete this plugin:

1. **Implement DeathListener** - This is the most critical system
2. **Implement all weapon listeners** - Each has unique mechanics
3. **Implement command logic** - Wire up all commands to data/mechanics
4. **Add Discord dashboard** - Create auto-updating embeds
5. **Implement potion ban** - Background task + event handler
6. **Add anti-farm system** - Map-based kill tracking
7. **Implement soulbind** - NBT tags + inventory management

## Development Tips

### Adding a New Feature
1. Create a new listener class extending `Listener`
2. Register it in `StrengthSMP.registerEventListeners()`
3. Use `plugin.getDataManager()` for data access
4. Use `plugin.getConfigManager()` for settings

### Adding a New Command
1. Extend `BaseCommand`
2. Implement `onCommand()` method
3. Register in `StrengthSMP.registerCommands()`
4. Add command entry to `plugin.yml`

### Accessing Player Data
```java
// Get a player's strength
int strength = plugin.getDataManager().getStrength(player.getUniqueId());

// Set strength
plugin.getDataManager().setStrength(player.getUniqueId(), 5);

// Get player's weapon
WeaponType weapon = plugin.getDataManager().getWeapon(player.getUniqueId());
```

## Testing

Run with Paper test server:
1. Install plugin JAR
2. Give yourself admin permission
3. Use `/strengthsmp` command to verify
4. Check console for initialization messages

## Troubleshooting

### Bot won't connect
- Verify bot token in `config.yml`
- Check bot has required Discord permissions

### Commands not working
- Verify command entry in `plugin.yml`
- Check player permissions
- Review command class implementation

### Data not persisting
- Ensure `plugins/StrengthSMP/` directory exists
- Check file permissions
- Verify YAML syntax in config.yml

## Support & Documentation

For Paper API documentation: https://papermc.io/javadocs
For JDA Discord bot: https://github.com/DV8FromTheWorld/JDA

---

This plugin provides a solid foundation for a professional Strength SMP system. All core infrastructure is in place; now focus on implementing the game mechanics!
