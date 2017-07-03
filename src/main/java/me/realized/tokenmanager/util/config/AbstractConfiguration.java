/*
 *
 *   This file is part of TokenManager, licensed under the MIT License.
 *
 *   Copyright (c) Realized
 *   Copyright (c) contributors
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *   SOFTWARE.
 *
 */

package me.realized.tokenmanager.util.config;

import com.google.common.collect.Lists;
import lombok.Getter;
import me.realized.tokenmanager.util.config.convert.Converter;
import me.realized.tokenmanager.util.plugin.AbstractPluginDelegate;
import me.realized.tokenmanager.util.plugin.Reloadable;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * AbstractConfiguration created to maintain spaces and comments on save.
 * Also supports {@link Converter} for automated config updates.
 *
 * Class created at 6/15/17 by Realized
 **/

public abstract class AbstractConfiguration<P extends JavaPlugin> extends AbstractPluginDelegate<P> implements Configuration<P>, Reloadable {

    @Getter
    private final String name;
    private final File file;
    private FileConfiguration configuration;

    public AbstractConfiguration(final P plugin, final String name) {
        super(plugin);
        this.name = name;
        this.file = new File(plugin.getDataFolder(), name + ".yml");

        if (!file.exists()) {
            generateFile();
        }
    }

    protected void convert(final Converter converter) {
        getPlugin().getLogger().info("================================== NOTICE ==================================\n");
        getPlugin().getLogger().info("Now starting conversion of " + file.getName() + " to support the updated version.\n");

        try {
            final File result = Files.copy(file.toPath(), new File(getPlugin().getDataFolder(), name + "-old-" + System.nanoTime() + ".yml").toPath()).toFile();
            getPlugin().getLogger().info("Old config file was stored as " + result.getName() + ".");
        } catch (IOException ex) {
            getPlugin().getLogger().severe("Convert failed: " + ex.getMessage());
            return;
        }

        // Old previous config instance was not loaded, force load
        if (configuration == null) {
            try {
                setConfiguration();
            } catch (InvalidConfigurationException | IOException ex) {
                getPlugin().getLogger().severe("Convert failed: " + ex.getMessage());
                return;
            }
        }

        // Store a local copy of the previous config instance
        final FileConfiguration old = configuration;

        // Generate the updated config file from resources
        generateFile();
        getPlugin().getLogger().info("Generated the new config file. Updating values...");

        // Load new config
        try {
            setConfiguration();
        } catch (InvalidConfigurationException | IOException ex) {
            getPlugin().getLogger().severe("Convert failed: " + ex.getMessage());
            return;
        }

        final Map<String, String> renamedKeys = converter.renamedKeys();
        final Collection<String> censoredKeys = converter.censoredKeys();

        // Could use a stream but inefficient since value is still used after filters
        for (final String key : old.getKeys(true)) {
            final Object value = old.get(key);

            if (value == null) {
                continue;
            }

            String newKey = key;

            if (configuration.get(key) == null && (renamedKeys == null || (newKey = renamedKeys.get(key)) == null)) {
                continue;
            }

            configuration.set(newKey, value);

            if (censoredKeys != null && censoredKeys.contains(key)) {
                getPlugin().getLogger().info("(✔) Updated value of censored key '" + newKey + "'" + (!newKey.equals(key) ? " (previously: '" + key + "')" : "") + ".");
                continue;
            }

            getPlugin().getLogger().info("(✔) Updated value of key '" + newKey + "'" + (!newKey.equals(key) ? " (previously: '" + key + "')" : "") + " = " + value);
        }

        save();
        getPlugin().getLogger().info("Successfully converted the following config: " + file.getName() + "\n");
        getPlugin().getLogger().info("============================================================================");
    }

    public FileConfiguration getConfiguration() throws IOException, InvalidConfigurationException {
        if (configuration == null) {
            setConfiguration();
        }

        return configuration;
    }

    private void setConfiguration() throws IOException, InvalidConfigurationException {
        this.configuration = new YamlConfiguration();
        configuration.load(file);
    }

    @Override
    public void handleLoad() throws IOException, InvalidConfigurationException {
        setConfiguration();
    }

    @Override
    public void handleUnload() {}

    public void save() {
        final Map<Integer, List<String>> commentsAndSpaces = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            int index = 0;
            List<String> comments = new ArrayList<>();
            String line;

            while ((line = reader.readLine()) != null) {
                // Check for comments and spaces
                if (line.trim().startsWith("#") || line.trim().isEmpty()) {
                    comments.add(line);
                } else if (!line.isEmpty() && !line.trim().startsWith("-") && line.split(":").length > 0) {
                    // New key is detected, push previous information with last key index
                    if (!comments.isEmpty()) {
                        commentsAndSpaces.put(index, new ArrayList<>(comments));
                        comments.clear();
                    }

                    // Increase index only if key is detected
                    index++;
                }
            }
        } catch (IOException ex) {
            getPlugin().getLogger().warning("Configuration read operation failed: " + ex.getMessage());
            return;
        }

        configuration.options().header(null);

        List<String> entries = Lists.newArrayList(configuration.saveToString().split("\n"));

        if (!file.exists()) {
            generateFile();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (int index = 0; index < entries.size(); index++) {
                List<String> data = commentsAndSpaces.get(index);

                if (data != null) {
                    for (String comment : data) {
                        writer.write(comment);
                        writer.newLine();
                    }
                }

                writer.write(entries.get(index));

                if (index + 1 != entries.size()) {
                    writer.newLine();
                }
            }

            writer.flush();
        } catch (IOException ex) {
            getPlugin().getLogger().warning("Configuration write operation failed: " + ex.getMessage());
        }
    }

    private void generateFile() {
        getPlugin().saveResource(name + ".yml", true);
    }
}
