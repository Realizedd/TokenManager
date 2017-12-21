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

import java.util.OptionalLong;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.command.BaseCommand;
import me.realized.tokenmanager.util.NumberUtil;
import org.bukkit.command.CommandSender;

public class AddCommand extends BaseCommand {

    public AddCommand(final TokenManagerPlugin plugin) {
        super(plugin, "add", "add <username> <amount>", null, 3, false, "give");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        getTarget(args[1], target -> {
            if (!target.isPresent()) {
                sendMessage(sender, true, "invalid-player", "input", args[1]);
                return;
            }

            getDataManager().get(target.get(), balance -> {
                if (!balance.isPresent()) {
                    sendMessage(sender, false, "&cFailed to load data of " + args[1] + ".");
                    return;
                }

                final OptionalLong amount = NumberUtil.parseLong(args[2]);

                if (!amount.isPresent() || amount.getAsLong() <= 0) {
                    sendMessage(sender, true, "invalid-amount", "input", args[2]);
                    return;
                }

                getDataManager().set(target.get(), false, amount.getAsLong(), balance.getAsLong() + amount.getAsLong(), success -> {
                    if (success) {
                        sendMessage(sender, true, "on-add", "amount", amount.getAsLong(), "player", args[1]);
                    } else {
                        sendMessage(sender, false, "&cThere was an error while executing this command, please contact an administrator.");
                    }
                });
            });
        });

//        final TokenReceiveEvent event = new TokenReceiveEvent(target.getUniqueId(), amount.get());
//        Bukkit.getPluginManager().callEvent(event);
//
//        if (event.isCancelled()) {
//            return;
//        }
//
//        getDataManager().get(target, data -> {
//            if (!data.isPresent()) {
//                sendMessage(sender, false, "&cFailed to load data of " + target + ".");
//                return;
//            }
//
//
//        });

        //final Player target;
        //
        //if ((target = Bukkit.getPlayerExact(args[1])) == null) {
        //    sendMessage(sender, true, "invalid-player", "input", args[1]);
        //    return;
        //}
        //
        //final Optional<Integer> balance = getDataManager().get(target);
        //
        //if (!balance.isPresent()) {
        //    sendMessage(sender, false, "&cFailed to load data of " + target.getName() + ".");
        //    return;
        //}
        //
        //final Optional<Integer> amount;
        //
        //if (!(amount = NumberUtil.parseInt(args[2])).isPresent() || amount.get() <= 0) {
        //    sendMessage(sender, true, "invalid-amount", "input", args[2]);
        //    return;
        //}
        //
        //final TokenReceiveEvent event = new TokenReceiveEvent(target.getUniqueId(), amount.get());
        //Bukkit.getPluginManager().callEvent(event);
        //
        //if (event.isCancelled()) {
        //    return;
        //}
        //
        //getDataManager().set(target, balance.get() + amount.get());
        //sendMessage(sender, true, "on-add", "amount", amount.get(), "player", target.getName());
        //sendMessage(target, true, "on-receive", "amount", amount.get());
    }
}
