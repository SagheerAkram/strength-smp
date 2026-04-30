package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.DataManager;
import java.util.UUID;
import org.bukkit.Bukkit;
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
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Handles player join events, strength-item right-click consumption, and display name updates.
 * Uses Adventure API (no deprecated legacy string API).
 */
public class PlayerListener implements Listener {

    private final StrengthSMP plugin;
    private final DataManager  dataManager;

    public PlayerListener(StrengthSMP plugin) {
        this.plugin      = plugin;
        this.dataManager = plugin.getDataManager();
    }

    // ── JOIN & RESPAWN ──────────────────────────────────────────────────────

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // 🛡️ SECURITY FIX: Ensure player is initialized in DataManager BEFORE any checks run.
        dataManager.initializePlayer(player.getUniqueId());
        
        updatePlayerDisplay(player);

        if (!player.hasPlayedBefore()) {
            com.floki.strengthsmp.util.MessageUtil.send(player, "monarch.broadcast", "player", player.getName());

            // Grant 8-hour newbie protection on very first join
            long newbieExpiry = System.currentTimeMillis() + (8 * 60 * 60 * 1000L);
            dataManager.setNewbieProtection(player.getUniqueId(), newbieExpiry);
            dataManager.savePlayer(player.getUniqueId());

            // Delay message by 2 ticks so it arrives after join messages
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () ->
                com.floki.strengthsmp.util.MessageUtil.send(player, "strength.newbie-protection-start"), 2L);
        }
        
        // Check for daily contracts
        plugin.getContractService().checkAndRefreshContracts(player);

        // One-time Free Reroll Reward
        if (!dataManager.hasReceivedFreeReroll(player.getUniqueId())) {
            giveFreeReroll(player);
        }
    }

    private void giveFreeReroll(Player player) {
        UUID uuid = player.getUniqueId();
        
        // 🛡️ Double-lock check: Verify again in case of rapid join events
        if (dataManager.hasReceivedFreeReroll(uuid)) return;
        
        // 1. Mark as received FIRST to prevent race conditions
        dataManager.setReceivedFreeReroll(uuid, true);
        dataManager.savePlayer(uuid); // Fast, targeted save
        
        // 2. Create and give the item
        ItemStack rerollBook = com.floki.strengthsmp.util.ItemFactory.createRerollItem(plugin, 1);
        java.util.Map<Integer, ItemStack> leftover = player.getInventory().addItem(rerollBook);
        
        // If full, drop at location
        if (!leftover.isEmpty()) {
            for (ItemStack item : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }
        
        com.floki.strengthsmp.util.MessageUtil.send(player, "strength.free-reroll-reward");
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.5f);
        
        plugin.getLogger().info("✓ Granted free reroll book to " + player.getName() + " (" + uuid + ")");
    }

    @EventHandler
    public void onPlayerRespawn(org.bukkit.event.player.PlayerRespawnEvent event) {
        // Delay slightly to ensure player is fully respawned
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (event.getPlayer().isOnline()) {
                updatePlayerDisplay(event.getPlayer());
            }
        }, 1L);
    }

    // ── STRENGTH ITEM RIGHT-CLICK ────────────────────────────────────────────

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player    player = event.getPlayer();
        ItemStack item   = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        String rawName = "";
        if (meta.hasDisplayName()) {
            rawName = LegacyComponentSerializer.legacySection().serialize(meta.displayName());
        }

        if (!rawName.equals("§6§lSTRENGTH ITEM")) return;

        event.setCancelled(true);
        event.setUseItemInHand(Event.Result.DENY);

        UUID uuid = player.getUniqueId();
        
        // ── DYNAMIC COOLDOWN ─────────────────────────────────────────────────
        int currentStr = dataManager.getStrength(uuid);
        // Formula: 3s base + 2s per additional strength, max 12s
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
            com.floki.strengthsmp.util.MessageUtil.send(player, "strength.max-reached");
            return;
        }

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().removeItem(item);
        }

        // Set new cooldown
        dataManager.setAbilityCooldown(uuid, "strength_use", System.currentTimeMillis());

        plugin.getStrengthService().addStrength(player, 1);
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        updatePlayerDisplay(player);
    }

    // ── DISPLAY NAME ─────────────────────────────────────────────────────────

    public void updatePlayerDisplay(Player player) {
        int strength = dataManager.getStrength(player.getUniqueId());
        UUID monarchUUID = plugin.getMonarchService().getCurrentMonarchUUID();
        boolean isMonarch = monarchUUID != null && monarchUUID.equals(player.getUniqueId());

        // 1. Refresh Monarch Effects (Scoreboard Team & Potion)
        if (isMonarch) {
            plugin.getMonarchService().applyMonarchEffects(player);
        } else {
            // Ensure they aren't stuck with monarch glow
            org.bukkit.scoreboard.Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
            org.bukkit.scoreboard.Team team = sb.getTeam("MonarchGlow");
            if (team != null && team.hasEntry(player.getName())) {
                team.removeEntry(player.getName());
            }
        }

        // 2. Handle Bounty Glow
        boolean hasBounty = dataManager.getBounty(player.getUniqueId()) > 0;
        long bountyExp = dataManager.getBountyGlowExpiration(player.getUniqueId());
        boolean hasBountyGlow = hasBounty || (bountyExp > System.currentTimeMillis());

        if (hasBountyGlow) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false), true);
        } else {
            if (bountyExp > 0) dataManager.clearBountyGlowExpiration(player.getUniqueId());
            player.removePotionEffect(PotionEffectType.GLOWING);
            
            // If they aren't monarch, they shouldn't be glowing at all
            if (!isMonarch) {
                player.setGlowing(false);
            }
        }

        // 3. Update Name Tags
        String rawName = player.getName();
        if (isMonarch) {
            String monarchTag = "§x§E§B§0§0§F§F§l👑 §lMONARCH §r";
            player.setDisplayName(monarchTag + rawName);
            player.setPlayerListName(monarchTag + rawName);
        } else {
            String prefix = getStrengthPrefix(strength);
            player.setDisplayName(prefix + " §r" + rawName);
            player.setPlayerListName(prefix + " §r" + rawName);
        }
    }

    // ── RANK PREFIXES ────────────────────────────────────────────────────────

    private String getStrengthPrefix(int strength) {
        if      (strength >= 5)  return "§x§E§B§0§0§F§F§lᴛɪᴛᴀɴ §8[" + strength + "]";
        else if (strength >= 3)  return "§x§0§0§D§B§F§F§lᴡᴀʀʀɪᴏʀ §8[" + strength + "]";
        else if (strength >= 1)  return "§x§0§0§F§F§6§B§lꜰɪɢʜᴛᴇʀ §8[" + strength + "]";
        else if (strength == 0)  return "§x§B§A§B§A§B§A§lɴᴇᴜᴛʀᴀʟ §8[" + strength + "]";
        else if (strength >= -2) return "§x§F§F§B§B§0§0§lᴡᴇᴀᴋ §8[" + strength + "]";
        else if (strength >= -4) return "§x§F§F§5§5§0§0§lꜰʀᴀɢɪʟᴇ §8[" + strength + "]";
        else                     return "§x§A§A§0§0§0§0§lꜰʀᴀɪʟ §8[" + strength + "]";
    }
}