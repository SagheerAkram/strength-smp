package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.WeaponType;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ShieldListener implements Listener {

    private final StrengthSMP plugin;
    private final Set<UUID> reflecting = new HashSet<>();
    private static final long REFLECT_COOLDOWN = 40000L; // 40 seconds

    public ShieldListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShieldDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        // Passive: 50% Damage Reduction when holding shield (and Shield Class)
        if (plugin.getDataManager().getWeaponType(victim.getUniqueId()) == WeaponType.SHIELD) {
            boolean holdingShield = victim.getInventory().getItemInMainHand().getType() == Material.SHIELD ||
                                   victim.getInventory().getItemInOffHand().getType() == Material.SHIELD;
            
            if (holdingShield) {
                // If they are blocking, vanilla handles it. If not, we apply 50% reduction.
                if (!victim.isBlocking()) {
                    event.setDamage(event.getDamage() * 0.50);
                }
            }
        }

        // Ability: Reflect 100% (Active for 3s)
        if (reflecting.contains(victim.getUniqueId())) {
            if (event.getDamager() instanceof Player attacker) {
                double damage = event.getDamage();
                attacker.damage(damage, victim); // Reflect 100%
                
                victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 2.0f);
                victim.getWorld().spawnParticle(Particle.FLASH, victim.getLocation().add(0, 1, 0), 5);
            }
        }
    }

    @EventHandler
    public void onShieldInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (plugin.getDataManager().getWeaponType(player.getUniqueId()) != WeaponType.SHIELD) return;
        
        boolean holdingShield = player.getInventory().getItemInMainHand().getType() == Material.SHIELD ||
                               player.getInventory().getItemInOffHand().getType() == Material.SHIELD;
        if (!holdingShield) return;

        long now = System.currentTimeMillis();
        long lastUse = plugin.getDataManager().getAbilityCooldown(player.getUniqueId(), "shield_reflect");

        if (now - lastUse < REFLECT_COOLDOWN) {
            long remaining = (REFLECT_COOLDOWN - (now - lastUse)) / 1000;
            player.sendMessage("§cAbility on cooldown! (" + remaining + "s)");
            return;
        }

        triggerReflect(player);
        plugin.getDataManager().setAbilityCooldown(player.getUniqueId(), "shield_reflect", now);
    }

    private void triggerReflect(Player player) {
        reflecting.add(player.getUniqueId());
        player.sendMessage("§6§lREFLECT §r§7— You are reflecting all damage for 7s!");
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.5f);
        player.getWorld().spawnParticle(Particle.ENCHANT, player.getLocation(), 100, 0.5, 1.0, 0.5, 0.1);

        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
            reflecting.remove(player.getUniqueId());
            if (player.isOnline()) {
                player.sendMessage("§cReflect has worn off.");
            }
        }, 140L); // 7 seconds
    }
}
