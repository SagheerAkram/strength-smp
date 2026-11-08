/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.ChatColor
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemFlag
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 */
package com.floki.strengthsmp.util;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.config.Config;
import com.floki.strengthsmp.util.MessageUtil;
import com.floki.strengthsmp.util.SkullTextureUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class ItemFactory {
    private static final String STRENGTH_ITEM_KEY = "smp_strength_item";
    private static final String REROLL_ITEM_KEY = "smp_reroll_token";
    private static final String DEATH_CERT_KEY = "smp_death_certificate";

    private static final String STARTER_BOOK_OWNER_KEY = "smp_starter_book_owner";

    private static String color(String msg) {
        return ChatColor.translateAlternateColorCodes((char)'&', (String)msg);
    }

    public static ItemStack createStrengthItem(int amount) {
        ItemStack item;
        ItemMeta meta;
        Config config = StrengthSMP.getInstance().getConfigManager();
        StrengthSMP plugin = StrengthSMP.getInstance();
        Material mat = Material.matchMaterial((String)config.getStrengthItemMaterial());
        if (mat == null) {
            mat = Material.NAUTILUS_SHELL;
        }
        if ((meta = (item = new ItemStack(mat, amount)).getItemMeta()) != null) {
            meta.setDisplayName(ItemFactory.color("&6&lSTRENGTH ITEM"));
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(ItemFactory.color("&fRight click to add &4+1&f strength"));
            meta.setLore(lore);
            meta.setCustomModelData(Integer.valueOf(config.getStrengthItemModel()));
            meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS});
            meta.getPersistentDataContainer().set(new NamespacedKey((Plugin)plugin, STRENGTH_ITEM_KEY), PersistentDataType.STRING, "true");
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack createRerollItem(Plugin plugin, int amount) {
        ItemStack item;
        ItemMeta meta;
        Config config = StrengthSMP.getInstance().getConfigManager();
        Material mat = Material.matchMaterial((String)config.getRerollItemMaterial());
        if (mat == null) {
            mat = Material.BOOK;
        }
        if ((meta = (item = new ItemStack(mat, amount)).getItemMeta()) != null) {
            meta.setDisplayName(ItemFactory.color("&6&lREROLL WEAPON"));
            meta.setLore(Arrays.asList(ItemFactory.color("&fRight click to reroll your weapon"), "", ItemFactory.color("&c&lCOST: &71 Strength")));
            meta.setCustomModelData(Integer.valueOf(config.getRerollItemModel()));
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, REROLL_ITEM_KEY), PersistentDataType.STRING, "true");
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack createSealedDestiny(Plugin plugin) {
        return createSealedDestiny(plugin, null);
    }

    public static ItemStack createSealedDestiny(Plugin plugin, Player owner) {
        ItemStack item = ItemFactory.createRerollItem(plugin, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ItemFactory.color(MessageUtil.get("items.sealed-destiny-name", "&d&lSEALED DESTINY")));
            String tag = MessageUtil.get("items.sealed-destiny-lore-tag", "&a&lFREE &7(First Join Reward)");
            meta.setLore(Arrays.asList(
                ItemFactory.color(MessageUtil.get("items.sealed-destiny-lore-1", "&7An ancient artifact that reveals")),
                ItemFactory.color(MessageUtil.get("items.sealed-destiny-lore-2", "&7your inner warrior potential.")),
                "",
                ItemFactory.color(MessageUtil.get("items.sealed-destiny-lore-click", "&fRight-click to &bAWAKEN &fyour power!")),
                "",
                ItemFactory.color(tag + (owner != null ? " &8[&e" + owner.getName() + "&8]" : ""))
            ));
            if (owner != null) {
                meta.getPersistentDataContainer().set(new NamespacedKey(plugin, STARTER_BOOK_OWNER_KEY), PersistentDataType.STRING, owner.getUniqueId().toString());
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isStarterRerollBook(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        StrengthSMP plugin = StrengthSMP.getInstance();
        NamespacedKey key = new NamespacedKey((Plugin)plugin, STARTER_BOOK_OWNER_KEY);
        return meta.getPersistentDataContainer().has(key, PersistentDataType.STRING);
    }

    public static String getStarterRerollBookOwner(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        StrengthSMP plugin = StrengthSMP.getInstance();
        NamespacedKey key = new NamespacedKey((Plugin)plugin, STARTER_BOOK_OWNER_KEY);
        return meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

    public static ItemStack createDeathCertificate(Plugin plugin, String victimName, String killerName) {
        ItemStack item;
        ItemMeta meta;
        Config config = StrengthSMP.getInstance().getConfigManager();
        Material mat = Material.matchMaterial((String)config.getDeathCertMaterial());
        if (mat == null) {
            mat = Material.PAPER;
        }
        if ((meta = (item = new ItemStack(mat)).getItemMeta()) != null) {
            meta.setDisplayName(ItemFactory.color("&c&lDEATH CERTIFICATE"));
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(ItemFactory.color("&7&m-----------------------"));
            lore.add(ItemFactory.color("&fVICTIM: &c" + victimName));
            lore.add(ItemFactory.color("&fKILLER: &a" + killerName));
            lore.add(ItemFactory.color("&7&m-----------------------"));
            lore.add(ItemFactory.color("&8&oA memento of a fallen warrior"));
            meta.setLore(lore);
            meta.setCustomModelData(Integer.valueOf(config.getDeathCertModel()));
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, DEATH_CERT_KEY), PersistentDataType.STRING, "true");
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isStrengthItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        StrengthSMP plugin = StrengthSMP.getInstance();
        NamespacedKey key = new NamespacedKey((Plugin)plugin, STRENGTH_ITEM_KEY);
        if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            return true;
        }
        return meta.hasCustomModelData() && meta.getCustomModelData() == 12345;
    }

    public static boolean isRerollItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        StrengthSMP plugin = StrengthSMP.getInstance();
        NamespacedKey key = new NamespacedKey((Plugin)plugin, REROLL_ITEM_KEY);
        if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            return true;
        }
        return meta.hasCustomModelData() && meta.getCustomModelData() == 12346;
    }

    public static boolean isDeathCertificate(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        StrengthSMP plugin = StrengthSMP.getInstance();
        NamespacedKey key = new NamespacedKey((Plugin)plugin, DEATH_CERT_KEY);
        if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            return true;
        }
        return meta.hasCustomModelData() && meta.getCustomModelData() == 12347;
    }

    public static void upgradeInventoryItems(Player player) {
        Config config = StrengthSMP.getInstance().getConfigManager();
        StrengthSMP plugin = StrengthSMP.getInstance();
        boolean useCustomHeads = false;
        ItemStack[] contents = player.getInventory().getContents();
        boolean changed = false;
        for (int i = 0; i < contents.length; ++i) {
            ItemStack newItem;
            ItemStack item = contents[i];
            if (item == null || item.getType() == Material.AIR) continue;
            Material targetMaterial = null;
            int customModelData = 0;
            String pdcKey = null;
            if (ItemFactory.isStrengthItem(item)) {
                if (useCustomHeads && !config.getStrengthHeadTexture().isEmpty()) {
                    targetMaterial = Material.PLAYER_HEAD;
                } else {
                    targetMaterial = Material.matchMaterial((String)config.getStrengthItemMaterial());
                    if (targetMaterial == null) {
                        targetMaterial = Material.NAUTILUS_SHELL;
                    }
                }
                customModelData = config.getStrengthItemModel();
                pdcKey = STRENGTH_ITEM_KEY;
            } else if (ItemFactory.isRerollItem(item)) {
                if (useCustomHeads && !config.getRerollHeadTexture().isEmpty()) {
                    targetMaterial = Material.PLAYER_HEAD;
                } else {
                    targetMaterial = Material.matchMaterial((String)config.getRerollItemMaterial());
                    if (targetMaterial == null) {
                        targetMaterial = Material.BOOK;
                    }
                }
                customModelData = config.getRerollItemModel();
                pdcKey = REROLL_ITEM_KEY;
            } else if (ItemFactory.isDeathCertificate(item)) {
                if (useCustomHeads && !config.getDeathCertHeadTexture().isEmpty()) {
                    targetMaterial = Material.PLAYER_HEAD;
                } else {
                    targetMaterial = Material.matchMaterial((String)config.getDeathCertMaterial());
                    if (targetMaterial == null) {
                        targetMaterial = Material.PAPER;
                    }
                }
                customModelData = config.getDeathCertModel();
                pdcKey = DEATH_CERT_KEY;
            }
            if (targetMaterial == null || item.getType() == targetMaterial) continue;
            int amount = item.getAmount();
            if (targetMaterial == Material.PLAYER_HEAD) {
                String headTex = "";
                if (pdcKey.equals(STRENGTH_ITEM_KEY)) {
                    headTex = config.getStrengthHeadTexture();
                } else if (pdcKey.equals(REROLL_ITEM_KEY)) {
                    headTex = config.getRerollHeadTexture();
                } else if (pdcKey.equals(DEATH_CERT_KEY)) {
                    headTex = config.getDeathCertHeadTexture();
                }
                newItem = SkullTextureUtil.createCustomHead(headTex);
                newItem.setAmount(amount);
            } else {
                newItem = new ItemStack(targetMaterial, amount);
            }
            ItemMeta oldMeta = item.getItemMeta();
            ItemMeta newMeta = newItem.getItemMeta();
            if (oldMeta != null && newMeta != null) {
                if (oldMeta.hasDisplayName()) {
                    newMeta.setDisplayName(oldMeta.getDisplayName());
                }
                if (oldMeta.hasLore()) {
                    newMeta.setLore(oldMeta.getLore());
                }
                newMeta.setCustomModelData(Integer.valueOf(customModelData));
                oldMeta.getPersistentDataContainer().getKeys().forEach(key -> {
                    try {
                        String val = (String)oldMeta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                        if (val != null) {
                            newMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, val);
                        }
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                });
                newMeta.getPersistentDataContainer().set(new NamespacedKey((Plugin)plugin, pdcKey), PersistentDataType.STRING, "true");
                newItem.setItemMeta(newMeta);
            }
            contents[i] = newItem;
            changed = true;
        }
        if (changed) {
            player.getInventory().setContents(contents);
            player.updateInventory();
            plugin.getLogger().info("\ud83d\udd04 Legacy/mismatched custom items upgraded in " + player.getName() + "'s inventory.");
        }
    }

    public static void unbindItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        StrengthSMP plugin = StrengthSMP.getInstance();
        NamespacedKey boundKey = new NamespacedKey((Plugin)plugin, "bound_player_uuid");
        NamespacedKey itemIdKey = new NamespacedKey((Plugin)plugin, "bound_item_id");
        NamespacedKey origCmdKey = new NamespacedKey((Plugin)plugin, "smp_original_cmd");
        NamespacedKey origNameKey = new NamespacedKey((Plugin)plugin, "smp_original_name");
        NamespacedKey origLoreKey = new NamespacedKey((Plugin)plugin, "smp_original_lore");

        meta.getPersistentDataContainer().remove(boundKey);
        meta.getPersistentDataContainer().remove(itemIdKey);

        // 1. Texture (CustomModelData)
        if (meta.getPersistentDataContainer().has(origCmdKey, PersistentDataType.INTEGER)) {
            int origCmd = (Integer)meta.getPersistentDataContainer().get(origCmdKey, PersistentDataType.INTEGER);
            if (origCmd >= 12000 && origCmd <= 14000) {
                meta.setCustomModelData(null);
            } else {
                meta.setCustomModelData(Integer.valueOf(origCmd));
            }
            meta.getPersistentDataContainer().remove(origCmdKey);
        } else {
            meta.setCustomModelData(null);
        }

        // 2. Display Name
        if (meta.getPersistentDataContainer().has(origNameKey, PersistentDataType.STRING)) {
            String origName = (String)meta.getPersistentDataContainer().get(origNameKey, PersistentDataType.STRING);
            if (origName != null && (origName.contains("⚡") || origName.contains("\u26a1"))) {
                meta.setDisplayName(null);
            } else {
                meta.setDisplayName(origName);
            }
            meta.getPersistentDataContainer().remove(origNameKey);
        } else {
            meta.setDisplayName(null);
        }

        // 3. Lore
        if (meta.getPersistentDataContainer().has(origLoreKey, PersistentDataType.STRING)) {
            String origLoreRaw = (String)meta.getPersistentDataContainer().get(origLoreKey, PersistentDataType.STRING);
            if (origLoreRaw != null && !origLoreRaw.isEmpty()) {
                if (origLoreRaw.contains("BOUND WEAPON") || origLoreRaw.contains("Soulbound to:")) {
                    meta.setLore(null);
                } else {
                    meta.setLore(Arrays.asList(origLoreRaw.split("\\|\\|\\|")));
                }
            } else {
                meta.setLore(null);
            }
            meta.getPersistentDataContainer().remove(origLoreKey);
        } else {
            meta.setLore(null);
        }

        item.setItemMeta(meta);
    }

    public static void unbindAllPlayerItems(Player player) {
        if (player == null) {
            return;
        }
        StrengthSMP plugin = StrengthSMP.getInstance();
        NamespacedKey boundKey = new NamespacedKey((Plugin)plugin, "bound_player_uuid");
        ItemStack[] contents = player.getInventory().getContents();
        boolean changed = false;
        for (int i = 0; i < contents.length; ++i) {
            ItemStack item = contents[i];
            if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) continue;
            ItemMeta meta = item.getItemMeta();
            if (meta == null || !meta.getPersistentDataContainer().has(boundKey, PersistentDataType.STRING)) continue;
            String ownerUuidStr = (String)meta.getPersistentDataContainer().get(boundKey, PersistentDataType.STRING);
            if (!player.getUniqueId().toString().equals(ownerUuidStr)) continue;
            unbindItem(item);
            changed = true;
        }
        ItemStack cursor = player.getItemOnCursor();
        if (cursor != null && cursor.getType() != Material.AIR && cursor.hasItemMeta()) {
            ItemMeta meta = cursor.getItemMeta();
            if (meta != null && meta.getPersistentDataContainer().has(boundKey, PersistentDataType.STRING)) {
                String ownerUuidStr = (String)meta.getPersistentDataContainer().get(boundKey, PersistentDataType.STRING);
                if (player.getUniqueId().toString().equals(ownerUuidStr)) {
                    unbindItem(cursor);
                    changed = true;
                }
            }
        }
        if (changed) {
            player.updateInventory();
            plugin.getLogger().info("\u26a1 All bound items have been automatically unbound for player " + player.getName());
        }
    }
}

