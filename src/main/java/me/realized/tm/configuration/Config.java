package me.realized.tm.configuration;

import me.realized.tm.Core;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Config {

    private final File file;
    private final Map<String, Object> values = new HashMap<>();
    private boolean sql;

    public Config(Core instance) {
        this.file = new File(instance.getDataFolder(), "config.yml");

        if (!file.exists()) {
            instance.saveResource("config.yml", true);
        }
    }

    public void load() {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (String key : config.getKeys(false)) {
            if (config.isConfigurationSection(key)) {
                for (String nextKey : config.getConfigurationSection(key).getKeys(false)) {
                    values.put(key + "." + nextKey, config.get(key + "." + nextKey));
                }
            } else {
                if (key.equals("auto-save") && (int) config.get(key) <= 5) {
                    values.put(key, 5);
                    continue;
                }

                if (key.equals("click-delay") && (int) config.get(key) < 0) {
                    values.put(key, 0);
                    continue;
                }

                if (key.equals("default-balance") && (int) config.get(key) < 0) {
                    values.put(key, 25);
                    continue;
                }

                if (key.equals("update-balance-top") && (int) config.get(key) <= 5) {
                    values.put(key, 5);
                    continue;
                }

                values.put(key, config.get(key));
            }
        }

        sql = (boolean) values.get("mysql.enabled");
    }

    public boolean isSQLEnabled() {
        return sql;
    }

    public Object getValue(String key) {
        return values.get(key);
    }
}
