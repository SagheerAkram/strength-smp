package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.floki.strengthsmp.data.WeaponType;
import com.floki.strengthsmp.util.BindAbilityManager;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class BindListener implements Listener {

    private final StrengthSMP plugin;
    private final NamespacedKey boundKey;
    private final NamespacedKey itemIdKey;

    // Temporary storage for items to restore upon respawn
    private final Map<UUID, List<ItemStack>> boundItemsToRestore = new ConcurrentHashMap<>();
    private final Map<UUID, Location> deathLocations = new ConcurrentHashMap<>();
    
    private final BindAbilityManager abilityManager;
    private final Map<UUID, Long> abilityCooldowns = new ConcurrentHashMap<>();

    public BindListener(StrengthSMP plugin) {
        this.plugin = plugin;
        this.boundKey = new NamespacedKey(plugin, "bound_player_uuid");
        this.itemIdKey = new NamespacedKey(plugin, "bound_item_id");
        this.abilityManager = new BindAbilityManager(plugin);

        // Start Soulbound particles task
        startParticleTask();
    }

    private void startParticleTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.getConfigManager().isBindSystemEnabled()) return;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    ItemStack held = player.getInventory().getItemInMainHand();
                    if (held != null && held.hasItemMeta()) {
                        if (held.getItemMeta().getPersistentDataContainer().has(boundKey, PersistentDataType.STRING)) {
                            String itemBoundId = held.getItemMeta().getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
                            String activeBoundId = plugin.getDataManager().getBoundWeaponId(player.getUniqueId());
                            if (activeBoundId != null && activeBoundId.equals(itemBoundId)) {
                                // Emit subtle portal/enchantment particles
                                player.getWorld().spawnParticle(
                                    Particle.SPELL_WITCH, 
                                    player.getLocation().add(0, 1.0, 0), 
                                    2, 
                                    0.2, 0.4, 0.2, 
                                    0.01
                                );
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.getConfigManager().isBindSystemEnabled()) return;

        Player player = event.getEntity();
        List<ItemStack> toRestore = new ArrayList<>();
        String activeBoundId = plugin.getDataManager().getBoundWeaponId(player.getUniqueId());

        if (activeBoundId == null) return;

        Iterator<ItemStack> iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            if (item == null || item.getType().isAir()) continue;

            if (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(boundKey, PersistentDataType.STRING)) {
                String ownerUuidStr = item.getItemMeta().getPersistentDataContainer().get(boundKey, PersistentDataType.STRING);
                if (player.getUniqueId().toString().equals(ownerUuidStr)) {
                    String itemBoundId = item.getItemMeta().getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
                    if (activeBoundId.equals(itemBoundId)) {
                        toRestore.add(item.clone());
                        iterator.remove(); // Prevent dropping on the ground
                    }
                }
            }
        }

        if (!toRestore.isEmpty()) {
            boundItemsToRestore.put(player.getUniqueId(), toRestore);
            deathLocations.put(player.getUniqueId(), player.getLocation());
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!plugin.getConfigManager().isBindSystemEnabled()) return;

        Player player = event.getPlayer();
        List<ItemStack> items = boundItemsToRestore.remove(player.getUniqueId());
        Location deathLoc = deathLocations.remove(player.getUniqueId());

        if (items == null || deathLoc == null) return;

        // Run the Loyalty return animations for each item
        for (ItemStack item : items) {
            triggerLoyaltyFlight(player, deathLoc, item);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Safe fallback: if player quits before respawning or during flight, restore items immediately
        Player player = event.getPlayer();
        List<ItemStack> items = boundItemsToRestore.remove(player.getUniqueId());
        if (items != null) {
            for (ItemStack item : items) {
                player.getInventory().addItem(item);
            }
        }
        deathLocations.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!plugin.getConfigManager().isBindSystemEnabled()) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();
        plugin.getLogger().info("[DEBUG] onPlayerDropItem fired for " + player.getName() + " with " + (item != null ? item.getType() : "null"));
        if (isOwnActiveBoundItem(item, player.getUniqueId())) {
            plugin.getLogger().info("[DEBUG] onPlayerDropItem: isOwnActiveBoundItem = true! Triggering ability...");
            event.setCancelled(true);
            handleBoundAbilityTrigger(player, item);
        }
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        if (!plugin.getConfigManager().isBindSystemEnabled()) return;

        Player player = event.getPlayer();
        ItemStack mainItem = event.getMainHandItem();
        ItemStack offItem = event.getOffHandItem();
        plugin.getLogger().info("[DEBUG] onPlayerSwapHandItems fired for " + player.getName() + 
            ". Main: " + (mainItem != null ? mainItem.getType() : "null") + 
            ", Off: " + (offItem != null ? offItem.getType() : "null"));
        ItemStack boundItem = null;

        if (isOwnActiveBoundItem(mainItem, player.getUniqueId())) {
            boundItem = mainItem;
        } else if (isOwnActiveBoundItem(offItem, player.getUniqueId())) {
            boundItem = offItem;
        }

        if (boundItem != null) {
            plugin.getLogger().info("[DEBUG] onPlayerSwapHandItems: bound item found! Triggering ability...");
            event.setCancelled(true);
            handleBoundAbilityTrigger(player, boundItem);
        }
    }

    private void handleBoundAbilityTrigger(Player player, ItemStack item) {
        WeaponType type = WeaponType.fromMaterial(item.getType());
        if (type == null) return;

        long now = System.currentTimeMillis();
        long lastUsed = abilityCooldowns.getOrDefault(player.getUniqueId(), 0L);
        long cooldownMs = plugin.getConfigManager().getBoundAbilityCooldown();

        if (now - lastUsed < cooldownMs) {
            long remainingSec = (cooldownMs - (now - lastUsed)) / 1000L;
            if (remainingSec <= 0) remainingSec = 1;
            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(
                    MessageUtil.parse("&e&l⚡ &7Bound Ability on cooldown! &c(" + remainingSec + "s)")
                )
            );
            return;
        }

        abilityCooldowns.put(player.getUniqueId(), now);
        abilityManager.triggerAbility(player, type);
        plugin.getDataManager().incrementBoundStat(player.getUniqueId(), "ability_uses");
    }


    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!plugin.getConfigManager().isBindSystemEnabled()) return;
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack item = event.getItem().getItemStack();
        if (isSomeoneElsesActiveBoundItem(item, player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!plugin.getConfigManager().isBindSystemEnabled()) return;

        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        // Prevent picking up someone else's active bound item from any inventory
        if (isSomeoneElsesActiveBoundItem(clickedItem, event.getWhoClicked().getUniqueId())) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(MessageUtil.parse("&d&l⚡ &7This weapon is bound to another soul!"));
            return;
        }
        if (isSomeoneElsesActiveBoundItem(cursorItem, event.getWhoClicked().getUniqueId())) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(MessageUtil.parse("&d&l⚡ &7This weapon is bound to another soul!"));
            return;
        }

        // Prevent owner from storing their own active bound item in containers
        org.bukkit.inventory.Inventory topInv = event.getView().getTopInventory();
        if (topInv != null && isContainerInventory(event.getView(), topInv)) {
            // Shift-clicking into container
            if (event.getAction() == org.bukkit.event.inventory.InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                if (isOwnActiveBoundItem(clickedItem, event.getWhoClicked().getUniqueId())) {
                    event.setCancelled(true);
                    event.getWhoClicked().sendMessage(MessageUtil.parse("&d&l⚡ &7You cannot store your bound weapon in containers!"));
                    return;
                }
            }

            // Clicked container slot or hotbar swapping to container slot
            if (event.getClickedInventory() != null && event.getClickedInventory().equals(topInv)) {
                if (isOwnActiveBoundItem(cursorItem, event.getWhoClicked().getUniqueId()) || isOwnActiveBoundItem(clickedItem, event.getWhoClicked().getUniqueId())) {
                    event.setCancelled(true);
                    event.getWhoClicked().sendMessage(MessageUtil.parse("&d&l⚡ &7You cannot store your bound weapon in containers!"));
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!plugin.getConfigManager().isBindSystemEnabled()) return;

        ItemStack item = event.getOldCursor();
        if (isOwnActiveBoundItem(item, event.getWhoClicked().getUniqueId())) {
            org.bukkit.inventory.Inventory topInv = event.getView().getTopInventory();
            if (topInv != null && isContainerInventory(event.getView(), topInv)) {
                for (int slot : event.getRawSlots()) {
                    if (slot < topInv.getSize()) {
                        event.setCancelled(true);
                        event.getWhoClicked().sendMessage(MessageUtil.parse("&d&l⚡ &7You cannot store your bound weapon in containers!"));
                        return;
                    }
                }
            }
        }
        
        if (isSomeoneElsesActiveBoundItem(item, event.getWhoClicked().getUniqueId())) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(MessageUtil.parse("&d&l⚡ &7This weapon is bound to another soul!"));
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!plugin.getConfigManager().isBindSystemEnabled()) return;
        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();
        if (isOwnActiveBoundItem(held, player.getUniqueId()) || isSomeoneElsesActiveBoundItem(held, player.getUniqueId())) {
            Entity clicked = event.getRightClicked();
            if (clicked instanceof org.bukkit.entity.ItemFrame || clicked instanceof org.bukkit.entity.ArmorStand) {
                event.setCancelled(true);
                player.sendMessage(MessageUtil.parse("&d&l⚡ &7You cannot place your bound weapon on this!"));
            }
        }
    }

    @EventHandler
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        if (!plugin.getConfigManager().isBindSystemEnabled()) return;
        ItemStack item = event.getItem().getItemStack();
        if (isActiveBoundItem(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (!plugin.getConfigManager().isBindSystemEnabled()) return;
        ItemStack item = event.getItem();
        if (isActiveBoundItem(item)) {
            event.setCancelled(true);
        }
    }

    private boolean isContainerInventory(org.bukkit.inventory.InventoryView view, Inventory inv) {
        if (inv == null) return false;
        org.bukkit.event.inventory.InventoryType type = inv.getType();
        
        if (type == org.bukkit.event.inventory.InventoryType.PLAYER || 
            type == org.bukkit.event.inventory.InventoryType.CRAFTING ||
            type == org.bukkit.event.inventory.InventoryType.WORKBENCH ||
            type == org.bukkit.event.inventory.InventoryType.ANVIL ||
            type == org.bukkit.event.inventory.InventoryType.SMITHING ||
            type == org.bukkit.event.inventory.InventoryType.GRINDSTONE ||
            type == org.bukkit.event.inventory.InventoryType.ENCHANTING ||
            type == org.bukkit.event.inventory.InventoryType.LOOM ||
            type == org.bukkit.event.inventory.InventoryType.CARTOGRAPHY ||
            type == org.bukkit.event.inventory.InventoryType.STONECUTTER) {
            return false;
        }

        if (view.getTitle() != null && view.getTitle().contains("Weapon Binding")) {
            return false;
        }

        return true;
    }

    private boolean isOwnActiveBoundItem(ItemStack item, UUID playerUuid) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta.getPersistentDataContainer().has(boundKey, PersistentDataType.STRING)) {
            String ownerUuidStr = meta.getPersistentDataContainer().get(boundKey, PersistentDataType.STRING);
            if (playerUuid.toString().equals(ownerUuidStr)) {
                String itemBoundId = meta.getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
                String activeBoundId = plugin.getDataManager().getBoundWeaponId(playerUuid);
                return activeBoundId != null && activeBoundId.equals(itemBoundId);
            }
        }
        return false;
    }

    private boolean isSomeoneElsesActiveBoundItem(ItemStack item, UUID playerUuid) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta.getPersistentDataContainer().has(boundKey, PersistentDataType.STRING)) {
            String ownerUuidStr = meta.getPersistentDataContainer().get(boundKey, PersistentDataType.STRING);
            if (!playerUuid.toString().equals(ownerUuidStr)) {
                try {
                    UUID ownerUuid = UUID.fromString(ownerUuidStr);
                    String itemBoundId = meta.getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
                    String activeBoundId = plugin.getDataManager().getBoundWeaponId(ownerUuid);
                    return activeBoundId != null && activeBoundId.equals(itemBoundId);
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
        }
        return false;
    }

    private boolean isActiveBoundItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta.getPersistentDataContainer().has(boundKey, PersistentDataType.STRING)) {
            String ownerUuidStr = meta.getPersistentDataContainer().get(boundKey, PersistentDataType.STRING);
            try {
                UUID ownerUuid = UUID.fromString(ownerUuidStr);
                String itemBoundId = meta.getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
                String activeBoundId = plugin.getDataManager().getBoundWeaponId(ownerUuid);
                return activeBoundId != null && activeBoundId.equals(itemBoundId);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerKill(PlayerDeathEvent event) {
        if (!plugin.getConfigManager().isBindSystemEnabled()) return;
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null) return;

        ItemStack held = killer.getInventory().getItemInMainHand();
        if (held == null || !held.hasItemMeta()) return;

        if (held.getItemMeta().getPersistentDataContainer().has(boundKey, PersistentDataType.STRING)) {
            String ownerUuidStr = held.getItemMeta().getPersistentDataContainer().get(boundKey, PersistentDataType.STRING);
            if (killer.getUniqueId().toString().equals(ownerUuidStr)) {
                String itemBoundId = held.getItemMeta().getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
                String activeBoundId = plugin.getDataManager().getBoundWeaponId(killer.getUniqueId());
                if (activeBoundId != null && activeBoundId.equals(itemBoundId)) {
                    plugin.getDataManager().recordBoundKill(killer.getUniqueId(), victim.getName());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWeaponDamage(EntityDamageByEntityEvent event) {
        if (!plugin.getConfigManager().isBindSystemEnabled()) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        ItemStack held = attacker.getInventory().getItemInMainHand();
        if (held == null || !held.hasItemMeta()) return;

        if (held.getItemMeta().getPersistentDataContainer().has(boundKey, PersistentDataType.STRING)) {
            String ownerUuidStr = held.getItemMeta().getPersistentDataContainer().get(boundKey, PersistentDataType.STRING);
            if (attacker.getUniqueId().toString().equals(ownerUuidStr)) {
                String itemBoundId = held.getItemMeta().getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
                String activeBoundId = plugin.getDataManager().getBoundWeaponId(attacker.getUniqueId());
                if (activeBoundId != null && activeBoundId.equals(itemBoundId)) {
                    plugin.getDataManager().addBoundStatDouble(attacker.getUniqueId(), "damage_dealt", event.getFinalDamage());
                }
            }
        }
    }

    // Soul Sacrifice (totem hack) removed — bound weapons only provide F/Q abilities
    // and the loyalty flight return on death.

    private void triggerLoyaltyFlight(Player player, Location deathLoc, ItemStack itemStack) {
        Location startLoc = deathLoc.clone();
        Location playerLoc = player.getLocation();

        // Safe range / cross-world handler
        if (!startLoc.getWorld().equals(playerLoc.getWorld())) {
            startLoc = playerLoc.clone().add(0, 25, 0); // Fly down from the sky
        } else if (startLoc.distance(playerLoc) > 120) {
            Vector dir = startLoc.toVector().subtract(playerLoc.toVector()).normalize();
            startLoc = playerLoc.clone().add(dir.multiply(60)); // Limit start distance to 60 blocks
        }

        final Location currentPos = startLoc.clone();
        
        // Spawn temporary flying Item entity
        Item flyingItem = startLoc.getWorld().dropItem(startLoc, itemStack);
        flyingItem.setGravity(false);
        flyingItem.setPickupDelay(32767); // Prevent anyone from picking it up
        flyingItem.setInvulnerable(true);

        // Get config settings
        final double kbRadius = plugin.getConfigManager().getBindKnockbackRadius();
        final double kbStrength = plugin.getConfigManager().getBindKnockbackStrength();
        final int maxDuration = plugin.getConfigManager().getBindAnimationTicks();

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                ticks++;

                if (!player.isOnline() || flyingItem.isDead() || ticks > maxDuration) {
                    // Safe cleanup: return item directly to inventory if something went wrong
                    if (player.isOnline()) {
                        player.getInventory().addItem(itemStack);
                        player.sendMessage(MessageUtil.parse("&d&l⚡ &fYour bound weapon returned from the void. &d&l⚡"));
                    }
                    flyingItem.remove();
                    cancel();
                    return;
                }

                Location target = player.getEyeLocation().subtract(0, 0.5, 0);
                double distance = currentPos.distance(target);

                if (distance < 1.5) {
                    // Reached player!
                    player.getInventory().addItem(itemStack);
                    player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_RETURN, 1.0f, 1.0f);
                    player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(MessageUtil.parse("&d&l⚡ Bound Weapon Returned! ⚡")));
                    flyingItem.remove();
                    cancel();
                    return;
                }

                // Move item pos closer to player (loyalty effect)
                Vector dir = target.toVector().subtract(currentPos.toVector()).normalize();
                double speed = Math.min(1.8, 0.4 + (ticks * 0.08)); // accelerate
                currentPos.add(dir.multiply(speed));
                flyingItem.teleport(currentPos);

                // Play return sound & particles
                if (ticks % 3 == 0) {
                    currentPos.getWorld().playSound(currentPos, Sound.ITEM_TRIDENT_THROW, 0.8f, 0.6f + (ticks * 0.05f));
                }
                currentPos.getWorld().spawnParticle(Particle.SPELL_WITCH, currentPos, 5, 0.1, 0.1, 0.1, 0.02);
                currentPos.getWorld().spawnParticle(Particle.CRIT, currentPos, 3, 0.1, 0.1, 0.1, 0.05);

                // Knockback entities in path
                for (Entity nearby : flyingItem.getNearbyEntities(kbRadius, kbRadius, kbRadius)) {
                    if (nearby instanceof LivingEntity && !nearby.equals(player)) {
                        Vector push = nearby.getLocation().toVector().subtract(currentPos.toVector());
                        if (push.lengthSquared() > 0) {
                            push.normalize().setY(0.35).multiply(kbStrength);
                        } else {
                            push = new Vector(0, 0.35, 0);
                        }
                        nearby.setVelocity(push);
                        if (nearby instanceof Player targetPlayer) {
                            targetPlayer.sendMessage(MessageUtil.parse("&d&l⚡ &7You were knocked aside by a returning bound weapon!"));
                        }
                        currentPos.getWorld().playSound(nearby.getLocation(), Sound.ENTITY_PHANTOM_BITE, 1.0f, 0.5f);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
