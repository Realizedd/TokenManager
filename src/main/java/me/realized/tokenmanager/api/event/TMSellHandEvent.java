package me.realized.tokenmanager.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class TMSellHandEvent extends TMEvent {

    private static final HandlerList handlers = new HandlerList();

    private final ItemStack item;

    public TMSellHandEvent(final Player player, final long amount, final ItemStack item) {
        super(player, amount);
        this.item = item;
    }

    public ItemStack getItem() {
        return item;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
