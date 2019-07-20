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
import me.realized.tokenmanager.Permissions;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.api.event.TMTokenSendEvent;
import me.realized.tokenmanager.command.BaseCommand;
import me.realized.tokenmanager.event.TokenReceiveEvent;
import me.realized.tokenmanager.util.NumberUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SendCommand extends BaseCommand {

    private static final List<String> TAB_AMOUNTS = Arrays.asList("5", "10", "25", "50", "75", "100", "500", "1000");

    public SendCommand(final TokenManagerPlugin plugin) {
        super(plugin, "send", "send <username> <amount>", Permissions.CMD_SEND, 3, true);
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

        if (config.isAltPrevention() && target.getAddress().getHostName().equals(((Player) sender).getAddress().getHostName())) {
            sendMessage(sender, true, "ERROR.target-has-same-ip");
            return;
        }

        final OptionalLong targetBalance = dataManager.get(target);

        if (!targetBalance.isPresent()) {
            sendMessage(sender, false, "&cFailed to load data of " + target.getName() + ".");
            return;
        }

        final long amount = NumberUtil.parseLong(args[2]).orElse(0);

        if (amount <= 0 || (config.getSendMin() > -1 && amount < config.getSendMin()) || (config.getSendMax() > -1 && amount > config.getSendMax())) {
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

        if ((needed = balance.getAsLong() - amount) < 0) {
            sendMessage(sender, true, "ERROR.balance-not-enough", "needed", Math.abs(needed));
            return;
        }

        final TMTokenSendEvent event = new TMTokenSendEvent(player, target, amount);
        plugin.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        dataManager.set(player, balance.getAsLong() - amount);
        sendMessage(sender, true, "COMMAND.token.send", "player", target.getName(), "amount", amount);

        final TokenReceiveEvent tokenReceiveEvent = new TokenReceiveEvent(target.getUniqueId(), (int) amount);
        Bukkit.getPluginManager().callEvent(tokenReceiveEvent);

        if (tokenReceiveEvent.isCancelled()) {
            return;
        }

        dataManager.set(target, targetBalance.getAsLong() + amount);
        sendMessage(target, true, "COMMAND.token.receive", "player", sender.getName(), "amount", amount);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 3) {
            return TAB_AMOUNTS;
        }

        return null;
    }
}