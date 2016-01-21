package me.realized.tm.management;

import me.realized.tm.Core;
import me.realized.tm.utilities.ProfileUtil;
import me.realized.tm.utilities.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

public class DataManager {

    private final Core instance;
    private boolean sql = false;

    private File file;
    private FileConfiguration config;
    private Connection connection = null;
    private boolean connected;

    private final List<String> topBalances = new ArrayList<>();
    private long lastUpdate = -1L;

    public DataManager(Core instance) {
        this.instance = instance;
    }

    public boolean load() {
        long start = System.currentTimeMillis();

        FileConfiguration localConfig = instance.getConfig();
        String path = "mysql.";

        if (localConfig.isBoolean(path + "enabled")) {
            sql = localConfig.getBoolean(path + "enabled");
        }

        instance.info("Data Storage: " + (sql ? "MySQL" : "Flatfile"));

        if (sql) {
            String host = localConfig.getString(path + "hostname");
            String port = localConfig.getString(path + "port");
            String database = localConfig.getString(path + "database");
            String user = localConfig.getString(path + "username");
            String password = localConfig.getString(path + "password");
            instance.info("Loaded credentials for SQL connection.");

            try {
                connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, user, password);
                validateConnection(true);

                try (Statement statement = connection.createStatement()) {
                    statement.execute(Queries.TABLE.query());
                    instance.info("Table check done.");
                }

                long end = System.currentTimeMillis();
                instance.info("Connection to the database was successful! (Took " + (end - start) + "ms)");
                return true;
            } catch (SQLException e) {
                validateConnection(false);
                instance.warn("SQL error caught while executing query! (" + e.getMessage() + ")");
                return false;
            }
        } else {
            file = new File(instance.getDataFolder(), "data.yml");

            try {
                file.createNewFile();
                instance.info("Generated local data file!");
            } catch (IOException e) {
                instance.warn("An IO exception caught while generating file! (" + e.getMessage() + ")");
                return false;
            }

            config = YamlConfiguration.loadConfiguration(file);
            long end = System.currentTimeMillis();

            instance.info("Loaded flatfile storage! (Took " + (end - start) + "ms)");
            return true;
        }
    }

    public void close() {
        try {
            if (connection != null && connection.isValid(5)) {
                connection.close();
            }
        } catch (SQLException e) {
            validateConnection(false);
            instance.warn("SQL error caught while executing SQL query! (" + e.getMessage() + ")");
        }
    }

    public void loadTopBalances() {
        if (hasSQLEnabled() && !isConnected()) {
            return;
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(instance, new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();

                if (lastUpdate == -1L) {
                    lastUpdate = System.currentTimeMillis();
                }

                if (now - lastUpdate < 900000) {
                    List<String> datas = sql ? getSQLData() : getLocalData();

                    topBalances.clear();

                    if (datas.isEmpty()) {
                        topBalances.add("&cData load failed or was not found.");
                        lastUpdate = System.currentTimeMillis();
                        return;
                    }

                    Collections.sort(datas, new Comparator<String>() {
                        @Override
                        public int compare(String s2, String s1) {
                            return Long.valueOf(s1.split(":")[1]).compareTo(Long.valueOf(s2.split(":")[1]));
                        }
                    });

                    for (int i = 0; i < 10; i++) {
                        if (i < 0 || i >= datas.size()) {
                            break;
                        }

                        String[] data = datas.get(i).split(":");
                        String name = ProfileUtil.getName(UUID.fromString(data[0]));
                        topBalances.add(String.valueOf(i + 1) + ":" + name + ":" + data[1]);
                    }

                    lastUpdate = System.currentTimeMillis();
                }
            }
        }, 0L, 20L * 60L * 15);
    }

    private List<String> getLocalData() {
        List<String> result = new ArrayList<>();

        if (config.isConfigurationSection("Players")) {
            for (String key : config.getConfigurationSection("Players").getKeys(false)) {
                result.add(key + ":" + config.getInt("Players." + key));
            }
        }

        return result;
    }

    private List<String> getSQLData() {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        final Future<List<String>> future = executor.submit(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                List<String> result = new ArrayList<>();

                try (Statement statement = connection.createStatement(); ResultSet results = statement.executeQuery(Queries.GET_ALL.query())) {
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
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return new ArrayList<>();
        }
    }

    private boolean saveLocalData() {
        if (!sql) {
            try {
                config.save(file);
                return true;
            } catch (IOException e) {
                instance.warn("An IO exception caught while generating file! (" + e.getMessage() + ")");
                return false;
            }
        }

        return true;
    }

    public boolean hasSQLEnabled() {
        return sql;
    }

    public boolean isConnected() {
        return connected;
    }

    private void validateConnection(boolean val) {
        this.connected = val;
    }

    public int balance(final UUID uuid) {
        if (sql) {
            ExecutorService executor = Executors.newFixedThreadPool(1);

            final Future<Integer> future = executor.submit(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(Queries.GET.query(uuid.toString()))) {
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
                }
            });

            executor.shutdown();

            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                return 0;
            }

        } else {
            return config.isInt("Players." + uuid) ? config.getInt("Players." + uuid) : 0;
        }
    }

    public boolean generate(final UUID uuid) {
        if (sql) {
            ExecutorService executor = Executors.newFixedThreadPool(1);

            final Future<Boolean> future = executor.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(Queries.GET.query(uuid.toString()))) {
                        return !result.isBeforeFirst() && statement.execute(Queries.GENERATE.query(uuid.toString(), String.valueOf(instance.getTMConfig().getDefaultBalance())));
                    } catch (SQLException e) {
                        validateConnection(false);
                        instance.warn("SQL error caught while executing SQL query! (" + e.getMessage() + ")");
                        return false;
                    }
                }
            });

            executor.shutdown();

            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                return false;
            }
        } else {
            if (!config.isInt("Players." + uuid)) {
                config.set("Players." + uuid, instance.getTMConfig().getDefaultBalance());
                saveLocalData();
            }
        }

        return true;
    }

    public boolean found(final UUID uuid) {
        if (sql) {
            ExecutorService executor = Executors.newFixedThreadPool(1);
            final Future<Boolean> future = executor.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(Queries.GET.query(uuid.toString()))) {
                        return result.isBeforeFirst();
                    }
                }
            });

            executor.shutdown();

            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                return false;
            }
        } else {
            return config.isInt("Players." + uuid);
        }
    }

    public boolean add(final UUID uuid, final int amount) {
        if (amount <= 0) {
            return false;
        }

        if (sql) {
            ExecutorService executor = Executors.newFixedThreadPool(1);

            final Future<Boolean> future = executor.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(Queries.GET.query(uuid.toString()))) {
                        if (!result.isBeforeFirst()) {
                            return false;
                        }

                        int balance = 0;

                        while (result.next()) {
                            balance = (int) result.getLong("tokens");
                        }

                        statement.execute(Queries.SET.query(String.valueOf(balance + amount), uuid.toString()));
                        return true;
                    } catch (SQLException e) {
                        validateConnection(false);
                        instance.warn("SQL error caught while executing SQL query! (" + e.getMessage() + ")");
                        return false;
                    }
                }
            });

            executor.shutdown();

            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                return false;
            }
        } else {
            int balance = config.isInt("Players." + uuid) ? config.getInt("Players." + uuid) : 0;
            config.set("Players." + uuid, balance + amount);
            saveLocalData();
            return true;
        }
    }

    public boolean remove(final UUID uuid, final int amount) {
        if (amount <= 0) {
            return false;
        }

        if (sql) {
            ExecutorService executor = Executors.newFixedThreadPool(1);

            final Future<Boolean> future = executor.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(Queries.GET.query(uuid.toString()))) {
                        if (!result.isBeforeFirst()) {
                            return false;
                        }

                        int balance = 0;

                        while (result.next()) {
                            balance = (int) result.getLong("tokens");
                        }

                        if (balance - amount < 0) {
                            return false;
                        }

                        statement.execute(Queries.SET.query(String.valueOf(balance - amount), uuid.toString()));
                        return true;
                    } catch (SQLException e) {
                        validateConnection(false);
                        instance.warn("SQL error caught while executing SQL query! (" + e.getMessage() + ")");
                        return false;
                    }
                }
            });

            executor.shutdown();

            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                return false;
            }
        } else {
            int balance = config.isInt("Players." + uuid) ? config.getInt("Players." + uuid) : 0;
            config.set("Players." + uuid, balance - amount);
            saveLocalData();
            return true;
        }
    }

    public boolean set(final UUID uuid, final int amount) {
        if (amount < 0) {
            return false;
        }

        if (sql) {
            ExecutorService executor = Executors.newFixedThreadPool(1);

            final Future<Boolean> future = executor.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(Queries.GET.query(uuid.toString()))) {

                        if (!result.isBeforeFirst()) {
                            return false;
                        }

                        statement.execute(Queries.SET.query(String.valueOf(amount), uuid.toString()));
                        return true;
                    } catch (SQLException e) {
                        validateConnection(false);
                        instance.warn("SQL error caught while executing SQL query! (" + e.getMessage() + ")");
                        return false;
                    }
                }
            });

            executor.shutdown();

            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                return false;
            }
        } else {
            config.set("Players." + uuid, amount);
            saveLocalData();
            return true;
        }
    }

    public int size() {
        return sql ? getSQLData().size() : getLocalData().size();
    }

    public List<String> getTopBalances() {
        return topBalances;
    }

    public String getNextUpdate() {
        return StringUtil.format((lastUpdate + 900000 - System.currentTimeMillis()) / 1000);
    }

    private enum Queries {

        TABLE("CREATE TABLE IF NOT EXISTS tokenmanager (uuid varchar(36) NOT NULL, tokens bigint(255) NOT NULL, PRIMARY KEY (uuid)) ENGINE=InnoDB DEFAULT CHARSET=latin1"),
        GENERATE("INSERT INTO tokenmanager (uuid, tokens) VALUES (\"{0}\", {1})"),
        GET_ALL("SELECT * FROM tokenmanager"),
        GET("SELECT * FROM tokenmanager WHERE uuid=\"{0}\""),
        SET("UPDATE tokenmanager SET tokens={0} WHERE uuid=\"{1}\"");

        private final String query;

        Queries(String query) {
            this.query = query;
        }

        public String query() {
            return query;
        }

        public String query(String a) {
            return query.replace("{0}", a);
        }

        public String query(String a, String a1) {
            return query.replace("{0}", a).replace("{1}", a1);
        }
    }
}
