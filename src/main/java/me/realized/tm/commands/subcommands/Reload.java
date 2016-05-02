package me.realized.tm.commands.subcommands;

import org.bukkit.command.CommandSender;

public class Reload extends SubCommand {

    public Reload() {
        super(new String[] {"reload", "rl"}, "reload", "admin", 1);
    }

    @Override
    public void run(CommandSender sender, String label, String[] args) {
        getShopManager().close();
        getConfig().load();
        getDataManager().reloadableMethods();
        getLang().load();
        getShopManager().load();
        pm(sender, "&a" + getInstance().getDescription().getFullName() + "&9: Reload complete.");
    }
}
