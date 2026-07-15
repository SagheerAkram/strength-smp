/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Sound
 *  org.bukkit.block.Block
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.metadata.FixedMetadataValue
 *  org.bukkit.metadata.MetadataValue
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 *  org.bukkit.util.Vector
 */
package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.WeaponType;
import com.floki.strengthsmp.util.CompatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class SwordListener
implements Listener {
    private final StrengthSMP plugin;
    private static final long BLINK_COOLDOWN = 30000L;

    public SwordListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onSwordDamage(EntityDamageByEntityEvent event) {
        Entity entity = event.getDamager();
        if (!(entity instanceof Player)) {
            return;
        }
        Player attacker = (Player)entity;
        Entity entity2 = event.getEntity();
        if (!(entity2 instanceof LivingEntity)) {
            return;
        }
        LivingEntity victim = (LivingEntity)entity2;
        if (this.plugin.getDataManager().getWeaponType(attacker.getUniqueId()) != WeaponType.SWORD) {
            return;
        }
        if (!WeaponType.SWORD.isValidMaterial(attacker.getInventory().getItemInMainHand().getType())) {
            return;
        }
        if (Math.random() < 0.15) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1));
            CompatUtil.spawnParticle(attacker.getWorld(), "CRIT", victim.getLocation().add(0.0, 1.0, 0.0), 10, 0.3, 0.3, 0.3, 0.1);
            attacker.playSound(attacker.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 2.0f);
            attacker.sendMessage("\u00a75\u00a7lBLEED \u00a7r\u00a77\u2014 Your blade caused the victim to wither!");
        }
    }

    @EventHandler
    public void onSwordInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (this.plugin.getDataManager().getWeaponType(player.getUniqueId()) != WeaponType.SWORD) {
            return;
        }
        if (!WeaponType.SWORD.isValidMaterial(player.getInventory().getItemInMainHand().getType())) {
            return;
        }
        long now = System.currentTimeMillis();
        if (player.isSneaking()) {
            long cooldownMs;
            long lastUse = this.plugin.getDataManager().getAbilityCooldown(player.getUniqueId(), "sword_berserk");
            if (now - lastUse < (cooldownMs = 60000L)) {
                long remaining = (cooldownMs - (now - lastUse)) / 1000L;
                player.sendMessage("\u00a7cBerserk is on cooldown! (" + remaining + "s)");
                return;
            }
            this.triggerBerserk(player);
            this.plugin.getDataManager().setAbilityCooldown(player.getUniqueId(), "sword_berserk", now);
        } else {
            long lastUse = this.plugin.getDataManager().getAbilityCooldown(player.getUniqueId(), "sword_blink");
            if (now - lastUse < 30000L) {
                long remaining = (30000L - (now - lastUse)) / 1000L;
                player.sendMessage("\u00a7cBlink is on cooldown! (" + remaining + "s)");
                return;
            }
            this.triggerBlink(player);
            this.plugin.getDataManager().setAbilityCooldown(player.getUniqueId(), "sword_blink", now);
        }
    }

    private void triggerBerserk(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 300, 2));
        player.setMetadata("berserk", (MetadataValue)new FixedMetadataValue((Plugin)this.plugin, (Object)true));
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (player.isOnline()) {
                player.removeMetadata("berserk", (Plugin)this.plugin);
                player.sendMessage("\u00a7cBerserk mode has ended.");
            }
        }, 300L);
        player.sendMessage("\u00a7e\u00a7lBERSERK \u00a7r\u00a77\u2014 Gained Berserk mode! (+2 Attack Damage, Speed III for 15s)");
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        CompatUtil.spawnParticle(player.getWorld(), "VILLAGER_ANGRY", player.getLocation().add(0.0, 1.5, 0.0), 10, 0.5, 0.5, 0.5, 0.1);
    }

    private void triggerBlink(Player player) {
        Location start = player.getLocation();
        Location destination = start.clone();
        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection().normalize();
        for (double d = 0.5; d <= 5.0; d += 0.5) {
            Location check = start.clone().add(direction.clone().multiply(d));
            Block feetBlock = check.getBlock();
            Block headBlock = check.clone().add(0.0, 1.0, 0.0).getBlock();
            if (feetBlock.getType().isSolid() || headBlock.getType().isSolid()) break;
            destination = check;
        }
        for (int safetyCounter = 0; destination.getBlock().getType().isSolid() && safetyCounter < 5; ++safetyCounter) {
            destination.add(0.0, 1.0, 0.0);
        }
        destination.setYaw(start.getYaw());
        destination.setPitch(start.getPitch());
        Location origin = start.clone().add(0.0, 1.0, 0.0);
        player.getWorld().playSound(origin, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.9f);
        for (int i = 0; i < 30; ++i) {
            double rx = (Math.random() - 0.5) * 1.0;
            double ry = (Math.random() - 0.5) * 1.8;
            double rz = (Math.random() - 0.5) * 1.0;
            CompatUtil.spawnParticle(player.getWorld(), "PORTAL", origin.clone().add(rx, ry, rz), 1, 0.0, 0.0, 0.0, 0.15);
        }
        CompatUtil.spawnParticle(player.getWorld(), "SPELL_WITCH", origin, 15, 0.4, 0.8, 0.4, 0.05);
        CompatUtil.spawnParticle(player.getWorld(), "DRAGON_BREATH", origin, 10, 0.3, 0.6, 0.3, 0.02);
        player.teleport(destination);
        Location dest = destination.clone().add(0.0, 1.0, 0.0);
        player.getWorld().playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
        for (int i = 0; i < 35; ++i) {
            double rx = (Math.random() - 0.5) * 1.0;
            double ry = (Math.random() - 0.5) * 1.8;
            double rz = (Math.random() - 0.5) * 1.0;
            CompatUtil.spawnParticle(player.getWorld(), "PORTAL", dest.clone().add(rx, ry, rz), 1, 0.0, 0.0, 0.0, 0.15);
        }
        CompatUtil.spawnParticle(player.getWorld(), "SPELL_WITCH", dest, 20, 0.4, 0.8, 0.4, 0.05);
        CompatUtil.spawnParticle(player.getWorld(), "DRAGON_BREATH", dest, 12, 0.3, 0.6, 0.3, 0.02);
        player.sendMessage("\u00a75\u00a7lBLINK \u00a7r\u00a77\u2014 You teleported forward!");
    }
}

