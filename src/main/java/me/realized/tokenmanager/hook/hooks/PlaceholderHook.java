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
        public String getRequiredPlugin() {
            return plugin.getName();
        }

        @Override
        public boolean canRegister() {
            return true;
        }

        @Override
        public String getAuthor() {
            return plugin.getDescription().getAuthors().get(0);
        }

        @Override
        public String getVersion() {
            return plugin.getDescription().getVersion();
        }

        @Override
        public boolean persist() {
            return true;
        }

        @Override
        public String onPlaceholderRequest(final Player player, final String identifier) {
            return plugin.handlePlaceholderRequest(player, identifier);
        }
    }
}
