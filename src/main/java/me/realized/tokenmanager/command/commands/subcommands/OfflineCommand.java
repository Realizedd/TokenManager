package me.realized.tokenmanager.command.commands.subcommands;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.command.BaseCommand;
import me.realized.tokenmanager.util.NumberUtil;
import me.realized.tokenmanager.util.profile.ProfileUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OfflineCommand extends BaseCommand {

    public enum ModifyType {

        ADD("COMMAND.tokenmanager.add", (balance, amount) -> balance + amount),
        SET("COMMAND.tokenmanager.set", (balance, amount) -> amount),
        REMOVE("COMMAND.tokenmanager.remove", (balance, amount) -> balance - amount);

        private final String messageKey;
        private final BiFunction<Long, Long, Long> action;

        ModifyType(final String messageKey, final BiFunction<Long, Long, Long> action) {
            this.messageKey = messageKey;
            this.action = action;
        }

        public String getMessageKey() {
            return messageKey;
        }

        public long apply(final long balance, final long amount) {
            return action.apply(balance, amount);
        }
    }

    private final ModifyType type;

    public OfflineCommand(final TokenManagerPlugin plugin, final ModifyType type, final String... aliases) {
        super(plugin, type.name().toLowerCase(), type.name().toLowerCase() + " <username> <amount>", null, 3, false, aliases);
        this.type = type;
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final long amount = NumberUtil.parseLong(args[2]).orElse(0);

        if (amount < 0) {
            sendMessage(sender, true, "ERROR.invalid-amount", "input", args[2]);
            return;
        }

        final List<String> options = Arrays.asList(args);
        final boolean silent = options.contains("-s"), onlineOnly = options.contains("-o");
        final Player target = Bukkit.getPlayerExact(args[1]);

        // Case: Target player is online
        if (target != null) {
            sendMessage(sender, true, type.getMessageKey(), "amount", amount, "player", target.getName());

            if (type == ModifyType.SET) {
                dataManager.set(target, amount);
                return;
            } else {
                final OptionalLong balance = dataManager.get(target);

                if (!balance.isPresent()) {
                    dataManager.queueCommand(target, type, amount, silent);
                    sendMessage(sender, false, "&c" + target.getName() + "'s data is currently loading! Command has been queued for future execution.");
                    return;
                }

                dataManager.set(target, type.apply(balance.getAsLong(), amount));
            }

            if (!silent) {
                plugin.getLang().sendMessage(target, true, "COMMAND." + (type == ModifyType.ADD ? "add" : "remove"), "amount", amount);
            }

            return;
        } else if (onlineOnly) {
            sendMessage(sender, true, "ERROR.player-not-found", "input", args[1]);
            return;
        }

        // UUID conversion if server is online mode
        getTarget(sender, args[1], key -> {
            if (!key.isPresent()) {
                sendMessage(sender, true, "ERROR.player-not-found", "input", args[1]);
                return;
            }

            dataManager.get(key.get(), balance -> {
                // Case: Not found in the database
                if (!balance.isPresent()) {
                    sendMessage(sender, true, "ERROR.player-not-found", "input", args[1]);
                    return;
                }

                dataManager.set(key.get(), type, amount, type.apply(balance.getAsLong(), amount), silent,
                    () -> sendMessage(sender, true, type.getMessageKey(), "amount", amount, "player", args[1]),
                    error -> sendMessage(sender, false, "&cThere was an error while executing this command, please contact an administrator."));
            }, error -> sender.sendMessage(ChatColor.RED + "Could not get token balance of " + key.get() + ": " + error));
        });
    }

    private void getTarget(final CommandSender sender, final String input, final Consumer<Optional<String>> consumer) {
        // Case: Server is in offline mode.
        if (!online) {
            consumer.accept(Optional.of(input));
            return;
        }

        plugin.doAsync(() -> ProfileUtil.getUUID(input, uuid -> consumer.accept(Optional.ofNullable(uuid)),
            error -> sender.sendMessage(ChatColor.RED + "Failed to obtain UUID of " + input + ": " + error)));
    }
}
