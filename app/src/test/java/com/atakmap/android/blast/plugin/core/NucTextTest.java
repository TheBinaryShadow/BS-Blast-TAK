package com.atakmap.android.blast.plugin.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NucTextTest {

    private static NucRecord rec(NucModel.Burst b, boolean wind) {
        return NucRecord.create(10, b, "", wind ? 270 : Double.NaN,
                wind ? 15 : Double.NaN, "ALPHA-1", 1793544725000L)
                .withNumber(1);
    }

    @Test
    public void distances() {
        assertEquals("0.50 mi (0.80 km)", NucText.dist(0.5 * NucModel.M_PER_MI));
        assertEquals("3.0 mi (4.8 km)", NucText.dist(3 * NucModel.M_PER_MI));
    }

    @Test
    public void ringTitles() {
        NucRecord r = rec(NucModel.Burst.SURFACE, false);
        assertEquals("NUC SEVERE DAMAGE ZONE 0.50 mi (0.80 km) - planning "
                + "estimate", NucText.ringTitle(r, NucText.Ring.SDZ));
        assertTrue(NucText.ringTitle(rec(NucModel.Burst.AIR, false),
                NucText.Ring.LDZ).contains("air envelope"));
        assertFalse(NucText.ringTitle(rec(NucModel.Burst.AIR, false),
                NucText.Ring.BURN).contains("air envelope"));
        assertTrue(NucText.ringTitle(r.cleared("B", 1), NucText.Ring.MDZ)
                .startsWith("ENDED NUC"));
        assertTrue(NucText.ringTitle(r, NucText.Ring.GET_INSIDE)
                .contains("50 mi"));
    }

    @Test
    public void remarksMerge() {
        NucRecord r = rec(NucModel.Burst.SURFACE, true);
        String block = NucText.remarks(r);
        assertTrue(block.startsWith("[BLAST-NUC] Nuclear detonation 10 kt"));
        assertTrue(block.contains("GET INSIDE, STAY INSIDE, STAY TUNED"));
        assertTrue(block.endsWith(NucText.REMARKS_END));
        String merged = NucText.mergeRemarks("Mine.\n\n" + block + "\nAfter",
                r.cleared("B", 2));
        assertTrue(merged.startsWith("Mine.\n\n[BLAST-NUC] ENDED."));
        assertTrue(merged.endsWith("\nAfter"));
    }

    @Test
    public void whereAmI() {
        NucRecord r = rec(NucModel.Burst.SURFACE, true);
        double mi = NucModel.M_PER_MI;
        NucText.Where w = NucText.where(r, 0, 0.3 * mi);
        assertEquals(NucText.Ring.SDZ, w.zone);
        assertTrue(w.inBurn && w.inRad);
        assertEquals(NucText.Ring.MDZ, NucText.where(r, 0.8 * mi, 0).zone);
        assertEquals(NucText.Ring.LDZ, NucText.where(r, 0, -2 * mi).zone);
        assertEquals(NucText.Ring.GET_INSIDE,
                NucText.where(r, 0, 20 * mi).zone);
        assertNull(NucText.where(r, 0, 60 * mi).zone);
        // Wind from 270 (west): fallout goes east. 10 mi east is inside the
        // 100 R/h contour (25 mi long at 10 kt) and arrives ~40 min later.
        NucText.Where e = NucText.where(r, 10 * mi, 0);
        assertEquals(300, e.falloutRate);
        assertEquals(40.0 / 60, e.arrivalH, 1e-6);
        assertTrue(e.detail().contains("40 min"));
        // Upwind: none.
        assertEquals(0, NucText.where(r, -10 * mi, 0).falloutRate);
        // North of the axis by 3 mi at 10 mi east: outside 30 R/h width.
        assertTrue(NucText.where(r, 10 * mi, 3 * mi).falloutRate <= 10);
        // Air burst: no fallout drawn.
        assertEquals(0, NucText.where(rec(NucModel.Burst.AIR, true), 10 * mi,
                0).falloutRate);
        assertNull(NucText.where(r, Double.NaN, 0));
        assertArrayEquals(new int[] { 1, 2, 2 }, NucText.count(r,
                new double[][] { { 10 * mi, 0 }, { 0, 0.2 * mi }, null }));
    }

    @Test
    public void falloutLines() {
        assertTrue(NucText.falloutLine(rec(NucModel.Burst.AIR, true))
                .startsWith("Air burst"));
        assertTrue(NucText.falloutLine(rec(NucModel.Burst.UNKNOWN, false))
                .startsWith("Fallout direction unknown"));
        assertTrue(NucText.falloutLine(rec(NucModel.Burst.SURFACE, true))
                .contains("toward 90°"));
    }

    @Test
    public void referenceNumbersMatchTheModel() {
        StringBuilder all = new StringBuilder();
        for (UxoReference.Section s : NucReference.sections())
            for (String p : s.body)
                all.append(p).append('\n');
        String t = all.toString();
        assertTrue(t.contains("10 kt surface: severe 0.50 mi (0.80 km), "
                + "moderate 1.0 mi (1.6 km), light 3.0 mi (4.8 km)."));
        assertTrue(t.contains("x1.58") && t.contains("x1.67"));
        assertEquals(1.58, NucModel.AIR_SDZ, 0.005);
        assertEquals(1.67, NucModel.AIR_MDZ, 0.005);
        assertEquals(3.9, NucModel.burnThreshold(10), 0.05);
        assertEquals(5.5, NucModel.burnThreshold(1000), 0.05);
        assertEquals(0.44, NucModel.decay(2), 0.005);
        assertEquals(0.12, NucModel.decay(6), 0.005);
        assertEquals(0.022, NucModel.decay(24), 0.0005);
        assertEquals(0.0096, NucModel.decay(48), 0.00005);
        assertNotNull(NucReference.allSections());
        assertEquals(UxoReference.sections().size()
                + NucReference.sections().size(),
                NucReference.allSections().size());
    }

    @Test
    public void namesAndNumbers() {
        assertEquals("NUC 3 10 kt", Names.initialNuc(3, "10 kt"));
        assertEquals(3, Names.nucNumberIn("NUC 3 10 kt"));
        assertEquals(0, Names.nucNumberIn("UXO 3 x"));
        assertEquals(0, Names.numberIn("NUC 3 10 kt"));
    }
}
