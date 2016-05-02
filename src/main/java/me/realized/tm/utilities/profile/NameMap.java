package me.realized.tm.utilities.profile;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class NameMap {

    private static final Map<UUID, PlayerProfile> names = new ConcurrentHashMap<>();

    static void place(UUID uuid, String name) {
        names.put(uuid, new PlayerProfile(name));
    }

    static PlayerProfile get(UUID uuid) {
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
