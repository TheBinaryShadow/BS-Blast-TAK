package com.atakmap.android.blast.plugin.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Everything one incident carries: which table, edition and row, the
 * distances used, who placed it and when, its number and its status.
 * Immutable; edits produce a new record.
 */
public final class BlastRecord {

    public static final int VERSION = 1;

    /** Highest incident number accepted (also the "BLAST n" name limit). */
    public static final int MAX_NUMBER = 999_999;

    public enum Status {
        ACTIVE, CLEARED;

        public static Status parse(String s) {
            return "CLEARED".equals(s) ? CLEARED : ACTIVE;
        }
    }

    public final int version;
    public final String sourceId;
    public final String edition;
    public final String rowId;
    /** Row label (carried so clients without the table can show it). */
    public final String rowLabel;
    public final int capacityLb;
    public final int mandatoryFt;
    public final int shelterToFt;
    public final int preferredFt;
    /** User's estimate, NaN if a card row was picked directly. */
    public final double requestedValue;
    public final ZoneCalculator.MassUnit requestedUnit;
    public final String byCallsign;
    public final long timeMillis;
    public final Status status;
    public final String clearedBy;
    public final long clearedMillis;
    /** Attributes this version doesn't know; kept and re-emitted. */
    public final Map<String, String> extra;
    /**
     * Incident number (BLAST n), 0 if unknown (0.1-0.5 records). Kept here
     * so numbering survives renames, including by clients without Blast.
     */
    public final int number;
    /**
     * True if a received record carried distances different from this
     * build's copy of the same edition and row. The table's values were
     * used instead (see {@link BlastCodec#decode}). Not transmitted.
     */
    public final boolean receivedMismatch;

    public BlastRecord(int version, String sourceId, String edition,
            String rowId, String rowLabel, int capacityLb, int mandatoryFt,
            int shelterToFt, int preferredFt, double requestedValue,
            ZoneCalculator.MassUnit requestedUnit, String byCallsign,
            long timeMillis, Status status, String clearedBy,
            long clearedMillis, Map<String, String> extra, int number,
            boolean receivedMismatch) {
        this.version = version;
        this.sourceId = sourceId == null ? "" : sourceId;
        this.edition = edition == null ? "" : edition;
        this.rowId = rowId == null ? "" : rowId;
        this.rowLabel = rowLabel == null ? "" : rowLabel;
        this.capacityLb = capacityLb;
        this.mandatoryFt = mandatoryFt;
        this.shelterToFt = shelterToFt;
        this.preferredFt = preferredFt;
        this.requestedValue = requestedValue;
        this.requestedUnit = requestedUnit == null
                ? ZoneCalculator.MassUnit.LB
                : requestedUnit;
        this.byCallsign = byCallsign == null ? "" : byCallsign;
        this.timeMillis = timeMillis;
        this.status = status == null ? Status.ACTIVE : status;
        this.clearedBy = clearedBy == null ? "" : clearedBy;
        this.clearedMillis = clearedMillis;
        this.extra = extra == null
                ? Collections.<String, String> emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(extra));
        this.number = clampNumber(number);
        this.receivedMismatch = receivedMismatch;
    }

    static int clampNumber(int n) {
        return n < 0 || n > MAX_NUMBER ? 0 : n;
    }

    /** A new incident from a usable selection. */
    public static BlastRecord create(ZoneCalculator.Selection sel,
            String byCallsign, long timeMillis) {
        if (sel == null || !sel.isUsable())
            throw new IllegalArgumentException("selection has no row");
        StandoffRow r = sel.row;
        return new BlastRecord(VERSION, sel.table.sourceId, sel.table.edition,
                r.id, r.label, r.capacityLb, r.mandatoryFt, r.shelterToFt,
                r.preferredFt, sel.hasRequest() ? sel.requestedValue
                        : Double.NaN,
                sel.requestedUnit, byCallsign, timeMillis, Status.ACTIVE, "",
                -1, null, 0, false);
    }

    private BlastRecord with(Status st, String cby, long ct, int n) {
        return new BlastRecord(version, sourceId, edition, rowId, rowLabel,
                capacityLb, mandatoryFt, shelterToFt, preferredFt,
                requestedValue, requestedUnit, byCallsign, timeMillis, st,
                cby, ct, extra, n, false);
    }

    public BlastRecord withNumber(int n) {
        return with(status, clearedBy, clearedMillis, n);
    }

    public BlastRecord cleared(String by, long when) {
        return with(Status.CLEARED, by, when, number);
    }

    public BlastRecord reopened() {
        return with(Status.ACTIVE, "", -1, number);
    }

    public boolean hasRequest() {
        return !Double.isNaN(requestedValue);
    }

    public boolean isCleared() {
        return status == Status.CLEARED;
    }

    /**
     * The row to draw and label. The table's row (with its published
     * metres) when this build has the edition and the feet match; otherwise
     * a row built from the carried feet (an edition this build lacks).
     */
    public StandoffRow row() {
        StandoffTable t = table();
        StandoffRow known = t == null ? null : t.row(rowId);
        StandoffRow carried = new StandoffRow(rowId, rowLabel, capacityLb,
                mandatoryFt, mandatoryFt + 1, shelterToFt, preferredFt);
        if (known != null && known.sameFeet(carried))
            return known;
        return carried;
    }

    /** The matching table, or null if this build doesn't have that edition. */
    public StandoffTable table() {
        return StandoffTable.find(sourceId, edition);
    }

    /** Short name of the source for labels. */
    public String sourceName() {
        StandoffTable t = table();
        if (t != null)
            return t.shortName;
        return sourceId;
    }

    /**
     * True if the sender's distances differed from this build's copy of the
     * same edition and row (shown as a warning; the table's values are
     * drawn), or if the edition is known but the row isn't.
     */
    public boolean disagreesWithTable() {
        if (receivedMismatch)
            return true;
        StandoffTable t = table();
        if (t == null)
            return false;
        StandoffRow r = t.row(rowId);
        if (r == null)
            return true;
        return r.capacityLb != capacityLb || r.mandatoryFt != mandatoryFt
                || r.shelterToFt != shelterToFt || r.preferredFt != preferredFt;
    }
}
