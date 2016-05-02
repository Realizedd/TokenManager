package me.realized.tm.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Shop extends SubCommand {

    public Shop() {
        super(new String[] {"shop"}, "shop <name>", "use.shop", 2);
    }

    @Override
    public void run(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            pm(sender, "&cConsole can not use shops! :(");
            return;
        }

        Player player = (Player) sender;

        if ((boolean) getConfig().getValue("use-default.enabled")) {
            String name = (String) getConfig().getValue("use-default.shop");

            if (!getShopManager().isShop(name)) {
                pm(player, getLang().getString("invalid-shop").replace("%input%", name));
                return;
            }

            me.realized.tm.shop.Shop shop = getShopManager().getShop(name);
            player.openInventory(shop.get());
            return;
        }

        if (args.length < getMinLength()) {
            pm(sender, getLang().getString("sub-command-usage").replace("%usage%", getUsage()).replace("%command%", label));
            return;
        }

        String name = args[1].toLowerCase();

        if (!getShopManager().isShop(name)) {
            pm(player, getLang().getString("invalid-shop").replace("%input%", name));
            return;
        }

        me.realized.tm.shop.Shop shop = getShopManager().getShop(name);

        if (shop.hasPermission() && !player.hasPermission("tokenmanager.use.shop." + name)) {
            pm(player, getLang().getString("no-permission").replace("%permission%", "tokenmanager.use.shop." + name));
            return;
        }

        player.openInventory(shop.get());
    }
}
