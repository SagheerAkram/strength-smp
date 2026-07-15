/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.Sound
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.event.Event$Result
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.event.player.PlayerJoinEvent
 *  org.bukkit.event.player.PlayerRespawnEvent
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.potion.PotionEffectType
 *  org.bukkit.scoreboard.Scoreboard
 *  org.bukkit.scoreboard.Team
 */
package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.DataManager;
import com.floki.strengthsmp.util.ItemFactory;
import com.floki.strengthsmp.util.MessageUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class PlayerListener
implements Listener {
    private final StrengthSMP plugin;
    private final DataManager dataManager;

    public PlayerListener(StrengthSMP plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        this.dataManager.initializePlayer(player.getUniqueId());
        this.updatePlayerDisplay(player);
        ItemFactory.upgradeInventoryItems(player);
        if (!player.hasPlayedBefore() && this.plugin.getConfigManager().isNewbieProtectionEnabled()) {
            MessageUtil.send((CommandSender)player, "monarch.broadcast", "player", player.getName());
            long durationHours = this.plugin.getConfigManager().getNewbieProtectionHours();
            long newbieExpiry = System.currentTimeMillis() + durationHours * 60L * 60L * 1000L;
            this.dataManager.setNewbieProtection(player.getUniqueId(), newbieExpiry);
            this.dataManager.savePlayer(player.getUniqueId());
            Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> MessageUtil.send((CommandSender)player, "strength.newbie-protection-start", new String[0]), 2L);
        } else if (!player.hasPlayedBefore()) {
            MessageUtil.send((CommandSender)player, "monarch.broadcast", "player", player.getName());
        }
        this.plugin.getContractService().checkAndRefreshContracts(player);
        if (!this.dataManager.hasReceivedFreeReroll(player.getUniqueId())) {
            this.giveFreeReroll(player);
        }
    }

    private void giveFreeReroll(Player player) {
        UUID uuid = player.getUniqueId();
        if (this.dataManager.hasReceivedFreeReroll(uuid)) {
            return;
        }
        this.dataManager.setReceivedFreeReroll(uuid, true);
        this.dataManager.savePlayer(uuid);
        ItemStack sealedDestiny = ItemFactory.createSealedDestiny((Plugin)this.plugin);
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack[]{sealedDestiny});
        if (!leftover.isEmpty()) {
            for (ItemStack item : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }
        MessageUtil.send((CommandSender)player, "strength.free-reroll-reward", new String[0]);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.5f);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (event.getPlayer().isOnline()) {
                this.updatePlayerDisplay(event.getPlayer());
            }
        }, 1L);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getHand() == null) {
            return;
        }
        Player player = event.getPlayer();
        ItemFactory.upgradeInventoryItems(player);
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (!ItemFactory.isStrengthItem(item)) {
            return;
        }
        if (meta.getLore() == null || meta.getLore().isEmpty()) {
            return;
        }
        event.setCancelled(true);
        event.setUseItemInHand(Event.Result.DENY);
        UUID uuid = player.getUniqueId();
        int currentStr = this.dataManager.getStrength(uuid);
        long cooldownSeconds = Math.min(12L, 3L + 2L * (long)(currentStr - 1));
        long lastUse = this.dataManager.getAbilityCooldown(uuid, "strength_use");
        long remaining = lastUse + cooldownSeconds * 1000L - System.currentTimeMillis();
        if (remaining > 0L) {
            double secondsLeft = Math.ceil((double)remaining / 1000.0);
            MessageUtil.send((CommandSender)player, "strength.cooldown", "time", String.valueOf((int)secondsLeft));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }
        if (this.dataManager.isAtMaxStrength(uuid)) {
            MessageUtil.send((CommandSender)player, "strength.max-reached", "amount", String.valueOf(this.plugin.getConfigManager().getMaxStrength()));
            return;
        }
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().removeItem(new ItemStack[]{item});
        }
        this.dataManager.setAbilityCooldown(uuid, "strength_use", System.currentTimeMillis());
        this.plugin.getStrengthService().addStrength(player, 1);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        this.updatePlayerDisplay(player);
    }

    public void updatePlayerDisplay(Player player) {
        boolean hasBountyGlow;
        boolean isMonarch;
        int strength = this.dataManager.getStrength(player.getUniqueId());
        UUID monarchUUID = this.plugin.getMonarchService().getCurrentMonarchUUID();
        boolean bl = isMonarch = monarchUUID != null && monarchUUID.equals(player.getUniqueId());
        if (isMonarch) {
            this.plugin.getMonarchService().applyMonarchEffects(player);
        } else {
            player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
            Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
            Team team = sb.getTeam("MonarchGlow");
            if (team != null && team.hasEntry(player.getName())) {
                team.removeEntry(player.getName());
            }
        }
        int bounty = this.dataManager.getBounty(player.getUniqueId());
        long bountyExp = this.dataManager.getBountyGlowExpiration(player.getUniqueId());
        boolean bl2 = hasBountyGlow = bounty > 0 || bountyExp > System.currentTimeMillis();
        if (hasBountyGlow) {
            this.plugin.getAestheticService().updateBountyGlow(player, bounty > 0 ? bounty : 1);
        } else {
            if (bountyExp > 0L) {
                this.dataManager.clearBountyGlowExpiration(player.getUniqueId());
            }
            if (!isMonarch) {
                player.setGlowing(false);
                Bukkit.getScoreboardManager().getMainScoreboard().getTeams().forEach(t -> {
                    if (t.getName().startsWith("Bounty_")) {
                        t.removeEntry(player.getName());
                    }
                });
            }
        }
        String rawName = player.getName();
        if (isMonarch) {
            String monarchTag = String.valueOf(ChatColor.GOLD) + String.valueOf(ChatColor.BOLD) + "\ud83d\udc51 MONARCH " + String.valueOf(ChatColor.RESET);
            player.setDisplayName(monarchTag + rawName);
            player.setPlayerListName(monarchTag + rawName);
        } else {
            String prefix = this.getStrengthPrefix(strength);
            player.setDisplayName(prefix + " " + String.valueOf(ChatColor.RESET) + rawName);
            player.setPlayerListName(prefix + " " + String.valueOf(ChatColor.RESET) + rawName);
        }
    }

    private String getStrengthPrefix(int strength) {
        String rankName = this.plugin.getConfigManager().getRankNameForStrength(strength);
        return MessageUtil.color(rankName) + MessageUtil.color(" &8[" + strength + "]");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player)event.getWhoClicked();
        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
            if (player.isOnline()) {
                ItemFactory.upgradeInventoryItems(player);
            }
        });
    }
}

