package me.realized.tokenmanager.shop.gui.guis;

import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.shop.Shop;
import me.realized.tokenmanager.shop.ShopManager;
import me.realized.tokenmanager.shop.gui.BaseGui;
import me.realized.tokenmanager.util.inventory.InventoryUtil;
import me.realized.tokenmanager.util.inventory.ItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ConfirmGui extends BaseGui {

    public static final int CONFIRM_PURCHASE_SLOT = 10;
    public static final int CANCEL_PURCHASE_SLOT = 16;
    private static final int ITEM_SLOT = 13;

    private final ShopManager shopManager;
    private final int slot;

    public ConfirmGui(final TokenManagerPlugin plugin, final Shop shop, final int slot) {
        super(plugin, shop, InventoryUtil.deepCopyOf(plugin.getShopConfig().getConfirmGuiSample()));
        this.shopManager = plugin.getShopManager();
        this.slot = slot;
    }

    @Override
    public void refresh(final long balance) {
        final int cost = shop.getSlot(this.slot).getCost();
        inventory.setItem(CONFIRM_PURCHASE_SLOT, replace(inventory.getItem(CONFIRM_PURCHASE_SLOT), balance, cost));
        inventory.setItem(ITEM_SLOT, replace(shop.getSlot(this.slot).getDisplayed().clone(), balance, cost));
        inventory.setItem(CANCEL_PURCHASE_SLOT, replace(inventory.getItem(CANCEL_PURCHASE_SLOT), balance, cost));
    }

    private ItemStack replace(ItemStack item, final long balance, final int price) {
        item = ItemUtil.replace(item, price, "price");
        item = ItemUtil.replace(item, balance, "tokens", "balance");
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
