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

package me.realized.tokenmanager.command;

import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.config.Config;
import me.realized.tokenmanager.config.WorthConfig;
import me.realized.tokenmanager.data.DataManager;
import me.realized.tokenmanager.shop.ShopConfig;
import me.realized.tokenmanager.shop.ShopManager;
import me.realized.tokenmanager.util.command.AbstractCommand;
import me.realized.tokenmanager.util.profile.ProfileUtil;
import org.bukkit.command.CommandSender;

public abstract class BaseCommand extends AbstractCommand<TokenManagerPlugin> {

    protected final Config config;
    protected final ShopConfig shopConfig;
    protected final ShopManager shopManager;
    protected final DataManager dataManager;
    protected final WorthConfig worthConfig;
    protected final boolean online;

    public BaseCommand(final TokenManagerPlugin plugin, final String name, final String permission, final boolean playerOnly) {
        this(plugin, name, null, permission, 0, playerOnly);
    }

    public BaseCommand(final TokenManagerPlugin plugin, final String name, final String usage, final String permission, final int length,
        final boolean playerOnly, final String... aliases) {
        super(plugin, name, usage, permission, length, playerOnly, aliases);
        this.config = plugin.getConfiguration();
        this.shopConfig = plugin.getShopConfig();
        this.shopManager = plugin.getShopManager();
        this.dataManager = plugin.getDataManager();
        this.worthConfig = plugin.getWorthConfig();

        final String mode = config.getOnlineMode();
        this.online = mode.equals("auto") ? ProfileUtil.isOnlineMode() : mode.equals("true");
    }

    protected void sendMessage(final CommandSender receiver, final boolean config, final String in, final Object... replacers) {
        plugin.getLang().sendMessage(receiver, config, in, replacers);
    }

    @Override
    protected void handleMessage(final CommandSender sender, final MessageType type, final String... args) {
        switch (type) {
            case PLAYER_ONLY:
                sendMessage(sender, false, "&cThis command can only be executed by a player!");
                break;
            case NO_PERMISSION:
                sendMessage(sender, true, "ERROR.no-permission", "permission", args[0]);
                break;
            case SUB_COMMAND_INVALID:
                sendMessage(sender, true, "ERROR.invalid-sub-command", "command", args[0], "input", args[1]);
                break;
            case SUB_COMMAND_USAGE:
                sendMessage(sender, true, "COMMAND.sub-command-usage", "command", args[0], "usage", args[1]);
                break;
        }
    }
}
