package com.floki.strengthsmp.util;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.WeaponType;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class BindAbilityManager implements Listener {

    private final StrengthSMP plugin;
    private final Set<UUID> bowRapidFirePlayers = new HashSet<>();

    public BindAbilityManager(StrengthSMP plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void triggerAbility(Player player, WeaponType type) {
        switch (type) {
            case SWORD:
                triggerExcaliburBeam(player);
                break;
            case AXE:
                triggerDimensionalCleave(player);
                break;
            case BOW:
                triggerGravitySingularity(player);
                break;
            case CROSSBOW:
                triggerScorpionHook(player);
                break;
            case TRIDENT:
                triggerPoseidonsWrath(player);
                break;
            case SHIELD:
                triggerForceField(player);
                break;
        }
    }

    // 🗡️ SWORD: Excalibur Beam (Golden Slicing Crescent)
    private void triggerExcaliburBeam(Player player) {
        Location start = player.getEyeLocation();
        Vector dir = start.getDirection().normalize();
        
        // Perpendicular vector for the width of the arc
        Vector perp = new Vector(-dir.getZ(), 0, dir.getX());
        if (perp.lengthSquared() < 0.001) {
            perp = new Vector(1, 0, 0);
        } else {
            perp.normalize();
        }
        final Vector finalPerp = perp;
        double beamDamage = plugin.getConfigManager().getExcaliburBeamDamage();
        Set<UUID> hitList = new HashSet<>();

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0f, 0.7f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 0.5f);
        com.floki.strengthsmp.util.CompatUtil.spawnParticle(player.getWorld(), "FLASH", start, 3, 0.1, 0.1, 0.1, 0.0);

        new BukkitRunnable() {
            int step = 0;
            @Override
            public void run() {
                if (step >= 12) {
                    this.cancel();
                    return;
                }

                Location center = start.clone().add(dir.clone().multiply(step));
                
                // Spawn golden arc particles
                for (double offset = -1.5; offset <= 1.5; offset += 0.3) {
                    Vector offsetVec = finalPerp.clone().multiply(offset);
                    // Push the edges slightly backward to form a curved crescent arc
                    Vector curveVec = dir.clone().multiply((1.5 - Math.abs(offset)) * 0.25);
                    Location point = center.clone().add(offsetVec).add(curveVec);
                    
                    // Golden/Magical crescent sparks with fine glowing trail rods
                    com.floki.strengthsmp.util.CompatUtil.spawnParticle(player.getWorld(), "FIREWORKS_SPARK", point, 2, 0.05, 0.05, 0.05, 0.02);
                    com.floki.strengthsmp.util.CompatUtil.spawnParticle(player.getWorld(), "CRIT_MAGIC", point, 2, 0.05, 0.05, 0.05, 0.02);
                    com.floki.strengthsmp.util.CompatUtil.spawnParticle(player.getWorld(), "END_ROD", point, 1, 0.02, 0.02, 0.02, 0.01);
                    
                    // Hit detection
                    for (Entity entity : player.getWorld().getNearbyEntities(point, 1.0, 1.0, 1.0)) {
                        if (entity instanceof LivingEntity && !entity.equals(player) && !(entity instanceof ArmorStand)) {
                            LivingEntity target = (LivingEntity) entity;
                            if (hitList.add(target.getUniqueId())) {
                                target.damage(beamDamage, player);
                                target.setVelocity(new Vector(0, 0.5, 0));
                                com.floki.strengthsmp.util.CompatUtil.spawnParticle(target.getWorld(), "CRIT", target.getLocation().add(0, 1, 0), 8, 0.2, 0.2, 0.2, 0.1);
                                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.0f);
                            }
                        }
                    }
                }
                step++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // 🪓 AXE: Dimensional Cleave (Gravity floating crack + Sonic Boom)
    private void triggerDimensionalCleave(Player player) {
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        Vector perp = new Vector(-dir.getZ(), 0, dir.getX());
        if (perp.lengthSquared() < 0.001) {
            perp = new Vector(1, 0, 0);
        } else {
            perp.normalize();
        }
        final Vector finalPerp = perp;
        Vector up = new Vector(0, 1, 0);

        // Center of the floating crack (3.5 blocks ahead)
        Location crackCenter = eye.clone().add(dir.clone().multiply(3.5));
        double damage = plugin.getConfigManager().getDimensionalCleaveDamage();

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.6f, 1.5f);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 40) {
                    this.cancel();
                    
                    // Implosion shockwave detonation!
                    CompatUtil.spawnParticle(crackCenter.getWorld(), "SONIC_BOOM", crackCenter, 1);
                    CompatUtil.playSound(crackCenter.getWorld(), crackCenter, "ENTITY_WARDEN_SONIC_BOOM", 1.2f, 1.0f);
                    
                    // Spawn dramatic expanding rings of black dust and ash
                    for (int i = 0; i < 30; i++) {
                        double angle = i * (2.0 * Math.PI / 30.0);
                        Location pLoc = crackCenter.clone().add(2.5 * Math.cos(angle), 0.5, 2.5 * Math.sin(angle));
                        com.floki.strengthsmp.util.CompatUtil.spawnParticle(crackCenter.getWorld(), "SMOKE_LARGE", pLoc, 2, 0.05, 0.05, 0.05, 0.02);
                        com.floki.strengthsmp.util.CompatUtil.spawnParticle(crackCenter.getWorld(), "FLASH", pLoc, 1, 0, 0, 0, 0);
                    }
                    
                    for (Entity entity : crackCenter.getWorld().getNearbyEntities(crackCenter, 5.0, 5.0, 5.0)) {
                        if (entity instanceof LivingEntity && !entity.equals(player) && !(entity instanceof ArmorStand)) {
                            LivingEntity target = (LivingEntity) entity;
                            target.damage(damage, player);
                            Vector push = target.getLocation().toVector().subtract(crackCenter.toVector()).normalize().multiply(1.4).setY(0.4);
                            target.setVelocity(push);
                        }
                    }
                    return;
                }

                // Render the beautiful floating diagonal rift particles
                for (double offset = -1.5; offset <= 1.5; offset += 0.2) {
                    Location point = crackCenter.clone()
                            .add(finalPerp.clone().multiply(offset))
                            .add(up.clone().multiply(offset * 0.7)); // Slanted diagonal angle

                    // Dark void portal particles + neon purple witch + thick dragon void mist
                    com.floki.strengthsmp.util.CompatUtil.spawnParticle(crackCenter.getWorld(), "PORTAL", point, 3, 0.05, 0.05, 0.05, 0.02);
                    com.floki.strengthsmp.util.CompatUtil.spawnParticle(crackCenter.getWorld(), "SPELL_WITCH", point, 2, 0.05, 0.05, 0.05, 0.02);
                    com.floki.strengthsmp.util.CompatUtil.spawnParticle(crackCenter.getWorld(), "DRAGON_BREATH", point, 1, 0.01, 0.01, 0.01, 0.01);
                }

                // Vacuum Pull Logic (Pull all entities towards the crack)
                for (Entity entity : crackCenter.getWorld().getNearbyEntities(crackCenter, 5.0, 5.0, 5.0)) {
                    if (entity instanceof LivingEntity && !entity.equals(player) && !(entity instanceof ArmorStand)) {
                        LivingEntity target = (LivingEntity) entity;
                        Vector pull = crackCenter.toVector().subtract(target.getLocation().toVector()).normalize().multiply(0.22);
                        target.setVelocity(pull);
                        
                        // Apply tiny hover effect
                        target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 3, 0, false, false, false));
                    }
                }

                // Ticking ambient sounds
                if (ticks % 5 == 0) {
                    crackCenter.getWorld().playSound(crackCenter, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.6f, 0.5f + (ticks / 40.0f));
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // 🏹 BOW: Gravity Singularity (Vortex Arrow + Instant fire mode)
    private void triggerGravitySingularity(Player player) {
        Location start = player.getEyeLocation();
        Vector dir = start.getDirection().normalize();

        // Spawn high-speed tracer projectile
        Snowball singularityBall = player.launchProjectile(Snowball.class);
        singularityBall.setVelocity(dir.multiply(2.0));
        singularityBall.setMetadata("gravity_singular", new org.bukkit.metadata.FixedMetadataValue(plugin, true));

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);

        // Schedule visual trail
        new BukkitRunnable() {
            @Override
            public void run() {
                if (singularityBall.isDead() || singularityBall.isOnGround()) {
                    this.cancel();
                    triggerBlackhole(singularityBall.getLocation(), player);
                    return;
                }
                com.floki.strengthsmp.util.CompatUtil.spawnParticle(singularityBall.getWorld(), "PORTAL", singularityBall.getLocation(), 4, 0.1, 0.1, 0.1, 0.05);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void triggerBlackhole(Location loc, Player owner) {
        int duration = plugin.getConfigManager().getVoidSingularityDuration();
        loc.getWorld().playSound(loc, Sound.BLOCK_PORTAL_TRIGGER, 1.0f, 0.5f);

        // Turn on rapid-fire for owner
        bowRapidFirePlayers.add(owner.getUniqueId());
        owner.sendMessage("§d§lSUPERCHARGE §r§7— Your bow has instant pull velocity!");
        owner.playSound(owner.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.5f);

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= duration || !owner.isOnline()) {
                    this.cancel();
                    bowRapidFirePlayers.remove(owner.getUniqueId());
                    com.floki.strengthsmp.util.CompatUtil.spawnParticle(loc.getWorld(), "FLASH", loc, 5, 0.5, 0.5, 0.5, 0.1);
                    loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 0.5f);
                    return;
                }

                // Render Swirling Vortex lines drawing inwards like a real cosmic singularity
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 4) {
                    double r = 1.8 + Math.sin(ticks / 5.0) * 0.5; // Pulsing radius
                    double x = Math.cos(angle + (ticks * 0.2)) * r;
                    double z = Math.sin(angle + (ticks * 0.2)) * r;
                    Location particleLoc = loc.clone().add(x, 1.0, z);

                    com.floki.strengthsmp.util.CompatUtil.spawnParticle(loc.getWorld(), "PORTAL", particleLoc, 1, 0, 0, 0, 0);
                    com.floki.strengthsmp.util.CompatUtil.spawnParticle(loc.getWorld(), "SPELL_WITCH", particleLoc, 1, 0, 0, 0, 0);
                }

                // Gorgeous Cosmic Spiral Drawing into center
                for (int i = 0; i < 3; i++) {
                    double offsetAngle = i * (2.0 * Math.PI / 3.0);
                    double spiralAngle = (ticks * 0.3) + offsetAngle;
                    double spiralRadius = 3.5 - ((ticks * 0.12) % 3.5); // spiral drawing inward
                    double sx = Math.cos(spiralAngle) * spiralRadius;
                    double sz = Math.sin(spiralAngle) * spiralRadius;
                    Location sLoc = loc.clone().add(sx, 1.0, sz);
                    
                    com.floki.strengthsmp.util.CompatUtil.spawnParticle(loc.getWorld(), "PORTAL", sLoc, 3, 0, 0, 0, 0.02);
                    com.floki.strengthsmp.util.CompatUtil.spawnParticle(loc.getWorld(), "DRAGON_BREATH", sLoc, 1, 0.01, 0.01, 0.01, 0.01);
                }

                // Pull enemies inside 8 blocks to the center
                for (Entity entity : loc.getWorld().getNearbyEntities(loc, 8.0, 8.0, 8.0)) {
                    if (entity instanceof LivingEntity && !entity.equals(owner) && !(entity instanceof ArmorStand)) {
                        LivingEntity target = (LivingEntity) entity;
                        Vector pull = loc.toVector().subtract(target.getLocation().toVector()).normalize().multiply(0.24);
                        target.setVelocity(pull);
                    }
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // ➶ CROSSBOW: Scorpion Hook (Harpoon Pull and Stun)
    private void triggerScorpionHook(Player player) {
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        double maxRange = plugin.getConfigManager().getScorpionHookRange();

        // Robust cone-based target search using distance and field-of-view dot product
        LivingEntity victim = null;
        double bestDot = 0.90; // Cone of ~25 degrees for smooth targeting under latency

        for (Entity entity : player.getWorld().getNearbyEntities(eye, maxRange, maxRange, maxRange)) {
            if (entity instanceof LivingEntity && !entity.equals(player) && !(entity instanceof ArmorStand)) {
                LivingEntity target = (LivingEntity) entity;
                Vector toTarget = target.getEyeLocation().toVector().subtract(eye.toVector());
                double distance = toTarget.length();
                if (distance <= maxRange) {
                    toTarget.normalize();
                    double dot = dir.dot(toTarget);
                    if (dot > bestDot) {
                        victim = target;
                        bestDot = dot;
                    }
                }
            }
        }

        if (victim == null) {
            // Missed hook shot
            player.sendMessage("§c⚡ Scorpion Hook missed!");
            player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1.0f, 0.5f);
            return;
        }

        // Hit!
        final LivingEntity target = victim;
        Location startLoc = player.getLocation().add(0, 1.0, 0);
        Location targetLoc = target.getLocation().add(0, 1.0, 0);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_THROW, 1.2f, 0.6f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT, 1.0f, 1.5f);

        // Render fiery rusty chain hook particles instantly
        Vector chainDir = targetLoc.toVector().subtract(startLoc.toVector());
        double dist = chainDir.length();
        Vector chainStep = chainDir.normalize().multiply(0.4);

        for (double i = 0; i < dist; i += 0.4) {
            Location chainPoint = startLoc.clone().add(chainStep.clone().multiply(i / 0.4));
            com.floki.strengthsmp.util.CompatUtil.spawnParticle(player.getWorld(), "CRIT", chainPoint, 3, 0.05, 0.05, 0.05, 0.02);
            com.floki.strengthsmp.util.CompatUtil.spawnParticle(player.getWorld(), "LAVA", chainPoint, 1, 0, 0, 0, 0.0);
            com.floki.strengthsmp.util.CompatUtil.spawnParticle(player.getWorld(), "SMOKE_NORMAL", chainPoint, 1, 0, 0, 0, 0);
        }

        // Pull victim straight to owner's feet
        Vector pull = player.getLocation().toVector().subtract(target.getLocation().toVector()).normalize().multiply(1.35).setY(0.25);
        target.setVelocity(pull);

        // Particle puff of dirt/drag on arrival
        com.floki.strengthsmp.util.CompatUtil.spawnParticle(player.getWorld(), "EXPLOSION_NORMAL", target.getLocation(), 10, 0.3, 0.2, 0.3, 0.05);

        // Apply 1.2 second Stun (Axe-style complete movement block)
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 24, 10, false, false, false));
        target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 24, 200, false, false, false));

        if (target instanceof Player) {
            ((Player) target).sendMessage("§c⚡ You were pulled and stunned by " + player.getName() + "'s Scorpion Hook!");
        }
        player.sendMessage("§a⚡ Scorpion Hook hit! Dragged " + target.getName() + " directly to you!");
    }

    // 🔱 TRIDENT: Poseidon's Wrath (Triple lightning strikes, safe for terrain)
    private void triggerPoseidonsWrath(Player player) {
        Location loc = player.getLocation();
        double damage = plugin.getConfigManager().getPoseidonsWrathDamage();

        player.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.2f, 1.0f);

        // Calculate 3 triangular points (5 blocks away from the player)
        Location[] strikes = new Location[]{
            loc.clone().add(5.0 * Math.cos(0), 0, 5.0 * Math.sin(0)),
            loc.clone().add(5.0 * Math.cos(2.094), 0, 5.0 * Math.sin(2.094)), // 120 degrees
            loc.clone().add(5.0 * Math.cos(4.188), 0, 5.0 * Math.sin(4.188))  // 240 degrees
        };

        // Trigger visual-only lightning (strictly safe, does not damage terrain or players natively)
        for (Location strike : strikes) {
            Location highest = player.getWorld().getHighestBlockAt(strike).getLocation();
            highest.getWorld().strikeLightningEffect(highest);
            
            // Ocean/Storm themed splash circle ripples expanding at strikes
            for (int i = 0; i < 20; i++) {
                double angle = i * (2.0 * Math.PI / 20.0);
                Location ringLoc = highest.clone().add(2.0 * Math.cos(angle), 0.2, 2.0 * Math.sin(angle));
                com.floki.strengthsmp.util.CompatUtil.spawnParticle(highest.getWorld(), "WATER_SPLASH", ringLoc, 4, 0.1, 0.2, 0.1, 0.1);
                com.floki.strengthsmp.util.CompatUtil.spawnParticle(highest.getWorld(), "WATER_WAKE", ringLoc, 2, 0.1, 0.1, 0.1, 0.05);
            }
        }

        // Casters Storm electricity aura around them
        Location casterLoc = player.getLocation().add(0, 1.0, 0);
        for (int i = 0; i < 24; i++) {
            double angle = i * (2.0 * Math.PI / 24.0);
            Location ringLoc = casterLoc.clone().add(1.8 * Math.cos(angle), 0, 1.8 * Math.sin(angle));
            CompatUtil.spawnParticle(player.getWorld(), "ELECTRIC_SPARK", ringLoc, 2, 0.05, 0.05, 0.05, 0.02);
            com.floki.strengthsmp.util.CompatUtil.spawnParticle(player.getWorld(), "CRIT_MAGIC", ringLoc, 1, 0.02, 0.02, 0.02, 0.01);
        }

        // Deal manual damage, knockback, and ignite targets within a 6 block radius
        for (Entity entity : player.getWorld().getNearbyEntities(loc, 6.0, 6.0, 6.0)) {
            if (entity instanceof LivingEntity && !entity.equals(player) && !(entity instanceof ArmorStand)) {
                LivingEntity target = (LivingEntity) entity;
                
                target.damage(damage, player);
                target.setFireTicks(60); // Burn for 3 seconds (60 ticks)
                
                // Knockback away from owner
                Vector knock = target.getLocation().subtract(player.getLocation()).toVector().normalize().multiply(1.1).setY(0.3);
                target.setVelocity(knock);
                
                com.floki.strengthsmp.util.CompatUtil.spawnParticle(target.getWorld(), "FLAME", target.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.05);
            }
        }
    }

    // 🛡️ SHIELD: Force Field (Concentric Expanding Shockwave)
    private void triggerForceField(Player player) {
        Location loc = player.getLocation().add(0, 0.5, 0);
        double maxRadius = plugin.getConfigManager().getForceFieldRadius();
        Set<UUID> hitList = new HashSet<>();

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0f, 0.6f);

        // Expand shockwave over 5 ticks (1.2 blocks per tick up to 6.0)
        new BukkitRunnable() {
            int tick = 1;
            @Override
            public void run() {
                if (tick > 5) {
                    this.cancel();
                    return;
                }

                double radius = tick * (maxRadius / 5.0);

                // Draw horizontal circular wave particles
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    Location particleLoc = loc.clone().add(x, 0, z);

                    com.floki.strengthsmp.util.CompatUtil.spawnParticle(loc.getWorld(), "CLOUD", particleLoc, 1, 0, 0, 0, 0.02);
                    com.floki.strengthsmp.util.CompatUtil.spawnParticle(loc.getWorld(), "ELECTRIC_SPARK", particleLoc, 1, 0, 0, 0, 0.02);
                }

                // Render beautiful expanding dome hemispherical bubble shell
                for (int i = 0; i < 20; i++) {
                    double u = Math.random();
                    double v = Math.random();
                    double theta = u * 2.0 * Math.PI;
                    double phi = Math.acos(2.0 * v - 1.0);
                    
                    double py = Math.abs(Math.sin(phi) * radius); // only top hemisphere dome
                    double px = Math.cos(theta) * Math.sin(phi) * radius;
                    double pz = Math.sin(theta) * Math.sin(phi) * radius;
                    Location sphereLoc = loc.clone().add(px, py, pz);
                    
                    com.floki.strengthsmp.util.CompatUtil.spawnParticle(loc.getWorld(), "CLOUD", sphereLoc, 1, 0.01, 0.01, 0.01, 0.0);
                    com.floki.strengthsmp.util.CompatUtil.spawnParticle(loc.getWorld(), "ELECTRIC_SPARK", sphereLoc, 1, 0.01, 0.01, 0.01, 0.0);
                }

                // Push and slow entities caught in the expanding ring
                for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, 3.0, radius)) {
                    if (entity instanceof LivingEntity && !entity.equals(player) && !(entity instanceof ArmorStand)) {
                        LivingEntity target = (LivingEntity) entity;
                        if (hitList.add(target.getUniqueId())) {
                            Vector push = target.getLocation().subtract(player.getLocation()).toVector().normalize().multiply(1.45).setY(0.45);
                            target.setVelocity(push);
                            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 1)); // Slow II for 3 seconds
                            
                            com.floki.strengthsmp.util.CompatUtil.spawnParticle(target.getWorld(), "FLASH", target.getLocation().add(0, 1, 0), 1);
                        }
                    }
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // 🏹 Instant Bow Rapid Fire Handler
    @EventHandler
    public void onPlayerLeftClickWithBow(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (!bowRapidFirePlayers.contains(player.getUniqueId())) return;
        
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held == null || held.getType() != Material.BOW) return;

        // Verify weapon is indeed active bound weapon
        if (!plugin.getDataManager().hasBoundWeapon(player.getUniqueId())) return;
        
        event.setCancelled(true);

        // Consume arrow if not creative
        if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            if (!hasArrow(player)) {
                player.sendMessage("§cYou have no arrows!");
                return;
            }
            consumeArrow(player);
        }

        // Fire fully charged arrow instantly!
        Arrow arrow = player.launchProjectile(Arrow.class);
        arrow.setVelocity(player.getEyeLocation().getDirection().multiply(3.0));
        arrow.setCritical(true);
        
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.0f);
        com.floki.strengthsmp.util.CompatUtil.spawnParticle(player.getWorld(), "SPELL_INSTANT", player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(1.0)), 5, 0.1, 0.1, 0.1, 0.05);
    }

    private boolean hasArrow(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && (item.getType() == Material.ARROW || item.getType() == Material.SPECTRAL_ARROW || item.getType() == Material.TIPPED_ARROW)) {
                return true;
            }
        }
        return false;
    }

    private void consumeArrow(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && (item.getType() == Material.ARROW || item.getType() == Material.SPECTRAL_ARROW || item.getType() == Material.TIPPED_ARROW)) {
                item.setAmount(item.getAmount() - 1);
                player.getInventory().setItem(i, item.getAmount() <= 0 ? null : item);
                return;
            }
        }
    }
}
