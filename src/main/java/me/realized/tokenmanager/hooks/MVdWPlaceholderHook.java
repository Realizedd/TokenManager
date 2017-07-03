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

package me.realized.tokenmanager.hooks;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import me.realized.tokenmanager.TokenManager;
import me.realized.tokenmanager.data.DataManager;
import me.realized.tokenmanager.util.plugin.hook.PluginHook;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class MVdWPlaceholderHook extends PluginHook<TokenManager> implements PlaceholderReplacer {

    private final DataManager dataManager;

    public MVdWPlaceholderHook(final TokenManager plugin, final Plugin target) {
        super(plugin, target, "MVdWPlaceholderAPI");
        this.dataManager = plugin.getDataManager();

        PlaceholderAPI.registerPlaceholder(plugin, "tm_tokens", this);
    }

    @Override
    public String onPlaceholderReplace(PlaceholderReplaceEvent event) {
        final Player player = event.getPlayer();

        if (player == null) {
            return "Player is required to use this placeholder!";
        }

        return String.valueOf(dataManager.get(player).orElse(0));
    }
}
