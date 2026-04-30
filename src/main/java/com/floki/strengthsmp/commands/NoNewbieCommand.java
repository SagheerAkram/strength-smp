package com.floki.strengthsmp.commands;

import com.floki.strengthsmp.StrengthSMP;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /nonewbie <player> — Admin command to remove a player's newbie protection early.
 * Useful if a player is exploiting the protection (e.g., farming while immune).
 */
public class NoNewbieCommand extends BaseCommand {

    public NoNewbieCommand(StrengthSMP plugin) {
        super(plugin);
        plugin.getCommand("nonewbie").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("strengthsmp.admin")) {
            sendError(sender, "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 1) {
            sendError(sender, "Usage: /nonewbie <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sendError(sender, "Player not found or not online.");
            return true;
        }

        if (!plugin.getDataManager().isNewbieProtected(target.getUniqueId())) {
            sendError(sender, "<#f1c40f>" + target.getName() + "</#f1c40f> doesn't have active newbie protection.");
            return true;
        }

        plugin.getDataManager().clearNewbieProtection(target.getUniqueId());
        plugin.getDataManager().savePlayer(target.getUniqueId());

        sendSuccess(sender, "Removed newbie protection from <#f1c40f>" + target.getName() + "</#f1c40f>. They are now in the normal strength economy.");
        sendMessage(target, "<#e74c3c><b>[ADMIN]</b></#e74c3c> <gray>Your newbie protection has been removed. You are now fully in the strength economy.</gray>");

        plugin.getLogger().info("[NoNewbie] " + sender.getName() + " removed newbie protection from " + target.getName());
        return true;
    }
}
