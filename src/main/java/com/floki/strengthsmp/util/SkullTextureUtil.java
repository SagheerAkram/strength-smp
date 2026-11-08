/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.inventory.meta.SkullMeta
 */
package com.floki.strengthsmp.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class SkullTextureUtil {
    public static ItemStack createCustomHead(String base64Texture) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (base64Texture == null || base64Texture.isEmpty()) {
            return head;
        }
        try {
            SkullMeta meta = (SkullMeta)head.getItemMeta();
            if (meta == null) {
                return head;
            }
            UUID profileId = UUID.nameUUIDFromBytes(base64Texture.getBytes());
            boolean success = false;
            try {
                Class<?> playerProfileClass = Class.forName("org.bukkit.profile.PlayerProfile");
                Class<?> playerTexturesClass = Class.forName("org.bukkit.profile.PlayerTextures");
                Method createProfileMethod = Bukkit.class.getMethod("createPlayerProfile", UUID.class, String.class);
                Object profile = createProfileMethod.invoke(null, profileId, "CustomHead");
                Method getTexturesMethod = playerProfileClass.getMethod("getTextures", new Class[0]);
                Object textures = getTexturesMethod.invoke(profile, new Object[0]);
                String textureUrl = SkullTextureUtil.extractTextureUrl(base64Texture);
                if (textureUrl != null) {
                    URL skinUrl = URI.create(textureUrl.replace("http://", "https://")).toURL();
                    Method setSkinMethod = playerTexturesClass.getMethod("setSkin", URL.class);
                    setSkinMethod.invoke(textures, skinUrl);
                    Method setTexturesMethod = playerProfileClass.getMethod("setTextures", playerTexturesClass);
                    setTexturesMethod.invoke(profile, textures);
                    Method setOwnerProfileMethod = meta.getClass().getMethod("setOwnerProfile", playerProfileClass);
                    setOwnerProfileMethod.invoke((Object)meta, profile);
                    success = true;
                }
            }
            catch (Throwable playerProfileClass) {
                // empty catch block
            }
            if (!success) {
                try {
                    Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
                    Constructor<?> gameProfileConstructor = gameProfileClass.getConstructor(UUID.class, String.class);
                    Object gameProfile = gameProfileConstructor.newInstance(profileId, "CustomHead");
                    Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
                    Constructor<?> propertyConstructor = propertyClass.getConstructor(String.class, String.class);
                    Object property = propertyConstructor.newInstance("textures", base64Texture);
                    Method getPropertiesMethod = gameProfileClass.getMethod("getProperties", new Class[0]);
                    Object propertiesMap = getPropertiesMethod.invoke(gameProfile, new Object[0]);
                    Method putMethod = propertiesMap.getClass().getMethod("put", Object.class, Object.class);
                    putMethod.invoke(propertiesMap, "textures", property);
                    Field profileField = meta.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                    profileField.set(meta, gameProfile);
                    success = true;
                }
                catch (Throwable t) {
                    Bukkit.getLogger().log(Level.WARNING, "[StrengthSMP] Failed legacy game profile fallback: " + t.getMessage());
                }
            }
            if (success) {
                head.setItemMeta((ItemMeta)meta);
            }
        }
        catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "[StrengthSMP] Failed to create custom head: " + e.getMessage());
        }
        return head;
    }

    private static String extractTextureUrl(String base64) {
        try {
            String json = new String(Base64.getDecoder().decode(base64));
            int urlIndex = json.indexOf("\"url\"");
            if (urlIndex == -1) {
                return null;
            }
            int colonIndex = json.indexOf(58, urlIndex);
            if (colonIndex == -1) {
                return null;
            }
            int startQuote = json.indexOf(34, colonIndex);
            if (startQuote == -1) {
                return null;
            }
            int endQuote = json.indexOf(34, startQuote + 1);
            if (endQuote == -1) {
                return null;
            }
            String url = json.substring(startQuote + 1, endQuote);
            if (!url.startsWith("http://textures.minecraft.net/") && !url.startsWith("https://textures.minecraft.net/")) {
                return null;
            }
            return url;
        }
        catch (Exception e) {
            return null;
        }
    }
}

