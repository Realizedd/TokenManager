package me.realized.tm.commands;

import me.realized.tm.Core;
import me.realized.tm.commands.subcommands.*;
import me.realized.tm.configuration.TMConfig;
import me.realized.tm.management.DataManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TMCommand implements CommandExecutor {

    private final TMConfig config;
    private final DataManager dataManager;
    private final List<SubCommand> subCommands;

    public TMCommand(Core instance) {
        config = instance.getTMConfig();
        dataManager = instance.getDataManager();
        subCommands = new ArrayList<>();
        subCommands.addAll(Arrays.asList(new Add(), new Open(), new Remove(), new Set(), new Reload()));
    }

    private void pm(CommandSender sender, String txt) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', txt));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("tokenmanager.admin")) {
            pm(sender, config.getString("no-permission").replace("%permission%", "tokenmanager.admin"));
            return true;
        }

        if (dataManager.hasSQLEnabled() && !dataManager.isConnected()) {
            pm(sender, "&c&lCould not connect to the database. Please contact an administrator.");
            return true;
        }

        if (args.length == 0) {
            for (String st : config.getList("tm-help-page")) {
                pm(sender, st);
            }

            return true;
        }


        for (SubCommand sub : subCommands) {
            boolean valid = false;

            for (String alias : sub.getNames()) {
                if (args[0].equalsIgnoreCase(alias)) {
                    valid = true;
                }
            }

            if (valid) {
                if (args.length < sub.getMinLength()) {
                    pm(sender, config.getString("sub-command-usage").replace("%usage%", sub.getUsage()).replace("%command%", command.getName()));
                    return true;
                }

                sub.run(sender, args);
                return true;
            }
        }

        pm(sender, config.getString("invalid-sub-command").replace("%input%", args[0]).replace("%command%", command.getName()));
        return false;
    }
}
