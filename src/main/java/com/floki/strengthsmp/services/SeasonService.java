package com.floki.strengthsmp.services;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.DataManager;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages seasonal progression and resets.
 */
public class SeasonService {

    private final StrengthSMP plugin;
    private final DataManager dataManager;

    public SeasonService(StrengthSMP plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
    }

    /**
     * Ends the current season, archiving top players and resetting everyone's stats.
     */
    public void endSeason() {
        int currentSeason = plugin.getInternalConfigManager().getEvents().getInt("seasons.current-season", 1);
        
        // 1. Capture Hall of Fame (Top 3)
        List<Map.Entry<UUID, Integer>> sorted = new ArrayList<>(dataManager.getStrengthCache().entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        List<String> hof = new ArrayList<>();
        for (int i = 0; i < Math.min(3, sorted.size()); i++) {
            Map.Entry<UUID, Integer> entry = sorted.get(i);
            String name = Bukkit.getOfflinePlayer(entry.getKey()).getName();
            hof.add((i + 1) + ". " + (name != null ? name : "Unknown") + " (" + entry.getValue() + " Strength)");
        }
        
        // 2. Save to events.yml
        String path = "seasons.history.season-" + currentSeason;
        plugin.getInternalConfigManager().getEvents().set(path + ".top-players", hof);
        plugin.getInternalConfigManager().getEvents().set(path + ".end-timestamp", System.currentTimeMillis());
        
        // 3. Broadcast and Reset
        MessageUtil.broadcast("season.ended", "season", String.valueOf(currentSeason));
        
        dataManager.resetAllStats();
        dataManager.setLastSeasonReset(System.currentTimeMillis());
        
        // 4. Increment season
        plugin.getInternalConfigManager().getEvents().set("seasons.current-season", currentSeason + 1);
        plugin.getInternalConfigManager().saveEvents();
        
        MessageUtil.broadcast("season.started", "season", String.valueOf(currentSeason + 1));
    }

    public int getSeasonDay() {
        long reset = dataManager.getLastSeasonReset();
        if (reset == 0) return 1;
        
        long diff = System.currentTimeMillis() - reset;
        return (int) (diff / (1000 * 60 * 60 * 24)) + 1;
    }

    public double getStrengthMultiplier() {
        return plugin.getInternalConfigManager().getEvents().getDouble("seasons.modifiers.strength-gain-multiplier", 1.0);
    }
}
