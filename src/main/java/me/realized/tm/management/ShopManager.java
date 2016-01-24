package me.realized.tm.management;

import me.realized.tm.Core;
import me.realized.tm.configuration.TMConfig;
import me.realized.tm.utilities.ItemUtil;
import me.realized.tm.utilities.StringUtil;
import me.realized.tm.utilities.TMShop;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class ShopManager {

    private final Core instance;
    private final DataManager dataManager;
    private final TMConfig config;
    private final List<TMShop> shops = new ArrayList<>();
    private final Map<UUID, Long> clicks = new WeakHashMap<>();

    public ShopManager(Core instance) {
        this.instance = instance;
        this.dataManager = instance.getDataManager();
        this.config = instance.getTMConfig();
    }

    public void load() {
        shops.clear();
        clicks.clear();

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

            if (!config.isString(path + "title")) {
                instance.warn("Failed to load shop '" + key + "': The title of the shop GUi was invalid.");
                continue;
            }

            String title = StringUtil.color(config.getString(path + "title"));
            int size = 18;

            if (config.isInt(path + "rows")) {
                size = config.getInt(path + "rows") * 9;

                if (size > 54 || size < 1) {
                    instance.warn("Failed to load shop '" + key + "': The size of the shop GUI must be a multiply of 9.");
                    size = 18;
                }
            } else {
                instance.warn("Failed to load shop '" + key + ": The size of the shop GUI was invalid, using 9x2 by default.");
            }

            boolean close = false;

            if (config.isBoolean(path + "auto-close")) {
                close = config.getBoolean(path + "auto-close");
            } else {
                instance.warn("Failed to load shop '" + key + ": The value of auto-close option is invalid, using 'false' by default.");
            }

            boolean permission = false;

            if (config.isBoolean(path + "use-permission")) {
                permission = config.getBoolean(path + "use-permission");
            } else {
                instance.warn("Failed to load shop '" + key + ": The value of use-permission option is invalid, using 'false' by default.");
            }

            TMShop shop = new TMShop(name, title, size, close, permission);

            if (!config.isConfigurationSection(path + "items")) {
                instance.info("No displayed items found for shop '" + key + "', moving on to the next section.");
                continue;
            }

            for (String k : config.getConfigurationSection(path + "items").getKeys(false)) {
                path = "shops." + key + "." + "items." + k + ".";

                int slot;

                try {
                    slot = Integer.parseInt(k);

                    if (slot > shop.get().getSize() || slot < 0) {
                        instance.warn("Failed to load slot '" + k + "' for shop '" + key + "': Invalid slot number.");
                        continue;
                    }
                } catch (NumberFormatException e) {
                    instance.warn("Failed to load slot '" + k + "' for shop '" + key + "': Invalid slot number.");
                    continue;
                }

                ItemStack item;

                if (!config.isString(path + "displayed")) {
                    instance.warn("Failed to load slot '" + k + "' for shop '" + key + "': No displayed item found.");
                    continue;
                }

                item = ItemUtil.toItemStack(config.getString(path + "displayed"));

                if (item == null) {
                    instance.warn("Failed to load slot '" + k + "' for shop '" + key + "': Item is null, see logs for more.");
                    continue;
                }

                int cost = 0;

                if (config.isInt(path + "cost")) {
                    cost = config.getInt(path + "cost");
                } else {
                    instance.warn("Failed to load slot '" + k + "' for shop '" + key + "': Cost for slot was invalid, using 0 by default.");
                }

                String message = null;

                if (config.isString(path + "message")) {
                    message = config.getString(path + "message");
                }

                String subshop = null;

                if (config.isString(path + "subshop")) {
                    subshop = config.getString(path + "subshop");
                }

                List<String> commands = new ArrayList<>();

                if (config.isList(path + "commands") && !config.getStringList(path + "commands").isEmpty()) {
                    commands = config.getStringList(path + "commands");
                }

                shop.setItem(slot, item, cost, commands);

                if (message != null) {
                    shop.setMessage(slot, message);
                }

                if (subshop != null) {
                    shop.setSubShop(slot, subshop);
                }
            }

            shops.add(shop);
        }

        instance.info("Loaded " + shops.size() + " shops.");
    }

    public void close() {
        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            for (Player all : Bukkit.getOnlinePlayers()) {
                Inventory top = all.getOpenInventory().getTopInventory();

                if (top != null && isShop(top.getTitle())) {
                    all.closeInventory();
                    all.sendMessage(ChatColor.RED + "[TM] All shops are automatically closed on reload.");
                }
            }
        }
    }

    public boolean isShop(String input) {
        for (TMShop shop : shops) {
            if (shop.getTitle().equals(input) || shop.getName().equals(input)) {
                return true;
            }
        }
        return false;
    }

    public TMShop getShop(String input) {
        for (TMShop shop : shops) {
            if (shop.getTitle().equals(input) || shop.getName().equals(input)) {
                return shop;
            }
        }
        return null;
    }

    public List<TMShop> getShops() {
        return shops;
    }

    public void handleClick(InventoryClickEvent event) {
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

        int delay = config.getClickDelay();
        long now = System.currentTimeMillis();

        if (delay != 0 && clicks.get(player.getUniqueId()) != null) {
            long lastClick = clicks.get(player.getUniqueId());
            long remaining = lastClick + delay * 1000 - now;

            if (remaining > 0) {
                pm(player, config.getString("click-spamming").replace("%remaining%", StringUtil.format(remaining / 1000 + (remaining % 1000 > 0 ? 1 : 0))));
                return;
            } else {
                clicks.remove(player.getUniqueId());
            }

            if (clicks.get(player.getUniqueId()) == null) {
                clicks.put(player.getUniqueId(), now);
            }
        }

        int slot = event.getSlot();
        TMShop shop = getShop(top.getTitle());
        int price = shop.getPrice(slot);
        int balance = dataManager.balance(player.getUniqueId());

        if (balance - price < 0) {
            pm(player, config.getString("not-enough-tokens").replace("%needed%", String.valueOf(price - balance)));
            return;
        }

        if (price > 0) {
            boolean success = dataManager.remove(player.getUniqueId(), price);

            if (!success) {
                pm(player, "&cOperation (remove) failed, please contact an administrator.");
                return;
            }
        }

        if (!shop.getCommands(slot).isEmpty()) {
            for (String command : shop.getCommands(slot)) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
            }
        }

        if (shop.isAutoCloseEnabled()) {
            player.closeInventory();
        }

        if (shop.getMessage(slot) != null) {
            pm(player, shop.getMessage(slot).replace("%player%", player.getName()));
        }

        if (shop.getSubShop(slot) != null) {
            String name = shop.getSubShop(slot);

            if (!isShop(name)) {
                pm(player, config.getString("invalid-shop").replace("%input%", name));
                return;
            }

            TMShop subShop = getShop(name);

            if (subShop.hasPermission() && !player.hasPermission("tokenmanager.use.shop." + name)) {
                pm(player, config.getString("no-permission").replace("%permission%", "tokenmanager.use.shop." + name));
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
}
