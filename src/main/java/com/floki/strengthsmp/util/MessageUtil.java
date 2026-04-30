package com.floki.strengthsmp.util;

import com.floki.strengthsmp.StrengthSMP;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for sending formatted and localized messages.
 * Updated to handle MiniMessage-style tags for premium visuals.
 */
public class MessageUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern TAG_PATTERN = Pattern.compile("<([a-z0-9#]+)>");
    private static StrengthSMP plugin;

    public static void init(StrengthSMP instance) {
        plugin = instance;
    }

    /**
     * Alias for color() to maintain compatibility with existing code.
     */
    public static String parse(String message) {
        return color(message);
    }

    public static String color(String message) {
        if (message == null || message.isEmpty()) return "";
        
        String msg = message;

        // 1. Handle Hex <#HEX>
        Matcher hexTag = Pattern.compile("<#([A-Fa-f0-9]{6})>").matcher(msg);
        StringBuffer sb = new StringBuffer();
        while (hexTag.find()) {
            try {
                hexTag.appendReplacement(sb, ChatColor.of("#" + hexTag.group(1)).toString());
            } catch (Exception ignored) {
                hexTag.appendReplacement(sb, "");
            }
        }
        hexTag.appendTail(sb);
        msg = sb.toString();

        // 2. Handle named color tags and formatting precisely
        msg = msg.replace("<white>", "§f").replace("<WHITE>", "§f")
                 .replace("<gray>", "§7").replace("<GRAY>", "§7")
                 .replace("<black>", "§0").replace("<BLACK>", "§0")
                 .replace("<red>", "§c").replace("<RED>", "§c")
                 .replace("<green>", "§a").replace("<GREEN>", "§a")
                 .replace("<yellow>", "§e").replace("<YELLOW>", "§e")
                 .replace("<blue>", "§9").replace("<BLUE>", "§9")
                 .replace("<gold>", "§6").replace("<GOLD>", "§6")
                 .replace("<aqua>", "§b").replace("<AQUA>", "§b")
                 .replace("<bold>", "§l").replace("<BOLD>", "§l")
                 .replace("<b>", "§l").replace("<B>", "§l")
                 .replace("<italic>", "§o").replace("<ITALIC>", "§o")
                 .replace("<i>", "§o").replace("<I>", "§o")
                 .replace("<reset>", "§r").replace("<RESET>", "§r");

        // 3. Handle closing tags
        msg = msg.replace("</white>", "§r").replace("</WHITE>", "§r")
                 .replace("</gray>", "§r").replace("</GRAY>", "§r")
                 .replace("</black>", "§r").replace("</BLACK>", "§r")
                 .replace("</red>", "§r").replace("</RED>", "§r")
                 .replace("</green>", "§r").replace("</GREEN>", "§r")
                 .replace("</yellow>", "§r").replace("</YELLOW>", "§r")
                 .replace("</blue>", "§r").replace("</BLUE>", "§r")
                 .replace("</gold>", "§r").replace("</GOLD>", "§r")
                 .replace("</aqua>", "§r").replace("</AQUA>", "§r")
                 .replace("</bold>", "§r").replace("</BOLD>", "§r")
                 .replace("</b>", "§r").replace("</B>", "§r")
                 .replace("</italic>", "§r").replace("</ITALIC>", "§r")
                 .replace("</i>", "§r").replace("</I>", "§r");

        // 4. Handle closing HEX tags (e.g., </#f1c40f>)
        Matcher hexClosing = Pattern.compile("</#([A-Fa-f0-9]{6})>").matcher(msg);
        msg = hexClosing.replaceAll("§r");

        // 5. Handle &#HEX legacy codes
        Matcher hexLegacy = HEX_PATTERN.matcher(msg);
        sb = new StringBuffer();
        while (hexLegacy.find()) {
            try {
                hexLegacy.appendReplacement(sb, ChatColor.of("#" + hexLegacy.group(1)).toString());
            } catch (Exception ignored) {
                hexLegacy.appendReplacement(sb, "");
            }
        }
        hexLegacy.appendTail(sb);
        msg = sb.toString();

        // 7. Translate standard & codes
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
}
