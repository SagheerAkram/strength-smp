/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.entity.Player
 */
package com.floki.strengthsmp.data;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.WeaponType;
import com.floki.strengthsmp.util.ItemFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class DataManager {
    private final StrengthSMP plugin;
    private final File dataFile;
    private FileConfiguration data;
    private final Map<UUID, Integer> strengthCache = new ConcurrentHashMap<UUID, Integer>();
    private final Map<UUID, WeaponType> weaponCache = new ConcurrentHashMap<UUID, WeaponType>();
    private final Map<UUID, Integer> killsCache = new ConcurrentHashMap<UUID, Integer>();
    private final Map<UUID, Integer> deathsCache = new ConcurrentHashMap<UUID, Integer>();
    private final Map<UUID, Map<UUID, Integer>> bountyCache = new ConcurrentHashMap<UUID, Map<UUID, Integer>>();
    private final Map<UUID, Long> bountyGlowExpirationCache = new ConcurrentHashMap<UUID, Long>();
    private final Map<UUID, Map<String, Long>> abilityCooldownCache = new ConcurrentHashMap<UUID, Map<String, Long>>();
    private final Map<UUID, Map<String, Integer>> contractProgressCache = new ConcurrentHashMap<UUID, Map<String, Integer>>();
    private final Map<UUID, List<String>> playerContractsCache = new ConcurrentHashMap<UUID, List<String>>();
    private final Map<UUID, String> playerContractDateCache = new ConcurrentHashMap<UUID, String>();
    private final Map<UUID, Map<UUID, Long>> killHistoryCache = new ConcurrentHashMap<UUID, Map<UUID, Long>>();
    private final Map<UUID, Boolean> receivedFreeRerollCache = new ConcurrentHashMap<UUID, Boolean>();
    private final Map<UUID, Long> punishmentExpiryCache = new ConcurrentHashMap<UUID, Long>();
    private final Map<UUID, Long> deathProtectionExpiryCache = new ConcurrentHashMap<UUID, Long>();
    private final Map<UUID, Long> newbieProtectionExpiryCache = new ConcurrentHashMap<UUID, Long>();
    private final Map<UUID, Set<String>> playerSkillsCache = new ConcurrentHashMap<UUID, Set<String>>();
    private long lastContractRefresh = 0L;
    private long lastSeasonReset = 0L;
    private long lastMonarchUpdate = 0L;
    private long monarchStartTime = 0L;
    private UUID monarchUUID = null;
    private String dashboardMessageId = null;
    private String leaderboardMessageId = null;
    private String monarchMessageId = null;
    private boolean systemEnabled = true;
    private final DatabaseManager databaseManager;
    // One background thread writes playerdata.yml, so writes never overlap and stay in order.
    private final ExecutorService fileWriter = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "StrengthSMP-DataWriter");
        t.setDaemon(true);
        return t;
    });
    private final Object fileLock = new Object();

    public DataManager(StrengthSMP plugin) {
        this.plugin = plugin;
        this.databaseManager = new DatabaseManager(plugin);
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        this.loadData();
    }

    private void loadData() {
        if (!this.dataFile.exists()) {
            try {
                this.plugin.getDataFolder().mkdirs();
                this.dataFile.createNewFile();
            }
            catch (IOException e) {
                this.plugin.getLogger().severe("Failed to create data file: " + e.getMessage());
            }
        }
        this.data = YamlConfiguration.loadConfiguration((File)this.dataFile);
        if (this.data.contains("players")) {
            for (String uuidStr : this.data.getConfigurationSection("players").getKeys(false)) {
                try {
                    long nbExp;
                    long dpExp;
                    long punishExp;
                    long legacyCd;
                    UUID uuid = UUID.fromString(uuidStr);
                    String path = "players." + uuidStr;
                    this.strengthCache.put(uuid, this.data.getInt(path + ".strength", 0));
                    this.killsCache.put(uuid, this.data.getInt(path + ".kills", 0));
                    this.deathsCache.put(uuid, this.data.getInt(path + ".deaths", 0));
                    String weaponStr = this.data.getString(path + ".weapon");
                    if (weaponStr != null) {
                        try {
                            this.weaponCache.put(uuid, WeaponType.valueOf(weaponStr));
                        }
                        catch (IllegalArgumentException e) {
                            this.weaponCache.put(uuid, WeaponType.getRandomWeapon());
                        }
                    }
                    if (this.data.contains(path + ".bounties")) {
                        HashMap<UUID, Integer> bounties = new HashMap<UUID, Integer>();
                        for (String setterStr : this.data.getConfigurationSection(path + ".bounties").getKeys(false)) {
                            try {
                                bounties.put(UUID.fromString(setterStr), this.data.getInt(path + ".bounties." + setterStr, 0));
                            }
                            catch (IllegalArgumentException illegalArgumentException) {}
                        }
                        if (!bounties.isEmpty()) {
                            this.bountyCache.put(uuid, bounties);
                        }
                    }
                    long glowExp = this.data.getLong(path + ".bounty_glow_expiration", 0L);
                    if (this.data.contains(path + ".contracts")) {
                        this.playerContractsCache.put(uuid, this.data.getStringList(path + ".contracts"));
                        this.playerContractDateCache.put(uuid, this.data.getString(path + ".contract_date"));
                        HashMap<String, Integer> progress = new HashMap<String, Integer>();
                        if (this.data.contains(path + ".contract_progress")) {
                            for (String key : this.data.getConfigurationSection(path + ".contract_progress").getKeys(false)) {
                                progress.put(key, this.data.getInt(path + ".contract_progress." + key));
                            }
                        }
                        this.contractProgressCache.put(uuid, progress);
                    }
                    if (this.data.contains(path + ".kill_history")) {
                        HashMap<UUID, Long> history = new HashMap<UUID, Long>();
                        for (String victimStr : this.data.getConfigurationSection(path + ".kill_history").getKeys(false)) {
                            try {
                                history.put(UUID.fromString(victimStr), this.data.getLong(path + ".kill_history." + victimStr));
                            }
                            catch (IllegalArgumentException illegalArgumentException) {}
                        }
                        this.killHistoryCache.put(uuid, history);
                    }
                    if (this.data.contains(path + ".ability_cooldowns")) {
                        HashMap<String, Long> cds = new HashMap<String, Long>();
                        for (String key : this.data.getConfigurationSection(path + ".ability_cooldowns").getKeys(false)) {
                            cds.put(key, this.data.getLong(path + ".ability_cooldowns." + key));
                        }
                        this.abilityCooldownCache.put(uuid, cds);
                    }
                    if ((legacyCd = this.data.getLong(path + ".ability_cooldown", 0L)) > 0L) {
                        this.getAbilityCooldownMap(uuid).put("ultimate", legacyCd);
                    }
                    this.receivedFreeRerollCache.put(uuid, this.data.getBoolean(path + ".received_free_reroll", false));
                    if (this.data.contains(path + ".skills")) {
                        this.playerSkillsCache.put(uuid, new HashSet(this.data.getStringList(path + ".skills")));
                    }
                    if ((punishExp = this.data.getLong(path + ".punishment_expiry", 0L)) > 0L) {
                        this.punishmentExpiryCache.put(uuid, punishExp);
                    }
                    if ((dpExp = this.data.getLong(path + ".death_protection_expiry", 0L)) > 0L) {
                        this.deathProtectionExpiryCache.put(uuid, dpExp);
                    }
                    if ((nbExp = this.data.getLong(path + ".newbie_protection_expiry", 0L)) <= 0L) continue;
                    this.newbieProtectionExpiryCache.put(uuid, nbExp);
                }
                catch (IllegalArgumentException e) {
                    this.plugin.getLogger().warning("Invalid UUID in data file: " + uuidStr);
                }
            }
        }
        this.dashboardMessageId = this.data.getString("system.discord.dashboard_id");
        this.leaderboardMessageId = this.data.getString("system.discord.leaderboard_id");
        this.monarchMessageId = this.data.getString("system.discord.monarch_id");
        this.systemEnabled = this.data.getBoolean("system.enabled", true);
        this.lastContractRefresh = this.data.getLong("system.last_contract_refresh", 0L);
        this.lastSeasonReset = this.data.getLong("system.last_season_reset", 0L);
        String savedMonarch = this.data.getString("system.monarch_uuid");
        if (savedMonarch != null && !savedMonarch.isEmpty()) {
            try {
                this.monarchUUID = UUID.fromString(savedMonarch);
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
        }
        this.lastMonarchUpdate = this.data.getLong("system.last_monarch_update", 0L);
        this.monarchStartTime = this.data.getLong("system.monarch_start_time", 0L);
        if (this.monarchStartTime == 0L) {
            this.monarchStartTime = System.currentTimeMillis() + 3600000L;
            this.data.set("system.monarch_start_time", (Object)this.monarchStartTime);
        }
        if (this.databaseManager != null && this.databaseManager.getType() != DatabaseManager.DatabaseType.YAML) {
            this.databaseManager.loadAll(this);
        }
        this.printDistributionReport();
    }

    private void printDistributionReport() {
        HashMap<WeaponType, Integer> counts = new HashMap<WeaponType, Integer>();
        for (WeaponType type2 : WeaponType.values()) {
            counts.put(type2, 0);
        }
        for (WeaponType type3 : this.weaponCache.values()) {
            counts.put(type3, counts.getOrDefault((Object)type3, 0) + 1);
        }
        this.plugin.getLogger().info("\ud83d\udcca --- Weapon Distribution Report ---");
        counts.forEach((type, count) -> this.plugin.getLogger().info("  > " + type.getDisplayName() + ": " + count + " players"));
        this.plugin.getLogger().info("\ud83d\udcca ----------------------------------");
    }

    public void saveAll() {
        this.saveAll(true);
    }

    /** Autosave: snapshots on the main thread, writes the file and database in the background. */
    public void saveAllAsync() {
        this.saveAll(false);
    }

    private void saveAll(boolean sync) {
        if (!Bukkit.isPrimaryThread()) {
            // The YAML tree is only touched on the main thread; hop over instead of racing it.
            if (this.plugin.isEnabled()) {
                Bukkit.getScheduler().runTask((org.bukkit.plugin.Plugin)this.plugin, () -> this.saveAll(false));
            }
            return;
        }
        HashSet<UUID> allUuids = new HashSet<UUID>();
        allUuids.addAll(this.strengthCache.keySet());
        allUuids.addAll(this.receivedFreeRerollCache.keySet());
        allUuids.addAll(this.weaponCache.keySet());
        allUuids.addAll(this.playerContractsCache.keySet());
        allUuids.addAll(this.bountyCache.keySet());
        for (UUID uuid : allUuids) {
            this.savePlayerToConfig(uuid);
        }
        this.data.set("system.discord.dashboard_id", (Object)this.dashboardMessageId);
        this.data.set("system.discord.leaderboard_id", (Object)this.leaderboardMessageId);
        this.data.set("system.discord.monarch_id", (Object)this.monarchMessageId);
        this.data.set("system.enabled", (Object)this.systemEnabled);
        this.data.set("system.last_contract_refresh", (Object)this.lastContractRefresh);
        this.data.set("system.last_season_reset", (Object)this.lastSeasonReset);
        this.data.set("system.last_monarch_update", (Object)this.lastMonarchUpdate);
        this.data.set("system.monarch_start_time", (Object)this.monarchStartTime);
        this.data.set("system.monarch_uuid", this.monarchUUID != null ? this.monarchUUID.toString() : null);
        if (sync) {
            this.flushWrites();
            this.writeYaml(this.data.saveToString());
            this.saveDatabase();
        } else {
            String snapshot = this.data.saveToString();
            this.fileWriter.execute(() -> {
                this.writeYaml(snapshot);
                this.saveDatabase();
            });
        }
    }

    private void saveDatabase() {
        if (this.databaseManager != null) {
            this.databaseManager.saveAllSync(this);
            this.databaseManager.saveSystemVariable("monarch_uuid", this.monarchUUID != null ? this.monarchUUID.toString() : "");
            this.databaseManager.saveSystemVariable("last_monarch_update", String.valueOf(this.lastMonarchUpdate));
            this.databaseManager.saveSystemVariable("monarch_start_time", String.valueOf(this.monarchStartTime));
            this.databaseManager.saveSystemVariable("system_enabled", String.valueOf(this.systemEnabled));
        }
    }

    /** Waits for queued background writes, then stops the writer. Call once on shutdown. */
    public void shutdown() {
        this.saveAll(true);
        this.fileWriter.shutdown();
    }

    private void flushWrites() {
        try {
            this.fileWriter.submit(() -> {}).get(10L, TimeUnit.SECONDS);
        }
        catch (Exception exception) {
            // writer already stopped or stuck; the sync write below still goes through
        }
    }

    public void savePlayer(UUID uuid) {
        if (!Bukkit.isPrimaryThread()) {
            if (this.plugin.isEnabled()) {
                Bukkit.getScheduler().runTask((org.bukkit.plugin.Plugin)this.plugin, () -> this.savePlayer(uuid));
            }
            return;
        }
        this.savePlayerToConfig(uuid);
        this.saveConfigToDisk();
        if (this.databaseManager != null) {
            this.databaseManager.savePlayerAsync(uuid, this);
        }
    }

    private void savePlayerToConfig(UUID uuid) {
        Map<String, Long> cds;
        String path = "players." + String.valueOf(uuid);
        if (this.strengthCache.containsKey(uuid)) {
            int currentStr = this.strengthCache.get(uuid);
            this.data.set(path + ".strength", (Object)currentStr);
            String rankName = this.plugin.getConfigManager().getRankNameForStrength(currentStr);
            this.data.set(path + ".rank_name", (Object)rankName);
        }
        if (this.killsCache.containsKey(uuid)) {
            this.data.set(path + ".kills", (Object)this.killsCache.get(uuid));
        }
        if (this.deathsCache.containsKey(uuid)) {
            this.data.set(path + ".deaths", (Object)this.deathsCache.get(uuid));
        }
        if (this.weaponCache.containsKey(uuid)) {
            this.data.set(path + ".weapon", (Object)this.weaponCache.get(uuid).name());
        }
        this.data.set(path + ".received_free_reroll", (Object)this.receivedFreeRerollCache.getOrDefault(uuid, false));
        Long punishExp = this.punishmentExpiryCache.get(uuid);
        this.data.set(path + ".punishment_expiry", (Object)(punishExp != null ? punishExp : 0L));
        Long dpExp = this.deathProtectionExpiryCache.get(uuid);
        this.data.set(path + ".death_protection_expiry", (Object)(dpExp != null ? dpExp : 0L));
        Long nbExp = this.newbieProtectionExpiryCache.get(uuid);
        this.data.set(path + ".newbie_protection_expiry", (Object)(nbExp != null ? nbExp : 0L));
        // Clear map-shaped sections first, or removed entries survive in the file and come back on restart.
        this.data.set(path + ".bounties", null);
        this.data.set(path + ".contract_progress", null);
        this.data.set(path + ".ability_cooldowns", null);
        Map<UUID, Integer> bounties = this.bountyCache.get(uuid);
        if (bounties != null && !bounties.isEmpty()) {
            for (Map.Entry<UUID, Integer> e : bounties.entrySet()) {
                this.data.set(path + ".bounties." + String.valueOf(e.getKey()), (Object)e.getValue());
            }
        }
        if (this.playerContractsCache.containsKey(uuid)) {
            this.data.set(path + ".contracts", this.playerContractsCache.get(uuid));
            this.data.set(path + ".contract_date", (Object)this.playerContractDateCache.get(uuid));
            Map<String, Integer> progress = this.contractProgressCache.get(uuid);
            if (progress != null) {
                for (Map.Entry<String, Integer> entry : progress.entrySet()) {
                    this.data.set(path + ".contract_progress." + entry.getKey(), (Object)entry.getValue());
                }
            }
        }
        if ((cds = this.abilityCooldownCache.get(uuid)) != null && !cds.isEmpty()) {
            for (Map.Entry<String, Long> entry : cds.entrySet()) {
                this.data.set(path + ".ability_cooldowns." + entry.getKey(), (Object)entry.getValue());
            }
        }
        if (this.playerSkillsCache.containsKey(uuid)) {
            this.data.set(path + ".skills", new ArrayList(this.playerSkillsCache.get(uuid)));
        }
    }

    private void saveConfigToDisk() {
        if (!this.plugin.isEnabled() || this.fileWriter.isShutdown()) {
            this.writeYaml(this.data.saveToString());
            return;
        }
        String snapshot = this.data.saveToString();
        this.fileWriter.execute(() -> this.writeYaml(snapshot));
    }

    private void writeYaml(String yaml) {
        synchronized (this.fileLock) {
            try {
                File tempFile = new File(this.plugin.getDataFolder(), "playerdata.yml.tmp");
                java.nio.file.Files.write(tempFile.toPath(), yaml.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                try {
                    java.nio.file.Files.move(tempFile.toPath(), this.dataFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
                }
                catch (java.nio.file.AtomicMoveNotSupportedException e) {
                    java.nio.file.Files.move(tempFile.toPath(), this.dataFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }
            catch (IOException e) {
                this.plugin.getLogger().severe("Failed to save data safely: " + e.getMessage());
            }
        }
    }

    public int getStrength(UUID uuid) {
        return this.strengthCache.getOrDefault(uuid, this.plugin.getConfigManager().getDefaultStrength());
    }

    public void setStrength(UUID uuid, int strength) {
        int min = this.plugin.getConfigManager().getMinStrength();
        int max = this.plugin.getConfigManager().getMaxStrength();
        this.strengthCache.compute(uuid, (k, v) -> Math.max(min, Math.min(max, strength)));
        this.savePlayer(uuid);
    }

    public void addStrength(UUID uuid, int amount) {
        this.setStrength(uuid, this.getStrength(uuid) + amount);
    }

    public int addStrengthCapped(UUID uuid, int amount) {
        int max = this.plugin.getConfigManager().getMaxStrength();
        int[] actualAdded = new int[]{0};
        this.strengthCache.compute(uuid, (k, current) -> {
            if (current == null) {
                current = this.plugin.getConfigManager().getDefaultStrength();
            }
            if (current >= max) {
                actualAdded[0] = 0;
                return current;
            }
            actualAdded[0] = Math.min(amount, max - current);
            return current + actualAdded[0];
        });
        this.savePlayer(uuid);
        return actualAdded[0];
    }

    public boolean isAtMaxStrength(UUID uuid) {
        return this.getStrength(uuid) >= this.plugin.getConfigManager().getMaxStrength();
    }

    public void subtractStrength(UUID uuid, int amount) {
        int min = this.plugin.getConfigManager().getMinStrength();
        this.strengthCache.compute(uuid, (k, current) -> {
            if (current == null) {
                current = this.plugin.getConfigManager().getDefaultStrength();
            }
            return Math.max(min, current - amount);
        });
        this.savePlayer(uuid);
    }

    public String getRank(UUID uuid) {
        ArrayList<Map.Entry<UUID, Integer>> sorted = new ArrayList<Map.Entry<UUID, Integer>>(this.strengthCache.entrySet());
        sorted.sort((a, b) -> ((Integer)b.getValue()).compareTo((Integer)a.getValue()));
        for (int i = 0; i < sorted.size(); ++i) {
            if (!((UUID)((Map.Entry)sorted.get(i)).getKey()).equals(uuid)) continue;
            return "#" + (i + 1);
        }
        return "#?";
    }

    public int getKills(UUID uuid) {
        return this.killsCache.getOrDefault(uuid, 0);
    }

    public void addKill(UUID uuid) {
        this.killsCache.put(uuid, this.getKills(uuid) + 1);
        this.savePlayer(uuid);
    }

    public int getDeaths(UUID uuid) {
        return this.deathsCache.getOrDefault(uuid, 0);
    }

    public void addDeath(UUID uuid) {
        this.deathsCache.put(uuid, this.getDeaths(uuid) + 1);
        this.savePlayer(uuid);
    }

    public int resetAllKills() {
        ConfigurationSection players;
        for (UUID uuid : this.killsCache.keySet()) {
            this.killsCache.put(uuid, 0);
        }
        int count = 0;
        if (this.data.contains("players") && (players = this.data.getConfigurationSection("players")) != null) {
            for (String uuidStr : players.getKeys(false)) {
                this.data.set("players." + uuidStr + ".kills", (Object)0);
                try {
                    this.killsCache.put(UUID.fromString(uuidStr), 0);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                ++count;
            }
        }
        this.saveAll();
        return count;
    }

    public WeaponType getWeaponType(UUID uuid) {
        return this.weaponCache.get(uuid);
    }

    public void setWeaponType(UUID uuid, WeaponType weapon) {
        this.weaponCache.put(uuid, weapon);
        this.setBoundWeaponId(uuid, null);
        this.setHasBoundWeapon(uuid, false);
        this.savePlayer(uuid);
        Player player = Bukkit.getPlayer((UUID)uuid);
        if (player != null && player.isOnline()) {
            ItemFactory.unbindAllPlayerItems(player);
        }
    }

    public WeaponType getWeapon(UUID uuid) {
        return this.getWeaponType(uuid);
    }

    public void setWeapon(UUID uuid, WeaponType weapon) {
        this.setWeaponType(uuid, weapon);
    }

    public int getBounty(UUID uuid) {
        return this.bountyCache.getOrDefault(uuid, Collections.emptyMap()).values().stream().mapToInt(Integer::intValue).sum();
    }

    public void setBounty(UUID targetUUID, UUID setterUUID, int amount) {
        this.bountyCache.computeIfAbsent(targetUUID, k -> new ConcurrentHashMap()).put(setterUUID, amount);
        this.savePlayer(targetUUID);
    }

    public void addBounty(UUID targetUUID, UUID setterUUID, int amount) {
        Map<UUID, Integer> targetBounties = this.bountyCache.computeIfAbsent(targetUUID, k -> new ConcurrentHashMap<UUID, Integer>());
        int current = targetBounties.getOrDefault(setterUUID, 0);
        targetBounties.put(setterUUID, current + amount);
        this.savePlayer(targetUUID);
    }

    public void removeBounty(UUID uuid) {
        this.bountyCache.remove(uuid);
        this.savePlayer(uuid);
    }

    public long getBountyGlowExpiration(UUID uuid) {
        return this.bountyGlowExpirationCache.getOrDefault(uuid, 0L);
    }

    public void setBountyGlowExpiration(UUID uuid, long timestamp) {
        this.bountyGlowExpirationCache.put(uuid, timestamp);
        this.savePlayer(uuid);
    }

    public void clearBountyGlowExpiration(UUID uuid) {
        this.bountyGlowExpirationCache.remove(uuid);
        this.savePlayer(uuid);
    }

    public long getAbilityCooldown(UUID uuid) {
        return this.getAbilityCooldown(uuid, "ultimate");
    }

    public long getAbilityCooldown(UUID uuid, String key) {
        return this.getAbilityCooldownMap(uuid).getOrDefault(key, 0L);
    }

    public void setAbilityCooldown(UUID uuid, long timestamp) {
        this.setAbilityCooldown(uuid, "ultimate", timestamp);
    }

    public void setAbilityCooldown(UUID uuid, String key, long timestamp) {
        this.getAbilityCooldownMap(uuid).put(key, timestamp);
        if (key != null && (key.endsWith("_reflect") || key.endsWith("_blink") || key.endsWith("_explosive") || key.endsWith("_riptide") || key.endsWith("_burst") || key.endsWith("_pound")) && this.getBoundWeaponId(uuid) != null) {
            this.incrementBoundStat(uuid, "ability_uses");
        }
    }

    private Map<String, Long> getAbilityCooldownMap(UUID uuid) {
        return this.abilityCooldownCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap());
    }

    public String getPlayerContractDate(UUID uuid) {
        return this.playerContractDateCache.get(uuid);
    }

    public void setPlayerContractDate(UUID uuid, String date) {
        this.playerContractDateCache.put(uuid, date);
        this.savePlayer(uuid);
    }

    public List<String> getPlayerContracts(UUID uuid) {
        return this.playerContractsCache.getOrDefault(uuid, new ArrayList());
    }

    public void setPlayerContracts(UUID uuid, List<String> contracts) {
        this.playerContractsCache.put(uuid, contracts);
        this.savePlayer(uuid);
    }

    public Map<String, Integer> getContractProgress(UUID uuid) {
        return this.contractProgressCache.computeIfAbsent(uuid, k -> new HashMap());
    }

    public void updateContractProgress(UUID uuid, String contract, int value) {
        this.contractProgressCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap()).put(contract, value);
        this.savePlayer(uuid);
    }

    public void resetContractProgress(UUID uuid) {
        this.contractProgressCache.put(uuid, new HashMap());
        this.savePlayer(uuid);
    }

    public long getLastContractRefresh() {
        return this.lastContractRefresh;
    }

    public long getLastKillTime(UUID attacker, UUID victim) {
        Map<UUID, Long> history = this.killHistoryCache.get(attacker);
        if (history == null) {
            return 0L;
        }
        return history.getOrDefault(victim, 0L);
    }

    public void recordKill(UUID attacker, UUID victim) {
        this.killHistoryCache.computeIfAbsent(attacker, k -> new ConcurrentHashMap()).put(victim, System.currentTimeMillis());
        this.savePlayer(attacker);
    }

    public void clearKillHistory(UUID uuid) {
        this.killHistoryCache.remove(uuid);
    }

    public int getKillHistoryCount(UUID uuid) {
        Map<UUID, Long> history = this.killHistoryCache.get(uuid);
        if (history == null) {
            return 0;
        }
        long now = System.currentTimeMillis();
        long window = 3600000L;
        return (int)history.values().stream().filter(t -> now - t < window).count();
    }

    public long getLastSeasonReset() {
        return this.lastSeasonReset;
    }

    public void setLastSeasonReset(long t) {
        this.lastSeasonReset = t;
    }

    public void resetAllStats() {
        this.strengthCache.clear();
        this.killsCache.clear();
        this.deathsCache.clear();
        this.bountyCache.clear();
        this.contractProgressCache.clear();
        this.playerContractsCache.clear();
        this.playerContractDateCache.clear();
        this.monarchUUID = null;
        for (Player p : Bukkit.getOnlinePlayers()) {
            this.initializePlayer(p.getUniqueId());
        }
        this.saveAll();
    }

    public UUID getMonarch() {
        return this.monarchUUID;
    }

    public void setMonarch(UUID uuid) {
        this.monarchUUID = uuid;
        this.monarchStartTime = System.currentTimeMillis();
        this.saveAll();
    }

    public long getMonarchStartTime() {
        return this.monarchStartTime;
    }

    public long getLastMonarchUpdate() {
        return this.lastMonarchUpdate;
    }

    public void setLastMonarchUpdate(long t) {
        this.lastMonarchUpdate = t;
    }

    public void calculateMonarch() {
        UUID topPlayer = null;
        int topStrength = -1;
        int topKills = -1;
        int topDeaths = Integer.MAX_VALUE;
        for (Map.Entry<UUID, Integer> entry : this.strengthCache.entrySet()) {
            UUID uuid = entry.getKey();
            int strength = entry.getValue();
            int kills = this.getKills(uuid);
            int deaths = this.getDeaths(uuid);
            if (strength <= 0) continue;
            if (strength > topStrength) {
                topStrength = strength;
                topKills = kills;
                topDeaths = deaths;
                topPlayer = uuid;
                continue;
            }
            if (strength != topStrength) continue;
            if (kills > topKills) {
                topKills = kills;
                topDeaths = deaths;
                topPlayer = uuid;
                continue;
            }
            if (kills != topKills || deaths >= topDeaths) continue;
            topDeaths = deaths;
            topPlayer = uuid;
        }
        this.monarchUUID = topPlayer;
        this.saveAll();
    }

    public String getTopPlayerName() {
        UUID monarch = this.getMonarch();
        if (monarch == null) {
            return "None";
        }
        return Bukkit.getOfflinePlayer((UUID)monarch).getName();
    }

    public int getTopPlayerStrength() {
        UUID monarch = this.getMonarch();
        if (monarch == null) {
            return 0;
        }
        return this.getStrength(monarch);
    }

    public boolean hasReceivedFreeReroll(UUID uuid) {
        return this.receivedFreeRerollCache.getOrDefault(uuid, false);
    }

    public void setReceivedFreeReroll(UUID uuid, boolean val) {
        this.receivedFreeRerollCache.put(uuid, val);
        this.savePlayer(uuid);
    }

    public boolean isPunished(UUID uuid) {
        Long expiry = this.punishmentExpiryCache.get(uuid);
        if (expiry == null || expiry <= 0L) {
            return false;
        }
        if (System.currentTimeMillis() > expiry) {
            this.punishmentExpiryCache.remove(uuid);
            return false;
        }
        return true;
    }

    public void setPunishment(UUID uuid, long expiryTimestamp) {
        this.punishmentExpiryCache.put(uuid, expiryTimestamp);
        this.savePlayer(uuid);
    }

    public void clearPunishment(UUID uuid) {
        this.punishmentExpiryCache.remove(uuid);
        this.savePlayer(uuid);
    }

    public long getPunishmentExpiry(UUID uuid) {
        return this.punishmentExpiryCache.getOrDefault(uuid, 0L);
    }

    public boolean hasDeathProtection(UUID uuid) {
        Long expiry = this.deathProtectionExpiryCache.get(uuid);
        if (expiry == null || expiry <= 0L) {
            return false;
        }
        if (System.currentTimeMillis() > expiry) {
            this.deathProtectionExpiryCache.remove(uuid);
            return false;
        }
        return true;
    }

    public void setDeathProtection(UUID uuid, long expiryTimestamp) {
        this.deathProtectionExpiryCache.put(uuid, expiryTimestamp);
        this.savePlayer(uuid);
    }

    public void clearDeathProtection(UUID uuid) {
        this.deathProtectionExpiryCache.remove(uuid);
        this.savePlayer(uuid);
    }

    public long getDeathProtectionExpiry(UUID uuid) {
        return this.deathProtectionExpiryCache.getOrDefault(uuid, 0L);
    }

    public boolean isNewbieProtected(UUID uuid) {
        Long expiry = this.newbieProtectionExpiryCache.get(uuid);
        if (expiry == null || expiry <= 0L) {
            return false;
        }
        if (System.currentTimeMillis() > expiry) {
            this.newbieProtectionExpiryCache.remove(uuid);
            return false;
        }
        return true;
    }

    public void setNewbieProtection(UUID uuid, long expiryTimestamp) {
        this.newbieProtectionExpiryCache.put(uuid, expiryTimestamp);
        this.savePlayer(uuid);
    }

    public void clearNewbieProtection(UUID uuid) {
        this.newbieProtectionExpiryCache.remove(uuid);
        this.savePlayer(uuid);
    }

    public long getNewbieProtectionExpiry(UUID uuid) {
        return this.newbieProtectionExpiryCache.getOrDefault(uuid, 0L);
    }

    public void initializePlayer(UUID uuid) {
        // New players start with no weapon assigned until they use their free starter reroll book
        if (!this.strengthCache.containsKey(uuid)) {
            this.strengthCache.put(uuid, this.plugin.getConfigManager().getDefaultStrength());
        }
        if (!this.killsCache.containsKey(uuid)) {
            this.killsCache.put(uuid, 0);
        }
        if (!this.deathsCache.containsKey(uuid)) {
            this.deathsCache.put(uuid, 0);
        }
        if (!this.receivedFreeRerollCache.containsKey(uuid)) {
            this.receivedFreeRerollCache.put(uuid, false);
        }
        this.savePlayer(uuid);
    }

    public boolean hasSkill(UUID uuid, String skillId) {
        return this.playerSkillsCache.getOrDefault(uuid, Collections.emptySet()).contains(skillId);
    }

    public void addSkill(UUID uuid, String skillId) {
        this.playerSkillsCache.computeIfAbsent(uuid, k -> new HashSet()).add(skillId);
        this.savePlayer(uuid);
    }

    public Set<String> getSkills(UUID uuid) {
        return this.playerSkillsCache.getOrDefault(uuid, Collections.emptySet());
    }

    public Map<UUID, Integer> getStrengthCache() {
        return this.strengthCache;
    }

    public Map<UUID, Integer> getKillsCache() {
        return this.killsCache;
    }

    public Map<UUID, Integer> getDeathsCache() {
        return this.deathsCache;
    }

    public Map<UUID, WeaponType> getWeaponCache() {
        return this.weaponCache;
    }

    public Map<UUID, Map<UUID, Integer>> getBountyCache() {
        return this.bountyCache;
    }

    public String getDashboardMessageId() {
        return this.dashboardMessageId;
    }

    public void setDashboardMessageId(String id) {
        this.dashboardMessageId = id;
        this.saveAll();
    }

    public String getLeaderboardMessageId() {
        return this.leaderboardMessageId;
    }

    public void setLeaderboardMessageId(String id) {
        this.leaderboardMessageId = id;
        this.saveAll();
    }

    public String getMonarchMessageId() {
        return this.monarchMessageId;
    }

    public void setMonarchMessageId(String id) {
        this.monarchMessageId = id;
        this.saveAll();
    }

    public boolean isSystemEnabled() {
        return this.systemEnabled;
    }

    public void setSystemEnabled(boolean e) {
        this.systemEnabled = e;
        this.saveAll();
    }

    /** Bound weapons waiting to be handed back after a death, kept on disk so a quit or crash can't eat them. */
    public List<org.bukkit.inventory.ItemStack> getPendingBoundItems(UUID uuid) {
        List<?> raw = this.data.getList("players." + String.valueOf(uuid) + ".pending_bound", null);
        ArrayList<org.bukkit.inventory.ItemStack> items = new ArrayList<org.bukkit.inventory.ItemStack>();
        if (raw != null) {
            for (Object o : raw) {
                if (o instanceof org.bukkit.inventory.ItemStack) {
                    items.add((org.bukkit.inventory.ItemStack)o);
                }
            }
        }
        return items;
    }

    public void setPendingBoundItems(UUID uuid, List<org.bukkit.inventory.ItemStack> items) {
        this.data.set("players." + String.valueOf(uuid) + ".pending_bound", items == null || items.isEmpty() ? null : new ArrayList<org.bukkit.inventory.ItemStack>(items));
        this.saveConfigToDisk();
    }

    public String getBoundWeaponId(UUID uuid) {
        return this.data.getString("players." + String.valueOf(uuid) + ".bound_weapon_id", null);
    }

    public void setBoundWeaponId(UUID uuid, String itemId) {
        this.data.set("players." + String.valueOf(uuid) + ".bound_weapon_id", (Object)itemId);
        this.savePlayer(uuid);
    }

    public boolean hasBoundWeapon(UUID uuid) {
        return this.data.getBoolean("players." + String.valueOf(uuid) + ".has_bound_weapon", false);
    }

    public void setHasBoundWeapon(UUID uuid, boolean val) {
        this.data.set("players." + String.valueOf(uuid) + ".has_bound_weapon", (Object)val);
        this.savePlayer(uuid);
    }

    public boolean isFreeBindAvailable(UUID uuid) {
        return this.data.getBoolean("players." + String.valueOf(uuid) + ".free_bind_available", false);
    }

    public void setFreeBindAvailable(UUID uuid, boolean val) {
        this.data.set("players." + String.valueOf(uuid) + ".free_bind_available", (Object)val);
        this.savePlayer(uuid);
    }

    public void incrementBoundStat(UUID uuid, String stat) {
        String path = "players." + String.valueOf(uuid) + ".bound_stats." + stat;
        int current = this.data.getInt(path, 0);
        this.data.set(path, (Object)(current + 1));
        this.savePlayer(uuid);
    }

    public void addBoundStatDouble(UUID uuid, String stat, double amount) {
        String path = "players." + String.valueOf(uuid) + ".bound_stats." + stat;
        double current = this.data.getDouble(path, 0.0);
        this.data.set(path, (Object)(current + amount));
        this.savePlayer(uuid);
    }

    public int getBoundStat(UUID uuid, String stat) {
        return this.data.getInt("players." + String.valueOf(uuid) + ".bound_stats." + stat, 0);
    }

    public double getBoundStatDouble(UUID uuid, String stat) {
        return this.data.getDouble("players." + String.valueOf(uuid) + ".bound_stats." + stat, 0.0);
    }

    public void setBoundTimestamp(UUID uuid, long timestamp) {
        this.data.set("players." + String.valueOf(uuid) + ".bound_stats.bound_timestamp", (Object)timestamp);
        this.savePlayer(uuid);
    }

    public long getBoundTimestamp(UUID uuid) {
        return this.data.getLong("players." + String.valueOf(uuid) + ".bound_stats.bound_timestamp", 0L);
    }

    public Map<String, Integer> getBoundKillsBreakdown(UUID uuid) {
        HashMap<String, Integer> breakdown = new HashMap<String, Integer>();
        ConfigurationSection sec = this.data.getConfigurationSection("players." + String.valueOf(uuid) + ".bound_stats.kills_breakdown");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                breakdown.put(key, sec.getInt(key, 0));
            }
        }
        return breakdown;
    }

    public void recordBoundKill(UUID uuid, String victimName) {
        String path = "players." + String.valueOf(uuid) + ".bound_stats.kills_breakdown." + victimName;
        int count = this.data.getInt(path, 0);
        this.data.set(path, (Object)(count + 1));
        this.incrementBoundStat(uuid, "kills");
    }

    public void setSystemVariableFromDb(String k, String v) {
        if ("monarch_uuid".equalsIgnoreCase(k)) {
            try { this.monarchUUID = UUID.fromString(v); } catch (Exception ignored) {}
        } else if ("last_monarch_update".equalsIgnoreCase(k)) {
            try { this.lastMonarchUpdate = Long.parseLong(v); } catch (Exception ignored) {}
        } else if ("monarch_start_time".equalsIgnoreCase(k)) {
            try { this.monarchStartTime = Long.parseLong(v); } catch (Exception ignored) {}
        } else if ("system_enabled".equalsIgnoreCase(k)) {
            this.systemEnabled = Boolean.parseBoolean(v);
        }
    }

    public Map<UUID, Boolean> getReceivedFreeRerollCache() { return this.receivedFreeRerollCache; }
    public Map<UUID, Long> getPunishmentExpiryCache() { return this.punishmentExpiryCache; }
    public Map<UUID, Long> getDeathProtectionExpiryCache() { return this.deathProtectionExpiryCache; }
    public Map<UUID, Long> getNewbieProtectionExpiryCache() { return this.newbieProtectionExpiryCache; }
    public Map<UUID, List<String>> getPlayerContractsCache() { return this.playerContractsCache; }
    public Map<UUID, Map<String, Integer>> getContractProgressCache() { return this.contractProgressCache; }
    public Map<UUID, Set<String>> getPlayerSkillsCache() { return this.playerSkillsCache; }
    public Map<UUID, Map<String, Long>> getAbilityCooldownCache() { return this.abilityCooldownCache; }
    public Map<UUID, Map<UUID, Long>> getKillHistoryCache() { return this.killHistoryCache; }
    public DatabaseManager getDatabaseManager() { return this.databaseManager; }
    public FileConfiguration getData() { return this.data; }
}

