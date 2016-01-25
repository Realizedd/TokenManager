package me.realized.tm.utilities;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NameMap {

    private static final Map<UUID, PlayerProfile> names = new ConcurrentHashMap<>();

    protected static void place(UUID uuid, String name) {
        names.put(uuid, new PlayerProfile(name));
    }

    protected static PlayerProfile get(UUID uuid) {
        PlayerProfile profile = names.get(uuid);

        if (profile == null) {
            return null;
        }

        if (profile.getTime() + (1000 * 600) - System.currentTimeMillis() <= 0) {
            return null;
        }

        return profile;
    }
}
