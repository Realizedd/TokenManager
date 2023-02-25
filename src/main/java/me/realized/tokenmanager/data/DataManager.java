package me.realized.tokenmanager.data;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.Getter;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.command.commands.subcommands.OfflineCommand.ModifyType;
import me.realized.tokenmanager.data.database.Database;
import me.realized.tokenmanager.data.database.Database.TopElement;
import me.realized.tokenmanager.data.database.FileDatabase;
import me.realized.tokenmanager.data.database.MySQLDatabase;
import me.realized.tokenmanager.util.Loadable;
import me.realized.tokenmanager.util.Log;
import me.realized.tokenmanager.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;

public class DataManager implements Loadable, Listener {

    private final TokenManagerPlugin plugin;

    private Database database;

    @Getter
    private List<TopElement> topCache = new ArrayList<>();
    private Integer topTask, updateInterval;
    private long lastUpdateMillis;

    private final Multimap<UUID, QueuedCommand> queuedCommands = LinkedHashMultimap.create();

    public DataManager(final TokenManagerPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() throws Exception {
        this.database = plugin.getConfiguration().isMysqlEnabled() ? new MySQLDatabase(plugin) : new FileDatabase(plugin);
        final boolean online = database.isOnlineMode();
        Log.info("===============================================");
        Log.info("TokenManager has detected your server as " + (online ? "online" : "offline") + " mode.");
        Log.info("DataManager will operate with " + (online ? "UUID" : "Username") + "s.");
        Log.info("If your server is NOT in " + (online ? "online" : "offline") + " mode, please manually set online-mode in TokenManager's config.yml.");
        Log.info("===============================================");
        database.setup();

        topTask = plugin.doSyncRepeat(() -> database.ordered(10, args -> plugin.doSync(() -> {
            lastUpdateMillis = System.currentTimeMillis();
            topCache = args;
        })), 0L, 20L * 60L * getUpdateInterval());

        Bukkit.getOnlinePlayers().forEach(player -> database.load(player));
    }

    @Override
    public void handleUnload() throws Exception {
        if (topTask != null) {
            final BukkitScheduler scheduler = Bukkit.getScheduler();

            if (scheduler.isCurrentlyRunning(topTask) || scheduler.isQueued(topTask)) {
                scheduler.cancelTask(topTask);
            }
        }

        database.shutdown();
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

    public void get(final String key, final Consumer<OptionalLong> onLoad, final Consumer<String> onError) {
        if (database != null) {
            database.get(key, onLoad, onError, false);
        }
    }

    public void set(final String key, final ModifyType type, final long amount, final long balance, final boolean silent, final Runnable onDone,
        final Consumer<String> onError) {
        if (database != null) {
            database.set(key, type, amount, balance, silent, onDone, onError);
        }
    }

    public void transfer(final CommandSender sender, final Consumer<String> onError) {
        if (database != null) {
            database.transfer(sender, onError);
        }
    }

    public void queueCommand(final Player player, final ModifyType type, final long amount, final boolean silent) {
        queuedCommands.put(player.getUniqueId(), new QueuedCommand(type, amount, silent));
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
        final Player player = event.getPlayer();

        if (database == null) {
            return;
        }

        database.load(player, balance -> {
            final Collection<QueuedCommand> commands = queuedCommands.asMap().remove(player.getUniqueId());

            if (commands == null) {
                return balance;
            }

            long total = balance;

            for (final QueuedCommand command : commands) {
                final ModifyType type = command.type;
                final long amount = command.amount;
                total = type.apply(total, amount);

                if (!command.silent) {
                    plugin.getLang().sendMessage(player, true, "COMMAND." + (type == ModifyType.ADD ? "add" : "remove"), "amount", amount);
                }
            }

            return total;
        });
    }

    @EventHandler
    public void on(final PlayerQuitEvent event) {
        if (database == null) {
            return;
        }

        final Player player = event.getPlayer();
        queuedCommands.asMap().remove(player.getUniqueId());
        database.save(player);
    }

    private class QueuedCommand {

        private final ModifyType type;
        private final long amount;
        private final boolean silent;

        QueuedCommand(final ModifyType type, final long amount, final boolean silent) {
            this.type = type;
            this.amount = amount;
            this.silent = silent;
        }
    }
}
