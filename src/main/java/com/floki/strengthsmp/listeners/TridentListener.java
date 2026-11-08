/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.Projectile
 *  org.bukkit.entity.Trident
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.entity.ProjectileLaunchEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.projectiles.ProjectileSource
 *  org.bukkit.scheduler.BukkitRunnable
 *  org.bukkit.util.Vector
 */
package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.WeaponType;
import com.floki.strengthsmp.util.CompatUtil;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class TridentListener
implements Listener {
    private final StrengthSMP plugin;
    private static final long RIPTIDE_COOLDOWN = 15000L;

    public TridentListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
    public void onTridentThrow(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        if (!(projectile instanceof Trident)) {
            return;
        }
        Trident trident = (Trident)projectile;
        ProjectileSource projectileSource = trident.getShooter();
        if (!(projectileSource instanceof Player)) {
            return;
        }
        Player player = (Player)projectileSource;
        event.setCancelled(true);
        MessageUtil.sendWithDefault(player, "abilities.trident-no-throw", "§c§lYou cannot throw tridents! §7Use it as a melee weapon.");
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 1.0f);
    }

    @EventHandler
    public void onTridentInteract(PlayerInteractEvent event) {
        long lastUse;
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (this.plugin.getDataManager().getWeaponType(player.getUniqueId()) != WeaponType.TRIDENT) {
            return;
        }
        if (player.getInventory().getItemInMainHand().getType() != Material.TRIDENT) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - (lastUse = this.plugin.getDataManager().getAbilityCooldown(player.getUniqueId(), "trident_riptide")) < 15000L) {
            long remaining = (15000L - (now - lastUse)) / 1000L;
            MessageUtil.sendWithDefault(player, "abilities.ability-cooldown", "§cAbility on cooldown! (" + remaining + "s)", "remaining", String.valueOf(remaining));
            return;
        }
        this.triggerRiptide(player);
        this.plugin.getDataManager().setAbilityCooldown(player.getUniqueId(), "trident_riptide", now);
    }

    private void triggerRiptide(final Player player) {
        Vector direction = player.getLocation().getDirection().normalize();
        player.setVelocity(direction.multiply(2.0));
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 1.0f, 1.0f);
        CompatUtil.spawnParticle(player.getWorld(), "NAUTILUS", player.getLocation(), 50, 0.5, 0.5, 0.5, 0.1);
        BukkitRunnable damageTask = new BukkitRunnable(){
            int ticks = 0;

            public void run() {
                if (this.ticks > 6) {
                    this.cancel();
                    return;
                }
                for (Entity entity : player.getNearbyEntities(1.5, 1.5, 1.5)) {
                    if (!(entity instanceof LivingEntity) || entity.equals((Object)player)) continue;
                    LivingEntity victim = (LivingEntity)entity;
                    victim.damage(6.0, (Entity)player);
                    CompatUtil.spawnParticle(victim.getWorld(), "FLASH", victim.getLocation().add(0.0, 1.0, 0.0), 1);
                }
                ++this.ticks;
            }
        };
        damageTask.runTaskTimer((Plugin)this.plugin, 1L, 1L);
    }
}

