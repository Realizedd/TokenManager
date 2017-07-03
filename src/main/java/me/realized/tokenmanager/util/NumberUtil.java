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

package me.realized.tokenmanager.util;

import java.util.Optional;

/**
 * Class created at 6/23/17 by Realized
 **/

public final class NumberUtil {

    private NumberUtil() {}

    /**
     *
     * Exactly the same as {@link Integer#parseInt(String)} but returns empty {@link Optional} instead of throwing exception.
     *
     * @param s String to parse.
     * @return {@link Optional} instance with parsed value inside or empty if string is invalid.
     */
    public static Optional<Integer> parseInt(String s) {
        if (s == null) {
            return Optional.empty();
        }

        int result = 0;
        boolean negative = false;
        int i = 0, len = s.length();
        int limit = -Integer.MAX_VALUE;
        int multmin;
        int digit;

        if (len > 0) {
            char firstChar = s.charAt(0);

            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Integer.MIN_VALUE;
                } else if (firstChar != '+')
                    return Optional.empty();
                if (len == 1) {
                    return Optional.empty();
                }

                i++;
            }

            multmin = limit / 10;

            while (i < len) {
                digit = Character.digit(s.charAt(i++), 10);

                if (digit < 0) {
                    return Optional.empty();
                }

                if (result < multmin) {
                    return Optional.empty();
                }

                result *= 10;

                if (result < limit + digit) {
                    return Optional.empty();
                }

                result -= digit;
            }
        } else {
            return Optional.empty();
        }

        return Optional.of(negative ? result : -result);
    }
}
