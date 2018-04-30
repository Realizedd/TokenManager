package me.realized.tm.api;

import java.util.UUID;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.api.TokenManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 *
 * A static API for TokenManager.
 *
 * @deprecated As of v3.0.0, replaced by {@link TokenManager}
 *
 */

@Deprecated
public class TMAPI {

    private static TokenManager getApi() {
        return TokenManagerPlugin.getInstance();
    }

    /**
     * @deprecated As of v3.0.0, use {@link TokenManager#setTokens(Player, long)} instead.
     */
    @Deprecated
    public static void addTokens(Player player, int amount) {
        setTokens(player, (int) (getTokens(player) + amount));
    }

    /**
     * @deprecated As of v3.0.0, use {@link TokenManager#setTokens(Player, long)} instead.
     */
    @Deprecated
    public static void addTokens(UUID uuid, int amount) {
        setTokens(uuid, (int) (getTokens(uuid) + amount));
    }

    /**
     * @deprecated As of v3.0.0, use {@link TokenManager#setTokens(Player, long)} instead.
     */
    @Deprecated
    public static void removeTokens(Player player, int amount) {
        setTokens(player, (int) (getTokens(player) - amount));
    }

    /**
     * @deprecated As of v3.0.0, use {@link TokenManager#setTokens(Player, long)} instead.
     */
    @Deprecated
    public static void removeTokens(UUID uuid, int amount) {
        setTokens(uuid, (int) (getTokens(uuid) - amount));
    }

    /**
     * @deprecated As of v3.0.0, use {@link TokenManager#setTokens(Player, long)} instead.
     */
    @Deprecated
    public static void setTokens(Player player, int amount) {
        getApi().setTokens(player, amount);
    }

    /**
     * @deprecated As of v3.0.0, use {@link TokenManager#setTokens(Player, long)} instead.
     */
    @Deprecated
    public static void setTokens(UUID uuid, int amount) {
        final Player player = Bukkit.getPlayer(uuid);

        if (player == null) {
            return;
        }

        setTokens(player, amount);
    }

    /**
     * @deprecated As of v3.0.0, use {@link TokenManager#getTokens(Player)} instead.
     */
    @Deprecated
    public static long getTokens(Player player) {
        return getApi().getTokens(player).orElse(0);
    }

    /**
     * @deprecated As of v3.0.0, use {@link TokenManager#getTokens(Player)} instead.
     */
    @Deprecated
    public static long getTokens(UUID uuid) {
        final Player player = Bukkit.getPlayer(uuid);

        if (player == null) {
            return 0;
        }

        return getTokens(player);
    }
}