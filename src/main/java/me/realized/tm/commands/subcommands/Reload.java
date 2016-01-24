package me.realized.tm.commands.subcommands;

import org.bukkit.command.CommandSender;

public class Reload extends SubCommand {

    public Reload() {
        super(new String[]{"reload", "rl"}, "reload", "admin", 1);
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        shopManager.close();
        dataManager.close();
        config.load();
        shopManager.load();
        dataManager.load();
        pm(sender, "&a&l" + instance.getDescription().getFullName() + " has been reloaded.");
    }
}
