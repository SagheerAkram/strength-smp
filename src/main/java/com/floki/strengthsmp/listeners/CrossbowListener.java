/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.entity.Arrow
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.entity.EntityShootBowEvent
 *  org.bukkit.metadata.FixedMetadataValue
 *  org.bukkit.metadata.MetadataValue
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
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class CrossbowListener
implements Listener {
    private final StrengthSMP plugin;
    private static final long BURST_COOLDOWN = 25000L;

    public CrossbowListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Entity entity = event.getProjectile();
        if (!(entity instanceof Arrow)) {
            return;
        }
        Arrow arrow = (Arrow)entity;
        if (event.getBow() != null && event.getBow().getType() == Material.CROSSBOW) {
            arrow.setMetadata("crossbow_shot", (MetadataValue)new FixedMetadataValue((Plugin)this.plugin, (Object)true));
        }
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onArrowHit(EntityDamageByEntityEvent event) {
        long lastUse;
        Entity entity = event.getDamager();
        if (!(entity instanceof Arrow)) {
            return;
        }
        Arrow arrow = (Arrow)entity;
        ProjectileSource projectileSource = arrow.getShooter();
        if (!(projectileSource instanceof Player)) {
            return;
        }
        Player shooter = (Player)projectileSource;
        Entity entity2 = event.getEntity();
        if (!(entity2 instanceof LivingEntity)) {
            return;
        }
        LivingEntity victim = (LivingEntity)entity2;
        if (this.plugin.getDataManager().getWeaponType(shooter.getUniqueId()) != WeaponType.CROSSBOW) {
            return;
        }
        if (shooter.getInventory().getItemInMainHand().getType() != Material.CROSSBOW) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - (lastUse = this.plugin.getDataManager().getAbilityCooldown(shooter.getUniqueId(), "crossbow_burst")) < 25000L) {
            return;
        }
        this.triggerBurst(shooter, victim);
        this.plugin.getDataManager().setAbilityCooldown(shooter.getUniqueId(), "crossbow_burst", now);
    }

    private void triggerBurst(final Player shooter, final LivingEntity victim) {
        MessageUtil.sendWithDefault(shooter, "abilities.rapid-fire", "§b§lRAPID FIRE §r§7— Unleashing burst volley!");
        shooter.playSound(shooter.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1.0f, 2.0f);
        new BukkitRunnable(){
            int count = 0;

            public void run() {
                if (this.count >= 6 || !shooter.isOnline() || !victim.isValid()) {
                    this.cancel();
                    return;
                }
                Vector dir = victim.getLocation().add(0.0, 1.0, 0.0).toVector().subtract(shooter.getEyeLocation().toVector()).normalize();
                Arrow burstArrow = (Arrow)shooter.getWorld().spawn(shooter.getEyeLocation(), Arrow.class);
                burstArrow.setShooter((ProjectileSource)shooter);
                burstArrow.setVelocity(dir.multiply(2.5));
                burstArrow.setMetadata("crossbow_burst", (MetadataValue)new FixedMetadataValue((Plugin)CrossbowListener.this.plugin, (Object)true));
                burstArrow.setCritical(true);
                shooter.getWorld().playSound(shooter.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 0.8f, 1.5f + (float)this.count * 0.1f);
                CompatUtil.spawnParticle(shooter.getWorld(), "SMOKE_NORMAL", shooter.getEyeLocation(), 5, 0.2, 0.2, 0.2, 0.05);
                ++this.count;
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 3L);
    }
}

