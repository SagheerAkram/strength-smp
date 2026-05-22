package com.floki.strengthsmp.services;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages the "Strength Rift" world event.
 */
public class RiftService {

    private final StrengthSMP plugin;
    private final Random random = new Random();
    
    private Location activeRiftLocation;
    private final List<LivingEntity> riftMobs = new ArrayList<>();
    private boolean riftActive = false;

    public RiftService(StrengthSMP plugin) {
        this.plugin = plugin;
        startRiftTimer();
    }

    private void startRiftTimer() {
        // Attempt to spawn a rift every 1-2 hours
        new BukkitRunnable() {
            @Override
            public void run() {
                if (riftActive) return;
                if (Bukkit.getOnlinePlayers().size() < 2) return; // Need competition

                spawnRift();
            }
        }.runTaskTimer(plugin, 20 * 60 * 30, 20 * 60 * 60); // Start check after 30m, then every hour
    }

    public void spawnRift() {
        Player randomPlayer = new ArrayList<>(Bukkit.getOnlinePlayers()).get(random.nextInt(Bukkit.getOnlinePlayers().size()));
        Location loc = randomPlayer.getLocation().add(random.nextInt(100) - 50, 0, random.nextInt(100) - 50);
        loc.setY(loc.getWorld().getHighestBlockYAt(loc) + 1);

        this.activeRiftLocation = loc;
        this.riftActive = true;

        MessageUtil.broadcast("<#9b59b6><b>[RIFT]</b></#9b59b6> <gray>A massive</gray> <#8e44ad>Strength Rift</#8e44ad> <gray>has opened at</gray> <white>" + 
                loc.getBlockX() + ", " + loc.getBlockZ() + "</white><gray>!</gray>");
        
        startVortexAnimation();
        spawnInitialWaves();
    }

    private void startVortexAnimation() {
        new BukkitRunnable() {
            double angle = 0;
            int ticks = 0;

            @Override
            public void run() {
                if (!riftActive || ticks > 1200) { // Max 1 minute duration or until cleared
                    this.cancel();
                    return;
                }

                // Swirling vortex of purple particles
                for (int i = 0; i < 3; i++) {
                    double x = Math.cos(angle + (i * 2)) * 2;
                    double z = Math.sin(angle + (i * 2)) * 2;
                    activeRiftLocation.getWorld().spawnParticle(Particle.DRAGON_BREATH, activeRiftLocation.clone().add(x, 1 + (Math.sin(angle)), z), 5, 0, 0, 0, 0.05);
                    activeRiftLocation.getWorld().spawnParticle(Particle.SPELL_WITCH, activeRiftLocation.clone().add(-x, 2 - (Math.sin(angle)), -z), 5, 0, 0, 0, 0.05);
                }
                
                if (ticks % 20 == 0) {
                    activeRiftLocation.getWorld().playSound(activeRiftLocation, Sound.BLOCK_PORTAL_AMBIENT, 1.0f, 0.5f);
                }

                angle += 0.2;
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void spawnInitialWaves() {
        for (int i = 0; i < 5; i++) {
            Location spawnLoc = activeRiftLocation.clone().add(random.nextInt(6) - 3, 0, random.nextInt(6) - 3);
            Zombie zombie = (Zombie) activeRiftLocation.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
            
            zombie.setCustomName(MessageUtil.color("&5&lRift Wraith"));
            zombie.setCustomNameVisible(true);
            
            // Give purple gear
            zombie.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
            zombie.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE)); // We could dye it purple but let's stick to basics for now
            zombie.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
            
            zombie.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(40.0);
            zombie.setHealth(40.0);
            
            riftMobs.add(zombie);
        }
    }

    public void onMobDeath(LivingEntity entity, Player killer) {
        if (!riftActive || !riftMobs.contains(entity)) return;

        riftMobs.remove(entity);
        
        // Reward per kill
        if (random.nextInt(100) < 20) { // 20% chance for a fragment
             entity.getWorld().dropItemNaturally(entity.getLocation(), 
                     com.floki.strengthsmp.util.ItemFactory.createStrengthItem(1));
             MessageUtil.send(killer, "strength.rift-fragment-drop");
        }

        if (riftMobs.isEmpty()) {
            closeRift(killer);
        }
    }

    private void closeRift(Player closer) {
        this.riftActive = false;
        MessageUtil.broadcast("<#9b59b6><b>[RIFT]</b></#9b59b6> <white>" + closer.getName() + "</white> <gray>has stabilized the rift and claimed the</gray> <#f1c40f>Titan Heart</#f1c40f><gray>!</gray>");
        
        // Grand Prize
        closer.getInventory().addItem(com.floki.strengthsmp.util.ItemFactory.createStrengthItem(3));
        closer.playSound(closer.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        
        activeRiftLocation.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, activeRiftLocation, 1);
        activeRiftLocation.getWorld().playSound(activeRiftLocation, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
    }

    public boolean isRiftActive() {
        return riftActive;
    }
}
