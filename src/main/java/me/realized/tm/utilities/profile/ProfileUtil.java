package me.realized.tm.utilities.profile;

import me.realized.tm.Core;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ProfileUtil {

    @SuppressWarnings("deprecation")
    public static UUID getUniqueId(final String username) {
        if (Bukkit.getOnlineMode() || Bukkit.spigot().getConfig().getBoolean("settings.bungeecord")) {
            if (Bukkit.getPlayer(username) != null) {
                return Bukkit.getPlayer(username).getUniqueId();
            }

            OfflinePlayer player = Bukkit.getOfflinePlayer(username);

            if (player != null && player.hasPlayedBefore()) {
                return Bukkit.getOfflinePlayer(username).getUniqueId();
            }
        }

        PlayerProfile profile = UUIDMap.get(username);

        if (profile != null) {
            return profile.getUUID();
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<UUID> future = executor.submit(new Callable<UUID>() {
            @Override
            public UUID call() throws Exception {
                return UUIDFetcher.getUUIDOf(username);
            }
        });

        executor.shutdown();

        try {
            UUID uuid = future.get();

            if (uuid != null) {
                UUIDMap.place(username, uuid);
            }

            return uuid;
        } catch (Exception e) {
            Core.getInstance().warn("Failed to fetch UUID for " + username + ": " + e.getMessage());
            return null;
        }
    }

    public static List<String> getNames(List<UUID> uuids, boolean async) {
        List<String> result = new ArrayList<>();
        ExecutorService executor = null;

        if (async) {
            executor = Executors.newSingleThreadExecutor();
        }

        for (final UUID uuid : uuids) {
            if (uuid == null) {
                result.add(null);
                continue;
            }

            if (Bukkit.getOnlineMode() || Bukkit.spigot().getConfig().getBoolean("settings.bungeecord")) {
                if (Bukkit.getPlayer(uuid) != null) {
                    result.add(Bukkit.getPlayer(uuid).getName());
                    continue;
                }

                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

                if (player != null && player.hasPlayedBefore()) {
                    result.add(player.getName());
                    continue;
                }
            }

            PlayerProfile profile = NameMap.get(uuid);

            if (profile != null) {
                result.add(profile.getName());
                continue;
            }

            if (async) {
                Future<String> future = executor.submit(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return NameFetcher.getNameOf(uuid);
                    }
                });

                try {
                    String name = future.get();

                    if (name != null) {
                        NameMap.place(uuid, name);
                    }

                    result.add(name);
                } catch (Exception e) {
                    result.add(null);
                    Core.getInstance().warn("Failed to fetch username for " + uuid + ": " + e.getMessage());
                }
            }
        }

        if (executor != null && !executor.isTerminated()) {
            executor.shutdown();
        }

        return result;
    }
}
