package me.realized.tm.commands.subcommands;

import me.realized.tm.Core;
import me.realized.tm.configuration.Config;
import me.realized.tm.configuration.Lang;
import me.realized.tm.data.DataManager;
import me.realized.tm.shop.ShopManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public abstract class SubCommand {

    private final String[] names;
    private final String usage;
    private final String permission;
    private final int minLength;

    private transient final Core instance = Core.getInstance();

    SubCommand(String[] names, String usage, String permission, int minLength) {
        this.names = names;
        this.usage = usage;
        this.permission = permission;
        this.minLength = minLength;
    }

    public String[] getNames() {
        return names;
    }

    public String getUsage() {
        return usage;
    }

    public String getPermission() {
        return permission;
    }

    public int getMinLength() {
        return minLength;
    }

    Config getConfig() {
        return instance.getConfiguration();
    }

    Lang getLang() {
        return instance.getLang();
    }

    DataManager getDataManager() {
        return instance.getDataManager();
    }

    ShopManager getShopManager() {
        return instance.getShopManager();
    }

    Core getInstance() {
        return instance;
    }

    void pm(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    public abstract void run(CommandSender sender, String label, String[] args);
}
