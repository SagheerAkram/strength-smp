package com.floki.strengthsmp.commands;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BountyRefundCommand extends BaseCommand {

    public BountyRefundCommand(StrengthSMP plugin) {
        super(plugin);
        plugin.getCommand("bountyrefund").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("strengthsmp.admin")) {
            sendError(sender, "You don't have permission to use this command!");
            return true;
        }

        if (args.length < 1) {
            sendError(sender, "Usage: /bountyrefund <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sendError(sender, "Player not found!");
            return true;
        }

        int totalBounty = plugin.getDataManager().getBounty(target.getUniqueId());
        if (totalBounty <= 0) {
            sendError(sender, "This player has no active bounty!");
            return true;
        }

        // Refund all contributors
        java.util.Map<UUID, Integer> contributors = plugin.getDataManager().getBountyCache().get(target.getUniqueId());
        if (contributors != null) {
            for (java.util.Map.Entry<UUID, Integer> entry : contributors.entrySet()) {
                UUID setterUuid = entry.getKey();
                int amount = entry.getValue();

                plugin.getDataManager().addStrength(setterUuid, amount);
                
                Player setter = Bukkit.getPlayer(setterUuid);
                if (setter != null && setter.isOnline()) {
                    sendMessage(setter, "<#2ecc71><b>[!]</b></#2ecc71> <gray>Your bounty of</gray> <#f1c40f>" + amount + " STR</#f1c40f> <gray>on " + target.getName() + " was refunded by an admin.</gray>");
                }
            }
        }

        plugin.getDataManager().removeBounty(target.getUniqueId());
        sendSuccess(sender, "Successfully refunded bounty for <#f1c40f>" + target.getName() + "</#f1c40f> <gray>(Total: " + totalBounty + " STR)</gray>");
        
        return true;
    }
}
