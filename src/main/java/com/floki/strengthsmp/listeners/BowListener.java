/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
 *  org.bukkit.Sound
 *  org.bukkit.entity.Arrow
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.Projectile
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.EntityShootBowEvent
 *  org.bukkit.event.entity.ProjectileHitEvent
 *  org.bukkit.metadata.FixedMetadataValue
 *  org.bukkit.metadata.MetadataValue
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 *  org.bukkit.util.Vector
 */
package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.WeaponType;
import com.floki.strengthsmp.util.CompatUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class BowListener
implements Listener {
    private final StrengthSMP plugin;
    private final NamespacedKey boundKey;

    public BowListener(StrengthSMP plugin) {
        this.plugin = plugin;
        this.boundKey = new NamespacedKey((Plugin)plugin, "bound_player_uuid");
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Player)) {
            return;
        }
        Player player = (Player)livingEntity;
        Entity entity = event.getProjectile();
        if (!(entity instanceof Arrow)) {
            return;
        }
        Arrow arrow = (Arrow)entity;
        if (this.plugin.getDataManager().getWeaponType(player.getUniqueId()) != WeaponType.BOW) {
            return;
        }
        if (event.getBow() == null || event.getBow().getType() != Material.BOW) {
            return;
        }
        if (event.getBow().getItemMeta() == null) {
            return;
        }
        if (!event.getBow().getItemMeta().getPersistentDataContainer().has(this.boundKey, PersistentDataType.STRING)) {
            return;
        }
        if (Math.random() < 0.25) {
            arrow.setMetadata("homing", (MetadataValue)new FixedMetadataValue((Plugin)this.plugin, (Object)true));
            this.startHomingTask(arrow);
        }
        if (Math.random() < 0.2) {
            arrow.setMetadata("explosive", (MetadataValue)new FixedMetadataValue((Plugin)this.plugin, (Object)true));
        }
    }

    private void startHomingTask(final Arrow arrow) {
        new BukkitRunnable(){

            public void run() {
                if (arrow.isDead() || arrow.isOnGround()) {
                    this.cancel();
                    return;
                }
                Entity target = null;
                double closest = 5.0;
                for (Entity entity : arrow.getNearbyEntities(5.0, 5.0, 5.0)) {
                    double dist;
                    Player p;
                    if (!(entity instanceof Player) || (p = (Player)entity).equals((Object)arrow.getShooter()) || !((dist = entity.getLocation().distance(arrow.getLocation())) < closest)) continue;
                    closest = dist;
                    target = entity;
                }
                if (target != null) {
                    Vector direction = target.getLocation().add(0.0, 1.0, 0.0).toVector().subtract(arrow.getLocation().toVector()).normalize();
                    Vector currentVel = arrow.getVelocity();
                    double speed = currentVel.length();
                    arrow.setVelocity(currentVel.add(direction.multiply(0.2)).normalize().multiply(speed));
                    CompatUtil.spawnParticle(arrow.getWorld(), "SPELL_INSTANT", arrow.getLocation(), 1, 0.0, 0.0, 0.0, 0.0);
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 1L, 1L);
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (!(projectile instanceof Arrow)) {
            return;
        }
        Arrow arrow = (Arrow)projectile;
        if (!arrow.hasMetadata("explosive")) {
            return;
        }
        Location loc = arrow.getLocation();
        CompatUtil.spawnParticle(arrow.getWorld(), "EXPLOSION_HUGE", loc, 1);
        arrow.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        double radius = 3.5;
        for (Entity entity : arrow.getNearbyEntities(radius, radius, radius)) {
            if (!(entity instanceof LivingEntity)) continue;
            LivingEntity living = (LivingEntity)entity;
            if (entity.equals((Object)arrow.getShooter())) continue;
            living.damage(6.0, (Entity)arrow.getShooter());
            Vector knockback = living.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(0.8);
            living.setVelocity(living.getVelocity().add(knockback));
        }
    }
}

