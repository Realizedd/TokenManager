package me.realized.tokenmanager.command.commands.subcommands;

import java.util.OptionalLong;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.command.BaseCommand;
import me.realized.tokenmanager.util.NumberUtil;
import org.apache.commons.lang.WordUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WorthCommand extends BaseCommand {

    public WorthCommand(final TokenManagerPlugin plugin) {
        super(plugin, "worth", "worth", "tokenmanager.use.worth", 1, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        ItemStack item = player.getInventory().getItem(player.getInventory().getHeldItemSlot());

        if (item == null) {
            sendMessage(sender, true, "ERROR.no-item-in-hand");
            return;
        }

        item = item.clone();
        final OptionalLong worth = plugin.getWorth(item);

        if (!worth.isPresent()) {
            sendMessage(sender, true, "ERROR.item-has-no-price");
            return;
        }

        if (args.length > getLength()) {
            item.setAmount((int) Math.max(NumberUtil.parseLong(args[1]).orElse(1), 1));
        }

        final long price = worth.getAsLong();
        final String name = WordUtils.capitalizeFully(item.getType().toString().replace("_", " ").toLowerCase());
        sendMessage(sender, true, "COMMAND.token.worth", "item_type", name, "item_amount", item.getAmount(), "amount", price);
    }
}
