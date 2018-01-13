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

package me.realized.tokenmanager.shop;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.util.ItemBuilder;
import me.realized.tokenmanager.util.ItemUtil;
import me.realized.tokenmanager.util.Log;
import me.realized.tokenmanager.util.NumberUtil;
import me.realized.tokenmanager.util.config.AbstractConfiguration;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public class ShopConfig extends AbstractConfiguration<TokenManagerPlugin> {

    private final Map<String, Shop> shops = new LinkedHashMap<>();

    public ShopConfig(final TokenManagerPlugin plugin) {
        super(plugin, "shops");
    }

    @Override
    protected void loadValues(final FileConfiguration configuration) {
        final ConfigurationSection section = configuration.getConfigurationSection("shops");

        if (section == null) {
            return;
        }

        for (final String name : section.getKeys(false)) {
            final ConfigurationSection shopSection = section.getConfigurationSection(name);
            final Shop shop;

            try {
                shop = new Shop(
                    name,
                    shopSection.getString("title", "&cShop title was not specified."),
                    shopSection.getInt("rows", 1),
                    shopSection.getBoolean("auto-close", false),
                    shopSection.getBoolean("use-permission", false)
                );
            } catch (IllegalArgumentException ex) {
                Log.error(this, "Failed to initialize shop '" + name + "': " + ex.getMessage());
                continue;
            }

            final ConfigurationSection items = shopSection.getConfigurationSection("items");

            if (items != null) {
                for (final String num : items.getKeys(false)) {
                    final OptionalLong slot = NumberUtil.parseLong(num);

                    if (!slot.isPresent() || slot.getAsLong() < 0 || slot.getAsLong() >= shop.getGui().getSize()) {
                        Log.error(this, "Failed to load slot '" + num + "' for shop '" + name + "': '" + slot
                            + "' is not a valid number or is over the shop size.");
                        continue;
                    }

                    final ConfigurationSection slotSection = items.getConfigurationSection(num);
                    final ItemStack displayed;

                    try {
                        displayed = ItemUtil.loadFromString(slotSection.getString("displayed"));
                    } catch (Exception ex) {
                        shop.getGui().setItem((int) slot.getAsLong(), ItemBuilder
                            .of(Material.REDSTONE_BLOCK)
                            .name("&4&m------------------")
                            .lore(
                                "&cThere was an error",
                                "&cwhile loading this",
                                "&citem, please contact",
                                "&can administrator.",
                                "&4&m------------------"
                            )
                            .build()
                        );
                        Log.error(this, "Failed to load displayed item for slot '" + num + "' of shop '" + name + "': " + ex.getMessage());
                        continue;
                    }

                    shop.setSlot((int) slot.getAsLong(), displayed, new Slot(
                        (int) slot.getAsLong(),
                        slotSection.getInt("cost", 1000000),
                        slotSection.getString("message"),
                        slotSection.getString("subshop"),
                        slotSection.getStringList("commands"),
                        slotSection.getBoolean("use-permission", false)
                    ));
                }
            }

            register(name, shop);
        }
    }

    @Override
    public void handleUnload() {
        shops.clear();
    }

    public Optional<Shop> getShop(final String name) {
        return Optional.ofNullable(shops.get(name));
    }

    public Collection<Shop> getShops() {
        return shops.values();
    }

    public Shop register(final String name, final Shop shop) {
        return shops.put(name, shop);
    }
}
