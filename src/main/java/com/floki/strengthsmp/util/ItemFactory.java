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

        Material mat = Material.matchMaterial(config.getStrengthItemMaterial());
        if (mat == null) mat = Material.NAUTILUS_SHELL;
        ItemStack item = new ItemStack(mat, amount);

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

        Material mat = Material.matchMaterial(config.getRerollItemMaterial());
        if (mat == null) mat = Material.BOOK;
        ItemStack item = new ItemStack(mat, amount);

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

        Material mat = Material.matchMaterial(config.getDeathCertMaterial());
        if (mat == null) mat = Material.PAPER;
        ItemStack item = new ItemStack(mat);

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

    /**
     * Checks if an item is a Death Certificate using PersistentDataContainer.
     */
    public static boolean isDeathCertificate(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        org.bukkit.plugin.Plugin plugin = com.floki.strengthsmp.StrengthSMP.getInstance();
        NamespacedKey key = new NamespacedKey(plugin, DEATH_CERT_KEY);

        if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            return true;
        }

        // Legacy fallback
        return meta.hasCustomModelData() && meta.getCustomModelData() == 12347;
    }

    /**
     * Scans and upgrades any custom items (Strength Item, Reroll Token, Death Certificate)
     * in the player's inventory to match the current configured materials.
     * Converts legacy custom heads (PLAYER_HEAD) to the new material (NAUTILUS_SHELL, etc.)
     * if the server configuration currently does not use custom heads, and vice versa.
     */
    public static void upgradeInventoryItems(org.bukkit.entity.Player player) {
        com.floki.strengthsmp.config.Config config = com.floki.strengthsmp.StrengthSMP.getInstance().getConfigManager();
        org.bukkit.plugin.Plugin plugin = com.floki.strengthsmp.StrengthSMP.getInstance();
        boolean useCustomHeads = false;

        ItemStack[] contents = player.getInventory().getContents();
        boolean changed = false;

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() == Material.AIR) continue;

            Material targetMaterial = null;
            int customModelData = 0;
            String pdcKey = null;

            if (isStrengthItem(item)) {
                if (useCustomHeads && !config.getStrengthHeadTexture().isEmpty()) {
                    targetMaterial = Material.PLAYER_HEAD;
                } else {
                    targetMaterial = Material.matchMaterial(config.getStrengthItemMaterial());
                    if (targetMaterial == null) targetMaterial = Material.NAUTILUS_SHELL;
                }
                customModelData = config.getStrengthItemModel();
                pdcKey = STRENGTH_ITEM_KEY;
            } else if (isRerollItem(item)) {
                if (useCustomHeads && !config.getRerollHeadTexture().isEmpty()) {
                    targetMaterial = Material.PLAYER_HEAD;
                } else {
                    targetMaterial = Material.matchMaterial(config.getRerollItemMaterial());
                    if (targetMaterial == null) targetMaterial = Material.BOOK;
                }
                customModelData = config.getRerollItemModel();
                pdcKey = REROLL_ITEM_KEY;
            } else if (isDeathCertificate(item)) {
                if (useCustomHeads && !config.getDeathCertHeadTexture().isEmpty()) {
                    targetMaterial = Material.PLAYER_HEAD;
                } else {
                    targetMaterial = Material.matchMaterial(config.getDeathCertMaterial());
                    if (targetMaterial == null) targetMaterial = Material.PAPER;
                }
                customModelData = config.getDeathCertModel();
                pdcKey = DEATH_CERT_KEY;
            }

            if (targetMaterial != null && item.getType() != targetMaterial) {
                // We need to convert it!
                int amount = item.getAmount();
                ItemStack newItem;

                if (targetMaterial == Material.PLAYER_HEAD) {
                    String headTex = "";
                    if (pdcKey.equals(STRENGTH_ITEM_KEY)) headTex = config.getStrengthHeadTexture();
                    else if (pdcKey.equals(REROLL_ITEM_KEY)) headTex = config.getRerollHeadTexture();
                    else if (pdcKey.equals(DEATH_CERT_KEY)) headTex = config.getDeathCertHeadTexture();

                    newItem = SkullTextureUtil.createCustomHead(headTex);
                    newItem.setAmount(amount);
                } else {
                    newItem = new ItemStack(targetMaterial, amount);
                }

                // Copy over item meta where applicable
                ItemMeta oldMeta = item.getItemMeta();
                ItemMeta newMeta = newItem.getItemMeta();

                if (oldMeta != null && newMeta != null) {
                    if (oldMeta.hasDisplayName()) {
                        newMeta.setDisplayName(oldMeta.getDisplayName());
                    }
                    if (oldMeta.hasLore()) {
                        newMeta.setLore(oldMeta.getLore());
                    }
                    newMeta.setCustomModelData(customModelData);
                    
                    // Copy existing PDC tags
                    oldMeta.getPersistentDataContainer().getKeys().forEach(key -> {
                        try {
                            String val = oldMeta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                            if (val != null) {
                                newMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, val);
                            }
                        } catch (Exception ignored) {}
                    });

                    // Ensure target tag is present
                    newMeta.getPersistentDataContainer().set(
                            new NamespacedKey(plugin, pdcKey),
                            PersistentDataType.STRING, "true");

                    newItem.setItemMeta(newMeta);
                }

                contents[i] = newItem;
                changed = true;
            }
        }

        if (changed) {
            player.getInventory().setContents(contents);
            player.updateInventory();
            plugin.getLogger().info("🔄 Legacy/mismatched custom items upgraded in " + player.getName() + "'s inventory.");
        }
    }

    /**
     * Unbinds and clears the custom model data/bound tags from all items matching the player's uuid.
     */
    public static void unbindAllPlayerItems(org.bukkit.entity.Player player) {
        org.bukkit.plugin.Plugin plugin = com.floki.strengthsmp.StrengthSMP.getInstance();
        NamespacedKey boundKey = new NamespacedKey(plugin, "bound_player_uuid");
        NamespacedKey itemIdKey = new NamespacedKey(plugin, "bound_item_id");

        ItemStack[] contents = player.getInventory().getContents();
        boolean changed = false;

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() == Material.AIR) continue;
            if (!item.hasItemMeta()) continue;

            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            if (meta.getPersistentDataContainer().has(boundKey, PersistentDataType.STRING)) {
                String ownerUuidStr = meta.getPersistentDataContainer().get(boundKey, PersistentDataType.STRING);
                if (player.getUniqueId().toString().equals(ownerUuidStr)) {
                    // Remove item metadata
                    meta.getPersistentDataContainer().remove(boundKey);
                    meta.getPersistentDataContainer().remove(itemIdKey);
                    if (meta.hasCustomModelData()) {
                        meta.setCustomModelData(null);
                    }

                    // Remove lore prefix
                    List<String> lore = meta.getLore();
                    if (lore != null) {
                        String prefix = MessageUtil.color(com.floki.strengthsmp.StrengthSMP.getInstance().getConfigManager().getBindLorePrefix());
                        lore.remove(prefix);
                        // Also try stripped prefix or MessageUtil.parse styled versions just in case
                        String parsedPrefix = MessageUtil.parse(com.floki.strengthsmp.StrengthSMP.getInstance().getConfigManager().getBindLorePrefix());
                        lore.remove(parsedPrefix);
                        lore.remove(ChatColor.stripColor(parsedPrefix));
                        meta.setLore(lore);
                    }

                    // Clean display name
                    if (meta.hasDisplayName()) {
                        String strippedName = ChatColor.stripColor(meta.getDisplayName());
                        strippedName = strippedName.replace("⚡", "").trim();
                        meta.setDisplayName(color("&f" + strippedName));
                    }

                    item.setItemMeta(meta);
                    changed = true;
                }
            }
        }

        if (changed) {
            player.updateInventory();
            plugin.getLogger().info("⚡ All bound items have been automatically unbound for player " + player.getName());
        }
    }
}
