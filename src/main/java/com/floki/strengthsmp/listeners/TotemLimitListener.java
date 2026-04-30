package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.services.TotemService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener to enforce Totem of Undying limits.
 */
public class TotemLimitListener implements Listener {

    private final StrengthSMP plugin;
    private final TotemService totemService;

    public TotemLimitListener(StrengthSMP plugin, TotemService totemService) {
        this.plugin = plugin;
        this.totemService = totemService;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        // Sanitize immediately on join
        totemService.sanitizeTotems(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!plugin.getConfigManager().isTotemLimitEnabled()) return;

        Player player = (Player) event.getEntity();
        ItemStack item = event.getItem().getItemStack();

        if (item.getType() != Material.TOTEM_OF_UNDYING) return;

        int current = totemService.countTotems(player);
        int max = plugin.getConfigManager().getTotemMax();

        if (current >= max) {
            event.setCancelled(true);
            totemService.sendWarning(player);
            return;
        }

        // If picking up a stack that would exceed the limit
        if (current + item.getAmount() > max) {
            int allowed = max - current;
            int remaining = item.getAmount() - allowed;

            // Partial pickup
            item.setAmount(remaining);
            event.getItem().setItemStack(item);

            ItemStack toAdd = item.clone();
            toAdd.setAmount(allowed);
            player.getInventory().addItem(toAdd);

            event.setCancelled(true); // Cancel full pickup
            totemService.sendWarning(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!plugin.getConfigManager().isTotemLimitEnabled()) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack currentItem = event.getCurrentItem(); // Item in slot being clicked
        ItemStack cursorItem = event.getCursor();     // Item on cursor

        int max = plugin.getConfigManager().getTotemMax();

        // 0. Catch-all: If picking up a totem to cursor (from any inventory)
        if (currentItem != null && currentItem.getType() == Material.TOTEM_OF_UNDYING) {
            // If the action results in the item being on the cursor
            if (event.getAction() == InventoryAction.PICKUP_ALL || 
                event.getAction() == InventoryAction.PICKUP_HALF || 
                event.getAction() == InventoryAction.PICKUP_ONE || 
                event.getAction() == InventoryAction.PICKUP_SOME ||
                event.getAction() == InventoryAction.CLONE_STACK) { // Creative middle-click
                
                // When picking up to cursor, the item IS NOT yet on the cursor.
                // We check: current_inv + current_cursor >= max
                int total = totemService.countTotems(player);
                ItemStack cursor = player.getItemOnCursor();
                if (cursor != null && cursor.getType() == Material.TOTEM_OF_UNDYING) {
                    total += cursor.getAmount();
                }

                if (total >= max) {
                    event.setCancelled(true);
                    totemService.sendWarning(player);
                    return;
                }
            }
        }

        // 0.5 Merchant / Villager result slot
        if (event.getInventory().getType() == InventoryType.MERCHANT && event.getRawSlot() == 2) {
            if (currentItem != null && currentItem.getType() == Material.TOTEM_OF_UNDYING) {
                int total = totemService.countTotems(player);
                ItemStack cursor = player.getItemOnCursor();
                if (cursor != null && cursor.getType() == Material.TOTEM_OF_UNDYING) {
                    total += cursor.getAmount();
                }

                if (total >= max) {
                    event.setCancelled(true);
                    totemService.sendWarning(player);
                    return;
                }
            }
        }

        // 1. Shift Click into player inventory
        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            if (event.getClickedInventory() != null && event.getClickedInventory().getType() != InventoryType.PLAYER) {
                if (currentItem != null && currentItem.getType() == Material.TOTEM_OF_UNDYING) {
                    int total = totemService.countTotems(player);
                    ItemStack cursor = player.getItemOnCursor();
                    if (cursor != null && cursor.getType() == Material.TOTEM_OF_UNDYING) {
                        total += cursor.getAmount();
                    }

                    if (total + currentItem.getAmount() > max) {
                        event.setCancelled(true);
                        totemService.sendWarning(player);
                    }
                }
            }
            return;
        }

        // 2. Hotbar Number Key Swap
        if (event.getClick() == ClickType.NUMBER_KEY) {
            // Check if player is trying to move a totem FROM another inventory INTO their hotbar
            if (event.getClickedInventory() != null && event.getClickedInventory().getType() != InventoryType.PLAYER) {
                // If moving totem FROM chest TO hotbar
                if (currentItem != null && currentItem.getType() == Material.TOTEM_OF_UNDYING) {
                    int total = totemService.countTotems(player);
                    ItemStack cursor = player.getItemOnCursor();
                    if (cursor != null && cursor.getType() == Material.TOTEM_OF_UNDYING) {
                        total += cursor.getAmount();
                    }

                    if (total + currentItem.getAmount() > max) {
                        event.setCancelled(true);
                        totemService.sendWarning(player);
                    }
                }
            }
            return;
        }

        // 3. Placing totem from cursor into inventory
        if (cursorItem != null && cursorItem.getType() == Material.TOTEM_OF_UNDYING) {
            // Only care if clicking INTO player inventory
            if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.PLAYER) {
                // If the slot is NOT a totem (so we are adding new totems to the inventory count)
                // If it IS a totem, we are just swapping or stacking, but we need to be careful.
                
                // Simplified: if we are adding ANY amount from cursor to player inv, 
                // and that amount is not already in the player inv.
                // But the cursor IS NOT counted in countTotems(player).
                
                if (totemService.countTotems(player) + cursorItem.getAmount() > max) {
                    // Check if we are clicking a slot that already has totems (stacking)
                    // countTotems already includes all slots.
                    event.setCancelled(true);
                    totemService.sendWarning(player);
                }
            }
        }
        
        // 4. Collect to cursor (double click)
        if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            if (cursorItem != null && cursorItem.getType() == Material.TOTEM_OF_UNDYING) {
                // This pulls from everywhere.
                if (totemService.countTotems(player) + cursorItem.getAmount() >= max) {
                    event.setCancelled(true);
                    totemService.sendWarning(player);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        if (!plugin.getConfigManager().isTotemLimitEnabled()) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getOldCursor();

        if (item.getType() != Material.TOTEM_OF_UNDYING) return;

        // Check if any of the slots are in the player inventory
        boolean toPlayerInv = false;
        for (int slot : event.getRawSlots()) {
            if (event.getView().getInventory(slot).getType() == InventoryType.PLAYER) {
                toPlayerInv = true;
                break;
            }
        }

        if (toPlayerInv) {
            int total = totemService.countTotems(player);
            // On drag, cursor is ALREADY the item being dragged (it's the 'old cursor')
            // But Bukkit says event.getOldCursor() is the one before drag started.
            // During drag, the item is distributed, so checking countTotems + item.getAmount() is safe.
            if (total + item.getAmount() > plugin.getConfigManager().getTotemMax()) {
                event.setCancelled(true);
                totemService.sendWarning(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        // Swapping doesn't change total count, but sanitization uses priority.
        // If they swap a totem to offhand, it's fine.
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onArmorStand(PlayerArmorStandManipulateEvent event) {
        if (!plugin.getConfigManager().isTotemLimitEnabled()) return;
        
        ItemStack item = event.getArmorStandItem();
        if (item != null && item.getType() == Material.TOTEM_OF_UNDYING) {
            if (totemService.countTotems(event.getPlayer()) >= plugin.getConfigManager().getTotemMax()) {
                event.setCancelled(true);
                totemService.sendWarning(event.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (!plugin.getConfigManager().isTotemLimitEnabled()) return;
        
        // Covers Item Frames, etc.
        // Usually, these don't "give" items on right click unless it's an armor stand (handled above)
        // but some entities/plugins might. sanitizeTotems on close is the real backup.
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            totemService.sanitizeTotems((Player) event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        // Final sanitize on quit (covers cursor items being dropped/injected)
        totemService.sanitizeTotems(event.getPlayer());
    }
}
