/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package com.floki.strengthsmp.commands;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.commands.BaseCommand;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BountyCommand
extends BaseCommand {
    public BountyCommand(StrengthSMP plugin) {
        super(plugin);
        plugin.getCommand("bounty").setExecutor((CommandExecutor)this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        int min;
        int amount;
        if (!(sender instanceof Player)) {
            this.sendError(sender, "Only players can use this command.");
            return true;
        }
        Player player = (Player)sender;
        if (args.length == 0 || args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("gui")) {
            new com.floki.strengthsmp.gui.BountyBoardGUI(this.plugin).open(player);
            return true;
        }
        if (args.length < 2) {
            this.sendError((CommandSender)player, "Usage: /bounty <player> <amount>");
            return true;
        }
        Player target = Bukkit.getPlayer((String)args[0]);
        if (target == null) {
            this.sendError((CommandSender)player, "Player not found!");
            return true;
        }
        if (target.getUniqueId().equals(player.getUniqueId())) {
            this.sendError((CommandSender)player, "You cannot put a bounty on yourself!");
            return true;
        }
        try {
            amount = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e) {
            this.sendError((CommandSender)player, "Invalid amount!");
            return true;
        }
        if (amount <= 0) {
            this.sendError((CommandSender)player, "Amount must be positive!");
            return true;
        }
        int playerStrength = this.plugin.getDataManager().getStrength(player.getUniqueId());
        if (playerStrength - amount < (min = this.plugin.getConfigManager().getMinStrength())) {
            this.sendError((CommandSender)player, "You cannot afford this bounty! You must maintain at least <#f1c40f>" + min + " STR</#f1c40f>.");
            return true;
        }
        this.plugin.getDataManager().subtractStrength(player.getUniqueId(), amount);
        this.plugin.getDataManager().addBounty(target.getUniqueId(), player.getUniqueId(), amount);
        this.plugin.updateDisplay(target);
        this.plugin.updateDisplay(player);
        this.plugin.getMonarchService().calculateNewMonarch();
        this.sendMessage((CommandSender)player, "<#2ecc71><b>[!]</b></#2ecc71> <gray>You placed a bounty of</gray> <#f1c40f>" + amount + " STR</#f1c40f> <gray>on " + target.getName() + "!</gray>");
        Bukkit.broadcastMessage((String)MessageUtil.parse("<#e74c3c><b>[BOUNTY]</b></#e74c3c> <white>" + player.getName() + "</white> <gray>placed</gray> <#f1c40f>" + amount + " Strength</#f1c40f> <gray>bounty on</gray> <white>" + target.getName() + "</white><gray>!</gray>"));
        this.plugin.getDiscordManager().updateDashboard();
        return true;
    }
}

