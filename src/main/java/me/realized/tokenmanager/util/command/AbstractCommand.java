/*
 *
 *   This file is part of TokenManager, licensed under the MIT License.
 *
 *   Copyright (c) Realized
 *   Copyright (c) contributors
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *   SOFTWARE.
 *
 */

package me.realized.tokenmanager.util.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import me.realized.tokenmanager.util.plugin.AbstractPluginDelegate;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class AbstractCommand<P extends JavaPlugin> extends AbstractPluginDelegate<P> implements TabCompleter {

    private final String name;
    private final String usage;
    private final String permission;
    private final boolean playerOnly;
    private final int length;
    private final List<String> aliases;

    private Map<String, AbstractCommand<P>> children;

    public AbstractCommand(final P plugin, final String name, final String usage, final String permission, final int length,
        final boolean playerOnly, final String... aliases) {
        super(plugin);
        this.name = name;
        this.usage = usage;
        this.permission = permission;
        this.length = length;
        this.playerOnly = playerOnly;
        this.aliases = Collections.unmodifiableList(Arrays.asList(aliases));
    }

    public final String getName() {
        return name;
    }

    public final String getUsage() {
        return usage;
    }

    public final String getPermission() {
        return permission;
    }

    public final int length() {
        return length;
    }

    public final boolean isPlayerOnly() {
        return playerOnly;
    }

    public final List<String> getAliases() {
        return aliases;
    }

    @SafeVarargs
    protected final void child(final AbstractCommand<P>... commands) {
        if (commands == null || commands.length == 0) {
            return;
        }

        if (children == null) {
            children = new HashMap<>();
        }

        for (AbstractCommand<P> child : commands) {
            // Command Name is contained in aliases.
            for (String alias : child.getAliases()) {
                children.put(alias.toLowerCase(), child);
            }
        }

        if (children != null && !children.isEmpty()) {
            getCommand().setTabCompleter((sender, command, alias, args) -> {
                if (args.length > 1) {
                    List<String> result;

                    for (AbstractCommand<P> child : children.values()) {
                        // Filter out unrelated sub-commands
                        if (!child.getAliases().contains(args[0].toLowerCase())) {
                            continue;
                        }

                        result = child.onTabComplete(sender, command, alias, args);

                        if (result != null) {
                            return result;
                        }
                    }
                }

                return onTabComplete(sender, command, alias, args);
            });
        }
    }

    protected abstract void execute(final CommandSender sender, final String label, final String[] args);

    public final void register() {
        final PluginCommand pluginCommand = getCommand();

        pluginCommand.setExecutor((sender, command, label, args) -> {
            if (isPlayerOnly() && !(sender instanceof Player)) {
                handleMessage(sender, MessageType.PLAYER_ONLY);
                return true;
            }

            if (permission != null && !sender.hasPermission(getPermission())) {
                handleMessage(sender, MessageType.NO_PERMISSION, label, args[0]);
                return true;
            }

            if (args.length > 0 && children != null) {
                AbstractCommand child = children.get(args[0].toLowerCase());

                if (child == null) {
                    handleMessage(sender, MessageType.SUB_COMMAND_INVALID, label, args[0]);
                    return true;
                }

                if (child.isPlayerOnly() && !(sender instanceof Player)) {
                    handleMessage(sender, MessageType.PLAYER_ONLY);
                    return true;
                }

                if (child.getPermission() != null && !sender.hasPermission(child.getPermission())) {
                    handleMessage(sender, MessageType.NO_PERMISSION, child.getPermission());
                    return true;
                }

                if (args.length < child.length) {
                    handleMessage(sender, MessageType.SUB_COMMAND_USAGE, label, child.getUsage());
                    return true;
                }

                child.execute(sender, label, args);
                return true;
            }

            execute(sender, label, args);
            return true;
        });

        // Use the default tab completer if no sub-commands exist.
        if (pluginCommand.getTabCompleter() == null) {
            pluginCommand.setTabCompleter(this);
        }
    }

    private PluginCommand getCommand() {
        PluginCommand pluginCommand = getPlugin().getCommand(name);

        if (pluginCommand == null) {
            throw new IllegalArgumentException("Command is not registered in plugin.yml");
        }

        return pluginCommand;
    }

    public void handleMessage(final CommandSender sender, final MessageType type, final String... args) {
        final String message;

        switch (type) {
            case PLAYER_ONLY:
                message = "This command can only be executed by a player!";
                break;
            case NO_PERMISSION:
                message = "You need the following permission: " + args[0];
                break;
            case SUB_COMMAND_INVALID:
                message = "'" + args[1] + "' is not a valid sub command. Type /" + args[0] + " for help.";
                break;
            case SUB_COMMAND_USAGE:
                message = "Usage: /" + args[0] + " " + args[1];
                break;
            default:
                message = "Invalid MessageType";
                break;
        }

        sender.sendMessage(ChatColor.RED + message);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        if (args.length == 0) {
            return null;
        }

        if (args.length == 1 && children != null) {
            return children.values().stream()
                .filter(child -> child.getName().startsWith(args[0].toLowerCase()))
                .map(AbstractCommand::getName)
                .distinct()
                .sorted(String::compareTo)
                .collect(Collectors.toList());
        }

        return null;
    }

    protected enum MessageType {

        PLAYER_ONLY,
        NO_PERMISSION,
        SUB_COMMAND_INVALID,
        SUB_COMMAND_USAGE
    }
}