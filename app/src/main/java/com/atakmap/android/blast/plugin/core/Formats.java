package com.atakmap.android.blast.plugin.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Units and text formatting. No String.format on resource text anywhere in
 * the plugin: everything here builds strings by concatenation.
 */
public final class Formats {

    private Formats() {
    }

    /** International foot, exact. */
    public static final double M_PER_FT = 0.3048;
    /** International avoirdupois pound, exact. */
    public static final double KG_PER_LB = 0.45359237;

    private static final String[] MONTHS = {
            "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP",
            "OCT", "NOV", "DEC"
    };
    private static final String[] MONTHS_TITLE = {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep",
            "Oct", "Nov", "Dec"
    };

    public static double ftToM(double ft) {
        return ft * M_PER_FT;
    }

    public static double mToFt(double m) {
        return m / M_PER_FT;
    }

    public static double kgToLb(double kg) {
        return kg / KG_PER_LB;
    }

    /** 1200 → "1,200". */
    public static String thousands(long v) {
        boolean neg = v < 0;
        String s = Long.toString(Math.abs(v));
        StringBuilder sb = new StringBuilder();
        int n = s.length();
        for (int i = 0; i < n; i++) {
            sb.append(s.charAt(i));
            int left = n - 1 - i;
            if (left > 0 && left % 3 == 0)
                sb.append(',');
        }
        return (neg ? "-" : "") + sb;
    }

    /** "1,200 ft". */
    public static String ft(long ft) {
        return thousands(ft) + " ft";
    }

    /**
     * Converted metres for a card distance, rounded UP so the converted
     * value is never closer to the device than the published feet.
     */
    public static long cardMetres(int ft) {
        // Subtract a tiny epsilon so exact metre values don't round up a step.
        return (long) Math.ceil(ftToM(ft) - 1e-9);
    }

    /** "1,200 ft (366 m)". */
    public static String cardDistance(int ft) {
        return ft(ft) + " (" + thousands(cardMetres(ft)) + " m)";
    }

    /**
     * Above this, measured distances switch to miles and kilometres. Every
     * card distance (up to 9,300 ft) stays in feet, so readouts near a zone
     * compare directly with the rings.
     */
    public static final double MILES_ABOVE_FT = 10000;

    /**
     * Measured distance, ordinary rounding: "450 ft (137 m)", or
     * "12.2 mi (19.6 km)" beyond {@link #MILES_ABOVE_FT}.
     */
    public static String measured(double metres) {
        double feet = mToFt(metres);
        if (feet >= MILES_ABOVE_FT)
            return decimal(feet / 5280.0) + " mi (" + decimal(metres / 1000.0)
                    + " km)";
        long ft = Math.round(feet);
        long m = Math.round(metres);
        return thousands(ft) + " ft (" + thousands(m) + " m)";
    }

    /** One decimal below 100, whole numbers with separators above. */
    static String decimal(double v) {
        if (v >= 100)
            return thousands(Math.round(v));
        long tenths = Math.round(v * 10.0);
        return thousands(tenths / 10) + "." + (tenths % 10);
    }

    /**
     * A user-entered quantity: integers as "800", otherwise one decimal
     * ("661.4"). Never rounds a value down to a row it doesn't reach.
     */
    public static String quantity(double v) {
        if (Math.abs(v - Math.rint(v)) < 1e-9)
            return thousands((long) Math.rint(v));
        double one = Math.ceil(v * 10.0 - 1e-9) / 10.0;
        long whole = (long) Math.floor(one);
        long tenth = Math.round((one - whole) * 10.0);
        if (tenth == 10) {
            whole++;
            tenth = 0;
        }
        return thousands(whole) + "." + tenth;
    }

    /** "2025-08" → "Aug 2025"; unknown formats are returned unchanged. */
    public static String editionLabel(String edition) {
        if (edition == null)
            return "";
        if (edition.length() == 7 && edition.charAt(4) == '-') {
            try {
                int y = Integer.parseInt(edition.substring(0, 4));
                int m = Integer.parseInt(edition.substring(5, 7));
                if (m >= 1 && m <= 12)
                    return MONTHS_TITLE[m - 1] + " " + y;
            } catch (NumberFormatException ignored) {
            }
        }
        return edition;
    }

    /** Military date-time group in Zulu: "301845Z OCT 26". */
    public static String dtg(long millis) {
        java.util.Calendar c = java.util.Calendar.getInstance(
                TimeZone.getTimeZone("UTC"), Locale.US);
        c.setTimeInMillis(millis);
        return two(c.get(java.util.Calendar.DAY_OF_MONTH))
                + two(c.get(java.util.Calendar.HOUR_OF_DAY))
                + two(c.get(java.util.Calendar.MINUTE)) + "Z "
                + MONTHS[c.get(java.util.Calendar.MONTH)] + " "
                + two(c.get(java.util.Calendar.YEAR) % 100);
    }

    private static String two(int v) {
        return v < 10 ? "0" + v : Integer.toString(v);
    }

    /** ISO 8601 UTC, CoT style: "2026-10-30T18:45:00.000Z". */
    public static String iso(long millis) {
        SimpleDateFormat f = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f.format(new Date(millis));
    }

    /** Parse {@link #iso} output (milliseconds optional); -1 if invalid. */
    public static long parseIso(String s) {
        if (s == null || s.isEmpty())
            return -1;
        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'"
        };
        for (String p : patterns) {
            SimpleDateFormat f = new SimpleDateFormat(p, Locale.US);
            f.setTimeZone(TimeZone.getTimeZone("UTC"));
            f.setLenient(false);
            try {
                Date d = f.parse(s);
                if (d != null)
                    return d.getTime();
            } catch (ParseException ignored) {
            }
        }
        return -1;
    }
}
