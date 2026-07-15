/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Particle
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 */
package com.floki.strengthsmp.services;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.DataManager;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ExperimentalManager {
    private final StrengthSMP plugin;
    private final DataManager dataManager;
    private boolean bloodMoonActive = false;

    public ExperimentalManager(StrengthSMP plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
        this.startRepeatingTask();
    }

    public boolean isBloodMoonActive() {
        return this.bloodMoonActive;
    }

    private void startRepeatingTask() {
        new BukkitRunnable(){

            public void run() {
                Player monarch;
                if (!ExperimentalManager.this.plugin.getConfigManager().isExperimentalEnabled()) {
                    return;
                }
                UUID monarchId = ExperimentalManager.this.dataManager.getMonarch();
                if (monarchId != null && (monarch = Bukkit.getPlayer((UUID)monarchId)) != null && monarch.isOnline()) {
                    monarch.getWorld().spawnParticle(Particle.TOTEM, monarch.getLocation().add(0.0, 1.0, 0.0), 3, 0.5, 0.5, 0.5, 0.05);
                    if (!monarch.getPlayerListName().contains("Monarch")) {
                        monarch.setPlayerListName("\u00a7e[\ud83d\udc51 Monarch] \u00a7r" + monarch.getName());
                    }
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    int bounty = ExperimentalManager.this.dataManager.getBounty(player.getUniqueId());
                    if (bounty < 2 || !(Math.random() < 0.3)) continue;
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1.0f, 0.5f);
                    player.getWorld().spawnParticle(Particle.SMOKE_NORMAL, player.getLocation().add(0.0, 1.0, 0.0), 5, 0.3, 0.3, 0.3, 0.02);
                }
                if (Bukkit.getWorld((String)"world") != null) {
                    long time = Bukkit.getWorld((String)"world").getTime();
                    if (time >= 13000L && time <= 13050L && !ExperimentalManager.this.bloodMoonActive) {
                        if (Math.random() < 0.1) {
                            ExperimentalManager.this.bloodMoonActive = true;
                            Bukkit.broadcastMessage((String)"\u00a74\u00a7lTHE BLOOD MOON IS RISING...");
                            Bukkit.broadcastMessage((String)"\u00a7cKill rewards yield an extra temporary item tonight!");
                        }
                    } else if ((time >= 23000L || time < 13000L) && ExperimentalManager.this.bloodMoonActive) {
                        ExperimentalManager.this.bloodMoonActive = false;
                        Bukkit.broadcastMessage((String)"\u00a7aThe Blood Moon has passed.");
                    }
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 40L, 40L);
    }
}

