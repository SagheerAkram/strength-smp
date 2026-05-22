package com.floki.strengthsmp.commands;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.gui.BindGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BindCommand extends BaseCommand {

    public BindCommand(StrengthSMP plugin) {
        super(plugin);
        plugin.getCommand("bind").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!isPlayer(sender)) return true;
        Player player = (Player) sender;

        if (!plugin.getConfigManager().isBindSystemEnabled()) {
            sendError(player, "The weapon binding system is currently disabled.");
            return true;
        }

        // Open the beautiful binding GUI
        new BindGUI(plugin, player).open();
        return true;
    }
}
