/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.AreaEffectCloud
 *  org.bukkit.entity.Arrow
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.AreaEffectCloudApplyEvent
 *  org.bukkit.event.entity.EntityPotionEffectEvent
 *  org.bukkit.event.entity.EntityPotionEffectEvent$Cause
 *  org.bukkit.event.entity.PotionSplashEvent
 *  org.bukkit.event.entity.ProjectileHitEvent
 *  org.bukkit.event.player.PlayerItemConsumeEvent
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.PotionMeta
 *  org.bukkit.potion.PotionData
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 *  org.bukkit.potion.PotionType
 */
package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class PotionBanListener
implements Listener {
    private final StrengthSMP plugin;

    public PotionBanListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPotionConsume(PlayerItemConsumeEvent event) {
        if (!this.plugin.getConfigManager().isBanStrengthPotions()) {
            return;
        }
        ItemStack item = event.getItem();
        if (this.isStrengthSource(item)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("\u00a7c\u26a0 Strength potions are disabled.");
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        if (!this.plugin.getConfigManager().isBanStrengthPotions()) {
            return;
        }
        if (this.isStrengthSource(event.getPotion().getItem())) {
            event.setCancelled(true);
            if (event.getPotion().getShooter() instanceof Player) {
                ((Player)event.getPotion().getShooter()).sendMessage("\u00a7c\u26a0 Strength potions are disabled.");
            }
        }
    }

    @EventHandler
    public void onCloudApply(AreaEffectCloudApplyEvent event) {
        if (!this.plugin.getConfigManager().isBanStrengthPotions()) {
            return;
        }
        AreaEffectCloud cloud = event.getEntity();
        PotionData data = cloud.getBasePotionData();
        if (data != null && data.getType() == PotionType.STRENGTH) {
            event.setCancelled(true);
            return;
        }
        for (PotionEffect effect : cloud.getCustomEffects()) {
            if (!effect.getType().equals((Object)PotionEffectType.INCREASE_DAMAGE)) continue;
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (!this.plugin.getConfigManager().isBanStrengthPotions()) {
            return;
        }
        if (!(event.getEntity() instanceof Arrow)) {
            return;
        }
        Arrow arrow = (Arrow)event.getEntity();
        PotionData data = arrow.getBasePotionData();
        if (data != null && data.getType() == PotionType.STRENGTH) {
            arrow.setBasePotionData(new PotionData(PotionType.WATER));
        }
        if (!arrow.getCustomEffects().isEmpty()) {
            arrow.removeCustomEffect(PotionEffectType.INCREASE_DAMAGE);
        }
    }

    @EventHandler
    public void onStrengthEffectApplied(EntityPotionEffectEvent event) {
        EntityPotionEffectEvent.Cause cause;
        if (!this.plugin.getConfigManager().isBanStrengthPotions()) {
            return;
        }
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (event.getNewEffect() == null) {
            return;
        }
        if (event.getNewEffect().getType().equals((Object)PotionEffectType.INCREASE_DAMAGE) && ((cause = event.getCause()).name().contains("POTION") || cause == EntityPotionEffectEvent.Cause.AREA_EFFECT_CLOUD || cause == EntityPotionEffectEvent.Cause.ARROW)) {
            event.setCancelled(true);
        }
    }

    private boolean isStrengthSource(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        if (!(item.getItemMeta() instanceof PotionMeta)) {
            return false;
        }
        PotionMeta meta = (PotionMeta)item.getItemMeta();
        PotionData data = meta.getBasePotionData();
        if (data != null && data.getType() == PotionType.STRENGTH) {
            return true;
        }
        for (PotionEffect effect : meta.getCustomEffects()) {
            if (!effect.getType().equals((Object)PotionEffectType.INCREASE_DAMAGE)) continue;
            return true;
        }
        return false;
    }
}

