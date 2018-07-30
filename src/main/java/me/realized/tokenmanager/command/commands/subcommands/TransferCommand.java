package me.realized.tokenmanager.command.commands.subcommands;

import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.command.BaseCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class TransferCommand extends BaseCommand {

    public TransferCommand(final TokenManagerPlugin plugin) {
        super(plugin, "transfer", "transfer", null, 1, false);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        dataManager.transfer(sender, error -> sender.sendMessage(ChatColor.RED + "Could not transfer data: " + error));
    }
}
