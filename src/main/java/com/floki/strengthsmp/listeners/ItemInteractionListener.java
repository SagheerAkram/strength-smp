/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
 *  org.bukkit.Sound
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Item
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.EntityDamageEvent
 *  org.bukkit.event.entity.ItemDespawnEvent
 *  org.bukkit.event.entity.ItemMergeEvent
 *  org.bukkit.event.entity.ItemSpawnEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 */
package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.WeaponType;
import com.floki.strengthsmp.gui.WeaponShuffleGUI;
import com.floki.strengthsmp.util.ItemFactory;
import com.floki.strengthsmp.util.MessageUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ItemInteractionListener
implements Listener {
    private final StrengthSMP plugin;
    private final NamespacedKey rerollKey;
    private final Random random = new Random();

    public ItemInteractionListener(StrengthSMP plugin) {
        this.plugin = plugin;
        this.rerollKey = new NamespacedKey((Plugin)plugin, "smp_reroll_token");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        if (!item.hasItemMeta()) {
            return;
        }
        if (!ItemFactory.isRerollItem(item)) {
            return;
        }
        event.setCancelled(true);
        int strength = this.plugin.getDataManager().getStrength(player.getUniqueId());
        WeaponType current = this.plugin.getDataManager().getWeapon(player.getUniqueId());
        if (current != null && strength < 1) {
            MessageUtil.send((CommandSender)player, "strength.insufficient", "required", "1");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        ArrayList<WeaponType> available = new ArrayList<WeaponType>(Arrays.asList(WeaponType.values()));
        available.remove((Object)current);
        if (available.isEmpty()) {
            player.sendMessage("\u00a7cNo other weapon types available!");
            return;
        }
        if (current != null) {
            this.plugin.getDataManager().subtractStrength(player.getUniqueId(), 1);
            this.plugin.updateDisplay(player);
            this.plugin.getMonarchService().calculateNewMonarch();
        }
        item.setAmount(item.getAmount() - 1);
        new WeaponShuffleGUI(this.plugin, player).open();
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
    }

    private String getWeaponHex(WeaponType type) {
        switch (type) {
            case SWORD: {
                return "&#e74c3c";
            }
            case AXE: {
                return "&#e67e22";
            }
            case TRIDENT: {
                return "&#3498db";
            }
            case SHIELD: {
                return "&#f1c40f";
            }
            case BOW: {
                return "&#2ecc71";
            }
            case CROSSBOW: {
                return "&#9b59b6";
            }
        }
        return "&#ffffff";
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        Item itemEntity = event.getEntity();
        ItemStack stack = itemEntity.getItemStack();
        if (ItemFactory.isRerollItem(stack) || ItemFactory.isStrengthItem(stack)) {
            itemEntity.setUnlimitedLifetime(true);
            itemEntity.setInvulnerable(true);
            itemEntity.setPersistent(true);
        }
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        Item itemEntity = event.getEntity();
        ItemStack stack = itemEntity.getItemStack();
        if (ItemFactory.isRerollItem(stack) || ItemFactory.isStrengthItem(stack)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDamage(EntityDamageEvent event) {
        Item itemEntity;
        ItemStack stack;
        Entity entity = event.getEntity();
        if (entity instanceof Item && (ItemFactory.isRerollItem(stack = (itemEntity = (Item)entity).getItemStack()) || ItemFactory.isStrengthItem(stack))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemMerge(ItemMergeEvent event) {
        Item target = event.getTarget();
        Item entity = event.getEntity();
        ItemStack stack = target.getItemStack();
        if (ItemFactory.isRerollItem(stack) || ItemFactory.isStrengthItem(stack)) {
            target.setUnlimitedLifetime(true);
            target.setInvulnerable(true);
            target.setPersistent(true);
            entity.setUnlimitedLifetime(true);
            entity.setInvulnerable(true);
            entity.setPersistent(true);
        }
    }
}

