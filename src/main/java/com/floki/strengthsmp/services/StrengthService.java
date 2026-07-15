/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.Sound
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package com.floki.strengthsmp.services;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.DataManager;
import com.floki.strengthsmp.util.CompatUtil;
import com.floki.strengthsmp.util.ItemFactory;
import com.floki.strengthsmp.util.MessageUtil;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class StrengthService {
    private final StrengthSMP plugin;
    private final DataManager dataManager;

    public StrengthService(StrengthSMP plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
    }

    public void addStrength(OfflinePlayer player, int amount, Location fallbackLocation) {
        int max;
        if (player.isOnline()) {
            this.addStrength((Player)player, amount);
            return;
        }
        int current = this.dataManager.getStrength(player.getUniqueId());
        if (current >= (max = this.plugin.getConfigManager().getMaxStrength())) {
            if (fallbackLocation != null) {
                fallbackLocation.getWorld().dropItemNaturally(fallbackLocation, ItemFactory.createStrengthItem(amount));
            } else {
                this.plugin.getLogger().warning("Offline reward LOST for " + player.getName() + " - No fallback location provided.");
            }
            return;
        }
        int added = this.dataManager.addStrengthCapped(player.getUniqueId(), amount);
        this.plugin.getMonarchService().calculateNewMonarch();
        if (added < amount && fallbackLocation != null) {
            fallbackLocation.getWorld().dropItemNaturally(fallbackLocation, ItemFactory.createStrengthItem(amount - added));
        }
    }

    public void addStrength(Player player, int amount) {
        int max;
        int current = this.dataManager.getStrength(player.getUniqueId());
        if (current >= (max = this.plugin.getConfigManager().getMaxStrength())) {
            this.handleMaxStrengthReward(player, amount);
            return;
        }
        int added = this.dataManager.addStrengthCapped(player.getUniqueId(), amount);
        int next = current + added;
        MessageUtil.send((CommandSender)player, "strength.gain", "strength", String.valueOf(next));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.5f);
        CompatUtil.spawnParticle(player.getWorld(), "TOTEM", player.getLocation().add(0.0, 1.0, 0.0), 40, 0.5, 0.5, 0.5, 0.15);
        CompatUtil.spawnParticle(player.getWorld(), "VILLAGER_HAPPY", player.getLocation().add(0.0, 1.0, 0.0), 20, 0.5, 0.5, 0.5, 0.1);
        this.plugin.updateDisplay(player);
        this.plugin.getMonarchService().calculateNewMonarch();
        this.plugin.getContractService().progressContract(player, "STRENGTH", added);
        if (added < amount) {
            this.handleMaxStrengthReward(player, amount - added);
        }
    }

    public boolean removeStrength(OfflinePlayer player, int amount) {
        int min;
        if (player.isOnline()) {
            return this.removeStrength((Player)player, amount);
        }
        int current = this.dataManager.getStrength(player.getUniqueId());
        if (current <= (min = this.plugin.getConfigManager().getMinStrength())) {
            return false;
        }
        this.dataManager.subtractStrength(player.getUniqueId(), amount);
        this.plugin.getMonarchService().calculateNewMonarch();
        return true;
    }

    public boolean removeStrength(Player player, int amount) {
        int min;
        int current = this.dataManager.getStrength(player.getUniqueId());
        if (current <= (min = this.plugin.getConfigManager().getMinStrength())) {
            MessageUtil.send((CommandSender)player, "strength.at-minimum", new String[0]);
            return false;
        }
        this.dataManager.subtractStrength(player.getUniqueId(), amount);
        int next = Math.max(min, current - amount);
        MessageUtil.send((CommandSender)player, "strength.loss", "strength", String.valueOf(next));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.5f, 0.8f);
        CompatUtil.spawnParticle(player.getWorld(), "SMOKE_LARGE", player.getLocation().add(0.0, 1.0, 0.0), 40, 0.5, 0.5, 0.5, 0.1);
        CompatUtil.spawnParticle(player.getWorld(), "SOUL", player.getLocation().add(0.0, 1.0, 0.0), 15, 0.3, 0.3, 0.3, 0.05);
        this.plugin.updateDisplay(player);
        this.plugin.getMonarchService().calculateNewMonarch();
        return true;
    }

    public void handleKill(Player attacker, Player victim) {
        UUID attackerUUID = attacker.getUniqueId();
        UUID victimUUID = victim.getUniqueId();
        boolean victimProtected = false;
        if (this.dataManager.isNewbieProtected(victimUUID)) {
            MessageUtil.send((CommandSender)victim, "strength.newbie-you-protected", new String[0]);
            MessageUtil.send((CommandSender)attacker, "strength.newbie-protected", new String[0]);
            victimProtected = true;
        } else if (this.dataManager.hasDeathProtection(victimUUID)) {
            MessageUtil.send((CommandSender)victim, "strength.protected", new String[0]);
            MessageUtil.send((CommandSender)attacker, "strength.protection-blocked", new String[0]);
            victimProtected = true;
        }
        boolean isFarm = false;
        if (!victimProtected) {
            long lastKill = this.dataManager.getLastKillTime(attackerUUID, victimUUID);
            long windowMs = (long)(this.plugin.getConfigManager().getAntiFarmWindowMinutes() * 60) * 1000L;
            if (lastKill > 0L && System.currentTimeMillis() - lastKill < windowMs) {
                MessageUtil.send((CommandSender)attacker, "strength.anti-farm", new String[0]);
                isFarm = true;
            }
        }
        boolean victimAtMin = false;
        int victimStr = this.dataManager.getStrength(victimUUID);
        int minStrength = this.plugin.getConfigManager().getMinStrength();
        if (!victimProtected && !isFarm && victimStr <= minStrength) {
            MessageUtil.send((CommandSender)victim, "strength.at-minimum", new String[0]);
            MessageUtil.send((CommandSender)attacker, "strength.victim-at-minimum", "victim", victim.getName());
            victimAtMin = true;
        }
        if (!(victimProtected || isFarm || victimAtMin)) {
            int penalty = this.plugin.getConfigManager().getDeathPenalty();
            this.removeStrength(victim, penalty);
            if (this.dataManager.isNewbieProtected(attackerUUID)) {
                MessageUtil.send((CommandSender)attacker, "strength.newbie-no-gain", new String[0]);
            } else if (this.dataManager.hasDeathProtection(attackerUUID)) {
                MessageUtil.send((CommandSender)attacker, "strength.protected-no-gain", new String[0]);
            } else {
                int attackerStr = this.dataManager.getStrength(attackerUUID);
                int gain = this.plugin.getConfigManager().getDeathGain();
                int threshold = this.plugin.getConfigManager().getUnderdogThreshold();
                int mult = this.plugin.getConfigManager().getUnderdogMultiplier();
                if (victimStr - attackerStr >= threshold && mult > 1) {
                    gain *= mult;
                    MessageUtil.send((CommandSender)attacker, "strength.underdog-bonus", "multiplier", String.valueOf(mult));
                }

                String transferStyle = this.plugin.getConfigManager().getC().getString("strength.kill-transfer-style", "DIRECT");
                if ("DROP".equalsIgnoreCase(transferStyle)) {
                    ItemStack shard = ItemFactory.createStrengthItem(gain);
                    victim.getWorld().dropItemNaturally(victim.getLocation(), shard);
                    MessageUtil.send((CommandSender)attacker, "strength.gain-dropped", "amount", String.valueOf(gain));
                } else {
                    this.addStrength(attacker, gain);
                }
            }
        }
        this.dataManager.recordKill(attackerUUID, victimUUID);
        this.dataManager.addKill(attackerUUID);
    }

    private void handleMaxStrengthReward(Player player, int amount) {
        ItemStack reward = ItemFactory.createStrengthItem(amount);
        HashMap leftover = player.getInventory().addItem(new ItemStack[]{reward});
        if (!leftover.isEmpty()) {
            player.getWorld().dropItem(player.getLocation(), (ItemStack)leftover.values().iterator().next());
        }
        MessageUtil.send((CommandSender)player, "strength.max-reached", "amount", String.valueOf(this.plugin.getConfigManager().getMaxStrength()));
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 0.5f);
    }
}

