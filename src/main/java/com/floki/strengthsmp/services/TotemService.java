package com.floki.strengthsmp.services;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service to manage totem limits and inventory sanitization.
 */
public class TotemService {

    private final StrengthSMP plugin;
    private final Map<UUID, Long> lastMessageTime = new HashMap<>();

    public TotemService(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    /**
     * Counts the total number of totems in a player's possession.
     * Includes main inventory, armor, and offhand.
     */
    public int countTotems(Player player) {
        int count = 0;
        PlayerInventory inv = player.getInventory();

        // Check all contents (storage, armor, extra)
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType() == Material.TOTEM_OF_UNDYING) {
                count += item.getAmount();
            }
        }
        
        return count;
    }

    /**
     * Checks if a player can receive a specific amount of totems.
     */
    public boolean canReceiveTotems(Player player, int amount) {
        if (!plugin.getConfigManager().isTotemLimitEnabled()) return true;
        
        int current = countTotems(player);
        int max = plugin.getConfigManager().getTotemMax();
        
        return (current + amount) <= max;
    }

    /**
     * Sanitizes a player's inventory to ensure they don't exceed the totem cap.
     * Priority: Offhand > Hotbar > Main Inventory.
     * Excess totems are dropped at the player's location.
     */
    public void sanitizeTotems(Player player) {
        if (!plugin.getConfigManager().isTotemLimitEnabled()) return;

        int invCount = countTotems(player);
        ItemStack cursor = player.getItemOnCursor();
        int cursorCount = (cursor != null && cursor.getType() == Material.TOTEM_OF_UNDYING) ? cursor.getAmount() : 0;
        
        int total = invCount + cursorCount;
        int max = plugin.getConfigManager().getTotemMax();

        if (total <= max) return;

        int toRemove = total - max;
        int removed = 0;

        // 0. Remove from Cursor FIRST (if player is trying to drag/glitch it)
        if (cursorCount > 0) {
            int toTake = Math.min(cursorCount, toRemove);
            
            ItemStack drop = cursor.clone();
            drop.setAmount(toTake);
            if (plugin.getConfigManager().isTotemDropExcess()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
            
            if (toTake == cursorCount) {
                player.setItemOnCursor(null);
            } else {
                cursor.setAmount(cursorCount - toTake);
                player.setItemOnCursor(cursor);
            }
            removed += toTake;
        }

        PlayerInventory inv = player.getInventory();

        // 1. Remove from Main Inventory (35 -> 9)
        for (int i = 35; i >= 9 && removed < toRemove; i--) {
            removed += removeAndDrop(player, inv, i, toRemove - removed);
        }

        // 2. Remove from Hotbar (8 -> 0)
        for (int i = 8; i >= 0 && removed < toRemove; i--) {
            removed += removeAndDrop(player, inv, i, toRemove - removed);
        }

        // 3. Remove from Armor (just in case)
        ItemStack[] armor = inv.getArmorContents();
        for (int i = armor.length - 1; i >= 0 && removed < toRemove; i--) {
            if (armor[i] != null && armor[i].getType() == Material.TOTEM_OF_UNDYING) {
                int amountInSlot = armor[i].getAmount();
                int toTake = Math.min(amountInSlot, toRemove - removed);
                
                ItemStack drop = armor[i].clone();
                drop.setAmount(toTake);
                if (plugin.getConfigManager().isTotemDropExcess()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
                
                if (toTake == amountInSlot) {
                    armor[i] = null;
                } else {
                    armor[i].setAmount(amountInSlot - toTake);
                }
                removed += toTake;
            }
        }
        inv.setArmorContents(armor);

        // 4. Remove from Offhand (lowest priority to remove)
        if (removed < toRemove) {
            ItemStack offhand = inv.getItemInOffHand();
            if (offhand != null && offhand.getType() == Material.TOTEM_OF_UNDYING) {
                int amountInSlot = offhand.getAmount();
                int toTake = Math.min(amountInSlot, toRemove - removed);
                
                ItemStack drop = offhand.clone();
                drop.setAmount(toTake);
                if (plugin.getConfigManager().isTotemDropExcess()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
                
                if (toTake == amountInSlot) {
                    inv.setItemInOffHand(null);
                } else {
                    offhand.setAmount(amountInSlot - toTake);
                    inv.setItemInOffHand(offhand);
                }
                removed += toTake;
            }
        }

        if (removed > 0) {
            sendWarning(player);
        }
    }

    private int removeAndDrop(Player player, PlayerInventory inv, int slot, int amountNeeded) {
        ItemStack item = inv.getItem(slot);
        if (item == null || item.getType() != Material.TOTEM_OF_UNDYING) return 0;

        int amountInSlot = item.getAmount();
        int toTake = Math.min(amountInSlot, amountNeeded);

        if (toTake == amountInSlot) {
            if (plugin.getConfigManager().isTotemDropExcess()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
            inv.setItem(slot, null);
        } else {
            ItemStack drop = item.clone();
            drop.setAmount(toTake);
            if (plugin.getConfigManager().isTotemDropExcess()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
            item.setAmount(amountInSlot - toTake);
            inv.setItem(slot, item);
        }

        return toTake;
    }

    /**
     * Sends a warning message to the player with a cooldown.
     */
    public void sendWarning(Player player) {
        long now = System.currentTimeMillis();
        long last = lastMessageTime.getOrDefault(player.getUniqueId(), 0L);
        int cooldown = plugin.getConfigManager().getTotemMessageCooldown() * 1000;

        if (now - last > cooldown) {
            MessageUtil.send(player, "totem.limit-exceeded", "max", String.valueOf(plugin.getConfigManager().getTotemMax()));
            lastMessageTime.put(player.getUniqueId(), now);
        }
    }
}
