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
import org.bukkit.OfflinePlayer;
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

    /** Resolve a localised admin string, falling back to the built-in English text. */
    private String msg(String key, String fallback, String ... placeholders) {
        return MessageUtil.get("commands.admin." + key, fallback, placeholders);
    }

    /** Send a localised admin string verbatim (no [ERROR]/[SUCCESS] decoration). */
    private void say(CommandSender sender, String key, String fallback, String ... placeholders) {
        sender.sendMessage(this.msg(key, fallback, placeholders));
    }

    private void handleHollow(CommandSender sender, String[] args) {
        com.floki.strengthsmp.api.HollowHook hollow = this.plugin.getHollowHook();
        if (hollow == null) {
            this.sendMessage(sender, StrengthSMP.PRO_PITCH_SHORT);
            return;
        }
        hollow.handleAdmin(sender, args);
    }

    private boolean handlePack(CommandSender sender) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendWithDefault(sender, "only-players", "&c&l✖ &c&lPLAYERS ONLY! &7This command can only be run in-game.");
            return true;
        }
        Player player = (Player)sender;
        this.plugin.getLogger().info("🛠️ Manual pack request from " + player.getName());
        this.plugin.getResourcePackListener().sendPack(player);
        this.say(sender, "pack-sending", "<#2ecc71>Attempting to send resource pack... Check console for debug info.</#2ecc71>");
        return true;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String sub;
        if (!sender.hasPermission("strengthsmp.admin")) {
            MessageUtil.sendWithDefault(sender, "no-permission", "&c&l✖ &c&lNO PERMISSION! &7You do not have access to this command.");
            return true;
        }
        if (args.length == 0) {
            this.say(sender, "help-header", "<#f1c40f><b>[StrengthSMP Admin]</b></#f1c40f>");
            this.say(sender, "help-maintenance", " <#f39c12>•</#f39c12> <white>/strengthsmp maintenance <on|off></white> <gray>— Toggle maintenance mode</gray>");
            this.say(sender, "help-toggle", " <#f39c12>•</#f39c12> <white>/strengthsmp toggle</white> <gray>— Quick toggle system</gray>");
            this.say(sender, "help-reload", " <#f39c12>•</#f39c12> <white>/strengthsmp reload</white> <gray>— Reload config</gray>");
            this.say(sender, "help-lang", " <#f39c12>•</#f39c12> <white>/strengthsmp lang <en|ru|es|de|fr|pt|hi|ur|he></white> <gray>— Switch plugin language</gray>");
            this.say(sender, "help-info", " <#f39c12>•</#f39c12> <white>/strengthsmp info</white> <gray>— System status</gray>");
            this.say(sender, "help-setupboard", " <#f39c12>•</#f39c12> <white>/strengthsmp setupboard</white> <gray>— Setup Discord</gray>");
            this.say(sender, "help-resetplayer", " <#f39c12>•</#f39c12> <white>/strengthsmp resetplayer <player></white>");
            this.say(sender, "help-reroll", " <#f39c12>•</#f39c12> <white>/strengthsmp reroll <player></white>");
            this.say(sender, "help-setstrength", " <#f39c12>•</#f39c12> <white>/strengthsmp setstrength <player> <amount></white>");
            this.say(sender, "help-setweapon", " <#f39c12>•</#f39c12> <white>/strengthsmp setweapon <player> <weapon></white>");
            this.say(sender, "help-giveitem", " <#f39c12>•</#f39c12> <white>/strengthsmp giveitem <player> <strength|reroll></white>");
            this.say(sender, "help-resetmonarch", " <#f39c12>•</#f39c12> <white>/strengthsmp resetmonarch</white> <gray>— Recalculate monarch</gray>");
            this.say(sender, "help-pack", " <#f39c12>•</#f39c12> <white>/strengthsmp pack</white> <gray>— Force resource pack</gray>");
            this.say(sender, "help-hollow", " <#f39c12>•</#f39c12> <white>/strengthsmp hollow <send|release|status|list|visit|complete|shade|shiftmaze|rebuild></white> <gray>— Manage the Hollow</gray>");
            return true;
        }
        switch (sub = args[0].toLowerCase()) {
            case "maintenance": {
                if (args.length >= 2) {
                    String mode = args[1].toLowerCase();
                    if (mode.equals("on") || mode.equals("enable") || mode.equals("true")) {
                        this.plugin.getDataManager().setSystemEnabled(false);
                        this.say(sender, "maintenance-on", "<#e74c3c><b>[Maintenance Mode]</b> Maintenance is now <b>ON</b> (System Disabled & KeepInventory Active).</#e74c3c>");
                    } else if (mode.equals("off") || mode.equals("disable") || mode.equals("false")) {
                        this.plugin.getDataManager().setSystemEnabled(true);
                        this.say(sender, "maintenance-off", "<#2ecc71><b>[Maintenance Mode]</b> Maintenance is now <b>OFF</b> (System Enabled).</#2ecc71>");
                    } else {
                        this.sendError(sender, this.msg("maintenance-usage", "Usage: /strengthsmp maintenance <on|off>"));
                    }
                } else {
                    boolean current = this.plugin.getDataManager().isSystemEnabled();
                    this.plugin.getDataManager().setSystemEnabled(!current);
                    String state = !current
                            ? this.msg("state-on", "<#e74c3c><b>ON</b></#e74c3c>")
                            : this.msg("state-off", "<b>OFF</b>");
                    this.say(sender, "maintenance-toggled", "<#2ecc71>Maintenance mode is now %state%", "state", state);
                }
                break;
            }
            case "toggle": {
                boolean current = this.plugin.getDataManager().isSystemEnabled();
                this.plugin.getDataManager().setSystemEnabled(!current);
                String state = !current
                        ? this.msg("state-enabled", "<b>ENABLED</b>")
                        : this.msg("state-disabled", "<#e74c3c><b>DISABLED</b></#e74c3c>");
                this.say(sender, "system-toggled", "<#2ecc71>System is now %state%", "state", state);
                break;
            }
            case "reload": {
                this.plugin.reloadConfig();
                this.plugin.getConfigManager().reloadAll();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    this.plugin.updateDisplay(p);
                }
                this.sendSuccess(sender, this.msg("reload-success", "Configuration reloaded successfully."));
                break;
            }
            case "lang":
            case "language": {
                if (args.length >= 2) {
                    String newLang = args[1].toLowerCase();
                    this.plugin.getConfigManager().getConfig().set("language", newLang);
                    this.plugin.getConfigManager().saveConfig();
                    this.plugin.getConfigManager().reloadAll();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        this.plugin.updateDisplay(p);
                    }
                    MessageUtil.sendWithDefault(sender, "commands.language-changed", "<#2ecc71><b>[Language]</b></#2ecc71> <gray>Language changed to</gray> <white>%lang%</white><gray>!</gray>", "lang", newLang);
                } else {
                    String currentLang = this.plugin.getConfigManager().getConfig().getString("language", "en");
                    this.say(sender, "lang-current", "<#f1c40f><b>[Language]</b> Current language is: <b>%lang%</b>. Usage: /strengthsmp lang <en|ru|es|de|fr|pt|hi|ur|he></#f1c40f>", "lang", currentLang);
                }
                break;
            }
            case "pack": {
                return this.handlePack(sender);
            }
            case "info": {
                boolean enabled = this.plugin.getDataManager().isSystemEnabled();
                UUID mon = this.plugin.getDataManager().getMonarch();
                String monarchName = this.msg("info-monarch-none", "None");
                if (mon != null) {
                    Player monP = Bukkit.getPlayer((UUID)mon);
                    monarchName = monP != null ? monP.getName() : mon.toString();
                }
                String status = enabled
                        ? this.msg("info-status-enabled", "<#2ecc71>ENABLED</#2ecc71>")
                        : this.msg("info-status-disabled", "<#e74c3c>DISABLED</#e74c3c>");
                this.say(sender, "info-header", "<#f1c40f><b>[System Info]</b></#f1c40f>");
                this.say(sender, "info-status", " <#95a5a6>Status:</#95a5a6> %status%", "status", status);
                this.say(sender, "info-monarch", " <#95a5a6>Monarch:</#95a5a6> <#f1c40f>%monarch%</#f1c40f>", "monarch", monarchName);
                this.say(sender, "info-online", " <#95a5a6>Online players:</#95a5a6> <white>%count%</white>", "count", String.valueOf(Bukkit.getOnlinePlayers().size()));
                break;
            }
            case "reroll": {
                if (args.length < 2) {
                    this.sendError(sender, this.msg("reroll-usage", "Usage: /strengthsmp reroll <player>"));
                    return true;
                }
                Player target = Bukkit.getPlayer((String)args[1]);
                if (target == null) {
                    MessageUtil.sendWithDefault(sender, "player-not-found", "&c&l✖ &c&lPLAYER NOT FOUND! &7The specified player is not online.");
                    return true;
                }
                WeaponType newType = WeaponType.getRandomWeapon();
                this.plugin.getDataManager().setWeapon(target.getUniqueId(), newType);
                this.sendSuccess(sender, this.msg("reroll-success", "Rerolled %player% → %icon% %weapon%",
                        "player", target.getName(), "icon", newType.getIcon(), "weapon", newType.getDisplayName()));
                this.say(target, "reroll-target", "<#2ecc71><b>[!]</b></#2ecc71> <gray>An admin rerolled your weapon to</gray> <#f1c40f><b>%weapon%</b></#f1c40f>",
                        "weapon", newType.getDisplayName().toUpperCase());
                break;
            }
            case "setstrength": {
                if (args.length < 3) {
                    this.sendError(sender, this.msg("setstrength-usage", "Usage: /strengthsmp setstrength <player> <amount>"));
                    return true;
                }
                Player target = Bukkit.getPlayer((String)args[1]);
                if (target == null) {
                    MessageUtil.sendWithDefault(sender, "player-not-found", "&c&l✖ &c&lPLAYER NOT FOUND! &7The specified player is not online.");
                    return true;
                }
                try {
                    int amount = Integer.parseInt(args[2]);
                    this.plugin.getDataManager().setStrength(target.getUniqueId(), amount);
                    this.plugin.getMonarchService().calculateNewMonarch();
                    this.plugin.updateDisplay(target);
                    this.sendSuccess(sender, this.msg("setstrength-success", "Set %player%'s strength to <#f1c40f>%amount%</#f1c40f>",
                            "player", target.getName(), "amount", String.valueOf(amount)));
                }
                catch (NumberFormatException e) {
                    MessageUtil.sendWithDefault(sender, "invalid-amount", "&c&l✖ &c&lINVALID AMOUNT! &7Please specify a valid positive number.");
                }
                break;
            }
            case "setweapon": {
                if (args.length < 3) {
                    this.sendError(sender, this.msg("setweapon-usage", "Usage: /strengthsmp setweapon <player> <weapon>"));
                    return true;
                }
                Player target = Bukkit.getPlayer((String)args[1]);
                if (target == null) {
                    MessageUtil.sendWithDefault(sender, "player-not-found", "&c&l✖ &c&lPLAYER NOT FOUND! &7The specified player is not online.");
                    return true;
                }
                try {
                    WeaponType type = WeaponType.valueOf(args[2].toUpperCase());
                    this.plugin.getDataManager().setWeapon(target.getUniqueId(), type);
                    this.sendSuccess(sender, this.msg("setweapon-success", "Set %player%'s weapon to <#f1c40f>%weapon%</#f1c40f>",
                            "player", target.getName(), "weapon", type.getDisplayName()));
                    this.say(target, "setweapon-target", "<#2ecc71><b>[!]</b></#2ecc71> <gray>Your weapon class was changed to</gray> <#f1c40f><b>%weapon%</b></#f1c40f> <gray>by an admin.</gray>",
                            "weapon", type.getDisplayName().toUpperCase());
                }
                catch (IllegalArgumentException e) {
                    this.sendError(sender, this.msg("setweapon-invalid", "Invalid weapon type! Use: SWORD, AXE, BOW, CROSSBOW, SHIELD, TRIDENT"));
                }
                break;
            }
            case "resetplayer": {
                if (args.length < 2) {
                    this.sendError(sender, this.msg("resetplayer-usage", "Usage: /strengthsmp resetplayer <player>"));
                    return true;
                }
                Player target = Bukkit.getPlayer((String)args[1]);
                if (target == null) {
                    MessageUtil.sendWithDefault(sender, "player-not-found", "&c&l✖ &c&lPLAYER NOT FOUND! &7The specified player is not online.");
                    return true;
                }
                UUID uid = target.getUniqueId();
                this.plugin.getDataManager().setStrength(uid, this.plugin.getConfigManager().getDefaultStrength());
                this.plugin.getDataManager().setWeapon(uid, WeaponType.getRandomWeapon());
                this.plugin.getMonarchService().calculateNewMonarch();
                this.plugin.updateDisplay(target);
                this.sendSuccess(sender, this.msg("resetplayer-success", "Reset stats for <#f1c40f>%player%</#f1c40f>", "player", target.getName()));
                this.say(target, "resetplayer-target", "<#e74c3c><b>[!]</b></#e74c3c> <gray>Your stats have been reset by an admin.</gray>");
                break;
            }
            case "giveitem": {
                if (args.length < 3) {
                    this.sendError(sender, this.msg("giveitem-usage", "Usage: /strengthsmp giveitem <player> <strength|reroll|death>"));
                    return true;
                }
                Player target = Bukkit.getPlayer((String)args[1]);
                if (target == null) {
                    MessageUtil.sendWithDefault(sender, "player-not-found", "&c&l✖ &c&lPLAYER NOT FOUND! &7The specified player is not online.");
                    return true;
                }
                String typeStr = args[2].toLowerCase();
                if (typeStr.equals("strength")) {
                    target.getInventory().addItem(new ItemStack[]{ItemFactory.createStrengthItem(1)});
                    this.sendSuccess(sender, this.msg("giveitem-strength", "Gave a Strength item to <#f1c40f>%player%</#f1c40f>", "player", target.getName()));
                    break;
                }
                if (typeStr.equals("reroll")) {
                    target.getInventory().addItem(new ItemStack[]{ItemFactory.createRerollItem((Plugin)this.plugin, 1)});
                    this.sendSuccess(sender, this.msg("giveitem-reroll", "Gave a Reroll item to <#f1c40f>%player%</#f1c40f>", "player", target.getName()));
                    break;
                }
                if (typeStr.equals("death") || typeStr.equals("death_certificate") || typeStr.equals("deathcertificate")) {
                    target.getInventory().addItem(new ItemStack[]{ItemFactory.createDeathCertificate((Plugin)this.plugin, "Victim", "Killer")});
                    this.sendSuccess(sender, this.msg("giveitem-death", "Gave a Death Certificate to <#f1c40f>%player%</#f1c40f>", "player", target.getName()));
                    break;
                }
                this.sendError(sender, this.msg("giveitem-unknown", "Unknown item type. Use: strength, reroll, or death"));
                break;
            }
            case "checkitem": {
                ItemStack item;
                Player target = null;
                if (args.length > 1) {
                    target = Bukkit.getPlayer((String)args[1]);
                    if (target == null) {
                        MessageUtil.sendWithDefault(sender, "player-not-found", "&c&l✖ &c&lPLAYER NOT FOUND! &7The specified player is not online.");
                        return true;
                    }
                } else if (sender instanceof Player) {
                    Player player;
                    target = player = (Player)sender;
                } else {
                    this.sendError(sender, this.msg("checkitem-usage", "Usage: /strengthsmp checkitem <player>"));
                    return true;
                }
                if ((item = target.getInventory().getItemInMainHand()).getType() == Material.AIR) {
                    this.sendError(sender, this.msg("checkitem-not-holding", "%player% is not holding an item!", "player", target.getName()));
                    return true;
                }
                ItemMeta meta = item.getItemMeta();
                int cmdValue = meta != null && meta.hasCustomModelData() ? meta.getCustomModelData() : 0;
                this.say(sender, "checkitem-header", "<#2ecc71><b>[Item Check]</b></#2ecc71>");
                this.say(sender, "checkitem-player", " <#95a5a6>Player:</#95a5a6> <white>%player%</white>", "player", target.getName());
                this.say(sender, "checkitem-material", " <#95a5a6>Material:</#95a5a6> <white>%material%</white>", "material", item.getType().name());
                this.say(sender, "checkitem-cmd", " <#95a5a6>CustomModelData:</#95a5a6> <#f1c40f>%value%</#f1c40f>", "value", String.valueOf(cmdValue));
                return true;
            }
            case "setupboard":
            case "setupleaderboard": {
                if (this.plugin.getDiscordManager() == null || !this.plugin.getDiscordManager().isConnected()) {
                    this.sendError(sender, this.msg("board-not-connected", "Discord bot is not connected!"));
                    return true;
                }
                this.plugin.getDiscordManager().updateDashboard();
                this.sendSuccess(sender, this.msg("board-success", "Discord board updated."));
                break;
            }
            case "event": {
                if (args.length < 2) {
                    this.sendError(sender, "Usage: /strengthsmp event <bloodmoon|rift> <start|stop> [minutes]");
                    return true;
                }
                String eventType = args[1].toLowerCase();
                if (eventType.equals("bloodmoon")) {
                    if (args.length >= 3 && args[2].equalsIgnoreCase("stop")) {
                        this.plugin.getRiftService().stopBloodMoon();
                        this.sendSuccess(sender, "Stopped the Blood Moon event.");
                    } else {
                        int mins = 15;
                        if (args.length >= 4) {
                            try { mins = Integer.parseInt(args[3]); } catch (Exception ignored) {}
                        }
                        this.plugin.getRiftService().startBloodMoon(mins);
                        this.sendSuccess(sender, "Started the Blood Moon event for " + mins + " minutes!");
                    }
                } else if (eventType.equals("rift")) {
                    this.plugin.getRiftService().spawnRift();
                    this.sendSuccess(sender, "Spawned a Strength Rift event!");
                } else {
                    this.sendError(sender, "Unknown event type. Use: bloodmoon or rift");
                }
                break;
            }
            case "resetmonarch": {
                this.plugin.getMonarchService().calculateNewMonarch();
                this.sendSuccess(sender, this.msg("resetmonarch-success", "Monarch status has been recalculated."));
                break;
            }
            case "setmonarch": {
                if (args.length < 2) {
                    this.sendError(sender, "Usage: /strengthsmp setmonarch <player>");
                    break;
                }
                Player target = Bukkit.getPlayer(args[1]);
                OfflinePlayer offTarget = target != null ? target : Bukkit.getOfflinePlayer(args[1]);
                if (offTarget == null || (!offTarget.hasPlayedBefore() && !offTarget.isOnline())) {
                    this.sendError(sender, "Player '" + args[1] + "' has never joined the server!");
                    break;
                }
                this.plugin.getMonarchService().setMonarchDirectly(offTarget.getUniqueId());
                this.sendSuccess(sender, "Crowned " + offTarget.getName() + " as the new Monarch!");
                break;
            }
            case "resettracker": {
                if (args.length < 2) {
                    this.sendError(sender, "Usage: /strengthsmp resettracker <player>");
                    break;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    this.sendError(sender, "Player '" + args[1] + "' is not online!");
                    break;
                }
                if (this.plugin.getTrackingService() != null) {
                    this.plugin.getTrackingService().resetCooldown(target.getUniqueId());
                    this.sendSuccess(sender, "Reset radar tracking cooldown for " + target.getName() + "!");
                }
                break;
            }
            case "hollow": {
                this.handleHollow(sender, args);
                break;
            }
            default: {
                this.sendError(sender, this.msg("unknown-subcommand", "Unknown subcommand: <#f1c40f>%sub%</#f1c40f>. Use <white>/strengthsmp</white> for help.", "sub", sub));
            }
        }
        return true;
    }
}
