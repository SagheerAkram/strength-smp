package com.floki.strengthsmp.gui;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.WeaponType;
import com.floki.strengthsmp.util.CompatUtil;
import com.floki.strengthsmp.util.MessageUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * /strength: a calm, three-row overview. You, your class, the Monarch, bounties.
 * One listener for the whole plugin (registered on first use).
 */
public class StrengthHubGUI implements Listener {
    private static final int SLOT_YOU = 10;
    private static final int SLOT_CLASS = 12;
    private static final int SLOT_MONARCH = 14;
    private static final int SLOT_BOUNTY = 16;
    private static StrengthHubGUI listener;

    public static class StrengthHubHolder implements org.bukkit.inventory.InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private final StrengthSMP plugin;

    public StrengthHubGUI(StrengthSMP plugin) {
        this.plugin = plugin;
        if (listener == null) {
            listener = this;
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }
    }

    public void open(Player player) {
        UUID id = player.getUniqueId();
        int strength = this.plugin.getDataManager().getStrength(id);
        String rank = this.plugin.getConfigManager().getRankNameForStrength(strength);
        Inventory inv = Bukkit.createInventory(new StrengthHubHolder(), 27, MessageUtil.color("&8Strength  " + rank));

        ItemStack fill = item(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, fill);
        }

        // You
        int kills = this.plugin.getDataManager().getKills(id);
        int deaths = this.plugin.getDataManager().getDeaths(id);
        String kd = deaths == 0 ? String.valueOf(kills) : String.format("%.2f", kills / (double) deaths);
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta sm = (SkullMeta) head.getItemMeta();
        if (sm != null) {
            sm.setOwningPlayer(player);
            sm.setDisplayName(MessageUtil.color("&f" + player.getName()));
            List<String> lore = new ArrayList<>();
            lore.add(MessageUtil.color("&7Strength &f" + (strength > 0 ? "+" : "") + strength + "  " + rank));
            lore.add(MessageUtil.color(this.bar(strength)));
            lore.add("");
            lore.add(MessageUtil.color("&7" + kills + " kills  &8·  &7" + deaths + " deaths  &8·  &7K/D &f" + kd));
            sm.setLore(lore);
            head.setItemMeta(sm);
        }
        inv.setItem(SLOT_YOU, head);

        // Class
        WeaponType weapon = this.plugin.getDataManager().getWeapon(id);
        Material icon = Material.IRON_SWORD;
        if (weapon != null) {
            switch (weapon) {
                case AXE: icon = Material.NETHERITE_AXE; break;
                case BOW: icon = Material.BOW; break;
                case CROSSBOW: icon = Material.CROSSBOW; break;
                case TRIDENT: icon = Material.TRIDENT; break;
                case SHIELD: icon = Material.SHIELD; break;
                default: icon = Material.NETHERITE_SWORD; break;
            }
        }
        inv.setItem(SLOT_CLASS, item(icon, "&fClass",
                "&7" + (weapon != null ? weapon.getDisplayName() : "None yet"),
                "",
                "&8Click: binding & stats"));

        // Monarch
        UUID monarch = this.plugin.getMonarchService() != null ? this.plugin.getMonarchService().getCurrentMonarchUUID() : null;
        if (monarch != null) {
            OfflinePlayer m = Bukkit.getOfflinePlayer(monarch);
            inv.setItem(SLOT_MONARCH, item(Material.GOLDEN_HELMET, "&fMonarch",
                    "&e" + (m.getName() != null ? m.getName() : "Unknown") + " &8·  &7" + this.plugin.getDataManager().getStrength(monarch) + " STR",
                    m.isOnline() ? "&aOnline" : "&8Offline",
                    "",
                    "&8Click: track"));
        } else {
            inv.setItem(SLOT_MONARCH, item(Material.GOLDEN_HELMET, "&fMonarch", "&7The throne is empty."));
        }

        // Bounties
        int bounty = this.plugin.getDataManager().getBounty(id);
        inv.setItem(SLOT_BOUNTY, item(Material.EMERALD, "&fBounties",
                bounty > 0 ? "&7On your head: &c" + bounty + " STR" : "&7No bounty on you.",
                "",
                "&8Click: bounty board"));

        player.openInventory(inv);
        CompatUtil.playSound(player, player.getLocation(), "UI_BUTTON_CLICK", 0.5f, 1.2f);
    }

    /** A strength meter from the server's minimum to its maximum. */
    private String bar(int strength) {
        int min = this.plugin.getConfigManager().getMinStrength();
        int max = this.plugin.getConfigManager().getMaxStrength();
        int cells = 20;
        int filled = max == min ? cells : (int) Math.round((strength - min) / (double) (max - min) * cells);
        StringBuilder sb = new StringBuilder("&8[");
        for (int i = 0; i < cells; i++) {
            sb.append(i < filled ? (strength >= 0 ? "&f|" : "&c|") : "&8|");
        }
        return sb.append("&8]").toString();
    }

    private static ItemStack item(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtil.color(name));
            if (lore.length > 0) {
                List<String> list = new ArrayList<>();
                for (String l : lore) {
                    list.add(MessageUtil.color(l));
                }
                meta.setLore(list);
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player) || !(event.getView().getTopInventory().getHolder() instanceof StrengthHubHolder)) {
            return;
        }
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        if (slot == SLOT_CLASS) {
            player.closeInventory();
            player.performCommand("bind");
        } else if (slot == SLOT_BOUNTY) {
            player.closeInventory();
            player.performCommand("bounty");
        } else if (slot == SLOT_MONARCH) {
            UUID monarch = this.plugin.getMonarchService() != null ? this.plugin.getMonarchService().getCurrentMonarchUUID() : null;
            if (monarch == null) {
                return;
            }
            player.closeInventory();
            OfflinePlayer m = Bukkit.getOfflinePlayer(monarch);
            if (monarch.equals(player.getUniqueId())) {
                player.sendMessage(MessageUtil.color("&eYou are the Monarch. Hold the throne."));
            } else if (m.isOnline() && m.getPlayer() != null && this.plugin.getTrackingService() != null) {
                this.plugin.getTrackingService().startTracking(player, m.getPlayer(), "MONARCH");
            } else {
                player.sendMessage(MessageUtil.color("&7The Monarch is offline."));
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof StrengthHubHolder) {
            event.setCancelled(true);
        }
    }
}
