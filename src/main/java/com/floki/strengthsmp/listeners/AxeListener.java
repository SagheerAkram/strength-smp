/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 *  org.bukkit.util.Vector
 */
package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.WeaponType;
import com.floki.strengthsmp.util.CompatUtil;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.Material;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class AxeListener
implements Listener {
    private final StrengthSMP plugin;
    private static final long GROUND_POUND_COOLDOWN = 25000L;

    public AxeListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onAxeDamage(EntityDamageByEntityEvent event) {
        int stunTicks;
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
        if (this.plugin.getDataManager().getWeaponType(attacker.getUniqueId()) != WeaponType.AXE) {
            return;
        }
        if (!WeaponType.AXE.isValidMaterial(attacker.getInventory().getItemInMainHand().getType())) {
            return;
        }
        int strength = this.plugin.getDataManager().getStrength(attacker.getUniqueId());
        if (strength >= 1 && (stunTicks = this.plugin.getConfigManager().getAxeStunTicks()) > 0) {
            victim.addPotionEffect(new PotionEffect(CompatUtil.resolvePotionEffect("SLOWNESS", "SLOW"), stunTicks, 10, false, false, false));
            victim.addPotionEffect(new PotionEffect(CompatUtil.resolvePotionEffect("JUMP_BOOST", "JUMP"), stunTicks, 200, false, false, false));
            CompatUtil.spawnParticle(victim.getWorld(), "CRIT", victim.getLocation().add(0.0, 1.0, 0.0), 10, 0.2, 0.2, 0.2, 0.05);
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_ATTACK_WEAK, 1.0f, 0.5f);
            if (victim instanceof Player) {
                Player playerVictim = (Player)victim;
                MessageUtil.sendWithDefault(playerVictim, "abilities.axe-stunned", "§cYou have been stunned by an axe strike!");
            }
        }
        if (victim instanceof Player) {
            Player playerVictim = (Player)victim;
            if (Math.random() < 0.1) {
                playerVictim.setCooldown(Material.SHIELD, 100);
                MessageUtil.sendWithDefault(playerVictim, "abilities.axe-shield-disabled", "§cYour shield has been disabled by an axe strike!");
                playerVictim.playSound(playerVictim.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.0f, 1.0f);
                attacker.playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.5f);
            }
        }
    }

    private final java.util.Map<java.util.UUID, Long> lastAxeInteract = new java.util.concurrent.ConcurrentHashMap<>();

    @EventHandler
    public void onAxeInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (this.plugin.getDataManager().getWeaponType(player.getUniqueId()) != WeaponType.AXE) {
            return;
        }
        org.bukkit.inventory.ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand == null || !WeaponType.AXE.isValidMaterial(mainHand.getType())) {
            return;
        }
        long now = System.currentTimeMillis();
        long lastClick = this.lastAxeInteract.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastClick < 300L) {
            return;
        }
        long lastUse = this.plugin.getDataManager().getAbilityCooldown(player.getUniqueId(), "axe_pound");
        if (now - lastUse < 25000L) {
            long remaining = (25000L - (now - lastUse)) / 1000L;
            MessageUtil.sendWithDefault(player, "abilities.ability-cooldown", "§cAbility on cooldown! (" + remaining + "s)", "remaining", String.valueOf(remaining));
            this.lastAxeInteract.put(player.getUniqueId(), now);
            return;
        }
        this.lastAxeInteract.put(player.getUniqueId(), now);
        event.setCancelled(true);
        this.triggerGroundPound(player);
        this.plugin.getDataManager().setAbilityCooldown(player.getUniqueId(), "axe_pound", now);
    }

    private void triggerGroundPound(Player player) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);
        CompatUtil.spawnParticle(player.getWorld(), "EXPLOSION_HUGE", player.getLocation(), 1);
        CompatUtil.spawnParticle(player.getWorld(), "BLOCK_CRACK", player.getLocation(), 100, 3.0, 0.1, 3.0, 0.0, Material.DIRT.createBlockData());
        for (Entity entity : player.getNearbyEntities(6.0, 4.0, 6.0)) {
            if (!(entity instanceof LivingEntity) || entity.equals((Object)player)) continue;
            LivingEntity victim = (LivingEntity)entity;
            Vector diff = victim.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
            diff.setY(0.6);
            victim.setVelocity(diff.multiply(1.8));
            victim.damage(10.0, (Entity)player);
        }
    }
}

