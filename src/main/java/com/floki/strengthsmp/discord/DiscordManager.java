package com.floki.strengthsmp.discord;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.config.Config;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Lightweight Discord integration using Webhooks.
 * Replaces the heavy JDA library to reduce plugin size.
 */
public class DiscordManager {

    private final StrengthSMP plugin;
    private final HttpClient httpClient;

    public DiscordManager(StrengthSMP plugin, String unused) {
        this.plugin = plugin;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    public void connect() {
        plugin.getLogger().info("✓ Discord Webhook system ready (Ultra-Lightweight)");
        // Send initial heartbeat/announcement
        sendWebhook(plugin.getConfigManager().getAnnouncementWebhook(), 
            createEmbed("🛡️ Strength SMP", "System initialized and monitoring combat.", 0x2ecc71));
        
        updateDashboard(); // Initial leaderboard push
    }

    public void disconnect() {
        sendWebhook(plugin.getConfigManager().getAnnouncementWebhook(), 
            createEmbed("🔴 System Offline", "Strength SMP system has been disabled.", 0xe74c3c));
    }

    public void updateDashboard() {
        if (!plugin.getConfigManager().isLbEnabled()) return;
        
        String webhook = plugin.getConfigManager().getLeaderboardWebhook();
        if (webhook == null || webhook.isEmpty()) return;

        JsonObject embed = createLeaderboardEmbed();
        sendWebhook(webhook, embed);
    }

    public void logKill(Player killer, Player victim, int bounty) {
        String webhook = plugin.getConfigManager().getAuditWebhook();
        if (webhook == null || webhook.isEmpty()) return;

        JsonObject embed = new JsonObject();
        if (killer == null) {
            embed.addProperty("title", "🚫 COMBAT LOG");
            embed.addProperty("description", "**" + victim.getName() + "** logged out in combat!");
            embed.addProperty("color", 0xe74c3c);
        } else {
            embed.addProperty("title", "⚔️ KILL LOG");
            embed.addProperty("description", "**" + killer.getName() + "** killed **" + victim.getName() + "**");
            embed.addProperty("color", 0xe67e22);
            if (bounty > 0) {
                embed.addProperty("footer", "Bounty Claimed: " + bounty + " Strength");
            }
        }
        
        sendWebhook(webhook, embed);
    }

    private JsonObject createLeaderboardEmbed() {
        Config config = plugin.getConfigManager();
        JsonObject embed = new JsonObject();
        embed.addProperty("title", "🏆 Strength SMP Leaderboard");
        embed.addProperty("description", "Live competitive rankings updated automatically.");
        embed.addProperty("color", Integer.parseInt(config.getLbColor().replace("#", ""), 16));
        
        JsonArray fields = new JsonArray();

        // 1. TOP STRENGTH
        Map<UUID, Integer> strengthStats = plugin.getDataManager().getStrengthCache();
        List<Map.Entry<UUID, Integer>> topStrength = strengthStats.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .collect(Collectors.toList());

        StringBuilder strBuilder = new StringBuilder();
        String[] rankIcons = { "🥇", "🥈", "🥉", "🏅", "🏅" };
        if (topStrength.isEmpty()) {
            strBuilder.append("No rankings available yet.");
        } else {
            for (int i = 0; i < topStrength.size(); i++) {
                Map.Entry<UUID, Integer> entry = topStrength.get(i);
                String name = Bukkit.getOfflinePlayer(entry.getKey()).getName();
                strBuilder.append(rankIcons[i]).append(" **").append(name != null ? name : "Unknown").append("** — ")
                        .append(entry.getValue()).append(" STR\n");
            }
        }
        fields.add(createField("⚡ Top Strength", strBuilder.toString(), false));

        // 2. TOP KILLERS
        if (config.lbShowKills()) {
            Map<UUID, Integer> killStats = plugin.getDataManager().getKillsCache();
            List<Map.Entry<UUID, Integer>> topKillers = killStats.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .limit(3)
                    .collect(Collectors.toList());

            StringBuilder killBuilder = new StringBuilder();
            if (topKillers.isEmpty()) {
                killBuilder.append("No kills recorded yet.");
            } else {
                for (int i = 0; i < topKillers.size(); i++) {
                    Map.Entry<UUID, Integer> entry = topKillers.get(i);
                    String name = Bukkit.getOfflinePlayer(entry.getKey()).getName();
                    killBuilder.append(rankIcons[i]).append(" **").append(name != null ? name : "Unknown").append("** — ")
                            .append(entry.getValue()).append(" Kills\n");
                }
            }
            fields.add(createField("⚔️ Top Killers", killBuilder.toString(), true));
        }

        // 3. SERVER STATUS
        if (config.lbShowStatus()) {
            fields.add(createField("📡 Status", "🟢 **Online** (" + Bukkit.getOnlinePlayers().size() + " online)", true));
        }

        embed.add("fields", fields);
        JsonObject footer = new JsonObject();
        footer.addProperty("text", "Updates periodically • Floki Strength SMP");
        embed.add("footer", footer);
        embed.addProperty("timestamp", Instant.now().toString());

        return embed;
    }

    private JsonObject createEmbed(String title, String desc, int color) {
        JsonObject embed = new JsonObject();
        embed.addProperty("title", title);
        embed.addProperty("description", desc);
        embed.addProperty("color", color);
        return embed;
    }

    private JsonObject createField(String name, String value, boolean inline) {
        JsonObject field = new JsonObject();
        field.addProperty("name", name);
        field.addProperty("value", value);
        field.addProperty("inline", inline);
        return field;
    }

    private void sendWebhook(String url, JsonObject embed) {
        if (url == null || url.isEmpty() || url.equals("YOUR_WEBHOOK_URL_HERE")) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                JsonObject payload = new JsonObject();
                JsonArray embeds = new JsonArray();
                embeds.add(embed);
                payload.add("embeds", embeds);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                        .build();

                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send Discord webhook: " + e.getMessage());
            }
        });
    }

    public boolean isConnected() {
        return true; // Webhooks are stateless
    }
}
