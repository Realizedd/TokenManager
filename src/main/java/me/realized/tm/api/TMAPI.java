package me.realized.tm.api;

import me.realized.tm.Core;
import me.realized.tm.data.Action;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 *
 * A static API for TokenManager.
 *
 * @author Realized
 *
 */

public class TMAPI {

    private static final Core instance = Core.getInstance();

    /**
     * @param player - Player to add tokens.
     *
     * @param amount - Amount to add.
     */
    public static void addTokens(Player player, int amount) {
        addTokens(player.getUniqueId(), amount);
    }

    /**
     * @param uuid - UUID of the player to add tokens.
     *
     * @param amount - Amount to add.
     */
    public static void addTokens(UUID uuid, int amount) {
        instance.getDataManager().executeAction(Action.ADD, uuid, amount);
    }

    /**
     * @param player - Player to remove tokens.
     * *
     * @param amount - Amount to remove.
     */
    public static void removeTokens(Player player, int amount) {
        removeTokens(player.getUniqueId(), amount);
    }

    /**
     * @param uuid - UUID of the player to remove tokens.
     * *
     * @param amount - Amount to remove.
     */
    public static void removeTokens(UUID uuid, int amount) {
        instance.getDataManager().executeAction(Action.REMOVE, uuid, amount);
    }

    /**
     * @param player - Player to set tokens.
     * *
     * @param amount - Amount to set.
     */
    public static void setTokens(Player player, int amount) {
        setTokens(player.getUniqueId(), amount);
    }

    /**
     * @param uuid - UUID of the player to set tokens.
     *
     * @param amount - Amount to set.
     */
    public static void setTokens(UUID uuid, int amount) {
        instance.getDataManager().executeAction(Action.SET, uuid, amount);
    }

    /**
     * @param player - Player to get token balance.
     *
     * @return long - The player's token balance.
     */
    public static long getTokens(Player player) {
        return getTokens(player.getUniqueId());
    }

    /**
     * @param uuid - UUID of the player to get token balance.
     *
     * @return long - The UUID owner's token balance.
     */
    public static long getTokens(UUID uuid) {
        return (long) instance.getDataManager().executeAction(Action.BALANCE, uuid, 0);
    }
}
