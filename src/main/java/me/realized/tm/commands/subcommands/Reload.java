package me.realized.tm.commands.subcommands;

import org.bukkit.command.CommandSender;

public class Reload extends SubCommand {

    public Reload() {
        super(new String[]{"reload", "rl"}, "reload", "admin", 1);
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        shopManager.closeShops();
        dataManager.close();
        shopManager.load();
        dataManager.load();
        config.load();
        pm(sender, "&a&l[TM] Reloaded!");
    }
}
