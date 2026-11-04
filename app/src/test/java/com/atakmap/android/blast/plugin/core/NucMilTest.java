package com.atakmap.android.blast.plugin.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** FM 3-3-1 / FM 3-11.3 worked examples and table checks. */
public class NucMilTest {

    @Test
    public void tableE4SpotValues() {
        // Read from the page image of FM 3-3-1 p. E-11.
        assertEquals(57, NucMil.E4_W.length);
        assertEquals(0.5, NucMil.zoneIKm(0.1, 8), 1e-9);
        assertEquals(19, NucMil.zoneIKm(10, 30), 1e-9);
        assertEquals(134, NucMil.zoneIKm(100, 160), 1e-9);
        assertEquals(398, NucMil.zoneIKm(1000, 160), 1e-9);
        assertEquals(1184, NucMil.zoneIKm(10000, 160), 1e-9);
        assertEquals(260, NucMil.zoneIKm(10000, 8), 1e-9);
    }

    @Test
    public void publishedDipsAreRaised() {
        // 4 kt at 8 km/h is printed 6 (below 3 kt's 6.1).
        int i4 = 21;
        assertEquals(4, NucMil.E4_W[i4], 0);
        assertEquals(6, NucMil.E4_PUBLISHED[i4][0], 0);
        assertEquals(6.1, NucMil.E4[i4][0], 1e-9);
        for (int i = 0; i < NucMil.E4.length; i++)
            for (int j = 0; j < NucMil.E4[i].length; j++) {
                assertTrue(NucMil.E4[i][j] >= NucMil.E4_PUBLISHED[i][j]);
                if (i > 0)
                    assertTrue(NucMil.E4[i][j] >= NucMil.E4[i - 1][j]);
                if (j > 0)
                    assertTrue(NucMil.E4[i][j] >= NucMil.E4[i][j - 1]);
            }
    }

    @Test
    public void fm331NormalCaseExample() {
        // FM 3-3-1 p. 3-24: 16 kt, EWS 16 km/h, line CHARLIE: Zone I 18 km,
        // Zone II 36 km, 30 kt semicircle, arrival arcs at 16 and 32 km.
        NucMil.Prediction p = NucMil.predict(16, 90, 16, Double.NaN);
        assertEquals(18, Math.round(p.zoneIKm));
        assertEquals(2 * p.zoneIKm, p.zoneIIKm, 1e-9);
        assertEquals("CHARLIE", NucMil.group(16));
        assertEquals(4.2, p.cloudRadiusKm, 1e-9);
        assertEquals(40, p.angleDeg, 0);
        assertEquals(2, p.arrivalHours().size());
        assertEquals(1.0, p.arrivalH(16), 1e-9);
    }

    @Test
    public void fm3113NomogramExample() {
        // FM 3-11.3 Fig. G-16: 35 kt, EWS 29 km/h: Zone I about 33 km.
        assertEquals(33, NucMil.zoneIKm(35, 29), 2);
    }

    @Test
    public void navyExample() {
        // FM 3-3-1 p. 3-28: 100 kt (DELTA), 16 knots: Zone I 30 nmi, cloud
        // radius 3.7 nmi.
        double kph = 16 * 1.852;
        assertEquals(30 * 1.852, NucMil.zoneIKm(100, kph), 2);
        assertEquals(3.7 * 1.852, NucMil.cloudRadiusKm(70), 0.1);
    }

    @Test
    public void circularCases() {
        // FM 3-3-1 p. 3-24: 4 kt, EDM line "007": circles of 7 and 14 km.
        NucMil.Edm e = NucMil.parseEdm("BRAVO 007");
        assertNotNull(e);
        assertTrue(e.isCircular());
        NucMil.Prediction c = NucMil.circular(4, e.zoneIKm);
        assertEquals(14, c.zoneIIKm, 1e-9);
        assertEquals(1, c.zoneAt(0, 6.9));
        assertEquals(2, c.zoneAt(-13.9, 0));
        assertEquals(0, c.zoneAt(14.1, 0));
        assertTrue(c.arrivalHours().isEmpty());
        // Below 8 km/h: circular, Zone I from the 8 km/h column.
        NucMil.Prediction low = NucMil.predict(10, 90, 5, Double.NaN);
        assertTrue(low.circular);
        assertEquals(10, low.zoneIKm, 1e-9);
    }

    @Test
    public void edmParsing() {
        NucMil.Edm n = NucMil.parseEdm("CHARLIE 090016");
        assertEquals(90, n.eddDeg, 0);
        assertEquals(16, n.ewsKph, 0);
        assertTrue(Double.isNaN(n.angleDeg));
        NucMil.Edm x = NucMil.parseEdm("090016060");
        assertEquals(60, x.angleDeg, 0);
        assertNull(NucMil.parseEdm("09001"));
        assertNull(NucMil.parseEdm("400016"));
        assertNull(NucMil.parseEdm("090000"));
        assertNull(NucMil.parseEdm("090016020"));
        assertNull(NucMil.parseEdm("09-0016"));
        assertNull(NucMil.parseEdm(null));
    }

    @Test
    public void zoneGeometry() {
        NucMil.Prediction p = NucMil.predict(16, 90, 16, Double.NaN);
        double z = p.zoneIKm;
        assertEquals(1, p.zoneAt(z * 0.9, 0));
        assertEquals(1, p.zoneAt(-4.0, 0)); // upwind semicircle
        assertEquals(0, p.zoneAt(-4.4, 0));
        assertEquals(2, p.zoneAt(z * 1.5, 0));
        assertEquals(0, p.zoneAt(z * 2.1, 0));
        // Radial lines at 20 degrees.
        double a = Math.toRadians(19);
        assertEquals(2, p.zoneAt(1.5 * z * Math.cos(a), 1.5 * z * Math.sin(a)));
        a = Math.toRadians(21);
        assertEquals(0, p.zoneAt(1.5 * z * Math.cos(a), 1.5 * z * Math.sin(a)));
        // Expanded 60 degrees.
        NucMil.Prediction w = NucMil.predict(16, 90, 16, 60);
        assertEquals(2, w.zoneAt(1.5 * z * Math.cos(a), 1.5 * z * Math.sin(a)));
        assertNotNull(p.arc(16));
        assertNull(p.arc(40));
        // Tiny yield with a weak wind: Zone I inside the cloud radius.
        NucMil.Prediction t = NucMil.predict(0.1, 0, 8, Double.NaN);
        assertTrue(t.withinCloud);
        assertEquals(1, t.zoneAt(-1.1, 0));
    }

    @Test
    public void groupsAndRadii() {
        assertEquals("ALFA", NucMil.group(2));
        assertEquals("BRAVO", NucMil.group(2.1));
        assertEquals("GOLF", NucMil.group(3000));
        assertNull(NucMil.group(3001));
        assertEquals(46, NucMil.cloudRadiusKm(10000), 1e-9);
        assertFalse(NucMil.extrapolated(10000, 160));
        assertTrue(NucMil.extrapolated(15000, 20));
        assertEquals(7, NucMil.polarKm(20), 1e-9);
    }
}
