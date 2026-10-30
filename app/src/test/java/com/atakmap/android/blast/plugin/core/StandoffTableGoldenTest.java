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

    /**
     * {id, label, capacity lb, mandatory ft, SIP from, SIP to, preferred ft,
     *  kg, mandatory m, SIP from m, SIP to m, preferred m}
     * Feet: CISA card (Aug 2025) = ERG2024 p. 367. Metres: ERG2024 p. 367.
     */
    private static final Object[][] CARD = {
            { "PIPE_BOMB", "Pipe Bomb", 5, 70, 71, 1199, 1200, 2.3, 21, 22, 365, 366 },
            { "SUICIDE_BOMBER", "Suicide Bomber", 20, 110, 111, 1699, 1700, 9.0, 34, 35, 518, 519 },
            { "BRIEFCASE", "Briefcase/Suitcase", 50, 150, 151, 1849, 1850, 23.0, 46, 47, 563, 564 },
            { "CAR", "Car", 500, 320, 321, 1899, 1900, 227.0, 98, 99, 579, 580 },
            { "SUV_VAN", "SUV/Van", 1000, 400, 401, 2399, 2400, 454.0, 122, 123, 731, 732 },
            { "SMALL_TRUCK", "Small Delivery Truck", 4000, 640, 641, 3799, 3800, 1814.0, 195, 196, 1158, 1159 },
            { "CONTAINER_TRUCK", "Container/Water Truck", 10000, 860, 861, 5099, 5100, 4536.0, 263, 264, 1554, 1555 },
            { "SEMI_TRAILER", "Semi-Trailer", 60000, 1570, 1571, 9299, 9300, 27216.0, 475, 476, 2834, 2835 },
    };

    @Test
    public void provenance() {
        assertEquals("CISA-BTSOC", T.sourceId);
        assertEquals("2025-08", T.edition);
        assertEquals("Aug 2025", T.editionLabel());
        assertEquals("Explosives capacity (TNT equivalent)", T.quantityHeading);
        assertEquals("ERG2024 p. 367", StandoffTable.METRIC_SOURCE);
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
            assertEquals(at, (Double) c[7], r.capacityKg, 0);
            assertEquals(at, (int) (Integer) c[8], r.mandatoryM);
            assertEquals(at, (int) (Integer) c[9], r.shelterFromM);
            assertEquals(at, (int) (Integer) c[10], r.shelterToM);
            assertEquals(at, (int) (Integer) c[11], r.preferredM);
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

    @Test
    public void publishedMetresAgreeWithFeet() {
        // ERG metres are within 1 m of the converted feet, except
        // Semi-Trailer mandatory (475 m vs 478.5 m from 1,570 ft).
        for (StandoffRow r : T.rows()) {
            String at = r.id;
            if (!r.id.equals("SEMI_TRAILER"))
                assertTrue(at, Math.abs(Formats.ftToM(r.mandatoryFt)
                        - r.mandatoryM) < 1.0);
            assertTrue(at, Math.abs(Formats.ftToM(r.preferredFt)
                    - r.preferredM) < 1.0);
            assertEquals(at, r.mandatoryM + 1, r.shelterFromM);
            assertEquals(at, r.preferredM - 1, r.shelterToM);
            // Drawn radii are never smaller than either published value.
            assertTrue(at, r.mandatoryRadiusM() >= Formats.ftToM(r.mandatoryFt));
            assertTrue(at, r.mandatoryRadiusM() >= r.mandatoryM);
            assertTrue(at, r.preferredRadiusM() >= Formats.ftToM(r.preferredFt));
            assertTrue(at, r.preferredRadiusM() >= r.preferredM);
        }
        StandoffRow semi = T.row("SEMI_TRAILER");
        assertEquals(475, semi.mandatoryM);
        assertEquals(478.536, semi.mandatoryRadiusM(), 1e-9);
    }
}
