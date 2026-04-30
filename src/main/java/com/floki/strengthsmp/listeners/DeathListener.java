package com.floki.strengthsmp.listeners;

import com.floki.strengthsmp.StrengthSMP;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import org.bukkit.event.player.PlayerQuitEvent;
import com.floki.strengthsmp.util.MessageUtil;

/**
 * Handles player death, strength transfers, bounties.
 * and other combat-related death processing.
 *
 * FIXES:
 * - Anti-farm is now unidirectional (attacker→victim only). B can still kill A.
 * - Uses config values for window/max instead of hardcoded.
 * - Records kills/deaths via DataManager.
 * - Duplicate reward prevention via processed-death set.
 */
public class DeathListener implements Listener {

    private final StrengthSMP plugin;
    private final Set<UUID> processedDeaths = new HashSet<>();
    
    private static final long ANTI_FARM_COOLDOWN = 3600000L; // 1 Hour

    public DeathListener(StrengthSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.getDataManager().isSystemEnabled()) return;

        Player victim   = event.getEntity();
        Player attacker = victim.getKiller();
        UUID victimUUID = victim.getUniqueId();

        if (attacker != null && attacker.equals(victim)) return;

        // ── Prevent duplicate processing ────────────────────────────────────
        if (processedDeaths.contains(victimUUID)) return;
        processedDeaths.add(victimUUID);
        Bukkit.getScheduler().runTaskLater(plugin, () -> processedDeaths.remove(victimUUID), 1L);

        // ── Stats and Bounty (Always process) ───────────────────────────────
        plugin.getDataManager().addDeath(victimUUID);

        if (attacker != null) {
            handleDeathRewards(attacker, victim, false);
        } else if (plugin.getCombatListener().isInCombat(victimUUID)) {
            UUID lastAttackerUUID = plugin.getCombatListener().getLastAttacker(victimUUID);
            if (lastAttackerUUID != null) {
                org.bukkit.OfflinePlayer offAttacker = Bukkit.getOfflinePlayer(lastAttackerUUID);
                handleDeathRewards(offAttacker, victim, true);
            }
        }

        plugin.updateDisplay(victim);
        if (attacker != null) plugin.updateDisplay(attacker);
    }

    private void handleDeathRewards(org.bukkit.OfflinePlayer attacker, Player victim, boolean isCombatLog) {
        UUID aUid = attacker.getUniqueId();
        UUID vUid = victim.getUniqueId();

        // ── 1. Bounty Payout (MANDATORY - No Anti-Farm) ──────────────────────
        int bounty = plugin.getDataManager().getBounty(vUid);
        if (bounty > 0) {
            plugin.getDataManager().removeBounty(vUid);
            plugin.getDataManager().clearBountyGlowExpiration(vUid);
            plugin.getStrengthService().addStrength(attacker, bounty, victim.getLocation());
            
            if (attacker.isOnline()) {
                MessageUtil.broadcast("bounty.claim", 
                    "player", attacker.getName(), 
                    "target", victim.getName(), 
                    "amount", String.valueOf(bounty));
                plugin.getContractService().progressContract((Player) attacker, "BOUNTY", 1);
            } else {
                // If attacker is offline (combat log), still broadcast but use offline name
                MessageUtil.broadcast("bounty.claim", 
                    "player", attacker.getName() != null ? attacker.getName() : "Unknown", 
                    "target", victim.getName(), 
                    "amount", String.valueOf(bounty));
            }
        }

        // ── 2. Strength Transfer / Loss ─────────────────────────────────────
        int victimStrength = plugin.getDataManager().getStrength(vUid);
        boolean victimIsNewbie   = plugin.getDataManager().isNewbieProtected(vUid);
        boolean attackerIsNewbie = plugin.getDataManager().isNewbieProtected(aUid);

        if (victimStrength > plugin.getConfigManager().getMinStrength()) {

            // ── NEWBIE PROTECTION (VICTIM) ───────────────────────────────────
            // Victim is a new player — no STR loss, no drop, no 4h timer.
            // Kill still counts for attacker.
            if (victimIsNewbie) {
                if (attacker.isOnline()) {
                    MessageUtil.send((Player) attacker, "strength.newbie-protected");
                    plugin.getContractService().progressContract((Player) attacker, "KILLS", 1);
                }
                MessageUtil.send(victim, "strength.newbie-you-protected");
                plugin.getDataManager().addKill(aUid);
                plugin.getDataManager().recordKill(aUid, vUid);
                return; // No 4h protection granted — newbie protection covers them already
            }

            // ── DEATH PROTECTION CHECK ───────────────────────────────────────
            if (plugin.getDataManager().hasDeathProtection(vUid)) {
                // Victim is shielded — no STR loss, no drop, but kill is still counted
                if (attacker.isOnline()) {
                    MessageUtil.send((Player) attacker, "strength.protection-blocked");
                    plugin.getContractService().progressContract((Player) attacker, "KILLS", 1);
                }
                MessageUtil.send(victim, "strength.protected");
                plugin.getDataManager().addKill(aUid);
                plugin.getDataManager().recordKill(aUid, vUid);
                // addDeath already called in onPlayerDeath, do NOT call again
                return;
            }

            // Anti-Farm Check (Only for the normal loss/drop, not bounty)
            long lastKill = plugin.getDataManager().getLastKillTime(aUid, vUid);
            if (System.currentTimeMillis() - lastKill < ANTI_FARM_COOLDOWN) {
                if (attacker.isOnline()) {
                    MessageUtil.send((Player) attacker, "strength.anti-farm");
                }
            } else {
                // ── PUNISHMENT CHECK ─────────────────────────────────────────
                boolean isPunished = plugin.getDataManager().isPunished(vUid);
                int lossAmount = isPunished ? 2 : 1;
                int dropAmount = isPunished ? 2 : 1;

                // Victim loses strength (1 normally, 2 if punished)
                plugin.getStrengthService().removeStrength(victim, lossAmount);

                // ── Grant 4-hour death protection to victim ──────────────────
                if (!isPunished) {
                    long protectionExpiry = System.currentTimeMillis() + (4 * 60 * 60 * 1000L); // 4 hours
                    plugin.getDataManager().setDeathProtection(vUid, protectionExpiry);
                    MessageUtil.send(victim, "strength.protection-granted");
                } else {
                    MessageUtil.send(victim, "strength.punished-no-protection");
                }

                // ── 3. Strength Drop ─────────────────────────────────────────
                // Skip drop entirely if the ATTACKER is a newbie (they cannot gain from kills).
                // Skip drop if the ATTACKER has death protection active.
                // Skip drop if a bounty was claimed (no double-dip).
                boolean attackerHasProtection = plugin.getDataManager().hasDeathProtection(aUid);

                if (bounty <= 0) {
                    if (!attackerIsNewbie && !attackerHasProtection) {
                        for (int i = 0; i < dropAmount; i++) {
                            victim.getWorld().dropItemNaturally(victim.getLocation(),
                                com.floki.strengthsmp.util.ItemFactory.createStrengthItem(1));
                        }
                    } else if (attackerIsNewbie) {
                        // Newbie attacker — no drop, but tell them why
                        if (attacker.isOnline()) {
                            MessageUtil.send((Player) attacker, "strength.newbie-no-gain");
                        }
                    } else if (attackerHasProtection) {
                        // Protected attacker - no drop
                        if (attacker.isOnline()) {
                            MessageUtil.send((Player) attacker, "strength.protected-no-gain");
                        }
                    }

                    if (attacker.isOnline()) {
                        plugin.getContractService().progressContract((Player) attacker, "KILLS", 1);
                    }
                    plugin.getDataManager().addKill(aUid);
                    plugin.getDataManager().recordKill(aUid, vUid);
                } else {
                    // Bounty was claimed — skip extra drop
                    if (attacker.isOnline()) {
                        MessageUtil.send((Player) attacker, "bounty.claim-bonus-skip");
                    }
                }
            }
        }
    }

    // Removed onQuit for processedDeaths because it's already handled by a 1-tick delay scheduler
    // which is safer and prevents memory leak if quit happens in the same tick.
}
