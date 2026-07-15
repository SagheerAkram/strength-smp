/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package com.floki.strengthsmp.commands;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class BaseCommand
implements CommandExecutor {
    protected final StrengthSMP plugin;

    public BaseCommand(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    protected void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(MessageUtil.parse(message));
    }

    protected void sendError(CommandSender sender, String message) {
        sender.sendMessage(MessageUtil.parse("<#e74c3c><b>[ERROR]</b> " + message));
    }

    protected void sendSuccess(CommandSender sender, String message) {
        sender.sendMessage(MessageUtil.parse("<#2ecc71><b>[SUCCESS]</b> " + message));
    }

    protected boolean isPlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtil.parse("<#e74c3c>This command is only available for players!"));
            return false;
        }
        return true;
    }
}

