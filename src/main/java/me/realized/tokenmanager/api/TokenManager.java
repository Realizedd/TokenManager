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

package me.realized.tokenmanager.api;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import me.realized.tokenmanager.shop.Shop;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public interface TokenManager {

    /**
     * Get a shop by name.
     *
     * @param name Shop name
     * @return Optional containing the shop instance if found, otherwise empty
     */
    Optional<Shop> getShop(final String name);

    /**
     * Get a shop by inventory.
     *
     * @param inventory inventory to look up for matching shop instance
     * @return Optional containing the shop instance if found, otherwise empty
     */
    Optional<Shop> getShop(final Inventory inventory);

    /**
     * Get online player's token balance.
     *
     * @param player Player to get token balance
     * @return OptionalLong containing token balance if found, otherwise empty
     */
    OptionalLong getTokens(final Player player);

    /**
     * Set online player's token balance.
     *
     * @param player Player to set token balance
     * @param amount Amount to replace player's token balance
     */
    void setTokens(final Player player, final long amount);

    /**
     * Set player's token balance.
     * @since v3.1.0
     *
     * @param key {@link UUID#toString()} if server is in online mode, otherwise name of the player
     * @param amount Amount to replace player's token balance
     */
    void setTokens(final String key, final long amount);

    /**
     * Add tokens to player's token balance.
     * @since v3.1.0
     *
     * @param key {@link UUID#toString()} if server is in online mode, otherwise name of the player
     * @param amount Amount to add to player's token balance
     * @param silent true to prevent sending message if target player is online
     */
    void addTokens(final String key, final long amount, final boolean silent);

    /**
     * Works the same as {@link #addTokens(String, long, boolean)} with silent defaulting to false.
     *
     * @see #addTokens(String, long, boolean)
     */
    void addTokens(final String key, final long amount);

    /**
     * Remove tokens from player's token balance.
     * @since v3.1.0
     *
     * @param key {@link UUID#toString()} if server is in online mode, otherwise name of the player
     * @param amount Amount to remove from player's token balance
     * @param silent true to prevent sending message if target player is online
     */
    void removeTokens(final String key, final long amount, final boolean silent);

    /**
     * Works the same as {@link #removeTokens(String, long, boolean)} with silent defaulting to false.
     *
     * @see #removeTokens(String, long, boolean)
     */
    void removeTokens(final String key, final long amount);

    /**
     * Reload the modules of the plugin.
     *
     * @return true if reload was successful, otherwise false
     */
    boolean reload();
}
