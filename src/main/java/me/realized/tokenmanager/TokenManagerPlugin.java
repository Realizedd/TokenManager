/*
 *
 *   This file is part of TokenManager, licensed under the MIT License.
 *
 *   Copyright (c) Realized
 *   Copyright (c) contributors
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *   SOFTWARE.
 *
 */

package me.realized.tokenmanager;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.Collectors;
import lombok.Getter;
import me.realized.tokenmanager.api.TokenManager;
import me.realized.tokenmanager.command.commands.TMCommand;
import me.realized.tokenmanager.command.commands.TokenCommand;
import me.realized.tokenmanager.config.Config;
import me.realized.tokenmanager.config.Lang;
import me.realized.tokenmanager.data.DataManager;
import me.realized.tokenmanager.hooks.HookManager;
import me.realized.tokenmanager.shop.Shop;
import me.realized.tokenmanager.shop.ShopListener;
import me.realized.tokenmanager.shop.ShopsConfig;
import me.realized.tokenmanager.util.Loadable;
import me.realized.tokenmanager.util.Log;
import me.realized.tokenmanager.util.Reloadable;
import me.realized.tokenmanager.util.StringUtil;
import org.bstats.bukkit.Metrics;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.update.spiget.SpigetUpdate;
import org.inventivetalent.update.spiget.UpdateCallback;
import org.inventivetalent.update.spiget.comparator.VersionComparator;

public class TokenManagerPlugin extends JavaPlugin implements TokenManager, Listener {

    private static final int RESOURCE_ID = 8610;
    private static final String ADMIN_UPDATE_MESSAGE = "&9[TM] &bThere is an update available for TokenManager. Download at &7%s";

    @Getter
    private static TokenManagerPlugin instance;

    private final List<Loadable> loadables = new ArrayList<>();
    private int lastLoad;

    @Getter
    private Config configuration;
    @Getter
    private Lang lang;
    @Getter
    private ShopsConfig shopConfig;
    @Getter
    private DataManager dataManager;

    private volatile boolean updateAvailable;
    private volatile String downloadLink;

    @Override
    public void onEnable() {
        instance = this;
        Log.setSource(this);
        loadables.add(configuration = new Config(this));
        loadables.add(lang = new Lang(this));
        loadables.add(shopConfig = new ShopsConfig(this));
        loadables.add(dataManager = new DataManager(this));
        loadables.add(new ShopListener(this));
        loadables.add(new HookManager(this));

        if (!load()) {
            getPluginLoader().disablePlugin(this);
            return;
        }

        new TMCommand(this).register();
        new TokenCommand(this).register();

        new Metrics(this);

        if (!configuration.isCheckForUpdates()) {
            return;
        }

        final SpigetUpdate updateChecker = new SpigetUpdate(this, RESOURCE_ID);
        updateChecker.setVersionComparator(VersionComparator.SEM_VER_SNAPSHOT);
        updateChecker.checkForUpdate(new UpdateCallback() {
            @Override
            public void updateAvailable(final String newVersion, final String downloadUrl, final boolean hasDirectDownload) {
                updateAvailable = true;
                downloadLink = downloadUrl;
                Log.info("===============================================");
                Log.info("An update for " + getName() + " is available!");
                Log.info("Download " + getName() + " v" + newVersion + " here:");
                Log.info(downloadUrl);
                Log.info("===============================================");
            }

            @Override
            public void upToDate() {
                Log.info("No updates were available. You are on the latest version!");
            }
        });
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        instance = null;
        unload();
        Log.setSource(null);
    }

    /**
     * @return true if load was successful, otherwise false
     */
    private boolean load() {
        for (final Loadable loadable : loadables) {
            try {
                loadable.handleLoad();
                lastLoad = loadables.indexOf(loadable);
                Log.info("Loaded " + loadable.getClass().getSimpleName() + ".");
            } catch (Exception ex) {
                Log.error("There was an error while loading " + loadable.getClass().getSimpleName()
                    + "! If you believe this is an issue from the plugin, please contact the developer.");
                Log.error("Cause of error: " + ex.getMessage());
                ex.printStackTrace();
                return false;
            }
        }

        return true;
    }

    /**
     * @return true if unload was successful, otherwise false
     */
    private boolean unload() {
        for (final Loadable loadable : Lists.reverse(loadables)) {
            try {
                if (loadables.indexOf(loadable) > lastLoad) {
                    continue;
                }

                loadable.handleUnload();
                Log.info("Unloaded " + loadable.getClass().getSimpleName() + ".");
            } catch (Exception ex) {
                Log.error("There was an error while unloading " + loadable.getClass().getSimpleName()
                    + "! If you believe this is an issue from the plugin, please contact the developer.");
                Log.error("Cause of error: " + ex.getMessage());
                ex.printStackTrace();
                return false;
            }
        }

        return true;
    }

    public void doSync(final Runnable runnable) {
        getServer().getScheduler().runTask(this, runnable);
    }

    public void doSyncAfter(final Runnable runnable, final long delay) {
        getServer().getScheduler().runTaskLater(this, runnable, delay);
    }

    public int doSyncRepeat(final Runnable runnable, final long delay, final long period) {
        return getServer().getScheduler().runTaskTimer(this, runnable, delay, period).getTaskId();
    }

    public void doAsync(final Runnable runnable) {
        getServer().getScheduler().runTaskAsynchronously(this, runnable);
    }

    @Override
    public Optional<Shop> getShop(final String name) {
        return shopConfig.getShop(name);
    }

    @Override
    public Optional<Shop> getShop(final Inventory inventory) {
        return shopConfig.getShops().stream().filter(shop -> shop.getGui().equals(inventory)).findFirst();
    }

    @Override
    public OptionalLong getTokens(final Player player) {
        return dataManager.get(player);
    }

    @Override
    public void setTokens(final Player player, final long amount) {
        dataManager.set(player, amount);
    }

    @Override
    public void setTokens(final String key, final long amount) {
        dataManager.set(key, true, true, amount, amount, null, Log::error);
    }

    @Override
    public void addTokens(final String key, final long amount, final boolean silent) {
        dataManager.get(key, balance -> {
            if (!balance.isPresent()) {
                return;
            }

            dataManager.set(key, silent, false, amount, balance.getAsLong() + amount, null, Log::error);
        }, Log::error);
    }

    @Override
    public void addTokens(final String key, final long amount) {
        addTokens(key, amount, false);
    }

    @Override
    public void removeTokens(final String key, final long amount, final boolean silent) {
        dataManager.get(key, balance -> {
            if (!balance.isPresent()) {
                return;
            }

            dataManager.set(key, silent, false, -amount, balance.getAsLong() - amount, null, Log::error);
        }, Log::error);
    }

    @Override
    public void removeTokens(final String key, final long amount) {
        removeTokens(key, amount, false);
    }

    @Override
    public boolean reload() {
        if (!(unload() && load())) {
            getPluginLoader().disablePlugin(this);
            return false;
        }

        return true;
    }

    public boolean reload(final Loadable loadable) {
        final String name = loadable.getClass().getSimpleName();
        boolean unloaded = false;
        try {
            loadable.handleUnload();
            unloaded = true;
            Log.info("UnLoaded " + name + ".");
            loadable.handleLoad();
            Log.info("Loaded " + name + ".");
            return true;
        } catch (Exception ex) {
            Log.error("There was an error while " + (unloaded ? "loading " : "unloading ") + name
                + "! If you believe this is an issue from the plugin, please contact the developer.");
            Log.error("Cause of error: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public Optional<Loadable> find(final String name) {
        return loadables.stream().filter(loadable -> loadable.getClass().getSimpleName().equalsIgnoreCase(name)).findFirst();
    }

    public List<String> getReloadables() {
        return loadables.stream()
            .filter(loadable -> loadable instanceof Reloadable)
            .map(loadable -> loadable.getClass().getSimpleName())
            .collect(Collectors.toList());
    }

    @EventHandler
    public void on(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        if (updateAvailable && (player.isOp() || player.hasPermission("tokenmanager.admin"))) {
            player.sendMessage(StringUtil.color(String.format(ADMIN_UPDATE_MESSAGE, downloadLink)));
        }
    }
}
