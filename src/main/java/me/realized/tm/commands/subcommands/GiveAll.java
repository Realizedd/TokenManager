package me.realized.tm.commands.subcommands;

import me.realized.tm.data.Action;
import me.realized.tm.events.TokenReceiveEvent;
import me.realized.tm.utilities.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

public class GiveAll extends SubCommand {

    public GiveAll() {
        super(new String[] {"giveall", "sendall"}, "giveall <amount>", "admin", 2);
    }

    @Override
    public void run(CommandSender sender, String label, String[] args) {
        if (!StringUtil.isInt(args[1], false)) {
            pm(sender, getLang().getString("invalid-amount").replace("%input%", String.valueOf(args[2])));
            return;
        }

        int amount = Integer.parseInt(args[1]);
        Collection<? extends Player> online = Bukkit.getOnlinePlayers();

        for (Player player : online) {
            TokenReceiveEvent event = new TokenReceiveEvent(player.getUniqueId(), amount);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                continue;
            }

            getDataManager().executeAction(Action.ADD, player.getUniqueId(), amount);
            pm(player, getLang().getString("on-receive").replace("%amount%", String.valueOf(amount)));
        }

        pm(sender, getLang().getString("on-give-all").replace("%players%", String.valueOf(online.size())).replace("%amount%", String.valueOf(amount)));
    }
}
