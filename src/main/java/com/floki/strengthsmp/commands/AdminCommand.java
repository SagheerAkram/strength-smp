/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.event.Event
 *  org.bukkit.event.player.PlayerJoinEvent
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.plugin.Plugin
 */
package com.floki.strengthsmp.commands;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.commands.BaseCommand;
import com.floki.strengthsmp.data.WeaponType;
import com.floki.strengthsmp.util.ItemFactory;
import com.floki.strengthsmp.util.MessageUtil;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class AdminCommand
extends BaseCommand {
    public AdminCommand(StrengthSMP plugin) {
        super(plugin);
        org.bukkit.command.PluginCommand cmd = plugin.getCommand("strengthsmp");
        if (cmd != null) {
            cmd.setExecutor((CommandExecutor)this);
            cmd.setTabCompleter(new AdminTabCompleter());
        }
    }

    private boolean handlePack(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player)sender;
        this.plugin.getLogger().info("\ud83d\udee0\ufe0f Manual pack request from " + player.getName());
        this.plugin.getServer().getPluginManager().callEvent((Event)new PlayerJoinEvent(player, null));
        sender.sendMessage(MessageUtil.parse("<#2ecc71>Attempting to send resource pack... Check console for debug info.</#2ecc71>"));
        return true;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String sub;
        if (!sender.hasPermission("strengthsmp.admin")) {
            this.sendError(sender, "You don't have permission to use this command!");
            return true;
        }
        if (args.length == 0) {
            this.sendMessage(sender, "<#f1c40f><b>[StrengthSMP Admin]</b></#f1c40f>");
            this.sendMessage(sender, " <#f39c12>\u2022</#f39c12> <white>/strengthsmp maintenance <on|off></white> <gray>\u2014 Toggle maintenance mode</gray>");
            this.sendMessage(sender, " <#f39c12>\u2022</#f39c12> <white>/strengthsmp toggle</white> <gray>\u2014 Quick toggle system</gray>");
            this.sendMessage(sender, " <#f39c12>\u2022</#f39c12> <white>/strengthsmp reload</white> <gray>\u2014 Reload config</gray>");
            this.sendMessage(sender, " <#f39c12>\u2022</#f39c12> <white>/strengthsmp info</white> <gray>\u2014 System status</gray>");
            this.sendMessage(sender, " <#f39c12>\u2022</#f39c12> <white>/strengthsmp setupboard</white> <gray>\u2014 Setup Discord</gray>");
            this.sendMessage(sender, " <#f39c12>\u2022</#f39c12> <white>/strengthsmp resetplayer <player></white>");
            this.sendMessage(sender, " <#f39c12>\u2022</#f39c12> <white>/strengthsmp reroll <player></white>");
            this.sendMessage(sender, " <#f39c12>\u2022</#f39c12> <white>/strengthsmp setstrength <player> <amount></white>");
            this.sendMessage(sender, " <#f39c12>\u2022</#f39c12> <white>/strengthsmp setweapon <player> <weapon></white>");
            this.sendMessage(sender, " <#f39c12>\u2022</#f39c12> <white>/strengthsmp giveitem <player> <strength|reroll></white>");
            this.sendMessage(sender, " <#f39c12>\u2022</#f39c12> <white>/strengthsmp resetmonarch</white> <gray>\u2014 Recalculate monarch</gray>");
            this.sendMessage(sender, " <#f39c12>\u2022</#f39c12> <white>/strengthsmp pack</white> <gray>\u2014 Force resource pack</gray>");
            return true;
        }
        switch (sub = args[0].toLowerCase()) {
            case "maintenance": {
                if (args.length >= 2) {
                    String mode = args[1].toLowerCase();
                    if (mode.equals("on") || mode.equals("enable") || mode.equals("true")) {
                        this.plugin.getDataManager().setSystemEnabled(false);
                        this.sendMessage(sender, "<#e74c3c><b>[Maintenance Mode]</b> Maintenance is now <b>ON</b> (System Disabled & KeepInventory Active).</#e74c3c>");
                    } else if (mode.equals("off") || mode.equals("disable") || mode.equals("false")) {
                        this.plugin.getDataManager().setSystemEnabled(true);
                        this.sendMessage(sender, "<#2ecc71><b>[Maintenance Mode]</b> Maintenance is now <b>OFF</b> (System Enabled).</#2ecc71>");
                    } else {
                        this.sendError(sender, "Usage: /strengthsmp maintenance <on|off>");
                    }
                } else {
                    boolean current = this.plugin.getDataManager().isSystemEnabled();
                    this.plugin.getDataManager().setSystemEnabled(!current);
                    this.sendMessage(sender, "<#2ecc71>Maintenance mode is now " + (!current ? "<#e74c3c><b>ON</b></#e74c3c>" : "<b>OFF</b>"));
                }
                break;
            }
            case "toggle": {
                boolean current = this.plugin.getDataManager().isSystemEnabled();
                this.plugin.getDataManager().setSystemEnabled(!current);
                this.sendMessage(sender, "<#2ecc71>System is now " + (!current ? "<b>ENABLED</b>" : "<#e74c3c><b>DISABLED</b></#e74c3c>"));
                break;
            }
            case "reload": {
                this.plugin.reloadConfig();
                this.plugin.getConfigManager().reload();
                this.sendSuccess(sender, "Configuration reloaded successfully.");
                break;
            }
            case "pack": {
                return this.handlePack(sender);
            }
            case "info": {
                boolean enabled = this.plugin.getDataManager().isSystemEnabled();
                UUID mon = this.plugin.getDataManager().getMonarch();
                String monarchName = "None";
                if (mon != null) {
                    Player monP = Bukkit.getPlayer((UUID)mon);
                    monarchName = monP != null ? monP.getName() : mon.toString();
                }
                this.sendMessage(sender, "<#f1c40f><b>[System Info]</b></#f1c40f>");
                this.sendMessage(sender, " <#95a5a6>Status:</#95a5a6> " + (enabled ? "<#2ecc71>ENABLED</#2ecc71>" : "<#e74c3c>DISABLED</#e74c3c>"));
                this.sendMessage(sender, " <#95a5a6>Monarch:</#95a5a6> <#f1c40f>" + monarchName + "</#f1c40f>");
                this.sendMessage(sender, " <#95a5a6>Online players:</#95a5a6> <white>" + Bukkit.getOnlinePlayers().size() + "</white>");
                break;
            }
            case "reroll": {
                if (args.length < 2) {
                    this.sendError(sender, "Usage: /strengthsmp reroll <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer((String)args[1]);
                if (target == null) {
                    this.sendError(sender, "Player not found!");
                    return true;
                }
                WeaponType newType = WeaponType.getRandomWeapon();
                this.plugin.getDataManager().setWeapon(target.getUniqueId(), newType);
                this.sendSuccess(sender, "Rerolled " + target.getName() + " \u2192 " + newType.getIcon() + " " + newType.getDisplayName());
                target.sendMessage(MessageUtil.parse("<#2ecc71><b>[!]</b></#2ecc71> <gray>An admin rerolled your weapon to</gray> <#f1c40f><b>" + newType.getDisplayName().toUpperCase() + "</b></#f1c40f>"));
                break;
            }
            case "setstrength": {
                if (args.length < 3) {
                    this.sendError(sender, "Usage: /strengthsmp setstrength <player> <amount>");
                    return true;
                }
                Player target = Bukkit.getPlayer((String)args[1]);
                if (target == null) {
                    this.sendError(sender, "Player not found!");
                    return true;
                }
                try {
                    int amount = Integer.parseInt(args[2]);
                    this.plugin.getDataManager().setStrength(target.getUniqueId(), amount);
                    this.plugin.getMonarchService().calculateNewMonarch();
                    this.plugin.updateDisplay(target);
                    this.sendSuccess(sender, "Set " + target.getName() + "'s strength to <#f1c40f>" + amount + "</#f1c40f>");
                }
                catch (NumberFormatException e) {
                    this.sendError(sender, "Invalid amount!");
                }
                break;
            }
            case "setweapon": {
                if (args.length < 3) {
                    this.sendError(sender, "Usage: /strengthsmp setweapon <player> <weapon>");
                    return true;
                }
                Player target = Bukkit.getPlayer((String)args[1]);
                if (target == null) {
                    this.sendError(sender, "Player not found!");
                    return true;
                }
                try {
                    WeaponType type = WeaponType.valueOf(args[2].toUpperCase());
                    this.plugin.getDataManager().setWeapon(target.getUniqueId(), type);
                    this.sendSuccess(sender, "Set " + target.getName() + "'s weapon to <#f1c40f>" + type.getDisplayName() + "</#f1c40f>");
                    target.sendMessage(MessageUtil.parse("<#2ecc71><b>[!]</b></#2ecc71> <gray>Your weapon class was changed to</gray> <#f1c40f><b>" + type.getDisplayName().toUpperCase() + "</b></#f1c40f> <gray>by an admin.</gray>"));
                }
                catch (IllegalArgumentException e) {
                    this.sendError(sender, "Invalid weapon type! Use: SWORD, AXE, BOW, CROSSBOW, SHIELD, TRIDENT");
                }
                break;
            }
            case "resetplayer": {
                if (args.length < 2) {
                    this.sendError(sender, "Usage: /strengthsmp resetplayer <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer((String)args[1]);
                if (target == null) {
                    this.sendError(sender, "Player not found!");
                    return true;
                }
                UUID uid = target.getUniqueId();
                this.plugin.getDataManager().setStrength(uid, this.plugin.getConfigManager().getDefaultStrength());
                this.plugin.getDataManager().setWeapon(uid, WeaponType.getRandomWeapon());
                this.plugin.getMonarchService().calculateNewMonarch();
                this.plugin.updateDisplay(target);
                this.sendSuccess(sender, "Reset stats for <#f1c40f>" + target.getName() + "</#f1c40f>");
                target.sendMessage(MessageUtil.parse("<#e74c3c><b>[!]</b></#e74c3c> <gray>Your stats have been reset by an admin.</gray>"));
                break;
            }
            case "giveitem": {
                if (args.length < 3) {
                    this.sendError(sender, "Usage: /strengthsmp giveitem <player> <strength|reroll|death>");
                    return true;
                }
                Player target = Bukkit.getPlayer((String)args[1]);
                if (target == null) {
                    this.sendError(sender, "Player not found!");
                    return true;
                }
                String typeStr = args[2].toLowerCase();
                if (typeStr.equals("strength")) {
                    target.getInventory().addItem(new ItemStack[]{ItemFactory.createStrengthItem(1)});
                    this.sendSuccess(sender, "Gave a Strength item to <#f1c40f>" + target.getName() + "</#f1c40f>");
                    break;
                }
                if (typeStr.equals("reroll")) {
                    target.getInventory().addItem(new ItemStack[]{ItemFactory.createRerollItem((Plugin)this.plugin, 1)});
                    this.sendSuccess(sender, "Gave a Reroll item to <#f1c40f>" + target.getName() + "</#f1c40f>");
                    break;
                }
                if (typeStr.equals("death") || typeStr.equals("death_certificate") || typeStr.equals("deathcertificate")) {
                    target.getInventory().addItem(new ItemStack[]{ItemFactory.createDeathCertificate((Plugin)this.plugin, "Victim", "Killer")});
                    this.sendSuccess(sender, "Gave a Death Certificate to <#f1c40f>" + target.getName() + "</#f1c40f>");
                    break;
                }
                this.sendError(sender, "Unknown item type. Use: strength, reroll, or death");
                break;
            }
            case "checkitem": {
                ItemStack item;
                Player target = null;
                if (args.length > 1) {
                    target = Bukkit.getPlayer((String)args[1]);
                    if (target == null) {
                        this.sendError(sender, "Player not found!");
                        return true;
                    }
                } else if (sender instanceof Player) {
                    Player player;
                    target = player = (Player)sender;
                } else {
                    this.sendError(sender, "Usage: /strengthsmp checkitem <player>");
                    return true;
                }
                if ((item = target.getInventory().getItemInMainHand()).getType() == Material.AIR) {
                    this.sendError(sender, target.getName() + " is not holding an item!");
                    return true;
                }
                ItemMeta meta = item.getItemMeta();
                int cmdValue = meta != null && meta.hasCustomModelData() ? meta.getCustomModelData() : 0;
                this.sendMessage(sender, "<#2ecc71><b>[Item Check]</b></#2ecc71>");
                this.sendMessage(sender, " <#95a5a6>Player:</#95a5a6> <white>" + target.getName() + "</white>");
                this.sendMessage(sender, " <#95a5a6>Material:</#95a5a6> <white>" + item.getType().name() + "</white>");
                this.sendMessage(sender, " <#95a5a6>CustomModelData:</#95a5a6> <#f1c40f>" + cmdValue + "</#f1c40f>");
                return true;
            }
            case "setupboard": 
            case "setupleaderboard": {
                if (this.plugin.getDiscordManager() == null || !this.plugin.getDiscordManager().isConnected()) {
                    this.sendError(sender, "Discord bot is not connected!");
                    return true;
                }
                this.plugin.getDiscordManager().updateDashboard();
                this.sendSuccess(sender, "Discord board updated.");
                break;
            }
            case "resetmonarch": {
                this.plugin.getMonarchService().calculateNewMonarch();
                this.sendSuccess(sender, "Monarch status has been recalculated.");
                break;
            }
            default: {
                this.sendError(sender, "Unknown subcommand: <#f1c40f>" + sub + "</#f1c40f>. Use <white>/strengthsmp</white> for help.");
            }
        }
        return true;
    }
}

