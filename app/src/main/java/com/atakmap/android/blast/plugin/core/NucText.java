package com.atakmap.android.blast.plugin.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Wording for nuclear reference incidents: ring titles, the card, the
 * remarks block and the position check. Protective-action wording follows
 * FEMA's Planning Guidance for Response to a Nuclear Detonation (3rd ed.,
 * 2022), a US government work.
 */
public final class NucText {

    public static final String REMARKS_PREFIX = "[BLAST-NUC]";
    public static final String REMARKS_END = "Planning estimate, not a forecast.";

    public static final String BANNER = "PLANNING ESTIMATE. Follow official "
            + "instructions. Measure radiation with a dose-rate meter.";

    private NucText() {
    }

    /** One drawn ring or contour. */
    public enum Ring {
        SDZ(".sdz", "SEVERE DAMAGE ZONE", "Severe Damage Zone"),
        MDZ(".mdz", "MODERATE DAMAGE ZONE", "Moderate Damage Zone"),
        LDZ(".ldz", "LIGHT DAMAGE ZONE", "Light Damage Zone"),
        BURN(".burn", "BURNS (2nd degree)", "Burns (2nd degree)"),
        RAD(".rad", "INITIAL RADIATION 100 rad", "Initial radiation 100 rad"),
        GET_INSIDE(".gi", "GET INSIDE, STAY INSIDE, STAY TUNED",
                "50-mile Get Inside area");

        public final String suffix;
        public final String title;
        public final String label;

        Ring(String suffix, String title, String label) {
            this.suffix = suffix;
            this.title = title;
            this.label = label;
        }

        public double radiusM(NucRecord r) {
            switch (this) {
                case SDZ:
                    return r.radii.sdzM;
                case MDZ:
                    return r.radii.mdzM;
                case LDZ:
                    return r.radii.ldzM;
                case BURN:
                    return r.radii.burnM;
                case RAD:
                    return r.radii.radM;
                default:
                    return NucModel.GET_INSIDE_M;
            }
        }
    }

    /** Fallout contour suffix: ".fo100". */
    public static String falloutSuffix(int rate) {
        return ".fo" + rate;
    }

    /** "10 kt, surface burst" / "455 kt, burst unknown". */
    public static String threat(NucRecord r) {
        String b;
        switch (r.burst) {
            case SURFACE:
                b = "surface burst";
                break;
            case AIR:
                b = "air burst (envelope)";
                break;
            default:
                b = "burst unknown (worst case)";
        }
        return "Nuclear detonation " + r.yieldLabel() + ", " + b;
    }

    /** Distance in miles and km, two decimals below 1 mi. */
    public static String dist(double m) {
        double mi = m / NucModel.M_PER_MI;
        String smi = mi < 1 ? two(mi) : Formats.decimal(mi);
        double km = m / 1000.0;
        String skm = km < 1 ? two(km) : Formats.decimal(km);
        return smi + " mi (" + skm + " km)";
    }

    private static String two(double v) {
        long h = Math.round(v * 100);
        return (h / 100) + "." + (h % 100 < 10 ? "0" : "") + (h % 100);
    }

    private static String airTag(NucRecord r) {
        return r.burst == NucModel.Burst.SURFACE ? "" : ", air envelope";
    }

    public static String ringTitle(NucRecord r, Ring ring) {
        String pre = r.isCleared() ? "ENDED " : "";
        if (ring == Ring.GET_INSIDE)
            return pre + "NUC: GET INSIDE, STAY INSIDE, STAY TUNED within "
                    + "50 mi (FEMA)";
        return pre + "NUC " + ring.title + " " + dist(ring.radiusM(r))
                + (ring.ordinal() <= Ring.LDZ.ordinal() ? airTag(r) : "")
                + " - planning estimate";
    }

    public static String falloutTitle(NucRecord r, int rate) {
        return (r.isCleared() ? "ENDED " : "") + "NUC FALLOUT " + rate
                + " R/h at H+1 (idealized, wind from "
                + Math.round(r.windFromDeg) + "° at " + Math.round(r.windMph)
                + " mph) - not a forecast";
    }

    /** What each zone means, from FEMA's descriptions (3rd ed. Fig. 4). */
    public static String meaning(Ring ring) {
        switch (ring) {
            case SDZ:
                return "Few buildings standing, few survivors, dangerous "
                        + "radiation outdoors. People inside large structures "
                        + "may survive and should keep sheltering for the "
                        + "first 24 hours if it is safe to stay.";
            case MDZ:
                return "Heavy building damage, rubble, downed poles and "
                        + "fires. Many serious injuries: early medical help "
                        + "saves lives. Evacuate when it is safe, first "
                        + "where fire threatens.";
            case LDZ:
                return "Most windows shattered. Injuries mostly from flying "
                        + "glass and debris, highly survivable.";
            case BURN:
                return "Second-degree burns likely to bare skin with a direct "
                        + "view of the fireball (clear day). Any shade or "
                        + "clothing protects; haze shortens and snow or a "
                        + "cloud layer above can lengthen this distance.";
            case RAD:
                return "Unshielded outdoor dose from the first minute: "
                        + "radiation sickness possible inside this ring. It "
                        + "is received at once, so it is not a reason to "
                        + "move; buildings cut it.";
            default:
                return "FEMA: give the Get Inside message to everyone within "
                        + "50 miles until fallout areas are identified.";
        }
    }

    /** Protective actions, in order (FEMA 3rd ed. pp. 21, 43-46). */
    public static final List<String> DO_NOW = Collections.unmodifiableList(
            Arrays.asList(
                    "GET INSIDE, STAY INSIDE, STAY TUNED. This is the most "
                            + "important protective action (FEMA).",
                    "Get inside a basement or the middle of a large, dense "
                            + "building (concrete or brick). It is best to "
                            + "be in a shelter when fallout arrives; any "
                            + "shelter is better than staying outside.",
                    "Fallout will likely take 10 minutes or more to arrive: "
                            + "use them to reach the best shelter close by. "
                            + "Cars and light buildings give little "
                            + "protection.",
                    "Stay inside for 12-24 hours unless officials say "
                            + "otherwise, or fire, collapse or a medical "
                            + "emergency forces you to move.",
                    "Stay tuned: AM/FM radio is best; TV, phone or internet "
                            + "if they work.",
                    "If you were outside when fallout fell: remove or change "
                            + "the outer layer of clothing and footwear and "
                            + "brush or wipe exposed skin."));

    /** If the detonation hasn't happened or the flash is seen. */
    public static final String FLASH = "If you see an intense, unexpected "
            + "flash: take cover at once, away from windows. The blast wave "
            + "can follow by seconds.";

    public static final List<String> CAVEATS = Collections.unmodifiableList(
            Arrays.asList(
                    "Reference distances for planning, not a prediction of "
                            + "any real event. Actual damage depends on "
                            + "yield, burst height, terrain, buildings and "
                            + "weather.",
                    "Blast zones are about ±20%, burns ±25%, initial "
                            + "radiation within a factor of 0.5-2 "
                            + "(Glasstone & Dolan).",
                    "Air burst: every ring is drawn at its own outer bound "
                            + "over all burst heights. No single burst "
                            + "produces all of them at once.",
                    "Fallout contours are idealized averages, not a "
                            + "forecast. Rain or snow can create hot spots "
                            + "anywhere. Only a meter tells you the dose "
                            + "rate."));

    /** Basis line for the card. */
    public static String basis(NucRecord r) {
        String s = "Damage zones: FEMA Planning Guidance 3rd ed. (0.5 / 1 / "
                + "3 mi at 10 kT), cube-root scaled";
        if (r.burst != NucModel.Burst.SURFACE)
            s += ", times Glasstone's air-burst envelope ratio";
        s += ". Burns, radiation, fallout: Glasstone & Dolan 1977.";
        if (NucModel.extrapolated(r.yieldKt))
            s += " This yield is outside Glasstone's plotted 1 kt-10 Mt "
                    + "range: burn and radiation rings are extrapolated.";
        return s;
    }

    public static String provenance(NucRecord r) {
        String s = "Model " + r.modelId + " " + r.modelEd + ". " + threat(r)
                + ".";
        if (!r.presetLabel.isEmpty()) {
            NucPresets.Preset p = NucPresets.byId(r.presetId);
            s += " Preset: " + r.presetLabel;
            if (p != null && !p.source.isEmpty())
                s += " (" + p.note + "; " + p.source + ")";
            s += ".";
        }
        if (!r.byCallsign.isEmpty())
            s += " Placed by " + r.byCallsign;
        if (r.timeMillis > 0)
            s += (r.byCallsign.isEmpty() ? " Placed " : ", ")
                    + Formats.dtg(r.timeMillis);
        if (!r.byCallsign.isEmpty() || r.timeMillis > 0)
            s += ".";
        if (r.isCleared())
            s += " Ended" + (r.clearedBy.isEmpty() ? "" : " by " + r.clearedBy)
                    + (r.clearedMillis > 0 ? " " + Formats.dtg(r.clearedMillis)
                            : "")
                    + ".";
        return s;
    }

    /** Fallout line for the card. */
    public static String falloutLine(NucRecord r) {
        if (r.burst == NucModel.Burst.AIR)
            return "Air burst: little local fallout is expected, but rain or "
                    + "snow can still bring it down, and the area near ground "
                    + "zero may be radioactive. Shelter anyway until told "
                    + "otherwise.";
        if (!r.hasWind())
            return "Fallout direction unknown: add the wind to draw idealized "
                    + "fallout contours. Until then everyone within 50 miles "
                    + "should get inside.";
        NucModel.Contour c = NucModel.contour(r.yieldKt, 10, r.windMph);
        return "Fallout drifts toward " + Math.round(r.downwindDeg())
                + "° (wind from " + Math.round(r.windFromDeg) + "° at "
                + Math.round(r.windMph) + " mph). The idealized 10 R/h (H+1) "
                + "contour reaches about " + dist(c.downwindM)
                + " downwind. Real fallout follows the winds aloft.";
    }

    public static String remarks(NucRecord r) {
        StringBuilder sb = new StringBuilder(REMARKS_PREFIX).append(' ');
        if (r.isCleared())
            sb.append("ENDED. ");
        sb.append(threat(r)).append(". ");
        sb.append("GET INSIDE, STAY INSIDE, STAY TUNED (FEMA). ");
        sb.append("Severe damage ").append(dist(r.radii.sdzM))
                .append(", moderate ").append(dist(r.radii.mdzM))
                .append(", light ").append(dist(r.radii.ldzM))
                .append(". Burns to bare skin ").append(dist(r.radii.burnM))
                .append(". Initial radiation 100 rad ")
                .append(dist(r.radii.radM)).append(". ");
        if (r.hasWind() && r.burst != NucModel.Burst.AIR)
            sb.append("Wind from ").append(Math.round(r.windFromDeg))
                    .append("° at ").append(Math.round(r.windMph))
                    .append(" mph. ");
        sb.append(BANNER).append(' ');
        sb.append(REMARKS_END);
        return sb.toString();
    }

    public static String mergeRemarks(String existing, NucRecord r) {
        return Labels.mergeBlock(existing, remarks(r), REMARKS_PREFIX,
                REMARKS_END);
    }

    // ------------------------------------------------------------------
    // Where am I?

    /** Position relative to the incident. */
    public static final class Where {
        public final double distanceM;
        /** Innermost damage / effect ring containing the point, or null. */
        public final Ring zone;
        public final boolean inBurn, inRad;
        /** Highest H+1 contour containing the point, 0 if none or no wind. */
        public final int falloutRate;
        /** Hours after detonation fallout may arrive; NaN if not downwind. */
        public final double arrivalH;

        Where(double distanceM, Ring zone, boolean inBurn, boolean inRad,
                int falloutRate, double arrivalH) {
            this.distanceM = distanceM;
            this.zone = zone;
            this.inBurn = inBurn;
            this.inRad = inRad;
            this.falloutRate = falloutRate;
            this.arrivalH = arrivalH;
        }

        public String headline() {
            String z = zone == null ? "beyond 50 miles"
                    : zone == Ring.GET_INSIDE ? "outside the damage zones, "
                            + "inside the 50-mile Get Inside area"
                            : "in the " + zone.label;
            return dist(distanceM) + " from ground zero: " + z + ".";
        }

        public String detail() {
            StringBuilder sb = new StringBuilder();
            if (inBurn)
                sb.append("Inside the burn ring. ");
            if (inRad)
                sb.append("Inside the initial-radiation ring. ");
            if (falloutRate > 0) {
                sb.append("Inside the idealized ").append(falloutRate)
                        .append(" R/h (H+1) fallout contour");
                if (!Double.isNaN(arrivalH))
                    sb.append("; fallout could arrive about ")
                            .append(arrival(arrivalH))
                            .append(" after the detonation");
                sb.append(". ");
            }
            sb.append(zone == null
                    ? "Follow official instructions."
                    : "Get inside the best shelter close by and stay inside.");
            return sb.toString().trim();
        }
    }

    static String arrival(double h) {
        if (h < 1) {
            long min = Math.max(1, Math.round(h * 60));
            return min + " min";
        }
        return Formats.decimal(h) + " h";
    }

    /**
     * Where a point is; null if the distance is unusable. {@code eastM} /
     * {@code northM} are the point's offsets from ground zero.
     */
    public static Where where(NucRecord r, double eastM, double northM) {
        double d = Math.hypot(eastM, northM);
        if (Double.isNaN(d) || Double.isInfinite(d))
            return null;
        Ring zone = null;
        for (Ring ring : new Ring[] { Ring.SDZ, Ring.MDZ, Ring.LDZ,
                Ring.GET_INSIDE }) {
            if (d < ring.radiusM(r)) {
                zone = ring;
                break;
            }
        }
        int rate = 0;
        double arrival = Double.NaN;
        if (r.drawsFallout()) {
            double[] xy = toAxis(r, eastM, northM);
            for (int fr : NucModel.FALLOUT_RATES) {
                NucModel.Contour c = NucModel.contour(r.yieldKt, fr,
                        r.windMph);
                if (Polygons.contains(NucModel.outline(c), xy[0], xy[1])) {
                    rate = fr;
                    break;
                }
            }
            if (xy[0] > 0) {
                double mph = Math.max(NucModel.WIND_MIN_MPH, r.windMph);
                arrival = xy[0] / NucModel.M_PER_MI / mph;
            }
            if (rate == 0)
                arrival = Double.NaN;
        }
        return new Where(d, zone, d < r.radii.burnM, d < r.radii.radM, rate,
                arrival);
    }

    /** East/north offsets to (downwind, left) axis coordinates. */
    static double[] toAxis(NucRecord r, double eastM, double northM) {
        double th = Math.toRadians(r.downwindDeg());
        // Unit vector downwind (bearing th): east = sin, north = cos.
        double ux = Math.sin(th), uy = Math.cos(th);
        double along = eastM * ux + northM * uy;
        // Left of the downwind direction.
        double left = -eastM * uy + northM * ux;
        return new double[] { along, left };
    }

    /** Team counts: {inside LDZ, inside any fallout contour, total}. */
    public static int[] count(NucRecord r, double[][] eastNorth) {
        int inLdz = 0, inFallout = 0, n = 0;
        if (eastNorth == null)
            return new int[] { 0, 0, 0 };
        for (double[] en : eastNorth) {
            if (en == null)
                continue;
            Where w = where(r, en[0], en[1]);
            if (w == null)
                continue;
            n++;
            if (w.distanceM < r.radii.ldzM)
                inLdz++;
            if (w.falloutRate > 0)
                inFallout++;
        }
        return new int[] { inLdz, inFallout, n };
    }

    /** Card list of the rings: {label, distance, meaning}. */
    public static List<String[]> rows(NucRecord r) {
        List<String[]> out = new ArrayList<>();
        for (Ring ring : new Ring[] { Ring.SDZ, Ring.MDZ, Ring.LDZ, Ring.BURN,
                Ring.RAD }) {
            out.add(new String[] { ring.label,
                    dist(ring.radiusM(r)), meaning(ring) });
        }
        return out;
    }
}
