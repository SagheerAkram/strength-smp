package com.floki.strengthsmp.data;

import com.floki.strengthsmp.StrengthSMP;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player strength data persistence and in-memory caching.
 * Follows a Service-Driven Architecture where this class only handles raw data.
 */
public class DataManager {

    private final StrengthSMP plugin;
    private final File dataFile;
    private FileConfiguration data;

    // ── In-memory caches (Concurrent for async Discord reading) ────────────────
    private final Map<UUID, Integer>              strengthCache = new ConcurrentHashMap<>();
    private final Map<UUID, WeaponType>           weaponCache   = new ConcurrentHashMap<>();
    private final Map<UUID, Integer>              killsCache    = new ConcurrentHashMap<>();
    private final Map<UUID, Integer>              deathsCache   = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, Integer>>   bountyCache   = new ConcurrentHashMap<>();
    private final Map<UUID, Long>                bountyGlowExpirationCache = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Long>>    abilityCooldownCache = new ConcurrentHashMap<>();

    // Contract tracking
    private final Map<UUID, Map<String, Integer>> contractProgressCache = new ConcurrentHashMap<>();
    private final Map<UUID, List<String>>         playerContractsCache = new ConcurrentHashMap<>();
    private final Map<UUID, String>               playerContractDateCache = new ConcurrentHashMap<>();
    
    // Anti-farm tracking
    private final Map<UUID, Map<UUID, Long>> killHistoryCache = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean>          receivedFreeRerollCache = new ConcurrentHashMap<>();

    // Punishment & Death Protection
    private final Map<UUID, Long>             punishmentExpiryCache    = new ConcurrentHashMap<>();
    private final Map<UUID, Long>             deathProtectionExpiryCache = new ConcurrentHashMap<>();
    private final Map<UUID, Long>             newbieProtectionExpiryCache  = new ConcurrentHashMap<>();
    
    private long lastContractRefresh = 0;
    private long lastSeasonReset = 0;
    private long lastMonarchUpdate = 0;
    private long monarchStartTime = 0;

    private UUID    monarchUUID          = null;
    private String  dashboardMessageId   = null;
    private String  leaderboardMessageId = null;
    private String  monarchMessageId     = null;
    private boolean systemEnabled        = true;

    public DataManager(StrengthSMP plugin) {
        this.plugin   = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        loadData();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  LOAD / SAVE
    // ════════════════════════════════════════════════════════════════════════

    private void loadData() {
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create data file: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);

        // ── Players ─────────────────────────────────────────────────────────
        if (data.contains("players")) {
            for (String uuidStr : data.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    String path = "players." + uuidStr;

                    strengthCache.put(uuid, data.getInt(path + ".strength", 0));
                    killsCache.put(uuid,    data.getInt(path + ".kills",    0));
                    deathsCache.put(uuid,   data.getInt(path + ".deaths",   0));

                    String weaponStr = data.getString(path + ".weapon");
                    if (weaponStr != null) {
                        try {
                            weaponCache.put(uuid, WeaponType.valueOf(weaponStr));
                        } catch (IllegalArgumentException e) {
                            weaponCache.put(uuid, WeaponType.getRandomWeapon());
                        }
                    }

                    // Bounties
                    if (data.contains(path + ".bounties")) {
                        Map<UUID, Integer> bounties = new HashMap<>();
                        for (String setterStr : data.getConfigurationSection(path + ".bounties").getKeys(false)) {
                            try {
                                bounties.put(UUID.fromString(setterStr), data.getInt(path + ".bounties." + setterStr, 0));
                            } catch (IllegalArgumentException ignored) {}
                        }
                        if (!bounties.isEmpty()) bountyCache.put(uuid, bounties);
                    }

                    long glowExp = data.getLong(path + ".bounty_glow_expiration", 0);

                    // Contracts
                    if (data.contains(path + ".contracts")) {
                        playerContractsCache.put(uuid, data.getStringList(path + ".contracts"));
                        playerContractDateCache.put(uuid, data.getString(path + ".contract_date"));
                        
                        Map<String, Integer> progress = new HashMap<>();
                        if (data.contains(path + ".contract_progress")) {
                            for (String key : data.getConfigurationSection(path + ".contract_progress").getKeys(false)) {
                                progress.put(key, data.getInt(path + ".contract_progress." + key));
                            }
                        }
                        contractProgressCache.put(uuid, progress);
                    }

                    // Kill History (for anti-farm)
                    if (data.contains(path + ".kill_history")) {
                        Map<UUID, Long> history = new HashMap<>();
                        for (String victimStr : data.getConfigurationSection(path + ".kill_history").getKeys(false)) {
                            try {
                                history.put(UUID.fromString(victimStr), data.getLong(path + ".kill_history." + victimStr));
                            } catch (IllegalArgumentException ignored) {}
                        }
                        killHistoryCache.put(uuid, history);
                    }

                    // Ability Cooldowns
                    if (data.contains(path + ".ability_cooldowns")) {
                        Map<String, Long> cds = new HashMap<>();
                        for (String key : data.getConfigurationSection(path + ".ability_cooldowns").getKeys(false)) {
                            cds.put(key, data.getLong(path + ".ability_cooldowns." + key));
                        }
                        abilityCooldownCache.put(uuid, cds);
                    }

                    // Legacy support
                    long legacyCd = data.getLong(path + ".ability_cooldown", 0L);
                    if (legacyCd > 0) {
                        getAbilityCooldownMap(uuid).put("ultimate", legacyCd);
                    }

                    // Reroll Reward
                    receivedFreeRerollCache.put(uuid, data.getBoolean(path + ".received_free_reroll", false));

                    // Punishment & Death Protection
                    long punishExp = data.getLong(path + ".punishment_expiry", 0L);
                    if (punishExp > 0) punishmentExpiryCache.put(uuid, punishExp);

                    long dpExp = data.getLong(path + ".death_protection_expiry", 0L);
                    if (dpExp > 0) deathProtectionExpiryCache.put(uuid, dpExp);

                    long nbExp = data.getLong(path + ".newbie_protection_expiry", 0L);
                    if (nbExp > 0) newbieProtectionExpiryCache.put(uuid, nbExp);

                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in data file: " + uuidStr);
                }
            }
        }

        // ── System state ────────────────────────────────────────────────────
        dashboardMessageId   = data.getString("system.discord.dashboard_id");
        leaderboardMessageId = data.getString("system.discord.leaderboard_id");
        monarchMessageId     = data.getString("system.discord.monarch_id");
        systemEnabled        = data.getBoolean("system.enabled", true);
        lastContractRefresh  = data.getLong("system.last_contract_refresh", 0);
        lastSeasonReset      = data.getLong("system.last_season_reset", 0);

        String savedMonarch = data.getString("system.monarch_uuid");
        if (savedMonarch != null && !savedMonarch.isEmpty()) {
            try { monarchUUID = UUID.fromString(savedMonarch); } catch (IllegalArgumentException ignored) {}
        }
        lastMonarchUpdate = data.getLong("system.last_monarch_update", 0);
        monarchStartTime = data.getLong("system.monarch_start_time", 0);
        
        // Initialize start time to 1 hour from now if never set
        if (monarchStartTime == 0) {
            monarchStartTime = System.currentTimeMillis() + 3600000L;
            data.set("system.monarch_start_time", monarchStartTime);
        }

        printDistributionReport();
    }

    private void printDistributionReport() {
        Map<WeaponType, Integer> counts = new HashMap<>();
        for (WeaponType type : WeaponType.values()) counts.put(type, 0);
        for (WeaponType type : weaponCache.values()) {
            counts.put(type, counts.getOrDefault(type, 0) + 1);
        }
        
        plugin.getLogger().info("📊 --- Weapon Distribution Report ---");
        counts.forEach((type, count) -> {
            plugin.getLogger().info("  > " + type.getDisplayName() + ": " + count + " players");
        });
        plugin.getLogger().info("📊 ----------------------------------");
    }

    public void saveAll() {
        // Collect all unique UUIDs across all caches to ensure no data is missed
        Set<UUID> allUuids = new HashSet<>();
        allUuids.addAll(strengthCache.keySet());
        allUuids.addAll(receivedFreeRerollCache.keySet());
        allUuids.addAll(weaponCache.keySet());
        allUuids.addAll(playerContractsCache.keySet());

        for (UUID uuid : allUuids) {
            savePlayerToConfig(uuid);
        }

        // System state
        data.set("system.discord.dashboard_id",   dashboardMessageId);
        data.set("system.discord.leaderboard_id", leaderboardMessageId);
        data.set("system.discord.monarch_id",     monarchMessageId);
        data.set("system.enabled",                systemEnabled);
        data.set("system.last_contract_refresh",  lastContractRefresh);
        data.set("system.last_season_reset",      lastSeasonReset);
        data.set("system.last_monarch_update",    lastMonarchUpdate);
        data.set("system.monarch_start_time",    monarchStartTime);
        data.set("system.monarch_uuid", monarchUUID != null ? monarchUUID.toString() : null);

        saveConfigToDisk();
    }

    public void savePlayer(UUID uuid) {
        savePlayerToConfig(uuid);
        saveConfigToDisk();
    }

    private void savePlayerToConfig(UUID uuid) {
        String path = "players." + uuid;
        if (strengthCache.containsKey(uuid)) data.set(path + ".strength", strengthCache.get(uuid));
        if (killsCache.containsKey(uuid))    data.set(path + ".kills",    killsCache.get(uuid));
        if (deathsCache.containsKey(uuid))   data.set(path + ".deaths",   deathsCache.get(uuid));
        if (weaponCache.containsKey(uuid))   data.set(path + ".weapon",   weaponCache.get(uuid).name());
        
        data.set(path + ".received_free_reroll", receivedFreeRerollCache.getOrDefault(uuid, false));

        // Punishment & Death Protection
        Long punishExp = punishmentExpiryCache.get(uuid);
        data.set(path + ".punishment_expiry", punishExp != null ? punishExp : 0L);
        Long dpExp = deathProtectionExpiryCache.get(uuid);
        data.set(path + ".death_protection_expiry", dpExp != null ? dpExp : 0L);
        Long nbExp = newbieProtectionExpiryCache.get(uuid);
        data.set(path + ".newbie_protection_expiry", nbExp != null ? nbExp : 0L);

        // Bounties
        Map<UUID, Integer> bounties = bountyCache.get(uuid);
        if (bounties != null && !bounties.isEmpty()) {
            for (Map.Entry<UUID, Integer> e : bounties.entrySet()) {
                data.set(path + ".bounties." + e.getKey(), e.getValue());
            }
        }

        // Contracts
        if (playerContractsCache.containsKey(uuid)) {
            data.set(path + ".contracts", playerContractsCache.get(uuid));
            data.set(path + ".contract_date", playerContractDateCache.get(uuid));
            Map<String, Integer> progress = contractProgressCache.get(uuid);
            if (progress != null) {
                for (Map.Entry<String, Integer> e : progress.entrySet()) {
                    data.set(path + ".contract_progress." + e.getKey(), e.getValue());
                }
            }
        }

        // Cooldowns
        Map<String, Long> cds = abilityCooldownCache.get(uuid);
        if (cds != null && !cds.isEmpty()) {
            for (Map.Entry<String, Long> e : cds.entrySet()) {
                data.set(path + ".ability_cooldowns." + e.getKey(), e.getValue());
            }
        }
    }

    private void saveConfigToDisk() {
        try {
            File tempFile = new File(plugin.getDataFolder(), "playerdata.yml.tmp");
            data.save(tempFile);
            if (dataFile.exists()) {
                dataFile.delete();
            }
            tempFile.renameTo(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save data safely: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  STRENGTH & RANKING
    // ════════════════════════════════════════════════════════════════════════

    public int getStrength(UUID uuid) {
        return strengthCache.getOrDefault(uuid, plugin.getConfigManager().getDefaultStrength());
    }

    public void setStrength(UUID uuid, int strength) {
        int min = plugin.getConfigManager().getMinStrength();
        int max = plugin.getConfigManager().getMaxStrength();
        strengthCache.compute(uuid, (k, v) -> Math.max(min, Math.min(max, strength)));
    }

    public void addStrength(UUID uuid, int amount) {
        setStrength(uuid, getStrength(uuid) + amount);
    }

    public int addStrengthCapped(UUID uuid, int amount) {
        int max = plugin.getConfigManager().getMaxStrength();
        final int[] actualAdded = {0};
        
        strengthCache.compute(uuid, (k, current) -> {
            if (current == null) current = plugin.getConfigManager().getDefaultStrength();
            if (current >= max) {
                actualAdded[0] = 0;
                return current;
            }
            actualAdded[0] = Math.min(amount, max - current);
            return current + actualAdded[0];
        });
        
        return actualAdded[0];
    }

    public boolean isAtMaxStrength(UUID uuid) {
        return getStrength(uuid) >= plugin.getConfigManager().getMaxStrength();
    }

    public void subtractStrength(UUID uuid, int amount) {
        int min = plugin.getConfigManager().getMinStrength();
        strengthCache.compute(uuid, (k, current) -> {
            if (current == null) current = plugin.getConfigManager().getDefaultStrength();
            return Math.max(min, current - amount);
        });
    }

    public String getRank(UUID uuid) {
        List<Map.Entry<UUID, Integer>> sorted = new ArrayList<>(strengthCache.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getKey().equals(uuid)) return "#" + (i + 1);
        }
        return "#?";
    }

    // ════════════════════════════════════════════════════════════════════════
    //  KILLS / DEATHS / WEAPON
    // ════════════════════════════════════════════════════════════════════════

    public int  getKills(UUID uuid)  { return killsCache.getOrDefault(uuid, 0); }
    public void addKill(UUID uuid)   { killsCache.put(uuid, getKills(uuid) + 1); }
    public int  getDeaths(UUID uuid) { return deathsCache.getOrDefault(uuid, 0); }
    public void addDeath(UUID uuid)  { deathsCache.put(uuid, getDeaths(uuid) + 1); }

    /**
     * Resets kill counts for ALL players (online + offline) then saves to disk.
     */
    public int resetAllKills() {
        // 1. Zero out in-memory cache
        for (UUID uuid : killsCache.keySet()) {
            killsCache.put(uuid, 0);
        }
        // 2. Zero out every entry directly in the YAML (covers offline players)
        int count = 0;
        if (data.contains("players")) {
            org.bukkit.configuration.ConfigurationSection players = data.getConfigurationSection("players");
            if (players != null) {
                for (String uuidStr : players.getKeys(false)) {
                    data.set("players." + uuidStr + ".kills", 0);
                    // Also zero the cache entry so live reads reflect the reset
                    try { killsCache.put(java.util.UUID.fromString(uuidStr), 0); } catch (Exception ignored) {}
                    count++;
                }
            }
        }
        saveAll();
        return count;
    }

    public WeaponType getWeaponType(UUID uuid) {
        // If they have 0 strength, they are Neutral and have no class abilities
        if (getStrength(uuid) <= 0) return null;
        
        // Use cache-first approach. If not in cache, roll ONCE and save.
        WeaponType type = weaponCache.get(uuid);
        if (type == null) {
            type = WeaponType.getRandomWeapon();
            weaponCache.put(uuid, type);
            savePlayer(uuid); // Ensure it's persisted immediately
            plugin.getLogger().info("🎲 Assigned persistent weapon " + type.name() + " to " + uuid);
        }
        return type;
    }

    public void setWeaponType(UUID uuid, WeaponType weapon) {
        weaponCache.put(uuid, weapon);
    }

    // Aliases for compatibility with command classes
    public WeaponType getWeapon(UUID uuid) { return getWeaponType(uuid); }
    public void setWeapon(UUID uuid, WeaponType weapon) { setWeaponType(uuid, weapon); }

    // ════════════════════════════════════════════════════════════════════════
    //  BOUNTY SYSTEM
    // ════════════════════════════════════════════════════════════════════════

    public int getBounty(UUID uuid) {
        return bountyCache.getOrDefault(uuid, Collections.emptyMap())
                .values().stream().mapToInt(Integer::intValue).sum();
    }

    public void setBounty(UUID targetUUID, UUID setterUUID, int amount) {
        bountyCache.computeIfAbsent(targetUUID, k -> new ConcurrentHashMap<>()).put(setterUUID, amount);
    }

    public void addBounty(UUID targetUUID, UUID setterUUID, int amount) {
        Map<UUID, Integer> targetBounties = bountyCache.computeIfAbsent(targetUUID, k -> new ConcurrentHashMap<>());
        int current = targetBounties.getOrDefault(setterUUID, 0);
        targetBounties.put(setterUUID, current + amount);
    }

    public void removeBounty(UUID uuid) {
        bountyCache.remove(uuid);
    }

    public long getBountyGlowExpiration(UUID uuid) {
        return bountyGlowExpirationCache.getOrDefault(uuid, 0L);
    }

    public void setBountyGlowExpiration(UUID uuid, long timestamp) {
        bountyGlowExpirationCache.put(uuid, timestamp);
    }

    public void clearBountyGlowExpiration(UUID uuid) {
        bountyGlowExpirationCache.remove(uuid);
    }

    public long getAbilityCooldown(UUID uuid) {
        return getAbilityCooldown(uuid, "ultimate");
    }

    public long getAbilityCooldown(UUID uuid, String key) {
        return getAbilityCooldownMap(uuid).getOrDefault(key, 0L);
    }

    public void setAbilityCooldown(UUID uuid, long timestamp) {
        setAbilityCooldown(uuid, "ultimate", timestamp);
    }

    public void setAbilityCooldown(UUID uuid, String key, long timestamp) {
        getAbilityCooldownMap(uuid).put(key, timestamp);
    }

    private Map<String, Long> getAbilityCooldownMap(UUID uuid) {
        return abilityCooldownCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CONTRACTS
    // ════════════════════════════════════════════════════════════════════════

    public String getPlayerContractDate(UUID uuid) {
        return playerContractDateCache.get(uuid);
    }

    public void setPlayerContractDate(UUID uuid, String date) {
        playerContractDateCache.put(uuid, date);
    }

    public List<String> getPlayerContracts(UUID uuid) {
        return playerContractsCache.getOrDefault(uuid, new ArrayList<>());
    }

    public void setPlayerContracts(UUID uuid, List<String> contracts) {
        playerContractsCache.put(uuid, contracts);
    }

    public Map<String, Integer> getContractProgress(UUID uuid) {
        return contractProgressCache.computeIfAbsent(uuid, k -> new HashMap<>());
    }

    public void updateContractProgress(UUID uuid, String contract, int value) {
        contractProgressCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(contract, value);
    }

    public void resetContractProgress(UUID uuid) {
        contractProgressCache.put(uuid, new HashMap<>());
    }

    public long getLastContractRefresh() {
        return lastContractRefresh;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  ANTI-FARM (KILL HISTORY)
    // ════════════════════════════════════════════════════════════════════════

    public long getLastKillTime(UUID attacker, UUID victim) {
        Map<UUID, Long> history = killHistoryCache.get(attacker);
        if (history == null) return 0;
        return history.getOrDefault(victim, 0L);
    }

    public void recordKill(UUID attacker, UUID victim) {
        killHistoryCache.computeIfAbsent(attacker, k -> new ConcurrentHashMap<>()).put(victim, System.currentTimeMillis());
    }

    public void clearKillHistory(UUID uuid) {
        killHistoryCache.remove(uuid);
    }

    public int getKillHistoryCount(UUID uuid) {
        Map<UUID, Long> history = killHistoryCache.get(uuid);
        if (history == null) return 0;
        
        // Count entries within 1 hour
        long now = System.currentTimeMillis();
        long window = 3600000L;
        return (int) history.values().stream().filter(t -> now - t < window).count();
    }


    // ════════════════════════════════════════════════════════════════════════
    //  SEASONS & MONARCH
    // ════════════════════════════════════════════════════════════════════════

    public long getLastSeasonReset() { return lastSeasonReset; }
    public void setLastSeasonReset(long t) { this.lastSeasonReset = t; }

    public void resetAllStats() {
        strengthCache.clear();
        killsCache.clear();
        deathsCache.clear();
        bountyCache.clear();
        contractProgressCache.clear();
        playerContractsCache.clear();
        playerContractDateCache.clear();
        monarchUUID = null;
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            initializePlayer(p.getUniqueId());
        }
        saveAll();
    }

    public UUID getMonarch() { 
        return monarchUUID; 
    }
    
    public long getMonarchStartTime() { return monarchStartTime; }
    public long getLastMonarchUpdate() { return lastMonarchUpdate; }
    public void setLastMonarchUpdate(long t) { this.lastMonarchUpdate = t; }

    public void calculateMonarch() {
        UUID topPlayer = null;
        int  topStrength = -1;
        int  topKills = -1;

        for (Map.Entry<UUID, Integer> entry : strengthCache.entrySet()) {
            UUID uuid = entry.getKey();
            int strength = entry.getValue();
            int kills = getKills(uuid);

            // Monarch must have at least 1 strength to be eligible
            if (strength <= 0) continue;

            if (strength > topStrength) {
                topStrength = strength;
                topKills = kills;
                topPlayer = uuid;
            } else if (strength == topStrength) {
                // Tie-breaker: total kills
                if (kills > topKills) {
                    topKills = kills;
                    topPlayer = uuid;
                }
            }
        }
        monarchUUID = topPlayer;
    }

    public String getTopPlayerName() {
        UUID monarch = getMonarch();
        if (monarch == null) return "None";
        return Bukkit.getOfflinePlayer(monarch).getName();
    }

    public int getTopPlayerStrength() {
        UUID monarch = getMonarch();
        if (monarch == null) return 0;
        return getStrength(monarch);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  INIT & CACHE ACCESSORS
    // ════════════════════════════════════════════════════════════════════════

    public boolean hasReceivedFreeReroll(UUID uuid) {
        return receivedFreeRerollCache.getOrDefault(uuid, false);
    }

    public void setReceivedFreeReroll(UUID uuid, boolean val) {
        receivedFreeRerollCache.put(uuid, val);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PUNISHMENT SYSTEM
    // ════════════════════════════════════════════════════════════════════════

    /** Returns true if the player is currently under an active punishment. */
    public boolean isPunished(UUID uuid) {
        Long expiry = punishmentExpiryCache.get(uuid);
        if (expiry == null || expiry <= 0) return false;
        if (System.currentTimeMillis() > expiry) {
            punishmentExpiryCache.remove(uuid);
            return false;
        }
        return true;
    }

    public void setPunishment(UUID uuid, long expiryTimestamp) {
        punishmentExpiryCache.put(uuid, expiryTimestamp);
    }

    public void clearPunishment(UUID uuid) {
        punishmentExpiryCache.remove(uuid);
    }

    public long getPunishmentExpiry(UUID uuid) {
        return punishmentExpiryCache.getOrDefault(uuid, 0L);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  DEATH PROTECTION SYSTEM
    // ════════════════════════════════════════════════════════════════════════

    /** Returns true if the player currently has death protection (cannot lose strength). */
    public boolean hasDeathProtection(UUID uuid) {
        Long expiry = deathProtectionExpiryCache.get(uuid);
        if (expiry == null || expiry <= 0) return false;
        if (System.currentTimeMillis() > expiry) {
            deathProtectionExpiryCache.remove(uuid);
            return false;
        }
        return true;
    }

    public void setDeathProtection(UUID uuid, long expiryTimestamp) {
        deathProtectionExpiryCache.put(uuid, expiryTimestamp);
    }

    public void clearDeathProtection(UUID uuid) {
        deathProtectionExpiryCache.remove(uuid);
    }

    public long getDeathProtectionExpiry(UUID uuid) {
        return deathProtectionExpiryCache.getOrDefault(uuid, 0L);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  NEWBIE PROTECTION SYSTEM
    // ════════════════════════════════════════════════════════════════════════

    /** Returns true if the player is under their 8-hour new-player protection. */
    public boolean isNewbieProtected(UUID uuid) {
        Long expiry = newbieProtectionExpiryCache.get(uuid);
        if (expiry == null || expiry <= 0) return false;
        if (System.currentTimeMillis() > expiry) {
            newbieProtectionExpiryCache.remove(uuid);
            return false;
        }
        return true;
    }

    public void setNewbieProtection(UUID uuid, long expiryTimestamp) {
        newbieProtectionExpiryCache.put(uuid, expiryTimestamp);
    }

    public void clearNewbieProtection(UUID uuid) {
        newbieProtectionExpiryCache.remove(uuid);
    }

    public long getNewbieProtectionExpiry(UUID uuid) {
        return newbieProtectionExpiryCache.getOrDefault(uuid, 0L);
    }

        public void initializePlayer(UUID uuid) {
        // ONLY assign if NO saved data exists in cache
        if (!weaponCache.containsKey(uuid)) {
            weaponCache.put(uuid, WeaponType.getRandomWeapon());
        }
        if (!strengthCache.containsKey(uuid)) {
            strengthCache.put(uuid, plugin.getConfigManager().getDefaultStrength());
        }
        if (!killsCache.containsKey(uuid)) {
            killsCache.put(uuid, 0);
        }
        if (!deathsCache.containsKey(uuid)) {
            deathsCache.put(uuid, 0);
        }
        if (!receivedFreeRerollCache.containsKey(uuid)) {
            receivedFreeRerollCache.put(uuid, false);
        }
    }

    public Map<UUID, Integer>    getStrengthCache() { return new HashMap<>(strengthCache); }
    public Map<UUID, Integer>    getKillsCache()    { return new HashMap<>(killsCache); }
    public Map<UUID, Integer>    getDeathsCache()   { return new HashMap<>(deathsCache); }
    public Map<UUID, WeaponType> getWeaponCache()   { return new HashMap<>(weaponCache);   }
    public Map<UUID, Map<UUID, Integer>> getBountyCache() { return new HashMap<>(bountyCache); }

    // System state getters/setters
    public String  getDashboardMessageId()    { return dashboardMessageId; }
    public void    setDashboardMessageId(String id)   { this.dashboardMessageId = id; }
    public String  getLeaderboardMessageId()  { return leaderboardMessageId; }
    public void    setLeaderboardMessageId(String id) { this.leaderboardMessageId = id; }
    public String  getMonarchMessageId()      { return monarchMessageId; }
    public void    setMonarchMessageId(String id)     { this.monarchMessageId = id; }
    public boolean isSystemEnabled()          { return systemEnabled; }
    public void    setSystemEnabled(boolean e){ this.systemEnabled = e; }
}
