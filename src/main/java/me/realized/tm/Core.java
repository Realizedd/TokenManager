package me.realized.tm;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import me.realized.tm.commands.TMCommand;
import me.realized.tm.commands.TokenCommand;
import me.realized.tm.configuration.TMConfig;
import me.realized.tm.listeners.PlayerListener;
import me.realized.tm.management.DataManager;
import me.realized.tm.management.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Core extends JavaPlugin {

    private static Core instance = null;
    private static final Logger logger = Bukkit.getLogger();

    private TMConfig config;
    private ShopManager shopManager = null;
    private DataManager dataManager;

    @Override
    public void onEnable() {
        instance = this;

        config = new TMConfig(this);
        config.load();

        dataManager = new DataManager(this);

        if (!dataManager.load()) {
            instance.info("DataManager has failed to load, disabling.");
            getPluginLoader().disablePlugin(this);
            return;
        }

        dataManager.loadTopBalances();
        dataManager.initializeAutoSave();
        dataManager.checkConnection();

        shopManager = new ShopManager(this);
        shopManager.load();

        getCommand("token").setExecutor(new TokenCommand(this));
        getCommand("tm").setExecutor(new TMCommand(this));

        PluginManager manager = Bukkit.getPluginManager();

        manager.registerEvents(new PlayerListener(this), this);

        if (manager.isPluginEnabled("MVdWPlaceholderAPI")) {
            PlaceholderAPI.registerPlaceholder(this, "tm_tokens", new PlaceholderReplacer() {
                @Override
                public String onPlaceholderReplace(PlaceholderReplaceEvent placeholderReplaceEvent) {
                    Player player = placeholderReplaceEvent.getPlayer();

                    if (player == null) {
                        return "Player is required.";
                    }

                    return String.valueOf(dataManager.balance(player.getUniqueId()));
                }
            });
        }
    }

    @Override
    public void onDisable() {
        if (shopManager == null) {
            return;
        }

        shopManager.close();
        dataManager.close();
    }

    public void warn(String message) {
        logger.warning("[TokenManager] " + message);
    }

    public void info(String message) {
        logger.info("[TokenManager] " + message);
    }

    public TMConfig getTMConfig() {
        return config;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public static Core getInstance() {
        return instance;
    }
}
