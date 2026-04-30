package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.WeaponType;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BowListener implements Listener {

    private final StrengthSMP plugin;
    private final Set<UUID> explosiveNext = new HashSet<>();
    private static final long EXPLOSIVE_COOLDOWN = 30000L; // 30 seconds

    public BowListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }



    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getProjectile() instanceof Arrow arrow)) return;
        if (plugin.getDataManager().getWeaponType(player.getUniqueId()) != WeaponType.BOW) return;

        // Passive: Homing (25% chance)
        if (Math.random() < 0.25) {
            arrow.setMetadata("homing", new FixedMetadataValue(plugin, true));
            startHomingTask(arrow);
        }

        // Active: Explosive
        if (explosiveNext.contains(player.getUniqueId())) {
            arrow.setMetadata("explosive", new FixedMetadataValue(plugin, true));
            explosiveNext.remove(player.getUniqueId());
            player.sendMessage("§4§lEXPLOSIVE §r§7— Arrow launched!");
        }
    }

    private void startHomingTask(Arrow arrow) {
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                if (arrow.isDead() || arrow.isOnGround()) {
                    this.cancel();
                    return;
                }

                Entity target = null;
                double closest = 5.0;

                for (Entity entity : arrow.getNearbyEntities(5, 5, 5)) {
                    if (entity instanceof Player p && !p.equals(arrow.getShooter())) {
                        double dist = entity.getLocation().distance(arrow.getLocation());
                        if (dist < closest) {
                            closest = dist;
                            target = entity;
                        }
                    }
                }

                if (target != null) {
                    Vector direction = target.getLocation().add(0, 1, 0).toVector().subtract(arrow.getLocation().toVector()).normalize();
                    Vector currentVel = arrow.getVelocity();
                    double speed = currentVel.length();
                    arrow.setVelocity(currentVel.add(direction.multiply(0.2)).normalize().multiply(speed));
                    arrow.getWorld().spawnParticle(Particle.INSTANT_EFFECT, arrow.getLocation(), 1, 0, 0, 0, 0);
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) return;
        if (!arrow.hasMetadata("explosive")) return;

        org.bukkit.Location loc = arrow.getLocation();
        arrow.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 1);
        arrow.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

        // Manual damage loop to prevent self-damage
        double radius = 3.5;
        for (Entity entity : arrow.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity living && !entity.equals(arrow.getShooter())) {
                living.damage(6.0, (Entity) arrow.getShooter());
                Vector knockback = living.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(0.8);
                living.setVelocity(living.getVelocity().add(knockback));
            }
        }
    }

    @EventHandler
    public void onBowInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (plugin.getDataManager().getWeaponType(player.getUniqueId()) != WeaponType.BOW) return;
        if (player.getInventory().getItemInMainHand().getType() != Material.BOW) return;

        // If they are pulling the bow, don't trigger the toggle? 
        // Actually, we'll use a sneak-right-click or just right-click if they haven't started pulling.
        // But for simplicity, we'll use a cooldown check.

        long now = System.currentTimeMillis();
        long lastUse = plugin.getDataManager().getAbilityCooldown(player.getUniqueId(), "bow_explosive");

        if (now - lastUse < EXPLOSIVE_COOLDOWN) {
            // Only message if they aren't already primed
            if (!explosiveNext.contains(player.getUniqueId())) {
                // long remaining = (EXPLOSIVE_COOLDOWN - (now - lastUse)) / 1000;
                // player.sendMessage("§cExplosive Shot on cooldown! (" + remaining + "s)");
            }
            return;
        }

        if (!explosiveNext.contains(player.getUniqueId())) {
            explosiveNext.add(player.getUniqueId());
            player.sendMessage("§4§lEXPLOSIVE §r§7— Your next arrow will explode!");
            player.playSound(player.getLocation(), Sound.ENTITY_TNT_PRIMED, 1.0f, 1.5f);
            plugin.getDataManager().setAbilityCooldown(player.getUniqueId(), "bow_explosive", now);
        }
    }
}
