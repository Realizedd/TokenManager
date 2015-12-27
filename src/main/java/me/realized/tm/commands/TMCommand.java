package me.realized.tm.commands;

import me.realized.tm.Core;
import me.realized.tm.commands.subcommands.*;
import me.realized.tm.configuration.TMConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TMCommand implements CommandExecutor {

    private TMConfig config;
    private List<SubCommand> subCommands;

    public TMCommand(Core instance) {
        config = instance.getTMConfig();
        subCommands = new ArrayList<>();
        subCommands.addAll(Arrays.asList(new Add(), new Open(), new Remove(), new Set()));
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

        if (args.length == 0) {
            for (String st : config.getList("tm-help-page")) {
                pm(sender, st);
            }
            return true;
        }


        for (SubCommand sub : subCommands) {
            boolean valid = false;

            if (args[0].equalsIgnoreCase(sub.getName())) {
                valid = true;
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
