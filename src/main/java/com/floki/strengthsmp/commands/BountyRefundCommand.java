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
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BountyRefundCommand
extends BaseCommand {
    public BountyRefundCommand(StrengthSMP plugin) {
        super(plugin);
        plugin.getCommand("bountyrefund").setExecutor((CommandExecutor)this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("strengthsmp.admin")) {
            this.sendError(sender, "You don't have permission to use this command!");
            return true;
        }
        if (args.length < 1) {
            this.sendError(sender, "Usage: /bountyrefund <player>");
            return true;
        }
        Player target = Bukkit.getPlayer((String)args[0]);
        if (target == null) {
            this.sendError(sender, "Player not found!");
            return true;
        }
        int totalBounty = this.plugin.getDataManager().getBounty(target.getUniqueId());
        if (totalBounty <= 0) {
            this.sendError(sender, "This player has no active bounty!");
            return true;
        }
        Map<UUID, Integer> contributors = this.plugin.getDataManager().getBountyCache().get(target.getUniqueId());
        if (contributors != null) {
            for (Map.Entry<UUID, Integer> entry : contributors.entrySet()) {
                UUID setterUuid = entry.getKey();
                int amount = entry.getValue();
                this.plugin.getDataManager().addStrength(setterUuid, amount);
                Player setter = Bukkit.getPlayer((UUID)setterUuid);
                if (setter == null || !setter.isOnline()) continue;
                this.plugin.updateDisplay(setter);
                this.sendMessage((CommandSender)setter, "<#2ecc71><b>[!]</b></#2ecc71> <gray>Your bounty of</gray> <#f1c40f>" + amount + " STR</#f1c40f> <gray>on " + target.getName() + " was refunded by an admin.</gray>");
            }
            this.plugin.getMonarchService().calculateNewMonarch();
        }
        this.plugin.getDataManager().removeBounty(target.getUniqueId());
        this.plugin.updateDisplay(target);
        this.sendSuccess(sender, "Successfully refunded bounty for <#f1c40f>" + target.getName() + "</#f1c40f> <gray>(Total: " + totalBounty + " STR)</gray>");
        return true;
    }
}

