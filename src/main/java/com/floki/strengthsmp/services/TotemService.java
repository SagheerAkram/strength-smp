/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.PlayerInventory
 */
package com.floki.strengthsmp.services;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.util.MessageUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class TotemService {
    private final StrengthSMP plugin;
    private final Map<UUID, Long> lastMessageTime = new HashMap<UUID, Long>();

    public TotemService(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    public int countTotems(Player player) {
        int count = 0;
        PlayerInventory inv = player.getInventory();
        for (ItemStack item : inv.getContents()) {
            if (item == null || item.getType() != Material.TOTEM_OF_UNDYING) continue;
            count += item.getAmount();
        }
        return count;
    }

    public boolean canReceiveTotems(Player player, int amount) {
        int max;
        if (!this.plugin.getConfigManager().isTotemLimitEnabled()) {
            return true;
        }
        int current = this.countTotems(player);
        return current + amount <= (max = this.plugin.getConfigManager().getTotemMax());
    }

    public void sanitizeTotems(Player player) {
        ItemStack offhand;
        ItemStack drop;
        int toTake;
        int amountInSlot;
        int i;
        int max;
        ItemStack cursor;
        int cursorCount;
        if (!this.plugin.getConfigManager().isTotemLimitEnabled()) {
            return;
        }
        int invCount = this.countTotems(player);
        int total = invCount + (cursorCount = (cursor = player.getItemOnCursor()) != null && cursor.getType() == Material.TOTEM_OF_UNDYING ? cursor.getAmount() : 0);
        if (total <= (max = this.plugin.getConfigManager().getTotemMax())) {
            return;
        }
        int toRemove = total - max;
        int removed = 0;
        if (cursorCount > 0) {
            int toTake2 = Math.min(cursorCount, toRemove);
            ItemStack drop2 = cursor.clone();
            drop2.setAmount(toTake2);
            if (this.plugin.getConfigManager().isTotemDropExcess()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop2);
            }
            if (toTake2 == cursorCount) {
                player.setItemOnCursor(null);
            } else {
                cursor.setAmount(cursorCount - toTake2);
                player.setItemOnCursor(cursor);
            }
            removed += toTake2;
        }
        PlayerInventory inv = player.getInventory();
        for (i = 35; i >= 9 && removed < toRemove; removed += this.removeAndDrop(player, inv, i, toRemove - removed), --i) {
        }
        for (i = 8; i >= 0 && removed < toRemove; removed += this.removeAndDrop(player, inv, i, toRemove - removed), --i) {
        }
        ItemStack[] armor = inv.getArmorContents();
        for (int i2 = armor.length - 1; i2 >= 0 && removed < toRemove; --i2) {
            if (armor[i2] == null || armor[i2].getType() != Material.TOTEM_OF_UNDYING) continue;
            amountInSlot = armor[i2].getAmount();
            toTake = Math.min(amountInSlot, toRemove - removed);
            drop = armor[i2].clone();
            drop.setAmount(toTake);
            if (this.plugin.getConfigManager().isTotemDropExcess()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
            if (toTake == amountInSlot) {
                armor[i2] = null;
            } else {
                armor[i2].setAmount(amountInSlot - toTake);
            }
            removed += toTake;
        }
        inv.setArmorContents(armor);
        if (removed < toRemove && (offhand = inv.getItemInOffHand()) != null && offhand.getType() == Material.TOTEM_OF_UNDYING) {
            amountInSlot = offhand.getAmount();
            toTake = Math.min(amountInSlot, toRemove - removed);
            drop = offhand.clone();
            drop.setAmount(toTake);
            if (this.plugin.getConfigManager().isTotemDropExcess()) {
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
        if (removed > 0) {
            this.sendWarning(player);
        }
    }

    private int removeAndDrop(Player player, PlayerInventory inv, int slot, int amountNeeded) {
        ItemStack item = inv.getItem(slot);
        if (item == null || item.getType() != Material.TOTEM_OF_UNDYING) {
            return 0;
        }
        int amountInSlot = item.getAmount();
        int toTake = Math.min(amountInSlot, amountNeeded);
        if (toTake == amountInSlot) {
            if (this.plugin.getConfigManager().isTotemDropExcess()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
            inv.setItem(slot, null);
        } else {
            ItemStack drop = item.clone();
            drop.setAmount(toTake);
            if (this.plugin.getConfigManager().isTotemDropExcess()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
            item.setAmount(amountInSlot - toTake);
            inv.setItem(slot, item);
        }
        return toTake;
    }

    public void sendWarning(Player player) {
        int cooldown;
        long last;
        long now = System.currentTimeMillis();
        if (now - (last = this.lastMessageTime.getOrDefault(player.getUniqueId(), 0L).longValue()) > (long)(cooldown = this.plugin.getConfigManager().getTotemMessageCooldown() * 1000)) {
            MessageUtil.send((CommandSender)player, "totem.limit-exceeded", "max", String.valueOf(this.plugin.getConfigManager().getTotemMax()));
            this.lastMessageTime.put(player.getUniqueId(), now);
        }
    }
}

