package me.realized.tokenmanager.hook;

import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.hook.hooks.MVdWPlaceholderHook;
import me.realized.tokenmanager.hook.hooks.PlaceholderHook;
import me.realized.tokenmanager.hook.hooks.VaultHook;
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
