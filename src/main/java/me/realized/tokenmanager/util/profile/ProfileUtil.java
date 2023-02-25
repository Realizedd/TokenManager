package me.realized.tokenmanager.util.profile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class ProfileUtil {

    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    public static boolean isOnlineMode() {
        if (Bukkit.getOnlineMode()) {
            return true;
        }

        try {
            Class<?> clazz = Class.forName("org.spigotmc.SpigotConfig");
            return (boolean) clazz.getField("bungee").get(null);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException ex) {
            return false;
        }
    }

    public static boolean isUUID(final String s) {
        return UUID_PATTERN.matcher(s).matches();
    }

    public static void getNames(final List<UUID> uuids, final Consumer<Map<UUID, String>> consumer) {
        NameFetcher.getNames(uuids, consumer);
    }

    public static void getUUID(final String name, final Consumer<String> consumer, final Consumer<String> errorHandler) {
        final Player player;

        if ((player = Bukkit.getPlayerExact(name)) != null) {
            consumer.accept(player.getUniqueId().toString());
            return;
        }

        try {
            consumer.accept(UUIDFetcher.getUUID(name));
        } catch (Exception ex) {
            errorHandler.accept(ex.getMessage());
        }
    }

    private ProfileUtil() {}
}
