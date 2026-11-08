package com.floki.strengthsmp.metrics;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

public class Metrics {
    private final Plugin plugin;
    private final int serviceId;

    public Metrics(JavaPlugin plugin, int serviceId) {
        this.plugin = plugin;
        this.serviceId = serviceId;

        File bStatsFolder = new File(plugin.getDataFolder().getParentFile(), "bStats");
        File configFile = new File(bStatsFolder, "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        if (!config.isSet("enabled")) {
            config.set("enabled", true);
        }
        if (!config.isSet("serverUuid")) {
            config.set("serverUuid", UUID.randomUUID().toString());
        }
        if (!config.isSet("logFailedRequests")) {
            config.set("logFailedRequests", false);
        }
        if (!config.isSet("logSentData")) {
            config.set("logSentData", false);
        }
        if (!config.isSet("logResponseStatusText")) {
            config.set("logResponseStatusText", false);
        }

        try {
            config.save(configFile);
        } catch (IOException ignored) {}

        boolean enabled = config.getBoolean("enabled", true);
        String serverUUID = config.getString("serverUuid");

        if (enabled) {
            startSubmitting(serverUUID);
        }
    }

    private void startSubmitting(String serverUUID) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                submitData(serverUUID);
            } catch (Exception ignored) {}
        }, 1, 30, TimeUnit.MINUTES);
    }

    private void submitData(String serverUUID) {
        try {
            int players = Bukkit.getOnlinePlayers().size();
            String mcVersion = Bukkit.getVersion();
            String pluginVersion = plugin.getDescription().getVersion();
            String javaVersion = System.getProperty("java.version");
            String osName = System.getProperty("os.name");
            String osArch = System.getProperty("os.arch");
            int coreCount = Runtime.getRuntime().availableProcessors();

            String json = "{"
                    + "\"serverUUID\":\"" + serverUUID + "\","
                    + "\"serviceId\":" + serviceId + ","
                    + "\"playerAmount\":" + players + ","
                    + "\"mcVersion\":\"" + mcVersion + "\","
                    + "\"pluginVersion\":\"" + pluginVersion + "\","
                    + "\"javaVersion\":\"" + javaVersion + "\","
                    + "\"osName\":\"" + osName + "\","
                    + "\"osArch\":\"" + osArch + "\","
                    + "\"coreCount\":" + coreCount
                    + "}";

            URL url = new URL("https://bstats.org/api/v1/data/bukkit");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
                gzos.write(json.getBytes(StandardCharsets.UTF_8));
            }
            byte[] compressedData = baos.toByteArray();

            conn.setRequestMethod("POST");
            conn.addRequestProperty("Accept", "application/json");
            conn.addRequestProperty("Connection", "close");
            conn.addRequestProperty("Content-Encoding", "gzip");
            conn.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("User-Agent", "MC-Server/" + pluginVersion);
            conn.setDoOutput(true);

            try (DataOutputStream dos = new DataOutputStream(conn.getOutputStream())) {
                dos.write(compressedData);
            }

            conn.getResponseCode();
            conn.disconnect();
        } catch (Exception ignored) {}
    }
}
