package com.floki.strengthsmp.services;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.util.CompatUtil;
import com.floki.strengthsmp.util.ItemFactory;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RiftService {
    private final StrengthSMP plugin;
    private final Random random = new Random();
    private Location activeRiftLocation;
    private final List<LivingEntity> riftMobs = new ArrayList<>();
    private boolean riftActive = false;

    // Blood Moon state
    private boolean bloodMoonActive = false;
    private BossBar bloodMoonBar;
    private BukkitTask bloodMoonTask;
    private int bloodMoonSecondsLeft = 0;
    private int originalWarningDistance = 5;

    public RiftService(StrengthSMP plugin) {
        this.plugin = plugin;
        this.startRiftTimer();
    }

    private void startRiftTimer() {
        new BukkitRunnable() {
            public void run() {
                if (riftActive || bloodMoonActive) return;
                if (Bukkit.getOnlinePlayers().size() < 2) return;
                spawnRift();
            }
        }.runTaskTimer(this.plugin, 36000L, 72000L); // Every 30-60 mins
    }

    public boolean isRiftActive() {
        return this.riftActive;
    }

    public boolean isBloodMoonActive() {
        return this.bloodMoonActive;
    }

    public String getRiftCoords() {
        if (activeRiftLocation == null) return "Unknown";
        return activeRiftLocation.getBlockX() + ", " + activeRiftLocation.getBlockZ();
    }

    public void startBloodMoon(int durationMinutes) {
        if (bloodMoonActive) return;
        bloodMoonActive = true;
        bloodMoonSecondsLeft = durationMinutes * 60;

        World world = Bukkit.getWorlds().get(0);
        if (world != null) {
            // 1. Midnight Full Moon Lock
            world.setTime(18000);
            // 2. Pulse Red Screen Vignette via WorldBorder Warning Distance
            originalWarningDistance = world.getWorldBorder().getWarningDistance();
            world.getWorldBorder().setWarningDistance(99999999);
        }

        // 3. Glowing BossBar
        bloodMoonBar = Bukkit.createBossBar(
                MessageUtil.color("&4&l⚔ THE BLOOD MOON HAS RISEN &8| &e2X STRENGTH DROPS &4&l⚔"),
                BarColor.RED,
                BarStyle.SEGMENTED_10
        );
        for (Player p : Bukkit.getOnlinePlayers()) {
            bloodMoonBar.addPlayer(p);
            p.sendTitle(MessageUtil.color("&4&lBLOOD MOON"), MessageUtil.color("&cAll PvP kills drop 2X Strength Shards!"), 10, 80, 20);
            p.playSound(p.getLocation(), Sound.EVENT_RAID_HORN, 1.0f, 0.6f);
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.8f, 0.5f);
        }

        MessageUtil.broadcast("&4&l[EVENT] &c&lTHE BLOOD MOON HAS RISEN! &ePvP Kills grant 2X Strength for the next " + durationMinutes + " minutes!", new String[0]);

        // Spawn a Titan Boss somewhere in the world
        spawnRift();

        // Blood Moon Tick Task
        bloodMoonTask = new BukkitRunnable() {
            int ticks = 0;
            final int totalSeconds = durationMinutes * 60;

            public void run() {
                if (!bloodMoonActive || bloodMoonSecondsLeft <= 0) {
                    stopBloodMoon();
                    cancel();
                    return;
                }

                // Keep time locked at night
                if (world != null && ticks % 100 == 0) {
                    world.setTime(18000);
                }

                // Update BossBar progress
                double progress = Math.max(0.0, Math.min(1.0, (double) bloodMoonSecondsLeft / totalSeconds));
                bloodMoonBar.setProgress(progress);
                int mins = bloodMoonSecondsLeft / 60;
                int secs = bloodMoonSecondsLeft % 60;
                bloodMoonBar.setTitle(MessageUtil.color(String.format("&4&l⚔ BLOOD MOON &8[&c2X STR&8] &7Time Left: &e%02d:%02d &4&l⚔", mins, secs)));

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!bloodMoonBar.getPlayers().contains(p)) {
                        bloodMoonBar.addPlayer(p);
                    }
                    // Atmospheric red dust / ash
                    if (ticks % 10 == 0) {
                        Location loc = p.getLocation().add((random.nextDouble() - 0.5) * 10, random.nextDouble() * 3, (random.nextDouble() - 0.5) * 10);
                        CompatUtil.spawnParticle(p.getWorld(), "CRIMSON_SPORE", loc, 4, 0.2, 0.2, 0.2, 0.05);
                    }
                }

                if (ticks % 20 == 0) {
                    bloodMoonSecondsLeft--;
                }
                ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
    }

    public void stopBloodMoon() {
        if (!bloodMoonActive) return;
        bloodMoonActive = false;

        if (bloodMoonTask != null) {
            bloodMoonTask.cancel();
            bloodMoonTask = null;
        }

        if (bloodMoonBar != null) {
            bloodMoonBar.removeAll();
            bloodMoonBar = null;
        }

        World world = Bukkit.getWorlds().get(0);
        if (world != null) {
            world.getWorldBorder().setWarningDistance(originalWarningDistance);
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle(MessageUtil.color("&a&lTHE SKY CLEARS"), MessageUtil.color("&7The Blood Moon has faded..."), 10, 60, 20);
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.2f);
        }

        MessageUtil.broadcast("&a&l[EVENT] &7The Blood Moon has faded. Normal strength rules have resumed.", new String[0]);
    }

    public void spawnRift() {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (players.isEmpty()) return;

        Player anchor = players.get(this.random.nextInt(players.size()));
        Location loc = anchor.getLocation().add((double) (this.random.nextInt(80) - 40), 0.0, (double) (this.random.nextInt(80) - 40));
        loc.setY((double) (loc.getWorld().getHighestBlockYAt(loc) + 1));
        this.activeRiftLocation = loc;
        this.riftActive = true;

        MessageUtil.broadcast("&5&l[RIFT] &7A dimensional &d&lStrength Rift&7 has opened at &f" + loc.getBlockX() + ", " + loc.getBlockZ() + "&7!", new String[0]);
        this.startVortexAnimation();
        this.spawnInitialWaves();
    }

    private void startVortexAnimation() {
        new BukkitRunnable() {
            double angle = 0.0;
            int ticks = 0;

            public void run() {
                if (!riftActive || ticks > 1800) {
                    cancel();
                    return;
                }
                for (int i = 0; i < 3; ++i) {
                    double x = Math.cos(angle + (double) (i * 2)) * 2.5;
                    double z = Math.sin(angle + (double) (i * 2)) * 2.5;
                    CompatUtil.spawnParticle(activeRiftLocation.getWorld(), "DRAGON_BREATH", activeRiftLocation.clone().add(x, 1.0 + Math.sin(angle), z), 4, 0.0, 0.0, 0.0, 0.05);
                    CompatUtil.spawnParticle(activeRiftLocation.getWorld(), "SPELL_WITCH", activeRiftLocation.clone().add(-x, 2.0 - Math.sin(angle), -z), 4, 0.0, 0.0, 0.0, 0.05);
                }
                if (ticks % 20 == 0) {
                    activeRiftLocation.getWorld().playSound(activeRiftLocation, Sound.BLOCK_PORTAL_AMBIENT, 1.0f, 0.5f);
                }
                angle += 0.2;
                ticks++;
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
    }

    private void spawnInitialWaves() {
        World w = activeRiftLocation.getWorld();
        // 4 Rift Wraiths
        for (int i = 0; i < 4; ++i) {
            Location spawnLoc = activeRiftLocation.clone().add((double) (this.random.nextInt(6) - 3), 0.0, (double) (this.random.nextInt(6) - 3));
            Zombie zombie = (Zombie) w.spawnEntity(spawnLoc, EntityType.ZOMBIE);
            zombie.setCustomName(MessageUtil.color("&5&lRift Wraith"));
            zombie.setCustomNameVisible(true);
            zombie.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
            zombie.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
            zombie.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
            Attribute maxHpAttr = CompatUtil.resolveAttribute("MAX_HEALTH", "GENERIC_MAX_HEALTH");
            if (maxHpAttr != null) {
                zombie.getAttribute(maxHpAttr).setBaseValue(40.0);
            }
            zombie.setHealth(40.0);
            this.riftMobs.add(zombie);
        }

        // 1 Mini-Boss: The Rift Titan
        Location bossLoc = activeRiftLocation.clone().add(0, 1, 0);
        Zombie boss = (Zombie) w.spawnEntity(bossLoc, EntityType.ZOMBIE);
        boss.setCustomName(MessageUtil.color("&4&l👑 THE RIFT TITAN [BOSS]"));
        boss.setCustomNameVisible(true);
        boss.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
        boss.getEquipment().setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
        boss.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_AXE));
        Attribute maxHpAttr = CompatUtil.resolveAttribute("MAX_HEALTH", "GENERIC_MAX_HEALTH");
        if (maxHpAttr != null) {
            boss.getAttribute(maxHpAttr).setBaseValue(100.0);
        }
        boss.setHealth(100.0);
        this.riftMobs.add(boss);
    }

    public void onMobDeath(LivingEntity entity, Player killer) {
        if (!this.riftActive || !this.riftMobs.contains(entity)) {
            return;
        }
        this.riftMobs.remove(entity);

        boolean isBoss = entity.getCustomName() != null && entity.getCustomName().contains("TITAN");
        if (isBoss) {
            entity.getWorld().dropItemNaturally(entity.getLocation(), ItemFactory.createStrengthItem(3));
            MessageUtil.broadcast("&6&l[RIFT] &f" + killer.getName() + " &ehas slain the &4&lRift Titan &eand claimed 3 Strength Shards!", new String[0]);
        } else if (this.random.nextInt(100) < 35) {
            entity.getWorld().dropItemNaturally(entity.getLocation(), ItemFactory.createStrengthItem(1));
            MessageUtil.send((CommandSender) killer, "strength.rift-fragment-drop", new String[0]);
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
        if (this.activeRiftLocation != null) {
            CompatUtil.spawnParticle(this.activeRiftLocation.getWorld(), "EXPLOSION_LARGE", this.activeRiftLocation, 1);
            this.activeRiftLocation.getWorld().playSound(this.activeRiftLocation, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
        }
    }
}
