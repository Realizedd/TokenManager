package me.realized.tm.utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UUIDMap {

    private static Map<String, PlayerProfile> uuids = new HashMap<>();

    protected static void place(String name, UUID uuid) {
        uuids.put(name, new PlayerProfile(System.currentTimeMillis(), uuid));
    }

    protected static PlayerProfile get(String name) {
        PlayerProfile profile = uuids.get(name);

        if (profile == null) {
            return null;
        }

        if (profile.getTime() + (1000 * 600) - System.currentTimeMillis() <= 0) {
            return null;
        }
        return uuids.get(name);
    }

    protected static class PlayerProfile {

        private final long time;
        private final UUID uuid;

        public PlayerProfile(long time, UUID uuid) {
            this.time = time;
            this.uuid = uuid;
        }

        public long getTime() {
            return time;
        }

        public UUID getUUID() {
            return uuid;
        }
    }
}
