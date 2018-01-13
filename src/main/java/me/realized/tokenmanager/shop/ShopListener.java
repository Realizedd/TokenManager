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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.data.DataManager;
import me.realized.tokenmanager.util.Reloadable;
import me.realized.tokenmanager.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ShopListener implements Reloadable, Listener {

    private final TokenManagerPlugin plugin;
    private final ShopConfig config;
    private final DataManager dataManager;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public ShopListener(final TokenManagerPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getShopConfig();
        this.dataManager = plugin.getDataManager();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() {
        cooldowns.clear();
    }

    @Override
    public void handleUnload() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            final Inventory top = player.getOpenInventory().getTopInventory();

            if (config.getShops().stream().anyMatch(shop -> shop.getGui().equals(top))) {
                player.closeInventory();
                player.sendMessage(StringUtil.color("&cTokenManager: Shops are automatically closed when the plugin is disabling."));
            }
        });
    }

    @EventHandler
    public void on(InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        final Inventory clicked = event.getClickedInventory();
        final Inventory top = player.getOpenInventory().getTopInventory();

        if (clicked == null || top == null) {
            return;
        }

        final ItemStack item = event.getCurrentItem();

        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        Shop target = null;

        for (final Shop shop : config.getShops()) {
            if (shop.getGui().equals(top)) {
                target = shop;
                break;
            }
        }

        if (target == null) {
            return;
        }

        event.setCancelled(true);

        if (!clicked.equals(top)) {
            return;
        }

        final long now = System.currentTimeMillis();
        final long remaining = cooldowns.getOrDefault(player.getUniqueId(), 0L) + plugin.getConfiguration().getClickDelay() - now;

        if (remaining > 0) {
            plugin.getLang().sendMessage(player, true, "ERROR.on-click-cooldown", "remaining",
                StringUtil.format(remaining / 1000 + (remaining % 1000 > 0 ? 1 : 0)));
            return;
        }

        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());

        final int slot;
        final Slot data;

        if ((data = target.getSlot(slot = event.getSlot())) == null) {
            return;
        }

        if (data.isUsePermission() && !player.hasPermission("tokenmanager.use." + target.getName() + "-" + slot)) {
            plugin.getLang()
                .sendMessage(player, true, "ERROR.no-permission", "permission", "tokenmanager.use." + target.getName() + "-" + slot);
            return;
        }

        final int cost = data.getCost();
        final OptionalLong cached = dataManager.get(player);

        if (!cached.isPresent()) {
            plugin.getLang().sendMessage(player, false, "&cYour data is improperly loaded, please re-log.");
            return;
        }

        final long balance = cached.getAsLong();

        if (balance - cost < 0) {
            plugin.getLang().sendMessage(player, true, "ERROR.balance-not-enough", "needed", cost - balance);
            return;
        }

        if (cost > 0) {
            dataManager.set(player, balance - cost);
        }

        final List<String> commands;

        if ((commands = data.getCommands()) != null) {
            for (final String command : commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("player", player.getName()));
            }
        }

        if (target.isAutoClose()) {
            player.closeInventory();
        }

        final String message, subshop;

        if ((message = data.getMessage()) != null) {
            plugin.getLang().sendMessage(player, false, message, "player", player.getName());
        }

        if ((subshop = data.getSubshop()) != null) {
            Optional<Shop> shop = config.getShop(subshop);

            if (!shop.isPresent()) {
                plugin.getLang().sendMessage(player, true, "ERROR.shop-not-found", "input", subshop);
                return;
            }

            player.openInventory(shop.get().getGui());
        }
    }

    @EventHandler
    public void on(final PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
    }
}
