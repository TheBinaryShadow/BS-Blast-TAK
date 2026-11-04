package com.atakmap.android.blast.plugin.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Military wording for nuclear incidents: Zone I / Zone II text and the
 * NBC 1 NUC observer report and NBC 3 NUC prediction line (FM 3-11.3 C1,
 * 30 Apr 2009, Figs. G-3 and G-26; line formats Table III-2). Line ALFA
 * (strike serial number) is assigned by the CBRN cell and never filled in.
 */
public final class NucReports {

    private NucReports() {
    }

    /** FM 3-11.3 G-23 zone definitions (exposed, unprotected personnel). */
    public static final String ZONE_I = "Zone I: immediate operational "
            + "concern. Exposed, unprotected personnel may receive 125 cGy or "
            + "more in a relatively short time (less than 4 hours after the "
            + "actual arrival of fallout). Casualties and major disruption may "
            + "occur in parts of this zone.";
    public static final String ZONE_II = "Zone II: secondary hazard. The "
            + "total dose to exposed, unprotected personnel is not expected to "
            + "reach 125 cGy within 4 hours of arrival, but may reach 75 cGy "
            + "or more within the first 24 hours.";
    public static final String OUTSIDE = "Outside both zones, exposed, "
            + "unprotected personnel may receive a total dose that does not "
            + "reach 75 cGy in the first 24 hours after arrival.";
    public static final List<String> CAVEATS = java.util.Collections
            .unmodifiableList(java.util.Arrays.asList(
                    "Military planning zones for troops, built on mission "
                            + "doses, not public safety: civilians shelter "
                            + "anyway (FEMA).",
                    "The prediction is an estimate only. Its boundaries are "
                            + "not dose-rate contours, and not every point "
                            + "inside will get dangerous fallout; even in "
                            + "Zone I units may not be affected (FM 3-11.3).",
                    "Uses the effective downwind (winds through the cloud), "
                            + "from an EDM. A surface wind is only a rough "
                            + "substitute.",
                    "Based on legacy public doctrine (FM 3-3-1, 1994; FM "
                            + "3-11.3, 2009). Current manuals (TM 3-11.32, "
                            + "ATP-45) are restricted and may differ."));

    public static String zoneTitle(NucRecord r, int zone) {
        return (r.isCleared() ? "ENDED " : "") + "NUC "
                + (zone == 1 ? "ZONE I (125 cGy in <4 h)"
                        : "ZONE II (75 cGy in 24 h)")
                + " - " + NucMil.SOURCE + " simplified prediction, estimate";
    }

    public static String arcTitle(NucRecord r, int hours) {
        return (r.isCleared() ? "ENDED " : "") + "NUC fallout H+" + hours
                + " (estimated arrival)";
    }

    /** "DELTA" style DTG: 201405ZSEP2005. */
    public static String dtg(long millis) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"),
                Locale.US);
        c.setTimeInMillis(millis);
        String[] mon = { "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL",
                "AUG", "SEP", "OCT", "NOV", "DEC" };
        return two(c.get(Calendar.DAY_OF_MONTH)) + two(c.get(Calendar.HOUR_OF_DAY))
                + two(c.get(Calendar.MINUTE)) + "Z" + mon[c.get(Calendar.MONTH)]
                + c.get(Calendar.YEAR);
    }

    private static String two(int v) {
        return v < 10 ? "0" + v : Integer.toString(v);
    }

    static String three(double deg) {
        long d = Math.round(((deg % 360) + 360) % 360) % 360;
        String s = Long.toString(d);
        while (s.length() < 3)
            s = "0" + s;
        return s;
    }

    /** HOTEL code for the burst choice. */
    public static String hotel(NucModel.Burst b) {
        return b == NucModel.Burst.SURFACE ? "SURF"
                : b == NucModel.Burst.AIR ? "AIR" : "UNK";
    }

    /** Summary of the prediction for the card. */
    public static String summary(NucMil.Prediction p) {
        String g = NucMil.group(p.yieldKt);
        StringBuilder sb = new StringBuilder();
        if (p.circular) {
            sb.append("Circular case (effective wind below 8 km/h): Zone I ")
                    .append(km(p.zoneIKm)).append(" around GZ, Zone II ")
                    .append(km(p.zoneIIKm)).append('.');
        } else {
            sb.append("Effective downwind ").append(three(p.eddDeg))
                    .append("° at ").append(Math.round(p.ewsKph))
                    .append(" km/h. Zone I ").append(km(p.zoneIKm))
                    .append(", Zone II ").append(km(p.zoneIIKm))
                    .append(" downwind, ").append(Math.round(p.angleDeg))
                    .append("° sector, cloud radius ")
                    .append(Formats.decimal(p.cloudRadiusKm)).append(" km");
            sb.append(g == null ? " (above the 3 MT yield groups: FM 3-3-1 "
                    + "Table E-1)" : " (yield group " + g + ")");
            sb.append('.');
            List<Integer> h = p.arrivalHours();
            if (!h.isEmpty())
                sb.append(" Arrival arcs every ").append(Math.round(p.ewsKph))
                        .append(" km (H+1 … H+").append(h.get(h.size() - 1))
                        .append(").");
            if (p.withinCloud)
                sb.append(" Zone I lies inside the cloud radius and is drawn "
                        + "as that circle.");
        }
        if (NucMil.extrapolated(p.yieldKt, p.ewsKph))
            sb.append(" Outside FM 3-3-1 Table E-4 (10 MT, 160 km/h): "
                    + "extrapolated.");
        return sb.toString();
    }

    static String km(double v) {
        return Math.round(v) + " km (" + Formats.decimal(v / 1.609344)
                + " mi)";
    }

    /** NBC 3 NUC PAPAB line (sss/xxx/rr/left/right, or xxx circular). */
    public static String papab(NucMil.Prediction p) {
        if (p.circular)
            return "PAPAB/" + pad3(Math.round(p.zoneIKm)) + "KM//";
        double h = p.angleDeg / 2;
        return "PAPAB/" + pad3(Math.round(p.ewsKph)) + "KPH/"
                + pad3(Math.round(p.zoneIKm)) + "KM/"
                + two((int) Math.ceil(p.cloudRadiusKm - 1e-9)) + "KM/"
                + three(p.eddDeg - h) + "DGT/" + three(p.eddDeg + h) + "DGT//";
    }

    static String pad3(long v) {
        String s = Long.toString(v);
        while (s.length() < 3)
            s = "0" + s;
        return s;
    }

    /** NBC 3 NUC-style prediction text (ALFA left to the CBRN cell). */
    public static String nbc3(NucRecord r, NucMil.Prediction p, String gzGrid) {
        List<String> l = new ArrayList<>();
        l.add("NBC3 NUC (Blast simplified prediction; ALFA from CBRN cell)");
        l.add("DELTA/" + dtg(r.timeMillis) + "//");
        l.add("FOXTROT/" + grid(gzGrid) + "/" + r.obs.gzQualifier + "//");
        l.add("HOTEL/" + hotel(r.burst) + "//");
        l.add("NOVEMBER/" + Formats.decimalTrim(r.yieldKt) + "//");
        l.add(papab(p));
        return join(l);
    }

    /** NBC 1 NUC observer report (FM 3-11.3 Fig. G-3). */
    public static String nbc1(NucRecord r, String observerGrid,
            String gzGrid) {
        NucRecord.Obs o = r.obs;
        List<String> l = new ArrayList<>();
        l.add("NBC1 NUC");
        String bravo = "BRAVO/" + grid(observerGrid);
        if (!Double.isNaN(o.azDeg))
            bravo += "/" + three(o.azDeg) + "DGT";
        l.add(bravo + "//");
        l.add("DELTA/" + dtg(r.timeMillis) + "//");
        if (gzGrid != null && !gzGrid.isEmpty())
            l.add("FOXTROT/" + grid(gzGrid) + "/" + o.gzQualifier + "//");
        l.add("GOLF/" + o.golfSo + "/" + o.golfDelivery + "/1/"
                + o.golfContainer + "/1//");
        l.add("HOTEL/" + hotel(r.burst) + "//");
        if (!Double.isNaN(o.flashToBangS))
            l.add("JULIET/" + Math.round(o.flashToBangS) + "//");
        if (!o.crater.isEmpty())
            l.add("KILO/" + o.crater + "//");
        if (!Double.isNaN(o.cloudWidthDeg))
            l.add("LIMA/" + three(o.cloudWidthDeg) + "DEG//");
        if (r.hasWind())
            l.add("YANKEE/" + three(r.downwindDeg()) + "DGT/"
                    + pad3(Math.round(r.windMph * NucRecord.KM_PER_MI))
                    + "KPH//");
        String gen = gentext(r);
        if (!gen.isEmpty())
            l.add("GENTEXT/" + gen + "//");
        return join(l);
    }

    /** How GZ was found, for GENTEXT. */
    static String gentext(NucRecord r) {
        NucRecord.Obs o = r.obs;
        if (!"EE".equals(o.gzQualifier))
            return "";
        if (!Double.isNaN(o.azDeg) && !Double.isNaN(o.flashToBangS))
            return "GZ ESTIMATED BY POLAR PLOT, AZIMUTH " + three(o.azDeg)
                    + "DGT, FLASH-TO-BANG " + Math.round(o.flashToBangS)
                    + " S X 0.35 KM/S = "
                    + Formats.decimal(NucMil.polarKm(o.flashToBangS)) + " KM";
        return "GZ ESTIMATED";
    }

    /** Grid as one token (spaces removed); "UNKNOWN" if empty. */
    static String grid(String g) {
        if (g == null)
            return "UNKNOWN";
        String s = g.replace(" ", "").toUpperCase(Locale.US);
        return s.isEmpty() ? "UNKNOWN" : s;
    }

    private static String join(List<String> l) {
        StringBuilder sb = new StringBuilder();
        for (String s : l)
            sb.append(s).append('\n');
        return sb.toString().trim();
    }

    /** What is still missing for a complete NBC 1 NUC, or empty. */
    public static List<String> nbc1Missing(NucRecord r, String observerGrid) {
        List<String> m = new ArrayList<>();
        if (observerGrid == null || observerGrid.isEmpty())
            m.add("BRAVO: your position (no GPS fix)");
        if (Double.isNaN(r.obs.azDeg))
            m.add("BRAVO: direction to the burst from you");
        return m;
    }

    /** FM 3-11.3 G-11 burst-type guidance for line HOTEL. */
    public static final String HOTEL_HELP = "Burst type (HOTEL): if the cloud "
            + "is lighter in colour than the stem, or the stem is ragged or "
            + "broken (not solidly joined to the cloud), it was an AIR burst. "
            + "If the stem is thick and dark and joins the cloud, SURFACE. "
            + "Otherwise, or at night, UNKNOWN.";
    public static final String FLASH_HELP = "Flash-to-bang (JULIET): at the "
            + "flash cover your eyes, get down and count seconds until the "
            + "shock wave arrives (there will be two). Distance = seconds x "
            + "0.35 km.";
    public static final String WIDTH_HELP = "Cloud width (LIMA): at H+5 "
            + "minutes, take compass bearings to the left and right edges of "
            + "the cloud; the difference is the angular width.";
    public static final String PRECEDENCE = "Precedence: FLASH for the first "
            + "nuclear use against your forces, otherwise IMMEDIATE. Send it "
            + "to the next higher headquarters (unit SOP).";
}
