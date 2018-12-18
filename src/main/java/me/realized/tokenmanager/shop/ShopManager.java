package me.realized.tokenmanager.shop;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.config.Config;
import me.realized.tokenmanager.config.Lang;
import me.realized.tokenmanager.data.DataManager;
import me.realized.tokenmanager.shop.gui.BaseGui;
import me.realized.tokenmanager.util.Loadable;
import me.realized.tokenmanager.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

public class ShopManager implements Loadable, Listener {

    private final TokenManagerPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final DataManager dataManager;

    private final Map<UUID, BaseGui> cache = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public ShopManager(final TokenManagerPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.dataManager = plugin.getDataManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() {}

    @Override
    public void handleUnload() {
        clearCache();
    }

    public void clearCache() {
        cooldowns.clear();

        if (cache.isEmpty()) {
            return;
        }

        Bukkit.getOnlinePlayers().forEach(player -> {
            final Inventory top = player.getOpenInventory().getTopInventory();
            final BaseGui gui = cache.get(player.getUniqueId());

            if (gui == null || !gui.isGui(top)) {
                return;
            }

            player.closeInventory();
            player.sendMessage(StringUtil.color("&cShop was automatically closed since the plugin is deactivating."));
        });
        cache.clear();
    }

    public void open(final Player player, final BaseGui gui) {
        cache.put(player.getUniqueId(), gui);
        gui.refresh(dataManager.get(player).orElse(0));
        gui.open(player);
    }

    public Optional<Shop> find(final Inventory inventory) {
        return cache.values().stream().filter(gui -> gui.isGui(inventory)).findFirst().map(BaseGui::getShop);
    }

    @EventHandler
    public void on(final InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        final Inventory clicked = event.getClickedInventory();
        final Inventory top = player.getOpenInventory().getTopInventory();

        if (clicked == null || top == null) {
            return;
        }

        final BaseGui gui = cache.get(player.getUniqueId());

        if (gui == null || !gui.isGui(top)) {
            return;
        }

        event.setCancelled(true);

        if (!clicked.equals(top)) {
            return;
        }

        final long now = System.currentTimeMillis();
        final long remaining = cooldowns.getOrDefault(player.getUniqueId(), 0L) + config.getClickDelay() * 1000L - now;

        if (remaining > 0) {
            plugin.doSync(player::closeInventory);
            lang.sendMessage(player, true, "ERROR.on-click-cooldown", "remaining", StringUtil.format(remaining / 1000 + (remaining % 1000 > 0 ? 1 : 0)));
            return;
        }

        if (gui.handle(player, event.getSlot())) {
            cooldowns.put(player.getUniqueId(), now);
        }
    }

    @EventHandler
    public void on(final PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
        cache.remove(event.getPlayer().getUniqueId());
    }
}
