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

public class RemoveProtectionCommand
extends BaseCommand {
    public RemoveProtectionCommand(StrengthSMP plugin) {
        super(plugin);
        plugin.getCommand("removeprotection").setExecutor((CommandExecutor)this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            this.sendError(sender, "Only players can use this command.");
            return true;
        }
        Player player = (Player)sender;
        boolean removed = false;
        if (this.plugin.getDataManager().isNewbieProtected(player.getUniqueId())) {
            this.plugin.getDataManager().clearNewbieProtection(player.getUniqueId());
            removed = true;
        }
        if (this.plugin.getDataManager().hasDeathProtection(player.getUniqueId())) {
            this.plugin.getDataManager().clearDeathProtection(player.getUniqueId());
            removed = true;
        }
        if (removed) {
            this.plugin.getDataManager().savePlayer(player.getUniqueId());
            this.sendSuccess((CommandSender)player, "Your protection has been removed. You are now fully vulnerable and can gain/lose strength in PvP!");
        } else {
            this.sendError((CommandSender)player, "You do not currently have any active protection.");
        }
        return true;
    }
}

