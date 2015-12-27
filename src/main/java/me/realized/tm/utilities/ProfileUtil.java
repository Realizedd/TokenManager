package me.realized.tm.utilities;

import org.bukkit.Bukkit;

import java.util.UUID;

public class ProfileUtil {

    @SuppressWarnings("deprecation")
    public static UUID getUniqueId(String username) {
        if (Bukkit.getOnlineMode()) {
            if (Bukkit.getPlayerExact(username) != null) {
                return Bukkit.getPlayerExact(username).getUniqueId();
            }

            if (Bukkit.getOfflinePlayer(username).hasPlayedBefore()) {
                return Bukkit.getOfflinePlayer(username).getUniqueId();
            }
        }

        try {
            return UUIDFetcher.getUUIDOf(username);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getName(UUID uuid) {
        if (Bukkit.getOnlineMode()) {
            if (Bukkit.getPlayer(uuid) != null) {
                return Bukkit.getPlayer(uuid).getName();
            }

            if (Bukkit.getOfflinePlayer(uuid).hasPlayedBefore()) {
                return Bukkit.getOfflinePlayer(uuid).getName();
            }
        }

        try {
            return NameFetcher.getNameOf(uuid);
        } catch (Exception e) {
            return "failed to connect to mojang server";
        }
    }
}
