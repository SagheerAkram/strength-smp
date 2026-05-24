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
import org.bukkit.Location;
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
            com.floki.strengthsmp.util.CompatUtil.spawnParticle(attacker.getWorld(), "CRIT", victim.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.1);
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
        
        // Find safe destination (5 blocks max)
        org.bukkit.Location destination = start.clone();
        org.bukkit.Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection().normalize();
        
        for (double d = 0.5; d <= 5.0; d += 0.5) {
            org.bukkit.Location check = start.clone().add(direction.clone().multiply(d));
            org.bukkit.block.Block feetBlock = check.getBlock();
            org.bukkit.block.Block headBlock = check.clone().add(0, 1, 0).getBlock();
            
            // If feet or head block is solid, stop raycasting here
            if (feetBlock.getType().isSolid() || headBlock.getType().isSolid()) {
                break;
            }
            destination = check;
        }
        
        // Safety check to prevent Y-level suffocation (e.g. looking down at the surface)
        int safetyCounter = 0;
        while (destination.getBlock().getType().isSolid() && safetyCounter < 5) {
            destination.add(0, 1, 0);
            safetyCounter++;
        }
        
        destination.setYaw(start.getYaw());
        destination.setPitch(start.getPitch());

        // Particle effects: Beautiful Enderman portal burst at origin
        Location origin = start.clone().add(0, 1.0, 0);
        player.getWorld().playSound(origin, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.9f);
        
        // Premium particle cloud at origin
        for (int i = 0; i < 30; i++) {
            double rx = (Math.random() - 0.5) * 1.0;
            double ry = (Math.random() - 0.5) * 1.8;
            double rz = (Math.random() - 0.5) * 1.0;
            com.floki.strengthsmp.util.CompatUtil.spawnParticle(player.getWorld(), "PORTAL", origin.clone().add(rx, ry, rz), 1, 0, 0, 0, 0.15);
        }
        com.floki.strengthsmp.util.CompatUtil.spawnParticle(player.getWorld(), "SPELL_WITCH", origin, 15, 0.4, 0.8, 0.4, 0.05);
        com.floki.strengthsmp.util.CompatUtil.spawnParticle(player.getWorld(), "DRAGON_BREATH", origin, 10, 0.3, 0.6, 0.3, 0.02);

        player.teleport(destination);

        // Beautiful Enderman portal burst at destination
        Location dest = destination.clone().add(0, 1.0, 0);
        player.getWorld().playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
        
        // Premium particle cloud at destination
        for (int i = 0; i < 35; i++) {
            double rx = (Math.random() - 0.5) * 1.0;
            double ry = (Math.random() - 0.5) * 1.8;
            double rz = (Math.random() - 0.5) * 1.0;
            com.floki.strengthsmp.util.CompatUtil.spawnParticle(player.getWorld(), "PORTAL", dest.clone().add(rx, ry, rz), 1, 0, 0, 0, 0.15);
        }
        com.floki.strengthsmp.util.CompatUtil.spawnParticle(player.getWorld(), "SPELL_WITCH", dest, 20, 0.4, 0.8, 0.4, 0.05);
        com.floki.strengthsmp.util.CompatUtil.spawnParticle(player.getWorld(), "DRAGON_BREATH", dest, 12, 0.3, 0.6, 0.3, 0.02);
        
        player.sendMessage("§5§lBLINK §r§7— You teleported forward!");
    }
}
