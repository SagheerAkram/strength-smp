package com.floki.strengthsmp.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemFactory {

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacySection();

    /**
     * Creates the custom Strength Item (Nautilus Shell)
     */
    public static ItemStack createStrengthItem(int amount) {
        ItemStack item = new ItemStack(Material.NAUTILUS_SHELL, amount);
        ItemMeta meta = item.getItemMeta();
        
        // Consistent display name that PlayerListener checks for
        meta.displayName(SERIALIZER.deserialize("§6§lSTRENGTH ITEM")
                .decoration(TextDecoration.ITALIC, false));
        
        // Each shell always represents 1 strength, regardless of stack size
        List<Component> lore = new ArrayList<>();
        lore.add(SERIALIZER.deserialize("§fRight click to add §4+1§f strength")
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        
        meta.setCustomModelData(12345);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Creates the Reroll Item (Book)
     */
     public static ItemStack createRerollItem(org.bukkit.plugin.Plugin plugin, int amount) {
        ItemStack item = new ItemStack(Material.BOOK, amount);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(SERIALIZER.deserialize("§x§F§F§D§2§5§C§lʀ§x§F§6§C§7§4§D§lᴇ§x§E§C§B§C§3§D§lʀ§x§E§3§B§1§2§E§lᴏ§x§D§9§A§6§1§F§lʟ§x§D§0§9§B§0§F§lʟ §x§C§6§9§0§0§0§lᴡ§x§C§6§9§0§0§0§lᴇ§x§C§6§9§0§0§0§lᴀ§x§C§6§9§0§0§0§lᴘ§x§C§6§9§0§0§0§lᴏ§x§C§6§9§0§0§0§lɴ")
                .decoration(TextDecoration.ITALIC, false));
        
        meta.lore(java.util.Arrays.asList(
            SERIALIZER.deserialize("§fʀɪɢʜᴛ ᴄʟɪᴄᴋ ᴛᴏ ʀᴇʀᴏʟʟ ʏᴏᴜʀ ᴡᴇᴀᴘᴏɴ").decoration(TextDecoration.ITALIC, false),
            SERIALIZER.deserialize("").decoration(TextDecoration.ITALIC, false),
            SERIALIZER.deserialize("§c§lCOST: §71 Strength").decoration(TextDecoration.ITALIC, false)
        ));
        
        meta.setCustomModelData(12346);
        meta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "smp_reroll_token"), org.bukkit.persistence.PersistentDataType.STRING, "true");
        
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Creates a Death Certificate item
     */
    public static ItemStack createDeathCertificate(String victimName, String killerName) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(SERIALIZER.deserialize("§x§F§F§0§0§0§0§lᴅ§x§E§3§0§0§0§0§lᴇ§x§C§7§0§0§0§0§lᴀ§x§A§B§0§0§0§0§lᴛ§x§8§F§0§0§0§0§lʜ §x§8§F§0§0§0§0§lᴄ§x§8§F§0§0§0§0§lᴇ§x§8§F§0§0§0§0§lʀ§x§8§F§0§0§0§0§lᴛ§x§8§F§0§0§0§0§lɪ§x§8§F§0§0§0§0§lꜰ§x§8§F§0§0§0§0§lɪ§x§8§F§0§0§0§0§lᴄ§x§8§F§0§0§0§0§lᴀ§x§8§F§0§0§0§0§lᴛ§x§8§F§0§0§0§0§lᴇ")
                .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(SERIALIZER.deserialize("§7§m-----------------------").decoration(TextDecoration.ITALIC, false));
        lore.add(SERIALIZER.deserialize("§fᴠɪᴄᴛɪᴍ: §c" + victimName).decoration(TextDecoration.ITALIC, false));
        lore.add(SERIALIZER.deserialize("§fᴋɪʟʟᴇʀ: §a" + killerName).decoration(TextDecoration.ITALIC, false));
        lore.add(SERIALIZER.deserialize("§7§m-----------------------").decoration(TextDecoration.ITALIC, false));
        lore.add(SERIALIZER.deserialize("§8§oᴀ ᴍᴇᴍᴇɴᴛᴏ ᴏꜰ ᴀ ꜰᴀʟʟᴇɴ ᴡᴀʀʀɪᴏʀ").decoration(TextDecoration.ITALIC, false));
        
        meta.lore(lore);
        meta.setCustomModelData(12347); // New CMD for certificate
        
        item.setItemMeta(meta);
        return item;
    }
}
