package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.WeaponType;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Handles item interactions (Reroll Book, etc.)
 */
public class ItemInteractionListener implements Listener {
    
    private final StrengthSMP plugin;
    private final NamespacedKey rerollKey;
    private final Random random = new Random();
    
    public ItemInteractionListener(StrengthSMP plugin) {
        this.plugin = plugin;
        this.rerollKey = new NamespacedKey(plugin, "smp_reroll_token");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() == Material.AIR) return;
        if (!item.hasItemMeta()) return;

        // Identify using PersistentDataContainer (supports both custom heads and vanilla items)
        if (!com.floki.strengthsmp.util.ItemFactory.isRerollItem(item)) return;


        event.setCancelled(true);

        int strength = plugin.getDataManager().getStrength(player.getUniqueId());
        WeaponType current = plugin.getDataManager().getWeapon(player.getUniqueId());
        
        // Only require strength if they already have a class (this is a reroll, not first roll)
        if (current != null && strength < 1) {
            MessageUtil.send(player, "strength.insufficient", "required", "1");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // 1. Selection: List-based unique choice
        List<WeaponType> available = new ArrayList<>(Arrays.asList(WeaponType.values()));
        available.remove(current);

        if (available.isEmpty()) {
            player.sendMessage("§cNo other weapon types available!");
            return;
        }

        // 2. Transaction: Deduct only if they are REROLLING (already have a class)
        if (current != null) {
            plugin.getDataManager().subtractStrength(player.getUniqueId(), 1);
            plugin.updateDisplay(player);
            plugin.getMonarchService().calculateNewMonarch();
        }
        
        // 3. Consume
        item.setAmount(item.getAmount() - 1);
        
        // 4. Open Shuffle GUI
        new com.floki.strengthsmp.gui.WeaponShuffleGUI(plugin, player).open();
        
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
    }

    private String getWeaponHex(WeaponType type) {
        switch (type) {
            case SWORD:    return "&#e74c3c"; // Red
            case AXE:      return "&#e67e22"; // Orange
            case TRIDENT:  return "&#3498db"; // Blue
            case SHIELD:   return "&#f1c40f"; // Gold
            case BOW:      return "&#2ecc71"; // Green
            case CROSSBOW: return "&#9b59b6"; // Purple
            default:       return "&#ffffff";
        }
    }


}
