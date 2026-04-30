package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Arrow;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Blocks vanilla Strength potions from being used.
 * Uses the modern PotionMeta API compatible with Paper 1.21.
 */
public class PotionBanListener implements Listener {

    private final StrengthSMP plugin;

    public PotionBanListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    /** Block drinking strength potions */
    @EventHandler
    public void onPotionConsume(PlayerItemConsumeEvent event) {
        if (!plugin.getConfigManager().isBanStrengthPotions()) return;

        ItemStack item = event.getItem();
        if (isStrengthSource(item)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§c⚠ Strength potions are disabled.");
        }
    }

    /** Block splash potions containing strength */
    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        if (!plugin.getConfigManager().isBanStrengthPotions()) return;
        
        if (isStrengthSource(event.getPotion().getItem())) {
            event.setCancelled(true);
            if (event.getPotion().getShooter() instanceof Player shooter) {
                shooter.sendMessage("§c⚠ Strength potions are disabled.");
            }
        }
    }

    /** Block lingering potion clouds from applying strength */
    @EventHandler
    public void onCloudApply(AreaEffectCloudApplyEvent event) {
        if (!plugin.getConfigManager().isBanStrengthPotions()) return;
        
        AreaEffectCloud cloud = event.getEntity();
        if (cloud.getBasePotionType() != null && cloud.getBasePotionType() == PotionType.STRENGTH) {
            event.setCancelled(true);
            return;
        }
        
        // Check custom effects in cloud
        for (PotionEffect effect : cloud.getCustomEffects()) {
            if (effect.getType().equals(PotionEffectType.STRENGTH)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /** Block tipped arrows with strength */
    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (!plugin.getConfigManager().isBanStrengthPotions()) return;
        if (!(event.getEntity() instanceof Arrow arrow)) return;
        
        if (arrow.getBasePotionType() != null && arrow.getBasePotionType() == PotionType.STRENGTH) {
            arrow.setBasePotionType(null); // Strip effect
        }
        
        // Remove custom strength effects
        if (!arrow.getCustomEffects().isEmpty()) {
            arrow.removeCustomEffect(PotionEffectType.STRENGTH);
        }
    }

    /** Global backstop for any strength effect application */
    @EventHandler
    public void onStrengthEffectApplied(EntityPotionEffectEvent event) {
        if (!plugin.getConfigManager().isBanStrengthPotions()) return;
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getNewEffect() == null) return;

        // If it's strength and comes from a potion source, cancel it
        if (event.getNewEffect().getType().equals(PotionEffectType.STRENGTH)) {
            EntityPotionEffectEvent.Cause cause = event.getCause();
            if (cause.name().contains("POTION") || 
                cause == EntityPotionEffectEvent.Cause.AREA_EFFECT_CLOUD ||
                cause == EntityPotionEffectEvent.Cause.ARROW) {
                event.setCancelled(true);
            }
        }
    }

    /** Helper to check if an item provides strength */
    private boolean isStrengthSource(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        if (!(item.getItemMeta() instanceof PotionMeta meta)) return false;

        // Check base potion type
        if (meta.getBasePotionType() != null && meta.getBasePotionType() == PotionType.STRENGTH) {
            return true;
        }

        // Check custom effects
        for (PotionEffect effect : meta.getCustomEffects()) {
            if (effect.getType().equals(PotionEffectType.STRENGTH)) {
                return true;
            }
        }

        return false;
    }
}
