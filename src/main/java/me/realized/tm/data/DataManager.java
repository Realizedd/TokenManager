package me.realized.tm.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import me.realized.tm.Core;
import me.realized.tm.configuration.Config;
import me.realized.tm.events.TokenReceiveEvent;
import me.realized.tm.utilities.StringUtil;
import me.realized.tm.utilities.profile.ProfileUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.*;

public class DataManager implements Listener {

    private final Core instance;
    private final Config config;
    private final boolean sql;

    private File file;
    private FileConfiguration dataConfig;
    private Map<UUID, Integer> data = new ConcurrentHashMap<>();

    private HikariDataSource dataSource;
    private boolean connected;

    private final List<String> top = new ArrayList<>();
    private long lastUpdate;

    private List<BukkitTask> tasks = new ArrayList<>();

    public DataManager(Core instance) {
        this.instance = instance;
        this.config = instance.getConfiguration();
        this.sql = config.isSQLEnabled();
        Bukkit.getPluginManager().registerEvents(this, instance);
    }

    public boolean load() {
        instance.info("Data Storage: " + (sql ? "MySQL" : "Flatfile"));

        if (sql) {
            boolean success = connect();

            if (success) {
                instance.info("Successfully connected to the database.");
                return true;
            } else {
                instance.warn("Connection failed.");
                return false;
            }
        } else {
            data.clear();
            file = new File(instance.getDataFolder(), "data.yml");

            try {
                boolean generated = file.createNewFile();

                if (generated) {
                    instance.info("Generated data file!");
                }
            } catch (IOException e) {
                instance.warn("Error caught while generating file! (" + e.getMessage() + ")");
                return false;
            }

            dataConfig = YamlConfiguration.loadConfiguration(file);

            if (dataConfig.isConfigurationSection("Players")) {
                for (String key : dataConfig.getConfigurationSection("Players").getKeys(false)) {
                    UUID uuid = UUID.fromString(key);
                    int amount = dataConfig.getInt("Players." + key);
                    data.put(uuid, amount);
                }
            }

            instance.info("Loaded from flatfile storage!");
            return true;
        }
    }

    private boolean connect() {
        String host = (String) config.getValue("mysql.hostname");
        String port = (String) config.getValue("mysql.port");
        String database = (String) config.getValue("mysql.database");
        String user = (String) config.getValue("mysql.username");
        String password = (String) config.getValue("mysql.password");
        int maxPoolSize = (int) config.getValue("mysql.maxPoolSize");
        int idleTimeOut = (int) config.getValue("mysql.idleTimeOut");
        int connectionTimeOut = (int) config.getValue("mysql.connectionTimeOut");
        instance.info("Loaded credentials for SQL database.");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(password);
        hikariConfig.setMaximumPoolSize(maxPoolSize);
        hikariConfig.setIdleTimeout(idleTimeOut);
        hikariConfig.setConnectionTimeout(connectionTimeOut);

        try {
            dataSource = new HikariDataSource(hikariConfig);
            validateConnection(true);

            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
                statement.execute(Action.TABLE.query());
            }

            return true;
        } catch (HikariPool.PoolInitializationException | SQLException e) {
            validateConnection(false);
            instance.warn("Error caught while connecting to the database! (" + e.getMessage() + ")");
            return false;
        }
    }

    public void close() {
        Bukkit.getScheduler().cancelTasks(instance);

        if (sql) {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
            }
        } else {
            saveLocalData();
        }
    }

    public void reloadableMethods() {
        for (BukkitTask task : tasks) {
            task.cancel();
        }

        loadTopBalances();
        initializeAutoSave();
    }

    private void loadTopBalances() {
        if (sql && !connected) {
            return;
        }

        tasks.add(Bukkit.getScheduler().runTaskTimerAsynchronously(instance, new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();

                if (now - (lastUpdate != 0L ? lastUpdate : now) < 900000) {
                    top.clear();

                    List<String> data = getData(sql);

                    if (data.isEmpty()) {
                        top.add("&cNo data found.");
                        lastUpdate = now;
                        return;
                    }

                    Collections.sort(data, new Comparator<String>() {
                        @Override
                        public int compare(String s1, String s2) {
                            return Long.valueOf(s2.split(":")[1]).compareTo(Long.valueOf(s1.split(":")[1]));
                        }
                    });

                    List<UUID> uuids = new ArrayList<>();
                    List<String> extra = new ArrayList<>();

                    for (int i = 0; i < 10; i++) {
                        if (i < 0 || i >= data.size()) {
                            break;
                        }

                        String[] split = data.get(i).split(":");
                        uuids.add(UUID.fromString(split[0]));
                        extra.add(String.valueOf(i + 1) + ":" + split[1]);
                    }

                    List<String> names = ProfileUtil.getNames(uuids, sql);

                    for (int i = 0; i < names.size(); i++) {
                        top.add(extra.get(i) + ":" + names.get(i));
                    }

                    lastUpdate = System.currentTimeMillis();
                }
            }
        }, 0L, 20L * 60L * (int) config.getValue("update-balance-top")));
    }

    public void checkOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            executeAction(Action.CREATE, player.getUniqueId(), 0);
        }
    }

    private void initializeAutoSave() {
        if (sql) {
            return;
        }

        tasks.add(Bukkit.getScheduler().runTaskTimerAsynchronously(instance, new Runnable() {
            @Override
            public void run() {
                instance.info("Auto-saving...");
                saveLocalData();
            }
        }, 0L, 20L * 60L * (int) config.getValue("auto-save")));
    }

    public void checkConnection() {
        if (!sql) {
            return;
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(instance, new Runnable() {
            @Override
            public void run() {
                if (connected) {
                    return;
                }

                instance.info("Attempting to reconnect to the database...");
                boolean success = connect();

                if (success) {
                    instance.info("Successfully connected to the database.");
                } else {
                    instance.warn("Connection failed.");
                }
            }
        }, 0L, 20L * 60L * 3);
    }

    private List<String> getData(boolean sql) {
        if (sql) {
            ExecutorService executor = Executors.newFixedThreadPool(1);
            Future<List<String>> future = executor.submit(new Callable<List<String>>() {
                @Override
                public List<String> call() throws Exception {
                    List<String> result = new ArrayList<>();

                    try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement(); ResultSet results = statement.executeQuery(Action.TOP.query())) {
                        while (results.next()) {
                            result.add(results.getString("uuid") + ":" + results.getLong("tokens"));
                        }

                    } catch (SQLException e) {
                        validateConnection(false);
                        instance.warn("SQL error caught while executing SQL query! (" + e.getMessage() + ")");
                    }

                    return result;
                }
            });

            executor.shutdown();

            try {
                return future.get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                return new ArrayList<>();
            }

        } else {
            List<String> result = new ArrayList<>();

            if (!data.isEmpty()) {
                for (Map.Entry<UUID, Integer> entry : data.entrySet()) {
                    result.add(entry.getKey() + ":" + entry.getValue());
                }
            }

            return result;
        }
    }

    private boolean saveLocalData() {
        if (!data.isEmpty()) {
            for (UUID key : data.keySet()) {
                dataConfig.set("Players." + key, data.get(key));
            }
        }

        try {
            dataConfig.save(file);
            instance.info("Saving to local file was completed.");
            return true;
        } catch (IOException e) {
            instance.warn("Error caught while saving file! (" + e.getMessage() + ")");
            return false;
        }
    }

    public boolean hasSQLEnabled() {
        return sql;
    }

    public boolean isConnected() {
        return connected;
    }

    private void validateConnection(boolean connected) {
        this.connected = connected;
    }

    private boolean create(UUID uuid) {
        if (sql) {
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(Action.GET.query("{0}", uuid))) {
                if (result.isBeforeFirst()) {
                    return false;
                }

                statement.execute(Action.CREATE.query("{0}", uuid, "{1}", config.getValue("default-balance")));
                return true;
            } catch (SQLException e) {
                validateConnection(false);
                instance.warn("SQL error caught while executing SQL query! (" + e.getMessage() + ")");
                return false;
            }
        } else {
            if (data.get(uuid) != null) {
                return false;
            }

            data.put(uuid, ((int) config.getValue("default-balance")));
            return true;
        }
    }

    private int balance(UUID uuid) {
        if (sql) {
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(Action.GET.query("{0}", uuid))) {
                if (!result.isBeforeFirst()) {
                    return 0;
                }

                int balance = 0;

                while (result.next()) {
                    balance = (int) result.getLong("tokens");
                }

                return balance;
            } catch (SQLException e) {
                validateConnection(false);
                instance.warn("SQL error caught while executing SQL query! (" + e.getMessage() + ")");
                return 0;
            }
        } else {
            return data.get(uuid) != null ? data.get(uuid) : 0;
        }
    }

    private boolean exists(UUID uuid) {
        if (sql) {
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(Action.GET.query("{0}", uuid))) {
                return result.isBeforeFirst();
            } catch (SQLException e) {
                validateConnection(false);
                instance.warn("SQL error caught while executing SQL query! (" + e.getMessage() + ")");
                return false;
            }
        } else {
            return data.get(uuid) != null;
        }
    }

    private boolean set(UUID uuid, int amount) {
        if (sql) {
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
                statement.execute(Action.SET.query("{0}", uuid, "{1}", amount));
                return true;
            } catch (SQLException e) {
                validateConnection(false);
                instance.warn("SQL error caught while executing SQL query! (" + e.getMessage() + ")");
                return false;
            }
        } else {
            data.put(uuid, amount);
            return true;
        }
    }

    private boolean add(UUID uuid, int amount) {
        return set(uuid, balance(uuid) + amount);
    }

    private boolean remove(UUID uuid, int amount) {
        return set(uuid, Math.abs(balance(uuid) - amount));
    }

    private Object callByAction(final Action action, final UUID target, final int amount) {
        switch (action) {
            case CREATE:
                return create(target);
            case BALANCE:
                return balance(target);
            case EXISTS:
                return exists(target);
            case SET:
                return set(target, amount);
            case ADD:
                return add(target, amount);
            case REMOVE:
                return remove(target, amount);
        }

        return null;
    }

    public Object executeAction(final Action action, final UUID target, int amount) {
        if (action == Action.ADD) {
            TokenReceiveEvent event = new TokenReceiveEvent(target, amount);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return true;
            }

            amount = event.getAmount();
        }

        if (sql) {
            final int actualAmount = amount;
            ExecutorService executor = Executors.newFixedThreadPool(1);
            Future<Object> future = executor.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    return callByAction(action, target, actualAmount);
                }
            });

            executor.shutdown();

            try {
                return future.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                instance.warn("Error caught while executing action " + action + "! (" + e.getMessage() + ")");
                return action == Action.BALANCE ? 0 : false;
            }
        } else {
            return callByAction(action, target, amount);
        }
    }

    public List<String> getTopBalances() {
        return top;
    }

    public String getNextUpdate() {
        return StringUtil.format((lastUpdate + 1000L * 60L * (int) config.getValue("update-balance-top") - System.currentTimeMillis()) / 1000);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        executeAction(Action.CREATE, event.getPlayer().getUniqueId(), 0);
    }
}
