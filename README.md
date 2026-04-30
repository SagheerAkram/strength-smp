# 🗡️ StrengthSMP

StrengthSMP is a lightweight, competitive Minecraft survival plugin where players earn, trade, and lose strength dynamically! Features include strength-based combat scaling, a glowing bounty hunt system, custom end-game crafting, and a real-time strength tablist. Perfect for action-packed servers!

## 🎯 Core Features

### ✅ Strength System
- **Persistent strength tracking** per player UUID (-3 to +5 range)
- **Dynamic visuals** with colored name prefixes and glow effects
- **Monarch system** - Top player gets special perks
- **Real-time leaderboard** synced to Discord

### ⚔️ Weapon Classes
Six unique weapon types with passives and ultimates:
1. **Sword** - Dual-wield + Auto-crit passive
2. **Axe** - Critical hit stuns + Kinetic damage storage
3. **Trident** - Lightning strikes + Water wave mobility
4. **Shield** - Speed II on stun + Immortality ultimate
5. **Bow** - Linear sonic beams + AOE damage
6. **Crossbow** - Glowing arrows + Grapple hook

### 🛡️ Safety Mechanics
- **Safety Net (-3 Rule)**: Weakest players protected from overpowered enemies
- **Underdog Rule**: Negative strength players get +2 on high kills
- **Grace Period**: 5-second invincibility on join

### 🔗 Discord Integration
- **Live Dashboard**: Auto-updating embed with monarch and top 10
- **Leaderboard**: Top 15 with online status and stats
- **Audit Logging**: Kill chronicle and bounty tracking
- **Teammate Detection**: Flag suspicious multi-kills to prevent farming

### 🚫 Anti-Exploit Systems
- **Anti-Farm**: Max 2 kills per player per 10 minutes
- **Combat Logout**: -1 STR penalty (unless ping > 500ms)
- **Potion Ban**: All Strength potions blocked
- **Bounty Protection**: Bounties refunded on suspicious activity

## 📦 Project Structure

```
StrengthSMP PLUGIN/
├── pom.xml                        # Maven build configuration
├── src/main/
│   ├── java/com/floki/strengthsmp/
│   │   ├── StrengthSMP.java      # Main plugin entry
│   │   ├── config/Config.java    # Configuration manager
│   │   ├── data/                 # Player data and persistence
│   │   ├── discord/              # Discord bot integration
│   │   ├── listeners/            # Event handlers
│   │   ├── commands/             # Command implementations
│   │   └── weapons/              # Weapon mechanics
│   └── resources/
│       ├── plugin.yml            # Plugin manifest
│       └── config.yml            # Default configuration
└── target/                       # Build output
```

## 🚀 Quick Start

### Prerequisites
- **Java 21+**
- **Maven 3.8.0+**
- **Paper Server 1.21.x+**

### Building

```bash
# Navigate to project
cd "c:\Users\SM\Desktop\StrengthSMP PLUGIN"

# Build with Maven
mvn clean package

# Find JAR in target folder
# StrengthSMP-1.0.0.jar
```

### Installation

1. Copy `target/StrengthSMP-1.0.0.jar` to your server's `plugins/` folder
2. Restart server
3. Edit `plugins/StrengthSMP/config.yml` with your Discord bot token
4. Reload plugin: `/strengthsmp reload`

## 📝 Configuration

Edit `src/main/resources/config.yml` before building:

```yaml
strength:
  min: -3              # Minimum strength
  max: 5               # Maximum strength
  default: 0           # Starting strength
  death-penalty: 1     # Strength lost on death
  death-gain: 1        # Strength gained on kill

discord:
  enabled: true
  bot-token: "YOUR_TOKEN_HERE"
  channels:
    dashboard: "CHANNEL_ID"
    leaderboard: "CHANNEL_ID"
    audit: "CHANNEL_ID"
```

## 🎮 In-Game Commands

### Player Commands
```
/strength              # View your strength and weapon
/withdraw <amount>     # Convert strength to Nautilus Shells
/bounty <player> <amt> # Place bounty on player
/ability               # Activate weapon ultimate ability
```

### Admin Commands
```
/strengthsmp toggle                    # Enable/disable system
/strengthsmp setupboard                # Initialize Discord dashboard
/strengthsmp setupleaderboard          # Initialize Discord leaderboard
/strengthsmp resetplayer <player>      # Reset player stats
/strengthsmp reroll <player>           # Randomize player weapon
/strengthsmp setstrength <player> <#>  # Set player strength
/strengthsmp setweapon <player> <type> # Set player weapon
```

## 🎮 Game Mechanics

### Death System
- **PvP Kill**: Victim -1 STR, Attacker +1 STR
- **Natural Death**: No strength change
- **Drop Items**: 
  - Nautilus Shell (worth +1 STR)
  - Death Certificate (record of defeat)

### Bounties
- Set by any player: `/bounty <target> <amount>`
- Cost: Deducted from your strength immediately
- Reward: Claimed by whoever kills the target
- Refund: Auto-refunded if farming detected

### Safety Net (-3 Rule)
When victim is at -3 STR and attacker is +3 or higher:
- ✓ Keep armor and hotbar
- ✗ No strength transfer
- ✗ No drops
- ✓ Speed I for 30s
- ✓ Broadcast to protect weakest

### Underdog Rule
When -STR player kills +3 STR player:
- +2 STR instead of +1
- Broadcast to celebrate upset victory

## 🛠️ Implementation Status

### ✅ Completed
- [ ] Core plugin infrastructure
- [ ] Configuration system
- [ ] Data persistence (YAML)
- [ ] Command framework
- [ ] Event listener system
- [ ] Discord bot skeleton
- [ ] Weapon enumeration
- [ ] Player initialization

### 🔨 Requiring Implementation
- [ ] Death mechanics & strength transfer
- [ ] All weapon passive effects
- [ ] All weapon ultimate abilities
- [ ] Bounty system logic
- [ ] Combat logging detection
- [ ] Anti-farm tracking
- [ ] Discord dashboard updates
- [ ] Potion blocking

## 📚 Developer Guide

### Adding a New Command

```java
public class MyCommand extends BaseCommand {
    public MyCommand(StrengthSMP plugin) {
        super(plugin);
        plugin.getCommand("mycommand").setExecutor(this);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!isPlayer(sender)) return true;
        Player player = (Player) sender;
        
        // Your logic here
        sendSuccess(sender, "Command executed!");
        return true;
    }
}
```

### Adding a New Event Listener

```java
public class MyListener implements Listener {
    private final StrengthSMP plugin;
    
    public MyListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onMyEvent(MyEvent event) {
        // Your logic here
    }
}
```

Register in `StrengthSMP.registerEventListeners()`:
```java
Bukkit.getPluginManager().registerEvents(new MyListener(this), this);
```

### Accessing Player Data

```java
// Get strength
int str = plugin.getDataManager().getStrength(uuid);

// Set strength
plugin.getDataManager().setStrength(uuid, 5);

// Get weapon
WeaponType weapon = plugin.getDataManager().getWeapon(uuid);

// Track kills
plugin.getDataManager().addKill(uuid);
```

### Config Access

```java
// In your code
int max = plugin.getConfigManager().getMaxStrength();
String token = plugin.getConfigManager().getBotToken();
```

## 🐛 Troubleshooting

### Plugin won't load
```
Check console for errors during startup
Verify Java 21+ is installed
Ensure Paper 1.21.x is running
```

### Commands not working
```
Verify command entry in plugin.yml
Check player has appropriate permissions
Review command executor registration
```

### Discord bot offline
```
Verify token in config.yml
Check bot has Discord permissions
Ensure internet connection
Review console for connection errors
```

### Data not saving
```
Check plugins/StrengthSMP/ directory exists
Verify file permissions
Review console for IO errors
```

## 📞 Support

For help implementing missing features:
1. Review IMPLEMENTATION_GUIDE.md
2. Check Paper API documentation: https://papermc.io/javadocs
3. JDA Discord library: https://github.com/DV8FromTheWorld/JDA

## 📄 License

This plugin is provided as-is for the Floki SMP community.

---

**Status**: Framework complete, ready for mechanic implementation
**Build**: Maven (Java 21)
**Target**: Paper 1.21.x+
**Discord**: JDA 5.0.0-beta

🎮 **Ready to dominate the leaderboard!** ⚔️
