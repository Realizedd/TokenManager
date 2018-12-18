package me.realized.tokenmanager.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class TMTokenSendEvent extends TMEvent {

    private static final HandlerList handlers = new HandlerList();

    private final Player target;

    public TMTokenSendEvent(final Player player, final Player target, final long amount) {
        super(player, amount);
        this.target = target;
    }

    public Player getTarget() {
        return target;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
