package me.realized.tokenmanager.shop.gui.guis;

import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.data.DataManager;
import me.realized.tokenmanager.shop.Shop;
import me.realized.tokenmanager.shop.Slot;
import me.realized.tokenmanager.shop.gui.BaseGui;
import me.realized.tokenmanager.util.inventory.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ShopGui extends BaseGui {

    private final DataManager dataManager;

    public ShopGui(final TokenManagerPlugin plugin, final Shop shop) {
        super(plugin, shop, Bukkit.createInventory(null, shop.getSize(), shop.getTitle()));
        this.dataManager = plugin.getDataManager();
    }

    @Override
    public void refresh(final long balance) {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            final Slot data = shop.getSlot(slot);

            if (data == null) {
                continue;
            }

            inventory.setItem(slot, ItemUtil.replace(data.getDisplayed().clone(), balance, "tokens", "balance"));
        }
    }

    @Override
    public boolean handle(final Player player, final int slot) {
        final Slot data = shop.getSlot(slot);

        if (data == null) {
            return false;
        }

        if (data.isUsePermission() && !player.hasPermission("tokenmanager.use." + shop.getName() + "-" + slot)) {
            plugin.doSync(player::closeInventory);
            plugin.getLang().sendMessage(player, true, "ERROR.no-permission", "permission", "tokenmanager.use." + shop.getName() + "-" + slot);
            return false;
        }

        if (data.purchase(player, shop.isConfirmPurchase() || data.isConfirmPurchase(), false)) {
            refresh(dataManager.get(player).orElse(0));
            return true;
        }

        return false;
    }
}
