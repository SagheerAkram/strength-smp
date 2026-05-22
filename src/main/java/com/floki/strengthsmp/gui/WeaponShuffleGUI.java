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

    public WeaponShuffleGUI(StrengthSMP plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 27, MessageUtil.color("&0&lDestiny Awakening..."));
        
        setupRollList();
        setupBorders();
        
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void setupRollList() {
        List<WeaponType> types = new ArrayList<>(List.of(WeaponType.values()));
        // Create a long list to shuffle through (at least 100 entries for safety)
        for (int i = 0; i < 20; i++) {
            Collections.shuffle(types);
            rollList.addAll(types);
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
            int maxSteps = 30 + new Random().nextInt(15); // Duration
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
        
        if (finalResult == null) finalResult = WeaponType.getRandomWeapon();
        
        // Visual flair
        ItemStack winner = getIcon(finalResult);
        ItemMeta meta = winner.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtil.color("&6&l⭐ " + finalResult.getDisplayName().toUpperCase() + " ⭐"));
            winner.setItemMeta(meta);
        }
        inventory.setItem(13, winner);
        
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.5f);
        player.getWorld().spawnParticle(org.bukkit.Particle.TOTEM, player.getLocation().add(0, 1, 0), 100, 0.5, 0.5, 0.5, 0.2);
        player.getWorld().spawnParticle(org.bukkit.Particle.FIREWORKS_SPARK, player.getLocation().add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0.1);
        
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
