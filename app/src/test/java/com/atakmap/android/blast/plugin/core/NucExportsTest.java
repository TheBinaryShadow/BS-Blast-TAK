package com.atakmap.android.blast.plugin.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import javax.xml.parsers.DocumentBuilderFactory;

public class NucExportsTest {

    private static int count(String s, String sub) {
        int n = 0;
        for (int i = s.indexOf(sub); i >= 0; i = s.indexOf(sub, i + 1))
            n++;
        return n;
    }

    private static void parses(String xml) throws Exception {
        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
    }

    @Test
    public void kmlHasRingsFalloutAndPoint() throws Exception {
        NucRecord r = NucRecord.create(10, NucModel.Burst.SURFACE, "", 270, 15,
                "A & B", 1793544725000L).withNumber(1);
        String kml = Exports.kmlNuc("NUC 1 <10 kt>", 38.9, -77.0, r);
        parses(kml);
        assertEquals(NucText.Ring.values().length
                + NucModel.FALLOUT_RATES.length, count(kml, "<Polygon>"));
        assertEquals(1, count(kml, "<Point>"));
        assertEquals("ff1c1cb7", Exports.kmlColor(0xFFB71C1C));
        // Fallout goes east of ground zero (wind from the west).
        double[][] fo = NucGeo.fallout(r, 10, 38.9, -77.0);
        double maxLon = -180;
        for (double[] p : fo)
            maxLon = Math.max(maxLon, p[1]);
        assertTrue(maxLon > -77.0 + 1.0);
        String air = Exports.kmlNuc("x", 38.9, -77.0, NucRecord.create(10,
                NucModel.Burst.AIR, "", 270, 15, "", 0));
        assertEquals(NucText.Ring.values().length, count(air, "<Polygon>"));
    }

    @Test
    public void gpxHasTracks() throws Exception {
        NucRecord r = NucRecord.create(100, NucModel.Burst.UNKNOWN, "", 90, 20,
                "A", 1793544725000L);
        String gpx = Exports.gpxNuc("NUC 2", 51.5, -0.1, r, 1793544725000L);
        parses(gpx);
        assertEquals(NucText.Ring.values().length
                + NucModel.FALLOUT_RATES.length, count(gpx, "<trk>"));
        assertEquals(1, count(gpx, "<wpt "));
    }
}
