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
import org.bukkit.event.entity.PlayerDeathEvent;
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
            if (!this.penaltyAllowed(uuid, attackerUUID)) {
                this.plugin.getLogger().info(player.getName() + " logged out in combat: no penalty (protection or anti-farm).");
                this.combatTag.remove(uuid);
                this.lastAttacker.remove(uuid);
                return;
            }
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

    /** Same rules as a real kill, so logging out can't be used to feed strength to an alt. */
    private boolean penaltyAllowed(UUID victim, UUID attacker) {
        com.floki.strengthsmp.data.DataManager dm = this.plugin.getDataManager();
        if (dm.isNewbieProtected(victim) || dm.hasDeathProtection(victim)) {
            return false;
        }
        if (attacker != null) {
            if (dm.isNewbieProtected(attacker) || dm.hasDeathProtection(attacker)) {
                return false;
            }
            long lastKill = dm.getLastKillTime(attacker, victim);
            long windowMs = (long)(this.plugin.getConfigManager().getAntiFarmWindowMinutes() * 60) * 1000L;
            if (lastKill > 0L && System.currentTimeMillis() - lastKill < windowMs) {
                return false;
            }
        }
        return true;
    }

    /** Dying ends combat, so quitting on the respawn screen isn't punished a second time. */
    @EventHandler(priority=EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        UUID uuid = event.getEntity().getUniqueId();
        this.combatTag.remove(uuid);
        this.lastAttacker.remove(uuid);
    }
}

