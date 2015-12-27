package me.realized.tm.commands.subcommands;

import me.realized.tm.utilities.ProfileUtil;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class Set extends SubCommand {

    public Set() {
        super("set", "set <username> <amount>", "admin", 3);
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        if (args.length >= getMinLength()) {
            UUID target = ProfileUtil.getUniqueId(args[1]);

            if (target == null || !dataManager.hasLoadedData(target)) {
                pm(sender, config.getString("invalid-player").replace("%input%", args[1]));
                return;
            }

            int amount;

            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                pm(sender, config.getString("invalid-amount").replace("%input%", String.valueOf(args[2])));
                return;
            }

            if (amount < 0) {
                pm(sender, config.getString("invalid-amount").replace("%input%", String.valueOf(args[2])));
                return;
            }

            dataManager.set(target, amount);
            pm(sender, config.getString("on-set").replace("%amount%", String.valueOf(amount)).replace("%player%", args[1]));
        }
    }
}
