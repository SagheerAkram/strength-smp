/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Arrow
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.Projectile
 *  org.bukkit.entity.Trident
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.entity.EntityDamageEvent
 *  org.bukkit.event.entity.EntityDamageEvent$DamageModifier
 *  org.bukkit.projectiles.ProjectileSource
 */
package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.config.Config;
import com.floki.strengthsmp.data.WeaponType;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

public class DamageListener
implements Listener {
    private final StrengthSMP plugin;

    public DamageListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player victim = (Player)event.getEntity();
        if (victim.getTicksLived() < 100) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onPlayerDamageByEntity(EntityDamageByEntityEvent event) {
        double bonus;
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) {
            return;
        }

        Config config = this.plugin.getConfigManager();
        if (!config.isDamageScalingEnabled()) {
            return;
        }

        boolean isMobTarget = !(entity instanceof Player);
        boolean allowMobScaling = config.getC().getBoolean("damage-scaling.enable-mobs", true);
        if (isMobTarget && !allowMobScaling) {
            return;
        }

        Player attacker = null;
        WeaponType itemType = null;
        double baseDamage = event.getDamage();
        Entity damager = event.getDamager();

        if (damager instanceof Player) {
            Player p = (Player)damager;
            attacker = p;
            ItemStack mainHand = attacker.getInventory().getItemInMainHand();
            itemType = WeaponType.fromMaterial(mainHand.getType());
            if (itemType == WeaponType.BOW || itemType == WeaponType.CROSSBOW || itemType == WeaponType.SHIELD) {
                return;
            }
        } else {
            Projectile projectile;
            ProjectileSource projectileSource;
            if (damager instanceof Projectile && (projectileSource = (projectile = (Projectile)damager).getShooter()) instanceof Player) {
                Player p;
                attacker = p = (Player)projectileSource;
                if (projectile instanceof Arrow) {
                    itemType = projectile.hasMetadata("crossbow_shot") || projectile.hasMetadata("crossbow_burst") || projectile.hasMetadata("tripleshot") ? WeaponType.CROSSBOW : WeaponType.BOW;
                } else if (projectile instanceof Trident) {
                    itemType = WeaponType.TRIDENT;
                }
            }
        }

        if (attacker == null) {
            return;
        }

        WeaponType playerClass = this.plugin.getDataManager().getWeaponType(attacker.getUniqueId());
        boolean onlyOnWeaponMatch = config.isOnlyOnWeaponMatch();
        if (onlyOnWeaponMatch && (playerClass != itemType || itemType == null)) {
            return;
        }

        int attackerStr = this.plugin.getDataManager().getStrength(attacker.getUniqueId());
        double strengthMultiplier = this.getStrengthDamageMultiplier(config, attackerStr);

        if (entity instanceof Player) {
            Player victim = (Player)entity;
            int victimStr = this.plugin.getDataManager().getStrength(victim.getUniqueId());
            double victimRed = config.getVictimReductionPerStrength();
            if (victimRed > 0) {
                strengthMultiplier = Math.max(0.1, strengthMultiplier - (victimStr * victimRed));
            }
        }

        if (itemType != null) {
            baseDamage = switch (itemType) {
                case SWORD -> 7.0;
                case AXE -> 9.0;
                case TRIDENT -> {
                    if (event.getDamager() instanceof Trident) {
                        yield 13.5;
                    }
                    yield 9.0;
                }
                case SHIELD -> 3.0;
                case BOW -> event.getDamage();
                case CROSSBOW -> event.getDamager().hasMetadata("crossbow_burst") ? 8.0 : event.getDamage();
                default -> event.getDamage();
            };
        }

        double finalDamage = baseDamage * strengthMultiplier;
        if (event.getDamager().hasMetadata("crossbow_burst")) {
            finalDamage = 5.0;
        }
        if (itemType == WeaponType.BOW && (bonus = finalDamage - baseDamage) > 5.0) {
            finalDamage = baseDamage + 5.0;
        }
        if (attacker.hasMetadata("berserk")) {
            finalDamage += 2.0;
        }
        if (itemType == WeaponType.CROSSBOW) {
            event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0.0);
        }
        event.setDamage(Math.max(0.5, finalDamage));
    }

    private double getStrengthDamageMultiplier(Config config, int strength) {
        double perStr = config.getDamagePerStrength();
        if (perStr > 0.0) {
            return 1.0 + (strength * perStr);
        }
        return switch (strength) {
            case 0 -> 0.85;
            case 1 -> 0.95;
            case 2 -> 1.0;
            case 3 -> 1.1;
            case 4 -> 1.18;
            case 5 -> 1.25;
            default -> strength > 5 ? (1.0 + (strength * 0.05)) : 0.85;
        };
    }
}

