package me.realized.tokenmanager.command.commands.subcommands;

import java.util.OptionalLong;
import java.util.function.BiFunction;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.command.BaseCommand;
import me.realized.tokenmanager.util.NumberUtil;
import org.bukkit.command.CommandSender;

public class OfflineCommand extends BaseCommand {

    private final ModifyType type;

    public OfflineCommand(final TokenManagerPlugin plugin, final ModifyType type, final String usage,
        final String... aliases) {
        super(plugin, type.name().toLowerCase(), usage, null, 3, false, aliases);
        this.type = type;
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        getTarget(args[1], target -> {
            if (!target.isPresent()) {
                sendMessage(sender, true, "invalid-player", "args[1]", args[1]);
                return;
            }

            getDataManager().get(target.get(), balance -> {
                if (!balance.isPresent()) {
                    sendMessage(sender, false, "&cFailed to load data of " + args[1] + ".");
                    return;
                }

                final OptionalLong amount = NumberUtil.parseLong(args[2]);

                if (!amount.isPresent() || amount.getAsLong() <= 0) {
                    sendMessage(sender, true, "invalid-amount", "args[1]", args[2]);
                    return;
                }

                getDataManager().set(target.get(), type == ModifyType.SET, (type == ModifyType.REMOVE ? -1 : 1) * amount.getAsLong(),
                    type.exec(balance.getAsLong(), amount.getAsLong()), success -> {
                        if (success) {
                            sendMessage(sender, true, type.getMessageKey(), "amount", amount.getAsLong(), "player", args[1]);
                        } else {
                            sendMessage(sender, false,
                                "&cThere was an error while executing this command, please contact an administrator.");
                        }
                    });
            });
        });
    }

    public enum ModifyType {

        ADD("on-add", (balance, amount) -> balance + amount),
        SET("on-set", (balance, amount) -> amount),
        REMOVE("on-remove", (balance, amount) -> balance - amount);

        private final String messageKey;
        private final BiFunction<Long, Long, Long> function;

        ModifyType(final String messageKey, final BiFunction<Long, Long, Long> function) {
            this.messageKey = messageKey;
            this.function = function;
        }

        public String getMessageKey() {
            return messageKey;
        }

        public long exec(final long balance, final long amount) {
            return function.apply(balance, amount);
        }
    }
}
