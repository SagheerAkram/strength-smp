package com.floki.strengthsmp.commands;

import com.floki.strengthsmp.StrengthSMP;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BountyCommand extends BaseCommand {

    public BountyCommand(StrengthSMP plugin) {
        super(plugin);
        plugin.getCommand("bounty").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sendError(sender, "Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            sendMessage(player, "<#f1c40f><b>[Bounty System]</b></#f1c40f>");
            sendMessage(player, " <#f39c12>•</#f39c12> <white>/bounty <player> <amount></white> <gray>Place a bounty using your STR</gray>");
            sendMessage(player, " <#f39c12>•</#f39c12> <white>/bounty list</white> <gray>See all active bounties</gray>");
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            sendMessage(player, "<#f1c40f><b>[Active Bounties]</b></#f1c40f>");
            sendMessage(player, " <gray>Check Discord for the full leaderboard and bounty list!</gray>");
            // Optional: iterate and show top 5 here if needed, but Discord is primary.
            return true;
        }

        if (args.length < 2) {
            sendError(player, "Usage: /bounty <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sendError(player, "Player not found!");
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            sendError(player, "You cannot put a bounty on yourself!");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sendError(player, "Invalid amount!");
            return true;
        }

        if (amount <= 0) {
            sendError(player, "Amount must be positive!");
            return true;
        }

        int playerStrength = plugin.getDataManager().getStrength(player.getUniqueId());
        int min = plugin.getConfigManager().getMinStrength();

        if (playerStrength - amount < min) {
            sendError(player, "You cannot afford this bounty! You must maintain at least <#f1c40f>" + min + " STR</#f1c40f>.");
            return true;
        }

        // Deduct from setter, add to target's bounty
        plugin.getDataManager().subtractStrength(player.getUniqueId(), amount);
        plugin.getDataManager().addBounty(target.getUniqueId(), player.getUniqueId(), amount);

        // Update display to trigger glowing effect immediately
        plugin.updateDisplay(target);
        plugin.updateDisplay(player);
        plugin.getMonarchService().calculateNewMonarch();

        sendMessage(player, "<#2ecc71><b>[!]</b></#2ecc71> <gray>You placed a bounty of</gray> <#f1c40f>" + amount + " STR</#f1c40f> <gray>on " + target.getName() + "!</gray>");
        Bukkit.broadcastMessage(com.floki.strengthsmp.util.MessageUtil.parse("<#e74c3c><b>[BOUNTY]</b></#e74c3c> <white>" + player.getName() + "</white> <gray>placed</gray> <#f1c40f>" + amount + " Strength</#f1c40f> <gray>bounty on</gray> <white>" + target.getName() + "</white><gray>!</gray>"));
        
        plugin.getDiscordManager().updateDashboard();

        return true;
    }
}
