/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.configuration.file.FileConfiguration
 */
package com.floki.strengthsmp.config;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.config.ConfigManager;
import com.floki.strengthsmp.data.WeaponType;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {
    private final StrengthSMP plugin;
    private final ConfigManager configManager;

    public Config(StrengthSMP plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getInternalConfigManager();
    }

    public void reload() {
        this.configManager.reloadAll();
    }

    public FileConfiguration getC() {
        return this.configManager.getConfig();
    }

    public FileConfiguration getW() {
        return this.configManager.getWeapons();
    }

    public FileConfiguration getM() {
        return this.configManager.getMessages();
    }

    public FileConfiguration getE() {
        return this.configManager.getEvents();
    }

    public FileConfiguration getD() {
        return this.configManager.getDiscord();
    }

    public FileConfiguration getMessages() {
        return this.getM();
    }

    public FileConfiguration getMainConfig() {
        return this.getC();
    }

    public String getMessage(String path, String def) {
        return this.getM().getString(path, def);
    }

    public int getMinStrength() {
        return this.getC().getInt("strength.min", 0);
    }

    public int getMaxStrength() {
        return this.getC().getInt("strength.max", 5);
    }

    public int getDefaultStrength() {
        return this.getC().getInt("strength.default", 0);
    }

    public int getDeathPenalty() {
        return this.getC().getInt("strength.death-penalty", 1);
    }

    public int getDeathGain() {
        return this.getC().getInt("strength.death-gain", 1);
    }

    public int getUnderdogMultiplier() {
        return this.getC().getInt("strength.underdog-multiplier", 2);
    }

    public int getUnderdogThreshold() {
        return this.getC().getInt("strength.underdog-threshold", 3);
    }

    public boolean isDamageScalingEnabled() {
        return this.getC().getBoolean("damage-scaling.enabled", true);
    }

    public boolean isOnlyOnWeaponMatch() {
        return this.getC().getBoolean("damage-scaling.only-on-weapon-match", true);
    }

    public double getDamagePerStrength() {
        return this.getC().getDouble("damage-scaling.per-strength", 0.5);
    }

    public double getVictimReductionPerStrength() {
        return this.getC().getDouble("damage-scaling.victim-reduction-per-strength", 0.3);
    }

    private int getWeaponInt(String keyW, String keyC, int def) {
        if (this.getW().contains(keyW)) {
            return this.getW().getInt(keyW);
        }
        return this.getC().getInt(keyC, def);
    }

    private double getWeaponDouble(String keyW, String keyC, double def) {
        if (this.getW().contains(keyW)) {
            return this.getW().getDouble(keyW);
        }
        return this.getC().getDouble(keyC, def);
    }

    public int getSwordComboHits() {
        return this.getWeaponInt("sword.passive-combo-hits", "weapons.sword.passive-combo-hits", 3);
    }

    public double getSwordPassiveMult() {
        return this.getWeaponDouble("sword.passive-damage-mult", "weapons.sword.passive-damage-mult", 1.25);
    }

    public double getSwordShockwaveDamage() {
        return this.getWeaponDouble("sword.shockwave.damage", "weapons.sword.shockwave-damage", 0.0);
    }

    public double getSwordShockwaveRadius() {
        return this.getWeaponDouble("sword.shockwave.radius", "weapons.sword.shockwave-radius", 0.0);
    }

    public int getAxeCritHits() {
        return this.getWeaponInt("axe.passive-crit-hits", "weapons.axe.passive-crit-hits", 5);
    }

    public int getAxeStunTicks() {
        return this.getWeaponInt("axe.stun-duration-ticks", "weapons.axe.stun-duration-ticks", 10);
    }

    public double getAxeBleedDamage() {
        return this.getWeaponDouble("axe.bleed.damage", "weapons.axe.bleed-damage", 1.5);
    }

    public int getAxeBleedDelayTicks() {
        return this.getWeaponInt("axe.bleed.ticks", "weapons.axe.bleed-delay-ticks", 40);
    }

    public int getTridentHitsForLightning() {
        return this.getWeaponInt("trident.lightning-hits", "weapons.trident.hits-for-lightning", 4);
    }

    public double getTridentLightningExtraDamage() {
        return this.getWeaponDouble("trident.lightning-damage", "weapons.trident.lightning-extra-damage", 0.5);
    }

    public int getTridentSpeedLevel() {
        return this.getWeaponInt("trident.speed-level", "weapons.trident.speed-level", 0);
    }

    public int getTridentSpeedTicks() {
        return this.getWeaponInt("trident.speed-ticks", "weapons.trident.speed-duration-ticks", 60);
    }

    public int getShieldSpeedLevel() {
        return this.getWeaponInt("shield.speed-level", "weapons.shield.speed-level", 1);
    }

    public int getShieldSpeedTicks() {
        return this.getWeaponInt("shield.speed-ticks", "weapons.shield.speed-duration-ticks", 100);
    }

    public double getShieldCounterDamage() {
        return this.getWeaponDouble("shield.counter.damage", "weapons.shield.counter-damage", 2.0);
    }

    public double getShieldCounterRadius() {
        return this.getWeaponDouble("shield.counter.radius", "weapons.shield.counter-radius", 3.0);
    }

    public int getShieldCounterCooldownSeconds() {
        return this.getWeaponInt("shield.counter.cooldown", "weapons.shield.counter-cooldown-seconds", 3);
    }

    public double getBowExplosionRadius() {
        return this.getWeaponDouble("bow.explosion.radius", "weapons.bow.explosion-radius", 4.0);
    }

    public double getBowExplosionDamage() {
        return this.getWeaponDouble("bow.explosion.damage", "weapons.bow.explosion-damage", 3.0);
    }

    public double getBowKnockbackHorizontal() {
        return this.getWeaponDouble("bow.explosion.knockback-h", "weapons.bow.explosion-knockback-h", 2.5);
    }

    public double getBowKnockbackVertical() {
        return this.getWeaponDouble("bow.explosion.knockback-v", "weapons.bow.explosion-knockback-v", 0.6);
    }

    public double getCrossbowPullStrength() {
        return this.getWeaponDouble("crossbow.pull-strength", "weapons.crossbow.pull-strength", 1.5);
    }

    public double getCrossbowChainRadius() {
        return this.getWeaponDouble("crossbow.chain.radius", "weapons.crossbow.chain-radius", 3.0);
    }

    public double getCrossbowChainPull() {
        return this.getWeaponDouble("crossbow.chain.pull-strength", "weapons.crossbow.chain-pull-strength", 1.2);
    }

    public String getWeaponCosmeticName(WeaponType type) {
        String typeName = type.name().toLowerCase();
        if (this.getW().contains(typeName + ".cosmetic-name")) {
            return this.getW().getString(typeName + ".cosmetic-name");
        }
        return this.getC().getString("weapons." + typeName + ".cosmetic-name", type.getDisplayName());
    }

    public boolean shouldRenameOnBind(WeaponType type) {
        String typeName = type.name().toLowerCase();
        if (this.getW().contains(typeName + ".rename-on-bind")) {
            return this.getW().getBoolean(typeName + ".rename-on-bind");
        }
        return this.getC().getBoolean("weapons." + typeName + ".rename-on-bind", true);
    }

    public int getWeaponCustomModelData(WeaponType type) {
        String typeName = type.name().toLowerCase();
        if (this.getW().contains(typeName + ".custom-model-data")) {
            return this.getW().getInt(typeName + ".custom-model-data");
        }
        return this.getC().getInt("weapons." + typeName + ".custom-model-data", 0);
    }

    public int getCooldown(String weapon) {
        return this.getC().getInt("cooldowns." + weapon, 60);
    }

    public int getDuration(String weapon) {
        return this.getC().getInt("durations." + weapon, 15);
    }

    public int getGracePeriod() {
        return this.getC().getInt("combat.grace-period", 5);
    }

    public int getCombatTagDuration() {
        return this.getC().getInt("combat.combat-tag-duration", 15);
    }

    public int getPingLogoutThreshold() {
        return this.getC().getInt("combat.ping-logout-threshold", 500);
    }

    public int getMaxKillsSamePlayer() {
        return this.getC().getInt("anti-farm.max-kills-same-player", 2);
    }

    public int getAntiFarmWindowMinutes() {
        return this.getC().getInt("anti-farm.window-minutes", 10);
    }

    public boolean isDiscordEnabled() {
        return this.getD().getBoolean("enabled", true);
    }

    public String getAuditWebhook() {
        return this.getD().getString("webhooks.audit", "");
    }

    public String getAnnouncementWebhook() {
        return this.getD().getString("webhooks.announcements", "");
    }

    public String getLeaderboardWebhook() {
        return this.getD().getString("webhooks.leaderboard", "");
    }

    public int getDiscordUpdateInterval() {
        return this.getD().getInt("update-interval", 30);
    }

    public boolean isDiscordHeartbeatEnabled() {
        return this.getD().getBoolean("system-heartbeats", false);
    }

    public boolean isLbEnabled() {
        return this.getD().getBoolean("leaderboard.enabled", true);
    }

    public int getLbUpdateMins() {
        return this.getD().getInt("leaderboard.update-minutes", 5);
    }

    public int getLbLimit() {
        return this.getD().getInt("leaderboard.limit", 20);
    }

    public String getLbColor() {
        return this.getD().getString("leaderboard.color", "gold");
    }

    public boolean lbShowKills() {
        return this.getD().getBoolean("leaderboard.show-kills", true);
    }

    public boolean lbShowBounties() {
        return this.getD().getBoolean("leaderboard.show-bounties", true);
    }

    public boolean lbShowMonarch() {
        return this.getD().getBoolean("leaderboard.show-monarch", true);
    }

    public boolean lbShowStatus() {
        return this.getD().getBoolean("leaderboard.show-status", true);
    }

    public boolean isBanStrengthPotions() {
        return this.getC().getBoolean("potions.ban-strength-potions", true);
    }

    public boolean isActiveOnStartup() {
        return this.getC().getBoolean("system.active-on-startup", false);
    }

    public boolean isMaintenanceKeepInventory() {
        return this.getC().getBoolean("system.maintenance-keep-inventory", true);
    }

    public boolean isExperimentalEnabled() {
        return this.getC().getBoolean("experimental.enabled", false);
    }

    public boolean isTotemLimitEnabled() {
        return this.getC().getBoolean("totem-limit.enabled", true);
    }

    public int getTotemMax() {
        return this.getC().getInt("totem-limit.max", 2);
    }

    public boolean isTotemDropExcess() {
        return this.getC().getBoolean("totem-limit.drop-excess", true);
    }

    public int getTotemMessageCooldown() {
        return this.getC().getInt("totem-limit.message-cooldown-seconds", 2);
    }

    public boolean isMonarchEnabled() {
        return this.getC().getBoolean("monarch.enabled", true);
    }

    public boolean isMonarchGlowing() {
        return this.getC().getBoolean("monarch.glowing", true);
    }

    public boolean isMonarchAnnouncements() {
        return this.getC().getBoolean("monarch.announcements", true);
    }

    public boolean isDeathProtectionEnabled() {
        return this.getC().getBoolean("protection.death.enabled", true);
    }

    public int getDeathProtectionHours() {
        return this.getC().getInt("protection.death.duration-hours", 4);
    }

    public boolean isNewbieProtectionEnabled() {
        return this.getC().getBoolean("protection.newbie.enabled", true);
    }

    public int getNewbieProtectionHours() {
        return this.getC().getInt("protection.newbie.duration-hours", 8);
    }

    public boolean useCustomHeads() {
        return this.getC().getBoolean("items.use-custom-heads", true);
    }

    public String getStrengthItemMaterial() {
        return this.getC().getString("items.strength-item.material", "NAUTILUS_SHELL");
    }

    public int getStrengthItemModel() {
        return this.getC().getInt("items.strength-item.custom-model-data", 12345);
    }

    public String getStrengthHeadTexture() {
        return this.getC().getString("items.strength-item.head-texture", "");
    }

    public String getRerollItemMaterial() {
        return this.getC().getString("items.reroll-item.material", "BOOK");
    }

    public int getRerollItemModel() {
        return this.getC().getInt("items.reroll-item.custom-model-data", 12346);
    }

    public String getRerollHeadTexture() {
        return this.getC().getString("items.reroll-item.head-texture", "");
    }

    public String getDeathCertMaterial() {
        return this.getC().getString("items.death-certificate.material", "PAPER");
    }

    public int getDeathCertModel() {
        return this.getC().getInt("items.death-certificate.custom-model-data", 12347);
    }

    public String getDeathCertHeadTexture() {
        return this.getC().getString("items.death-certificate.head-texture", "");
    }

    public boolean isResourcePackEnabled() {
        return this.getC().getBoolean("resource-pack.enabled", true);
    }

    public String getResourcePackMode() {
        return this.getC().getString("resource-pack.hosting.mode", "INTERNAL");
    }

    public int getResourcePackPort() {
        return this.getC().getInt("resource-pack.hosting.internal-port", 8080);
    }

    public String getResourcePackPublicIp() {
        return this.getC().getString("resource-pack.hosting.public-ip", "AUTO");
    }

    public String getResourcePackUrl() {
        return this.getC().getString("resource-pack.url", "");
    }

    public String getResourcePackHash() {
        return this.getC().getString("resource-pack.hash", "");
    }

    public boolean isResourcePackRequired() {
        return this.getC().getBoolean("resource-pack.force", false);
    }

    public String getResourcePackPrompt() {
        return this.getC().getString("resource-pack.prompt-message", "&6&lStrengthSMP &7requires a custom resource pack.");
    }

    public String getRankNameForStrength(int strength) {
        String key = "strength-ranks." + strength;
        if (this.getC().contains(key)) {
            return this.getC().getString(key);
        }
        return "&eRank " + strength;
    }

    public boolean isBindSystemEnabled() {
        return this.getC().getBoolean("bind-system.enabled", false);
    }

    public int getBindCostStrength() {
        return this.getC().getInt("bind-system.cost-strength", 1);
    }

    public String getBindLorePrefix() {
        return this.getC().getString("bind-system.formatted-lore-prefix", "&d&l\u26a1 BOUND WEAPON \u26a1");
    }

    public int getBindAnimationTicks() {
        return this.getC().getInt("bind-system.animation-ticks", 40);
    }

    public double getBindKnockbackRadius() {
        return this.getC().getDouble("bind-system.knockback-radius", 2.0);
    }

    public double getBindKnockbackStrength() {
        return this.getC().getDouble("bind-system.knockback-strength", 1.5);
    }

    public long getBoundAbilityCooldown() {
        return this.getC().getLong("bind-system.abilities.cooldown-seconds", 25L) * 1000L;
    }

    public double getExcaliburBeamDamage() {
        return this.getC().getDouble("bind-system.abilities.excalibur-beam-damage", 8.0);
    }

    public double getDimensionalCleaveDamage() {
        return this.getC().getDouble("bind-system.abilities.dimensional-cleave-damage", 8.0);
    }

    public int getVoidSingularityDuration() {
        return this.getC().getInt("bind-system.abilities.void-singularity-duration-ticks", 70);
    }

    public double getScorpionHookRange() {
        return this.getC().getDouble("bind-system.abilities.scorpion-hook-range", 14.0);
    }

    public double getPoseidonsWrathDamage() {
        return this.getC().getDouble("bind-system.abilities.poseidons-wrath-damage", 7.0);
    }

    public double getForceFieldRadius() {
        return this.getC().getDouble("bind-system.abilities.force-field-radius", 6.0);
    }
}

