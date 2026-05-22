package com.floki.strengthsmp.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemFactory {

    // Persistent Data keys for identifying custom items (more reliable than CustomModelData)
    private static final String STRENGTH_ITEM_KEY = "smp_strength_item";
    private static final String REROLL_ITEM_KEY = "smp_reroll_token";
    private static final String DEATH_CERT_KEY = "smp_death_certificate";

    /**
     * Translates & codes to ChatColor
     */
    private static String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    /**
     * Creates the custom Strength Item.
     * If custom heads are enabled, renders as a 3D orange energy orb.
     * Otherwise falls back to the configured vanilla material.
     */
    public static ItemStack createStrengthItem(int amount) {
        com.floki.strengthsmp.config.Config config = com.floki.strengthsmp.StrengthSMP.getInstance().getConfigManager();
        org.bukkit.plugin.Plugin plugin = com.floki.strengthsmp.StrengthSMP.getInstance();

        ItemStack item;

        // Use custom head texture if enabled
        if (config.useCustomHeads() && !config.getStrengthHeadTexture().isEmpty()) {
            item = SkullTextureUtil.createCustomHead(config.getStrengthHeadTexture());
            item.setAmount(amount);
        } else {
            Material mat = Material.matchMaterial(config.getStrengthItemMaterial());
            if (mat == null) mat = Material.NAUTILUS_SHELL;
            item = new ItemStack(mat, amount);
        }

        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(color("&6&lSTRENGTH ITEM"));
            
            List<String> lore = new ArrayList<>();
            lore.add(color("&fRight click to add &4+1&f strength"));
            meta.setLore(lore);
            
            // Always set CustomModelData for resource pack users
            meta.setCustomModelData(config.getStrengthItemModel());
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);

            // Tag with PersistentDataContainer for reliable identification
            meta.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, STRENGTH_ITEM_KEY),
                    PersistentDataType.STRING, "true");

            item.setItemMeta(meta);
        }
        
        return item;
    }

    /**
     * Creates the Reroll Item (Book).
     * If custom heads are enabled, renders as a 3D enchanted grimoire.
     * Otherwise falls back to the configured vanilla material.
     */
    public static ItemStack createRerollItem(org.bukkit.plugin.Plugin plugin, int amount) {
        com.floki.strengthsmp.config.Config config = com.floki.strengthsmp.StrengthSMP.getInstance().getConfigManager();

        ItemStack item;

        if (config.useCustomHeads() && !config.getRerollHeadTexture().isEmpty()) {
            item = SkullTextureUtil.createCustomHead(config.getRerollHeadTexture());
            item.setAmount(amount);
        } else {
            Material mat = Material.matchMaterial(config.getRerollItemMaterial());
            if (mat == null) mat = Material.BOOK;
            item = new ItemStack(mat, amount);
        }

        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(color("&6&lREROLL WEAPON"));
            
            meta.setLore(Arrays.asList(
                color("&fRight click to reroll your weapon"),
                "",
                color("&c&lCOST: &71 Strength")
            ));
            
            meta.setCustomModelData(config.getRerollItemModel());
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, REROLL_ITEM_KEY), 
                    PersistentDataType.STRING, "true");
            
            item.setItemMeta(meta);
        }
        
        return item;
    }

    /**
     * Creates a Sealed Destiny item for first-time joins.
     */
    public static ItemStack createSealedDestiny(org.bukkit.plugin.Plugin plugin) {
        ItemStack item = createRerollItem(plugin, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color("&d&lSEALED DESTINY"));
            meta.setLore(Arrays.asList(
                color("&7An ancient artifact that reveals"),
                color("&7your inner warrior potential."),
                "",
                color("&fRight-click to &bAWAKEN &fyour power!"),
                "",
                color("&a&lFREE &7(First Join Reward)")
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Creates a Death Certificate item.
     * If custom heads are enabled, renders as a 3D tombstone.
     * Otherwise falls back to the configured vanilla material.
     */
    public static ItemStack createDeathCertificate(org.bukkit.plugin.Plugin plugin, String victimName, String killerName) {
        com.floki.strengthsmp.config.Config config = com.floki.strengthsmp.StrengthSMP.getInstance().getConfigManager();

        ItemStack item;

        if (config.useCustomHeads() && !config.getDeathCertHeadTexture().isEmpty()) {
            item = SkullTextureUtil.createCustomHead(config.getDeathCertHeadTexture());
        } else {
            Material mat = Material.matchMaterial(config.getDeathCertMaterial());
            if (mat == null) mat = Material.PAPER;
            item = new ItemStack(mat);
        }

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
            meta.setCustomModelData(config.getDeathCertModel());

            // Tag for identification
            meta.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, DEATH_CERT_KEY),
                    PersistentDataType.STRING, "true");

            item.setItemMeta(meta);
        }
        
        return item;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Item Identification Helpers
    // ═══════════════════════════════════════════════════════════════

    /**
     * Checks if an item is a Strength Item using PersistentDataContainer.
     * Works regardless of whether the item is a custom head or vanilla material.
     */
    public static boolean isStrengthItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        org.bukkit.plugin.Plugin plugin = com.floki.strengthsmp.StrengthSMP.getInstance();
        NamespacedKey key = new NamespacedKey(plugin, STRENGTH_ITEM_KEY);

        // Check PDC first (new items), then fall back to CustomModelData (legacy items from shops)
        if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            return true;
        }

        // Legacy fallback: items created before this update or from external shops
        return meta.hasCustomModelData() && meta.getCustomModelData() == 12345;
    }

    /**
     * Checks if an item is a Reroll Item using PersistentDataContainer.
     */
    public static boolean isRerollItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        org.bukkit.plugin.Plugin plugin = com.floki.strengthsmp.StrengthSMP.getInstance();
        NamespacedKey key = new NamespacedKey(plugin, REROLL_ITEM_KEY);

        if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            return true;
        }

        // Legacy fallback
        return meta.hasCustomModelData() && meta.getCustomModelData() == 12346;
    }
}
