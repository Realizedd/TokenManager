package me.realized.tokenmanager.hook.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.util.hook.PluginHook;
import org.bukkit.entity.Player;

public class PlaceholderHook extends PluginHook<TokenManagerPlugin> {

    public PlaceholderHook(final TokenManagerPlugin plugin) {
        super(plugin, "PlaceholderAPI");
        new Placeholders().register();
    }

    public class Placeholders extends PlaceholderExpansion {

        @Override
        public String getIdentifier() {
            return "tm";
        }

        @Override
        public String getPlugin() {
            return plugin.getName();
        }

        @Override
        public String getAuthor() {
            return "Realized";
        }

        @Override
        public String getVersion() {
            return "1.0";
        }

        @Override
        public String onPlaceholderRequest(final Player player, final String identifier) {
            return plugin.handlePlaceholderRequest(player, identifier);
        }
    }
}
