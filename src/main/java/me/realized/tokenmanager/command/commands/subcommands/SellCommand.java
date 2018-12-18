package me.realized.tokenmanager.command.commands.subcommands;

import java.util.OptionalLong;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.api.event.TMSellHandEvent;
import me.realized.tokenmanager.command.BaseCommand;
import org.apache.commons.lang.WordUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SellCommand extends BaseCommand {

    public SellCommand(final TokenManagerPlugin plugin) {
        super(plugin, "sell", "sell", "tokenmanager.use.sell", 1, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
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
        final TMSellHandEvent event = new TMSellHandEvent(player, price, item);
        plugin.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        player.getInventory().setItem(player.getInventory().getHeldItemSlot(), null);
        dataManager.set(player, dataManager.get(player).orElse(0) + price);

        final String name = WordUtils.capitalizeFully(item.getType().toString().replace("_", " ").toLowerCase());
        sendMessage(sender, true, "COMMAND.token.sell", "item_type", name, "item_amount", item.getAmount(), "amount", price);
    }
}
