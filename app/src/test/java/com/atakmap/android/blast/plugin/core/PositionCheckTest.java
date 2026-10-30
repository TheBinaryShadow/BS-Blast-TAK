package com.atakmap.android.blast.plugin.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.atakmap.android.blast.plugin.core.PositionCheck.Zone;

import org.junit.Test;

public class PositionCheckTest {

    private static final StandoffRow SUV = StandoffTable.CISA_2025_08
            .row("SUV_VAN");

    private static double ft(double f) {
        return Formats.ftToM(f);
    }

    @Test
    public void boundariesFollowTheCard() {
        for (StandoffRow r : StandoffTable.CISA_2025_08.rows()) {
            assertEquals(Zone.MANDATORY, PositionCheck.zoneOf(r, 0));
            // Zones follow the drawn rings: the larger of feet and the
            // published metres.
            assertEquals(Zone.MANDATORY,
                    PositionCheck.zoneOf(r, ft(r.mandatoryFt)));
            assertEquals(Zone.MANDATORY,
                    PositionCheck.zoneOf(r, r.mandatoryRadiusM()));
            assertEquals(Zone.SHELTER,
                    PositionCheck.zoneOf(r, r.mandatoryRadiusM() + 0.1));
            assertEquals(Zone.SHELTER, PositionCheck.zoneOf(r,
                    Math.max(ft(r.shelterFromFt), r.shelterFromM)));
            assertEquals(Zone.SHELTER,
                    PositionCheck.zoneOf(r, ft(r.shelterToFt)));
            assertEquals(Zone.SHELTER,
                    PositionCheck.zoneOf(r, r.preferredRadiusM() - 0.01));
            assertEquals(Zone.PREFERRED,
                    PositionCheck.zoneOf(r, r.preferredRadiusM()));
            assertEquals(Zone.PREFERRED,
                    PositionCheck.zoneOf(r, Math.max(ft(r.preferredFt),
                            r.preferredM)));
            assertEquals(Zone.PREFERRED,
                    PositionCheck.zoneOf(r, ft(r.preferredFt * 10)));
        }
        assertEquals(Zone.MANDATORY, PositionCheck.zoneOf(SUV, -3));
    }

    @Test
    public void shelterGuidance() {
        PositionCheck.Result res = PositionCheck.check(SUV, ft(450), 5);
        assertEquals(Zone.SHELTER, res.zone);
        assertEquals("450 ft (137 m) from the device: SHELTER-IN-PLACE zone.",
                res.headline());
        assertEquals("Move about 1,952 ft farther, to at least 2,400 ft "
                + "(732 m). If you can't, shelter inside a building away from "
                + "windows and exterior walls.", res.guidance());
        assertFalse(res.nearBoundary);
        assertNull(res.boundaryWarning());
    }

    @Test
    public void mandatoryAndPreferredGuidance() {
        assertTrue(PositionCheck.check(SUV, ft(100), 5).guidance()
                .startsWith("Evacuate now."));
        PositionCheck.Result p = PositionCheck.check(SUV, ft(3000), 5);
        assertEquals(Zone.PREFERRED, p.zone);
        assertEquals(0, p.toPreferredM, 0);
    }

    @Test
    public void uncertaintyFlagsTheSafeSideOfABoundary() {
        // 10 m beyond the preferred line with 15 m uncertainty.
        PositionCheck.Result r = PositionCheck.check(SUV, 732 + 10, 15);
        assertEquals(Zone.PREFERRED, r.zone);
        assertTrue(r.nearBoundary);
        assertTrue(r.boundaryWarning().contains("Move farther"));
        // Default uncertainty when unknown.
        assertTrue(PositionCheck.check(SUV, 732 + 10, Double.NaN)
                .nearBoundary);
        assertFalse(PositionCheck.check(SUV, 732 + 30, Double.NaN)
                .nearBoundary);
        // Inside the mandatory zone there's nothing worse.
        assertFalse(PositionCheck.check(SUV, 5, 50).nearBoundary);
    }

    @Test
    public void countsPerZone() {
        // SUV/Van preferred line is drawn at 732 m (ERG), just past 2,400 ft.
        double[] d = { ft(10), ft(400), ft(401), ft(2399), 732, ft(9999),
                Double.NaN };
        assertArrayEquals(new int[] { 2, 2, 2 }, PositionCheck.count(SUV, d));
        assertArrayEquals(new int[] { 0, 0, 0 },
                PositionCheck.count(SUV, null));
    }

    @Test
    public void carRingIsDrawnAtTheErgMetres() {
        // 320 ft = 97.5 m but ERG2024 p. 367 publishes 98 m: the larger is
        // drawn, so 321 ft (97.8 m) is still inside the mandatory ring.
        StandoffRow car = StandoffTable.CISA_2025_08.row("CAR");
        assertEquals(98.0, car.mandatoryRadiusM(), 0);
        assertEquals(Zone.MANDATORY, PositionCheck.zoneOf(car, ft(321)));
    }
}
