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
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Arrow;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionData;

/**
 * Blocks vanilla Strength potions from being used.
 * Refactored for 1.16.5 Spigot API compatibility.
 */
public class PotionBanListener implements Listener {

    private final StrengthSMP plugin;

    public PotionBanListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPotionConsume(PlayerItemConsumeEvent event) {
        if (!plugin.getConfigManager().isBanStrengthPotions()) return;

        ItemStack item = event.getItem();
        if (isStrengthSource(item)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§c⚠ Strength potions are disabled.");
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        if (!plugin.getConfigManager().isBanStrengthPotions()) return;
        
        if (isStrengthSource(event.getPotion().getItem())) {
            event.setCancelled(true);
            if (event.getPotion().getShooter() instanceof Player) {
                ((Player) event.getPotion().getShooter()).sendMessage("§c⚠ Strength potions are disabled.");
            }
        }
    }

    @EventHandler
    public void onCloudApply(AreaEffectCloudApplyEvent event) {
        if (!plugin.getConfigManager().isBanStrengthPotions()) return;
        
        AreaEffectCloud cloud = event.getEntity();
        PotionData data = cloud.getBasePotionData();
        if (data != null && data.getType() == PotionType.STRENGTH) {
            event.setCancelled(true);
            return;
        }
        
        for (PotionEffect effect : cloud.getCustomEffects()) {
            if (effect.getType().equals(PotionEffectType.INCREASE_DAMAGE)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (!plugin.getConfigManager().isBanStrengthPotions()) return;
        if (!(event.getEntity() instanceof Arrow)) return;
        Arrow arrow = (Arrow) event.getEntity();
        
        PotionData data = arrow.getBasePotionData();
        if (data != null && data.getType() == PotionType.STRENGTH) {
            arrow.setBasePotionData(new PotionData(PotionType.WATER)); // Strip effect
        }
        
        if (!arrow.getCustomEffects().isEmpty()) {
            arrow.removeCustomEffect(PotionEffectType.INCREASE_DAMAGE);
        }
    }

    @EventHandler
    public void onStrengthEffectApplied(EntityPotionEffectEvent event) {
        if (!plugin.getConfigManager().isBanStrengthPotions()) return;
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getNewEffect() == null) return;

        if (event.getNewEffect().getType().equals(PotionEffectType.INCREASE_DAMAGE)) {
            EntityPotionEffectEvent.Cause cause = event.getCause();
            if (cause.name().contains("POTION") || 
                cause == EntityPotionEffectEvent.Cause.AREA_EFFECT_CLOUD ||
                cause == EntityPotionEffectEvent.Cause.ARROW) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isStrengthSource(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        if (!(item.getItemMeta() instanceof PotionMeta)) return false;
        PotionMeta meta = (PotionMeta) item.getItemMeta();

        PotionData data = meta.getBasePotionData();
        if (data != null && data.getType() == PotionType.STRENGTH) {
            return true;
        }

        for (PotionEffect effect : meta.getCustomEffects()) {
            if (effect.getType().equals(PotionEffectType.INCREASE_DAMAGE)) {
                return true;
            }
        }

        return false;
    }
}
