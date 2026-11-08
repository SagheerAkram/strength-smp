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
import com.floki.strengthsmp.util.MessageUtil;
import java.util.Date;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PunishCommand
extends BaseCommand {
    private static final long PUNISHMENT_DURATION_MS = 86400000L;

    public PunishCommand(StrengthSMP plugin) {
        super(plugin);
        plugin.getCommand("punish").setExecutor((CommandExecutor)this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("strengthsmp.admin")) {
            this.sendError(sender, "You don't have permission to use this command.");
            return true;
        }
        if (args.length < 1) {
            this.sendError(sender, "Usage: /punish <player>");
            return true;
        }
        Player target = Bukkit.getPlayer((String)args[0]);
        if (target == null) {
            this.sendError(sender, "Player not found or not online.");
            return true;
        }
        long expiry = System.currentTimeMillis() + 86400000L;
        this.plugin.getDataManager().setPunishment(target.getUniqueId(), expiry);
        this.plugin.getDataManager().savePlayer(target.getUniqueId());
        this.sendSuccess(sender, "Punished <#f1c40f>" + target.getName() + "</#f1c40f> for <white>24 hours</white>. They will now lose <red>2 STR</red> on death.");
        MessageUtil.sendWithDefault(target, "commands.punish-message", "<#e74c3c><b>[PUNISH]</b></#e74c3c> <gray>You have been punished by an admin. For the next</gray> <white>24 hours</white><gray>, you will lose</gray> <red><b>2 STR</b></red> <gray>on each death.</gray>");
        this.plugin.getLogger().info("[Punish] " + sender.getName() + " punished " + target.getName() + " until " + String.valueOf(new Date(expiry)));
        return true;
    }
}

