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

package me.realized.tokenmanager.command.commands.subcommands;

import me.realized.tokenmanager.TokenManager;
import me.realized.tokenmanager.command.BaseCommand;
import me.realized.tokenmanager.shop.Shop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class ShopCommand extends BaseCommand {

    public ShopCommand(final TokenManager plugin) {
        super(plugin, "shop", "shop <name>", "tokenmanager.use.shop", plugin.getConfiguration().isOpenSelectedEnabled() ? 1 : 2, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        final String target;
        final Shop shop;

        if (getConfig().isOpenSelectedEnabled()) {
            target = getConfig().getOpenSelectedShop();

            if ((shop = getShopConfig().getShop(target)) == null) {
                sendMessage(sender, true, "invalid-shop", "input", target);
                return;
            }

            player.openInventory(shop.getGui());
            return;
        }

        target = args[1].toLowerCase();

        if ((shop = getShopConfig().getShop(target)) == null) {
            sendMessage(player, true, "invalid-shop", "input", target);
            return;
        }

        if (shop.isUsePermission() && !player.hasPermission("tokenmanager.use.shop." + target)) {
            sendMessage(player, true, "no-permission", "permission", "tokenmanager.use.shop." + target);
            return;
        }

        player.openInventory(shop.getGui());
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 2 && !getConfig().isOpenSelectedEnabled()) {
            // Collects the names of registered shops for tab completion.
            return getShopConfig().getShops().stream().map(Shop::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .sorted(String::compareTo)
                    .collect(Collectors.toList());
        }

        return null;
    }
}
