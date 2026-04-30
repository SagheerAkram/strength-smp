package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.WeaponType;
import com.floki.strengthsmp.util.MessageUtil;
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

import java.util.UUID;

public class SwordListener implements Listener {

    private final StrengthSMP plugin;
    private static final long BLINK_COOLDOWN = 30000L; // 30 seconds

    public SwordListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSwordDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        if (plugin.getDataManager().getWeaponType(attacker.getUniqueId()) != WeaponType.SWORD) return;
        if (!WeaponType.SWORD.isValidMaterial(attacker.getInventory().getItemInMainHand().getType())) return;

        // Passive: Bleed (15% chance to apply Wither II for 3s)
        if (Math.random() < 0.15) {
            victim.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.WITHER, 60, 1));
            attacker.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.1);
            attacker.playSound(attacker.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 2.0f);
            attacker.sendMessage("§5§lBLEED §r§7— Your blade caused the victim to wither!");
        }
    }

    @EventHandler
    public void onSwordInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (plugin.getDataManager().getWeaponType(player.getUniqueId()) != WeaponType.SWORD) return;
        if (!WeaponType.SWORD.isValidMaterial(player.getInventory().getItemInMainHand().getType())) return;

        long now = System.currentTimeMillis();
        long lastUse = plugin.getDataManager().getAbilityCooldown(player.getUniqueId(), "sword_blink");

        if (now - lastUse < BLINK_COOLDOWN) {
            long remaining = (BLINK_COOLDOWN - (now - lastUse)) / 1000;
            player.sendMessage("§cAbility on cooldown! (" + remaining + "s)");
            return;
        }

        // Trigger Blink
        triggerBlink(player);
        plugin.getDataManager().setAbilityCooldown(player.getUniqueId(), "sword_blink", now);
    }

    private void triggerBlink(Player player) {
        org.bukkit.Location start = player.getLocation();
        Vector dir = start.getDirection().normalize();
        
        // Find safe destination (5 blocks max)
        org.bukkit.block.Block target = player.getTargetBlockExact(5);
        org.bukkit.Location destination;
        
        if (target == null) {
            destination = start.clone().add(dir.multiply(5));
        } else {
            destination = target.getLocation().subtract(dir).add(0.5, 0, 0.5); // Back up 1 block from wall
        }
        
        destination.setYaw(start.getYaw());
        destination.setPitch(start.getPitch());

        // Particle effects at start and end
        player.getWorld().spawnParticle(Particle.PORTAL, start.add(0, 1, 0), 30, 0.2, 0.5, 0.2, 0.1);
        player.teleport(destination);
        player.getWorld().spawnParticle(Particle.PORTAL, destination.add(0, 1, 0), 30, 0.2, 0.5, 0.2, 0.1);
        player.getWorld().playSound(destination, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
        
        player.sendMessage("§5§lBLINK §r§7— You teleported forward!");
    }
}
