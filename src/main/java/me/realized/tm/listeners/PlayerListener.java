package me.realized.tm.listeners;

import me.realized.tm.Core;
import me.realized.tm.management.DataManager;
import me.realized.tm.management.ShopManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    private final ShopManager shopManager;
    private final DataManager dataManager;

    public PlayerListener(Core instance) {
        shopManager = instance.getShopManager();
        dataManager = instance.getDataManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        dataManager.generate(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        shopManager.handleClick(event);
    }
}
