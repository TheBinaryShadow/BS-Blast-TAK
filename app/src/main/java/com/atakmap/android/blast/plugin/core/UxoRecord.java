package com.atakmap.android.blast.plugin.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One UXO incident: the type, the cordon, the reporter's situation, who
 * placed it and when, its status, its number and the (optional) report
 * details. Immutable; edits produce a new record.
 */
public final class UxoRecord {

    public static final int VERSION = 1;

    /** Where the reporter was when they found it (selects the drill). */
    public enum Where {
        PATH, AREA, VEHICLE, UNSET;

        public static Where parse(String s) {
            if ("PATH".equals(s))
                return PATH;
            if ("AREA".equals(s))
                return AREA;
            if ("VEHICLE".equals(s))
                return VEHICLE;
            return UNSET;
        }
    }

    /** How the point was placed. */
    public enum Placement {
        MAP, RB, CENTER, MARKER, UNKNOWN;

        public static Placement parse(String s) {
            if (s == null)
                return UNKNOWN;
            switch (s) {
                case "MAP":
                    return MAP;
                case "RB":
                    return RB;
                case "CENTER":
                    return CENTER;
                case "MARKER":
                    return MARKER;
                default:
                    return UNKNOWN;
            }
        }
    }

    /** EOD 9-line line 9 (ATP 4-32.2 Table 2). */
    public enum Priority {
        UNSET(""), IMMEDIATE("Immediate"), INDIRECT("Indirect"),
        MINOR("Minor"), NO_THREAT("No threat");

        public final String label;

        Priority(String label) {
            this.label = label;
        }

        public static Priority parse(String s) {
            if (s != null)
                for (Priority p : values())
                    if (p.name().equals(s))
                        return p;
            return UNSET;
        }
    }

    /** Report details the user adds (all optional). */
    public static final class Report {
        public static final Report EMPTY = new Report(0, "", "", "", "", "",
                "", "", Priority.UNSET, "");

        /** Number of items seen; 0 = not given. */
        public final int count;
        public final String description;
        public final String landmark;
        /** Line 4 method of emplacement (dropped, projected, ...). */
        public final String emplacement;
        public final String cbrn;
        public final String resources;
        public final String impact;
        public final String protective;
        public final Priority priority;
        public final String contact;

        public Report(int count, String description, String landmark,
                String emplacement, String cbrn, String resources,
                String impact, String protective, Priority priority,
                String contact) {
            this.count = count < 0 || count > 9999 ? 0 : count;
            this.description = nz(description);
            this.landmark = nz(landmark);
            this.emplacement = nz(emplacement);
            this.cbrn = nz(cbrn);
            this.resources = nz(resources);
            this.impact = nz(impact);
            this.protective = nz(protective);
            this.priority = priority == null ? Priority.UNSET : priority;
            this.contact = nz(contact);
        }

        public boolean isEmpty() {
            return count == 0 && description.isEmpty() && landmark.isEmpty()
                    && emplacement.isEmpty() && cbrn.isEmpty()
                    && resources.isEmpty() && impact.isEmpty()
                    && protective.isEmpty() && priority == Priority.UNSET
                    && contact.isEmpty();
        }

        private static String nz(String s) {
            return s == null ? "" : s;
        }
    }

    public final int version;
    public final String sourceId;
    public final String edition;
    public final String categoryId;
    /** Doctrine label (carried so clients without the table can show it). */
    public final String categoryLabel;
    /** Cordon radius drawn, metres. */
    public final int cordonM;
    public final boolean multiple;
    /** Unknown item that looks bomb or missile sized. */
    public final boolean large;
    public final Where where;
    public final Placement placement;
    public final String byCallsign;
    public final long timeMillis;
    public final BlastRecord.Status status;
    public final String clearedBy;
    public final long clearedMillis;
    public final int number;
    public final Report report;
    public final Map<String, String> extra;
    /** Received cordon differed from this build's table (not transmitted). */
    public final boolean receivedMismatch;

    public UxoRecord(int version, String sourceId, String edition,
            String categoryId, String categoryLabel, int cordonM,
            boolean multiple, boolean large, Where where,
            Placement placement, String byCallsign, long timeMillis,
            BlastRecord.Status status, String clearedBy, long clearedMillis,
            int number, Report report, Map<String, String> extra,
            boolean receivedMismatch) {
        this.version = version;
        this.sourceId = sourceId == null ? "" : sourceId;
        this.edition = edition == null ? "" : edition;
        this.categoryId = categoryId == null ? "" : categoryId;
        this.categoryLabel = categoryLabel == null ? "" : categoryLabel;
        this.cordonM = cordonM;
        this.multiple = multiple;
        this.large = large;
        this.where = where == null ? Where.UNSET : where;
        this.placement = placement == null ? Placement.UNKNOWN : placement;
        this.byCallsign = byCallsign == null ? "" : byCallsign;
        this.timeMillis = timeMillis;
        this.status = status == null ? BlastRecord.Status.ACTIVE : status;
        this.clearedBy = clearedBy == null ? "" : clearedBy;
        this.clearedMillis = clearedMillis;
        this.number = BlastRecord.clampNumber(number);
        this.report = report == null ? Report.EMPTY : report;
        this.extra = extra == null
                ? Collections.<String, String> emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(extra));
        this.receivedMismatch = receivedMismatch;
    }

    /** A new incident from the picker. */
    public static UxoRecord create(String categoryId, boolean multiple,
            boolean large, Where where, Placement placement,
            String byCallsign, long timeMillis) {
        UxoTable.Category c = UxoTable.category(categoryId);
        if (c == null)
            throw new IllegalArgumentException("unknown category "
                    + categoryId);
        boolean lg = c.isUnknown() && large;
        return new UxoRecord(VERSION, UxoTable.SOURCE_ID, UxoTable.EDITION,
                c.id, c.doctrineLabel, UxoTable.cordonM(c.id, multiple, lg),
                multiple, lg, where, placement, byCallsign, timeMillis,
                BlastRecord.Status.ACTIVE, "", -1, 0, Report.EMPTY, null,
                false);
    }

    private UxoRecord with(BlastRecord.Status st, String cby, long ct, int n,
            Report rep) {
        return new UxoRecord(version, sourceId, edition, categoryId,
                categoryLabel, cordonM, multiple, large, where, placement,
                byCallsign, timeMillis, st, cby, ct, n, rep, extra, false);
    }

    public UxoRecord withNumber(int n) {
        return with(status, clearedBy, clearedMillis, n, report);
    }

    public UxoRecord withReport(Report r) {
        return with(status, clearedBy, clearedMillis, number, r);
    }

    public UxoRecord cleared(String by, long when) {
        return with(BlastRecord.Status.CLEARED, by, when, number, report);
    }

    public UxoRecord reopened() {
        return with(BlastRecord.Status.ACTIVE, "", -1, number, report);
    }

    public boolean isCleared() {
        return status == BlastRecord.Status.CLEARED;
    }

    /** True when this build carries the record's source and edition. */
    public boolean isKnownEdition() {
        return UxoTable.isKnown(sourceId, edition);
    }

    /** The category in this build's table, or null. */
    public UxoTable.Category category() {
        return isKnownEdition() ? UxoTable.category(categoryId) : null;
    }

    /** Plain-words name of the type ("Artillery shell or mortar"). */
    public String plainLabel() {
        UxoTable.Category c = category();
        if (c != null)
            return c.plainLabel;
        return categoryLabel.isEmpty() ? categoryId : categoryLabel;
    }

    /** Doctrine name of the type ("Projectiles and Mortars"). */
    public String doctrineLabel() {
        UxoTable.Category c = category();
        if (c != null)
            return c.doctrineLabel;
        return categoryLabel.isEmpty() ? categoryId : categoryLabel;
    }
}
