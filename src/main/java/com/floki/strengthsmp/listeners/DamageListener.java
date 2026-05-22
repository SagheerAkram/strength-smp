package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.WeaponType;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles damage scaling based on strength.
 * This is the central source of truth for ALL damage scaling in StrengthSMP.
 */
public class DamageListener implements Listener {

    private final StrengthSMP plugin;

    public DamageListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player victim = (Player) event.getEntity();
        // 5-second grace period after joining (100 ticks)
        if (victim.getTicksLived() < 100) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Player attacker = null;
        WeaponType itemType = null;
        double baseDamage = event.getDamage();

        // 1. Resolve Attacker and Weapon Type
        if (event.getDamager() instanceof Player p) {
            attacker = p;
            ItemStack weapon = attacker.getInventory().getItemInMainHand();
            itemType = WeaponType.fromMaterial(weapon.getType());
            
            // Ranged/defensive classes (Bow, Crossbow, Shield) cannot deal custom melee damage!
            if (itemType == WeaponType.BOW || itemType == WeaponType.CROSSBOW || itemType == WeaponType.SHIELD) {
                return;
            }
        } else if (event.getDamager() instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player p) {
                attacker = p;
                if (projectile instanceof Arrow) {
                    if (projectile.hasMetadata("crossbow_shot") || projectile.hasMetadata("crossbow_burst") || projectile.hasMetadata("tripleshot")) {
                        itemType = WeaponType.CROSSBOW;
                    } else {
                        itemType = WeaponType.BOW;
                    }
                } else if (projectile instanceof Trident) {
                    itemType = WeaponType.TRIDENT;
                }
            }
        }

        if (attacker == null) return;
        if (!plugin.getConfigManager().isDamageScalingEnabled()) return;

        // 2. Calculate Strength Multiplier
        int attackerStr = plugin.getDataManager().getStrength(attacker.getUniqueId());
        double strengthMultiplier = getStrengthDamageMultiplier(attackerStr);

        // 3. Calculate Class Modifier
        WeaponType playerClass = plugin.getDataManager().getWeaponType(attacker.getUniqueId());
        
        // 🛡️ SECURITY FIX: Only apply StrengthSMP damage scaling if the weapon matches the class.
        // If not matched, we return and let Vanilla Minecraft handle the damage (enchantments, crits, etc).
        if (playerClass != itemType || itemType == null) {
            return;
        }

        // 4. Base Damage Overrides according to spec
        baseDamage = switch (itemType) {
            case SWORD -> 7.0;
            case AXE -> 9.0;
            case TRIDENT -> (event.getDamager() instanceof Trident) ? 9.0 * 1.5 : 9.0; // 1.5x for thrown
            case SHIELD -> 3.0;
            case BOW -> event.getDamage(); // Use vanilla pull-time base for bow
            case CROSSBOW -> {
                if (event.getDamager().hasMetadata("crossbow_burst")) yield 8.0; // High damage for burst arrows
                yield event.getDamage(); // Scale actual vanilla damage for normal crossbow shots
            }
        };

        // finalDamage = baseDamage * strengthMultiplier
        double finalDamage = baseDamage * strengthMultiplier;
        
        // Handle Crossbow Burst specific behavior
        if (event.getDamager().hasMetadata("crossbow_burst")) {
            finalDamage = 5.0; // 5.0 * 6 arrows = 30 damage. After Prot IV (~64%), results in ~5.5 hearts.
        }
        
        // ⚡ ISSUE 3: Clamp Bow Bonus Damage to +5
        if (itemType == WeaponType.BOW) {
            double bonus = finalDamage - baseDamage;
            if (bonus > 5.0) {
                finalDamage = baseDamage + 5.0;
            }
        }

        // ⚔️ ISSUE 2: Berserk Mode Bonus (+2 flat damage)
        if (attacker.hasMetadata("berserk")) {
            finalDamage += 2.0;
        }

        // Armor piercing for Crossbow (100% generic)
        if (itemType == WeaponType.CROSSBOW) {
            event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0);
            // Optional: Also pierce some protection if needed, but ARMOR=0 is already strong
        }

        // Floor at 0.5 so we never cancel the hit entirely
        event.setDamage(Math.max(0.5, finalDamage));
    }

    private double getStrengthDamageMultiplier(int strength) {
        return switch (strength) {
            case 0 -> 0.85;
            case 1 -> 0.95;
            case 2 -> 1.00;
            case 3 -> 1.10;
            case 4 -> 1.18;
            case 5 -> 1.25;
            default -> {
                if (strength > 5) yield 1.25;
                yield 0.85;
            }
        };
    }
}
