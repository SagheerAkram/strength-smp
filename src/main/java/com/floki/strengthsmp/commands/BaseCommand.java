package com.floki.strengthsmp.commands;

import com.floki.strengthsmp.StrengthSMP;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Base command implementation
 */
public abstract class BaseCommand implements org.bukkit.command.CommandExecutor {
    
    protected final StrengthSMP plugin;
    
    public BaseCommand(StrengthSMP plugin) {
        this.plugin = plugin;
    }
    
    protected void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(com.floki.strengthsmp.util.MessageUtil.parse(message));
    }
    
    protected void sendError(CommandSender sender, String message) {
        sender.sendMessage(com.floki.strengthsmp.util.MessageUtil.parse("<#e74c3c><b>[ERROR]</b> " + message));
    }
    
    protected void sendSuccess(CommandSender sender, String message) {
        sender.sendMessage(com.floki.strengthsmp.util.MessageUtil.parse("<#2ecc71><b>[SUCCESS]</b> " + message));
    }
    
    protected boolean isPlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(com.floki.strengthsmp.util.MessageUtil.parse("<#e74c3c>This command is only available for players!"));
            return false;
        }
        return true;
    }
}
