/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package com.floki.strengthsmp.commands;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.commands.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StrengthCommand
extends BaseCommand {
    public StrengthCommand(StrengthSMP plugin) {
        super(plugin);
        plugin.getCommand("strength").setExecutor((CommandExecutor)this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            this.sendError(sender, "Only players can use this command.");
            return true;
        }
        Player player = (Player)sender;
        new com.floki.strengthsmp.gui.StrengthHubGUI(this.plugin).open(player);
        return true;
    }
}

