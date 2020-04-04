package me.realized.tokenmanager.command.commands.subcommands;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import me.realized.tokenmanager.Permissions;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.command.BaseCommand;
import me.realized.tokenmanager.shop.Shop;
import me.realized.tokenmanager.shop.gui.guis.ShopGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand extends BaseCommand {

    public ShopCommand(final TokenManagerPlugin plugin) {
        super(plugin, "shop", "shop <name>", Permissions.CMD_SHOP, plugin.getConfiguration().isOpenSelectedEnabled() ? 1 : 2, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        final String target;
        final Optional<Shop> shop;

        if (config.isOpenSelectedEnabled()) {
            target = config.getOpenSelectedShop();
            shop = shopConfig.getShop(target);

            if (!shop.isPresent()) {
                sendMessage(sender, true, "ERROR.shop-not-found", "input", target);
                return;
            }

            shopManager.open(player, new ShopGui(plugin, shop.get()));
            return;
        }

        target = args[1].toLowerCase();
        shop = shopConfig.getShop(target);

        if (!shop.isPresent()) {
            sendMessage(player, true, "ERROR.shop-not-found", "input", target);
            return;
        }

        if (shop.get().isUsePermission() && !player.hasPermission(Permissions.SHOP + target)) {
            sendMessage(player, true, "ERROR.no-permission", "permission", Permissions.SHOP + target);
            return;
        }

        shopManager.open(player, new ShopGui(plugin, shop.get()));
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 2 && !config.isOpenSelectedEnabled()) {
            return shopConfig.getShops().stream().map(Shop::getName)
                .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
        }

        return null;
    }
}
