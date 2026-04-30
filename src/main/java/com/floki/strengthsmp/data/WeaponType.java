package com.floki.strengthsmp.data;

/**
 * Represents the different weapon classes in Strength SMP
 */
public enum WeaponType {
    SWORD("🗡", "Sword", "Dual-wield & Auto-crit passive"),
    AXE("🪓", "Axe", "Critical hits Stun + Kinetic Damage Storage"),
    TRIDENT("🔱", "Trident", "Lightning passive + Water Wave mobility"),
    SHIELD("🛡", "Shield", "Speed II on stun + 15s Immortality"),
    BOW("🏹", "Bow", "Linear Sonic Beam Ultimate (AOE)"),
    CROSSBOW("➶", "Crossbow", "Glowing arrows + Leash Ultimate");
    
    private final String icon;
    private final String displayName;
    private final String description;
    
    WeaponType(String icon, String displayName, String description) {
        this.icon = icon;
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isValidMaterial(org.bukkit.Material material) {
        String name = material.name();
        switch (this) {
            case SWORD:    return name.contains("SWORD");
            case AXE:      return name.contains("_AXE");
            case BOW:      return name.equals("BOW");
            case CROSSBOW: return name.equals("CROSSBOW");
            case TRIDENT:  return name.equals("TRIDENT");
            case SHIELD:   return name.equals("SHIELD");
            default:       return false;
        }
    }
    
    public static WeaponType fromMaterial(org.bukkit.Material material) {
        for (WeaponType type : values()) {
            if (type.isValidMaterial(material)) return type;
        }
        return null;
    }

    public static WeaponType getRandomWeapon() {
        WeaponType[] values = values();
        int index = java.util.concurrent.ThreadLocalRandom.current().nextInt(values.length);
        return values[index];
    }
}
