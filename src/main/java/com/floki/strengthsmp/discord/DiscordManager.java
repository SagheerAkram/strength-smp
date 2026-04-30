package com.floki.strengthsmp.discord;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.config.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.awt.*;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Manages Discord bot integration for Strength SMP
 */
public class DiscordManager extends ListenerAdapter {

    private final StrengthSMP plugin;
    private final String botToken;
    private JDA jda;

    private String dashboardMessageId;
    private String leaderboardMessageId;
    private String monarchMessageId;

    public DiscordManager(StrengthSMP plugin, String botToken) {
        this.plugin = plugin;
        this.botToken = botToken;
    }

    public void connect() {
        try {
            jda = JDABuilder.createDefault(botToken)
                    .addEventListeners(this)
                    .build();
            // Removed awaitReady() to prevent main thread hanging.
            // JDA will fire status changes as it connects.

            // Load existing message IDs from DataManager
            this.dashboardMessageId = plugin.getDataManager().getDashboardMessageId();
            this.leaderboardMessageId = plugin.getDataManager().getLeaderboardMessageId();
            this.monarchMessageId = plugin.getDataManager().getMonarchMessageId();

            plugin.getLogger().info("✓ Discord bot connected successfully!");

            // Send initial dashboard and leaderboard (on main thread to ensure data safety)
            Bukkit.getScheduler().runTask(plugin, () -> {
                sendDashboard();
                sendLeaderboard();
                updateMonarchBoard();
            });
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to connect Discord bot: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (jda != null) {
            jda.shutdownNow(); // Use shutdownNow for immediate termination on disable
            plugin.getLogger().info("Discord bot disconnected");
        }
    }

    public void sendDashboard() {
        if (jda == null)
            return;
        TextChannel channel = jda.getTextChannelById(plugin.getConfigManager().getDashboardChannelId());
        if (channel == null)
            return;

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🗡️ FLOKI STRENGTH SMP - SEASON 4 🗡️")
                .setDescription("Welcome to the most intense season of **Strength SMP**!\n\n" +
                        "**CORE MECHANICS:**\n" +
                        "• Kill players to gain **+1 Strength**\n" +
                        "• Die to lose **1 Strength**\n" +
                        "• Max: 5 STR | Min: 0 STR\n" +
                        "• Use `/withdraw` to bank strength\n\n" +
                        "**SYSTEM STATUS:** ✅ ONLINE")
                .setColor(Color.RED)
                .setThumbnail(
                        "https://cdn.discordapp.com/attachments/1497462636873253006/1497462636873253006/strength_icon.png")
                .setFooter("Season 4 - The Final Evolution", null)
                .setTimestamp(Instant.now());

        if (dashboardMessageId != null) {
            // Try to edit existing message
            channel.editMessageEmbedsById(dashboardMessageId, embed.build()).queue(
                    success -> plugin.getLogger().info("Dashboard updated in Discord."),
                    error -> {
                        plugin.getLogger().warning("Failed to update dashboard, sending new: " + error.getMessage());
                        // Send new message
                        channel.sendMessageEmbeds(embed.build()).queue(message -> {
                            this.dashboardMessageId = message.getId();
                            plugin.getDataManager().setDashboardMessageId(message.getId());
                            // Removed saveAll() from async callback to prevent thread safety issues.
                            // Data will be saved on normal save cycles or shutdown.
                            plugin.getLogger().info("New dashboard sent to Discord.");
                        });
                    });
        } else {
            // Send new message
            channel.sendMessageEmbeds(embed.build()).queue(message -> {
                this.dashboardMessageId = message.getId();
                plugin.getDataManager().setDashboardMessageId(message.getId());
                plugin.getLogger().info("Dashboard initialized in Discord.");
            });
        }
    }

    public void sendLeaderboard() {
        if (jda == null)
            return;
        TextChannel channel = jda.getTextChannelById(plugin.getConfigManager().getLeaderboardChannelId());
        if (channel == null)
            return;

        if (leaderboardMessageId != null) {
            // Try to edit existing message
            channel.editMessageEmbedsById(leaderboardMessageId, createLeaderboardEmbed()).queue(
                    success -> plugin.getLogger().info("Leaderboard updated in Discord."),
                    error -> {
                        plugin.getLogger().warning("Failed to update leaderboard, sending new: " + error.getMessage());
                        // Send new message
                        channel.sendMessageEmbeds(createLeaderboardEmbed()).queue(message -> {
                            this.leaderboardMessageId = message.getId();
                            plugin.getDataManager().setLeaderboardMessageId(message.getId());
                            plugin.getDataManager().saveAll();
                            plugin.getLogger().info("New leaderboard sent to Discord.");
                        });
                    });
        } else {
            // Send new message
            channel.sendMessageEmbeds(createLeaderboardEmbed()).queue(message -> {
                this.leaderboardMessageId = message.getId();
                plugin.getDataManager().setLeaderboardMessageId(message.getId());
                plugin.getLogger().info("Leaderboard initialized in Discord.");
            });
        }
    }

    // Monarch board: shows current monarch and next update time (uses dashboard
    // channel)
    public void sendMonarchBoard() {
        if (jda == null)
            return;
        String channelId = plugin.getConfigManager().getMonarchChannelId();
        if (channelId == null || channelId.isEmpty()) {
            channelId = plugin.getConfigManager().getDashboardChannelId();
        }
        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel == null)
            return;

        channel.sendMessageEmbeds(createMonarchEmbed()).queue(message -> {
            this.monarchMessageId = message.getId();
            plugin.getDataManager().setMonarchMessageId(message.getId());
            plugin.getDataManager().saveAll();
            plugin.getLogger().info("Monarch board initialized in Discord.");
        });
    }

    public void updateMonarchBoard() {
        if (jda == null)
            return;
        if (monarchMessageId != null) {
            String channelId = plugin.getConfigManager().getMonarchChannelId();
            if (channelId == null || channelId.isEmpty()) {
                channelId = plugin.getConfigManager().getDashboardChannelId();
            }
            TextChannel channel = jda.getTextChannelById(channelId);
            if (channel != null) {
                channel.editMessageEmbedsById(monarchMessageId, createMonarchEmbed()).queue(
                        null,
                        error -> plugin.getLogger().warning("Failed to update monarch board: " + error.getMessage()));
            }
        }
    }

    private MessageEmbed createMonarchEmbed() {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("👑 MONARCH BOARD 👑")
                .setColor(new Color(255, 215, 0))
                .setTimestamp(Instant.now());

        UUID monarchUUID = plugin.getDataManager().getMonarch();
        String monarchName = "None";
        if (monarchUUID != null) {
            monarchName = Bukkit.getOfflinePlayer(monarchUUID).getName();
            if (monarchName == null)
                monarchName = "Unknown";
        }

        // Calculate next update time (next 5:00 AM)
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneId.systemDefault());
        java.time.ZonedDateTime next = now.withHour(5).withMinute(0).withSecond(0).withNano(0);
        if (!now.isBefore(next)) {
            next = next.plusDays(1);
        }
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm z");
        String nextUpdate = next.format(formatter);

        embed.addField("Current Monarch", monarchName, false);
        embed.addField("Next Update (5:00 AM)", nextUpdate, false);
        embed.setFooter("Monarch updates daily at 5:00 AM", null);
        return embed.build();
    }

    public void updateDashboard() {
        if (jda == null)
            return;

        // Update Leaderboard
        if (leaderboardMessageId != null) {
            TextChannel lbChannel = jda.getTextChannelById(plugin.getConfigManager().getLeaderboardChannelId());
            if (lbChannel != null) {
                lbChannel.editMessageEmbedsById(leaderboardMessageId, createLeaderboardEmbed()).queue(
                        null,
                        error -> plugin.getLogger().warning("Failed to update leaderboard: " + error.getMessage()));
            }
        }
    }

    private MessageEmbed createLeaderboardEmbed() {
        Config config = plugin.getConfigManager();
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🏆 Strength SMP Leaderboard")
                .setDescription("Live competitive rankings updated automatically.")
                .setColor(parseColor(config.getLbColor()))
                .setThumbnail(
                        "https://cdn.discordapp.com/attachments/1497462636873253006/1497462636873253006/strength_icon.png") // Optional:
                                                                                                                            // replace
                                                                                                                            // with
                                                                                                                            // server
                                                                                                                            // icon
                                                                                                                            // if
                                                                                                                            // available
                .setTimestamp(Instant.now())
                .setFooter("Updates every " + config.getLbUpdateMins() + " minutes • Floki Strength SMP", null);

        // 1. TOP STRENGTH (Ranked Top 5)
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
                if (name == null)
                    name = "Unknown";
                strBuilder.append(rankIcons[i]).append(" **").append(name).append("** — ")
                        .append(entry.getValue()).append(" STR\n");
            }
        }
        embed.addField("⚡ Top Strength", strBuilder.toString(), false);

        // 2. TOP KILLERS (Top 3)
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
                    if (name == null)
                        name = "Unknown";
                    killBuilder.append(rankIcons[i]).append(" **").append(name).append("** — ")
                            .append(entry.getValue()).append(" Kills\n");
                }
            }
            embed.addField("⚔️ Top Killers", killBuilder.toString(), true);
        }

        // 3. TOP BOUNTIES (Top 3)
        if (config.lbShowBounties()) {
            Map<UUID, Map<UUID, Integer>> bountyCache = plugin.getDataManager().getBountyCache();
            Map<UUID, Integer> totalBounties = bountyCache.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().values().stream().mapToInt(Integer::intValue).sum()));

            List<Map.Entry<UUID, Integer>> topBounties = totalBounties.entrySet().stream()
                    .filter(e -> e.getValue() > 0)
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .limit(3)
                    .collect(Collectors.toList());

            StringBuilder bountyBuilder = new StringBuilder();
            if (topBounties.isEmpty()) {
                bountyBuilder.append("No active bounties.");
            } else {
                for (int i = 0; i < topBounties.size(); i++) {
                    Map.Entry<UUID, Integer> entry = topBounties.get(i);
                    String name = Bukkit.getOfflinePlayer(entry.getKey()).getName();
                    if (name == null)
                        name = "Unknown";
                    bountyBuilder.append(rankIcons[i]).append(" **").append(name).append("** — ")
                            .append(entry.getValue()).append(" STR\n");
                }
            }
            embed.addField("💰 Top Bounties", bountyBuilder.toString(), true);
        }

        // 4. CURRENT MONARCH
        if (config.lbShowMonarch()) {
            UUID monarchUUID = plugin.getDataManager().getMonarch();
            String monarchName = "None";
            if (monarchUUID != null) {
                monarchName = Bukkit.getOfflinePlayer(monarchUUID).getName();
                if (monarchName == null)
                    monarchName = "Unknown";
            }
            embed.addField("👑 Current Monarch", "**" + monarchName + "**", false);
        }

        // 5. SERVER STATUS & TIPS
        if (config.lbShowStatus()) {
            String[] tips = {
                    "💡 Use `/withdraw` to bank your strength!",
                    "💡 Bounties reward the killer with extra strength.",
                    "💡 Match your weapon class for more damage!",
                    "💡 Monarchs receive special bonuses!",
                    "💡 Don't forget to check your contracts with `/contracts`."
            };
            String randomTip = tips[(int) (Math.random() * tips.length)];
            embed.addField("📡 Server Status",
                    "🟢 **Online** (" + Bukkit.getOnlinePlayers().size() + " players online)\n" + randomTip, false);
        }

        return embed.build();
    }

    private Color parseColor(String colorStr) {
        Color gold = new Color(255, 215, 0);
        if (colorStr == null)
            return gold;
        switch (colorStr.toLowerCase()) {
            case "gold":
                return gold;
            case "red":
                return Color.RED;
            case "blue":
                return Color.BLUE;
            case "green":
                return Color.GREEN;
            case "purple":
                return new Color(148, 103, 189);
            case "orange":
                return Color.ORANGE;
            default:
                try {
                    return Color.decode(colorStr);
                } catch (NumberFormatException e) {
                    return gold;
                }
        }
    }

    public void logKill(Player killer, Player victim, int bounty) {
        if (jda == null)
            return;
        TextChannel channel = jda.getTextChannelById(plugin.getConfigManager().getAuditChannelId());
        if (channel == null)
            return;

        EmbedBuilder embed = new EmbedBuilder();

        if (killer == null) {
            embed.setTitle("🚫 COMBAT LOG")
                    .setDescription("**" + victim.getName() + "** logged out in combat!")
                    .addField("Penalty", "-1 Strength", true)
                    .setColor(Color.RED);
        } else {
            embed.setTitle("⚔️ KILL LOG")
                    .setDescription("**" + killer.getName() + "** killed **" + victim.getName() + "**")
                    .addField("Reward", "+1 Strength", true)
                    .setColor(Color.ORANGE);

            if (bounty > 0) {
                embed.addField("Bounty Claimed", bounty + " Strength", true);
                embed.setColor(Color.YELLOW);
            }
        }

        embed.setTimestamp(Instant.now());
        channel.sendMessageEmbeds(embed.build()).queue();
    }

    public JDA getJDA() {
        return jda;
    }

    public boolean isConnected() {
        return jda != null && jda.getStatus().isInit();
    }

    // Simple Discord command handling
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || jda == null)
            return;
        String content = event.getMessage().getContentRaw().trim();
        if (!content.startsWith("!"))
            return;

        String[] args = content.substring(1).split("\\s+");
        String cmd = args[0].toLowerCase();

        switch (cmd) {
            case "strengthsmp":
            case "help":
                event.getChannel().sendMessage("**Strength SMP Bot Commands:**\n" +
                        "`!dashboard` – Refresh dashboard embed\n" +
                        "`!leaderboard` – Refresh leaderboard embed\n" +
                        "`!help` – Show this help message").queue();
                break;
            case "dashboard":
                sendDashboard();
                event.getChannel().sendMessage("Dashboard refreshed.").queue();
                break;
            case "leaderboard":
                sendLeaderboard();
                event.getChannel().sendMessage("Leaderboard refreshed.").queue();
                break;
            default:
                event.getChannel().sendMessage("Unknown command. Use `!help` for a list of commands.").queue();
                break;
        }
    }

}
