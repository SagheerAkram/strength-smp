/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 */
package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.WeaponType;
import com.floki.strengthsmp.util.CompatUtil;
import com.floki.strengthsmp.util.MessageUtil;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ShieldListener
implements Listener {
    private final StrengthSMP plugin;
    private final Set<UUID> reflecting = new HashSet<UUID>();
    private static final long REFLECT_COOLDOWN = 40000L;

    public ShieldListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onShieldDamage(EntityDamageByEntityEvent event) {
        Entity entity;
        Entity entity2 = event.getEntity();
        if (!(entity2 instanceof Player)) {
            return;
        }
        Player victim = (Player)entity2;
        if (this.plugin.getDataManager().getWeaponType(victim.getUniqueId()) == WeaponType.SHIELD) {
            boolean holdingShield;
            boolean bl = holdingShield = victim.getInventory().getItemInMainHand().getType() == Material.SHIELD || victim.getInventory().getItemInOffHand().getType() == Material.SHIELD;
            if (holdingShield && !victim.isBlocking()) {
                event.setDamage(event.getDamage() * 0.5);
            }
        }
        if (this.reflecting.contains(victim.getUniqueId()) && (entity = event.getDamager()) instanceof Player) {
            Player attacker = (Player)entity;
            double damage = event.getDamage();
            attacker.damage(damage, (Entity)victim);
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 2.0f);
            CompatUtil.spawnParticle(victim.getWorld(), "FLASH", victim.getLocation().add(0.0, 1.0, 0.0), 5);
        }
    }

    @EventHandler
    public void onShieldInteract(PlayerInteractEvent event) {
        long lastUse;
        boolean holdingShield;
        Material type;
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && ((type = event.getClickedBlock().getType()).name().contains("DOOR") || type.name().contains("CHEST") || type.name().contains("BUTTON") || type.name().contains("BED") || type.name().contains("FURNACE") || type.name().contains("TABLE") || type.name().contains("GATE") || type.name().contains("SHULKER_BOX") || type.name().contains("ANVIL") || type.name().contains("LEVER") || type.name().contains("TRAPDOOR") || type.name().contains("BARREL") || type.name().contains("HOPPER") || type.name().contains("DROPPER") || type.name().contains("DISPENSER") || type.name().contains("BREWING_STAND"))) {
            return;
        }
        if (this.plugin.getDataManager().getWeaponType(player.getUniqueId()) != WeaponType.SHIELD) {
            return;
        }
        if (!player.isSneaking()) {
            return;
        }
        boolean bl = holdingShield = player.getInventory().getItemInMainHand().getType() == Material.SHIELD || player.getInventory().getItemInOffHand().getType() == Material.SHIELD;
        if (!holdingShield) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - (lastUse = this.plugin.getDataManager().getAbilityCooldown(player.getUniqueId(), "shield_reflect")) < 40000L) {
            long remaining = (40000L - (now - lastUse)) / 1000L;
            MessageUtil.sendWithDefault(player, "abilities.ability-cooldown", "§cAbility on cooldown! (" + remaining + "s)", "remaining", String.valueOf(remaining));
            return;
        }
        this.triggerReflect(player);
        this.plugin.getDataManager().setAbilityCooldown(player.getUniqueId(), "shield_reflect", now);
    }

    private void triggerReflect(Player player) {
        this.reflecting.add(player.getUniqueId());
        player.addPotionEffect(new PotionEffect(CompatUtil.resolvePotionEffect("RESISTANCE", "DAMAGE_RESISTANCE"), 140, 1));
        MessageUtil.sendWithDefault(player, "abilities.shield-active", "§6§lSHIELD ACTIVE §r§7— Reflecting all damage & Resistance II for 7s!");
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.5f);
        CompatUtil.spawnParticle(player.getWorld(), "ENCHANTMENT_TABLE", player.getLocation(), 100, 0.5, 1.0, 0.5, 0.1);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            this.reflecting.remove(player.getUniqueId());
            if (player.isOnline()) {
                MessageUtil.sendWithDefault(player, "abilities.shield-ended", "§cReflect has worn off.");
            }
        }, 140L);
    }
}

