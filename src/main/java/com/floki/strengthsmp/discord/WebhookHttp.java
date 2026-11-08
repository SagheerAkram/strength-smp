package com.floki.strengthsmp.discord;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * A tiny HTTPS client for Discord webhooks that runs on Java 8 through 25.
 * Java 8's HttpURLConnection cannot send PATCH (needed to edit the leaderboard message
 * in place), and java.net.http only exists from Java 11, so we speak HTTP/1.1 directly.
 */
final class WebhookHttp {
    private WebhookHttp() {
    }

    static final class Response {
        final int status;
        final String body;

        Response(int status, String body) {
            this.status = status;
            this.body = body;
        }
    }

    static Response send(String method, String url, String jsonBody) throws IOException {
        URI uri = URI.create(url);
        String host = uri.getHost();
        int port = uri.getPort() > 0 ? uri.getPort() : 443;
        String path = (uri.getRawPath() == null || uri.getRawPath().isEmpty() ? "/" : uri.getRawPath())
                + (uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "");
        byte[] body = jsonBody == null ? new byte[0] : jsonBody.getBytes(StandardCharsets.UTF_8);
        try (SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(host, port)) {
            socket.setSoTimeout(10_000);
            socket.startHandshake();
            OutputStream out = socket.getOutputStream();
            String head = method + " " + path + " HTTP/1.1\r\n"
                    + "Host: " + host + "\r\n"
                    + "User-Agent: StrengthSMP\r\n"
                    + "Content-Type: application/json\r\n"
                    + "Content-Length: " + body.length + "\r\n"
                    + "Connection: close\r\n\r\n";
            out.write(head.getBytes(StandardCharsets.US_ASCII));
            out.write(body);
            out.flush();
            return read(new BufferedInputStream(socket.getInputStream()));
        }
    }

    private static Response read(InputStream in) throws IOException {
        String statusLine = readLine(in);
        int status = Integer.parseInt(statusLine.split(" ")[1]);
        boolean chunked = false;
        int length = -1;
        for (String line = readLine(in); !line.isEmpty(); line = readLine(in)) {
            String lower = line.toLowerCase(Locale.ROOT);
            if (lower.startsWith("transfer-encoding:") && lower.contains("chunked")) {
                chunked = true;
            } else if (lower.startsWith("content-length:")) {
                length = Integer.parseInt(line.substring(15).trim());
            }
        }
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        if (chunked) {
            for (int size = Integer.parseInt(readLine(in).split(";")[0].trim(), 16); size > 0;
                 size = Integer.parseInt(readLine(in).split(";")[0].trim(), 16)) {
                copy(in, body, size);
                readLine(in); // CRLF after each chunk
            }
        } else if (length >= 0) {
            copy(in, body, length);
        } else {
            byte[] buf = new byte[4096];
            for (int n; (n = in.read(buf)) > 0; ) {
                body.write(buf, 0, n);
            }
        }
        return new Response(status, new String(body.toByteArray(), StandardCharsets.UTF_8));
    }

    private static void copy(InputStream in, ByteArrayOutputStream out, int n) throws IOException {
        byte[] buf = new byte[4096];
        while (n > 0) {
            int r = in.read(buf, 0, Math.min(buf.length, n));
            if (r < 0) {
                throw new IOException("Connection closed mid-response");
            }
            out.write(buf, 0, r);
            n -= r;
        }
    }

    private static String readLine(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int c; (c = in.read()) >= 0; ) {
            if (c == '\n') {
                break;
            }
            if (c != '\r') {
                sb.append((char) c);
            }
        }
        return sb.toString();
    }
}
