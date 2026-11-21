package jp.example.mekanismgizmo.util;

import java.util.Locale;

/**
 * Short number strings for the machine screens: numbers that fit a GUI line keep their thousands separators, larger
 * ones are shortened to one decimal with an SI style suffix, so a fusion machine full of compressed generators still
 * fits next to its slot grid.
 */
public final class AmountFormat {

    private static final String[] SUFFIXES = {"k", "M", "G", "T", "P", "E"};

    private AmountFormat() {
    }

    /** {@code 1728 -> "1,728"}, {@code 546048 -> "546,048"}, {@code 920371200 -> "920.4M"}. */
    public static String format(long value) {
        if (value > -1_000_000 && value < 1_000_000) {
            return String.format(Locale.ROOT, "%,d", value);
        }
        boolean negative = value < 0;
        double scaled = negative ? -(double) value : value;
        int suffix = -1;
        while (scaled >= 1_000 && suffix < SUFFIXES.length - 1) {
            scaled /= 1_000;
            suffix++;
        }
        String text = String.format(Locale.ROOT, "%.1f%s", scaled, SUFFIXES[suffix]);
        return negative ? "-" + text : text;
    }
}
