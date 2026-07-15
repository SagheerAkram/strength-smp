/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 */
package com.floki.strengthsmp.discord;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.config.Config;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class DiscordManager {
    private final StrengthSMP plugin;
    private final HttpClient httpClient;
    private final LinkedList<String> killFeed = new LinkedList();
    private final AtomicBoolean isUpdating = new AtomicBoolean(false);
    private long lastUpdate = 0L;

    public DiscordManager(StrengthSMP plugin, String unused) {
        this.plugin = plugin;
        this.httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    }

    public void connect() {
        this.plugin.getLogger().info("\u2713 Discord Webhook system ready (Ultra-Lightweight)");
        if (this.plugin.getConfigManager().isDiscordHeartbeatEnabled()) {
            this.sendWebhook(this.plugin.getConfigManager().getAnnouncementWebhook(), this.createEmbed("\ud83d\udee1\ufe0f Strength SMP", "System initialized and monitoring combat.", 3066993));
        }
        this.updateDashboard();
    }

    public void disconnect() {
        if (this.plugin.getConfigManager().isDiscordHeartbeatEnabled()) {
            this.sendWebhook(this.plugin.getConfigManager().getAnnouncementWebhook(), this.createEmbed("\ud83d\udd34 System Offline", "Strength SMP system has been disabled.", 15158332));
        }
    }

    public void updateDashboard() {
        if (!this.plugin.getConfigManager().isLbEnabled()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - this.lastUpdate < 30000L && this.lastUpdate != 0L) {
            return;
        }
        if (!this.isUpdating.compareAndSet(false, true)) {
            return;
        }
        String webhookUrl = this.plugin.getConfigManager().getLeaderboardWebhook();
        if (webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.equals("YOUR_WEBHOOK_URL_HERE")) {
            this.isUpdating.set(false);
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try {
                this.lastUpdate = System.currentTimeMillis();
                String messageId = this.plugin.getDataManager().getLeaderboardMessageId();
                JsonObject embed = this.createLeaderboardEmbed();
                JsonObject payload = new JsonObject();
                JsonArray embeds = new JsonArray();
                embeds.add((JsonElement)embed);
                payload.add("embeds", (JsonElement)embeds);
                Object finalUrl = webhookUrl;
                String method = "POST";
                if (messageId != null && !messageId.isEmpty()) {
                    finalUrl = webhookUrl + "/messages/" + messageId;
                    method = "PATCH";
                } else {
                    finalUrl = webhookUrl + "?wait=true";
                }
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create((String)finalUrl)).header("Content-Type", "application/json").method(method, HttpRequest.BodyPublishers.ofString(payload.toString())).build();
                HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 404 && method.equals("PATCH")) {
                    this.plugin.getDataManager().setLeaderboardMessageId(null);
                    this.isUpdating.set(false);
                    this.updateDashboard();
                    return;
                }
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    JsonObject responseJson;
                    if (method.equals("POST") && (responseJson = JsonParser.parseString((String)response.body()).getAsJsonObject()).has("id")) {
                        String newId = responseJson.get("id").getAsString();
                        this.plugin.getDataManager().setLeaderboardMessageId(newId);
                    }
                } else {
                    this.plugin.getLogger().warning("Discord Webhook error: " + response.statusCode() + " - " + response.body());
                }
            }
            catch (Exception e) {
                this.plugin.getLogger().warning("Failed to update Discord leaderboard: " + e.getMessage());
            }
            finally {
                this.isUpdating.set(false);
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void logKill(Player killer, Player victim, int bounty) {
        String killerName = killer != null ? killer.getName() : "Unknown";
        String victimName = victim.getName();
        String entry = killer == null ? "\ud83d\udd34 **" + victimName + "** logged out in combat!" : "\u2694\ufe0f **" + killerName + "** killed **" + victimName + "** " + (String)(bounty > 0 ? "*(+" + bounty + " STR)*" : "");
        LinkedList<String> linkedList = this.killFeed;
        synchronized (linkedList) {
            this.killFeed.addFirst(entry);
            if (this.killFeed.size() > 5) {
                this.killFeed.removeLast();
            }
        }
        this.updateDashboard();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private JsonObject createLeaderboardEmbed() {
        Serializable strBuilder;
        List topStrength;
        Config config = this.plugin.getConfigManager();
        JsonObject embed = new JsonObject();
        embed.addProperty("title", "\ud83c\udfc6 Strength SMP Dashboard");
        embed.addProperty("description", "Live competitive status and recent activity.");
        int color = 15844367;
        try {
            String hex = config.getLbColor().replace("#", "");
            color = Integer.parseInt(hex, 16);
        }
        catch (NumberFormatException hex) {
            // empty catch block
        }
        embed.addProperty("color", (Number)color);
        JsonArray fields = new JsonArray();
        Map<UUID, Integer> strengthStats = this.plugin.getDataManager().getStrengthCache();
        int limit = config.getLbLimit();
        if (limit <= 0) {
            limit = 20;
        }
        if ((topStrength = strengthStats.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(limit).collect(Collectors.toList())).isEmpty()) {
            fields.add((JsonElement)this.createField("\u26a1 Top Players", "No rankings available yet.", false));
        } else {
            int chunkSize = 20;
            for (int chunkStart = 0; chunkStart < topStrength.size(); chunkStart += chunkSize) {
                int chunkEnd = Math.min(chunkStart + chunkSize, topStrength.size());
                strBuilder = new StringBuilder();
                for (int i = chunkStart; i < chunkEnd; ++i) {
                    int rank;
                    Map.Entry entry = (Map.Entry)topStrength.get(i);
                    String name = Bukkit.getOfflinePlayer((UUID)((UUID)entry.getKey())).getName();
                    if (name == null) {
                        name = "Unknown";
                    }
                    String rankIcon = (rank = i + 1) == 1 ? "\ud83e\udd47" : (rank == 2 ? "\ud83e\udd48" : (rank == 3 ? "\ud83e\udd49" : (rank <= 10 ? "\ud83c\udfc5" : "\ud83d\udd39")));
                    ((StringBuilder)strBuilder).append(rankIcon).append(" **").append(name).append("** \u2014 ").append(entry.getValue()).append(" STR\n");
                }
                String fieldName = "\u26a1 Top Players #" + (chunkStart + 1) + "-" + chunkEnd;
                fields.add((JsonElement)this.createField(fieldName, ((StringBuilder)strBuilder).toString(), false));
            }
        }
        if (config.lbShowMonarch()) {
            UUID monarchUUID = this.plugin.getDataManager().getMonarch();
            if (monarchUUID != null) {
                String monarchName = Bukkit.getOfflinePlayer((UUID)monarchUUID).getName();
                if (monarchName == null) {
                    monarchName = "Unknown";
                }
                int strength = this.plugin.getDataManager().getStrength(monarchUUID);
                fields.add((JsonElement)this.createField("\ud83d\udc51 Current Monarch", "**" + monarchName + "** (" + strength + " STR)\n*The throne is claimed by the strongest.*", true));
            } else {
                fields.add((JsonElement)this.createField("\ud83d\udc51 Current Monarch", "*Throne is currently vacant.*", true));
            }
        }
        int totalStrength = strengthStats.values().stream().mapToInt(Integer::intValue).sum();
        int activePlayers = (int)strengthStats.values().stream().filter(s -> s > 0).count();
        fields.add((JsonElement)this.createField("\ud83d\udcca Global Stats", "**Total Strength:** " + totalStrength + " STR\n**Active Warriors:** " + activePlayers, true));
        StringBuilder feedBuilder = new StringBuilder();
        strBuilder = this.killFeed;
        synchronized (strBuilder) {
            if (this.killFeed.isEmpty()) {
                feedBuilder.append("*No recent combat activity.*");
            } else {
                for (String string : this.killFeed) {
                    feedBuilder.append(string).append("\n");
                }
            }
        }
        fields.add((JsonElement)this.createField("\ud83d\udcdc Recent Activity", feedBuilder.toString(), false));
        if (config.lbShowStatus()) {
            fields.add((JsonElement)this.createField("\ud83d\udce1 Server Status", "\ud83d\udfe2 **Online** (" + Bukkit.getOnlinePlayers().size() + " players)", true));
        }
        embed.add("fields", (JsonElement)fields);
        JsonObject footer = new JsonObject();
        footer.addProperty("text", "Auto-updating \u2022 Strength SMP");
        embed.add("footer", (JsonElement)footer);
        embed.addProperty("timestamp", Instant.now().toString());
        return embed;
    }

    private JsonObject createEmbed(String title, String desc, int color) {
        JsonObject embed = new JsonObject();
        embed.addProperty("title", title);
        embed.addProperty("description", desc);
        embed.addProperty("color", (Number)color);
        return embed;
    }

    private JsonObject createField(String name, String value, boolean inline) {
        JsonObject field = new JsonObject();
        field.addProperty("name", name);
        field.addProperty("value", value);
        field.addProperty("inline", Boolean.valueOf(inline));
        return field;
    }

    private void sendWebhook(String url, JsonObject embed) {
        if (url == null || url.isEmpty() || url.equals("YOUR_WEBHOOK_URL_HERE")) {
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try {
                JsonObject payload = new JsonObject();
                JsonArray embeds = new JsonArray();
                embeds.add((JsonElement)embed);
                payload.add("embeds", (JsonElement)embeds);
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(payload.toString())).build();
                this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            }
            catch (Exception e) {
                this.plugin.getLogger().warning("Failed to send Discord webhook: " + e.getMessage());
            }
        });
    }

    public boolean isConnected() {
        return true;
    }
}

