package com.floki.strengthsmp.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Compatibility utility to prevent NoSuchFieldError and NoClassDefFoundError
 * when using newer Minecraft sounds and particles on older versions (like 1.16.5),
 * or older particle names on newer versions (like 1.20.5+ / 1.21.1).
 */
public class CompatUtil {

    private static final Map<String, String> LEGACY_TO_MODERN = new HashMap<>();
    private static final Map<String, String> MODERN_TO_LEGACY = new HashMap<>();

    static {
        // Pre-1.20.5 particle name to 1.20.5+ (1.21.1) particle name mapping
        LEGACY_TO_MODERN.put("SPELL_WITCH", "WITCH");
        LEGACY_TO_MODERN.put("SPELL_INSTANT", "INSTANT_EFFECT");
        LEGACY_TO_MODERN.put("EXPLOSION_HUGE", "EXPLOSION_EMITTER");
        LEGACY_TO_MODERN.put("SMOKE_NORMAL", "SMOKE");
        LEGACY_TO_MODERN.put("ENCHANTMENT_TABLE", "ENCHANT");
        LEGACY_TO_MODERN.put("BLOCK_CRACK", "BLOCK");
        LEGACY_TO_MODERN.put("EXPLOSION_NORMAL", "POOF");
        LEGACY_TO_MODERN.put("WATER_SPLASH", "SPLASH");
        LEGACY_TO_MODERN.put("WATER_WAKE", "FISHING");
        LEGACY_TO_MODERN.put("CRIT_MAGIC", "ENCHANTED_HIT");
        LEGACY_TO_MODERN.put("FIREWORKS_SPARK", "FIREWORK");

        // Bidirectional loading
        for (Map.Entry<String, String> entry : LEGACY_TO_MODERN.entrySet()) {
            MODERN_TO_LEGACY.put(entry.getValue(), entry.getKey());
        }
    }

    /**
     * Resolves a particle enum dynamically to prevent compile/load time errors
     */
    private static Particle resolveParticle(String name) {
        try {
            return Particle.valueOf(name);
        } catch (IllegalArgumentException e) {
            // If the name failed to resolve, try translating
            String translated = LEGACY_TO_MODERN.get(name);
            if (translated != null) {
                try {
                    return Particle.valueOf(translated);
                } catch (IllegalArgumentException ignored) {}
            }
            
            translated = MODERN_TO_LEGACY.get(name);
            if (translated != null) {
                try {
                    return Particle.valueOf(translated);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return null;
    }

    /**
     * Spawns a particle by name, falling back to older particles if the particle doesn't exist.
     */
    public static void spawnParticle(World world, String particleName, Location loc, int count, double ox, double oy, double oz, double extra) {
        try {
            Particle particle = resolveParticle(particleName);
            if (particle != null) {
                world.spawnParticle(particle, loc, count, ox, oy, oz, extra);
            } else {
                fallbackParticle(world, particleName, loc, count, ox, oy, oz, extra);
            }
        } catch (Exception e) {
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
            Particle particle = resolveParticle(particleName);
            if (particle != null) {
                world.spawnParticle(particle, loc, count, ox, oy, oz, extra, data);
            } else {
                world.spawnParticle(Particle.valueOf("FALLING_DUST"), loc, count, ox, oy, oz, extra, data);
            }
        } catch (Exception e) {
            try {
                world.spawnParticle(Particle.valueOf("FALLING_DUST"), loc, count, ox, oy, oz, extra, data);
            } catch (Exception ignored) {}
        }
    }

    private static void fallbackParticle(World world, String name, Location loc, int count, double ox, double oy, double oz, double extra) {
        try {
            if (name.equals("SCULK_CHARGE_POP") || name.equals("SCULK_SOUL")) {
                world.spawnParticle(Particle.valueOf("SOUL"), loc, count, ox, oy, oz, extra);
            } else if (name.equals("SONIC_BOOM")) {
                world.spawnParticle(Particle.valueOf("EXPLOSION_LARGE"), loc, count, ox, oy, oz, extra);
            } else if (name.equals("ELECTRIC_SPARK") || name.equals("WAX_OFF")) {
                Particle fw = resolveParticle("FIREWORKS_SPARK");
                if (fw != null) {
                    world.spawnParticle(fw, loc, count, ox, oy, oz, extra);
                } else {
                    world.spawnParticle(Particle.valueOf("PORTAL"), loc, count, ox, oy, oz, extra);
                }
            } else {
                // Generic safe fallback for all other enums
                world.spawnParticle(Particle.valueOf("PORTAL"), loc, count, ox, oy, oz, extra);
            }
        } catch (Exception ignored) {
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
