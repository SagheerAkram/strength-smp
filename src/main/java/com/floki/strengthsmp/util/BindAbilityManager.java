/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.GameMode
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.Sound
 *  org.bukkit.entity.ArmorStand
 *  org.bukkit.entity.Arrow
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.Snowball
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.metadata.FixedMetadataValue
 *  org.bukkit.metadata.MetadataValue
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 *  org.bukkit.scheduler.BukkitRunnable
 *  org.bukkit.util.Vector
 */
package com.floki.strengthsmp.util;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.WeaponType;
import com.floki.strengthsmp.util.CompatUtil;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class BindAbilityManager
implements Listener {
    private final StrengthSMP plugin;
    private final Set<UUID> bowRapidFirePlayers = new HashSet<UUID>();

    public BindAbilityManager(StrengthSMP plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)plugin);
    }

    public void triggerAbility(Player player, WeaponType type) {
        switch (type) {
            case SWORD: {
                this.triggerExcaliburBeam(player);
                break;
            }
            case AXE: {
                this.triggerDimensionalCleave(player);
                break;
            }
            case BOW: {
                this.triggerGravitySingularity(player);
                break;
            }
            case CROSSBOW: {
                this.triggerScorpionHook(player);
                break;
            }
            case TRIDENT: {
                this.triggerPoseidonsWrath(player);
                break;
            }
            case SHIELD: {
                this.triggerForceField(player);
            }
        }
    }

    private void triggerExcaliburBeam(final Player player) {
        final Location start = player.getEyeLocation();
        final Vector dir = start.getDirection().normalize();
        Vector perp = new Vector(-dir.getZ(), 0.0, dir.getX());
        if (perp.lengthSquared() < 0.001) {
            perp = new Vector(1, 0, 0);
        } else {
            perp.normalize();
        }
        final Vector finalPerp = perp;
        final double beamDamage = this.plugin.getConfigManager().getExcaliburBeamDamage();
        final HashSet hitList = new HashSet();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0f, 0.7f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 0.5f);
        CompatUtil.spawnParticle(player.getWorld(), "FLASH", start, 3, 0.1, 0.1, 0.1, 0.0);
        new BukkitRunnable(){
            int step = 0;

            public void run() {
                if (this.step >= 12) {
                    this.cancel();
                    return;
                }
                Location center = start.clone().add(dir.clone().multiply(this.step));
                for (double offset = -1.5; offset <= 1.5; offset += 0.3) {
                    Vector offsetVec = finalPerp.clone().multiply(offset);
                    Vector curveVec = dir.clone().multiply((1.5 - Math.abs(offset)) * 0.25);
                    Location point = center.clone().add(offsetVec).add(curveVec);
                    CompatUtil.spawnParticle(player.getWorld(), "FIREWORKS_SPARK", point, 2, 0.05, 0.05, 0.05, 0.02);
                    CompatUtil.spawnParticle(player.getWorld(), "CRIT_MAGIC", point, 2, 0.05, 0.05, 0.05, 0.02);
                    CompatUtil.spawnParticle(player.getWorld(), "END_ROD", point, 1, 0.02, 0.02, 0.02, 0.01);
                    for (Entity entity : player.getWorld().getNearbyEntities(point, 1.0, 1.0, 1.0)) {
                        LivingEntity target;
                        if (!(entity instanceof LivingEntity) || entity.equals((Object)player) || entity instanceof ArmorStand || !hitList.add((target = (LivingEntity)entity).getUniqueId())) continue;
                        target.damage(beamDamage, (Entity)player);
                        target.setVelocity(new Vector(0.0, 0.5, 0.0));
                        CompatUtil.spawnParticle(target.getWorld(), "CRIT", target.getLocation().add(0.0, 1.0, 0.0), 8, 0.2, 0.2, 0.2, 0.1);
                        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.0f);
                    }
                }
                ++this.step;
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 1L);
    }

    private void triggerDimensionalCleave(final Player player) {
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        Vector perp = new Vector(-dir.getZ(), 0.0, dir.getX());
        if (perp.lengthSquared() < 0.001) {
            perp = new Vector(1, 0, 0);
        } else {
            perp.normalize();
        }
        final Vector finalPerp = perp;
        final Vector up = new Vector(0, 1, 0);
        final Location crackCenter = eye.clone().add(dir.clone().multiply(3.5));
        final double damage = this.plugin.getConfigManager().getDimensionalCleaveDamage();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.6f, 1.5f);
        new BukkitRunnable(){
            int ticks = 0;

            public void run() {
                if (this.ticks >= 40) {
                    this.cancel();
                    CompatUtil.spawnParticle(crackCenter.getWorld(), "SONIC_BOOM", crackCenter, 1);
                    CompatUtil.playSound(crackCenter.getWorld(), crackCenter, "ENTITY_WARDEN_SONIC_BOOM", 1.2f, 1.0f);
                    for (int i = 0; i < 30; ++i) {
                        double angle = (double)i * 0.20943951023931953;
                        Location pLoc = crackCenter.clone().add(2.5 * Math.cos(angle), 0.5, 2.5 * Math.sin(angle));
                        CompatUtil.spawnParticle(crackCenter.getWorld(), "SMOKE_LARGE", pLoc, 2, 0.05, 0.05, 0.05, 0.02);
                        CompatUtil.spawnParticle(crackCenter.getWorld(), "FLASH", pLoc, 1, 0.0, 0.0, 0.0, 0.0);
                    }
                    for (Entity entity : crackCenter.getWorld().getNearbyEntities(crackCenter, 5.0, 5.0, 5.0)) {
                        if (!(entity instanceof LivingEntity) || entity.equals((Object)player) || entity instanceof ArmorStand) continue;
                        LivingEntity target = (LivingEntity)entity;
                        target.damage(damage, (Entity)player);
                        Vector push = target.getLocation().toVector().subtract(crackCenter.toVector()).normalize().multiply(1.4).setY(0.4);
                        target.setVelocity(push);
                    }
                    return;
                }
                for (double offset = -1.5; offset <= 1.5; offset += 0.2) {
                    Location point = crackCenter.clone().add(finalPerp.clone().multiply(offset)).add(up.clone().multiply(offset * 0.7));
                    CompatUtil.spawnParticle(crackCenter.getWorld(), "PORTAL", point, 3, 0.05, 0.05, 0.05, 0.02);
                    CompatUtil.spawnParticle(crackCenter.getWorld(), "SPELL_WITCH", point, 2, 0.05, 0.05, 0.05, 0.02);
                    CompatUtil.spawnParticle(crackCenter.getWorld(), "DRAGON_BREATH", point, 1, 0.01, 0.01, 0.01, 0.01);
                }
                for (Entity entity : crackCenter.getWorld().getNearbyEntities(crackCenter, 5.0, 5.0, 5.0)) {
                    if (!(entity instanceof LivingEntity) || entity.equals((Object)player) || entity instanceof ArmorStand) continue;
                    LivingEntity target = (LivingEntity)entity;
                    Vector pull = crackCenter.toVector().subtract(target.getLocation().toVector()).normalize().multiply(0.22);
                    target.setVelocity(pull);
                    target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 3, 0, false, false, false));
                }
                if (this.ticks % 5 == 0) {
                    crackCenter.getWorld().playSound(crackCenter, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.6f, 0.5f + (float)this.ticks / 40.0f);
                }
                ++this.ticks;
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 1L);
    }

    private void triggerGravitySingularity(final Player player) {
        Location start = player.getEyeLocation();
        Vector dir = start.getDirection().normalize();
        final Snowball singularityBall = (Snowball)player.launchProjectile(Snowball.class);
        singularityBall.setVelocity(dir.multiply(2.0));
        singularityBall.setMetadata("gravity_singular", (MetadataValue)new FixedMetadataValue((Plugin)this.plugin, (Object)true));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);
        new BukkitRunnable(){

            public void run() {
                if (singularityBall.isDead() || singularityBall.isOnGround()) {
                    this.cancel();
                    BindAbilityManager.this.triggerBlackhole(singularityBall.getLocation(), player);
                    return;
                }
                CompatUtil.spawnParticle(singularityBall.getWorld(), "PORTAL", singularityBall.getLocation(), 4, 0.1, 0.1, 0.1, 0.05);
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 1L);
    }

    private void triggerBlackhole(final Location loc, final Player owner) {
        final int duration = this.plugin.getConfigManager().getVoidSingularityDuration();
        loc.getWorld().playSound(loc, Sound.BLOCK_PORTAL_TRIGGER, 1.0f, 0.5f);
        this.bowRapidFirePlayers.add(owner.getUniqueId());
        owner.sendMessage("\u00a7d\u00a7lSUPERCHARGE \u00a7r\u00a77\u2014 Your bow has instant pull velocity!");
        owner.playSound(owner.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.5f);
        new BukkitRunnable(){
            int ticks = 0;

            public void run() {
                if (this.ticks >= duration || !owner.isOnline()) {
                    this.cancel();
                    BindAbilityManager.this.bowRapidFirePlayers.remove(owner.getUniqueId());
                    CompatUtil.spawnParticle(loc.getWorld(), "FLASH", loc, 5, 0.5, 0.5, 0.5, 0.1);
                    loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 0.5f);
                    return;
                }
                for (double angle = 0.0; angle < Math.PI * 2; angle += 0.7853981633974483) {
                    double r = 1.8 + Math.sin((double)this.ticks / 5.0) * 0.5;
                    double x = Math.cos(angle + (double)this.ticks * 0.2) * r;
                    double z = Math.sin(angle + (double)this.ticks * 0.2) * r;
                    Location particleLoc = loc.clone().add(x, 1.0, z);
                    CompatUtil.spawnParticle(loc.getWorld(), "PORTAL", particleLoc, 1, 0.0, 0.0, 0.0, 0.0);
                    CompatUtil.spawnParticle(loc.getWorld(), "SPELL_WITCH", particleLoc, 1, 0.0, 0.0, 0.0, 0.0);
                }
                for (int i = 0; i < 3; ++i) {
                    double offsetAngle = (double)i * 2.0943951023931953;
                    double spiralAngle = (double)this.ticks * 0.3 + offsetAngle;
                    double spiralRadius = 3.5 - (double)this.ticks * 0.12 % 3.5;
                    double sx = Math.cos(spiralAngle) * spiralRadius;
                    double sz = Math.sin(spiralAngle) * spiralRadius;
                    Location sLoc = loc.clone().add(sx, 1.0, sz);
                    CompatUtil.spawnParticle(loc.getWorld(), "PORTAL", sLoc, 3, 0.0, 0.0, 0.0, 0.02);
                    CompatUtil.spawnParticle(loc.getWorld(), "DRAGON_BREATH", sLoc, 1, 0.01, 0.01, 0.01, 0.01);
                }
                for (Entity entity : loc.getWorld().getNearbyEntities(loc, 8.0, 8.0, 8.0)) {
                    if (!(entity instanceof LivingEntity) || entity.equals((Object)owner) || entity instanceof ArmorStand) continue;
                    LivingEntity target = (LivingEntity)entity;
                    Vector pull = loc.toVector().subtract(target.getLocation().toVector()).normalize().multiply(0.24);
                    target.setVelocity(pull);
                }
                ++this.ticks;
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 1L);
    }

    private void triggerScorpionHook(Player player) {
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        double maxRange = this.plugin.getConfigManager().getScorpionHookRange();
        LivingEntity victim = null;
        double bestDot = 0.9;
        for (Entity entity : player.getWorld().getNearbyEntities(eye, maxRange, maxRange, maxRange)) {
            LivingEntity target;
            Vector toTarget;
            double distance;
            if (!(entity instanceof LivingEntity) || entity.equals((Object)player) || entity instanceof ArmorStand || !((distance = (toTarget = (target = (LivingEntity)entity).getEyeLocation().toVector().subtract(eye.toVector())).length()) <= maxRange)) continue;
            toTarget.normalize();
            double dot = dir.dot(toTarget);
            if (!(dot > bestDot)) continue;
            victim = target;
            bestDot = dot;
        }
        if (victim == null) {
            player.sendMessage("\u00a7c\u26a1 Scorpion Hook missed!");
            player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1.0f, 0.5f);
            return;
        }
        LivingEntity target = victim;
        Location startLoc = player.getLocation().add(0.0, 1.0, 0.0);
        Location targetLoc = target.getLocation().add(0.0, 1.0, 0.0);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_THROW, 1.2f, 0.6f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT, 1.0f, 1.5f);
        Vector chainDir = targetLoc.toVector().subtract(startLoc.toVector());
        double dist = chainDir.length();
        Vector chainStep = chainDir.normalize().multiply(0.4);
        for (double i = 0.0; i < dist; i += 0.4) {
            Location chainPoint = startLoc.clone().add(chainStep.clone().multiply(i / 0.4));
            CompatUtil.spawnParticle(player.getWorld(), "CRIT", chainPoint, 3, 0.05, 0.05, 0.05, 0.02);
            CompatUtil.spawnParticle(player.getWorld(), "LAVA", chainPoint, 1, 0.0, 0.0, 0.0, 0.0);
            CompatUtil.spawnParticle(player.getWorld(), "SMOKE_NORMAL", chainPoint, 1, 0.0, 0.0, 0.0, 0.0);
        }
        Vector pull = player.getLocation().toVector().subtract(target.getLocation().toVector()).normalize().multiply(1.35).setY(0.25);
        target.setVelocity(pull);
        CompatUtil.spawnParticle(player.getWorld(), "EXPLOSION_NORMAL", target.getLocation(), 10, 0.3, 0.2, 0.3, 0.05);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 24, 10, false, false, false));
        target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 24, 200, false, false, false));
        if (target instanceof Player) {
            ((Player)target).sendMessage("\u00a7c\u26a1 You were pulled and stunned by " + player.getName() + "'s Scorpion Hook!");
        }
        player.sendMessage("\u00a7a\u26a1 Scorpion Hook hit! Dragged " + target.getName() + " directly to you!");
    }

    private void triggerPoseidonsWrath(Player player) {
        Location[] strikes;
        Location loc = player.getLocation();
        double damage = this.plugin.getConfigManager().getPoseidonsWrathDamage();
        player.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.2f, 1.0f);
        for (Location strike : strikes = new Location[]{loc.clone().add(5.0 * Math.cos(0.0), 0.0, 5.0 * Math.sin(0.0)), loc.clone().add(5.0 * Math.cos(2.094), 0.0, 5.0 * Math.sin(2.094)), loc.clone().add(5.0 * Math.cos(4.188), 0.0, 5.0 * Math.sin(4.188))}) {
            Location highest = player.getWorld().getHighestBlockAt(strike).getLocation();
            highest.getWorld().strikeLightningEffect(highest);
            for (int i = 0; i < 20; ++i) {
                double angle = (double)i * 0.3141592653589793;
                Location ringLoc = highest.clone().add(2.0 * Math.cos(angle), 0.2, 2.0 * Math.sin(angle));
                CompatUtil.spawnParticle(highest.getWorld(), "WATER_SPLASH", ringLoc, 4, 0.1, 0.2, 0.1, 0.1);
                CompatUtil.spawnParticle(highest.getWorld(), "WATER_WAKE", ringLoc, 2, 0.1, 0.1, 0.1, 0.05);
            }
        }
        Location casterLoc = player.getLocation().add(0.0, 1.0, 0.0);
        for (int i = 0; i < 24; ++i) {
            double angle = (double)i * 0.2617993877991494;
            Location ringLoc = casterLoc.clone().add(1.8 * Math.cos(angle), 0.0, 1.8 * Math.sin(angle));
            CompatUtil.spawnParticle(player.getWorld(), "ELECTRIC_SPARK", ringLoc, 2, 0.05, 0.05, 0.05, 0.02);
            CompatUtil.spawnParticle(player.getWorld(), "CRIT_MAGIC", ringLoc, 1, 0.02, 0.02, 0.02, 0.01);
        }
        for (Entity entity : player.getWorld().getNearbyEntities(loc, 6.0, 6.0, 6.0)) {
            if (!(entity instanceof LivingEntity) || entity.equals((Object)player) || entity instanceof ArmorStand) continue;
            LivingEntity target = (LivingEntity)entity;
            target.damage(damage, (Entity)player);
            target.setFireTicks(60);
            Vector knock = target.getLocation().subtract(player.getLocation()).toVector().normalize().multiply(1.1).setY(0.3);
            target.setVelocity(knock);
            CompatUtil.spawnParticle(target.getWorld(), "FLAME", target.getLocation().add(0.0, 1.0, 0.0), 10, 0.3, 0.3, 0.3, 0.05);
        }
    }

    private void triggerForceField(final Player player) {
        final Location loc = player.getLocation().add(0.0, 0.5, 0.0);
        final double maxRadius = this.plugin.getConfigManager().getForceFieldRadius();
        final HashSet hitList = new HashSet();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.0f, 0.6f);
        new BukkitRunnable(){
            int tick = 1;

            public void run() {
                if (this.tick > 5) {
                    this.cancel();
                    return;
                }
                double radius = (double)this.tick * (maxRadius / 5.0);
                for (double angle = 0.0; angle < Math.PI * 2; angle += 0.19634954084936207) {
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    Location particleLoc = loc.clone().add(x, 0.0, z);
                    CompatUtil.spawnParticle(loc.getWorld(), "CLOUD", particleLoc, 1, 0.0, 0.0, 0.0, 0.02);
                    CompatUtil.spawnParticle(loc.getWorld(), "ELECTRIC_SPARK", particleLoc, 1, 0.0, 0.0, 0.0, 0.02);
                }
                for (int i = 0; i < 20; ++i) {
                    double u = Math.random();
                    double v = Math.random();
                    double theta = u * 2.0 * Math.PI;
                    double phi = Math.acos(2.0 * v - 1.0);
                    double py = Math.abs(Math.sin(phi) * radius);
                    double px = Math.cos(theta) * Math.sin(phi) * radius;
                    double pz = Math.sin(theta) * Math.sin(phi) * radius;
                    Location sphereLoc = loc.clone().add(px, py, pz);
                    CompatUtil.spawnParticle(loc.getWorld(), "CLOUD", sphereLoc, 1, 0.01, 0.01, 0.01, 0.0);
                    CompatUtil.spawnParticle(loc.getWorld(), "ELECTRIC_SPARK", sphereLoc, 1, 0.01, 0.01, 0.01, 0.0);
                }
                for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, 3.0, radius)) {
                    LivingEntity target;
                    if (!(entity instanceof LivingEntity) || entity.equals((Object)player) || entity instanceof ArmorStand || !hitList.add((target = (LivingEntity)entity).getUniqueId())) continue;
                    Vector push = target.getLocation().subtract(player.getLocation()).toVector().normalize().multiply(1.45).setY(0.45);
                    target.setVelocity(push);
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 1));
                    CompatUtil.spawnParticle(target.getWorld(), "FLASH", target.getLocation().add(0.0, 1.0, 0.0), 1);
                }
                ++this.tick;
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 1L);
    }

    @EventHandler
    public void onPlayerLeftClickWithBow(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        if (!this.bowRapidFirePlayers.contains(player.getUniqueId())) {
            return;
        }
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held == null || held.getType() != Material.BOW) {
            return;
        }
        if (!this.plugin.getDataManager().hasBoundWeapon(player.getUniqueId())) {
            return;
        }
        event.setCancelled(true);
        if (player.getGameMode() != GameMode.CREATIVE) {
            if (!this.hasArrow(player)) {
                player.sendMessage("\u00a7cYou have no arrows!");
                return;
            }
            this.consumeArrow(player);
        }
        Arrow arrow = (Arrow)player.launchProjectile(Arrow.class);
        arrow.setVelocity(player.getEyeLocation().getDirection().multiply(3.0));
        arrow.setCritical(true);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.0f);
        CompatUtil.spawnParticle(player.getWorld(), "SPELL_INSTANT", player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(1.0)), 5, 0.1, 0.1, 0.1, 0.05);
    }

    private boolean hasArrow(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() != Material.ARROW && item.getType() != Material.SPECTRAL_ARROW && item.getType() != Material.TIPPED_ARROW) continue;
            return true;
        }
        return false;
    }

    private void consumeArrow(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); ++i) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getType() != Material.ARROW && item.getType() != Material.SPECTRAL_ARROW && item.getType() != Material.TIPPED_ARROW) continue;
            item.setAmount(item.getAmount() - 1);
            player.getInventory().setItem(i, item.getAmount() <= 0 ? null : item);
            return;
        }
    }
}

