package com.floki.strengthsmp.commands;

import com.floki.strengthsmp.StrengthSMP;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PunishCommand extends BaseCommand {

    private static final long PUNISHMENT_DURATION_MS = 24 * 60 * 60 * 1000L; // 24 hours

    public PunishCommand(StrengthSMP plugin) {
        super(plugin);
        plugin.getCommand("punish").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("strengthsmp.admin")) {
            sendError(sender, "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 1) {
            sendError(sender, "Usage: /punish <player>");
            return true;
        }

        // Support both online and offline players
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sendError(sender, "Player not found or not online.");
            return true;
        }

        long expiry = System.currentTimeMillis() + PUNISHMENT_DURATION_MS;
        plugin.getDataManager().setPunishment(target.getUniqueId(), expiry);
        plugin.getDataManager().savePlayer(target.getUniqueId());

        sendSuccess(sender, "Punished <#f1c40f>" + target.getName() + "</#f1c40f> for <white>24 hours</white>. They will now lose <red>2 STR</red> on death.");
        sendMessage(target, "<#e74c3c><b>[PUNISH]</b></#e74c3c> <gray>You have been punished by an admin. For the next</gray> <white>24 hours</white><gray>, you will lose</gray> <red><b>2 STR</b></red> <gray>on each death.</gray>");

        plugin.getLogger().info("[Punish] " + sender.getName() + " punished " + target.getName() + " until " + new java.util.Date(expiry));
        return true;
    }
}
