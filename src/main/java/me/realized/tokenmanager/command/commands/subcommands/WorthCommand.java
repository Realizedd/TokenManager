package me.realized.tokenmanager.command.commands.subcommands;

import java.util.OptionalLong;
import me.realized.tokenmanager.Permissions;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.command.BaseCommand;
import org.apache.commons.lang.WordUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class WorthCommand extends BaseCommand {

    public WorthCommand(final TokenManagerPlugin plugin) {
        super(plugin, "worth", "worth", Permissions.CMD_WORTH, 1, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;

        if (args.length > getLength() && args[1].equalsIgnoreCase("all")) {
            if (!player.hasPermission(Permissions.CMD_WORTH_ALL)) {
                sendMessage(sender, true, "ERROR.no-permission", "permission", Permissions.CMD_WORTH_ALL);
                return;
            }

            final PlayerInventory inventory = player.getInventory();
            int total = 0;
            long price = 0;

            for (int slot = 0; slot < 36; slot++) {
                final ItemStack item = inventory.getItem(slot);

                if (item == null) {
                    continue;
                }

                final OptionalLong worth = plugin.getWorth(item);

                if (worth.isPresent()) {
                    price += worth.getAsLong();
                    total += item.getAmount();

                    final String name = WordUtils.capitalizeFully(item.getType().toString().replace("_", " ").toLowerCase());
                    sendMessage(sender, true, "COMMAND.token.worth-all.item-format", "item_type", name, "item_amount", item.getAmount(), "amount", price);
                }
            }

            sendMessage(sender, true, "COMMAND.token.worth-all.total", "item_amount", total, "amount", price);
            return;
        }

        final ItemStack item = player.getInventory().getItem(player.getInventory().getHeldItemSlot());

        if (item == null) {
            sendMessage(sender, true, "ERROR.no-item-in-hand");
            return;
        }

        final OptionalLong worth = plugin.getWorth(item);

        if (!worth.isPresent()) {
            sendMessage(sender, true, "ERROR.item-is-worthless");
            return;
        }

        final long price = worth.getAsLong();
        final String name = WordUtils.capitalizeFully(item.getType().toString().replace("_", " ").toLowerCase());
        sendMessage(sender, true, "COMMAND.token.worth", "item_type", name, "item_amount", item.getAmount(), "amount", price);
    }
}
