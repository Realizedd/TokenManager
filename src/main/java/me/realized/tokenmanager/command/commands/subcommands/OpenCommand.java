package me.realized.tokenmanager.command.commands.subcommands;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.command.BaseCommand;
import me.realized.tokenmanager.shop.Shop;
import me.realized.tokenmanager.shop.gui.guis.ShopGui;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OpenCommand extends BaseCommand {

    public OpenCommand(final TokenManagerPlugin plugin) {
        super(plugin, "open", "open <username> <shop>", null, 3, false, "show");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player target;

        if ((target = Bukkit.getPlayerExact(args[1])) == null) {
            sendMessage(sender, true, "ERROR.player-not-found", "input", args[1]);
            return;
        }

        final String name = args[2].toLowerCase();
        final Optional<Shop> shop = shopConfig.getShop(name);

        if (!shop.isPresent()) {
            sendMessage(sender, true, "ERROR.shop-not-found", "input", name);
            return;
        }

        shopManager.open(target, new ShopGui(plugin, shop.get()));
        sendMessage(sender, true, "COMMAND.tokenmanager.open", "name", name, "player", target.getName());
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 3) {
            return shopConfig.getShops().stream().map(Shop::getName)
                .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                .collect(Collectors.toList());
        }

        return null;
    }
}
