package me.realized.tm.commands.subcommands;

import org.bukkit.command.CommandSender;

import java.util.List;

public class Top extends SubCommand {

    public Top() {
        super(new String[] {"top"}, "top", "use.top", 1);
    }

    @Override
    public void run(CommandSender sender, String label, String[] args) {
        pm(sender, getLang().getString("top-next-update").replace("%remaining%", getDataManager().getNextUpdate()));

        List<String> top = getDataManager().getTopBalances();

        pm(sender, getLang().getString("top-header").replace("%total%", String.valueOf(top.size())));

        for (String msg : top) {
            String[] data = msg.split(":");

            if (data.length <= 1) {
                pm(sender, msg);
                break;
            }

            String formatted = getLang().getString("top-format");
            formatted = formatted.replace("%rank%", data[0]).replace("%name%", data[2]).replace("%tokens%", data[1]);
            pm(sender, formatted);
        }

        pm(sender, getLang().getString("top-footer"));
    }
}
