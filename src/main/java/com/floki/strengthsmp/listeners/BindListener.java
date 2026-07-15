/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.md_5.bungee.api.ChatMessageType
 *  net.md_5.bungee.api.chat.TextComponent
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.Location
 *  org.bukkit.NamespacedKey
 *  org.bukkit.Sound
 *  org.bukkit.entity.ArmorStand
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Item
 *  org.bukkit.entity.ItemFrame
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.entity.EntityPickupItemEvent
 *  org.bukkit.event.entity.PlayerDeathEvent
 *  org.bukkit.event.inventory.InventoryAction
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.inventory.InventoryDragEvent
 *  org.bukkit.event.inventory.InventoryMoveItemEvent
 *  org.bukkit.event.inventory.InventoryPickupItemEvent
 *  org.bukkit.event.inventory.InventoryType
 *  org.bukkit.event.player.PlayerDropItemEvent
 *  org.bukkit.event.player.PlayerInteractEntityEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.event.player.PlayerRespawnEvent
 *  org.bukkit.event.player.PlayerSwapHandItemsEvent
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.InventoryView
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 *  org.bukkit.util.Vector
 */
package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.WeaponType;
import com.floki.strengthsmp.util.BindAbilityManager;
import com.floki.strengthsmp.util.CompatUtil;
import com.floki.strengthsmp.util.MessageUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class BindListener
implements Listener {
    private final StrengthSMP plugin;
    private final NamespacedKey boundKey;
    private final NamespacedKey itemIdKey;
    private final Map<UUID, List<ItemStack>> boundItemsToRestore = new ConcurrentHashMap<UUID, List<ItemStack>>();
    private final Map<UUID, Location> deathLocations = new ConcurrentHashMap<UUID, Location>();
    private final BindAbilityManager abilityManager;
    private final Map<UUID, Long> abilityCooldowns = new ConcurrentHashMap<UUID, Long>();

    public BindListener(StrengthSMP plugin) {
        this.plugin = plugin;
        this.boundKey = new NamespacedKey((Plugin)plugin, "bound_player_uuid");
        this.itemIdKey = new NamespacedKey((Plugin)plugin, "bound_item_id");
        this.abilityManager = new BindAbilityManager(plugin);
        this.startParticleTask();
    }

    private void startParticleTask() {
        new BukkitRunnable(){

            public void run() {
                if (!BindListener.this.plugin.getConfigManager().isBindSystemEnabled()) {
                    return;
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    ItemStack held = player.getInventory().getItemInMainHand();
                    BindListener.this.checkAndRevertStaleItem(player, held);
                    if (held != null && held.hasItemMeta() && held.getItemMeta().getPersistentDataContainer().has(BindListener.this.boundKey, PersistentDataType.STRING)) {
                        String itemBoundId = (String)held.getItemMeta().getPersistentDataContainer().get(BindListener.this.itemIdKey, PersistentDataType.STRING);
                        String activeBoundId = BindListener.this.plugin.getDataManager().getBoundWeaponId(player.getUniqueId());
                        if (activeBoundId != null && activeBoundId.equals(itemBoundId)) {
                            CompatUtil.spawnParticle(player.getWorld(), "SPELL_WITCH", player.getLocation().add(0.0, 1.0, 0.0), 2, 0.2, 0.4, 0.2, 0.01);
                        }
                    }
                    ItemStack off = player.getInventory().getItemInOffHand();
                    BindListener.this.checkAndRevertStaleItem(player, off);
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 10L);
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!this.plugin.getConfigManager().isBindSystemEnabled()) {
            return;
        }
        Player player = event.getEntity();
        ArrayList<ItemStack> toRestore = new ArrayList<ItemStack>();
        String activeBoundId = this.plugin.getDataManager().getBoundWeaponId(player.getUniqueId());
        if (activeBoundId == null) {
            return;
        }
        Iterator iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            String itemBoundId;
            ItemStack item = (ItemStack)iterator.next();
            if (item == null || item.getType().isAir() || !item.hasItemMeta() || !item.getItemMeta().getPersistentDataContainer().has(this.boundKey, PersistentDataType.STRING)) continue;
            String ownerUuidStr = (String)item.getItemMeta().getPersistentDataContainer().get(this.boundKey, PersistentDataType.STRING);
            if (!player.getUniqueId().toString().equals(ownerUuidStr) || !activeBoundId.equals(itemBoundId = (String)item.getItemMeta().getPersistentDataContainer().get(this.itemIdKey, PersistentDataType.STRING))) continue;
            toRestore.add(item.clone());
            iterator.remove();
        }
        if (!toRestore.isEmpty()) {
            this.boundItemsToRestore.put(player.getUniqueId(), toRestore);
            this.deathLocations.put(player.getUniqueId(), player.getLocation());
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!this.plugin.getConfigManager().isBindSystemEnabled()) {
            return;
        }
        Player player = event.getPlayer();
        List<ItemStack> items = this.boundItemsToRestore.remove(player.getUniqueId());
        Location deathLoc = this.deathLocations.remove(player.getUniqueId());
        if (items == null || deathLoc == null) {
            return;
        }
        for (ItemStack item : items) {
            this.triggerLoyaltyFlight(player, deathLoc, item);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        List<ItemStack> items = this.boundItemsToRestore.remove(player.getUniqueId());
        if (items != null) {
            for (ItemStack item : items) {
                player.getInventory().addItem(new ItemStack[]{item});
            }
        }
        this.deathLocations.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!this.plugin.getConfigManager().isBindSystemEnabled()) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();
        this.plugin.getLogger().info("[DEBUG] onPlayerDropItem fired for " + player.getName() + " with " + String.valueOf(item != null ? item.getType() : "null"));
        if (this.isOwnActiveBoundItem(item, player.getUniqueId())) {
            this.plugin.getLogger().info("[DEBUG] onPlayerDropItem: isOwnActiveBoundItem = true! Triggering ability...");
            event.setCancelled(true);
            WeaponType type = WeaponType.fromMaterial(item.getType());
            if (type != WeaponType.SHIELD) {
                this.handleBoundAbilityTrigger(player, item);
            } else {
                player.sendMessage(MessageUtil.parse("&d&l\u26a1 &7Use your Swap Item key (F) to use the Shield ability!"));
            }
        }
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        if (!this.plugin.getConfigManager().isBindSystemEnabled()) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack mainItem = event.getMainHandItem();
        ItemStack offItem = event.getOffHandItem();
        ItemStack currentMain = player.getInventory().getItemInMainHand();
        ItemStack currentOff = player.getInventory().getItemInOffHand();
        if (this.isOwnActiveBoundItem(currentMain, player.getUniqueId())) {
            WeaponType type = WeaponType.fromMaterial(currentMain.getType());
            if (type == WeaponType.SHIELD) {
                return;
            }
            event.setCancelled(true);
            this.handleBoundAbilityTrigger(player, currentMain);
        } else if (this.isOwnActiveBoundItem(currentOff, player.getUniqueId())) {
            WeaponType type = WeaponType.fromMaterial(currentOff.getType());
            if (type == WeaponType.SHIELD) {
                event.setCancelled(true);
                this.handleBoundAbilityTrigger(player, currentOff);
            } else {
                event.setCancelled(true);
                this.handleBoundAbilityTrigger(player, currentOff);
            }
        }
    }

    private void handleBoundAbilityTrigger(Player player, ItemStack item) {
        long cooldownMs;
        long lastUsed;
        WeaponType type = WeaponType.fromMaterial(item.getType());
        if (type == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - (lastUsed = this.abilityCooldowns.getOrDefault(player.getUniqueId(), 0L).longValue()) < (cooldownMs = this.plugin.getConfigManager().getBoundAbilityCooldown())) {
            long remainingSec = (cooldownMs - (now - lastUsed)) / 1000L;
            if (remainingSec <= 0L) {
                remainingSec = 1L;
            }
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText((String)MessageUtil.parse("&e&l\u26a1 &7Bound Ability on cooldown! &c(" + remainingSec + "s)")));
            return;
        }
        this.abilityCooldowns.put(player.getUniqueId(), now);
        this.abilityManager.triggerAbility(player, type);
        this.plugin.getDataManager().incrementBoundStat(player.getUniqueId(), "ability_uses");
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!this.plugin.getConfigManager().isBindSystemEnabled()) {
            return;
        }
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Player)) {
            return;
        }
        Player player = (Player)livingEntity;
        ItemStack item = event.getItem().getItemStack();
        if (this.isSomeoneElsesActiveBoundItem(item, player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!this.plugin.getConfigManager().isBindSystemEnabled()) {
            return;
        }
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();
        if (clickedItem != null && clickedItem.hasItemMeta() && clickedItem.getItemMeta().getPersistentDataContainer().has(this.boundKey, PersistentDataType.STRING)) {
            this.checkAndRevertStaleItem((Player)event.getWhoClicked(), clickedItem);
        }
        if (cursorItem != null && cursorItem.hasItemMeta() && cursorItem.getItemMeta().getPersistentDataContainer().has(this.boundKey, PersistentDataType.STRING)) {
            this.checkAndRevertStaleItem((Player)event.getWhoClicked(), cursorItem);
        }
        if (this.isSomeoneElsesActiveBoundItem(clickedItem, event.getWhoClicked().getUniqueId())) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(MessageUtil.parse("&d&l\u26a1 &7This weapon is bound to another soul!"));
            return;
        }
        if (this.isSomeoneElsesActiveBoundItem(cursorItem, event.getWhoClicked().getUniqueId())) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(MessageUtil.parse("&d&l\u26a1 &7This weapon is bound to another soul!"));
            return;
        }
        Inventory topInv = event.getView().getTopInventory();
        if (topInv != null && this.isContainerInventory(event.getView(), topInv)) {
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && this.isOwnActiveBoundItem(clickedItem, event.getWhoClicked().getUniqueId())) {
                event.setCancelled(true);
                event.getWhoClicked().sendMessage(MessageUtil.parse("&d&l\u26a1 &7You cannot store your bound weapon in containers!"));
                return;
            }
            if (event.getClickedInventory() != null && event.getClickedInventory().equals((Object)topInv) && (this.isOwnActiveBoundItem(cursorItem, event.getWhoClicked().getUniqueId()) || this.isOwnActiveBoundItem(clickedItem, event.getWhoClicked().getUniqueId()))) {
                event.setCancelled(true);
                event.getWhoClicked().sendMessage(MessageUtil.parse("&d&l\u26a1 &7You cannot store your bound weapon in containers!"));
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory topInv;
        if (!this.plugin.getConfigManager().isBindSystemEnabled()) {
            return;
        }
        ItemStack item = event.getOldCursor();
        if (this.isOwnActiveBoundItem(item, event.getWhoClicked().getUniqueId()) && (topInv = event.getView().getTopInventory()) != null && this.isContainerInventory(event.getView(), topInv)) {
            Iterator iterator = event.getRawSlots().iterator();
            while (iterator.hasNext()) {
                int slot = (Integer)iterator.next();
                if (slot >= topInv.getSize()) continue;
                event.setCancelled(true);
                event.getWhoClicked().sendMessage(MessageUtil.parse("&d&l\u26a1 &7You cannot store your bound weapon in containers!"));
                return;
            }
        }
        if (this.isSomeoneElsesActiveBoundItem(item, event.getWhoClicked().getUniqueId())) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(MessageUtil.parse("&d&l\u26a1 &7This weapon is bound to another soul!"));
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity clicked;
        if (!this.plugin.getConfigManager().isBindSystemEnabled()) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();
        if ((this.isOwnActiveBoundItem(held, player.getUniqueId()) || this.isSomeoneElsesActiveBoundItem(held, player.getUniqueId())) && ((clicked = event.getRightClicked()) instanceof ItemFrame || clicked instanceof ArmorStand)) {
            event.setCancelled(true);
            player.sendMessage(MessageUtil.parse("&d&l\u26a1 &7You cannot place your bound weapon on this!"));
        }
    }

    @EventHandler
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        if (!this.plugin.getConfigManager().isBindSystemEnabled()) {
            return;
        }
        ItemStack item = event.getItem().getItemStack();
        if (this.isActiveBoundItem(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (!this.plugin.getConfigManager().isBindSystemEnabled()) {
            return;
        }
        ItemStack item = event.getItem();
        if (this.isActiveBoundItem(item)) {
            event.setCancelled(true);
        }
    }

    private boolean isContainerInventory(InventoryView view, Inventory inv) {
        if (inv == null) {
            return false;
        }
        InventoryType type = inv.getType();
        if (type == InventoryType.PLAYER || type == InventoryType.CRAFTING || type == InventoryType.WORKBENCH || type == InventoryType.ANVIL || type == InventoryType.SMITHING || type == InventoryType.GRINDSTONE || type == InventoryType.ENCHANTING || type == InventoryType.LOOM || type == InventoryType.CARTOGRAPHY || type == InventoryType.STONECUTTER) {
            return false;
        }
        return view.getTitle() == null || !view.getTitle().contains("Weapon Binding");
    }

    private boolean isOwnActiveBoundItem(ItemStack item, UUID playerUuid) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta.getPersistentDataContainer().has(this.boundKey, PersistentDataType.STRING)) {
            String ownerUuidStr = (String)meta.getPersistentDataContainer().get(this.boundKey, PersistentDataType.STRING);
            if (playerUuid.toString().equals(ownerUuidStr)) {
                String itemBoundId = (String)meta.getPersistentDataContainer().get(this.itemIdKey, PersistentDataType.STRING);
                String activeBoundId = this.plugin.getDataManager().getBoundWeaponId(playerUuid);
                return activeBoundId != null && activeBoundId.equals(itemBoundId);
            }
        }
        return false;
    }

    private boolean isSomeoneElsesActiveBoundItem(ItemStack item, UUID playerUuid) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta.getPersistentDataContainer().has(this.boundKey, PersistentDataType.STRING)) {
            String ownerUuidStr = (String)meta.getPersistentDataContainer().get(this.boundKey, PersistentDataType.STRING);
            if (!playerUuid.toString().equals(ownerUuidStr)) {
                try {
                    UUID ownerUuid = UUID.fromString(ownerUuidStr);
                    String itemBoundId = (String)meta.getPersistentDataContainer().get(this.itemIdKey, PersistentDataType.STRING);
                    String activeBoundId = this.plugin.getDataManager().getBoundWeaponId(ownerUuid);
                    return activeBoundId != null && activeBoundId.equals(itemBoundId);
                }
                catch (IllegalArgumentException e) {
                    return false;
                }
            }
        }
        return false;
    }

    private boolean isActiveBoundItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta.getPersistentDataContainer().has(this.boundKey, PersistentDataType.STRING)) {
            String ownerUuidStr = (String)meta.getPersistentDataContainer().get(this.boundKey, PersistentDataType.STRING);
            try {
                UUID ownerUuid = UUID.fromString(ownerUuidStr);
                String itemBoundId = (String)meta.getPersistentDataContainer().get(this.itemIdKey, PersistentDataType.STRING);
                String activeBoundId = this.plugin.getDataManager().getBoundWeaponId(ownerUuid);
                return activeBoundId != null && activeBoundId.equals(itemBoundId);
            }
            catch (IllegalArgumentException e) {
                return false;
            }
        }
        return false;
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onPlayerKill(PlayerDeathEvent event) {
        if (!this.plugin.getConfigManager().isBindSystemEnabled()) {
            return;
        }
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null) {
            return;
        }
        ItemStack held = killer.getInventory().getItemInMainHand();
        if (held == null || !held.hasItemMeta()) {
            return;
        }
        if (held.getItemMeta().getPersistentDataContainer().has(this.boundKey, PersistentDataType.STRING)) {
            String ownerUuidStr = (String)held.getItemMeta().getPersistentDataContainer().get(this.boundKey, PersistentDataType.STRING);
            if (killer.getUniqueId().toString().equals(ownerUuidStr)) {
                String itemBoundId = (String)held.getItemMeta().getPersistentDataContainer().get(this.itemIdKey, PersistentDataType.STRING);
                String activeBoundId = this.plugin.getDataManager().getBoundWeaponId(killer.getUniqueId());
                if (activeBoundId != null && activeBoundId.equals(itemBoundId)) {
                    this.plugin.getDataManager().recordBoundKill(killer.getUniqueId(), victim.getName());
                }
            }
        }
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onWeaponDamage(EntityDamageByEntityEvent event) {
        if (!this.plugin.getConfigManager().isBindSystemEnabled()) {
            return;
        }
        Entity entity = event.getDamager();
        if (!(entity instanceof Player)) {
            return;
        }
        Player attacker = (Player)entity;
        ItemStack held = attacker.getInventory().getItemInMainHand();
        if (held == null || !held.hasItemMeta()) {
            return;
        }
        if (held.getItemMeta().getPersistentDataContainer().has(this.boundKey, PersistentDataType.STRING)) {
            String ownerUuidStr = (String)held.getItemMeta().getPersistentDataContainer().get(this.boundKey, PersistentDataType.STRING);
            if (attacker.getUniqueId().toString().equals(ownerUuidStr)) {
                String itemBoundId = (String)held.getItemMeta().getPersistentDataContainer().get(this.itemIdKey, PersistentDataType.STRING);
                String activeBoundId = this.plugin.getDataManager().getBoundWeaponId(attacker.getUniqueId());
                if (activeBoundId != null && activeBoundId.equals(itemBoundId)) {
                    this.plugin.getDataManager().addBoundStatDouble(attacker.getUniqueId(), "damage_dealt", event.getFinalDamage());
                }
            }
        }
    }

    private void triggerLoyaltyFlight(final Player player, Location deathLoc, final ItemStack itemStack) {
        Location startLoc = deathLoc.clone();
        Location playerLoc = player.getLocation();
        if (!startLoc.getWorld().equals((Object)playerLoc.getWorld())) {
            startLoc = playerLoc.clone().add(0.0, 25.0, 0.0);
        } else if (startLoc.distance(playerLoc) > 120.0) {
            Vector dir = startLoc.toVector().subtract(playerLoc.toVector()).normalize();
            startLoc = playerLoc.clone().add(dir.multiply(60));
        }
        final Location currentPos = startLoc.clone();
        final Item flyingItem = startLoc.getWorld().dropItem(startLoc, itemStack);
        flyingItem.setGravity(false);
        flyingItem.setPickupDelay(Short.MAX_VALUE);
        flyingItem.setInvulnerable(true);
        final double kbRadius = this.plugin.getConfigManager().getBindKnockbackRadius();
        final double kbStrength = this.plugin.getConfigManager().getBindKnockbackStrength();
        final int maxDuration = this.plugin.getConfigManager().getBindAnimationTicks();
        new BukkitRunnable(){
            int ticks = 0;

            public void run() {
                ++this.ticks;
                if (!player.isOnline() || flyingItem.isDead() || this.ticks > maxDuration) {
                    if (player.isOnline()) {
                        player.getInventory().addItem(new ItemStack[]{itemStack});
                        player.sendMessage(MessageUtil.parse("&d&l\u26a1 &fYour bound weapon returned from the void. &d&l\u26a1"));
                    }
                    flyingItem.remove();
                    this.cancel();
                    return;
                }
                Location target = player.getEyeLocation().subtract(0.0, 0.5, 0.0);
                double distance = currentPos.distance(target);
                if (distance < 1.5) {
                    player.getInventory().addItem(new ItemStack[]{itemStack});
                    player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_RETURN, 1.0f, 1.0f);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText((String)MessageUtil.parse("&d&l\u26a1 Bound Weapon Returned! \u26a1")));
                    flyingItem.remove();
                    this.cancel();
                    return;
                }
                Vector dir = target.toVector().subtract(currentPos.toVector()).normalize();
                double speed = Math.min(1.8, 0.4 + (double)this.ticks * 0.08);
                currentPos.add(dir.multiply(speed));
                flyingItem.teleport(currentPos);
                if (this.ticks % 3 == 0) {
                    currentPos.getWorld().playSound(currentPos, Sound.ITEM_TRIDENT_THROW, 0.8f, 0.6f + (float)this.ticks * 0.05f);
                }
                CompatUtil.spawnParticle(currentPos.getWorld(), "SPELL_WITCH", currentPos, 5, 0.1, 0.1, 0.1, 0.02);
                CompatUtil.spawnParticle(currentPos.getWorld(), "CRIT", currentPos, 3, 0.1, 0.1, 0.1, 0.05);
                for (Entity nearby : flyingItem.getNearbyEntities(kbRadius, kbRadius, kbRadius)) {
                    if (!(nearby instanceof LivingEntity) || nearby.equals((Object)player)) continue;
                    Vector push = nearby.getLocation().toVector().subtract(currentPos.toVector());
                    if (push.lengthSquared() > 0.0) {
                        push.normalize().setY(0.35).multiply(kbStrength);
                    } else {
                        push = new Vector(0.0, 0.35, 0.0);
                    }
                    nearby.setVelocity(push);
                    if (nearby instanceof Player) {
                        Player targetPlayer = (Player)nearby;
                        targetPlayer.sendMessage(MessageUtil.parse("&d&l\u26a1 &7You were knocked aside by a returning bound weapon!"));
                    }
                    currentPos.getWorld().playSound(nearby.getLocation(), Sound.ENTITY_PHANTOM_BITE, 1.0f, 0.5f);
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 1L);
    }

    private void checkAndRevertStaleItem(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        if (!meta.getPersistentDataContainer().has(this.boundKey, PersistentDataType.STRING)) {
            return;
        }
        String ownerUuidStr = (String)meta.getPersistentDataContainer().get(this.boundKey, PersistentDataType.STRING);
        String itemBoundId = (String)meta.getPersistentDataContainer().get(this.itemIdKey, PersistentDataType.STRING);
        try {
            boolean isStale;
            UUID ownerUuid = UUID.fromString(ownerUuidStr);
            String activeBoundId = this.plugin.getDataManager().getBoundWeaponId(ownerUuid);
            boolean bl = isStale = activeBoundId == null || !activeBoundId.equals(itemBoundId);
            if (isStale) {
                this.revertBoundItem(item);
            }
        }
        catch (IllegalArgumentException e) {
            this.revertBoundItem(item);
        }
    }

    private void revertBoundItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        meta.getPersistentDataContainer().remove(this.boundKey);
        meta.getPersistentDataContainer().remove(this.itemIdKey);
        NamespacedKey origCmdKey = new NamespacedKey((Plugin)this.plugin, "smp_original_cmd");
        NamespacedKey origNameKey = new NamespacedKey((Plugin)this.plugin, "smp_original_name");
        if (meta.getPersistentDataContainer().has(origCmdKey, PersistentDataType.INTEGER)) {
            int origCmd = (Integer)meta.getPersistentDataContainer().get(origCmdKey, PersistentDataType.INTEGER);
            meta.setCustomModelData(Integer.valueOf(origCmd));
            meta.getPersistentDataContainer().remove(origCmdKey);
        } else {
            meta.setCustomModelData(null);
        }
        List<String> lore = meta.getLore();
        if (lore != null) {
            String prefix = MessageUtil.parse(this.plugin.getConfigManager().getBindLorePrefix());
            lore.removeIf(line -> line.contains("BOUND WEAPON") || line.contains(prefix));
            meta.setLore(lore);
        }
        if (meta.getPersistentDataContainer().has(origNameKey, PersistentDataType.STRING)) {
            String origName = (String)meta.getPersistentDataContainer().get(origNameKey, PersistentDataType.STRING);
            meta.setDisplayName(origName);
            meta.getPersistentDataContainer().remove(origNameKey);
        } else if (meta.hasDisplayName()) {
            String displayName = meta.getDisplayName();
            String strippedName = ChatColor.stripColor((String)displayName);
            strippedName = strippedName.replace("\u26a1", "").trim();
            meta.setDisplayName(MessageUtil.color("&f" + strippedName));
        }
        item.setItemMeta(meta);
    }
}

