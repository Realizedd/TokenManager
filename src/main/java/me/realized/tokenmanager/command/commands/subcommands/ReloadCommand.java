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

package me.realized.tokenmanager.command.commands.subcommands;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.command.BaseCommand;
import me.realized.tokenmanager.util.Loadable;
import me.realized.tokenmanager.util.Reloadable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends BaseCommand {

    public ReloadCommand(final TokenManagerPlugin plugin) {
        super(plugin, "reload", "reload", null, 1, false, "rl");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        if (args.length > getLength()) {
            final Optional<Loadable> target = plugin.find(args[1]);
            final Loadable loadable;

            if (!target.isPresent() || !((loadable = target.get()) instanceof Reloadable)) {
                sendMessage(sender, false, "&cInvalid module. Available: " + plugin.getReloadables());
                return;
            }

            final String name = loadable.getClass().getSimpleName();

            if (plugin.tryReload(loadable)) {
                sendMessage(sender, false, "&a[" + plugin.getDescription().getFullName() + "] Successfully reloaded " + name + ".");
            } else {
                sendMessage(sender, false, "&cAn error occured while reloading " + name + "! Please check the console for more information.");
            }

            return;
        }

        if (plugin.reload()) {
            sendMessage(sender, false, "&a[" + plugin.getDescription().getFullName() + "] Reload complete.");
        } else {
            sendMessage(sender, false, "&cAn error occured while reloading the plugin! The plugin will be disabled, please check the console for more information.");
        }
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 2) {
            return plugin.getReloadables().stream()
                .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
        }

        return null;
    }
}
