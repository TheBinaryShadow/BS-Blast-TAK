package com.atakmap.android.blast.plugin.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class ExportsTest {

    private static final long T0 = 1793385900000L; // 2026-10-30T18:45:00Z
    private static final StandoffTable T = StandoffTable.CISA_2025_08;

    private static Exports.Incident car(boolean cleared) {
        BlastRecord r = BlastRecord.create(ZoneCalculator.forRow(T, "CAR"),
                "VIPER", T0).withNumber(2);
        if (cleared)
            r = r.cleared("ROOK", T0 + 60000);
        return new Exports.Incident("uid-1", cleared ? "BLAST 2 Car CLEARED"
                : "BLAST 2 Car <Main & 5th>", 36.4, -79.93, r);
    }

    private static Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(
                new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }

    // ------------------------------------------------------------------
    // Geodesics against GeographicLib (WGS84)

    @Test
    public void directMatchesGeographicLib() {
        double[][] cases = {
                { 36.4, -79.93, 0, 98.0, 36.400883149889, -79.930000000000 },
                { 36.4, -79.93, 45, 564.0, 36.403593869897, -79.925554078348 },
                { 36.4, -79.93, 200, 2835.0, 36.375991945233, -79.940805600668 },
                { -33.87, 151.21, 90, 732.0, -33.869999746080, 151.217911351123 },
                { 64.84, -147.72, 315, 2835.0, 64.857974458823, -147.762269088390 },
                { 0.0, 0.0, 180, 21.336, -0.000192956272, 0.000000000000 },
                { 78.2, 15.6, 10, 2834.64, 78.225002801175, 15.621598447866 },
        };
        for (double[] c : cases) {
            double[] p = Geo.destination(c[0], c[1], c[2], c[3]);
            assertEquals(c[4], p[0], 1e-9); // ~0.1 mm
            assertEquals(c[5], p[1], 1e-9);
        }
    }

    @Test
    public void inverseMatchesGeographicLib() {
        assertEquals(713.446570, Geo.distance(36.4, -79.93, 36.405, -79.925), 1e-4);
        assertEquals(343492.815400, Geo.distance(51.5, -0.12, 48.85, 2.35), 1e-3);
        assertEquals(1444.477808, Geo.distance(-33.87, 151.21, -33.86, 151.22), 1e-4);
        assertEquals(0, Geo.distance(10, 10, 10, 10), 0);
    }

    @Test
    public void ringOutlineIsNeverInsideTheCircle() {
        for (StandoffRow row : T.rows()) {
            double r = row.preferredRadiusM();
            double[][] pts = Geo.ring(36.4, -79.93, r);
            assertEquals(Geo.RING_VERTICES + 1, pts.length);
            assertEquals(pts[0], pts[pts.length - 1]);
            for (int i = 0; i < Geo.RING_VERTICES; i++) {
                double[] a = pts[i], b = pts[i + 1];
                double va = Geo.distance(36.4, -79.93, a[0], a[1]);
                assertEquals(r / Math.cos(Math.PI / Geo.RING_VERTICES), va,
                        1e-4); // 0.1 mm
                // Edge midpoint (small rings: linear interpolation is fine)
                double mid = Geo.distance(36.4, -79.93, (a[0] + b[0]) / 2,
                        (a[1] + b[1]) / 2);
                assertTrue(row.id + " mid " + mid + " < " + r,
                        mid >= r * (1 - 1e-6));
            }
        }
    }

    // ------------------------------------------------------------------

    @Test
    public void kmlIsWellFormedAndComplete() throws Exception {
        String kml = Exports.kml(car(false));
        Document d = parse(kml);
        NodeList pm = d.getElementsByTagNameNS("http://www.opengis.net/kml/2.2",
                "Placemark");
        assertEquals(3, pm.getLength());
        NodeList rings = d.getElementsByTagNameNS(
                "http://www.opengis.net/kml/2.2", "LinearRing");
        assertEquals(3, rings.getLength()); // preferred outer, hole, mandatory
        String coords = rings.item(0).getTextContent().trim();
        assertEquals(Geo.RING_VERTICES + 1, coords.split(" ").length);
        assertTrue(kml.contains("BLAST 2 Car &lt;Main &amp; 5th&gt;"));
        assertTrue(kml.contains("ff0000d5"));
        assertTrue(kml.contains("MANDATORY EVAC 320 ft (98 m)"));
        assertTrue(kml.contains("CISA/FBI Bomb Threat Stand-Off Card"));
        assertTrue(Exports.kml(car(true)).contains("ff9e9e9e"));
        assertFalse(kml.contains("%"));
    }

    @Test
    public void gpxIsWellFormedAndComplete() throws Exception {
        String gpx = Exports.gpx(car(false), T0);
        Document d = parse(gpx);
        String ns = "http://www.topografix.com/GPX/1/1";
        assertEquals("1.1", d.getDocumentElement().getAttribute("version"));
        assertEquals(1, d.getElementsByTagNameNS(ns, "wpt").getLength());
        assertEquals(2, d.getElementsByTagNameNS(ns, "trk").getLength());
        assertEquals(2 * (Geo.RING_VERTICES + 1),
                d.getElementsByTagNameNS(ns, "trkpt").getLength());
        assertTrue(gpx.contains("<sym>Danger Area</sym>"));
        assertTrue(gpx.contains("<wpt lat=\"36.4000000\" lon=\"-79.9300000\">"));
        assertTrue(gpx.contains("PREF 580 m"));
    }

    @Test
    public void dataPackageZip() throws Exception {
        Map<String, String> cot = new LinkedHashMap<>();
        cot.put("uid-1", "<event uid=\"uid-1\"/>");
        cot.put("uid-1.pref", "<event uid=\"uid-1.pref\"/>");
        cot.put("uid-1.mand", "<event uid=\"uid-1.mand\"/>");
        Map<String, byte[]> files = Exports.dataPackage("pkg-uid",
                "BLAST 2 Car", cot);
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        Exports.zip(files, bo);
        ZipInputStream zin = new ZipInputStream(
                new ByteArrayInputStream(bo.toByteArray()));
        Map<String, String> read = new LinkedHashMap<>();
        ZipEntry e;
        while ((e = zin.getNextEntry()) != null) {
            ByteArrayOutputStream x = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n;
            while ((n = zin.read(buf)) > 0)
                x.write(buf, 0, n);
            read.put(e.getName(), x.toString("UTF-8"));
        }
        assertEquals(4, read.size());
        assertTrue(read.containsKey("MANIFEST/manifest.xml"));
        assertTrue(read.containsKey("uid-1/uid-1.cot"));
        assertTrue(read.containsKey("uid-1.pref/uid-1.pref.cot"));
        Document m = parse(read.get("MANIFEST/manifest.xml"));
        assertEquals("2", m.getDocumentElement().getAttribute("version"));
        assertEquals(3, m.getElementsByTagName("Content").getLength());
        assertEquals("uid-1.mand/uid-1.mand.cot",
                ((org.w3c.dom.Element) m.getElementsByTagName("Content").item(2))
                        .getAttribute("zipEntry"));
        assertNotNull(read.get("uid-1/uid-1.cot"));
        assertEquals("a_b_c/a_b_c.cot", Exports.cotEntry("a/b\\c"));
    }

    @Test
    public void fileStems() {
        assertEquals("BLAST_2_Car_Main_5th_301845Z",
                Exports.fileStem("BLAST 2 Car <Main & 5th> CLEARED", T0));
        assertEquals("BLAST_301845Z", Exports.fileStem("///", T0));
        assertEquals("BLAST_301845Z", Exports.fileStem(null, T0));
    }
}
