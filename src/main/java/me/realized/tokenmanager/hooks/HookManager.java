package me.realized.tokenmanager.hooks;

import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.util.hook.AbstractHookManager;

public class HookManager extends AbstractHookManager<TokenManagerPlugin> {

    public HookManager(final TokenManagerPlugin plugin) {
        super(plugin);
    }

    @Override
    public void handleLoad() {
        register("MVdWPlaceholderAPI", MVdWPlaceholderHook.class);
        register("PlaceholderAPI", PlaceholderHook.class);
        register("Vault", VaultHook.class);
    }

    @Override
    public void handleUnload() {}
}
