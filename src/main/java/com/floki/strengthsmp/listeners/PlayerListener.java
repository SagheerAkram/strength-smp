package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.DataManager;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class PlayerListener implements Listener {

    private final StrengthSMP plugin;
    private final DataManager dataManager;

    public PlayerListener(StrengthSMP plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        dataManager.initializePlayer(player.getUniqueId());
        updatePlayerDisplay(player);

        if (!player.hasPlayedBefore() && plugin.getConfigManager().isNewbieProtectionEnabled()) {
            com.floki.strengthsmp.util.MessageUtil.send(player, "monarch.broadcast", "player", player.getName());
            long durationHours = plugin.getConfigManager().getNewbieProtectionHours();
            long newbieExpiry = System.currentTimeMillis() + (durationHours * 60 * 60 * 1000L);
            dataManager.setNewbieProtection(player.getUniqueId(), newbieExpiry);
            dataManager.savePlayer(player.getUniqueId());

            Bukkit.getScheduler().runTaskLater(plugin, () ->
                com.floki.strengthsmp.util.MessageUtil.send(player, "strength.newbie-protection-start"), 2L);
        } else if (!player.hasPlayedBefore()) {
            com.floki.strengthsmp.util.MessageUtil.send(player, "monarch.broadcast", "player", player.getName());
        }
        
        plugin.getContractService().checkAndRefreshContracts(player);
        if (!dataManager.hasReceivedFreeReroll(player.getUniqueId())) {
            giveFreeReroll(player);
        }
    }

    private void giveFreeReroll(Player player) {
        UUID uuid = player.getUniqueId();
        if (dataManager.hasReceivedFreeReroll(uuid)) return;
        dataManager.setReceivedFreeReroll(uuid, true);
        dataManager.savePlayer(uuid);
        
        ItemStack sealedDestiny = com.floki.strengthsmp.util.ItemFactory.createSealedDestiny(plugin);
        java.util.Map<Integer, ItemStack> leftover = player.getInventory().addItem(sealedDestiny);
        
        if (!leftover.isEmpty()) {
            for (ItemStack item : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }
        
        com.floki.strengthsmp.util.MessageUtil.send(player, "strength.free-reroll-reward");
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.5f);
    }

    @EventHandler
    public void onPlayerRespawn(org.bukkit.event.player.PlayerRespawnEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (event.getPlayer().isOnline()) {
                updatePlayerDisplay(event.getPlayer());
            }
        }, 1L);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() == null) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        
        // Identify using PersistentDataContainer (supports both custom heads and vanilla items)
        if (!com.floki.strengthsmp.util.ItemFactory.isStrengthItem(item)) return;

        if (meta.getLore() == null || meta.getLore().isEmpty()) return;

        event.setCancelled(true);
        event.setUseItemInHand(Event.Result.DENY);

        UUID uuid = player.getUniqueId();
        int currentStr = dataManager.getStrength(uuid);
        long cooldownSeconds = Math.min(12, 3 + (2L * (currentStr - 1)));
        long lastUse = dataManager.getAbilityCooldown(uuid, "strength_use");
        long remaining = (lastUse + (cooldownSeconds * 1000)) - System.currentTimeMillis();

        if (remaining > 0) {
            double secondsLeft = Math.ceil(remaining / 1000.0);
            com.floki.strengthsmp.util.MessageUtil.send(player, "strength.cooldown", "time", String.valueOf((int)secondsLeft));
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        if (dataManager.isAtMaxStrength(uuid)) {
            com.floki.strengthsmp.util.MessageUtil.send(player, "strength.max-reached", "amount", String.valueOf(plugin.getConfigManager().getMaxStrength()));
            return;
        }

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().removeItem(item);
        }

        dataManager.setAbilityCooldown(uuid, "strength_use", System.currentTimeMillis());
        plugin.getStrengthService().addStrength(player, 1);
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        updatePlayerDisplay(player);
    }

    public void updatePlayerDisplay(Player player) {
        int strength = dataManager.getStrength(player.getUniqueId());
        UUID monarchUUID = plugin.getMonarchService().getCurrentMonarchUUID();
        boolean isMonarch = monarchUUID != null && monarchUUID.equals(player.getUniqueId());

        if (isMonarch) {
            plugin.getMonarchService().applyMonarchEffects(player);
        } else {
            // REMOVE Monarch effects if they are not the monarch
            player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
            
            org.bukkit.scoreboard.Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
            org.bukkit.scoreboard.Team team = sb.getTeam("MonarchGlow");
            if (team != null && team.hasEntry(player.getName())) {
                team.removeEntry(player.getName());
            }
        }

        int bounty = dataManager.getBounty(player.getUniqueId());
        long bountyExp = dataManager.getBountyGlowExpiration(player.getUniqueId());
        boolean hasBountyGlow = bounty > 0 || (bountyExp > System.currentTimeMillis());

        if (hasBountyGlow) {
            plugin.getAestheticService().updateBountyGlow(player, bounty > 0 ? bounty : 1);
        } else {
            if (bountyExp > 0) dataManager.clearBountyGlowExpiration(player.getUniqueId());
            if (!isMonarch) {
                player.setGlowing(false);
                // Clear from bounty teams
                Bukkit.getScoreboardManager().getMainScoreboard().getTeams().forEach(t -> {
                    if (t.getName().startsWith("Bounty_")) t.removeEntry(player.getName());
                });
            }
        }

        String rawName = player.getName();
        if (isMonarch) {
            String monarchTag = ChatColor.GOLD + "" + ChatColor.BOLD + "👑 MONARCH " + ChatColor.RESET;
            player.setDisplayName(monarchTag + rawName);
            player.setPlayerListName(monarchTag + rawName);
        } else {
            String prefix = getStrengthPrefix(strength);
            player.setDisplayName(prefix + " " + ChatColor.RESET + rawName);
            player.setPlayerListName(prefix + " " + ChatColor.RESET + rawName);
        }
    }

    private String getStrengthPrefix(int strength) {
        if      (strength >= 5)  return ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "TITAN " + ChatColor.GRAY + "[" + strength + "]";
        else if (strength >= 3)  return ChatColor.AQUA + "" + ChatColor.BOLD + "WARRIOR " + ChatColor.GRAY + "[" + strength + "]";
        else if (strength >= 1)  return ChatColor.GREEN + "" + ChatColor.BOLD + "FIGHTER " + ChatColor.GRAY + "[" + strength + "]";
        else if (strength == 0)  return ChatColor.GRAY + "" + ChatColor.BOLD + "NEUTRAL " + ChatColor.GRAY + "[" + strength + "]";
        else if (strength >= -2) return ChatColor.GOLD + "" + ChatColor.BOLD + "WEAK " + ChatColor.GRAY + "[" + strength + "]";
        else if (strength >= -4) return ChatColor.RED + "" + ChatColor.BOLD + "FRAGILE " + ChatColor.GRAY + "[" + strength + "]";
        else                     return ChatColor.DARK_RED + "" + ChatColor.BOLD + "FRAIL " + ChatColor.GRAY + "[" + strength + "]";
    }
}