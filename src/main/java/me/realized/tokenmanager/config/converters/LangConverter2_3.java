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

package me.realized.tokenmanager.config.converters;

import java.util.HashMap;
import java.util.Map;
import me.realized.tokenmanager.util.config.convert.Converter;

public class LangConverter2_3 implements Converter {

    @Override
    public Map<String, String> renamedKeys() {
        final Map<String, String> keys = new HashMap<>();
        keys.put("no-permission", "ERROR.no-permission");
        keys.put("invalid-amount", "ERROR.invalid-amount");
        keys.put("invalid-player", "ERROR.player-not-found");
        keys.put("invalid-shop", "ERROR.shop-not-found");
        keys.put("invalid-sub-command", "ERROR.invalid-sub-command");
        keys.put("not-enough-tokens", "ERROR.balance-not-enough");
        keys.put("no-data", "ERROR.data-not-enough");
        keys.put("click-spamming", "ERROR.on-click-cooldown");
        keys.put("token-help-page", "COMMAND.token.usage");
        keys.put("tm-help-page", "COMMAND.tokenmanager.usage");
        keys.put("sub-command-usage", "COMMAND.sub-command-usage");
        keys.put("top-next-update", "COMMAND.token.balance-top.next-update");
        keys.put("top-header", "COMMAND.token.balance-top.header");
        keys.put("top-format", "COMMAND.token.balance-top.display-format");
        keys.put("top-footer", "COMMAND.token.balance-top.footer");
        keys.put("balance", "COMMAND.token.balance");
        keys.put("balance-others", "COMMAND.token.balance-other");
        keys.put("on-send", "COMMAND.token.send");
        keys.put("on-receive", "COMMAND.receive");
        keys.put("on-take", "COMMAND.take");
        keys.put("on-add", "COMMAND.tokenmanager.add");
        keys.put("on-remove", "COMMAND.tokenmanager.remove");
        keys.put("on-set", "COMMAND.tokenmanager.set");
        keys.put("on-open", "COMMAND.tokenmanager.open");
        keys.put("shops", "COMMAND.token.shops");
        keys.put("on-give-all", "COMMAND.tokenmanager.giveall");
        return keys;
    }
}
