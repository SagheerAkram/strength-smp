package com.floki.strengthsmp.services;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.DataManager;
import com.floki.strengthsmp.util.ItemFactory;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

/**
 * Handles the Strength economy logic, including gains, losses, and rewards.
 */
public class StrengthService {

    private final StrengthSMP plugin;
    private final DataManager dataManager;

    public StrengthService(StrengthSMP plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
    }

    /**
     * Adds strength to a player. If at max, gives a physical item reward instead if they are online.
     * If offline, drops at the provided location (usually the victim's location).
     */
    public void addStrength(org.bukkit.OfflinePlayer player, int amount, org.bukkit.Location fallbackLocation) {
        if (player.isOnline()) {
            addStrength((Player) player, amount);
            return;
        }

        // Offline logic: update data first
        int current = dataManager.getStrength(player.getUniqueId());
        int max = plugin.getConfigManager().getMaxStrength();
        
        if (current >= max) {
            if (fallbackLocation != null) {
                fallbackLocation.getWorld().dropItemNaturally(fallbackLocation, ItemFactory.createStrengthItem(amount));
            } else {
                // Absolute fallback: if no location provided, we can't drop it. 
                // In production, fallbackLocation should always be passed from DeathListener.
                plugin.getLogger().warning("Offline reward LOST for " + player.getName() + " - No fallback location provided.");
            }
            return;
        }

        int added = dataManager.addStrengthCapped(player.getUniqueId(), amount);
        // Monarch logic removed from here
        
        // Handle leftovers if capped offline
        if (added < amount && fallbackLocation != null) {
            fallbackLocation.getWorld().dropItemNaturally(fallbackLocation, ItemFactory.createStrengthItem(amount - added));
        }
    }

    /**
     * Adds strength to an online player with sounds and effects.
     */
    public void addStrength(Player player, int amount) {
        int current = dataManager.getStrength(player.getUniqueId());
        int max = plugin.getConfigManager().getMaxStrength();
        
        if (current >= max) {
            handleMaxStrengthReward(player, amount);
            return;
        }

        int added = dataManager.addStrengthCapped(player.getUniqueId(), amount);
        int next = current + added;

        MessageUtil.send(player, "strength.gain", "strength", String.valueOf(next));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);

        // Update UI
        plugin.updateDisplay(player);
        
        // Progress Contracts
        plugin.getContractService().progressContract(player, "STRENGTH", added);

        // If we still have "leftover" strength because we hit the cap mid-way
        if (added < amount) {
            handleMaxStrengthReward(player, amount - added);
        }
    }

    public void removeStrength(org.bukkit.OfflinePlayer player, int amount) {
        if (player.isOnline()) {
            removeStrength((Player) player, amount);
            return;
        }

        // Offline logic: just update data
        int current = dataManager.getStrength(player.getUniqueId());
        int min = plugin.getConfigManager().getMinStrength();
        if (current <= min) return;

        dataManager.subtractStrength(player.getUniqueId(), amount);
    }

    public void removeStrength(Player player, int amount) {
        int current = dataManager.getStrength(player.getUniqueId());
        int min = plugin.getConfigManager().getMinStrength();
        
        if (current <= min) {
            MessageUtil.send(player, "strength.at-minimum");
            return;
        }

        dataManager.subtractStrength(player.getUniqueId(), amount);
        int next = Math.max(min, current - amount);
        
        MessageUtil.send(player, "strength.loss", "strength", String.valueOf(next));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
        
        // Update UI
        plugin.updateDisplay(player);
    }

    /**
     * Converts excess strength into physical Strength Items.
     */
    private void handleMaxStrengthReward(Player player, int amount) {
        ItemStack reward = ItemFactory.createStrengthItem(amount);
        
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(reward);
        if (!leftover.isEmpty()) {
            player.getWorld().dropItem(player.getLocation(), leftover.values().iterator().next());
        }

        MessageUtil.send(player, "strength.max-reached");
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 0.5f);
    }
}
