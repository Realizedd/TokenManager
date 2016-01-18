package me.realized.tm.commands.subcommands;

import org.bukkit.command.CommandSender;

public class Reload extends SubCommand {

    public Reload() {
        super(new String[]{"reload", "rl"}, "reload", "admin", 1);
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        config.load();
        shopManager.close();
        dataManager.close();
        shopManager.load();
        dataManager.load();
        pm(sender, "&a&l[TM] Reloaded!");
    }
}
