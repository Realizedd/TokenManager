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

import me.realized.tokenmanager.TokenManager;
import me.realized.tokenmanager.api.event.TokenReceiveEvent;
import me.realized.tokenmanager.command.BaseCommand;
import me.realized.tokenmanager.util.NumberUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SendCommand extends BaseCommand {

    public SendCommand(final TokenManager plugin) {
        super(plugin, "send", "send <username> <amount>", "tokenmanager.use.send", 3, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player target;

        if ((target = Bukkit.getPlayerExact(args[1])) == null) {
            sendMessage(sender, true, "invalid-player", "input", args[1]);
            return;
        }

        // TODO: 6/14/17 Check if target is sender to prevent token duplication

        final Optional<Integer> targetBalance = getDataManager().get(target);

        if (!targetBalance.isPresent()) {
            sendMessage(sender, false, "&cFailed to load data of " + target.getName() + ".");
            return;
        }

        final Optional<Integer> amount = NumberUtil.parseInt(args[2]);

        if (!amount.isPresent() || amount.get() <= 0) {
            sendMessage(sender, true, "invalid-amount", "input", args[2]);
            return;
        }

        final Player player = (Player) sender;
        final Optional<Integer> balance = getDataManager().get(player);

        if (!balance.isPresent()) {
            sendMessage(sender, true, "&cFailed to load data of " + sender.getName() + ".");
            return;
        }

        // TODO: 2/24/17 Instead of invalid-amount, use lack of money for msg
        if (balance.get() - amount.get() < 0) {
            sendMessage(sender, true, "invalid-amount", "input", args[2]);
            return;
        }

        final TokenReceiveEvent event = new TokenReceiveEvent(target.getUniqueId(), amount.get());
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        getDataManager().set(player, balance.get() - amount.get());
        getDataManager().set(target, targetBalance.get() + amount.get());
        sendMessage(sender, true, "on-send", "player", target.getName(), "amount", amount.get());
        sendMessage(target, true, "on-receive", "amount", amount.get());
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final org.bukkit.command.Command command, final String alias, final String[] args) {
        if (args.length == 3) {
            return Arrays.asList("5", "10", "25", "50", "100", "500", "1000");
        }

        return null;
    }
}