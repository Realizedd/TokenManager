package me.realized.tokenmanager.data.database;

import java.util.List;
import java.util.OptionalLong;
import java.util.function.Consumer;
import org.bukkit.entity.Player;

public interface Database {

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


    /**
     * Gets the stored data of the player from the database.
     *
     * @param player Player to get the data
     * @param consumer called once data is retrieved
     * @param errorHandler called if the operation fails
     */
    void get(final Player player, final Consumer<OptionalLong> consumer, final Consumer<String> errorHandler);


    /**
     * Gets the stored data of the player.
     *
     * @param key UUID or the name the player
     * @param consumer called once data is retrieved
     * @param errorHandler called if the operation fails
     * @param create true to create with default balance if not exists, false for no action
     */
    void get(final String key, final Consumer<OptionalLong> consumer, final Consumer<String> errorHandler, final boolean create);


    /**
     * Sets the cached data value for player.
     *
     * @param player Player to set the data
     * @param value Value to be set in the cache
     */
    void set(final Player player, final long value);


    /**
     * Updates the database with the new balance for the key.
     *
     * @param key Key associated with the balance
     * @param set true to set the balance to updated value, otherwise false
     * @param amount The difference between the old balance and the new balance
     * @param updated The new balance to save
     * @param errorHandler called if the operation fails
     */
    void set(final String key, final boolean set, final long amount, final long updated, final Runnable action, final Consumer<String> errorHandler);


    /**
     * Saves the cached data associated to key and clears it from cache. Must be called synchronously!
     *
     * @param player Player to save the balance
     */
    void save(final Player player);

    /**
     * Saves the online player data synchronously.
     *
     * @throws Exception if insertion to the database fails
     */
    void save() throws Exception;


    /**
     * Saves the balance of online players and returns top balances. Must be called synchronously!
     *
     * @param limit amount of the rows to be returned
     * @param consumer Consumer to call once data is retrieved
     */
    void ordered(final int limit, final Consumer<List<TopElement>> consumer);


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
