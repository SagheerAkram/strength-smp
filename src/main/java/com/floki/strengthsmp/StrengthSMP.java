/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
 *  org.bukkit.entity.Player
 *  org.bukkit.event.Listener
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.Recipe
 *  org.bukkit.inventory.ShapedRecipe
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.java.JavaPlugin
 */
package com.floki.strengthsmp;

import com.floki.strengthsmp.commands.AdminCommand;
import com.floki.strengthsmp.commands.AnnounceCommand;
import com.floki.strengthsmp.commands.BindCommand;
import com.floki.strengthsmp.commands.BountyCommand;
import com.floki.strengthsmp.commands.BountyRefundCommand;
import com.floki.strengthsmp.commands.KillResetCommand;
import com.floki.strengthsmp.commands.NoNewbieCommand;
import com.floki.strengthsmp.commands.PunishCommand;
import com.floki.strengthsmp.commands.RemoveProtectionCommand;
import com.floki.strengthsmp.commands.StrengthCommand;
import com.floki.strengthsmp.commands.WithdrawCommand;
import com.floki.strengthsmp.config.Config;
import com.floki.strengthsmp.config.ConfigManager;
import com.floki.strengthsmp.data.DataManager;
import com.floki.strengthsmp.discord.DiscordManager;
import com.floki.strengthsmp.hooks.PlaceholderExpansionImpl;
import com.floki.strengthsmp.listeners.AxeListener;
import com.floki.strengthsmp.listeners.BindListener;
import com.floki.strengthsmp.listeners.BowListener;
import com.floki.strengthsmp.listeners.CombatCommandListener;
import com.floki.strengthsmp.listeners.CombatListener;
import com.floki.strengthsmp.listeners.CrossbowListener;
import com.floki.strengthsmp.listeners.DamageListener;
import com.floki.strengthsmp.listeners.DeathListener;
import com.floki.strengthsmp.listeners.ItemInteractionListener;
import com.floki.strengthsmp.listeners.MobListener;
import com.floki.strengthsmp.listeners.PlayerListener;
import com.floki.strengthsmp.listeners.PotionBanListener;
import com.floki.strengthsmp.listeners.ResourcePackListener;
import com.floki.strengthsmp.listeners.ShieldListener;
import com.floki.strengthsmp.listeners.SwordListener;
import com.floki.strengthsmp.listeners.TotemLimitListener;
import com.floki.strengthsmp.listeners.TridentListener;
import com.floki.strengthsmp.services.AestheticService;
import com.floki.strengthsmp.services.ContractService;
import com.floki.strengthsmp.services.ExperimentalManager;
import com.floki.strengthsmp.services.MonarchService;
import com.floki.strengthsmp.services.RiftService;
import com.floki.strengthsmp.services.SeasonService;
import com.floki.strengthsmp.services.StrengthService;
import com.floki.strengthsmp.services.TotemService;
import com.floki.strengthsmp.util.ItemFactory;
import com.floki.strengthsmp.util.MessageUtil;
import com.floki.strengthsmp.util.ResourcePackServer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class StrengthSMP
extends JavaPlugin {
    private static StrengthSMP instance;
    private Config config;
    private DataManager dataManager;
    private DiscordManager discordManager;
    private PlayerListener playerListener;
    private DeathListener deathListener;
    private CombatListener combatListener;
    private TridentListener tridentListener;
    private StrengthService strengthService;
    private MonarchService monarchService;
    private SeasonService seasonService;
    private ContractService contractService;
    private ConfigManager configManager;
    private ExperimentalManager experimentalManager;
    private TotemService totemService;
    private AestheticService aestheticService;
    private RiftService riftService;
    private ResourcePackServer resourcePackServer;

    public void onEnable() {
        instance = this;
        long startTime = System.currentTimeMillis();
        this.getLogger().info("");
        this.getLogger().info("  \u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557");
        this.getLogger().info("  \u2551                                                   \u2551");
        this.getLogger().info("  \u2551   \u2694  S T R E N G T H  S M P                      \u2551");
        this.getLogger().info("  \u2551   \u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500                    \u2551");
        this.getLogger().info("  \u2551   v1.1.5  \u2022  by Floki                             \u2551");
        this.getLogger().info("  \u2551                                                   \u2551");
        this.getLogger().info("  \u255a\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255d");
        this.getLogger().info("");
        this.configManager = new ConfigManager(this);
        this.config = new Config(this);
        MessageUtil.init(this);
        this.getLogger().info("\u2713 Premium Configuration & Messaging System Ready");
        this.dataManager = new DataManager(this);
        this.getLogger().info("\u2713 Data manager initialized");
        this.strengthService = new StrengthService(this);
        this.monarchService = new MonarchService(this);
        this.seasonService = new SeasonService(this);
        this.contractService = new ContractService(this);
        this.experimentalManager = new ExperimentalManager(this);
        this.totemService = new TotemService(this);
        this.aestheticService = new AestheticService(this);
        this.aestheticService.startAestheticTasks();
        this.riftService = new RiftService(this);
        this.getLogger().info("\u2713 System Services Initialized (Monarch, Strength, Totem, Aesthetics, Rift)");
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderExpansionImpl(this).register();
            this.getLogger().info("\u2713 PlaceholderAPI Hook Registered");
        }
        this.registerEventListeners();
        this.getLogger().info("\u2713 Event listeners registered");
        this.registerCommands();
        this.getLogger().info("\u2713 Commands registered");
        if (this.config.isDiscordEnabled()) {
            this.discordManager = new DiscordManager(this, "");
            this.getLogger().info("\u2713 Discord bot connecting asynchronously...");
            Bukkit.getScheduler().runTaskAsynchronously((Plugin)this, () -> this.discordManager.connect());
            long interval = 1200L * (long)this.config.getLbUpdateMins();
            Bukkit.getScheduler().runTaskTimerAsynchronously((Plugin)this, () -> {
                if (this.discordManager.isConnected()) {
                    this.discordManager.updateDashboard();
                }
            }, 1200L, interval);
        }
        Bukkit.getScheduler().runTaskTimerAsynchronously((Plugin)this, () -> {
            if (this.dataManager != null) {
                this.getLogger().info("\ud83d\udcbe Auto-saving player data...");
                this.dataManager.saveAll();
            }
        }, 6000L, 6000L);
        this.resourcePackServer = new ResourcePackServer(this);
        this.resourcePackServer.start();
        long loadTime = System.currentTimeMillis() - startTime;
        this.getLogger().info("");
        this.getLogger().info("  \u250c\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2510");
        this.getLogger().info("  \u2502  \u2726  ALL SYSTEMS ONLINE                           \u2502");
        this.getLogger().info("  \u2502  \u2726  Load time: " + loadTime + "ms                                \u2502");
        this.getLogger().info("  \u2502  \u2726  Ready to dominate.                           \u2502");
        this.getLogger().info("  \u2514\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2518");
        this.getLogger().info("");
    }

    public void onDisable() {
        if (this.discordManager != null) {
            this.discordManager.disconnect();
        }
        if (this.monarchService != null) {
            this.monarchService.cleanup();
        }
        if (this.contractService != null) {
            this.contractService.cleanup();
        }
        if (this.dataManager != null) {
            this.dataManager.saveAll();
        }
        if (this.resourcePackServer != null) {
            this.resourcePackServer.stop();
        }
        this.getLogger().info("\ud83d\udd34 Strength SMP disabled. All data saved.");
    }

    private void registerEventListeners() {
        this.playerListener = new PlayerListener(this);
        this.deathListener = new DeathListener(this);
        Bukkit.getPluginManager().registerEvents((Listener)this.deathListener, (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)new DamageListener(this), (Plugin)this);
        this.combatListener = new CombatListener(this);
        Bukkit.getPluginManager().registerEvents((Listener)this.combatListener, (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)new SwordListener(this), (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)new AxeListener(this), (Plugin)this);
        this.tridentListener = new TridentListener(this);
        Bukkit.getPluginManager().registerEvents((Listener)this.tridentListener, (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)new ShieldListener(this), (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)new BowListener(this), (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)new CrossbowListener(this), (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)new ItemInteractionListener(this), (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)new PotionBanListener(this), (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)this.playerListener, (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)new TotemLimitListener(this, this.totemService), (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)new CombatCommandListener(this), (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)new ResourcePackListener(this), (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)new MobListener(this), (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)new BindListener(this), (Plugin)this);
        this.registerRerollRecipe();
        this.registerStrengthRecipe();
    }

    private void registerStrengthRecipe() {
        NamespacedKey key = new NamespacedKey((Plugin)this, "strength_item");
        Bukkit.removeRecipe((NamespacedKey)key);
        ItemStack item = ItemFactory.createStrengthItem(1);
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape(new String[]{"DPD", "ESE", "DND"});
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('P', Material.NETHERITE_PICKAXE);
        recipe.setIngredient('E', Material.ENDER_EYE);
        recipe.setIngredient('S', Material.NETHERITE_SCRAP);
        recipe.setIngredient('N', Material.NETHER_STAR);
        Bukkit.addRecipe((Recipe)recipe);
        this.getLogger().info("\u2705 Strength Item recipe registered successfully.");
    }

    private void registerRerollRecipe() {
        NamespacedKey key = new NamespacedKey((Plugin)this, "reroll_weapon");
        Bukkit.removeRecipe((NamespacedKey)key);
        ItemStack item = ItemFactory.createRerollItem((Plugin)this, 1);
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape(new String[]{"DHD", "SWS", "DND"});
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('H', Material.TRIPWIRE_HOOK);
        recipe.setIngredient('S', Material.PRISMARINE_SHARD);
        recipe.setIngredient('W', Material.WITHER_SKELETON_SKULL);
        recipe.setIngredient('N', Material.NETHER_STAR);
        Bukkit.addRecipe((Recipe)recipe);
        this.getLogger().info("\u2705 Reroll Weapon recipe registered successfully.");
    }

    private void registerCommands() {
        new StrengthCommand(this);
        new WithdrawCommand(this);
        new BountyCommand(this);
        new BountyRefundCommand(this);
        new AdminCommand(this);
        new AnnounceCommand(this);
        new PunishCommand(this);
        new KillResetCommand(this);
        new NoNewbieCommand(this);
        new RemoveProtectionCommand(this);
        new BindCommand(this);
    }

    public void updateDisplay(Player player) {
        if (this.playerListener != null) {
            this.playerListener.updatePlayerDisplay(player);
        }
    }

    public ResourcePackServer getResourcePackServer() {
        return this.resourcePackServer;
    }

    public static StrengthSMP getInstance() {
        return instance;
    }

    public Config getConfigManager() {
        return this.config;
    }

    public ConfigManager getInternalConfigManager() {
        return this.configManager;
    }

    public DataManager getDataManager() {
        return this.dataManager;
    }

    public DiscordManager getDiscordManager() {
        return this.discordManager;
    }

    public TridentListener getTridentListener() {
        return this.tridentListener;
    }

    public ExperimentalManager getExperimentalManager() {
        return this.experimentalManager;
    }

    public StrengthService getStrengthService() {
        return this.strengthService;
    }

    public MonarchService getMonarchService() {
        return this.monarchService;
    }

    public SeasonService getSeasonService() {
        return this.seasonService;
    }

    public ContractService getContractService() {
        return this.contractService;
    }

    public CombatListener getCombatListener() {
        return this.combatListener;
    }

    public DeathListener getDeathListener() {
        return this.deathListener;
    }

    public TotemService getTotemService() {
        return this.totemService;
    }

    public AestheticService getAestheticService() {
        return this.aestheticService;
    }

    public RiftService getRiftService() {
        return this.riftService;
    }
}

