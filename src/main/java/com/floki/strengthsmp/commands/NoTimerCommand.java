package com.floki.strengthsmp.commands;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.commands.BaseCommand;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NoTimerCommand
extends BaseCommand {
    public NoTimerCommand(StrengthSMP plugin) {
        super(plugin);
        plugin.getCommand("notimer").setExecutor((CommandExecutor)this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("strengthsmp.admin")) {
            this.sendError(sender, "You don't have permission to use this command.");
            return true;
        }
        if (args.length < 1) {
            this.sendError(sender, "Usage: /notimer <player>");
            return true;
        }
        Player target = Bukkit.getPlayer((String)args[0]);
        if (target == null) {
            this.sendError(sender, "Player not found or not online.");
            return true;
        }
        boolean hadDeath = this.plugin.getDataManager().hasDeathProtection(target.getUniqueId());
        boolean hadNewbie = this.plugin.getDataManager().isNewbieProtected(target.getUniqueId());
        if (!hadDeath && !hadNewbie) {
            this.sendError(sender, "<#f1c40f>" + target.getName() + "</#f1c40f> has no active protection timer.");
            return true;
        }
        if (hadDeath) {
            this.plugin.getDataManager().clearDeathProtection(target.getUniqueId());
        }
        if (hadNewbie) {
            this.plugin.getDataManager().clearNewbieProtection(target.getUniqueId());
            this.plugin.getDataManager().savePlayer(target.getUniqueId());
        }
        this.sendSuccess(sender, "Removed the protection timer from <#f1c40f>" + target.getName() + "</#f1c40f>.");
        MessageUtil.sendWithDefault(target, "commands.no-timer-message", "<#e74c3c><b>[ADMIN]</b></#e74c3c> <gray>Your protection timer has been removed.</gray>");
        this.plugin.getLogger().info("[NoTimer] " + sender.getName() + " removed protection from " + target.getName());
        return true;
    }
}
