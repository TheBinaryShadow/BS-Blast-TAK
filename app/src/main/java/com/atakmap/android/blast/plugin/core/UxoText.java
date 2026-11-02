package com.atakmap.android.blast.plugin.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Every UXO text: ring title, marker remarks (for clients without Blast),
 * the card's guidance, the position check and the reports. Guidance is
 * paraphrased from ATP 4-32.2 (01 Mar 2024), the UNMAS Landmines, ERW and
 * IED Safety Handbook (3rd ed., 2015) and IMAS 12.10 / TNMA 10.20/01.
 * Nothing here covers handling, fuzing, render-safe or disposal.
 */
public final class UxoText {

    private UxoText() {
    }

    public static final String REMARKS_PREFIX = "[BLAST-UXO]";

    // ------------------------------------------------------------------
    // Distances

    /** Feet for a cordon, rounded up to the next 10 ft (never smaller). */
    public static long cordonFt(int metres) {
        double ft = Formats.mToFt(metres);
        return (long) (Math.ceil(ft / 10.0 - 1e-9) * 10);
    }

    /** "375 m (1,240 ft)". */
    public static String cordon(int metres) {
        return Formats.thousands(metres) + " m (" + Formats.ft(cordonFt(metres))
                + ")";
    }

    /** What the radius is made of, e.g. "Fig. 31 + 50 m (more than one item)". */
    public static String basis(UxoRecord r) {
        if (!r.isKnownEdition())
            return "Distance as sent (" + r.sourceId + " " + r.edition
                    + "; not in this build).";
        StringBuilder sb = new StringBuilder();
        UxoTable.Category c = r.category();
        if (c != null && c.isUnknown()) {
            sb.append(r.large
                    ? "Unknown item that looks large: the Bombs and "
                            + "Dispensers distance (675 m)"
                    : "Unknown item: the 300 m minimum for exposed people");
        } else if (c != null) {
            sb.append(c.doctrineLabel).append(", ")
                    .append(Formats.thousands(c.cordonM)).append(" m");
        }
        if (r.multiple)
            sb.append(", +" + UxoTable.MULTI_ADD_M
                    + " m for more than one item (extend further if they are "
                    + "spread out)");
        sb.append(". ").append(UxoTable.SHORT_NAME).append(" Fig. ")
                .append(UxoTable.FIGURE).append(", ")
                .append(UxoTable.EDITION_LABEL).append('.');
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // Labels for any TAK client

    /** Title of the cordon circle. */
    public static String ringTitle(UxoRecord r) {
        return (r.isCleared() ? "CLEARED " : "") + "UXO CORDON "
                + cordon(r.cordonM) + " MIN - fragments may go farther";
    }

    /** "Artillery shell or mortar (more than one item)". */
    public static String threat(UxoRecord r) {
        String s = r.plainLabel();
        if (r.report.count > 1)
            s += " (" + r.report.count + " items)";
        else if (r.multiple)
            s += " (more than one item)";
        return s;
    }

    public static String provenance(UxoRecord r) {
        StringBuilder sb = new StringBuilder();
        sb.append(r.isKnownEdition()
                ? UxoTable.SHORT_NAME + " (" + UxoTable.EDITION_LABEL
                        + "), Fig. " + UxoTable.FIGURE + ", "
                        + r.doctrineLabel() + "."
                : r.sourceId + " " + r.edition + ".");
        sb.append(" Placed by ")
                .append(r.byCallsign.isEmpty() ? "unknown" : r.byCallsign);
        if (r.timeMillis > 0)
            sb.append(' ').append(Formats.dtg(r.timeMillis));
        sb.append('.');
        String plc = placement(r.placement);
        if (!plc.isEmpty())
            sb.append(" Position: ").append(plc).append('.');
        if (r.where != UxoRecord.Where.UNSET)
            sb.append(" Reported from: ").append(whereLabel(r.where))
                    .append('.');
        if (r.isCleared()) {
            sb.append(" CLEARED by ")
                    .append(r.clearedBy.isEmpty() ? "unknown" : r.clearedBy);
            if (r.clearedMillis > 0)
                sb.append(' ').append(Formats.dtg(r.clearedMillis));
            sb.append('.');
        }
        return sb.toString();
    }

    public static String placement(UxoRecord.Placement p) {
        switch (p) {
            case MAP:
                return "tapped on the map";
            case RB:
                return "range and bearing from the reporter (approximate)";
            case CENTER:
                return "map center";
            case MARKER:
                return "on an existing marker";
            default:
                return "";
        }
    }

    public static String whereLabel(UxoRecord.Where w) {
        switch (w) {
            case PATH:
                return "a road or path known to be safe";
            case AREA:
                return "possibly inside a minefield or bomblet area";
            case VEHICLE:
                return "a vehicle";
            default:
                return "";
        }
    }

    /** Marker remarks block (from REMARKS_PREFIX to Labels.REMARKS_END). */
    public static String remarks(UxoRecord r) {
        StringBuilder sb = new StringBuilder(REMARKS_PREFIX).append(' ');
        if (r.isCleared())
            sb.append("CLEARED. ");
        sb.append(threat(r)).append(". Minimum cordon ")
                .append(Formats.thousands(r.cordonM)).append(" m / ")
                .append(Formats.ft(cordonFt(r.cordonM))).append(" (")
                .append(basis(r)).append(") Fragments may travel farther. ")
                .append("Don't approach or touch it; keep people out; use "
                        + "radios and phones only outside the cordon; report "
                        + "to authorities.");
        String rep = reportSummary(r);
        if (!rep.isEmpty())
            sb.append(' ').append(rep);
        sb.append(" Placed by ")
                .append(r.byCallsign.isEmpty() ? "unknown" : r.byCallsign);
        if (r.timeMillis > 0)
            sb.append(' ').append(Formats.dtg(r.timeMillis));
        sb.append(". ").append(Labels.REMARKS_END);
        return sb.toString();
    }

    /** Short report details for the remarks ("" if none). */
    static String reportSummary(UxoRecord r) {
        UxoRecord.Report p = r.report;
        StringBuilder sb = new StringBuilder();
        if (!p.description.isEmpty())
            sb.append("Description: ").append(p.description).append('.');
        if (!p.landmark.isEmpty())
            sb.append(sb.length() > 0 ? " " : "").append("Location: ")
                    .append(p.landmark).append('.');
        if (p.priority != UxoRecord.Priority.UNSET)
            sb.append(sb.length() > 0 ? " " : "").append("Priority: ")
                    .append(p.priority.label).append('.');
        return sb.toString();
    }

    /** New remarks keeping anything people wrote themselves. */
    public static String mergeRemarks(String existing, UxoRecord r) {
        return Labels.mergeBlock(existing, remarks(r), REMARKS_PREFIX);
    }

    // ------------------------------------------------------------------
    // Guidance on the card

    /** The movement drill for one situation (ATP Ch. II; UNMAS 4.1). */
    public static String drill(UxoRecord.Where w) {
        switch (w) {
            case PATH:
                return "On a road or path you know is safe: stop. Don't go "
                        + "closer. Leave the way you came, on that path, and "
                        + "get outside the cordon.";
            case AREA:
                return "If you may be in a minefield or bomblet area: stop "
                        + "and don't move your feet. Call or shout for help "
                        + "and wait. Only move if you can reach a surface "
                        + "you know is safe (paved road, well-used path) "
                        + "without stepping on unknown ground. Don't try to "
                        + "retrace your footsteps unless you have been "
                        + "trained.";
            case VEHICLE:
                return "In a vehicle: stop. Don't reverse or turn the wheel. "
                        + "Stay in the vehicle unless it is on fire, and call "
                        + "for help.";
            default:
                return "";
        }
    }

    /** "Do now" list for the card. */
    public static List<String> doNow(UxoRecord r) {
        List<String> out = new ArrayList<>();
        out.add("Don't touch it, move it or go closer, not even for a photo "
                + "(use zoom).");
        out.add("Keep everyone out of the cordon.");
        out.add("Stay out of line of sight; get behind hard cover. Cover "
                + "helps but doesn't make it safe to be closer.");
        out.add("Use radios and phones only outside the cordon, never within "
                + UxoTable.RADIO_MIN_M + " m of the item.");
        out.add("Report it (REPORT on this card) once you are outside the "
                + "cordon.");
        UxoTable.Category c = r.category();
        String id = c == null ? "" : c.id;
        if (id.equals("MISSILE_ROCKET") || id.equals("LARGE_GUIDED_MISSILE")
                || id.equals("UNKNOWN"))
            out.add("Keep to the side: never stand in line with the nose or "
                    + "tail. Anti-tank (shaped-charge) items can throw a jet "
                    + "far along their axis.");
        if (id.equals("GREN_MINE_SUBMUN"))
            out.add("Where there is one, expect more: mines and bomblets "
                    + "are rarely alone.");
        if (id.equals("STOCKPILE"))
            out.add("If it is burning, move much farther away now and keep "
                    + "everyone back.");
        if (r.multiple && !id.equals("STOCKPILE"))
            out.add("Several items: extend the cordon further if they are "
                    + "spread out.");
        return out;
    }

    /** Fixed caveats shown on every UXO card. */
    public static final List<String> CAVEATS = Collections.unmodifiableList(
            Arrays.asList(
                    "This cordon is a minimum. Fragments may travel farther "
                            + "than this distance (" + UxoTable.SHORT_NAME
                            + ", Table 1).",
                    "Don't mark the item itself. Mark the area or the way "
                            + "in, from a safe place, and only if you can do "
                            + "it without going closer.",
                    "Never move it, touch it or take it as a souvenir. Old "
                            + "or damaged ordnance can still explode.",
                    "Blast doesn't replace EOD, police or mine action "
                            + "authorities."));

    // ------------------------------------------------------------------
    // Where you are

    /** One boundary: inside or outside the cordon. */
    public static final class You {
        public final boolean inside;
        public final double distanceM;
        public final double toCordonM;
        public final boolean nearBoundary;

        You(boolean inside, double distanceM, double toCordonM,
                boolean nearBoundary) {
            this.inside = inside;
            this.distanceM = distanceM;
            this.toCordonM = toCordonM;
            this.nearBoundary = nearBoundary;
        }

        public String headline(int cordonM) {
            return Formats.measured(distanceM) + " from the item: "
                    + (inside ? "INSIDE" : "OUTSIDE") + " the "
                    + Formats.thousands(cordonM) + " m cordon.";
        }

        public String guidance(int cordonM) {
            if (inside)
                return "Move out to at least " + cordon(cordonM)
                        + ", about " + Formats.measured(toCordonM)
                        + " farther, without going closer to the item.";
            return "Stay outside the cordon and keep others out.";
        }

        public String boundaryWarning() {
            return nearBoundary && !inside
                    ? "You are close to the line and GPS error could put "
                            + "you inside it. Move farther."
                    : null;
        }
    }

    /**
     * Position check; null if the distance is unusable. Inside when the
     * distance is less than the cordon. A non-finite uncertainty uses
     * {@link PositionCheck#DEFAULT_UNCERTAINTY_M}.
     */
    public static You check(int cordonM, double distanceM,
            double uncertaintyM) {
        if (cordonM <= 0 || Double.isNaN(distanceM)
                || Double.isInfinite(distanceM))
            return null;
        double u = Double.isNaN(uncertaintyM)
                || Double.isInfinite(uncertaintyM) || uncertaintyM < 0
                        ? PositionCheck.DEFAULT_UNCERTAINTY_M
                        : uncertaintyM;
        boolean inside = distanceM < cordonM;
        double to = Math.max(0, cordonM - distanceM);
        boolean near = !inside && distanceM - cordonM < u;
        return new You(inside, distanceM, to, near);
    }

    /** {inside, outside} for team distances (non-finite counts inside). */
    public static int[] count(int cordonM, double[] distancesM) {
        int[] n = new int[2];
        if (distancesM == null)
            return n;
        for (double d : distancesM) {
            if (Double.isNaN(d))
                continue;
            if (Double.isInfinite(d) || d < cordonM)
                n[0]++;
            else
                n[1]++;
        }
        return n;
    }

    // ------------------------------------------------------------------
    // Reports

    /**
     * Plain report for a phone call or text: what, where, when, measures.
     * {@code grid} is the item's position as the user's ATAK formats it.
     */
    public static String simpleReport(UxoRecord r, String grid) {
        UxoRecord.Report p = r.report;
        StringBuilder sb = new StringBuilder();
        sb.append("SUSPECTED UNEXPLODED ORDNANCE\n");
        sb.append("WHAT: ").append(r.plainLabel());
        if (p.count > 0)
            sb.append(", ").append(p.count).append(p.count == 1 ? " item"
                    : " items");
        else if (r.multiple)
            sb.append(", more than one item");
        if (!p.description.isEmpty())
            sb.append(". ").append(p.description);
        sb.append('\n');
        sb.append("WHERE: ").append(grid == null || grid.isEmpty()
                ? "(no position)" : grid);
        if (r.placement == UxoRecord.Placement.RB)
            sb.append(" (approximate, range and bearing)");
        if (!p.landmark.isEmpty())
            sb.append(". ").append(p.landmark);
        sb.append('\n');
        sb.append("WHEN: ").append(r.timeMillis > 0 ? Formats.dtg(r.timeMillis)
                : "unknown").append('\n');
        sb.append("MEASURES: ").append(p.protective.isEmpty()
                ? "cordon " + cordon(r.cordonM) + " advised"
                : p.protective).append('\n');
        if (!p.cbrn.isEmpty())
            sb.append("CHEMICAL / OTHER SIGNS: ").append(p.cbrn).append('\n');
        sb.append("REPORTED BY: ").append(p.contact.isEmpty()
                ? (r.byCallsign.isEmpty() ? "unknown" : r.byCallsign)
                : p.contact).append('\n');
        sb.append("Nobody has touched or moved it.");
        return sb.toString();
    }

    /**
     * EOD 9-line (ATP 4-32.2 Table 2). {@code reporter} is the reporting
     * callsign or unit, {@code grid} the item's position.
     */
    public static String nineLine(UxoRecord r, String reporter, String grid) {
        UxoRecord.Report p = r.report;
        StringBuilder sb = new StringBuilder();
        sb.append("EOD 9-LINE (UXO)\n");
        sb.append("1. DTG: ").append(r.timeMillis > 0
                ? Formats.dtg(r.timeMillis) : "unknown").append('\n');
        sb.append("2. UNIT/LOCATION: ")
                .append(reporter == null || reporter.isEmpty() ? "unknown"
                        : reporter)
                .append(", ").append(grid == null || grid.isEmpty()
                        ? "(no position)" : grid);
        if (r.placement == UxoRecord.Placement.RB)
            sb.append(" (approx.)");
        if (!p.landmark.isEmpty())
            sb.append(", ").append(p.landmark);
        sb.append('\n');
        sb.append("3. CONTACT: ").append(p.contact.isEmpty()
                ? (reporter == null ? "" : reporter) : p.contact).append('\n');
        sb.append("4. TYPE: ").append(r.doctrineLabel());
        if (!p.emplacement.isEmpty())
            sb.append(", ").append(p.emplacement);
        if (p.count > 0)
            sb.append(", ").append(p.count).append(" item")
                    .append(p.count == 1 ? "" : "s");
        else if (r.multiple)
            sb.append(", multiple items");
        if (!p.description.isEmpty())
            sb.append(", ").append(p.description);
        sb.append(" (observed without touching, disturbing or approaching)\n");
        sb.append("5. CBRN: ").append(p.cbrn.isEmpty() ? "None seen" : p.cbrn)
                .append('\n');
        sb.append("6. RESOURCES THREATENED: ")
                .append(p.resources.isEmpty() ? "-" : p.resources).append('\n');
        sb.append("7. IMPACT ON MISSION: ")
                .append(p.impact.isEmpty() ? "-" : p.impact).append('\n');
        sb.append("8. PROTECTIVE MEASURES: ").append(p.protective.isEmpty()
                ? "Cordon " + cordon(r.cordonM) + " advised" : p.protective)
                .append('\n');
        sb.append("9. RECOMMENDED PRIORITY: ")
                .append(p.priority == UxoRecord.Priority.UNSET ? "-"
                        : p.priority.label.toUpperCase(java.util.Locale.US));
        return sb.toString();
    }

    /** ATP 4-32.2 Table 2's warning, shown with the 9-line. */
    public static final String PRIORITY_NOTE = "A priority of MINOR or NO "
            + "THREAT (NO HAZARD) doesn't mean the item is safe.";

    /** Shown with every report (TNMA 12.10/01 reprisal caution). */
    public static final String REPORT_CAUTION = "In conflict areas, "
            + "reporting or marking items can put people at risk. Choose who "
            + "receives this report.";
}
