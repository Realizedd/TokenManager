package me.realized.tokenmanager.command.commands.subcommands;

import java.util.OptionalLong;
import java.util.function.BiFunction;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.command.BaseCommand;
import me.realized.tokenmanager.util.NumberUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class OfflineCommand extends BaseCommand {

    private final ModifyType type;

    public OfflineCommand(final TokenManagerPlugin plugin, final ModifyType type, final String usage, final String... aliases) {
        super(plugin, type.name().toLowerCase(), usage, null, 3, false, aliases);
        this.type = type;
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        getTarget(sender, args[1], key -> {
            // Case: Not a valid minecraft account
            if (!key.isPresent()) {
                sendMessage(sender, true, "ERROR.player-not-found", "input", args[1]);
                return;
            }

            dataManager.get(key.get(), balance -> {
                // Case: Not found in db
                if (!balance.isPresent()) {
                    sendMessage(sender, true, "ERROR.player-not-found", "input", args[1]);
                    return;
                }

                final OptionalLong amount = NumberUtil.parseLong(args[2]);

                // Case: Invalid amount specified
                if (!amount.isPresent() || amount.getAsLong() <= 0) {
                    sendMessage(sender, true, "ERROR.invalid-amount", "input", args[2]);
                    return;
                }

                dataManager.set(key.get(), type == ModifyType.SET, (type == ModifyType.REMOVE ? -1 : 1) * amount.getAsLong(),
                    type.exec(balance.getAsLong(), amount.getAsLong()),
                    () -> sendMessage(sender, true, type.getMessageKey(), "amount", amount.getAsLong(), "player", args[1]),
                    error -> sendMessage(sender, false, "&cThere was an error while executing this command, please contact an administrator."));
            }, error -> sender.sendMessage(ChatColor.RED + "Could not get token balance of " + key.get() + ": " + error));
        });
    }

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

        public long exec(final long balance, final long amount) {
            return action.apply(balance, amount);
        }
    }
}
