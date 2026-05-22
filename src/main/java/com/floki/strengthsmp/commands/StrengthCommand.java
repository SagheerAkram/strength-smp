package com.floki.strengthsmp.commands;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.WeaponType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StrengthCommand extends BaseCommand {

    public StrengthCommand(StrengthSMP plugin) {
        super(plugin);
        plugin.getCommand("strength").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sendError(sender, "Only players can use this command.");
            return true;
        }

        plugin.getAestheticService().openStatsLedger(player, player);

        return true;
    }
}
