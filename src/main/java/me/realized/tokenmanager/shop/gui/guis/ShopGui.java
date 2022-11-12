package me.realized.tokenmanager.shop.gui.guis;

import me.realized.tokenmanager.Permissions;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.config.Config;
import me.realized.tokenmanager.config.Lang;
import me.realized.tokenmanager.data.DataManager;
import me.realized.tokenmanager.shop.Shop;
import me.realized.tokenmanager.shop.Slot;
import me.realized.tokenmanager.shop.gui.BaseGui;
import me.realized.tokenmanager.util.Placeholders;
import me.realized.tokenmanager.util.compat.Items;
import me.realized.tokenmanager.util.inventory.InventoryUtil;
import me.realized.tokenmanager.util.inventory.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class ShopGui extends BaseGui {

    private final Config config;
    private final Lang lang;
    private final DataManager dataManager;

    public ShopGui(final TokenManagerPlugin plugin, final Shop shop) {
        super(plugin, shop, Bukkit.createInventory(null, shop.getSize(), shop.getTitle()));
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.dataManager = plugin.getDataManager();
    }

    @Override
    public void refresh(final Player player, final boolean firstLoad) {
        final long balance = dataManager.get(player).orElse(0);

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            final Slot data = shop.getSlot(slot);

            if (data == null) {
                continue;
            }

            ItemStack item = data.getDisplayed().clone();

            if (Items.equals(data.getDisplayed(), Items.HEAD)) {
                if (firstLoad) {
                    final SkullMeta skullMeta = (SkullMeta) item.getItemMeta();

                    if (skullMeta.getOwner() != null && skullMeta.getOwner().equals("%player%")) {
                        skullMeta.setOwner(player.getName());
                        item.setItemMeta(skullMeta);
                    }
                } else {
                    final ItemStack skull = inventory.getItem(slot);
                    ItemUtil.copyNameLore(item, skull);
                    item = skull;
                }
            }

            Placeholders.replace(item, balance, "tokens", "balance");
            Placeholders.replace(item, player.getName(), "player");
            inventory.setItem(slot, item);
        }
    }

    @Override
    public boolean handle(final Player player, final int slot) {
        final Slot data = shop.getSlot(slot);

        if (data == null) {
            return false;
        }

        final String slotInfo = shop.getName() + "-" + slot;

        if (!player.isOp() && !player.hasPermission(Permissions.CMD_ADMIN) && player.hasPermission(Permissions.SHOP_SLOT_CANCEL + slotInfo)) {
            plugin.doSync(player::closeInventory);
            lang.sendMessage(player, true, "ERROR.on-slot-cancel");
            return false;
        }

        if (data.isUsePermission() && !(player.hasPermission(Permissions.SHOP + slotInfo) || player.hasPermission(Permissions.SHOP_SLOT_OLD + slotInfo))) {
            plugin.doSync(player::closeInventory);
            lang.sendMessage(player, true, "ERROR.no-permission", "permission", Permissions.SHOP + slotInfo);
            return false;
        }

        if (data.getEmptySlotsRequired() > 0 && InventoryUtil.getEmptySlots(player.getInventory()) < data.getEmptySlotsRequired()) {
            plugin.doSync(player::closeInventory);
            lang.sendMessage(player, true, "ERROR.not-enough-space", "slots", data.getEmptySlotsRequired());
            return false;
        }

        if (data.purchase(player, shop.isConfirmPurchase() || data.isConfirmPurchase(), false)) {
            refresh(player, false);
            return true;
        }

        return false;
    }
}
