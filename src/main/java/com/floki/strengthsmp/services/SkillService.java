package com.floki.strengthsmp.services;

import com.floki.strengthsmp.StrengthSMP;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.UUID;

/**
 * Manages the persistent skills/passives from the Root Skill Tree.
 */
public class SkillService {

    private final StrengthSMP plugin;
    private BukkitTask refreshTask;

    public SkillService(StrengthSMP plugin) {
        this.plugin = plugin;
        startRefreshTask();
    }

    private void startRefreshTask() {
        // Every 5 seconds, ensure all online players have their skill effects
        this.refreshTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                applySkills(player);
            }
        }, 100L, 100L);
    }

    public void applySkills(Player player) {
        UUID uuid = player.getUniqueId();
        Set<String> skills = plugin.getDataManager().getSkills(uuid);

        if (skills.contains("tank")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 160, 0, false, false, true));
        }
        if (skills.contains("scout")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 160, 0, false, false, true));
        }
        if (skills.contains("berserker")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 160, 0, false, false, true));
        }
        
        // Vitality handled via Attribute elsewhere if possible, but let's do it here for now
        if (skills.contains("vitality")) {
            org.bukkit.attribute.AttributeInstance attr = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
            if (attr != null && attr.getBaseValue() < 24.0) {
                attr.setBaseValue(24.0);
            }
        } else {
            org.bukkit.attribute.AttributeInstance attr = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
            if (attr != null && attr.getBaseValue() > 20.0) {
                // Only reset if they don't have vitality (and aren't monarch etc, though monarch doesn't give health here)
                attr.setBaseValue(20.0);
            }
        }
    }

    public void cleanup() {
        if (refreshTask != null) refreshTask.cancel();
    }
}
