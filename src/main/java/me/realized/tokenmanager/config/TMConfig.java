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

import lombok.Getter;
import me.realized.tokenmanager.TokenManager;
import me.realized.tokenmanager.config.converters.ConfigConverter2_3;
import me.realized.tokenmanager.util.config.AbstractConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.IOException;

/**
 * Class created at 6/16/17 by Realized
 **/

public class TMConfig extends AbstractConfiguration<TokenManager> {

    @Getter private int version;
    @Getter private int defaultBalance;
    @Getter private boolean openSelectedEnabled;
    @Getter private String openSelectedShop;
    @Getter private int clickDelay;
    @Getter private boolean mysqlEnabled;
    @Getter private String mysqlUsername;
    @Getter private String mysqlPassword;
    @Getter private String mysqlHostname;
    @Getter private String mysqlPort;
    @Getter private String mysqlDatabase;
    @Getter private String mysqlTable;
    @Getter private boolean registerEconomy;
    @Getter private int balanceTopUpdateInterval;

    public TMConfig(final TokenManager plugin) {
        super(plugin, "config");
    }

    @Override
    public void handleLoad() throws IOException, InvalidConfigurationException {
        super.handleLoad();

        // Detects any config created before v3.0
        if (!getConfiguration().isInt("config-version")) {
            convert(new ConfigConverter2_3());
        }

        version = getConfiguration().getInt("config-version", 0);
        defaultBalance = getConfiguration().getInt("default-balance", 25);
        openSelectedEnabled = getConfiguration().getBoolean("shop.open-selected.enabled", false);
        openSelectedShop = getConfiguration().getString("shop.open-selected.shop", "test");
        clickDelay = getConfiguration().getInt("shop.click-delay", 0);
        mysqlEnabled = getConfiguration().getBoolean("data.mysql.enabled", false);
        mysqlUsername = getConfiguration().getString("data.mysql.username", "root");
        mysqlPassword = getConfiguration().getString("data.mysql.password", "password");
        mysqlHostname = getConfiguration().getString("data.mysql.hostname", "127.0.0.1");
        mysqlPort = getConfiguration().getString("data.mysql.port", "3306");
        mysqlDatabase = getConfiguration().getString("data.mysql.database", "database");
        mysqlTable = getConfiguration().getString("data.mysql.table", "tokenmanager");
        registerEconomy = getConfiguration().getBoolean("data.register-economy", false);
        balanceTopUpdateInterval = getConfiguration().getInt("data.balance-top-update-interval", 5);
    }
}
