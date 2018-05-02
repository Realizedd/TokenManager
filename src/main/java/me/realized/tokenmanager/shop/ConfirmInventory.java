package me.realized.tokenmanager.shop;

import lombok.Getter;
import me.realized.tokenmanager.util.inventory.ItemUtil;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

class ConfirmInventory {

    public static final int CONFIRM_PURCHASE_SLOT = 10;
    public static final int CANCEL_PURCHASE_SLOT = 16;
    private static final int ITEM_SLOT = 13;

    @Getter
    private final Inventory inventory;
    @Getter
    private Shop shop;
    @Getter
    private Slot slot;

    private final ItemStack confirmItem;

    ConfirmInventory(final Inventory inventory) {
        this.inventory = inventory;
        this.confirmItem = inventory.getItem(CONFIRM_PURCHASE_SLOT);
    }

    void update(final Shop target, final Slot data) {
        shop = target;
        slot = data;
        inventory.setItem(CONFIRM_PURCHASE_SLOT, ItemUtil.replace(confirmItem.clone(), "%price%", slot.getCost()));
        inventory.setItem(ITEM_SLOT, slot.getDisplayed().clone());
    }
}
