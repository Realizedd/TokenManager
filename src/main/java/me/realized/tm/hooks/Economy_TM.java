package me.realized.tm.hooks;

import me.realized.tm.Core;
import me.realized.tm.data.Action;
import me.realized.tm.data.DataManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class Economy_TM implements Economy {

    private final DataManager manager;

    public Economy_TM(Core instance) {
        this.manager = instance.getDataManager();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "TM";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
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
    public boolean hasAccount(String name) {
        Player player = Bukkit.getPlayerExact(name);
        return player != null && hasAccount(player);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return (boolean) manager.executeAction(Action.EXISTS, player.getUniqueId(), 0);
    }

    @Override
    public boolean hasAccount(String name, String world) {
        return hasAccount(name);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String world) {
        return hasAccount(player);
    }

    @Override
    public double getBalance(String name) {
        Player player = Bukkit.getPlayerExact(name);
        return player != null ? getBalance(player) : 0;
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return (int) manager.executeAction(Action.BALANCE, player.getUniqueId(), 0);
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
        Player player = Bukkit.getPlayerExact(name);
        return player != null && getBalance(player) >= amount;
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
        Player player = Bukkit.getPlayerExact(name);

        if (player == null) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player must be online!");
        }

        return withdrawPlayer(player, (int) amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        manager.executeAction(Action.REMOVE, player.getUniqueId(), (int) amount);
        return new EconomyResponse(getBalance(player), amount, EconomyResponse.ResponseType.SUCCESS, "");
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
        Player player = Bukkit.getPlayer(name);

        if (player == null) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Player must be online!");
        }

        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        manager.executeAction(Action.ADD, player.getUniqueId(), (int) amount);
        return new EconomyResponse(getBalance(player), amount, EconomyResponse.ResponseType.SUCCESS, "");
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
    public boolean createPlayerAccount(String name) {
        Player player = Bukkit.getPlayer(name);

        return player != null && createPlayerAccount(player);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return (boolean) manager.executeAction(Action.CREATE, player.getUniqueId(), 0);
    }

    @Override
    public boolean createPlayerAccount(String name, String world) {
        return createPlayerAccount(name);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String world) {
        return createPlayerAccount(player);
    }

    // Unused methods - START

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

    // Unused methods - END
}
