/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Sound
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.PlayerDeathEvent
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 */
package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.DataManager;
import com.floki.strengthsmp.util.CompatUtil;
import com.floki.strengthsmp.util.ItemFactory;
import com.floki.strengthsmp.util.MessageUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class DeathListener
implements Listener {
    private final StrengthSMP plugin;
    private final DataManager dataManager;

    public DeathListener(StrengthSMP plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player attacker = victim.getKiller();
        UUID vUid = victim.getUniqueId();
        com.floki.strengthsmp.api.HollowHook hollow = this.plugin.getHollowHook();
        if (hollow != null && hollow.isHollowWorld(victim.getWorld())) {
            return; // deaths inside the Hollow never touch strength
        }
        int strengthBefore = this.dataManager.getStrength(vUid);
        boolean wasProtected = this.dataManager.isNewbieProtected(vUid) || this.dataManager.hasDeathProtection(vUid);
        this.dataManager.addDeath(vUid);

        boolean systemActive = this.dataManager.isSystemEnabled();
        if (!systemActive && this.plugin.getConfigManager().isMaintenanceKeepInventory()) {
            event.setKeepInventory(true);
            event.getDrops().clear();
            event.setDroppedExp(0);
            MessageUtil.send((org.bukkit.command.CommandSender)victim, "strength.maintenance-keep-inventory", new String[0]);
            return;
        }
        if (!systemActive) {
            return; // maintenance: no strength changes, whether or not inventories are kept
        }

        if (this.plugin.getDiscordManager() != null) {
            this.plugin.getDiscordManager().logKill(attacker, victim, attacker != null ? 1 : 0);
        }
        if (hollow != null && hollow.shouldEliminate(victim, attacker, strengthBefore, wasProtected)) {
            hollow.eliminate(victim, attacker);
        }
        if (attacker == null || attacker.equals((Object)victim)) {
            return;
        }
        this.plugin.getStrengthService().handleKill(attacker, victim);
        ItemStack deathCert = ItemFactory.createDeathCertificate((Plugin)this.plugin, victim.getName(), attacker.getName());
        Map<Integer, ItemStack> leftover = attacker.getInventory().addItem(new ItemStack[]{deathCert});
        if (!leftover.isEmpty()) {
            for (ItemStack drop : leftover.values()) {
                attacker.getWorld().dropItemNaturally(attacker.getLocation(), drop);
            }
        }
        if (this.plugin.getMonarchService() != null) {
            UUID currentMonarch = this.plugin.getMonarchService().getCurrentMonarchUUID();
            boolean isRegicide = currentMonarch != null && currentMonarch.equals(vUid);
            this.plugin.getMonarchService().calculateNewMonarch();
            if (isRegicide) {
                this.playRegicideEffect(victim);
            }
        }
        this.playSoulHarvestEffect(attacker, victim);
    }

    private void playSoulHarvestEffect(final Player killer, final Player victim) {
        final Location start = victim.getLocation().add(0.0, 1.0, 0.0);
        CompatUtil.spawnParticle(victim.getWorld(), "SCULK_CHARGE_POP", start, 20, 0.3, 0.3, 0.3, 0.05);
        CompatUtil.spawnParticle(victim.getWorld(), "SPELL_WITCH", start, 30, 0.5, 0.5, 0.5, 0.1);
        new BukkitRunnable(){
            int ticks = 0;

            public void run() {
                if (!killer.isOnline() || this.ticks > 20) {
                    this.cancel();
                    return;
                }
                Location kLoc = killer.getLocation().add(0.0, 1.2, 0.0);
                Location current = start.clone().add(kLoc.toVector().subtract(start.toVector()).multiply((double)this.ticks / 20.0));
                CompatUtil.spawnParticle(victim.getWorld(), "SOUL", current, 3, 0.05, 0.05, 0.05, 0.02);
                CompatUtil.spawnParticle(victim.getWorld(), "SCULK_SOUL", current, 2, 0.02, 0.02, 0.02, 0.01);
                if (this.ticks % 5 == 0) {
                    killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f + (float)this.ticks * 0.02f);
                }
                ++this.ticks;
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 1L);
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

