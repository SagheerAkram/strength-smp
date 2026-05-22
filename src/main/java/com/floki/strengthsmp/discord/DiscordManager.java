package com.floki.strengthsmp.discord;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.config.Config;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
 * Now supports message editing to prevent spam.
 */
public class DiscordManager {

    private final StrengthSMP plugin;
    private final HttpClient httpClient;
    private final LinkedList<String> killFeed = new LinkedList<>();

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

    private final java.util.concurrent.atomic.AtomicBoolean isUpdating = new java.util.concurrent.atomic.AtomicBoolean(false);
    private long lastUpdate = 0;

    public void updateDashboard() {
        if (!plugin.getConfigManager().isLbEnabled()) return;
        
        // Throttling: Max once every 30 seconds to prevent rate limits and spam
        long now = System.currentTimeMillis();
        if (now - lastUpdate < 30000 && lastUpdate != 0) return;
        
        // Lock: Prevent concurrent updates
        if (!isUpdating.compareAndSet(false, true)) return;

        String webhookUrl = plugin.getConfigManager().getLeaderboardWebhook();
        if (webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.equals("YOUR_WEBHOOK_URL_HERE")) {
            isUpdating.set(false);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                lastUpdate = System.currentTimeMillis();
                String messageId = plugin.getDataManager().getLeaderboardMessageId();
                JsonObject embed = createLeaderboardEmbed();
                
                JsonObject payload = new JsonObject();
                JsonArray embeds = new JsonArray();
                embeds.add(embed);
                payload.add("embeds", embeds);

                String finalUrl = webhookUrl;
                String method = "POST";

                if (messageId != null && !messageId.isEmpty()) {
                    // Attempt to edit existing message
                    finalUrl = webhookUrl + "/messages/" + messageId;
                    method = "PATCH";
                } else {
                    // New message, ask for ID in response
                    finalUrl = webhookUrl + "?wait=true";
                }

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(finalUrl))
                        .header("Content-Type", "application/json")
                        .method(method, HttpRequest.BodyPublishers.ofString(payload.toString()))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 404 && method.equals("PATCH")) {
                    // Message was deleted, clear ID and retry once
                    plugin.getDataManager().setLeaderboardMessageId(null);
                    isUpdating.set(false); // Release lock before retry
                    updateDashboard(); 
                    return;
                } else if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    if (method.equals("POST")) {
                        // Save the message ID from the response
                        JsonObject responseJson = JsonParser.parseString(response.body()).getAsJsonObject();
                        if (responseJson.has("id")) {
                            String newId = responseJson.get("id").getAsString();
                            plugin.getDataManager().setLeaderboardMessageId(newId);
                        }
                    }
                } else {
                    plugin.getLogger().warning("Discord Webhook error: " + response.statusCode() + " - " + response.body());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to update Discord leaderboard: " + e.getMessage());
            } finally {
                isUpdating.set(false);
            }
        });
    }

    public void logKill(Player killer, Player victim, int bounty) {
        String killerName = (killer != null) ? killer.getName() : "Unknown";
        String victimName = victim.getName();
        
        String entry;
        if (killer == null) {
            entry = "🔴 **" + victimName + "** logged out in combat!";
        } else {
            entry = "⚔️ **" + killerName + "** killed **" + victimName + "** " + (bounty > 0 ? "*(+" + bounty + " STR)*" : "");
        }
        
        synchronized (killFeed) {
            killFeed.addFirst(entry);
            if (killFeed.size() > 5) killFeed.removeLast();
        }
        
        // Trigger dashboard update immediately to show the new kill
        updateDashboard();
    }

    private JsonObject createLeaderboardEmbed() {
        Config config = plugin.getConfigManager();
        JsonObject embed = new JsonObject();
        embed.addProperty("title", "🏆 Strength SMP Dashboard");
        embed.addProperty("description", "Live competitive status and recent activity.");
        int color = 0xf1c40f; // Default gold
        try {
            String hex = config.getLbColor().replace("#", "");
            color = Integer.parseInt(hex, 16);
        } catch (NumberFormatException ignored) {}
        
        embed.addProperty("color", color);
        
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
        fields.add(createField("⚡ Top Players", strBuilder.toString(), false));

        // 1.5 MONARCH SPOTLIGHT
        if (config.lbShowMonarch()) {
            UUID monarchUUID = plugin.getDataManager().getMonarch();
            if (monarchUUID != null) {
                String monarchName = Bukkit.getOfflinePlayer(monarchUUID).getName();
                if (monarchName == null) monarchName = "Unknown";
                
                int strength = plugin.getDataManager().getStrength(monarchUUID);
                fields.add(createField("👑 Current Monarch", "**" + monarchName + "** (" + strength + " STR)\n*The throne is claimed by the strongest.*", true));
            } else {
                fields.add(createField("👑 Current Monarch", "*Throne is currently vacant.*", true));
            }
        }

        // 2. GLOBAL STATS
        int totalStrength = strengthStats.values().stream().mapToInt(Integer::intValue).sum();
        int activePlayers = (int) strengthStats.values().stream().filter(s -> s > 0).count();
        fields.add(createField("📊 Global Stats", 
                "**Total Strength:** " + totalStrength + " STR\n" +
                "**Active Warriors:** " + activePlayers, true));

        // 3. LIVE KILL FEED
        StringBuilder feedBuilder = new StringBuilder();
        synchronized (killFeed) {
            if (killFeed.isEmpty()) {
                feedBuilder.append("*No recent combat activity.*");
            } else {
                for (String entry : killFeed) {
                    feedBuilder.append(entry).append("\n");
                }
            }
        }
        fields.add(createField("📜 Recent Activity", feedBuilder.toString(), false));

        // 4. SERVER STATUS
        if (config.lbShowStatus()) {
            fields.add(createField("📡 Server Status", "🟢 **Online** (" + Bukkit.getOnlinePlayers().size() + " players)", true));
        }

        embed.add("fields", fields);
        JsonObject footer = new JsonObject();
        footer.addProperty("text", "Auto-updating • Floki Strength SMP");
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
