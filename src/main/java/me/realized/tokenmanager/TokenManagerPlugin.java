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
import lombok.Getter;
import me.realized.tokenmanager.api.TokenManager;
import me.realized.tokenmanager.command.commands.TMCommand;
import me.realized.tokenmanager.command.commands.TokenCommand;
import me.realized.tokenmanager.config.Lang;
import me.realized.tokenmanager.config.TMConfig;
import me.realized.tokenmanager.data.DataManager;
import me.realized.tokenmanager.hooks.HookManager;
import me.realized.tokenmanager.shop.Shop;
import me.realized.tokenmanager.shop.ShopConfig;
import me.realized.tokenmanager.shop.ShopManager;
import me.realized.tokenmanager.util.Reloadable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public class TokenManagerPlugin extends JavaPlugin implements TokenManager {

    private final List<Reloadable> reloadables = new ArrayList<>();
    private int lastLoad;

    @Getter
    private TMConfig configuration;
    @Getter
    private Lang lang;
    @Getter
    private ShopConfig shopConfig;
    @Getter
    private DataManager dataManager;
    @Getter
    private ShopManager shopManager;
    @Getter
    private HookManager hookManager;

    @Override
    public void onEnable() {
        configuration = register(new TMConfig(this));
        lang = register(new Lang(this));
        shopConfig = register(new ShopConfig(this));
        dataManager = register(new DataManager(this));
        shopManager = register(new ShopManager(this));
        hookManager = register(new HookManager(this));

        if (!loadReloadables()) {
            getPluginLoader().disablePlugin(this);
            return;
        }

        new TMCommand(this).register();
        new TokenCommand(this).register();
    }

    @Override
    public void onDisable() {
        unloadReloadables();
    }

    private <R extends Reloadable> R register(final R reloadable) {
        reloadables.add(reloadable);
        return reloadable;
    }

    /**
     * @return true if load was successful, otherwise false
     */
    private boolean loadReloadables() {
        for (final Reloadable reloadable : reloadables) {
            try {
                reloadable.handleLoad();
                lastLoad = reloadables.indexOf(reloadable);
                info("Loaded " + reloadable.getClass().getSimpleName() + ".");
            } catch (Exception ex) {
                error("An error occured while loading " + reloadable.getClass().getSimpleName() + ", please contact the developer.");
                ex.printStackTrace();
                return false;
            }
        }

        return true;
    }

    /**
     * @return true if unload was successful, otherwise false
     */
    private boolean unloadReloadables() {
        for (final Reloadable reloadable : Lists.reverse(reloadables)) {
            try {
                if (reloadables.indexOf(reloadable) > lastLoad) {
                    continue;
                }

                reloadable.handleUnload();
                info("Unloaded " + reloadable.getClass().getSimpleName() + ".");
            } catch (Exception ex) {
                error("An error occured while unloading " + reloadable.getClass().getSimpleName() + ", please contact the developer.");
                ex.printStackTrace();
                return false;
            }
        }

        return true;
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
    public void info(final String msg) {
        getLogger().info(msg);
    }

    public void info(final Reloadable reloadable, final String msg) {
        info(reloadable.getClass().getSimpleName() + ": " + msg);
    }

    @Override
    public void error(final String msg) {
        Bukkit.getConsoleSender().sendMessage("[" + getName() + "] " + ChatColor.RED + msg);
    }

    public void error(final Reloadable reloadable, final String msg) {
        error(reloadable.getClass().getSimpleName() + ": " + msg);
    }

    @Override
    public boolean reload() {
        if (!(unloadReloadables() && loadReloadables())) {
            getPluginLoader().disablePlugin(this);
            return false;
        }

        return true;
    }
}
