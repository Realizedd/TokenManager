package me.realized.tm.commands.subcommands;

import me.realized.tm.utilities.ProfileUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class Remove extends SubCommand {

    public Remove() {
        super(new String[]{"remove", "delete"}, "remove <username> <amount>", "admin", 3);
    }

    @Override
    public void run(CommandSender sender, Command command, String[] args) {
        UUID target = ProfileUtil.getUniqueId(args[1]);

        if (target == null) {
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

        long balance = dataManager.balance(target);

        if (amount <= 0 || balance - amount < 0) {
            pm(sender, config.getString("invalid-amount").replace("%input%", String.valueOf(args[2])));
            return;
        }

        dataManager.remove(target, amount);
        pm(sender, config.getString("on-remove").replace("%amount%", String.valueOf(amount)).replace("%player%", args[1]));

        if (Bukkit.getPlayer(target) != null) {
            pm(Bukkit.getPlayer(target), config.getString("on-take").replace("%amount%", String.valueOf(amount)));
        }
    }
}
