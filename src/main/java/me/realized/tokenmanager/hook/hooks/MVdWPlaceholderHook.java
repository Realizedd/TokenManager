package me.realized.tokenmanager.hook.hooks;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.util.hook.PluginHook;

public class MVdWPlaceholderHook extends PluginHook<TokenManagerPlugin> {

    public MVdWPlaceholderHook(final TokenManagerPlugin plugin) {
        super(plugin, "MVdWPlaceholderAPI");

        final Placeholders placeholders = new Placeholders();
        PlaceholderAPI.registerPlaceholder(plugin, "tm_tokens", placeholders);
        PlaceholderAPI.registerPlaceholder(plugin, "tm_tokens_raw", placeholders);
        PlaceholderAPI.registerPlaceholder(plugin, "tm_tokens_commas", placeholders);
        PlaceholderAPI.registerPlaceholder(plugin, "tm_tokens_formatted", placeholders);
        PlaceholderAPI.registerPlaceholder(plugin, "tm_rank", placeholders);

        for (int i = 1; i <= 10; i++) {
            PlaceholderAPI.registerPlaceholder(plugin, "tm_top_name_" + i, placeholders);
            PlaceholderAPI.registerPlaceholder(plugin, "tm_top_value_" + i, placeholders);
        }
    }

    public class Placeholders implements PlaceholderReplacer {

        @Override
        public String onPlaceholderReplace(final PlaceholderReplaceEvent event) {
            return plugin.handlePlaceholderRequest(event.getPlayer(), event.getPlaceholder().substring(3));
        }
    }
}
