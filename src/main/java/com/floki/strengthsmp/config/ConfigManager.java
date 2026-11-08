/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.configuration.Configuration
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 */
package com.floki.strengthsmp.config;

import com.floki.strengthsmp.StrengthSMP;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {
    private final StrengthSMP plugin;
    private FileConfiguration config;
    private File configFile;
    private FileConfiguration messages;
    private File messagesFile;
    private FileConfiguration weapons;
    private File weaponsFile;
    private FileConfiguration events;
    private File eventsFile;
    private FileConfiguration discord;
    private File discordFile;

    public ConfigManager(StrengthSMP plugin) {
        this.plugin = plugin;
        this.setup();
    }

    public void setup() {
        if (!this.plugin.getDataFolder().exists()) {
            this.plugin.getDataFolder().mkdir();
        }
        this.config = this.loadFile("config.yml");
        this.messages = this.loadFile("messages.yml");
        this.weapons = this.loadFile("weapons.yml");
        this.events = this.loadFile("events.yml");
        this.discord = this.loadFile("discord.yml");
        this.migrateDiscordConfig();
        this.migrateResourcePack();
        this.migrateRecipes();
    }

    private FileConfiguration loadFile(String fileName) {
        File file = new File(this.plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            try {
                this.plugin.saveResource(fileName, false);
            } catch (Exception ignored) {}
        }
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration((File)file);
        InputStream defConfigStream = this.plugin.getResource(fileName);
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration((Reader)new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));
            configuration.setDefaults((Configuration)defConfig);
        }
        return configuration;
    }

    public void reloadAll() {
        this.config = this.loadFile("config.yml");
        this.messages = this.loadFile("messages.yml");
        this.weapons = this.loadFile("weapons.yml");
        this.events = this.loadFile("events.yml");
        this.discord = this.loadFile("discord.yml");
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public FileConfiguration getMessages() {
        return this.messages;
    }

    public FileConfiguration getWeapons() {
        return this.weapons;
    }

    public FileConfiguration getEvents() {
        return this.events;
    }

    public FileConfiguration getDiscord() {
        return this.discord;
    }

    public void saveConfig() {
        this.saveFile(this.config, "config.yml");
    }

    public void saveMessages() {
        this.saveFile(this.messages, "messages.yml");
    }

    public void saveWeapons() {
        this.saveFile(this.weapons, "weapons.yml");
    }

    public void saveEvents() {
        this.saveFile(this.events, "events.yml");
    }

    public void saveDiscord() {
        this.saveFile(this.discord, "discord.yml");
    }

    private void migrateDiscordConfig() {
        if (this.config.contains("discord.bot-token") && (this.discord.getString("bot-token") == null || this.discord.getString("bot-token").equals("YOUR_TOKEN_HERE") || this.discord.getString("bot-token").isEmpty())) {
            this.plugin.getLogger().warning("Legacy Discord config detected in config.yml. Migrating to discord.yml...");
            this.discord.set("enabled", this.config.get("discord.enabled"));
            this.discord.set("bot-token", this.config.get("discord.bot-token"));
            this.discord.set("bot-name", this.config.get("discord.bot-name"));
            this.discord.set("channels.dashboard", this.config.get("discord.channels.dashboard"));
            this.discord.set("channels.leaderboard", this.config.get("discord.channels.leaderboard"));
            this.discord.set("channels.welcome", this.config.get("discord.channels.welcome"));
            this.discord.set("channels.audit", this.config.get("discord.channels.audit"));
            this.discord.set("update-interval", this.config.get("discord.update-interval"));
            this.saveDiscord();
            this.plugin.getLogger().info("\u2713 Discord config migrated successfully!");
        }
    }

    /**
     * The recipes: section in config.yml used to be decorative — the recipes were
     * hardcoded in Java and never matched it. Now that the section is authoritative, any
     * config still carrying those stale, never-used defaults would silently change the
     * recipe on update, so rewrite it to the recipe that was actually in force.
     * Configs that were genuinely customised hold different values and are left untouched.
     */
    private void migrateRecipes() {
        boolean changed = false;
        if ("DIAMOND_PICKAXE".equalsIgnoreCase(this.config.getString("recipes.strength-item.ingredients.P", "")) && "NETHERITE_INGOT".equalsIgnoreCase(this.config.getString("recipes.strength-item.ingredients.S", ""))) {
            this.config.set("recipes.strength-item.ingredients.P", (Object)"NETHERITE_PICKAXE");
            this.config.set("recipes.strength-item.ingredients.S", (Object)"NETHERITE_SCRAP");
            changed = true;
        }
        if ("PRISMARINE_CRYSTALS".equalsIgnoreCase(this.config.getString("recipes.reroll-weapon.ingredients.S", ""))) {
            this.config.set("recipes.reroll-weapon.ingredients.S", (Object)"PRISMARINE_SHARD");
            changed = true;
        }
        if (changed) {
            this.saveConfig();
            this.plugin.getLogger().info("✅ Recipe config aligned with the recipes that were already in force (crafting is unchanged).");
        }
    }

    private void migrateResourcePack() {
        String currentUrl = this.config.getString("resource-pack.url", "");
        String currentHash = this.config.getString("resource-pack.hash", "");
        String oldBrokenHash = "373f6b038da9d35301d65f930be4d1e09701f8e2";
        if (currentUrl.contains(oldBrokenHash) || currentHash.equalsIgnoreCase(oldBrokenHash)) {
            this.plugin.getLogger().warning("⚠️ Detected outdated resource pack link. Switching to INTERNAL hosting mode...");
            this.config.set("resource-pack.hosting.mode", (Object)"INTERNAL");
            this.config.set("resource-pack.hosting.public-ip", (Object)"AUTO");
            this.config.set("resource-pack.url", (Object)"");
            this.config.set("resource-pack.hash", (Object)"");
            this.saveConfig();
            this.plugin.getLogger().info("✅ Resource pack hosting configured to INTERNAL mode!");
        }
    }

    private void saveFile(FileConfiguration configuration, String fileName) {
        try {
            configuration.save(new File(this.plugin.getDataFolder(), fileName));
        }
        catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not save " + fileName, e);
        }
    }
}

