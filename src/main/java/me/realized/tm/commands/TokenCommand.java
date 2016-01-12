package me.realized.tm.commands;

import me.realized.tm.Core;
import me.realized.tm.commands.subcommands.*;
import me.realized.tm.configuration.TMConfig;
import me.realized.tm.management.DataManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TokenCommand implements CommandExecutor {

    private TMConfig config;
    private DataManager dataManager;
    private List<SubCommand> subCommands;

    public TokenCommand(Core instance) {
        config = instance.getTMConfig();
        dataManager = instance.getDataManager();
        subCommands = new ArrayList<>();
        subCommands.addAll(Arrays.asList(new Balance(), new Send(), new Shop(), new Shops(), new Top(), new Version()));
    }

    private void pm(CommandSender sender, String txt) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', txt));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("tokenmanager.use")) {
            pm(sender, config.getString("no-permission").replace("%permission%", "tokenmanager.use"));
            return true;
        }

        if (dataManager.hasSQLEnabled() && !dataManager.isConnected()) {
            pm(sender, "&c&lCould not connect to the database. Please contact an administrator.");
            return true;
        }

        if (args.length == 0) {
            String balance = sender instanceof Player ? String.valueOf(dataManager.balance(((Player) sender).getUniqueId())) : "0";
            for (String st : config.getList("token-help-page")) {
                pm(sender, st.replace("%tokens%", balance));
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
                if (!sender.hasPermission("tokenmanager." + sub.getPermission())) {
                    pm(sender, config.getString("no-permission").replace("%permission%", "tokenmanager." + sub.getPermission()));
                    return true;
                }

                if (args.length < sub.getMinLength() && (!config.isDefaultEnabled() || args[0].equalsIgnoreCase("shop"))) {
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
