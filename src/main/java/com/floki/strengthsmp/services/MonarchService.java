/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 *  org.bukkit.scheduler.BukkitTask
 *  org.bukkit.scoreboard.Scoreboard
 *  org.bukkit.scoreboard.Team
 *  org.bukkit.scoreboard.Team$Option
 *  org.bukkit.scoreboard.Team$OptionStatus
 */
package com.floki.strengthsmp.services;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.DataManager;
import com.floki.strengthsmp.util.MessageUtil;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class MonarchService {
    private final StrengthSMP plugin;
    private final DataManager dataManager;
    private UUID currentMonarchUUID;
    private BukkitTask effectTask;

    public MonarchService(StrengthSMP plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
        this.currentMonarchUUID = this.dataManager.getMonarch();
        this.startDailyCrownTask();
        this.startEffectTask();
    }

    private void startDailyCrownTask() {
        Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> this.checkDailyCrown(), 0L, 1200L);
    }

    private void checkDailyCrown() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Karachi"));
        long today = Long.parseLong(now.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        long lastUpdate = this.dataManager.getLastMonarchUpdate();
        if (now.getHour() >= 5 && lastUpdate < today) {
            this.calculateNewMonarch();
            this.dataManager.setLastMonarchUpdate(today);
            this.dataManager.saveAll();
        }
    }

    public void calculateNewMonarch() {
        if (!this.plugin.getConfigManager().isMonarchEnabled()) {
            return;
        }
        UUID oldMonarch = this.currentMonarchUUID;
        this.dataManager.calculateMonarch();
        UUID newMonarch = this.dataManager.getMonarch();
        if (newMonarch == null) {
            if (oldMonarch != null) {
                this.clearMonarch();
            }
            return;
        }
        if (oldMonarch == null || !oldMonarch.equals(newMonarch)) {
            this.clearMonarch();
            this.announceNewMonarch(newMonarch);
            this.currentMonarchUUID = newMonarch;
            Player newPlayer = Bukkit.getPlayer((UUID)newMonarch);
            if (newPlayer != null) {
                this.applyMonarchEffects(newPlayer);
                this.plugin.updateDisplay(newPlayer);
            }
            if (this.plugin.getDiscordManager() != null) {
                this.plugin.getDiscordManager().updateDashboard();
            }
        }
    }

    private void announceNewMonarch(UUID uuid) {
        Player player;
        if (this.plugin.getConfigManager().isMonarchAnnouncements()) {
            String name = Bukkit.getOfflinePlayer((UUID)uuid).getName();
            Bukkit.broadcastMessage((String)MessageUtil.parse("<#f1c40f><b>[MONARCH]</b></#f1c40f> <white>" + (name != null ? name : "Unknown") + "</white> <gray>has claimed the Crown!</gray>"));
        }
        if ((player = Bukkit.getPlayer((UUID)uuid)) != null) {
            this.applyMonarchEffects(player);
        }
    }

    private void startEffectTask() {
        this.effectTask = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            if (this.currentMonarchUUID == null) {
                return;
            }
            Player player = Bukkit.getPlayer((UUID)this.currentMonarchUUID);
            if (player != null) {
                this.applyMonarchEffects(player);
            }
        }, 100L, 100L);
    }

    public void applyMonarchEffects(Player player) {
        if (player == null) {
            return;
        }
        if (this.plugin.getConfigManager().isMonarchGlowing()) {
            this.setMonarchGlow(player, true);
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false, true));
    }

    public void clearMonarch() {
        if (this.currentMonarchUUID == null) {
            return;
        }
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = sb.getTeam("MonarchGlow");
        if (team != null) {
            for (String entry : new ArrayList<String>(team.getEntries())) {
                team.removeEntry(entry);
            }
        }
        Player player = Bukkit.getPlayer((UUID)this.currentMonarchUUID);
        this.currentMonarchUUID = null;
        if (player != null) {
            player.setGlowing(false);
            player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
            this.plugin.updateDisplay(player);
        }
    }

    private void setMonarchGlow(Player player, boolean enabled) {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = sb.getTeam("MonarchGlow");
        if (team == null) {
            team = sb.registerNewTeam("MonarchGlow");
            team.setColor(ChatColor.RED);
            team.setPrefix(String.valueOf(ChatColor.DARK_RED) + String.valueOf(ChatColor.BOLD) + "[MONARCH] " + String.valueOf(ChatColor.RED));
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        }
        if (enabled) {
            for (String entry : new ArrayList<String>(team.getEntries())) {
                if (entry.equals(player.getName())) continue;
                team.removeEntry(entry);
            }
            team.addEntry(player.getName());
            player.setGlowing(true);
        } else {
            team.removeEntry(player.getName());
            player.setGlowing(false);
        }
    }

    public UUID getCurrentMonarchUUID() {
        return this.currentMonarchUUID;
    }

    public void cleanup() {
        if (this.effectTask != null) {
            this.effectTask.cancel();
        }
        this.clearMonarch();
    }
}

