package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.WeaponType;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.UUID;

public class CrossbowListener implements Listener {

    private final StrengthSMP plugin;
    private static final long BURST_COOLDOWN = 25000L; // 25 seconds

    public CrossbowListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArrowHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player shooter)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        // Check if it's a Crossbow class player
        if (plugin.getDataManager().getWeaponType(shooter.getUniqueId()) != WeaponType.CROSSBOW) return;
        if (shooter.getInventory().getItemInMainHand().getType() != Material.CROSSBOW) return;

        // Check Cooldown
        long now = System.currentTimeMillis();
        long lastUse = plugin.getDataManager().getAbilityCooldown(shooter.getUniqueId(), "crossbow_burst");
        if (now - lastUse < BURST_COOLDOWN) return;

        // Trigger Burst
        triggerBurst(shooter, victim);
        plugin.getDataManager().setAbilityCooldown(shooter.getUniqueId(), "crossbow_burst", now);
    }

    private void triggerBurst(Player shooter, LivingEntity victim) {
        shooter.sendMessage("§b§lRAPID FIRE §r§7— Unleashing burst volley!");
        shooter.playSound(shooter.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1.0f, 2.0f);

        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= 6 || !shooter.isOnline() || !victim.isValid()) {
                    this.cancel();
                    return;
                }

                // Fire arrow from shooter towards victim
                Vector dir = victim.getLocation().add(0, 1, 0).toVector().subtract(shooter.getEyeLocation().toVector()).normalize();
                Arrow burstArrow = shooter.getWorld().spawn(shooter.getEyeLocation(), Arrow.class);
                burstArrow.setShooter(shooter);
                burstArrow.setVelocity(dir.multiply(2.5));
                burstArrow.setMetadata("crossbow_burst", new FixedMetadataValue(plugin, true));
                burstArrow.setCritical(true);

                shooter.getWorld().playSound(shooter.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 0.8f, 1.5f + (count * 0.1f));
                shooter.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_NORMAL, shooter.getEyeLocation(), 5, 0.2, 0.2, 0.2, 0.05);

                count++;
            }
        }.runTaskTimer(plugin, 0L, 3L); // Burst every 3 ticks
    }
}
