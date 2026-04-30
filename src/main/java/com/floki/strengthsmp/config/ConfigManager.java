package com.floki.strengthsmp.config;

import com.floki.strengthsmp.StrengthSMP;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

/**
 * Manages all configuration files for StrengthSMP.
 * Handles loading, saving, and defaults for config.yml, messages.yml, weapons.yml, and events.yml.
 */
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
        setup();
    }

    public void setup() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        config = loadFile("config.yml");
        messages = loadFile("messages.yml");
        weapons = loadFile("weapons.yml");
        events = loadFile("events.yml");
        discord = loadFile("discord.yml");
        
        migrateDiscordConfig();
    }

    private FileConfiguration loadFile(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }

        FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        // Load defaults from jar
        InputStream defConfigStream = plugin.getResource(fileName);
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            configuration.setDefaults(defConfig);
        }
        
        return configuration;
    }

    public void reloadAll() {
        config = loadFile("config.yml");
        messages = loadFile("messages.yml");
        weapons = loadFile("weapons.yml");
        events = loadFile("events.yml");
        discord = loadFile("discord.yml");
    }

    public FileConfiguration getConfig() { return config; }
    public FileConfiguration getMessages() { return messages; }
    public FileConfiguration getWeapons() { return weapons; }
    public FileConfiguration getEvents() { return events; }
    public FileConfiguration getDiscord() { return discord; }

    public void saveConfig() { saveFile(config, "config.yml"); }
    public void saveMessages() { saveFile(messages, "messages.yml"); }
    public void saveWeapons() { saveFile(weapons, "weapons.yml"); }
    public void saveEvents() { saveFile(events, "events.yml"); }
    public void saveDiscord() { saveFile(discord, "discord.yml"); }

    private void migrateDiscordConfig() {
        if (config.contains("discord.bot-token") && 
            (discord.getString("bot-token") == null || discord.getString("bot-token").equals("YOUR_TOKEN_HERE") || discord.getString("bot-token").isEmpty())) {
            
            plugin.getLogger().warning("Legacy Discord config detected in config.yml. Migrating to discord.yml...");
            
            discord.set("enabled", config.get("discord.enabled"));
            discord.set("bot-token", config.get("discord.bot-token"));
            discord.set("bot-name", config.get("discord.bot-name"));
            discord.set("channels.dashboard", config.get("discord.channels.dashboard"));
            discord.set("channels.leaderboard", config.get("discord.channels.leaderboard"));
            discord.set("channels.welcome", config.get("discord.channels.welcome"));
            discord.set("channels.audit", config.get("discord.channels.audit"));
            discord.set("update-interval", config.get("discord.update-interval"));
            
            saveDiscord();
            
            // Note: We don't remove from config.yml yet to be safe, 
            // but we'll stop reading from it.
            plugin.getLogger().info("✓ Discord config migrated successfully!");
        }
    }

    private void saveFile(FileConfiguration configuration, String fileName) {
        try {
            configuration.save(new File(plugin.getDataFolder(), fileName));
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save " + fileName, e);
        }
    }
}
