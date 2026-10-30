package com.atakmap.android.blast.plugin.core;

/**
 * Every text that leaves the plugin (marker remarks, ring titles) so clients
 * without the plugin still get the full picture.
 */
public final class Labels {

    private Labels() {
    }

    public static final String REMARKS_PREFIX = "[BLAST]";

    /** "SUV/Van (1,000 lb)". */
    public static String threat(BlastRecord r) {
        if (r.capacityLb > 0)
            return r.rowLabel + " (" + Formats.thousands(r.capacityLb) + " lb)";
        return r.rowLabel;
    }

    /** Marker callsign: "BLAST 3 SUV/Van" / "BLAST 3 SUV/Van CLEARED". */
    public static String markerName(int number, BlastRecord r) {
        String base = "BLAST " + number + " " + r.rowLabel;
        return r.isCleared() ? base + " CLEARED" : base;
    }

    /** Title of the inner (mandatory) ring. */
    public static String mandatoryRing(BlastRecord r) {
        return prefix(r) + "MANDATORY EVAC " + Formats.cardDistance(r.mandatoryFt);
    }

    /** Title of the outer (preferred) ring. */
    public static String preferredRing(BlastRecord r) {
        return prefix(r) + "PREFERRED EVAC " + Formats.ft(r.preferredFt) + "+ ("
                + Formats.thousands(Formats.cardMetres(r.preferredFt))
                + " m) / SHELTER INSIDE";
    }

    private static String prefix(BlastRecord r) {
        return r.isCleared() ? "CLEARED " : "";
    }

    /** "CISA/FBI Bomb Threat Stand-Off Card, Aug 2025, SUV/Van row." */
    public static String source(BlastRecord r) {
        return r.sourceName() + ", " + Formats.editionLabel(r.edition) + ", "
                + r.rowLabel + " row.";
    }

    /** Provenance line shown on the zone card. */
    public static String provenance(BlastRecord r) {
        StringBuilder sb = new StringBuilder(source(r));
        if (r.hasRequest())
            sb.append(" Estimate ").append(Formats.quantity(r.requestedValue))
                    .append(' ').append(r.requestedUnit.symbol)
                    .append(", next row at or above.");
        sb.append(" Placed by ")
                .append(r.byCallsign.isEmpty() ? "unknown" : r.byCallsign);
        if (r.timeMillis > 0)
            sb.append(' ').append(Formats.dtg(r.timeMillis));
        sb.append('.');
        if (r.isCleared()) {
            sb.append(" CLEARED by ")
                    .append(r.clearedBy.isEmpty() ? "unknown" : r.clearedBy);
            if (r.clearedMillis > 0)
                sb.append(' ').append(Formats.dtg(r.clearedMillis));
            sb.append('.');
        }
        return sb.toString();
    }

    /** Marker remarks for any TAK client. */
    public static String remarks(BlastRecord r) {
        StringBuilder sb = new StringBuilder(REMARKS_PREFIX).append(' ');
        if (r.isCleared())
            sb.append("CLEARED. ");
        sb.append(threat(r)).append(": MANDATORY EVAC within ")
                .append(Formats.cardDistance(r.mandatoryFt))
                .append("; SHELTER-IN-PLACE ")
                .append(Formats.thousands(r.mandatoryFt + 1)).append('-')
                .append(Formats.ft(r.shelterToFt))
                .append(" (inside, away from windows and exterior walls, only if you can't reach the preferred distance)")
                .append("; PREFERRED EVAC ")
                .append(Formats.ft(r.preferredFt)).append("+ (")
                .append(Formats.thousands(Formats.cardMetres(r.preferredFt)))
                .append(" m). Source: ").append(provenance(r))
                .append(" Minimum distances, not guarantees; metres are converted from the card's feet.");
        return sb.toString();
    }

    /** Distance table rows for the zone card: {name, range}. */
    public static String[][] zoneRows(BlastRecord r) {
        return new String[][] {
                {
                        "Mandatory evacuation",
                        "within " + Formats.cardDistance(r.mandatoryFt)
                },
                {
                        "Shelter-in-place zone",
                        Formats.thousands(r.mandatoryFt + 1) + "–"
                                + Formats.ft(r.shelterToFt)
                },
                {
                        "Preferred evacuation",
                        Formats.ft(r.preferredFt) + "+ ("
                                + Formats.thousands(
                                        Formats.cardMetres(r.preferredFt))
                                + " m+)"
                }
        };
    }
}
