package com.atakmap.android.blast.plugin.core;

/**
 * Where a point is relative to a zone set, and what the card says to do.
 * Guidance text paraphrases the card and adds nothing the card doesn't say.
 */
public final class PositionCheck {

    private PositionCheck() {
    }

    public enum Zone {
        MANDATORY("MANDATORY EVACUATION"),
        SHELTER("SHELTER-IN-PLACE"),
        PREFERRED("PREFERRED EVACUATION");

        public final String title;

        Zone(String title) {
            this.title = title;
        }

        /** Worse = closer to the device. */
        boolean worseThan(Zone o) {
            return ordinal() < o.ordinal();
        }
    }

    /** Default position uncertainty when the fix reports none (metres). */
    public static final double DEFAULT_UNCERTAINTY_M = 15.0;

    /** The zone at a distance; a non-finite distance counts as MANDATORY. */
    public static Zone zoneOf(StandoffRow row, double distanceM) {
        if (Double.isNaN(distanceM) || Double.isInfinite(distanceM))
            return Zone.MANDATORY;
        // Same radii as the drawn rings. Tolerance only absorbs
        // floating-point noise from ft→m conversions.
        double d = Math.max(0, distanceM);
        if (d <= row.mandatoryRadiusM() + 1e-6)
            return Zone.MANDATORY;
        if (d >= row.preferredRadiusM() - 1e-6)
            return Zone.PREFERRED;
        return Zone.SHELTER;
    }

    public static final class Result {
        public final Zone zone;
        public final double distanceM;
        /** How much farther to reach the preferred distance (0 if there). */
        public final double toPreferredM;
        /** The position uncertainty could put this point in a worse zone. */
        public final boolean nearBoundary;
        public final StandoffRow row;

        Result(StandoffRow row, Zone zone, double distanceM,
                double toPreferredM, boolean nearBoundary) {
            this.row = row;
            this.zone = zone;
            this.distanceM = distanceM;
            this.toPreferredM = toPreferredM;
            this.nearBoundary = nearBoundary;
        }

        /** One headline line: "450 ft (137 m) from the device: SHELTER-IN-PLACE zone." */
        public String headline() {
            return Formats.measured(distanceM) + " from the device: "
                    + zone.title + " zone.";
        }

        /** What the card says to do from here. */
        public String guidance() {
            String pref = Formats.ft(row.preferredFt) + " ("
                    + Formats.thousands(row.preferredMetres()) + " m)";
            switch (zone) {
                case MANDATORY:
                    return "Evacuate now. Everyone inside and outside "
                            + "buildings leaves and goes to at least " + pref
                            + ".";
                case SHELTER: {
                    long ft = (long) Math.ceil(Formats.mToFt(toPreferredM));
                    return "Move about " + Formats.thousands(ft)
                            + " ft farther, to at least " + pref
                            + ". If you can't, shelter inside a building "
                            + "away from windows and exterior walls.";
                }
                default:
                    return "At or beyond the preferred evacuation distance ("
                            + pref + ").";
            }
        }

        public String boundaryWarning() {
            if (!nearBoundary)
                return null;
            return "Close to a zone boundary: position error could put you "
                    + "on the closer side. Move farther.";
        }
    }

    /**
     * @param distanceM    distance from the device (zone centre) in metres
     * @param uncertaintyM position uncertainty (CE) in metres; NaN, infinite
     *                     or <= 0 uses {@link #DEFAULT_UNCERTAINTY_M}
     * @return null if the distance isn't a usable number
     */
    public static Result check(StandoffRow row, double distanceM,
            double uncertaintyM) {
        if (row == null || Double.isNaN(distanceM)
                || Double.isInfinite(distanceM))
            return null;
        if (Double.isNaN(uncertaintyM) || Double.isInfinite(uncertaintyM)
                || uncertaintyM <= 0)
            uncertaintyM = DEFAULT_UNCERTAINTY_M;
        double d = Math.max(0, distanceM);
        Zone z = zoneOf(row, d);
        Zone worst = zoneOf(row, Math.max(0, d - uncertaintyM));
        double toPref = Math.max(0, row.preferredRadiusM() - d);
        return new Result(row, z, d, toPref, worst.worseThan(z));
    }

    /** Counts of points per zone: [mandatory, shelter, preferred]. */
    public static int[] count(StandoffRow row, double[] distancesM) {
        int[] c = new int[3];
        if (distancesM == null)
            return c;
        for (double d : distancesM) {
            if (Double.isNaN(d) || Double.isInfinite(d))
                continue;
            c[zoneOf(row, d).ordinal()]++;
        }
        return c;
    }
}
