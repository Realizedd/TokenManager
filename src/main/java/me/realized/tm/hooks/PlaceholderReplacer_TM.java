package me.realized.tm.hooks;

import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import me.realized.tm.Core;
import me.realized.tm.data.Action;
import me.realized.tm.data.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlaceholderReplacer_TM implements PlaceholderReplacer, Listener {

    private final DataManager manager;

    private Map<UUID, Request> lastRequest = new HashMap<>();

    public PlaceholderReplacer_TM(Core instance) {
        this.manager = instance.getDataManager();
        Bukkit.getPluginManager().registerEvents(this, instance);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (lastRequest.get(player.getUniqueId()) != null) {
            lastRequest.remove(player.getUniqueId());
        }
    }

    @Override
    public String onPlaceholderReplace(PlaceholderReplaceEvent event) {
        Player player = event.getPlayer();

        if (player == null) {
            return "Player is required.";
        }

        long now = System.currentTimeMillis();

        if (lastRequest.get(player.getUniqueId()) != null) {
            Request request = lastRequest.get(player.getUniqueId());

            if (request.getCreationMillis() + 20L * 1000L - now <= 0) {
                int balance = (int) manager.executeAction(Action.BALANCE, player.getUniqueId(), 0);
                lastRequest.put(player.getUniqueId(), new Request(now, balance));
                return String.valueOf(balance);
            } else {
                return String.valueOf(lastRequest.get(player.getUniqueId()).getBalance());
            }

        } else {
            int balance = (int) manager.executeAction(Action.BALANCE, player.getUniqueId(), 0);
            lastRequest.put(player.getUniqueId(), new Request(now, balance));
            return String.valueOf(balance);
        }
    }

    class Request {

        private final long creation;
        private final int balance;

        Request(long creation, int balance) {
            this.creation = creation;
            this.balance = balance;
        }

        public long getCreationMillis() {
            return creation;
        }

        public int getBalance() {
            return balance;
        }
    }
}
