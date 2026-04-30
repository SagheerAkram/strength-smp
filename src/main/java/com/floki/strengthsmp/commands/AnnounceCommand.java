package com.floki.strengthsmp.commands;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class AnnounceCommand extends BaseCommand {

    public AnnounceCommand(StrengthSMP plugin) {
        super(plugin);
        plugin.getCommand("announce").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("strengthsmp.admin")) {
            sendError(sender, "You don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            sendError(sender, "Usage: /announce <message>");
            return true;
        }

        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            builder.append(arg).append(" ");
        }

        String rawMessage = builder.toString().trim();
        
        // Use MiniMessage-style formatting for the broadcast
        Bukkit.broadcastMessage(MessageUtil.parse("<#34495e><b>§8§l» <#f1c40f><b>ANNOUNCEMENT</b> <#34495e><b>§8§l«</b>"));
        Bukkit.broadcastMessage(MessageUtil.parse("<white>" + rawMessage));
        Bukkit.broadcastMessage("");

        return true;
    }
}
