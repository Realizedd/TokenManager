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

package me.realized.tokenmanager.shop;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import lombok.Getter;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.api.event.TMShopPurchaseEvent;
import me.realized.tokenmanager.shop.gui.guis.ConfirmGui;
import me.realized.tokenmanager.shop.gui.guis.ShopGui;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Slot {

    @Getter
    private final TokenManagerPlugin plugin;
    @Getter
    private final Shop shop;
    @Getter
    private final int slot;
    @Getter
    private final int cost;
    @Getter
    private final ItemStack displayed;
    @Getter
    private final String message;
    @Getter
    private final String subshop;
    @Getter
    private final List<String> commands;
    @Getter
    private final boolean usePermission;
    @Getter
    private final boolean confirmPurchase;

    public Slot(final TokenManagerPlugin plugin, final Shop shop, final int slot, final int cost, final ItemStack displayed, final String message, final String subshop,
        final List<String> commands, final boolean usePermission, final boolean confirmPurchase) {
        this.plugin = plugin;
        this.shop = shop;
        this.slot = slot;
        this.cost = cost;
        this.displayed = displayed;
        this.message = message != null ? message.replace("%price%", String.valueOf(cost)) : null;
        this.subshop = subshop;
        this.commands = commands;
        this.usePermission = usePermission;
        this.confirmPurchase = confirmPurchase;
        commands.replaceAll(command -> command = command.replace("%price%", String.valueOf(cost)));
    }

    public boolean purchase(final Player player, final boolean confirmPurchase, final boolean close) {
        final OptionalLong cached = plugin.getDataManager().get(player);

        if (!cached.isPresent()) {
            plugin.doSync(player::closeInventory);
            plugin.getLang().sendMessage(player, false, "&cYour data is improperly loaded, please re-log.");
            return false;
        }

        long balance = cached.getAsLong();

        if (balance - cost < 0) {
            plugin.doSync(player::closeInventory);
            plugin.getLang().sendMessage(player, true, "ERROR.balance-not-enough", "needed", cost - balance);
            return false;
        }

        final TMShopPurchaseEvent event = new TMShopPurchaseEvent(player, cost, shop, this);
        plugin.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        if (cost > 0) {
            // If confirm-purchase is true for shop or slot
            if (confirmPurchase) {
                plugin.getShopManager().open(player, new ConfirmGui(plugin, shop, slot));
                return false;
            }

            plugin.getDataManager().set(player, balance = balance - cost);
        }

        if (commands != null) {
            for (final String command : commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), replace(command.replace("%player%", player.getName()), balance));
            }
        }

        if (message != null && !message.isEmpty()) {
            plugin.getLang().sendMessage(player, false, replace(message, balance), "player", player.getName());
        }

        if (subshop != null && !subshop.isEmpty()) {
            final Optional<Shop> result = plugin.getShopConfig().getShop(subshop);

            if (!result.isPresent()) {
                plugin.getLang().sendMessage(player, true, "ERROR.shop-not-found", "input", subshop);
                return true;
            }

            final Shop target = result.get();

            if (target.isUsePermission() && !player.hasPermission("tokenmanager.use.shop." + target.getName())) {
                plugin.getLang().sendMessage(player, true, "ERROR.no-permission", "permission", "tokenmanager.use.shop." + target.getName());
                return true;
            }

            plugin.getShopManager().open(player, new ShopGui(plugin, target));
            return true;
        }

        if (shop.isAutoClose() || close) {
            plugin.doSync(player::closeInventory);
        }

        return true;
    }

    private String replace(String s, final long balance) {
        return s.replace("%balance%", String.valueOf(balance)).replace("%tokens%", String.valueOf(balance));
    }
}
