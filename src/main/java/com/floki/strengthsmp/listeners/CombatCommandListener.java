package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Set;

/**
 * Blocks all commands for players who are in combat,
 * except for a small whitelist of safe commands.
 */
public class CombatCommandListener implements Listener {

    private final StrengthSMP plugin;

    /**
     * Commands that are always allowed even while in combat.
     * Store as lowercase, without the leading slash.
     */
    private static final Set<String> ALLOWED_COMMANDS = Set.of(
        "whisper", "w", "msg", "tell", "message",  // whisper / private message aliases
        "strength", "str", "power"                   // /strength and its aliases
    );

    public CombatCommandListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        // Only care about players in combat
        if (!plugin.getCombatListener().isInCombat(player.getUniqueId())) return;

        // Extract the base command name (first token, no slash, lowercase)
        String message = event.getMessage();
        String baseCmd = message.substring(1).split("\\s+")[0].toLowerCase();

        // Allow whitelisted commands
        if (ALLOWED_COMMANDS.contains(baseCmd)) return;

        // Block everything else
        event.setCancelled(true);
        MessageUtil.send(player, "combat.command-blocked");
    }
}
