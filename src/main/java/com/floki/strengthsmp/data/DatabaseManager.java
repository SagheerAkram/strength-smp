package com.floki.strengthsmp.data;

import com.floki.strengthsmp.StrengthSMP;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

public class DatabaseManager {
    public enum DatabaseType {
        SQLITE,
        MYSQL,
        YAML
    }

    private final StrengthSMP plugin;
    private final DatabaseType type;
    private final String url;
    private final String user;
    private final String password;
    private final Gson gson = new Gson();

    private final Type stringMapType = new TypeToken<Map<String, Object>>() {}.getType();
    private final Type uuidIntMapType = new TypeToken<Map<String, Integer>>() {}.getType();
    private final Type stringIntMapType = new TypeToken<Map<String, Integer>>() {}.getType();
    private final Type stringLongMapType = new TypeToken<Map<String, Long>>() {}.getType();
    private final Type stringListType = new TypeToken<List<String>>() {}.getType();

    public DatabaseManager(StrengthSMP plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        String typeStr = config.getString("database.type", "SQLITE").toUpperCase();

        DatabaseType resolvedType;
        try {
            resolvedType = DatabaseType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            resolvedType = DatabaseType.SQLITE;
        }
        if (!StrengthSMP.isPro() && resolvedType != DatabaseType.YAML) {
            plugin.getLogger().info("SQLite/MySQL storage is a StrengthSMP Pro feature; using playerdata.yml.");
            resolvedType = DatabaseType.YAML;
        }
        this.type = resolvedType;

        if (this.type == DatabaseType.MYSQL) {
            String host = config.getString("database.mysql.host", "localhost");
            int port = config.getInt("database.mysql.port", 3306);
            String db = config.getString("database.mysql.database", "strengthsmp");
            this.user = config.getString("database.mysql.username", "root");
            this.password = config.getString("database.mysql.password", "");
            this.url = "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&autoReconnect=true&allowPublicKeyRetrieval=true&characterEncoding=utf8";
        } else if (this.type == DatabaseType.SQLITE) {
            File dbFile = new File(plugin.getDataFolder(), "database.db");
            this.url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            this.user = null;
            this.password = null;
        } else {
            this.url = null;
            this.user = null;
            this.password = null;
        }

        if (this.type != DatabaseType.YAML) {
            initTables();
        }
    }

    public DatabaseType getType() {
        return type;
    }

    private Connection getConnection() throws SQLException {
        if (type == DatabaseType.MYSQL) {
            return DriverManager.getConnection(url, user, password);
        } else if (type == DatabaseType.SQLITE) {
            return DriverManager.getConnection(url);
        }
        return null;
    }

    private void initTables() {
        try (Connection conn = getConnection()) {
            if (conn == null) return;

            try (Statement st = conn.createStatement()) {
                // Players table
                st.executeUpdate("CREATE TABLE IF NOT EXISTS ssmp_players (" +
                        "uuid VARCHAR(36) PRIMARY KEY, " +
                        "strength INT DEFAULT 0, " +
                        "weapon VARCHAR(32), " +
                        "kills INT DEFAULT 0, " +
                        "deaths INT DEFAULT 0, " +
                        "free_reroll INT DEFAULT 0, " +
                        "punish_exp BIGINT DEFAULT 0, " +
                        "dp_exp BIGINT DEFAULT 0, " +
                        "nb_exp BIGINT DEFAULT 0, " +
                        "bounties_json TEXT, " +
                        "contracts_json TEXT, " +
                        "contract_progress_json TEXT, " +
                        "skills_json TEXT, " +
                        "cooldowns_json TEXT, " +
                        "kill_history_json TEXT, " +
                        "last_updated BIGINT DEFAULT 0" +
                        ")");

                // System variables table
                st.executeUpdate("CREATE TABLE IF NOT EXISTS ssmp_system (" +
                        "k VARCHAR(64) PRIMARY KEY, " +
                        "v TEXT" +
                        ")");
            }

            plugin.getLogger().info("✓ Connected to " + type.name() + " Database successfully!");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize " + type.name() + " database tables: " + e.getMessage(), e);
        }
    }

    public void loadAll(DataManager dm) {
        if (type == DatabaseType.YAML) return;

        try (Connection conn = getConnection()) {
            if (conn == null) return;

            // Load System stats
            try (PreparedStatement ps = conn.prepareStatement("SELECT k, v FROM ssmp_system");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String k = rs.getString("k");
                    String v = rs.getString("v");
                    dm.setSystemVariableFromDb(k, v);
                }
            }

            // Load Players
            int count = 0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM ssmp_players");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try {
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        int strength = rs.getInt("strength");
                        String weaponStr = rs.getString("weapon");
                        int kills = rs.getInt("kills");
                        int deaths = rs.getInt("deaths");
                        boolean freeReroll = rs.getInt("free_reroll") == 1;
                        long punishExp = rs.getLong("punish_exp");
                        long dpExp = rs.getLong("dp_exp");
                        long nbExp = rs.getLong("nb_exp");

                        dm.getStrengthCache().put(uuid, strength);
                        dm.getKillsCache().put(uuid, kills);
                        dm.getDeathsCache().put(uuid, deaths);
                        dm.getReceivedFreeRerollCache().put(uuid, freeReroll);

                        if (weaponStr != null && !weaponStr.isEmpty()) {
                            try {
                                dm.getWeaponCache().put(uuid, WeaponType.valueOf(weaponStr));
                            } catch (Exception ignored) {
                                dm.getWeaponCache().put(uuid, WeaponType.getRandomWeapon());
                            }
                        }

                        if (punishExp > 0) dm.getPunishmentExpiryCache().put(uuid, punishExp);
                        if (dpExp > 0) dm.getDeathProtectionExpiryCache().put(uuid, dpExp);
                        if (nbExp > 0) dm.getNewbieProtectionExpiryCache().put(uuid, nbExp);

                        // Bounties JSON
                        String bountiesJson = rs.getString("bounties_json");
                        // The database is authoritative: no bounties here means none, even if old YAML still lists some.
                        dm.getBountyCache().remove(uuid);
                        if (bountiesJson != null && !bountiesJson.isEmpty()) {
                            Map<String, Integer> map = gson.fromJson(bountiesJson, uuidIntMapType);
                            if (map != null) {
                                Map<UUID, Integer> converted = new HashMap<>();
                                for (Map.Entry<String, Integer> entry : map.entrySet()) {
                                    try {
                                        converted.put(UUID.fromString(entry.getKey()), entry.getValue());
                                    } catch (Exception ignored) {}
                                }
                                dm.getBountyCache().put(uuid, converted);
                            }
                        }

                        // Contracts JSON
                        String contractsJson = rs.getString("contracts_json");
                        if (contractsJson != null && !contractsJson.isEmpty()) {
                            List<String> list = gson.fromJson(contractsJson, stringListType);
                            if (list != null) dm.getPlayerContractsCache().put(uuid, list);
                        }

                        // Contract Progress JSON
                        String progressJson = rs.getString("contract_progress_json");
                        if (progressJson != null && !progressJson.isEmpty()) {
                            Map<String, Integer> progress = gson.fromJson(progressJson, stringIntMapType);
                            if (progress != null) dm.getContractProgressCache().put(uuid, progress);
                        }

                        // Skills JSON
                        String skillsJson = rs.getString("skills_json");
                        if (skillsJson != null && !skillsJson.isEmpty()) {
                            List<String> list = gson.fromJson(skillsJson, stringListType);
                            if (list != null) dm.getPlayerSkillsCache().put(uuid, new HashSet<>(list));
                        }

                        // Cooldowns JSON
                        String cdJson = rs.getString("cooldowns_json");
                        if (cdJson != null && !cdJson.isEmpty()) {
                            Map<String, Long> cdMap = gson.fromJson(cdJson, stringLongMapType);
                            if (cdMap != null) dm.getAbilityCooldownCache().put(uuid, cdMap);
                        }

                        count++;
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error reading player record from DB: " + e.getMessage());
                    }
                }
            }

            plugin.getLogger().info("✓ Loaded " + count + " players from " + type.name() + " Database!");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error loading data from " + type.name() + ": " + e.getMessage(), e);
        }
    }

    public void savePlayerAsync(UUID uuid, DataManager dm) {
        if (type == DatabaseType.YAML) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> savePlayerSync(uuid, dm));
    }

    public void savePlayerSync(UUID uuid, DataManager dm) {
        if (type == DatabaseType.YAML) return;

        String query;
        if (type == DatabaseType.SQLITE) {
            query = "INSERT OR REPLACE INTO ssmp_players (" +
                    "uuid, strength, weapon, kills, deaths, free_reroll, punish_exp, dp_exp, nb_exp, " +
                    "bounties_json, contracts_json, contract_progress_json, skills_json, cooldowns_json, kill_history_json, last_updated) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            // MySQL
            query = "INSERT INTO ssmp_players (" +
                    "uuid, strength, weapon, kills, deaths, free_reroll, punish_exp, dp_exp, nb_exp, " +
                    "bounties_json, contracts_json, contract_progress_json, skills_json, cooldowns_json, kill_history_json, last_updated) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "strength=VALUES(strength), weapon=VALUES(weapon), kills=VALUES(kills), deaths=VALUES(deaths), " +
                    "free_reroll=VALUES(free_reroll), punish_exp=VALUES(punish_exp), dp_exp=VALUES(dp_exp), nb_exp=VALUES(nb_exp), " +
                    "bounties_json=VALUES(bounties_json), contracts_json=VALUES(contracts_json), " +
                    "contract_progress_json=VALUES(contract_progress_json), skills_json=VALUES(skills_json), " +
                    "cooldowns_json=VALUES(cooldowns_json), kill_history_json=VALUES(kill_history_json), last_updated=VALUES(last_updated)";
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            if (conn == null) return;

            ps.setString(1, uuid.toString());
            ps.setInt(2, dm.getStrengthCache().getOrDefault(uuid, 0));
            WeaponType wt = dm.getWeaponCache().get(uuid);
            ps.setString(3, wt != null ? wt.name() : null);
            ps.setInt(4, dm.getKillsCache().getOrDefault(uuid, 0));
            ps.setInt(5, dm.getDeathsCache().getOrDefault(uuid, 0));
            ps.setInt(6, dm.getReceivedFreeRerollCache().getOrDefault(uuid, false) ? 1 : 0);
            ps.setLong(7, dm.getPunishmentExpiryCache().getOrDefault(uuid, 0L));
            ps.setLong(8, dm.getDeathProtectionExpiryCache().getOrDefault(uuid, 0L));
            ps.setLong(9, dm.getNewbieProtectionExpiryCache().getOrDefault(uuid, 0L));

            // Bounties
            Map<UUID, Integer> bounties = dm.getBountyCache().get(uuid);
            if (bounties != null && !bounties.isEmpty()) {
                Map<String, Integer> stringMap = new HashMap<>();
                bounties.forEach((k, v) -> stringMap.put(k.toString(), v));
                ps.setString(10, gson.toJson(stringMap));
            } else {
                ps.setString(10, null);
            }

            // Contracts
            List<String> contracts = dm.getPlayerContractsCache().get(uuid);
            ps.setString(11, (contracts != null && !contracts.isEmpty()) ? gson.toJson(contracts) : null);

            // Contract Progress
            Map<String, Integer> progress = dm.getContractProgressCache().get(uuid);
            ps.setString(12, (progress != null && !progress.isEmpty()) ? gson.toJson(progress) : null);

            // Skills
            Set<String> skills = dm.getPlayerSkillsCache().get(uuid);
            ps.setString(13, (skills != null && !skills.isEmpty()) ? gson.toJson(new ArrayList<>(skills)) : null);

            // Cooldowns
            Map<String, Long> cds = dm.getAbilityCooldownCache().get(uuid);
            ps.setString(14, (cds != null && !cds.isEmpty()) ? gson.toJson(cds) : null);

            // Kill History
            Map<UUID, Long> history = dm.getKillHistoryCache().get(uuid);
            if (history != null && !history.isEmpty()) {
                Map<String, Long> stringHist = new HashMap<>();
                history.forEach((k, v) -> stringHist.put(k.toString(), v));
                ps.setString(15, gson.toJson(stringHist));
            } else {
                ps.setString(15, null);
            }

            ps.setLong(16, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player " + uuid + " to " + type.name() + ": " + e.getMessage(), e);
        }
    }

    public void saveSystemVariable(String key, String value) {
        if (type == DatabaseType.YAML) return;

        String query;
        if (type == DatabaseType.SQLITE) {
            query = "INSERT OR REPLACE INTO ssmp_system (k, v) VALUES (?, ?)";
        } else {
            query = "INSERT INTO ssmp_system (k, v) VALUES (?, ?) ON DUPLICATE KEY UPDATE v=VALUES(v)";
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            if (conn == null) return;
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to save system variable " + key + ": " + e.getMessage());
        }
    }

    public void saveAllSync(DataManager dm) {
        if (type == DatabaseType.YAML) return;
        Set<UUID> all = new HashSet<>();
        all.addAll(dm.getStrengthCache().keySet());
        all.addAll(dm.getWeaponCache().keySet());
        all.addAll(dm.getKillsCache().keySet());

        for (UUID uuid : all) {
            savePlayerSync(uuid, dm);
        }
    }
}
