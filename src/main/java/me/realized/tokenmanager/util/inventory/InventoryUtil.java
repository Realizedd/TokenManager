package me.realized.tokenmanager.util.inventory;

import me.realized.tokenmanager.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public final class InventoryUtil {

    private InventoryUtil() {}

    public static Inventory deepCopyOf(final Inventory inventory, final String title) {
        final Inventory result = Bukkit.createInventory(null, inventory.getSize(), StringUtil.color(title));

        for (int i = 0; i < inventory.getSize(); i++) {
            result.setItem(i, inventory.getItem(i).clone());
        }

        return result;
    }

    public static int getEmptySlots(final Inventory inventory) {
        int empty = 0;

        for (int i = 0; i < 36; i++) {
            final ItemStack item = inventory.getItem(i);

            if (item == null) {
                empty++;
            }
        }

        return empty;
    }

    public static boolean isInventoryFull(final Player player) {
        return player.getInventory().firstEmpty() == -1;
    }

    public static Inventory getClickedInventory(final int rawSlot, final InventoryView view) {
        if (rawSlot < view.getTopInventory().getSize()) {
            return view.getTopInventory();
        } else {
            return view.getBottomInventory();
        }
    }
}
