package me.realized.tm.utilities;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TMShop {

    private String name;
    private Inventory shop;
    private boolean autoClose;
    private boolean permission;
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
        return commands.containsKey(slot) ? commands.get(slot) : new ArrayList<String>();
    }

    public void setCommands(int slot, List<String> cmds) {
        commands.put(slot, cmds);
    }

    public int getPrice(int slot) {
        return price.containsKey(slot) ? price.get(slot) : 0;
    }

    public boolean hasPrice(int slot) {
        return price.containsKey(slot);
    }

    public void setPrice(int slot, int amount) {
        price.put(slot, amount);
    }

    public String getSubShop(int slot) {
        return subShops.get(slot);
    }

    public boolean hasSubShop(int slot) {
        return subShops.containsKey(slot);
    }

    public void setSubShop(int slot, String name) {
        subShops.put(slot, name);
    }

    public boolean hasMessage(int slot) {
        return messages.containsKey(slot);
    }

    public String getMessage(int slot) {
        return hasMessage(slot) ? messages.get(slot) : null;
    }

    public void setMessage(int slot, String message) {
        messages.put(slot, message);
    }

    public void setItem(int slot, ItemStack item, int price, List<String> cmds) {
        shop.setItem(slot, item);
        setPrice(slot, price);
        setCommands(slot, cmds);
    }

    public boolean isAutoCloseEnabled() {
        return autoClose;
    }

    public boolean hasPermission() {
        return permission;
    }
}
