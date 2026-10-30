package com.atakmap.android.blast.plugin.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

/**
 * Golden values: every number on the DHS/DOJ Bomb Threat Stand-Off Card,
 * "As of Aug 2025" (CISA OBP / FBI Counter-IED Unit). If a value here
 * changes, the source changed: add a new edition, don't edit this one.
 */
public class StandoffTableGoldenTest {

    private static final StandoffTable T = StandoffTable.CISA_2025_08;

    /** {id, label, capacity lb, mandatory ft, SIP from, SIP to, preferred ft} */
    private static final Object[][] CARD = {
            { "PIPE_BOMB", "Pipe Bomb", 5, 70, 71, 1199, 1200 },
            { "SUICIDE_BOMBER", "Suicide Bomber", 20, 110, 111, 1699, 1700 },
            { "BRIEFCASE", "Briefcase/Suitcase", 50, 150, 151, 1849, 1850 },
            { "CAR", "Car", 500, 320, 321, 1899, 1900 },
            { "SUV_VAN", "SUV/Van", 1000, 400, 401, 2399, 2400 },
            { "SMALL_TRUCK", "Small Delivery Truck", 4000, 640, 641, 3799, 3800 },
            { "CONTAINER_TRUCK", "Container/Water Truck", 10000, 860, 861, 5099, 5100 },
            { "SEMI_TRAILER", "Semi-Trailer", 60000, 1570, 1571, 9299, 9300 },
    };

    @Test
    public void provenance() {
        assertEquals("CISA-BTSOC", T.sourceId);
        assertEquals("2025-08", T.edition);
        assertEquals("Aug 2025", T.editionLabel());
        assertEquals("Explosives capacity", T.quantityHeading);
        assertSame(T, StandoffTable.find("CISA-BTSOC", "2025-08"));
        assertSame(T, StandoffTable.current());
        assertNull(StandoffTable.find("CISA-BTSOC", "2020-01"));
        assertNull(StandoffTable.find("OTHER", "2025-08"));
    }

    @Test
    public void everyPublishedValue() {
        List<StandoffRow> rows = T.rows();
        assertEquals(CARD.length, rows.size());
        for (int i = 0; i < CARD.length; i++) {
            Object[] c = CARD[i];
            StandoffRow r = rows.get(i);
            String at = "row " + c[0];
            assertEquals(at, c[0], r.id);
            assertEquals(at, c[1], r.label);
            assertEquals(at, (int) (Integer) c[2], r.capacityLb);
            assertEquals(at, (int) (Integer) c[3], r.mandatoryFt);
            assertEquals(at, (int) (Integer) c[4], r.shelterFromFt);
            assertEquals(at, (int) (Integer) c[5], r.shelterToFt);
            assertEquals(at, (int) (Integer) c[6], r.preferredFt);
            assertSame(r, T.row((String) c[0]));
        }
    }

    @Test
    public void rowsAreConsistentAndMonotonic() {
        StandoffRow prev = null;
        for (StandoffRow r : T.rows()) {
            assertTrue(r.toString(), r.isConsistent());
            if (prev != null) {
                assertTrue(r.capacityLb > prev.capacityLb);
                assertTrue(r.mandatoryFt > prev.mandatoryFt);
                assertTrue(r.preferredFt > prev.preferredFt);
            }
            prev = r;
        }
        assertEquals("SEMI_TRAILER", T.largest().id);
    }

    @Test
    public void rowIdsAreUnique() {
        java.util.Set<String> ids = new java.util.HashSet<>();
        for (StandoffRow r : T.rows())
            assertTrue(ids.add(r.id));
        assertNotNull(T.row("CAR"));
        assertNull(T.row("TRUCK"));
        assertNull(T.row(null));
    }
}
