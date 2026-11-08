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
import com.floki.strengthsmp.commands.NoTimerCommand;
import com.floki.strengthsmp.commands.PunishCommand;
import com.floki.strengthsmp.commands.RemoveProtectionCommand;
import com.floki.strengthsmp.commands.StrengthCommand;
import com.floki.strengthsmp.commands.WithdrawCommand;
import com.floki.strengthsmp.config.Config;
import com.floki.strengthsmp.config.ConfigManager;
import com.floki.strengthsmp.data.DataManager;
import com.floki.strengthsmp.discord.DiscordManager;
import com.floki.strengthsmp.api.HollowHook;
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
import com.floki.strengthsmp.services.TrackingService;
import com.floki.strengthsmp.util.ItemFactory;
import com.floki.strengthsmp.util.MessageUtil;
import com.floki.strengthsmp.util.ResourcePackServer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class StrengthSMP
extends JavaPlugin {
    private ResourcePackListener resourcePackListener;
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
    private TrackingService trackingService;
    private ResourcePackServer resourcePackServer;
    /** The Hollow (StrengthSMP Pro only); null in the free edition. */
    private HollowHook hollowHook;

    public static final String PRO_LINK = "https://builtbybit.com/resources/strength-smp.105677/";
    public static final String PRO_PITCH_SHORT = "<#9b59b6><b>The Hollow</b></#9b59b6> <gray>is a</gray> <#f1c40f><b>StrengthSMP Pro</b></#f1c40f> <gray>feature. Get it: </gray><white>" + PRO_LINK + "</white>";

    /** True in StrengthSMP Pro: the Hollow's classes are only present in the Pro jar. */
    public static boolean isPro() {
        try {
            Class.forName("com.floki.strengthsmp.hollow.HollowService");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public void onEnable() {
        instance = this;
        long startTime = System.currentTimeMillis();
        this.getLogger().info("");
        this.getLogger().info("  \u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557");
        this.getLogger().info("  \u2551                                                   \u2551");
        this.getLogger().info("  \u2551   \u2694  S T R E N G T H  S M P                      \u2551");
        this.getLogger().info("  \u2551   \u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500                    \u2551");
        this.getLogger().info("  \u2551" + this.padRight("   v" + this.getDescription().getVersion() + "  \u2022  by Floki", 51) + "\u2551");
        this.getLogger().info("  \u2551                                                   \u2551");
        this.getLogger().info("  \u255a\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255d");
        this.getLogger().info("");
        this.configManager = new ConfigManager(this);
        this.config = new Config(this);
        MessageUtil.init(this);
        this.upgradeResourcePack();
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
        this.trackingService = new TrackingService(this);
        if (isPro()) {
            try {
                this.hollowHook = (HollowHook) Class.forName("com.floki.strengthsmp.hollow.HollowService")
                        .getConstructor(StrengthSMP.class).newInstance(this);
            } catch (ReflectiveOperationException e) {
                this.getLogger().severe("Could not start the Hollow: " + e);
            }
        }
        this.getLogger().info("✓ System Services Initialized (Monarch, Strength, Totem, Aesthetics, Rift, Tracking)");
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderExpansionImpl(this).register();
            this.getLogger().info("\u2713 PlaceholderAPI Hook Registered");
        }
        new com.floki.strengthsmp.metrics.Metrics(this, 23512);
        this.getLogger().info("\u2713 bStats Metrics Service Initialized");
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
        Bukkit.getScheduler().runTaskTimer((Plugin)this, () -> {
            if (this.dataManager != null) {
                this.dataManager.saveAllAsync();
            }
        }, 6000L, 6000L);
        this.resourcePackServer = new ResourcePackServer(this);
        this.resourcePackServer.start();
        if (this.hollowHook == null) {
            this.announcePro();
        }
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
            try {
                this.discordManager.disconnect();
            }
            catch (Exception e) {
                this.getLogger().warning("Discord shutdown message failed: " + e.getMessage());
            }
        }
        if (this.monarchService != null) {
            this.monarchService.cleanup();
        }
        if (this.trackingService != null) {
            this.trackingService.cleanup();
        }
        if (this.contractService != null) {
            this.contractService.cleanup();
        }
        if (this.hollowHook != null) {
            this.hollowHook.shutdown();
        }
        if (this.dataManager != null) {
            this.dataManager.shutdown();
        }
        if (this.resourcePackServer != null) {
            this.resourcePackServer.stop();
        }
        this.getLogger().info("\ud83d\udd34 Strength SMP disabled. All data saved.");
    }

    /** The textures pack shipped with this build (hosted on mc-packs.net, which names files by SHA-1). */
    public static final String PACK_HASH = "d3f983a4ceb7d2ae93ae470c87aca643569c9f33";
    public static final String PACK_URL = "https://download.mc-packs.net/pack/" + PACK_HASH + ".zip";
    private static final String OLD_PACK_HASH = "c70f17fdb4ee37502588659322d9d96e9cb92ce6";

    /** Servers still on the previous default pack get the new textures automatically; custom packs are left alone. */
    private void upgradeResourcePack() {
        org.bukkit.configuration.file.FileConfiguration c = this.config.getConfig();
        if (OLD_PACK_HASH.equalsIgnoreCase(c.getString("resource-pack.hash", ""))) {
            c.set("resource-pack.url", PACK_URL);
            c.set("resource-pack.hash", PACK_HASH);
            this.config.saveConfig();
            this.getLogger().info("✓ Resource pack updated to the new textures (" + PACK_HASH.substring(0, 8) + ").");
        }
        java.io.File local = new java.io.File(this.getDataFolder(), "strengthsmp.zip");
        if (local.exists() && OLD_PACK_HASH.equalsIgnoreCase(sha1(local))) {
            this.saveResource("strengthsmp.zip", true);
        }
    }

    private static String sha1(java.io.File f) {
        try (java.io.InputStream in = new java.io.FileInputStream(f)) {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-1");
            byte[] buf = new byte[8192];
            for (int n; (n = in.read(buf)) > 0; ) {
                md.update(buf, 0, n);
            }
            StringBuilder sb = new StringBuilder();
            for (byte b : md.digest()) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /** Free edition: tell the owner what StrengthSMP Pro adds. */
    private void announcePro() {
        String[] lines = {
            "",
            "  ╭─────────────────────────────────────────────────────────╮",
            "  │  ✦  STRENGTHSMP PRO  is available                      │",
            "  │                                                         │",
            "  │  THE HOLLOW: players who die at minimum strength fall   │",
            "  │  into a self-building void world and must fight back:   │",
            "  │   • The Ascent: a 150-jump parkour spiral to a crown    │",
            "  │   • Soul Harvest: wave arena with a Pyre Tyrant boss    │",
            "  │   • The Echo Maze: dark, shifting, guarded shrines      │",
            "  │   • The Warden's Throne: three hand-crafted bosses      │",
            "  │   • The Shade: their killer hunts them, wearing their   │",
            "  │     real skin. It only moves when they look away.       │",
            "  │  Forge the Hollow Key, wake the Last Door, walk free.   │",
            "  │                                                         │",
            "  │  + SQLite/MySQL storage, everything configurable        │",
            "  │  " + padRight("Get it: " + PRO_LINK.replace("https://", ""), 55) + "│",
            "  ╰─────────────────────────────────────────────────────────╯",
            ""};
        for (String line : lines) {
            this.getLogger().info(line);
        }
    }

    /** Pads (or truncates) a banner line so the box border stays aligned at any version length. */
    private String padRight(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        StringBuilder sb = new StringBuilder(text);
        while (sb.length() < width) {
            sb.append(' ');
        }
        return sb.toString();
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
        this.resourcePackListener = new ResourcePackListener(this);
        Bukkit.getPluginManager().registerEvents((Listener)this.resourcePackListener, (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)new MobListener(this), (Plugin)this);
        Bukkit.getPluginManager().registerEvents((Listener)new BindListener(this), (Plugin)this);
        if (this.hollowHook != null) {
            this.hollowHook.registerListeners();
        }
        this.registerRerollRecipe();
        this.registerStrengthRecipe();
    }

    private void registerStrengthRecipe() {
        LinkedHashMap<Character, Material> defaults = new LinkedHashMap<Character, Material>();
        defaults.put(Character.valueOf('D'), Material.DIAMOND_BLOCK);
        defaults.put(Character.valueOf('P'), Material.NETHERITE_PICKAXE);
        defaults.put(Character.valueOf('E'), Material.ENDER_EYE);
        defaults.put(Character.valueOf('S'), Material.NETHERITE_SCRAP);
        defaults.put(Character.valueOf('N'), Material.NETHER_STAR);
        this.registerConfiguredRecipe("recipes.strength-item", new NamespacedKey((Plugin)this, "strength_item"), ItemFactory.createStrengthItem(1), new String[]{"DPD", "ESE", "DND"}, defaults, "Strength Item");
    }

    private void registerRerollRecipe() {
        LinkedHashMap<Character, Material> defaults = new LinkedHashMap<Character, Material>();
        defaults.put(Character.valueOf('D'), Material.DIAMOND);
        defaults.put(Character.valueOf('H'), Material.TRIPWIRE_HOOK);
        defaults.put(Character.valueOf('S'), Material.PRISMARINE_SHARD);
        defaults.put(Character.valueOf('W'), Material.WITHER_SKELETON_SKULL);
        defaults.put(Character.valueOf('N'), Material.NETHER_STAR);
        this.registerConfiguredRecipe("recipes.reroll-weapon", new NamespacedKey((Plugin)this, "reroll_weapon"), ItemFactory.createRerollItem((Plugin)this, 1), new String[]{"DHD", "SWS", "DND"}, defaults, "Reroll Weapon");
    }

    /**
     * Registers a shaped recipe, preferring the shape/ingredients defined under {@code path}
     * in config.yml. If that section is missing, malformed, or names an unknown material,
     * the built-in recipe is used instead and the reason is logged.
     */
    private void registerConfiguredRecipe(String path, NamespacedKey key, ItemStack result, String[] defaultShape, Map<Character, Material> defaultIngredients, String label) {
        Bukkit.removeRecipe((NamespacedKey)key);
        String[] shape = defaultShape;
        Map<Character, Material> ingredients = defaultIngredients;
        boolean fromConfig = false;
        ConfigurationSection section = this.config.getConfig().getConfigurationSection(path);
        if (section != null) {
            List<String> rows = section.getStringList("shape");
            ConfigurationSection ingredientSection = section.getConfigurationSection("ingredients");
            if (rows.isEmpty() || ingredientSection == null) {
                this.getLogger().warning("\u26a0\ufe0f [Recipes] " + path + " is incomplete (needs both 'shape' and 'ingredients'). Using the built-in " + label + " recipe.");
            } else {
                Map<Character, Material> parsed = this.parseRecipe(path, rows, ingredientSection, label);
                if (parsed != null) {
                    shape = rows.toArray(new String[0]);
                    ingredients = parsed;
                    fromConfig = true;
                }
            }
        }
        try {
            ShapedRecipe recipe = new ShapedRecipe(key, result);
            recipe.shape(shape);
            for (Map.Entry<Character, Material> entry : ingredients.entrySet()) {
                if (!this.shapeUses(shape, ((Character)entry.getKey()).charValue())) continue;
                recipe.setIngredient(((Character)entry.getKey()).charValue(), (Material)entry.getValue());
            }
            Bukkit.addRecipe((Recipe)recipe);
            this.getLogger().info("\u2705 " + label + " recipe registered successfully" + (fromConfig ? " (from config.yml)." : " (built-in)."));
        }
        catch (Exception e) {
            this.getLogger().warning("\u26a0\ufe0f [Recipes] Failed to register the " + label + " recipe: " + e.getMessage());
        }
    }

    /** Validates a configured shape/ingredient pair. Returns null (and logs why) when unusable. */
    private Map<Character, Material> parseRecipe(String path, List<String> rows, ConfigurationSection ingredientSection, String label) {
        if (rows.size() > 3) {
            this.getLogger().warning("\u26a0\ufe0f [Recipes] " + path + ": 'shape' must have 1-3 rows. Using the built-in " + label + " recipe.");
            return null;
        }
        int width = ((String)rows.get(0)).length();
        if (width < 1 || width > 3) {
            this.getLogger().warning("\u26a0\ufe0f [Recipes] " + path + ": each 'shape' row must be 1-3 characters. Using the built-in " + label + " recipe.");
            return null;
        }
        for (String row : rows) {
            if (row.length() == width) continue;
            this.getLogger().warning("\u26a0\ufe0f [Recipes] " + path + ": all 'shape' rows must be the same length. Using the built-in " + label + " recipe.");
            return null;
        }
        LinkedHashMap<Character, Material> parsed = new LinkedHashMap<Character, Material>();
        for (String symbol : ingredientSection.getKeys(false)) {
            if (symbol.length() != 1) {
                this.getLogger().warning("\u26a0\ufe0f [Recipes] " + path + ": ingredient key '" + symbol + "' must be a single character. Using the built-in " + label + " recipe.");
                return null;
            }
            String materialName = ingredientSection.getString(symbol, "");
            Material material = Material.matchMaterial((String)materialName);
            if (material == null) {
                this.getLogger().warning("\u26a0\ufe0f [Recipes] " + path + ": unknown material '" + materialName + "' for ingredient '" + symbol + "'. Using the built-in " + label + " recipe.");
                return null;
            }
            parsed.put(Character.valueOf(symbol.charAt(0)), material);
        }
        for (String row : rows) {
            for (char c : row.toCharArray()) {
                if (c == ' ' || parsed.containsKey(Character.valueOf(c))) continue;
                this.getLogger().warning("\u26a0\ufe0f [Recipes] " + path + ": shape uses '" + c + "' but no ingredient defines it. Using the built-in " + label + " recipe.");
                return null;
            }
        }
        return parsed;
    }

    private boolean shapeUses(String[] shape, char symbol) {
        for (String row : shape) {
            if (row.indexOf(symbol) < 0) continue;
            return true;
        }
        return false;
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
        new NoTimerCommand(this);
        new RemoveProtectionCommand(this);
        new BindCommand(this);
        if (this.getCommand("hollow") != null) {
            this.getCommand("hollow").setExecutor((sender, command, label, args) -> {
                if (this.hollowHook == null) {
                    sender.sendMessage(com.floki.strengthsmp.util.MessageUtil.parse(PRO_PITCH_SHORT));
                } else if (sender instanceof Player) {
                    this.hollowHook.sendJournal((Player) sender);
                }
                return true;
            });
        }
    }

    public void updateDisplay(Player player) {
        if (this.playerListener != null) {
            this.playerListener.updatePlayerDisplay(player);
        }
    }

    public ResourcePackListener getResourcePackListener() {
        return this.resourcePackListener;
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

    public TrackingService getTrackingService() {
        return this.trackingService;
    }

    public HollowHook getHollowHook() {
        return this.hollowHook;
    }
}

