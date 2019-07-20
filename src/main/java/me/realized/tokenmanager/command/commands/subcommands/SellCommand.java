package me.realized.tokenmanager.command.commands.subcommands;

import java.util.OptionalLong;
import me.realized.tokenmanager.Permissions;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.api.event.TMSellAllEvent;
import me.realized.tokenmanager.api.event.TMSellHandEvent;
import me.realized.tokenmanager.command.BaseCommand;
import org.apache.commons.lang.WordUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class SellCommand extends BaseCommand {

    public SellCommand(final TokenManagerPlugin plugin) {
        super(plugin, "sell", "sell", Permissions.CMD_SELL, 1, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;

        if (args.length > getLength() && args[1].equalsIgnoreCase("all")) {
            if (!player.hasPermission(Permissions.CMD_SELL_ALL)) {
                sendMessage(sender, true, "ERROR.no-permission", "permission", Permissions.CMD_SELL_ALL);
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
                }
            }

            final TMSellAllEvent event = new TMSellAllEvent(player, price);
            plugin.getServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return;
            }

            for (int slot = 0; slot < 36; slot++) {
                final ItemStack item = inventory.getItem(slot);

                if (item == null) {
                    continue;
                }

                final OptionalLong worth = plugin.getWorth(item);

                if (worth.isPresent()) {
                    inventory.setItem(slot, null);
                }
            }

            dataManager.set(player, dataManager.get(player).orElse(0) + price);
            sendMessage(sender, true, "COMMAND.token.sell-all", "item_amount", total, "amount", price);
            return;
        }

        final int heldSlot = player.getInventory().getHeldItemSlot();
        final ItemStack item = player.getInventory().getItem(heldSlot);

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

        player.getInventory().setItem(heldSlot, null);
        dataManager.set(player, dataManager.get(player).orElse(0) + price);

        final String name = WordUtils.capitalizeFully(item.getType().toString().replace("_", " ").toLowerCase());
        sendMessage(sender, true, "COMMAND.token.sell", "item_type", name, "item_amount", item.getAmount(), "amount", price);
    }
}
