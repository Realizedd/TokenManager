package me.realized.tokenmanager.util;

import java.text.DecimalFormat;
import java.util.OptionalLong;

public final class NumberUtil {

    private static final DecimalFormat COMMA_FORMAT = new DecimalFormat("#,###");

    /**
     * Copy of {@link Long#parseLong(String)} but returns an empty {@link OptionalLong} instead of throwing a {@link NumberFormatException}.
     *
     * @param s String to parse.
     * @return {@link OptionalLong} instance with parsed value inside or empty if string is invalid.
     */
    public static OptionalLong parseLong(final String s) throws NumberFormatException {
        if (s == null) {
            return OptionalLong.empty();
        }

        long result = 0;
        boolean negative = false;
        int i = 0, len = s.length();
        long limit = -Long.MAX_VALUE;
        long multmin;
        int digit;

        if (len > 0) {
            char firstChar = s.charAt(0);
            if (firstChar < '0') {
                if (firstChar == '-') {
                    negative = true;
                    limit = Long.MIN_VALUE;
                } else if (firstChar != '+') {
                    return OptionalLong.empty();
                }

                if (len == 1) {
                    return OptionalLong.empty();
                }

                i++;
            }
            multmin = limit / 10;
            while (i < len) {
                digit = Character.digit(s.charAt(i++), 10);

                if (digit < 0) {
                    return OptionalLong.empty();
                }
                if (result < multmin) {
                    return OptionalLong.empty();
                }
                result *= 10;
                if (result < limit + digit) {
                    return OptionalLong.empty();
                }
                result -= digit;
            }
        } else {
            return OptionalLong.empty();
        }

        return OptionalLong.of(negative ? result : -result);
    }

    public static String withCommas(final long n) {
        return COMMA_FORMAT.format(n);
    }

    // Source: https://stackoverflow.com/questions/9769554/how-to-convert-number-into-k-thousands-m-million-and-b-billion-suffix-in-jsp
    public static String withSuffix(final long n) {
        if (n < 1000) {
            return "" + n;
        }

        final int exp = (int) (Math.log(n) / Math.log(1000));
        return String.format("%.1f%c", n / Math.pow(1000, exp), "kMBTQ".charAt(exp - 1));
    }

    public static boolean isLower(String version, String otherVersion) {
        version = version.replace("-SNAPSHOT", "").replace(".", "");
        otherVersion = otherVersion.replace("-SNAPSHOT", "").replace(".", "");
        return NumberUtil.parseLong(version).orElse(0) < NumberUtil.parseLong(otherVersion).orElse(0);
    }


    private NumberUtil() {}
}
