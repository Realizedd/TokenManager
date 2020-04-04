package me.realized.tokenmanager.event;

import java.util.UUID;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @deprecated as of v3.2.0. Use {@link me.realized.tokenmanager.api.event.TMTokenSendEvent} instead.
 */
@Deprecated
public class TokenReceiveEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final UUID receiver;
    private int amount;
    private boolean cancelled;

    @Deprecated
    public TokenReceiveEvent(final UUID receiver, final int amount) {
        this.receiver = receiver;
        this.amount = amount;
    }

    @Deprecated
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Deprecated
    public UUID getReceiver() {
        return receiver;
    }

    @Deprecated
    public int getAmount() {
        return amount;
    }

    @Deprecated
    public void setAmount(final int amount) {
        this.amount = amount;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
}
