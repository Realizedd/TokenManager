package me.realized.tokenmanager.shop.gui.guis;

import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.data.DataManager;
import me.realized.tokenmanager.shop.Shop;
import me.realized.tokenmanager.shop.ShopManager;
import me.realized.tokenmanager.shop.gui.BaseGui;
import me.realized.tokenmanager.util.Placeholders;
import me.realized.tokenmanager.util.inventory.InventoryUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ConfirmGui extends BaseGui {

    public static final int CONFIRM_PURCHASE_SLOT = 10;
    public static final int CANCEL_PURCHASE_SLOT = 16;
    private static final int ITEM_SLOT = 13;

    private final DataManager dataManager;
    private final ShopManager shopManager;
    private final int slot;

    public ConfirmGui(final TokenManagerPlugin plugin, final Shop shop, final int slot) {
        super(plugin, shop, InventoryUtil.deepCopyOf(plugin.getShopConfig().getConfirmGuiSample()));
        this.dataManager = plugin.getDataManager();
        this.shopManager = plugin.getShopManager();
        this.slot = slot;
    }

    @Override
    public void refresh(final Player player, final boolean firstLoad) {
        final long balance = dataManager.get(player).orElse(0);
        final int cost = shop.getSlot(this.slot).getCost();
        inventory.setItem(CONFIRM_PURCHASE_SLOT, replace(player, inventory.getItem(CONFIRM_PURCHASE_SLOT), balance, cost));
        inventory.setItem(ITEM_SLOT, replace(player, shop.getSlot(this.slot).getDisplayed().clone(), balance, cost));
        inventory.setItem(CANCEL_PURCHASE_SLOT, replace(player, inventory.getItem(CANCEL_PURCHASE_SLOT), balance, cost));
    }

    private ItemStack replace(final Player player, final ItemStack item, final long balance, final int price) {
        Placeholders.replace(item, price, "price");
        Placeholders.replace(item, balance, "tokens", "balance");
        Placeholders.replace(item, player.getName(), "player");
        return item;
    }

    @Override
    public boolean handle(final Player player, final int slot) {
        if (slot == CONFIRM_PURCHASE_SLOT) {
            return shop.getSlot(this.slot).purchase(player, false, true);
        } else if (slot == CANCEL_PURCHASE_SLOT) {
            // Open shop
            shopManager.open(player, new ShopGui(plugin, shop));
        }

        return false;
    }
}
