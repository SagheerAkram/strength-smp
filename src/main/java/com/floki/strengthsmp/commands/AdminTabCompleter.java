package com.floki.strengthsmp.commands;

import com.floki.strengthsmp.data.WeaponType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AdminTabCompleter implements TabCompleter {
    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "maintenance", "toggle", "reload", "info", "pack", "setupboard",
            "resetplayer", "reroll", "setstrength", "setweapon", "giveitem", "resetmonarch"
    );

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("strengthsmp.admin")) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], SUBCOMMANDS, completions);
            Collections.sort(completions);
            return completions;
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("maintenance") || sub.equals("toggle")) {
                StringUtil.copyPartialMatches(args[1], Arrays.asList("on", "off"), completions);
            } else if (Arrays.asList("resetplayer", "reroll", "setstrength", "setweapon", "giveitem").contains(sub)) {
                List<String> players = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    players.add(p.getName());
                }
                StringUtil.copyPartialMatches(args[1], players, completions);
            }
            Collections.sort(completions);
            return completions;
        }

        if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (sub.equals("setweapon")) {
                List<String> weapons = new ArrayList<>();
                for (WeaponType w : WeaponType.values()) {
                    weapons.add(w.name());
                }
                StringUtil.copyPartialMatches(args[2], weapons, completions);
            } else if (sub.equals("giveitem")) {
                StringUtil.copyPartialMatches(args[2], Arrays.asList("strength", "reroll"), completions);
            } else if (sub.equals("setstrength")) {
                StringUtil.copyPartialMatches(args[2], Arrays.asList("0", "1", "2", "3", "4", "5"), completions);
            }
            Collections.sort(completions);
            return completions;
        }

        return Collections.emptyList();
    }
}
