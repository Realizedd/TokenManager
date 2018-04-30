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
import me.realized.tokenmanager.util.Loadable;
import me.realized.tokenmanager.util.StringUtil;
import me.realized.tokenmanager.util.inventory.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ShopListener implements Loadable, Listener {

    private final TokenManagerPlugin plugin;
    private final ShopsConfig config;
    private final DataManager dataManager;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public ShopListener(final TokenManagerPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getShopConfig();
        this.dataManager = plugin.getDataManager();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() {}

    @Override
    public void handleUnload() {
        cooldowns.clear();
    }

    @EventHandler
    public void on(final InventoryClickEvent event) {
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

        boolean confirmGui = false;
        Shop target = null;
        ConfirmInventory confirmInventory = config.getInventories().get(player.getUniqueId());

        if (confirmInventory != null && confirmInventory.getInventory().equals(top)) {
            target = confirmInventory.getShop();
            confirmGui = true;
        } else {
            for (final Shop shop : config.getShops()) {
                if (shop.getGui().equals(top)) {
                    target = shop;
                    break;
                }
            }
        }

        if (target == null) {
            return;
        }

        event.setCancelled(true);

        if (!clicked.equals(top)) {
            return;
        }

        boolean close = false;
        final int slot = event.getSlot();
        final Slot data;

        if (confirmGui) {
            if (slot == ConfirmInventory.CANCEL_PURCHASE_SLOT) {
                player.openInventory(target.getGui());
                return;
            } else if (slot == ConfirmInventory.CONFIRM_PURCHASE_SLOT) {
                data = confirmInventory.getSlot();
                close = true;
            } else {
                data = null;
            }
        } else {
            data = target.getSlot(slot);
        }

        if (data == null) {
            return;
        }

        if (data.isUsePermission() && !player.hasPermission("tokenmanager.use." + target.getName() + "-" + slot)) {
            plugin.getLang().sendMessage(player, true, "ERROR.no-permission", "permission", "tokenmanager.use." + target.getName() + "-" + slot);
            return;
        }

        final long now = System.currentTimeMillis();
        final long remaining = cooldowns.getOrDefault(player.getUniqueId(), 0L) + plugin.getConfiguration().getClickDelay() * 1000L - now;

        if (remaining > 0) {
            plugin.getLang().sendMessage(player, true, "ERROR.on-click-cooldown", "remaining", StringUtil.format(remaining / 1000 + (remaining % 1000 > 0 ? 1 : 0)));
            return;
        }

        cooldowns.put(player.getUniqueId(), now);

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
            if (!confirmGui && (target.isConfirmPurchase() || data.isConfirmPurchase())) {
                cooldowns.remove(player.getUniqueId());

                if (confirmInventory == null) {
                    confirmInventory = new ConfirmInventory(InventoryUtil.deepCopyOf(config.getConfirmGuiSample()));
                    config.getInventories().put(player.getUniqueId(), confirmInventory);
                }

                confirmInventory.update(target, data);
                player.openInventory(confirmInventory.getInventory());
                return;
            }

            dataManager.set(player, balance - cost);
        }

        handlePurchase(player, target, data, close);
    }

    @EventHandler
    public void on(final PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
        config.getInventories().remove(event.getPlayer().getUniqueId());
    }

    private void handlePurchase(final Player player, final Shop shop, final Slot slot, final boolean close) {
        final List<String> commands = slot.getCommands();

        if (commands != null) {
            for (final String command : commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
            }
        }

        final String message, subshop;

        if ((message = slot.getMessage()) != null && !message.isEmpty()) {
            plugin.getLang().sendMessage(player, false, message, "player", player.getName());
        }

        if ((subshop = slot.getSubshop()) != null && !subshop.isEmpty()) {
            final Optional<Shop> result = config.getShop(subshop);

            if (!result.isPresent()) {
                plugin.getLang().sendMessage(player, true, "ERROR.shop-not-found", "input", subshop);
                return;
            }

            final Shop target = result.get();

            if (target.isUsePermission() && !player.hasPermission("tokenmanager.use.shop." + target.getName())) {
                plugin.getLang().sendMessage(player, true, "ERROR.no-permission", "permission", "tokenmanager.use.shop." + target.getName());
                return;
            }

            player.openInventory(target.getGui());
            return;
        }

        if (shop.isAutoClose() || close) {
            plugin.doSync(player::closeInventory);
        }
    }
}
