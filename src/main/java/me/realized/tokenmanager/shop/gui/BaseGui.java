package me.realized.tokenmanager.shop.gui;

import lombok.Getter;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.shop.Shop;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public abstract class BaseGui {

    protected final TokenManagerPlugin plugin;

    @Getter
    protected final Shop shop;
    protected final Inventory inventory;

    protected BaseGui(final TokenManagerPlugin plugin, final Shop shop, final Inventory inventory) {
        this.plugin = plugin;
        this.shop = shop;
        this.inventory = inventory;
    }

    public boolean isGui(final Inventory inventory) {
        return this.inventory.equals(inventory);
    }

    public void open(final Player player) {
        player.openInventory(inventory);
    }

    /**
     * Updates gui items with the new token balance.
     *
     * @param balance New token balance to update.
     */
    public abstract void refresh(final long balance);

    /**
     * Handles inventory click for gui.
     *
     * @param player Player that clicked in the gui.
     * @param slot Slot that was clicked.
     * @return true if a purchase was made. false otherwise
     */
    public abstract boolean handle(final Player player, final int slot);
}
