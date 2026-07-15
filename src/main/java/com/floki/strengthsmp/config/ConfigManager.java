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
    }

    private FileConfiguration loadFile(String fileName) {
        File file = new File(this.plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            this.plugin.saveResource(fileName, false);
        }
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration((File)file);
        InputStream defConfigStream = this.plugin.getResource(fileName);
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration((Reader)new InputStreamReader(defConfigStream));
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

    private void migrateResourcePack() {
        String currentMode = this.config.getString("resource-pack.hosting.mode", "INTERNAL");
        String currentUrl = this.config.getString("resource-pack.url", "");
        String correctUrl = "https://download.mc-packs.net/pack/373f6b038da9d35301d65f930be4d1e09701f8e2.zip";
        String correctHash = "373f6b038da9d35301d65f930be4d1e09701f8e2";
        if (currentMode.equalsIgnoreCase("INTERNAL") || currentUrl.contains("github") || currentUrl.contains("127.0.0.1") || currentUrl.contains("localhost") || currentUrl.isEmpty()) {
            this.plugin.getLogger().warning("\u26a0\ufe0f Resource pack config is outdated. Auto-migrating to EXTERNAL CDN hosting...");
            this.config.set("resource-pack.hosting.mode", (Object)"EXTERNAL");
            this.config.set("resource-pack.hosting.public-ip", (Object)"AUTO");
            this.config.set("resource-pack.url", (Object)correctUrl);
            this.config.set("resource-pack.hash", (Object)correctHash);
            this.saveConfig();
            this.plugin.getLogger().info("\u2705 Resource pack config migrated to EXTERNAL CDN (mc-packs.net)!");
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

