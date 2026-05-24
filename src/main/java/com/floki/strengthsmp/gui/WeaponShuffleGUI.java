package com.floki.strengthsmp.gui;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.WeaponType;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import org.bukkit.Location;
import com.floki.strengthsmp.util.CompatUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A cinematic Crate-style GUI that shuffles through weapon types.
 */
public class WeaponShuffleGUI implements Listener {

    private final StrengthSMP plugin;
    private final Player player;
    private final Inventory inventory;
    private final List<WeaponType> rollList = new ArrayList<>();
    
    private boolean completed = false;
    private BukkitRunnable task;
    private WeaponType finalResult;
    private final int maxSteps;

    public WeaponShuffleGUI(StrengthSMP plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 27, MessageUtil.color("&0&lDestiny Awakening..."));
        this.maxSteps = 30 + new Random().nextInt(15);
        
        setupRollList();
        setupBorders();
        
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void setupRollList() {
        WeaponType current = plugin.getDataManager().getWeaponType(player.getUniqueId());
        Random random = new Random();
        WeaponType last = null;
        
        for (int i = 0; i < 150; i++) {
            // For the winning index (which is maxSteps + 3), force it to be a class that is NOT current!
            if (i == maxSteps + 3) {
                List<WeaponType> pool = new ArrayList<>();
                for (WeaponType type : WeaponType.values()) {
                    if (type != current) {
                        pool.add(type);
                    }
                }
                if (pool.isEmpty()) pool.addAll(List.of(WeaponType.values()));
                
                // Ensure it doesn't repeat the immediately adjacent scrolling item if possible
                if (last != null && pool.contains(last) && pool.size() > 1) {
                    pool.remove(last);
                }
                WeaponType win = pool.get(random.nextInt(pool.size()));
                rollList.add(win);
                last = win;
                continue;
            }

            // Otherwise, pick any WeaponType from all 6 classes (so all classes can scroll past!),
            // but ensure no adjacent duplicates in the list!
            List<WeaponType> available = new ArrayList<>(List.of(WeaponType.values()));
            if (last != null) {
                available.remove(last);
            }
            WeaponType next = available.get(random.nextInt(available.size()));
            rollList.add(next);
            last = next;
        }
    }

    private void setupBorders() {
        ItemStack black = createNamedItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack marker = createNamedItem(Material.YELLOW_STAINED_GLASS_PANE, "&e&l▲ YOUR DESTINY ▼");
        
        for (int i = 0; i < 9; i++) inventory.setItem(i, black);
        for (int i = 18; i < 27; i++) inventory.setItem(i, black);
        
        inventory.setItem(4, marker);
        inventory.setItem(22, marker);
    }

    public void open() {
        player.openInventory(inventory);
        startShuffle();
    }

    private void startShuffle() {
        this.task = new BukkitRunnable() {
            int step = 0;
            int delay = 1;
            int ticksProcessed = 0;

            @Override
            public void run() {
                ticksProcessed++;
                if (ticksProcessed < delay) return;
                ticksProcessed = 0;

                if (step >= maxSteps) {
                    finalizeRoll();
                    this.cancel();
                    return;
                }

                // Scroll the items
                for (int i = 0; i < 9; i++) {
                    WeaponType type = rollList.get(step + i);
                    inventory.setItem(9 + i, getIcon(type));
                    if (i == 4) finalResult = type; // The one at index 4 is the center (slot 13)
                }

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f + (step * 0.01f));
                
                // Spawn premium rotating double helix particles around the player in real-time
                Location baseLoc = player.getLocation();
                double theta1 = step * 0.25;
                double r = 0.85; // snug radius to surround player
                
                double x1 = r * Math.cos(theta1);
                double z1 = r * Math.sin(theta1);
                double y1 = (step * 0.08) % 2.0; // rising motion
                Location p1 = baseLoc.clone().add(x1, y1, z1);
                CompatUtil.spawnParticle(player.getWorld(), "WITCH", p1, 1, 0, 0, 0, 0);
                CompatUtil.spawnParticle(player.getWorld(), "PORTAL", p1, 1, 0, 0, 0, 0);

                double theta2 = theta1 + Math.PI;
                double x2 = r * Math.cos(theta2);
                double z2 = r * Math.sin(theta2);
                double y2 = ((step * 0.08) + 1.0) % 2.0; // opposite offset
                Location p2 = baseLoc.clone().add(x2, y2, z2);
                CompatUtil.spawnParticle(player.getWorld(), "WITCH", p2, 1, 0, 0, 0, 0);
                CompatUtil.spawnParticle(player.getWorld(), "PORTAL", p2, 1, 0, 0, 0, 0);

                step++;
                
                // Decelerate
                if (step > maxSteps * 0.9) delay = 5;
                else if (step > maxSteps * 0.8) delay = 4;
                else if (step > maxSteps * 0.7) delay = 3;
                else if (step > maxSteps * 0.5) delay = 2;
            }
        };
        task.runTaskTimer(plugin, 0L, 1L);
    }

    private void finalizeRoll() {
        if (completed) return;
        completed = true;
        
        if (finalResult == null) {
            WeaponType current = plugin.getDataManager().getWeaponType(player.getUniqueId());
            List<WeaponType> pool = new ArrayList<>();
            for (WeaponType type : WeaponType.values()) {
                if (type != current) {
                    pool.add(type);
                }
            }
            if (pool.isEmpty()) {
                pool.addAll(List.of(WeaponType.values()));
            }
            finalResult = pool.get(new Random().nextInt(pool.size()));
        }
        
        // Visual flair
        ItemStack winner = getIcon(finalResult);
        ItemMeta meta = winner.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtil.color("&6&l⭐ " + finalResult.getDisplayName().toUpperCase() + " ⭐"));
            winner.setItemMeta(meta);
        }
        inventory.setItem(13, winner);
        
        // Initial massive sound impact
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.5f);
        
        // 4-Second Animated Infusion Beam and Ring Effect
        new BukkitRunnable() {
            int time = 0;
            @Override
            public void run() {
                if (time >= 80 || !player.isOnline()) {
                    this.cancel();
                    return;
                }
                
                Location base = player.getLocation();
                
                // 1. Tornado Beam of Light: spirals down from 8 blocks above directly into player
                double angleOffset = time * 0.35;
                double currentY = 8.0 - (time * 0.12);
                if (currentY < 0) currentY = 0;
                
                double radius = 1.6 * (currentY / 8.0) + 0.35; // contracts tightly as it gets lower!
                double x1 = radius * Math.cos(angleOffset);
                double z1 = radius * Math.sin(angleOffset);
                Location beamPt1 = base.clone().add(x1, currentY, z1);
                
                CompatUtil.spawnParticle(player.getWorld(), "FIREWORKS_SPARK", beamPt1, 2, 0.02, 0.02, 0.02, 0.01);
                CompatUtil.spawnParticle(player.getWorld(), "END_ROD", beamPt1, 1, 0, 0, 0, 0);
                
                // Double Helix opposite helix beam
                double x2 = radius * Math.cos(angleOffset + Math.PI);
                double z2 = radius * Math.sin(angleOffset + Math.PI);
                Location beamPt2 = base.clone().add(x2, currentY, z2);
                
                CompatUtil.spawnParticle(player.getWorld(), "FIREWORKS_SPARK", beamPt2, 2, 0.02, 0.02, 0.02, 0.01);
                CompatUtil.spawnParticle(player.getWorld(), "END_ROD", beamPt2, 1, 0, 0, 0, 0);

                // 2. Contracting horizontal circles of light infusing into player's chest
                double ringRadius = 3.5 - ((time * 0.15) % 3.5);
                for (int i = 0; i < 18; i++) {
                    double theta = i * (2.0 * Math.PI / 18.0);
                    double rx = ringRadius * Math.cos(theta);
                    double rz = ringRadius * Math.sin(theta);
                    Location ringPt = base.clone().add(rx, 1.0, rz);
                    
                    CompatUtil.spawnParticle(player.getWorld(), "END_ROD", ringPt, 1, 0, 0, 0, 0);
                    if (time % 4 == 0) {
                        CompatUtil.spawnParticle(player.getWorld(), "FIREWORKS_SPARK", ringPt, 1, 0.01, 0.01, 0.01, 0.02);
                    }
                }
                
                // Rising chest rings
                double risingY = (time * 0.06) % 2.0;
                double risingRadius = 0.85;
                for (int i = 0; i < 12; i++) {
                    double theta = i * (2.0 * Math.PI / 12.0);
                    double rx = risingRadius * Math.cos(theta);
                    double rz = risingRadius * Math.sin(theta);
                    Location ringPt = base.clone().add(rx, risingY, rz);
                    CompatUtil.spawnParticle(player.getWorld(), "CRIT_MAGIC", ringPt, 1, 0, 0, 0, 0.01);
                }

                // Melodic ticking sound
                if (time % 5 == 0) {
                    player.getWorld().playSound(base, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.6f, 1.0f + (time * 0.006f));
                }

                time++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        // Title Reveal
        String weaponName = finalResult.getDisplayName();
        player.sendTitle("§6§lAWAKENED", "§fYou are a §e§l" + weaponName.toUpperCase(), 10, 60, 20);

        // Final assignment
        plugin.getDataManager().setWeaponType(player.getUniqueId(), finalResult);
        plugin.updateDisplay(player);
        
        MessageUtil.send(player, "strength.reroll-success", "type", weaponName);
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.closeInventory();
            HandlerList.unregisterAll(this);
        }, 80L);
    }

    private ItemStack getIcon(WeaponType type) {
        Material mat;
        switch (type) {
            case SWORD:    mat = Material.NETHERITE_SWORD; break;
            case AXE:      mat = Material.NETHERITE_AXE; break;
            case TRIDENT:  mat = Material.TRIDENT; break;
            case SHIELD:   mat = Material.SHIELD; break;
            case BOW:      mat = Material.BOW; break;
            case CROSSBOW: mat = Material.CROSSBOW; break;
            default:       mat = Material.PAPER;
        }
        return createNamedItem(mat, "&f" + type.getDisplayName());
    }

    private ItemStack createNamedItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtil.color(name));
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().equals(inventory)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory) && !completed) {
            if (task != null) task.cancel();
            
            // Finalize instantly if closed early to prevent bypassing
            finalizeRoll();
        }
    }
}
