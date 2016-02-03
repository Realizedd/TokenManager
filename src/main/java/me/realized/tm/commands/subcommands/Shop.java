package me.realized.tm.commands.subcommands;

import me.realized.tm.utilities.TMShop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class Shop extends SubCommand {

    public Shop() {
        super(new String[]{"shop"}, "shop <name>", "use.shop", 2);
    }

    @Override
    public void run(CommandSender sender, Command command, String[]args) {
        if (!(sender instanceof Player)) {
            pm(sender, "&cConsole can not use shops! :(");
            return;
        }

        Player player = (Player) sender;

        if (config.isDefaultEnabled()) {
            String name = config.getDefaultShop();

            if (!shopManager.isShop(name)) {
                pm(player, config.getString("invalid-shop").replace("%input%", name));
                return;
            }

            TMShop shop = shopManager.getShop(name);

            player.openInventory(shop.get());
            return;
        }

        if (args.length < getMinLength()) {
            System.out.println(Arrays.toString(args));
            pm(sender, config.getString("sub-command-usage").replace("%usage%", getUsage()).replace("%command%", command.getName()));
            return;
        }

        String name = args[1].toLowerCase();

        if (!shopManager.isShop(name)) {
            pm(player, config.getString("invalid-shop").replace("%input%", name));
            return;
        }

        TMShop shop = shopManager.getShop(name);

        if (shop.hasPermission() && !player.hasPermission("tokenmanager.use.shop." + name)) {
            pm(player, config.getString("no-permission").replace("%permission%", "tokenmanager.use.shop." + name));
            return;
        }

        player.openInventory(shop.get());
    }
}
