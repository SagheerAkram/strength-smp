package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.WeaponType;
import org.bukkit.Material;
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

public class AxeListener implements Listener {

    private final StrengthSMP plugin;
    private static final long GROUND_POUND_COOLDOWN = 25000L; // 25 seconds

    public AxeListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAxeDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        if (plugin.getDataManager().getWeaponType(attacker.getUniqueId()) != WeaponType.AXE) return;
        if (!WeaponType.AXE.isValidMaterial(attacker.getInventory().getItemInMainHand().getType())) return;

        // Stun condition: strength is at least 1
        int strength = plugin.getDataManager().getStrength(attacker.getUniqueId());
        if (strength >= 1) {
            int stunTicks = plugin.getConfigManager().getAxeStunTicks();
            if (stunTicks > 0) {
                victim.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SLOW, stunTicks, 10, false, false, false));
                victim.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.JUMP, stunTicks, 200, false, false, false));
                
                victim.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0, 1, 0), 10, 0.2, 0.2, 0.2, 0.05);
                victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_ATTACK_WEAK, 1.0f, 0.5f);

                // No messages for mobs
                if (victim instanceof Player playerVictim) {
                    playerVictim.sendMessage("§cYou have been stunned by an axe strike!");
                }
            }
        }

        // Passive: Shield Disable (10% chance to disable shield for 5s) - players only
        if (victim instanceof Player playerVictim) {
            if (Math.random() < 0.10) {
                playerVictim.setCooldown(Material.SHIELD, 100); // 5 seconds (100 ticks)
                playerVictim.sendMessage("§cYour shield has been disabled by an axe strike!");
                playerVictim.playSound(playerVictim.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.0f, 1.0f);
                attacker.playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.5f);
            }
        }
    }

    @EventHandler
    public void onAxeInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (plugin.getDataManager().getWeaponType(player.getUniqueId()) != WeaponType.AXE) return;
        if (!WeaponType.AXE.isValidMaterial(player.getInventory().getItemInMainHand().getType())) return;

        long now = System.currentTimeMillis();
        long lastUse = plugin.getDataManager().getAbilityCooldown(player.getUniqueId(), "axe_pound");

        if (now - lastUse < GROUND_POUND_COOLDOWN) {
            long remaining = (GROUND_POUND_COOLDOWN - (now - lastUse)) / 1000;
            player.sendMessage("§cAbility on cooldown! (" + remaining + "s)");
            return;
        }

        triggerGroundPound(player);
        plugin.getDataManager().setAbilityCooldown(player.getUniqueId(), "axe_pound", now);
    }

    private void triggerGroundPound(Player player) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);
        player.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_HUGE, player.getLocation(), 1);
        com.floki.strengthsmp.util.CompatUtil.spawnParticle(player.getWorld(), "BLOCK_CRACK", player.getLocation(), 100, 3.0, 0.1, 3.0, 0, org.bukkit.Material.DIRT.createBlockData());

        for (Entity entity : player.getNearbyEntities(6.0, 4.0, 6.0)) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                LivingEntity victim = (LivingEntity) entity;
                
                // Knockback
                Vector diff = victim.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                diff.setY(0.6); // Upward boost
                victim.setVelocity(diff.multiply(1.8));
                
                victim.damage(10.0, player); // 10 damage as per spec
            }
        }
    }
}
