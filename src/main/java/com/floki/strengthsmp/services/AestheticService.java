package com.floki.strengthsmp.services;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.WeaponType;
import com.floki.strengthsmp.util.CompatUtil;
import com.floki.strengthsmp.util.ItemFactory;
import com.floki.strengthsmp.util.MessageUtil;
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
        CompatUtil.spawnParticle(p.getWorld(), "SOUL", p.getLocation().add(0.0, 1.2, 0.0), 1, 0.2, 0.2, 0.2, 0.02);
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
        String className = type != null ? type.getDisplayName() : "Unassigned";
        String classIcon = type != null ? type.getIcon() : "\u26aa";
        
        String title = MessageUtil.get("ledger.title", "  LEDGER OF LEGENDS");
        String header = MessageUtil.get("ledger.header", "-------------------");
        String warriorLabel = MessageUtil.get("ledger.warrior", "Warrior: ");
        String strLabel = MessageUtil.get("ledger.strength", "Strength: ");
        String classLabel = MessageUtil.get("ledger.class", "Class: ");
        String bountyLabel = MessageUtil.get("ledger.bounty", "Bounty: ");
        String killsLabel = MessageUtil.get("ledger.kills", "Kills: ");
        String deathsLabel = MessageUtil.get("ledger.deaths", "Deaths: ");
        String kdLabel = MessageUtil.get("ledger.kd", "K/D: ");
        String footer = MessageUtil.get("ledger.footer", "Destiny is forged in blood.");

        StringBuilder page = new StringBuilder();
        page.append(ChatColor.DARK_RED).append(ChatColor.BOLD).append(title).append("\n");
        page.append(ChatColor.BLACK).append(header).append("\n");
        page.append(ChatColor.DARK_GRAY).append(warriorLabel).append(ChatColor.BLUE).append(target.getName()).append("\n\n");
        page.append(ChatColor.BLACK).append(strLabel).append(ChatColor.DARK_RED).append(strength).append(" STR\n");
        page.append(ChatColor.BLACK).append(classLabel).append(classIcon).append(" ").append(ChatColor.DARK_PURPLE).append(className).append("\n");
        page.append(ChatColor.BLACK).append(bountyLabel).append(ChatColor.GOLD).append(bounty).append(" STR\n\n");
        page.append(ChatColor.BLACK).append(killsLabel).append(ChatColor.DARK_GREEN).append(kills).append("\n");
        page.append(ChatColor.BLACK).append(deathsLabel).append(ChatColor.RED).append(deaths).append("\n");
        page.append(ChatColor.BLACK).append(kdLabel).append(ChatColor.DARK_GRAY).append(deaths == 0 ? kills : String.format("%.2f", (double)kills / (double)deaths)).append("\n");
        page.append(ChatColor.BLACK).append(header).append("\n");
        page.append(ChatColor.ITALIC).append(footer);
        meta.setPages(new String[]{page.toString()});
        meta.setTitle(ChatColor.stripColor(title).trim());
        meta.setAuthor("Strength SMP");
        book.setItemMeta((ItemMeta)meta);
        viewer.openBook(book);
        viewer.playSound(viewer.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
    }
}
