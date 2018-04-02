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
import me.realized.tokenmanager.config.TMConfig;
import me.realized.tokenmanager.data.DataManager;
import me.realized.tokenmanager.util.Log;
import me.realized.tokenmanager.util.Reloadable;
import me.realized.tokenmanager.util.StringUtil;
import me.realized.tokenmanager.util.inventory.GUIBuilder;
import me.realized.tokenmanager.util.inventory.GUIBuilder.Pattern;
import me.realized.tokenmanager.util.inventory.InventoryUtil;
import me.realized.tokenmanager.util.inventory.ItemBuilder;
import me.realized.tokenmanager.util.inventory.ItemUtil;
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

    private static final int CONFIRM_PURCHASE_SLOT = 10;
    private static final int ITEM_SLOT = 13;
    private static final int CANCEL_PURCHASE_SLOT = 16;

    private final TokenManagerPlugin plugin;
    private final ShopConfig config;
    private final DataManager dataManager;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, ConfirmInventory> inventories = new HashMap<>();
    private Inventory confirmGuiSample;

    public ShopListener(final TokenManagerPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getShopConfig();
        this.dataManager = plugin.getDataManager();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() {
        cooldowns.clear();
        inventories.clear();

        final TMConfig config = plugin.getConfiguration();

        this.confirmGuiSample = GUIBuilder.of(StringUtil.color(config.getConfirmPurchaseTitle()), 3)
            .pattern(
                Pattern.of("AAABBBCCC", "AAABBBCCC", "AAABBBCCC")
                    .specify('A', ItemBuilder.of(Material.STAINED_GLASS_PANE, 1, (short) 13).name(" ").build())
                    .specify('B', ItemBuilder.of(Material.STAINED_GLASS_PANE, 1, (short) 7).name(" ").build())
                    .specify('C', ItemBuilder.of(Material.STAINED_GLASS_PANE, 1, (short) 14).name(" ").build()))
            .set(
                CONFIRM_PURCHASE_SLOT,
                ItemUtil.loadFromString(config.getConfirmPurchaseConfirm(), error -> Log.error(this, "Failed to load confirm-button: " + error))
            )
            .set(
                CANCEL_PURCHASE_SLOT,
                ItemUtil.loadFromString(config.getConfirmPurchaseCancel(), error -> Log.error(this, "Failed to load cancel-button: " + error))
            )
            .build();
    }

    @Override
    public void handleUnload() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            final Inventory top = player.getOpenInventory().getTopInventory();

            if (config.getShops().stream().anyMatch(shop -> shop.getGui().equals(top))) {
                player.closeInventory();
                player.sendMessage(StringUtil.color("&cShop was automatically closed since the plugin is deactivating."));
                return;
            }

            final ConfirmInventory inventory = inventories.get(player.getUniqueId());

            if (inventory != null && inventory.inventory.equals(top)) {
                player.closeInventory();
            }
        });
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
        ConfirmInventory confirmInventory = inventories.get(player.getUniqueId());

        if (confirmInventory != null && confirmInventory.inventory.equals(top)) {
            target = confirmInventory.shop;
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

        final int slot = event.getSlot();
        final Slot data;

        if (confirmGui) {
            if (slot == CANCEL_PURCHASE_SLOT) {
                player.openInventory(target.getGui());
                return;
            } else if (slot == CONFIRM_PURCHASE_SLOT) {
                data = confirmInventory.slot;
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

        if (!confirmGui && (target.isConfirmPurchase() || data.isConfirmPurchase())) {
            cooldowns.remove(player.getUniqueId());

            if (confirmInventory == null) {
                confirmInventory = new ConfirmInventory(InventoryUtil.deepCopyOf(confirmGuiSample));
                inventories.put(player.getUniqueId(), confirmInventory);
            }

            confirmInventory.update(target, data);
            player.openInventory(confirmInventory.inventory);
            return;
        }

        if (cost > 0) {
            dataManager.set(player, balance - cost);
        }

        handlePurchase(player, target, data);
    }

    @EventHandler
    public void on(final PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
        inventories.remove(event.getPlayer().getUniqueId());
    }

    private void handlePurchase(final Player player, final Shop shop, final Slot slot) {
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
            Optional<Shop> target = config.getShop(subshop);

            if (!target.isPresent()) {
                plugin.getLang().sendMessage(player, true, "ERROR.shop-not-found", "input", subshop);
                return;
            }

            player.openInventory(target.get().getGui());
            return;
        }

        if (shop.isAutoClose()) {
            plugin.doSync(player::closeInventory);
        }
    }

    private class ConfirmInventory {

        private Shop shop;
        private Slot slot;
        private final Inventory inventory;

        ConfirmInventory(final Inventory inventory) {
            this.inventory = inventory;
        }

        private void update(final Shop target, final Slot data) {
            shop = target;
            slot = data;
            inventory.setItem(CONFIRM_PURCHASE_SLOT, ItemUtil.replace(inventory.getItem(CONFIRM_PURCHASE_SLOT), "%price%", slot.getCost()));
            inventory.setItem(ITEM_SLOT, slot.getDisplayed().clone());
        }
    }
}
