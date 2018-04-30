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

package me.realized.tokenmanager.data;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.function.Consumer;
import lombok.Getter;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.data.database.Database;
import me.realized.tokenmanager.data.database.Database.TopElement;
import me.realized.tokenmanager.data.database.FileDatabase;
import me.realized.tokenmanager.data.database.MySQLDatabase;
import me.realized.tokenmanager.util.Loadable;
import me.realized.tokenmanager.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;

public class DataManager implements Loadable, Listener {

    // Time to wait before loading userdata. An attempt to solve synchronization issues
    private static final long LOGIN_WAIT_DURATION = 30L;

    private final TokenManagerPlugin plugin;

    private Database database;

    @Getter
    private List<TopElement> topCache = new ArrayList<>();
    private Integer task;
    private Integer updateInterval;
    private long lastUpdateMillis;

    public DataManager(final TokenManagerPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() throws Exception {
        this.database = plugin.getConfiguration().isMysqlEnabled() ? new MySQLDatabase(plugin) : new FileDatabase(plugin);
        database.setup();

        task = plugin.doSyncRepeat(() -> database.ordered(10, args -> plugin.doSync(() -> {
            lastUpdateMillis = System.currentTimeMillis();
            topCache = args;
        })), 0L, 20L * 60L * getUpdateInterval());

        for (final Player player : Bukkit.getOnlinePlayers()) {
            database.get(player, balance -> {
                if (!balance.isPresent()) {
                    return;
                }

                plugin.doSync(() -> {
                    if (!player.isOnline()) {
                        return;
                    }

                    database.set(player, balance.getAsLong());
                });
            }, error -> player.sendMessage(ChatColor.RED + "Failed to load your token balance: " + error));
        }
    }

    @Override
    public void handleUnload() throws Exception {
        if (task != null) {
            final BukkitScheduler scheduler = Bukkit.getScheduler();

            if (scheduler.isCurrentlyRunning(task) || scheduler.isQueued(task)) {
                scheduler.cancelTask(task);
            }
        }

        database.save();
        database = null;
    }

    public OptionalLong get(final Player player) {
        return database != null ? database.get(player) : OptionalLong.empty();
    }

    public void set(final Player player, final long amount) {
        if (database != null) {
            database.set(player, amount);
        }
    }

    public void get(final String key, final Consumer<OptionalLong> consumer, final Consumer<String> errorHandler) {
        if (database != null) {
            database.get(key, consumer, errorHandler, false);
        }
    }

    public void set(final String key, final boolean set, final long amount, final long updated, final Runnable action, final Consumer<String> errorHandler) {
        if (database != null) {
            database.set(key, set, amount, updated, action, errorHandler);
        }
    }

    public void transfer(final CommandSender sender, final Consumer<String> errorHandler) {
        if (database != null && database instanceof MySQLDatabase) {
            ((MySQLDatabase) database).transfer(sender, errorHandler);
        }
    }

    private int getUpdateInterval() {
        if (updateInterval != null) {
            return updateInterval;
        }

        return (updateInterval = plugin.getConfiguration().getBalanceTopUpdateInterval()) < 1 ? 1 : updateInterval;
    }

    public String getNextUpdate() {
        return StringUtil.format((lastUpdateMillis + 60000L * getUpdateInterval() - System.currentTimeMillis()) / 1000);
    }

    @EventHandler
    public void on(final PlayerJoinEvent event) {
        if (database == null) {
            return;
        }

        final Player player = event.getPlayer();

        plugin.doSyncAfter(() -> database.get(player, balance -> {
            if (!balance.isPresent()) {
                return;
            }

            plugin.doSync(() -> database.set(player, balance.getAsLong()));
        }, error -> player.sendMessage(ChatColor.RED + "Failed to load your token balance: " + error)), LOGIN_WAIT_DURATION);
    }

    @EventHandler
    public void on(final PlayerQuitEvent event) {
        if (database == null) {
            return;
        }

        database.save(event.getPlayer());
    }
}
