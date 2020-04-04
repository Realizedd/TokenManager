package me.realized.tokenmanager.command.commands.subcommands;

import java.util.OptionalLong;
import me.realized.tokenmanager.Permissions;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.command.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand extends BaseCommand {

    public BalanceCommand(final TokenManagerPlugin plugin) {
        super(plugin, "balance", "balance", null, 1, false, "bal", "money");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final OptionalLong balance;

        if (args.length == getLength()) {
            balance = sender instanceof Player ? dataManager.get((Player) sender) : OptionalLong.of(0);

            if (!balance.isPresent()) {
                sendMessage(sender, false, "&cFailed to load data of " + sender.getName() + ".");
                return;
            }

            sendMessage(sender, true, "COMMAND.token.balance", "tokens", balance.getAsLong());
            return;
        }

        if (!sender.hasPermission(Permissions.CMD_BALANCE_OTHERS)) {
            sendMessage(sender, true, "ERROR.no-permission", "permission", Permissions.CMD_BALANCE_OTHERS);
            return;
        }

        final Player target;

        if ((target = Bukkit.getPlayerExact(args[1])) == null) {
            sendMessage(sender, true, "ERROR.player-not-found", "input", args[1]);
            return;
        }

        balance = dataManager.get(target);

        if (!balance.isPresent()) {
            sendMessage(sender, true, "ERROR.player-not-found", "input", target.getName());
            return;
        }

        sendMessage(sender, true, "COMMAND.token.balance-other", "player", target.getName(), "tokens", balance.getAsLong());
    }
}
