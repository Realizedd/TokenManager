package me.realized.tm.listeners;

import me.realized.tm.Core;
import me.realized.tm.management.DataManager;
import me.realized.tm.management.ShopManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private ShopManager shopManager;
    private DataManager dataManager;

    public PlayerListener(Core c) {
        shopManager = c.getShopManager();
        dataManager = c.getDataManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        dataManager.loadData(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        dataManager.saveData(e.getPlayer().getUniqueId(), true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        shopManager.handleClick(e);
    }
}
