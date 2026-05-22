package com.floki.strengthsmp.services;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.WeaponType;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.UUID;

/**
 * Handles visual flair like bounty glows and the Stats Ledger book.
 */
public class AestheticService {

    private final StrengthSMP plugin;
    private final Scoreboard scoreboard;

    public AestheticService(StrengthSMP plugin) {
        this.plugin = plugin;
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        setupTeams();
    }

    private void setupTeams() {
        createTeam("Bounty_Low", ChatColor.GREEN);
        createTeam("Bounty_Med", ChatColor.GOLD);
        createTeam("Bounty_High", ChatColor.RED);
    }

    private void createTeam(String name, ChatColor color) {
        Team team = scoreboard.getTeam(name);
        if (team == null) {
            team = scoreboard.registerNewTeam(name);
        }
        team.setColor(color);
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
    }

    public void updateBountyGlow(Player player, int bounty) {
        // Clear old teams
        scoreboard.getTeams().forEach(t -> {
            if (t.getName().startsWith("Bounty_")) {
                t.removeEntry(player.getName());
            }
        });

        // If they are monarch, MonarchService handles the glow (RED)
        if (plugin.getMonarchService().getCurrentMonarchUUID() != null && 
            plugin.getMonarchService().getCurrentMonarchUUID().equals(player.getUniqueId())) {
            return; 
        }

        if (bounty <= 0) {
            player.setGlowing(false);
            return;
        }

        String teamName;
        if (bounty >= 6) teamName = "Bounty_High";
        else if (bounty >= 3) teamName = "Bounty_Med";
        else teamName = "Bounty_Low";

        Team team = scoreboard.getTeam(teamName);
        if (team != null) {
            team.addEntry(player.getName());
            player.setGlowing(true);
        }
    }

    public void startAestheticTasks() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                int strength = plugin.getDataManager().getStrength(p.getUniqueId());
                // Soul Aura for high strength (5+)
                if (strength >= 5) {
                    playSoulAura(p);
                }
                
                // Monarch Subtle Glow (not the scoreboard one, actual particles)
                UUID monarchUUID = plugin.getMonarchService().getCurrentMonarchUUID();
                if (monarchUUID != null && monarchUUID.equals(p.getUniqueId())) {
                    playMonarchAura(p);
                }
            }
        }, 20L, 20L); // Every second
    }

    private void playSoulAura(Player p) {
        // Subtle soul particles emerging from the chest area
        p.getWorld().spawnParticle(org.bukkit.Particle.SOUL, p.getLocation().add(0, 1.2, 0), 1, 0.2, 0.2, 0.2, 0.02);
    }

    private void playMonarchAura(Player p) {
        // Golden sparks for the monarch
        com.floki.strengthsmp.util.CompatUtil.spawnParticle(p.getWorld(), "WAX_OFF", p.getLocation().add(0, 2.2, 0), 2, 0.1, 0.1, 0.1, 0.05);
    }

    public void openStatsLedger(Player viewer, Player target) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        
        if (meta == null) return;

        UUID uuid = target.getUniqueId();
        int strength = plugin.getDataManager().getStrength(uuid);
        WeaponType type = plugin.getDataManager().getWeapon(uuid);
        int kills = plugin.getDataManager().getKills(uuid);
        int deaths = plugin.getDataManager().getDeaths(uuid);
        int bounty = plugin.getDataManager().getBounty(uuid);
        
        String className = (type != null) ? type.getDisplayName() : "Neutral";
        String classIcon = (type != null) ? type.getIcon() : "⚪";

        StringBuilder page = new StringBuilder();
        page.append(ChatColor.DARK_RED + "" + ChatColor.BOLD + "  LEDGER OF LEGENDS\n");
        page.append(ChatColor.BLACK + "-------------------\n");
        page.append(ChatColor.DARK_GRAY + "Warrior: " + ChatColor.BLUE + target.getName() + "\n\n");
        
        page.append(ChatColor.BLACK + "Strength: " + ChatColor.DARK_RED + strength + " STR\n");
        page.append(ChatColor.BLACK + "Class: " + classIcon + " " + ChatColor.DARK_PURPLE + className + "\n");
        page.append(ChatColor.BLACK + "Bounty: " + ChatColor.GOLD + bounty + " STR\n\n");
        
        page.append(ChatColor.BLACK + "Kills: " + ChatColor.DARK_GREEN + kills + "\n");
        page.append(ChatColor.BLACK + "Deaths: " + ChatColor.RED + deaths + "\n");
        page.append(ChatColor.BLACK + "K/D: " + ChatColor.DARK_GRAY + (deaths == 0 ? kills : String.format("%.2f", (double)kills/deaths)) + "\n");
        
        page.append(ChatColor.BLACK + "-------------------\n");
        page.append(ChatColor.ITALIC + "Destiny is forged in blood.");

        meta.setPages(page.toString());
        meta.setTitle("Ledger of Legends");
        meta.setAuthor("Floki SMP");
        
        book.setItemMeta(meta);
        viewer.openBook(book);
        
        viewer.playSound(viewer.getLocation(), org.bukkit.Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
    }
}
