package me.realized.tm.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class TokenReceiveEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final UUID receiver;
    private int amount;
    private boolean cancelled;

    public TokenReceiveEvent(UUID receiver, int amount) {
        this.receiver = receiver;
        this.amount = amount;
    }

    public UUID getReceiver() {
        return receiver;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
