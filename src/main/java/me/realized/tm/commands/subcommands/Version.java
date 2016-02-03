package me.realized.tm.commands.subcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class Version extends SubCommand {

    public Version() {
        super(new String[]{"version", "v"}, "version", "use", 1);
    }

    @Override
    public void run(CommandSender sender, Command command, String[] args) {
        pm(sender, "&dThis server is running " + instance.getDescription().getFullName() + " by Realized.");
        pm(sender, "&bDownload TokenManager: https://www.spigotmc.org/resources/tokenmanager.8610/");
    }
}
