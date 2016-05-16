package me.realized.tm.shop;

import java.util.List;

public class SlotData {

    private final int cost;
    private final List<String> commands;
    private final String message;
    private final String subshop;
    private boolean permission;

    public SlotData(int cost, List<String> commands, String message, String subshop, boolean permission) {
        this.cost = cost;
        this.commands = commands;
        this.message = message;
        this.subshop = subshop;
        this.permission = permission;
    }

    public int getCost() {
        return cost;
    }

    public List<String> getCommands() {
        return commands;
    }

    public String getMessage() {
        return message;
    }

    public String getSubShop() {
        return subshop;
    }

    public boolean hasMessage() {
        return message != null;
    }

    public boolean hasSubShop() {
        return subshop != null;
    }

    public boolean hasPermission() {
        return permission;
    }
}
