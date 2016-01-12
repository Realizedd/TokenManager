package me.realized.tm.commands.subcommands;

import me.realized.tm.Core;
import me.realized.tm.configuration.TMConfig;
import me.realized.tm.management.DataManager;
import me.realized.tm.management.ShopManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public abstract class SubCommand {

    private final String[] names;
    private final String usage;
    private final String permission;
    private final int minLength;

    protected transient final Core instance = Core.getInstance();
    protected transient final DataManager dataManager = instance.getDataManager();
    protected transient final ShopManager shopManager = instance.getShopManager();
    protected transient final TMConfig config = instance.getTMConfig();

    public SubCommand(String[] names, String usage, String permission, int minLength) {
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

    protected void pm(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    public abstract void run(CommandSender sender, String[] args);
}
