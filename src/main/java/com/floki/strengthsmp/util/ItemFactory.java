package com.floki.strengthsmp.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemFactory {

    /**
     * Translates & codes to ChatColor
     */
    private static String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    /**
     * Creates the custom Strength Item (Nautilus Shell)
     */
    public static ItemStack createStrengthItem(int amount) {
        ItemStack item = new ItemStack(Material.NAUTILUS_SHELL, amount);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(color("&6&lSTRENGTH ITEM"));
            
            List<String> lore = new ArrayList<>();
            lore.add(color("&fRight click to add &4+1&f strength"));
            meta.setLore(lore);
            
            meta.setCustomModelData(12345);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        
        return item;
    }

    /**
     * Creates the Reroll Item (Book)
     */
    public static ItemStack createRerollItem(org.bukkit.plugin.Plugin plugin, int amount) {
        ItemStack item = new ItemStack(Material.BOOK, amount);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Using legacy HEX or standard colors for compatibility
            meta.setDisplayName(color("&6&lREROLL WEAPON"));
            
            meta.setLore(Arrays.asList(
                color("&fRight click to reroll your weapon"),
                "",
                color("&c&lCOST: &71 Strength")
            ));
            
            meta.setCustomModelData(12346);
            meta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "smp_reroll_token"), 
                    org.bukkit.persistence.PersistentDataType.STRING, "true");
            
            item.setItemMeta(meta);
        }
        
        return item;
    }

    /**
     * Creates a Death Certificate item
     */
    public static ItemStack createDeathCertificate(String victimName, String killerName) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(color("&c&lDEATH CERTIFICATE"));
            
            List<String> lore = new ArrayList<>();
            lore.add(color("&7&m-----------------------"));
            lore.add(color("&fVICTIM: &c" + victimName));
            lore.add(color("&fKILLER: &a" + killerName));
            lore.add(color("&7&m-----------------------"));
            lore.add(color("&8&oA memento of a fallen warrior"));
            
            meta.setLore(lore);
            meta.setCustomModelData(12347);
            item.setItemMeta(meta);
        }
        
        return item;
    }
}
