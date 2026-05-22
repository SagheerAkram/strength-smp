package com.floki.strengthsmp.commands;

import com.floki.strengthsmp.StrengthSMP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class KillResetCommand extends BaseCommand {

    public KillResetCommand(StrengthSMP plugin) {
        super(plugin);
        plugin.getCommand("killreset").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("strengthsmp.admin")) {
            sendError(sender, "You don't have permission to use this command.");
            return true;
        }

        int count = plugin.getDataManager().resetAllKills();

        sendSuccess(sender, "Kill counts wiped for <#f1c40f>" + count + "</#f1c40f> players. Monarch is unchanged.");
        plugin.getLogger().info("[KillReset] " + sender.getName() + " reset all kill counts (" + count + " players affected).");
        return true;
    }
}
