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

package me.realized.tokenmanager.hook.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.data.DataManager;
import me.realized.tokenmanager.util.NumberUtil;
import me.realized.tokenmanager.util.hook.PluginHook;
import org.bukkit.entity.Player;

public class PlaceholderHook extends PluginHook<TokenManagerPlugin> {

    private final DataManager dataManager;

    public PlaceholderHook(final TokenManagerPlugin plugin) {
        super(plugin, "PlaceholderAPI");
        this.dataManager = plugin.getDataManager();
        new Placeholders().register();
    }

    public class Placeholders extends PlaceholderExpansion {

        @Override
        public String getIdentifier() {
            return "tm";
        }

        @Override
        public String getPlugin() {
            return plugin.getName();
        }

        @Override
        public String getAuthor() {
            return "Realized";
        }

        @Override
        public String getVersion() {
            return "1.0";
        }

        @Override
        public String onPlaceholderRequest(final Player player, final String identifier) {
            if (player == null) {
                return "Player is required";
            }

            final long balance = dataManager.get(player).orElse(0);

            switch (identifier) {
                case "tokens":
                    return NumberUtil.withCommas(balance);
                case "tokens_formatted":
                    return NumberUtil.withSuffix(balance);
                case "tokens_raw":
                    return String.valueOf(balance);
            }

            return null;
        }
    }
}
