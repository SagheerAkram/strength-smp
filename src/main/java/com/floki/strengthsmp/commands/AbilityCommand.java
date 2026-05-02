package com.floki.strengthsmp.commands;

import com.floki.strengthsmp.StrengthSMP;
import com.floki.strengthsmp.data.DataManager;
import com.floki.strengthsmp.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;

public class AbilityCommand extends BaseCommand {
    private final DataManager dataManager;

    public AbilityCommand(StrengthSMP plugin) {
        super(plugin);
        this.dataManager = plugin.getDataManager();
        plugin.getCommand("ability").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (!isPlayer(sender)) return true;
        Player player = (Player) sender;
        
        long lastUse = dataManager.getAbilityCooldown(player.getUniqueId());
        long now = System.currentTimeMillis();
        long cooldownMs = plugin.getConfigManager().getCooldown("ultimate") * 1000L;

        if (now - lastUse < cooldownMs) {
            long remaining = (cooldownMs - (now - lastUse)) / 1000;
            sendError(player, "Ability on cooldown: <#f1c40f>" + remaining + "s</#f1c40f>");
            return true;
        }

        com.floki.strengthsmp.data.WeaponType type = dataManager.getWeapon(player.getUniqueId());
        boolean success = false;

        switch (type) {
            case SHIELD:
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 1));
                sendMessage(player, "<#3498db><b>SHIELD</b></#3498db> <gray>Active: Resistance II (5s)</gray>");
                success = true;
                break;
            case SWORD:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 300, 2));
                player.setMetadata("berserk", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
                // Auto-remove metadata after 15 seconds (300 ticks)
                org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> player.removeMetadata("berserk", plugin), 300L);
                
                sendMessage(player, "<#f1c40f><b>SWORD ULTIMATE</b></#f1c40f> <gray>Gained</gray> <b>BERSERK</b> <gray>mode! (+2 Attack Damage)</gray>");
                success = true;
                break;
            default:
                sendMessage(player, "<#95a5a6>Ultimate activation for " + type.getDisplayName() + " is currently</#95a5a6> <b>PASSIVE</b> <gray>or</gray> <b>AUTOMATIC</b>.");
                break;
        }

        if (success) {
            dataManager.setAbilityCooldown(player.getUniqueId(), now);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }

        return true;
    }
}
