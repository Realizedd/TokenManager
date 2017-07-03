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

import me.clip.placeholderapi.external.EZPlaceholderHook;
import me.realized.tokenmanager.TokenManager;
import me.realized.tokenmanager.data.DataManager;
import me.realized.tokenmanager.util.plugin.hook.PluginHook;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Class created at 6/26/17 by Realized
 **/

public class PlaceholderHook extends PluginHook<TokenManager> {

    private final DataManager dataManager;

    public PlaceholderHook(final TokenManager plugin, final Plugin target) {
        super(plugin, target, "PlaceholderAPI");
        this.dataManager = plugin.getDataManager();

        new EZPlaceholderHook(plugin, "tm") {
            @Override
            public String onPlaceholderRequest(final Player player, final String identifier) {
                if (!identifier.equals("tokens")) {
                    return "";
                }

                if (player == null) {
                    return "Player is required to use this placeholder!";
                }

                return String.valueOf(dataManager.get(player).orElse(0));
            }
        }.hook();
    }
}
