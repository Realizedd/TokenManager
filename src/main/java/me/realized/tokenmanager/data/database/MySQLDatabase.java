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

package me.realized.tokenmanager.data.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
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
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import lombok.Getter;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.config.TMConfig;
import me.realized.tokenmanager.util.Callback;
import me.realized.tokenmanager.util.Log;
import me.realized.tokenmanager.util.NumberUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class MySQLDatabase extends AbstractDatabase {

    private static final String SERVER_MODE_MISMATCH = "Server is in %s mode, but found table '%s' does not have column '%s'! Please choose a different table name.";

    private enum Query {

        CREATE_TABLE("CREATE TABLE IF NOT EXISTS {table} ({column} NOT NULL, tokens bigint(255) NOT NULL, PRIMARY KEY ({identifier}));"),
        SELECT_WITH_LIMIT("SELECT {identifier}, tokens from {table} ORDER BY tokens DESC LIMIT ?;"),
        SELECT_ONE("SELECT tokens FROM {table} WHERE {identifier}=?;"),
        INSERT("INSERT INTO {table} ({identifier}, tokens) VALUES (?, ?) ON DUPLICATE KEY UPDATE tokens=?;");

        private String query;

        Query(final String query) {
            this.query = query;
        }

        private String get() {
            return query;
        }

        private void replace(final Function<String, String> function) {
            this.query = function.apply(query);
        }

        private static void update(final String table, final boolean online) {
            for (final Query query : values()) {
                query.replace(s -> s.replace("{table}", table).replace("{identifier}", online ? "UUID" : "NAME"));

                if (query == CREATE_TABLE) {
                    query.replace(s -> s.replace("{column}", online ? "uuid varchar(36)" : "name varchar(16)"));
                }
            }
        }
    }

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
        final TMConfig config = plugin.getConfiguration();
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + config.getMysqlHostname() + ":" + config.getMysqlPort() + "/" + config.getMysqlDatabase());
        hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");
        hikariConfig.setUsername(config.getMysqlUsername());
        hikariConfig.setPassword(config.getMysqlPassword());

        this.dataSource = new HikariDataSource(hikariConfig);

        final String password = config.getRedisPassword();

        if (password.isEmpty()) {
            this.jedisPool = new JedisPool(new JedisPoolConfig(), config.getRedisServer(), config.getRedisPort(), 0);

        } else {
            this.jedisPool = new JedisPool(new JedisPoolConfig(), config.getRedisServer(), config.getRedisPort(), 0,
                config.getRedisPassword());
        }

        plugin.doAsync(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.subscribe(listener = new JedisListener(), "tokenmanager");
                usingRedis = true;
            } catch (JedisConnectionException ex) {
                Log.error("Failed to connect to the redis server! Player balance synchronization issues may occur when modifying them while offline.");
                usingRedis = false;
            }
        });

        try (
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement()
        ) {
            statement.execute(Query.CREATE_TABLE.get());

            try (ResultSet resultSet = connection.getMetaData().getColumns(null, null, table, "name")) {
                if (resultSet.isBeforeFirst() == online) {
                    throw new Exception(String.format(SERVER_MODE_MISMATCH, online ? "ONLINE" : "OFFLINE", table, online ? "uuid" : "name"));
                }
            }
        }
    }

    @Override
    public OptionalLong get(final Player player) {
        final Long value = data.get(player.getUniqueId());

        if (value == null) {
            return OptionalLong.empty();
        }

        return OptionalLong.of(value);
    }

    @Override
    public void get(final Player player, final Callback<OptionalLong> callback) {
        get(online ? player.getUniqueId().toString() : player.getName(), callback, true);
    }

    @Override
    public void get(final String key, final Callback<OptionalLong> callback, final boolean create) {
        executor.execute(() -> {
            try {
                callback.call(select(key, create));
            } catch (Exception ex) {
                Log.error("Failed to obtain data for " + key + ": " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    @Override
    public void set(final Player player, final long value) {
        data.put(player.getUniqueId(), value);
    }

    @Override
    public void set(final String key, final boolean set, final long amount, final long updated, final Callback<Boolean> callback) {
        executor.execute(() -> {
            try (Connection connection = dataSource.getConnection()) {
                insert(connection, key, updated);

                if (usingRedis) {
                    publish(key + ":" + (set ? updated : amount) + ":" + set);
                } else {
                    plugin.doSync(() -> onModification(key, set ? updated : amount, set));
                }

                callback.call(true);
            } catch (Exception ex) {
                Log.error("Failed to save data for " + key + ": " + ex.getMessage());
                ex.printStackTrace();
                callback.call(false);
            }
        });
    }

    @Override
    public void save(final Player player) {
        final OptionalLong balance = get(player);

        if (!balance.isPresent()) {
            return;
        }

        data.remove(player.getUniqueId());
        executor.execute(() -> {
            try (Connection connection = dataSource.getConnection()) {
                insert(connection, online ? player.getUniqueId().toString() : player.getName(), balance.getAsLong());
            } catch (Exception ex) {
                Log.error("Failed to save data for " + player.getName() + ": " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    @Override
    public void save() throws Exception {
        executor.shutdown();

        if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
            Log.error("Some tasks have failed to execute.");
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
    public void ordered(final int limit, final Callback<List<TopElement>> callback) {
        final List<TopElement> result = new ArrayList<>();

        if (limit <= 0) {
            callback.call(result);
            return;
        }

        // Create a copy of the current cache to prevent HashMap being accessed by multiple threads
        final Map<UUID, Long> copy = new HashMap<>(data);

        executor.execute(() -> {
            try (Connection connection = dataSource.getConnection()) {
                insertCache(connection, copy, false);
                connection.setAutoCommit(true);

                try (PreparedStatement statement = connection.prepareStatement(Query.SELECT_WITH_LIMIT.get())) {
                    statement.setInt(1, limit);

                    final List<UUID> uuids = new ArrayList<>();

                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            final String key = online ? resultSet.getString("uuid") : resultSet.getString("name");

                            if (online) {
                                uuids.add(UUID.fromString(key));
                            }

                            result.add(new TopElement(key, (int) resultSet.getLong("tokens")));
                        }

                        checkNames(uuids, result, callback);
                    }
                }
            } catch (Exception ex) {
                Log.error("Failed to load top balances: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    private void insert(final Connection connection, final String key, final long value) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(Query.INSERT.get())) {
            statement.setString(1, key);
            statement.setLong(2, value);
            statement.setLong(3, value);
            statement.execute();
        }
    }

    private void insertCache(final Connection connection, final Map<UUID, Long> cache, final boolean remove) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(Query.INSERT.get())) {
            connection.setAutoCommit(false);

            int i = 0;
            final Collection<? extends Player> players = Bukkit.getOnlinePlayers();

            for (final Player player : players) {
                final Optional<Long> balance = Optional
                    .ofNullable(remove ? cache.remove(player.getUniqueId()) : cache.get(player.getUniqueId()));

                if (!balance.isPresent()) {
                    continue;
                }

                statement.setString(1, online ? player.getUniqueId().toString() : player.getName());
                statement.setLong(2, balance.get());
                statement.setLong(3, balance.get());
                statement.addBatch();

                if (++i % 100 == 0 || i == players.size()) {
                    statement.executeBatch();
                }
            }

            connection.commit();
        }
    }

    private OptionalLong select(final String key, final boolean create) throws Exception {
        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(Query.SELECT_ONE.get())
        ) {
            statement.setString(1, key);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.isBeforeFirst()) {
                    if (create) {
                        final long defaultBalance = plugin.getConfiguration().getDefaultBalance();
                        insert(connection, key, defaultBalance);
                        return OptionalLong.of(defaultBalance);
                    }

                    return OptionalLong.empty();
                }

                resultSet.next();
                return OptionalLong.of(resultSet.getLong("tokens"));
            }
        }
    }

    private void publish(final String message) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish("tokenmanager", message);
        } catch (JedisConnectionException ignored) {}
    }

    private class JedisListener extends JedisPubSub implements AutoCloseable {

        @Override
        public void onMessage(final String channel, final String message) {
            final String[] args = message.split(":");

            if (args.length < 3) {
                return;
            }

            plugin.doSync(() -> {
                final OptionalLong amount = NumberUtil.parseLong(args[1]);

                if (!amount.isPresent()) {
                    return;
                }

                onModification(args[0], amount.getAsLong(), args[2].equalsIgnoreCase("true"));
            });
        }

        @Override
        public void close() {
            unsubscribe();
        }
    }
}
