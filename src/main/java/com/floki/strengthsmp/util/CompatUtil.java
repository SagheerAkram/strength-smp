/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.Particle
 *  org.bukkit.Sound
 *  org.bukkit.World
 *  org.bukkit.entity.Player
 */
package com.floki.strengthsmp.util;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class CompatUtil {
    private static final Map<String, String> LEGACY_TO_MODERN = new HashMap<String, String>();
    private static final Map<String, String> MODERN_TO_LEGACY = new HashMap<String, String>();

    private static Particle resolveParticle(String name) {
        try {
            return Particle.valueOf((String)name);
        }
        catch (IllegalArgumentException e) {
            String translated = LEGACY_TO_MODERN.get(name);
            if (translated != null) {
                try {
                    return Particle.valueOf((String)translated);
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    // empty catch block
                }
            }
            if ((translated = MODERN_TO_LEGACY.get(name)) != null) {
                try {
                    return Particle.valueOf((String)translated);
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    // empty catch block
                }
            }
            return null;
        }
    }

    public static void spawnParticle(World world, String particleName, Location loc, int count, double ox, double oy, double oz, double extra) {
        try {
            Particle particle = CompatUtil.resolveParticle(particleName);
            if (particle != null) {
                world.spawnParticle(particle, loc, count, ox, oy, oz, extra);
            } else {
                CompatUtil.fallbackParticle(world, particleName, loc, count, ox, oy, oz, extra);
            }
        }
        catch (Exception e) {
            CompatUtil.fallbackParticle(world, particleName, loc, count, ox, oy, oz, extra);
        }
    }

    public static void spawnParticle(World world, String particleName, Location loc, int count) {
        CompatUtil.spawnParticle(world, particleName, loc, count, 0.0, 0.0, 0.0, 0.0);
    }

    public static void spawnParticle(World world, String particleName, Location loc, int count, double ox, double oy, double oz, double extra, Object data) {
        try {
            Particle particle = CompatUtil.resolveParticle(particleName);
            if (particle != null) {
                world.spawnParticle(particle, loc, count, ox, oy, oz, extra, data);
            } else {
                world.spawnParticle(Particle.valueOf((String)"FALLING_DUST"), loc, count, ox, oy, oz, extra, data);
            }
        }
        catch (Exception e) {
            try {
                world.spawnParticle(Particle.valueOf((String)"FALLING_DUST"), loc, count, ox, oy, oz, extra, data);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    private static void fallbackParticle(World world, String name, Location loc, int count, double ox, double oy, double oz, double extra) {
        try {
            if (name.equals("SCULK_CHARGE_POP") || name.equals("SCULK_SOUL")) {
                world.spawnParticle(Particle.valueOf((String)"SOUL"), loc, count, ox, oy, oz, extra);
            } else if (name.equals("SONIC_BOOM")) {
                world.spawnParticle(Particle.valueOf((String)"EXPLOSION_LARGE"), loc, count, ox, oy, oz, extra);
            } else if (name.equals("ELECTRIC_SPARK") || name.equals("WAX_OFF")) {
                Particle fw = CompatUtil.resolveParticle("FIREWORKS_SPARK");
                if (fw != null) {
                    world.spawnParticle(fw, loc, count, ox, oy, oz, extra);
                } else {
                    world.spawnParticle(Particle.valueOf((String)"PORTAL"), loc, count, ox, oy, oz, extra);
                }
            } else {
                world.spawnParticle(Particle.valueOf((String)"PORTAL"), loc, count, ox, oy, oz, extra);
            }
        }
        catch (Exception ignored) {
            try {
                world.spawnParticle(Particle.valueOf((String)"PORTAL"), loc, count, ox, oy, oz, extra);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public static void playSound(World world, Location loc, String soundName, float volume, float pitch) {
        try {
            Sound sound = Sound.valueOf((String)soundName);
            world.playSound(loc, sound, volume, pitch);
        }
        catch (IllegalArgumentException e) {
            CompatUtil.fallbackSoundWorld(world, loc, soundName, volume, pitch);
        }
    }

    public static void playSound(Player player, Location loc, String soundName, float volume, float pitch) {
        try {
            Sound sound = Sound.valueOf((String)soundName);
            player.playSound(loc, sound, volume, pitch);
        }
        catch (IllegalArgumentException e) {
            CompatUtil.fallbackSoundPlayer(player, loc, soundName, volume, pitch);
        }
    }

    private static void fallbackSoundWorld(World world, Location loc, String name, float vol, float pitch) {
        try {
            if (name.equals("ENTITY_WARDEN_SONIC_BOOM")) {
                world.playSound(loc, Sound.valueOf((String)"ENTITY_ENDER_DRAGON_GROWL"), vol, pitch);
            } else if (name.equals("BLOCK_AMETHYST_BLOCK_CHIME")) {
                try {
                    world.playSound(loc, Sound.valueOf((String)"BLOCK_NOTE_BLOCK_CHIME"), vol, pitch);
                }
                catch (IllegalArgumentException e) {
                    world.playSound(loc, Sound.valueOf((String)"BLOCK_NOTE_BLOCK_PLING"), vol, pitch);
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static void fallbackSoundPlayer(Player player, Location loc, String name, float vol, float pitch) {
        try {
            if (name.equals("ENTITY_WARDEN_SONIC_BOOM")) {
                player.playSound(loc, Sound.valueOf((String)"ENTITY_ENDER_DRAGON_GROWL"), vol, pitch);
            } else if (name.equals("BLOCK_AMETHYST_BLOCK_CHIME")) {
                try {
                    player.playSound(loc, Sound.valueOf((String)"BLOCK_NOTE_BLOCK_CHIME"), vol, pitch);
                }
                catch (IllegalArgumentException e) {
                    player.playSound(loc, Sound.valueOf((String)"BLOCK_NOTE_BLOCK_PLING"), vol, pitch);
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    static {
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
        for (Map.Entry<String, String> entry : LEGACY_TO_MODERN.entrySet()) {
            MODERN_TO_LEGACY.put(entry.getValue(), entry.getKey());
        }
    }
}

