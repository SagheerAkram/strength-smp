package com.floki.strengthsmp.services;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.DataManager;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * Manages daily contracts for players.
 * Every day, players get a set of tasks to complete for strength rewards.
 */
public class ContractService {

    private final StrengthSMP plugin;
    private final DataManager dataManager;
    private final Random random = new Random();

    public ContractService(StrengthSMP plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
    }

    /**
     * Checks if the player needs new daily contracts.
     * Contracts refresh once per day based on system time.
     */
    public void checkAndRefreshContracts(Player player) {
        UUID uuid = player.getUniqueId();
        long lastRefresh = dataManager.getLastContractRefresh(); // This should be per player if we want individual resets, but global is easier to manage.
        
        // Actually, let's make it per-player for better UX.
        // I need to check if the player's data has a last refresh timestamp.
        // For now, let's use a simple "date" comparison.
        
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        String lastDateStr = dataManager.getPlayerContractDate(uuid);
        
        if (lastDateStr == null || !lastDateStr.equals(today.toString())) {
            refreshPlayerContracts(player);
            dataManager.setPlayerContractDate(uuid, today.toString());
        }
    }

    private void refreshPlayerContracts(Player player) {
        int count = plugin.getInternalConfigManager().getConfig().getInt("system.contracts.count-per-day", 3);
        List<String> newContracts = getRandomContracts(count);
        
        dataManager.setPlayerContracts(player.getUniqueId(), newContracts);
        dataManager.resetContractProgress(player.getUniqueId());
        
        MessageUtil.send(player, "contract.refreshed");
    }

    public List<String> getRandomContracts(int count) {
        List<String> pool = plugin.getInternalConfigManager().getEvents().getStringList("events.daily-contracts.pool");
        if (pool.isEmpty()) {
            // Fallback default contracts if pool is empty
            return Arrays.asList("KILLS:3", "BOUNTY:1", "STRENGTH:2");
        }

        List<String> selected = new ArrayList<>();
        List<String> tempPool = new ArrayList<>(pool);
        Collections.shuffle(tempPool);

        for (int i = 0; i < Math.min(count, tempPool.size()); i++) {
            selected.add(tempPool.get(i));
        }
        return selected;
    }

    /**
     * Increments progress on a specific contract type.
     * @param type The type of contract (e.g. "KILLS", "BOUNTY")
     * @param amount The amount to add
     */
    public void progressContract(Player player, String type, int amount) {
        UUID uuid = player.getUniqueId();
        List<String> contracts = dataManager.getPlayerContracts(uuid);
        if (contracts == null || contracts.isEmpty()) return;

        Map<String, Integer> progress = dataManager.getContractProgress(uuid);
        
        for (String contract : contracts) {
            String[] split = contract.split(":");
            if (split.length != 2) continue;
            
            String cType = split[0];
            int goal = Integer.parseInt(split[1]);
            
            if (cType.equalsIgnoreCase(type)) {
                int current = progress.getOrDefault(contract, 0);
                if (current >= goal) continue; // Already done

                int next = current + amount;
                progress.put(contract, next);
                dataManager.updateContractProgress(uuid, contract, next);

                if (next >= goal) {
                    completeContract(player, contract);
                }
            }
        }
    }

    public void completeContract(Player player, String contract) {
        String[] split = contract.split(":");
        String typeDisplay = split[0].replace("_", " ").toLowerCase();
        
        MessageUtil.send(player, "contract.complete", "contract", typeDisplay);
        
        int reward = plugin.getInternalConfigManager().getConfig().getInt("system.contracts.reward-strength", 1);
        plugin.getStrengthService().addStrength(player, reward);
    }

    public int getTotalProgress(UUID uuid) {
        Map<String, Integer> progress = dataManager.getContractProgress(uuid);
        return progress.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int getTotalGoal(UUID uuid) {
        List<String> contracts = dataManager.getPlayerContracts(uuid);
        int total = 0;
        for (String contract : contracts) {
            String[] split = contract.split(":");
            if (split.length == 2) {
                try {
                    total += Integer.parseInt(split[1]);
                } catch (NumberFormatException ignored) {}
            }
        }
        return total;
    }

    public void cleanup() {
        // Placeholder for future cleanup logic if needed
    }
}
