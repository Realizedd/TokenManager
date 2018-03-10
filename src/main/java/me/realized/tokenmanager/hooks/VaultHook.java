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

package me.realized.tokenmanager.hooks;

import java.util.List;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.util.hook.PluginHook;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.ServicePriority;

public class VaultHook extends PluginHook<TokenManagerPlugin> {

    public VaultHook(final TokenManagerPlugin plugin) {
        super(plugin, "Vault");
        if (plugin.getConfiguration().isRegisterEconomy()) {
            Bukkit.getServicesManager().register(Economy.class, new TokenManagerEconomy(), plugin, ServicePriority.Highest);
        }
    }

    private class TokenManagerEconomy implements Economy {

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public String getName() {
            return "TokenManager";
        }

        @Override
        public int fractionalDigits() {
            return -1;
        }

        @Override
        public String format(double v) {
            return v + " " + (v > 1 ? currencyNamePlural() : currencyNameSingular());
        }

        @Override
        public String currencyNamePlural() {
            return "tokens";
        }

        @Override
        public String currencyNameSingular() {
            return "token";
        }

        @Override
        public double getBalance(String name) {
            return getBalance(Bukkit.getPlayerExact(name));
        }

        @Override
        public double getBalance(OfflinePlayer player) {
            return player != null ? plugin.getTokens(player.getPlayer()).orElse(0) : 0;
        }

        @Override
        public double getBalance(String name, String world) {
            return getBalance(name);
        }

        @Override
        public double getBalance(OfflinePlayer player, String world) {
            return getBalance(player);
        }

        @Override
        public boolean has(String name, double amount) {
            return getBalance(name) >= (long) amount;
        }

        @Override
        public boolean has(OfflinePlayer player, double amount) {
            return getBalance(player) >= amount;
        }

        @Override
        public boolean has(String name, String world, double amount) {
            return has(name, amount);
        }

        @Override
        public boolean has(OfflinePlayer player, String world, double amount) {
            return has(player, amount);
        }

        @Override
        public EconomyResponse withdrawPlayer(String name, double amount) {
            return withdrawPlayer(Bukkit.getPlayerExact(name), amount);
        }

        @Override
        public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
            if (player == null) {
                return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player is not online");
            }

            final long balance = (long) getBalance(player);
            plugin.setTokens(player.getPlayer(), Math.abs(balance - (long) amount));
            return new EconomyResponse(balance - (long) amount, (long) amount, EconomyResponse.ResponseType.SUCCESS, "");
        }

        @Override
        public EconomyResponse withdrawPlayer(String name, String world, double amount) {
            return withdrawPlayer(name, amount);
        }

        @Override
        public EconomyResponse withdrawPlayer(OfflinePlayer player, String name, double amount) {
            return withdrawPlayer(player, amount);
        }

        @Override
        public EconomyResponse depositPlayer(String name, double amount) {
            return withdrawPlayer(Bukkit.getPlayer(name), amount);
        }

        @Override
        public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
            if (player == null) {
                return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player is not online");
            }

            final long balance = (long) getBalance(player);
            plugin.setTokens(player.getPlayer(), balance + (long) amount);
            return new EconomyResponse(balance + (long) amount, amount, EconomyResponse.ResponseType.SUCCESS, "");
        }

        @Override
        public EconomyResponse depositPlayer(String name, String world, double amount) {
            return depositPlayer(name, amount);
        }

        @Override
        public EconomyResponse depositPlayer(OfflinePlayer player, String world, double amount) {
            return depositPlayer(player, amount);
        }

        @Override
        public boolean hasBankSupport() {
            return false;
        }

        @Override
        public boolean createPlayerAccount(String name) {
            return true;
        }

        @Override
        public boolean createPlayerAccount(OfflinePlayer player) {
            return true;
        }

        @Override
        public boolean createPlayerAccount(String name, String world) {
            return true;
        }

        @Override
        public boolean createPlayerAccount(OfflinePlayer player, String world) {
            return true;
        }

        @Override
        public boolean hasAccount(String name) {
            return true;
        }

        @Override
        public boolean hasAccount(OfflinePlayer player) {
            return true;
        }

        @Override
        public boolean hasAccount(String name, String world) {
            return true;
        }

        @Override
        public boolean hasAccount(OfflinePlayer player, String world) {
            return true;
        }

        @Override
        public EconomyResponse createBank(String s, String s1) {
            return null;
        }

        @Override
        public EconomyResponse createBank(String s, OfflinePlayer offlinePlayer) {
            return null;
        }

        @Override
        public EconomyResponse deleteBank(String s) {
            return null;
        }

        @Override
        public EconomyResponse bankBalance(String s) {
            return null;
        }

        @Override
        public EconomyResponse bankHas(String s, double v) {
            return null;
        }

        @Override
        public EconomyResponse bankWithdraw(String s, double v) {
            return null;
        }

        @Override
        public EconomyResponse bankDeposit(String s, double v) {
            return null;
        }

        @Override
        public EconomyResponse isBankOwner(String s, String s1) {
            return null;
        }

        @Override
        public EconomyResponse isBankOwner(String s, OfflinePlayer offlinePlayer) {
            return null;
        }

        @Override
        public EconomyResponse isBankMember(String s, String s1) {
            return null;
        }

        @Override
        public EconomyResponse isBankMember(String s, OfflinePlayer offlinePlayer) {
            return null;
        }

        @Override
        public List<String> getBanks() {
            return null;
        }
    }
}
