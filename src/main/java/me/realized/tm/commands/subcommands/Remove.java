package me.realized.tm.commands.subcommands;

import me.realized.tm.data.Action;
import me.realized.tm.utilities.StringUtil;
import me.realized.tm.utilities.profile.ProfileUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class Remove extends SubCommand {

    public Remove() {
        super(new String[] {"remove", "delete"}, "remove <username> <amount>", "admin", 3);
    }

    @Override
    public void run(CommandSender sender, String label, String[] args) {
        UUID target = ProfileUtil.getUniqueId(args[1]);

        if (target == null || !((boolean) getDataManager().executeAction(Action.EXISTS, target, 0))) {
            pm(sender, getLang().getString("invalid-player").replace("%input%", args[1]));
            return;
        }

        if (!StringUtil.isInt(args[2], false)) {
            pm(sender, getLang().getString("invalid-amount").replace("%input%", String.valueOf(args[2])));
            return;
        }

        int amount = Integer.parseInt(args[2]);

        getDataManager().executeAction(Action.REMOVE, target, amount);
        pm(sender, getLang().getString("on-remove").replace("%amount%", String.valueOf(amount)).replace("%player%", args[1]));

        if (Bukkit.getPlayer(target) != null) {
            pm(Bukkit.getPlayer(target), getLang().getString("on-take").replace("%amount%", String.valueOf(amount)));
        }
    }
}
