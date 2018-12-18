package me.realized.tokenmanager.api.event;

import me.realized.tokenmanager.shop.Shop;
import me.realized.tokenmanager.shop.Slot;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class TMShopPurchaseEvent extends TMEvent {

    private static final HandlerList handlers = new HandlerList();

    private final Shop shop;
    private final Slot slot;

    public TMShopPurchaseEvent(final Player player, final long amount, final Shop shop, final Slot slot) {
        super(player, amount);
        this.shop = shop;
        this.slot = slot;
    }

    public Shop getShop() {
        return shop;
    }

    public Slot getSlot() {
        return slot;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
