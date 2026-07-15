/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 */
package com.floki.strengthsmp.commands;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.commands.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class KillResetCommand
extends BaseCommand {
    public KillResetCommand(StrengthSMP plugin) {
        super(plugin);
        plugin.getCommand("killreset").setExecutor((CommandExecutor)this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("strengthsmp.admin")) {
            this.sendError(sender, "You don't have permission to use this command.");
            return true;
        }
        int count = this.plugin.getDataManager().resetAllKills();
        this.sendSuccess(sender, "Kill counts wiped for <#f1c40f>" + count + "</#f1c40f> players. Monarch is unchanged.");
        this.plugin.getLogger().info("[KillReset] " + sender.getName() + " reset all kill counts (" + count + " players affected).");
        return true;
    }
}

