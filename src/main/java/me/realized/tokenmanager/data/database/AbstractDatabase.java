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

import java.util.List;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.function.Consumer;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.util.profile.NameFetcher;
import me.realized.tokenmanager.util.profile.ProfileUtil;

public abstract class AbstractDatabase implements Database {

    protected final TokenManagerPlugin plugin;
    final boolean online;

    AbstractDatabase(final TokenManagerPlugin plugin) {
        this.plugin = plugin;
        this.online = ProfileUtil.isOnlineMode();
    }

    OptionalLong from(final Long value) {
        return value != null ? OptionalLong.of(value) : OptionalLong.empty();
    }

    void checkNames(final List<UUID> uuids, final List<TopElement> result, final Consumer<List<TopElement>> callback) {
        if (online) {
            NameFetcher.getNames(uuids, names -> {
                for (final TopElement element : result) {
                    final String name = names.get(UUID.fromString(element.getKey()));

                    if (name == null) {
                        element.setKey("&cFailed to get name!");
                        continue;
                    }

                    element.setKey(name);
                }

                callback.accept(result);
            });
        } else {
            callback.accept(result);
        }
    }
}
