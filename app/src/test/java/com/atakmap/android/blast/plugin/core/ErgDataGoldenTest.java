package com.atakmap.android.blast.plugin.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Golden values from ERG2024 (page numbers as printed). A changed value
 * means a new ERG edition: add it alongside, don't edit these.
 */
public class ErgDataGoldenTest {

    @Test
    public void lpgIedTable_p368() {
        int[][] v = {
                // kg, L, lb, gal, fireball m, ft, safe m, ft
                { 9, 19, 20, 5, 12, 40, 48, 160 },
                { 45, 95, 100, 25, 21, 69, 84, 276 },
                { 907, 1893, 2000, 500, 56, 184, 224, 736 },
                { 3630, 7570, 8000, 2000, 89, 292, 356, 1168 },
                { 18144, 37850, 40000, 10000, 152, 499, 608, 1996 },
        };
        String[] labels = { "Small LPG Tank", "Large LPG Tank",
                "Commercial/Residential LPG Tank", "Small LPG Truck",
                "Semitanker LPG" };
        assertEquals(v.length, ErgData.LPG_IED.size());
        for (int i = 0; i < v.length; i++) {
            ErgData.LpgRow r = ErgData.LPG_IED.get(i);
            String at = labels[i];
            assertEquals(at, labels[i], r.label);
            assertEquals(at, v[i][0], r.massKg);
            assertEquals(at, v[i][1], r.volumeL);
            assertEquals(at, v[i][2], r.massLb);
            assertEquals(at, v[i][3], r.volumeGal);
            assertEquals(at, v[i][4], r.fireballDiameterM);
            assertEquals(at, v[i][5], r.fireballDiameterFt);
            assertEquals(at, v[i][6], r.safeDistanceM);
            assertEquals(at, v[i][7], r.safeDistanceFt);
            // Footnote 3: safe distance about 4 x flame height; here 4 x diameter.
            assertEquals(at, 4 * r.fireballDiameterM, r.safeDistanceM);
        }
    }

    @Test
    public void bleveTable_p359() {
        // L, propane kg, min failure, empty, fireball m, ft, response m, ft,
        // min evac m, ft, pref evac m, ft, L/min, gal/min
        int[][] v = {
                { 100, 40, 4, 8, 10, 33, 90, 295, 154, 505, 307, 1007, 97, 26 },
                { 400, 160, 4, 12, 16, 52, 90, 295, 244, 801, 488, 1601, 195, 52 },
                { 2000, 800, 5, 18, 28, 92, 111, 364, 417, 1368, 834, 2736, 435, 115 },
                { 4000, 1600, 5, 20, 35, 115, 140, 459, 525, 1722, 1050, 3445, 615, 163 },
                { 8000, 3200, 6, 22, 44, 144, 176, 577, 661, 2169, 1323, 4341, 870, 230 },
                { 22000, 8800, 7, 28, 62, 203, 247, 810, 926, 3038, 1852, 6076, 1443, 381 },
                { 42000, 16800, 7, 32, 77, 253, 306, 1004, 1149, 3770, 2200, 7218, 1994, 527 },
                { 82000, 32800, 8, 40, 96, 315, 383, 1257, 1435, 4708, 2200, 7218, 2786, 736 },
                { 140000, 56000, 9, 45, 114, 374, 457, 1499, 1715, 5627, 2200, 7218, 3640, 962 },
        };
        double[][] dims = { // gal, diameter m, length m
                { 26.4, 0.3, 1.5 }, { 106, 0.61, 1.5 }, { 528, 0.96, 3 },
                { 1057, 1, 4.9 }, { 2113, 1.25, 6.5 }, { 5812, 2.1, 6.7 },
                { 11095, 2.1, 11.8 }, { 21662, 2.75, 13.7 },
                { 36984, 3.3, 17.2 } };
        assertEquals(v.length, ErgData.BLEVE.size());
        int prevPref = 0;
        for (int i = 0; i < v.length; i++) {
            ErgData.BleveRow r = ErgData.BLEVE.get(i);
            String at = "BLEVE " + v[i][0] + " L";
            int[] e = { r.capacityL, r.propaneKg, r.minFailureMin,
                    r.timeToEmptyMin, r.fireballRadiusM, r.fireballRadiusFt,
                    r.responseM, r.responseFt, r.minEvacM, r.minEvacFt,
                    r.prefEvacM, r.prefEvacFt, r.coolingLpm, r.coolingGpm };
            for (int k = 0; k < e.length; k++)
                assertEquals(at + " col " + k, v[i][k], e[k]);
            assertEquals(at, dims[i][0], r.capacityGal, 0);
            assertEquals(at, dims[i][1], r.diameterM, 0);
            assertEquals(at, dims[i][2], r.lengthM, 0);
            assertTrue(at, r.minEvacM < r.prefEvacM || r.prefEvacM == 2200);
            assertTrue(at, r.prefEvacM >= prevPref);
            prevPref = r.prefEvacM;
        }
    }

    @Test
    public void explosivesGuides() {
        assertEquals(112, ErgData.GUIDE_112.guide);
        assertEquals(500, ErgData.GUIDE_112.isolateM);
        assertEquals(800, ErgData.GUIDE_112.largeSpillEvacM);
        assertEquals(1600, ErgData.GUIDE_112.fireIsolateM);
        assertEquals(1600, ErgData.GUIDE_112.fragmentsM);
        assertEquals(114, ErgData.GUIDE_114.guide);
        assertEquals(100, ErgData.GUIDE_114.isolateM);
        assertEquals(250, ErgData.GUIDE_114.largeSpillEvacM);
        assertEquals(800, ErgData.GUIDE_114.fireIsolateM);
        assertEquals(800, ErgData.GUIDE_114.fragmentsM);
        assertEquals(15, ErgData.GUIDE_114_1_4S_ISOLATE_M);
        assertEquals(100, ErgData.RADIO_STANDOFF_M);
    }
}
