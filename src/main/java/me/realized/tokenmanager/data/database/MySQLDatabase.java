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
import java.util.Arrays;
import java.util.OptionalLong;
import lombok.Getter;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.config.TMConfig;
import me.realized.tokenmanager.util.Log;
import me.realized.tokenmanager.util.NumberUtil;
import org.bukkit.Bukkit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

public class MySQLDatabase extends Database {

    private HikariDataSource dataSource;

    @Getter
    private JedisPool jedisPool;
    private JedisListener listener;

    public MySQLDatabase(final TokenManagerPlugin plugin) {
        super(plugin);
        Query.INSERT.replace(s -> "INSERT INTO {table} ({identifier}, tokens) VALUES (?, ?) ON DUPLICATE KEY UPDATE tokens=?;");
        updateQueries();
    }

    @Override
    public void setupTable() throws Exception {
        final TMConfig config = plugin.getConfiguration();
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig
            .setJdbcUrl("jdbc:mysql://" + config.getMysqlHostname() + ":" + config.getMysqlPort() + "/" + config.getMysqlDatabase());
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

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.subscribe(listener = new JedisListener(), "tokenmanager");
            } catch (Exception ex) {
                Log.error(
                    "Failed to connect to the redis server! Commands modifying offline player token balance will not synchronize between servers properly.");
            }
        });

        super.setupTable();
    }

    @Override
    public Connection getConnection() throws Exception {
        return dataSource.getConnection();
    }

    @Override
    Iterable<AutoCloseable> getCloseables() {
        return Arrays.asList(dataSource, listener, jedisPool);
    }

    void publish(final String message) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish("tokenmanager", message);
        }
    }

    private class JedisListener extends JedisPubSub implements AutoCloseable {

        @Override
        public void onMessage(final String channel, final String message) {
            final String[] args = message.split(":");

            if (args.length < 3) {
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                final OptionalLong amount = NumberUtil.parseLong(args[1]);

                if (!amount.isPresent()) {
                    return;
                }

                handleModification(args[0], amount.getAsLong(), args[2].equalsIgnoreCase("true"));
            });
        }

        @Override
        public void close() {
            unsubscribe();
        }
    }
}
