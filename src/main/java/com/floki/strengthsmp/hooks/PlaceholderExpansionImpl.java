package com.floki.strengthsmp.hooks;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.DataManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * PlaceholderAPI integration for StrengthSMP.
 * Provides placeholders like %strengthsmp_strength%, %strengthsmp_bounty%, etc.
 */
public class PlaceholderExpansionImpl extends PlaceholderExpansion {

    private final StrengthSMP plugin;
    private final DataManager dataManager;

    public PlaceholderExpansionImpl(StrengthSMP plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "strengthsmp";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Floki";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // Very important for PAPI
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";
        UUID uuid = player.getUniqueId();

        switch (params.toLowerCase()) {
            case "strength":
                return String.valueOf(dataManager.getStrength(uuid));
            case "max_strength":
                return String.valueOf(plugin.getConfigManager().getMaxStrength());
            case "bounty":
                return String.valueOf(dataManager.getBounty(uuid));
            case "monarch":
            case "monarch_name":
                return dataManager.getTopPlayerName();
            case "season_day":
                return String.valueOf(plugin.getSeasonService().getSeasonDay());
            case "contract_progress":
                return String.valueOf(plugin.getContractService().getTotalProgress(uuid));
            case "contract_goal":
                return String.valueOf(plugin.getContractService().getTotalGoal(uuid));
            case "contract_percentage":
                int progress = plugin.getContractService().getTotalProgress(uuid);
                int goal = plugin.getContractService().getTotalGoal(uuid);
                if (goal == 0) return "100%";
                return (int)((double)progress / goal * 100) + "%";
            case "combat_status":
                return plugin.getCombatListener().isInCombat(uuid) ? "&cIn Combat" : "&aSafe";
            case "weapon_class":
                return dataManager.getWeaponType(uuid).getDisplayName();
            case "kill_history_count":
                return String.valueOf(dataManager.getKillHistoryCount(uuid));
            default:
                return null;
        }
    }
}
