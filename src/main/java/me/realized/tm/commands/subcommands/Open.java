package me.realized.tm.commands.subcommands;

import me.realized.tm.utilities.TMShop;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Open extends SubCommand {

    public Open() {
        super(new String[]{"open"}, "open <username> <shop>", "admin", 3);
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        Player target = Bukkit.getPlayerExact(args[1]);

        if (Bukkit.getPlayerExact(args[1]) == null) {
            pm(sender, config.getString("invalid-player").replace("%input%", args[1]));
            return;
        }

        String name = args[2].toLowerCase();

        if (!shopManager.isShop(name)) {
            pm(sender, config.getString("invalid-shop").replace("%input%", name));
            return;
        }

        TMShop shop = shopManager.getShop(name);

        target.openInventory(shop.get());
        pm(sender, config.getString("on-open").replace("%name%", name).replace("%player%", args[1]));
    }
}
