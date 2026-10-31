package com.atakmap.android.blast.plugin.core;

/**
 * Row selection. The one rule: never interpolate a table downward. A custom
 * quantity uses the smallest row whose capacity is at or above it; above the
 * largest row there is no published answer and nothing is drawn.
 */
public final class ZoneCalculator {

    private ZoneCalculator() {
    }

    public enum MassUnit {
        LB("lb"), KG("kg");

        public final String symbol;

        MassUnit(String symbol) {
            this.symbol = symbol;
        }

        public static MassUnit parse(String s) {
            return s != null && "kg".equals(s.trim()) ? KG : LB;
        }
    }

    public enum Status {
        /** A published row was chosen directly. */
        ROW,
        /** A quantity exactly matched a row's capacity. */
        EXACT,
        /** A quantity was rounded up to the next row. */
        NEXT_ROW_UP,
        /** Above the largest row: no distances are published. */
        ABOVE_TABLE,
        /** Not a usable number. */
        INVALID
    }

    public static final class Selection {
        public final StandoffTable table;
        /** Null for ABOVE_TABLE and INVALID. */
        public final StandoffRow row;
        public final Status status;
        /** The value the user typed (NaN when a row was picked). */
        public final double requestedValue;
        public final MassUnit requestedUnit;
        /** The request in pounds (NaN when a row was picked). */
        public final double requestedLb;

        Selection(StandoffTable table, StandoffRow row, Status status,
                double requestedValue, MassUnit unit, double requestedLb) {
            this.table = table;
            this.row = row;
            this.status = status;
            this.requestedValue = requestedValue;
            this.requestedUnit = unit;
            this.requestedLb = requestedLb;
        }

        public boolean isUsable() {
            return row != null;
        }

        public boolean hasRequest() {
            return !Double.isNaN(requestedLb);
        }

        /** Plain-language explanation of which row is used and why. */
        public String explain() {
            switch (status) {
                case ROW:
                    return row.label + " row ("
                            + Formats.thousands(row.capacityLb) + " lb).";
                case EXACT:
                    return "Your " + requestText()
                            + " estimate matches the " + row.label
                            + " row (" + Formats.thousands(row.capacityLb)
                            + " lb).";
                case NEXT_ROW_UP:
                    return "Using the " + row.label + " row ("
                            + Formats.thousands(row.capacityLb)
                            + " lb), the next row at or above your "
                            + requestText() + " estimate. The card is never "
                            + "scaled down.";
                case ABOVE_TABLE: {
                    StandoffRow big = table.largest();
                    return "Your " + requestText() + " estimate is above "
                            + Formats.thousands(big.capacityLb)
                            + " lb, the largest row on the card ("
                            + big.label + "). The card publishes no distance "
                            + "for this quantity, so no zone is drawn. Get "
                            + "expert guidance; distances will be greater "
                            + "than the " + big.label + " row ("
                            + Formats.ft(big.preferredFt) + "+).";
                }
                default:
                    return "Enter a quantity greater than zero.";
            }
        }

        private String requestText() {
            if (requestedUnit == MassUnit.KG)
                return Formats.quantity(requestedValue) + " kg ("
                        + Formats.quantity(requestedLb) + " lb)";
            return Formats.quantity(requestedValue) + " lb";
        }
    }

    public static Selection forRow(StandoffTable table, String rowId) {
        StandoffRow r = table.row(rowId);
        if (r == null)
            return new Selection(table, null, Status.INVALID, Double.NaN,
                    MassUnit.LB, Double.NaN);
        return new Selection(table, r, Status.ROW, Double.NaN, MassUnit.LB,
                Double.NaN);
    }

    public static Selection forQuantity(StandoffTable table, double value,
            MassUnit unit) {
        if (unit == null)
            unit = MassUnit.LB;
        if (Double.isNaN(value) || Double.isInfinite(value) || value <= 0)
            return new Selection(table, null, Status.INVALID, value, unit,
                    Double.NaN);
        double lb = unit == MassUnit.KG ? Formats.kgToLb(value) : value;
        if (Double.isInfinite(lb))
            return new Selection(table, null, Status.ABOVE_TABLE, value, unit,
                    Double.MAX_VALUE);
        for (StandoffRow r : table.rows()) {
            if (lb <= r.capacityLb) {
                // Exact only for an exact pound entry; a kg conversion that
                // lands within rounding of a row still uses that row.
                boolean exact = Math.abs(lb - r.capacityLb) < 1e-9;
                return new Selection(table, r,
                        exact ? Status.EXACT : Status.NEXT_ROW_UP, value, unit,
                        lb);
            }
        }
        return new Selection(table, null, Status.ABOVE_TABLE, value, unit, lb);
    }

    /** Longest quantity text accepted (the pane limits input to this). */
    public static final int MAX_QUANTITY_CHARS = 12;

    /** Parse a typed quantity ("800", "1,000", "2.5"); NaN if unusable. */
    public static double parseQuantity(String s) {
        if (s == null)
            return Double.NaN;
        String t = s.trim().replace(",", "").replace(" ", "");
        if (t.isEmpty() || t.length() > MAX_QUANTITY_CHARS)
            return Double.NaN;
        try {
            double v = Double.parseDouble(t);
            return Double.isInfinite(v) ? Double.NaN : v;
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }
}
