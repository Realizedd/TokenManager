package me.realized.tm.commands.subcommands;

import me.realized.tm.data.Action;
import me.realized.tm.events.TokenReceiveEvent;
import me.realized.tm.utilities.StringUtil;
import me.realized.tm.utilities.profile.ProfileUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Add extends SubCommand {

    public Add() {
        super(new String[] {"add", "give"}, "add <username> <amount>", "admin", 3);
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
        TokenReceiveEvent event = new TokenReceiveEvent(target, amount);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        getDataManager().executeAction(Action.ADD, target, amount);
        pm(sender, getLang().getString("on-add").replace("%amount%", String.valueOf(amount)).replace("%player%", args[1]));

        Player player = Bukkit.getPlayer(target);

        if (player != null) {
            pm(player, getLang().getString("on-receive").replace("%amount%", String.valueOf(amount)));
        }
    }
}
