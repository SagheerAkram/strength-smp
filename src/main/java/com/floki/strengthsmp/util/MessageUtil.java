/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.md_5.bungee.api.ChatColor
 *  org.bukkit.ChatColor
 *  org.bukkit.command.CommandSender
 */
package com.floki.strengthsmp.util;

import com.floki.strengthsmp.StrengthSMP;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtil {
    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private static final Pattern HEX_CLOSE_PATTERN = Pattern.compile("</#[A-Fa-f0-9]{6}>");
    private static StrengthSMP plugin;
    private static boolean supportsHex;

    public static void init(StrengthSMP instance) {
        plugin = instance;
        try {
            net.md_5.bungee.api.ChatColor.of("#FFFFFF");
            supportsHex = true;
        } catch (Throwable e) {
            supportsHex = false;
        }
    }

    public static String parse(String message) {
        return MessageUtil.color(message);
    }

    public static String color(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        String msg = message;
        if (supportsHex) {
            Matcher matcher = HEX_PATTERN.matcher(msg);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(sb, net.md_5.bungee.api.ChatColor.of("#" + matcher.group(1)).toString());
            }
            matcher.appendTail(sb);
            msg = sb.toString();
            Matcher closeMatcher = HEX_CLOSE_PATTERN.matcher(msg);
            msg = closeMatcher.replaceAll("§r");
        }
        msg = msg.replace("<white>", "§f").replace("<gray>", "§7").replace("<red>", "§c")
                 .replace("<green>", "§a").replace("<yellow>", "§e").replace("<blue>", "§9")
                 .replace("<gold>", "§6").replace("<aqua>", "§b").replace("<bold>", "§l")
                 .replace("<b>", "§l").replace("<italic>", "§o").replace("<i>", "§o").replace("<reset>", "§r");
        msg = msg.replace("</white>", "§r").replace("</gray>", "§r").replace("</red>", "§r")
                 .replace("</green>", "§r").replace("</yellow>", "§r").replace("</blue>", "§r")
                 .replace("</gold>", "§r").replace("</aqua>", "§r").replace("</bold>", "§r")
                 .replace("</b>", "§r").replace("</italic>", "§r").replace("</i>", "§r");
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    private static String getActiveLanguage() {
        if (plugin != null && plugin.getConfigManager() != null && plugin.getConfigManager().getConfig() != null) {
            return plugin.getConfigManager().getConfig().getString("language", "en").toLowerCase();
        }
        return "en";
    }

    private static String getPrefix() {
        if (plugin == null || plugin.getConfigManager() == null || plugin.getConfigManager().getMessages() == null) {
            return "";
        }
        String lang = getActiveLanguage();
        String prefix = plugin.getConfigManager().getMessages().getString(lang + ".prefix");
        if (prefix == null && !lang.equals("en")) {
            prefix = plugin.getConfigManager().getMessages().getString("en.prefix");
        }
        if (prefix == null) {
            prefix = plugin.getConfigManager().getMessages().getString("prefix", "");
        }
        return prefix != null ? prefix : "";
    }

    private static String getRawString(String key, String fallback) {
        if (plugin == null || plugin.getConfigManager() == null || plugin.getConfigManager().getMessages() == null) {
            return fallback;
        }
        String lang = getActiveLanguage();
        String msg = plugin.getConfigManager().getMessages().getString(lang + "." + key);
        if (msg == null && !lang.equals("en")) {
            msg = plugin.getConfigManager().getMessages().getString("en." + key);
        }
        if (msg == null) {
            msg = plugin.getConfigManager().getMessages().getString(key, fallback);
        }
        return msg != null ? msg : fallback;
    }

    public static String get(String key) {
        return get(key, "");
    }

    public static String get(String key, String fallback, String... placeholders) {
        String msg = getRawString(key, fallback);
        if (msg == null) {
            msg = fallback != null ? fallback : "";
        }
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 >= placeholders.length) continue;
            msg = msg.replace("%" + placeholders[i] + "%", placeholders[i + 1]);
        }
        return color(msg);
    }

    public static List<String> getList(String key, List<String> fallback, String... placeholders) {
        String lang = getActiveLanguage();
        List<String> rawList = plugin != null && plugin.getConfigManager() != null && plugin.getConfigManager().getMessages() != null
                ? plugin.getConfigManager().getMessages().getStringList(lang + "." + key)
                : null;
        if ((rawList == null || rawList.isEmpty()) && !lang.equals("en") && plugin != null && plugin.getConfigManager() != null && plugin.getConfigManager().getMessages() != null) {
            rawList = plugin.getConfigManager().getMessages().getStringList("en." + key);
        }
        if ((rawList == null || rawList.isEmpty()) && plugin != null && plugin.getConfigManager() != null && plugin.getConfigManager().getMessages() != null) {
            rawList = plugin.getConfigManager().getMessages().getStringList(key);
        }
        if (rawList == null || rawList.isEmpty()) {
            rawList = fallback != null ? fallback : new ArrayList<>();
        }
        List<String> formatted = new ArrayList<>();
        for (String line : rawList) {
            String temp = line;
            for (int i = 0; i < placeholders.length; i += 2) {
                if (i + 1 >= placeholders.length) continue;
                temp = temp.replace("%" + placeholders[i] + "%", placeholders[i + 1]);
            }
            formatted.add(color(temp));
        }
        return formatted;
    }

    public static void send(CommandSender sender, String key, String... placeholders) {
        sendWithDefault(sender, key, null, placeholders);
    }

    public static void sendWithDefault(CommandSender sender, String key, String fallback, String... placeholders) {
        if (sender == null) return;
        String message = getRawString(key, fallback);
        if (message == null || message.isEmpty()) {
            return;
        }
        String prefix = getPrefix();
        message = prefix + message;
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 >= placeholders.length) continue;
            message = message.replace("%" + placeholders[i] + "%", placeholders[i + 1]);
        }
        sender.sendMessage(color(message));
    }

    public static void sendActionBar(Player player, String key, String fallback, String... placeholders) {
        if (player == null) return;
        String message = get(key, fallback, placeholders);
        if (message == null || message.isEmpty()) return;
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    public static void broadcast(String key, String... placeholders) {
        broadcastWithDefault(key, null, placeholders);
    }

    public static void broadcastWithDefault(String key, String fallback, String... placeholders) {
        String message = getRawString(key, fallback);
        if (message == null || message.isEmpty()) {
            return;
        }
        String prefix = getPrefix();
        message = prefix + message;
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 >= placeholders.length) continue;
            message = message.replace("%" + placeholders[i] + "%", placeholders[i + 1]);
        }
        plugin.getServer().broadcastMessage(color(message));
    }

    public static byte[] hexToBytes(String hex) {
        if (hex == null || hex.isEmpty()) {
            return null;
        }
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte)((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}

