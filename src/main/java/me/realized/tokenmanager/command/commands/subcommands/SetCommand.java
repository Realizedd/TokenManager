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

public class SetCommand extends BaseCommand {

    public SetCommand(TokenManagerPlugin plugin) {
        super(plugin, "set", "set <username> <amount>", null, 3, false);
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
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

                getDataManager().set(target.get(), true, 0, amount.getAsLong(), success -> {
                    if (success) {
                        sendMessage(sender, true, "on-remove", "amount", amount.getAsLong(), "player", args[1]);
                    } else {
                        sendMessage(sender, false, "&cThere was an error while executing this command, please contact an administrator.");
                    }
                });
            });
        });
//
//        Player target = Bukkit.getPlayerExact(args[1]);
//
//        if (target == null) {
//            sendMessage(sender, true, "invalid-player", "%input%", args[1]);
//            return;
//        }
//
//        if (!getDataManager().get(target).isPresent()) {
//            sendMessage(sender, false, "&cFailed to load data of " + target.getName() + ".");
//            return;
//        }
//
//        final OptionalInt amount = NumberUtil.parseInt(args[2]);
//
//        if (!amount.isPresent() || amount.getAsInt() <= 0) {
//            sendMessage(sender, true, "invalid-amount", "input", args[2]);
//            return;
//        }
//
//        getDataManager().set(target, amount.getAsInt());
//        sendMessage(sender, true, "on-set", "%amount%", amount.getAsInt(), "%player%", target.getName());
    }
}
