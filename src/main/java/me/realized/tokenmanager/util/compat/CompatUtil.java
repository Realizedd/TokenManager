package me.realized.tokenmanager.util.compat;

import me.realized.tokenmanager.util.NumberUtil;
import org.bukkit.Bukkit;

public final class CompatUtil {

    private static final long SUB_VERSION;

    static {
        final String packageName = Bukkit.getServer().getBukkitVersion().split(".")[1];
        SUB_VERSION = NumberUtil.parseLong(packageName).orElse(0);
    }

    private CompatUtil() {}

    public static boolean isPre1_17() {
        return SUB_VERSION < 17;
    }

    public static boolean isPre1_14() {
        return SUB_VERSION < 14;
    }

    public static boolean isPre1_13() {
        return SUB_VERSION < 13;
    }

    public static boolean isPre1_12() {
        return SUB_VERSION < 12;
    }

    public static boolean isPre1_9() {
        return SUB_VERSION < 9;
    }
}
