/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Sound
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package com.floki.strengthsmp.commands;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.commands.BaseCommand;
import com.floki.strengthsmp.util.MessageUtil;
import com.floki.strengthsmp.util.CompatUtil;
import com.floki.strengthsmp.util.ItemFactory;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WithdrawCommand
extends BaseCommand {
    public WithdrawCommand(StrengthSMP plugin) {
        super(plugin);
        plugin.getCommand("withdraw").setExecutor((CommandExecutor)this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        int min;
        int amount;
        if (!(sender instanceof Player)) {
            this.sendError(sender, "Only players can use this command.");
            return true;
        }
        Player player = (Player)sender;
        if (args.length < 1) {
            this.sendError((CommandSender)player, "Usage: /withdraw <amount>");
            return true;
        }
        try {
            amount = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException e) {
            this.sendError((CommandSender)player, "Please enter a valid number.");
            return true;
        }
        if (amount <= 0) {
            this.sendError((CommandSender)player, "You must withdraw at least 1 strength!");
            return true;
        }
        com.floki.strengthsmp.api.HollowHook hollow = this.plugin.getHollowHook();
        long lockMs = hollow != null ? hollow.getWithdrawLockRemainingMs(player.getUniqueId()) : 0L;
        if (lockMs > 0L) {
            this.sendError((CommandSender)player, "You escaped the Hollow recently. Withdrawing unlocks in <#f1c40f>" + com.floki.strengthsmp.util.TimeFormat.duration(lockMs) + "</#f1c40f>.");
            return true;
        }
        int currentStrength = this.plugin.getDataManager().getStrength(player.getUniqueId());
        // Withdrawing below the floor (0 by default) would let players dump strength and farm revives.
        min = Math.max(this.plugin.getConfigManager().getMinStrength(), this.plugin.getConfigManager().getConfig().getInt("strength.withdraw-floor", 0));
        if (currentStrength - amount < min) {
            this.sendError((CommandSender)player, "You cannot withdraw this much! You must keep at least <#f1c40f>" + min + " STR.");
            return true;
        }
        long cooldownSeconds = Math.min(12L, 3L + 2L * (long)(amount - 1));
        long lastUse = this.plugin.getDataManager().getAbilityCooldown(player.getUniqueId(), "withdraw_cmd");
        long remaining = lastUse + cooldownSeconds * 1000L - System.currentTimeMillis();
        if (remaining > 0L) {
            double secondsLeft = Math.ceil((double)remaining / 1000.0);
            this.sendError((CommandSender)player, "Please wait <#f1c40f>" + (int)secondsLeft + "s</#f1c40f> before withdrawing again.");
            return true;
        }
        if (player.getInventory().firstEmpty() == -1) {
            this.sendError((CommandSender)player, "Please have at least 1 slot space to withdraw strength");
            return true;
        }
        this.plugin.getStrengthService().removeStrength(player, amount);
        player.getInventory().addItem(new ItemStack[]{ItemFactory.createStrengthItem(amount)});
        this.plugin.getDataManager().setAbilityCooldown(player.getUniqueId(), "withdraw_cmd", System.currentTimeMillis());
        int newBalance = this.plugin.getDataManager().getStrength(player.getUniqueId());
        MessageUtil.sendWithDefault(player, "commands.withdraw-success", "<#2ecc71><b>WITHDRAW</b></#2ecc71> <gray>You withdrew</gray> <#f1c40f>%amount% STR</#f1c40f><gray>. Remaining:</gray> <white>%remaining%</white>", "amount", String.valueOf(amount), "remaining", String.valueOf(newBalance));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        CompatUtil.playSound(player, player.getLocation(), "BLOCK_AMETHYST_BLOCK_CHIME", 1.0f, 1.0f);
        for (int i = 0; i < 360; i += 30) {
            double angle = Math.toRadians(i);
            double x = Math.cos(angle) * 0.8;
            double z = Math.sin(angle) * 0.8;
            CompatUtil.spawnParticle(player.getWorld(), "ENCHANTMENT_TABLE", player.getLocation().add(x, 1.0, z), 5, 0.1, 0.1, 0.1, 0.05);
            CompatUtil.spawnParticle(player.getWorld(), "FIREWORKS_SPARK", player.getLocation().add(x, 0.5, z), 2, 0.05, 0.05, 0.05, 0.02);
        }
        return true;
    }
}

