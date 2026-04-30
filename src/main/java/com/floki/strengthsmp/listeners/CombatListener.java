package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles combat tagging and logout penalties.
 * Players who quit while in combat are killed and penalized.
 */
public class CombatListener implements Listener {
    
    private final StrengthSMP plugin;
    private final Map<UUID, Long> combatTag = new HashMap<>();
    private final Map<UUID, UUID> lastAttacker = new HashMap<>();

    public boolean isInCombat(UUID uuid) {
        Long expiry = combatTag.get(uuid);
        return expiry != null && expiry > System.currentTimeMillis();
    }

    public CombatListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    public UUID getLastAttacker(UUID victim) {
        return lastAttacker.get(victim);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCombat(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        
        Player attacker = null;
        if (event.getDamager() instanceof Player p) {
            attacker = p;
        } else if (event.getDamager() instanceof org.bukkit.entity.Projectile proj && proj.getShooter() instanceof Player p) {
            attacker = p;
        }

        if (attacker == null || attacker.equals(victim)) return;

        long tagDurationMs = plugin.getConfigManager().getCombatTagDuration() * 1000L;
        long now = System.currentTimeMillis();
        combatTag.put(victim.getUniqueId(), now + tagDurationMs);
        combatTag.put(attacker.getUniqueId(), now + tagDurationMs);
        lastAttacker.put(victim.getUniqueId(), attacker.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        Long tagExpiry = combatTag.get(uuid);
        if (tagExpiry != null && tagExpiry > System.currentTimeMillis()) {
            // Combat log detected!
            UUID attackerUUID = lastAttacker.get(uuid);
            Player attacker = (attackerUUID != null) ? Bukkit.getPlayer(attackerUUID) : null;
            
            String attackerName = (attacker != null) ? attacker.getName() : "Unknown";
            MessageUtil.broadcast("combat.penalty", "player", player.getName(), "killer", attackerName);

            // PENALTY: Remove 1 strength instead of killing (keeps inventory safe)
            plugin.getStrengthService().removeStrength(player, 1);
            
            // Drop physical item at logout location
            player.getWorld().dropItemNaturally(player.getLocation(), com.floki.strengthsmp.util.ItemFactory.createStrengthItem(1));
            
            // Record the "kill" for the attacker if they exist, but don't transfer items
            if (attackerUUID != null) {
                plugin.getDataManager().recordKill(attackerUUID, uuid);
                plugin.getDataManager().addKill(attackerUUID);
            }
        }
        
        combatTag.remove(uuid);
        lastAttacker.remove(uuid);
    }
}
