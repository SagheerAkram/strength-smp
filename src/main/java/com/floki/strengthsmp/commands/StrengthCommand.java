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

        int strength = plugin.getDataManager().getStrength(player.getUniqueId());
        WeaponType weapon = plugin.getDataManager().getWeapon(player.getUniqueId());

        sendMessage(player, "<#34495e>----------------------------------</#34495e>");
        sendMessage(player, "  <#f1c40f><b>STRENGTH STATS</b></#f1c40f>");
        sendMessage(player, " ");
        sendMessage(player, "  <#95a5a6>Strength:</#95a5a6> <white>" + strength + "</white>");
        
        if (weapon == null) {
            sendMessage(player, "  <#95a5a6>Weapon:</#95a5a6> <white>Neutral</white>");
            sendMessage(player, "  <#95a5a6>Description:</#95a5a6> <gray>You have no weapon abilities at 0 strength.</gray>");
        } else {
            sendMessage(player, "  <#95a5a6>Weapon:</#95a5a6> <white>" + weapon.getIcon() + " " + weapon.getDisplayName() + "</white>");
            sendMessage(player, "  <#95a5a6>Description:</#95a5a6> <gray>" + weapon.getDescription() + "</gray>");
        }
        sendMessage(player, " ");
        
        if (player.hasPermission("strengthsmp.admin")) {
            sendMessage(player, "  <#e74c3c><b>ADMIN COMMANDS:</b></#e74c3c>");
            sendMessage(player, "  <#f39c12>•</#f39c12> <white>/strengthsmp reroll <player></white>");
            sendMessage(player, "  <#f39c12>•</#f39c12> <white>/strengthsmp setstrength <player> <amount></white>");
            sendMessage(player, "  <#f39c12>•</#f39c12> <white>/strengthsmp setweapon <player> <weapon></white>");
            sendMessage(player, "  <#f39c12>•</#f39c12> <white>/strengthsmp resetplayer <player></white>");
        }
        
        sendMessage(player, "<#34495e>----------------------------------</#34495e>");

        return true;
    }
}
