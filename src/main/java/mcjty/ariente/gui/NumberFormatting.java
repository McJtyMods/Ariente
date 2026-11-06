package mcjty.ariente.gui;

import java.util.Locale;

public final class NumberFormatting {

    private static final String[] SUFFIXES = {"", "k", "M", "G", "T", "P", "E"};

    private NumberFormatting() {
    }

    public static String compact(long value) {
        if (value > -1000 && value < 1000) {
            return Long.toString(value);
        }
        double amount = Math.abs((double) value);
        int unit = 0;
        while (amount >= 1000 && unit < SUFFIXES.length - 1) {
            amount /= 1000;
            unit++;
        }
        // Truncate so a value just below a unit boundary never displays as 1000k.
        double truncated = Math.floor(amount * 10) / 10;
        String digits = truncated < 100 && truncated != Math.floor(truncated)
                ? String.format(Locale.ROOT, "%.1f", truncated) : Long.toString((long) truncated);
        return (value < 0 ? "-" : "") + digits + SUFFIXES[unit];
    }
}
