/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.Particle
 *  org.bukkit.Sound
 *  org.bukkit.World
 *  org.bukkit.attribute.Attribute
 *  org.bukkit.entity.Player
 */
package com.floki.strengthsmp.util;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public class CompatUtil {
    private static final Map<String, String> LEGACY_TO_MODERN = new HashMap<String, String>();
    private static final Map<String, String> MODERN_TO_LEGACY = new HashMap<String, String>();

    private static Particle resolveParticle(String name) {
        if (name == null) return null;
        try {
            java.lang.reflect.Method m = Particle.class.getMethod("valueOf", String.class);
            return (Particle) m.invoke(null, name);
        }
        catch (Throwable t) {
            String translated = LEGACY_TO_MODERN.get(name);
            if (translated != null) {
                try {
                    java.lang.reflect.Method m = Particle.class.getMethod("valueOf", String.class);
                    return (Particle) m.invoke(null, translated);
                }
                catch (Throwable ignored) {}
            }
            if ((translated = MODERN_TO_LEGACY.get(name)) != null) {
                try {
                    java.lang.reflect.Method m = Particle.class.getMethod("valueOf", String.class);
                    return (Particle) m.invoke(null, translated);
                }
                catch (Throwable ignored) {}
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
        catch (Throwable t) {
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
                CompatUtil.fallbackParticle(world, particleName, loc, count, ox, oy, oz, extra);
            }
        }
        catch (Throwable t) {
            CompatUtil.fallbackParticle(world, particleName, loc, count, ox, oy, oz, extra);
        }
    }

    private static void fallbackParticle(World world, String name, Location loc, int count, double ox, double oy, double oz, double extra) {
        try {
            Particle portal = CompatUtil.resolveParticle("PORTAL");
            if (portal != null) {
                world.spawnParticle(portal, loc, count, ox, oy, oz, extra);
            }
        }
        catch (Throwable ignored) {}
    }

    public static void playSound(World world, Location loc, String soundName, float volume, float pitch) {
        if (world == null || loc == null || soundName == null) return;
        try {
            java.lang.reflect.Method m = Sound.class.getMethod("valueOf", String.class);
            Sound sound = (Sound) m.invoke(null, soundName);
            if (sound != null) {
                world.playSound(loc, sound, volume, pitch);
                return;
            }
        } catch (Throwable ignored) {}
        try {
            String mcSound = soundName.toLowerCase().replace('_', '.');
            world.playSound(loc, mcSound, volume, pitch);
        } catch (Throwable ignored) {}
    }

    public static void playSound(Player player, Location loc, String soundName, float volume, float pitch) {
        if (player == null || loc == null || soundName == null) return;
        try {
            java.lang.reflect.Method m = Sound.class.getMethod("valueOf", String.class);
            Sound sound = (Sound) m.invoke(null, soundName);
            if (sound != null) {
                player.playSound(loc, sound, volume, pitch);
                return;
            }
        } catch (Throwable ignored) {}
        try {
            String mcSound = soundName.toLowerCase().replace('_', '.');
            player.playSound(loc, mcSound, volume, pitch);
        } catch (Throwable ignored) {}
    }

    /**
     * Resolves an Attribute by name, trying alternate names for cross-version compat.
     * e.g. GENERIC_MAX_HEALTH (pre-1.21.1) vs MAX_HEALTH (1.21.1+)
     */
    public static Attribute resolveAttribute(String... names) {
        for (String name : names) {
            try {
                return Attribute.valueOf(name);
            } catch (IllegalArgumentException ignored) {
                // try next name
            }
        }
        return null;
    }

    /**
     * Resolves a PotionEffectType by its static field name via reflection,
     * trying each name in order — handles renames across MC versions:
     *   SLOW → SLOWNESS (1.21.x)
     *   JUMP → JUMP_BOOST (1.21.x)
     *   DAMAGE_RESISTANCE → RESISTANCE (1.21.x)
     *   INCREASE_DAMAGE → STRENGTH (1.21.x)
     */
    public static org.bukkit.potion.PotionEffectType resolvePotionEffect(String... fieldNames) {
        for (String name : fieldNames) {
            try {
                java.lang.reflect.Field f = org.bukkit.potion.PotionEffectType.class.getField(name);
                Object val = f.get(null);
                if (val instanceof org.bukkit.potion.PotionEffectType) {
                    return (org.bukkit.potion.PotionEffectType) val;
                }
            } catch (Exception ignored) {
                // try next name
            }
        }
        return null;
    }

    /**
     * Resolves an Enchantment by its static field name via reflection,
     * trying each name in order — handles renames across MC versions:
     *   DURABILITY → UNBREAKING (1.21.x)
     */
    public static org.bukkit.enchantments.Enchantment resolveEnchantment(String... fieldNames) {
        for (String name : fieldNames) {
            try {
                java.lang.reflect.Field f = org.bukkit.enchantments.Enchantment.class.getField(name);
                Object val = f.get(null);
                if (val instanceof org.bukkit.enchantments.Enchantment) {
                    return (org.bukkit.enchantments.Enchantment) val;
                }
            } catch (Exception ignored) {
                // try next name
            }
        }
        return null;
    }

    static {
        LEGACY_TO_MODERN.put("SPELL_WITCH", "WITCH");
        LEGACY_TO_MODERN.put("SPELL_INSTANT", "INSTANT_EFFECT");
        LEGACY_TO_MODERN.put("EXPLOSION_HUGE", "EXPLOSION_EMITTER");
        // EXPLOSION_LARGE (medium) was renamed to EXPLOSION in 1.21.4
        LEGACY_TO_MODERN.put("EXPLOSION_LARGE", "EXPLOSION");
        LEGACY_TO_MODERN.put("SMOKE_NORMAL", "SMOKE");
        LEGACY_TO_MODERN.put("ENCHANTMENT_TABLE", "ENCHANT");
        LEGACY_TO_MODERN.put("BLOCK_CRACK", "BLOCK");
        LEGACY_TO_MODERN.put("EXPLOSION_NORMAL", "POOF");
        LEGACY_TO_MODERN.put("WATER_SPLASH", "SPLASH");
        LEGACY_TO_MODERN.put("WATER_WAKE", "FISHING");
        LEGACY_TO_MODERN.put("CRIT_MAGIC", "ENCHANTED_HIT");
        LEGACY_TO_MODERN.put("FIREWORKS_SPARK", "FIREWORK");
        // TOTEM was renamed to TOTEM_OF_UNDYING in 1.21.4
        LEGACY_TO_MODERN.put("TOTEM", "TOTEM_OF_UNDYING");
        LEGACY_TO_MODERN.put("SMOKE_LARGE", "LARGE_SMOKE");
        LEGACY_TO_MODERN.put("VILLAGER_HAPPY", "HAPPY_VILLAGER");
        LEGACY_TO_MODERN.put("VILLAGER_ANGRY", "ANGRY_VILLAGER");
        for (Map.Entry<String, String> entry : LEGACY_TO_MODERN.entrySet()) {
            MODERN_TO_LEGACY.put(entry.getValue(), entry.getKey());
        }
    }
}

