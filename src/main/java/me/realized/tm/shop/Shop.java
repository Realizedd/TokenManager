package me.realized.tm.shop;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Shop {

    private final String name;
    private final Inventory shop;
    private final boolean autoClose;
    private final boolean permission;
    private final Map<Integer, SlotData> slots = new HashMap<>();

    public Shop(String name, String title, int size, boolean autoClose, boolean permission) {
        this.name = name;
        this.autoClose = autoClose;
        this.permission = permission;
        shop = Bukkit.createInventory(null, size, title);
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return shop.getTitle();
    }

    public Inventory get() {
        return shop;
    }

    public SlotData getSlot(int slot) {
        return slots.get(slot);
    }

    public void setItem(int slot, ItemStack displayed, int cost, List<String> commands, String message, String subshop, boolean permission) {
        shop.setItem(slot, displayed);
        slots.put(slot, new SlotData(cost, commands, message, subshop, permission));
    }

    public boolean isAutoCloseEnabled() {
        return autoClose;
    }

    public boolean hasPermission() {
        return permission;
    }
}
