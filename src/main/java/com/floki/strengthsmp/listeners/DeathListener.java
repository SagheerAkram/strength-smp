package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.DataManager;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class DeathListener implements Listener {

    private final StrengthSMP plugin;
    private final DataManager dataManager;

    public DeathListener(StrengthSMP plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player attacker = victim.getKiller();
        UUID vUid = victim.getUniqueId();

        // ── 1. Increment Death Count ────────────────────────────────────────
        dataManager.addDeath(vUid);

        // ── 2. Handle Discord Logging ────────────────────────────────────────
        if (plugin.getDiscordManager() != null) {
            plugin.getDiscordManager().logKill(attacker, victim, (attacker != null ? 1 : 0));
        }

        if (attacker == null || attacker.equals(victim)) {
            return;
        }

        // ── 3. Handle Strength Shift & Kill Stat ─────────────────────────────
        plugin.getStrengthService().handleKill(attacker, victim);

        // ── 4. Drop Death Certificate to Killer ─────────────────────────────
        org.bukkit.inventory.ItemStack deathCert = com.floki.strengthsmp.util.ItemFactory
                .createDeathCertificate(plugin, victim.getName(), attacker.getName());
        java.util.Map<Integer, org.bukkit.inventory.ItemStack> leftover = attacker.getInventory().addItem(deathCert);
        if (!leftover.isEmpty()) {
            for (org.bukkit.inventory.ItemStack drop : leftover.values()) {
                attacker.getWorld().dropItemNaturally(attacker.getLocation(), drop);
            }
        }

        // ── 5. Handle Monarch Recalculation ──────────────────────────────
        if (plugin.getMonarchService() != null) {
            UUID currentMonarch = plugin.getMonarchService().getCurrentMonarchUUID();
            boolean isRegicide = currentMonarch != null && currentMonarch.equals(vUid);
            
            plugin.getMonarchService().calculateNewMonarch();
            
            if (isRegicide) {
                playRegicideEffect(victim);
            }
        }

        // ── 6. Visual Soul Harvest ──────────────────────────────────────────
        playSoulHarvestEffect(attacker, victim);
    }

    private void playSoulHarvestEffect(Player killer, Player victim) {
        org.bukkit.Location start = victim.getLocation().add(0, 1, 0);
        
        // Initial burst
        com.floki.strengthsmp.util.CompatUtil.spawnParticle(victim.getWorld(), "SCULK_CHARGE_POP", start, 20, 0.3, 0.3, 0.3, 0.05);
        com.floki.strengthsmp.util.CompatUtil.spawnParticle(victim.getWorld(), "SPELL_WITCH", start, 30, 0.5, 0.5, 0.5, 0.1);
        
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!killer.isOnline() || ticks > 20) {
                    this.cancel();
                    return;
                }
                
                org.bukkit.Location kLoc = killer.getLocation().add(0, 1.2, 0);
                org.bukkit.Location current = start.clone().add(kLoc.toVector().subtract(start.toVector()).multiply(ticks / 20.0));
                
                com.floki.strengthsmp.util.CompatUtil.spawnParticle(victim.getWorld(), "SOUL", current, 3, 0.05, 0.05, 0.05, 0.02);
                com.floki.strengthsmp.util.CompatUtil.spawnParticle(victim.getWorld(), "SCULK_SOUL", current, 2, 0.02, 0.02, 0.02, 0.01);
                
                if (ticks % 5 == 0) {
                    killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f + (ticks * 0.02f));
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void playRegicideEffect(Player victim) {
        String title = MessageUtil.color("&4&lREGICIDE");
        String subtitle = MessageUtil.color("&cThe Monarch " + victim.getName() + " has fallen!");
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle(title, subtitle, 10, 70, 20);
            p.playSound(p.getLocation(), Sound.ENTITY_ZOGLIN_ANGRY, 1.0f, 0.5f);
            p.playSound(p.getLocation(), Sound.EVENT_RAID_HORN, 1.0f, 0.8f);
        }
    }
}
