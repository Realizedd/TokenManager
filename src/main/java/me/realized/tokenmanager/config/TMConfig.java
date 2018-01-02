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

package me.realized.tokenmanager.config;

import java.io.IOException;
import lombok.Getter;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.config.converters.ConfigConverter2_3;
import me.realized.tokenmanager.util.config.AbstractConfiguration;
import org.bukkit.configuration.file.FileConfiguration;

public class TMConfig extends AbstractConfiguration<TokenManagerPlugin> {

    @Getter
    private int version;
    @Getter
    private int defaultBalance;
    @Getter
    private boolean openSelectedEnabled;
    @Getter
    private String openSelectedShop;
    @Getter
    private int clickDelay;
    @Getter
    private boolean mysqlEnabled;
    @Getter
    private String mysqlUsername;
    @Getter
    private String mysqlPassword;
    @Getter
    private String mysqlHostname;
    @Getter
    private String mysqlPort;
    @Getter
    private String mysqlDatabase;
    @Getter
    private String mysqlTable;
    @Getter
    private String redisServer;
    @Getter
    private int redisPort;
    @Getter
    private String redisPassword;
    @Getter
    private boolean registerEconomy;
    @Getter
    private int balanceTopUpdateInterval;

    public TMConfig(final TokenManagerPlugin plugin) {
        super(plugin, "config");
    }

    @Override
    protected void loadValues(FileConfiguration configuration) throws IOException {
        if (!configuration.isInt("config-version")) {
            configuration = convert(new ConfigConverter2_3());
        } else if (configuration.getInt("config-version") < getLatestVersion()) {
            configuration = convert(null);
        }

        version = configuration.getInt("config-version");
        defaultBalance = configuration.getInt("default-balance", 25);
        openSelectedEnabled = configuration.getBoolean("shop.open-selected.enabled", false);
        openSelectedShop = configuration.getString("shop.open-selected.shop", "test");
        clickDelay = configuration.getInt("shop.click-delay", 0);
        mysqlEnabled = configuration.getBoolean("data.mysql.enabled", false);
        mysqlUsername = configuration.getString("data.mysql.username", "root");
        mysqlPassword = configuration.getString("data.mysql.password", "password");
        mysqlHostname = configuration.getString("data.mysql.hostname", "127.0.0.1");
        mysqlPort = configuration.getString("data.mysql.port", "3306");
        mysqlDatabase = configuration.getString("data.mysql.database", "database");
        mysqlTable = configuration.getString("data.mysql.table", "tokenmanager");
        redisServer = configuration.getString("data.mysql.redis.server", "127.0.0.1");
        redisPort = configuration.getInt("data.mysql.redis.port", 6379);
        redisPassword = configuration.getString("data.mysql.redis.password", "");
        registerEconomy = configuration.getBoolean("data.register-economy", false);
        balanceTopUpdateInterval = configuration.getInt("data.balance-top-update-interval", 5);
    }
}
