package me.realized.tm.utilities;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TMShop {

    private final String name;
    private final Inventory shop;
    private final boolean autoClose;
    private final boolean permission;
    private Map<Integer, Integer> price = new HashMap<>();
    private Map<Integer, List<String>> commands = new HashMap<>();
    private Map<Integer, String> messages = new HashMap<>();
    private Map<Integer, String> subShops = new HashMap<>();

    public TMShop(String name, String title, int size, boolean autoClose, boolean permission) {
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

    public List<String> getCommands(int slot) {
        return commands.get(slot) != null ? commands.get(slot) : new ArrayList<String>();
    }

    public int getPrice(int slot) {
        return price.get(slot) != null ? price.get(slot) : 0;
    }

    public String getSubShop(int slot) {
        return subShops.get(slot);
    }

    public void setSubShop(int slot, String name) {
        subShops.put(slot, name);
    }

    public String getMessage(int slot) {
        return messages.get(slot);
    }

    public void setMessage(int slot, String message) {
        messages.put(slot, message);
    }

    public void setItem(int slot, ItemStack item, int price, List<String> cmds) {
        shop.setItem(slot, item);
        this.price.put(slot, price);
        this.commands.put(slot, cmds);
    }

    public boolean isAutoCloseEnabled() {
        return autoClose;
    }

    public boolean hasPermission() {
        return permission;
    }
}
