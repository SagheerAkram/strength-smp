package com.floki.strengthsmp.services;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TrackingService implements Listener {
    private final StrengthSMP plugin;
    private final NamespacedKey keyTempTracker;
    private final Map<UUID, TrackingSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public TrackingService(StrengthSMP plugin) {
        this.plugin = plugin;
        this.keyTempTracker = new NamespacedKey(plugin, "temporary_tracker");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void startTracking(Player hunter, Player target, String targetType) {
        if (hunter == null || target == null || !hunter.isOnline() || !target.isOnline()) {
            return;
        }

        if (hunter.equals(target)) {
            hunter.sendMessage(MessageUtil.color("&c✖ You cannot track yourself!"));
            return;
        }

        if (!hunter.getWorld().equals(target.getWorld())) {
            hunter.sendMessage(MessageUtil.color("&c✖ Target is in another dimension (&f" + target.getWorld().getName() + "&c)!"));
            return;
        }

        // Check if tracking is enabled in config
        if (!plugin.getConfig().getBoolean("tracking.enabled", true)) {
            hunter.sendMessage(MessageUtil.color("&cTracking is currently disabled on this server."));
            return;
        }

        // Cooldown check (default 1 hour = 3600s, configurable)
        if (!checkAndApplyCooldown(hunter)) {
            return;
        }

        // Cancel previous session if any
        stopTracking(hunter, false);

        int durationSeconds = plugin.getConfig().getInt("tracking.duration-seconds", 5);
        long expiresAt = System.currentTimeMillis() + (durationSeconds * 1000L);

        // 1. Give temporary soulbound compass (Dynamic Lodestone, no distance in lore)
        ItemStack trackerCompass = createTrackerCompass(target, targetType, durationSeconds);
        cleanTrackersFromInventory(hunter);

        Map<Integer, ItemStack> leftover = hunter.getInventory().addItem(trackerCompass);
        if (!leftover.isEmpty()) {
            ItemStack off = hunter.getInventory().getItemInOffHand();
            if (off == null || off.getType().isAir()) {
                hunter.getInventory().setItemInOffHand(trackerCompass);
                hunter.sendMessage(MessageUtil.color("&e[!] Inventory full: temporary tracker placed in off-hand."));
            } else {
                // Never overwrite the off-hand (totems, shields); the boss bar radar still tracks.
                hunter.sendMessage(MessageUtil.color("&e[!] Inventory full: no compass given, follow the radar bar instead."));
            }
        }

        hunter.setCompassTarget(target.getLocation());
        hunter.playSound(hunter.getLocation(), Sound.ITEM_LODESTONE_COMPASS_LOCK, 1.0f, 1.6f);

        // 2. Create interactive BossBar Radar (Direction ONLY, no coordinates/distance)
        BarColor barColor = parseBarColor(plugin.getConfig().getString("tracking.bossbar.color", "RED"));
        BarStyle barStyle = parseBarStyle(plugin.getConfig().getString("tracking.bossbar.style", "SOLID"));
        BossBar bossBar = Bukkit.createBossBar(
                buildBarTitle(hunter, target, targetType, durationSeconds),
                barColor,
                barStyle
        );
        bossBar.setProgress(1.0);
        bossBar.addPlayer(hunter);
        bossBar.setVisible(true);

        // 3. Live tracking runnable (updates direction arrow and lodestone every 5 ticks = 0.25s)
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!hunter.isOnline()) {
                    stopTracking(hunter, false);
                    cancel();
                    return;
                }

                if (!target.isOnline() || !hunter.getWorld().equals(target.getWorld())) {
                    hunter.sendMessage(MessageUtil.color("&c✖ Tracking lost: target disconnected or entered another dimension!"));
                    stopTracking(hunter, false);
                    cancel();
                    return;
                }

                long remainingMillis = expiresAt - System.currentTimeMillis();
                if (remainingMillis <= 0) {
                    stopTracking(hunter, true);
                    cancel();
                    return;
                }

                double remainingSec = remainingMillis / 1000.0;
                double progress = Math.max(0.0, Math.min(1.0, remainingSec / (double) durationSeconds));

                // Update BossBar Title (Direction ONLY) & Progress
                bossBar.setTitle(buildBarTitle(hunter, target, targetType, remainingSec));
                bossBar.setProgress(progress);

                // Update compass lodestone live
                updateCompassLodestone(hunter, target);
                hunter.setCompassTarget(target.getLocation());
            }
        }.runTaskTimer(plugin, 5L, 5L);

        TrackingSession session = new TrackingSession(hunter.getUniqueId(), target.getUniqueId(), targetType, bossBar, task, expiresAt);
        activeSessions.put(hunter.getUniqueId(), session);

        hunter.sendMessage(MessageUtil.color("&a✦ &lRADAR LOCKED &7— Pointing towards &e" + target.getName() + " &7for &c" + durationSeconds + "s&7!"));
    }

    public boolean checkAndApplyCooldown(Player hunter) {
        boolean opBypass = plugin.getConfig().getBoolean("tracking.op-bypass", false);
        if (opBypass && hunter.hasPermission("strengthsmp.tracking.bypass")) {
            return true;
        }

        int cdSeconds = plugin.getConfig().getInt("tracking.cooldown-seconds", 3600);
        if (cdSeconds <= 0) return true;

        long now = System.currentTimeMillis();
        Long lastUse = getLastUse(hunter.getUniqueId());
        if (lastUse != null) {
            long elapsedSec = (now - lastUse) / 1000L;
            if (elapsedSec < cdSeconds) {
                long remainingSec = cdSeconds - elapsedSec;
                String formatted = formatTime(remainingSec);
                String msg = plugin.getConfig().getString("tracking.messages.cooldown", "&c✖ Tracking is on cooldown! Please wait &e%time% &cbefore tracking again.");
                hunter.sendMessage(MessageUtil.color(msg.replace("%time%", formatted)));
                hunter.playSound(hunter.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return false;
            }
        }
        setLastUse(hunter.getUniqueId(), now);
        return true;
    }

    public Long getLastUse(UUID uuid) {
        Long cached = cooldowns.get(uuid);
        if (cached != null) return cached;
        if (plugin.getDataManager() != null && plugin.getDataManager().getData() != null) {
            long saved = plugin.getDataManager().getData().getLong("tracking_cooldowns." + uuid, 0L);
            if (saved > 0) {
                cooldowns.put(uuid, saved);
                return saved;
            }
        }
        return null;
    }

    public void setLastUse(UUID uuid, long timestamp) {
        cooldowns.put(uuid, timestamp);
        if (plugin.getDataManager() != null && plugin.getDataManager().getData() != null) {
            plugin.getDataManager().getData().set("tracking_cooldowns." + uuid, timestamp);
        }
    }

    public void resetCooldown(UUID playerUuid) {
        cooldowns.remove(playerUuid);
        if (plugin.getDataManager() != null && plugin.getDataManager().getData() != null) {
            plugin.getDataManager().getData().set("tracking_cooldowns." + playerUuid, null);
        }
    }

    public static String formatTime(long seconds) {
        if (seconds >= 3600) {
            long hours = seconds / 3600;
            long mins = (seconds % 3600) / 60;
            long secs = seconds % 60;
            return hours + "h " + mins + "m " + secs + "s";
        } else if (seconds >= 60) {
            long mins = seconds / 60;
            long secs = seconds % 60;
            return mins + "m " + secs + "s";
        } else {
            return seconds + "s";
        }
    }

    public void stopTracking(Player hunter, boolean notifyExpired) {
        if (hunter == null) return;
        TrackingSession session = activeSessions.remove(hunter.getUniqueId());
        if (session != null) {
            try {
                session.task.cancel();
            } catch (Exception ignored) {}

            try {
                session.bossBar.removeAll();
                session.bossBar.setVisible(false);
            } catch (Exception ignored) {}
        }

        cleanTrackersFromInventory(hunter);

        if (notifyExpired && hunter.isOnline()) {
            hunter.playSound(hunter.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.7f, 1.4f);
            String msg = plugin.getConfig().getString("tracking.messages.expired", "&8[&cTracker&8] &7The tracking signal has faded. Compass vanished!");
            hunter.sendMessage(MessageUtil.color(msg));
        }
    }

    private ItemStack createTrackerCompass(Player target, String targetType, int durationSeconds) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        CompassMeta meta = (CompassMeta) compass.getItemMeta();
        if (meta != null) {
            meta.setLodestone(target.getLocation());
            meta.setLodestoneTracked(false);

            String nameFormat = targetType.equalsIgnoreCase("MONARCH")
                    ? plugin.getConfig().getString("tracking.compass.monarch-name", "&6&l👑 ROYAL TRACKER &7(%time_left%s)")
                    : plugin.getConfig().getString("tracking.compass.bounty-name", "&c&l🎯 BOUNTY RADAR &7(%time_left%s)");
            meta.setDisplayName(MessageUtil.color(nameFormat.replace("%time_left%", String.valueOf(durationSeconds))));

            List<String> rawLore = plugin.getConfig().getStringList("tracking.compass.lore");
            if (rawLore.isEmpty()) {
                rawLore = Arrays.asList(
                        "&8&m─────────────────────────",
                        "&7Target: &e&l%target%",
                        "&7Radar: &bActive Direction",
                        "&c⌛ Auto-vanishes in %time_left% seconds",
                        "&4✖ Soulbound: Cannot be dropped or looted",
                        "&8&m─────────────────────────"
                );
            }
            List<String> lore = new ArrayList<>();
            for (String l : rawLore) {
                lore.add(MessageUtil.color(l
                        .replace("%target%", target.getName())
                        .replace("%direction%", "Locked")
                        .replace("%time_left%", String.valueOf(durationSeconds))
                ));
            }
            meta.setLore(lore);

            meta.getPersistentDataContainer().set(keyTempTracker, PersistentDataType.BYTE, (byte) 1);
            meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            compass.setItemMeta(meta);
        }
        return compass;
    }

    private void updateCompassLodestone(Player hunter, Player target) {
        for (ItemStack item : hunter.getInventory().getContents()) {
            if (isTemporaryTracker(item) && item.getItemMeta() instanceof CompassMeta) {
                CompassMeta cm = (CompassMeta) item.getItemMeta();
                cm.setLodestone(target.getLocation());
                cm.setLodestoneTracked(false);
                item.setItemMeta(cm);
            }
        }
    }

    public void cleanTrackersFromInventory(Player player) {
        if (player == null || !player.isOnline()) return;
        ItemStack[] contents = player.getInventory().getContents();
        boolean removed = false;
        for (int i = 0; i < contents.length; i++) {
            if (isTemporaryTracker(contents[i])) {
                contents[i] = null;
                removed = true;
            }
        }
        if (removed) {
            player.getInventory().setContents(contents);
            player.updateInventory();
        }
    }

    public boolean isTemporaryTracker(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        if (meta.getPersistentDataContainer().has(keyTempTracker, PersistentDataType.BYTE)) {
            return true;
        }
        if (meta.hasDisplayName()) {
            String name = meta.getDisplayName();
            return name.contains("TRACKER") || name.contains("RADAR");
        }
        return false;
    }

    private String buildBarTitle(Player hunter, Player target, String targetType, double remainingSec) {
        String direction = getDirection(hunter, target);
        String format = targetType.equalsIgnoreCase("MONARCH")
                ? plugin.getConfig().getString("tracking.bossbar.monarch-title", "&6&l👑 TRACKING MONARCH: &e%target% &8| &b%direction% &8| &c%time_left%s")
                : plugin.getConfig().getString("tracking.bossbar.bounty-title", "&c&l🎯 TRACKING BOUNTY: &e%target% &8| &b%direction% &8| &c%time_left%s");

        return MessageUtil.color(format
                .replace("%target%", target.getName())
                .replace("%direction%", direction)
                .replace("%time_left%", String.format("%.1f", remainingSec))
        );
    }

    public String getDirection(Player hunter, Player target) {
        if (!hunter.getWorld().equals(target.getWorld())) {
            return plugin.getConfig().getString("tracking.directions.other-dimension", "🌀 OTHER DIMENSION");
        }
        Location hLoc = hunter.getLocation();
        Location tLoc = target.getLocation();

        float playerYaw = hLoc.getYaw();
        while (playerYaw < -180) playerYaw += 360;
        while (playerYaw > 180) playerYaw -= 360;

        double dx = tLoc.getX() - hLoc.getX();
        double dz = tLoc.getZ() - hLoc.getZ();

        // Minecraft coordinate convention: Yaw 0 = +Z (South), 90 = -X (West), -90 = +X (East), 180/-180 = -Z (North)
        double angleToTarget = Math.toDegrees(Math.atan2(-dx, dz));

        double diff = angleToTarget - playerYaw;
        while (diff < -180) diff += 360;
        while (diff > 180) diff -= 360;

        if (diff >= -22.5 && diff < 22.5) return plugin.getConfig().getString("tracking.directions.ahead", "⬆ AHEAD");
        if (diff >= 22.5 && diff < 67.5) return plugin.getConfig().getString("tracking.directions.right-ahead", "↗ RIGHT-AHEAD");
        if (diff >= 67.5 && diff < 112.5) return plugin.getConfig().getString("tracking.directions.right", "➡ RIGHT");
        if (diff >= 112.5 && diff < 157.5) return plugin.getConfig().getString("tracking.directions.right-behind", "↘ RIGHT-BEHIND");
        if (diff >= -67.5 && diff < -22.5) return plugin.getConfig().getString("tracking.directions.left-ahead", "↖ LEFT-AHEAD");
        if (diff >= -112.5 && diff < -67.5) return plugin.getConfig().getString("tracking.directions.left", "⬅ LEFT");
        if (diff >= -157.5 && diff < -112.5) return plugin.getConfig().getString("tracking.directions.left-behind", "↙ LEFT-BEHIND");
        return plugin.getConfig().getString("tracking.directions.behind", "⬇ BEHIND");
    }

    private BarColor parseBarColor(String colorName) {
        if (colorName == null) return BarColor.RED;
        try {
            return BarColor.valueOf(colorName.toUpperCase());
        } catch (Exception e) {
            return BarColor.RED;
        }
    }

    private BarStyle parseBarStyle(String styleName) {
        if (styleName == null) return BarStyle.SOLID;
        try {
            return BarStyle.valueOf(styleName.toUpperCase());
        } catch (Exception e) {
            return BarStyle.SOLID;
        }
    }

    // --- FAIR PLAY SAFEGUARD LISTENERS ---

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDrop(PlayerDropItemEvent event) {
        if (isTemporaryTracker(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
            event.getItemDrop().remove();
            cleanTrackersFromInventory(event.getPlayer());
            String msg = plugin.getConfig().getString("tracking.messages.cannot-drop", "&c✖ Soulbound tracker cannot be dropped!");
            event.getPlayer().sendMessage(MessageUtil.color(msg));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        boolean currentIsTracker = isTemporaryTracker(current);
        boolean cursorIsTracker = isTemporaryTracker(cursor);

        if (!currentIsTracker && !cursorIsTracker) return;

        String msg = plugin.getConfig().getString("tracking.messages.cannot-store", "&c✖ Soulbound tracker cannot be placed in containers!");

        // Block moving into any external container (Chest, DoubleChest, Hopper, Shulker, Barrel, EnderChest, Furnace, etc.)
        if (event.getClickedInventory() != null && event.getClickedInventory().getType() != InventoryType.PLAYER && event.getClickedInventory().getType() != InventoryType.CRAFTING) {
            event.setCancelled(true);
            player.sendMessage(MessageUtil.color(msg));
            return;
        }

        // Block shift-clicking into top inventory if top inventory is not player inventory
        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            if (event.getView().getTopInventory().getType() != InventoryType.CRAFTING && event.getView().getTopInventory().getType() != InventoryType.PLAYER) {
                event.setCancelled(true);
                player.sendMessage(MessageUtil.color(msg));
                return;
            }
        }

        // Block dropping item from within inventory screen
        if (event.getAction() == InventoryAction.DROP_ALL_CURSOR || event.getAction() == InventoryAction.DROP_ONE_CURSOR
                || event.getAction() == InventoryAction.DROP_ALL_SLOT || event.getAction() == InventoryAction.DROP_ONE_SLOT) {
            event.setCancelled(true);
            String dropMsg = plugin.getConfig().getString("tracking.messages.cannot-drop", "&c✖ Soulbound tracker cannot be dropped!");
            player.sendMessage(MessageUtil.color(dropMsg));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (isTemporaryTracker(event.getOldCursor())) {
            if (event.getView().getTopInventory().getType() != InventoryType.CRAFTING && event.getView().getTopInventory().getType() != InventoryType.PLAYER) {
                for (int rawSlot : event.getRawSlots()) {
                    if (rawSlot < event.getView().getTopInventory().getSize()) {
                        event.setCancelled(true);
                        if (event.getWhoClicked() instanceof Player) {
                            String msg = plugin.getConfig().getString("tracking.messages.cannot-store", "&c✖ Soulbound tracker cannot be placed in containers!");
                            ((Player) event.getWhoClicked()).sendMessage(MessageUtil.color(msg));
                        }
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        stopTracking(player, false);

        // Remove tracker completely from death drops so it never drops on ground or gets looted
        event.getDrops().removeIf(this::isTemporaryTracker);

        // If dead player was a target of any active hunts, notify hunters
        for (Map.Entry<UUID, TrackingSession> entry : activeSessions.entrySet()) {
            if (entry.getValue().targetUuid.equals(player.getUniqueId())) {
                Player hunter = Bukkit.getPlayer(entry.getKey());
                if (hunter != null && hunter.isOnline()) {
                    hunter.sendMessage(MessageUtil.color("&a✦ [Tracker] Your target &e" + player.getName() + " &ahas been slain!"));
                    stopTracking(hunter, false);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        stopTracking(player, false);
        cleanTrackersFromInventory(player);

        // If disconnected player was target, alert hunters
        for (Map.Entry<UUID, TrackingSession> entry : activeSessions.entrySet()) {
            if (entry.getValue().targetUuid.equals(player.getUniqueId())) {
                Player hunter = Bukkit.getPlayer(entry.getKey());
                if (hunter != null && hunter.isOnline()) {
                    hunter.sendMessage(MessageUtil.color("&c✖ [Tracker] Target &e" + player.getName() + " &chas disconnected!"));
                    stopTracking(hunter, false);
                }
            }
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (isTemporaryTracker(event.getItem().getItemStack())) {
            event.setCancelled(true);
            event.getItem().remove();
        }
    }

    public void cleanup() {
        for (TrackingSession session : activeSessions.values()) {
            try {
                session.task.cancel();
            } catch (Exception ignored) {}
            try {
                session.bossBar.removeAll();
            } catch (Exception ignored) {}
        }
        activeSessions.clear();

        for (Player p : Bukkit.getOnlinePlayers()) {
            cleanTrackersFromInventory(p);
        }
    }

    public static class TrackingSession {
        private final UUID hunterUuid;
        private final UUID targetUuid;
        private final String targetType;
        private final BossBar bossBar;
        private final BukkitTask task;
        private final long expiresAt;

        public TrackingSession(UUID hunterUuid, UUID targetUuid, String targetType, BossBar bossBar, BukkitTask task, long expiresAt) {
            this.hunterUuid = hunterUuid;
            this.targetUuid = targetUuid;
            this.targetType = targetType;
            this.bossBar = bossBar;
            this.task = task;
            this.expiresAt = expiresAt;
        }

        public UUID getHunterUuid() { return hunterUuid; }
        public UUID getTargetUuid() { return targetUuid; }
        public String getTargetType() { return targetType; }
        public BossBar getBossBar() { return bossBar; }
        public BukkitTask getTask() { return task; }
        public long getExpiresAt() { return expiresAt; }
    }
}
