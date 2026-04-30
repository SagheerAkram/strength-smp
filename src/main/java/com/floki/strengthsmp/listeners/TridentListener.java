package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.WeaponType;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class TridentListener implements Listener {

    private final StrengthSMP plugin;
    private static final long RIPTIDE_COOLDOWN = 15000L; // 15 seconds

    public TridentListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }



    @EventHandler
    public void onTridentInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (plugin.getDataManager().getWeaponType(player.getUniqueId()) != WeaponType.TRIDENT) return;
        if (player.getInventory().getItemInMainHand().getType() != org.bukkit.Material.TRIDENT) return;

        long now = System.currentTimeMillis();
        long lastUse = plugin.getDataManager().getAbilityCooldown(player.getUniqueId(), "trident_riptide");

        if (now - lastUse < RIPTIDE_COOLDOWN) {
            long remaining = (RIPTIDE_COOLDOWN - (now - lastUse)) / 1000;
            player.sendMessage("§cAbility on cooldown! (" + remaining + "s)");
            return;
        }

        triggerRiptide(player);
        plugin.getDataManager().setAbilityCooldown(player.getUniqueId(), "trident_riptide", now);
    }

    private void triggerRiptide(Player player) {
        Vector direction = player.getLocation().getDirection().normalize();
        player.setVelocity(direction.multiply(2.0));
        
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.NAUTILUS, player.getLocation(), 50, 0.5, 0.5, 0.5, 0.1);

        // Damage entities in path
        org.bukkit.scheduler.BukkitRunnable damageTask = new org.bukkit.scheduler.BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks > 6) {
                    this.cancel();
                    return;
                }
                
                for (Entity entity : player.getNearbyEntities(1.5, 1.5, 1.5)) {
                    if (entity instanceof LivingEntity && !entity.equals(player)) {
                        LivingEntity victim = (LivingEntity) entity;
                        victim.damage(6.0, player);
                        victim.getWorld().spawnParticle(Particle.FLASH, victim.getLocation().add(0, 1, 0), 1);
                    }
                }
                ticks++;
            }
        };
        damageTask.runTaskTimer(plugin, 1L, 1L);
    }
}
