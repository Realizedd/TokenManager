package me.realized.tokenmanager.util.compat;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;

public final class Terracottas {

    private static final Map<Short, String> DATA_TO_TERRACOTTA = new HashMap<>();

    static {
        DATA_TO_TERRACOTTA.put((short) 0, "WHITE_TERRACOTTA");
        DATA_TO_TERRACOTTA.put((short) 1, "ORANGE_TERRACOTTA");
        DATA_TO_TERRACOTTA.put((short) 2, "MAGENTA_TERRACOTTA");
        DATA_TO_TERRACOTTA.put((short) 3, "LIGHT_BLUE_TERRACOTTA");
        DATA_TO_TERRACOTTA.put((short) 4, "YELLOW_TERRACOTTA");
        DATA_TO_TERRACOTTA.put((short) 5, "LIME_TERRACOTTA");
        DATA_TO_TERRACOTTA.put((short) 6, "PINK_TERRACOTTA");
        DATA_TO_TERRACOTTA.put((short) 7, "GRAY_TERRACOTTA");
        DATA_TO_TERRACOTTA.put((short) 8, "LIGHT_GRAY_TERRACOTTA");
        DATA_TO_TERRACOTTA.put((short) 9, "CYAN_TERRACOTTA");
        DATA_TO_TERRACOTTA.put((short) 10, "PURPLE_TERRACOTTA");
        DATA_TO_TERRACOTTA.put((short) 11, "BLUE_TERRACOTTA");
        DATA_TO_TERRACOTTA.put((short) 12, "BROWN_TERRACOTTA");
        DATA_TO_TERRACOTTA.put((short) 13, "GREEN_TERRACOTTA");
        DATA_TO_TERRACOTTA.put((short) 14, "RED_TERRACOTTA");
        DATA_TO_TERRACOTTA.put((short) 15, "BLACK_TERRACOTTA");
    }

    public static Material from(final short data) {
        return Material.getMaterial(DATA_TO_TERRACOTTA.get(data));
    }

    private Terracottas() {}
}
