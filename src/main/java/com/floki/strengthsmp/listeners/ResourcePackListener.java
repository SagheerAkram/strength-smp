/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerJoinEvent
 *  org.bukkit.event.player.PlayerResourcePackStatusEvent
 *  org.bukkit.event.player.PlayerResourcePackStatusEvent$Status
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 */
package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ResourcePackListener
implements Listener {
    private final StrengthSMP plugin;

    public ResourcePackListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        this.sendPack(event.getPlayer());
    }

    /** Sends the resource pack to one player (on join, or from /strengthsmp pack). */
    public void sendPack(final Player player) {
        if (!this.plugin.getConfigManager().isResourcePackEnabled()) {
            return;
        }
        final String url = this.plugin.getResourcePackServer().getPackUrl(player);
        String rawHash = this.plugin.getResourcePackServer().getPackHash();
        if (rawHash == null || rawHash.isEmpty()) {
            rawHash = this.plugin.getConfigManager().getResourcePackHash();
        }
        final String hash = rawHash;
        final String prompt = this.plugin.getConfigManager().getResourcePackPrompt();
        final boolean force = this.plugin.getConfigManager().isResourcePackRequired();
        if (url == null || url.isEmpty() || url.contains("your-hosting.com")) {
            this.plugin.getLogger().warning("\u26a0\ufe0f Resource pack URL is invalid or placeholder: " + url);
            return;
        }
        this.plugin.getLogger().info("\ud83d\udce6 Attempting to send resource pack to " + player.getName());
        this.plugin.getLogger().info("\ud83d\udd17 URL: " + url);
        this.plugin.getLogger().info("\ud83d\udd11 Hash: " + hash);
        new BukkitRunnable(){

            public void run() {
                if (!player.isOnline()) {
                    return;
                }
                try {
                    player.setResourcePack(url, MessageUtil.hexToBytes(hash), MessageUtil.color(prompt), force);
                }
                catch (NoSuchMethodError e) {
                    player.setResourcePack(url);
                }
            }
        }.runTaskLater((Plugin)this.plugin, 40L);
    }

    @EventHandler
    public void onStatus(PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();
        PlayerResourcePackStatusEvent.Status status = event.getStatus();
        switch (status) {
            case SUCCESSFULLY_LOADED: {
                this.plugin.getLogger().info("\u2705 Resource pack successfully loaded for " + player.getName());
                break;
            }
            case DECLINED: {
                this.plugin.getLogger().warning("\u274c " + player.getName() + " declined the resource pack.");
                break;
            }
            case FAILED_DOWNLOAD: {
                this.plugin.getLogger().severe("\u274c Resource pack download FAILED for " + player.getName());
                break;
            }
            case ACCEPTED: {
                this.plugin.getLogger().info("\ud83d\udce5 " + player.getName() + " accepted the resource pack. Downloading...");
            }
        }
    }
}

