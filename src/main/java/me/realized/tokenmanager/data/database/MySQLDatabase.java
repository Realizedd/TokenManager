package me.realized.tokenmanager.data.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.Getter;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.command.commands.subcommands.OfflineCommand.ModifyType;
import me.realized.tokenmanager.config.Config;
import me.realized.tokenmanager.util.Log;
import me.realized.tokenmanager.util.NumberUtil;
import me.realized.tokenmanager.util.compat.CompatUtil;
import me.realized.tokenmanager.util.profile.ProfileUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class MySQLDatabase extends AbstractDatabase {

    private static final String SERVER_MODE_MISMATCH = "Server is in %s mode, but found table '%s' does not have column '%s'! Please choose a different table name.";
    private final String table;
    private final ExecutorService executor;
    private final Map<UUID, Long> data = new HashMap<>();

    private HikariDataSource dataSource;

    @Getter
    private JedisPool jedisPool;
    private JedisListener listener;
    private transient boolean usingRedis;

    public MySQLDatabase(final TokenManagerPlugin plugin) {
        super(plugin);
        this.table = StringEscapeUtils.escapeSql(plugin.getConfiguration().getMysqlTable());
        this.executor = Executors.newCachedThreadPool();
        Query.update(table, online);
    }

    @Override
    public void setup() throws Exception {
        final Config config = plugin.getConfiguration();
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getMysqlUrl()
            .replace("%hostname%", config.getMysqlHostname())
            .replace("%port%", config.getMysqlPort())
            .replace("%database%", config.getMysqlDatabase())
        );
        hikariConfig.setDriverClassName("com.mysql." + (CompatUtil.isPre1_17() ? "" : "cj.") + "jdbc.Driver");
        hikariConfig.setUsername(config.getMysqlUsername());
        hikariConfig.setPassword(config.getMysqlPassword());

        this.dataSource = new HikariDataSource(hikariConfig);

        if (config.isRedisEnabled()) {
            final String password = config.getRedisPassword();

            if (password.isEmpty()) {
                this.jedisPool = new JedisPool(new JedisPoolConfig(), config.getRedisServer(), config.getRedisPort(), 0);
            } else {
                this.jedisPool = new JedisPool(new JedisPoolConfig(), config.getRedisServer(), config.getRedisPort(), 0, password);
            }

            plugin.doAsync(() -> {
                usingRedis = true;

                try (Jedis jedis = jedisPool.getResource()) {
                    jedis.subscribe(listener = new JedisListener(), "tokenmanager");
                } catch (Exception ex) {
                    usingRedis = false;
                    Log.error("Failed to connect to the redis server! Player balance synchronization issues may occur when modifying them while offline.");
                    Log.error("Cause of error: " + ex.getMessage());
                }
            });
        }

        try (
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement()
        ) {
            statement.execute(Query.CREATE_TABLE.query);

            try (ResultSet resultSet = connection.getMetaData().getColumns(null, null, table, "name")) {
                if (resultSet.isBeforeFirst() == online) {
                    throw new Exception(String.format(SERVER_MODE_MISMATCH, online ? "ONLINE" : "OFFLINE", table, online ? "uuid" : "name"));
                }
            }
        }
    }

    @Override
    public OptionalLong get(final Player player) {
        return from(data.get(player.getUniqueId()));
    }

    @Override
    public void get(final String key, final Consumer<OptionalLong> onLoad, final Consumer<String> onError, final boolean create) {
        try (Connection connection = dataSource.getConnection()) {
            onLoad.accept(select(connection, key, create));
        } catch (Exception ex) {
            if (onError != null) {
                onError.accept(ex.getMessage());
            }

            Log.error("Failed to obtain data for " + key + ": " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void set(final Player player, final long value) {
        data.put(player.getUniqueId(), value);
    }

    @Override
    public void set(final String key, final ModifyType type, final long amount, final long balance, final boolean silent, final Runnable onDone, final Consumer<String> onError) {
        plugin.doAsync(() -> {
            try (Connection connection = dataSource.getConnection()) {
                update(connection, key, balance);

                if (usingRedis) {
                    publish(key + ":" + type.name() + ":" + amount + ":" + silent);
                } else {
                    plugin.doSync(() -> onModification(key, type, amount, silent));
                }

                if (onDone != null) {
                    onDone.run();
                }
            } catch (Exception ex) {
                if (onError != null) {
                    onError.accept(ex.getMessage());
                }

                Log.error("Failed to save data for " + key + ": " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    @Override
    public void load(final Player player, final Function<Long, Long> modifyLoad) {
        plugin.doAsync(() -> get(from(player), balance -> {
            if (!balance.isPresent()) {
                return;
            }

            plugin.doSync(() -> {
                // Cancel caching if player has left before loading was completed
                if (!player.isOnline()) {
                    return;
                }

                long totalBalance = balance.getAsLong();

                if (modifyLoad != null) {
                    totalBalance = modifyLoad.apply(totalBalance);
                }

                data.put(player.getUniqueId(), totalBalance);
            });
        }, null, true));
    }

    @Override
    public void load(final Player player) {
        load(player, null);
    }

    @Override
    public void save(final Player player) {
        final OptionalLong balance = from(data.remove(player.getUniqueId()));

        if (!balance.isPresent()) {
            return;
        }

        executor.execute(() -> {
            try (Connection connection = dataSource.getConnection()) {
                update(connection, from(player), balance.getAsLong());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    @Override
    public void shutdown() throws Exception {
        executor.shutdown();

        if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
            Log.error("Some tasks have failed to execute!");
        }

        try (Connection connection = dataSource.getConnection()) {
            insertCache(connection, data, true);
        } finally {
            for (final AutoCloseable closeable : Arrays.asList(dataSource, listener, jedisPool)) {
                if (closeable != null) {
                    try {
                        closeable.close();
                    } catch (Exception ex) {
                        Log.error("Failed to close " + closeable.getClass().getSimpleName() + ": " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void ordered(final int limit, final Consumer<List<TopElement>> onLoad) {
        final List<TopElement> result = new ArrayList<>();

        if (limit <= 0) {
            onLoad.accept(result);
            return;
        }

        // Create a copy of the current cache to prevent HashMap being accessed by multiple threads
        final Map<UUID, Long> copy = new HashMap<>(data);

        plugin.doAsync(() -> {
            try (Connection connection = dataSource.getConnection()) {
                insertCache(connection, copy, false);
                connection.setAutoCommit(true);

                try (PreparedStatement statement = connection.prepareStatement(Query.SELECT_WITH_LIMIT.query)) {
                    statement.setInt(1, limit);

                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            final String key = online ? resultSet.getString("uuid") : resultSet.getString("name");
                            result.add(new TopElement(key, (int) resultSet.getLong("tokens")));
                        }

                        replaceNames(result, onLoad);
                    }
                }
            } catch (Exception ex) {
                Log.error("Failed to load top balances: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    @Override
    public void transfer(final CommandSender sender, final Consumer<String> onError) {
        plugin.doAsync(() -> {
            final File file = new File(plugin.getDataFolder(), "data.yml");

            if (!file.exists()) {
                sender.sendMessage(ChatColor.RED + "File not found!");
                return;
            }

            sender.sendMessage(ChatColor.BLUE + plugin.getDescription().getFullName() + ": Loading user data from " + file.getName() + "...");

            final FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            final ConfigurationSection section = config.getConfigurationSection("Players");

            if (section == null) {
                sender.sendMessage(ChatColor.RED + "Data not found!");
                return;
            }

            sender.sendMessage(ChatColor.BLUE + plugin.getDescription().getFullName() + ": Load Complete. Starting the transfer...");

            try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(Query.INSERT_OR_UPDATE.query)
            ) {
                connection.setAutoCommit(false);
                int i = 0;
                final Set<String> keys = section.getKeys(false);

                for (final String key : keys) {
                    final long value = section.getLong(key);
                    statement.setString(1, key);
                    statement.setLong(2, value);
                    statement.setLong(3, value);
                    statement.addBatch();

                    if (++i % 100 == 0 || i == keys.size()) {
                        statement.executeBatch();
                    }
                }

                connection.commit();
                connection.setAutoCommit(true);
                sender.sendMessage(ChatColor.BLUE + plugin.getDescription().getFullName() + ": Transfer Complete. Total Transferred Data: " + keys.size());
            } catch (SQLException ex) {
                onError.accept(ex.getMessage());
                Log.error("Failed to transfer data from file: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    private void insertCache(final Connection connection, final Map<UUID, Long> cache, final boolean remove) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(Query.UPDATE.query)) {
            connection.setAutoCommit(false);

            int i = 0;
            final Collection<? extends Player> players = Bukkit.getOnlinePlayers();

            for (final Player player : players) {
                final Optional<Long> balance = Optional.ofNullable(remove ? cache.remove(player.getUniqueId()) : cache.get(player.getUniqueId()));

                if (!balance.isPresent()) {
                    continue;
                }

                statement.setLong(1, balance.get());
                statement.setString(2, online ? player.getUniqueId().toString() : player.getName());
                statement.addBatch();

                if (++i % 100 == 0 || i == players.size()) {
                    statement.executeBatch();
                }
            }
        } finally {
            connection.commit();
        }
    }

    private OptionalLong select(final Connection connection, final String key, final boolean create) throws Exception {
        try (PreparedStatement selectStatement = connection.prepareStatement(Query.SELECT_ONE.query)) {
            selectStatement.setString(1, key);

            try (ResultSet resultSet = selectStatement.executeQuery()) {
                if (!resultSet.next()) {
                    if (create) {
                        final long defaultBalance = plugin.getConfiguration().getDefaultBalance();

                        try (PreparedStatement insertStatement = connection.prepareStatement(Query.INSERT.query)) {
                            insertStatement.setString(1, key);
                            insertStatement.setLong(2, plugin.getConfiguration().getDefaultBalance());
                            insertStatement.execute();
                        }

                        return OptionalLong.of(defaultBalance);
                    }

                    return OptionalLong.empty();
                }

                return OptionalLong.of(resultSet.getLong("tokens"));
            }
        }
    }

    private void update(final Connection connection, final String key, final long value) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(Query.UPDATE.query)) {
            statement.setLong(1, value);
            statement.setString(2, key);
            statement.execute();
        }
    }

    private void onModification(final String key, final ModifyType type, final long amount, final boolean silent) {
        final Player player;

        if (ProfileUtil.isUUID(key)) {
            player = Bukkit.getPlayer(UUID.fromString(key));
        } else {
            player = Bukkit.getPlayerExact(key);
        }

        if (player == null) {
            return;
        }

        if (type == ModifyType.SET) {
            set(player, amount);
            return;
        }

        final OptionalLong cached;

        if (!(cached = get(player)).isPresent()) {
            return;
        }

        set(player, type.apply(cached.getAsLong(), amount));

        if (silent) {
            return;
        }

        plugin.getLang().sendMessage(player, true, "COMMAND." + (type == ModifyType.ADD ? "add" : "remove"), "amount", amount);
    }

    private void publish(final String message) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish("tokenmanager", message);
        } catch (JedisConnectionException ignored) {}
    }

    private enum Query {

        CREATE_TABLE("CREATE TABLE IF NOT EXISTS {table} (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, {column} NOT NULL UNIQUE, tokens BIGINT(255) NOT NULL);"),
        SELECT_WITH_LIMIT("SELECT {identifier}, tokens FROM {table} ORDER BY tokens DESC LIMIT ?;"),
        SELECT_ONE("SELECT tokens FROM {table} WHERE {identifier}=?;"),
        INSERT("INSERT INTO {table} ({identifier}, tokens) VALUES (?, ?);"),
        UPDATE("UPDATE {table} SET tokens=? WHERE {identifier}=?;"),
        INSERT_OR_UPDATE("INSERT INTO {table} ({identifier}, tokens) VALUES (?, ?) ON DUPLICATE KEY UPDATE tokens=?;");

        private String query;

        Query(final String query) {
            this.query = query;
        }

        private static void update(final String table, final boolean online) {
            for (final Query query : values()) {
                query.replace(s -> s.replace("{table}", table).replace("{identifier}", online ? "uuid" : "name"));

                if (query == CREATE_TABLE) {
                    query.replace(s -> s.replace("{column}", online ? "uuid VARCHAR(36)" : "name VARCHAR(16)"));
                }
            }
        }

        private void replace(final Function<String, String> function) {
            this.query = function.apply(query);
        }
    }

    private class JedisListener extends JedisPubSub implements AutoCloseable {

        @Override
        public void onMessage(final String channel, final String message) {
            final String[] args = message.split(":");

            if (args.length < 3) {
                return;
            }

            plugin.doSync(() -> {
                final ModifyType type = ModifyType.valueOf(args[1]);
                final OptionalLong amount = NumberUtil.parseLong(args[2]);

                if (!amount.isPresent()) {
                    return;
                }

                onModification(args[0], type, amount.getAsLong(), args[3].equals("true"));
            });
        }

        @Override
        public void close() {
            unsubscribe();
        }
    }
}
