/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.HandlerList
 *  org.bukkit.event.Listener
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.inventory.InventoryCloseEvent
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.ItemFlag
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 */
package com.floki.strengthsmp.gui;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.WeaponType;
import com.floki.strengthsmp.util.CompatUtil;
import com.floki.strengthsmp.util.MessageUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class WeaponShuffleGUI
implements Listener {
    private final StrengthSMP plugin;
    private final Player player;
    private final Inventory inventory;
    private final List<WeaponType> rollList = new ArrayList<WeaponType>();
    private boolean completed = false;
    private BukkitRunnable task;
    private WeaponType finalResult;
    private final int maxSteps;

    public WeaponShuffleGUI(StrengthSMP plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, (int)27, (String)MessageUtil.color("&0&lDestiny Awakening..."));
        this.maxSteps = 30 + new Random().nextInt(15);
        this.setupRollList();
        this.setupBorders();
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)plugin);
    }

    private void setupRollList() {
        WeaponType current = this.plugin.getDataManager().getWeaponType(this.player.getUniqueId());
        Random random = new Random();
        WeaponType last = null;
        for (int i = 0; i < 150; ++i) {
            if (i == this.maxSteps + 3) {
                ArrayList<WeaponType> pool = new ArrayList<WeaponType>();
                for (WeaponType type : WeaponType.values()) {
                    if (type == current) continue;
                    pool.add(type);
                }
                if (pool.isEmpty()) {
                    pool.addAll(List.of(WeaponType.values()));
                }
                if (last != null && pool.contains((Object)last) && pool.size() > 1) {
                    pool.remove((Object)last);
                }
                WeaponType win = (WeaponType)((Object)pool.get(random.nextInt(pool.size())));
                this.rollList.add(win);
                last = win;
                continue;
            }
            ArrayList<WeaponType> available = new ArrayList<WeaponType>(List.of(WeaponType.values()));
            if (last != null) {
                available.remove((Object)last);
            }
            WeaponType next = (WeaponType)((Object)available.get(random.nextInt(available.size())));
            this.rollList.add(next);
            last = next;
        }
    }

    private void setupBorders() {
        int i;
        ItemStack black = this.createNamedItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack marker = this.createNamedItem(Material.YELLOW_STAINED_GLASS_PANE, "&e&l\u25b2 YOUR DESTINY \u25bc");
        for (i = 0; i < 9; ++i) {
            this.inventory.setItem(i, black);
        }
        for (i = 18; i < 27; ++i) {
            this.inventory.setItem(i, black);
        }
        this.inventory.setItem(4, marker);
        this.inventory.setItem(22, marker);
    }

    public void open() {
        this.player.openInventory(this.inventory);
        this.startShuffle();
    }

    private void startShuffle() {
        this.task = new BukkitRunnable(){
            int step = 0;
            int delay = 1;
            int ticksProcessed = 0;

            public void run() {
                ++this.ticksProcessed;
                if (this.ticksProcessed < this.delay) {
                    return;
                }
                this.ticksProcessed = 0;
                if (this.step >= WeaponShuffleGUI.this.maxSteps) {
                    WeaponShuffleGUI.this.finalizeRoll();
                    this.cancel();
                    return;
                }
                for (int i = 0; i < 9; ++i) {
                    WeaponType type = WeaponShuffleGUI.this.rollList.get(this.step + i);
                    WeaponShuffleGUI.this.inventory.setItem(9 + i, WeaponShuffleGUI.this.getIcon(type));
                    if (i != 4) continue;
                    WeaponShuffleGUI.this.finalResult = type;
                }
                WeaponShuffleGUI.this.player.playSound(WeaponShuffleGUI.this.player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f + (float)this.step * 0.01f);
                Location baseLoc = WeaponShuffleGUI.this.player.getLocation();
                double theta1 = (double)this.step * 0.25;
                double r = 0.85;
                double x1 = r * Math.cos(theta1);
                double z1 = r * Math.sin(theta1);
                double y1 = (double)this.step * 0.08 % 2.0;
                Location p1 = baseLoc.clone().add(x1, y1, z1);
                CompatUtil.spawnParticle(WeaponShuffleGUI.this.player.getWorld(), "WITCH", p1, 1, 0.0, 0.0, 0.0, 0.0);
                CompatUtil.spawnParticle(WeaponShuffleGUI.this.player.getWorld(), "PORTAL", p1, 1, 0.0, 0.0, 0.0, 0.0);
                double theta2 = theta1 + Math.PI;
                double x2 = r * Math.cos(theta2);
                double z2 = r * Math.sin(theta2);
                double y2 = ((double)this.step * 0.08 + 1.0) % 2.0;
                Location p2 = baseLoc.clone().add(x2, y2, z2);
                CompatUtil.spawnParticle(WeaponShuffleGUI.this.player.getWorld(), "WITCH", p2, 1, 0.0, 0.0, 0.0, 0.0);
                CompatUtil.spawnParticle(WeaponShuffleGUI.this.player.getWorld(), "PORTAL", p2, 1, 0.0, 0.0, 0.0, 0.0);
                ++this.step;
                if ((double)this.step > (double)WeaponShuffleGUI.this.maxSteps * 0.9) {
                    this.delay = 5;
                } else if ((double)this.step > (double)WeaponShuffleGUI.this.maxSteps * 0.8) {
                    this.delay = 4;
                } else if ((double)this.step > (double)WeaponShuffleGUI.this.maxSteps * 0.7) {
                    this.delay = 3;
                } else if ((double)this.step > (double)WeaponShuffleGUI.this.maxSteps * 0.5) {
                    this.delay = 2;
                }
            }
        };
        this.task.runTaskTimer((Plugin)this.plugin, 0L, 1L);
    }

    private void finalizeRoll() {
        ItemStack winner;
        ItemMeta meta;
        if (this.completed) {
            return;
        }
        this.completed = true;
        if (this.finalResult == null) {
            WeaponType current = this.plugin.getDataManager().getWeaponType(this.player.getUniqueId());
            ArrayList<WeaponType> pool = new ArrayList<WeaponType>();
            for (WeaponType type : WeaponType.values()) {
                if (type == current) continue;
                pool.add(type);
            }
            if (pool.isEmpty()) {
                pool.addAll(List.of(WeaponType.values()));
            }
            this.finalResult = (WeaponType)((Object)pool.get(new Random().nextInt(pool.size())));
        }
        if ((meta = (winner = this.getIcon(this.finalResult)).getItemMeta()) != null) {
            meta.setDisplayName(MessageUtil.color("&6&l\u2b50 " + this.finalResult.getDisplayName().toUpperCase() + " \u2b50"));
            winner.setItemMeta(meta);
        }
        this.inventory.setItem(13, winner);
        this.player.playSound(this.player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        this.player.playSound(this.player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.5f);
        new BukkitRunnable(){
            int time = 0;

            public void run() {
                if (this.time >= 80 || !WeaponShuffleGUI.this.player.isOnline()) {
                    this.cancel();
                    return;
                }
                Location base = WeaponShuffleGUI.this.player.getLocation();
                double angleOffset = (double)this.time * 0.35;
                double currentY = 8.0 - (double)this.time * 0.12;
                if (currentY < 0.0) {
                    currentY = 0.0;
                }
                double radius = 1.6 * (currentY / 8.0) + 0.35;
                double x1 = radius * Math.cos(angleOffset);
                double z1 = radius * Math.sin(angleOffset);
                Location beamPt1 = base.clone().add(x1, currentY, z1);
                CompatUtil.spawnParticle(WeaponShuffleGUI.this.player.getWorld(), "FIREWORKS_SPARK", beamPt1, 2, 0.02, 0.02, 0.02, 0.01);
                CompatUtil.spawnParticle(WeaponShuffleGUI.this.player.getWorld(), "END_ROD", beamPt1, 1, 0.0, 0.0, 0.0, 0.0);
                double x2 = radius * Math.cos(angleOffset + Math.PI);
                double z2 = radius * Math.sin(angleOffset + Math.PI);
                Location beamPt2 = base.clone().add(x2, currentY, z2);
                CompatUtil.spawnParticle(WeaponShuffleGUI.this.player.getWorld(), "FIREWORKS_SPARK", beamPt2, 2, 0.02, 0.02, 0.02, 0.01);
                CompatUtil.spawnParticle(WeaponShuffleGUI.this.player.getWorld(), "END_ROD", beamPt2, 1, 0.0, 0.0, 0.0, 0.0);
                double ringRadius = 3.5 - (double)this.time * 0.15 % 3.5;
                for (int i = 0; i < 18; ++i) {
                    double theta = (double)i * 0.3490658503988659;
                    double rx = ringRadius * Math.cos(theta);
                    double rz = ringRadius * Math.sin(theta);
                    Location ringPt = base.clone().add(rx, 1.0, rz);
                    CompatUtil.spawnParticle(WeaponShuffleGUI.this.player.getWorld(), "END_ROD", ringPt, 1, 0.0, 0.0, 0.0, 0.0);
                    if (this.time % 4 != 0) continue;
                    CompatUtil.spawnParticle(WeaponShuffleGUI.this.player.getWorld(), "FIREWORKS_SPARK", ringPt, 1, 0.01, 0.01, 0.01, 0.02);
                }
                double risingY = (double)this.time * 0.06 % 2.0;
                double risingRadius = 0.85;
                for (int i = 0; i < 12; ++i) {
                    double theta = (double)i * 0.5235987755982988;
                    double rx = risingRadius * Math.cos(theta);
                    double rz = risingRadius * Math.sin(theta);
                    Location ringPt = base.clone().add(rx, risingY, rz);
                    CompatUtil.spawnParticle(WeaponShuffleGUI.this.player.getWorld(), "CRIT_MAGIC", ringPt, 1, 0.0, 0.0, 0.0, 0.01);
                }
                if (this.time % 5 == 0) {
                    WeaponShuffleGUI.this.player.getWorld().playSound(base, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.6f, 1.0f + (float)this.time * 0.006f);
                }
                ++this.time;
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 1L);
        String weaponName = this.finalResult.getDisplayName();
        this.player.sendTitle("\u00a76\u00a7lAWAKENED", "\u00a7fYou are a \u00a7e\u00a7l" + weaponName.toUpperCase(), 10, 60, 20);
        this.plugin.getDataManager().setWeaponType(this.player.getUniqueId(), this.finalResult);
        this.plugin.updateDisplay(this.player);
        MessageUtil.send((CommandSender)this.player, "strength.reroll-success", "type", weaponName);
        Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            this.player.closeInventory();
            HandlerList.unregisterAll((Listener)this);
        }, 80L);
    }

    private ItemStack getIcon(WeaponType type) {
        return this.createNamedItem(switch (type) {
            case SWORD -> Material.NETHERITE_SWORD;
            case AXE -> Material.NETHERITE_AXE;
            case TRIDENT -> Material.TRIDENT;
            case SHIELD -> Material.SHIELD;
            case BOW -> Material.BOW;
            case CROSSBOW -> Material.CROSSBOW;
            default -> Material.PAPER;
        }, "&f" + type.getDisplayName());
    }

    private ItemStack createNamedItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtil.color(name));
            meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().equals((Object)this.inventory)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().equals((Object)this.inventory) && !this.completed) {
            if (this.task != null) {
                this.task.cancel();
            }
            this.finalizeRoll();
        }
    }
}

