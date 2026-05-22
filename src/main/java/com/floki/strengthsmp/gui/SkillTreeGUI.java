package com.floki.strengthsmp.gui;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SkillTreeGUI implements Listener {

    private final StrengthSMP plugin;
    private final String title = MessageUtil.color("&0The Root Skill Tree");

    public SkillTreeGUI(StrengthSMP plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, title);

        // Fill background
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        inv.setItem(10, createSkillItem(player, "tank", Material.SHIELD, "&6&lTank Path", "&7Permanent &eResistance I", 10));
        inv.setItem(12, createSkillItem(player, "scout", Material.FEATHER, "&b&lScout Path", "&7Permanent &eSpeed I", 5));
        inv.setItem(14, createSkillItem(player, "berserker", Material.GOLDEN_PICKAXE, "&c&lBerserker Path", "&7Permanent &eHaste I", 5));
        inv.setItem(16, createSkillItem(player, "vitality", Material.ENCHANTED_GOLDEN_APPLE, "&d&lVitality Path", "&7Permanent &e+2 Max Hearts", 15));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);
    }

    private ItemStack createSkillItem(Player player, String id, Material mat, String name, String benefit, int cost) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtil.color(name));
        
        List<String> lore = new ArrayList<>();
        lore.add(MessageUtil.color("&8&m------------------"));
        lore.add(MessageUtil.color("&7Benefit: " + benefit));
        lore.add(MessageUtil.color("&7Cost: &c" + cost + " Strength"));
        lore.add("");
        
        if (plugin.getDataManager().hasSkill(player.getUniqueId(), id)) {
            lore.add(MessageUtil.color("&a&lUNLOCKED"));
            item.setType(Material.ENCHANTED_BOOK);
        } else {
            int current = plugin.getDataManager().getStrength(player.getUniqueId());
            if (current >= cost) {
                lore.add(MessageUtil.color("&eClick to Unlock"));
            } else {
                lore.add(MessageUtil.color("&cNot enough Strength"));
            }
        }
        lore.add(MessageUtil.color("&8&m------------------"));
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(title)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR || item.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        int slot = event.getRawSlot();
        String skillId = "";
        int cost = 0;

        switch (slot) {
            case 10 -> { skillId = "tank"; cost = 10; }
            case 12 -> { skillId = "scout"; cost = 5; }
            case 14 -> { skillId = "berserker"; cost = 5; }
            case 16 -> { skillId = "vitality"; cost = 15; }
            default -> { return; }
        }

        if (plugin.getDataManager().hasSkill(player.getUniqueId(), skillId)) {
            MessageUtil.send(player, "&cYou have already unlocked this path!");
            return;
        }

        int current = plugin.getDataManager().getStrength(player.getUniqueId());
        int min = plugin.getConfigManager().getMinStrength();
        if (current - cost < min) {
            MessageUtil.send(player, "&cYou cannot afford this! You must maintain at least &e" + min + " &cStrength.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // UNLOCK
        plugin.getStrengthService().removeStrength(player, cost);
        plugin.getDataManager().addSkill(player.getUniqueId(), skillId);
        
        player.closeInventory();
        MessageUtil.broadcast("&d&l" + player.getName() + " &7has awakened the &f" + skillId.toUpperCase() + " &7path!");
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.5f);
        
        // Apply immediately
        if (plugin.getSkillService() != null) plugin.getSkillService().applySkills(player);
    }
}
