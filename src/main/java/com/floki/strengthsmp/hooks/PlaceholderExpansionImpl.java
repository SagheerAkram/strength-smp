/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  me.clip.placeholderapi.expansion.PlaceholderExpansion
 *  org.bukkit.OfflinePlayer
 *  org.jetbrains.annotations.NotNull
 */
package com.floki.strengthsmp.hooks;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.DataManager;
import java.util.UUID;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlaceholderExpansionImpl
extends PlaceholderExpansion {
    private final StrengthSMP plugin;
    private final DataManager dataManager;

    public PlaceholderExpansionImpl(StrengthSMP plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
    }

    @NotNull
    public String getIdentifier() {
        return "strengthsmp";
    }

    @NotNull
    public String getAuthor() {
        return "Floki";
    }

    @NotNull
    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    public boolean persist() {
        return true;
    }

    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        UUID uuid = player.getUniqueId();
        switch (params.toLowerCase()) {
            case "strength": {
                return String.valueOf(this.dataManager.getStrength(uuid));
            }
            case "strength_rank": {
                return this.plugin.getConfigManager().getRankNameForStrength(this.dataManager.getStrength(uuid));
            }
            case "max_strength": {
                return String.valueOf(this.plugin.getConfigManager().getMaxStrength());
            }
            case "bounty": {
                return String.valueOf(this.dataManager.getBounty(uuid));
            }
            case "monarch": 
            case "monarch_name": {
                return this.dataManager.getTopPlayerName();
            }
            case "season_day": {
                return String.valueOf(this.plugin.getSeasonService().getSeasonDay());
            }
            case "contract_progress": {
                return String.valueOf(this.plugin.getContractService().getTotalProgress(uuid));
            }
            case "contract_goal": {
                return String.valueOf(this.plugin.getContractService().getTotalGoal(uuid));
            }
            case "contract_percentage": {
                int progress = this.plugin.getContractService().getTotalProgress(uuid);
                int goal = this.plugin.getContractService().getTotalGoal(uuid);
                if (goal == 0) {
                    return "100%";
                }
                return (int)((double)progress / (double)goal * 100.0) + "%";
            }
            case "combat_status": {
                return this.plugin.getCombatListener().isInCombat(uuid) ? "&cIn Combat" : "&aSafe";
            }
            case "weapon_class": {
                return this.dataManager.getWeaponType(uuid).getDisplayName();
            }
            case "has_bound_weapon": {
                return this.dataManager.hasBoundWeapon(uuid) ? "Yes" : "No";
            }
            case "bound_weapon_class": {
                return this.dataManager.hasBoundWeapon(uuid) ? this.dataManager.getWeaponType(uuid).getDisplayName() : "None";
            }
            case "kill_history_count": {
                return String.valueOf(this.dataManager.getKillHistoryCount(uuid));
            }
        }
        return null;
    }
}

