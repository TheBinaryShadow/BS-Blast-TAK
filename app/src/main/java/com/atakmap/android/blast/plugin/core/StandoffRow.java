package com.atakmap.android.blast.plugin.core;

/**
 * One row of a published standoff table, stored exactly as printed.
 * Distances are in feet (the card's own unit); conversions happen only for
 * display and drawing.
 */
public final class StandoffRow {

    /** Stable identifier carried in CoT ({@code row="SUV_VAN"}). Never rename. */
    public final String id;
    /** Threat description as printed on the source. */
    public final String label;
    /** Explosives capacity as printed (pounds). */
    public final int capacityLb;
    /** Mandatory evacuation distance (ft): inside this, everyone evacuates. */
    public final int mandatoryFt;
    /** First foot of the shelter-in-place zone as printed (e.g. 71). */
    public final int shelterFromFt;
    /** Last foot of the shelter-in-place zone as printed (e.g. 1199). */
    public final int shelterToFt;
    /** Preferred evacuation distance (ft): at or beyond this. */
    public final int preferredFt;

    public StandoffRow(String id, String label, int capacityLb,
            int mandatoryFt, int shelterFromFt, int shelterToFt,
            int preferredFt) {
        this.id = id;
        this.label = label;
        this.capacityLb = capacityLb;
        this.mandatoryFt = mandatoryFt;
        this.shelterFromFt = shelterFromFt;
        this.shelterToFt = shelterToFt;
        this.preferredFt = preferredFt;
    }

    /** True if the row is internally consistent (used for received data). */
    public boolean isConsistent() {
        return capacityLb > 0 && mandatoryFt > 0
                && shelterFromFt == mandatoryFt + 1
                && shelterToFt >= shelterFromFt
                && preferredFt == shelterToFt + 1;
    }

    @Override
    public String toString() {
        return id + "(" + capacityLb + " lb: " + mandatoryFt + "/"
                + shelterFromFt + "-" + shelterToFt + "/" + preferredFt + " ft)";
    }
}
