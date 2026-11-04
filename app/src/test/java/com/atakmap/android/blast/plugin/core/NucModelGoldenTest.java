package com.atakmap.android.blast.plugin.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Golden values for the nuclear model, each tied to a statement or worked
 * example in the source documents, plus monotonicity sweeps.
 */
public class NucModelGoldenTest {

    private static final NucModel.Burst S = NucModel.Burst.SURFACE,
            A = NucModel.Burst.AIR, U = NucModel.Burst.UNKNOWN;

    private static double mi(double m) {
        return m / NucModel.M_PER_MI;
    }

    @Test
    public void femaZonesAt10kT() {
        // FEMA 3rd ed. pp. 18-19.
        assertEquals(0.5, mi(NucModel.sdzM(10, S)), 1e-9);
        assertEquals(1.0, mi(NucModel.mdzM(10, S)), 1e-9);
        assertEquals(3.0, mi(NucModel.ldzM(10, S)), 1e-9);
    }

    @Test
    public void femaFigure5Scaling() {
        // MDZ for 0.1, 1, 10, 100 kT: about 1/4, 1/2, 1, 2 1/4 miles.
        assertEquals(0.25, mi(NucModel.mdzM(0.1, S)), 0.04);
        assertEquals(0.5, mi(NucModel.mdzM(1, S)), 0.05);
        assertEquals(2.25, mi(NucModel.mdzM(100, S)), 0.12);
        // "a factor of two" per tenfold: cube root of 10.
        assertEquals(Math.cbrt(10),
                NucModel.ldzM(100, S) / NucModel.ldzM(10, S), 1e-9);
    }

    @Test
    public void airEnvelopeRatiosFromFigure373c() {
        assertEquals(1683.0 / 1068.0,
                NucModel.sdzM(10, A) / NucModel.sdzM(10, S), 1e-9);
        assertEquals(4199.0 / 2516.0,
                NucModel.mdzM(10, A) / NucModel.mdzM(10, S), 1e-9);
        assertEquals(2.0, NucModel.ldzM(10, A) / NucModel.ldzM(10, S), 1e-9);
        // Glasstone §3.73: the 4 psi envelope at 1 kt is about 2,600 ft; it
        // lies between the air SDZ and MDZ edges (5-8 and 2-3 psi).
        double ft4 = 2600 * 0.3048;
        assertTrue(NucModel.sdzM(1, A) < ft4);
        assertTrue(NucModel.mdzM(1, A) > ft4);
        // Unknown draws the air envelope.
        assertEquals(NucModel.ldzM(50, A), NucModel.ldzM(50, U), 1e-9);
    }

    @Test
    public void burnThresholdFromFigure1264() {
        assertEquals(3.55, NucModel.burnThreshold(1), 1e-9);
        assertEquals(3.85, NucModel.burnThreshold(10), 1e-9);
        assertEquals(5.46, NucModel.burnThreshold(1000), 1e-9);
        assertTrue(NucModel.burnThreshold(200) > 4.51);
        assertTrue(NucModel.burnThreshold(200) < 5.17);
    }

    @Test
    public void thermalRangesFromFigure742() {
        // §7.43: 8 cal/cm² from a 1 Mt air burst at about 7 miles.
        assertEquals(7.0, NucModel.thermalRangeMi(1000, 8), 0.5);
        // §12.66: 4.5-6 cal/cm² at 1 Mt "from 9 to 10 miles" (read loosely
        // from the same figure); ours lies close.
        double far = NucModel.thermalRangeMi(1000, 4.5);
        double near = NucModel.thermalRangeMi(1000, 6);
        assertTrue(far > 8.5 && far < 10.5);
        assertTrue(near > 7.5 && near < far);
        // Table points reproduce exactly.
        assertEquals(1.382, NucModel.thermalRangeMi(10, 5), 1e-6);
    }

    @Test
    public void burnRingsMatchFemaFigure7() {
        // FEMA Fig. 7 (surface): 2nd-degree burns about 1 mi at 10 kT and
        // about 3 mi at 100 kT.
        assertEquals(1.1, mi(NucModel.burnM(10, S)), 0.2);
        assertEquals(2.9, mi(NucModel.burnM(100, S)), 0.3);
        assertTrue(NucModel.burnM(10, A) > NucModel.burnM(10, S));
        assertEquals(NucModel.burnM(10, A), NucModel.burnM(10, U), 1e-9);
    }

    @Test
    public void glasstoneGammaExample834() {
        // §8.34: 50 kt fission air burst at 2,000 yards: "about 250 rads".
        assertEquals(250, NucModel.gammaDose(50, 2000 * 0.9144), 40);
    }

    @Test
    public void radiationRingsMatchFemaFigure7() {
        // FEMA Fig. 7 (surface, 100 rad): about 0.4, 0.7, 1.0, 1.3 miles.
        assertEquals(0.4, mi(NucModel.radiationM(0.1, S)), 0.08);
        assertEquals(0.7, mi(NucModel.radiationM(1, S)), 0.08);
        assertEquals(1.0, mi(NucModel.radiationM(10, S)), 0.1);
        assertEquals(1.3, mi(NucModel.radiationM(100, S)), 0.2);
        // "within about a mile" (FEMA §1.4.1) for the 10 kT scenario.
        assertTrue(mi(NucModel.radiationM(10, U)) < 1.2);
    }

    @Test
    public void surfaceCorrectionsTable837() {
        assertEquals(2.0 / 3.0, NucModel.surfaceGammaFactor(10), 1e-9);
        assertEquals(1, NucModel.surfaceGammaFactor(100), 1e-9);
        assertEquals(1.25, NucModel.surfaceGammaFactor(300), 1e-9);
        assertEquals(2, NucModel.surfaceGammaFactor(2000), 1e-9);
        assertEquals(3, NucModel.surfaceGammaFactor(10000), 1e-9);
        double r = 1500 * 0.9144;
        assertEquals(NucModel.gammaDose(10, r) * 2 / 3
                + NucModel.neutronDose(10, r) / 2,
                NucModel.initialDose(10, r, true), 1e-6);
        // Unknown is the larger of the two.
        assertTrue(NucModel.radiationM(10000, U) >= NucModel.radiationM(10000,
                A));
        assertTrue(NucModel.radiationM(10000, U) >= NucModel.radiationM(10000,
                S));
    }

    @Test
    public void figureTablesAreMonotonic() {
        double[][][] figs = { NucModel.GAMMA_FIS, NucModel.NEUT_FIS,
                NucModel.GAMMA_TN, NucModel.NEUT_TN, NucModel.TH_MI };
        for (double[][] f : figs) {
            for (int i = 0; i < f.length; i++) {
                for (int j = 1; j < f[i].length; j++)
                    assertTrue("rises with yield", f[i][j] > f[i][j - 1]);
                if (i > 0)
                    for (int j = 0; j < f[i].length; j++)
                        assertTrue("falls with dose", f[i][j] < f[i - 1][j]);
            }
        }
    }

    @Test
    public void ringsGrowWithYieldEverywhere() {
        for (NucModel.Burst b : NucModel.Burst.values()) {
            double prevS = 0, prevB = 0, prevR = 0;
            for (double kt = NucModel.MIN_KT; kt <= NucModel.MAX_KT; kt *= 1.2) {
                double s = NucModel.ldzM(kt, b), br = NucModel.burnM(kt, b),
                        r = NucModel.radiationM(kt, b);
                assertTrue(s > prevS);
                assertTrue(b + " burn " + kt, br > prevB);
                assertTrue(b + " rad " + kt, r > prevR * 0.999);
                assertTrue(NucModel.sdzM(kt, b) < NucModel.mdzM(kt, b));
                assertTrue(NucModel.mdzM(kt, b) < s);
                prevS = s;
                prevB = br;
                prevR = r;
            }
        }
    }

    @Test
    public void table993Fallout() {
        // 10 kt, 15 mph: downwind 8.9 W^0.45 for 100 rad/h.
        NucModel.Contour c = NucModel.contour(10, 100, 15);
        assertNotNull(c);
        assertEquals(8.9 * Math.pow(10, 0.45), mi(c.downwindM), 1e-9);
        assertEquals(0.38 * Math.pow(10, 0.60), mi(c.maxWidthM), 1e-9);
        assertEquals(0.39 * Math.pow(10, 0.42), mi(c.gzWidthM), 1e-9);
        // §9.97 worked example: 30 mph gives F = 1.25.
        assertEquals(1.25, NucModel.windFactor(30), 1e-9);
        assertEquals(1 + (8 - 15) / 30.0, NucModel.windFactor(3), 1e-9);
        assertEquals(1.5, NucModel.windFactor(99), 1e-9);
        // At 1 kt the ground-zero width exceeds the stated maximum.
        NucModel.Contour low = NucModel.contour(1, 3000, 15);
        assertEquals(low.gzWidthM, low.maxWidthM, 1e-9);
        assertNull(NucModel.contour(10, 42, 15));
    }

    @Test
    public void falloutOutlineShape() {
        NucModel.Contour c = NucModel.contour(10, 10, 15);
        double[][] p = NucModel.outline(c);
        assertTrue(Polygons.contains(p, 0, 0));
        assertTrue(Polygons.contains(p, c.downwindM * 0.5, 0));
        assertTrue(Polygons.contains(p, c.downwindM * 0.99, 0));
        assertFalse(Polygons.contains(p, c.downwindM * 1.01, 0));
        assertFalse(Polygons.contains(p, -c.upwindM() * 1.05, 0));
        assertTrue(Polygons.contains(p, -c.upwindM() * 0.9, 0));
        assertTrue(Polygons.contains(p, NucModel.WIDEST_AT * c.downwindM,
                c.maxWidthM / 2 * 0.98));
        assertFalse(Polygons.contains(p, NucModel.WIDEST_AT * c.downwindM,
                c.maxWidthM / 2 * 1.02));
        // Inner contours lie inside outer ones along the axis.
        assertTrue(NucModel.contour(10, 1000, 15).downwindM
                < NucModel.contour(10, 100, 15).downwindM);
    }

    @Test
    public void sevenTenRule() {
        // §9.15: every sevenfold increase in time, a tenfold decrease.
        assertEquals(0.1, NucModel.decay(7), 0.005);
        assertEquals(0.01, NucModel.decay(49), 0.001);
        assertEquals(0.435, NucModel.decay(2), 0.001);
    }

    @Test
    public void presetsAreValidAndUnique() {
        java.util.Set<String> ids = new java.util.HashSet<>();
        for (NucPresets.Preset p : NucPresets.all()) {
            assertTrue(p.id, ids.add(p.id));
            assertTrue(p.id, NucModel.validYield(p.kt));
            if (p.group != NucPresets.Group.YIELD)
                assertFalse(p.id, p.source.isEmpty());
        }
        assertEquals(90, NucPresets.byId("US_W76_1").kt, 0);
        assertEquals(455, NucPresets.byId("US_W88").kt, 0);
        assertEquals(4500, NucPresets.byId("CN_DF5A").kt, 0);
        assertEquals("4.5 Mt", NucPresets.yieldLabel(4500));
        assertEquals("0.1 kt", NucPresets.yieldLabel(0.1));
        assertEquals("15 kt", NucPresets.yieldLabel(15));
    }
}
