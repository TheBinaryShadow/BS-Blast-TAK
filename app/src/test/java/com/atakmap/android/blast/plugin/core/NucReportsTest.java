package com.atakmap.android.blast.plugin.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NucReportsTest {

    private static NucRecord mil() {
        // 16 kt, effective downwind 090 at 16 km/h (FM 3-3-1 example).
        NucRecord r = NucRecord.create(16, NucModel.Burst.SURFACE, "", 270,
                16 / NucRecord.KM_PER_MI, "ALPHA-1", 1600610700000L);
        return r.withMil(r.mil.withOn(true));
    }

    @Test
    public void militaryPictureReplacesCivilContours() {
        NucRecord r = mil();
        assertTrue(r.drawsMilitary());
        assertFalse(r.drawsFallout());
        NucMil.Prediction p = r.militaryPrediction();
        assertEquals(90, p.eddDeg, 1e-9);
        assertEquals(16, p.ewsKph, 1e-9);
        assertEquals(18, Math.round(p.zoneIKm));
        // 10 km east: Zone I, arrival ~H+0.6.
        NucText.Where w = NucText.where(r, 10000, 0);
        assertEquals(1, w.milZone);
        assertEquals(10.0 / 16, w.arrivalH, 1e-6);
        assertEquals(2, NucText.where(r, 30000, 0).milZone);
        assertEquals(0, NucText.where(r, 0, 30000).milZone);
        // Air bursts get no prediction.
        NucRecord air = NucRecord.create(16, NucModel.Burst.AIR, "", 270, 10,
                "", 0);
        assertNull(air.withMil(air.mil.withOn(true)).militaryPrediction());
        // Codec keeps the military settings and observations.
        NucRecord o = r.withObs(new NucRecord.Obs(38.9, -77.0, 45, 20, 16,
                "CRATER", "OBS", "MSL", "MSL", "EE"));
        NucRecord d = NucCodec.fromXml(NucCodec.toXml(o));
        assertTrue(d.mil.on);
        assertEquals(45, d.obs.azDeg, 0);
        assertEquals(20, d.obs.flashToBangS, 0);
        assertEquals("CRATER", d.obs.crater);
        assertEquals("MSL", d.obs.golfDelivery);
        assertEquals(38.9, d.obs.obsLat, 1e-9);
        assertEquals(NucCodec.toXml(o), NucCodec.toXml(d));
    }

    @Test
    public void edmLines() {
        NucRecord r = mil().withEdm(NucMil.parseEdm("090016060"));
        assertTrue(r.mil.edmWind);
        assertEquals(60, r.militaryPrediction().angleDeg, 0);
        assertEquals(90, r.militaryPrediction().eddDeg, 1e-6);
        NucRecord c = mil().withEdm(NucMil.parseEdm("007"));
        assertFalse(c.hasWind());
        assertTrue(c.militaryPrediction().circular);
        assertEquals(7, c.militaryPrediction().zoneIKm, 0);
        NucRecord d = NucCodec.fromXml(NucCodec.toXml(c));
        assertEquals(7, d.mil.edmZoneIKm, 0);
        assertTrue(d.drawsMilitary());
        // A new surface wind clears the EDM values.
        NucRecord w = c.withWind(270, 10);
        assertTrue(Double.isNaN(w.mil.edmZoneIKm));
        assertFalse(w.mil.edmWind);
    }

    @Test
    public void nbc3Papab() {
        NucMil.Prediction p = mil().militaryPrediction();
        assertEquals("PAPAB/016KPH/018KM/05KM/070DGT/110DGT//",
                NucReports.papab(p));
        assertEquals("PAPAB/007KM//",
                NucReports.papab(NucMil.circular(4, 7)));
        String s = NucReports.nbc3(mil(), p, "32U NB 0580 6400");
        assertTrue(s.contains("FOXTROT/32UNB05806400/EE//"));
        assertTrue(s.contains("DELTA/201405ZSEP2020//"));
    }

    @Test
    public void nbc1Report() {
        NucRecord r = mil().withObs(new NucRecord.Obs(Double.NaN, Double.NaN,
                45, 20, 16, "", "SUS", "UNK", "UNK", "EE"));
        String s = NucReports.nbc1(r, "32U NB 0620 6340", "32U NB 0580 6400");
        String[] l = s.split("\n");
        assertEquals("NBC1 NUC", l[0]);
        assertEquals("BRAVO/32UNB06206340/045DGT//", l[1]);
        assertEquals("DELTA/201405ZSEP2020//", l[2]);
        assertEquals("FOXTROT/32UNB05806400/EE//", l[3]);
        assertEquals("GOLF/SUS/UNK/1/UNK/1//", l[4]);
        assertEquals("HOTEL/SURF//", l[5]);
        assertEquals("JULIET/20//", l[6]);
        assertEquals("LIMA/016DEG//", l[7]);
        assertEquals("YANKEE/090DGT/016KPH//", l[8]);
        assertTrue(l[9].startsWith("GENTEXT/GZ ESTIMATED BY POLAR PLOT"));
        assertTrue(l[9].contains("7.0 KM"));
        assertTrue(NucReports.nbc1Missing(r, "x").isEmpty());
        assertEquals(1, NucReports.nbc1Missing(r, "").size());
        assertNotNull(NucReports.summary(mil().militaryPrediction()));
    }
}
