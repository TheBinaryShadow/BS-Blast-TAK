package com.atakmap.android.blast.plugin.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;



import org.w3c.dom.Document;

public class UxoExportsTest {

    private static UxoRecord rec() {
        return UxoRecord.create("BOMB_DISPENSER", true, false,
                UxoRecord.Where.PATH, UxoRecord.Placement.MAP, "A & B <x>",
                1793544725000L).withNumber(4);
    }

    private static Document parse(String xml) throws Exception {
        return SafeXml.parse(xml, false);
    }

    @Test
    public void kmlIsWellFormedAndOutsideTheCircle() throws Exception {
        UxoRecord r = rec();
        String kml = Exports.kmlUxo("UXO 4 Bomb", 50.1, 8.6, r);
        Document d = parse(kml);
        assertEquals(2, d.getElementsByTagName("Placemark").getLength());
        assertTrue(kml.contains("ff0051e6"));
        // Ring vertices circumscribe the 725 m circle.
        String coords = d.getElementsByTagName("coordinates").item(0)
                .getTextContent().trim();
        String[] pts = coords.split(" ");
        assertEquals(Geo.RING_VERTICES + 1, pts.length);
        for (String p : pts) {
            String[] c = p.split(",");
            double dist = Geo.distance(50.1, 8.6, Double.parseDouble(c[1]),
                    Double.parseDouble(c[0]));
            assertTrue(dist >= 725 - 0.01);
        }
        assertTrue(Exports.kmzUxo("x", 50.1, 8.6, r).containsKey("doc.kml"));
    }

    @Test
    public void gpxIsWellFormed() throws Exception {
        String gpx = Exports.gpxUxo("UXO 4 Bomb", 50.1, 8.6, rec(),
                1793544725000L);
        Document d = parse(gpx);
        assertEquals(1, d.getElementsByTagName("wpt").getLength());
        assertEquals(1, d.getElementsByTagName("trk").getLength());
        assertEquals(Geo.RING_VERTICES + 1,
                d.getElementsByTagName("trkpt").getLength());
        assertTrue(gpx.contains("CORDON 725 m"));
        assertTrue(gpx.contains("A &amp; B &lt;x&gt;"));
    }
}
