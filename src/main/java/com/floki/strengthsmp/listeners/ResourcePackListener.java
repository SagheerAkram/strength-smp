package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ResourcePackListener implements Listener {

    private final StrengthSMP plugin;

    public ResourcePackListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.getConfigManager().isResourcePackEnabled()) return;

        Player player = event.getPlayer();
        String url = plugin.getResourcePackServer().getPackUrl();
        String rawHash = plugin.getResourcePackServer().getPackHash();
        if (rawHash == null || rawHash.isEmpty()) {
            rawHash = plugin.getConfigManager().getResourcePackHash();
        }
        final String hash = rawHash;
        String prompt = plugin.getConfigManager().getResourcePackPrompt();
        boolean force = plugin.getConfigManager().isResourcePackRequired();

        if (url == null || url.isEmpty() || url.contains("your-hosting.com")) {
            plugin.getLogger().warning("⚠️ Resource pack URL is invalid or placeholder: " + url);
            return;
        }

        plugin.getLogger().info("📦 Attempting to send resource pack to " + player.getName());
        plugin.getLogger().info("🔗 URL: " + url);
        plugin.getLogger().info("🔑 Hash: " + hash);

        // Delay slightly to ensure player is fully loaded (Increased to 2 seconds for reliability)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;
                
                try {
                    // Modern Spigot/Paper API (1.17+)
                    player.setResourcePack(
                        url, 
                        MessageUtil.hexToBytes(hash), 
                        MessageUtil.color(prompt), 
                        force
                    );
                } catch (NoSuchMethodError e) {
                    // Fallback for older versions (pre-1.17)
                    player.setResourcePack(url);
                }
            }
        }.runTaskLater(plugin, 40L); // 2 second delay
    }

    @EventHandler
    public void onStatus(PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();
        PlayerResourcePackStatusEvent.Status status = event.getStatus();
        
        switch (status) {
            case SUCCESSFULLY_LOADED:
                plugin.getLogger().info("✅ Resource pack successfully loaded for " + player.getName());
                break;
            case DECLINED:
                plugin.getLogger().warning("❌ " + player.getName() + " declined the resource pack.");
                break;
            case FAILED_DOWNLOAD:
                plugin.getLogger().severe("❌ Resource pack download FAILED for " + player.getName());
                break;
            case ACCEPTED:
                plugin.getLogger().info("📥 " + player.getName() + " accepted the resource pack. Downloading...");
                break;
        }
    }
}
