/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 */
package com.floki.strengthsmp.commands;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.commands.BaseCommand;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AnnounceCommand
extends BaseCommand {
    public AnnounceCommand(StrengthSMP plugin) {
        super(plugin);
        plugin.getCommand("announce").setExecutor((CommandExecutor)this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("strengthsmp.admin")) {
            this.sendError(sender, "You don't have permission to use this command!");
            return true;
        }
        if (args.length == 0) {
            this.sendError(sender, "Usage: /announce <message>");
            return true;
        }
        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            builder.append(arg).append(" ");
        }
        String rawMessage = builder.toString().trim();
        Bukkit.broadcastMessage((String)MessageUtil.parse("<#34495e><b>\u00a78\u00a7l\u00bb <#f1c40f><b>ANNOUNCEMENT</b> <#34495e><b>\u00a78\u00a7l\u00ab</b>"));
        Bukkit.broadcastMessage((String)MessageUtil.parse("<white>" + rawMessage));
        Bukkit.broadcastMessage((String)"");
        return true;
    }
}

