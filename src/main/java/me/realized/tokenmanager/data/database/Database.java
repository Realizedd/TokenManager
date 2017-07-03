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

import me.realized.tokenmanager.TokenManager;
import me.realized.tokenmanager.util.Callback;
import me.realized.tokenmanager.util.plugin.AbstractPluginDelegate;
import me.realized.tokenmanager.util.profile.ProfileUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.logging.Level;

public abstract class Database extends AbstractPluginDelegate<TokenManager> {

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

    private final boolean mysql, online;
    private final String table;
    private final ExecutorService executor;

    // UUID is still usable for servers in offline mode since it's a unique key for online players only.
    private final Map<UUID, Integer> data = new HashMap<>();

    Database(final TokenManager plugin) throws Exception {
        super(plugin);
        this.mysql = getPlugin().getConfiguration().isMysqlEnabled();
        this.online = ProfileUtil.isOnlineMode();
        this.executor = Executors.newCachedThreadPool();

        Query.CREATE_TABLE.replace(s -> s.replace("{column}", online ? "uuid varchar(36)" : "name varchar(16)"));

        this.table = StringEscapeUtils.escapeSql(getPlugin().getConfiguration().getMysqlTable());
        updateQueries();
    }

    void updateQueries() {
        for (final Query query : Query.values()) {
            query.replace(s -> s.replace("{table}", table).replace("{identifier}", online ? "uuid" : "name"));
        }
    }

    /**
     *
     * Checks and creates the table for the plugin if it does not exist.
     *
     * @throws Exception If the table was found but it does contain the needed column for the server mode, an {@link Exception} is thrown.
     */
    public void setup() throws Exception {
        try (
                Connection connection = getConnection();
                Statement statement = connection.createStatement();
        ) {
            statement.execute(Query.CREATE_TABLE.get());

            try (ResultSet resultSet = connection.getMetaData().getColumns(null, null, table, "name")) {
                if (!resultSet.isBeforeFirst()) {
                    throw new Exception("Server is in " + (online ? "ONLINE" : "OFFLINE") + " mode, but table '" + table + "' does not have the column '" + (online ? "uuid" : "name") + "'! Please choose a different table.");
                }
            }
        }
    }

    /**
     *
     * Connection to be used for all database operations.
     *
     * @return Connection instance to be used.
     * @throws Exception If DataSource fails to return a valid connection.
     */
    abstract Connection getConnection() throws Exception;

    /**
     *
     * AutoCloseable instance to be closed on unload.
     *
     * @return AutoCloseable instance to be closed.
     */
    abstract AutoCloseable getCloseable();

    /**
     *
     * Get cached data of the player.
     *
     * @param player Player to get the data.
     * @return Optional with data inside if found, otherwise empty.
     */
    public Optional<Integer> get(final Player player) {
        return Optional.ofNullable(data.get(player.getUniqueId()));
    }

    /**
     *
     * Set the cached data value for player.
     *
     * @param player Player to set the data.
     * @param value Value to be set in the cache.
     */
    public void set(final Player player, final int value) {
        data.put(player.getUniqueId(), value);
    }

    /**
     *
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
        final Map<UUID, Integer> copy = new HashMap<>(data);

        executor.execute(() -> {
            try (Connection connection = getConnection()) {
                save(connection, copy);
                connection.setAutoCommit(true);

                try (PreparedStatement statement = connection.prepareStatement(Query.SELECT_WITH_LIMIT.get())) {
                    statement.setInt(1, limit);

                    final ResultSet resultSet = statement.executeQuery();
                    int rank = 0;

                    while (resultSet.next()) {
                        final String name = online ? ProfileUtil.getName(resultSet.getString("uuid")) : resultSet.getString("name");
                        result.add(new RankedData(++rank, name != null ? name : "&cName load failure!", (int) resultSet.getLong("tokens")));
                    }

                    callback.call(result);
                }
            } catch (Exception ex) {
                getPlugin().getLogger().log(Level.SEVERE, "Failed to load top balances for /token top!", ex);
            }
        });
    }

    /**
     *
     * Loads the data associated with the provided key from the database.
     *
     * @param player Player to get the data.
     * @param callback Callback to call with the loaded data.
     */
    public void load(final Player player, final Callback<Integer> callback) {
        executor.execute(() -> {
            try (
                    Connection connection = getConnection();
                    PreparedStatement statement = connection.prepareStatement(Query.SELECT_ONE.get())
            ) {
                statement.setString(1, online ? player.getUniqueId().toString() : player.getName());

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.isBeforeFirst()) {
                        callback.call(getPlugin().getConfiguration().getDefaultBalance());
                    } else {
                        while (resultSet.next()) {
                            callback.call((int) resultSet.getLong("tokens"));
                        }
                    }
                }
            } catch (Exception ex) {
                getPlugin().getLogger().log(Level.SEVERE, "Failed to load data for " + player.getName() + "!", ex);
            }
        });
    }

    /**
     *
     * Saves the cached data associated to key and clears it from cache. Must be called synchronously!
     *
     * @param player Player to save.
     */
    public void save(final Player player) {
        final Optional<Integer> balance = get(player);

        if (!balance.isPresent()) {
            return;
        }

        data.remove(player.getUniqueId());
        executor.execute(() -> {
            try (
                    Connection connection = getConnection();
                    PreparedStatement statement = connection.prepareStatement(Query.INSERT.get())
            ) {
                statement.setString(1, online ? player.getUniqueId().toString() : player.getName());
                statement.setLong(2, balance.get());

                if (mysql) {
                    statement.setLong(3, balance.get());
                }

                statement.execute();
            } catch (Exception ex) {
                getPlugin().getLogger().log(Level.SEVERE, "Failed to save data for " + player.getName() + "!", ex);
            }
        });
    }

    /**
     * Shuts down the executor and saves online player data synchronously.
     */
    public void save() {
        executor.shutdown();

        try (Connection connection = getConnection()) {
            save(connection, data);
        } catch (Exception ex) {
            getPlugin().getLogger().log(Level.SEVERE, "Failed to save online player data!", ex);
        } finally {
            final AutoCloseable closeable = getCloseable();

            if (closeable != null) {
                try {
                    closeable.close();
                } catch (Exception ex) {
                    getPlugin().getLogger().log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * Saves online player's cached data to db.
     */
    private void save(final Connection connection, final Map<UUID, Integer> dataSource) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(Query.INSERT.get())) {
            connection.setAutoCommit(false);

            int i = 0;
            final Collection<? extends Player> players = Bukkit.getOnlinePlayers();

            for (final Player player : players) {
                final Optional<Integer> balance = Optional.ofNullable(dataSource.get(player.getUniqueId()));

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

    /**
     *
     * Handles data conversion from 2.0 -> 3.0
     *
     */
    public void transfer(final File file) throws Exception {
        final ConfigurationSection data = YamlConfiguration.loadConfiguration(file).getConfigurationSection("Players");

        if (data == null) {
            return;
        }

        getPlugin().getLogger().info("Starting to transfer data of file('" + data.getName() + "') to the new data storage...");

        try {
            final File renamed = Files.copy(file.toPath(), new File(getPlugin().getDataFolder(), "data-old-" + System.nanoTime() + ".yml").toPath()).toFile();
            getPlugin().getLogger().info("Old data file('" + file.getName() + "') was stored as " + renamed.getName() + ".");

            if (!file.delete()) {
                getPlugin().getLogger().severe("Failed to delete '" + file.getName() + "'. Please manually rename/delete it or the data conversion will occur on every load.");
            }
        } catch (IOException ex) {
            getPlugin().getLogger().severe("Failed to transfer data: " + ex.getMessage());
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
                    getPlugin().getLogger().severe("Key '" + key + "' was not a valid " + (online ? "UUID" : "name") + ". Assigned data '" + data.getInt(key) + "' will not be transferred.");
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
            getPlugin().getLogger().info("Successfully transferred data of " + i + " user(s).");
        }
    }

    public class RankedData {

        private final int rank;
        private final String key;
        private final int value;

        RankedData(final int rank, final String key, final int value) {
            this.rank = rank;
            this.key = key;
            this.value = value;
        }

        public int getRank() {
            return rank;
        }

        public String getKey() {
            return key;
        }

        public int getValue() {
            return value;
        }
    }
}
