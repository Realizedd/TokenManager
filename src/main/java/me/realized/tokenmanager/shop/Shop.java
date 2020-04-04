package me.realized.tokenmanager.shop;

import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import me.realized.tokenmanager.util.Placeholders;
import me.realized.tokenmanager.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Shop {

    @Getter
    private final String name;
    private final String title;
    @Getter(value = AccessLevel.PACKAGE)
    private final Inventory inventory;
    @Getter
    private final boolean autoClose;
    @Getter
    private final boolean usePermission;
    @Getter
    private final boolean confirmPurchase;

    private Map<Integer, Slot> slots;

    public Shop(final String name, final String title, final int rows, final boolean autoClose, final boolean usePermission, final boolean confirmPurchase)
        throws IllegalArgumentException {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name is null or empty.");
        }

        if (name.contains("-")) {
            throw new IllegalArgumentException("Shop name cannot contain a dash. (This is implemented to prevent errors with shop slot permissions)");
        }

        this.name = name;

        if (title.length() > 32) {
            throw new IllegalArgumentException("Shop title cannot be longer than 32 characters.");
        }

        if (rows <= 0 || rows > 6) {
            throw new IllegalArgumentException("Shop rows must be in between 1 - 6.");
        }

        this.title = StringUtil.color(title);
        this.inventory = Bukkit.createInventory(null, rows * 9, title);
        this.autoClose = autoClose;
        this.usePermission = usePermission;
        this.confirmPurchase = confirmPurchase;
    }

    public String getTitle() {
        return title;
    }

    public int getSize() {
        return inventory.getSize();
    }

    public void setSlot(final int slot, final ItemStack displayed, final Slot data) {
        Placeholders.replace(displayed, data.getCost(), "price");
        inventory.setItem(slot, displayed);

        if (slots == null) {
            slots = new HashMap<>();
        }

        slots.put(slot, data);
    }

    public Slot getSlot(final int slot) {
        return slots != null ? slots.get(slot) : null;
    }
}
