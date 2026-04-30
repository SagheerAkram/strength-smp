package com.floki.strengthsmp.commands;

import com.floki.strengthsmp.StrengthSMP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoveProtectionCommand extends BaseCommand {

    public RemoveProtectionCommand(StrengthSMP plugin) {
        super(plugin);
        plugin.getCommand("removeprotection").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sendError(sender, "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        boolean removed = false;

        if (plugin.getDataManager().isNewbieProtected(player.getUniqueId())) {
            plugin.getDataManager().clearNewbieProtection(player.getUniqueId());
            removed = true;
        }

        if (plugin.getDataManager().hasDeathProtection(player.getUniqueId())) {
            plugin.getDataManager().clearDeathProtection(player.getUniqueId());
            removed = true;
        }

        if (removed) {
            plugin.getDataManager().savePlayer(player.getUniqueId());
            sendSuccess(player, "Your protection has been removed. You are now fully vulnerable and can gain/lose strength in PvP!");
        } else {
            sendError(player, "You do not currently have any active protection.");
        }

        return true;
    }
}
