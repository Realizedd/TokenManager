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
import lombok.Getter;
import me.realized.tokenmanager.api.TokenManagerAPI;
import me.realized.tokenmanager.command.commands.TMCommand;
import me.realized.tokenmanager.command.commands.TokenCommand;
import me.realized.tokenmanager.config.Lang;
import me.realized.tokenmanager.config.TMConfig;
import me.realized.tokenmanager.data.DataManager;
import me.realized.tokenmanager.hooks.MVdWPlaceholderHook;
import me.realized.tokenmanager.hooks.PlaceholderHook;
import me.realized.tokenmanager.hooks.VaultHook;
import me.realized.tokenmanager.shop.ShopConfig;
import me.realized.tokenmanager.shop.ShopManager;
import me.realized.tokenmanager.util.plugin.Reloadable;
import me.realized.tokenmanager.util.plugin.hook.HookManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TokenManager extends JavaPlugin implements TokenManagerAPI {

    @Getter private TMConfig configuration;
    @Getter private Lang lang;
    @Getter private ShopConfig shopConfig;
    @Getter private DataManager dataManager;
    @Getter private ShopManager shopManager;
    @Getter private HookManager<TokenManager> hookManager;

    private final List<Reloadable> reloadables = new ArrayList<>();

    @Override
    public void onEnable() {
        configuration = register(new TMConfig(this));
        lang = register(new Lang(this));
        shopConfig = register(new ShopConfig(this));
        dataManager = register(new DataManager(this));
        shopManager = register(new ShopManager(this));

        if (!loadReloadables()) {
            return;
        }

        new TMCommand(this).register();
        new TokenCommand(this).register();

        hookManager = new HookManager<>(this);
        hookManager.register("MVdWPlaceholderAPI", MVdWPlaceholderHook.class);
        hookManager.register("PlaceholderAPI", PlaceholderHook.class);
        hookManager.register("Vault", VaultHook.class);
    }

    @Override
    public void onDisable() {
        unloadReloadables();
    }

    private boolean loadReloadables() {
        for (final Reloadable reloadable : reloadables) {
            try {
                reloadable.handleLoad();
            } catch (Exception ex) {
                getLogger().severe(reloadable.getClass().getSimpleName() + " has failed to load! plugin will be disabled.");
                getLogger().severe("Cause: " + ex.getMessage());
                getPluginLoader().disablePlugin(this);
                return false;
            }
        }

        return true;
    }

    private void unloadReloadables() {
        Lists.reverse(reloadables).forEach(reloadable -> {
            try {
                reloadable.handleUnload();
            } catch (Exception ex) {
                getLogger().severe(reloadable.getClass().getName() + " has failed to unload!");
                getLogger().severe("Cause: " + ex.getMessage());
            }
        });
    }

    private <R extends Reloadable> R register(final R reloadable) {
        reloadables.add(reloadable);
        return reloadable;
    }

    public boolean reload() {
        unloadReloadables();
        return loadReloadables();
    }

    @Override
    public int getTokens(final Player player) {
        return dataManager.get(player).get();
    }

    @Override
    public int getTokens(final UUID uuid) {
        return getTokens(Bukkit.getPlayer(uuid));
    }

    @Override
    public boolean setTokens(final Player player, final int amount) {
        return false;
    }

    @Override
    public boolean setTokens(final UUID uuid, final int amount) {
        return false;
    }
}
