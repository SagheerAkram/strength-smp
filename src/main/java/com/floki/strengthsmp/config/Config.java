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
    private int getWeaponInt(String keyW, String keyC, int def) {
        if (getW().contains(keyW)) return getW().getInt(keyW);
        return getC().getInt(keyC, def);
    }

    private double getWeaponDouble(String keyW, String keyC, double def) {
        if (getW().contains(keyW)) return getW().getDouble(keyW);
        return getC().getDouble(keyC, def);
    }

    //  WEAPON — SWORD
    // ════════════════════════════════════════════════════════════════
    public int    getSwordComboHits()       { return getWeaponInt("sword.passive-combo-hits", "weapons.sword.passive-combo-hits", 3); }
    public double getSwordPassiveMult()     { return getWeaponDouble("sword.passive-damage-mult", "weapons.sword.passive-damage-mult", 1.25); }
    public double getSwordShockwaveDamage() { return getWeaponDouble("sword.shockwave.damage", "weapons.sword.shockwave-damage", 0.0); }
    public double getSwordShockwaveRadius() { return getWeaponDouble("sword.shockwave.radius", "weapons.sword.shockwave-radius", 0.0); }

    // ════════════════════════════════════════════════════════════════
    //  WEAPON — AXE
    // ════════════════════════════════════════════════════════════════
    public int    getAxeCritHits()      { return getWeaponInt("axe.passive-crit-hits", "weapons.axe.passive-crit-hits", 5); }
    public int    getAxeStunTicks()     { return getWeaponInt("axe.stun-duration-ticks", "weapons.axe.stun-duration-ticks", 10); }
    public double getAxeBleedDamage()   { return getWeaponDouble("axe.bleed.damage", "weapons.axe.bleed-damage", 1.5); }
    public int    getAxeBleedDelayTicks(){ return getWeaponInt("axe.bleed.ticks", "weapons.axe.bleed-delay-ticks", 40); }

    // ════════════════════════════════════════════════════════════════
    //  WEAPON — TRIDENT
    // ════════════════════════════════════════════════════════════════
    public int    getTridentHitsForLightning()  { return getWeaponInt("trident.lightning-hits", "weapons.trident.hits-for-lightning", 4); }
    public double getTridentLightningExtraDamage(){ return getWeaponDouble("trident.lightning-damage", "weapons.trident.lightning-extra-damage", 0.5); }
    public int    getTridentSpeedLevel()        { return getWeaponInt("trident.speed-level", "weapons.trident.speed-level", 0); }
    public int    getTridentSpeedTicks()        { return getWeaponInt("trident.speed-ticks", "weapons.trident.speed-duration-ticks", 60); }

    // ════════════════════════════════════════════════════════════════
    //  WEAPON — SHIELD
    // ════════════════════════════════════════════════════════════════
    public int    getShieldSpeedLevel()              { return getWeaponInt("shield.speed-level", "weapons.shield.speed-level", 1); }
    public int    getShieldSpeedTicks()              { return getWeaponInt("shield.speed-ticks", "weapons.shield.speed-duration-ticks", 100); }
    public double getShieldCounterDamage()           { return getWeaponDouble("shield.counter.damage", "weapons.shield.counter-damage", 2.0); }
    public double getShieldCounterRadius()           { return getWeaponDouble("shield.counter.radius", "weapons.shield.counter-radius", 3.0); }
    public int    getShieldCounterCooldownSeconds()  { return getWeaponInt("shield.counter.cooldown", "weapons.shield.counter-cooldown-seconds", 3); }

    // ════════════════════════════════════════════════════════════════
    //  WEAPON — BOW
    // ════════════════════════════════════════════════════════════════
    public double getBowExplosionRadius()      { return getWeaponDouble("bow.explosion.radius", "weapons.bow.explosion-radius", 4.0); }
    public double getBowExplosionDamage()      { return getWeaponDouble("bow.explosion.damage", "weapons.bow.explosion-damage", 3.0); }
    public double getBowKnockbackHorizontal()  { return getWeaponDouble("bow.explosion.knockback-h", "weapons.bow.explosion-knockback-h", 2.5); }
    public double getBowKnockbackVertical()    { return getWeaponDouble("bow.explosion.knockback-v", "weapons.bow.explosion-knockback-v", 0.6); }

    // ════════════════════════════════════════════════════════════════
    //  WEAPON — CROSSBOW
    // ════════════════════════════════════════════════════════════════
    public double getCrossbowPullStrength() { return getWeaponDouble("crossbow.pull-strength", "weapons.crossbow.pull-strength", 1.5); }
    public double getCrossbowChainRadius()  { return getWeaponDouble("crossbow.chain.radius", "weapons.crossbow.chain-radius", 3.0); }
    public double getCrossbowChainPull()    { return getWeaponDouble("crossbow.chain.pull-strength", "weapons.crossbow.chain-pull-strength", 1.2); }

    // ════════════════════════════════════════════════════════════════
    //  WEAPON COSMETICS
    // ════════════════════════════════════════════════════════════════
    public String  getWeaponCosmeticName(com.floki.strengthsmp.data.WeaponType type) { 
        String typeName = type.name().toLowerCase();
        if (getW().contains(typeName + ".cosmetic-name")) {
            return getW().getString(typeName + ".cosmetic-name");
        }
        return getC().getString("weapons." + typeName + ".cosmetic-name", type.getDisplayName()); 
    }
    public boolean shouldRenameOnBind(com.floki.strengthsmp.data.WeaponType type) { 
        String typeName = type.name().toLowerCase();
        if (getW().contains(typeName + ".rename-on-bind")) {
            return getW().getBoolean(typeName + ".rename-on-bind");
        }
        return getC().getBoolean("weapons." + typeName + ".rename-on-bind", true); 
    }
    public int getWeaponCustomModelData(com.floki.strengthsmp.data.WeaponType type) { 
        String typeName = type.name().toLowerCase();
        if (getW().contains(typeName + ".custom-model-data")) {
            return getW().getInt(typeName + ".custom-model-data");
        }
        return getC().getInt("weapons." + typeName + ".custom-model-data", 0); 
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
    // Discord (read only from discord.yml)
    public boolean isDiscordEnabled()       { return getD().getBoolean("enabled",          true); }
    public String  getAuditWebhook()        { return getD().getString ("webhooks.audit",      ""); }
    public String  getAnnouncementWebhook() { return getD().getString ("webhooks.announcements", ""); }
    public String  getLeaderboardWebhook()  { return getD().getString ("webhooks.leaderboard",   ""); }
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

    // ════════════════════════════════════════════════════════════════
    //  MONARCH
    // ════════════════════════════════════════════════════════════════
    public boolean isMonarchEnabled()           { return getC().getBoolean("monarch.enabled", true); }
    public boolean isMonarchGlowing()           { return getC().getBoolean("monarch.glowing", true); }
    public boolean isMonarchAnnouncements()     { return getC().getBoolean("monarch.announcements", true); }

    // ════════════════════════════════════════════════════════════════
    //  PROTECTION
    // ════════════════════════════════════════════════════════════════
    public boolean isDeathProtectionEnabled()   { return getC().getBoolean("protection.death.enabled", true); }
    public int     getDeathProtectionHours()     { return getC().getInt("protection.death.duration-hours", 4); }
    public boolean isNewbieProtectionEnabled()  { return getC().getBoolean("protection.newbie.enabled", true); }
    public int     getNewbieProtectionHours()    { return getC().getInt("protection.newbie.duration-hours", 8); }

    // ════════════════════════════════════════════════════════════════
    //  ITEMS
    // ════════════════════════════════════════════════════════════════
    public boolean useCustomHeads()          { return getC().getBoolean("items.use-custom-heads", true); }

    public String getStrengthItemMaterial() { return getC().getString("items.strength-item.material", "NAUTILUS_SHELL"); }
    public int    getStrengthItemModel()    { return getC().getInt   ("items.strength-item.custom-model-data", 12345);  }
    public String getStrengthHeadTexture()  { return getC().getString("items.strength-item.head-texture", ""); }
    
    public String getRerollItemMaterial()   { return getC().getString("items.reroll-item.material", "BOOK"); }
    public int    getRerollItemModel()      { return getC().getInt   ("items.reroll-item.custom-model-data", 12346); }
    public String getRerollHeadTexture()    { return getC().getString("items.reroll-item.head-texture", ""); }
    

    public String getDeathCertMaterial()    { return getC().getString("items.death-certificate.material", "PAPER"); }
    public int    getDeathCertModel()       { return getC().getInt   ("items.death-certificate.custom-model-data", 12347); }
    public String getDeathCertHeadTexture() { return getC().getString("items.death-certificate.head-texture", ""); }

    // ════════════════════════════════════════════════════════════════
    //  RESOURCE PACK
    // ════════════════════════════════════════════════════════════════
    public boolean isResourcePackEnabled()  { return getC().getBoolean("resource-pack.enabled", true); }
    public String  getResourcePackMode()    { return getC().getString ("resource-pack.hosting.mode", "INTERNAL"); }
    public int     getResourcePackPort()    { return getC().getInt    ("resource-pack.hosting.internal-port", 8080); }
    public String  getResourcePackPublicIp(){ return getC().getString ("resource-pack.hosting.public-ip", "AUTO"); }
    
    public String  getResourcePackUrl()     { return getC().getString ("resource-pack.url", ""); }
    public String  getResourcePackHash()    { return getC().getString ("resource-pack.hash", ""); }
    public boolean isResourcePackRequired() { return getC().getBoolean("resource-pack.force", false); }
    public String  getResourcePackPrompt()  { return getC().getString ("resource-pack.prompt-message", "&6&lStrengthSMP &7requires a custom resource pack."); }

    // ════════════════════════════════════════════════════════════════
    //  STRENGTH RANKS
    // ════════════════════════════════════════════════════════════════
    public String getRankNameForStrength(int strength) {
        String key = "strength-ranks." + strength;
        if (getC().contains(key)) {
            return getC().getString(key);
        }
        return "&eRank " + strength;
    }

    // ════════════════════════════════════════════════════════════════
    //  WEAPON BINDING SYSTEM
    // ════════════════════════════════════════════════════════════════
    public boolean isBindSystemEnabled()       { return getC().getBoolean("bind-system.enabled", false); }
    public int     getBindCostStrength()       { return getC().getInt("bind-system.cost-strength", 3); }
    public String  getBindLorePrefix()         { return getC().getString("bind-system.formatted-lore-prefix", "&d&l⚡ BOUND WEAPON ⚡"); }
    public int     getBindAnimationTicks()     { return getC().getInt("bind-system.animation-ticks", 40); }
    public double  getBindKnockbackRadius()     { return getC().getDouble("bind-system.knockback-radius", 2.0); }
    public double  getBindKnockbackStrength()   { return getC().getDouble("bind-system.knockback-strength", 1.5); }

    // Bound Abilities Active getters
    public long getBoundAbilityCooldown()       { return getC().getLong("bind-system.abilities.cooldown-seconds", 25) * 1000L; }
    public double getExcaliburBeamDamage()     { return getC().getDouble("bind-system.abilities.excalibur-beam-damage", 8.0); }
    public double getDimensionalCleaveDamage() { return getC().getDouble("bind-system.abilities.dimensional-cleave-damage", 8.0); }
    public int getVoidSingularityDuration()    { return getC().getInt("bind-system.abilities.void-singularity-duration-ticks", 70); }
    public double getScorpionHookRange()        { return getC().getDouble("bind-system.abilities.scorpion-hook-range", 14.0); }
    public double getPoseidonsWrathDamage()    { return getC().getDouble("bind-system.abilities.poseidons-wrath-damage", 7.0); }
    public double getForceFieldRadius()        { return getC().getDouble("bind-system.abilities.force-field-radius", 6.0); }
}

