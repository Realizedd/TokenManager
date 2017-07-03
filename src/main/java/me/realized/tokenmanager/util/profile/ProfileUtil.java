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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.regex.Pattern;

public final class ProfileUtil {

    private static final Pattern UUID_PATTERN = Pattern.compile("/^\\{?[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\u200C\u200B}?$/\n");
    private static boolean USING_SPIGOT;

    static {
        try {
            Class.forName("org.spigotmc.CustomTimingsHandler");
            USING_SPIGOT = true;
        } catch (ClassNotFoundException ignored) {}
    }

    public static boolean isUUID(final String input) {
        return UUID_PATTERN.matcher(input).matches();
    }

    public static boolean isOnlineMode() {
        final boolean online;
        return !(online = Bukkit.getOnlineMode()) && USING_SPIGOT && Bukkit.spigot().getConfig().getBoolean("settings.bungeecord") || online;
    }

    public static String getName(final String input) {
        if (!isUUID(input)) {
            return null;
        }

        final Player player;

        if ((player = Bukkit.getPlayerExact(input)) != null) {
            return player.getName();
        }

        return NameFetcher.getName(UUID.fromString(input));
    }

    public static UUID getUUID(final String name) {
        final Player player;

        if ((player = Bukkit.getPlayerExact(name)) != null) {
            return player.getUniqueId();
        }

        return UUIDFetcher.getUUID(name);
    }

    private ProfileUtil() {}
}
