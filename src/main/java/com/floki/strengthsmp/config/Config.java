package com.floki.strengthsmp.config;

import com.floki.strengthsmp.StrengthSMP;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Configuration manager for Strength SMP.
 * Every tunable value has a sensible default so the plugin works out-of-the-box.
 */
public class Config {

    private final StrengthSMP plugin;
    private final ConfigManager configManager;

    public Config(StrengthSMP plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getInternalConfigManager();
    }

    /** Reload all configuration files */
    public void reload() {
        configManager.reloadAll();
    }

    public FileConfiguration getC() { return configManager.getConfig(); }
    public FileConfiguration getW() { return configManager.getWeapons(); }
    public FileConfiguration getM() { return configManager.getMessages(); }
    public FileConfiguration getE() { return configManager.getEvents(); }
    public FileConfiguration getD() { return configManager.getDiscord(); }
    
    public FileConfiguration getMessages() { return getM(); }
    public FileConfiguration getMainConfig() { return getC(); }
    
    public String getMessage(String path, String def) {
        return getM().getString(path, def);
    }

    // ════════════════════════════════════════════════════════════════
    //  STRENGTH
    // ════════════════════════════════════════════════════════════════
    public int getMinStrength()       { return getC().getInt("strength.min",             0); }
    public int getMaxStrength()       { return getC().getInt("strength.max",             5); }
    public int getDefaultStrength()   { return getC().getInt("strength.default",         0); }
    public int getDeathPenalty()      { return getC().getInt("strength.death-penalty",   1); }
    public int getDeathGain()         { return getC().getInt("strength.death-gain",      1); }
    public int getUnderdogMultiplier(){ return getC().getInt("strength.underdog-multiplier", 2); }
    public int getUnderdogThreshold() { return getC().getInt("strength.underdog-threshold", 3); }

    // ... (rest of the getters will need to be updated to use getC(), getW(), etc.)


    // ════════════════════════════════════════════════════════════════
    //  DAMAGE SCALING
    // ════════════════════════════════════════════════════════════════
    public boolean isDamageScalingEnabled()      { return getC().getBoolean("damage-scaling.enabled",               true); }
    public boolean isOnlyOnWeaponMatch()         { return getC().getBoolean("damage-scaling.only-on-weapon-match",  true); }
    public double  getDamagePerStrength()        { return getC().getDouble ("damage-scaling.per-strength",          0.5);  }
    public double  getVictimReductionPerStrength(){ return getC().getDouble("damage-scaling.victim-reduction-per-strength", 0.3); }

    // ════════════════════════════════════════════════════════════════
    //  WEAPON — SWORD
    // ════════════════════════════════════════════════════════════════
    public int    getSwordComboHits()       { return getW().getInt   ("weapons.sword.passive-combo-hits",  3);    }
    public double getSwordPassiveMult()     { return getW().getDouble("weapons.sword.passive-damage-mult", 1.25); }
    public double getSwordShockwaveDamage() { return getW().getDouble("weapons.sword.shockwave-damage",    0.0);  }
    public double getSwordShockwaveRadius() { return getW().getDouble("weapons.sword.shockwave-radius",    0.0);  }

    // ════════════════════════════════════════════════════════════════
    //  WEAPON — AXE
    // ════════════════════════════════════════════════════════════════
    public int    getAxeCritHits()      { return getW().getInt   ("weapons.axe.passive-crit-hits",  5);   }
    public int    getAxeStunTicks()     { return getW().getInt   ("weapons.axe.stun-duration-ticks", 10);  }
    public double getAxeBleedDamage()   { return getW().getDouble("weapons.axe.bleed-damage",        1.5); }
    public int    getAxeBleedDelayTicks(){ return getW().getInt  ("weapons.axe.bleed-delay-ticks",   40);  }

    // ════════════════════════════════════════════════════════════════
    //  WEAPON — TRIDENT
    // ════════════════════════════════════════════════════════════════
    public int    getTridentHitsForLightning()  { return getW().getInt   ("weapons.trident.hits-for-lightning",     4);   }
    public double getTridentLightningExtraDamage(){ return getW().getDouble("weapons.trident.lightning-extra-damage", 0.5); }
    public int    getTridentSpeedLevel()        { return getW().getInt   ("weapons.trident.speed-level",            0);   }
    public int    getTridentSpeedTicks()        { return getW().getInt   ("weapons.trident.speed-duration-ticks",   60);  }

    // ════════════════════════════════════════════════════════════════
    //  WEAPON — SHIELD
    // ════════════════════════════════════════════════════════════════
    public int    getShieldSpeedLevel()              { return getW().getInt   ("weapons.shield.speed-level",              1);   }
    public int    getShieldSpeedTicks()              { return getW().getInt   ("weapons.shield.speed-duration-ticks",      100); }
    public double getShieldCounterDamage()           { return getW().getDouble("weapons.shield.counter-damage",           2.0); }
    public double getShieldCounterRadius()           { return getW().getDouble("weapons.shield.counter-radius",           3.0); }
    public int    getShieldCounterCooldownSeconds()  { return getW().getInt   ("weapons.shield.counter-cooldown-seconds", 3);   }

    // ════════════════════════════════════════════════════════════════
    //  WEAPON — BOW
    // ════════════════════════════════════════════════════════════════
    public double getBowExplosionRadius()      { return getW().getDouble("weapons.bow.explosion-radius",       4.0); }
    public double getBowExplosionDamage()      { return getW().getDouble("weapons.bow.explosion-damage",       3.0); }
    public double getBowKnockbackHorizontal()  { return getW().getDouble("weapons.bow.explosion-knockback-h", 2.5); }
    public double getBowKnockbackVertical()    { return getW().getDouble("weapons.bow.explosion-knockback-v", 0.6); }

    // ════════════════════════════════════════════════════════════════
    //  WEAPON — CROSSBOW
    // ════════════════════════════════════════════════════════════════
    public double getCrossbowPullStrength() { return getW().getDouble("weapons.crossbow.pull-strength",       1.5); }
    public double getCrossbowChainRadius()  { return getW().getDouble("weapons.crossbow.chain-radius",        3.0); }
    public double getCrossbowChainPull()    { return getW().getDouble("weapons.crossbow.chain-pull-strength", 1.2); }

    // ════════════════════════════════════════════════════════════════
    //  WEAPON COSMETICS
    // ════════════════════════════════════════════════════════════════
    public String  getWeaponCosmeticName(com.floki.strengthsmp.data.WeaponType type) { 
        return getW().getString("weapons." + type.name().toLowerCase() + ".cosmetic-name", type.getDisplayName()); 
    }
    public boolean shouldRenameOnBind(com.floki.strengthsmp.data.WeaponType type) { 
        return getW().getBoolean("weapons." + type.name().toLowerCase() + ".rename-on-bind", true); 
    }

    // ════════════════════════════════════════════════════════════════
    //  LEGACY / MISC (kept for compatibility)
    // ════════════════════════════════════════════════════════════════
    public int getCooldown(String weapon)   { return getC().getInt("cooldowns." + weapon, 60); }
    public int getDuration(String weapon)   { return getC().getInt("durations." + weapon, 15); }

    // Combat
    public int getGracePeriod()            { return getC().getInt("combat.grace-period",          5);   }
    public int getCombatTagDuration()      { return getC().getInt("combat.combat-tag-duration",  15);   }
    public int getPingLogoutThreshold()    { return getC().getInt("combat.ping-logout-threshold", 500); }

    // Anti-Farm
    public int getMaxKillsSamePlayer()     { return getC().getInt("anti-farm.max-kills-same-player", 2);  }
    public int getAntiFarmWindowMinutes()  { return getC().getInt("anti-farm.window-minutes",        10); }

    // Discord
    // Discord (read only from discord.yml)
    public boolean isDiscordEnabled()       { return getD().getBoolean("enabled",          true); }
    public String  getBotToken()            { return getD().getString ("bot-token",           ""); }
    public String  getBotName()             { return getD().getString ("bot-name",    "FlokiBot"); }
    public String  getDashboardChannelId()  { return getD().getString ("channels.dashboard",  ""); }
    public String  getLeaderboardChannelId(){ return getD().getString ("channels.leaderboard",""); }
    public String  getWelcomeChannelId()    { return getD().getString ("channels.welcome",    ""); }
    public String  getAuditChannelId()      { return getD().getString ("channels.audit",      ""); }
    public String  getMonarchChannelId()    { return getD().getString ("channels.monarch",    ""); }
    public int     getDiscordUpdateInterval(){ return getD().getInt   ("update-interval",     30); }

    // Discord Leaderboard (discord.yml)
    public boolean isLbEnabled()       { return getD().getBoolean("leaderboard.enabled", true); }
    public int     getLbUpdateMins()   { return getD().getInt("leaderboard.update-minutes", 5); }
    public String  getLbColor()        { return getD().getString("leaderboard.color", "gold"); }
    public boolean lbShowKills()       { return getD().getBoolean("leaderboard.show-kills", true); }
    public boolean lbShowBounties()    { return getD().getBoolean("leaderboard.show-bounties", true); }
    public boolean lbShowMonarch()     { return getD().getBoolean("leaderboard.show-monarch", true); }
    public boolean lbShowStatus()      { return getD().getBoolean("leaderboard.show-status", true); }

    // Potions
    public boolean isBanStrengthPotions()  { return getC().getBoolean("potions.ban-strength-potions", true); }

    // System
    public boolean isActiveOnStartup()          { return getC().getBoolean("system.active-on-startup",       false); }
    public boolean isMaintenanceKeepInventory() { return getC().getBoolean("system.maintenance-keep-inventory", true); }

    // Experimental
    public boolean isExperimentalEnabled()      { return getC().getBoolean("experimental.enabled",           false); }

    // ════════════════════════════════════════════════════════════════
    //  TOTEM LIMIT
    // ════════════════════════════════════════════════════════════════
    public boolean isTotemLimitEnabled()        { return getC().getBoolean("totem-limit.enabled", true); }
    public int     getTotemMax()                { return getC().getInt("totem-limit.max", 2); }
    public boolean isTotemDropExcess()          { return getC().getBoolean("totem-limit.drop-excess", true); }
    public int     getTotemMessageCooldown()    { return getC().getInt("totem-limit.message-cooldown-seconds", 2); }
}
