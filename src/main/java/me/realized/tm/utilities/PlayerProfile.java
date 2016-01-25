package me.realized.tm.utilities;

import java.util.UUID;

public class PlayerProfile {

    private UUID uuid = null;
    private String name = null;
    private final long time;

    public PlayerProfile(UUID uuid) {
        this.uuid = uuid;
        this.time = System.currentTimeMillis();
    }

    public PlayerProfile(String name) {
        this.name = name;
        this.time = System.currentTimeMillis();
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public long getTime() {
        return time;
    }
}
