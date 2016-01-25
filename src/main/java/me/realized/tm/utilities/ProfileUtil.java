package me.realized.tm.utilities;

import me.realized.tm.Core;
import org.bukkit.Bukkit;

import java.util.UUID;

public class ProfileUtil {

    @SuppressWarnings("deprecation")
    public static UUID getUniqueId(String username) {
        if (Bukkit.getOnlineMode() || Bukkit.spigot().getConfig().getBoolean("settings.bungeecord")) {
            if (Bukkit.getPlayerExact(username) != null) {
                return Bukkit.getPlayerExact(username).getUniqueId();
            }

            if (Bukkit.getOfflinePlayer(username).hasPlayedBefore()) {
                return Bukkit.getOfflinePlayer(username).getUniqueId();
            }
        }

        PlayerProfile profile = UUIDMap.get(username);

        if (profile != null) {
            return profile.getUUID();
        }

        try {
            UUID uuid = UUIDFetcher.getUUIDOf(username);
            UUIDMap.place(username, uuid);
            return uuid;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static String getName(UUID uuid) {
        if (Bukkit.getOnlineMode() || Bukkit.spigot().getConfig().getBoolean("settings.bungeecord")) {
            if (Bukkit.getPlayer(uuid) != null) {
                return Bukkit.getPlayer(uuid).getName();
            }

            if (Bukkit.getOfflinePlayer(uuid).hasPlayedBefore()) {
                return Bukkit.getOfflinePlayer(uuid).getName();
            }
        }

        PlayerProfile profile = NameMap.get(uuid);

        if (profile != null) {
            return profile.getName();
        }

        try {
            String name = NameFetcher.getNameOf(uuid);
            NameMap.place(uuid, name);
            return name;
        } catch (Exception e) {
            Core.getInstance().warn("Failed to fetch username for " + uuid.toString() + ": " + e.getMessage());
            return null;
        }
    }
}
