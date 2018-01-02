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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.Getter;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.util.Callback;
import me.realized.tokenmanager.util.profile.NameFetcher;
import me.realized.tokenmanager.util.profile.ProfileUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public abstract class Database {

    protected final TokenManagerPlugin plugin;

    @Getter(value = AccessLevel.PROTECTED)
    private final boolean online;
    private final boolean mysql;
    private final String table;
    // TODO: 12/23/17 New ExecutorService for /token top to load names with delays?
    private final ScheduledExecutorService executor;
    // NOTE: UUID is still usable for servers in offline mode since it only tracks players currently online.
    private final Map<UUID, Long> data = new HashMap<>();

    Database(final TokenManagerPlugin plugin) {
        this.plugin = plugin;
        this.online = ProfileUtil.isOnlineMode();
        this.mysql = plugin.getConfiguration().isMysqlEnabled();
        this.executor = Executors.newSingleThreadScheduledExecutor();

        Query.CREATE_TABLE.replace(s -> s.replace("{column}", online ? "uuid varchar(36)" : "name varchar(16)"));

        this.table = StringEscapeUtils.escapeSql(plugin.getConfiguration().getMysqlTable());
        updateQueries();
    }

    void updateQueries() {
        for (final Query query : Query.values()) {
            query.replace(s -> s.replace("{table}", table).replace("{identifier}", online ? "uuid" : "name"));
        }
    }

    /**
     * Checks and creates the table for the plugin if it does not exist.
     *
     * @throws Exception If the table was found but it does contain the needed column for the server mode, an {@link Exception} is thrown.
     */
    public void setup() throws Exception {
        try (
            Connection connection = getConnection();
            Statement statement = connection.createStatement()
        ) {
            statement.execute(Query.CREATE_TABLE.get());

            try (ResultSet resultSet = connection.getMetaData().getColumns(null, null, table, "name")) {
                if (resultSet.isBeforeFirst() == online) {
                    throw new Exception(
                        "Server is in " + (online ? "ONLINE" : "OFFLINE") + " mode, but table '" + table + "' does not have the column '"
                            + (online ? "uuid" : "name") + "'! Please choose a different table.");
                }
            }
        }
    }

    /**
     * Connection to be used for all database operations.
     *
     * @return Connection instance to be used.
     * @throws Exception If DataSource fails to return a valid connection.
     */
    abstract Connection getConnection() throws Exception;

    /**
     * AutoCloseable instance to be closed on unload.
     *
     * @return AutoCloseable instance to be closed.
     */
    abstract Iterable<AutoCloseable> getCloseables();

    /**
     * Get cached data of the player.
     *
     * @param player Player to get the data.
     * @return Optional with data inside if found, otherwise empty.
     */
    public OptionalLong get(final Player player) {
        final Long value = data.get(player.getUniqueId());

        if (value != null) {
            return OptionalLong.of(value);
        }

        return OptionalLong.empty();
    }

    /**
     * Get the stored data of the player.
     *
     * @param key UUID or the name the player.
     * @param callback Callback to call once data is retrieved.
     */
    public void get(final String key, final Callback<OptionalLong> callback, final boolean save) {
        executor.execute(() -> {
            try {
                callback.call(select(key, save));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void get(final Player player, final Callback<OptionalLong> callback) {
        get(online ? player.getUniqueId().toString() : player.getName(), callback, true);
    }

    /**
     * Set the cached data value for player.
     *
     * @param player Player to set the data.
     * @param value Value to be set in the cache.
     */
    public void set(final Player player, final long value) {
        data.put(player.getUniqueId(), value);
    }


    /**
     * Save the updated balance of the key to the database.
     *
     * @param key Key associated with the balance.
     * @param set true to set the balance to updated value, otherwise false.
     * @param amount The difference between the old balance and the new balance.
     * @param updated The new balance to save.
     * @param callback Callback to call once the operation is completed.
     */
    public void set(final String key, final boolean set, final long amount, final long updated, final Callback<Boolean> callback) {
        executor.execute(() -> {
            try (Connection connection = getConnection()) {
                insert(connection, key, updated);
                callback.call(true);

                if (this instanceof MySQLDatabase) {
                    ((MySQLDatabase) this).publish(key + ":" + (set ? updated : amount) + ":" + set);
                }
            } catch (Exception ex) {
                plugin.error("Failed to save data for " + key + ": " + ex.getMessage());
                ex.printStackTrace();
                callback.call(false);
            }
        });
    }

    /**
     * Saves the cached data associated to key and clears it from cache. Must be called synchronously!
     *
     * @param player Player to save.
     */
    public void save(final Player player) {
        final OptionalLong balance = get(player);

        if (!balance.isPresent()) {
            return;
        }

        data.remove(player.getUniqueId());
        executor.execute(() -> {
            try (Connection connection = getConnection()) {
                insert(connection, online ? player.getUniqueId().toString() : player.getName(), balance.getAsLong());
            } catch (Exception ex) {
                plugin.error("Failed to save data for " + player.getName() + ": " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    /**
     * Saves the current cache and returns top balances of the table. Must be called within server thread!
     *
     * @param limit limitation of the rows to be returned
     * @param callback Callback to call once data is retrieved
     */
    public void ordered(final int limit, final Callback<List<RankedData>> callback) {
        final List<RankedData> result = new ArrayList<>();

        if (limit <= 0) {
            callback.call(result);
            return;
        }

        // Create a copy of the current cache to prevent HashMap being accessed by multiple threads
        final Map<UUID, Long> copy = new HashMap<>(data);

        executor.execute(() -> {
            try (Connection connection = getConnection()) {
                save(connection, copy, false);
                connection.setAutoCommit(true);

                try (PreparedStatement statement = connection.prepareStatement(Query.SELECT_WITH_LIMIT.get())) {
                    statement.setInt(1, limit);

                    final List<UUID> uuids = new ArrayList<>();
                    final ResultSet resultSet = statement.executeQuery();
                    int rank = 0;

                    while (resultSet.next()) {
                        final String key = online ? resultSet.getString("uuid") : resultSet.getString("name");

                        if (online) {
                            uuids.add(UUID.fromString(key));
                        }

                        result.add(new RankedData(key, ++rank, (int) resultSet.getLong("tokens")));
                    }

                    if (online) {
                        NameFetcher.getNames(uuids, names -> {
                            for (final RankedData data : result) {
                                final String name = names.get(UUID.fromString(data.getKey()));

                                if (name == null) {
                                    data.setKey("&cFailed to get name!");
                                    continue;
                                }

                                data.setKey(name);
                            }

                            callback.call(result);
                        });
                    } else {
                        callback.call(result);
                    }
                }
            } catch (Exception ex) {
                plugin.error("Failed to load top balances for /token top: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    /**
     * Shuts down the executor and saves online player data synchronously.
     */
    public void save() throws Exception {
        executor.shutdown();

        try (Connection connection = getConnection()) {
            save(connection, data, true);
        } finally {
            final Iterable<AutoCloseable> closeables = getCloseables();

            if (closeables != null) {
                for (final AutoCloseable closeable : closeables) {
                    if (closeable == null) {
                        continue;
                    }

                    try {
                        closeable.close();
                    } catch (Exception ex) {
                        plugin.error("Failed to close " + closeable.getClass().getSimpleName() + ": " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Saves online player's cached data to db.
     */
    private void save(final Connection connection, final Map<UUID, Long> cache, final boolean remove) throws SQLException {
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

                if (mysql) {
                    statement.setLong(3, balance.get());
                }

                statement.addBatch();

                if (++i % 100 == 0 || i == players.size()) {
                    statement.executeBatch();
                }
            }

            connection.commit();
        }
    }

    private OptionalLong select(final String key, final boolean save) throws Exception {
        try (
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(Query.SELECT_ONE.get())
        ) {
            statement.setString(1, key);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.isBeforeFirst()) {
                    if (save) {
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

    private void insert(final Connection connection, final String key, final long value) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(Query.INSERT.get())) {
            statement.setString(1, key);
            statement.setLong(2, value);

            if (mysql) {
                statement.setLong(3, value);
            }

            statement.execute();
        }
    }

    /**
     * Handles data conversion from 2.0 -> 3.0
     */
    public void transfer(final File file) throws Exception {
        final ConfigurationSection data = YamlConfiguration.loadConfiguration(file).getConfigurationSection("Players");

        if (data == null) {
            return;
        }

        plugin.getLogger().info("Starting to transfer data of file('" + data.getName() + "') to the new data storage...");

        try {
            final File renamed = Files
                .copy(file.toPath(), new File(plugin.getDataFolder(), "data-old-" + System.nanoTime() + ".yml").toPath()).toFile();
            plugin.getLogger().info("Old data file('" + file.getName() + "') was stored as " + renamed.getName() + ".");

            if (!file.delete()) {
                plugin.getLogger().severe("Failed to delete '" + file.getName()
                    + "'. Please manually rename/delete it or the data conversion will occur on every load.");
            }
        } catch (IOException ex) {
            plugin.error("Failed to convert from flatfile to sql: " + ex.getMessage());
            ex.printStackTrace();
            return;
        }

        try (
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(Query.INSERT.get())
        ) {
            connection.setAutoCommit(false);

            int i = 0;
            final Set<String> keys = data.getKeys(false);

            for (final String key : keys) {
                if (ProfileUtil.isUUID(key) != online) {
                    plugin.getLogger().severe(
                        "Key '" + key + "' was not a valid " + (online ? "UUID" : "name") + ". Assigned data '" + data.getInt(key)
                            + "' will not be transferred.");
                    continue;
                }

                statement.setString(1, key);
                statement.setLong(2, data.getInt(key));

                if (mysql) {
                    statement.setLong(3, data.getInt(key));
                }

                statement.addBatch();

                if (++i % 100 == 0 || i == keys.size()) {
                    statement.executeBatch();
                }
            }

            connection.commit();
            plugin.getLogger().info("Successfully transferred data of " + i + " user(s).");
        }
    }

    enum Query {

        CREATE_TABLE("CREATE TABLE IF NOT EXISTS {table} ({column} NOT NULL, tokens bigint(255) NOT NULL, PRIMARY KEY ({identifier}));"),
        SELECT_WITH_LIMIT("SELECT {identifier}, tokens from {table} ORDER BY tokens DESC LIMIT ?;"),
        SELECT_ONE("SELECT tokens FROM {table} WHERE {identifier}=?;"),
        INSERT("INSERT OR REPLACE INTO {table} ({identifier}, tokens) VALUES (?, ?);");

        private String query;

        Query(final String query) {
            this.query = query;
        }

        String get() {
            return query;
        }

        void replace(final Function<String, String> function) {
            this.query = function.apply(query);
        }
    }

    public class RankedData {

        private String key;
        private final int rank, tokens;

        RankedData(final String key, final int rank, final int tokens) {
            this.key = key;
            this.rank = rank;
            this.tokens = tokens;
        }

        public String getKey() {
            return key;
        }

        public void setKey(final String key) {
            this.key = key;
        }

        public int getRank() {
            return rank;
        }

        public int getTokens() {
            return tokens;
        }
    }
}
