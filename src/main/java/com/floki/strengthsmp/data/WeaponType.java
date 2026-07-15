/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 */
package com.floki.strengthsmp.data;

import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;

public enum WeaponType {
    SWORD("\ud83d\udde1", "Sword", "Dual-wield & Auto-crit passive"),
    AXE("\ud83e\ude93", "Axe", "Critical hits Stun + Kinetic Damage Storage"),
    TRIDENT("\ud83d\udd31", "Trident", "Lightning passive + Water Wave mobility"),
    SHIELD("\ud83d\udee1", "Shield", "Speed II on stun + 15s Immortality"),
    BOW("\ud83c\udff9", "Bow", "Linear Sonic Beam Ultimate (AOE)"),
    CROSSBOW("\u27b6", "Crossbow", "Glowing arrows + Leash Ultimate");

    private final String icon;
    private final String displayName;
    private final String description;

    private WeaponType(String icon, String displayName, String description) {
        this.icon = icon;
        this.displayName = displayName;
        this.description = description;
    }

    public String getIcon() {
        return this.icon;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isValidMaterial(Material material) {
        String name = material.name();
        switch (this) {
            case SWORD: {
                return name.contains("SWORD");
            }
            case AXE: {
                return name.contains("_AXE");
            }
            case BOW: {
                return name.equals("BOW");
            }
            case CROSSBOW: {
                return name.equals("CROSSBOW");
            }
            case TRIDENT: {
                return name.equals("TRIDENT");
            }
            case SHIELD: {
                return name.equals("SHIELD");
            }
        }
        return false;
    }

    public static WeaponType fromMaterial(Material material) {
        for (WeaponType type : WeaponType.values()) {
            if (!type.isValidMaterial(material)) continue;
            return type;
        }
        return null;
    }

    public static WeaponType getRandomWeapon() {
        WeaponType[] values = WeaponType.values();
        int index = ThreadLocalRandom.current().nextInt(values.length);
        return values[index];
    }
}

