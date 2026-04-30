package com.floki.strengthsmp.commands;

import com.floki.strengthsmp.StrengthSMP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Sound;
import com.floki.strengthsmp.util.MessageUtil;
import com.floki.strengthsmp.util.ItemFactory;

public class WithdrawCommand extends BaseCommand {
    public WithdrawCommand(StrengthSMP plugin) {
        super(plugin);
        plugin.getCommand("withdraw").setExecutor(this);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sendError(sender, "Only players can use this command.");
            return true;
        }

        if (args.length < 1) {
            sendError(player, "Usage: /withdraw <amount>");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sendError(player, "Please enter a valid number.");
            return true;
        }

        if (amount <= 0) {
            sendError(player, "You must withdraw at least 1 strength!");
            return true;
        }

        int currentStrength = plugin.getDataManager().getStrength(player.getUniqueId());
        int min = plugin.getConfigManager().getMinStrength();
        
        if (currentStrength - amount < min) {
            sendError(player, "You cannot withdraw this much! You must maintain at least <#f1c40f>" + min + " STR.");
            return true;
        }

        // ── DYNAMIC COOLDOWN ─────────────────────────────────────────────────
        // Formula: 3s base + 2s per additional strength withdrawn, max 12s
        long cooldownSeconds = Math.min(12, 3 + (2L * (amount - 1)));
        long lastUse = plugin.getDataManager().getAbilityCooldown(player.getUniqueId(), "withdraw_cmd");
        long remaining = (lastUse + (cooldownSeconds * 1000)) - System.currentTimeMillis();

        if (remaining > 0) {
            double secondsLeft = Math.ceil(remaining / 1000.0);
            sendError(player, "Please wait <#f1c40f>" + (int)secondsLeft + "s</#f1c40f> before withdrawing again.");
            return true;
        }

        if (player.getInventory().firstEmpty() == -1) {
            sendError(player, "Please have at least 1 slot space to withdraw strength");
            return true;
        }

        // Subtract strength and give item via service (triggers monarch update)
        plugin.getStrengthService().removeStrength(player, amount);
        player.getInventory().addItem(ItemFactory.createStrengthItem(amount));
        
        // Set cooldown
        plugin.getDataManager().setAbilityCooldown(player.getUniqueId(), "withdraw_cmd", System.currentTimeMillis());

        int newBalance = plugin.getDataManager().getStrength(player.getUniqueId());
        
        sendMessage(player, "<#2ecc71><b>WITHDRAW</b></#2ecc71> <gray>You withdrew</gray> <#f1c40f>" + amount + " STR</#f1c40f><gray>. Remaining:</gray> <white>" + newBalance + "</white>");
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
        
        return true;
    }
}
