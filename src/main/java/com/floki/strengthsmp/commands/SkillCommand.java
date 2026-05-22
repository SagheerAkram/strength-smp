package com.floki.strengthsmp.commands;

import com.floki.strengthsmp.StrengthSMP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkillCommand extends BaseCommand {

    public SkillCommand(StrengthSMP plugin) {
        super(plugin);
        plugin.getCommand("skills").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sendError(sender, "Only players can use this command.");
            return true;
        }

        if (plugin.getSkillTreeGUI() != null) {
            plugin.getSkillTreeGUI().open(player);
        }
        
        return true;
    }
}
