package me.realized.tm.management;

import me.realized.tm.Core;
import me.realized.tm.utilities.ProfileUtil;
import me.realized.tm.utilities.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {

    private final Core instance;
    private final boolean sql;
    private final ConcurrentHashMap<UUID, Long> data;

    private File dataFile;
    private FileConfiguration config;
    private Connection connection = null;

    private List<String> topSaved;
    private long lastUpdate;

    public DataManager(Core instance, boolean sql) {
        this.instance = instance;
        this.sql = sql;
        data = new ConcurrentHashMap<>();
        topSaved = new ArrayList<>();
        lastUpdate = -1L;
    }

    public boolean load() {
        long start = System.currentTimeMillis();
        instance.info("Loading datas...");

        if (sql) {
            FileConfiguration config = instance.getConfig();
            String path = "mysql.";
            String hostname = config.getString(path + "hostname");
            String port = config.getString(path + "port");
            String database = config.getString(path + "database");
            String username = config.getString(path + "username");
            String password = config.getString(path + "password");

            try {
                connection = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database, username, password);
                Statement statement = connection.createStatement();
                statement.execute("CREATE TABLE IF NOT EXISTS tokenmanager (uuid varchar(36) NOT NULL, tokens bigint(255) NOT NULL, PRIMARY KEY (uuid)) ENGINE=InnoDB DEFAULT CHARSET=latin1");

                ResultSet results = statement.executeQuery("SELECT * FROM tokenmanager");

                while (results.next()) {
                    String uuid = results.getString("uuid");

                    if (uuid.split("-").length != 5) {
                        continue;
                    }

                    long balance = results.getLong("tokens");
                    data.put(UUID.fromString(uuid), balance);
                }

                long end = System.currentTimeMillis();

                instance.info("Loaded " + data.size() + " balances from the database, took " + (end - start) + "ms.");
                return true;
            } catch (SQLException e) {
                instance.warn("A SQL error caught while trying to connect to the database. Error:" + e.getMessage());
                instance.warn("Connection to the database has failed, disabling...");
                return false;
            }
        } else {
            dataFile = new File(instance.getDataFolder(), "data.yml");

            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                instance.warn("An IO exception caught while trying to load from flatfile. (data.yml) Error: " + e.getMessage());
                instance.warn("Flatfile data load was failed, disabling...");
                return false;
            }

            config = YamlConfiguration.loadConfiguration(dataFile);

            if (config.isConfigurationSection("Players")) {
                for (String key : config.getConfigurationSection("Players").getKeys(false)) {
                    UUID uuid = UUID.fromString(key);
                    long balance = config.getLong("Players." + key);
                    data.put(uuid, balance);
                }
            }

            long end = System.currentTimeMillis();

            instance.info("Loaded " + data.size() + " balances from the flatfile storage, took " + (end - start) + "ms.");
            return true;
        }
    }

    public void save(boolean silent) {
        long start = System.currentTimeMillis();

        if (!silent) {
            instance.info("Saving datas...");
        }

        if (!sql) {
            try {
                config.set("Players", null);

                if (!data.isEmpty()) {
                    for (UUID key : data.keySet()) {
                        config.set("Players." + key.toString(), data.get(key));
                    }
                }

                config.save(dataFile);
            } catch (IOException e) {
                instance.warn("An IO exception caught while trying to save to flatfile. (data.yml) Error: " + e.getMessage());
                return;
            }
        } else {
            if (!Bukkit.getOnlinePlayers().isEmpty()) {
                for (Player all : Bukkit.getOnlinePlayers()) {
                    if (hasLoadedData(all.getUniqueId())) {
                        saveData(all.getUniqueId(), false);
                    }
                }
            }
        }

        long end = System.currentTimeMillis();

        if (!silent) {
            instance.info("Saved " + data.size() + " balances to the database, took " + (end - start) + "ms.");
        }
    }

    public void initializeAutoSave() {
        if (!sql) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(instance, new Runnable() {
                @Override
                public void run() {
                    save(true);
                }
            }, 0L, 20L * 60L * 5);
        }
    }

    public void loadTopAutomatically() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(instance, new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();

                if (lastUpdate == -1L) {
                    lastUpdate = System.currentTimeMillis();
                }

                if (now - lastUpdate < 900000) {
                    List<String> datas = new ArrayList<>();

                    for (Map.Entry<UUID, Long> entry : data.entrySet()) {
                        datas.add(entry.getKey() + ":" + entry.getValue());
                    }

                    Collections.sort(datas, new Comparator<String>() {
                        @Override
                        public int compare(String s2, String s1) {
                            return Long.valueOf(s1.split(":")[1]).compareTo(Long.valueOf(s2.split(":")[1]));
                        }
                    });

                    topSaved.clear();

                    for (int i = 0; i < 10; i++) {
                        if (i < 0 || i >= datas.size()) {
                            break;
                        }

                        String[] data = datas.get(i).split(":");

                        if (data.length == 0) {
                            continue;
                        }

                        String name = ProfileUtil.getName(UUID.fromString(data[0]));
                        topSaved.add(String.valueOf(i + 1) + ":" + name + ":" + data[1]);
                    }

                    lastUpdate = System.currentTimeMillis();
                }
            }
        }, 0L, 20L * 60L * 15);
    }

    public void loadData(final UUID uuid) {
        if (sql) {
            if (!isConnected()) {
                instance.warn("SQL was enabled in config, but connection failed to the database! Failed to load " + uuid + "'s data.");
                instance.warn("Disabling due to no connection to the database. Please disable SQL in the config or establish the connection.");
                instance.getPluginLoader().disablePlugin(instance);
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(instance, new Runnable() {
                @Override
                public void run() {
                    try {
                        Statement statement = connection.createStatement();
                        ResultSet result = statement.executeQuery("SELECT * FROM tokenmanager WHERE uuid=\"" + uuid.toString() + "\"");

                        if (!result.isBeforeFirst()) {
                            statement.execute("INSERT INTO tokenmanager (uuid, tokens) VALUES (\"" + uuid.toString() + "\", " + instance.getTMConfig().getDefaultBalance() + ")");
                            data.put(uuid, instance.getTMConfig().getDefaultBalance());
                            return;
                        }

                        while (result.next()) {
                            long balance = result.getLong("tokens");
                            data.put(uuid, balance);
                        }
                    } catch (SQLException e) {
                        instance.warn("SQL was enabled in config, but connection failed to the database! Failed to load " + uuid + "'s data.");
                        instance.warn("A SQL error caught while executing SQL query. Error:" + e.getMessage());
                    }
                }
            });
        } else {
            if (data.get(uuid) == null) {
                data.put(uuid, instance.getTMConfig().getDefaultBalance());
            }
        }
    }

    public void saveData(final UUID uuid, boolean async) {
        final long balance = balance(uuid);

        if (sql) {
            if (!isConnected()) {
                instance.warn("SQL was enabled in config, but connection failed to the database! Failed to save " + uuid + "'s data.");
                instance.warn("Disabling due to no connection to the database. Please disable SQL in the config or establish the connection.");
                instance.getPluginLoader().disablePlugin(instance);
                return;
            }

            if (async) {
                Bukkit.getScheduler().runTaskAsynchronously(instance, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Statement statement = connection.createStatement();
                            statement.execute("UPDATE tokenmanager SET tokens=" + balance + " WHERE uuid=\"" + uuid.toString() + "\"");
                        } catch (SQLException e) {
                            instance.warn("SQL was enabled in config, but connection failed to the database! Failed to save " + uuid + "'s data.");
                            instance.warn("A SQL error caught while executing SQL query. Error:" + e.getMessage());
                        }
                    }
                });

                data.remove(uuid);
            } else {
                try {
                    Statement statement = connection.createStatement();
                    statement.execute("UPDATE tokenmanager SET tokens=" + balance + " WHERE uuid=\"" + uuid.toString() + "\"");
                } catch (SQLException e) {
                    instance.warn("SQL was enabled in config, but connection failed to the database! Failed to save " + uuid + "'s data.");
                    instance.warn("A SQL error caught while executing SQL query. Error:" + e.getMessage());
                }
            }
        } else {
            save(true);
        }
    }

    public boolean hasLoadedData(UUID uuid) {
        return data.get(uuid) != null;
    }

    // Might have a better option.
    private boolean isConnected() {
        try {
            return connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }

    public long balance(UUID uuid) {
        return data.get(uuid) != null ? data.get(uuid) : 0;
    }

    public void add(UUID uuid, long amount) {
        if (amount < 0) {
            return;
        }

        if (data.get(uuid) != null) {
            long current = data.get(uuid);
            data.put(uuid, current + amount);
        }
    }

    public void remove(UUID uuid, long amount) {
        if (amount < 0) {
            return;
        }

        if (data.get(uuid) != null) {
            long current = data.get(uuid);

            if (current - amount >= 0) {
                data.put(uuid, current - amount);
            }
        }
    }

    public void set(UUID uuid, long amount) {
        if (amount < 0) {
            return;
        }

        data.put(uuid, amount);
    }

    public int size() {
        return data.size();
    }

    public List<String> getTopBalances() {
        return topSaved;
    }

    public String getNextUpdate() {
        return StringUtil.format((lastUpdate + 900000 - System.currentTimeMillis()) / 1000);
    }
}
