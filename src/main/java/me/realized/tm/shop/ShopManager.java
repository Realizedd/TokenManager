package me.realized.tm.shop;

import me.realized.tm.Core;
import me.realized.tm.configuration.Config;
import me.realized.tm.configuration.Lang;
import me.realized.tm.data.Action;
import me.realized.tm.data.DataManager;
import me.realized.tm.utilities.ItemUtil;
import me.realized.tm.utilities.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class ShopManager implements Listener {

    private final Core instance;
    private final Config config;
    private final Lang lang;
    private final DataManager manager;
    private final List<Shop> shops = new ArrayList<>();
    private final Map<UUID, Long> clicks = new WeakHashMap<>();

    public ShopManager(Core instance) {
        this.instance = instance;
        this.config = instance.getConfiguration();
        this.lang = instance.getLang();
        this.manager = instance.getDataManager();
        Bukkit.getPluginManager().registerEvents(this, instance);
    }

    public void load() {
        shops.clear();

        File file = new File(instance.getDataFolder(), "shops.yml");

        if (!file.exists()) {
            instance.saveResource("shops.yml", true);
            instance.info("Successfully generated file 'shops.yml'.");
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (!config.isConfigurationSection("shops")) {
            instance.info("No shops were found in the shop config, skipping load.");
            return;
        }

        for (String key : config.getConfigurationSection("shops").getKeys(false)) {
            String name = key.toLowerCase();
            String path = "shops." + key + ".";
            final String warnShop = "Error occurred while trying to load shop '" + name + "': ";

            if (!config.isString(path + "title")) {
                instance.warn(warnShop + "The title of the shop was invalid.");
                continue;
            }

            String title = StringUtil.color(config.getString(path + "title"));
            int size;

            if (!config.isInt(path + "rows")) {
                instance.warn(warnShop + "The size of the shop was invalid, using size of 18 by default.");
                size = 18;
            } else {
                size = config.getInt(path + "rows") * 9;

                if (size > 54 || size < 1) {
                    instance.warn(warnShop + "The row of the shop must be in between range 1 - 6. Using size of 18 by default.");
                    size = 18;
                }
            }

            boolean autoClose = false;

            if (config.isBoolean(path + "auto-close")) {
                autoClose = config.getBoolean(path + "auto-close");
            } else {
                instance.warn(warnShop + "The value of 'auto-close' option is invalid, using 'false' by default.");
            }

            boolean permission = false;

            if (config.isBoolean(path + "use-permission")) {
                permission = config.getBoolean(path + "use-permission");
            } else {
                instance.warn(warnShop + "The value of 'use-permission' option is invalid, using 'false' by default.");
            }

            Shop shop = new Shop(name, title, size, autoClose, permission);

            if (config.isConfigurationSection(path + "items")) {
                for (String nextKey : config.getConfigurationSection(path + "items").getKeys(false)) {
                    final String warnSlot = "Error occurred while trying to load slot '" + nextKey + "' for shop '" + name + "': ";
                    path = "shops." + key + "." + "items." + nextKey + ".";

                    int slot;

                    try {
                        slot = Integer.parseInt(nextKey);

                        if (slot < 0 || slot > shop.get().getSize() - 1) {
                            instance.warn(warnSlot + "Invalid slot number.");
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        instance.warn(warnSlot + "Invalid slot number.");
                        continue;
                    }

                    ItemStack item;

                    if (!config.isString(path + "displayed")) {
                        instance.warn(warnSlot + "No displayed item found.");
                        continue;
                    }

                    item = ItemUtil.toItemStack(config.getString(path + "displayed"));

                    if (item == null) {
                        instance.warn(warnSlot + "Item is invalid, see previous logs for more information.");
                        continue;
                    }

                    int cost = 10000;

                    if (config.isInt(path + "cost")) {
                        cost = config.getInt(path + "cost");
                    } else {
                        instance.warn(warnSlot + "Cost for slot was invalid, using 10000 by default.");
                    }

                    String message = null;

                    if (config.isString(path + "message")) {
                        message = config.getString(path + "message");
                    }

                    String subShop = null;

                    if (config.isString(path + "subshop")) {
                        subShop = config.getString(path + "subshop");
                    }

                    List<String> commands = new ArrayList<>();

                    if (config.isList(path + "commands") && !config.getStringList(path + "commands").isEmpty()) {
                        commands = config.getStringList(path + "commands");
                    }

                    shop.setItem(slot, item, cost, commands, message, subShop);
                }
            }

            shops.add(shop);
        }

        instance.info("Loaded " + shops.size() + " shops.");
    }

    public void close() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Inventory top = player.getOpenInventory().getTopInventory();

            if (top != null && isShop(top.getTitle())) {
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "[TokenManager] All shops are automatically closed on reload.");
            }
        }
    }

    public boolean isShop(String input) {
        for (Shop shop : shops) {
            if (shop.getTitle().equals(input) || shop.getName().equals(input)) {
                return true;
            }
        }
        return false;
    }

    public Shop getShop(String input) {
        for (Shop shop : shops) {
            if (shop.getTitle().equals(input) || shop.getName().equals(input)) {
                return shop;
            }
        }
        return null;
    }

    public List<Shop> getShops() {
        return shops;
    }

    private void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clicked = event.getClickedInventory();
        Inventory top = player.getOpenInventory().getTopInventory();

        if (clicked == null || top == null) {
            return;
        }

        ItemStack item = event.getCurrentItem();

        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        if (!isShop(top.getTitle())) {
            return;
        }

        event.setCancelled(true);

        if (!isShop(clicked.getTitle())) {
            return;
        }

        int delay = (int) config.getValue("click-delay");
        long now = System.currentTimeMillis();

        if (delay > 0 && clicks.get(player.getUniqueId()) != null) {
            long lastClick = clicks.get(player.getUniqueId());
            long remaining = lastClick + delay * 1000 - now;

            if (remaining > 0) {
                pm(player, lang.getString("click-spamming").replace("%remaining%", StringUtil.format(remaining / 1000 + (remaining % 1000 > 0 ? 1 : 0))));
                return;
            } else {
                clicks.put(player.getUniqueId(), now);
            }
        }

        int slot = event.getSlot();
        Shop shop = getShop(top.getTitle());
        SlotData data = shop.getSlot(slot);

        if (data == null) {
            return;
        }

        int cost = data.getCost();
        int balance = (int) manager.executeAction(Action.BALANCE, player.getUniqueId(), 0);

        if (balance - cost < 0) {
            pm(player, lang.getString("not-enough-tokens").replace("%needed%", String.valueOf(cost - balance)));
            return;
        }

        if (cost > 0) {
            boolean success = (boolean) manager.executeAction(Action.REMOVE, player.getUniqueId(), cost);

            if (!success) {
                pm(player, "&cFailed to remove " + cost + " token(s) from your balance, please contact an administrator.");
                return;
            }
        }

        for (String command : data.getCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
        }

        if (shop.isAutoCloseEnabled()) {
            player.closeInventory();
        }

        if (data.hasMessage()) {
            pm(player, data.getMessage().replace("%player%", player.getName()));
        }

        if (data.hasSubShop()) {
            String name = data.getSubShop();

            if (!isShop(name)) {
                pm(player, lang.getString("invalid-shop").replace("%input%", name));
                return;
            }

            Shop subShop = getShop(name);

            if (subShop.hasPermission() && !player.hasPermission("tokenmanager.use.shop." + name)) {
                pm(player, lang.getString("no-permission").replace("%permission%", "tokenmanager.use.shop." + name));
                return;
            }

            player.openInventory(subShop.get());
        }

        if (clicks.get(player.getUniqueId()) == null) {
            clicks.put(player.getUniqueId(), now);
        }
    }

    private void pm(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        handleClick(event);
    }
}
