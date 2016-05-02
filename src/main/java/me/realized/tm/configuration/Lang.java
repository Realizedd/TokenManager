package me.realized.tm.configuration;

import me.realized.tm.Core;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lang {

    private final File file;

    private final Map<String, String> strings = new HashMap<>();
    private final Map<String, List<String>> stringLists = new HashMap<>();

    public Lang(Core instance) {
        this.file = new File(instance.getDataFolder(), "lang.yml");

        if (!file.exists()) {
            instance.saveResource("lang.yml", true);
        }
    }

    public void load() {
        strings.clear();
        stringLists.clear();

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (String key : config.getKeys(false)) {
            if (config.isString(key)) {
                strings.put(key, config.getString(key));
            } else if (config.isList(key)) {
                stringLists.put(key, config.getStringList(key));
            }
        }
    }

    public String getString(String key) {
        return strings.get(key);
    }

    public List<String> getStringList(String key) {
        return stringLists.get(key);
    }
}
