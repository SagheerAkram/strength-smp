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
import com.floki.strengthsmp.util.ItemFactory;
import com.floki.strengthsmp.util.MessageUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
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

public class BindGUI implements Listener {

    public static class WeaponSkin {
        private final int cmd;
        private final String name;
        private final String description;

        public WeaponSkin(int cmd, String name, String description) {
            this.cmd = cmd;
            this.name = name;
            this.description = description;
        }

        public int getCmd() {
            return this.cmd;
        }

        public String getName() {
            return this.name;
        }

        public String getDescription() {
            return this.description;
        }
    }

    private final StrengthSMP plugin;
    private final Player player;
    private final Inventory mainInventory;
    private final Inventory selectInventory;
    private ItemStack weaponInSlot;
    private int selectedModelData;
    private boolean isSwitchingView = false;

    public BindGUI(StrengthSMP plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.mainInventory = Bukkit.createInventory(null, 27, MessageUtil.get("gui.bind-title", "&d&l⚡ Weapon Binding ⚡"));
        this.selectInventory = Bukkit.createInventory(null, 27, MessageUtil.get("gui.bind-select-title", "&d&l⚡ Choose Weapon Model ⚡"));
        
        WeaponType playerClass = plugin.getDataManager().getWeaponType(player.getUniqueId());
        this.selectedModelData = this.getDefaultSkinForClass(playerClass);
        
        this.setupMainBorders();
        this.updateSlots();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static List<WeaponSkin> getSkinsForClass(WeaponType type) {
        List<WeaponSkin> list = new ArrayList<WeaponSkin>();
        if (type == null) {
            return list;
        }
        switch (type) {
            case SWORD: {
                list.add(new WeaponSkin(12348, "&b&lDuelist Katana", "&7Razor-sharp katana of the swift duelist."));
                list.add(new WeaponSkin(13001, "&b&lCrescent Moonblade", "&7Luminous celestial blade infused with moonlight."));
                list.add(new WeaponSkin(13501, "&c&lHellfire Blade", "&7Blazing infernal blade forged in the depths of the Nether."));
                break;
            }
            case AXE: {
                list.add(new WeaponSkin(13010, "&b&lLunar Cleaver", "&7Massive glowing crescent battleaxe of starlight."));
                list.add(new WeaponSkin(13410, "&6&lDragon Waraxe", "&7Ancient dragonbone greataxe that shatters defenses."));
                list.add(new WeaponSkin(13510, "&c&lDemon's Hatchet", "&7Smoldering infernal hatchet that rends armor."));
                break;
            }
            case BOW: {
                list.add(new WeaponSkin(12350, "&e&lRecurve Bow", "&7A finely crafted recurve bow with smooth tension."));
                list.add(new WeaponSkin(13030, "&b&lLunar Longbow", "&7Radiant recurve bow that channels starry constellations."));
                list.add(new WeaponSkin(13530, "&c&lHellfire Bow", "&7Volcanic bow unleashing devastating searing barrages."));
                break;
            }
            case CROSSBOW: {
                list.add(new WeaponSkin(13040, "&b&lLunar Arbalest", "&7Celestial heavy arbalest firing star-infused bolts."));
                list.add(new WeaponSkin(13440, "&6&lDragon Ballista", "&7Devastating siege ballista carved from dragon bone."));
                list.add(new WeaponSkin(13540, "&c&lHellfire Launcher", "&7Launches searing bolts of explosive hellfire."));
                break;
            }
            case TRIDENT: {
                list.add(new WeaponSkin(12356, "&a&lNetherite Scythe", "&7Lethal curved executioner scythe of harvesting."));
                list.add(new WeaponSkin(13050, "&b&lLunar Trident", "&7Forged from stellar iron to harness the cosmic tides."));
                list.add(new WeaponSkin(13550, "&c&lInfernal Trident", "&7Forged in magma to command waves of nether flame."));
                break;
            }
            case SHIELD: {
                list.add(new WeaponSkin(13020, "&b&lLunar Aegis", "&7Celestial barrier protecting against all mortal assault."));
                list.add(new WeaponSkin(13420, "&6&lDragon Scale Bulwark", "&7Impenetrable shield forged from ancient dragon scales."));
                list.add(new WeaponSkin(13520, "&c&lDemon's Bulwark", "&7Protective aegis forged in active magma vents."));
                break;
            }
        }
        return list;
    }

    private int getDefaultSkinForClass(WeaponType type) {
        List<WeaponSkin> skins = getSkinsForClass(type);
        if (!skins.isEmpty()) {
            return skins.get(0).getCmd();
        }
        return 0;
    }

    private void setupMainBorders() {
        ItemStack border = this.createItem(Material.PURPLE_STAINED_GLASS_PANE, "&8 ");
        for (int i = 0; i < 27; ++i) {
            if (i == 2 || i == 3 || i == 4 || i == 5 || i == 6 || i == 11 || i == 13 || i == 15 || i == 22) continue;
            this.mainInventory.setItem(i, border);
        }
        ItemStack infoGuide = this.createItem(Material.ENDER_EYE, MessageUtil.get("gui.bind-guide-title", "&d&l⚡ How It Works ⚡"));
        ItemMeta meta = infoGuide.getItemMeta();
        if (meta != null) {
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(MessageUtil.get("gui.bind-guide-step1", "&71. Place your class weapon in &eSlot 14&7."));
            lore.add(MessageUtil.get("gui.bind-guide-step2", "&72. Click &d&lBind Weapon &7to view &e3 custom models&7."));
            lore.add(MessageUtil.get("gui.bind-guide-step3", "&73. Select your favorite model and click &a&lConfirm&7!"));
            meta.setLore(lore);
            infoGuide.setItemMeta(meta);
        }
        this.mainInventory.setItem(22, infoGuide);
    }

    private void updateSlots() {
        this.setupBindItem();
        this.setupStatusItem();
        this.setupUnbindItem();
        this.setupStatsBooks();
    }

    private void setupStatusItem() {
        ItemStack statusItem;
        ItemMeta meta;
        WeaponType playerClass = this.plugin.getDataManager().getWeaponType(this.player.getUniqueId());
        String classStr = playerClass != null ? playerClass.getDisplayName() : "&cNone";
        String boundStatus = "&cInactive";
        String currentBoundId = this.plugin.getDataManager().getBoundWeaponId(this.player.getUniqueId());
        if (currentBoundId != null) {
            boundStatus = "&aActive &7(" + currentBoundId.substring(0, Math.min(8, currentBoundId.length())) + "...)";
        }
        if ((meta = (statusItem = new ItemStack(Material.NETHER_STAR)).getItemMeta()) != null) {
            meta.setDisplayName(MessageUtil.get("gui.bind-status-title", "&b&lBinding Status & Perks"));
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(MessageUtil.get("gui.bind-status-class", "&7Your Class: %class%", "class", classStr));
            lore.add(MessageUtil.get("gui.bind-status-bound", "&7Bound Weapon: %status%", "status", boundStatus));
            lore.add("");
            lore.add(MessageUtil.get("gui.bind-status-perks", "&eBinding Perks:"));
            lore.add(MessageUtil.get("gui.bind-status-perk-death", "&d- Keep on Death: &7Bound weapon is never dropped on death"));
            lore.add(MessageUtil.get("gui.bind-status-perk-loyalty", "&d- Loyalty Flight: &7Flies back to you on respawn, knocking back enemies"));
            lore.add(MessageUtil.get("gui.bind-status-perk-soulbound", "&d- Soulbound Aura: &7Emits custom class magic particles while wielded"));
            meta.setLore(lore);
            statusItem.setItemMeta(meta);
        }
        this.mainInventory.setItem(4, statusItem);
    }

    private void setupBindItem() {
        ItemStack bindItem;
        ItemMeta meta;
        ItemStack target = this.mainInventory.getItem(13);
        WeaponType playerClass = this.plugin.getDataManager().getWeaponType(this.player.getUniqueId());
        boolean canAction = false;
        boolean isChangeSkin = false;
        String statusMessage = "&cPlease place a weapon in Slot 14.";
        int cost = this.plugin.getConfigManager().getBindCostStrength();
        
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
                        String ownerUuidStr = (String)targetMeta.getPersistentDataContainer().get(boundKey, PersistentDataType.STRING);
                        if (this.player.getUniqueId().toString().equals(ownerUuidStr)) {
                            isChangeSkin = true;
                            canAction = true;
                            cost = 1;
                            statusMessage = "&aClick to choose a new model!";
                        } else {
                            statusMessage = "&cBound to a different player!";
                        }
                    } else {
                        cost = this.plugin.getConfigManager().getBindCostStrength();
                        canAction = true;
                        statusMessage = "&aReady to choose your model!";
                    }
                }
            }
        }
        
        Material iconMat = canAction ? (isChangeSkin ? Material.NETHER_STAR : Material.ANVIL) : Material.BARRIER;
        if ((meta = (bindItem = new ItemStack(iconMat)).getItemMeta()) != null) {
            ArrayList<String> lore = new ArrayList<String>();
            if (isChangeSkin) {
                meta.setDisplayName(MessageUtil.color(canAction ? "&a&l✨ Change Model (1 Strength) ✨" : "&c&l✨ Cannot Change Model"));
                lore.add(MessageUtil.color("&7Change the 3D model of your bound weapon."));
                lore.add("");
                lore.add(MessageUtil.color("&7Cost: &e1 Strength"));
                lore.add(MessageUtil.color("&7Status: " + statusMessage));
                if (canAction) {
                    lore.add("");
                    lore.add(MessageUtil.color("&eClick to choose from 3 models!"));
                }
            } else {
                meta.setDisplayName(MessageUtil.color(canAction ? "&d&l⚡ Bind Weapon ⚡" : "&c&l⚡ Place Weapon to Bind"));
                lore.add(MessageUtil.color("&7Bind your class weapon to your soul."));
                lore.add("");
                lore.add(MessageUtil.color("&7Cost: &e" + cost + " Strength"));
                lore.add(MessageUtil.color("&7Status: " + statusMessage));
                if (canAction) {
                    lore.add("");
                    lore.add(MessageUtil.color("&eClick to choose from 3 models!"));
                }
            }
            meta.setLore(lore);
            bindItem.setItemMeta(meta);
        }
        this.mainInventory.setItem(11, bindItem);
    }

    private void setupUnbindItem() {
        ItemStack unbindItem;
        ItemMeta meta;
        ItemMeta targetMeta;
        ItemStack target = this.mainInventory.getItem(13);
        boolean canUnbind = false;
        String statusMessage = "&cPlace your bound weapon in Slot 14.";
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
            meta.setDisplayName(MessageUtil.color(canUnbind ? "&c&l❌ Confirm Unbind" : "&c&l❌ Cannot Unbind"));
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(MessageUtil.color("&7Removes the soul binding from the weapon in Slot 14."));
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
        this.mainInventory.setItem(15, unbindItem);
    }

    private void setupStatsBooks() {
        UUID uuid = this.player.getUniqueId();
        boolean hasActive = this.plugin.getDataManager().hasBoundWeapon(uuid);
        
        ItemStack killsBook = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta kMeta = killsBook.getItemMeta();
        if (kMeta != null) {
            kMeta.setDisplayName(MessageUtil.color("&d&l⚡ Souls Harvested"));
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
                    lore.add(MessageUtil.color("&d- " + entry.getKey() + ": &f" + entry.getValue() + "x"));
                    ++limit;
                }
            }
            kMeta.setLore(lore);
            killsBook.setItemMeta(kMeta);
        }
        this.mainInventory.setItem(2, killsBook);

        ItemStack dmgBook = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta dMeta = dmgBook.getItemMeta();
        if (dMeta != null) {
            dMeta.setDisplayName(MessageUtil.color("&d&l💥 Damage Dealt"));
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(MessageUtil.color("&7Tracks final damage dealt with your bound weapon."));
            lore.add("");
            double damage = this.plugin.getDataManager().getBoundStatDouble(uuid, "damage_dealt");
            lore.add(MessageUtil.color("&7Total Damage: &f&l" + String.format("%.1f", damage)));
            dMeta.setLore(lore);
            dmgBook.setItemMeta(dMeta);
        }
        this.mainInventory.setItem(3, dmgBook);

        ItemStack timeBook = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta tMeta = timeBook.getItemMeta();
        if (tMeta != null) {
            tMeta.setDisplayName(MessageUtil.color("&d&l⌛ Duration Bound"));
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
        this.mainInventory.setItem(5, timeBook);

        ItemStack abilityBook = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta aMeta = abilityBook.getItemMeta();
        if (aMeta != null) {
            aMeta.setDisplayName(MessageUtil.color("&d&l✨ Ability Triggers"));
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(MessageUtil.color("&7Tracks weapon ability uses while bound."));
            lore.add("");
            int uses = this.plugin.getDataManager().getBoundStat(uuid, "ability_uses");
            lore.add(MessageUtil.color("&7Total Uses: &f&l" + uses));
            aMeta.setLore(lore);
            abilityBook.setItemMeta(aMeta);
        }
        this.mainInventory.setItem(6, abilityBook);
    }

    private void openModelSelection() {
        WeaponType playerClass = this.plugin.getDataManager().getWeaponType(this.player.getUniqueId());
        if (playerClass == null || this.weaponInSlot == null) {
            return;
        }

        ItemStack border = this.createItem(Material.MAGENTA_STAINED_GLASS_PANE, "&8 ");
        for (int i = 0; i < 27; ++i) {
            this.selectInventory.setItem(i, border);
        }

        // Header showing weapon being bound
        ItemStack previewItem = this.weaponInSlot.clone();
        ItemMeta pMeta = previewItem.getItemMeta();
        if (pMeta != null) {
            pMeta.setDisplayName(MessageUtil.get("gui.bind-target-preview", "&e&lBinding Target: &f%item%", "item", this.weaponInSlot.getType().name()));
            ArrayList<String> pLore = new ArrayList<String>();
            pLore.add(MessageUtil.get("gui.bind-target-preview-lore", "&7Select 1 of the 3 exclusive 3D models below."));
            pMeta.setLore(pLore);
            previewItem.setItemMeta(pMeta);
        }
        this.selectInventory.setItem(4, previewItem);

        // Populate 3 models
        this.renderModelOptions();

        // Back button (Slot 18)
        ItemStack backBtn = this.createItem(Material.ARROW, MessageUtil.get("gui.bind-back-btn-title", "&c&l⬅ Back to Weapon Slot"));
        ItemMeta bMeta = backBtn.getItemMeta();
        if (bMeta != null) {
            bMeta.setLore(Arrays.asList(MessageUtil.get("gui.bind-back-btn-lore", "&7Return to place or take back your weapon.")));
            backBtn.setItemMeta(bMeta);
        }
        this.selectInventory.setItem(18, backBtn);

        // Confirm button (Slot 22)
        this.renderConfirmButton();

        this.isSwitchingView = true;
        this.player.openInventory(this.selectInventory);
        this.isSwitchingView = false;
        CompatUtil.playSound(this.player, this.player.getLocation(), "UI_BUTTON_CLICK", 1.0f, 1.2f);
    }

    private void renderModelOptions() {
        WeaponType playerClass = this.plugin.getDataManager().getWeaponType(this.player.getUniqueId());
        List<WeaponSkin> skins = getSkinsForClass(playerClass);
        int[] slots = new int[]{11, 13, 15};
        Material displayMat = this.weaponInSlot != null ? this.weaponInSlot.getType() : Material.NETHERITE_SWORD;

        for (int i = 0; i < slots.length; ++i) {
            if (i >= skins.size()) break;
            WeaponSkin skin = skins.get(i);
            int slot = slots[i];
            
            ItemStack item = new ItemStack(displayMat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(MessageUtil.color(skin.getName()));
                meta.setCustomModelData(Integer.valueOf(skin.getCmd()));
                NamespacedKey skinCmdKey = new NamespacedKey((Plugin)this.plugin, "gui_skin_cmd");
                meta.getPersistentDataContainer().set(skinCmdKey, PersistentDataType.INTEGER, skin.getCmd());

                ArrayList<String> lore = new ArrayList<String>();
                lore.add(MessageUtil.color(skin.getDescription()));
                lore.add("");
                if (this.selectedModelData == skin.getCmd()) {
                    lore.add(MessageUtil.get("gui.bind-model-selected", "&7Status: &a&lSELECTED"));
                    lore.add(MessageUtil.get("gui.bind-model-selected-lore", "&eThis model will be applied upon confirmation."));
                    meta.addEnchant(CompatUtil.resolveEnchantment("UNBREAKING", "DURABILITY"), 1, true);
                    meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES});
                } else {
                    lore.add(MessageUtil.get("gui.bind-model-not-selected", "&7Status: &cNot Selected"));
                    lore.add("");
                    lore.add(MessageUtil.get("gui.bind-model-click-to-select", "&eClick to select this model!"));
                    meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
                }
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            this.selectInventory.setItem(slot, item);
        }
    }

    private void renderConfirmButton() {
        boolean isChangeSkin = false;
        if (this.weaponInSlot != null && this.weaponInSlot.hasItemMeta()) {
            NamespacedKey boundKey = new NamespacedKey((Plugin)this.plugin, "bound_player_uuid");
            if (this.weaponInSlot.getItemMeta().getPersistentDataContainer().has(boundKey, PersistentDataType.STRING)) {
                isChangeSkin = true;
            }
        }
        int cost = isChangeSkin ? 1 : this.plugin.getConfigManager().getBindCostStrength();
        int currentStrength = this.plugin.getDataManager().getStrength(this.player.getUniqueId());
        boolean hasEnough = currentStrength >= cost;

        String selectedName = this.getSkinDisplayName(this.selectedModelData);
        ItemStack confirmBtn = new ItemStack(hasEnough ? Material.EMERALD : Material.BARRIER);
        ItemMeta meta = confirmBtn.getItemMeta();
        if (meta != null) {
            String confirmTitle;
            if (!hasEnough) {
                confirmTitle = MessageUtil.get("gui.bind-confirm-btn-not-enough", "&c&l❌ Not Enough Strength");
            } else if (isChangeSkin) {
                confirmTitle = MessageUtil.get("gui.bind-confirm-btn-change", "&a&l⚡ Confirm Model Change ⚡");
            } else {
                confirmTitle = MessageUtil.get("gui.bind-confirm-btn-bind", "&a&l⚡ Confirm Soul Binding ⚡");
            }
            meta.setDisplayName(confirmTitle);
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(MessageUtil.get("gui.bind-confirm-selected", "&7Selected Model: &b%model%", "model", (selectedName != null ? selectedName : "Default")));
            String costStr = MessageUtil.get("gui.bind-confirm-cost-str", "%cost% Strength", "cost", String.valueOf(cost));
            lore.add(MessageUtil.get("gui.bind-confirm-cost-line", "&7Binding Cost: &e%cost%", "cost", costStr));
            lore.add(MessageUtil.get("gui.bind-confirm-strength-line", "&7Your Strength: &f%strength%", "strength", String.valueOf(currentStrength)));
            lore.add("");
            if (hasEnough) {
                lore.add(MessageUtil.get("gui.bind-confirm-click-action", "&eClick to bind your weapon with this model!"));
            } else {
                lore.add(MessageUtil.get("gui.bind-confirm-need-more", "&cYou need at least %cost% Strength to bind!", "cost", String.valueOf(cost)));
            }
            meta.setLore(lore);
            confirmBtn.setItemMeta(meta);
        }
        this.selectInventory.setItem(22, confirmBtn);
    }

    private String getSkinDisplayName(int customModelData) {
        WeaponType playerClass = this.plugin.getDataManager().getWeaponType(this.player.getUniqueId());
        for (WeaponSkin skin : getSkinsForClass(playerClass)) {
            if (skin.getCmd() == customModelData) {
                return skin.getName();
            }
        }
        switch (customModelData) {
            case 12348: return "&b&lDuelist Katana";
            case 13001: return "&b&lCrescent Moonblade";
            case 13501: return "&c&lHellfire Blade";
            case 12361: return "&b&lFrost Fang";

            case 13010: return "&b&lLunar Cleaver";
            case 13410: return "&6&lDragon Waraxe";
            case 13510: return "&c&lDemon's Hatchet";
            case 12349: return "&c&lHeirloom Axe";
            case 12363: return "&2&lWarden's Cleaver";

            case 12350: return "&e&lRecurve Bow";
            case 13030: return "&b&lLunar Longbow";
            case 13530: return "&c&lHellfire Bow";
            case 12369: return "&6&lPhoenix Wing";
            case 12370: return "&8&lShadow Stalker";

            case 13040: return "&b&lLunar Arbalest";
            case 13440: return "&6&lDragon Ballista";
            case 13540: return "&c&lHellfire Launcher";
            case 12351: return "&6&lHeavy Crossbow";
            case 12374: return "&6&lRadiant Arbalest";

            case 12356: return "&a&lNetherite Scythe";
            case 13050: return "&b&lLunar Trident";
            case 13550: return "&c&lInfernal Trident";
            case 12352: return "&3&lRoyal Spear";
            case 12375: return "&b&lPoseidon's Wrath";

            case 13020: return "&b&lLunar Aegis";
            case 13420: return "&6&lDragon Scale Bulwark";
            case 13520: return "&c&lDemon's Bulwark";
            case 12353: return "&f&lBulwark Shield";
            case 12367: return "&7&lSentinel's Wall";
        }
        return "&d&lLegendary Weapon";
    }

    private String getSkinLore(int customModelData) {
        WeaponType playerClass = this.plugin.getDataManager().getWeaponType(this.player.getUniqueId());
        for (WeaponSkin skin : getSkinsForClass(playerClass)) {
            if (skin.getCmd() == customModelData) {
                return skin.getDescription();
            }
        }
        return "&7A legendary soulbound weapon of immense power.";
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
        this.player.openInventory(this.mainInventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getWhoClicked().equals(this.player)) {
            return;
        }

        Inventory topInv = event.getView().getTopInventory();
        if (!topInv.equals(this.mainInventory) && !topInv.equals(this.selectInventory)) {
            return;
        }

        // Click in player inventory while in main GUI
        if (topInv.equals(this.mainInventory)) {
            if (event.getClickedInventory() != null && event.getClickedInventory().equals(this.player.getInventory())) {
                this.refreshMainGUI();
                return;
            }
            int slot = event.getRawSlot();
            if (slot == 13) {
                this.refreshMainGUI();
                return;
            }
            event.setCancelled(true);

            if (slot == 11) {
                ItemStack target = this.mainInventory.getItem(13);
                WeaponType playerClass = this.plugin.getDataManager().getWeaponType(this.player.getUniqueId());
                if (target == null || target.getType().isAir()) {
                    MessageUtil.sendWithDefault(this.player, "gui.place-weapon-first", "&cPlease place your class weapon in Slot 14 first!");
                    this.player.playSound(this.player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }
                if (playerClass == null || !playerClass.isValidMaterial(target.getType())) {
                    MessageUtil.sendWithDefault(this.player, "gui.invalid-class-slot", "&cThis weapon does not match your weapon class!");
                    this.player.playSound(this.player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }
                ItemMeta meta = target.getItemMeta();
                if (meta != null) {
                    NamespacedKey boundKey = new NamespacedKey((Plugin)this.plugin, "bound_player_uuid");
                    if (meta.getPersistentDataContainer().has(boundKey, PersistentDataType.STRING)) {
                        String ownerUuid = (String)meta.getPersistentDataContainer().get(boundKey, PersistentDataType.STRING);
                        if (!this.player.getUniqueId().toString().equals(ownerUuid)) {
                            MessageUtil.sendWithDefault(this.player, "gui.not-your-weapon", "&cThis weapon is bound to another player!");
                            this.player.playSound(this.player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                            return;
                        }
                    }
                }
                // Transition to Model Selection screen
                this.weaponInSlot = target.clone();
                this.mainInventory.setItem(13, null);
                if (meta != null && meta.hasCustomModelData()) {
                    this.selectedModelData = meta.getCustomModelData();
                } else {
                    this.selectedModelData = this.getDefaultSkinForClass(playerClass);
                }
                this.openModelSelection();
            } else if (slot == 15) {
                this.handleUnbind();
            }
        } else if (topInv.equals(this.selectInventory)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();

            // Select Model (Slots 11, 13, 15)
            if (slot == 11 || slot == 13 || slot == 15) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null && clickedItem.hasItemMeta()) {
                    NamespacedKey skinCmdKey = new NamespacedKey((Plugin)this.plugin, "gui_skin_cmd");
                    if (clickedItem.getItemMeta().getPersistentDataContainer().has(skinCmdKey, PersistentDataType.INTEGER)) {
                        this.selectedModelData = ((Integer)clickedItem.getItemMeta().getPersistentDataContainer().get(skinCmdKey, PersistentDataType.INTEGER)).intValue();
                        CompatUtil.playSound(this.player, this.player.getLocation(), "UI_BUTTON_CLICK", 1.0f, 1.4f);
                        this.renderModelOptions();
                        this.renderConfirmButton();
                    }
                }
            } else if (slot == 18) {
                // Back button
                this.isSwitchingView = true;
                this.mainInventory.setItem(13, this.weaponInSlot);
                this.weaponInSlot = null;
                this.updateSlots();
                this.player.openInventory(this.mainInventory);
                this.isSwitchingView = false;
                CompatUtil.playSound(this.player, this.player.getLocation(), "UI_BUTTON_CLICK", 1.0f, 0.9f);
            } else if (slot == 22) {
                // Confirm Bind button
                this.handleConfirmBind();
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getWhoClicked().equals(this.player)) {
            Inventory top = event.getView().getTopInventory();
            if (top.equals(this.mainInventory) || top.equals(this.selectInventory)) {
                if (top.equals(this.selectInventory)) {
                    event.setCancelled(true);
                } else if (event.getRawSlots().stream().anyMatch(s -> s != 13 && s < 27)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private void refreshMainGUI() {
        Bukkit.getScheduler().runTask((Plugin)this.plugin, this::updateSlots);
    }

    private void handleConfirmBind() {
        if (this.weaponInSlot == null || this.weaponInSlot.getType().isAir()) {
            return;
        }
        WeaponType playerClass = this.plugin.getDataManager().getWeaponType(this.player.getUniqueId());
        if (playerClass == null || !playerClass.isValidMaterial(this.weaponInSlot.getType())) {
            return;
        }

        ItemMeta meta = this.weaponInSlot.getItemMeta();
        if (meta == null) {
            return;
        }

        NamespacedKey boundKey = new NamespacedKey((Plugin)this.plugin, "bound_player_uuid");
        final NamespacedKey itemIdKey = new NamespacedKey((Plugin)this.plugin, "bound_item_id");
        boolean isChangeSkin = meta.getPersistentDataContainer().has(boundKey, PersistentDataType.STRING);

        int cost = isChangeSkin ? 1 : this.plugin.getConfigManager().getBindCostStrength();
        int currentStrength = this.plugin.getDataManager().getStrength(this.player.getUniqueId());

        if (currentStrength < cost) {
            MessageUtil.sendWithDefault(this.player, "gui.need-more-strength", "&cYou need at least %cost% strength to bind your weapon. You have %current%.", "cost", String.valueOf(cost), "current", String.valueOf(currentStrength));
            this.player.playSound(this.player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Deduct cost
        if (cost > 0) {
            this.plugin.getDataManager().subtractStrength(this.player.getUniqueId(), cost);
            if (isChangeSkin) {
                MessageUtil.sendWithDefault(this.player, "gui.skin-changed-success", "&aSpent &e1 strength &ato change your weapon model!");
            } else {
                MessageUtil.sendWithDefault(this.player, "gui.bound-success", "&aSpent &e%cost% strength &ato bind your class weapon!", "cost", String.valueOf(cost));
            }
        }

        this.plugin.updateDisplay(this.player);
        this.plugin.getMonarchService().calculateNewMonarch();

        final UUID boundWeaponId = UUID.randomUUID();
        this.plugin.getDataManager().setBoundWeaponId(this.player.getUniqueId(), boundWeaponId.toString());
        this.plugin.getDataManager().setHasBoundWeapon(this.player.getUniqueId(), true);
        if (!isChangeSkin) {
            ItemFactory.unbindAllPlayerItems(this.player);
            this.plugin.getDataManager().setBoundTimestamp(this.player.getUniqueId(), System.currentTimeMillis());
        }

        meta.getPersistentDataContainer().set(boundKey, PersistentDataType.STRING, this.player.getUniqueId().toString());
        meta.getPersistentDataContainer().set(itemIdKey, PersistentDataType.STRING, boundWeaponId.toString());

        NamespacedKey origCmdKey = new NamespacedKey((Plugin)this.plugin, "smp_original_cmd");
        NamespacedKey origNameKey = new NamespacedKey((Plugin)this.plugin, "smp_original_name");
        NamespacedKey origLoreKey = new NamespacedKey((Plugin)this.plugin, "smp_original_lore");

        if (!isChangeSkin) {
            if (meta.hasCustomModelData()) {
                meta.getPersistentDataContainer().set(origCmdKey, PersistentDataType.INTEGER, meta.getCustomModelData());
            }
            if (meta.hasDisplayName()) {
                meta.getPersistentDataContainer().set(origNameKey, PersistentDataType.STRING, meta.getDisplayName());
            }
            if (meta.hasLore()) {
                meta.getPersistentDataContainer().set(origLoreKey, PersistentDataType.STRING, String.join("|||", meta.getLore()));
            }
        }

        int cmdToApply = this.selectedModelData > 0 ? this.selectedModelData : this.getDefaultSkinForClass(playerClass);
        meta.setCustomModelData(Integer.valueOf(cmdToApply));

        String skinName = this.getSkinDisplayName(cmdToApply);
        String finalDisplayName = skinName != null ? ChatColor.stripColor(skinName) : playerClass.getDisplayName();
        meta.setDisplayName(MessageUtil.parse("&d&l⚡ &5&l" + finalDisplayName + " &d&l⚡"));

        ArrayList<String> newLore = new ArrayList<String>();
        String prefix = MessageUtil.parse(this.plugin.getConfigManager().getBindLorePrefix());
        newLore.add(prefix);
        String skinLore = this.getSkinLore(cmdToApply);
        if (skinLore != null && !skinLore.isEmpty()) {
            newLore.add("");
            for (String line : skinLore.split("\n")) {
                newLore.add(MessageUtil.parse(line));
            }
        }
        newLore.add("");
        newLore.add(MessageUtil.parse("&8Soulbound to: &7" + this.player.getName()));
        meta.setLore(newLore);
        this.weaponInSlot.setItemMeta(meta);

        ItemStack finalItem = this.weaponInSlot.clone();
        this.weaponInSlot = null;

        // Close GUI cleanly
        this.player.closeInventory();

        // Safely add item to player inventory
        Map<Integer, ItemStack> leftover = this.player.getInventory().addItem(finalItem);
        if (!leftover.isEmpty()) {
            for (ItemStack drop : leftover.values()) {
                this.player.getWorld().dropItemNaturally(this.player.getLocation(), drop);
            }
        }

        final String broadcastName = finalDisplayName;
        final int finalModelCmd = cmdToApply;

        // Run the loyalty flight / binding particle animation
        new BukkitRunnable() {
            int step = 0;
            final String[] colors = new String[]{"&d", "&5", "&b", "&a", "&e", "&6", "&c", "&4"};
            final String particle1;
            final String particle2;
            final String tickSound;
            final String finalSound;
            final String finalParticle;

            {
                if (finalModelCmd >= 13500 && finalModelCmd <= 13599) {
                    this.particle1 = "FLAME";
                    this.particle2 = "LAVA";
                    this.tickSound = "BLOCK_FIRE_AMBIENT";
                    this.finalSound = "ENTITY_GENERIC_EXPLODE";
                    this.finalParticle = "EXPLOSION_EMITTER";
                } else if (finalModelCmd >= 13000 && finalModelCmd <= 13099) {
                    this.particle1 = "END_ROD";
                    this.particle2 = "PORTAL";
                    this.tickSound = "BLOCK_AMETHYST_BLOCK_CHIME";
                    this.finalSound = "UI_TOAST_CHALLENGE_COMPLETE";
                    this.finalParticle = "TOTEM_OF_UNDYING";
                } else if (finalModelCmd >= 13400 && finalModelCmd <= 13499) {
                    this.particle1 = "DRAGON_BREATH";
                    this.particle2 = "ENCHANTED_HIT";
                    this.tickSound = "ENTITY_ENDER_DRAGON_GROWL";
                    this.finalSound = "ENTITY_ENDER_DRAGON_DEATH";
                    this.finalParticle = "DRAGON_BREATH";
                } else if (finalModelCmd == 12361 || finalModelCmd == 12363) {
                    this.particle1 = "SOUL_FIRE_FLAME";
                    this.particle2 = "SOUL";
                    this.tickSound = "BLOCK_SOUL_SAND_BREAK";
                    this.finalSound = "ENTITY_WITHER_SPAWN";
                    this.finalParticle = "SCULK_CHARGE_POP";
                } else if (finalModelCmd == 12352 || finalModelCmd == 12375 || finalModelCmd == 12356) {
                    this.particle1 = "PORTAL";
                    this.particle2 = "END_ROD";
                    this.tickSound = "BLOCK_AMETHYST_BLOCK_CHIME";
                    this.finalSound = "UI_TOAST_CHALLENGE_COMPLETE";
                    this.finalParticle = "DRAGON_BREATH";
                } else {
                    this.particle1 = "WITCH";
                    this.particle2 = "ENCHANTED_HIT";
                    this.tickSound = "BLOCK_NOTE_BLOCK_CHIME";
                    this.finalSound = "ENTITY_PLAYER_LEVELUP";
                    this.finalParticle = "INSTANT_EFFECT";
                }
            }

            public void run() {
                if (!BindGUI.this.player.isOnline()) {
                    this.cancel();
                    return;
                }
                ItemStack targetItem = null;
                boolean isGroundItem = false;
                Item groundItemEntity = null;
                int targetSlot = -1;
                boolean isOffHand = false;

                for (int slot = 0; slot < BindGUI.this.player.getInventory().getSize(); ++slot) {
                    ItemStack invItem = BindGUI.this.player.getInventory().getItem(slot);
                    if (invItem == null || !invItem.hasItemMeta()) continue;
                    String id = (String)invItem.getItemMeta().getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
                    if (!boundWeaponId.toString().equals(id)) continue;
                    targetItem = invItem;
                    targetSlot = slot;
                    break;
                }
                if (targetItem == null) {
                    ItemStack offHand = BindGUI.this.player.getInventory().getItemInOffHand();
                    if (offHand != null && offHand.hasItemMeta()) {
                        String id2 = (String)offHand.getItemMeta().getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
                        if (boundWeaponId.toString().equals(id2)) {
                            targetItem = offHand;
                            isOffHand = true;
                        }
                    }
                }
                if (targetItem == null) {
                    for (Entity entity : BindGUI.this.player.getNearbyEntities(6.0, 6.0, 6.0)) {
                        Item gi;
                        ItemStack stack;
                        if (!(entity instanceof Item) || (stack = (gi = (Item)entity).getItemStack()) == null || !stack.hasItemMeta()) continue;
                        String id3 = (String)stack.getItemMeta().getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
                        if (!boundWeaponId.toString().equals(id3)) continue;
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
                    currentMeta.setDisplayName(MessageUtil.parse("&d&l⚡ &5&l" + broadcastName + " &d&l⚡"));
                    targetItem.setItemMeta(currentMeta);
                    if (targetSlot != -1) {
                        BindGUI.this.player.getInventory().setItem(targetSlot, targetItem);
                    } else if (isOffHand) {
                        BindGUI.this.player.getInventory().setItemInOffHand(targetItem);
                    } else if (isGroundItem && groundItemEntity != null) {
                        groundItemEntity.setItemStack(targetItem);
                    }
                    BindGUI.this.player.updateInventory();
                    MessageUtil.sendWithDefault(BindGUI.this.player, "gui.bound-broadcast", "&d&l⚡ &f&lYOUR WEAPON HAS BEEN BOUND TO YOU! &d&l⚡");
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
                    currentMeta.setDisplayName(MessageUtil.parse(this.colors[this.step % this.colors.length] + "⚡ " + broadcastName + " ⚡"));
                    targetItem.setItemMeta(currentMeta);
                    if (targetSlot != -1) {
                        BindGUI.this.player.getInventory().setItem(targetSlot, targetItem);
                    } else if (isOffHand) {
                        BindGUI.this.player.getInventory().setItemInOffHand(targetItem);
                    } else if (isGroundItem && groundItemEntity != null) {
                        groundItemEntity.setItemStack(targetItem);
                    }
                    BindGUI.this.player.updateInventory();
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
        ItemStack target = this.mainInventory.getItem(13);
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
            MessageUtil.sendWithDefault(this.player, "gui.not-your-weapon", "&cThis weapon is not bound to you!");
            this.player.playSound(this.player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        this.plugin.getDataManager().setBoundWeaponId(this.player.getUniqueId(), null);
        this.plugin.getDataManager().setHasBoundWeapon(this.player.getUniqueId(), false);

        ItemFactory.unbindItem(target);
        ItemStack finalItem = target.clone();
        this.mainInventory.setItem(13, null);
        this.player.closeInventory();

        Map<Integer, ItemStack> leftover = this.player.getInventory().addItem(finalItem);
        if (!leftover.isEmpty()) {
            for (ItemStack drop : leftover.values()) {
                this.player.getWorld().dropItemNaturally(this.player.getLocation(), drop);
            }
        }
        ItemFactory.unbindAllPlayerItems(this.player);
        MessageUtil.sendWithDefault(this.player, "gui.unbound-success", "&aSuccessfully unbound your weapon.");
        this.player.playSound(this.player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!event.getPlayer().equals(this.player)) {
            return;
        }
        Inventory closed = event.getInventory();
        if (closed.equals(this.mainInventory) || closed.equals(this.selectInventory)) {
            if (this.isSwitchingView) {
                return;
            }
            HandlerList.unregisterAll(this);

            // Safe return of any weapon currently held in slot or model selector
            if (this.weaponInSlot != null && !this.weaponInSlot.getType().isAir()) {
                ItemStack returning = this.weaponInSlot.clone();
                this.weaponInSlot = null;
                Map<Integer, ItemStack> left = this.player.getInventory().addItem(returning);
                if (!left.isEmpty()) {
                    for (ItemStack item : left.values()) {
                        this.player.getWorld().dropItemNaturally(this.player.getLocation(), item);
                    }
                }
            }

            ItemStack leftoverMain = this.mainInventory.getItem(13);
            if (leftoverMain != null && !leftoverMain.getType().isAir()) {
                this.mainInventory.setItem(13, null);
                Map<Integer, ItemStack> left2 = this.player.getInventory().addItem(leftoverMain);
                if (!left2.isEmpty()) {
                    for (ItemStack item : left2.values()) {
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
