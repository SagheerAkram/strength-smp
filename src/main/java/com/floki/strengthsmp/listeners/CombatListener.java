/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.Projectile
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 */
package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.util.ItemFactory;
import com.floki.strengthsmp.util.MessageUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CombatListener
implements Listener {
    private final StrengthSMP plugin;
    private final Map<UUID, Long> combatTag = new HashMap<UUID, Long>();
    private final Map<UUID, UUID> lastAttacker = new HashMap<UUID, UUID>();

    public boolean isInCombat(UUID uuid) {
        Long expiry = this.combatTag.get(uuid);
        return expiry != null && expiry > System.currentTimeMillis();
    }

    public CombatListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    public UUID getLastAttacker(UUID victim) {
        return this.lastAttacker.get(victim);
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onCombat(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player victim = (Player)entity;
        Player attacker = null;
        Entity damager = event.getDamager();
        if (damager instanceof Player) {
            attacker = (Player)damager;
        } else if (damager instanceof Projectile) {
            Projectile proj = (Projectile)damager;
            if (proj.getShooter() instanceof Player) {
                attacker = (Player)proj.getShooter();
            }
        }
        if (attacker == null || attacker.equals((Object)victim)) {
            return;
        }
        long tagDurationMs = (long)this.plugin.getConfigManager().getCombatTagDuration() * 1000L;
        long now = System.currentTimeMillis();
        this.combatTag.put(victim.getUniqueId(), now + tagDurationMs);
        this.combatTag.put(attacker.getUniqueId(), now + tagDurationMs);
        this.lastAttacker.put(victim.getUniqueId(), attacker.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Long tagExpiry = this.combatTag.get(uuid);
        if (tagExpiry != null && tagExpiry > System.currentTimeMillis()) {
            UUID attackerUUID = this.lastAttacker.get(uuid);
            Player attacker = attackerUUID != null ? Bukkit.getPlayer((UUID)attackerUUID) : null;
            String attackerName = attacker != null ? attacker.getName() : "Unknown";
            MessageUtil.broadcast("combat.penalty", "player", player.getName(), "killer", attackerName);
            boolean removed = this.plugin.getStrengthService().removeStrength(player, 1);
            if (removed) {
                player.getWorld().dropItemNaturally(player.getLocation(), ItemFactory.createStrengthItem(1));
            }
            if (attackerUUID != null) {
                this.plugin.getDataManager().recordKill(attackerUUID, uuid);
                this.plugin.getDataManager().addKill(attackerUUID);
            }
        }
        this.combatTag.remove(uuid);
        this.lastAttacker.remove(uuid);
    }
}

