package com.floki.strengthsmp.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.logging.Level;

/**
 * Compatibility utility to prevent NoSuchFieldError and NoClassDefFoundError
 * when using newer Minecraft sounds and particles on older versions (like 1.16.5).
 */
public class CompatUtil {

    /**
     * Spawns a particle by name, falling back to older particles if the particle doesn't exist.
     */
    public static void spawnParticle(World world, String particleName, Location loc, int count, double ox, double oy, double oz, double extra) {
        try {
            // Particle.valueOf(String) is safe because it only throws at runtime if name is invalid.
            // We resolve the enum dynamically to prevent classloader errors.
            Particle particle = Particle.valueOf(particleName);
            world.spawnParticle(particle, loc, count, ox, oy, oz, extra);
        } catch (IllegalArgumentException e) {
            fallbackParticle(world, particleName, loc, count, ox, oy, oz, extra);
        }
    }

    public static void spawnParticle(World world, String particleName, Location loc, int count) {
        spawnParticle(world, particleName, loc, count, 0, 0, 0, 0);
    }

    /**
     * Spawns block/item data particles (like BLOCK_CRACK) with dynamic fallback.
     */
    public static void spawnParticle(World world, String particleName, Location loc, int count, double ox, double oy, double oz, double extra, Object data) {
        try {
            // Modern Bukkit 1.20.5+ consolidated BLOCK_CRACK into BLOCK.
            // We try the name first.
            Particle particle = null;
            try {
                particle = Particle.valueOf(particleName);
            } catch (IllegalArgumentException e) {
                // If BLOCK_CRACK is missing (1.20.5+), try BLOCK
                if (particleName.equals("BLOCK_CRACK")) {
                    particle = Particle.valueOf("BLOCK");
                }
            }

            if (particle != null) {
                world.spawnParticle(particle, loc, count, ox, oy, oz, extra, data);
            }
        } catch (Exception e) {
            // Fallback for older versions or issues with data
            try {
                world.spawnParticle(Particle.valueOf("FALLING_DUST"), loc, count, ox, oy, oz, extra, data);
            } catch (Exception ex) {
                // Ignore if fallback also fails
            }
        }
    }

    private static void fallbackParticle(World world, String name, Location loc, int count, double ox, double oy, double oz, double extra) {
        try {
            if (name.equals("SCULK_CHARGE_POP") || name.equals("SCULK_SOUL")) {
                world.spawnParticle(Particle.valueOf("SOUL"), loc, count, ox, oy, oz, extra);
            } else if (name.equals("SONIC_BOOM")) {
                world.spawnParticle(Particle.valueOf("EXPLOSION_LARGE"), loc, count, ox, oy, oz, extra);
            } else if (name.equals("ELECTRIC_SPARK") || name.equals("WAX_OFF")) {
                world.spawnParticle(Particle.valueOf("FIREWORKS_SPARK"), loc, count, ox, oy, oz, extra);
            }
        } catch (Exception ignored) {
            // Fallback to PORTAL if even SOUL fails (e.g. pre-1.16)
            try {
                world.spawnParticle(Particle.valueOf("PORTAL"), loc, count, ox, oy, oz, extra);
            } catch (Exception secondaryIgnored) {}
        }
    }

    /**
     * Plays a sound by name, falling back to older sounds if the sound doesn't exist.
     */
    public static void playSound(World world, Location loc, String soundName, float volume, float pitch) {
        try {
            Sound sound = Sound.valueOf(soundName);
            world.playSound(loc, sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            fallbackSoundWorld(world, loc, soundName, volume, pitch);
        }
    }

    public static void playSound(Player player, Location loc, String soundName, float volume, float pitch) {
        try {
            Sound sound = Sound.valueOf(soundName);
            player.playSound(loc, sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            fallbackSoundPlayer(player, loc, soundName, volume, pitch);
        }
    }

    private static void fallbackSoundWorld(World world, Location loc, String name, float vol, float pitch) {
        try {
            if (name.equals("ENTITY_WARDEN_SONIC_BOOM")) {
                world.playSound(loc, Sound.valueOf("ENTITY_ENDER_DRAGON_GROWL"), vol, pitch);
            } else if (name.equals("BLOCK_AMETHYST_BLOCK_CHIME")) {
                // Fallback to note block pling or chime
                try {
                    world.playSound(loc, Sound.valueOf("BLOCK_NOTE_BLOCK_CHIME"), vol, pitch);
                } catch (IllegalArgumentException e) {
                    world.playSound(loc, Sound.valueOf("BLOCK_NOTE_BLOCK_PLING"), vol, pitch);
                }
            }
        } catch (Exception ignored) {}
    }

    private static void fallbackSoundPlayer(Player player, Location loc, String name, float vol, float pitch) {
        try {
            if (name.equals("ENTITY_WARDEN_SONIC_BOOM")) {
                player.playSound(loc, Sound.valueOf("ENTITY_ENDER_DRAGON_GROWL"), vol, pitch);
            } else if (name.equals("BLOCK_AMETHYST_BLOCK_CHIME")) {
                try {
                    player.playSound(loc, Sound.valueOf("BLOCK_NOTE_BLOCK_CHIME"), vol, pitch);
                } catch (IllegalArgumentException e) {
                    player.playSound(loc, Sound.valueOf("BLOCK_NOTE_BLOCK_PLING"), vol, pitch);
                }
            }
        } catch (Exception ignored) {}
    }
}
