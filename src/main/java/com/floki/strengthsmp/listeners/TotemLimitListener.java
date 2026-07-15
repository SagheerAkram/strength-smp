/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.EntityPickupItemEvent
 *  org.bukkit.event.inventory.ClickType
 *  org.bukkit.event.inventory.InventoryAction
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.inventory.InventoryCloseEvent
 *  org.bukkit.event.inventory.InventoryDragEvent
 *  org.bukkit.event.inventory.InventoryType
 *  org.bukkit.event.player.PlayerArmorStandManipulateEvent
 *  org.bukkit.event.player.PlayerInteractEntityEvent
 *  org.bukkit.event.player.PlayerJoinEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.event.player.PlayerSwapHandItemsEvent
 *  org.bukkit.inventory.ItemStack
 */
package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.services.TotemService;
import java.util.Iterator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public class TotemLimitListener
implements Listener {
    private final StrengthSMP plugin;
    private final TotemService totemService;

    public TotemLimitListener(StrengthSMP plugin, TotemService totemService) {
        this.plugin = plugin;
        this.totemService = totemService;
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        this.totemService.sanitizeTotems(event.getPlayer());
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onPickup(EntityPickupItemEvent event) {
        int max;
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (!this.plugin.getConfigManager().isTotemLimitEnabled()) {
            return;
        }
        Player player = (Player)event.getEntity();
        ItemStack item = event.getItem().getItemStack();
        if (item.getType() != Material.TOTEM_OF_UNDYING) {
            return;
        }
        int current = this.totemService.countTotems(player);
        if (current >= (max = this.plugin.getConfigManager().getTotemMax())) {
            event.setCancelled(true);
            this.totemService.sendWarning(player);
            return;
        }
        if (current + item.getAmount() > max) {
            int allowed = max - current;
            int remaining = item.getAmount() - allowed;
            item.setAmount(remaining);
            event.getItem().setItemStack(item);
            ItemStack toAdd = item.clone();
            toAdd.setAmount(allowed);
            player.getInventory().addItem(new ItemStack[]{toAdd});
            event.setCancelled(true);
            this.totemService.sendWarning(player);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack cursor;
        int total;
        if (!this.plugin.getConfigManager().isTotemLimitEnabled()) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player)event.getWhoClicked();
        ItemStack currentItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();
        int max = this.plugin.getConfigManager().getTotemMax();
        if (currentItem != null && currentItem.getType() == Material.TOTEM_OF_UNDYING && (event.getAction() == InventoryAction.PICKUP_ALL || event.getAction() == InventoryAction.PICKUP_HALF || event.getAction() == InventoryAction.PICKUP_ONE || event.getAction() == InventoryAction.PICKUP_SOME || event.getAction() == InventoryAction.CLONE_STACK)) {
            total = this.totemService.countTotems(player);
            cursor = player.getItemOnCursor();
            if (cursor != null && cursor.getType() == Material.TOTEM_OF_UNDYING) {
                total += cursor.getAmount();
            }
            if (total >= max) {
                event.setCancelled(true);
                this.totemService.sendWarning(player);
                return;
            }
        }
        if (event.getInventory().getType() == InventoryType.MERCHANT && event.getRawSlot() == 2 && currentItem != null && currentItem.getType() == Material.TOTEM_OF_UNDYING) {
            total = this.totemService.countTotems(player);
            cursor = player.getItemOnCursor();
            if (cursor != null && cursor.getType() == Material.TOTEM_OF_UNDYING) {
                total += cursor.getAmount();
            }
            if (total >= max) {
                event.setCancelled(true);
                this.totemService.sendWarning(player);
                return;
            }
        }
        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            if (event.getClickedInventory() != null && event.getClickedInventory().getType() != InventoryType.PLAYER && currentItem != null && currentItem.getType() == Material.TOTEM_OF_UNDYING) {
                total = this.totemService.countTotems(player);
                cursor = player.getItemOnCursor();
                if (cursor != null && cursor.getType() == Material.TOTEM_OF_UNDYING) {
                    total += cursor.getAmount();
                }
                if (total + currentItem.getAmount() > max) {
                    event.setCancelled(true);
                    this.totemService.sendWarning(player);
                }
            }
            return;
        }
        if (event.getClick() == ClickType.NUMBER_KEY) {
            if (event.getClickedInventory() != null && event.getClickedInventory().getType() != InventoryType.PLAYER && currentItem != null && currentItem.getType() == Material.TOTEM_OF_UNDYING) {
                total = this.totemService.countTotems(player);
                cursor = player.getItemOnCursor();
                if (cursor != null && cursor.getType() == Material.TOTEM_OF_UNDYING) {
                    total += cursor.getAmount();
                }
                if (total + currentItem.getAmount() > max) {
                    event.setCancelled(true);
                    this.totemService.sendWarning(player);
                }
            }
            return;
        }
        if (cursorItem != null && cursorItem.getType() == Material.TOTEM_OF_UNDYING && event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.PLAYER && this.totemService.countTotems(player) + cursorItem.getAmount() > max) {
            event.setCancelled(true);
            this.totemService.sendWarning(player);
        }
        if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR && cursorItem != null && cursorItem.getType() == Material.TOTEM_OF_UNDYING && this.totemService.countTotems(player) + cursorItem.getAmount() >= max) {
            event.setCancelled(true);
            this.totemService.sendWarning(player);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onDrag(InventoryDragEvent event) {
        int total;
        if (!this.plugin.getConfigManager().isTotemLimitEnabled()) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player)event.getWhoClicked();
        ItemStack item = event.getOldCursor();
        if (item.getType() != Material.TOTEM_OF_UNDYING) {
            return;
        }
        boolean toPlayerInv = false;
        Iterator iterator = event.getRawSlots().iterator();
        while (iterator.hasNext()) {
            int slot = (Integer)iterator.next();
            if (event.getView().getInventory(slot).getType() != InventoryType.PLAYER) continue;
            toPlayerInv = true;
            break;
        }
        if (toPlayerInv && (total = this.totemService.countTotems(player)) + item.getAmount() > this.plugin.getConfigManager().getTotemMax()) {
            event.setCancelled(true);
            this.totemService.sendWarning(player);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onArmorStand(PlayerArmorStandManipulateEvent event) {
        if (!this.plugin.getConfigManager().isTotemLimitEnabled()) {
            return;
        }
        ItemStack item = event.getArmorStandItem();
        if (item != null && item.getType() == Material.TOTEM_OF_UNDYING && this.totemService.countTotems(event.getPlayer()) >= this.plugin.getConfigManager().getTotemMax()) {
            event.setCancelled(true);
            this.totemService.sendWarning(event.getPlayer());
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (!this.plugin.getConfigManager().isTotemLimitEnabled()) {
            return;
        }
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            this.totemService.sanitizeTotems((Player)event.getPlayer());
        }
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        this.totemService.sanitizeTotems(event.getPlayer());
    }
}

