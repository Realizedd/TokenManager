package me.realized.tm.configuration;

import me.realized.tm.Core;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TMConfig {

    private final Core instance;
    private int defaultBalance = 25;
    private int clickDelay = 0;
    private boolean useDefault = false;
    private String defaultShop = null;
    private final Map<String, String> strings = new HashMap<>();
    private final Map<String, List<String>> stringLists = new HashMap<>();

    public TMConfig(Core instance) {
        this.instance = instance;
    }

    public void load() {
        strings.clear();
        stringLists.clear();

        File file = new File(instance.getDataFolder(), "config.yml");
        if (!file.exists()) {
            instance.saveResource("config.yml", true);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (config.isInt("default-balance")) {
            defaultBalance = config.getInt("default-balance");
        }

        if (config.isBoolean("use-default.enabled")) {
            useDefault = config.getBoolean("use-default.enabled");
        }

        if (config.isInt("click-delay")) {
            clickDelay = config.getInt("click-delay");
        }

        if (config.isString("use-default.shop")) {
            defaultShop = config.getString("use-default.shop");
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

    public int getDefaultBalance() {
        return defaultBalance;
    }

    public String getDefaultShop() {
        return defaultShop;
    }

    public boolean isDefaultEnabled() {
        return useDefault;
    }

    public int getClickDelay() {
        return clickDelay;
    }

    public String getString(String key) {
        return strings.get(key);
    }

    public List<String> getList(String key) {
        return stringLists.get(key);
    }
}
