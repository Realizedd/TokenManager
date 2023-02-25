package me.realized.tokenmanager.data.database;

import java.util.List;
import java.util.OptionalLong;
import java.util.function.Consumer;
import java.util.function.Function;
import me.realized.tokenmanager.command.commands.subcommands.OfflineCommand.ModifyType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface Database {

    boolean isOnlineMode();

    /**
     * Checks and creates the table for the plugin if it does not exist.
     *
     * @throws Exception If the found table does not have the key column matching with the server mode (UUID for online/name for offline)
     */
    void setup() throws Exception;

    /**
     * Gets the cached balance of the player.
     *
     * @param player Player to get the data
     * @return instance of {@link OptionalLong} with the player's token balance if found, otherwise empty
     */
    OptionalLong get(final Player player);

    void get(final String key, final Consumer<OptionalLong> onLoad, final Consumer<String> onError, final boolean create);

    void set(final Player player, final long value);

    void set(final String key, final ModifyType type, final long amount, final long balance, final boolean silent, final Runnable onDone, final Consumer<String> onError);

    void load(final Player player, final Function<Long, Long> modifyLoad);

    void load(final Player player);

    void save(final Player player);

    void shutdown() throws Exception;

    /**
     * Returns top balances. Must be called synchronously!
     *
     * @param limit amount of the rows to be returned
     * @param onLoad Consumer to call once data is retrieved
     */
    void ordered(final int limit, final Consumer<List<TopElement>> onLoad);

    void transfer(final CommandSender sender, final Consumer<String> onError);

    class TopElement {

        private final long tokens;
        private String key;

        TopElement(final String key, final long tokens) {
            this.key = key;
            this.tokens = tokens;
        }

        public String getKey() {
            return key;
        }

        void setKey(final String key) {
            this.key = key;
        }

        public long getTokens() {
            return tokens;
        }
    }
}
