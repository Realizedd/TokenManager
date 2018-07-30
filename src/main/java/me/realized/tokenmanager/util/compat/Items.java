package me.realized.tokenmanager.util.compat;

import me.realized.tokenmanager.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class Items {

    private static final String PANE = "STAINED_GLASS_PANE";

    public static final ItemStack RED_PANE;
    public static final ItemStack GRAY_PANE;
    public static final ItemStack GREEN_PANE;

    public static final ItemStack HEAD;

    static {
        RED_PANE = (CompatUtil.isPre1_13() ? ItemBuilder.of(PANE, 1, (short) 14) : ItemBuilder.of(Material.RED_STAINED_GLASS_PANE)).name(" ").build();
        GRAY_PANE = (CompatUtil.isPre1_13() ? ItemBuilder.of(PANE, 1, (short) 7) : ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE)).name(" ").build();
        GREEN_PANE = (CompatUtil.isPre1_13() ? ItemBuilder.of(PANE, 1, (short) 13) : ItemBuilder.of(Material.GREEN_STAINED_GLASS_PANE)).name(" ").build();
        HEAD = (CompatUtil.isPre1_13() ? ItemBuilder.of("SKULL_ITEM", 1, (short) 3) : ItemBuilder.of(Material.PLAYER_HEAD)).build();
    }

    public static boolean equals(final ItemStack item, final ItemStack other) {
        return item.getType() == other.getType() && item.getDurability() == other.getDurability();
    }

    private Items() {}
}
