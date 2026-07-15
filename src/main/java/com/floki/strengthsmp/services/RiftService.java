/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.Particle
 *  org.bukkit.Sound
 *  org.bukkit.attribute.Attribute
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.EntityType
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.Zombie
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 */
package com.floki.strengthsmp.services;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.util.ItemFactory;
import com.floki.strengthsmp.util.MessageUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class RiftService {
    private final StrengthSMP plugin;
    private final Random random = new Random();
    private Location activeRiftLocation;
    private final List<LivingEntity> riftMobs = new ArrayList<LivingEntity>();
    private boolean riftActive = false;

    public RiftService(StrengthSMP plugin) {
        this.plugin = plugin;
        this.startRiftTimer();
    }

    private void startRiftTimer() {
        new BukkitRunnable(){

            public void run() {
                if (RiftService.this.riftActive) {
                    return;
                }
                if (Bukkit.getOnlinePlayers().size() < 2) {
                    return;
                }
                RiftService.this.spawnRift();
            }
        }.runTaskTimer((Plugin)this.plugin, 36000L, 72000L);
    }

    public void spawnRift() {
        Player randomPlayer = (Player)new ArrayList(Bukkit.getOnlinePlayers()).get(this.random.nextInt(Bukkit.getOnlinePlayers().size()));
        Location loc = randomPlayer.getLocation().add((double)(this.random.nextInt(100) - 50), 0.0, (double)(this.random.nextInt(100) - 50));
        loc.setY((double)(loc.getWorld().getHighestBlockYAt(loc) + 1));
        this.activeRiftLocation = loc;
        this.riftActive = true;
        MessageUtil.broadcast("<#9b59b6><b>[RIFT]</b></#9b59b6> <gray>A massive</gray> <#8e44ad>Strength Rift</#8e44ad> <gray>has opened at</gray> <white>" + loc.getBlockX() + ", " + loc.getBlockZ() + "</white><gray>!</gray>", new String[0]);
        this.startVortexAnimation();
        this.spawnInitialWaves();
    }

    private void startVortexAnimation() {
        new BukkitRunnable(){
            double angle = 0.0;
            int ticks = 0;

            public void run() {
                if (!RiftService.this.riftActive || this.ticks > 1200) {
                    this.cancel();
                    return;
                }
                for (int i = 0; i < 3; ++i) {
                    double x = Math.cos(this.angle + (double)(i * 2)) * 2.0;
                    double z = Math.sin(this.angle + (double)(i * 2)) * 2.0;
                    RiftService.this.activeRiftLocation.getWorld().spawnParticle(Particle.DRAGON_BREATH, RiftService.this.activeRiftLocation.clone().add(x, 1.0 + Math.sin(this.angle), z), 5, 0.0, 0.0, 0.0, 0.05);
                    RiftService.this.activeRiftLocation.getWorld().spawnParticle(Particle.SPELL_WITCH, RiftService.this.activeRiftLocation.clone().add(-x, 2.0 - Math.sin(this.angle), -z), 5, 0.0, 0.0, 0.0, 0.05);
                }
                if (this.ticks % 20 == 0) {
                    RiftService.this.activeRiftLocation.getWorld().playSound(RiftService.this.activeRiftLocation, Sound.BLOCK_PORTAL_AMBIENT, 1.0f, 0.5f);
                }
                this.angle += 0.2;
                ++this.ticks;
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 1L);
    }

    private void spawnInitialWaves() {
        for (int i = 0; i < 5; ++i) {
            Location spawnLoc = this.activeRiftLocation.clone().add((double)(this.random.nextInt(6) - 3), 0.0, (double)(this.random.nextInt(6) - 3));
            Zombie zombie = (Zombie)this.activeRiftLocation.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
            zombie.setCustomName(MessageUtil.color("&5&lRift Wraith"));
            zombie.setCustomNameVisible(true);
            zombie.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
            zombie.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
            zombie.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
            zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40.0);
            zombie.setHealth(40.0);
            this.riftMobs.add((LivingEntity)zombie);
        }
    }

    public void onMobDeath(LivingEntity entity, Player killer) {
        if (!this.riftActive || !this.riftMobs.contains(entity)) {
            return;
        }
        this.riftMobs.remove(entity);
        if (this.random.nextInt(100) < 20) {
            entity.getWorld().dropItemNaturally(entity.getLocation(), ItemFactory.createStrengthItem(1));
            MessageUtil.send((CommandSender)killer, "strength.rift-fragment-drop", new String[0]);
        }
        if (this.riftMobs.isEmpty()) {
            this.closeRift(killer);
        }
    }

    private void closeRift(Player closer) {
        this.riftActive = false;
        MessageUtil.broadcast("<#9b59b6><b>[RIFT]</b></#9b59b6> <white>" + closer.getName() + "</white> <gray>has stabilized the rift and claimed the</gray> <#f1c40f>Titan Heart</#f1c40f><gray>!</gray>", new String[0]);
        closer.getInventory().addItem(new ItemStack[]{ItemFactory.createStrengthItem(3)});
        closer.playSound(closer.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        this.activeRiftLocation.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, this.activeRiftLocation, 1);
        this.activeRiftLocation.getWorld().playSound(this.activeRiftLocation, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
    }

    public boolean isRiftActive() {
        return this.riftActive;
    }
}

