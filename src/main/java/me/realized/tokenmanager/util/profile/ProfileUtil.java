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

package me.realized.tokenmanager.util.profile;

import java.util.UUID;
import java.util.regex.Pattern;
import me.realized.tokenmanager.util.Callback;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class ProfileUtil {

    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}");
    private static boolean USING_SPIGOT;

    static {
        try {
            Class.forName("org.spigotmc.CustomTimingsHandler");
            USING_SPIGOT = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    private ProfileUtil() {
    }

    public static boolean isUUID(final String input) {
        return UUID_PATTERN.matcher(input).matches();
    }

    public static boolean isOnlineMode() {
        final boolean online;
        return !(online = Bukkit.getOnlineMode()) && USING_SPIGOT && Bukkit.spigot().getConfig().getBoolean("settings.bungeecord")
            || online;
    }

    public static String getName(final String input) {
        if (!isUUID(input)) {
            return null;
        }

        final Player player;
        final UUID uuid;

        if ((player = Bukkit.getPlayer(uuid = UUID.fromString(input))) != null) {
            return player.getName();
        }

        return NameFetcher.getName(uuid);
    }

    public static void getUUIDString(final String name, final Callback<String> callback) {
        final Player player;

        if ((player = Bukkit.getPlayerExact(name)) != null) {
            callback.call(player.getUniqueId().toString());
            return;
        }

        callback.call(UUIDFetcher.getUUID(name));
    }
}
