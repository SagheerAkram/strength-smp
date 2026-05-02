package com.floki.strengthsmp;

import com.floki.strengthsmp.config.Config;
import com.floki.strengthsmp.config.ConfigManager;
import com.floki.strengthsmp.data.DataManager;
import com.floki.strengthsmp.discord.DiscordManager;
import com.floki.strengthsmp.listeners.*;
import com.floki.strengthsmp.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class StrengthSMP extends JavaPlugin {
    
    private static StrengthSMP instance;
    private Config config;
    private DataManager dataManager;
    private DiscordManager discordManager;
    private PlayerListener playerListener;
    private DeathListener deathListener;
    private CombatListener combatListener;
    private TridentListener tridentListener;
    private com.floki.strengthsmp.services.StrengthService strengthService;
    private com.floki.strengthsmp.services.MonarchService monarchService;
    private com.floki.strengthsmp.services.SeasonService seasonService;
    private com.floki.strengthsmp.services.ContractService contractService;
    private com.floki.strengthsmp.config.ConfigManager configManager;
    private com.floki.strengthsmp.services.ExperimentalManager experimentalManager;
    private com.floki.strengthsmp.services.TotemService totemService;
    
    @Override
    public void onEnable() {
        instance = this;
        
        long startTime = System.currentTimeMillis();
        getLogger().info("╔════════════════════════════════════════════════════════════╗");
        getLogger().info("║       🗡️  FLOKI STRENGTH SMP - v1.0.1 INITIALIZING  🗡️      ║");
        getLogger().info("╚════════════════════════════════════════════════════════════╝");
        
        // 1. Initialize Core Configuration
        this.configManager = new ConfigManager(this);
        this.config = new Config(this); // Wraps ConfigManager
        com.floki.strengthsmp.util.MessageUtil.init(this);
        getLogger().info("✓ Premium Configuration & Messaging System Ready");
        
        // 2. Initialize Data Manager
        this.dataManager = new DataManager(this);
        getLogger().info("✓ Data manager initialized");
        
        // 3. Initialize Services
        this.strengthService = new com.floki.strengthsmp.services.StrengthService(this);
        this.monarchService = new com.floki.strengthsmp.services.MonarchService(this);
        this.seasonService = new com.floki.strengthsmp.services.SeasonService(this);
        this.contractService = new com.floki.strengthsmp.services.ContractService(this);
        this.experimentalManager = new com.floki.strengthsmp.services.ExperimentalManager(this);
        this.totemService = new com.floki.strengthsmp.services.TotemService(this);
        getLogger().info("✓ System Services Initialized (Monarch, Strength, Totem)");

        // 4. Hook PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new com.floki.strengthsmp.hooks.PlaceholderExpansionImpl(this).register();
            getLogger().info("✓ PlaceholderAPI Hook Registered");
        }

        // 5. Register Event Listeners
        registerEventListeners();
        getLogger().info("✓ Event listeners registered");
        
        // 6. Register Commands
        registerCommands();
        getLogger().info("✓ Commands registered");
        
        // 7. Initialize Discord bot
        if (config.isDiscordEnabled()) {
            this.discordManager = new DiscordManager(this, "");
            getLogger().info("✓ Discord bot connecting asynchronously...");
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                discordManager.connect();
            });
            long interval = 20L * 60L * config.getLbUpdateMins();
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
                if (discordManager.isConnected()) {
                    discordManager.updateDashboard();
                }
            }, 20 * 60, interval);
        }
        
        long loadTime = System.currentTimeMillis() - startTime;
        getLogger().info("╔════════════════════════════════════════════════════════════╗");
        getLogger().info("║          ✅ FLOKI SMP STRENGTH SYSTEM READY TO GO ✅         ║");
        getLogger().info("║                 Load time: " + loadTime + "ms                        ║");
        getLogger().info("╚════════════════════════════════════════════════════════════╝");
    }
    
    @Override
    public void onDisable() {
        if (discordManager != null) {
            discordManager.disconnect();
        }
        if (monarchService != null) {
            monarchService.cleanup();
        }
        if (contractService != null) {
            contractService.cleanup();
        }
        if (dataManager != null) {
            dataManager.saveAll();
        }
        getLogger().info("🔴 Floki Strength SMP disabled. All data saved.");
    }
    
    private void registerEventListeners() {
        this.playerListener = new PlayerListener(this);
        
        // PvP and Death System
        this.deathListener = new DeathListener(this);
        Bukkit.getPluginManager().registerEvents(deathListener, this);
        Bukkit.getPluginManager().registerEvents(new DamageListener(this), this);
        this.combatListener = new CombatListener(this);
        Bukkit.getPluginManager().registerEvents(combatListener, this);
        
        // Weapon Systems
        Bukkit.getPluginManager().registerEvents(new SwordListener(this), this);
        Bukkit.getPluginManager().registerEvents(new AxeListener(this), this);
        this.tridentListener = new TridentListener(this);
        Bukkit.getPluginManager().registerEvents(tridentListener, this);
        Bukkit.getPluginManager().registerEvents(new ShieldListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BowListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CrossbowListener(this), this);
        
        // Item and Interaction
        Bukkit.getPluginManager().registerEvents(new ItemInteractionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PotionBanListener(this), this);
        
        // Player
        Bukkit.getPluginManager().registerEvents(playerListener, this);
        
        // Totem Limit
        Bukkit.getPluginManager().registerEvents(new TotemLimitListener(this, totemService), this);

        // Combat Command Block
        Bukkit.getPluginManager().registerEvents(new CombatCommandListener(this), this);
        
        // 5. Register Recipes
        registerRerollRecipe();
        registerStrengthRecipe();
    }
    
    private void registerStrengthRecipe() {
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(this, "strength_item");
        Bukkit.removeRecipe(key);
        
        org.bukkit.inventory.ItemStack item = com.floki.strengthsmp.util.ItemFactory.createStrengthItem(1);
        org.bukkit.inventory.ShapedRecipe recipe = new org.bukkit.inventory.ShapedRecipe(key, item);
        
        // Recipe based on user screenshot:
        // D P D
        // E S E
        // D N D
        recipe.shape("DPD", "ESE", "DND");
        
        recipe.setIngredient('D', org.bukkit.Material.DIAMOND_BLOCK);
        recipe.setIngredient('P', org.bukkit.Material.NETHERITE_PICKAXE);
        recipe.setIngredient('E', org.bukkit.Material.ENDER_EYE);
        recipe.setIngredient('S', org.bukkit.Material.NETHERITE_SCRAP);
        recipe.setIngredient('N', org.bukkit.Material.NETHER_STAR);
        
        Bukkit.addRecipe(recipe);
        getLogger().info("✅ Strength Item recipe registered successfully.");
    }
    
    private void registerRerollRecipe() {
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(this, "reroll_weapon");
        
        // Remove existing recipe if it exists (for reloads/re-registrations)
        Bukkit.removeRecipe(key);
        
        org.bukkit.inventory.ItemStack item = com.floki.strengthsmp.util.ItemFactory.createRerollItem(this, 1);
        org.bukkit.inventory.ShapedRecipe recipe = new org.bukkit.inventory.ShapedRecipe(key, item);
        
        // Final Recipe Shape:
        // D H D
        // S W S
        // D N D
        recipe.shape("DHD", "SWS", "DND");
        
        recipe.setIngredient('D', org.bukkit.Material.DIAMOND);
        recipe.setIngredient('H', org.bukkit.Material.TRIPWIRE_HOOK);
        recipe.setIngredient('S', org.bukkit.Material.PRISMARINE_SHARD);
        recipe.setIngredient('W', org.bukkit.Material.WITHER_SKELETON_SKULL);
        recipe.setIngredient('N', org.bukkit.Material.NETHER_STAR);
        
        // CRITICAL FIX: Ensure the recipe is actually added to the server
        Bukkit.addRecipe(recipe);
        getLogger().info("✅ Reroll Weapon recipe registered successfully.");
    }
    
    private void registerCommands() {
        new StrengthCommand(this);
        new WithdrawCommand(this);
        new BountyCommand(this);
        new BountyRefundCommand(this);
        new AbilityCommand(this);
        new AdminCommand(this);
        new AnnounceCommand(this);
        new PunishCommand(this);
        new KillResetCommand(this);
        new NoNewbieCommand(this);
        new RemoveProtectionCommand(this);
    }
    
    public void updateDisplay(org.bukkit.entity.Player player) {
        if (playerListener != null) {
            playerListener.updatePlayerDisplay(player);
        }
    }
    
    public static StrengthSMP getInstance() {
        return instance;
    }
    
    public Config getConfigManager() {
        return config;
    }

    public com.floki.strengthsmp.config.ConfigManager getInternalConfigManager() {
        return configManager;
    }
    
    public DataManager getDataManager() {
        return dataManager;
    }
    
    public DiscordManager getDiscordManager() {
        return discordManager;
    }

    public TridentListener getTridentListener() {
        return tridentListener;
    }

    public com.floki.strengthsmp.services.ExperimentalManager getExperimentalManager() {
        return experimentalManager;
    }

    public com.floki.strengthsmp.services.StrengthService getStrengthService() {
        return strengthService;
    }

    public com.floki.strengthsmp.services.MonarchService getMonarchService() {
        return monarchService;
    }

    public com.floki.strengthsmp.services.SeasonService getSeasonService() {
        return seasonService;
    }

    public com.floki.strengthsmp.services.ContractService getContractService() {
        return contractService;
    }

    public CombatListener getCombatListener() {
        return combatListener;
    }

    public DeathListener getDeathListener() {
        return deathListener;
    }

    public com.floki.strengthsmp.services.TotemService getTotemService() {
        return totemService;
    }
}
