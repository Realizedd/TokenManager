package me.realized.tm.shop;

import java.util.List;

public class SlotData {

    private final int cost;
    private final List<String> commands;
    private final String message;
    private final String subshop;

    public SlotData(int cost, List<String> commands, String message, String subshop) {
        this.cost = cost;
        this.commands = commands;
        this.message = message;
        this.subshop = subshop;
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
}
