package me.realized.tm.commands.subcommands;

import me.realized.tm.utilities.StringUtil;
import org.bukkit.command.CommandSender;

public class Shops extends SubCommand {

    public Shops() {
        super(new String[] {"shops"}, "shops", "use.shop", 1);
    }

    @Override
    public void run(CommandSender sender, String label, String[] args) {
        String shops = StringUtil.join(", ", getShopManager().getShops());
        pm(sender, getLang().getString("shops").replace("%shops%", shops));
    }
}
