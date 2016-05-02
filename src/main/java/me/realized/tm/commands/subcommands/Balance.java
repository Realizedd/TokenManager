package me.realized.tm.commands.subcommands;

import me.realized.tm.data.Action;
import me.realized.tm.utilities.profile.ProfileUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Balance extends SubCommand {

    public Balance() {
        super(new String[] {"balance", "bal", "money"}, "balance", "use", 1);
    }

    @Override
    public void run(CommandSender sender, String label, String[] args) {
        if (args.length == getMinLength()) {
            if (!(sender instanceof Player)) {
                pm(sender, "&cCONSOLE does not have any tokens! :(");
                return;
            }

            Player player = (Player) sender;
            String balance = String.valueOf(getDataManager().executeAction(Action.BALANCE, player.getUniqueId(), 0));
            pm(player, getLang().getString("balance").replace("%tokens%", balance));
            return;
        }

        if (args.length > getMinLength()) {
            UUID target = ProfileUtil.getUniqueId(args[1]);

            if (target == null) {
                pm(sender, getLang().getString("invalid-player").replace("%input%", args[1]));
                return;
            }

            String balance = String.valueOf(getDataManager().executeAction(Action.BALANCE, target, 0));
            pm(sender, getLang().getString("balance-others").replace("%tokens%", balance).replace("%player%", args[1]));
        }
    }
}
