package me.realized.tokenmanager.util;

import java.util.List;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class Placeholders {

    public static void replace(final ItemStack item, final Object value, final String... placeholders) {
        if (!item.hasItemMeta()) {
            return;
        }

        final ItemMeta meta = item.getItemMeta();

        if (meta.hasLore()) {
            final List<String> lore = meta.getLore();
            lore.replaceAll(line -> {
                for (final String placeholder : placeholders) {
                    if (value instanceof Number) {
                        line = line
                            .replace("%" + placeholder + "%", NumberUtil.withCommas(((Number) value).longValue()))
                            .replace("%" + placeholder + " formatted%", NumberUtil.withSuffix(((Number) value).longValue()))
                            .replace("%" + placeholder + " raw%", String.valueOf(value));
                    } else {
                        line = line.replace("%" + placeholder + "%", String.valueOf(value));
                    }
                }

                return line;
            });
            meta.setLore(lore);
        }

        if (meta.hasDisplayName()) {
            String displayName = meta.getDisplayName();

            for (final String placeholder : placeholders) {
                if (value instanceof Number) {
                    displayName = displayName
                        .replace("%" + placeholder + "%", NumberUtil.withCommas(((Number) value).longValue()))
                        .replace("%" + placeholder + " formatted%", NumberUtil.withSuffix(((Number) value).longValue()))
                        .replace("%" + placeholder + " raw%", String.valueOf(value));
                } else {
                    displayName = displayName.replace("%" + placeholder + "%", String.valueOf(value));
                }
            }

            meta.setDisplayName(displayName);
        }

        item.setItemMeta(meta);
    }

    public static String replaceLong(String line, final long value, final String... placeholders) {
        for (final String key : placeholders) {
            line = line
                .replace("%" + key + "%", NumberUtil.withCommas(value))
                .replace("%" + key + "_formatted%", NumberUtil.withSuffix(value))
                .replace("%" + key + "_raw%", String.valueOf(value));
        }
        return line;
    }

    private Placeholders() {}
}
