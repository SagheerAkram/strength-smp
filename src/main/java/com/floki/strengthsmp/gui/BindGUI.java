package com.floki.strengthsmp.gui;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.WeaponType;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BindGUI implements Listener {

    private final StrengthSMP plugin;
    private final Player player;
    private final Inventory inventory;

    public BindGUI(StrengthSMP plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 27, MessageUtil.color("&d&l⚡ Weapon Binding ⚡"));

        setupBorders();
        updateSlots();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void setupBorders() {
        // Fill borders and non-functional slots with dark purple panes
        ItemStack border = createItem(Material.PURPLE_STAINED_GLASS_PANE, "&8 ");
        for (int i = 0; i < 27; i++) {
            if (i != 2 && i != 3 && i != 4 && i != 5 && i != 6 && i != 7 && i != 11 && i != 13 && i != 15) {
                inventory.setItem(i, border);
            }
        }
    }

    private void updateSlots() {
        setupBindItem();
        setupStatusItem();
        setupUnbindItem();
        setupStatsBooks();
    }

    private void setupStatusItem() {
        WeaponType playerClass = plugin.getDataManager().getWeaponType(player.getUniqueId());
        String classStr = playerClass != null ? playerClass.getDisplayName() : "&cNone";
        
        String boundStatus = "&cInactive";
        String currentBoundId = plugin.getDataManager().getBoundWeaponId(player.getUniqueId());
        if (currentBoundId != null) {
            boundStatus = "&aActive &7(" + currentBoundId.substring(0, 8) + "...)";
        }

        ItemStack statusItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = statusItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtil.color("&b&lBinding Status & Perks"));
            List<String> lore = new ArrayList<>();
            lore.add(MessageUtil.color("&7Your Class: " + classStr));
            lore.add(MessageUtil.color("&7Bound Weapon: " + boundStatus));
            lore.add("");
            lore.add(MessageUtil.color("&eBinding Perks:"));
            lore.add(MessageUtil.color("&d- Keep on Death: &7Bound weapon is never dropped on death"));
            lore.add(MessageUtil.color("&d- Loyalty Flight: &7Flies back to you on respawn, knocking back players"));
            lore.add(MessageUtil.color("&d- Soulbound: &7Emits active magic particles while held"));
            meta.setLore(lore);
            statusItem.setItemMeta(meta);
        }
        inventory.setItem(4, statusItem);
    }

    private void setupBindItem() {
        ItemStack target = inventory.getItem(13);
        WeaponType playerClass = plugin.getDataManager().getWeaponType(player.getUniqueId());

        boolean canBind = false;
        String statusMessage = "&cPlease place a weapon in the slot.";
        
        int cost = plugin.getConfigManager().getBindCostStrength();
        boolean isFree = plugin.getDataManager().isFreeBindAvailable(player.getUniqueId());
        boolean alreadyHasBound = plugin.getDataManager().hasBoundWeapon(player.getUniqueId());
        
        if (isFree) {
            cost = 0;
        } else if (alreadyHasBound) {
            cost = 1; // Swap weapon cost
        }

        if (target != null && !target.getType().isAir()) {
            if (playerClass == null) {
                statusMessage = "&cYou do not have a weapon class!";
            } else if (!playerClass.isValidMaterial(target.getType())) {
                statusMessage = "&cWeapon does not match your class (" + playerClass.getDisplayName() + ")!";
            } else {
                ItemMeta targetMeta = target.getItemMeta();
                if (targetMeta != null) {
                    NamespacedKey boundKey = new NamespacedKey(plugin, "bound_player_uuid");
                    if (targetMeta.getPersistentDataContainer().has(boundKey, PersistentDataType.STRING)) {
                        statusMessage = "&cThis weapon is already bound!";
                    } else {
                        canBind = true;
                        statusMessage = "&aReady to Bind!";
                    }
                }
            }
        }

        ItemStack bindItem = new ItemStack(canBind ? Material.ANVIL : Material.BARRIER);
        ItemMeta meta = bindItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtil.color("&d&l⚡ Confirm Bind ⚡"));
            List<String> lore = new ArrayList<>();
            lore.add(MessageUtil.color("&7Bind the class weapon placed in Slot 13."));
            lore.add("");
            lore.add(MessageUtil.color("&7Cost: &e" + (isFree ? "FREE (Soul Sacrifice)" : cost + " Strength")));
            lore.add(MessageUtil.color("&7Status: " + statusMessage));
            if (canBind) {
                lore.add("");
                lore.add(MessageUtil.color("&eClick to bind this weapon!"));
            }
            meta.setLore(lore);
            bindItem.setItemMeta(meta);
        }
        inventory.setItem(11, bindItem);
    }

    private void setupUnbindItem() {
        ItemStack target = inventory.getItem(13);
        boolean canUnbind = false;
        String statusMessage = "&cPlease place a bound weapon in Slot 13.";

        if (target != null && !target.getType().isAir()) {
            ItemMeta targetMeta = target.getItemMeta();
            if (targetMeta != null) {
                NamespacedKey boundKey = new NamespacedKey(plugin, "bound_player_uuid");
                if (targetMeta.getPersistentDataContainer().has(boundKey, PersistentDataType.STRING)) {
                    String ownerUuidStr = targetMeta.getPersistentDataContainer().get(boundKey, PersistentDataType.STRING);
                    if (player.getUniqueId().toString().equals(ownerUuidStr)) {
                        canUnbind = true;
                        statusMessage = "&aReady to Unbind!";
                    } else {
                        statusMessage = "&cBound to a different player!";
                    }
                } else {
                    statusMessage = "&cThis weapon is not bound.";
                }
            }
        }

        ItemStack unbindItem = new ItemStack(canUnbind ? Material.GRINDSTONE : Material.BARRIER);
        ItemMeta meta = unbindItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtil.color("&c&l❌ Confirm Unbind"));
            List<String> lore = new ArrayList<>();
            lore.add(MessageUtil.color("&7Removes the binding from the weapon in Slot 13."));
            lore.add("");
            lore.add(MessageUtil.color("&7Cost: &eFree"));
            lore.add(MessageUtil.color("&7Status: " + statusMessage));
            if (canUnbind) {
                lore.add("");
                lore.add(MessageUtil.color("&eClick to unbind!"));
            }
            meta.setLore(lore);
            unbindItem.setItemMeta(meta);
        }
        inventory.setItem(15, unbindItem);
    }

    private void setupStatsBooks() {
        UUID uuid = player.getUniqueId();
        boolean hasActive = plugin.getDataManager().hasBoundWeapon(uuid);

        // Book 1: Souls Harvested (Slot 2)
        ItemStack killsBook = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta kMeta = killsBook.getItemMeta();
        if (kMeta != null) {
            kMeta.setDisplayName(MessageUtil.color("&d&l⚡ Souls Harvested"));
            List<String> lore = new ArrayList<>();
            lore.add(MessageUtil.color("&7Tracks players killed using your bound weapon."));
            lore.add("");
            int kills = plugin.getDataManager().getBoundStat(uuid, "kills");
            lore.add(MessageUtil.color("&7Total Kills: &f&l" + kills));
            lore.add("");
            lore.add(MessageUtil.color("&eRecent Victims:"));
            Map<String, Integer> breakdown = plugin.getDataManager().getBoundKillsBreakdown(uuid);
            if (breakdown.isEmpty()) {
                lore.add(MessageUtil.color("&c- None recorded."));
            } else {
                int limit = 0;
                for (Map.Entry<String, Integer> entry : breakdown.entrySet()) {
                    if (limit >= 5) break;
                    lore.add(MessageUtil.color("&d- " + entry.getKey() + ": &f" + entry.getValue() + "x"));
                    limit++;
                }
            }
            kMeta.setLore(lore);
            killsBook.setItemMeta(kMeta);
        }
        inventory.setItem(2, killsBook);

        // Book 2: Damage Dealt (Slot 3)
        ItemStack dmgBook = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta dMeta = dmgBook.getItemMeta();
        if (dMeta != null) {
            dMeta.setDisplayName(MessageUtil.color("&d&l💥 Damage Dealt"));
            List<String> lore = new ArrayList<>();
            lore.add(MessageUtil.color("&7Tracks final damage dealt with your bound weapon."));
            lore.add("");
            double damage = plugin.getDataManager().getBoundStatDouble(uuid, "damage_dealt");
            lore.add(MessageUtil.color("&7Total Damage: &f&l" + String.format("%.1f", damage)));
            dMeta.setLore(lore);
            dmgBook.setItemMeta(dMeta);
        }
        inventory.setItem(3, dmgBook);

        // Book 3: Death Saves Triggered (Slot 5)
        ItemStack savesBook = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta sMeta = savesBook.getItemMeta();
        if (sMeta != null) {
            sMeta.setDisplayName(MessageUtil.color("&d&l🛡️ Soul Sacrifices"));
            List<String> lore = new ArrayList<>();
            lore.add(MessageUtil.color("&7Tracks death saves triggered by your bound weapon."));
            lore.add("");
            int saves = plugin.getDataManager().getBoundStat(uuid, "sacrifices");
            lore.add(MessageUtil.color("&7Total Saves: &f&l" + saves));
            sMeta.setLore(lore);
            savesBook.setItemMeta(sMeta);
        }
        inventory.setItem(5, savesBook);

        // Book 4: Time Bound (Slot 6)
        ItemStack timeBook = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta tMeta = timeBook.getItemMeta();
        if (tMeta != null) {
            tMeta.setDisplayName(MessageUtil.color("&d&l⏳ Duration Bound"));
            List<String> lore = new ArrayList<>();
            lore.add(MessageUtil.color("&7Tracks elapsed time since you bound your current weapon."));
            lore.add("");
            if (hasActive) {
                long boundTime = plugin.getDataManager().getBoundTimestamp(uuid);
                long elapsed = System.currentTimeMillis() - boundTime;
                lore.add(MessageUtil.color("&7Current Duration: &f&l" + formatDuration(elapsed)));
            } else {
                lore.add(MessageUtil.color("&7Current Duration: &cNo active bond."));
            }
            tMeta.setLore(lore);
            timeBook.setItemMeta(tMeta);
        }
        inventory.setItem(6, timeBook);

        // Book 5: Ability Triggers (Slot 7)
        ItemStack abilityBook = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta aMeta = abilityBook.getItemMeta();
        if (aMeta != null) {
            aMeta.setDisplayName(MessageUtil.color("&d&l✨ Ability Triggers"));
            List<String> lore = new ArrayList<>();
            lore.add(MessageUtil.color("&7Tracks weapon ability uses while bound."));
            lore.add("");
            int uses = plugin.getDataManager().getBoundStat(uuid, "ability_uses");
            lore.add(MessageUtil.color("&7Total Uses: &f&l" + uses));
            aMeta.setLore(lore);
            abilityBook.setItemMeta(aMeta);
        }
        inventory.setItem(7, abilityBook);
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "d " + (hours % 24) + "h " + (minutes % 60) + "m";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m " + (seconds % 60) + "s";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }

    public void open() {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;

        // Allow interacting with player inventory completely
        if (event.getClickedInventory() != null && event.getClickedInventory().equals(player.getInventory())) {
            refreshGUI();
            return;
        }

        int slot = event.getRawSlot();

        // Slot 13 is the input slot - allow player to insert/withdraw items
        if (slot == 13) {
            refreshGUI();
            return;
        }

        // Cancel clicks in any other GUI slots
        event.setCancelled(true);

        if (slot == 11) {
            handleBind();
        } else if (slot == 15) {
            handleUnbind();
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().equals(inventory)) {
            event.setCancelled(true);
        }
    }

    private void refreshGUI() {
        Bukkit.getScheduler().runTask(plugin, this::updateSlots);
    }

    private void handleBind() {
        ItemStack target = inventory.getItem(13);
        WeaponType playerClass = plugin.getDataManager().getWeaponType(player.getUniqueId());

        if (target == null || target.getType().isAir() || playerClass == null || !playerClass.isValidMaterial(target.getType())) {
            player.sendMessage(MessageUtil.parse("&cInvalid class weapon in Slot 13!"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        ItemMeta meta = target.getItemMeta();
        if (meta == null) return;

        NamespacedKey boundKey = new NamespacedKey(plugin, "bound_player_uuid");
        NamespacedKey itemIdKey = new NamespacedKey(plugin, "bound_item_id");

        if (meta.getPersistentDataContainer().has(boundKey, PersistentDataType.STRING)) {
            player.sendMessage(MessageUtil.parse("&cThis weapon is already bound!"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Determine cost
        boolean isFree = plugin.getDataManager().isFreeBindAvailable(player.getUniqueId());
        boolean alreadyHasBound = plugin.getDataManager().hasBoundWeapon(player.getUniqueId());
        int cost = isFree ? 0 : (alreadyHasBound ? 1 : plugin.getConfigManager().getBindCostStrength());

        // Check strength
        int currentStrength = plugin.getDataManager().getStrength(player.getUniqueId());
        if (currentStrength < cost) {
            player.sendMessage(MessageUtil.parse("&cYou need at least " + cost + " strength to bind your weapon. You have " + currentStrength + "."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Deduct strength
        if (cost > 0) {
            plugin.getDataManager().subtractStrength(player.getUniqueId(), cost);
            player.sendMessage(MessageUtil.parse("&aSpent &e" + cost + " strength &ato bind your class weapon!"));
        } else {
            player.sendMessage(MessageUtil.parse("&aYour weapon has been bound for &eFREE &aby the grace of your Soul Sacrifice!"));
            plugin.getDataManager().setFreeBindAvailable(player.getUniqueId(), false); // Consume the free bind
        }

        plugin.updateDisplay(player);
        plugin.getMonarchService().calculateNewMonarch();

        // Generate a new unique ID for this bound item instance
        UUID boundWeaponId = UUID.randomUUID();
        plugin.getDataManager().setBoundWeaponId(player.getUniqueId(), boundWeaponId.toString());
        plugin.getDataManager().setHasBoundWeapon(player.getUniqueId(), true);
        plugin.getDataManager().setBoundTimestamp(player.getUniqueId(), System.currentTimeMillis());

        // Save metadata on the item
        meta.getPersistentDataContainer().set(boundKey, PersistentDataType.STRING, player.getUniqueId().toString());
        meta.getPersistentDataContainer().set(itemIdKey, PersistentDataType.STRING, boundWeaponId.toString());

        // Set CustomModelData for custom textures
        int modelData = plugin.getConfigManager().getWeaponCustomModelData(playerClass);
        if (modelData > 0) {
            meta.setCustomModelData(modelData);
        }

        // Add lore indicator
        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<>();
        String prefix = MessageUtil.parse(plugin.getConfigManager().getBindLorePrefix());
        lore.remove(prefix);
        lore.add(0, prefix);
        meta.setLore(lore);
        target.setItemMeta(meta);

        // Give the item to the player
        ItemStack finalItem = target.clone();
        inventory.setItem(13, null); // Clear slot so it won't get returned on close
        player.closeInventory();

        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(finalItem);
        if (!leftover.isEmpty()) {
            for (ItemStack drop : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
        }

        // Run animating name change (flashing)
        final String displayName;
        if (plugin.getConfigManager().shouldRenameOnBind(playerClass)) {
            displayName = plugin.getConfigManager().getWeaponCosmeticName(playerClass);
        } else {
            displayName = meta.hasDisplayName() ? meta.getDisplayName() : playerClass.getDisplayName();
        }
        final String originalName = displayName;
        new BukkitRunnable() {
            int step = 0;
            final String[] colors = {"&d", "&5", "&b", "&a", "&e", "&6", "&c", "&4"};
            
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                
                // Locate item in inventory to animate
                ItemStack targetItem = null;
                for (ItemStack invItem : player.getInventory().getContents()) {
                    if (invItem != null && invItem.hasItemMeta()) {
                        String id = invItem.getItemMeta().getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
                        if (boundWeaponId.toString().equals(id)) {
                            targetItem = invItem;
                            break;
                        }
                    }
                }

                if (targetItem == null) {
                    cancel();
                    return;
                }

                ItemMeta currentMeta = targetItem.getItemMeta();
                if (currentMeta == null) return;

                if (step >= 8) {
                    currentMeta.setDisplayName(MessageUtil.parse("&d&l⚡ &5&l" + org.bukkit.ChatColor.stripColor(originalName) + " &d&l⚡"));
                    targetItem.setItemMeta(currentMeta);
                    player.sendMessage(MessageUtil.parse("&d&l⚡ &f&lYOUR WEAPON HAS BEEN BOUND TO YOU! &d&l⚡"));
                    player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_RETURN, 1.0f, 1.2f);
                    cancel();
                } else {
                    currentMeta.setDisplayName(MessageUtil.parse(colors[step % colors.length] + "⚡ " + org.bukkit.ChatColor.stripColor(originalName) + " ⚡"));
                    targetItem.setItemMeta(currentMeta);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 0.5f + (step * 0.15f));
                    step++;
                }
            }
        }.runTaskTimer(plugin, 0L, 3L);
    }

    private void handleUnbind() {
        ItemStack target = inventory.getItem(13);
        if (target == null || target.getType().isAir()) return;

        ItemMeta meta = target.getItemMeta();
        if (meta == null) return;

        NamespacedKey boundKey = new NamespacedKey(plugin, "bound_player_uuid");
        NamespacedKey itemIdKey = new NamespacedKey(plugin, "bound_item_id");

        if (!meta.getPersistentDataContainer().has(boundKey, PersistentDataType.STRING)) return;

        String ownerUuidStr = meta.getPersistentDataContainer().get(boundKey, PersistentDataType.STRING);
        if (!player.getUniqueId().toString().equals(ownerUuidStr)) return;

        // Perform unbind in player storage
        plugin.getDataManager().setBoundWeaponId(player.getUniqueId(), null);
        plugin.getDataManager().setHasBoundWeapon(player.getUniqueId(), false);

        // Remove item metadata
        meta.getPersistentDataContainer().remove(boundKey);
        meta.getPersistentDataContainer().remove(itemIdKey);
        if (meta.hasCustomModelData()) {
            meta.setCustomModelData(null);
        }

        // Remove lore prefix
        List<String> lore = meta.getLore();
        if (lore != null) {
            String prefix = MessageUtil.parse(plugin.getConfigManager().getBindLorePrefix());
            lore.remove(prefix);
            meta.setLore(lore);
        }

        // Clean display name
        if (meta.hasDisplayName()) {
            String strippedName = org.bukkit.ChatColor.stripColor(meta.getDisplayName());
            strippedName = strippedName.replace("⚡", "").trim();
            meta.setDisplayName(MessageUtil.color("&f" + strippedName));
        }

        target.setItemMeta(meta);

        // Return the item to the player
        ItemStack finalItem = target.clone();
        inventory.setItem(13, null); // Clear slot so it won't get returned on close
        player.closeInventory();

        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(finalItem);
        if (!leftover.isEmpty()) {
            for (ItemStack drop : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
        }

        player.sendMessage(MessageUtil.parse("&aSuccessfully unbound your weapon."));
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) {
            HandlerList.unregisterAll(this);

            // Safely return any item left in slot 13 to the player
            ItemStack leftoverItem = inventory.getItem(13);
            if (leftoverItem != null && !leftoverItem.getType().isAir()) {
                inventory.setItem(13, null);
                HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(leftoverItem);
                if (!remaining.isEmpty()) {
                    for (ItemStack item : remaining.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), item);
                    }
                }
            }
        }
    }

    private ItemStack createItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtil.color(name));
            item.setItemMeta(meta);
        }
        return item;
    }
}
