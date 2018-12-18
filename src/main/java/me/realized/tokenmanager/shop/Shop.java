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

package me.realized.tokenmanager.shop;

import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import me.realized.tokenmanager.util.StringUtil;
import me.realized.tokenmanager.util.inventory.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Shop {

    @Getter
    private final String name;
    @Getter(value = AccessLevel.PACKAGE)
    private final Inventory inventory;
    @Getter
    private final boolean autoClose;
    @Getter
    private final boolean usePermission;
    @Getter
    private final boolean confirmPurchase;

    private Map<Integer, Slot> slots;

    public Shop(final String name, final String title, final int rows, final boolean autoClose, final boolean usePermission, final boolean confirmPurchase)
        throws IllegalArgumentException {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name is null or empty.");
        }

        if (name.contains("-")) {
            throw new IllegalArgumentException("Shop name cannot contain a dash. (This is implemented to prevent errors with shop slot permissions)");
        }

        this.name = name;

        if (title.length() > 32) {
            throw new IllegalArgumentException("Shop title cannot be longer than 32 characters.");
        }

        if (rows <= 0 || rows > 6) {
            throw new IllegalArgumentException("Shop rows must be in between 1 - 6.");
        }

        this.inventory = Bukkit.createInventory(null, rows * 9, StringUtil.color(title));
        this.autoClose = autoClose;
        this.usePermission = usePermission;
        this.confirmPurchase = confirmPurchase;
    }

    public String getTitle() {
        return inventory.getTitle();
    }

    public int getSize() {
        return inventory.getSize();
    }

    public void setSlot(final int slot, final ItemStack displayed, final Slot data) {
        ItemUtil.replace(displayed, data.getCost(), "%price%");
        inventory.setItem(slot, displayed);

        if (slots == null) {
            slots = new HashMap<>();
        }

        slots.put(slot, data);
    }

    public Slot getSlot(final int slot) {
        return slots != null ? slots.get(slot) : null;
    }
}
