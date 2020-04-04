package me.realized.tokenmanager.command.commands.subcommands;

import java.util.List;
import me.realized.tokenmanager.Permissions;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.command.BaseCommand;
import me.realized.tokenmanager.data.database.Database.TopElement;
import org.bukkit.command.CommandSender;

public class TopCommand extends BaseCommand {

    public TopCommand(final TokenManagerPlugin plugin) {
        super(plugin, "top", "top", Permissions.CMD_TOP, 1, false, "balancetop");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        sendMessage(sender, true, "COMMAND.token.balance-top.next-update", "remaining", dataManager.getNextUpdate());

        final List<TopElement> top = dataManager.getTopCache();

        sendMessage(sender, true, "COMMAND.token.balance-top.header", "total", (top != null ? top.size() : 0));

        if (top == null || top.isEmpty()) {
            sendMessage(sender, true, "ERROR.data-not-enough");
        } else {
            for (int i = 0; i < top.size(); i++) {
                final TopElement element = top.get(i);
                sendMessage(sender, true, "COMMAND.token.balance-top.display-format", "rank", i + 1, "name", element.getKey(), "tokens", element.getTokens());
            }
        }

        sendMessage(sender, true, "COMMAND.token.balance-top.footer");
    }
}
