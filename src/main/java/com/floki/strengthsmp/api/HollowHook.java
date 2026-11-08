package com.floki.strengthsmp.api;

import java.util.List;
import java.util.UUID;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * What the core plugin needs from the Hollow. The Hollow lives only in StrengthSMP Pro;
 * the free edition ships without it, so the core talks to it through this interface.
 */
public interface HollowHook {
    boolean isHollowWorld(World world);

    boolean isPrisoner(UUID player);

    /** Called with the victim's strength from before this death was processed. */
    boolean shouldEliminate(Player victim, Player killer, int strengthBefore, boolean wasProtected);

    void eliminate(Player victim, Player killer);

    boolean sealsBoundWeapons();

    void sealIntoCustody(UUID player, List<ItemStack> items);

    long getWithdrawLockRemainingMs(UUID player);

    /** /strengthsmp hollow ... */
    void handleAdmin(CommandSender sender, String[] args);

    /** /hollow */
    void sendJournal(Player player);

    void registerListeners();

    void shutdown();
}
