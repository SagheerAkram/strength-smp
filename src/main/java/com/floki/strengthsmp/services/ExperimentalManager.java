package com.floki.strengthsmp.services;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class ExperimentalManager {

    private final StrengthSMP plugin;
    private final DataManager dataManager;
    private boolean bloodMoonActive = false;

    public ExperimentalManager(StrengthSMP plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
        
        startRepeatingTask();
    }

    public boolean isBloodMoonActive() {
        return bloodMoonActive;
    }

    private void startRepeatingTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.getConfigManager().isExperimentalEnabled()) {
                    return; // Do nothing if disabled
                }

                // Check Monarch Aura (Feature C)
                UUID monarchId = dataManager.getMonarch();
                if (monarchId != null) {
                    Player monarch = Bukkit.getPlayer(monarchId);
                    if (monarch != null && monarch.isOnline()) {
                        monarch.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, monarch.getLocation().add(0, 1, 0), 3, 0.5, 0.5, 0.5, 0.05);
                        if (!monarch.getPlayerListName().contains("Monarch")) {
                            monarch.setPlayerListName("§e[👑 Monarch] §r" + monarch.getName());
                        }
                    }
                }

                // Check Cursed Bounty (Feature D)
                for (Player player : Bukkit.getOnlinePlayers()) {
                    int bounty = dataManager.getBounty(player.getUniqueId());
                    if (bounty >= 2) {
                        if (Math.random() < 0.3) { // 30% chance every 2 seconds to hear heartbeat
                            player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.0f, 1.0f);
                            player.getWorld().spawnParticle(Particle.SMOKE, player.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.02);
                        }
                    }
                }

                // Check Blood Moon (Feature B)
                // Assuming world is "world"
                if (Bukkit.getWorld("world") != null) {
                    long time = Bukkit.getWorld("world").getTime();
                    // Dusk is around 13000. 
                    // This task runs every 40 ticks (2 seconds), time progresses by 40.
                    // We only want to trigger it once per day.
                    if (time >= 13000 && time <= 13050 && !bloodMoonActive) {
                        if (Math.random() < 0.10) { // 10% chance
                            bloodMoonActive = true;
                            Bukkit.broadcastMessage("§4§lTHE BLOOD MOON IS RISING...");
                            Bukkit.broadcastMessage("§cKill rewards yield an extra temporary item tonight!");
                        }
                    } else if (time >= 23000 || time < 13000) {
                        if (bloodMoonActive) {
                            bloodMoonActive = false;
                            Bukkit.broadcastMessage("§aThe Blood Moon has passed.");
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 40L, 40L); // Run every 2 seconds
    }
}
