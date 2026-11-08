package com.floki.strengthsmp.util;

/** Short human durations: "45s", "12m", "2h 5m". */
public final class TimeFormat {
    private TimeFormat() {
    }

    public static String duration(long ms) {
        if (ms < 60_000L) {
            return Math.max(0L, ms / 1000L) + "s";
        }
        long totalMinutes = ms / 60_000L;
        long h = totalMinutes / 60;
        long m = totalMinutes % 60;
        return h > 0 ? h + "h " + m + "m" : m + "m";
    }
}
