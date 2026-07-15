/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package com.floki.strengthsmp.commands;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.commands.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NoNewbieCommand
extends BaseCommand {
    public NoNewbieCommand(StrengthSMP plugin) {
        super(plugin);
        plugin.getCommand("nonewbie").setExecutor((CommandExecutor)this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("strengthsmp.admin")) {
            this.sendError(sender, "You don't have permission to use this command.");
            return true;
        }
        if (args.length < 1) {
            this.sendError(sender, "Usage: /nonewbie <player>");
            return true;
        }
        Player target = Bukkit.getPlayer((String)args[0]);
        if (target == null) {
            this.sendError(sender, "Player not found or not online.");
            return true;
        }
        if (!this.plugin.getDataManager().isNewbieProtected(target.getUniqueId())) {
            this.sendError(sender, "<#f1c40f>" + target.getName() + "</#f1c40f> doesn't have active newbie protection.");
            return true;
        }
        this.plugin.getDataManager().clearNewbieProtection(target.getUniqueId());
        this.plugin.getDataManager().savePlayer(target.getUniqueId());
        this.sendSuccess(sender, "Removed newbie protection from <#f1c40f>" + target.getName() + "</#f1c40f>. They are now in the normal strength economy.");
        this.sendMessage((CommandSender)target, "<#e74c3c><b>[ADMIN]</b></#e74c3c> <gray>Your newbie protection has been removed. You are now fully in the strength economy.</gray>");
        this.plugin.getLogger().info("[NoNewbie] " + sender.getName() + " removed newbie protection from " + target.getName());
        return true;
    }
}

