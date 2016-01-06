package me.realized.tm.configuration;

import me.realized.tm.Core;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TMConfig {

    private final Core instance;
    private long defaultBalance = 25;
    private Map<String, String> strings = new HashMap<>();
    private Map<String, List<String>> stringLists = new HashMap<>();

    public TMConfig(Core instance) {
        this.instance = instance;
    }

    public void load() {
        if (!new File(instance.getDataFolder(), "config.yml").exists()) {
            instance.saveResource("config.yml", true);
        }

        FileConfiguration config = instance.getConfig();

        if (config.isInt("default-balance")) {
            defaultBalance = config.getInt("default-balance");
        }

        if (config.isConfigurationSection("Messages")) {
            for (String key : config.getConfigurationSection("Messages").getKeys(false)) {
                String path = "Messages." + key;
                if (config.isString(path)) {
                    strings.put(key, config.getString(path));
                } else if (config.isList(path)) {
                    stringLists.put(key, config.getStringList(path));
                }
            }
        }
    }

    public long getDefaultBalance() {
        return defaultBalance;
    }

    public String getString(String key) {
        return strings.get(key);
    }

    public List<String> getList(String key) {
        return stringLists.get(key);
    }
}
