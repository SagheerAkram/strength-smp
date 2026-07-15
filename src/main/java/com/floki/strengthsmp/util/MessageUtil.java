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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class MessageUtil {
    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private static final Pattern HEX_CLOSE_PATTERN = Pattern.compile("</#[A-Fa-f0-9]{6}>");
    private static StrengthSMP plugin;
    private static boolean supportsHex;

    public static void init(StrengthSMP instance) {
        plugin = instance;
        try {
            net.md_5.bungee.api.ChatColor.of((String)"#FFFFFF");
            supportsHex = true;
        }
        catch (Throwable e) {
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
                matcher.appendReplacement(sb, net.md_5.bungee.api.ChatColor.of((String)("#" + matcher.group(1))).toString());
            }
            matcher.appendTail(sb);
            msg = sb.toString();
            Matcher closeMatcher = HEX_CLOSE_PATTERN.matcher(msg);
            msg = closeMatcher.replaceAll("\u00a7r");
        }
        msg = msg.replace("<white>", "\u00a7f").replace("<gray>", "\u00a77").replace("<red>", "\u00a7c").replace("<green>", "\u00a7a").replace("<yellow>", "\u00a7e").replace("<blue>", "\u00a79").replace("<gold>", "\u00a76").replace("<aqua>", "\u00a7b").replace("<bold>", "\u00a7l").replace("<b>", "\u00a7l").replace("<italic>", "\u00a7o").replace("<i>", "\u00a7o").replace("<reset>", "\u00a7r");
        msg = msg.replace("</white>", "\u00a7r").replace("</gray>", "\u00a7r").replace("</red>", "\u00a7r").replace("</green>", "\u00a7r").replace("</yellow>", "\u00a7r").replace("</blue>", "\u00a7r").replace("</gold>", "\u00a7r").replace("</aqua>", "\u00a7r").replace("</bold>", "\u00a7r").replace("</b>", "\u00a7r").replace("</italic>", "\u00a7r").replace("</i>", "\u00a7r");
        return ChatColor.translateAlternateColorCodes((char)'&', (String)msg);
    }

    public static void send(CommandSender sender, String key, String ... placeholders) {
        Object message = plugin.getConfigManager().getMessages().getString(key);
        if (message == null || ((String)message).isEmpty()) {
            return;
        }
        String prefix = plugin.getConfigManager().getMessages().getString("prefix", "");
        message = prefix + (String)message;
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 >= placeholders.length) continue;
            message = ((String)message).replace("%" + placeholders[i] + "%", placeholders[i + 1]);
        }
        sender.sendMessage(MessageUtil.color((String)message));
    }

    public static void broadcast(String key, String ... placeholders) {
        Object message = plugin.getConfigManager().getMessages().getString(key);
        if (message == null || ((String)message).isEmpty()) {
            return;
        }
        String prefix = plugin.getConfigManager().getMessages().getString("prefix", "");
        message = prefix + (String)message;
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 >= placeholders.length) continue;
            message = ((String)message).replace("%" + placeholders[i] + "%", placeholders[i + 1]);
        }
        plugin.getServer().broadcastMessage(MessageUtil.color((String)message));
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

