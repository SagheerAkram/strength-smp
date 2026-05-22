package com.floki.strengthsmp.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Utility class for creating custom-textured player heads.
 * Works across all versions from 1.16.5 to 1.21.1+ using reflection
 * to adapt to Spigot's profile APIs without compile-time errors.
 */
public class SkullTextureUtil {

    /**
     * Creates a custom-textured player head from a Base64 texture value.
     * Falls back to a regular PLAYER_HEAD if the texture fails to apply.
     *
     * @param base64Texture The Base64-encoded texture JSON from minecraft-heads.com
     * @return An ItemStack of PLAYER_HEAD with the custom texture applied
     */
    public static ItemStack createCustomHead(String base64Texture) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);

        if (base64Texture == null || base64Texture.isEmpty()) {
            return head;
        }

        try {
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta == null) return head;

            // Generate a deterministic UUID based on the texture Base64 to ensure proper caching
            UUID profileId = UUID.nameUUIDFromBytes(base64Texture.getBytes());
            boolean success = false;

            // 1. Try the modern PlayerProfile Spigot API (1.18+) via reflection
            try {
                Class<?> playerProfileClass = Class.forName("org.bukkit.profile.PlayerProfile");
                Class<?> playerTexturesClass = Class.forName("org.bukkit.profile.PlayerTextures");

                // PlayerProfile profile = Bukkit.createPlayerProfile(profileId, "CustomHead");
                Method createProfileMethod = Bukkit.class.getMethod("createPlayerProfile", UUID.class, String.class);
                Object profile = createProfileMethod.invoke(null, profileId, "CustomHead");

                // PlayerTextures textures = profile.getTextures();
                Method getTexturesMethod = playerProfileClass.getMethod("getTextures");
                Object textures = getTexturesMethod.invoke(profile);

                // Extract texture URL
                String textureUrl = extractTextureUrl(base64Texture);
                if (textureUrl != null) {
                    URL skinUrl = URI.create(textureUrl.replace("http://", "https://")).toURL();
                    // textures.setSkin(skinUrl);
                    Method setSkinMethod = playerTexturesClass.getMethod("setSkin", URL.class);
                    setSkinMethod.invoke(textures, skinUrl);

                    // profile.setTextures(textures);
                    Method setTexturesMethod = playerProfileClass.getMethod("setTextures", playerTexturesClass);
                    setTexturesMethod.invoke(profile, textures);

                    // meta.setOwnerProfile(profile);
                    Method setOwnerProfileMethod = meta.getClass().getMethod("setOwnerProfile", playerProfileClass);
                    setOwnerProfileMethod.invoke(meta, profile);
                    success = true;
                }
            } catch (Throwable ignored) {
                // Fall back to legacy GameProfile reflection
            }

            // 2. Try the legacy GameProfile reflection (1.16 - 1.17)
            if (!success) {
                try {
                    Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
                    Constructor<?> gameProfileConstructor = gameProfileClass.getConstructor(UUID.class, String.class);
                    Object gameProfile = gameProfileConstructor.newInstance(profileId, "CustomHead");

                    Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
                    Constructor<?> propertyConstructor = propertyClass.getConstructor(String.class, String.class);
                    Object property = propertyConstructor.newInstance("textures", base64Texture);

                    Method getPropertiesMethod = gameProfileClass.getMethod("getProperties");
                    Object propertiesMap = getPropertiesMethod.invoke(gameProfile);

                    // propertiesMap is a Multimap (from Guava, which is bundled in Spigot/Paper)
                    Method putMethod = propertiesMap.getClass().getMethod("put", Object.class, Object.class);
                    putMethod.invoke(propertiesMap, "textures", property);

                    Field profileField = meta.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                    profileField.set(meta, gameProfile);
                    success = true;
                } catch (Throwable t) {
                    Bukkit.getLogger().log(Level.WARNING, "[StrengthSMP] Failed legacy game profile fallback: " + t.getMessage());
                }
            }

            if (success) {
                head.setItemMeta(meta);
            }

        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "[StrengthSMP] Failed to create custom head: " + e.getMessage());
        }

        return head;
    }

    /**
     * Extracts the skin texture URL from a Base64-encoded texture string.
     *
     * @param base64 The Base64 string
     * @return The texture URL, or null if parsing fails
     */
    private static String extractTextureUrl(String base64) {
        try {
            String json = new String(Base64.getDecoder().decode(base64));

            // Simple JSON parsing without dependencies - find the URL value
            int urlIndex = json.indexOf("\"url\"");
            if (urlIndex == -1) return null;

            // Find the colon after "url"
            int colonIndex = json.indexOf(':', urlIndex);
            if (colonIndex == -1) return null;

            // Find the opening quote of the URL value
            int startQuote = json.indexOf('"', colonIndex);
            if (startQuote == -1) return null;

            // Find the closing quote
            int endQuote = json.indexOf('"', startQuote + 1);
            if (endQuote == -1) return null;

            String url = json.substring(startQuote + 1, endQuote);

            // Validate it's a Mojang texture URL
            if (!url.startsWith("http://textures.minecraft.net/") &&
                !url.startsWith("https://textures.minecraft.net/")) {
                return null;
            }

            return url;
        } catch (Exception e) {
            return null;
        }
    }
}
