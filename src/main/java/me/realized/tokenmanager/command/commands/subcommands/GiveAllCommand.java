package me.realized.tokenmanager.command.commands.subcommands;

import java.util.Collection;
import java.util.OptionalLong;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.command.BaseCommand;
import me.realized.tokenmanager.util.NumberUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveAllCommand extends BaseCommand {

    public GiveAllCommand(final TokenManagerPlugin plugin) {
        super(plugin, "giveall", "giveall <amount>", null, 2, false, "sendall");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final OptionalLong amount = NumberUtil.parseLong(args[1]);

        if (!amount.isPresent() || amount.getAsLong() <= 0) {
            sendMessage(sender, true, "ERROR.invalid-amount", "input", args[1]);
            return;
        }

        final Collection<? extends Player> online = Bukkit.getOnlinePlayers();

        for (final Player player : online) {
            final OptionalLong balance = dataManager.get(player);

            if (!balance.isPresent()) {
                continue;
            }

            dataManager.set(player, balance.getAsLong() + amount.getAsLong());
            sendMessage(player, true, "COMMAND.add", "amount", amount.getAsLong());
        }

        sendMessage(sender, true, "COMMAND.tokenmanager.giveall", "players", online.size(), "amount", amount.getAsLong());
    }
}
