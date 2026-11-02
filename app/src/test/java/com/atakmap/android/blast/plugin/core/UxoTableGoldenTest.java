package com.atakmap.android.blast.plugin.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Golden values: ATP 4-32.2 / MCRP 10-10D.1 / NTTP 3-02.4.1 / AFTTP 3-2.12,
 * 01 March 2024. Figure 31 (p. 58) and Table 1 (p. 18), read from the
 * publication. If a value here changes, the source changed: add an edition.
 */
public class UxoTableGoldenTest {

    /** {id, Fig. 31 wording, average weight lb, cordon m}. */
    private static final Object[][] FIG31 = {
            { "GREN_MINE_SUBMUN", "Grenades, Landmines, Submunitions", 27, 300 },
            { "PROJ_MORTAR", "Projectiles and Mortars", 50, 375 },
            { "MISSILE_ROCKET", "Missiles and Rockets", 100, 475 },
            { "LARGE_GUIDED_MISSILE", "Large and Guided Missiles", 200, 600 },
            { "BOMB_DISPENSER", "Bombs and Dispensers", 300, 675 },
            { "STOCKPILE", "Stockpiles of EO or UXO", 500, 800 },
    };

    @Test
    public void provenance() {
        assertEquals("ATP4-32.2", UxoTable.SOURCE_ID);
        assertEquals("2024-03-01", UxoTable.EDITION);
        assertEquals("31", UxoTable.FIGURE);
        assertEquals("01 Mar 2024", UxoTable.EDITION_LABEL);
        assertTrue(UxoTable.isKnown("ATP4-32.2", "2024-03-01"));
        assertFalse(UxoTable.isKnown("ATP4-32.2", "2020-03-12"));
        assertFalse(UxoTable.isKnown("OTHER", "2024-03-01"));
    }

    @Test
    public void figure31() {
        List<UxoTable.Category> cats = UxoTable.categories();
        assertEquals(FIG31.length, cats.size());
        for (int i = 0; i < FIG31.length; i++) {
            UxoTable.Category c = cats.get(i);
            assertEquals(FIG31[i][0], c.id);
            assertEquals(FIG31[i][1], c.doctrineLabel);
            assertEquals((int) (Integer) FIG31[i][2], c.maxWeightLb);
            assertEquals((int) (Integer) FIG31[i][3], c.cordonM);
            assertEquals(c.cordonM, UxoTable.cordonM(c.id, false, false));
            // "extended by 50 meters or more" for more than one item.
            assertEquals(c.cordonM + 50, UxoTable.cordonM(c.id, true, false));
            // "large" only means something for Unknown.
            assertEquals(c.cordonM, UxoTable.cordonM(c.id, false, true));
        }
    }

    @Test
    public void figure31MatchesTable1Rows() {
        // Fig. 31 is Table 1 applied to typical items.
        for (UxoTable.Category c : UxoTable.categories())
            assertEquals(c.id, c.cordonM, UxoTable.table1M(c.maxWeightLb));
    }

    @Test
    public void minimumAndUnknown() {
        assertEquals(300, UxoTable.MIN_CORDON_M);
        assertEquals(50, UxoTable.MULTI_ADD_M);
        assertEquals(300, UxoTable.cordonM("UNKNOWN", false, false));
        assertEquals(675, UxoTable.cordonM("UNKNOWN", false, true));
        assertEquals(350, UxoTable.cordonM("UNKNOWN", true, false));
        assertEquals(725, UxoTable.cordonM("UNKNOWN", true, true));
        assertEquals(-1, UxoTable.cordonM("NOPE", false, false));
        assertEquals(-1, UxoTable.cordonM(null, false, false));
        for (UxoTable.Category c : UxoTable.pickerCategories())
            assertTrue(UxoTable.cordonM(c.id, false, false)
                    >= UxoTable.MIN_CORDON_M);
    }

    @Test
    public void table1() {
        int[][] expected = { { 27, 300 }, { 30, 310 }, { 35, 330 },
                { 40, 350 }, { 45, 360 }, { 50, 375 }, { 100, 475 },
                { 150, 550 }, { 200, 600 }, { 300, 675 }, { 400, 725 },
                { 500, 800 } };
        assertEquals(expected.length, UxoTable.TABLE1.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i][0], UxoTable.TABLE1[i][0]);
            assertEquals(expected[i][1], UxoTable.TABLE1[i][1]);
        }
        // No 250 lb row in the 2024 edition (FM 4-30.51 had one).
        for (int[] r : UxoTable.TABLE1)
            assertFalse(r[0] == 250);
        // Next row at or above; never below.
        assertEquals(300, UxoTable.table1M(1));
        assertEquals(300, UxoTable.table1M(27));
        assertEquals(310, UxoTable.table1M(27.1));
        assertEquals(675, UxoTable.table1M(250));
        assertEquals(800, UxoTable.table1M(500));
        assertEquals(-1, UxoTable.table1M(500.1));
        assertEquals(-1, UxoTable.table1M(0));
        assertEquals(-1, UxoTable.table1M(Double.NaN));
    }

    @Test
    public void lookup() {
        Set<String> ids = new HashSet<>();
        for (UxoTable.Category c : UxoTable.pickerCategories()) {
            assertTrue(ids.add(c.id));
            assertNotNull(UxoTable.category(c.id));
            assertFalse(c.plainLabel.isEmpty());
        }
        assertEquals(7, ids.size());
        assertNull(UxoTable.category("TRUCK"));
        assertTrue(UxoTable.category("UNKNOWN").isUnknown());
    }

    @Test
    public void radioRule() {
        // Larger of ATP 4-32.2 (30 m) and UNMAS (50 m); inside every cordon.
        assertEquals(50, UxoTable.RADIO_MIN_M);
        assertTrue(UxoTable.RADIO_MIN_M < UxoTable.MIN_CORDON_M);
    }
}
