package me.realized.tm.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

public class Version extends SubCommand {

    public Version() {
        super(new String[] {"version", "v", "ver"}, "version", "use", 1);
    }

    @Override
    public void run(CommandSender sender, String label, String[] args) {
        PluginDescriptionFile desc = getInstance().getDescription();
        pm(sender, "&9This server is running &a" + desc.getFullName() + " &9by &a" + desc.getAuthors().toString().replace("[", "").replace("]", "") + "&9.");
        pm(sender, "&9Download: &ahttps://www.spigotmc.org/resources/tokenmanager.8610/");
    }
}
