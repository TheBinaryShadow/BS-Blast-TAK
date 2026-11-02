package com.atakmap.android.blast.plugin.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Cordon distances for explosive ordnance (UXO) by type, from
 * ATP 4-32.2 / MCRP 10-10D.1 / NTTP 3-02.4.1 / AFTTP 3-2.12, "Multi-Service
 * Tactics, Techniques, and Procedures for Explosive Ordnance", 01 March 2024
 * (Distribution A: approved for public release; distribution unlimited).
 *
 * <ul>
 * <li>Figure 31 (p. 58), "Cordon Distances by Type Quick Reference Guide":
 * six types with their cordon distance.</li>
 * <li>"Minimum safe distance for exposed personnel is 300 meters from
 * EO/UXO" (Fig. 31; para 3.a.(2), p. 19).</li>
 * <li>"When more than one EO/UXO is found the CORDON should be extended by
 * 50 meters or more" (Fig. 31).</li>
 * <li>Table 1 (p. 18), evacuation distance by explosive weight, kept for
 * reference and the planned calculator.</li>
 * </ul>
 *
 * If any value here changes, the source changed: add a new edition, don't
 * edit this one.
 */
public final class UxoTable {

    public static final String SOURCE_ID = "ATP4-32.2";
    public static final String EDITION = "2024-03-01";
    public static final String FIGURE = "31";
    public static final String EDITION_LABEL = "01 Mar 2024";
    public static final String SHORT_NAME = "ATP 4-32.2";
    public static final String TITLE = "ATP 4-32.2 / MCRP 10-10D.1 / "
            + "NTTP 3-02.4.1 / AFTTP 3-2.12, Multi-Service Tactics, "
            + "Techniques, and Procedures for Explosive Ordnance";

    /** Minimum safe distance for exposed personnel (Fig. 31, para 3.a.(2)). */
    public static final int MIN_CORDON_M = 300;
    /** Added when more than one item is found ("50 meters or more"). */
    public static final int MULTI_ADD_M = 50;
    /**
     * Radios and phones: ATP 4-32.2 says 30 m (100 ft), the UNMAS handbook
     * 50 m. Blast uses the larger.
     */
    public static final int RADIO_MIN_M = 50;
    /** Largest cordon accepted from an edition this build doesn't have. */
    static final int MAX_CORDON_M = 30_000;

    public static final String UNKNOWN = "UNKNOWN";

    /** One type of ordnance. */
    public static final class Category {
        public final String id;
        /** Wording of ATP 4-32.2 Fig. 31. */
        public final String doctrineLabel;
        /** Button text in plain words. */
        public final String plainLabel;
        /** Fig. 31 "average weight of EO/UXO" bound, lb (0 for unknown). */
        public final int maxWeightLb;
        /** Fig. 31 cordon distance, metres. */
        public final int cordonM;

        Category(String id, String doctrineLabel, String plainLabel,
                int maxWeightLb, int cordonM) {
            this.id = id;
            this.doctrineLabel = doctrineLabel;
            this.plainLabel = plainLabel;
            this.maxWeightLb = maxWeightLb;
            this.cordonM = cordonM;
        }

        public boolean isUnknown() {
            return UNKNOWN.equals(id);
        }
    }

    private static final List<Category> FIG31 = Collections.unmodifiableList(
            Arrays.asList(
                    new Category("GREN_MINE_SUBMUN",
                            "Grenades, Landmines, Submunitions",
                            "Grenade, landmine or bomblet", 27, 300),
                    new Category("PROJ_MORTAR", "Projectiles and Mortars",
                            "Artillery shell or mortar", 50, 375),
                    new Category("MISSILE_ROCKET", "Missiles and Rockets",
                            "Rocket or missile", 100, 475),
                    new Category("LARGE_GUIDED_MISSILE",
                            "Large and Guided Missiles",
                            "Large or guided missile", 200, 600),
                    new Category("BOMB_DISPENSER", "Bombs and Dispensers",
                            "Aircraft bomb or dispenser", 300, 675),
                    new Category("STOCKPILE", "Stockpiles of EO or UXO",
                            "Pile or cache of ordnance", 500, 800)));

    /**
     * Not a Fig. 31 row: an item the user can't put in a type. Drawn at the
     * doctrinal minimum, or at the largest single-item row (Bombs and
     * Dispensers, 675 m) when it looks large.
     */
    public static final Category UNKNOWN_CATEGORY = new Category(UNKNOWN,
            "Unknown explosive ordnance", "Unknown object", 0,
            MIN_CORDON_M);
    public static final int UNKNOWN_LARGE_M = 675;

    /** Table 1, Evacuation Distances (p. 18): {explosive weight lb, metres}. */
    static final int[][] TABLE1 = {
            { 27, 300 }, { 30, 310 }, { 35, 330 }, { 40, 350 }, { 45, 360 },
            { 50, 375 }, { 100, 475 }, { 150, 550 }, { 200, 600 },
            { 300, 675 }, { 400, 725 }, { 500, 800 }
    };

    private UxoTable() {
    }

    /** The six Fig. 31 types, in the figure's order. */
    public static List<Category> categories() {
        return FIG31;
    }

    /** The Fig. 31 types plus Unknown (picker order). */
    public static List<Category> pickerCategories() {
        Category[] all = FIG31.toArray(new Category[0]);
        Category[] out = Arrays.copyOf(all, all.length + 1);
        out[all.length] = UNKNOWN_CATEGORY;
        return Collections.unmodifiableList(Arrays.asList(out));
    }

    /** A category by id (including UNKNOWN), or null. */
    public static Category category(String id) {
        if (id == null)
            return null;
        if (UNKNOWN.equals(id))
            return UNKNOWN_CATEGORY;
        for (Category c : FIG31)
            if (c.id.equals(id))
                return c;
        return null;
    }

    /** True for the source and edition this build carries. */
    public static boolean isKnown(String sourceId, String edition) {
        return SOURCE_ID.equals(sourceId) && EDITION.equals(edition);
    }

    /**
     * The cordon to draw, in metres: the Fig. 31 distance, or for Unknown
     * the 300 m minimum (675 m when it looks large), plus 50 m when more
     * than one item was found. Never below 300 m. -1 for an unknown id.
     */
    public static int cordonM(String categoryId, boolean multiple,
            boolean large) {
        Category c = category(categoryId);
        if (c == null)
            return -1;
        int base = c.isUnknown() && large ? UNKNOWN_LARGE_M : c.cordonM;
        int r = base + (multiple ? MULTI_ADD_M : 0);
        return Math.max(r, MIN_CORDON_M);
    }

    /** Table 1 row at or above an explosive weight; -1 above 500 lb. */
    public static int table1M(double explosiveLb) {
        if (!(explosiveLb > 0) || Double.isInfinite(explosiveLb))
            return -1;
        for (int[] row : TABLE1)
            if (explosiveLb <= row[0])
                return row[1];
        return -1;
    }
}
