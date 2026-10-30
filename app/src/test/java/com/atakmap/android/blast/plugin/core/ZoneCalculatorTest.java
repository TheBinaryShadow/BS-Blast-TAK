package com.atakmap.android.blast.plugin.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.atakmap.android.blast.plugin.core.ZoneCalculator.MassUnit;
import com.atakmap.android.blast.plugin.core.ZoneCalculator.Selection;
import com.atakmap.android.blast.plugin.core.ZoneCalculator.Status;

import org.junit.Test;

public class ZoneCalculatorTest {

    private static final StandoffTable T = StandoffTable.CISA_2025_08;

    private static Selection lb(double v) {
        return ZoneCalculator.forQuantity(T, v, MassUnit.LB);
    }

    @Test
    public void exactCapacitiesUseThatRow() {
        for (StandoffRow r : T.rows()) {
            Selection s = lb(r.capacityLb);
            assertEquals(r.id, s.row.id);
            assertEquals(Status.EXACT, s.status);
        }
    }

    @Test
    public void neverRoundsDown() {
        // Just above each row must go to the next row; just below stays.
        java.util.List<StandoffRow> rows = T.rows();
        for (int i = 0; i < rows.size(); i++) {
            StandoffRow r = rows.get(i);
            assertEquals(r.id, lb(r.capacityLb - 0.001).row.id);
            Selection above = lb(r.capacityLb + 0.001);
            if (i + 1 < rows.size()) {
                assertEquals(rows.get(i + 1).id, above.row.id);
                assertEquals(Status.NEXT_ROW_UP, above.status);
            } else {
                assertEquals(Status.ABOVE_TABLE, above.status);
                assertNull(above.row);
            }
        }
    }

    @Test
    public void everyQuantityGetsARowAtOrAboveIt() {
        for (double q = 0.1; q <= 60000; q *= 1.07) {
            Selection s = lb(q);
            assertTrue(s.isUsable());
            assertTrue("q=" + q, s.row.capacityLb >= q);
            // And it's the smallest such row.
            for (StandoffRow r : T.rows())
                if (r.capacityLb >= q)
                    assertTrue(r.capacityLb >= s.row.capacityLb);
        }
    }

    @Test
    public void typicalEstimates() {
        assertEquals("PIPE_BOMB", lb(1).row.id);
        assertEquals("SUICIDE_BOMBER", lb(10).row.id);
        assertEquals("BRIEFCASE", lb(21).row.id);
        assertEquals("CAR", lb(51).row.id);
        assertEquals("SUV_VAN", lb(800).row.id);
        assertEquals("SMALL_TRUCK", lb(1001).row.id);
        assertEquals("CONTAINER_TRUCK", lb(5000).row.id);
        assertEquals("SEMI_TRAILER", lb(40000).row.id);
    }

    @Test
    public void kilograms() {
        // 2.3 kg = 5.07 lb: above the pipe bomb row, so Suicide Bomber.
        Selection s = ZoneCalculator.forQuantity(T, 2.3, MassUnit.KG);
        assertEquals("SUICIDE_BOMBER", s.row.id);
        assertEquals(Status.NEXT_ROW_UP, s.status);
        // 2.26 kg = 4.98 lb: pipe bomb.
        assertEquals("PIPE_BOMB",
                ZoneCalculator.forQuantity(T, 2.26, MassUnit.KG).row.id);
        // 300 kg = 661.4 lb: SUV/Van.
        assertEquals("SUV_VAN",
                ZoneCalculator.forQuantity(T, 300, MassUnit.KG).row.id);
        // 27,216 kg ≈ 60,001 lb: above the card.
        assertEquals(Status.ABOVE_TABLE,
                ZoneCalculator.forQuantity(T, 27216, MassUnit.KG).status);
        assertTrue(ZoneCalculator.forQuantity(T, 300, MassUnit.KG).explain()
                .contains("300 kg (661.4 lb)"));
    }

    @Test
    public void aboveTableDrawsNothingAndSaysSo() {
        Selection s = lb(60000.5);
        assertFalse(s.isUsable());
        assertEquals(Status.ABOVE_TABLE, s.status);
        String e = s.explain();
        assertTrue(e, e.contains("60,000 lb"));
        assertTrue(e, e.contains("no zone is drawn"));
        assertTrue(e, e.contains("9,300 ft+"));
    }

    @Test
    public void invalidInput() {
        assertEquals(Status.INVALID, lb(0).status);
        assertEquals(Status.INVALID, lb(-5).status);
        assertEquals(Status.INVALID, lb(Double.NaN).status);
        assertEquals(Status.INVALID, lb(Double.POSITIVE_INFINITY).status);
        assertFalse(lb(0).isUsable());
        assertEquals(Status.INVALID, ZoneCalculator.forRow(T, "NOPE").status);
    }

    @Test
    public void explanations() {
        assertEquals("Using the SUV/Van row (1,000 lb), the next row at or "
                + "above your 800 lb estimate. The card is never scaled down.",
                lb(800).explain());
        assertEquals("Your 500 lb estimate matches the Car row (500 lb).",
                lb(500).explain());
        assertEquals("Car row (500 lb).",
                ZoneCalculator.forRow(T, "CAR").explain());
    }

    @Test
    public void parseQuantity() {
        assertEquals(800, ZoneCalculator.parseQuantity(" 800 "), 0);
        assertEquals(1000, ZoneCalculator.parseQuantity("1,000"), 0);
        assertEquals(2.5, ZoneCalculator.parseQuantity("2.5"), 0);
        assertTrue(Double.isNaN(ZoneCalculator.parseQuantity("")));
        assertTrue(Double.isNaN(ZoneCalculator.parseQuantity("abc")));
        assertTrue(Double.isNaN(ZoneCalculator.parseQuantity(null)));
    }
}
