package me.realized.tm.commands.subcommands;

import me.realized.tm.utilities.TMShop;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Shop extends SubCommand {

    public Shop() {
        super("shop", "shop <name>", "use.shop", 2);
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            pm(sender, "&cConsole can not use shops! :(");
            return;
        }

        if (args.length >= getMinLength()) {
            Player player = (Player) sender;
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
}
