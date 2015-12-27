package me.realized.tm.commands.subcommands;

import me.realized.tm.utilities.ProfileUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Balance extends SubCommand {

    public Balance() {
        super("balance", "balance", "use", 1);
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        if (args.length == getMinLength()) {
            if (!(sender instanceof Player)) {
                pm(sender, "&cCONSOLE does not have any tokens! :(");
                return;
            }

            Player player = (Player) sender;

            String balance = String.valueOf(dataManager.balance(player.getUniqueId()));
            pm(player, config.getString("balance").replace("%tokens%", balance));
            return;
        }

        if (args.length > getMinLength()) {
            UUID target = ProfileUtil.getUniqueId(args[1]);

            if (target == null || !dataManager.hasLoadedData(target)) {
                pm(sender, config.getString("invalid-player").replace("%input%", args[1]));
                return;
            }

            String balance = String.valueOf(dataManager.balance(target));
            pm(sender, config.getString("balance-others").replace("%tokens%", balance).replace("%player%", args[1]));
        }
    }
}
