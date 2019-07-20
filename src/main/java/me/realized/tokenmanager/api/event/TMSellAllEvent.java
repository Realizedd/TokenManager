package me.realized.tokenmanager.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class TMSellAllEvent extends TMEvent {

    private static final HandlerList handlers = new HandlerList();

    public TMSellAllEvent(final Player player, final long amount) {
        super(player, amount);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
