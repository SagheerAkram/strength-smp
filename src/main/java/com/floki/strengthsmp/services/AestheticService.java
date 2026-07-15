/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.Particle
 *  org.bukkit.Sound
 *  org.bukkit.World
 *  org.bukkit.entity.Item
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.BookMeta
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scoreboard.Scoreboard
 *  org.bukkit.scoreboard.Team
 *  org.bukkit.scoreboard.Team$Option
 *  org.bukkit.scoreboard.Team$OptionStatus
 */
package com.floki.strengthsmp.services;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.WeaponType;
import com.floki.strengthsmp.util.CompatUtil;
import com.floki.strengthsmp.util.ItemFactory;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class AestheticService {
    private final StrengthSMP plugin;
    private final Scoreboard scoreboard;

    public AestheticService(StrengthSMP plugin) {
        this.plugin = plugin;
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        this.setupTeams();
    }

    private void setupTeams() {
        this.createTeam("Bounty_Low", ChatColor.GREEN);
        this.createTeam("Bounty_Med", ChatColor.GOLD);
        this.createTeam("Bounty_High", ChatColor.RED);
    }

    private void createTeam(String name, ChatColor color) {
        Team team = this.scoreboard.getTeam(name);
        if (team == null) {
            team = this.scoreboard.registerNewTeam(name);
        }
        team.setColor(color);
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
    }

    public void updateBountyGlow(Player player, int bounty) {
        this.scoreboard.getTeams().forEach(t -> {
            if (t.getName().startsWith("Bounty_")) {
                t.removeEntry(player.getName());
            }
        });
        if (this.plugin.getMonarchService().getCurrentMonarchUUID() != null && this.plugin.getMonarchService().getCurrentMonarchUUID().equals(player.getUniqueId())) {
            return;
        }
        if (bounty <= 0) {
            player.setGlowing(false);
            return;
        }
        String teamName = bounty >= 6 ? "Bounty_High" : (bounty >= 3 ? "Bounty_Med" : "Bounty_Low");
        Team team = this.scoreboard.getTeam(teamName);
        if (team != null) {
            team.addEntry(player.getName());
            player.setGlowing(true);
        }
    }

    public void startAestheticTasks() {
        Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                UUID monarchUUID;
                int strength = this.plugin.getDataManager().getStrength(p.getUniqueId());
                if (strength >= 5) {
                    this.playSoulAura(p);
                }
                if ((monarchUUID = this.plugin.getMonarchService().getCurrentMonarchUUID()) != null && monarchUUID.equals(p.getUniqueId())) {
                    this.playMonarchAura(p);
                }
                if (!ItemFactory.isRerollItem(p.getInventory().getItemInMainHand()) && !ItemFactory.isRerollItem(p.getInventory().getItemInOffHand())) continue;
                this.playRerollBookAura(p);
            }
            for (World world : Bukkit.getWorlds()) {
                if (world.getPlayers().isEmpty()) continue;
                for (Item itemEntity : world.getEntitiesByClass(Item.class)) {
                    if (!ItemFactory.isRerollItem(itemEntity.getItemStack())) continue;
                    this.playDroppedRerollBookAura(itemEntity);
                }
            }
        }, 10L, 10L);
    }

    private void playSoulAura(Player p) {
        p.getWorld().spawnParticle(Particle.SOUL, p.getLocation().add(0.0, 1.2, 0.0), 1, 0.2, 0.2, 0.2, 0.02);
    }

    private void playMonarchAura(Player p) {
        CompatUtil.spawnParticle(p.getWorld(), "WAX_OFF", p.getLocation().add(0.0, 2.2, 0.0), 2, 0.1, 0.1, 0.1, 0.05);
    }

    private void playRerollBookAura(Player p) {
        Location loc = p.getLocation().add(0.0, 1.0, 0.0);
        CompatUtil.spawnParticle(p.getWorld(), "ENCHANTMENT_TABLE", loc, 2, 0.4, 0.4, 0.4, 0.1);
        CompatUtil.spawnParticle(p.getWorld(), "SPELL_WITCH", loc, 1, 0.3, 0.3, 0.3, 0.02);
    }

    private void playDroppedRerollBookAura(Item itemEntity) {
        Location loc = itemEntity.getLocation().add(0.0, 0.2, 0.0);
        CompatUtil.spawnParticle(itemEntity.getWorld(), "ENCHANTMENT_TABLE", loc, 1, 0.25, 0.25, 0.25, 0.05);
        CompatUtil.spawnParticle(itemEntity.getWorld(), "SPELL_WITCH", loc, 1, 0.15, 0.15, 0.15, 0.01);
    }

    public void openStatsLedger(Player viewer, Player target) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta)book.getItemMeta();
        if (meta == null) {
            return;
        }
        UUID uuid = target.getUniqueId();
        int strength = this.plugin.getDataManager().getStrength(uuid);
        WeaponType type = this.plugin.getDataManager().getWeapon(uuid);
        int kills = this.plugin.getDataManager().getKills(uuid);
        int deaths = this.plugin.getDataManager().getDeaths(uuid);
        int bounty = this.plugin.getDataManager().getBounty(uuid);
        String className = type != null ? type.getDisplayName() : "Neutral";
        String classIcon = type != null ? type.getIcon() : "\u26aa";
        StringBuilder page = new StringBuilder();
        page.append(String.valueOf(ChatColor.DARK_RED) + String.valueOf(ChatColor.BOLD) + "  LEDGER OF LEGENDS\n");
        page.append(String.valueOf(ChatColor.BLACK) + "-------------------\n");
        page.append(String.valueOf(ChatColor.DARK_GRAY) + "Warrior: " + String.valueOf(ChatColor.BLUE) + target.getName() + "\n\n");
        page.append(String.valueOf(ChatColor.BLACK) + "Strength: " + String.valueOf(ChatColor.DARK_RED) + strength + " STR\n");
        page.append(String.valueOf(ChatColor.BLACK) + "Class: " + classIcon + " " + String.valueOf(ChatColor.DARK_PURPLE) + className + "\n");
        page.append(String.valueOf(ChatColor.BLACK) + "Bounty: " + String.valueOf(ChatColor.GOLD) + bounty + " STR\n\n");
        page.append(String.valueOf(ChatColor.BLACK) + "Kills: " + String.valueOf(ChatColor.DARK_GREEN) + kills + "\n");
        page.append(String.valueOf(ChatColor.BLACK) + "Deaths: " + String.valueOf(ChatColor.RED) + deaths + "\n");
        page.append(String.valueOf(ChatColor.BLACK) + "K/D: " + String.valueOf(ChatColor.DARK_GRAY) + String.valueOf(deaths == 0 ? Integer.valueOf(kills) : String.format("%.2f", (double)kills / (double)deaths)) + "\n");
        page.append(String.valueOf(ChatColor.BLACK) + "-------------------\n");
        page.append(String.valueOf(ChatColor.ITALIC) + "Destiny is forged in blood.");
        meta.setPages(new String[]{page.toString()});
        meta.setTitle("Ledger of Legends");
        meta.setAuthor("Strength SMP");
        book.setItemMeta((ItemMeta)meta);
        viewer.openBook(book);
        viewer.playSound(viewer.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
    }
}

