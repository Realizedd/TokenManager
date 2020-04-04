package me.realized.tokenmanager.command.commands.subcommands;

import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.command.BaseCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class VersionCommand extends BaseCommand {

    public VersionCommand(final TokenManagerPlugin plugin) {
        super(plugin, "version", "version", null, 1, false, "v");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        sender.sendMessage(ChatColor.AQUA + plugin.getDescription().getFullName() + " by " + plugin.getDescription().getAuthors().get(0));
        sender.sendMessage(ChatColor.AQUA + "Download: " + plugin.getDescription().getWebsite());
    }
}
