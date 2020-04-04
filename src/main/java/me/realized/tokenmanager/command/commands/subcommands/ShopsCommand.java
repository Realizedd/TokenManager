package me.realized.tokenmanager.command.commands.subcommands;

import java.util.stream.Collectors;
import me.realized.tokenmanager.Permissions;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.command.BaseCommand;
import me.realized.tokenmanager.shop.Shop;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

public class ShopsCommand extends BaseCommand {

    public ShopsCommand(final TokenManagerPlugin plugin) {
        super(plugin, "shops", "shops", Permissions.CMD_SHOP, 1, false);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        sendMessage(sender, true, "COMMAND.token.shops", "shops", StringUtils
            .join(shopConfig.getShops().stream().map(Shop::getName).collect(Collectors.toList()), ", "));
    }
}
