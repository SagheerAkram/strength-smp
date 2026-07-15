/*
 * Decompiled with CFR 0.152.
 */
package com.floki.strengthsmp.util;

import com.floki.strengthsmp.StrengthSMP;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;

public class ResourcePackServer {
    private final StrengthSMP plugin;
    private HttpServer server;
    private String publicIp = "127.0.0.1";
    private final File packFile;
    private String calculatedHash = "";

    public ResourcePackServer(StrengthSMP plugin) {
        this.plugin = plugin;
        this.packFile = new File(plugin.getDataFolder(), "strengthsmp.zip");
    }

    public void start() {
        if (!this.plugin.getConfigManager().isResourcePackEnabled()) {
            return;
        }
        if (!this.plugin.getConfigManager().getResourcePackMode().equalsIgnoreCase("INTERNAL")) {
            return;
        }
        this.detectIp();
        if (!this.packFile.exists()) {
            try {
                this.plugin.saveResource("strengthsmp.zip", false);
            }
            catch (Exception e) {
                this.plugin.getLogger().warning("Could not save strengthsmp.zip: " + e.getMessage());
            }
        }
        if (this.packFile.exists()) {
            this.calculatedHash = this.calculateSHA1(this.packFile);
        }
        try {
            int port = this.plugin.getConfigManager().getResourcePackPort();
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
            this.server.createContext("/strengthsmp.zip", new FileHandler());
            this.server.setExecutor(null);
            this.server.start();
            this.plugin.getLogger().info("\ud83c\udf10 Internal Resource Pack Server started on port " + port);
            this.plugin.getLogger().info("\ud83d\udd17 Serving pack at: http://" + this.publicIp + ":" + port + "/strengthsmp.zip");
            this.plugin.getLogger().info("\ud83d\udd11 Calculated SHA-1 Hash: " + this.calculatedHash);
        }
        catch (IOException e) {
            this.plugin.getLogger().warning("\u26a0\ufe0f Internal resource pack server port " + this.plugin.getConfigManager().getResourcePackPort() + " is already in use. The existing server on this port will continue serving the pack.");
            this.plugin.getLogger().info("\ud83d\udd17 Serving pack at: http://" + this.publicIp + ":" + this.plugin.getConfigManager().getResourcePackPort() + "/strengthsmp.zip");
            this.plugin.getLogger().info("\ud83d\udd11 Calculated SHA-1 Hash: " + this.calculatedHash);
        }
    }

    public String getPackHash() {
        if (this.plugin.getConfigManager().getResourcePackMode().equalsIgnoreCase("EXTERNAL")) {
            return this.plugin.getConfigManager().getResourcePackHash();
        }
        if (this.calculatedHash.isEmpty() && this.packFile.exists()) {
            this.calculatedHash = this.calculateSHA1(this.packFile);
        }
        return this.calculatedHash;
    }

    private String calculateSHA1(File file) {
        try (FileInputStream is = new FileInputStream(file)) {
            int read;
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] buffer = new byte[8192];
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
            this.plugin.getLogger().severe("Error calculating SHA-1: " + e.getMessage());
            return "";
        }
    }

    public void stop() {
        if (this.server != null) {
            this.server.stop(0);
        }
    }

    private void detectIp() {
        String configIp = this.plugin.getConfigManager().getResourcePackPublicIp();
        if (!configIp.equalsIgnoreCase("AUTO")) {
            this.publicIp = configIp;
            return;
        }
        try {
            URL url = new URL("https://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            this.publicIp = in.readLine().trim();
            in.close();
        }
        catch (Exception e) {
            this.publicIp = "localhost";
            this.plugin.getLogger().warning("\u26a0\ufe0f Could not detect public IP. Using localhost. Set public-ip in config if external players can't connect.");
        }
    }

    public String getPackUrl() {
        if (this.plugin.getConfigManager().getResourcePackMode().equalsIgnoreCase("INTERNAL")) {
            return "http://" + this.publicIp + ":" + this.plugin.getConfigManager().getResourcePackPort() + "/strengthsmp.zip?v=" + this.getPackHash();
        }
        return this.plugin.getConfigManager().getResourcePackUrl();
    }

    private class FileHandler
    implements HttpHandler {
        private FileHandler() {
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!ResourcePackServer.this.packFile.exists()) {
                exchange.sendResponseHeaders(404, -1L);
                return;
            }
            byte[] bytes = Files.readAllBytes(ResourcePackServer.this.packFile.toPath());
            String requestMethod = exchange.getRequestMethod();
            if (requestMethod.equalsIgnoreCase("HEAD")) {
                exchange.getResponseHeaders().set("Content-Length", String.valueOf(bytes.length));
                exchange.sendResponseHeaders(200, -1L);
                exchange.close();
                return;
            }
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody();){
                os.write(bytes);
            }
        }
    }
}

