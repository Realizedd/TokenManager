package me.realized.tm.commands.subcommands;

import me.realized.tm.data.Action;
import me.realized.tm.events.TokenReceiveEvent;
import me.realized.tm.utilities.StringUtil;
import me.realized.tm.utilities.profile.ProfileUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Send extends SubCommand {

    public Send() {
        super(new String[] {"send"}, "send <username> <amount>", "use.send", 3);
    }

    @Override
    public void run(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            pm(sender, "&cConsole can not send tokens! :(");
            return;
        }

        Player player = (Player) sender;
        UUID target = ProfileUtil.getUniqueId(args[1]);

        if (target == null || !((boolean) getDataManager().executeAction(Action.EXISTS, target, 0))) {
            pm(sender, getLang().getString("invalid-player").replace("%input%", args[1]));
            return;
        }

        if (!StringUtil.isInt(args[2], false) || Integer.parseInt(args[2]) == 0) {
            pm(sender, getLang().getString("invalid-amount").replace("%input%", String.valueOf(args[2])));
            return;
        }

        int amount = Integer.parseInt(args[2]);
        int balance = (int) getDataManager().executeAction(Action.BALANCE, player.getUniqueId(), 0);

        if (balance - amount < 0) {
            pm(sender, getLang().getString("invalid-amount").replace("%input%", String.valueOf(args[2])));
            return;
        }

        boolean success = (boolean) getDataManager().executeAction(Action.REMOVE, player.getUniqueId(), amount);

        if (!success) {
            pm(sender, "&cFailed to remove " + amount + " token(s) from your balance, please contact an administrator.");
            return;
        }

        TokenReceiveEvent event = new TokenReceiveEvent(target, amount);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        success = (boolean) getDataManager().executeAction(Action.ADD, target, amount);

        if (!success) {
            pm(sender, "&cFailed to add " + amount + " token(s) to " + target + "'s balance, please contact an administrator.");
            return;
        }

        String sent = String.valueOf(amount);
        pm(sender, getLang().getString("on-send").replace("%amount%", sent).replace("%player%", args[1]));

        Player targetPlayer = Bukkit.getPlayer(target);

        if (targetPlayer != null) {
            pm(targetPlayer, getLang().getString("on-receive").replace("%amount%", sent));
        }
    }
}
