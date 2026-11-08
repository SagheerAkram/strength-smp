/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package com.floki.strengthsmp.services;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.DataManager;
import com.floki.strengthsmp.util.MessageUtil;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ContractService {
    private final StrengthSMP plugin;
    private final DataManager dataManager;
    private final Random random = new Random();

    public ContractService(StrengthSMP plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
    }

    public void checkAndRefreshContracts(Player player) {
        UUID uuid = player.getUniqueId();
        long lastRefresh = this.dataManager.getLastContractRefresh();
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        String lastDateStr = this.dataManager.getPlayerContractDate(uuid);
        if (lastDateStr == null || !lastDateStr.equals(today.toString())) {
            this.refreshPlayerContracts(player);
            this.dataManager.setPlayerContractDate(uuid, today.toString());
        }
    }

    private void refreshPlayerContracts(Player player) {
        int count = this.plugin.getInternalConfigManager().getConfig().getInt("system.contracts.count-per-day", 3);
        List<String> newContracts = this.getRandomContracts(count);
        this.dataManager.setPlayerContracts(player.getUniqueId(), newContracts);
        this.dataManager.resetContractProgress(player.getUniqueId());
        MessageUtil.send((CommandSender)player, "contract.refreshed", new String[0]);
    }

    public List<String> getRandomContracts(int count) {
        List pool = this.plugin.getInternalConfigManager().getEvents().getStringList("events.daily-contracts.pool");
        if (pool.isEmpty()) {
            return Arrays.asList("KILLS:3", "BOUNTY:1", "STRENGTH:2");
        }
        ArrayList<String> selected = new ArrayList<String>();
        ArrayList tempPool = new ArrayList(pool);
        Collections.shuffle(tempPool);
        for (int i = 0; i < Math.min(count, tempPool.size()); ++i) {
            selected.add((String)tempPool.get(i));
        }
        return selected;
    }

    public void progressContract(Player player, String type, int amount) {
        UUID uuid = player.getUniqueId();
        List<String> contracts = this.dataManager.getPlayerContracts(uuid);
        if (contracts == null || contracts.isEmpty()) {
            return;
        }
        Map<String, Integer> progress = this.dataManager.getContractProgress(uuid);
        for (String contract : contracts) {
            int current;
            String[] split = contract.split(":");
            if (split.length != 2) continue;
            String cType = split[0];
            int goal = Integer.parseInt(split[1]);
            if (!cType.equalsIgnoreCase(type) || (current = progress.getOrDefault(contract, 0).intValue()) >= goal) continue;
            int next = current + amount;
            progress.put(contract, next);
            this.dataManager.updateContractProgress(uuid, contract, next);
            if (next < goal) continue;
            this.completeContract(player, contract);
        }
    }

    public void completeContract(Player player, String contract) {
        String[] split = contract.split(":");
        String typeDisplay = split[0].replace("_", " ").toLowerCase();
        MessageUtil.send((CommandSender)player, "contract.complete", "contract", typeDisplay);
        int reward = this.plugin.getInternalConfigManager().getConfig().getInt("system.contracts.reward-strength", 1);
        this.plugin.getStrengthService().addStrength(player, reward);
    }

    public int getTotalProgress(UUID uuid) {
        Map<String, Integer> progress = this.dataManager.getContractProgress(uuid);
        return progress.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int getTotalGoal(UUID uuid) {
        List<String> contracts = this.dataManager.getPlayerContracts(uuid);
        int total = 0;
        for (String contract : contracts) {
            String[] split = contract.split(":");
            if (split.length != 2) continue;
            try {
                total += Integer.parseInt(split[1]);
            }
            catch (NumberFormatException numberFormatException) {}
        }
        return total;
    }

    public void cleanup() {
    }
}

