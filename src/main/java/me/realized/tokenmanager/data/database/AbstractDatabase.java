package me.realized.tokenmanager.data.database;

import java.util.List;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.util.profile.ProfileUtil;
import org.bukkit.entity.Player;

public abstract class AbstractDatabase implements Database {

    protected final TokenManagerPlugin plugin;
    final boolean online;

    AbstractDatabase(final TokenManagerPlugin plugin) {
        this.plugin = plugin;

        final String mode = plugin.getConfiguration().getOnlineMode().toLowerCase();
        this.online = mode.equals("auto") ? ProfileUtil.isOnlineMode() : mode.equals("true");
    }

    @Override
    public boolean isOnlineMode() {
        return online;
    }

    OptionalLong from(final Long value) {
        return value != null ? OptionalLong.of(value) : OptionalLong.empty();
    }

    String from(final Player player) {
        return online ? player.getUniqueId().toString() : player.getName();
    }

    void replaceNames(final List<TopElement> list, final Consumer<List<TopElement>> callback) {
        if (online) {
            ProfileUtil.getNames(list.stream().map(element -> UUID.fromString(element.getKey())).collect(Collectors.toList()), result -> {
                for (final TopElement element : list) {
                    final String name = result.get(UUID.fromString(element.getKey()));

                    if (name == null) {
                        element.setKey("&cFailed to get name!");
                        continue;
                    }

                    element.setKey(name);
                }

                callback.accept(list);
            });
        } else {
            callback.accept(list);
        }
    }
}
