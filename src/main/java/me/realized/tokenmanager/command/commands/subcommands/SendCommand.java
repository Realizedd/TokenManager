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

import java.util.Arrays;
import java.util.List;
import java.util.OptionalLong;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.command.BaseCommand;
import me.realized.tokenmanager.event.TokenReceiveEvent;
import me.realized.tokenmanager.util.NumberUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SendCommand extends BaseCommand {

    public SendCommand(final TokenManagerPlugin plugin) {
        super(plugin, "send", "send <username> <amount>", "tokenmanager.use.send", 3, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player target;

        if ((target = Bukkit.getPlayerExact(args[1])) == null) {
            sendMessage(sender, true, "ERROR.player-not-found", "input", args[1]);
            return;
        }

        if (target.equals(sender)) {
            sendMessage(sender, true, "ERROR.target-is-self");
            return;
        }

        final OptionalLong targetBalance = dataManager.get(target);

        if (!targetBalance.isPresent()) {
            sendMessage(sender, false, "&cFailed to load data of " + target.getName() + ".");
            return;
        }

        final OptionalLong amount = NumberUtil.parseLong(args[2]);

        if (!amount.isPresent() || amount.getAsLong() <= 0) {
            sendMessage(sender, true, "ERROR.invalid-amount", "input", args[2]);
            return;
        }

        final Player player = (Player) sender;
        final OptionalLong balance = dataManager.get(player);

        if (!balance.isPresent()) {
            sendMessage(sender, true, "&cFailed to load data of " + sender.getName() + ".");
            return;
        }

        final long needed;

        if ((needed = balance.getAsLong() - amount.getAsLong()) < 0) {
            sendMessage(sender, true, "ERROR.not-enough-tokens", "needed", needed);
            return;
        }

        dataManager.set(player, balance.getAsLong() - amount.getAsLong());
        sendMessage(sender, true, "COMMAND.token.send", "player", target.getName(), "amount", amount.getAsLong());

        final TokenReceiveEvent event = new TokenReceiveEvent(target.getUniqueId(), (int) amount.getAsLong());
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        dataManager.set(target, targetBalance.getAsLong() + amount.getAsLong());
        sendMessage(target, true, "COMMAND.receive", "amount", amount.getAsLong());
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label,
        final String[] args) {
        if (args.length == 3) {
            return Arrays.asList("5", "10", "25", "50", "100", "500", "1000");
        }

        return null;
    }
}