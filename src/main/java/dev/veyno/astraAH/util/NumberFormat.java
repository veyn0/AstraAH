package dev.veyno.astraAH.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumberFormat {

    public static String formatGerman(double value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.GERMAN);
        DecimalFormat format = new DecimalFormat("#,##0.###", symbols);
        return format.format(value);
    }

}
