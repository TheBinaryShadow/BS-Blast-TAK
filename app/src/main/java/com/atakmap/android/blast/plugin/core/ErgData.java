package com.atakmap.android.blast.plugin.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Reference tables from the 2024 Emergency Response Guidebook (PHMSA /
 * Transport Canada / SCT / CIQUIME), copied verbatim with printed page
 * numbers. Extracted Oct 30, 2026 from ERG2024-Eng-Web-a.pdf
 * (SHA-256 180eb92e…5c96e30) and checked against the rendered pages.
 * Not yet drawn by the plugin (planned secondary presets).
 */
public final class ErgData {

    private ErgData() {
    }

    public static final String EDITION = "ERG2024";

    // ------------------------------------------------------------------
    // p. 368: IED safe stand-off for LPG tanks with explosives on the exterior

    public static final class LpgRow {
        public final String id;
        public final String label;
        public final int massKg;
        public final int volumeL;
        public final int massLb;
        public final int volumeGal;
        public final int fireballDiameterM;
        public final int fireballDiameterFt;
        public final int safeDistanceM;
        public final int safeDistanceFt;

        LpgRow(String id, String label, int massKg, int volumeL, int massLb,
                int volumeGal, int fireballDiameterM, int fireballDiameterFt,
                int safeDistanceM, int safeDistanceFt) {
            this.id = id;
            this.label = label;
            this.massKg = massKg;
            this.volumeL = volumeL;
            this.massLb = massLb;
            this.volumeGal = volumeGal;
            this.fireballDiameterM = fireballDiameterM;
            this.fireballDiameterFt = fireballDiameterFt;
            this.safeDistanceM = safeDistanceM;
            this.safeDistanceFt = safeDistanceFt;
        }
    }

    public static final String LPG_PAGE = "p. 368";

    public static final List<LpgRow> LPG_IED = Collections.unmodifiableList(
            Arrays.asList(
                    new LpgRow("LPG_SMALL", "Small LPG Tank", 9, 19, 20, 5, 12, 40, 48, 160),
                    new LpgRow("LPG_LARGE", "Large LPG Tank", 45, 95, 100, 25, 21, 69, 84, 276),
                    new LpgRow("LPG_COMMERCIAL", "Commercial/Residential LPG Tank", 907, 1893, 2000, 500, 56, 184, 224, 736),
                    new LpgRow("LPG_SMALL_TRUCK", "Small LPG Truck", 3630, 7570, 8000, 2000, 89, 292, 356, 1168),
                    new LpgRow("LPG_SEMITANKER", "Semitanker LPG", 18144, 37850, 40000, 10000, 152, 499, 608, 1996)));

    /** Footnotes of p. 368, paraphrased. */
    public static final List<String> LPG_NOTES = Collections.unmodifiableList(Arrays.asList(
            "Mass/volume is the most LPG that could reasonably fit in the "
                    + "container or vehicle; variations possible.",
            "Fireball diameter assumes efficient mixing of the gas with air.",
            "Safe distance follows U.S. firefighting practice of about 4 times "
                    + "the flame height.",
            "For a loaded LPG tank with explosives on the exterior. A tank "
                    + "filled with high explosives needs a significantly "
                    + "greater stand-off."));

    // ------------------------------------------------------------------
    // p. 359: BLEVE (use with caution)

    public static final class BleveRow {
        public final int capacityL;
        public final double capacityGal;
        public final double diameterM;
        public final double lengthM;
        public final int propaneKg;
        public final int minFailureMin;
        public final int timeToEmptyMin;
        public final int fireballRadiusM;
        public final int fireballRadiusFt;
        public final int responseM;
        public final int responseFt;
        public final int minEvacM;
        public final int minEvacFt;
        public final int prefEvacM;
        public final int prefEvacFt;
        public final int coolingLpm;
        public final int coolingGpm;

        BleveRow(int capacityL, double capacityGal, double diameterM,
                double lengthM, int propaneKg, int minFailureMin,
                int timeToEmptyMin, int fireballRadiusM, int fireballRadiusFt,
                int responseM, int responseFt, int minEvacM, int minEvacFt,
                int prefEvacM, int prefEvacFt, int coolingLpm,
                int coolingGpm) {
            this.capacityL = capacityL;
            this.capacityGal = capacityGal;
            this.diameterM = diameterM;
            this.lengthM = lengthM;
            this.propaneKg = propaneKg;
            this.minFailureMin = minFailureMin;
            this.timeToEmptyMin = timeToEmptyMin;
            this.fireballRadiusM = fireballRadiusM;
            this.fireballRadiusFt = fireballRadiusFt;
            this.responseM = responseM;
            this.responseFt = responseFt;
            this.minEvacM = minEvacM;
            this.minEvacFt = minEvacFt;
            this.prefEvacM = prefEvacM;
            this.prefEvacFt = prefEvacFt;
            this.coolingLpm = coolingLpm;
            this.coolingGpm = coolingGpm;
        }
    }

    public static final String BLEVE_PAGE = "p. 359";

    /** Capacity L (gal), diameter m, length m, propane kg, times, distances. */
    public static final List<BleveRow> BLEVE = Collections.unmodifiableList(
            Arrays.asList(
                    new BleveRow(100, 26.4, 0.3, 1.5, 40, 4, 8, 10, 33, 90, 295, 154, 505, 307, 1007, 97, 26),
                    new BleveRow(400, 106, 0.61, 1.5, 160, 4, 12, 16, 52, 90, 295, 244, 801, 488, 1601, 195, 52),
                    new BleveRow(2000, 528, 0.96, 3, 800, 5, 18, 28, 92, 111, 364, 417, 1368, 834, 2736, 435, 115),
                    new BleveRow(4000, 1057, 1, 4.9, 1600, 5, 20, 35, 115, 140, 459, 525, 1722, 1050, 3445, 615, 163),
                    new BleveRow(8000, 2113, 1.25, 6.5, 3200, 6, 22, 44, 144, 176, 577, 661, 2169, 1323, 4341, 870, 230),
                    new BleveRow(22000, 5812, 2.1, 6.7, 8800, 7, 28, 62, 203, 247, 810, 926, 3038, 1852, 6076, 1443, 381),
                    new BleveRow(42000, 11095, 2.1, 11.8, 16800, 7, 32, 77, 253, 306, 1004, 1149, 3770, 2200, 7218, 1994, 527),
                    new BleveRow(82000, 21662, 2.75, 13.7, 32800, 8, 40, 96, 315, 383, 1257, 1435, 4708, 2200, 7218, 2786, 736),
                    new BleveRow(140000, 36984, 3.3, 17.2, 56000, 9, 45, 114, 374, 457, 1499, 1715, 5627, 2200, 7218, 3640, 962)));

    // ------------------------------------------------------------------
    // Guides 112 (pp. 154-155) and 114 (pp. 158-159): explosives in transport

    public static final class ExplosivesGuide {
        public final int guide;
        public final String divisions;
        public final String pages;
        public final int fragmentsM;
        public final int isolateM;
        public final int largeSpillEvacM;
        public final int fireIsolateM;

        ExplosivesGuide(int guide, String divisions, String pages,
                int fragmentsM, int isolateM, int largeSpillEvacM,
                int fireIsolateM) {
            this.guide = guide;
            this.divisions = divisions;
            this.pages = pages;
            this.fragmentsM = fragmentsM;
            this.isolateM = isolateM;
            this.largeSpillEvacM = largeSpillEvacM;
            this.fireIsolateM = fireIsolateM;
        }
    }

    /** Divisions 1.1, 1.2, 1.3, 1.5: isolate 500 m; large spill 800 m; fire 1600 m. */
    public static final ExplosivesGuide GUIDE_112 = new ExplosivesGuide(112,
            "1.1, 1.2, 1.3, 1.5", "pp. 154-155", 1600, 500, 800, 1600);

    /** Divisions 1.4, 1.6: isolate 100 m; large spill 250 m; fire 800 m. */
    public static final ExplosivesGuide GUIDE_114 = new ExplosivesGuide(114,
            "1.4, 1.6", "pp. 158-159", 800, 100, 250, 800);

    /** Guide 114: packages labelled 1.4S threatened by fire, at least 15 m. */
    public static final int GUIDE_114_1_4S_ISOLATE_M = 15;

    /** Guides 112 and 114: no radio transmitters within 100 m of electric detonators. */
    public static final int RADIO_STANDOFF_M = 100;
}
