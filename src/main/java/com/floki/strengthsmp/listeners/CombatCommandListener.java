/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerCommandPreprocessEvent
 */
package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.util.MessageUtil;
import java.util.Set;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CombatCommandListener
implements Listener {
    private final StrengthSMP plugin;
    private static final Set<String> ALLOWED_COMMANDS = Set.of("whisper", "w", "msg", "tell", "message", "strength", "str", "power", "mimictest", "identity", "mimic");

    public CombatCommandListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!this.plugin.getCombatListener().isInCombat(player.getUniqueId())) {
            return;
        }
        String message = event.getMessage();
        String baseCmd = message.substring(1).split("\\s+")[0].toLowerCase();
        if (ALLOWED_COMMANDS.contains(baseCmd)) {
            return;
        }
        event.setCancelled(true);
        MessageUtil.send((CommandSender)player, "combat.command-blocked", new String[0]);
    }
}

