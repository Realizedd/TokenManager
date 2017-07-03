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

import me.realized.tokenmanager.api.exception.UserNotFoundException;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * The API interface for TokenManager.
 */
public interface TokenManagerAPI {


    /**
     * Gets the token balance of the player in cache.
     *
     * @param   player the player to get the token balance.
     * @return  token balance of the player.
     * @throws  UserNotFoundException if the player was not found in the cache.
     */
    int getTokens(final Player player);


    /**
     * Gets the token balance of the player with uuid in cache.
     *
     * @param   uuid the uuid of the player to get the token balance.
     * @return  token balance of the player with the uuid.
     * @throws  UserNotFoundException if the player was not found in the cache.
     */
    int getTokens(final UUID uuid);


    /**
     *
     * Sets the token balance of the player with the given amount.
     *
     * @param   player the player to set the token balance.
     * @param   amount the amount for the new token balance.
     * @return  true if token balance was set successfully.
     */
    boolean setTokens(final Player player, final int amount);


    /**
     *
     * Sets the token balance of the player with uuid with the given amount.
     *
     * @param   uuid the uuid of the player to set the token balance.
     * @param   amount the amount for the new token balance.
     * @return  true if token balance was set successfully.
     */
    boolean setTokens(final UUID uuid, final int amount);

}
