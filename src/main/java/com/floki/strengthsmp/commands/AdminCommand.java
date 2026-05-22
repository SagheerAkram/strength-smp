package com.floki.strengthsmp.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.floki.strengthsmp.StrengthSMP;
import org.bukkit.command.CommandSender;
import com.floki.strengthsmp.util.MessageUtil;
import com.floki.strengthsmp.util.ItemFactory;
import com.floki.strengthsmp.data.WeaponType;

public class AdminCommand extends BaseCommand {

    public AdminCommand(StrengthSMP plugin) {
        super(plugin);
        plugin.getCommand("strengthsmp").setExecutor(this);
    }

    private boolean handlePack(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        
        Player player = (Player) sender;
        plugin.getLogger().info("🛠️ Manual pack request from " + player.getName());
        
        // Directly trigger the join logic but immediately
        plugin.getServer().getPluginManager().callEvent(new org.bukkit.event.player.PlayerJoinEvent(player, null));
        sender.sendMessage(MessageUtil.parse("<#2ecc71>Attempting to send resource pack... Check console for debug info.</#2ecc71>"));
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (!sender.hasPermission("strengthsmp.admin")) {
            sendError(sender, "You don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            sendMessage(sender, "<#f1c40f><b>[StrengthSMP Admin]</b></#f1c40f>");
            sendMessage(sender, " <#f39c12>•</#f39c12> <white>/strengthsmp toggle</white> <gray>— Toggle system</gray>");
            sendMessage(sender, " <#f39c12>•</#f39c12> <white>/strengthsmp reload</white> <gray>— Reload config</gray>");
            sendMessage(sender, " <#f39c12>•</#f39c12> <white>/strengthsmp info</white> <gray>— System status</gray>");
            sendMessage(sender, " <#f39c12>•</#f39c12> <white>/strengthsmp setupboard</white> <gray>— Setup Discord</gray>");
            sendMessage(sender, " <#f39c12>•</#f39c12> <white>/strengthsmp resetplayer <player></white>");
            sendMessage(sender, " <#f39c12>•</#f39c12> <white>/strengthsmp reroll <player></white>");
            sendMessage(sender, " <#f39c12>•</#f39c12> <white>/strengthsmp setstrength <player> <amount></white>");
            sendMessage(sender, " <#f39c12>•</#f39c12> <white>/strengthsmp setweapon <player> <weapon></white>");
            sendMessage(sender, " <#f39c12>•</#f39c12> <white>/strengthsmp giveitem <player> <strength|reroll></white>");
            sendMessage(sender, " <#f39c12>•</#f39c12> <white>/strengthsmp resetmonarch</white> <gray>— Recalculate monarch</gray>");
            sendMessage(sender, " <#f39c12>•</#f39c12> <white>/strengthsmp pack</white> <gray>— Force resource pack</gray>");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {

            case "toggle": {
                boolean current = plugin.getDataManager().isSystemEnabled();
                plugin.getDataManager().setSystemEnabled(!current);
                sendMessage(sender, "<#2ecc71>System is now " + (!current ? "<b>ENABLED</b>" : "<#e74c3c><b>DISABLED</b></#e74c3c>"));
                break;
            }

            case "reload": {
                plugin.reloadConfig();
                plugin.getConfigManager().reload();
                sendSuccess(sender, "Configuration reloaded successfully.");
                break;
            }

            case "pack": {
                return handlePack(sender);
            }

            case "info": {
                boolean enabled   = plugin.getDataManager().isSystemEnabled();
                java.util.UUID mon = plugin.getDataManager().getMonarch();
                String monarchName = "None";
                if (mon != null) {
                    Player monP = Bukkit.getPlayer(mon);
                    monarchName = monP != null ? monP.getName() : mon.toString();
                }
                sendMessage(sender, "<#f1c40f><b>[System Info]</b></#f1c40f>");
                sendMessage(sender, " <#95a5a6>Status:</#95a5a6> " + (enabled ? "<#2ecc71>ENABLED</#2ecc71>" : "<#e74c3c>DISABLED</#e74c3c>"));
                sendMessage(sender, " <#95a5a6>Monarch:</#95a5a6> <#f1c40f>" + monarchName + "</#f1c40f>");
                sendMessage(sender, " <#95a5a6>Online players:</#95a5a6> <white>" + Bukkit.getOnlinePlayers().size() + "</white>");
                break;
            }

            case "reroll": {
                if (args.length < 2) { sendError(sender, "Usage: /strengthsmp reroll <player>"); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { sendError(sender, "Player not found!"); return true; }
                WeaponType newType = WeaponType.getRandomWeapon();
                plugin.getDataManager().setWeapon(target.getUniqueId(), newType);
                sendSuccess(sender, "Rerolled " + target.getName() + " → " + newType.getIcon() + " " + newType.getDisplayName());
                target.sendMessage(MessageUtil.parse("<#2ecc71><b>[!]</b></#2ecc71> <gray>An admin rerolled your weapon to</gray> <#f1c40f><b>" + newType.getDisplayName().toUpperCase() + "</b></#f1c40f>"));
                break;
            }

            case "setstrength": {
                if (args.length < 3) { sendError(sender, "Usage: /strengthsmp setstrength <player> <amount>"); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { sendError(sender, "Player not found!"); return true; }
                try {
                    int amount = Integer.parseInt(args[2]);
                    plugin.getDataManager().setStrength(target.getUniqueId(), amount);
                    plugin.getMonarchService().calculateNewMonarch();
                    plugin.updateDisplay(target);
                    sendSuccess(sender, "Set " + target.getName() + "'s strength to <#f1c40f>" + amount + "</#f1c40f>");
                } catch (NumberFormatException e) {
                    sendError(sender, "Invalid amount!");
                }
                break;
            }

            case "setweapon": {
                if (args.length < 3) { sendError(sender, "Usage: /strengthsmp setweapon <player> <weapon>"); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { sendError(sender, "Player not found!"); return true; }
                try {
                    WeaponType type = WeaponType.valueOf(args[2].toUpperCase());
                    plugin.getDataManager().setWeapon(target.getUniqueId(), type);
                    sendSuccess(sender, "Set " + target.getName() + "'s weapon to <#f1c40f>" + type.getDisplayName() + "</#f1c40f>");
                    target.sendMessage(MessageUtil.parse("<#2ecc71><b>[!]</b></#2ecc71> <gray>Your weapon class was changed to</gray> <#f1c40f><b>" + type.getDisplayName().toUpperCase() + "</b></#f1c40f> <gray>by an admin.</gray>"));
                } catch (IllegalArgumentException e) {
                    sendError(sender, "Invalid weapon type! Use: SWORD, AXE, BOW, CROSSBOW, SHIELD, TRIDENT");
                }
                break;
            }

            case "resetplayer": {
                if (args.length < 2) { sendError(sender, "Usage: /strengthsmp resetplayer <player>"); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { sendError(sender, "Player not found!"); return true; }
                java.util.UUID uid = target.getUniqueId();
                plugin.getDataManager().setStrength(uid, plugin.getConfigManager().getDefaultStrength());
                plugin.getDataManager().setWeapon(uid, WeaponType.getRandomWeapon());
                plugin.getMonarchService().calculateNewMonarch();
                plugin.updateDisplay(target);
                sendSuccess(sender, "Reset stats for <#f1c40f>" + target.getName() + "</#f1c40f>");
                target.sendMessage(MessageUtil.parse("<#e74c3c><b>[!]</b></#e74c3c> <gray>Your stats have been reset by an admin.</gray>"));
                break;
            }

            case "giveitem": {
                if (args.length < 3) {
                    sendError(sender, "Usage: /strengthsmp giveitem <player> <strength|reroll|death>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { sendError(sender, "Player not found!"); return true; }
                String typeStr = args[2].toLowerCase();
                if (typeStr.equals("strength")) {
                    target.getInventory().addItem(ItemFactory.createStrengthItem(1));
                    sendSuccess(sender, "Gave a Strength item to <#f1c40f>" + target.getName() + "</#f1c40f>");
                } else if (typeStr.equals("reroll")) {
                    target.getInventory().addItem(ItemFactory.createRerollItem(plugin, 1));
                    sendSuccess(sender, "Gave a Reroll item to <#f1c40f>" + target.getName() + "</#f1c40f>");
                } else if (typeStr.equals("death") || typeStr.equals("death_certificate") || typeStr.equals("deathcertificate")) {
                    target.getInventory().addItem(ItemFactory.createDeathCertificate(plugin, "Victim", "Killer"));
                    sendSuccess(sender, "Gave a Death Certificate to <#f1c40f>" + target.getName() + "</#f1c40f>");
                } else {
                    sendError(sender, "Unknown item type. Use: strength, reroll, or death");
                }
                break;
            }

            case "checkitem": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can use this command.");
                    return true;
                }
                org.bukkit.inventory.ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() == org.bukkit.Material.AIR) {
                    sendError(sender, "Hold an item in your hand!");
                    return true;
                }
                org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
                int cmdValue = (meta != null && meta.hasCustomModelData()) ? meta.getCustomModelData() : 0;
                sendMessage(player, "<#2ecc71><b>[Item Check]</b></#2ecc71>");
                sendMessage(player, " <#95a5a6>Material:</#95a5a6> <white>" + item.getType().name() + "</white>");
                sendMessage(player, " <#95a5a6>CustomModelData:</#95a5a6> <#f1c40f>" + cmdValue + "</#f1c40f>");
                return true;
            }

            case "setupboard":
            case "setupleaderboard": {
                if (plugin.getDiscordManager() == null || !plugin.getDiscordManager().isConnected()) {
                    sendError(sender, "Discord bot is not connected!");
                    return true;
                }
                plugin.getDiscordManager().updateDashboard();
                sendSuccess(sender, "Discord board updated.");
                break;
            }

            case "resetmonarch": {
                plugin.getMonarchService().calculateNewMonarch();
                sendSuccess(sender, "Monarch status has been recalculated.");
                break;
            }

            default:
                sendError(sender, "Unknown subcommand: <#f1c40f>" + sub + "</#f1c40f>. Use <white>/strengthsmp</white> for help.");
        }

        return true;
    }
}
