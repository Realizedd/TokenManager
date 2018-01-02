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

public class ConfigConverter2_3 implements Converter {

    @Override
    public Map<String, String> renamedKeys() {
        final Map<String, String> keys = new HashMap<>();
        keys.put("use-default.enabled", "shop.open-selected.enabled");
        keys.put("use-default.shop", "shop.open-selected.shop");
        keys.put("mysql.enabled", "data.mysql.enabled");
        keys.put("mysql.hostname", "data.mysql.hostname");
        keys.put("mysql.port", "data.mysql.port");
        keys.put("mysql.username", "data.mysql.username");
        keys.put("mysql.password", "data.mysql.password");
        keys.put("mysql.database", "data.mysql.database");
        keys.put("click-delay", "shop.click-delay");
        keys.put("update-balance-top", "data.balance-top-update-interval");
        keys.put("vault-hooks", "data.register-economy");
        return keys;
    }
}
