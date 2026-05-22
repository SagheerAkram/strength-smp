package com.floki.strengthsmp.util;

import com.floki.strengthsmp.StrengthSMP;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Universal Utility for sending formatted messages.
 * Works on Paper, Spigot, and Bukkit from 1.16 to 1.21.
 */
public class MessageUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private static final Pattern HEX_CLOSE_PATTERN = Pattern.compile("</#[A-Fa-f0-9]{6}>");
    private static StrengthSMP plugin;
    private static boolean supportsHex;

    public static void init(StrengthSMP instance) {
        plugin = instance;
        // Check if the server version supports Hex (1.16+)
        try {
            net.md_5.bungee.api.ChatColor.of("#FFFFFF");
            supportsHex = true;
        } catch (Throwable e) {
            supportsHex = false;
        }
    }

    public static String parse(String message) {
        return color(message);
    }

    public static String color(String message) {
        if (message == null || message.isEmpty()) return "";
        
        String msg = message;

        // 1. Handle Hex <#HEX> (Only if supported)
        if (supportsHex) {
            // Opening tags
            Matcher matcher = HEX_PATTERN.matcher(msg);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(sb, net.md_5.bungee.api.ChatColor.of("#" + matcher.group(1)).toString());
            }
            matcher.appendTail(sb);
            msg = sb.toString();

            // Closing tags (treated as reset)
            Matcher closeMatcher = HEX_CLOSE_PATTERN.matcher(msg);
            msg = closeMatcher.replaceAll("§r");
        }

        // 2. Handle standard color tags
        msg = msg.replace("<white>", "§f").replace("<gray>", "§7")
                 .replace("<red>", "§c").replace("<green>", "§a")
                 .replace("<yellow>", "§e").replace("<blue>", "§9")
                 .replace("<gold>", "§6").replace("<aqua>", "§b")
                 .replace("<bold>", "§l").replace("<b>", "§l")
                 .replace("<italic>", "§o").replace("<i>", "§o")
                 .replace("<reset>", "§r");

        // 3. Handle closing tags
        msg = msg.replace("</white>", "§r").replace("</gray>", "§r")
                 .replace("</red>", "§r").replace("</green>", "§r")
                 .replace("</yellow>", "§r").replace("</blue>", "§r")
                 .replace("</gold>", "§r").replace("</aqua>", "§r")
                 .replace("</bold>", "§r").replace("</b>", "§r")
                 .replace("</italic>", "§r").replace("</i>", "§r");

        // 4. Translate standard & codes
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static void send(CommandSender sender, String key, String... placeholders) {
        String message = plugin.getConfigManager().getMessages().getString(key);
        if (message == null || message.isEmpty()) return;

        String prefix = plugin.getConfigManager().getMessages().getString("prefix", "");
        message = prefix + message;

        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace("%" + placeholders[i] + "%", placeholders[i + 1]);
            }
        }

        sender.sendMessage(color(message));
    }

    public static void broadcast(String key, String... placeholders) {
        String message = plugin.getConfigManager().getMessages().getString(key);
        if (message == null || message.isEmpty()) return;

        String prefix = plugin.getConfigManager().getMessages().getString("prefix", "");
        message = prefix + message;

        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace("%" + placeholders[i] + "%", placeholders[i + 1]);
            }
        }

        plugin.getServer().broadcastMessage(color(message));
    }

    /**
     * Converts a hex string to a byte array.
     */
    public static byte[] hexToBytes(String hex) {
        if (hex == null || hex.isEmpty()) return null;
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }
}
