package com.atakmap.android.blast.plugin.core;

/**
 * Every text that leaves the plugin (marker remarks, ring titles) so clients
 * without the plugin still get the full picture.
 */
public final class Labels {

    private Labels() {
    }

    public static final String REMARKS_PREFIX = "[BLAST]";
    /** Last sentence of the generated block (marks where it ends). */
    public static final String REMARKS_END = "Minimum distances, not guarantees.";

    /** "SUV/Van (1,000 lb)". */
    public static String threat(BlastRecord r) {
        if (r.capacityLb > 0)
            return r.rowLabel + " (" + Formats.thousands(r.capacityLb) + " lb)";
        return r.rowLabel;
    }

    /** Title of the inner (mandatory) ring. */
    public static String mandatoryRing(BlastRecord r) {
        StandoffRow row = r.row();
        return prefix(r) + "MANDATORY EVAC " + Formats.ft(row.mandatoryFt)
                + " (" + Formats.thousands(row.mandatoryMetres()) + " m)";
    }

    /** Title of the outer (preferred) ring. */
    public static String preferredRing(BlastRecord r) {
        StandoffRow row = r.row();
        return prefix(r) + "PREFERRED EVAC " + Formats.ft(row.preferredFt)
                + "+ (" + Formats.thousands(row.preferredMetres())
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
        StandoffRow row = r.row();
        StringBuilder sb = new StringBuilder(REMARKS_PREFIX).append(' ');
        if (r.isCleared())
            sb.append("CLEARED. ");
        sb.append(threat(r)).append(": MANDATORY EVAC within ")
                .append(Formats.ft(row.mandatoryFt)).append(" (")
                .append(Formats.thousands(row.mandatoryMetres()))
                .append(" m); SHELTER-IN-PLACE ")
                .append(Formats.thousands(row.shelterFromFt)).append('-')
                .append(Formats.ft(row.shelterToFt))
                .append(" (inside, away from windows and exterior walls, only if you can't reach the preferred distance)")
                .append("; PREFERRED EVAC ")
                .append(Formats.ft(row.preferredFt)).append("+ (")
                .append(Formats.thousands(row.preferredMetres()))
                .append(" m). Source: ").append(provenance(r))
                .append(' ').append(REMARKS_END);
        return sb.toString();
    }

    /**
     * New marker remarks that keep anything people wrote themselves.
     * Remarks are an ordinary field any TAK client can edit; only Blast's
     * own block (from {@link #REMARKS_PREFIX} to {@link #REMARKS_END}) is
     * replaced. Text before and after it is kept. If the block can't be
     * found intact, the new block goes first and the old text is kept
     * below it, so nothing a person wrote is ever dropped.
     */
    public static String mergeRemarks(String existing, BlastRecord r) {
        return mergeBlock(existing, remarks(r), REMARKS_PREFIX);
    }

    /**
     * Replace the generated block that starts with {@code prefix} and ends
     * with {@link #REMARKS_END}; keep everything else (see
     * {@link #mergeRemarks}).
     */
    public static String mergeBlock(String existing, String block,
            String prefix) {
        return mergeBlock(existing, block, prefix, REMARKS_END);
    }

    /** {@link #mergeBlock(String, String, String)} with another end marker. */
    public static String mergeBlock(String existing, String block,
            String prefix, String endMarker) {
        if (existing == null || existing.trim().isEmpty())
            return block;
        int start = existing.indexOf(prefix);
        if (start >= 0) {
            int end = existing.indexOf(endMarker, start);
            if (end >= 0) {
                String before = existing.substring(0, start);
                String after = existing.substring(end + endMarker.length());
                return before + block + after;
            }
        }
        return block + "\n\n" + existing.trim();
    }

    /** Distance table rows for the zone card: {name, feet, metres}. */
    public static String[][] zoneRows(BlastRecord r) {
        StandoffRow row = r.row();
        return new String[][] {
                {
                        "Mandatory evacuation",
                        "0–" + Formats.ft(row.mandatoryFt),
                        "0–" + Formats.thousands(row.mandatoryMetres()) + " m"
                },
                {
                        "Shelter-in-place zone",
                        Formats.thousands(row.shelterFromFt) + "–"
                                + Formats.ft(row.shelterToFt),
                        Formats.thousands(row.shelterFromMetres()) + "–"
                                + Formats.thousands(row.shelterToMetres())
                                + " m"
                },
                {
                        "Preferred evacuation",
                        Formats.ft(row.preferredFt) + "+",
                        Formats.thousands(row.preferredMetres()) + "+ m"
                }
        };
    }

    /** Where the metres on the card come from. */
    public static String metresNote(BlastRecord r) {
        return r.row().hasPublishedMetres()
                ? "Metres as published in " + StandoffTable.METRIC_SOURCE
                        + " (same table). Rings are drawn at the larger of the "
                        + "feet and metre values."
                : "Metres converted from the card's feet, rounded outward.";
    }
}
