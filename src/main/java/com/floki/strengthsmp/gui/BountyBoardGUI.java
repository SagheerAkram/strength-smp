package com.floki.strengthsmp.gui;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class BountyBoardGUI implements Listener {
    public static class BountyBoardHolder implements org.bukkit.inventory.InventoryHolder {
        @Override
        public Inventory getInventory() { return null; }
    }

    private final StrengthSMP plugin;

    private static boolean registered;

    public BountyBoardGUI(StrengthSMP plugin) {
        this.plugin = plugin;
        if (!registered) { // one listener for the whole plugin, not one per /bounty
            registered = true;
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(new BountyBoardHolder(), 45, MessageUtil.color("&8&l» &6&lBOUNTY BOARD &8&l«"));

        // Frame
        ItemStack darkGlass = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack goldGlass = createItem(Material.YELLOW_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 45; i++) {
            if (i < 9 || i >= 36 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, (i % 2 == 0) ? darkGlass : goldGlass);
            }
        }

        // Fetch all active bounties
        Map<UUID, Map<UUID, Integer>> allBounties = plugin.getDataManager().getBountyCache();
        List<Map.Entry<UUID, Integer>> sorted = new ArrayList<>();

        if (allBounties != null) {
            for (Map.Entry<UUID, Map<UUID, Integer>> entry : allBounties.entrySet()) {
                UUID targetUuid = entry.getKey();
                int totalBounty = 0;
                if (entry.getValue() != null) {
                    for (int amt : entry.getValue().values()) {
                        totalBounty += amt;
                    }
                }
                if (totalBounty > 0) {
                    sorted.add(new AbstractMap.SimpleEntry<>(targetUuid, totalBounty));
                }
            }
        }

        // Sort descending by bounty amount
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        int[] availableSlots = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        int index = 0;

        if (sorted.isEmpty()) {
            inv.setItem(22, createItem(Material.OAK_SIGN, "&e&lNo Active Bounties!",
                    "&7No player currently has a price on their head.",
                    "&7Use &f/bounty <player> <amount> &7to place one!"));
        } else {
            for (Map.Entry<UUID, Integer> b : sorted) {
                if (index >= availableSlots.length) break;
                OfflinePlayer target = Bukkit.getOfflinePlayer(b.getKey());
                int amount = b.getValue();

                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                if (meta != null) {
                    meta.setOwningPlayer(target);
                    meta.setDisplayName(MessageUtil.color("&c&l☠ " + (target.getName() != null ? target.getName() : "Unknown")));
                    List<String> lore = new ArrayList<>();
                    lore.add(MessageUtil.color("&8&m─────────────────────────"));
                    lore.add(MessageUtil.color("&7Bounty Reward: &6&l" + amount + " Strength Shards"));
                    lore.add(MessageUtil.color("&7Status: " + (target.isOnline() ? "&aOnline &8(Targetable)" : "&cOffline")));
                    lore.add(MessageUtil.color("&8&m─────────────────────────"));
                    if (target.isOnline()) {
                        lore.add(MessageUtil.color("&e▶ Click to lock tracking compass!"));
                    } else {
                        lore.add(MessageUtil.color("&7Target is currently offline."));
                    }
                    meta.setLore(lore);
                    head.setItemMeta(meta);
                }

                inv.setItem(availableSlots[index], head);
                index++;
            }
        }

        // Bottom Bar info
        inv.setItem(40, createItem(Material.ARROW, "&c&l« Back to Hub", "&7Return to main /strength menu"));
        inv.setItem(36, createItem(Material.BOOK, "&e&lHow to Place a Bounty?",
                "&7Use command:",
                "&f/bounty <player> <amount>",
                "&7The reward will drop upon their death."));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GOLD, 0.8f, 1.2f);
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtil.color(name));
            if (lore != null && lore.length > 0) {
                List<String> list = new ArrayList<>();
                for (String l : lore) {
                    list.add(MessageUtil.color(l));
                }
                meta.setLore(list);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof BountyBoardHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof BountyBoardHolder)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType() == Material.AIR) return;

        if (event.getRawSlot() == 40) {
            // Back to hub
            player.closeInventory();
            player.performCommand("strength");
            return;
        }

        if (current.getType() == Material.PLAYER_HEAD) {
            SkullMeta meta = (SkullMeta) current.getItemMeta();
            if (meta != null && meta.getOwningPlayer() != null) {
                OfflinePlayer target = meta.getOwningPlayer();
                if (target.isOnline() && target.getPlayer() != null) {
                    if (target.getUniqueId().equals(player.getUniqueId())) {
                        player.sendMessage(MessageUtil.color("&c✖ You cannot hunt your own bounty!"));
                        return;
                    }
                    player.closeInventory();
                    if (plugin.getTrackingService() != null) {
                        plugin.getTrackingService().startTracking(player, target.getPlayer(), "BOUNTY");
                    }
                } else {
                    player.sendMessage(MessageUtil.color("&cThat player is currently offline!"));
                }
            }
        }
    }
}
