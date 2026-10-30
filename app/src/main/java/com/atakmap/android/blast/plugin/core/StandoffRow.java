package com.atakmap.android.blast.plugin.core;

/**
 * One row of a published standoff table, stored exactly as printed.
 * Feet are the card's unit and set the drawn radii. Metres are the values
 * published for the same row in ERG2024 p. 367 (or -1 when no metric value
 * is published, in which case labels use an outward-rounded conversion).
 */
public final class StandoffRow {

    /** Stable identifier carried in CoT ({@code row="SUV_VAN"}). Never rename. */
    public final String id;
    /** Threat description as printed on the source. */
    public final String label;
    /** Explosives capacity as printed (pounds, TNT equivalent). */
    public final int capacityLb;
    /** Mandatory evacuation distance (ft): inside this, everyone evacuates. */
    public final int mandatoryFt;
    /** First foot of the shelter-in-place zone as printed (e.g. 71). */
    public final int shelterFromFt;
    /** Last foot of the shelter-in-place zone as printed (e.g. 1199). */
    public final int shelterToFt;
    /** Preferred evacuation distance (ft): at or beyond this. */
    public final int preferredFt;

    /** Published metric values, or -1 when none are published. */
    public final double capacityKg;
    public final int mandatoryM;
    public final int shelterFromM;
    public final int shelterToM;
    public final int preferredM;

    public StandoffRow(String id, String label, int capacityLb,
            int mandatoryFt, int shelterFromFt, int shelterToFt,
            int preferredFt) {
        this(id, label, capacityLb, mandatoryFt, shelterFromFt, shelterToFt,
                preferredFt, -1, -1, -1, -1, -1);
    }

    public StandoffRow(String id, String label, int capacityLb,
            int mandatoryFt, int shelterFromFt, int shelterToFt,
            int preferredFt, double capacityKg, int mandatoryM,
            int shelterFromM, int shelterToM, int preferredM) {
        this.id = id;
        this.label = label;
        this.capacityLb = capacityLb;
        this.mandatoryFt = mandatoryFt;
        this.shelterFromFt = shelterFromFt;
        this.shelterToFt = shelterToFt;
        this.preferredFt = preferredFt;
        this.capacityKg = capacityKg;
        this.mandatoryM = mandatoryM;
        this.shelterFromM = shelterFromM;
        this.shelterToM = shelterToM;
        this.preferredM = preferredM;
    }

    public boolean hasPublishedMetres() {
        return mandatoryM > 0 && preferredM > 0;
    }

    /** Metres for the mandatory distance: published, else converted (outward). */
    public long mandatoryMetres() {
        return mandatoryM > 0 ? mandatoryM : Formats.cardMetres(mandatoryFt);
    }

    public long shelterFromMetres() {
        return shelterFromM > 0 ? shelterFromM
                : Formats.cardMetres(shelterFromFt);
    }

    public long shelterToMetres() {
        return shelterToM > 0 ? shelterToM
                : (long) Math.floor(Formats.ftToM(shelterToFt) + 1e-9);
    }

    public long preferredMetres() {
        return preferredM > 0 ? preferredM : Formats.cardMetres(preferredFt);
    }

    /**
     * Radius to draw for the mandatory ring (m): the larger of the feet
     * and the published metres, so a rounded metric value never shrinks it.
     */
    public double mandatoryRadiusM() {
        return Math.max(Formats.ftToM(mandatoryFt), Math.max(0, mandatoryM));
    }

    /** Radius to draw for the preferred line (m), same rule. */
    public double preferredRadiusM() {
        return Math.max(Formats.ftToM(preferredFt), Math.max(0, preferredM));
    }

    /** True if the row is internally consistent (used for received data). */
    public boolean isConsistent() {
        return capacityLb > 0 && mandatoryFt > 0
                && shelterFromFt == mandatoryFt + 1
                && shelterToFt >= shelterFromFt
                && preferredFt == shelterToFt + 1;
    }

    /** Same distances (feet and capacity) as another row. */
    boolean sameFeet(StandoffRow o) {
        return o != null && capacityLb == o.capacityLb
                && mandatoryFt == o.mandatoryFt && shelterToFt == o.shelterToFt
                && preferredFt == o.preferredFt;
    }

    @Override
    public String toString() {
        return id + "(" + capacityLb + " lb: " + mandatoryFt + "/"
                + shelterFromFt + "-" + shelterToFt + "/" + preferredFt + " ft)";
    }
}
