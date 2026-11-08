/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 */
package com.floki.strengthsmp.services;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.DataManager;
import com.floki.strengthsmp.util.MessageUtil;
import java.lang.invoke.CallSite;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;

public class SeasonService {
    private final StrengthSMP plugin;
    private final DataManager dataManager;

    public SeasonService(StrengthSMP plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
    }

    public void endSeason() {
        int currentSeason = this.plugin.getInternalConfigManager().getEvents().getInt("seasons.current-season", 1);
        ArrayList<Map.Entry<UUID, Integer>> sorted = new ArrayList<Map.Entry<UUID, Integer>>(this.dataManager.getStrengthCache().entrySet());
        sorted.sort((a, b) -> ((Integer)b.getValue()).compareTo((Integer)a.getValue()));
        ArrayList<CallSite> hof = new ArrayList<CallSite>();
        for (int i = 0; i < Math.min(3, sorted.size()); ++i) {
            Map.Entry entry = (Map.Entry)sorted.get(i);
            String name = Bukkit.getOfflinePlayer((UUID)((UUID)entry.getKey())).getName();
            hof.add((CallSite)((Object)(i + 1 + ". " + (name != null ? name : "Unknown") + " (" + String.valueOf(entry.getValue()) + " Strength)")));
        }
        String path = "seasons.history.season-" + currentSeason;
        this.plugin.getInternalConfigManager().getEvents().set(path + ".top-players", hof);
        this.plugin.getInternalConfigManager().getEvents().set(path + ".end-timestamp", (Object)System.currentTimeMillis());
        MessageUtil.broadcast("season.ended", "season", String.valueOf(currentSeason));
        this.dataManager.resetAllStats();
        this.dataManager.setLastSeasonReset(System.currentTimeMillis());
        this.plugin.getInternalConfigManager().getEvents().set("seasons.current-season", (Object)(currentSeason + 1));
        this.plugin.getInternalConfigManager().saveEvents();
        MessageUtil.broadcast("season.started", "season", String.valueOf(currentSeason + 1));
    }

    public int getSeasonDay() {
        long reset = this.dataManager.getLastSeasonReset();
        if (reset == 0L) {
            return 1;
        }
        long diff = System.currentTimeMillis() - reset;
        return (int)(diff / 86400000L) + 1;
    }

    public double getStrengthMultiplier() {
        return this.plugin.getInternalConfigManager().getEvents().getDouble("seasons.modifiers.strength-gain-multiplier", 1.0);
    }
}

