package com.floki.strengthsmp.services;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.DataManager;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

/**
 * Manages the Monarch system, including selection, effects, and announcements.
 * The Monarch is the player with the highest strength on the server.
 */
public class MonarchService {

    private final StrengthSMP plugin;
    private final DataManager dataManager;
    private UUID currentMonarchUUID;
    private org.bukkit.scheduler.BukkitTask effectTask;

    public MonarchService(StrengthSMP plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
        this.currentMonarchUUID = dataManager.getMonarch();
        
        startDailyCrownTask();
        startEffectTask();
    }

    /**
     * Periodically checks if it's time to update the crown (5:00 AM PKT).
     */
    private void startDailyCrownTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            checkDailyCrown();
        }, 0L, 1200L); // Check every 1 minute
    }

    private void checkDailyCrown() {
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Karachi"));
        
        // Format date as YYYYMMDD to check if we already updated today
        long today = Long.parseLong(now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")));
        long lastUpdate = dataManager.getLastMonarchUpdate();

        // If it's 5 AM or later, and we haven't updated yet today
        if (now.getHour() >= 5 && lastUpdate < today) {
            calculateNewMonarch();
            dataManager.setLastMonarchUpdate(today);
            dataManager.saveAll();
        }
    }

    public void calculateNewMonarch() {
        if (!plugin.getConfigManager().isMonarchEnabled()) {
            return;
        }

        UUID oldMonarch = currentMonarchUUID;
        dataManager.calculateMonarch();
        UUID newMonarch = dataManager.getMonarch();

        if (newMonarch == null) {
            if (oldMonarch != null) clearMonarch();
            return;
        }

        // Always update Discord if either the monarch changed OR it's the first run
        if (oldMonarch == null || !oldMonarch.equals(newMonarch)) {
            clearMonarch();
            
            announceNewMonarch(newMonarch);
            currentMonarchUUID = newMonarch;
            
            Player newPlayer = Bukkit.getPlayer(newMonarch);
            if (newPlayer != null) {
                applyMonarchEffects(newPlayer);
                plugin.updateDisplay(newPlayer);
            }

            // Push to Discord immediately
            if (plugin.getDiscordManager() != null) {
                plugin.getDiscordManager().updateDashboard();
            }
        }
    }

    private void announceNewMonarch(UUID uuid) {
        if (plugin.getConfigManager().isMonarchAnnouncements()) {
            String name = Bukkit.getOfflinePlayer(uuid).getName();
            Bukkit.broadcastMessage(MessageUtil.parse("<#f1c40f><b>[MONARCH]</b></#f1c40f> <white>" + (name != null ? name : "Unknown") + "</white> <gray>has claimed the Crown!</gray>"));
        }
        
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            applyMonarchEffects(player);
        }
    }

    /**
     * Starts a repeating task to ensure the monarch always has their effects.
     */
    private void startEffectTask() {
        this.effectTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (currentMonarchUUID == null) return;
            
            Player player = Bukkit.getPlayer(currentMonarchUUID);
            if (player != null) {
                applyMonarchEffects(player);
            }
        }, 100L, 100L); // Every 5 seconds
    }

    public void applyMonarchEffects(Player player) {
        if (player == null) return;

        // Apply Red Glow via Team
        if (plugin.getConfigManager().isMonarchGlowing()) {
            setMonarchGlow(player, true);
        }
        
        // Strictly Fire Resistance and Red Glow as per new rules
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false, true));
    }

    public void clearMonarch() {
        if (currentMonarchUUID == null) return;
        
        org.bukkit.scoreboard.Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        org.bukkit.scoreboard.Team team = sb.getTeam("MonarchGlow");
        if (team != null) {
            for (String entry : new java.util.ArrayList<>(team.getEntries())) {
                team.removeEntry(entry);
            }
        }
        
        Player player = Bukkit.getPlayer(currentMonarchUUID);
        currentMonarchUUID = null;
        if (player != null) {
            player.setGlowing(false);
            player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
            plugin.updateDisplay(player);
        }
    }

    private void setMonarchGlow(Player player, boolean enabled) {
        org.bukkit.scoreboard.Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        org.bukkit.scoreboard.Team team = sb.getTeam("MonarchGlow");
        
        if (team == null) {
            team = sb.registerNewTeam("MonarchGlow");
            team.setColor(org.bukkit.ChatColor.RED);
            team.setPrefix(org.bukkit.ChatColor.DARK_RED + "" + org.bukkit.ChatColor.BOLD + "[MONARCH] " + org.bukkit.ChatColor.RED);
            team.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
        }

        if (enabled) {
            for (String entry : new java.util.ArrayList<>(team.getEntries())) {
                if (!entry.equals(player.getName())) {
                    team.removeEntry(entry);
                }
            }
            team.addEntry(player.getName());
            player.setGlowing(true);
        } else {
            team.removeEntry(player.getName());
            player.setGlowing(false);
        }
    }

    public UUID getCurrentMonarchUUID() {
        return currentMonarchUUID;
    }

    public void cleanup() {
        if (effectTask != null) {
            effectTask.cancel();
        }
        clearMonarch();
    }
}
