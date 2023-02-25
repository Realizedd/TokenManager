package me.realized.tokenmanager.data.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.command.commands.subcommands.OfflineCommand.ModifyType;
import me.realized.tokenmanager.config.Config;
import me.realized.tokenmanager.util.Log;
import me.realized.tokenmanager.util.compat.CompatUtil;
import me.realized.tokenmanager.util.profile.ProfileUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class FileDatabase extends AbstractDatabase {

    private static final String SERVER_MODE_MISMATCH = "Server is in %s mode, but data.yml does not contain %s! Please delete or rename data.yml to generate a new data.yml with valid format.";

    private final File file;
    private final Map<String, Long> data = new HashMap<>();

    private FileConfiguration config;

    public FileDatabase(final TokenManagerPlugin plugin) throws IOException {
        super(plugin);
        this.file = new File(plugin.getDataFolder(), "data.yml");

        if (!file.exists()) {
            file.createNewFile();
        }
    }

    @Override
    public void setup() throws Exception {
        this.config = YamlConfiguration.loadConfiguration(file);
        final ConfigurationSection section = config.getConfigurationSection("Players");

        if (section != null) {
            for (final String key : section.getKeys(false)) {
                if (ProfileUtil.isUUID(key) != online) {
                    // clear to prevent saving previously loaded data overwriting the file
                    data.clear();
                    throw new Exception(String.format(SERVER_MODE_MISMATCH, online ? "ONLINE" : "OFFLINE", online ? "UUIDs" : "usernames"));
                }

                data.put(key, section.getLong(key));
            }
        }
    }

    @Override
    public OptionalLong get(final Player player) {
        return from(data.get(from(player)));
    }

    @Override
    public void get(final String key, final Consumer<OptionalLong> onLoad, final Consumer<String> onError, final boolean create) {
        final OptionalLong cached = from(data.get(key));

        if (!cached.isPresent() && create) {
            final long defaultBalance = plugin.getConfiguration().getDefaultBalance();
            data.put(key, defaultBalance);

            if (onLoad != null) {
                onLoad.accept(from(defaultBalance));
            }
            return;
        }

        if (onLoad != null) {
            onLoad.accept(cached);
        }
    }

    @Override
    public void set(final Player player, final long value) {
        data.put(from(player), value);
    }

    @Override
    public void set(final String key, final ModifyType type, final long amount, final long balance, final boolean silent, final Runnable onDone, final Consumer<String> onError) {
        plugin.doSync(() -> {
            if (type == ModifyType.SET) {
                data.put(key, amount);
                return;
            }

            final OptionalLong cached = from(data.get(key));

            if (!cached.isPresent()) {
                return;
            }

            data.put(key, type.apply(cached.getAsLong(), amount));

            final Player player;

            if (ProfileUtil.isUUID(key)) {
                player = Bukkit.getPlayer(UUID.fromString(key));
            } else {
                player = Bukkit.getPlayerExact(key);
            }

            if (player == null || silent) {
                return;
            }

            plugin.getLang().sendMessage(player, true, "COMMAND." + (type == ModifyType.ADD ? "add" : "remove"), "amount", amount);
        });

        if (onDone != null) {
            onDone.run();
        }
    }

    @Override
    public void load(final Player player, final Function<Long, Long> modifyLoad) {
        plugin.doSync(() -> get(online ? player.getUniqueId().toString() : player.getName(), null, null, true));
    }

    @Override
    public void load(final Player player) {}

    @Override
    public void save(final Player player) {}

    @Override
    public void shutdown() throws IOException {
        if (data.isEmpty()) {
            return;
        }

        Log.info("Saving data to file...");
        data.forEach((key, value) -> config.set("Players." + key, value));
        config.save(file);
        Log.info("Save complete.");
    }

    @Override
    public void ordered(final int limit, final Consumer<List<TopElement>> consumer) {
        final List<TopElement> elements = new ArrayList<>();

        if (limit <= 0 || data.isEmpty()) {
            consumer.accept(elements);
            return;
        }

        data.forEach((key, value) -> {
            elements.add(new TopElement(key, value));
            config.set("Players." + key, value);
        });
        plugin.doAsync(() -> {
            elements.sort(Comparator.comparingLong(TopElement::getTokens).reversed());
            final List<TopElement> result = elements.size() > limit ? elements.subList(0, 10) : elements;
            replaceNames(result, consumer);

            try {
                config.save(file);
            } catch (IOException ex) {
                Log.error("Failed to save data: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    @Override
    public void transfer(final CommandSender sender, final Consumer<String> errorHandler) {
        final Config config = plugin.getConfiguration();
        final String query = String
            .format("SELECT %s, tokens FROM %s;", online ? "uuid" : "name", StringEscapeUtils.escapeSql(plugin.getConfiguration().getMysqlTable()));
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + config.getMysqlHostname() + ":" + config.getMysqlPort() + "/" + config.getMysqlDatabase());
        hikariConfig.setDriverClassName("com.mysql." + (CompatUtil.isPre1_17() ? "jdbc" : "cj") + ".Driver");
        hikariConfig.setUsername(config.getMysqlUsername());
        hikariConfig.setPassword(config.getMysqlPassword());
        hikariConfig.setMaximumPoolSize(1);

        plugin.doAsync(() -> {
            sender.sendMessage(ChatColor.BLUE + plugin.getDescription().getFullName() + ": Loading user data from MySQL database...");

            try (
                HikariDataSource dataSource = new HikariDataSource(hikariConfig);
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()
            ) {
                sender.sendMessage(ChatColor.BLUE + plugin.getDescription().getFullName() + ": Load Complete. Starting the transfer...");

                final File file = new File(plugin.getDataFolder(), "sqldump-" + System.currentTimeMillis() + ".yml");
                file.createNewFile();

                final FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
                int count = 0;

                while (resultSet.next()) {
                    final String key = online ? resultSet.getString("uuid") : resultSet.getString("name");
                    configuration.set("Players." + key, resultSet.getLong("tokens"));
                    count++;
                }

                configuration.save(file);
                sender.sendMessage(ChatColor.BLUE + plugin.getDescription().getFullName() + ": SQL Data saved to " + file.getName() + ". Total Transferred Rows: " + count);
            } catch (Exception ex) {
                errorHandler.accept(ex.getMessage());
                Log.error("Failed to transfer data from MySQL database: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }
}
