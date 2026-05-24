package com.floki.strengthsmp.services;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.DataManager;
import com.floki.strengthsmp.util.CompatUtil;
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
        plugin.getMonarchService().calculateNewMonarch();
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
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.5f);
        CompatUtil.spawnParticle(player.getWorld(), "TOTEM", player.getLocation().add(0, 1, 0), 40, 0.5, 0.5, 0.5, 0.15);
        CompatUtil.spawnParticle(player.getWorld(), "VILLAGER_HAPPY", player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);

        // Update UI
        plugin.updateDisplay(player);
        plugin.getMonarchService().calculateNewMonarch();
        
        // Progress Contracts
        plugin.getContractService().progressContract(player, "STRENGTH", added);

        // If we still have "leftover" strength because we hit the cap mid-way
        if (added < amount) {
            handleMaxStrengthReward(player, amount - added);
        }
    }

    public boolean removeStrength(org.bukkit.OfflinePlayer player, int amount) {
        if (player.isOnline()) {
            return removeStrength((Player) player, amount);
        }

        // Offline logic: just update data
        int current = dataManager.getStrength(player.getUniqueId());
        int min = plugin.getConfigManager().getMinStrength();
        if (current <= min) return false;

        dataManager.subtractStrength(player.getUniqueId(), amount);
        plugin.getMonarchService().calculateNewMonarch();
        return true;
    }

    public boolean removeStrength(Player player, int amount) {
        int current = dataManager.getStrength(player.getUniqueId());
        int min = plugin.getConfigManager().getMinStrength();
        
        if (current <= min) {
            MessageUtil.send(player, "strength.at-minimum");
            return false;
        }

        dataManager.subtractStrength(player.getUniqueId(), amount);
        int next = Math.max(min, current - amount);
        
        MessageUtil.send(player, "strength.loss", "strength", String.valueOf(next));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.5f, 0.8f);
        CompatUtil.spawnParticle(player.getWorld(), "SMOKE_LARGE", player.getLocation().add(0, 1, 0), 40, 0.5, 0.5, 0.5, 0.1);
        CompatUtil.spawnParticle(player.getWorld(), "SOUL", player.getLocation().add(0, 1, 0), 15, 0.3, 0.3, 0.3, 0.05);
        
        // Update UI
        plugin.updateDisplay(player);
        plugin.getMonarchService().calculateNewMonarch();
        return true;
    }

    public void handleKill(Player attacker, Player victim) {
        java.util.UUID attackerUUID = attacker.getUniqueId();
        java.util.UUID victimUUID = victim.getUniqueId();

        boolean victimProtected = false;

        // 1. Check Newbie Protection on Victim
        if (dataManager.isNewbieProtected(victimUUID)) {
            MessageUtil.send(victim, "strength.newbie-you-protected");
            MessageUtil.send(attacker, "strength.newbie-protected");
            victimProtected = true;
        }
        // 2. Check Death Protection on Victim
        else if (dataManager.hasDeathProtection(victimUUID)) {
            MessageUtil.send(victim, "strength.protected");
            MessageUtil.send(attacker, "strength.protection-blocked");
            victimProtected = true;
        }

        // 3. Check Anti-Farm
        boolean isFarm = false;
        if (!victimProtected) {
            long lastKill = dataManager.getLastKillTime(attackerUUID, victimUUID);
            long windowMs = plugin.getConfigManager().getAntiFarmWindowMinutes() * 60 * 1000L;
            if (lastKill > 0 && (System.currentTimeMillis() - lastKill) < windowMs) {
                MessageUtil.send(attacker, "strength.anti-farm");
                isFarm = true;
            }
        }

        // 4. Check Victim Minimum Strength
        boolean victimAtMin = false;
        int victimStr = dataManager.getStrength(victimUUID);
        int minStrength = plugin.getConfigManager().getMinStrength();
        if (!victimProtected && !isFarm && victimStr <= minStrength) {
            MessageUtil.send(victim, "strength.at-minimum");
            MessageUtil.send(attacker, "strength.victim-at-minimum", "victim", victim.getName());
            victimAtMin = true;
        }

        // ── Do strength adjustments ──
        if (!victimProtected && !isFarm && !victimAtMin) {
            // Victim loses strength
            removeStrength(victim, 1);

            // Attacker gains strength unless attacker is protected
            if (dataManager.isNewbieProtected(attackerUUID)) {
                MessageUtil.send(attacker, "strength.newbie-no-gain");
            } else if (dataManager.hasDeathProtection(attackerUUID)) {
                MessageUtil.send(attacker, "strength.protected-no-gain");
            } else {
                addStrength(attacker, 1);
            }
        }

        // Anti-farm: record the kill
        dataManager.recordKill(attackerUUID, victimUUID);
        
        // Progress killer's kills stat
        dataManager.addKill(attackerUUID);
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

        MessageUtil.send(player, "strength.max-reached", "amount", String.valueOf(plugin.getConfigManager().getMaxStrength()));
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 0.5f);
    }
}
