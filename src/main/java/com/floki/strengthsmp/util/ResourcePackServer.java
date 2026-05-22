package com.floki.strengthsmp.util;

import com.floki.strengthsmp.StrengthSMP;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Files;

public class ResourcePackServer {

    private final StrengthSMP plugin;
    private HttpServer server;
    private String publicIp;
    private final File packFile;

    public ResourcePackServer(StrengthSMP plugin) {
        this.plugin = plugin;
        this.packFile = new File(plugin.getDataFolder(), "strengthsmp.zip");
    }

    private String calculatedHash = "";

    public void start() {
        if (!plugin.getConfigManager().isResourcePackEnabled()) return;
        if (!plugin.getConfigManager().getResourcePackMode().equalsIgnoreCase("INTERNAL")) return;

        // Always overwrite to ensure the zip is synchronized with compile/jar updates
        if (packFile.exists()) {
            packFile.delete();
        }
        try {
            plugin.saveResource("strengthsmp.zip", false);
        } catch (Exception e) {
            plugin.getLogger().warning("Could not save strengthsmp.zip: " + e.getMessage());
        }

        // Calculate dynamic SHA-1 hash to prevent stale hash mismatch errors on client join
        if (packFile.exists()) {
            calculatedHash = calculateSHA1(packFile);
        }

        try {
            int port = plugin.getConfigManager().getResourcePackPort();
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/strengthsmp.zip", new FileHandler());
            server.setExecutor(null);
            server.start();

            detectIp();

            plugin.getLogger().info("🌐 Internal Resource Pack Server started on port " + port);
            plugin.getLogger().info("🔗 Serving pack at: http://" + publicIp + ":" + port + "/strengthsmp.zip");
            plugin.getLogger().info("🔑 Calculated SHA-1 Hash: " + calculatedHash);
            
        } catch (IOException e) {
            plugin.getLogger().severe("❌ Failed to start internal resource pack server: " + e.getMessage());
        }
    }

    public String getPackHash() {
        if (calculatedHash.isEmpty() && packFile.exists()) {
            calculatedHash = calculateSHA1(packFile);
        }
        return calculatedHash;
    }

    private String calculateSHA1(File file) {
        try (InputStream is = new FileInputStream(file)) {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-1");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            plugin.getLogger().severe("❌ Error calculating SHA-1: " + e.getMessage());
            return "";
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void detectIp() {
        String configIp = plugin.getConfigManager().getResourcePackPublicIp();
        if (!configIp.equalsIgnoreCase("AUTO")) {
            this.publicIp = configIp;
            return;
        }

        try {
            URL url = new URL("https://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            this.publicIp = in.readLine().trim();
            in.close();
        } catch (Exception e) {
            this.publicIp = "localhost";
            plugin.getLogger().warning("⚠️ Could not detect public IP. Using localhost. Set public-ip in config if external players can't connect.");
        }
    }

    public String getPackUrl() {
        if (plugin.getConfigManager().getResourcePackMode().equalsIgnoreCase("INTERNAL")) {
            return "http://" + publicIp + ":" + plugin.getConfigManager().getResourcePackPort() + "/strengthsmp.zip";
        }
        return plugin.getConfigManager().getResourcePackUrl();
    }

    private class FileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!packFile.exists()) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            byte[] bytes = Files.readAllBytes(packFile.toPath());
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }
}
