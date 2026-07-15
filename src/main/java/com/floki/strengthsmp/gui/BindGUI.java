/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
 *  org.bukkit.Sound
 *  org.bukkit.enchantments.Enchantment
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Item
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.HandlerList
 *  org.bukkit.event.Listener
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.inventory.InventoryCloseEvent
 *  org.bukkit.event.inventory.InventoryDragEvent
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.ItemFlag
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 */
package com.floki.strengthsmp.gui;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.WeaponType;
import com.floki.strengthsmp.util.CompatUtil;
import com.floki.strengthsmp.util.MessageUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class BindGUI
implements Listener {
    private final StrengthSMP plugin;
    private final Player player;
    private final Inventory inventory;
    private int selectedModelData;

    public BindGUI(StrengthSMP plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, (int)27, (String)MessageUtil.color("&d&l\u26a1 Weapon Binding \u26a1"));
        WeaponType playerClass = plugin.getDataManager().getWeaponType(player.getUniqueId());
        this.selectedModelData = this.getChosenSkinForClass(playerClass);
        this.setupBorders();
        this.updateSlots();
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)plugin);
    }

    private void setupBorders() {
        ItemStack border = this.createItem(Material.PURPLE_STAINED_GLASS_PANE, "&8 ");
        for (int i = 0; i < 27; ++i) {
            if (i == 2 || i == 3 || i == 4 || i == 5 || i == 6 || i == 7 || i == 11 || i == 13 || i == 15) continue;
            this.inventory.setItem(i, border);
        }
    }

    private void setupSkinSelection() {
        WeaponType playerClass = this.plugin.getDataManager().getWeaponType(this.player.getUniqueId());
        ItemStack border = this.createItem(Material.PURPLE_STAINED_GLASS_PANE, "&8 ");
        for (int i = 18; i < 27; ++i) {
            this.inventory.setItem(i, border);
        }
        if (playerClass == null) {
            return;
        }
        switch (playerClass) {
            case SWORD: {
                this.addSkinItem(22, Material.NETHERITE_SWORD, 13501, "&c&lInferno Sword", "&7A blazing blade forged in the depths of the Nether.");
                break;
            }
            case TRIDENT: {
                this.addSkinItem(22, Material.TRIDENT, 13250, "&7&lNecros Trident", "&7A soul-infused weapon forged from deep sea graves.");
                break;
            }
            case AXE: {
                this.addSkinItem(22, Material.NETHERITE_AXE, 13210, "&7&lNecros Axe", "&7A soul-harvesting weapon fueled by dark magic.");
                break;
            }
            case BOW: {
                this.addSkinItem(22, Material.BOW, 13030, "&b&lLunar Bow", "&7A celestial recurve bow reflecting moonlight.");
                break;
            }
            case CROSSBOW: {
                this.addSkinItem(22, Material.CROSSBOW, 13240, "&7&lNecros Crossbow", "&7A heavy repeating arbalest powered by nether energy.");
                break;
            }
            case SHIELD: {
                this.addSkinItem(22, Material.SHIELD, 13520, "&c&lInferno Shield", "&7A protective aegis forged in active volcanos.");
            }
        }
    }

    private void addSkinItem(int slot, Material material, int cmd, String name, String desc) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtil.color(name));
            meta.setCustomModelData(Integer.valueOf(cmd));
            NamespacedKey skinCmdKey = new NamespacedKey((Plugin)this.plugin, "gui_skin_cmd");
            meta.getPersistentDataContainer().set(skinCmdKey, PersistentDataType.INTEGER, cmd);
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(MessageUtil.color(desc));
            lore.add("");
            if (this.selectedModelData == cmd) {
                lore.add(MessageUtil.color("&7Status: &a&lSELECTED"));
                lore.add(MessageUtil.color("&eThis skin will be applied on binding."));
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
            } else {
                lore.add(MessageUtil.color("&7Status: &cNot Selected"));
                lore.add("");
                lore.add(MessageUtil.color("&eClick to select this skin!"));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        this.inventory.setItem(slot, item);
    }

    private int getChosenSkinForClass(WeaponType type) {
        if (type == null) {
            return 0;
        }
        switch (type) {
            case SWORD: {
                return 13501;
            }
            case TRIDENT: {
                return 13250;
            }
            case AXE: {
                return 13210;
            }
            case BOW: {
                return 13030;
            }
            case CROSSBOW: {
                return 13240;
            }
            case SHIELD: {
                return 13220;
            }
        }
        return 0;
    }

    private boolean isValidSkinForClass(WeaponType type, int cmd) {
        if (type == null) {
            return false;
        }
        switch (type) {
            case SWORD: {
                return cmd == 12348 || cmd == 12354 || cmd == 12355 || cmd == 12360 || cmd == 12361 || cmd == 12362 || cmd == 13501 || cmd == 13201 || cmd == 13301;
            }
            case AXE: {
                return cmd == 12349 || cmd == 12363 || cmd == 12364 || cmd == 12365 || cmd == 13210 || cmd == 13510 || cmd == 13310;
            }
            case BOW: {
                return cmd == 12350 || cmd == 12369 || cmd == 12370 || cmd == 12371 || cmd == 13030 || cmd == 13330 || cmd == 13530;
            }
            case CROSSBOW: {
                return cmd == 12351 || cmd == 12372 || cmd == 12373 || cmd == 12374 || cmd == 13240 || cmd == 13340 || cmd == 13540;
            }
            case TRIDENT: {
                return cmd == 12352 || cmd == 12356 || cmd == 12375 || cmd == 12376 || cmd == 12377 || cmd == 13250 || cmd == 13050 || cmd == 13350;
            }
            case SHIELD: {
                return cmd == 12353 || cmd == 12366 || cmd == 12367 || cmd == 12368 || cmd == 13520 || cmd == 13220 || cmd == 13320;
            }
        }
        return false;
    }

    private String getSkinDisplayName(int customModelData) {
        switch (customModelData) {
            case 12348: {
                return "&b&lDuelist Katana";
            }
            case 12354: {
                return "&5&lNetherite Claymore";
            }
            case 12355: {
                return "&b&lNetherite Twinblade";
            }
            case 12352: {
                return "&3&lRoyal Spear";
            }
            case 12356: {
                return "&a&lNetherite Scythe";
            }
            case 12349: {
                return "&c&lHeirloom Axe";
            }
            case 12350: {
                return "&e&lRecurve Bow";
            }
            case 12351: {
                return "&6&lHeavy Crossbow";
            }
            case 12353: {
                return "&f&lBulwark Shield";
            }
            case 12360: {
                return "&c&lInferno Blade";
            }
            case 12361: {
                return "&b&lFrost Fang";
            }
            case 12362: {
                return "&5&lVoid Reaper";
            }
            case 12363: {
                return "&2&lWarden's Cleaver";
            }
            case 12364: {
                return "&6&lEmber Hatchet";
            }
            case 12365: {
                return "&d&lCrystal Vanguard";
            }
            case 12366: {
                return "&c&lDragon's Aegis";
            }
            case 12367: {
                return "&7&lSentinel's Wall";
            }
            case 12368: {
                return "&e&lThorned Bulwark";
            }
            case 12369: {
                return "&6&lPhoenix Wing";
            }
            case 12370: {
                return "&8&lShadow Stalker";
            }
            case 12371: {
                return "&e&lCelestial Arc";
            }
            case 12372: {
                return "&4&lSiege Breaker";
            }
            case 12373: {
                return "&1&lNightfall Repeater";
            }
            case 12374: {
                return "&6&lRadiant Arbalest";
            }
            case 12375: {
                return "&b&lPoseidon's Wrath";
            }
            case 12376: {
                return "&9&lAbyssal Spike";
            }
            case 12377: {
                return "&e&lSolar Lance";
            }
            case 13501: {
                return "&c&lHellfire Blade";
            }
            case 13201: {
                return "&8&lDeathbringer";
            }
            case 13301: {
                return "&a&lSylvan Edge";
            }
            case 13250: {
                return "&8&lSoul Piercer";
            }
            case 13050: {
                return "&b&lCrescent Spear";
            }
            case 13350: {
                return "&a&lNature's Wrath";
            }
            case 13210: {
                return "&8&lAbyssal Cleaver";
            }
            case 13510: {
                return "&c&lDemon's Hatchet";
            }
            case 13310: {
                return "&a&lForest's Bite";
            }
            case 13030: {
                return "&b&lMoonlight Arc";
            }
            case 13330: {
                return "&a&lWhispering Bow";
            }
            case 13530: {
                return "&c&lBlaze String";
            }
            case 13240: {
                return "&8&lDeath's Arbalest";
            }
            case 13340: {
                return "&a&lPixie Shooter";
            }
            case 13540: {
                return "&c&lHellfire Launcher";
            }
            case 13520: {
                return "&c&lDemon's Bulwark";
            }
            case 13220: {
                return "&8&lAegis of the Damned";
            }
            case 13320: {
                return "&a&lEnchanted Ward";
            }
        }
        return null;
    }

    private String getSkinLore(int customModelData) {
        switch (customModelData) {
            case 13501: {
                return "&7Forged in the deepest pits of the nether.\n&7Its blade sears the souls of its victims.";
            }
            case 13201: {
                return "&7An ancient blade wielded by the forgotten king.\n&7It hums with a dark, necrotic energy.";
            }
            case 13301: {
                return "&7A shimmering sword blessed by forest spirits.\n&7It strikes with the swiftness of the wind.";
            }
            case 13250: {
                return "&7A sinister trident that pierces through armor\n&7and drains the essence of your foes.";
            }
            case 13050: {
                return "&7Bathed in moonlight, this elegant spear\n&7commands the tides of battle.";
            }
            case 13350: {
                return "&7Thorns intertwine around this trident,\n&7unleashing the wild wrath of nature.";
            }
            case 13210: {
                return "&7A heavy, brutal cleaver that shatters\n&7defenses and leaves only destruction.";
            }
            case 13510: {
                return "&7Screams echo from the molten edge of this axe\n&7as it rends flesh and bone.";
            }
            case 13310: {
                return "&7Carved from an ancient, enchanted oak.\n&7It strikes with the force of a falling redwood.";
            }
            case 13030: {
                return "&7A majestic bow that fires arrows composed\n&7of pure, condensed starlight.";
            }
            case 13330: {
                return "&7Delicate yet deadly. Its arrows whisper\n&7through the air before finding their mark.";
            }
            case 13530: {
                return "&7The string of this bow burns the fingers of\n&7the unworthy, firing bolts of pure fire.";
            }
            case 13240: {
                return "&7A mechanical terror that launches\n&7heavy bolts dripping with dark magic.";
            }
            case 13340: {
                return "&7A whimsical crossbow that shoots\n&7deadly, shimmering magical flares.";
            }
            case 13540: {
                return "&7A volatile siege weapon designed to\n&7rain destruction upon the battlefield.";
            }
            case 13520: {
                return "&7A massive, burning shield that reflects\n&7both damage and fear back at attackers.";
            }
            case 13220: {
                return "&7An impenetrable wall forged from the\n&7souls of fallen warriors.";
            }
            case 13320: {
                return "&7A beautiful but sturdy shield that blooms\n&7with magical protection when struck.";
            }
        }
        return "&7A legendary artifact of immense power.";
    }

    private void updateSlots() {
        int cmd;
        ItemMeta targetMeta;
        WeaponType playerClass = this.plugin.getDataManager().getWeaponType(this.player.getUniqueId());
        ItemStack target = this.inventory.getItem(13);
        this.selectedModelData = target != null && !target.getType().isAir() && playerClass != null && playerClass.isValidMaterial(target.getType()) ? ((targetMeta = target.getItemMeta()) != null && targetMeta.hasCustomModelData() ? (this.isValidSkinForClass(playerClass, cmd = targetMeta.getCustomModelData()) ? cmd : this.getChosenSkinForClass(playerClass)) : this.getChosenSkinForClass(playerClass)) : this.getChosenSkinForClass(playerClass);
        this.setupBindItem();
        this.setupStatusItem();
        this.setupUnbindItem();
        this.setupStatsBooks();
        this.setupSkinSelection();
    }

    private void setupStatusItem() {
        ItemStack statusItem;
        ItemMeta meta;
        WeaponType playerClass = this.plugin.getDataManager().getWeaponType(this.player.getUniqueId());
        String classStr = playerClass != null ? playerClass.getDisplayName() : "&cNone";
        Object boundStatus = "&cInactive";
        String currentBoundId = this.plugin.getDataManager().getBoundWeaponId(this.player.getUniqueId());
        if (currentBoundId != null) {
            boundStatus = "&aActive &7(" + currentBoundId.substring(0, 8) + "...)";
        }
        if ((meta = (statusItem = new ItemStack(Material.NETHER_STAR)).getItemMeta()) != null) {
            meta.setDisplayName(MessageUtil.color("&b&lBinding Status & Perks"));
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(MessageUtil.color("&7Your Class: " + classStr));
            lore.add(MessageUtil.color("&7Bound Weapon: " + (String)boundStatus));
            lore.add("");
            lore.add(MessageUtil.color("&eBinding Perks:"));
            lore.add(MessageUtil.color("&d- Keep on Death: &7Bound weapon is never dropped on death"));
            lore.add(MessageUtil.color("&d- Loyalty Flight: &7Flies back to you on respawn, knocking back players"));
            lore.add(MessageUtil.color("&d- Soulbound: &7Emits active magic particles while held"));
            meta.setLore(lore);
            statusItem.setItemMeta(meta);
        }
        this.inventory.setItem(4, statusItem);
    }

    private void setupBindItem() {
        ItemStack bindItem;
        ItemMeta meta;
        ItemStack target = this.inventory.getItem(13);
        WeaponType playerClass = this.plugin.getDataManager().getWeaponType(this.player.getUniqueId());
        boolean canBind = false;
        Object statusMessage = "&cPlease place a weapon in the slot.";
        int cost = this.plugin.getConfigManager().getBindCostStrength();
        boolean isFree = this.plugin.getDataManager().isFreeBindAvailable(this.player.getUniqueId());
        boolean alreadyHasBound = this.plugin.getDataManager().hasBoundWeapon(this.player.getUniqueId());
        if (isFree) {
            cost = 0;
        } else if (alreadyHasBound) {
            cost = 1;
        }
        if (target != null && !target.getType().isAir()) {
            if (playerClass == null) {
                statusMessage = "&cYou do not have a weapon class!";
            } else if (!playerClass.isValidMaterial(target.getType())) {
                statusMessage = "&cWeapon does not match your class (" + playerClass.getDisplayName() + ")!";
            } else {
                ItemMeta targetMeta = target.getItemMeta();
                if (targetMeta != null) {
                    NamespacedKey boundKey = new NamespacedKey((Plugin)this.plugin, "bound_player_uuid");
                    if (targetMeta.getPersistentDataContainer().has(boundKey, PersistentDataType.STRING)) {
                        statusMessage = "&cThis weapon is already bound!";
                    } else {
                        canBind = true;
                        statusMessage = "&aReady to Bind!";
                    }
                }
            }
        }
        if ((meta = (bindItem = new ItemStack(canBind ? Material.ANVIL : Material.BARRIER)).getItemMeta()) != null) {
            meta.setDisplayName(MessageUtil.color("&d&l\u26a1 Confirm Bind \u26a1"));
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(MessageUtil.color("&7Bind the class weapon placed in Slot 13."));
            lore.add("");
            lore.add(MessageUtil.color("&7Cost: &e" + (String)(isFree ? "FREE (Soul Sacrifice)" : cost + " Strength")));
            lore.add(MessageUtil.color("&7Status: " + (String)statusMessage));
            if (canBind) {
                lore.add("");
                lore.add(MessageUtil.color("&eClick to bind this weapon!"));
            }
            meta.setLore(lore);
            bindItem.setItemMeta(meta);
        }
        this.inventory.setItem(11, bindItem);
    }

    private void setupUnbindItem() {
        ItemStack unbindItem;
        ItemMeta meta;
        ItemMeta targetMeta;
        ItemStack target = this.inventory.getItem(13);
        boolean canUnbind = false;
        String statusMessage = "&cPlease place a bound weapon in Slot 13.";
        if (target != null && !target.getType().isAir() && (targetMeta = target.getItemMeta()) != null) {
            NamespacedKey boundKey = new NamespacedKey((Plugin)this.plugin, "bound_player_uuid");
            if (targetMeta.getPersistentDataContainer().has(boundKey, PersistentDataType.STRING)) {
                String ownerUuidStr = (String)targetMeta.getPersistentDataContainer().get(boundKey, PersistentDataType.STRING);
                if (this.player.getUniqueId().toString().equals(ownerUuidStr)) {
                    canUnbind = true;
                    statusMessage = "&aReady to Unbind!";
                } else {
                    statusMessage = "&cBound to a different player!";
                }
            } else {
                statusMessage = "&cThis weapon is not bound.";
            }
        }
        if ((meta = (unbindItem = new ItemStack(canUnbind ? Material.GRINDSTONE : Material.BARRIER)).getItemMeta()) != null) {
            meta.setDisplayName(MessageUtil.color("&c&l\u274c Confirm Unbind"));
            ArrayList<String> lore = new ArrayList<String>();
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
        this.inventory.setItem(15, unbindItem);
    }

    private void setupStatsBooks() {
        UUID uuid = this.player.getUniqueId();
        boolean hasActive = this.plugin.getDataManager().hasBoundWeapon(uuid);
        ItemStack killsBook = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta kMeta = killsBook.getItemMeta();
        if (kMeta != null) {
            kMeta.setDisplayName(MessageUtil.color("&d&l\u26a1 Souls Harvested"));
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(MessageUtil.color("&7Tracks players killed using your bound weapon."));
            lore.add("");
            int kills = this.plugin.getDataManager().getBoundStat(uuid, "kills");
            lore.add(MessageUtil.color("&7Total Kills: &f&l" + kills));
            lore.add("");
            lore.add(MessageUtil.color("&eRecent Victims:"));
            Map<String, Integer> breakdown = this.plugin.getDataManager().getBoundKillsBreakdown(uuid);
            if (breakdown.isEmpty()) {
                lore.add(MessageUtil.color("&c- None recorded."));
            } else {
                int limit = 0;
                for (Map.Entry<String, Integer> entry : breakdown.entrySet()) {
                    if (limit >= 5) break;
                    lore.add(MessageUtil.color("&d- " + entry.getKey() + ": &f" + String.valueOf(entry.getValue()) + "x"));
                    ++limit;
                }
            }
            kMeta.setLore(lore);
            killsBook.setItemMeta(kMeta);
        }
        this.inventory.setItem(2, killsBook);
        ItemStack dmgBook = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta dMeta = dmgBook.getItemMeta();
        if (dMeta != null) {
            dMeta.setDisplayName(MessageUtil.color("&d&l\ud83d\udca5 Damage Dealt"));
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(MessageUtil.color("&7Tracks final damage dealt with your bound weapon."));
            lore.add("");
            double damage = this.plugin.getDataManager().getBoundStatDouble(uuid, "damage_dealt");
            lore.add(MessageUtil.color("&7Total Damage: &f&l" + String.format("%.1f", damage)));
            dMeta.setLore(lore);
            dmgBook.setItemMeta(dMeta);
        }
        this.inventory.setItem(3, dmgBook);
        ItemStack savesBook = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta sMeta = savesBook.getItemMeta();
        if (sMeta != null) {
            sMeta.setDisplayName(MessageUtil.color("&d&l\ud83d\udee1\ufe0f Soul Sacrifices"));
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(MessageUtil.color("&7Tracks death saves triggered by your bound weapon."));
            lore.add("");
            int saves = this.plugin.getDataManager().getBoundStat(uuid, "sacrifices");
            lore.add(MessageUtil.color("&7Total Saves: &f&l" + saves));
            sMeta.setLore(lore);
            savesBook.setItemMeta(sMeta);
        }
        this.inventory.setItem(5, savesBook);
        ItemStack timeBook = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta tMeta = timeBook.getItemMeta();
        if (tMeta != null) {
            tMeta.setDisplayName(MessageUtil.color("&d&l\u23f3 Duration Bound"));
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(MessageUtil.color("&7Tracks elapsed time since you bound your current weapon."));
            lore.add("");
            if (hasActive) {
                long boundTime = this.plugin.getDataManager().getBoundTimestamp(uuid);
                long elapsed = System.currentTimeMillis() - boundTime;
                lore.add(MessageUtil.color("&7Current Duration: &f&l" + this.formatDuration(elapsed)));
            } else {
                lore.add(MessageUtil.color("&7Current Duration: &cNo active bond."));
            }
            tMeta.setLore(lore);
            timeBook.setItemMeta(tMeta);
        }
        this.inventory.setItem(6, timeBook);
        ItemStack abilityBook = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta aMeta = abilityBook.getItemMeta();
        if (aMeta != null) {
            aMeta.setDisplayName(MessageUtil.color("&d&l\u2728 Ability Triggers"));
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(MessageUtil.color("&7Tracks weapon ability uses while bound."));
            lore.add("");
            int uses = this.plugin.getDataManager().getBoundStat(uuid, "ability_uses");
            lore.add(MessageUtil.color("&7Total Uses: &f&l" + uses));
            aMeta.setLore(lore);
            abilityBook.setItemMeta(aMeta);
        }
        this.inventory.setItem(7, abilityBook);
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        long days = hours / 24L;
        if (days > 0L) {
            return days + "d " + hours % 24L + "h " + minutes % 60L + "m";
        }
        if (hours > 0L) {
            return hours + "h " + minutes % 60L + "m " + seconds % 60L + "s";
        }
        if (minutes > 0L) {
            return minutes + "m " + seconds % 60L + "s";
        }
        return seconds + "s";
    }

    public void open() {
        this.player.openInventory(this.inventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        ItemStack clickedItem;
        if (!event.getInventory().equals((Object)this.inventory)) {
            return;
        }
        if (event.getClickedInventory() != null && event.getClickedInventory().equals((Object)this.player.getInventory())) {
            this.refreshGUI();
            return;
        }
        int slot = event.getRawSlot();
        if (slot == 13) {
            this.refreshGUI();
            return;
        }
        event.setCancelled(true);
        if (slot == 11) {
            this.handleBind();
        } else if (slot == 15) {
            this.handleUnbind();
        } else if (slot >= 18 && slot <= 26 && (clickedItem = event.getCurrentItem()) != null && clickedItem.hasItemMeta()) {
            ItemMeta clickedMeta = clickedItem.getItemMeta();
            NamespacedKey skinCmdKey = new NamespacedKey((Plugin)this.plugin, "gui_skin_cmd");
            if (clickedMeta.getPersistentDataContainer().has(skinCmdKey, PersistentDataType.INTEGER)) {
                int cmd;
                this.selectedModelData = cmd = ((Integer)clickedMeta.getPersistentDataContainer().get(skinCmdKey, PersistentDataType.INTEGER)).intValue();
                this.player.playSound(this.player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                this.refreshGUI();
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().equals((Object)this.inventory)) {
            event.setCancelled(true);
        }
    }

    private void refreshGUI() {
        Bukkit.getScheduler().runTask((Plugin)this.plugin, this::updateSlots);
    }

    private void handleBind() {
        String skinName;
        ItemStack target = this.inventory.getItem(13);
        WeaponType playerClass = this.plugin.getDataManager().getWeaponType(this.player.getUniqueId());
        if (target == null || target.getType().isAir() || playerClass == null || !playerClass.isValidMaterial(target.getType())) {
            this.player.sendMessage(MessageUtil.parse("&cInvalid class weapon in Slot 13!"));
            this.player.playSound(this.player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        ItemMeta meta = target.getItemMeta();
        if (meta == null) {
            return;
        }
        NamespacedKey boundKey = new NamespacedKey((Plugin)this.plugin, "bound_player_uuid");
        final NamespacedKey itemIdKey = new NamespacedKey((Plugin)this.plugin, "bound_item_id");
        if (meta.getPersistentDataContainer().has(boundKey, PersistentDataType.STRING)) {
            this.player.sendMessage(MessageUtil.parse("&cThis weapon is already bound!"));
            this.player.playSound(this.player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        boolean isFree = this.plugin.getDataManager().isFreeBindAvailable(this.player.getUniqueId());
        boolean alreadyHasBound = this.plugin.getDataManager().hasBoundWeapon(this.player.getUniqueId());
        int cost = isFree ? 0 : (alreadyHasBound ? 1 : this.plugin.getConfigManager().getBindCostStrength());
        int currentStrength = this.plugin.getDataManager().getStrength(this.player.getUniqueId());
        if (currentStrength < cost) {
            this.player.sendMessage(MessageUtil.parse("&cYou need at least " + cost + " strength to bind your weapon. You have " + currentStrength + "."));
            this.player.playSound(this.player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        if (cost > 0) {
            this.plugin.getDataManager().subtractStrength(this.player.getUniqueId(), cost);
            this.player.sendMessage(MessageUtil.parse("&aSpent &e" + cost + " strength &ato bind your class weapon!"));
        } else {
            this.player.sendMessage(MessageUtil.parse("&aYour weapon has been bound for &eFREE &aby the grace of your Soul Sacrifice!"));
            this.plugin.getDataManager().setFreeBindAvailable(this.player.getUniqueId(), false);
        }
        this.plugin.updateDisplay(this.player);
        this.plugin.getMonarchService().calculateNewMonarch();
        final UUID boundWeaponId = UUID.randomUUID();
        this.plugin.getDataManager().setBoundWeaponId(this.player.getUniqueId(), boundWeaponId.toString());
        this.plugin.getDataManager().setHasBoundWeapon(this.player.getUniqueId(), true);
        this.plugin.getDataManager().setBoundTimestamp(this.player.getUniqueId(), System.currentTimeMillis());
        meta.getPersistentDataContainer().set(boundKey, PersistentDataType.STRING, this.player.getUniqueId().toString());
        meta.getPersistentDataContainer().set(itemIdKey, PersistentDataType.STRING, boundWeaponId.toString());
        NamespacedKey origCmdKey = new NamespacedKey((Plugin)this.plugin, "smp_original_cmd");
        NamespacedKey origNameKey = new NamespacedKey((Plugin)this.plugin, "smp_original_name");
        NamespacedKey origLoreKey = new NamespacedKey((Plugin)this.plugin, "smp_original_lore");
        if (meta.hasCustomModelData()) {
            meta.getPersistentDataContainer().set(origCmdKey, PersistentDataType.INTEGER, meta.getCustomModelData());
        }
        if (meta.hasDisplayName()) {
            meta.getPersistentDataContainer().set(origNameKey, PersistentDataType.STRING, meta.getDisplayName());
        }
        if (meta.hasLore()) {
            meta.getPersistentDataContainer().set(origLoreKey, PersistentDataType.STRING, String.join((CharSequence)"|||", meta.getLore()));
        }
        ArrayList<String> newLore = new ArrayList<String>();
        String prefix = MessageUtil.parse(this.plugin.getConfigManager().getBindLorePrefix());
        newLore.add(prefix);
        String skinLore = this.getSkinLore(this.selectedModelData);
        if (skinLore != null) {
            newLore.add("");
            for (String line : skinLore.split("\n")) {
                newLore.add(MessageUtil.parse(line));
            }
        }
        newLore.add("");
        newLore.add(MessageUtil.parse("&8Soulbound to: &7" + this.player.getName()));
        if (meta.hasLore()) {
            newLore.add("");
            newLore.addAll(meta.getLore());
        }
        meta.setLore(newLore);
        target.setItemMeta(meta);
        ItemStack finalItem = target.clone();
        this.inventory.setItem(13, null);
        this.player.closeInventory();
        Map<Integer, ItemStack> leftover = this.player.getInventory().addItem(new ItemStack[]{finalItem});
        if (!leftover.isEmpty()) {
            for (ItemStack drop : leftover.values()) {
                this.player.getWorld().dropItemNaturally(this.player.getLocation(), drop);
            }
        }
        String displayName = this.plugin.getConfigManager().shouldRenameOnBind(playerClass) ? ((skinName = this.getSkinDisplayName(this.selectedModelData)) != null ? skinName : this.plugin.getConfigManager().getWeaponCosmeticName(playerClass)) : (meta.hasDisplayName() ? meta.getDisplayName() : playerClass.getDisplayName());
        final String originalName = displayName;
        final int finalSelectedModelData = this.selectedModelData;
        new BukkitRunnable(){
            int step = 0;
            final String[] colors = new String[]{"&d", "&5", "&b", "&a", "&e", "&6", "&c", "&4"};
            final String particle1;
            final String particle2;
            final String tickSound;
            final String finalSound;
            final String finalParticle;
            {
                if (finalSelectedModelData == 13501 || finalSelectedModelData == 13520 || finalSelectedModelData == 13510 || finalSelectedModelData == 13530 || finalSelectedModelData == 13540 || finalSelectedModelData == 12360) {
                    this.particle1 = "FLAME";
                    this.particle2 = "LAVA";
                    this.tickSound = "BLOCK_FIRE_AMBIENT";
                    this.finalSound = "ENTITY_GENERIC_EXPLODE";
                    this.finalParticle = "EXPLOSION_EMITTER";
                } else if (finalSelectedModelData == 13210 || finalSelectedModelData == 13240 || finalSelectedModelData == 13250 || finalSelectedModelData == 13201 || finalSelectedModelData == 13220) {
                    this.particle1 = "SOUL_FIRE_FLAME";
                    this.particle2 = "SOUL";
                    this.tickSound = "BLOCK_SOUL_SAND_BREAK";
                    this.finalSound = "ENTITY_WITHER_SPAWN";
                    this.finalParticle = "SCULK_CHARGE_POP";
                } else if (finalSelectedModelData == 13030 || finalSelectedModelData == 13050 || finalSelectedModelData == 13010 || finalSelectedModelData == 13040 || finalSelectedModelData == 13020) {
                    this.particle1 = "PORTAL";
                    this.particle2 = "END_ROD";
                    this.tickSound = "BLOCK_AMETHYST_BLOCK_CHIME";
                    this.finalSound = "UI_TOAST_CHALLENGE_COMPLETE";
                    this.finalParticle = "DRAGON_BREATH";
                } else if (finalSelectedModelData == 13301 || finalSelectedModelData == 13310 || finalSelectedModelData == 13320 || finalSelectedModelData == 13330 || finalSelectedModelData == 13340 || finalSelectedModelData == 13350) {
                    this.particle1 = "GLOW";
                    this.particle2 = "HAPPY_VILLAGER";
                    this.tickSound = "BLOCK_CHERRY_SAPLING_PLACE";
                    this.finalSound = "ENTITY_PLAYER_LEVELUP";
                    this.finalParticle = "INSTANT_EFFECT";
                } else {
                    this.particle1 = "WITCH";
                    this.particle2 = "ENCHANTED_HIT";
                    this.tickSound = "BLOCK_NOTE_BLOCK_CHIME";
                    this.finalSound = "ENTITY_PLAYER_LEVELUP";
                    this.finalParticle = "INSTANT_EFFECT";
                }
            }

            public void run() {
                ItemStack offHand;
                String id;
                if (!BindGUI.this.player.isOnline()) {
                    this.cancel();
                    return;
                }
                ItemStack targetItem = null;
                boolean isGroundItem = false;
                Item groundItemEntity = null;
                for (ItemStack invItem : BindGUI.this.player.getInventory().getContents()) {
                    if (invItem == null || !invItem.hasItemMeta()) continue;
                    id = (String)invItem.getItemMeta().getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
                    if (!boundWeaponId.toString().equals(id)) continue;
                    targetItem = invItem;
                    break;
                }
                if (targetItem == null) {
                    for (ItemStack invItem : BindGUI.this.player.getInventory().getArmorContents()) {
                        if (invItem == null || !invItem.hasItemMeta()) continue;
                        id = (String)invItem.getItemMeta().getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
                        if (!boundWeaponId.toString().equals(id)) continue;
                        targetItem = invItem;
                        break;
                    }
                }
                if (targetItem == null && (offHand = BindGUI.this.player.getInventory().getItemInOffHand()) != null && offHand.hasItemMeta()) {
                    String id2 = (String)offHand.getItemMeta().getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
                    if (boundWeaponId.toString().equals(id2)) {
                        targetItem = offHand;
                    }
                }
                if (targetItem == null) {
                    for (Entity entity : BindGUI.this.player.getNearbyEntities(6.0, 6.0, 6.0)) {
                        Item gi;
                        ItemStack stack;
                        if (!(entity instanceof Item) || (stack = (gi = (Item)entity).getItemStack()) == null || !stack.hasItemMeta()) continue;
                        id = (String)stack.getItemMeta().getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
                        if (!boundWeaponId.toString().equals(id)) continue;
                        targetItem = stack;
                        isGroundItem = true;
                        groundItemEntity = gi;
                        break;
                    }
                }
                if (targetItem == null) {
                    this.cancel();
                    return;
                }
                ItemMeta currentMeta = targetItem.getItemMeta();
                if (currentMeta == null) {
                    return;
                }
                Location loc = BindGUI.this.player.getLocation();
                if (this.step >= 8) {
                    currentMeta.setDisplayName(MessageUtil.parse("&d&l\u26a1 &5&l" + ChatColor.stripColor((String)originalName) + " &d&l\u26a1"));
                    if (finalSelectedModelData > 0) {
                        currentMeta.setCustomModelData(Integer.valueOf(finalSelectedModelData));
                    }
                    targetItem.setItemMeta(currentMeta);
                    if (isGroundItem && groundItemEntity != null) {
                        groundItemEntity.setItemStack(targetItem);
                    } else {
                        BindGUI.this.player.updateInventory();
                    }
                    BindGUI.this.player.sendMessage(MessageUtil.parse("&d&l\u26a1 &f&lYOUR WEAPON HAS BEEN BOUND TO YOU! &d&l\u26a1"));
                    CompatUtil.playSound(BindGUI.this.player.getWorld(), loc, this.finalSound, 1.2f, 1.0f);
                    for (int i = 0; i < 360; i += 15) {
                        double angle = Math.toRadians(i);
                        double x = 1.2 * Math.cos(angle);
                        double z = 1.2 * Math.sin(angle);
                        CompatUtil.spawnParticle(BindGUI.this.player.getWorld(), this.particle1, loc.clone().add(x, 1.0, z), 2, 0.1, 0.1, 0.1, 0.02);
                        if (this.finalParticle == null) continue;
                        CompatUtil.spawnParticle(BindGUI.this.player.getWorld(), this.finalParticle, loc.clone().add(x, 0.5, z), 2, 0.1, 0.1, 0.1, 0.05);
                    }
                    this.cancel();
                } else {
                    currentMeta.setDisplayName(MessageUtil.parse(this.colors[this.step % this.colors.length] + "\u26a1 " + ChatColor.stripColor((String)originalName) + " \u26a1"));
                    targetItem.setItemMeta(currentMeta);
                    if (isGroundItem && groundItemEntity != null) {
                        groundItemEntity.setItemStack(targetItem);
                    } else {
                        BindGUI.this.player.updateInventory();
                    }
                    CompatUtil.playSound(BindGUI.this.player.getWorld(), loc, this.tickSound, 0.8f, 0.8f + (float)this.step * 0.1f);
                    double y = (double)this.step * 0.25;
                    double radius = 0.8;
                    for (int i = 0; i < 360; i += 45) {
                        double angle = Math.toRadians(i + this.step * 15);
                        double x = radius * Math.cos(angle);
                        double z = radius * Math.sin(angle);
                        CompatUtil.spawnParticle(BindGUI.this.player.getWorld(), this.particle1, loc.clone().add(x, y, z), 1, 0.0, 0.0, 0.0, 0.0);
                        if (this.step % 2 != 0) continue;
                        CompatUtil.spawnParticle(BindGUI.this.player.getWorld(), this.particle2, loc.clone().add(x * 0.8, y, z * 0.8), 1, 0.0, 0.0, 0.0, 0.0);
                    }
                    ++this.step;
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 3L);
    }

    private void handleUnbind() {
        ItemStack target = this.inventory.getItem(13);
        if (target == null || target.getType().isAir()) {
            return;
        }
        ItemMeta meta = target.getItemMeta();
        if (meta == null) {
            return;
        }
        NamespacedKey boundKey = new NamespacedKey((Plugin)this.plugin, "bound_player_uuid");
        NamespacedKey itemIdKey = new NamespacedKey((Plugin)this.plugin, "bound_item_id");
        if (!meta.getPersistentDataContainer().has(boundKey, PersistentDataType.STRING)) {
            return;
        }
        String ownerUuidStr = (String)meta.getPersistentDataContainer().get(boundKey, PersistentDataType.STRING);
        if (!this.player.getUniqueId().toString().equals(ownerUuidStr)) {
            return;
        }
        this.plugin.getDataManager().setBoundWeaponId(this.player.getUniqueId(), null);
        this.plugin.getDataManager().setHasBoundWeapon(this.player.getUniqueId(), false);
        meta.getPersistentDataContainer().remove(boundKey);
        meta.getPersistentDataContainer().remove(itemIdKey);
        NamespacedKey origCmdKey = new NamespacedKey((Plugin)this.plugin, "smp_original_cmd");
        NamespacedKey origNameKey = new NamespacedKey((Plugin)this.plugin, "smp_original_name");
        if (meta.getPersistentDataContainer().has(origCmdKey, PersistentDataType.INTEGER)) {
            int origCmd = (Integer)meta.getPersistentDataContainer().get(origCmdKey, PersistentDataType.INTEGER);
            meta.setCustomModelData(Integer.valueOf(origCmd));
            meta.getPersistentDataContainer().remove(origCmdKey);
        } else {
            meta.setCustomModelData(null);
        }
        NamespacedKey origLoreKey = new NamespacedKey((Plugin)this.plugin, "smp_original_lore");
        if (meta.getPersistentDataContainer().has(origLoreKey, PersistentDataType.STRING)) {
            String origLoreRaw = (String)meta.getPersistentDataContainer().get(origLoreKey, PersistentDataType.STRING);
            meta.setLore(Arrays.asList(origLoreRaw.split("\\|\\|\\|")));
            meta.getPersistentDataContainer().remove(origLoreKey);
        } else {
            meta.setLore(null);
        }
        if (meta.getPersistentDataContainer().has(origNameKey, PersistentDataType.STRING)) {
            String origName = (String)meta.getPersistentDataContainer().get(origNameKey, PersistentDataType.STRING);
            meta.setDisplayName(origName);
            meta.getPersistentDataContainer().remove(origNameKey);
        } else if (meta.hasDisplayName()) {
            String strippedName = ChatColor.stripColor((String)meta.getDisplayName());
            strippedName = strippedName.replace("\u26a1", "").trim();
            meta.setDisplayName(MessageUtil.color("&f" + strippedName));
        }
        target.setItemMeta(meta);
        ItemStack finalItem = target.clone();
        this.inventory.setItem(13, null);
        this.player.closeInventory();
        Map<Integer, ItemStack> leftover = this.player.getInventory().addItem(new ItemStack[]{finalItem});
        if (!leftover.isEmpty()) {
            for (ItemStack drop : leftover.values()) {
                this.player.getWorld().dropItemNaturally(this.player.getLocation(), drop);
            }
        }
        this.player.sendMessage(MessageUtil.parse("&aSuccessfully unbound your weapon."));
        this.player.playSound(this.player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().equals((Object)this.inventory)) {
            HandlerList.unregisterAll((Listener)this);
            ItemStack leftoverItem = this.inventory.getItem(13);
            if (leftoverItem != null && !leftoverItem.getType().isAir()) {
                this.inventory.setItem(13, null);
                Map<Integer, ItemStack> remaining = this.player.getInventory().addItem(new ItemStack[]{leftoverItem});
                if (!remaining.isEmpty()) {
                    for (ItemStack item : remaining.values()) {
                        this.player.getWorld().dropItemNaturally(this.player.getLocation(), item);
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

