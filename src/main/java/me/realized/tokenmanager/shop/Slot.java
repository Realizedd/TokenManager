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

import java.util.List;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

public class Slot {

    @Getter
    private final int slot;
    @Getter
    private final int cost;
    @Getter
    private final ItemStack displayed;
    @Getter
    private final String message;
    @Getter
    private final String subshop;
    @Getter
    private final List<String> commands;
    @Getter
    private final boolean usePermission;
    @Getter
    private final boolean confirmPurchase;

    public Slot(final int slot, final int cost, final ItemStack displayed, final String message, final String subshop, final List<String> commands, final boolean usePermission, final boolean confirmPurchase) {
        this.slot = slot;
        this.cost = cost;
        this.displayed = displayed;
        this.message = message;
        this.subshop = subshop;
        this.commands = commands;
        this.usePermission = usePermission;
        this.confirmPurchase = confirmPurchase;
    }
}
