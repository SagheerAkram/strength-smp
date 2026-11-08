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
import com.floki.strengthsmp.gui.BindGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BindCommand
extends BaseCommand {
    public BindCommand(StrengthSMP plugin) {
        super(plugin);
        plugin.getCommand("bind").setExecutor((CommandExecutor)this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!this.isPlayer(sender)) {
            return true;
        }
        Player player = (Player)sender;
        if (!this.plugin.getConfigManager().isBindSystemEnabled()) {
            this.sendError((CommandSender)player, "The weapon binding system is currently disabled.");
            return true;
        }
        new BindGUI(this.plugin, player).open();
        return true;
    }
}

