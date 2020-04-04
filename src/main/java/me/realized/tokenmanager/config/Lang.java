package me.realized.tokenmanager.config;

import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.util.Log;
import me.realized.tokenmanager.util.NumberUtil;
import me.realized.tokenmanager.util.Reloadable;
import me.realized.tokenmanager.util.StringUtil;
import me.realized.tokenmanager.util.config.AbstractConfiguration;
import me.realized.tokenmanager.util.config.convert.Converter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;

public class Lang extends AbstractConfiguration<TokenManagerPlugin> implements Reloadable {

    private final Map<String, String> messages = new HashMap<>();

    public Lang(final TokenManagerPlugin plugin) {
        super(plugin, "lang");
    }

    @Override
    protected void loadValues(FileConfiguration configuration) throws Exception {
        if (!configuration.isInt("config-version")) {
            configuration = convert(new Converter2_3());
        } else if (configuration.getInt("config-version") < getLatestVersion()) {
            configuration = convert(null);
        }

        final Map<String, String> strings = new HashMap<>();

        for (String key : configuration.getKeys(true)) {
            if (key.equals("config-version")) {
                continue;
            }

            // Fixes a weird occurrence with FileConfiguration#getKeys that an extra separator char is prepended when called after FileConfiguration#set
            if (key.startsWith(".")) {
                key = key.substring(1);
            }

            final Object value = configuration.get(key);

            if (value == null || value instanceof MemorySection) {
                continue;
            }

            final String message = value instanceof List ? StringUtil.fromList((List<?>) value) : value.toString();

            if (key.startsWith("STRINGS")) {
                final String[] args = key.split(Pattern.quote("."));
                strings.put(args[args.length - 1], message);
            } else {
                messages.put(key, message);
            }
        }

        messages.replaceAll((key, value) -> {
            for (final Map.Entry<String, String> entry : strings.entrySet()) {
                final String placeholder = "{" + entry.getKey() + "}";

                if (StringUtils.containsIgnoreCase(value, placeholder)) {
                    value = value.replaceAll("(?i)" + Pattern.quote(placeholder), entry.getValue());
                }
            }

            return value;
        });
    }

    @Override
    public void handleUnload() {
        messages.clear();
    }

    @Override
    protected Set<String> transferredSections() {
        return Sets.newHashSet("STRINGS");
    }

    private String getRawMessage(final String key) {
        final String message = messages.get(key);

        if (message == null) {
            Log.error(this, "Failed to load message: provided key '" + key + "' has no assigned value");
            return null;
        }

        // Allow disabling any message by setting it to ''
        if (message.isEmpty()) {
            return null;
        }

        return message;
    }

    public String getMessage(final String key) {
        final String message = getRawMessage(key);

        if (message == null) {
            return null;
        }

        return StringUtil.color(message);
    }

    public String getMessage(final String key, final Object... replacers) {
        final String message = getMessage(key);

        if (message == null) {
            return null;
        }

        return replace(message, replacers);
    }

    public void sendMessage(final CommandSender receiver, final boolean config, final String in, final Object... replacers) {
        if (config) {
            String message = getRawMessage(in);

            if (message == null) {
                return;
            }

            receiver.sendMessage(StringUtil.color(replace(message, replacers)));
        } else {
            receiver.sendMessage(StringUtil.color(replace(in, replacers)));
        }
    }

    private String replace(String message, final Object... replacers) {
        for (int i = 0; i < replacers.length; i += 2) {
            if (i + 1 >= replacers.length) {
                break;
            }

            final String key = String.valueOf(replacers[i]);
            final Object value = replacers[i + 1];

            if (value instanceof Long) {
                message = message.replace("%" + key + "_commas%", NumberUtil.withCommas((Long) value));
                message = message.replace("%" + key + "_formatted%", NumberUtil.withSuffix((Long) value));
            }

            message = message.replace("%" + key + "%", String.valueOf(value));
        }

        return message;
    }

    private static class Converter2_3 implements Converter {

        Converter2_3() {}

        @Override
        public Map<String, String> renamedKeys() {
            final Map<String, String> keys = new HashMap<>();
            keys.put("no-permission", "ERROR.no-permission");
            keys.put("invalid-amount", "ERROR.invalid-amount");
            keys.put("invalid-player", "ERROR.player-not-found");
            keys.put("invalid-shop", "ERROR.shop-not-found");
            keys.put("invalid-sub-command", "ERROR.invalid-sub-command");
            keys.put("not-enough-tokens", "ERROR.balance-not-enough");
            keys.put("no-data", "ERROR.data-not-enough");
            keys.put("click-spamming", "ERROR.on-click-cooldown");
            keys.put("token-help-page", "COMMAND.token.usage");
            keys.put("tm-help-page", "COMMAND.tokenmanager.usage");
            keys.put("sub-command-usage", "COMMAND.sub-command-usage");
            keys.put("top-next-update", "COMMAND.token.balance-top.next-update");
            keys.put("top-header", "COMMAND.token.balance-top.header");
            keys.put("top-format", "COMMAND.token.balance-top.display-format");
            keys.put("top-footer", "COMMAND.token.balance-top.footer");
            keys.put("balance", "COMMAND.token.balance");
            keys.put("balance-others", "COMMAND.token.balance-other");
            keys.put("on-send", "COMMAND.token.send");
            keys.put("on-receive", "COMMAND.add");
            keys.put("on-take", "COMMAND.remove");
            keys.put("on-add", "COMMAND.tokenmanager.add");
            keys.put("on-remove", "COMMAND.tokenmanager.remove");
            keys.put("on-set", "COMMAND.tokenmanager.set");
            keys.put("on-open", "COMMAND.tokenmanager.open");
            keys.put("shops", "COMMAND.token.shops");
            keys.put("on-give-all", "COMMAND.tokenmanager.giveall");
            return keys;
        }
    }
}
