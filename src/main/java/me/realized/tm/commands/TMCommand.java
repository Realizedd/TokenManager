package me.realized.tm.commands;

import me.realized.tm.Core;
import me.realized.tm.commands.subcommands.*;
import me.realized.tm.configuration.Lang;
import me.realized.tm.data.DataManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TMCommand implements CommandExecutor {

    private final Lang lang;
    private final DataManager dataManager;
    private final List<SubCommand> commands = new ArrayList<>();

    public TMCommand(Core instance) {
        this.lang = instance.getLang();
        this.dataManager = instance.getDataManager();
        commands.addAll(Arrays.asList(new Add(), new Open(), new Remove(), new Set(), new Reload(), new GiveAll()));
    }

    private void pm(CommandSender sender, String txt) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', txt));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("tokenmanager.admin")) {
            pm(sender, lang.getString("no-permission").replace("%permission%", "tokenmanager.admin"));
            return true;
        }

        if (dataManager.hasSQLEnabled() && !dataManager.isConnected()) {
            pm(sender, "&4Could not connect to the database. Please contact an administrator.");
            return true;
        }

        if (args.length == 0) {
            for (String st : lang.getStringList("tm-help-page")) {
                pm(sender, st);
            }

            return true;
        }


        for (SubCommand subCommand : commands) {
            boolean valid = false;

            for (String alias : subCommand.getNames()) {
                if (args[0].equalsIgnoreCase(alias)) {
                    valid = true;
                }
            }

            if (valid) {
                if (args.length < subCommand.getMinLength()) {
                    pm(sender, lang.getString("sub-command-usage").replace("%usage%", subCommand.getUsage()).replace("%command%", label));
                    return true;
                }

                subCommand.run(sender, label, args);
                return true;
            }
        }

        pm(sender, lang.getString("invalid-sub-command").replace("%input%", args[0]).replace("%command%", label));
        return false;
    }
}
