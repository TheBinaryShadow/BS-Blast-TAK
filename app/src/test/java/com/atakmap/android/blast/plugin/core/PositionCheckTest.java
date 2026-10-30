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
            assertEquals(Zone.MANDATORY,
                    PositionCheck.zoneOf(r, ft(r.mandatoryFt)));
            assertEquals(Zone.SHELTER,
                    PositionCheck.zoneOf(r, ft(r.mandatoryFt + 0.5)));
            assertEquals(Zone.SHELTER,
                    PositionCheck.zoneOf(r, ft(r.shelterFromFt)));
            assertEquals(Zone.SHELTER,
                    PositionCheck.zoneOf(r, ft(r.shelterToFt)));
            assertEquals(Zone.SHELTER,
                    PositionCheck.zoneOf(r, ft(r.preferredFt - 0.01)));
            assertEquals(Zone.PREFERRED,
                    PositionCheck.zoneOf(r, ft(r.preferredFt)));
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
        assertEquals("Move about 1,950 ft farther, to at least 2,400 ft "
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
        PositionCheck.Result r = PositionCheck.check(SUV, ft(2400) + 10, 15);
        assertEquals(Zone.PREFERRED, r.zone);
        assertTrue(r.nearBoundary);
        assertTrue(r.boundaryWarning().contains("Move farther"));
        // Default uncertainty when unknown.
        assertTrue(PositionCheck.check(SUV, ft(2400) + 10, Double.NaN)
                .nearBoundary);
        assertFalse(PositionCheck.check(SUV, ft(2400) + 30, Double.NaN)
                .nearBoundary);
        // Inside the mandatory zone there's nothing worse.
        assertFalse(PositionCheck.check(SUV, 5, 50).nearBoundary);
    }

    @Test
    public void countsPerZone() {
        double[] d = { ft(10), ft(400), ft(401), ft(2399), ft(2400), ft(9999),
                Double.NaN };
        assertArrayEquals(new int[] { 2, 2, 2 }, PositionCheck.count(SUV, d));
        assertArrayEquals(new int[] { 0, 0, 0 },
                PositionCheck.count(SUV, null));
    }
}
